package uk.gov.hmcts.reform.civil.helpers;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.civil.enums.CaseState;
import uk.gov.hmcts.reform.civil.ga.model.GeneralApplicationCaseData;
import uk.gov.hmcts.reform.civil.model.BaseCaseData;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.genapplication.GeneralApplication;

import java.util.HashMap;
import java.util.Map;

import static uk.gov.hmcts.reform.civil.helpers.CaseTypeIdentifier.isGeneralApplication;

@Service
public class CaseDetailsConverter {

    private final ObjectMapper objectMapper;

    public CaseDetailsConverter(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        this.objectMapper.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);
    }

    public BaseCaseData toBaseCaseData(CaseDetails caseDetails) {
        if (isGeneralApplication(caseDetails)) {
            return toGeneralApplicationCaseData(caseDetails);
        } else {
            return toCaseData(caseDetails);
        }
    }

    public CaseData toCaseData(CaseDetails caseDetails) {
        Map<String, Object> data = new HashMap<>(caseDetails.getData());
        data.put("ccdCaseReference", caseDetails.getId());
        if (caseDetails.getState() != null) {
            data.put("ccdState", CaseState.valueOf(caseDetails.getState()));
        }

        return objectMapper.convertValue(data, CaseData.class);
    }

    public CaseData toCaseData(Map<String, Object> caseDataMap) {
        return objectMapper.convertValue(caseDataMap, CaseData.class);
    }

    public GeneralApplicationCaseData toGeneralApplicationCaseData(CaseDetails caseDetails) {
        final Map<String, Object> data = new HashMap<>(caseDetails.getData());
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
}
