package uk.gov.hmcts.reform.civil.utils;

import uk.gov.hmcts.reform.civil.enums.YesOrNo;
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

    public static boolean isRespondentsResponseSatisfied(CaseData caseData, CaseData updatedCaseData) {

        if (caseData.getGeneralAppRespondentSolicitors() == null
            || updatedCaseData.getRespondentsResponses() == null) {
            return false;
        }

        List<Element<GARespondentResponse>> respondentsResponses = updatedCaseData.getRespondentsResponses();
        int noOfDefendantSolicitors = caseData.getGeneralAppRespondentSolicitors().size();

        if ((noOfDefendantSolicitors == ONE_V_ONE
            && respondentsResponses != null && respondentsResponses.size() == ONE_V_ONE)
            || (updatedCaseData.getIsMultiParty().equals(YesOrNo.NO) && respondentsResponses != null)) {
            return true;
        }

        if (noOfDefendantSolicitors >= ONE_V_TWO && respondentsResponses != null && updatedCaseData.getIsMultiParty().equals(YesOrNo.YES)) {

            List<Element<GASolicitorDetailsGAspec>> resp1SolList = updatedCaseData.getGeneralAppRespondentSolicitors().stream()
                .filter(gaRespondentSolElement -> gaRespondentSolElement.getValue().getOrganisationIdentifier()
                .equals(caseData.getGeneralAppRespondentSolicitors().get(0).getValue().getOrganisationIdentifier())).toList();
            List<Element<GARespondentResponse>> resp1ResponseSolList = updatedCaseData.getRespondentsResponses().stream()
                .filter(gaRespondent1ResponseElement -> resp1SolList.parallelStream().anyMatch(resp1Sol -> resp1Sol.getValue().getId()
                .equals(gaRespondent1ResponseElement.getValue().getGaRespondentDetails()))).toList();
            List<Element<GASolicitorDetailsGAspec>> resp2SolList = updatedCaseData.getGeneralAppRespondentSolicitors().stream()
                .filter(gaRespondentSolElement -> !(gaRespondentSolElement.getValue().getOrganisationIdentifier()
                .equals(caseData.getGeneralAppRespondentSolicitors().get(0).getValue().getOrganisationIdentifier()))).toList();
            List<Element<GARespondentResponse>> resp2ResponseSolList = updatedCaseData.getRespondentsResponses().stream()
                .filter(gaRespondent2ResponseElement -> resp2SolList.parallelStream().anyMatch(resp2Sol -> resp2Sol.getValue().getId()
                .equals(gaRespondent2ResponseElement.getValue().getGaRespondentDetails()))).toList();
            if (!resp1ResponseSolList.isEmpty() && !resp2ResponseSolList.isEmpty()) {
                return true;
            }
        }

        return false;

    }
}
