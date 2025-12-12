package uk.gov.hmcts.reform.civil.notification.handlers.changeofrepresentation.otherflow;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.notification.handlers.EmailDTOGenerator;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.service.flowstate.predicate.HearingPredicate;

import java.util.Map;

import static java.util.Objects.nonNull;

@Component
@AllArgsConstructor
public class NoCHearingFeeUnpaidAppSolEmailDTOGenerator extends EmailDTOGenerator {

    private final NoCHelper noCHelper;
    private final NotificationsProperties notificationsProperties;

    @Override
    public Boolean getShouldNotify(CaseData caseData) {
        return HearingPredicate.isInReadiness.test(caseData)
            && !noCHelper.isHearingFeePaid(caseData)
            && nonNull(caseData.getHearingFee());
    }

    @Override
    protected String getEmailAddress(CaseData caseData) {
        return caseData.getApplicantSolicitor1UserDetailsEmail();
    }

    @Override
    protected String getEmailTemplateId(CaseData caseData) {
        return notificationsProperties.getHearingFeeUnpaidNoc();
    }

    @Override
    protected String getReferenceTemplate() {
        return NoCHelper.REFERENCE_TEMPLATE;
    }

    @Override
    protected Map<String, String> addCustomProperties(Map<String, String> properties, CaseData caseData) {
        properties.putAll(noCHelper.getHearingFeeEmailProperties(caseData));
        return properties;
    }
}
