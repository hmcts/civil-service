package uk.gov.hmcts.reform.civil.handler.callback.user.task.evidenceupload;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.enums.MultiPartyScenario;
import uk.gov.hmcts.reform.civil.model.CaseData;

import java.util.ArrayList;
import java.util.List;

@Component
public class RespondentSetOptionsTask extends SetOptionsTask {

    public RespondentSetOptionsTask(ObjectMapper objectMapper) {
        super(objectMapper);
    }

    @Override
    List<String> setPartyOptions(CaseData caseData) {
        List<String> dynamicListOptions = new ArrayList<>();
        if (MultiPartyScenario.isOneVTwoLegalRep(caseData)) {
            dynamicListOptions.add(OPTION_DEF1 + caseData.getRespondent1().getPartyName());
            dynamicListOptions.add(OPTION_DEF2 + caseData.getRespondent2().getPartyName());
            dynamicListOptions.add(OPTION_DEF_BOTH);
        }
        return dynamicListOptions;
    }
}
