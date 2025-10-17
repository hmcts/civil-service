package uk.gov.hmcts.reform.civil.handler.callback.camunda;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.ga.model.GeneralApplicationCaseData;
import uk.gov.hmcts.reform.civil.model.CaseData;

import java.util.HashMap;
import java.util.Map;

/**
 * Utility helper for GA callbacks to merge GA DTO payloads with legacy {@link CaseData}.
 * Keeps the logic in one place so handlers can focus on their behaviour.
 */
public final class GaCallbackDataUtil {

    private GaCallbackDataUtil() {
        // Utility class
    }

    public static GeneralApplicationCaseData resolveGaCaseData(CallbackParams callbackParams,
                                                               ObjectMapper objectMapper) {
        GeneralApplicationCaseData gaCaseData = callbackParams.getGaCaseData();
        if (gaCaseData != null) {
            return gaCaseData;
        }
        CaseData fallback = callbackParams.getCaseData();
        if (fallback == null) {
            return null;
        }
        return toGaCaseData(fallback, objectMapper);
    }

    public static CaseData resolveCaseData(CallbackParams callbackParams, ObjectMapper objectMapper) {
        return mergeToCaseData(resolveGaCaseData(callbackParams, objectMapper), callbackParams.getCaseData(), objectMapper);
    }

    public static CaseData mergeToCaseData(GeneralApplicationCaseData gaCaseData,
                                           CaseData fallback,
                                           ObjectMapper objectMapper) {
        if (gaCaseData == null) {
            return fallback;
        }

        ObjectMapper mapperWithJavaTime = objectMapper.copy();
        mapperWithJavaTime.registerModule(new JavaTimeModule());
        Map<String, Object> base = fallback != null ? fallback.toMap(mapperWithJavaTime) : Map.of();
        Map<String, Object> gaMap = mapperWithJavaTime.convertValue(gaCaseData, new TypeReference<>() { });
        Map<String, Object> merged = new HashMap<>(base);
        merged.putAll(gaMap);

        CaseData converted = mapperWithJavaTime.convertValue(merged, CaseData.class);
        CaseData.CaseDataBuilder<?, ?> builder = converted.toBuilder();

        if (gaCaseData.getCcdCaseReference() != null) {
            builder.ccdCaseReference(gaCaseData.getCcdCaseReference());
        } else if (fallback != null) {
            builder.ccdCaseReference(fallback.getCcdCaseReference());
        }

        if (gaCaseData.getCcdState() != null) {
            builder.ccdState(gaCaseData.getCcdState());
        } else if (fallback != null) {
            builder.ccdState(fallback.getCcdState());
        }

        return builder.build();
    }

    public static GeneralApplicationCaseData toGaCaseData(CaseData caseData, ObjectMapper objectMapper) {
        if (caseData == null) {
            return null;
        }

        ObjectMapper mapperWithJavaTime = objectMapper.copy();
        mapperWithJavaTime.registerModule(new JavaTimeModule());

        GeneralApplicationCaseData converted = mapperWithJavaTime.convertValue(
            caseData,
            GeneralApplicationCaseData.class
        );

        GeneralApplicationCaseData.GeneralApplicationCaseDataBuilder builder = converted.toBuilder();
        if (caseData.getCcdCaseReference() != null) {
            builder.ccdCaseReference(caseData.getCcdCaseReference());
        }
        if (caseData.getCcdState() != null) {
            builder.ccdState(caseData.getCcdState());
        }
        return builder.build();
    }
}
