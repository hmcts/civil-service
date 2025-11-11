package uk.gov.hmcts.reform.civil.service.dj;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.IdamUserDetails;
import uk.gov.hmcts.reform.civil.model.common.DynamicList;
import uk.gov.hmcts.reform.civil.model.common.DynamicListElement;
import uk.gov.hmcts.reform.civil.sampledata.PartyBuilder;

import static org.assertj.core.api.Assertions.assertThat;

class DjNotificationRecipientServiceTest {

    private final DjNotificationRecipientService service = new DjNotificationRecipientService();

    @Test
    void shouldReturnClaimantSolicitorEmailWhenRepresented() {
        CaseData caseData = CaseData.builder()
            .applicant1Represented(YesOrNo.YES)
            .applicantSolicitor1UserDetails(IdamUserDetails.builder().email("solicitor@example.com").build())
            .build();

        assertThat(service.getClaimantEmail(caseData)).isEqualTo("solicitor@example.com");
    }

    @Test
    void shouldReturnClaimantLipEmailWhenUnrepresented() {
        CaseData caseData = CaseData.builder()
            .applicant1Represented(YesOrNo.NO)
            .claimantUserDetails(IdamUserDetails.builder().email("applicant@example.com").build())
            .build();

        assertThat(service.getClaimantEmail(caseData)).isEqualTo("applicant@example.com");
    }

    @Test
    void shouldReturnRespondent1SolicitorEmail() {
        CaseData caseData = CaseData.builder()
            .respondent1Represented(YesOrNo.YES)
            .respondentSolicitor1EmailAddress("respondentsolicitor@example.com")
            .build();

        assertThat(service.getRespondent1Email(caseData)).isEqualTo("respondentsolicitor@example.com");
    }

    @Test
    void shouldReturnRespondent1LipEmail() {
        CaseData caseData = CaseData.builder()
            .respondent1Represented(YesOrNo.NO)
            .defendantUserDetails(IdamUserDetails.builder().email("defendant@example.com").build())
            .build();

        assertThat(service.getRespondent1Email(caseData)).isEqualTo("defendant@example.com");
    }

    @Test
    void shouldReturnSharedSolicitorEmailForRespondent2WhenOneLegalRep() {
        CaseData caseData = CaseData.builder()
            .respondent1EmailAddress("sharedsolicitor@example.com")
            .respondent2SameLegalRepresentative(YesOrNo.YES)
            .respondent2(PartyBuilder.builder().individual().build())
            .build();

        assertThat(service.getRespondent2Email(caseData)).isEqualTo("sharedsolicitor@example.com");
    }

    @Test
    void shouldReturnRespondent2SolicitorEmailWhenRepresentedSeparately() {
        CaseData caseData = CaseData.builder()
            .respondent2Represented(YesOrNo.YES)
            .respondentSolicitor2EmailAddress("respondentsolicitor2@example.com")
            .respondent2(PartyBuilder.builder().individual().build())
            .build();

        assertThat(service.getRespondent2Email(caseData)).isEqualTo("respondentsolicitor2@example.com");
    }

    @Test
    void shouldRequireSelectionForRespondent1Notification() {
        var respondent1 = PartyBuilder.builder().individual().build();
        CaseData caseData = CaseData.builder()
            .respondent1Represented(YesOrNo.YES)
            .respondent1(respondent1)
            .respondentSolicitor1EmailAddress("respondentsolicitor@example.com")
            .defendantDetails(dynamicSelection(respondent1.getPartyName()))
            .build();

        assertThat(service.shouldNotifyRespondent1(caseData)).isTrue();
    }

    @Test
    void shouldRespectBothDefendantsSelection() {
        var respondent1 = PartyBuilder.builder().individual().build();
        CaseData caseData = CaseData.builder()
            .respondent1Represented(YesOrNo.YES)
            .respondent1(respondent1)
            .respondentSolicitor1EmailAddress("respondentsolicitor@example.com")
            .respondent2(PartyBuilder.builder().individual().build())
            .defendantDetails(dynamicSelection("Both Defendants"))
            .addRespondent2(YesOrNo.YES)
            .build();

        assertThat(service.shouldNotifyRespondent1(caseData)).isTrue();
        assertThat(service.shouldNotifyRespondent2(caseData)).isTrue();
    }

    @Test
    void shouldRequireRespondent2FlagBeforeNotifyingSecondRespondent() {
        CaseData caseData = CaseData.builder()
            .respondent2(PartyBuilder.builder().individual().build())
            .defendantDetails(dynamicSelection("Mr. John Rambo"))
            .addRespondent2(YesOrNo.NO)
            .build();

        assertThat(service.shouldNotifyRespondent2(caseData)).isFalse();
    }

    private DynamicList dynamicSelection(String label) {
        return DynamicList.builder()
            .value(DynamicListElement.dynamicElement(label))
            .build();
    }
}
