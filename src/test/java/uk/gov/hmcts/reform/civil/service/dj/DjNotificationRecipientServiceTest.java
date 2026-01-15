package uk.gov.hmcts.reform.civil.service.dj;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.IdamUserDetails;
import uk.gov.hmcts.reform.civil.model.common.DynamicList;
import uk.gov.hmcts.reform.civil.model.common.DynamicListElement;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.sampledata.PartyBuilder;

import static org.assertj.core.api.Assertions.assertThat;

class DjNotificationRecipientServiceTest {

    private final DjNotificationRecipientService service = new DjNotificationRecipientService();

    @Test
    void shouldReturnClaimantSolicitorEmailWhenRepresented() {
        CaseData caseData = CaseDataBuilder.builder().build().toBuilder()
            .applicant1Represented(YesOrNo.YES)
            .applicantSolicitor1UserDetails(userDetails("solicitor@example.com"))
            .build();

        assertThat(service.getClaimantEmail(caseData)).isEqualTo("solicitor@example.com");
    }

    @Test
    void shouldReturnClaimantLipEmailWhenUnrepresented() {
        CaseData caseData = CaseDataBuilder.builder().build().toBuilder()
            .applicant1Represented(YesOrNo.NO)
            .claimantUserDetails(userDetails("applicant@example.com"))
            .build();

        assertThat(service.getClaimantEmail(caseData)).isEqualTo("applicant@example.com");
    }

    @Test
    void shouldReturnRespondent1SolicitorEmail() {
        CaseData caseData = CaseDataBuilder.builder().build().toBuilder()
            .respondent1Represented(YesOrNo.YES)
            .respondentSolicitor1EmailAddress("respondentsolicitor@example.com")
            .build();

        assertThat(service.getRespondent1Email(caseData)).isEqualTo("respondentsolicitor@example.com");
    }

    @Test
    void shouldReturnRespondent1LipEmail() {
        CaseData caseData = CaseDataBuilder.builder().build().toBuilder()
            .respondent1Represented(YesOrNo.NO)
            .defendantUserDetails(userDetails("defendant@example.com"))
            .build();

        assertThat(service.getRespondent1Email(caseData)).isEqualTo("defendant@example.com");
    }

    @Test
    void shouldReturnSharedSolicitorEmailForRespondent2WhenOneLegalRep() {
        CaseData caseData = CaseDataBuilder.builder().build().toBuilder()
            .respondent1EmailAddress("sharedsolicitor@example.com")
            .respondent2SameLegalRepresentative(YesOrNo.YES)
            .respondent2(PartyBuilder.builder().individual().build())
            .build();

        assertThat(service.getRespondent2Email(caseData)).isEqualTo("sharedsolicitor@example.com");
    }

    @Test
    void shouldReturnRespondent2SolicitorEmailWhenRepresentedSeparately() {
        CaseData caseData = CaseDataBuilder.builder().build().toBuilder()
            .respondent2Represented(YesOrNo.YES)
            .respondentSolicitor2EmailAddress("respondentsolicitor2@example.com")
            .respondent2(PartyBuilder.builder().individual().build())
            .build();

        assertThat(service.getRespondent2Email(caseData)).isEqualTo("respondentsolicitor2@example.com");
    }

    @Test
    void shouldRequireSelectionForRespondent1Notification() {
        var respondent1 = PartyBuilder.builder().individual().build();
        CaseData caseData = CaseDataBuilder.builder().build().toBuilder()
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
        CaseData caseData = CaseDataBuilder.builder().build().toBuilder()
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
        CaseData caseData = CaseDataBuilder.builder().build().toBuilder()
            .respondent2(PartyBuilder.builder().individual().build())
            .defendantDetails(dynamicSelection("Mr. John Rambo"))
            .addRespondent2(YesOrNo.NO)
            .build();

        assertThat(service.shouldNotifyRespondent2(caseData)).isFalse();
    }

    private DynamicList dynamicSelection(String label) {
        DynamicList list = new DynamicList();
        list.setValue(DynamicListElement.dynamicElement(label));
        return list;
    }

    private IdamUserDetails userDetails(String email) {
        IdamUserDetails details = new IdamUserDetails();
        details.setEmail(email);
        return details;
    }
}
