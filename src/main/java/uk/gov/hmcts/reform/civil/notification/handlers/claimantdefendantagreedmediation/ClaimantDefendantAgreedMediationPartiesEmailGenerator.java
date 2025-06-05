package uk.gov.hmcts.reform.civil.notification.handlers.claimantdefendantagreedmediation;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.notification.handlers.AllLegalRepsEmailGenerator;
import uk.gov.hmcts.reform.civil.notification.handlers.EmailDTO;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;

import java.util.HashSet;
import java.util.Set;

import static uk.gov.hmcts.reform.civil.utils.NotificationUtils.shouldSendMediationNotificationDefendant2LRCarm;

@Component
public class ClaimantDefendantAgreedMediationPartiesEmailGenerator extends AllLegalRepsEmailGenerator {

    private final ClaimantDefendantAgreedMediationDefendantEmailDTOGenerator claimantDefendantAgreedMediationDefendantEmailDTOGenerator;

    private final FeatureToggleService featureToggleService;

    public ClaimantDefendantAgreedMediationPartiesEmailGenerator(
        ClaimantDefendantAgreedMediationAppSolOneEmailDTOGenerator appSolOneEmailGenerator,
        ClaimantDefendantAgreedMediationRespSolOneEmailDTOGenerator respSolOneEmailGenerator,
        ClaimantDefendantAgreedMediationRespSolTwoEmailDTOGenerator respSolTwoEmailGenerator,
        ClaimantDefendantAgreedMediationDefendantEmailDTOGenerator claimantDefendantAgreedMediationDefendantEmailDTOGenerator,
        FeatureToggleService featureToggleService) {
        super(appSolOneEmailGenerator, respSolOneEmailGenerator, respSolTwoEmailGenerator);
        this.claimantDefendantAgreedMediationDefendantEmailDTOGenerator = claimantDefendantAgreedMediationDefendantEmailDTOGenerator;
        this.featureToggleService = featureToggleService;
    }

    @Override
    protected Set<EmailDTO> getRespondents(CaseData caseData, String taskId) {
        Set<EmailDTO> recipients = new HashSet<>();
        if (caseData.isRespondent1NotRepresented()) {
            recipients.add(claimantDefendantAgreedMediationDefendantEmailDTOGenerator.buildEmailDTO(caseData, taskId));
        } else {
            recipients.add(respSolOneEmailGenerator.buildEmailDTO(caseData, taskId));

            boolean carmEnabled = featureToggleService.isCarmEnabledForCase(caseData);
            boolean shouldNotifyRespondent2LRCarm = shouldSendMediationNotificationDefendant2LRCarm(caseData, carmEnabled);

            if (shouldNotifyRespondent2LRCarm) {
                recipients.add(respSolTwoEmailGenerator.buildEmailDTO(caseData, taskId));
            }
        }

        return recipients;
    }
}
