package uk.gov.hmcts.reform.civil.service.directionsorder;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.sampledata.PartyBuilder;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;

class DirectionsOrderParticipantServiceTest {

    private final DirectionsOrderParticipantService service = new DirectionsOrderParticipantService();

    @Test
    void shouldFormatOneVOneParticipants() {
        CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build();

        String result = service.buildApplicantVRespondentText(caseData);

        assertThat(result).isEqualTo("Mr. John Rambo v Mr. Sole Trader");
    }

    @Test
    void shouldFormatOneVTwoParticipants() {
        CaseData caseData = CaseDataBuilder.builder()
            .atStateNotificationAcknowledged()
            .build().toBuilder()
            .respondent2(PartyBuilder.builder().individual("Jane").build())
            .addRespondent2(YES)
            .respondent2SameLegalRepresentative(YES)
            .build();

        String result = service.buildApplicantVRespondentText(caseData);

        assertThat(result).isEqualTo("Mr. John Rambo v Mr. Sole Trader and Mr. Jane Rambo");
    }

    @Test
    void shouldFormatTwoVOneParticipants() {
        CaseData caseData = CaseDataBuilder.builder()
            .atStateNotificationAcknowledged()
            .multiPartyClaimTwoApplicants()
            .build();

        String result = service.buildApplicantVRespondentText(caseData);

        assertThat(result).isEqualTo("Mr. John Rambo and Mr. Jason Rambo v Mr. Sole Trader");
    }
}
