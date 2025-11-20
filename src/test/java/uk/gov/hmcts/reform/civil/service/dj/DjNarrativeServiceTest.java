package uk.gov.hmcts.reform.civil.service.dj;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.common.DynamicList;
import uk.gov.hmcts.reform.civil.model.common.DynamicListElement;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;

import static org.assertj.core.api.Assertions.assertThat;

class DjNarrativeServiceTest {

    private static final String CLAIM_NUMBER = "000MC001";

    private final DjNarrativeService service = new DjNarrativeService();

    @Test
    void shouldBuildHeaderWithClaimNumber() {
        CaseData caseData = CaseData.builder()
            .legacyCaseReference(CLAIM_NUMBER)
            .build();

        String header = service.buildConfirmationHeader(caseData);

        assertThat(header).contains(CLAIM_NUMBER);
    }

    @Test
    void shouldBuildBodyForOneVsOne() {
        CaseData caseData = baseCaseData();

        String body = service.buildConfirmationBody(caseData);

        assertThat(body).contains("Claimant 1").contains("Defendant 1");
        assertThat(body).doesNotContain("Defendant 2");
    }

    @Test
    void shouldBuildBodyForTwoClaimantsVsOneDefendant() {
        CaseData caseData = CaseDataBuilder.builder()
            .atStateNotificationAcknowledged()
            .multiPartyClaimTwoApplicants()
            .legacyCaseReference(CLAIM_NUMBER)
            .build();

        assertThat(caseData.getApplicant2()).as("applicant2 should be present").isNotNull();

        String body = service.buildConfirmationBody(caseData);

        assertThat(body).contains("Claimant 1");
        assertThat(body).doesNotContain("## Claimant 2");
    }

    @Test
    void shouldBuildBodyForOneClaimantVsTwoDefendants() {
        CaseData caseData = CaseDataBuilder.builder()
            .atStateNotificationAcknowledged()
            .multiPartyClaimOneDefendantSolicitor()
            .legacyCaseReference(CLAIM_NUMBER)
            .build()
            .toBuilder()
            .defendantDetails(
                DynamicList.builder()
                    .value(DynamicListElement.builder().code("both").label("Both defendants").build())
                    .build())
            .build();

        assertThat(caseData.getRespondent2()).as("respondent2 should be present").isNotNull();

        String body = service.buildConfirmationBody(caseData);

        assertThat(body).contains("## Defendant 2");
    }

    private CaseData baseCaseData() {
        return CaseDataBuilder.builder()
            .atStateNotificationAcknowledged()
            .legacyCaseReference(CLAIM_NUMBER)
            .build();
    }
}
