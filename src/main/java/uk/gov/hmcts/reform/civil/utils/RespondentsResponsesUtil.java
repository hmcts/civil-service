package uk.gov.hmcts.reform.civil.utils;

import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.ga.model.GeneralApplicationCaseData;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.model.genapplication.GARespondentResponse;
import uk.gov.hmcts.reform.civil.model.genapplication.GASolicitorDetailsGAspec;

import java.util.List;

public class RespondentsResponsesUtil {

    private static final int ONE_V_ONE = 1;
    private static final int ONE_V_TWO = 2;

    private RespondentsResponsesUtil() {
        // Utilities class, no instances
    }

    public static boolean isRespondentsResponseSatisfied(GeneralApplicationCaseData caseData,
                                                         GeneralApplicationCaseData updatedCaseData) {
        if (caseData == null || updatedCaseData == null) {
            return false;
        }
        return isSatisfied(
            caseData.getGeneralAppRespondentSolicitors(),
            updatedCaseData.getGeneralAppRespondentSolicitors(),
            updatedCaseData.getRespondentsResponses(),
            caseData.getIsMultiParty(),
            updatedCaseData.getIsMultiParty()
        );
    }

    public static boolean isRespondentsResponseSatisfied(CaseData caseData, CaseData updatedCaseData) {
        if (caseData == null || updatedCaseData == null) {
            return false;
        }
        return isSatisfied(
            caseData.getGeneralAppRespondentSolicitors(),
            updatedCaseData.getGeneralAppRespondentSolicitors(),
            updatedCaseData.getRespondentsResponses(),
            caseData.getIsMultiParty(),
            updatedCaseData.getIsMultiParty()
        );
    }

    private static boolean isSatisfied(List<Element<GASolicitorDetailsGAspec>> originalSolicitors,
                                       List<Element<GASolicitorDetailsGAspec>> updatedSolicitors,
                                       List<Element<GARespondentResponse>> respondentsResponses,
                                       YesOrNo originalIsMultiParty,
                                       YesOrNo updatedIsMultiParty) {

        if (originalSolicitors == null || respondentsResponses == null) {
            return false;
        }

        int noOfDefendantSolicitors = originalSolicitors.size();

        if ((noOfDefendantSolicitors == ONE_V_ONE
            && respondentsResponses.size() == ONE_V_ONE)
            || (YesOrNo.NO.equals(updatedIsMultiParty) && !respondentsResponses.isEmpty())) {
            return true;
        }

        if (noOfDefendantSolicitors >= ONE_V_TWO && YesOrNo.YES.equals(updatedIsMultiParty) && updatedSolicitors != null) {

            List<Element<GASolicitorDetailsGAspec>> resp1SolList = updatedSolicitors.stream()
                .filter(gaRespondentSolElement -> gaRespondentSolElement.getValue().getOrganisationIdentifier()
                    .equals(originalSolicitors.get(0).getValue().getOrganisationIdentifier())).toList();
            List<Element<GARespondentResponse>> resp1ResponseSolList = respondentsResponses.stream()
                .filter(gaRespondent1ResponseElement -> resp1SolList.parallelStream().anyMatch(resp1Sol -> resp1Sol.getValue().getId()
                    .equals(gaRespondent1ResponseElement.getValue().getGaRespondentDetails()))).toList();
            List<Element<GASolicitorDetailsGAspec>> resp2SolList = updatedSolicitors.stream()
                .filter(gaRespondentSolElement -> !(gaRespondentSolElement.getValue().getOrganisationIdentifier()
                    .equals(originalSolicitors.get(0).getValue().getOrganisationIdentifier()))).toList();
            List<Element<GARespondentResponse>> resp2ResponseSolList = respondentsResponses.stream()
                .filter(gaRespondent2ResponseElement -> resp2SolList.parallelStream().anyMatch(resp2Sol -> resp2Sol.getValue().getId()
                    .equals(gaRespondent2ResponseElement.getValue().getGaRespondentDetails()))).toList();
            return !resp1ResponseSolList.isEmpty() && !resp2ResponseSolList.isEmpty();
        }

        return false;
    }
}
