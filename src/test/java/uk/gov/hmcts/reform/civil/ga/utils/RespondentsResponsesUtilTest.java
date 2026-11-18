package uk.gov.hmcts.reform.civil.ga.utils;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.ga.model.GeneralApplicationCaseData;
import uk.gov.hmcts.reform.civil.ga.model.genapplication.GARespondentResponse;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.model.genapplication.GASolicitorDetailsGAspec;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;
import static uk.gov.hmcts.reform.civil.ga.utils.RespondentsResponsesUtil.isRespondentsResponseSatisfied;
import static uk.gov.hmcts.reform.civil.utils.ElementUtils.element;

class RespondentsResponsesUtilTest {

    private static final String DEF1_SOL = "def1-sol";
    private static final String DEF1_SOL_COLLEAGUE = "def1-sol-colleague";
    private static final String DEF2_SOL = "def2-sol";
    private static final String ORG_1 = "org1";
    private static final String ORG_2 = "org2";

    @Test
    void shouldReturnFalse_whenNoRespondentSolicitors() {
        GeneralApplicationCaseData caseData = new GeneralApplicationCaseData()
            .isMultiParty(NO)
            .respondentsResponses(responses(DEF1_SOL));

        assertThat(isRespondentsResponseSatisfied(caseData, caseData)).isFalse();
    }

    @Test
    void shouldReturnFalse_whenNoResponses() {
        GeneralApplicationCaseData caseData = new GeneralApplicationCaseData()
            .isMultiParty(YES)
            .generalAppRespondentSolicitors(solicitors(sol(DEF1_SOL, ORG_1)));

        assertThat(isRespondentsResponseSatisfied(caseData, caseData)).isFalse();
    }

    @Test
    void shouldReturnTrue_for1v1_withOneResponse() {
        GeneralApplicationCaseData caseData = caseData(
            NO, solicitors(sol(DEF1_SOL, ORG_1)), responses(DEF1_SOL));

        assertThat(isRespondentsResponseSatisfied(caseData, caseData)).isTrue();
    }

    @Test
    void shouldReturnTrue_for1v1_withSeveralSolicitorsInSameFirm_andOneResponse() {
        GeneralApplicationCaseData caseData = caseData(
            NO, solicitors(sol(DEF1_SOL, ORG_1), sol(DEF1_SOL_COLLEAGUE, ORG_1)), responses(DEF1_SOL));

        assertThat(isRespondentsResponseSatisfied(caseData, caseData)).isTrue();
    }

    @Test
    void shouldReturnFalse_for1v2DifferentSolicitors_withResponseFromDefendant1Only() {
        GeneralApplicationCaseData caseData = caseData(
            YES, solicitors(sol(DEF1_SOL, ORG_1), sol(DEF2_SOL, ORG_2)), responses(DEF1_SOL));

        assertThat(isRespondentsResponseSatisfied(caseData, caseData)).isFalse();
    }

    @Test
    void shouldReturnFalse_for1v2DifferentSolicitors_withResponseFromDefendant2Only() {
        GeneralApplicationCaseData caseData = caseData(
            YES, solicitors(sol(DEF1_SOL, ORG_1), sol(DEF2_SOL, ORG_2)), responses(DEF2_SOL));

        assertThat(isRespondentsResponseSatisfied(caseData, caseData)).isFalse();
    }

    @Test
    void shouldReturnFalse_for1v2DifferentSolicitors_withTwoResponsesFromSameFirm() {
        GeneralApplicationCaseData caseData = caseData(
            YES,
            solicitors(sol(DEF1_SOL, ORG_1), sol(DEF1_SOL_COLLEAGUE, ORG_1), sol(DEF2_SOL, ORG_2)),
            responses(DEF1_SOL, DEF1_SOL_COLLEAGUE));

        assertThat(isRespondentsResponseSatisfied(caseData, caseData)).isFalse();
    }

    @Test
    void shouldReturnTrue_for1v2DifferentSolicitors_withResponseFromBothFirms() {
        GeneralApplicationCaseData caseData = caseData(
            YES, solicitors(sol(DEF1_SOL, ORG_1), sol(DEF2_SOL, ORG_2)), responses(DEF2_SOL, DEF1_SOL));

        assertThat(isRespondentsResponseSatisfied(caseData, caseData)).isTrue();
    }

