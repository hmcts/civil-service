package uk.gov.hmcts.reform.civil.notification.handlers.mediationsuccessfulandunsuccessful;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.notification.handlers.EmailDTO;
import uk.gov.hmcts.reform.civil.notification.handlers.EmailDTOGenerator;
import uk.gov.hmcts.reform.civil.notification.handlers.mediationsuccessfulandunsuccessful.carmdisabled.*;
import uk.gov.hmcts.reform.civil.notification.handlers.mediationsuccessfulandunsuccessful.carmenabled.*;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

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
    private final EmailDTO dummyDTO = new EmailDTO("test@example.com", "template-id", null, "ref-id");

    @BeforeEach
    void setUpMocks() {
        setUpGeneratorMock(carmAppSolOneEmailDTOGenerator);
        setUpGeneratorMock(carmRespSolOneEmailDTOGenerator);
        setUpGeneratorMock(carmRespSolTwoEmailDTOGenerator);
        setUpGeneratorMock(carmClaimantEmailDTOGenerator);
        setUpGeneratorMock(carmDefendantEmailDTOGenerator);

        setUpGeneratorMock(carmDisabledAppSolOneEmailDTOGenerator);
        setUpGeneratorMock(carmDisabledRespSolOneEmailDTOGenerator);
        setUpGeneratorMock(carmDisabledClaimantEmailDTOGenerator);
        setUpGeneratorMock(carmDisabledDefendantEmailDTOGenerator);
    }

    private void setUpGeneratorMock(EmailDTOGenerator generator) {
        when(generator.getShouldNotify(caseData)).thenReturn(true);
        when(generator.buildEmailDTO(caseData, taskId)).thenReturn(dummyDTO);
    }

    @Test
    void shouldReturnAllEnabledGenerators_whenCarmFeatureEnabled() {
        when(featureToggleService.isCarmEnabledForCase(caseData)).thenReturn(true);

        Set<EmailDTO> emails = generator.getPartiesToNotify(caseData, taskId);

        assertThat(emails).hasSize(5);
        verify(carmAppSolOneEmailDTOGenerator).getShouldNotify(caseData);
        verify(carmAppSolOneEmailDTOGenerator).buildEmailDTO(caseData, taskId);
    }

    @Test
    void shouldReturnAllDisabledGenerators_whenCarmFeatureDisabled() {
        when(featureToggleService.isCarmEnabledForCase(caseData)).thenReturn(false);

        Set<EmailDTO> emails = generator.getPartiesToNotify(caseData, taskId);

        assertThat(emails).hasSize(4);
        verify(carmDisabledDefendantEmailDTOGenerator).getShouldNotify(caseData);
        verify(carmDisabledDefendantEmailDTOGenerator).buildEmailDTO(caseData, taskId);
    }

    @Test
    void shouldSkipGenerator_whenItShouldNotNotify() {
        when(featureToggleService.isCarmEnabledForCase(caseData)).thenReturn(true);
        when(carmAppSolOneEmailDTOGenerator.getShouldNotify(caseData)).thenReturn(false);

        Set<EmailDTO> emails = generator.getPartiesToNotify(caseData, taskId);

        assertThat(emails).hasSize(4); // One generator skipped
        verify(carmAppSolOneEmailDTOGenerator, never()).buildEmailDTO(any(), any());
    }
}
