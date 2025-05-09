package uk.gov.hmcts.reform.civil.notification.handlers.changeofrepresentation.lrvlrlrandlipvlr;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.notification.handlers.EmailDTOGenerator;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;

import java.util.Map;

import static java.util.Objects.nonNull;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowPredicate.isInHearingReadiness;

@Component
@AllArgsConstructor
public class NoCHearingFeeUnpaidAppSolEmailDTOGenerator extends EmailDTOGenerator {

    private final NoCHelper noCHelper;
    private final NotificationsProperties notificationsProperties;

    @Override
    public boolean getShouldNotify(CaseData caseData) {
        return isInHearingReadiness.test(caseData)
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
