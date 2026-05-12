package top.wjr.serenoj.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import top.wjr.serenoj.shiro.ShiroConstant;

import java.util.Date;


@Slf4j(topic = "serenoj")
@Data
@Component
@ConfigurationProperties(prefix = "serenoj.jwt")
public class JwtUtils {

    @Value("${serenoj.jwt.secret:serenoj-jwt-secret-key-2024}")
    private String secret;

    @Value("${serenoj.jwt.expire:86400}")
    private long expire;

    @Value("${serenoj.jwt.header:Authorization}")
    private String header;

    @Value("${serenoj.jwt.refresh-expire:43200}")
    private long checkRefreshExpire;

    @Autowired
    private RedisUtils redisUtils;

    public String generateToken(String userId) {
        Date nowDate = new Date();
        Date expireDate = new Date(nowDate.getTime() + expire * 1000);

        String token = Jwts.builder()
                .setHeaderParam("type", "JWT")
                .setSubject(userId)
                .setIssuedAt(nowDate)
                .setExpiration(expireDate)
                .signWith(SignatureAlgorithm.HS512, secret)
                .compact();
        redisUtils.set(ShiroConstant.SHIRO_TOKEN_KEY + userId, token, expire);
        redisUtils.set(ShiroConstant.SHIRO_TOKEN_REFRESH + userId, "1", checkRefreshExpire);
        return token;
    }

    public Claims getClaimByToken(String token) {
        try {
            return Jwts.parser()
                    .setSigningKey(secret)
                    .parseClaimsJws(token)
                    .getBody();
        } catch (Exception e) {
            log.debug("validate is token error ", e);
            return null;
        }
    }

    public void cleanToken(String uid) {
        redisUtils.del(ShiroConstant.SHIRO_TOKEN_KEY + uid, ShiroConstant.SHIRO_TOKEN_REFRESH + uid);
    }

    public boolean hasToken(String uid) {
        return redisUtils.hasKey(ShiroConstant.SHIRO_TOKEN_KEY + uid);
    }

    public boolean isTokenExpired(Date expiration) {
        return expiration.before(new Date());
    }
}