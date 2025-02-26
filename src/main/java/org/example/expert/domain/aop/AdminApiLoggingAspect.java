package org.example.expert.domain.aop;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDateTime;

@Aspect
@Component
@Slf4j
public class AdminApiLoggingAspect {
    private final ObjectMapper mapper = new ObjectMapper();

       @Around("execution(* org.example.expert.domain.comment.controller.CommentAdminController.deleteComment(..)) || " +
               "execution(* org.example.expert.domain.user.controller.UserAdminController.changeUserRole(..))")
       public Object logAdminApi(ProceedingJoinPoint pjp) throws Throwable {
           // Given
           ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
           HttpServletRequest req = attrs.getRequest();
           String url = req.getRequestURL().toString();
           String method = req.getMethod();
           String adminId = req.getHeader("X-Admin-UserId");
           LocalDateTime startTime = LocalDateTime.now();

           // 요청 파라미터를 JSON 문자열로 변환함.
           // 실패하면 null
           String args;
           try {
               args = mapper.writeValueAsString(pjp.getArgs());
           } catch (Exception ignored) {
               args = null;
           }

           log.info("[admin API 요청] {} {} at {} 관리자[{}] - 요청값: {}",
                   method, url, startTime, adminId, args != null ? args : "null");

           //When
           // 실제 대상 메서드 실행 및 결과 캡쳐
           Object result = pjp.proceed();

           //Then
           //응답 데이터를 JSON 문자열로 변환하여 로그에 남깁니다.
           String response = "N/A";
           try {
               response = mapper.writeValueAsString(result);
           } catch (Exception ignored) { }

           log.info("[어드민 API 응답] {} - 응답값: {}", LocalDateTime.now(), response);
           return result;
       }
}
