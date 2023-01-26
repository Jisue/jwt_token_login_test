package com.example.token_login.jwt;

import com.example.token_login.domain.Auth;
import com.example.token_login.domain.User;
import com.example.token_login.repository.AuthRepository;
import com.example.token_login.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Service
@RequiredArgsConstructor
public class JwtService {

    private final Logger logger = LoggerFactory.getLogger(JwtService.class);

    private final UserRepository userRepository;

    private final AuthRepository authRepository;

    private final JwtTokenProvider jwtTokenProvider;

    public boolean checkValidationToken(HttpServletRequest request, HttpServletResponse response) throws Exception {
        String token = request.getHeader("access_token");

        if(!jwtTokenProvider.validateToken(token)) {
            return false;
        }

        Long userId = jwtTokenProvider.getUerIdFromJWT(token);

        User findUser = userRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다."));

        Auth findAuth = authRepository.findByUserId(findUser)
                .orElseThrow(() -> new IllegalArgumentException("회원의 token 정보를 찾을 수 없습니다."));

        if(!findAuth.getAccessToken().equals(token)) {
            logger.info("auth 토큰값이 일치하지 않습니다.");
            return false;
        }

        return true;
    }
}
