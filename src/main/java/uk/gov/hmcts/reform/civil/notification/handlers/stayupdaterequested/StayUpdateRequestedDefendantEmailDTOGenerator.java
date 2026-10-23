package uk.gov.hmcts.reform.civil.notification.handlers.stayupdaterequested;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.notification.handlers.DefendantEmailDTOGenerator;
import uk.gov.hmcts.reform.civil.notification.handlers.EmailDTO;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class StayUpdateRequestedDefendantEmailDTOGenerator extends DefendantEmailDTOGenerator {

    private final StayUpdateRequestedEmailHelper helper;

    @Override
    public Boolean getShouldNotify(CaseData caseData) {
        return true;
    }

    @Override
    public String getEmailAddress(CaseData caseData) {
        return caseData.isRespondent1LiP() && caseData.getRespondent1().getPartyEmail() != null
            ? caseData.getRespondent1().getPartyEmail()
            : caseData.getRespondentSolicitor1EmailAddress();
    }

    @Override
    protected String getEmailTemplateId(CaseData caseData) {
        return helper.getTemplate(caseData.isRespondent1LiP(), caseData.isRespondentResponseBilingual());
    }

    @Override
    protected String getReferenceTemplate() {
        return "stay-update-requested-defendant-notification-%s";
    }

    @Override
    public Map<String, String> addProperties(CaseData caseData) {
        return helper.addProperties(caseData, caseData.isRespondent1LiP());
    }

    @Override
    public EmailDTO buildEmailDTO(CaseData caseData, String taskId) {
        return helper.withCcdReference(super.buildEmailDTO(caseData, taskId), getReferenceTemplate(), caseData);
    }
}
