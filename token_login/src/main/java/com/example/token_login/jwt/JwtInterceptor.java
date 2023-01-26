package com.example.token_login.jwt;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

// interceptor: DispatcherServlet -> 컨트롤러 요청 전,후 요청과 응답을 가로챔
@Component
@RequiredArgsConstructor
public class JwtInterceptor implements HandlerInterceptor {

    private final JwtService jwtService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)  throws Exception {
        // TODO Auto-generated method stub

        if(jwtService.checkValidationToken(request, response)) {
            return true;
        }

        response.setStatus(403);
        response.setCharacterEncoding("utf-8");
        response.setContentType("text/html; charset=UTF-8");
        response.getWriter().write("접근 권한이 없습니다.");
        return false;
    }
}
