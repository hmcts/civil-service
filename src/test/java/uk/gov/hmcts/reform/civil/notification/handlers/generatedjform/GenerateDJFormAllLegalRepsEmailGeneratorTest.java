package uk.gov.hmcts.reform.civil.notification.handlers.generatedjform;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.enums.MultiPartyScenario;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.notification.handlers.EmailDTO;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GenerateDJFormAllLegalRepsEmailGeneratorTest {

    @Mock
    private GenerateDJFormApprovedAppSolOneEmailDTOGenerator approvedAppSolOneEmailDTOGenerator;

    @Mock
    private GenerateDJFormApprovedRespSolOneEmailDTOGenerator approvedRespSolOneEmailDTOGenerator;

    @Mock
    private GenerateDJFormApprovedRespSolTwoEmailDTOGenerator approvedRespSolTwoEmailDTOGenerator;

    @Mock
    private GenerateDJFormRequestedAppSolOneEmailDTOGenerator requestedAppSolOneEmailDTOGenerator;

    @Mock
    private GenerateDJFormRequestedRespSolOneEmailDTOGenerator requestedRespSolOneEmailDTOGenerator;

    @Mock
    private GenerateDJFormRequestedRespSolTwoEmailDTOGenerator requestedRespSolTwoEmailDTOGenerator;

    @Mock
    private GenerateDJFormHelper generateDJFormHelper;

    @InjectMocks
    private GenerateDJFormAllLegalRepsEmailGenerator generator;

    private MockedStatic<MultiPartyScenario> multiPartyScenarioMockedStatic;

    @BeforeEach
    void setUp() {
        multiPartyScenarioMockedStatic = Mockito.mockStatic(MultiPartyScenario.class);
    }

    @AfterEach
    void tearDown() {
        multiPartyScenarioMockedStatic.close();
    }

    @Test
    void shouldReturnApplicantAndRespondentsEmails_whenOneVTwoTwoLegalRep() {
        CaseData caseData = mock(CaseData.class);
        EmailDTO applicantEmail1 = mock(EmailDTO.class);
        EmailDTO applicantEmail2 = mock(EmailDTO.class);

        multiPartyScenarioMockedStatic.when(() -> MultiPartyScenario.isOneVTwoTwoLegalRep(caseData))
            .thenReturn(true);

        when(generateDJFormHelper.checkDefendantRequested(caseData, true)).thenReturn(false);
        when(generateDJFormHelper.checkDefendantRequested(caseData, false)).thenReturn(false);
        when(generateDJFormHelper.checkIfBothDefendants(caseData)).thenReturn(true);

        when(approvedAppSolOneEmailDTOGenerator.buildEmailDTO(caseData))
            .thenReturn(applicantEmail1)
            .thenReturn(applicantEmail2);

        Set<EmailDTO> result = generator.getPartiesToNotify(caseData);

        assertThat(result).containsExactlyInAnyOrder(applicantEmail1, applicantEmail2);

        verify(approvedAppSolOneEmailDTOGenerator, times(2)).buildEmailDTO(caseData);
    }

    @Test
    void shouldReturnRespondent1Email_whenDefendantRequestedTrue() {
        CaseData caseData = mock(CaseData.class);
        EmailDTO respondent1Email = mock(EmailDTO.class);

        multiPartyScenarioMockedStatic.when(() -> MultiPartyScenario.isOneVTwoTwoLegalRep(caseData))
            .thenReturn(true);
        when(generateDJFormHelper.checkDefendantRequested(caseData, true)).thenReturn(true);
        when(requestedRespSolOneEmailDTOGenerator.getShouldNotify(caseData)).thenReturn(true);
        when(requestedRespSolOneEmailDTOGenerator.buildEmailDTO(caseData)).thenReturn(respondent1Email);

        Set<EmailDTO> result = generator.getRespondents(caseData);

        assertThat(result).containsExactly(respondent1Email);
        verify(requestedRespSolOneEmailDTOGenerator).buildEmailDTO(caseData);
    }

    @Test
    void shouldReturnRespondent2Email_whenDefendantRequestedFalse() {
        CaseData caseData = mock(CaseData.class);
        EmailDTO respondent2Email = mock(EmailDTO.class);

        multiPartyScenarioMockedStatic.when(() -> MultiPartyScenario.isOneVTwoTwoLegalRep(caseData))
            .thenReturn(true);
        when(generateDJFormHelper.checkDefendantRequested(caseData, true)).thenReturn(false);
        when(generateDJFormHelper.checkDefendantRequested(caseData, false)).thenReturn(true);
        when(requestedRespSolTwoEmailDTOGenerator.getShouldNotify(caseData)).thenReturn(true);
        when(requestedRespSolTwoEmailDTOGenerator.buildEmailDTO(caseData)).thenReturn(respondent2Email);

        Set<EmailDTO> result = generator.getRespondents(caseData);

        assertThat(result).containsExactly(respondent2Email);
        verify(requestedRespSolTwoEmailDTOGenerator).buildEmailDTO(caseData);
    }

    @Test
    void shouldReturnBothRespondentsEmails_whenBothDefendants() {
        CaseData caseData = mock(CaseData.class);
        EmailDTO respondent1Email = mock(EmailDTO.class);
        EmailDTO respondent2Email = mock(EmailDTO.class);

        multiPartyScenarioMockedStatic.when(() -> MultiPartyScenario.isOneVTwoTwoLegalRep(caseData))
            .thenReturn(true);
        when(generateDJFormHelper.checkDefendantRequested(caseData, true)).thenReturn(false);
        when(generateDJFormHelper.checkDefendantRequested(caseData, false)).thenReturn(false);
        when(generateDJFormHelper.checkIfBothDefendants(caseData)).thenReturn(true);
        when(approvedRespSolOneEmailDTOGenerator.getShouldNotify(caseData)).thenReturn(true);
        when(approvedRespSolTwoEmailDTOGenerator.getShouldNotify(caseData)).thenReturn(true);
        when(approvedRespSolOneEmailDTOGenerator.buildEmailDTO(caseData)).thenReturn(respondent1Email);
        when(approvedRespSolTwoEmailDTOGenerator.buildEmailDTO(caseData)).thenReturn(respondent2Email);

        Set<EmailDTO> result = generator.getRespondents(caseData);

        assertThat(result).containsExactlyInAnyOrder(respondent1Email, respondent2Email);
        verify(approvedRespSolOneEmailDTOGenerator).buildEmailDTO(caseData);
        verify(approvedRespSolTwoEmailDTOGenerator).buildEmailDTO(caseData);
    }

    @Test
    void shouldReturnRespondent1Email_whenNotOneVTwoTwoLegalRep() {
        CaseData caseData = mock(CaseData.class);
        EmailDTO respondent1Email = mock(EmailDTO.class);

        multiPartyScenarioMockedStatic.when(() -> MultiPartyScenario.isOneVTwoTwoLegalRep(caseData))
            .thenReturn(false);
        when(approvedRespSolOneEmailDTOGenerator.getShouldNotify(caseData)).thenReturn(true);
        when(approvedRespSolOneEmailDTOGenerator.buildEmailDTO(caseData)).thenReturn(respondent1Email);

        Set<EmailDTO> result = generator.getRespondents(caseData);

        assertThat(result).containsExactly(respondent1Email);
        verify(approvedRespSolOneEmailDTOGenerator).buildEmailDTO(caseData);
    }

    @Test
    void shouldNotAddBothRespondentsEmails_whenCheckIfBothDefendantsIsFalse() {
        CaseData caseData = mock(CaseData.class);

        multiPartyScenarioMockedStatic.when(() -> MultiPartyScenario.isOneVTwoTwoLegalRep(caseData))
            .thenReturn(true);

        when(generateDJFormHelper.checkDefendantRequested(caseData, true)).thenReturn(false);
        when(generateDJFormHelper.checkDefendantRequested(caseData, false)).thenReturn(false);
        when(generateDJFormHelper.checkIfBothDefendants(caseData)).thenReturn(false);

        Set<EmailDTO> result = generator.getRespondents(caseData);

        assertThat(result).isEmpty();

        verifyNoInteractions(approvedRespSolOneEmailDTOGenerator);
        verifyNoInteractions(approvedRespSolTwoEmailDTOGenerator);
    }

    @Test
    void shouldReturnRequestedAppSolEmail_whenDefendantRequestedTrueOrFalse() {
        CaseData caseData = mock(CaseData.class);
        EmailDTO requestedAppSolEmail = mock(EmailDTO.class);

        multiPartyScenarioMockedStatic.when(() -> MultiPartyScenario.isOneVTwoTwoLegalRep(caseData))
            .thenReturn(true);

        when(generateDJFormHelper.checkDefendantRequested(caseData, true)).thenReturn(true);
        when(requestedAppSolOneEmailDTOGenerator.buildEmailDTO(caseData)).thenReturn(requestedAppSolEmail);

        Set<EmailDTO> result = generator.getApplicant(caseData);

        assertThat(result).containsExactly(requestedAppSolEmail);

        verify(requestedAppSolOneEmailDTOGenerator).buildEmailDTO(caseData);
    }

    @Test
    void shouldReturnTwoApprovedAppSolEmails_whenBothDefendants() {
        CaseData caseData = mock(CaseData.class);
        EmailDTO approvedAppSolEmail1 = mock(EmailDTO.class);
        EmailDTO approvedAppSolEmail2 = mock(EmailDTO.class);
        Map<String, String> updatedProperties = Map.of("key", "value");

        multiPartyScenarioMockedStatic.when(() -> MultiPartyScenario.isOneVTwoTwoLegalRep(caseData))
            .thenReturn(true);

        when(generateDJFormHelper.checkDefendantRequested(caseData, true)).thenReturn(false);
        when(generateDJFormHelper.checkDefendantRequested(caseData, false)).thenReturn(false);
        when(generateDJFormHelper.checkIfBothDefendants(caseData)).thenReturn(true);
        when(approvedAppSolOneEmailDTOGenerator.buildEmailDTO(caseData)).thenReturn(approvedAppSolEmail1, approvedAppSolEmail2);
        when(generateDJFormHelper.updateRespondent2Properties(anyMap(), eq(caseData))).thenReturn(updatedProperties);

        Set<EmailDTO> result = generator.getApplicant(caseData);

        assertThat(result).containsExactlyInAnyOrder(approvedAppSolEmail1, approvedAppSolEmail2);

        verify(approvedAppSolOneEmailDTOGenerator, times(2)).buildEmailDTO(caseData);
        verify(generateDJFormHelper).updateRespondent2Properties(anyMap(), eq(caseData));
    }

    @Test
    void shouldReturnApprovedAppSolEmail_whenNotOneVTwoTwoLegalRep() {
        CaseData caseData = mock(CaseData.class);
        EmailDTO approvedAppSolEmail = mock(EmailDTO.class);

        multiPartyScenarioMockedStatic.when(() -> MultiPartyScenario.isOneVTwoTwoLegalRep(caseData))
            .thenReturn(false);

        when(approvedAppSolOneEmailDTOGenerator.buildEmailDTO(caseData)).thenReturn(approvedAppSolEmail);

        Set<EmailDTO> result = generator.getApplicant(caseData);

        assertThat(result).containsExactly(approvedAppSolEmail);

        verify(approvedAppSolOneEmailDTOGenerator).buildEmailDTO(caseData);
    }

    @Test
    void shouldNotAddApprovedAppSolEmails_whenCheckIfBothDefendantsIsFalse() {
        CaseData caseData = mock(CaseData.class);

        multiPartyScenarioMockedStatic.when(() -> MultiPartyScenario.isOneVTwoTwoLegalRep(caseData))
            .thenReturn(true);

        when(generateDJFormHelper.checkDefendantRequested(caseData, true)).thenReturn(false);
        when(generateDJFormHelper.checkDefendantRequested(caseData, false)).thenReturn(false);
        when(generateDJFormHelper.checkIfBothDefendants(caseData)).thenReturn(false);

        Set<EmailDTO> result = generator.getApplicant(caseData);

        assertThat(result).isEmpty();

        verifyNoInteractions(approvedAppSolOneEmailDTOGenerator);
    }

    @Test
    void shouldAddRequestedAppSolEmail_whenDefendantRequestedFalseIsTrue() {
        CaseData caseData = mock(CaseData.class);
        EmailDTO requestedAppSolEmail = mock(EmailDTO.class);

        multiPartyScenarioMockedStatic.when(() -> MultiPartyScenario.isOneVTwoTwoLegalRep(caseData))
            .thenReturn(true);

        when(generateDJFormHelper.checkDefendantRequested(caseData, true)).thenReturn(false);
        when(generateDJFormHelper.checkDefendantRequested(caseData, false)).thenReturn(true);

        when(requestedAppSolOneEmailDTOGenerator.buildEmailDTO(caseData)).thenReturn(requestedAppSolEmail);

        Set<EmailDTO> result = generator.getApplicant(caseData);

        assertThat(result).containsExactly(requestedAppSolEmail);

        verify(requestedAppSolOneEmailDTOGenerator).buildEmailDTO(caseData);
    }

    @Test
    void shouldNotAddEmail_whenGeneratorIsNull() {
        CaseData caseData = mock(CaseData.class);
        Set<EmailDTO> partiesToEmail = new HashSet<>();

        generator.addIfPartyNeedsNotification(caseData, null, partiesToEmail);

        assertThat(partiesToEmail).isEmpty();
    }
}
