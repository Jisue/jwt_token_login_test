package com.example.token_login.jwt;

import com.example.token_login.domain.User;
import com.example.token_login.repository.UserRepository;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;

// 토큰 생성과 유효성 검증을 담당
@Component
public class JwtTokenProvider implements InitializingBean { // InitializingBean impl: Bean이 생성이 되고 의존성 주입받은 secret 값을 Base64 Decode해서 key변수에 할당하기 위해서 사용

    private final Logger logger = LoggerFactory.getLogger(JwtTokenProvider.class);

    private final String secret;
    private final String refresh;
    private final long accessTokenValidityTime;
    private final long refreshTokenValidityTime;

    private Key key;

    private Key refreshKey;

    private final UserRepository userRepository;

    // 의존성 주입
    public JwtTokenProvider(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.refresh}") String refresh,
            @Value("${jwt.access-token-validity-in-seconds}") long accessTokenValidityInSeconds,
            @Value("${jwt.refresh-token-validity-in-seconds}") long refreshTokenValidityInSeconds, UserRepository userRepository) {
        this.secret = secret;
        this.refresh = refresh;
        this.accessTokenValidityTime = accessTokenValidityInSeconds * 1000;
        this.refreshTokenValidityTime = refreshTokenValidityInSeconds * 1000;
        this.userRepository = userRepository;
    }

    // 객체 초기화, key -> Base64 인코딩
    @Override
    public void afterPropertiesSet() {
        byte[] keyBytes = Decoders.BASE64.decode(secret);
        byte[] refreshKeyBytes = Decoders.BASE64.decode(refresh);
        this.key = Keys.hmacShaKeyFor(keyBytes);
        this.refreshKey = Keys.hmacShaKeyFor(refreshKeyBytes);
    }

    // 토큰을 생성
    public String createAccessToken(User user) {
        Claims claims = Jwts.claims(); // JWT payload 에 저장되는 정보단위
        claims.put("userId", user.getUserId());
        claims.put("userName", user.getUserName());
        claims.put("nickName", user.getNickName());

        Date now = new Date();

        //토큰 빌더
        String jwt = Jwts.builder()
                .setClaims(claims) // 정보
                .setIssuedAt(now) // 토큰 발생 시간 정보
                .setExpiration(new Date(now.getTime() + accessTokenValidityTime)) // 만료 시간 설정
                .signWith(SignatureAlgorithm.HS512, key) // 사용할 알고리즘, signature 에 들어갈 secretKey 세팅
                .compact(); // 토큰 생성

        return jwt;
    }

    public String createRefreshToken(User user) {

        Date now = new Date();

        //토큰 빌더
        String jwt = Jwts.builder()
                .setIssuedAt(now) // 토큰 발생 시간 정보
                .setExpiration(new Date(now.getTime() + refreshTokenValidityTime)) // 만료 시간 설정
                .signWith(SignatureAlgorithm.HS512, refreshKey) // 사용할 알고리즘, signature 에 들어갈 secretKey 세팅
                .compact(); // 토큰 생성

        return jwt;
    }

    public Long getUerIdFromJWT(String token) {
        Claims claims = Jwts.parser()
                .setSigningKey(secret)
                .parseClaimsJws(token)
                .getBody();

        Long userId = Long.valueOf(String.valueOf(claims.get("userId")));

        return userId;
    }

    // token 파싱해서 유효성 검사
    public boolean validateToken(String token){
        try {
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
            return true;
        } catch (io.jsonwebtoken.security.SecurityException | MalformedJwtException e) {
            logger.info("잘못된 JWT 서명입니다.");
        } catch (ExpiredJwtException e) {
            logger.info("만료된 JWT 토큰입니다.");
        } catch (UnsupportedJwtException e) {
            logger.info("지원되지 않는 JWT 토큰입니다.");
        } catch (IllegalArgumentException e) {
            logger.info("JWT 토큰이 잘못되었습니다.");
        }

        return false;
    }
}
