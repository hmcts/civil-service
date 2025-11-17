package uk.gov.hmcts.reform.civil.service.robotics.support;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.dq.Applicant1DQ;
import uk.gov.hmcts.reform.civil.model.dq.Respondent1DQ;

import static org.assertj.core.api.Assertions.assertThat;

class RoboticsDirectionsQuestionnaireSupportTest {

    @Test
    void getRespondent1DQOrDefaultReturnsExistingValue() {
        Respondent1DQ dq = Respondent1DQ.builder().build();
        CaseData caseData = CaseData.builder()
            .respondent1DQ(dq)
            .build();

        assertThat(RoboticsDirectionsQuestionnaireSupport.getRespondent1DQOrDefault(caseData))
            .isEqualTo(dq);
    }

    @Test
    void getRespondent1DQOrDefaultReturnsNullWhenMissing() {
        CaseData caseData = CaseData.builder().build();

        assertThat(RoboticsDirectionsQuestionnaireSupport.getRespondent1DQOrDefault(caseData)).isNull();
    }

    @Test
    void getApplicant1DQOrDefaultReturnsExistingValue() {
        Applicant1DQ dq = Applicant1DQ.builder().build();
        CaseData caseData = CaseData.builder()
            .applicant1DQ(dq)
            .build();

        assertThat(RoboticsDirectionsQuestionnaireSupport.getApplicant1DQOrDefault(caseData))
            .isEqualTo(dq);
    }

    @Test
    void getApplicant1DQOrDefaultReturnsNullWhenMissing() {
        CaseData caseData = CaseData.builder().build();

        assertThat(RoboticsDirectionsQuestionnaireSupport.getApplicant1DQOrDefault(caseData)).isNull();
    }
}
