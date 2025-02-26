package org.example.expert.aop;

import nl.altindag.log.LogCaptor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.example.expert.domain.aop.AdminApiLoggingAspect;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

class AdminApiLoggingAspectTest {

    @Test
    void testLogAdminApi() throws Throwable {
        // given
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/admin/comments/123");
        request.setMethod("DELETE");
        request.addHeader("X-Admin-UserId", "관리자-1");

        ServletRequestAttributes attributes = new ServletRequestAttributes(request);
        RequestContextHolder.setRequestAttributes(attributes);

        ProceedingJoinPoint joinPoint = Mockito.mock(ProceedingJoinPoint.class);
        when(joinPoint.getArgs()).thenReturn(new Object[]{"arg1", "arg2"});
        when(joinPoint.proceed()).thenReturn("성공");

        // Signature 모의 객체 생성
        Signature signature = Mockito.mock(Signature.class);
        when(signature.getName()).thenReturn("dummySignature");
        when(joinPoint.getSignature()).thenReturn(signature);

        LogCaptor logCaptor = LogCaptor.forClass(AdminApiLoggingAspect.class);
        AdminApiLoggingAspect aspect = new AdminApiLoggingAspect();

        // when
        Object result = aspect.logAdminApi(joinPoint);

        // then
        assertThat(result).isEqualTo("성공");
        assertThat(logCaptor.getInfoLogs())
                .anySatisfy(log -> assertThat(log).containsIgnoringCase("api 요청"));
        assertThat(logCaptor.getInfoLogs())
                .anySatisfy(log -> assertThat(log).containsIgnoringCase("api 응답"));

        RequestContextHolder.resetRequestAttributes();
    }
}
