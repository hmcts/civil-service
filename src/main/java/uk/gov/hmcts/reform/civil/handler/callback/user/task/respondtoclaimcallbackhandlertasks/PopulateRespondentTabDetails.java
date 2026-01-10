package uk.gov.hmcts.reform.civil.handler.callback.user.task.respondtoclaimcallbackhandlertasks;

import com.fasterxml.jackson.databind.ObjectMapper;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Party;

import static java.util.Optional.ofNullable;

public class PopulateRespondentTabDetails {

    private PopulateRespondentTabDetails() {

    }

    public static void updateDataForClaimDetailsTab(CaseData caseData, ObjectMapper objectMapper, boolean respondent2CopyNeeded) {
        Party respondent1Clone = objectMapper.convertValue(caseData.getRespondent1(), Party.class);
        respondent1Clone.setFlags(null);
        caseData.setRespondent1DetailsForClaimDetailsTab(respondent1Clone);

        if (ofNullable(caseData.getRespondent2()).isPresent()) {
            Party respondent2Clone = objectMapper.convertValue(caseData.getRespondent2(), Party.class);
            respondent2Clone.setFlags(null);
            caseData.setRespondent2DetailsForClaimDetailsTab(respondent2Clone);
            if (respondent2CopyNeeded) {
                caseData.setRespondent2Copy(caseData.getRespondent2());
            }
        }
    }
}
