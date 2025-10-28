package uk.gov.hmcts.reform.civil.filters;

import org.jetbrains.annotations.NotNull;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@Order(Ordered.HIGHEST_PRECEDENCE)
@Component
public class RequestFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(@NotNull HttpServletRequest request,
                                    @NotNull HttpServletResponse response,
                                    @NotNull FilterChain filterChain) throws ServletException, IOException {
        ContentCachingRequestWrapper cachedBodyHttpServletRequest =
            new ContentCachingRequestWrapper(request);

        // Force caching for all content types
        cachedBodyHttpServletRequest.getInputStream();

        filterChain.doFilter(cachedBodyHttpServletRequest, response);
    }
}
