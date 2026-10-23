package uk.gov.hmcts.reform.civil.notification.handlers.stayupdaterequested;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.notification.handlers.ClaimantEmailDTOGenerator;
import uk.gov.hmcts.reform.civil.notification.handlers.EmailDTO;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class StayUpdateRequestedClaimantEmailDTOGenerator extends ClaimantEmailDTOGenerator {

    private final StayUpdateRequestedEmailHelper helper;

    @Override
    public Boolean getShouldNotify(CaseData caseData) {
        return true;
    }

    @Override
    public String getEmailAddress(CaseData caseData) {
        return caseData.isApplicantLiP() ? caseData.getClaimantUserDetails().getEmail()
            : caseData.getApplicantSolicitor1UserDetails().getEmail();
    }

    @Override
    protected String getEmailTemplateId(CaseData caseData) {
        return helper.getTemplate(caseData.isApplicantLiP(), caseData.isClaimantBilingual());
    }

    @Override
    protected String getReferenceTemplate() {
        return "stay-update-requested-claimant-notification-%s";
    }

    @Override
    public Map<String, String> addProperties(CaseData caseData) {
        return helper.addProperties(caseData, caseData.isApplicantLiP());
    }

    @Override
    public EmailDTO buildEmailDTO(CaseData caseData, String taskId) {
        return helper.withCcdReference(super.buildEmailDTO(caseData, taskId), getReferenceTemplate(), caseData);
    }
}
