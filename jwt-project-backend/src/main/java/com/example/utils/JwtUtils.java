package com.example.utils;

import ch.qos.logback.core.model.INamedModel;
import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.util.Calendar;
import java.util.Date;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Component
public class JwtUtils {

    @Value("${spring.security.jwt.key}")
    String key;

    @Value("${spring.security.jwt.expire}")
    int expire;

    @Resource
    StringRedisTemplate template;

    //把jwt加入黑名单
    public boolean invalidateJwt(String headerToken){
        String token = this.convertToken(headerToken);
        if(token == null) return false;
        Algorithm algorithm = Algorithm.HMAC256(key);
        JWTVerifier jwtVerifier = JWT.require(algorithm).build();

        try{
            DecodedJWT jwt = jwtVerifier.verify(token);
            String id = jwt.getId();
            return deleteToken(id, jwt.getExpiresAt());
        } catch (JWTVerificationException e){
            return false;
        }
    }

    private boolean deleteToken(String uuid, Date time){
        if(this.isInvalidToken(uuid))
            return false;
        Date now = new Date();
        //毫秒,计算放在redis里的黑名单数据的过期时间
        //这个计算还有多久过期,如果已经过期了,time.getTime() - now.getTime()就是负值,那么就redis存储过期时间为负值，明显不行

        long expire = Math.max(time.getTime() - now.getTime(), 0);
        template.opsForValue().set(Const.JWT_BLACK_LIST + uuid,"",expire, TimeUnit.MILLISECONDS);
        return true;
    }
    //判断jwt是否过期
    private boolean isInvalidToken(String uuid){
        return Boolean.TRUE.equals(template.hasKey(Const.JWT_BLACK_LIST + uuid));
    }

    //解析jwt
    public DecodedJWT resolveJwt(String headerToken){
        String token = convertToken(headerToken);
        if(token == null) return null;
        Algorithm algorithm = Algorithm.HMAC256(key);
        JWTVerifier jwtVerifier = JWT.require(algorithm).build();
        try{
            DecodedJWT verify = jwtVerifier.verify(token);
            //在黑名单里,直接返回空
            if(this.isInvalidToken(verify.getId())){
                return null;
            }
            Date expiresAt = verify.getExpiresAt();
            return new Date().after(expiresAt) ? null : verify;
        }catch (JWTVerificationException e){
            return null;
        }

    }
    //创建jwt
    public String createJwt(UserDetails userDetails, int id, String username){
        Algorithm algorithm = Algorithm.HMAC256(key);
        Date expire = this.expireTime();
        return JWT.create()
                .withJWTId(UUID.randomUUID().toString())
                .withClaim("id", id)
                .withClaim("username", username)
                .withClaim("authorities", userDetails.getAuthorities().stream().map(GrantedAuthority::getAuthority).toList())
                .withExpiresAt(expire)
                .withIssuedAt(new Date())
                .sign(algorithm);
    }
    //计算jwt的过期时间
    public Date expireTime(){
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.HOUR, expire * 24);
        return calendar.getTime();
    }

    public UserDetails toUser(DecodedJWT jwt){
        Map<String, Claim> claims = jwt.getClaims();
        return User
                .withUsername(claims.get("username").asString())
                .password("****")
                .authorities(claims.get("authorities").asArray(String.class))
                .build();
//                .authorities(claims.get("authorities").asList(GrantedAuthority.class))
    }

    public Integer toId(DecodedJWT jwt){
        Map<String, Claim> claims = jwt.getClaims();
        return claims.get("id").asInt();
    }
    private String convertToken(String headerToken){
        if(headerToken == null || !headerToken.startsWith("Bearer ")){
            return null;
        }
        return headerToken.substring(7);
    }
}
