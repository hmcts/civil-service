package uk.gov.hmcts.reform.civil.notification.handlers.generatespecdjform;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.notification.handlers.EmailDTO;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;

import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GenerateSpecDJFormAllPartiesEmailGeneratorTest {

    private static final String TASK_ID = "taskId";

    @Mock
    private GenerateSpecDJFormReceivedAppSolEmailDTOGenerator receivedAppSol;

    @Mock
    private GenerateSpecDJFormReceivedRespSolOneEmailDTOGenerator receivedRespSolOne;

    @Mock
    private GenerateSpecDJFormReceivedRespSolTwoEmailDTOGenerator receivedRespSolTwo;

    @Mock
    private GenerateSpecDJFormRecievedClaimantEmailDTOGenerator receivedClaimant;

    @Mock
    private GenerateSpecDJFormReceivedDefendantEmailDTOGenerator receivedDefendant;

    @Mock
    private GenerateSpecDJFormRequestedAppSolEmailDTOGenerator requestedAppSol;

    @Mock
    private GenerateSpecDJFormRequestedRespSolOneEmailDTOGenerator requestedRespSolOne;

    @Mock
    private GenerateSpecDJFormRequestedRespSolTwoEmailDTOGenerator requestedRespSolTwo;

    @Mock
    private GenerateSpecDJFormRequestedClaimantEmailDTOGenerator requestedClaimant;

    @Mock
    private GenerateSpecDJFormRequestedDefendantEmailDTOGenerator requestedDefendant;

    private GenerateSpecDJFormAllPartiesEmailGenerator generator;

    @BeforeEach
    void setUp() {
        generator = new GenerateSpecDJFormAllPartiesEmailGenerator(
            receivedAppSol,
            receivedRespSolOne,
            receivedRespSolTwo,
            receivedClaimant,
            receivedDefendant,
            requestedAppSol,
            requestedRespSolOne,
            requestedRespSolTwo,
            requestedClaimant,
            requestedDefendant
        );
    }

    @Test
    void shouldCollectEmailsForGeneratorsThatShouldNotify() {
        CaseData caseData = new CaseDataBuilder()
            .ccdCaseReference(1234567890123456L)
            .build();
        EmailDTO appSolEmail = new EmailDTO("app@example.com", "tmpl", Map.of(), "ref1");
        EmailDTO claimantEmail = new EmailDTO("claimant@example.com", "tmpl", Map.of(), "ref2");
        EmailDTO requestedEmail = new EmailDTO("requested@example.com", "tmpl", Map.of(), "ref3");

        when(receivedAppSol.getShouldNotify(caseData)).thenReturn(true);
        when(receivedAppSol.buildEmailDTO(caseData, TASK_ID)).thenReturn(appSolEmail);
        when(receivedClaimant.getShouldNotify(caseData)).thenReturn(true);
        when(receivedClaimant.buildEmailDTO(caseData, TASK_ID)).thenReturn(claimantEmail);
        when(requestedAppSol.getShouldNotify(caseData)).thenReturn(true);
        when(requestedAppSol.buildEmailDTO(caseData, TASK_ID)).thenReturn(requestedEmail);

        when(receivedRespSolOne.getShouldNotify(caseData)).thenReturn(false);
        when(receivedRespSolTwo.getShouldNotify(caseData)).thenReturn(false);
        when(receivedDefendant.getShouldNotify(caseData)).thenReturn(false);
        when(requestedRespSolOne.getShouldNotify(caseData)).thenReturn(false);
        when(requestedRespSolTwo.getShouldNotify(caseData)).thenReturn(false);
        when(requestedClaimant.getShouldNotify(caseData)).thenReturn(false);
        when(requestedDefendant.getShouldNotify(caseData)).thenReturn(false);

        Set<EmailDTO> result = generator.getPartiesToNotify(caseData, TASK_ID);

        assertThat(result).containsExactlyInAnyOrder(appSolEmail, claimantEmail, requestedEmail);
    }
}
