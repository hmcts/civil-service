package uk.gov.hmcts.reform.civil.utils;

import org.springframework.web.util.ContentCachingRequestWrapper;

import java.nio.charset.StandardCharsets;

import static uk.gov.hmcts.reform.civil.utils.JsonUtil.getValueByKey;

public class CaseDataRequestUtil {

    private CaseDataRequestUtil() {
    }

    public static String getCaseId(ContentCachingRequestWrapper requestBody) {
        return requestBody != null ? getValueByKey(new String(
            requestBody.getContentAsByteArray(),
            StandardCharsets.UTF_8
        ), "id") : "";
    }
}
