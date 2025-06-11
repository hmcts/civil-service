package uk.gov.hmcts.reform.civil.notification.handlers.claimantdefendantagreedmediation;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.notification.handlers.AllLegalRepsEmailGenerator;
import uk.gov.hmcts.reform.civil.notification.handlers.AllPartiesEmailGenerator;
import uk.gov.hmcts.reform.civil.notification.handlers.EmailDTO;
import uk.gov.hmcts.reform.civil.notification.handlers.EmailDTOGenerator;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static uk.gov.hmcts.reform.civil.utils.NotificationUtils.shouldSendMediationNotificationDefendant2LRCarm;

@Component
public class ClaimantDefendantAgreedMediationPartiesEmailGenerator extends AllPartiesEmailGenerator {

    public ClaimantDefendantAgreedMediationPartiesEmailGenerator(ClaimantDefendantAgreedMediationAppSolOneEmailDTOGenerator appSolOneEmailGenerator,
                                                                 ClaimantDefendantAgreedMediationRespSolOneEmailDTOGenerator respSolOneEmailGenerator,
                                                                 ClaimantDefendantAgreedMediationRespSolTwoEmailDTOGenerator respSolTwoEmailGenerator,
                                                                 ClaimantDefendantAgreedMediationDefendantEmailDTOGenerator defendantEmailDTOGenerator) {
        super(List.of(appSolOneEmailGenerator, respSolOneEmailGenerator, respSolTwoEmailGenerator, defendantEmailDTOGenerator));
    }
}
