package uk.gov.hmcts.reform.civil.notification.handlers.stayupdaterequested;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.notification.handlers.ClaimantEmailDTOGenerator;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class StayUpdateRequestedClaimantEmailDTOGenerator extends ClaimantEmailDTOGenerator {

    private final StayUpdateRequestedEmailHelper helper;

    @Override
    public Boolean getShouldNotify(CaseData caseData) {
        return StringUtils.isNotBlank(getEmailAddress(caseData));
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
    protected String buildEmailReference(CaseData caseData) {
        return String.format(getReferenceTemplate(), caseData.getCcdCaseReference());
    }
}
