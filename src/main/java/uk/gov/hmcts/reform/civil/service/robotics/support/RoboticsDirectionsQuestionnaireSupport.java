package uk.gov.hmcts.reform.civil.service.robotics.support;

import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.ClaimantResponseDetails;
import uk.gov.hmcts.reform.civil.model.dq.Applicant1DQ;
import uk.gov.hmcts.reform.civil.model.dq.Applicant2DQ;
import uk.gov.hmcts.reform.civil.model.dq.DQ;
import uk.gov.hmcts.reform.civil.model.dq.FileDirectionsQuestionnaire;
import uk.gov.hmcts.reform.civil.model.dq.RequestedCourt;
import uk.gov.hmcts.reform.civil.model.dq.Respondent1DQ;
import uk.gov.hmcts.reform.civil.model.dq.Respondent2DQ;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static java.lang.String.format;
import static uk.gov.hmcts.reform.civil.enums.CaseCategory.SPEC_CLAIM;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.TWO_V_ONE;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.getMultiPartyScenario;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;
import static uk.gov.hmcts.reform.civil.service.robotics.utils.RoboticsDataUtil.APPLICANT2_ID;
import static uk.gov.hmcts.reform.civil.service.robotics.utils.RoboticsDataUtil.APPLICANT_ID;

public final class RoboticsDirectionsQuestionnaireSupport {

    private RoboticsDirectionsQuestionnaireSupport() {
        // utility class
    }

    public static List<ClaimantResponseDetails> prepareApplicantsDetails(CaseData caseData) {
        List<ClaimantResponseDetails> applicantsDetails = new ArrayList<>();
        if (getMultiPartyScenario(caseData).equals(TWO_V_ONE)) {
            if (YES.equals(caseData.getApplicant1ProceedWithClaimMultiParty2v1())
                || YES.equals(caseData.getApplicant1ProceedWithClaimSpec2v1())) {
                applicantsDetails.add(new ClaimantResponseDetails()
                                          .setDq(getApplicant1DQOrDefault(caseData))
                                          .setLitigiousPartyID(APPLICANT_ID)
                                          .setResponseDate(caseData.getApplicant1ResponseDate()));
            }
            if (YES.equals(caseData.getApplicant2ProceedWithClaimMultiParty2v1())
                || YES.equals(caseData.getApplicant1ProceedWithClaimSpec2v1())) {
                applicantsDetails.add(new ClaimantResponseDetails()
                                          .setDq(getApplicant2DQOrDefault(caseData))
                                          .setLitigiousPartyID(APPLICANT2_ID)
                                          .setResponseDate(
                                              SPEC_CLAIM.equals(caseData.getCaseAccessCategory())
                                                  ? caseData.getApplicant1ResponseDate()
                                                  : caseData.getApplicant2ResponseDate()));
            }
        } else {
            applicantsDetails.add(new ClaimantResponseDetails()
                                      .setDq(getApplicant1DQOrDefault(caseData))
                                      .setLitigiousPartyID(APPLICANT_ID)
                                      .setResponseDate(caseData.getApplicant1ResponseDate()));
        }
        return applicantsDetails;
    }

    public static boolean isStayClaim(DQ dq) {
        return Optional.ofNullable(dq)
            .map(DQ::getFileDirectionQuestionnaire)
            .map(FileDirectionsQuestionnaire::getOneMonthStayRequested)
            .orElse(NO) == YES;
    }

    public static String getPreferredCourtCode(DQ dq) {
        return Optional.ofNullable(dq)
            .map(DQ::getRequestedCourt)
            .map(RequestedCourt::getResponseCourtCode)
            .orElse("");
    }

    public static String prepareEventDetailsText(DQ dq, String preferredCourtCode) {
        return format(
            "preferredCourtCode: %s; stayClaim: %s",
            preferredCourtCode,
            isStayClaim(dq)
        );
    }

    public static Respondent1DQ getRespondent1DQOrDefault(CaseData caseData) {
        if (caseData == null) {
            return null;
        }
        return caseData.getRespondent1DQ();
    }

    public static Respondent2DQ getRespondent2DQOrDefault(CaseData caseData) {
        if (caseData == null) {
            return null;
        }
        return caseData.getRespondent2DQ();
    }

    public static Applicant1DQ getApplicant1DQOrDefault(CaseData caseData) {
        if (caseData == null) {
            return null;
        }
        return caseData.getApplicant1DQ();
    }

    public static Applicant2DQ getApplicant2DQOrDefault(CaseData caseData) {
        if (caseData == null) {
            return null;
        }
        return caseData.getApplicant2DQ();
    }
}
