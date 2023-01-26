package com.example.token_login.service;

import com.example.token_login.domain.Auth;
import com.example.token_login.domain.User;
import com.example.token_login.dto.AuthRequest;
import com.example.token_login.dto.TokenResponse;
import com.example.token_login.dto.UserResponse;
import com.example.token_login.jwt.JwtTokenProvider;
import com.example.token_login.repository.AuthRepository;
import com.example.token_login.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final Logger logger = LoggerFactory.getLogger(UserService.class);

    private final PasswordEncoder passwordEncoder;

    private final UserRepository userRepository;

    private final AuthRepository authRepository;

    private final JwtTokenProvider jwtTokenProvider;

    public UserResponse signUp(User user) {
        User encodedUser = User.builder()
                .userName(user.getUserName())
                .userPw(passwordEncoder.encode(user.getUserPw()))
                .nickName(user.getNickName())
                .build();

        User saveUser = userRepository.save(encodedUser);

        UserResponse userResponse = UserResponse.builder()
                .userId(saveUser.getUserId())
                .userName(saveUser.getUserName())
                .nickName(saveUser.getNickName())
                .build();

        return userResponse;
    }

    @Transactional
    public TokenResponse login(User user) throws Exception {

        User findUser = userRepository.findByUserId(user.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다."));

        if (!passwordEncoder.matches(user.getUserPw(), findUser.getUserPw())) {
            throw new Exception("비밀번호가 일치하지 않습니다.");
        }

        TokenResponse tokenSet = createNewToken(findUser);
        String accessToken = tokenSet.getAccessToken();
        String refreshToken = tokenSet.getRefreshToken();

        Auth auth = Auth.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .userId(user)
                .build();

        authRepository.save(auth);

        return TokenResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }


    @Transactional
    public TokenResponse updateToken(AuthRequest authRequest) throws Exception {

        Long userId = jwtTokenProvider.getUerIdFromJWT(authRequest.getAccessToken());

        User findUser = userRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다."));

        TokenResponse tokenSet = createNewToken(findUser);
        String accessToken = tokenSet.getAccessToken();
        String refreshToken = tokenSet.getRefreshToken();

        Auth findAuth = authRepository.findByUserId(findUser)
                .orElseThrow(() -> new IllegalArgumentException("회원의 token 정보를 찾을 수 없습니다."));

        if(!findAuth.getRefreshToken().equals(authRequest.getRefreshToken())) {
            logger.info("auth 토큰값이 일치하지 않습니다.");
            throw new Exception("refresh 토큰값이 잘못되었습니다.");
        }

        Auth auth = Auth.builder()
                .authId(findAuth.getAuthId())
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .userId(findUser)
                .build();
        authRepository.save(auth);

        return TokenResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }

    @Transactional
    public TokenResponse createNewToken(User user) {
        String accessToken = jwtTokenProvider.createAccessToken(user);
        String refreshToken = jwtTokenProvider.createRefreshToken(user);

        return TokenResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }
}
