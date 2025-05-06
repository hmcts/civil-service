package uk.gov.hmcts.reform.civil.filters;

import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.NotNull;

@Component
public class RequestFilter extends OncePerRequestFilter {
    @Override
    protected void doFilterInternal(@NotNull HttpServletRequest request, @NotNull HttpServletResponse response,
                                    javax.servlet.FilterChain filterChain) throws ServletException, IOException {
                ContentCachingRequestWrapper cachedBodyHttpServletRequest =
            new ContentCachingRequestWrapper(request);

        filterChain.doFilter(cachedBodyHttpServletRequest, response);
    }
}
