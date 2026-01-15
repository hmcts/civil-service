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
            .defendantDetails(dynamicListWithLabel("Both Defendants"))
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
        Party respondent2 = baseRespondent("Second", "Resp");

        CaseData caseData = CaseDataBuilder.builder().build().toBuilder()
            .respondent1(baseRespondent("Resp", "One"))
            .respondent2(respondent2)
            .respondent1Represented(YesOrNo.YES)
            .defendantDetails(dynamicListWithLabel(respondent2.getPartyName()))
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

        return CaseDataBuilder.builder().build().toBuilder()
            .respondent1(respondent1)
            .respondent2(respondent2)
            .respondent1Represented(YesOrNo.YES)
            .defendantDetails(dynamicListWithLabel(respondent1.getPartyName()));
    }

    private Party baseRespondent(String firstName, String lastName) {
        Party party = new Party();
        party.setType(Party.Type.INDIVIDUAL);
        party.setIndividualFirstName(firstName);
        party.setIndividualLastName(lastName);
        return party;
    }

    private DynamicList dynamicListWithLabel(String label) {
        DynamicListElement element = new DynamicListElement();
        element.setLabel(label);
        DynamicList list = new DynamicList();
        list.setValue(element);
        return list;
    }
}
