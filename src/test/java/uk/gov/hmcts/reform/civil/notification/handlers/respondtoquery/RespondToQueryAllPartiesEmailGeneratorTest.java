package uk.gov.hmcts.reform.civil.notification.handlers.respondtoquery;

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
class RespondToQueryAllPartiesEmailGeneratorTest {

    private static final String TASK_ID = "taskId";

    @Mock
    private RespondToQueryAppSolEmailDTOGenerator appSolGenerator;
    @Mock
    private RespondToQueryRespSolOneEmailDTOGenerator respSolOneGenerator;
    @Mock
    private RespondToQueryRespSolTwoEmailDTOGenerator respSolTwoGenerator;
    @Mock
    private RespondToQueryClaimantEmailDTOGenerator claimantGenerator;
    @Mock
    private RespondToQueryDefendantEmailDTOGenerator defendantGenerator;

    private RespondToQueryAllPartiesEmailGenerator generator;

    @BeforeEach
    void setUp() {
        generator = new RespondToQueryAllPartiesEmailGenerator(
            appSolGenerator,
            respSolOneGenerator,
            respSolTwoGenerator,
            claimantGenerator,
            defendantGenerator
        );
    }

    @Test
    void shouldCollectEmailsFromGeneratorsThatShouldNotify() {
        CaseData caseData = CaseDataBuilder.builder().atStateClaimIssued().build();
        EmailDTO appSolEmail = new EmailDTO("app@example.com", "tmpl", Map.of(), "ref1");
        EmailDTO claimantEmail = new EmailDTO("claimant@example.com", "tmpl", Map.of(), "ref2");

        when(appSolGenerator.getShouldNotify(caseData)).thenReturn(true);
        when(appSolGenerator.buildEmailDTO(caseData, TASK_ID)).thenReturn(appSolEmail);
        when(claimantGenerator.getShouldNotify(caseData)).thenReturn(true);
        when(claimantGenerator.buildEmailDTO(caseData, TASK_ID)).thenReturn(claimantEmail);

        when(respSolOneGenerator.getShouldNotify(caseData)).thenReturn(false);
        when(respSolTwoGenerator.getShouldNotify(caseData)).thenReturn(false);
        when(defendantGenerator.getShouldNotify(caseData)).thenReturn(false);

        Set<EmailDTO> result = generator.getPartiesToNotify(caseData, TASK_ID);

        assertThat(result).containsExactlyInAnyOrder(appSolEmail, claimantEmail);
    }
}
