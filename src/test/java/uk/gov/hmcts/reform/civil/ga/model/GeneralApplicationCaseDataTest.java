package uk.gov.hmcts.reform.civil.ga.model;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.civil.sampledata.GeneralApplicationCaseDataBuilder;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowFlag.POST_JUDGE_ORDER_LIP_APPLICANT;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowFlag.POST_JUDGE_ORDER_LIP_RESPONDENT;

class GeneralApplicationCaseDataTest {

    @Test
    void shouldReturnEmptyPartyNameWhenApplicantOrRespondentIsMissing() {
        GeneralApplicationCaseData caseData = GeneralApplicationCaseDataBuilder.builder()
            .applicant1(null)
            .respondent1(null)
            .build();

        assertThat(List.of(
            caseData.getPartyName(true, POST_JUDGE_ORDER_LIP_APPLICANT, caseData),
            caseData.getPartyName(false, POST_JUDGE_ORDER_LIP_APPLICANT, caseData),
            caseData.getPartyName(true, POST_JUDGE_ORDER_LIP_RESPONDENT, caseData),
            caseData.getPartyName(false, POST_JUDGE_ORDER_LIP_RESPONDENT, caseData)
        )).containsExactly("", "", "", "");
    }
}
