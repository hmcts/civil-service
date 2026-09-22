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
        if (requestBody == null) {
            return "";
        }
        String caseId = getCaseReference(requestBody);
        return caseId != null ? caseId : "";
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
        }
        var queryCaseId = requestBody.getParameter("caseId");
        if (StringUtils.isNotBlank(queryCaseId)) {
            return queryCaseId;
        }
        return getBodyCaseId(requestBody);
    }

    @Nullable
    private static String getBodyCaseId(ContentCachingRequestWrapper requestBody) {
        byte[] content = requestBody.getContentAsByteArray();
        if (content == null) {
            return null;
        }
        String json = new String(content, StandardCharsets.UTF_8);
        String bodyId = getValueByKey(json, "id");
        String bodyCaseReference = getValueByKey(json, "caseReference");
        return bodyId != null ? bodyId : bodyCaseReference;
    }
}
