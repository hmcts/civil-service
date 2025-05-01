package uk.gov.hmcts.reform.civil.handler.callback.user.task.evidenceupload;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.enums.MultiPartyScenario;
import uk.gov.hmcts.reform.civil.model.CaseData;

import java.util.ArrayList;
import java.util.List;

@Component
public class ApplicantSetOptionsTask extends SetOptionsTask {

    public ApplicantSetOptionsTask(ObjectMapper objectMapper) {
        super(objectMapper);
    }

    @Override
    List<String> setPartyOptions(CaseData caseData) {
        List<String> dynamicListOptions = new ArrayList<>();
        if (MultiPartyScenario.isTwoVOne(caseData)) {
            dynamicListOptions.add(OPTION_APP1 + caseData.getApplicant1().getPartyName());
            dynamicListOptions.add(OPTION_APP2 + caseData.getApplicant2().getPartyName());
            dynamicListOptions.add(OPTION_APP_BOTH);
        }
        return dynamicListOptions;
    }
}
