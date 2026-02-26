package uk.gov.hmcts.reform.civil.aspect;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import uk.gov.hmcts.reform.civil.service.RateLimiterService;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Method;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@ExtendWith(MockitoExtension.class)
class RateLimiterAspectTest {

    @Mock
    private RateLimiterService rateLimiterService;

    @InjectMocks
    private RateLimiterAspect aspect;

    @Mock
    private ProceedingJoinPoint joinPoint;

    @Mock
    private MethodSignature methodSignature;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @BeforeEach
    void setUp() {
        when(joinPoint.getSignature()).thenReturn(methodSignature);
    }

    @AfterEach
    void tearDown() {
        RequestContextHolder.resetRequestAttributes();
    }

    @Test
    void shouldProceedWhenRequestIsAllowed() throws Throwable {
        // given
        Method method = TestController.class.getMethod("limitedEndpoint");

        when(methodSignature.getMethod()).thenReturn(method);
        when(request.getRemoteAddr()).thenReturn("127.0.0.1");
        when(request.getRequestURI()).thenReturn("/test");
        when(rateLimiterService.allowRequest("127.0.0.1", 5, 60))
            .thenReturn(true);
        when(joinPoint.proceed()).thenReturn("success");

        RequestContextHolder.setRequestAttributes(
            new ServletRequestAttributes(request, response)
        );

        // when
        Object result = aspect.checkRateLimit(joinPoint);

        // then
        assertEquals("success", result);
        verify(joinPoint).proceed();
        verify(rateLimiterService).registerRateLimit(
            eq("/test"),
            eq(5),
            eq(60),
            contains("limit: 5 requests per minute")
        );
    }

    @Test
    void shouldReturn429WhenRateLimitExceeded() throws Throwable {
        // given
        Method method = TestController.class.getMethod("limitedEndpoint");

        when(methodSignature.getMethod()).thenReturn(method);
        when(request.getRemoteAddr()).thenReturn("192.168.0.1");
        when(request.getRequestURI()).thenReturn("/test");
        when(rateLimiterService.allowRequest("192.168.0.1", 5, 60))
            .thenReturn(false);

        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);

        when(response.getWriter()).thenReturn(printWriter);

        RequestContextHolder.setRequestAttributes(
            new ServletRequestAttributes(request, response)
        );

        // when
        Object result = aspect.checkRateLimit(joinPoint);

        // then
        assertNull(result);
        verify(response).setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
        assertTrue(stringWriter.toString().contains("Rate limit exceeded"));
        verifyNoMoreInteractions(joinPoint);
    }

    @Test
    void shouldFormatDescriptionInSecondsWhenNotMinute() throws Throwable {
        // given
        Method method = TestController.class.getMethod("limitedEndpointSeconds");

        when(methodSignature.getMethod()).thenReturn(method);
        when(request.getRemoteAddr()).thenReturn("10.0.0.1");
        when(request.getRequestURI()).thenReturn("/seconds");
        when(rateLimiterService.allowRequest("10.0.0.1", 3, 30))
            .thenReturn(true);
        when(joinPoint.proceed()).thenReturn("ok");

        RequestContextHolder.setRequestAttributes(
            new ServletRequestAttributes(request, response)
        );

        // when
        aspect.checkRateLimit(joinPoint);

        // then
        verify(rateLimiterService).registerRateLimit(
            eq("/seconds"),
            eq(3),
            eq(30),
            contains("limit: 3 requests per 30 seconds")
        );
    }

    // --- Test Controller with Annotations ---

    static class TestController {

        @RateLimiter(rateLimit = 5, timeInSeconds = 60)
        public String limitedEndpoint() {
            return "limited";
        }

        @RateLimiter(rateLimit = 3, timeInSeconds = 30)
        public String limitedEndpointSeconds() {
            return "limitedSeconds";
        }
    }
}
