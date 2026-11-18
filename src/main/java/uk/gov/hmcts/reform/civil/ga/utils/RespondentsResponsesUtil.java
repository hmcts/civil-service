package uk.gov.hmcts.reform.civil.ga.utils;

import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.ga.model.GeneralApplicationCaseData;
import uk.gov.hmcts.reform.civil.ga.model.genapplication.GARespondentResponse;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.model.genapplication.GASolicitorDetailsGAspec;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.mapping;
import static java.util.stream.Collectors.toSet;

public class RespondentsResponsesUtil {

    private static final int ONE_V_ONE = 1;

    private RespondentsResponsesUtil() {
        // Utilities class, no instances
    }

    /**
     * Returns true once every respondent party has responded to the application.
     *
     * <p>Respondent solicitors are grouped by organisation and the responses are complete when each
     * organisation has at least one response from one of its solicitors. A 1v2 claim where both
     * defendants share a legal representative therefore needs a single response, while a 1v2 claim
     * with different representatives needs one from each organisation.</p>
     */
    public static boolean isRespondentsResponseSatisfied(GeneralApplicationCaseData caseData, GeneralApplicationCaseData updatedCaseData) {

        if (caseData.getGeneralAppRespondentSolicitors() == null
            || updatedCaseData.getRespondentsResponses() == null) {
            return false;
        }

        List<Element<GARespondentResponse>> respondentsResponses = updatedCaseData.getRespondentsResponses();
        int noOfDefendantSolicitors = caseData.getGeneralAppRespondentSolicitors().size();

        if ((noOfDefendantSolicitors == ONE_V_ONE && respondentsResponses.size() == ONE_V_ONE)
            || YesOrNo.NO.equals(updatedCaseData.getIsMultiParty())) {
            return true;
        }

        if (!YesOrNo.YES.equals(updatedCaseData.getIsMultiParty())
            || updatedCaseData.getGeneralAppRespondentSolicitors() == null
            || updatedCaseData.getGeneralAppRespondentSolicitors().isEmpty()) {
            return false;
        }

        Set<String> responderIds = respondentsResponses.stream()
            .map(Element::getValue)
            .filter(Objects::nonNull)
            .map(GARespondentResponse::getGaRespondentDetails)
            .filter(Objects::nonNull)
            .collect(toSet());

        Collection<Set<String>> solicitorIdsByParty = groupSolicitorIdsByParty(
            updatedCaseData.getGeneralAppRespondentSolicitors()).values();

        return solicitorIdsByParty.stream()
            .allMatch(solicitorIds -> solicitorIds.stream().anyMatch(responderIds::contains));
    }

    /**
     * Groups solicitor ids by organisation. A solicitor without an organisation (for example a LiP)
     * is treated as a party of their own.
     */
    private static Map<String, Set<String>> groupSolicitorIdsByParty(List<Element<GASolicitorDetailsGAspec>> solicitors) {
        return solicitors.stream()
            .map(Element::getValue)
            .filter(Objects::nonNull)
            .collect(groupingBy(
                solicitor -> solicitor.getOrganisationIdentifier() != null
                    ? "org:" + solicitor.getOrganisationIdentifier()
                    : "user:" + solicitor.getId(),
                mapping(GASolicitorDetailsGAspec::getId, toSet())
            ));
    }
}
