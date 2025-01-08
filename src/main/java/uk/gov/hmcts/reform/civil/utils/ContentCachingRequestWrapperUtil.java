package uk.gov.hmcts.reform.civil.utils;

import org.springframework.web.util.ContentCachingRequestWrapper;

import java.nio.charset.StandardCharsets;

import static uk.gov.hmcts.reform.civil.utils.JsonUtil.getValueByKey;

public class ContentCachingRequestWrapperUtil {

    private ContentCachingRequestWrapperUtil() {
    }

    public static String getCaseId(ContentCachingRequestWrapper requestBody) {
        return requestBody != null && requestBody.getContentAsByteArray() != null ? getValueByKey(new String(
            requestBody.getContentAsByteArray(),
            StandardCharsets.UTF_8
        ), "id") : "";
    }

    public static String getUserId(ContentCachingRequestWrapper requestBody) {
        return requestBody != null ? requestBody.getHeader("user-id") : "";
    }
}
