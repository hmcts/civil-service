package uk.gov.hmcts.reform.civil.request;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.HandlerMapping;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Collections.emptySet;
import static org.apache.commons.lang3.StringUtils.isBlank;

/**
 * Access http request related attributes.
 * Uses ThreadLocalCache as a cache to avoid accessing http request from spawned threads
 * (http request can be already closed at the time when a new thread accesses it)
 *
 * @see uk.gov.hmcts.reform.civil.config.AsyncHandlerConfiguration
 */

@Primary
@Slf4j
@Service
public class CacheAwareRequestData implements RequestData {

    private static final String CCD_CASE_IDENTIFIER_KEY = "ccd-case-identifier";

    private final HttpServletRequest httpServletRequest;

    @Autowired
    public CacheAwareRequestData(HttpServletRequest httpServletRequest) {
        this.httpServletRequest = httpServletRequest;
    }

    public String authorisation() {
        return RequestDataCache.get()
            .map(RequestData::authorisation)
            .orElseGet(() -> httpServletRequest.getHeader("authorization"));
    }

    public String userId() {
        return RequestDataCache.get()
            .map(RequestData::userId)
            .orElseGet(() -> httpServletRequest.getHeader("user-id"));
    }

    public Set<String> userRoles() {
        return RequestDataCache.get()
            .map(RequestData::userRoles)
            .orElseGet(() -> extractUserRoles(httpServletRequest));
    }

    @Override
    public String caseId() {
        return RequestDataCache.get()
            .map(RequestData::caseId)
            .orElseGet(() -> extractCaseIdFromBody(httpServletRequest));
    }

    private Set<String> extractUserRoles(HttpServletRequest httpServletRequest) {
        String userRoles = httpServletRequest.getHeader("user-roles");

        if (isBlank(userRoles)) {
            return emptySet();
        }

        return Stream.of(userRoles.split(","))
            .map(StringUtils::trim)
            .filter(StringUtils::isNotBlank)
            .collect(Collectors.toSet());
    }

    @SuppressWarnings("unchecked")
    private String extractCaseIdFromBody(HttpServletRequest httpServletRequest) {

        if (!httpServletRequest.getMethod().equals(HttpMethod.POST.name())) {
            Map<String, String> pathMappings =
                (Map<String, String>) httpServletRequest.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE);

            if (pathMappings == null) {
                return StringUtils.EMPTY;
            }

            return Optional.ofNullable(pathMappings.get(CCD_CASE_IDENTIFIER_KEY)).orElse(StringUtils.EMPTY);
        }

        try {

            var mapper = new ObjectMapper();
            String requestBody = IOUtils.toString(httpServletRequest.getReader());
            SimpleCallbackRequest callbackRequest = mapper.readValue(requestBody, SimpleCallbackRequest.class);

            if (callbackRequest == null || callbackRequest.getCaseDetails() == null) {
                return StringUtils.EMPTY;
            }

            return Optional.ofNullable(callbackRequest.getCaseDetails().getId())
                .map(Object::toString)
                .orElse(StringUtils.EMPTY);
        } catch (IOException e) {
            log.error("Unable to extract caseId from request body during caching due to exception %s".formatted(e.getMessage()));
            return StringUtils.EMPTY;
        }
    }
}

