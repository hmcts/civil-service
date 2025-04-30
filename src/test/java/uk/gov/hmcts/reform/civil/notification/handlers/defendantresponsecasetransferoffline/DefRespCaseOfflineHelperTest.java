package uk.gov.hmcts.reform.civil.notification.handlers.defendantresponsecasetransferoffline;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.civil.enums.CaseCategory;
import uk.gov.hmcts.reform.civil.enums.RespondentResponseType;
import uk.gov.hmcts.reform.civil.enums.RespondentResponseTypeSpec;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
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
            .applicant1(Party.builder()
                            .type(Party.Type.INDIVIDUAL)
                            .partyName("Applicant One")
                            .build())
            .applicant2(Party.builder()
                            .type(Party.Type.INDIVIDUAL)
                            .partyName("Applicant Two")
                            .build())
            .addApplicant2(YesOrNo.YES)
            .respondent1ClaimResponseType(RespondentResponseType.PART_ADMISSION)
            .respondent1ClaimResponseTypeToApplicant2(RespondentResponseType.FULL_ADMISSION)
            .build();

        Map<String, String> result = caseOfflineNotificationProperties(caseData);

        assertThat(result.get("reason")).contains("admits part of the claim")
            .contains("admits part of the claim against Applicant Two");
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

        assertThat(result).containsEntry("defendantOneName", "John Smith")
            .containsEntry("defendantTwoName", "ABC Ltd")
            .containsEntry("defendantOneResponse", "admits all of the claim")
            .containsEntry("defendantTwoResponse", "admits part of the claim");
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

        assertThat(result).containsEntry("defendantOneName", "Jane Doe")
            .containsEntry("defendantTwoName", "XYZ Org")
            .containsEntry("defendantOneResponse", "Reject all of the claim and wants to counterclaim")
            .containsEntry("defendantTwoResponse", "Defends all of the claim");
    }
}
