package com.example.weboxbackend.aop;

import com.example.weboxbackend.config.JwtUtil;
import com.example.weboxbackend.dto.Result;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.SignatureException;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;

@Aspect
@Component
public class AuthAspect {

    @Around("execution(* com.example.weboxbackend.controller..*(..)) && !execution(* com.example.weboxbackend.controller.AuthController.*(..))")
    public Object checkToken(ProceedingJoinPoint joinPoint) throws Throwable {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            return Result.fail(401, "无权限");
        }


        HttpServletRequest request = attributes.getRequest();
        if (request.getRequestURI().startsWith("/backdoor")) {
            return joinPoint.proceed();
        }
        String token = request.getHeader("token");

        if (token == null || token.isEmpty()) {
            return Result.fail(401, "无权限");
        }

        try {
            Claims claims = JwtUtil.parseToken(token);
            request.setAttribute("userId", Long.parseLong(claims.getSubject()));
            request.setAttribute("userEmail", claims.get("email", String.class));
        } catch (ExpiredJwtException e) {
            return Result.fail(401, "token已过期");
        } catch (SignatureException e) {
            return Result.fail(401, "token无效");
        } catch (Exception e) {
            return Result.fail(401, "无权限");
        }

        return joinPoint.proceed();
    }
}
