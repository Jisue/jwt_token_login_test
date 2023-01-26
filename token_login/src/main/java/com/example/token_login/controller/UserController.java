package com.example.token_login.controller;

import com.example.token_login.domain.User;
import com.example.token_login.dto.AuthRequest;
import com.example.token_login.dto.TokenResponse;
import com.example.token_login.dto.UserResponse;
import com.example.token_login.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.HashMap;
import java.util.Map;

@RestController // 사용자 요청에 대한 서비스의 응답을 구현
@RequestMapping("/api")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    // 회원 가입
    @PostMapping("/sign-up")
    public ResponseEntity<UserResponse> signUp(@Valid @RequestBody User user) {
        return ResponseEntity.ok(userService.signUp(user));
    }

    // 로그인
    @PostMapping("/login")
    public ResponseEntity<TokenResponse> login(@Valid @RequestBody User user) throws Exception {
        return ResponseEntity.ok(userService.login(user));
    }

    // 로그인
    @PostMapping("/refresh")
    public ResponseEntity<TokenResponse> refresh(@Valid @RequestBody AuthRequest authRequest) throws Exception {
        return ResponseEntity.ok(userService.updateToken(authRequest));
    }

    // token test api
    @PostMapping("/test")
    public Map userResponseTest(@RequestHeader Map<String, Object> requestHeader) {
        Map<String, String> result = new HashMap<>();
        result.put("result", "success");
        return result;
    }
}
