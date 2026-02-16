package uk.gov.hmcts.reform.civil.service.robotics.support;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.dq.Applicant1DQ;
import uk.gov.hmcts.reform.civil.model.dq.Applicant2DQ;
import uk.gov.hmcts.reform.civil.model.dq.FileDirectionsQuestionnaire;
import uk.gov.hmcts.reform.civil.model.dq.Respondent1DQ;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.civil.service.robotics.utils.RoboticsDataUtil.APPLICANT2_ID;

class RoboticsDirectionsQuestionnaireSupportTest {

    @Test
    void getRespondent1DQOrDefaultReturnsExistingValue() {
        Respondent1DQ dq = new Respondent1DQ();
        CaseData caseData = CaseDataBuilder.builder().build();
        caseData.setRespondent1DQ(dq);

        assertThat(RoboticsDirectionsQuestionnaireSupport.getRespondent1DQOrDefault(caseData))
            .isEqualTo(dq);
    }

    @Test
    void getRespondent1DQOrDefaultReturnsNullWhenMissing() {
        CaseData caseData = CaseDataBuilder.builder().build();

        assertThat(RoboticsDirectionsQuestionnaireSupport.getRespondent1DQOrDefault(caseData)).isNull();
    }

    @Test
    void getApplicant1DQOrDefaultReturnsExistingValue() {
        Applicant1DQ dq = new Applicant1DQ();
        CaseData caseData = CaseDataBuilder.builder().build();
        caseData.setApplicant1DQ(dq);

        assertThat(RoboticsDirectionsQuestionnaireSupport.getApplicant1DQOrDefault(caseData))
            .isEqualTo(dq);
    }

    @Test
    void getApplicant1DQOrDefaultReturnsNullWhenMissing() {
        CaseData caseData = CaseDataBuilder.builder().build();

        assertThat(RoboticsDirectionsQuestionnaireSupport.getApplicant1DQOrDefault(caseData)).isNull();
    }

    @Test
    void prepareApplicantsDetailsHandlesTwoApplicantsProceedingIndependently() {
        CaseData caseData = CaseDataBuilder.builder()
            .multiPartyClaimTwoApplicants()
            .atStateApplicant2RespondToDefenceAndProceed_2v1()
            .build();
        caseData.setApplicant1ProceedWithClaimMultiParty2v1(uk.gov.hmcts.reform.civil.enums.YesOrNo.NO);
        caseData.setApplicant2ProceedWithClaimMultiParty2v1(uk.gov.hmcts.reform.civil.enums.YesOrNo.YES);
        LocalDateTime responseDate = LocalDateTime.of(2024, 2, 20, 10, 0);
        caseData.setApplicant2ResponseDate(responseDate);
        Applicant2DQ applicant2DQ = new Applicant2DQ();
        caseData.setApplicant2DQ(applicant2DQ);

        var details = RoboticsDirectionsQuestionnaireSupport.prepareApplicantsDetails(caseData);

        assertThat(details).hasSize(1);
        assertThat(details.getFirst().getLitigiousPartyID()).isEqualTo(APPLICANT2_ID);
        assertThat(details.getFirst().getResponseDate()).isEqualTo(responseDate);
    }

    @Test
    void prepareApplicantsDetailsForSpecTwoVOneAddsBothApplicants() {
        CaseData caseData = CaseDataBuilder.builder()
            .multiPartyClaimTwoApplicants()
            .atStateBothApplicantsRespondToDefenceAndProceed_2v1_SPEC()
            .setClaimTypeToSpecClaim()
            .build();
        caseData.setApplicant1ProceedWithClaimSpec2v1(uk.gov.hmcts.reform.civil.enums.YesOrNo.YES);
        caseData.setApplicant1ResponseDate(LocalDateTime.of(2024, 3, 1, 9, 0));
        caseData.setApplicant2ResponseDate(LocalDateTime.of(2024, 3, 1, 9, 0));
        caseData.setApplicant1DQ(new Applicant1DQ());
        caseData.setApplicant2DQ(new Applicant2DQ());

        var details = RoboticsDirectionsQuestionnaireSupport.prepareApplicantsDetails(caseData);

        assertThat(details).hasSize(2);
    }

    @Test
    void isStayClaimReturnsTrueWhenRequested() {
        FileDirectionsQuestionnaire dq = new FileDirectionsQuestionnaire();
        dq.setOneMonthStayRequested(uk.gov.hmcts.reform.civil.enums.YesOrNo.YES);
        Applicant1DQ base = new Applicant1DQ();
        base.setApplicant1DQFileDirectionsQuestionnaire(dq);

        assertThat(RoboticsDirectionsQuestionnaireSupport.isStayClaim(base)).isTrue();
    }

    @Test
    void getPreferredCourtCodeReturnsEmptyWhenMissing() {
        Applicant1DQ base = new Applicant1DQ();

        assertThat(RoboticsDirectionsQuestionnaireSupport.getPreferredCourtCode(base)).isEqualTo("");
    }

    @Test
    void getApplicant2DQOrDefaultReturnsExistingValue() {
        Applicant2DQ dq = new Applicant2DQ();
        CaseData caseData = CaseDataBuilder.builder().build();
        caseData.setApplicant2DQ(dq);

        assertThat(RoboticsDirectionsQuestionnaireSupport.getApplicant2DQOrDefault(caseData))
            .isEqualTo(dq);
    }
}
