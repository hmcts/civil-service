package uk.gov.hmcts.reform.civil.service.robotics.support;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.civil.enums.CaseCategory;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.enums.dq.SupportRequirements;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.citizenui.CaseDataLiP;
import uk.gov.hmcts.reform.civil.model.citizenui.ClaimantLiPResponse;
import uk.gov.hmcts.reform.civil.model.citizenui.DQExtraDetailsLip;
import uk.gov.hmcts.reform.civil.model.citizenui.HearingSupportLip;
import uk.gov.hmcts.reform.civil.model.citizenui.RespondentLiPResponse;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.model.defaultjudgment.CaseLocationCivil;
import uk.gov.hmcts.reform.civil.model.dq.Applicant1DQ;
import uk.gov.hmcts.reform.civil.model.dq.Respondent1DQ;
import uk.gov.hmcts.reform.civil.model.dq.RequirementsLip;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;

class RoboticsDirectionsQuestionnaireSupportTest {

    @Test
    void getRespondent1DQOrDefaultBuildsFromLipData() {
        CaseData caseData = CaseData.builder()
            .caseAccessCategory(CaseCategory.SPEC_CLAIM)
            .respondent1Represented(NO)
            .caseManagementLocation(CaseLocationCivil.builder().baseLocation("372").region("4").build())
            .caseDataLiP(CaseDataLiP.builder()
                .respondent1LiPResponse(RespondentLiPResponse.builder()
                    .respondent1DQExtraDetails(DQExtraDetailsLip.builder()
                        .requestExtra4weeks(YES)
                        .determinationWithoutHearingRequired(YES)
                        .determinationWithoutHearingReason("Remote")
                        .build())
                    .respondent1DQHearingSupportLip(HearingSupportLip.builder()
                        .supportRequirementLip(YES)
                        .requirementsLip(List.of(
                            element(RequirementsLip.builder()
                                .requirements(List.of(SupportRequirements.DISABLED_ACCESS))
                                .signLanguageRequired("BSL")
                                .languageToBeInterpreted("Welsh")
                                .otherSupport("Other support")
                                .build())
                        ))
                        .build())
                    .build())
                .build())
            .build();

        Respondent1DQ dq = RoboticsDirectionsQuestionnaireSupport.getRespondent1DQOrDefault(caseData);

        assertThat(dq).isNotNull();
        assertThat(dq.getFileDirectionQuestionnaire().getOneMonthStayRequested()).isEqualTo(YES);
        assertThat(dq.getRequestedCourt()).isNotNull();
        assertThat(dq.getRequestedCourt().getResponseCourtCode()).isEqualTo("372");
        assertThat(dq.getHearingSupport()).isNotNull();
        assertThat(dq.getHearingSupport().getSupportRequirements()).isEqualTo(YES);
        assertThat(dq.getHearingSupport().getRequirements()).containsExactly(SupportRequirements.DISABLED_ACCESS);
    }

    @Test
    void getApplicant1DQOrDefaultBuildsFromLipData() {
        CaseData caseData = CaseData.builder()
            .caseAccessCategory(CaseCategory.SPEC_CLAIM)
            .applicant1Represented(NO)
            .caseManagementLocation(CaseLocationCivil.builder().baseLocation("372").region("4").build())
            .caseDataLiP(CaseDataLiP.builder()
                .applicant1LiPResponse(ClaimantLiPResponse.builder()
                    .applicant1DQExtraDetails(DQExtraDetailsLip.builder()
                        .requestExtra4weeks(NO)
                        .build())
                    .build())
                .build())
            .build();

        Applicant1DQ dq = RoboticsDirectionsQuestionnaireSupport.getApplicant1DQOrDefault(caseData);

        assertThat(dq).isNotNull();
        assertThat(dq.getFileDirectionQuestionnaire().getOneMonthStayRequested()).isEqualTo(NO);
        assertThat(dq.getRequestedCourt()).isNotNull();
        assertThat(dq.getRequestedCourt().getResponseCourtCode()).isEqualTo("372");
    }

    private static <T> Element<T> element(T value) {
        return Element.<T>builder().value(value).build();
    }
}
