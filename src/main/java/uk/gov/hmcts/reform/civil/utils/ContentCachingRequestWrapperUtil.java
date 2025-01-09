package uk.gov.hmcts.reform.civil.utils;

import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;
import org.springframework.web.util.ContentCachingRequestWrapper;

import java.nio.charset.StandardCharsets;

import static uk.gov.hmcts.reform.civil.utils.JsonUtil.getValueByKey;

public class ContentCachingRequestWrapperUtil {

    private ContentCachingRequestWrapperUtil() {
    }

    public static String getCaseId(ContentCachingRequestWrapper requestBody) {
        return requestBody != null && requestBody.getContentAsByteArray() != null ? getCaseReference(requestBody) : "";
    }

    public static String getUserId(ContentCachingRequestWrapper requestBody) {
        return requestBody != null ? getUserReference(requestBody) : "";
    }

    private static String getUserReference(ContentCachingRequestWrapper requestBody) {
        return requestBody.getHeader("user-id");
    }

    private static String getPathVariable(ContentCachingRequestWrapper request, String variableName) {
        String requestURI = request.getRequestURI();
        if (requestURI == null) {
            return null;
        }
        String[] uriParts = requestURI.split("/");
        for (int i = 0; i < uriParts.length; i++) {
            if (uriParts[i].equals(variableName) && i + 1 < uriParts.length) {
                return uriParts[i + 1];
            }
        }
        return null;
    }

    @Nullable
    private static String getCaseReference(ContentCachingRequestWrapper requestBody) {

        var pathCaseId = getPathVariable(requestBody, "caseId");
        if (StringUtils.isNotBlank(pathCaseId)) {
            return pathCaseId;
        } else {
            return getBodyCaseId(requestBody);
        }
    }

    @Nullable
    private static String getBodyCaseId(ContentCachingRequestWrapper requestBody) {
        String bodyId = getValueByKey(new String(
            requestBody.getContentAsByteArray(),
            StandardCharsets.UTF_8
        ), "id");
        String bodyCaseReference = getValueByKey(new String(
            requestBody.getContentAsByteArray(),
            StandardCharsets.UTF_8
        ), "caseReference");
        return bodyId != null ? bodyId : bodyCaseReference;
    }
}
