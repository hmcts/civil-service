package uk.gov.hmcts.reform.civil.notification.handlers.respondtoquery.otherpartyqueryresponse;

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
class OtherPartyQueryResponseAllPartiesEmailGeneratorTest {

    private static final String TASK_ID = "taskId";

    @Mock
    private OtherPartyQueryResponseAppSolEmailDTOGenerator appSolGenerator;
    @Mock
    private OtherPartyQueryResponseRespSolOneEmailDTOGenerator respSolOneGenerator;
    @Mock
    private OtherPartyQueryResponseRespSolTwoEmailDTOGenerator respSolTwoGenerator;
    @Mock
    private OtherPartyQueryResponseClaimantEmailDTOGenerator claimantGenerator;
    @Mock
    private OtherPartyQueryResponseDefendantEmailDTOGenerator defendantGenerator;

    private OtherPartyQueryResponseAllPartiesEmailGenerator generator;

    @BeforeEach
    void setUp() {
        generator = new OtherPartyQueryResponseAllPartiesEmailGenerator(
            appSolGenerator,
            respSolOneGenerator,
            respSolTwoGenerator,
            claimantGenerator,
            defendantGenerator
        );
    }

    @Test
    void shouldGatherEmailDtosWhenGeneratorsShouldNotify() {
        CaseData caseData = CaseDataBuilder.builder().atStateClaimIssued().build();
        EmailDTO respSolEmail = new EmailDTO("resp@example.com", "tmpl", Map.of(), "ref1");
        EmailDTO defendantEmail = new EmailDTO("def@example.com", "tmpl", Map.of(), "ref2");

        when(respSolOneGenerator.getShouldNotify(caseData)).thenReturn(true);
        when(respSolOneGenerator.buildEmailDTO(caseData, TASK_ID)).thenReturn(respSolEmail);
        when(defendantGenerator.getShouldNotify(caseData)).thenReturn(true);
        when(defendantGenerator.buildEmailDTO(caseData, TASK_ID)).thenReturn(defendantEmail);

        when(appSolGenerator.getShouldNotify(caseData)).thenReturn(false);
        when(respSolTwoGenerator.getShouldNotify(caseData)).thenReturn(false);
        when(claimantGenerator.getShouldNotify(caseData)).thenReturn(false);

        Set<EmailDTO> result = generator.getPartiesToNotify(caseData, TASK_ID);

        assertThat(result).containsExactlyInAnyOrder(respSolEmail, defendantEmail);
    }
}
