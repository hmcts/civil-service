package uk.gov.hmcts.reform.civil.notification.handlers.defendantresponsecasetransferoffline;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.civil.enums.CaseCategory;
import uk.gov.hmcts.reform.civil.enums.RespondentResponseType;
import uk.gov.hmcts.reform.civil.enums.RespondentResponseTypeSpec;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Party;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.civil.notification.handlers.defendantresponsecasetransferoffline.DefRespCaseOfflineHelper.caseOfflineNotificationProperties;

class DefRespCaseOfflineHelperTest {

    @Test
    void shouldReturnReasonFor1v1() {
        CaseData caseData = CaseData.builder()
            .respondent1ClaimResponseType(RespondentResponseType.FULL_DEFENCE)
            .build();

        Map<String, String> result = caseOfflineNotificationProperties(caseData);

        assertThat(result).containsEntry("reason", "rejects all of the claim");
    }

    @Test
    void shouldReturnConcatenatedReasonFor2v1LiP() {
        CaseData caseData = CaseData.builder()
            .caseAccessCategory(CaseCategory.UNSPEC_CLAIM)
            .applicant1(Party.builder().partyName("Applicant One").build())
            .applicant2(Party.builder().partyName("Applicant Two").build())
            .respondent1ClaimResponseType(RespondentResponseType.PART_ADMISSION)
            .respondent1ClaimResponseTypeToApplicant2(RespondentResponseType.FULL_ADMISSION)
            .build();

        Map<String, String> result = caseOfflineNotificationProperties(caseData);

        assertThat(result.get("reason")).contains("Part admission against Applicant One")
            .contains("Full admission against Applicant Two");
    }

    @Test
    void shouldReturnSpecReasonFor2v1() {
        CaseData caseData = CaseData.builder()
            .caseAccessCategory(CaseCategory.SPEC_CLAIM)
            .claimant1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.FULL_DEFENCE)
            .build();

        Map<String, String> result = caseOfflineNotificationProperties(caseData);

        assertThat(result).containsEntry("reason", "Full defence");
    }

    @Test
    void shouldReturn1v2ResponseFieldsForLiP() {
        CaseData caseData = CaseData.builder()
            .caseAccessCategory(CaseCategory.UNSPEC_CLAIM)
            .respondent1(Party.builder().type(Party.Type.INDIVIDUAL).individualFirstName("John").individualLastName("Smith").build())
            .respondent2(Party.builder().type(Party.Type.COMPANY).companyName("ABC Ltd").build())
            .respondent1ClaimResponseType(RespondentResponseType.FULL_ADMISSION)
            .respondent2ClaimResponseType(RespondentResponseType.PART_ADMISSION)
            .build();

        Map<String, String> result = caseOfflineNotificationProperties(caseData);

        assertThat(result).containsEntry("respondent1Name", "John Smith")
            .containsEntry("respondent2Name", "ABC Ltd")
            .containsEntry("respondent1Response", "Full admission")
            .containsEntry("respondent2Response", "Part admission");
    }

    @Test
    void shouldReturn1v2ResponseFieldsForSpec() {
        CaseData caseData = CaseData.builder()
            .caseAccessCategory(CaseCategory.SPEC_CLAIM)
            .respondent1(Party.builder().type(Party.Type.INDIVIDUAL).individualFirstName("Jane").individualLastName("Doe").build())
            .respondent2(Party.builder().type(Party.Type.ORGANISATION).organisationName("XYZ Org").build())
            .respondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.COUNTER_CLAIM)
            .respondent2ClaimResponseTypeForSpec(RespondentResponseTypeSpec.FULL_DEFENCE)
            .build();

        Map<String, String> result = caseOfflineNotificationProperties(caseData);

        assertThat(result).containsEntry("respondent1Name", "Jane Doe")
            .containsEntry("respondent2Name", "XYZ Org")
            .containsEntry("respondent1Response", "Counter claim")
            .containsEntry("respondent2Response", "Full defence");
    }
}
