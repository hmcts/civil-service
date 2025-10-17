package uk.gov.hmcts.reform.civil.helpers;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.DeserializationFeature;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.civil.enums.CaseState;
import uk.gov.hmcts.reform.civil.ga.model.GeneralApplicationCaseData;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.citizenui.CaseDataLiP;
import uk.gov.hmcts.reform.civil.model.genapplication.GeneralApplication;

import java.util.HashMap;
import java.util.Map;

@Service
public class CaseDetailsConverter {

    private final ObjectMapper objectMapper;

    public CaseDetailsConverter(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        this.objectMapper.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);
    }

    public CaseData toCaseData(CaseDetails caseDetails) {
        return convert(caseDetails, false);
    }

    public CaseData toCaseData(Map<String, Object> caseDataMap) {
        CaseData caseData = objectMapper.convertValue(caseDataMap, CaseData.class);
        return enrichCaseDataLiP(caseDataMap, caseData);
    }

    public CaseData toCaseDataGA(CaseDetails caseDetails) {
        return convert(caseDetails, true);
    }

    public GeneralApplicationCaseData toGeneralApplicationCaseData(CaseDetails caseDetails) {
        Map<String, Object> data = new HashMap<>(caseDetails.getData());
        data.put("ccdCaseReference", caseDetails.getId());
        if (caseDetails.getState() != null) {
            data.put("ccdState", CaseState.valueOf(caseDetails.getState()));
        }
        if (caseDetails.getCreatedDate() != null) {
            data.put("createdDate", caseDetails.getCreatedDate());
        }
        return objectMapper.convertValue(data, GeneralApplicationCaseData.class);
    }

    public CaseData toGACaseData(CaseDetails caseDetails) {
        Map<String, Object> data = new HashMap<>(caseDetails.getData());
        data.remove("hwfFeeType");
        data.put("ccdCaseReference", caseDetails.getId());

        return objectMapper.convertValue(data, CaseData.class);
    }

    public GeneralApplication toGeneralApplication(CaseDetails caseDetails) {
        Map<String, Object> data = new HashMap<>(caseDetails.getData());

        Map<String, Object> caseReference = new HashMap<>();
        caseReference.put("CaseReference", caseDetails.getId());

        data.put("caseLink", caseReference);

        if (caseDetails.getState() != null) {
            data.put("generalApplicationState", caseDetails.getState());
        }

        return objectMapper.convertValue(data, GeneralApplication.class);
    }

    private CaseData convert(CaseDetails caseDetails, boolean includeCreatedDate) {
        Map<String, Object> data = new HashMap<>(caseDetails.getData());
        data.put("ccdCaseReference", caseDetails.getId());
        if (caseDetails.getState() != null) {
            data.put("ccdState", CaseState.valueOf(caseDetails.getState()));
        }
        if (includeCreatedDate && caseDetails.getCreatedDate() != null) {
            data.put("createdDate", caseDetails.getCreatedDate());
        }

        CaseData caseData = objectMapper.convertValue(data, CaseData.class);
        return enrichCaseDataLiP(data, caseData);
    }

    private CaseData enrichCaseDataLiP(Map<String, Object> data, CaseData caseData) {

        CaseDataLiP existing = caseData.getCaseDataLiP();
        if (existing != null && existing.getRespondent1LiPResponse() != null) {
            return caseData;
        }

        CaseDataLiP mappedCaseDataLiP = objectMapper.copy()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            .convertValue(data, CaseDataLiP.class);

        CaseDataLiP.CaseDataLiPBuilder caseDataLiPBuilder = existing != null
            ? existing.toBuilder()
            : mappedCaseDataLiP != null ? mappedCaseDataLiP.toBuilder() : CaseDataLiP.builder();

        if (caseDataLiPBuilder.build().getRespondent1LiPResponse() == null
            && caseData.getRespondent1LiPResponse() != null) {
            caseDataLiPBuilder.respondent1LiPResponse(caseData.getRespondent1LiPResponse());
        }

        CaseDataLiP candidate = caseDataLiPBuilder.build();

        if (candidate.equals(CaseDataLiP.builder().build())) {
            return caseData;
        }

        return caseData.toBuilder()
            .caseDataLiP(candidate)
            .build();
    }
}
