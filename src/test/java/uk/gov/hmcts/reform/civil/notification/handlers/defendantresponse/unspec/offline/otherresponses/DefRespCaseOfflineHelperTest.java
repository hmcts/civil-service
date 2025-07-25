package uk.gov.hmcts.reform.civil.notification.handlers.defendantresponse.unspec.offline.otherresponses;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.civil.enums.CaseCategory;
import uk.gov.hmcts.reform.civil.enums.RespondentResponseType;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Party;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.civil.notification.handlers.defendantresponse.unspec.offline.otherresponses.DefRespCaseOfflineHelper.caseOfflineNotificationProperties;

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
                            .individualFirstName("Applicant")
                            .individualLastName("One")
                            .build())
            .applicant2(Party.builder()
                            .type(Party.Type.INDIVIDUAL)
                            .individualFirstName("Applicant")
                            .individualLastName("Two")
                            .build())
            .addApplicant2(YesOrNo.YES)
            .respondent1ClaimResponseType(RespondentResponseType.PART_ADMISSION)
            .respondent1ClaimResponseTypeToApplicant2(RespondentResponseType.FULL_ADMISSION)
            .build();

        Map<String, String> result = caseOfflineNotificationProperties(caseData);

        assertThat(result.get("reason")).contains("admits part of the claim")
            .contains("Applicant One and admits all of the claim against Applicant Two");
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
}
