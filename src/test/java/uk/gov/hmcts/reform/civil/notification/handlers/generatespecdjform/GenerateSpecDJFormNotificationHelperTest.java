package uk.gov.hmcts.reform.civil.notification.handlers.generatespecdjform;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.model.common.DynamicList;
import uk.gov.hmcts.reform.civil.model.common.DynamicListElement;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.sampledata.PartyBuilder;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class GenerateSpecDJFormNotificationHelperTest {

    private GenerateSpecDJFormNotificationHelper helper;

    @BeforeEach
    void setUp() {
        helper = new GenerateSpecDJFormNotificationHelper();
    }

    @Test
    void shouldNotifyApplicantSolicitorReceivedForSingleDefendant() {
        CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotified().build();

        assertThat(helper.shouldNotifyApplicantSolicitorReceived(caseData)).isTrue();

        CaseData lipCase = caseData.toBuilder()
            .applicant1Represented(YesOrNo.NO)
            .build();

        assertThat(helper.shouldNotifyApplicantSolicitorReceived(lipCase)).isFalse();
    }

    @Test
    void shouldNotifyApplicantSolicitorReceivedForBothDefendants() {
        CaseData caseData = multiPartyCaseData()
            .toBuilder()
            .defendantDetailsSpec(new DynamicList(bothDefendants(), List.of(bothDefendants())))
            .build();

        assertThat(helper.shouldNotifyApplicantSolicitorReceived(caseData)).isTrue();
    }

    @Test
    void shouldNotifyApplicantSolicitorRequestedOnlyWhenSingleDefendantSelection() {
        CaseData singleSelection = caseDataWithSelection(true, YesOrNo.NO);
        CaseData bothSelected = multiPartyCaseData()
            .toBuilder()
            .defendantDetailsSpec(new DynamicList(bothDefendants(), List.of(bothDefendants())))
            .build();

        assertThat(helper.shouldNotifyApplicantSolicitorRequested(singleSelection)).isTrue();
        assertThat(helper.shouldNotifyApplicantSolicitorRequested(bothSelected)).isFalse();
        assertThat(helper.shouldNotifyApplicantSolicitorRequested(CaseDataBuilder.builder().atStateClaimDetailsNotified().build()))
            .isFalse();
    }

    @Test
    void shouldNotifyRespondentSolicitorOneReceivedWhenNoSecondDefendantOrBothSelected() {
        CaseData singleDefendant = CaseDataBuilder.builder().atStateClaimDetailsNotified().build();
        CaseData bothSelected = multiPartyCaseData()
            .toBuilder()
            .defendantDetailsSpec(new DynamicList(bothDefendants(), List.of(bothDefendants())))
            .build();
        CaseData singleSelection = caseDataWithSelection(false, YesOrNo.NO);

        assertThat(helper.shouldNotifyRespondentSolicitorOneReceived(singleDefendant)).isTrue();
        assertThat(helper.shouldNotifyRespondentSolicitorOneReceived(bothSelected)).isTrue();
        assertThat(helper.shouldNotifyRespondentSolicitorOneReceived(singleSelection)).isFalse();
    }

    @Test
    void shouldNotifyRespondentSolicitorOneRequestedMatchingLegacyLogic() {
        CaseData firstSelected = caseDataWithSelection(true, YesOrNo.NO);
        CaseData secondSelectedSameSolicitor = caseDataWithSelection(false, YesOrNo.YES);
        CaseData secondSelectedDifferentSolicitor = caseDataWithSelection(false, YesOrNo.NO);
        CaseData bothSelected = multiPartyCaseData()
            .toBuilder()
            .defendantDetailsSpec(new DynamicList(bothDefendants(), List.of(bothDefendants())))
            .build();

        assertThat(helper.shouldNotifyRespondentSolicitorOneRequested(firstSelected)).isTrue();
        assertThat(helper.shouldNotifyRespondentSolicitorOneRequested(secondSelectedSameSolicitor)).isTrue();
        assertThat(helper.shouldNotifyRespondentSolicitorOneRequested(secondSelectedDifferentSolicitor)).isFalse();
        assertThat(helper.shouldNotifyRespondentSolicitorOneRequested(bothSelected)).isFalse();
        assertThat(helper.shouldNotifyRespondentSolicitorOneRequested(CaseDataBuilder.builder().atStateClaimDetailsNotified().build()))
            .isFalse();
    }

    private CaseData multiPartyCaseData() {
        Party respondent1 = new PartyBuilder().individual().build();
        Party respondent2 = new PartyBuilder().individual("Second").build();
        return CaseDataBuilder.builder().atStateClaimDetailsNotified()
            .respondent1(respondent1)
            .respondent2(respondent2)
            .respondent1Represented(YesOrNo.YES)
            .respondent2Represented(YesOrNo.YES)
            .addRespondent2(YesOrNo.YES)
            .build();
    }

    private CaseData caseDataWithSelection(boolean isFirstDefendant, YesOrNo sameSolicitor) {
        CaseData base = multiPartyCaseData();
        String label = isFirstDefendant ? base.getRespondent1().getPartyName() : base.getRespondent2().getPartyName();
        String code = isFirstDefendant ? "first" : "second";
        DynamicListElement selection = DynamicListElement.dynamicElementFromCode(code, label);
        return base.toBuilder()
            .respondent2SameLegalRepresentative(sameSolicitor)
            .defendantDetailsSpec(new DynamicList(selection, List.of(selection)))
            .build();
    }

    private DynamicListElement bothDefendants() {
        return DynamicListElement.dynamicElement("Both Defendants");
    }
}
