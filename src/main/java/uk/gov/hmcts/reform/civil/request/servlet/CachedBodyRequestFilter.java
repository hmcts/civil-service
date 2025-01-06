package uk.gov.hmcts.reform.civil.request.servlet;

import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * We need this filter to map the servlet request to our new cached body servlet request, which allows us to read
 * the request body as many times as we like. Its usual accessors (getInputStream and getReader) are consumed after
 * one use so extending the class and instantiating our own stream readers gets around this.
 * **/
@Component
public class CachedBodyRequestFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(@NotNull HttpServletRequest request,
                                    @NotNull HttpServletResponse response,
                                    @NotNull FilterChain filterChain) throws ServletException, IOException {
        ContentCachingRequestWrapper cachedBodyHttpServletRequest =
            new ContentCachingRequestWrapper(request);
        filterChain.doFilter(cachedBodyHttpServletRequest, response);
    }
}
