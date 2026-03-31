package uk.gov.hmcts.reform.civil.notification.handlers.notifylipgenerictemplate;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.notification.handlers.AllPartiesEmailGenerator;

import java.util.List;

@Component
public class NotifyLipGenericTemplateAllPartiesEmailGenerator extends AllPartiesEmailGenerator {

    public NotifyLipGenericTemplateAllPartiesEmailGenerator(NotifyLipGenericTemplateClaimantEmailDTOGenerator claimantGenerator) {
        super(List.of(claimantGenerator));
    }
}
