package uk.gov.hmcts.reform.civil.notification.handlers.mediationsuccessfulandunsuccessful;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.notification.handlers.EmailDTO;
import uk.gov.hmcts.reform.civil.notification.handlers.EmailDTOGenerator;
import uk.gov.hmcts.reform.civil.notification.handlers.PartiesEmailGenerator;
import uk.gov.hmcts.reform.civil.notification.handlers.mediationsuccessfulandunsuccessful.carmdisabled.CarmDisabledAppSolOneEmailDTOGenerator;
import uk.gov.hmcts.reform.civil.notification.handlers.mediationsuccessfulandunsuccessful.carmdisabled.CarmDisabledClaimantEmailDTOGenerator;
import uk.gov.hmcts.reform.civil.notification.handlers.mediationsuccessfulandunsuccessful.carmdisabled.CarmDisabledDefendantEmailDTOGenerator;
import uk.gov.hmcts.reform.civil.notification.handlers.mediationsuccessfulandunsuccessful.carmdisabled.CarmDisabledRespSolOneEmailDTOGenerator;
import uk.gov.hmcts.reform.civil.notification.handlers.mediationsuccessfulandunsuccessful.carmenabled.CarmAppSolOneEmailDTOGenerator;
import uk.gov.hmcts.reform.civil.notification.handlers.mediationsuccessfulandunsuccessful.carmenabled.CarmClaimantEmailDTOGenerator;
import uk.gov.hmcts.reform.civil.notification.handlers.mediationsuccessfulandunsuccessful.carmenabled.CarmDefendantEmailDTOGenerator;
import uk.gov.hmcts.reform.civil.notification.handlers.mediationsuccessfulandunsuccessful.carmenabled.CarmRespSolOneEmailDTOGenerator;
import uk.gov.hmcts.reform.civil.notification.handlers.mediationsuccessfulandunsuccessful.carmenabled.CarmRespSolTwoEmailDTOGenerator;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;

import java.util.HashSet;
import java.util.Set;

@Component
@AllArgsConstructor
@Slf4j
public class MediationUpdateAllPartiesEmailGenerator implements PartiesEmailGenerator {

    private final CarmAppSolOneEmailDTOGenerator carmAppSolOneEmailDTOGenerator;
    private final CarmRespSolOneEmailDTOGenerator carmRespSolOneEmailDTOGenerator;
    private final CarmRespSolTwoEmailDTOGenerator carmRespSolTwoEmailDTOGenerator;
    private final CarmClaimantEmailDTOGenerator carmClaimantEmailDTOGenerator;
    private final CarmDefendantEmailDTOGenerator carmDefendantEmailDTOGenerator;

    private final CarmDisabledAppSolOneEmailDTOGenerator carmDisabledAppSolOneEmailDTOGenerator;
    private final CarmDisabledRespSolOneEmailDTOGenerator carmDisabledRespSolOneEmailDTOGenerator;
    private final CarmDisabledClaimantEmailDTOGenerator carmDisabledClaimantEmailDTOGenerator;
    private final CarmDisabledDefendantEmailDTOGenerator carmDisabledDefendantEmailDTOGenerator;

    private final FeatureToggleService featureToggleService;

    @Override
    public Set<EmailDTO> getPartiesToNotify(final CaseData caseData, String taskId) {
        Set<EmailDTO> partiesToEmail = new HashSet<>();
        if (featureToggleService.isCarmEnabledForCase(caseData)) {
            addIfPartyNeedsNotification(caseData, carmAppSolOneEmailDTOGenerator, partiesToEmail, taskId);
            addIfPartyNeedsNotification(caseData, carmRespSolOneEmailDTOGenerator, partiesToEmail, taskId);
            addIfPartyNeedsNotification(caseData, carmRespSolTwoEmailDTOGenerator, partiesToEmail, taskId);
            addIfPartyNeedsNotification(caseData, carmClaimantEmailDTOGenerator, partiesToEmail, taskId);
            addIfPartyNeedsNotification(caseData, carmDefendantEmailDTOGenerator, partiesToEmail, taskId);
        } else {
            addIfPartyNeedsNotification(caseData, carmDisabledAppSolOneEmailDTOGenerator, partiesToEmail, taskId);
            addIfPartyNeedsNotification(caseData, carmDisabledRespSolOneEmailDTOGenerator, partiesToEmail, taskId);
            addIfPartyNeedsNotification(caseData, carmDisabledClaimantEmailDTOGenerator, partiesToEmail, taskId);
            addIfPartyNeedsNotification(caseData, carmDisabledDefendantEmailDTOGenerator, partiesToEmail, taskId);
        }
        return partiesToEmail;
    }

    private void addIfPartyNeedsNotification(CaseData caseData,
                                             EmailDTOGenerator generator,
                                             Set<EmailDTO> partiesToEmail,
                                             String taskId) {
        if ((generator != null) && generator.getShouldNotify(caseData)) {
            log.info("Generating email for party [{}] for case ID: {} and task ID: {} ",
                     generator.getClass().getSimpleName(), caseData.getCcdCaseReference(), taskId);
            partiesToEmail.add(generator.buildEmailDTO(caseData, taskId));
        }
    }
}
