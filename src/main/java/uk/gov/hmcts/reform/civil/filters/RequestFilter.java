package uk.gov.hmcts.reform.civil.filters;

import org.jetbrains.annotations.NotNull;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static uk.gov.hmcts.reform.civil.utils.ContentCachingRequestWrapperUtil.getCaseId;
import static uk.gov.hmcts.reform.civil.utils.ContentCachingRequestWrapperUtil.getUserId;

@Component
public class RequestFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(@NotNull HttpServletRequest request,
                                    @NotNull HttpServletResponse response,
                                    @NotNull FilterChain filterChain) throws ServletException, IOException {
        ContentCachingRequestWrapper cachedBodyHttpServletRequest =
            new ContentCachingRequestWrapper(request);
        MDC.put("caseId", getCaseId(cachedBodyHttpServletRequest));
        MDC.put("userId", getUserId(cachedBodyHttpServletRequest));

        filterChain.doFilter(cachedBodyHttpServletRequest, response);
    }
}
