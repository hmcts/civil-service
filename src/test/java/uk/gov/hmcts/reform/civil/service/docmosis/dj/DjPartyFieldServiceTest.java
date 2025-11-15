package uk.gov.hmcts.reform.civil.service.docmosis.dj;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.model.common.DynamicList;
import uk.gov.hmcts.reform.civil.model.common.DynamicListElement;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;

import static org.assertj.core.api.Assertions.assertThat;

class DjPartyFieldServiceTest {

    private final DjPartyFieldService service = new DjPartyFieldService();

    @Test
    void shouldResolveBothDefendants() {
        CaseData caseData = baseCaseDataBuilder()
            .defendantDetails(DynamicList.builder()
                .value(DynamicListElement.builder().label("Both Defendants").build())
                .build())
            .build();

        assertThat(service.resolveRespondent(caseData)).isEqualTo("Resp One, Resp Two");
    }

    @Test
    void shouldResolveRespondent1WhenLip() {
        CaseData caseData = baseCaseDataBuilder()
            .respondent1Represented(YesOrNo.NO)
            .build();

        assertThat(service.resolveRespondent(caseData)).isEqualTo("Resp One");
    }

    @Test
    void shouldResolveRespondent2WhenSelected() {
        Party respondent2 = Party.builder()
            .type(Party.Type.INDIVIDUAL)
            .individualFirstName("Second")
            .individualLastName("Resp")
            .build();

        CaseData caseData = CaseData.builder()
            .respondent1(baseRespondent("Resp", "One"))
            .respondent2(respondent2)
            .respondent1Represented(YesOrNo.YES)
            .defendantDetails(DynamicList.builder()
                .value(DynamicListElement.builder().label(respondent2.getPartyName()).build())
                .build())
            .build();

        assertThat(service.resolveRespondent(caseData)).isEqualTo(respondent2.getPartyName());
    }

    @Test
    void shouldDetectApplicantName() {
        CaseData caseData = CaseDataBuilder.builder()
            .atStateClaimDraft()
            .build();

        assertThat(service.hasApplicantPartyName(caseData)).isTrue();
    }

    private CaseData.CaseDataBuilder<?, ?> baseCaseDataBuilder() {
        Party respondent1 = baseRespondent("Resp", "One");
        Party respondent2 = baseRespondent("Resp", "Two");

        return CaseData.builder()
            .respondent1(respondent1)
            .respondent2(respondent2)
            .respondent1Represented(YesOrNo.YES)
            .defendantDetails(DynamicList.builder()
                .value(DynamicListElement.builder().label(respondent1.getPartyName()).build())
                .build());
    }

    private Party baseRespondent(String firstName, String lastName) {
        return Party.builder()
            .type(Party.Type.INDIVIDUAL)
            .individualFirstName(firstName)
            .individualLastName(lastName)
            .build();
    }
}
