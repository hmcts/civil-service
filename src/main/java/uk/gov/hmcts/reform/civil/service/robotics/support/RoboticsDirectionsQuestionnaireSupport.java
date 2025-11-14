package uk.gov.hmcts.reform.civil.service.robotics.support;

import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.ClaimantResponseDetails;
import uk.gov.hmcts.reform.civil.model.DeterWithoutHearing;
import uk.gov.hmcts.reform.civil.model.citizenui.CaseDataLiP;
import uk.gov.hmcts.reform.civil.model.citizenui.ClaimantLiPResponse;
import uk.gov.hmcts.reform.civil.model.citizenui.DQExtraDetailsLip;
import uk.gov.hmcts.reform.civil.model.citizenui.HearingSupportLip;
import uk.gov.hmcts.reform.civil.model.citizenui.RespondentLiPResponse;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.model.defaultjudgment.CaseLocationCivil;
import uk.gov.hmcts.reform.civil.model.dq.Applicant1DQ;
import uk.gov.hmcts.reform.civil.model.dq.Applicant2DQ;
import uk.gov.hmcts.reform.civil.model.dq.DQ;
import uk.gov.hmcts.reform.civil.model.dq.FileDirectionsQuestionnaire;
import uk.gov.hmcts.reform.civil.model.dq.HearingSupport;
import uk.gov.hmcts.reform.civil.model.dq.RequestedCourt;
import uk.gov.hmcts.reform.civil.model.dq.Respondent1DQ;
import uk.gov.hmcts.reform.civil.model.dq.Respondent2DQ;
import uk.gov.hmcts.reform.civil.model.dq.RequirementsLip;
import uk.gov.hmcts.reform.civil.enums.dq.SupportRequirements;
import uk.gov.hmcts.reform.civil.model.dq.Witness;
import uk.gov.hmcts.reform.civil.model.dq.Witnesses;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

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
                applicantsDetails.add(ClaimantResponseDetails.builder()
                                          .dq(getApplicant1DQOrDefault(caseData))
                                          .litigiousPartyID(APPLICANT_ID)
                                          .responseDate(caseData.getApplicant1ResponseDate())
                                          .build());
            }
            if (YES.equals(caseData.getApplicant2ProceedWithClaimMultiParty2v1())
                || YES.equals(caseData.getApplicant1ProceedWithClaimSpec2v1())) {
                applicantsDetails.add(ClaimantResponseDetails.builder()
                                          .dq(getApplicant2DQOrDefault(caseData))
                                          .litigiousPartyID(APPLICANT2_ID)
                                          .responseDate(
                                              SPEC_CLAIM.equals(caseData.getCaseAccessCategory())
                                                  ? caseData.getApplicant1ResponseDate()
                                                  : caseData.getApplicant2ResponseDate())
                                          .build());
            }
        } else {
            applicantsDetails.add(ClaimantResponseDetails.builder()
                                      .dq(getApplicant1DQOrDefault(caseData))
                                      .litigiousPartyID(APPLICANT_ID)
                                      .responseDate(caseData.getApplicant1ResponseDate())
                                      .build());
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
        if (caseData.getRespondent1DQ() != null) {
            return caseData.getRespondent1DQ();
        }
        if (!caseData.isRespondent1NotRepresented()) {
            return null;
        }
        return buildRespondent1LipDQ(caseData);
    }

    public static Respondent2DQ getRespondent2DQOrDefault(CaseData caseData) {
        if (caseData == null) {
            return null;
        }
        if (caseData.getRespondent2DQ() != null) {
            return caseData.getRespondent2DQ();
        }
        if (!caseData.isRespondent2NotRepresented()) {
            return null;
        }
        return Respondent2DQ.builder()
            .respondent2DQRequestedCourt(buildRequestedCourt(caseData))
            .respondent2DQFileDirectionsQuestionnaire(buildDefaultFileDirectionsQuestionnaire())
            .build();
    }

    public static Applicant1DQ getApplicant1DQOrDefault(CaseData caseData) {
        if (caseData == null) {
            return null;
        }
        if (caseData.getApplicant1DQ() != null) {
            return caseData.getApplicant1DQ();
        }
        if (!caseData.isApplicant1NotRepresented()) {
            return null;
        }
        return buildApplicant1LipDQ(caseData);
    }

    public static Applicant2DQ getApplicant2DQOrDefault(CaseData caseData) {
        if (caseData == null) {
            return null;
        }
        return caseData.getApplicant2DQ();
    }

    private static RequestedCourt buildRequestedCourt(CaseData caseData) {
        CaseLocationCivil location = Optional.ofNullable(caseData)
            .map(CaseData::getCaseManagementLocation)
            .orElse(null);
        return RequestedCourt.builder()
            .responseCourtCode(Optional.ofNullable(location).map(CaseLocationCivil::getBaseLocation).orElse(""))
            .caseLocation(location)
            .build();
    }

    private static FileDirectionsQuestionnaire buildDefaultFileDirectionsQuestionnaire() {
        return FileDirectionsQuestionnaire.builder()
            .oneMonthStayRequested(NO)
            .build();
    }

    private static Respondent1DQ buildRespondent1LipDQ(CaseData caseData) {
        RespondentLiPResponse lipResponse = Optional.ofNullable(caseData)
            .map(CaseData::getCaseDataLiP)
            .map(CaseDataLiP::getRespondent1LiPResponse)
            .orElse(null);
        DQExtraDetailsLip extraDetails = Optional.ofNullable(lipResponse)
            .map(RespondentLiPResponse::getRespondent1DQExtraDetails)
            .orElse(null);
        HearingSupport hearingSupport = buildHearingSupport(Optional.ofNullable(lipResponse)
            .map(RespondentLiPResponse::getRespondent1DQHearingSupportLip)
            .orElse(null));
        return Respondent1DQ.builder()
            .respondent1DQFileDirectionsQuestionnaire(buildFileDirectionsQuestionnaire(extraDetails))
            .respondent1DQRequestedCourt(buildRequestedCourt(caseData))
            .respondent1DQWitnesses(buildWitnesses(
                caseData.getRespondent1DQWitnessesRequiredSpec(),
                caseData.getRespondent1DQWitnessesDetailsSpec(),
                caseData.getRespondent1DQWitnessesSmallClaim()))
            .respondent1DQHearingSupport(hearingSupport)
            .deterWithoutHearingRespondent1(buildDeterWithoutHearing(extraDetails))
            .build();
    }

    private static Applicant1DQ buildApplicant1LipDQ(CaseData caseData) {
        ClaimantLiPResponse lipResponse = Optional.ofNullable(caseData)
            .map(CaseData::getCaseDataLiP)
            .map(CaseDataLiP::getApplicant1LiPResponse)
            .orElse(null);
        DQExtraDetailsLip extraDetails = Optional.ofNullable(lipResponse)
            .map(ClaimantLiPResponse::getApplicant1DQExtraDetails)
            .orElse(null);
        HearingSupport hearingSupport = buildHearingSupport(Optional.ofNullable(lipResponse)
            .map(ClaimantLiPResponse::getApplicant1DQHearingSupportLip)
            .orElse(null));
        return Applicant1DQ.builder()
            .applicant1DQFileDirectionsQuestionnaire(buildFileDirectionsQuestionnaire(extraDetails))
            .applicant1DQRequestedCourt(buildRequestedCourt(caseData))
            .applicant1DQWitnesses(buildWitnesses(
                null,
                null,
                caseData.getApplicant1DQWitnessesSmallClaim()))
            .applicant1DQHearingSupport(hearingSupport)
            .deterWithoutHearing(buildDeterWithoutHearing(extraDetails))
            .build();
    }

    private static FileDirectionsQuestionnaire buildFileDirectionsQuestionnaire(DQExtraDetailsLip extraDetailsLip) {
        if (extraDetailsLip == null) {
            return buildDefaultFileDirectionsQuestionnaire();
        }
        return FileDirectionsQuestionnaire.builder()
            .oneMonthStayRequested(Optional.ofNullable(extraDetailsLip.getRequestExtra4weeks()).orElse(NO))
            .build();
    }

    private static DeterWithoutHearing buildDeterWithoutHearing(DQExtraDetailsLip extraDetailsLip) {
        if (extraDetailsLip == null) {
            return null;
        }
        if (extraDetailsLip.getDeterminationWithoutHearingRequired() == null
            && extraDetailsLip.getDeterminationWithoutHearingReason() == null) {
            return null;
        }
        return DeterWithoutHearing.builder()
            .deterWithoutHearingYesNo(extraDetailsLip.getDeterminationWithoutHearingRequired())
            .deterWithoutHearingWhyNot(extraDetailsLip.getDeterminationWithoutHearingReason())
            .build();
    }

    private static Witnesses buildWitnesses(YesOrNo required, List<Element<Witness>> details, Witnesses smallClaimWitnesses) {
        if (smallClaimWitnesses != null) {
            return smallClaimWitnesses;
        }
        if (required == null && (details == null || details.isEmpty())) {
            return null;
        }
        return Witnesses.builder()
            .witnessesToAppear(required)
            .details(details)
            .build();
    }

    private static HearingSupport buildHearingSupport(HearingSupportLip hearingSupportLip) {
        if (hearingSupportLip == null) {
            return null;
        }
        List<SupportRequirements> requirements = Optional.ofNullable(hearingSupportLip.getRequirementsLip())
            .map(reqs -> reqs.stream()
                .map(Element::getValue)
                .filter(java.util.Objects::nonNull)
                .flatMap(value -> Optional.ofNullable(value.getRequirements()).stream().flatMap(List::stream))
                .toList())
            .orElse(Collections.emptyList());
        RequirementsLip firstRequirement = Optional.ofNullable(hearingSupportLip.getRequirementsLip())
            .flatMap(reqs -> reqs.stream()
                .map(Element::getValue)
                .filter(java.util.Objects::nonNull)
                .findFirst())
            .orElse(null);
        return HearingSupport.builder()
            .supportRequirements(hearingSupportLip.getSupportRequirementLip())
            .requirements(requirements.isEmpty() ? null : requirements)
            .signLanguageRequired(Optional.ofNullable(firstRequirement).map(RequirementsLip::getSignLanguageRequired).orElse(null))
            .languageToBeInterpreted(Optional.ofNullable(firstRequirement).map(RequirementsLip::getLanguageToBeInterpreted).orElse(null))
            .otherSupport(Optional.ofNullable(firstRequirement).map(RequirementsLip::getOtherSupport).orElse(null))
            .build();
    }
}