    @Test
    void shouldReturnTrue_for1v2SameSolicitor_withSingleResponse() {
        // Same user holds both the respondent 1 and respondent 2 solicitor roles, so they are listed twice
        GeneralApplicationCaseData caseData = caseData(
            YES, solicitors(sol(DEF1_SOL, ORG_1), sol(DEF1_SOL, ORG_1)), responses(DEF1_SOL));

        assertThat(isRespondentsResponseSatisfied(caseData, caseData)).isTrue();
    }

    @Test
    void shouldReturnTrue_for1v2SameSolicitorFirm_withResponseFromAnySolicitorInFirm() {
        GeneralApplicationCaseData caseData = caseData(
            YES,
            solicitors(sol(DEF1_SOL, ORG_1), sol(DEF1_SOL_COLLEAGUE, ORG_1)),
            responses(DEF1_SOL_COLLEAGUE));

        assertThat(isRespondentsResponseSatisfied(caseData, caseData)).isTrue();
    }

    @Test
    void shouldReturnFalse_for1v2SameSolicitor_whenResponseIsFromUnknownUser() {
        GeneralApplicationCaseData caseData = caseData(
            YES, solicitors(sol(DEF1_SOL, ORG_1), sol(DEF1_SOL_COLLEAGUE, ORG_1)), responses("someone-else"));

        assertThat(isRespondentsResponseSatisfied(caseData, caseData)).isFalse();
    }

    @Test
    void shouldTreatSolicitorWithoutOrganisationAsSeparateParty() {
        GeneralApplicationCaseData oneResponse = caseData(
            YES, solicitors(sol(DEF1_SOL, ORG_1), sol(DEF2_SOL, null)), responses(DEF1_SOL));
        GeneralApplicationCaseData bothResponses = caseData(
            YES, solicitors(sol(DEF1_SOL, ORG_1), sol(DEF2_SOL, null)), responses(DEF1_SOL, DEF2_SOL));

        assertThat(isRespondentsResponseSatisfied(oneResponse, oneResponse)).isFalse();
        assertThat(isRespondentsResponseSatisfied(bothResponses, bothResponses)).isTrue();
    }

    @Test
    void shouldIgnoreResponsesWithoutRespondentDetails() {
        GeneralApplicationCaseData caseData = caseData(
            YES,
            solicitors(sol(DEF1_SOL, ORG_1), sol(DEF2_SOL, ORG_2)),
            List.of(element(new GARespondentResponse()), element(response(DEF1_SOL))));

        assertThat(isRespondentsResponseSatisfied(caseData, caseData)).isFalse();
    }

    @Test
    void shouldReturnFalse_whenMultiPartyFlagMissing_andMoreThanOneSolicitor() {
        GeneralApplicationCaseData caseData = caseData(
            null, solicitors(sol(DEF1_SOL, ORG_1), sol(DEF2_SOL, ORG_2)), responses(DEF1_SOL, DEF2_SOL));

        assertThat(isRespondentsResponseSatisfied(caseData, caseData)).isFalse();
    }

    private static GeneralApplicationCaseData caseData(YesOrNo isMultiParty,
                                                       List<Element<GASolicitorDetailsGAspec>> solicitors,
                                                       List<Element<GARespondentResponse>> responses) {
        return new GeneralApplicationCaseData()
            .isMultiParty(isMultiParty)
            .generalAppRespondentSolicitors(solicitors)
            .respondentsResponses(responses);
    }

    private static GASolicitorDetailsGAspec sol(String id, String orgId) {
        return new GASolicitorDetailsGAspec()
            .setId(id)
            .setEmail(id + "@example.com")
            .setOrganisationIdentifier(orgId);
    }

    @SafeVarargs
    private static <T> List<Element<T>> elements(T... values) {
        return Arrays.stream(values).map(v -> element(v)).toList();
    }

    private static List<Element<GASolicitorDetailsGAspec>> solicitors(GASolicitorDetailsGAspec... solicitors) {
        return elements(solicitors);
    }

    private static List<Element<GARespondentResponse>> responses(String... respondentIds) {
        return Arrays.stream(respondentIds).map(id -> element(response(id))).toList();
    }

    private static GARespondentResponse response(String respondentId) {
        return new GARespondentResponse().setGaRespondentDetails(respondentId);
    }
}
