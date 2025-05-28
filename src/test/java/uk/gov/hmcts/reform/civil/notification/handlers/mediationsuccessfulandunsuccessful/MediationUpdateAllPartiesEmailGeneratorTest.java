package uk.gov.hmcts.reform.civil.notification.handlers.mediationsuccessfulandunsuccessful;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.notification.handlers.EmailDTO;
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

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MediationUpdateAllPartiesEmailGeneratorTest {

    @Mock
    private CarmAppSolOneEmailDTOGenerator carmAppSolOneEmailDTOGenerator;
    @Mock
    private CarmRespSolOneEmailDTOGenerator carmRespSolOneEmailDTOGenerator;
    @Mock
    private CarmRespSolTwoEmailDTOGenerator carmRespSolTwoEmailDTOGenerator;
    @Mock
    private CarmClaimantEmailDTOGenerator carmClaimantEmailDTOGenerator;
    @Mock
    private CarmDefendantEmailDTOGenerator carmDefendantEmailDTOGenerator;

    @Mock
    private CarmDisabledAppSolOneEmailDTOGenerator carmDisabledAppSolOneEmailDTOGenerator;
    @Mock
    private CarmDisabledRespSolOneEmailDTOGenerator carmDisabledRespSolOneEmailDTOGenerator;
    @Mock
    private CarmDisabledClaimantEmailDTOGenerator carmDisabledClaimantEmailDTOGenerator;
    @Mock
    private CarmDisabledDefendantEmailDTOGenerator carmDisabledDefendantEmailDTOGenerator;

    @Mock
    private FeatureToggleService featureToggleService;

    @InjectMocks
    private MediationUpdateAllPartiesEmailGenerator generator;

    private final CaseData caseData = CaseData.builder().ccdCaseReference(123456789L).build();
    private final String taskId = "some-task-id";

    @Test
    void shouldReturnAllEnabledGenerators_whenCarmFeatureEnabled() {
        when(carmAppSolOneEmailDTOGenerator.getShouldNotify(caseData)).thenReturn(true);
        when(carmAppSolOneEmailDTOGenerator.buildEmailDTO(caseData, taskId))
            .thenReturn(new EmailDTO("test@example.com", "AppSolOne", null, "ref-id"));

        when(carmRespSolOneEmailDTOGenerator.getShouldNotify(caseData)).thenReturn(true);
        when(carmRespSolOneEmailDTOGenerator.buildEmailDTO(caseData, taskId))
            .thenReturn(new EmailDTO("test@example.com", "RespSolOne", null, "ref-id"));

        when(carmRespSolTwoEmailDTOGenerator.getShouldNotify(caseData)).thenReturn(true);
        when(carmRespSolTwoEmailDTOGenerator.buildEmailDTO(caseData, taskId))
            .thenReturn(new EmailDTO("test@example.com", "RespSolTwo", null, "ref-id"));

        when(carmClaimantEmailDTOGenerator.getShouldNotify(caseData)).thenReturn(true);
        when(carmClaimantEmailDTOGenerator.buildEmailDTO(caseData, taskId))
            .thenReturn(new EmailDTO("test@example.com", "Claimant", null, "ref-id"));

        when(carmDefendantEmailDTOGenerator.getShouldNotify(caseData)).thenReturn(true);
        when(carmDefendantEmailDTOGenerator.buildEmailDTO(caseData, taskId))
            .thenReturn(new EmailDTO("test@example.com", "Defendant", null, "ref-id"));

        when(featureToggleService.isCarmEnabledForCase(caseData)).thenReturn(true);

        Set<EmailDTO> emails = generator.getPartiesToNotify(caseData, taskId);

        assertThat(emails).hasSize(5);
        verify(carmAppSolOneEmailDTOGenerator).getShouldNotify(caseData);
        verify(carmAppSolOneEmailDTOGenerator).buildEmailDTO(caseData, taskId);
    }

    @Test
    void shouldReturnAllDisabledGenerators_whenCarmFeatureDisabled() {
        when(carmDisabledAppSolOneEmailDTOGenerator.getShouldNotify(caseData)).thenReturn(true);
        when(carmDisabledAppSolOneEmailDTOGenerator.buildEmailDTO(caseData, taskId))
            .thenReturn(new EmailDTO("test@example.com", "DisabledAppSolOne", null, "ref-id"));

        when(carmDisabledRespSolOneEmailDTOGenerator.getShouldNotify(caseData)).thenReturn(true);
        when(carmDisabledRespSolOneEmailDTOGenerator.buildEmailDTO(caseData, taskId))
            .thenReturn(new EmailDTO("test@example.com", "DisabledRespSolOne", null, "ref-id"));

        when(carmDisabledClaimantEmailDTOGenerator.getShouldNotify(caseData)).thenReturn(true);
        when(carmDisabledClaimantEmailDTOGenerator.buildEmailDTO(caseData, taskId))
            .thenReturn(new EmailDTO("test@example.com", "DisabledClaimant", null, "ref-id"));

        when(carmDisabledDefendantEmailDTOGenerator.getShouldNotify(caseData)).thenReturn(true);
        when(carmDisabledDefendantEmailDTOGenerator.buildEmailDTO(caseData, taskId))
            .thenReturn(new EmailDTO("test@example.com", "DisabledDefendant", null, "ref-id"));

        when(featureToggleService.isCarmEnabledForCase(caseData)).thenReturn(false);

        Set<EmailDTO> emails = generator.getPartiesToNotify(caseData, taskId);

        assertThat(emails).hasSize(4);
        verify(carmDisabledDefendantEmailDTOGenerator).getShouldNotify(caseData);
        verify(carmDisabledDefendantEmailDTOGenerator).buildEmailDTO(caseData, taskId);
    }

    @Test
    void shouldSkipGenerator_whenItShouldNotNotify() {
        when(carmRespSolOneEmailDTOGenerator.getShouldNotify(caseData)).thenReturn(true);
        when(carmRespSolOneEmailDTOGenerator.buildEmailDTO(caseData, taskId))
            .thenReturn(new EmailDTO("test@example.com", "RespSolOne", null, "ref-id"));

        when(carmRespSolTwoEmailDTOGenerator.getShouldNotify(caseData)).thenReturn(true);
        when(carmRespSolTwoEmailDTOGenerator.buildEmailDTO(caseData, taskId))
            .thenReturn(new EmailDTO("test@example.com", "RespSolTwo", null, "ref-id"));

        when(carmClaimantEmailDTOGenerator.getShouldNotify(caseData)).thenReturn(true);
        when(carmClaimantEmailDTOGenerator.buildEmailDTO(caseData, taskId))
            .thenReturn(new EmailDTO("test@example.com", "Claimant", null, "ref-id"));

        when(carmDefendantEmailDTOGenerator.getShouldNotify(caseData)).thenReturn(true);
        when(carmDefendantEmailDTOGenerator.buildEmailDTO(caseData, taskId))
            .thenReturn(new EmailDTO("test@example.com", "Defendant", null, "ref-id"));

        when(featureToggleService.isCarmEnabledForCase(caseData)).thenReturn(true);
        when(carmAppSolOneEmailDTOGenerator.getShouldNotify(caseData)).thenReturn(false);

        Set<EmailDTO> emails = generator.getPartiesToNotify(caseData, taskId);

        assertThat(emails).hasSize(4); // One generator skipped
        verify(carmAppSolOneEmailDTOGenerator, never()).buildEmailDTO(any(), any());
    }
}
