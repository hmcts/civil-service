package uk.gov.hmcts.reform.civil.notification.handlers.defendantresponse.spec.common;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import uk.gov.hmcts.reform.civil.enums.MultiPartyScenario;
import uk.gov.hmcts.reform.civil.enums.RespondentResponsePartAdmissionPaymentTimeLRspec;
import uk.gov.hmcts.reform.civil.enums.RespondentResponseTypeSpec;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

class SpecDefRespEmailHelperTest {

    private NotificationsProperties notificationsProperties;
    private SpecDefRespEmailHelper helper;

    @BeforeEach
    void setUp() {
        notificationsProperties = mock(NotificationsProperties.class);
        helper = new SpecDefRespEmailHelper(notificationsProperties);
    }

    @Test
    void shouldReturnImmediatelyTemplate_whenFullAdmissionAndImmediatePayment() {
        CaseData caseData = CaseData.builder()
            .defenceAdmitPartPaymentTimeRouteRequired(RespondentResponsePartAdmissionPaymentTimeLRspec.IMMEDIATELY)
            .respondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.FULL_ADMISSION)
            .build();

        String expectedTemplate = "immediately-template";
        when(notificationsProperties.getClaimantSolicitorImmediatelyDefendantResponseForSpec()).thenReturn(expectedTemplate);

        String actual = helper.getAppSolTemplate(caseData);
        assertEquals(expectedTemplate, actual);
    }

    @Test
    void shouldReturn1v2DS_whenMultiPartyScenarioIs1v2TwoLegalRep() {
        CaseData caseData = mock(CaseData.class);
        when(caseData.getDefenceAdmitPartPaymentTimeRouteRequired()).thenReturn(null);
        when(caseData.getRespondent1ClaimResponseTypeForSpec()).thenReturn(null);
        when(caseData.getRespondent2ClaimResponseTypeForSpec()).thenReturn(null);
        try (MockedStatic<MultiPartyScenario> mockedStatic = mockStatic(MultiPartyScenario.class)) {
            mockedStatic
                .when(() -> MultiPartyScenario.getMultiPartyScenario(caseData))
                .thenReturn(MultiPartyScenario.ONE_V_TWO_TWO_LEGAL_REP);

            String expectedTemplate = "1v2-ds-template";
            when(notificationsProperties.getClaimantSolicitorDefendantResponse1v2DSForSpec()).thenReturn(expectedTemplate);

            String actual = helper.getAppSolTemplate(caseData);
            assertEquals(expectedTemplate, actual);
        }
    }

    @Test
    void shouldReturnDefaultAppSolTemplate_whenSingleDefendantScenario() {
        CaseData caseData = mock(CaseData.class);
        when(caseData.getDefenceAdmitPartPaymentTimeRouteRequired()).thenReturn(null);
        try (MockedStatic<MultiPartyScenario> mockedStatic = mockStatic(MultiPartyScenario.class)) {
            mockedStatic
                .when(() -> MultiPartyScenario.getMultiPartyScenario(caseData))
                .thenReturn(MultiPartyScenario.ONE_V_ONE);
            String expectedTemplate = "default-appsol-template";
            when(notificationsProperties.getClaimantSolicitorDefendantResponseForSpec()).thenReturn(expectedTemplate);

            String actual = helper.getAppSolTemplate(caseData);
            assertEquals(expectedTemplate, actual);
        }
    }

    @Test
    void shouldReturnBilingualLipTemplate_whenClaimantIsBilingual() {
        CaseData caseData = mock(CaseData.class);
        when(caseData.isClaimantBilingual()).thenReturn(true);

        String expectedTemplate = "bilingual-template";
        when(notificationsProperties.getClaimantLipClaimUpdatedBilingualTemplate()).thenReturn(expectedTemplate);

        String actual = helper.getLipTemplate(caseData);
        assertEquals(expectedTemplate, actual);
    }

    @Test
    void shouldReturnDefaultLipTemplate_whenClaimantIsNotBilingual() {
        CaseData caseData = mock(CaseData.class);
        when(caseData.isClaimantBilingual()).thenReturn(false);

        String expectedTemplate = "default-lip-template";
        when(notificationsProperties.getClaimantLipClaimUpdatedTemplate()).thenReturn(expectedTemplate);

        String actual = helper.getLipTemplate(caseData);
        assertEquals(expectedTemplate, actual);
    }

    @Test
    void shouldReturnRespondentTemplate1v2DS_whenMultiPartyScenarioIs1v2TwoLegalRep() {
        CaseData caseData = mock(CaseData.class);
        try (MockedStatic<MultiPartyScenario> mockedStatic = mockStatic(MultiPartyScenario.class)) {
            mockedStatic
                .when(() -> MultiPartyScenario.getMultiPartyScenario(caseData))
                .thenReturn(MultiPartyScenario.ONE_V_TWO_TWO_LEGAL_REP);
            String expectedTemplate = "resp-template-1v2ds";
            when(notificationsProperties.getRespondentSolicitorDefendantResponseForSpec()).thenReturn(expectedTemplate);

            String actual = helper.getRespondentTemplate(caseData);
            assertEquals(expectedTemplate, actual);
        }
    }

    @Test
    void shouldReturnRespondentTemplateWithClaimantAction_whenPartAdmissionAndApplicantRepresented() {
        CaseData caseData = mock(CaseData.class);
        when(caseData.getDefenceAdmitPartPaymentTimeRouteRequired()).thenReturn(RespondentResponsePartAdmissionPaymentTimeLRspec.BY_SET_DATE);
        when(caseData.getRespondent1ClaimResponseTypeForSpec()).thenReturn(RespondentResponseTypeSpec.PART_ADMISSION);
        when(caseData.isApplicantRepresented()).thenReturn(true);
        try (MockedStatic<MultiPartyScenario> mockedStatic = mockStatic(MultiPartyScenario.class)) {
            mockedStatic
                .when(() -> MultiPartyScenario.getMultiPartyScenario(caseData))
                .thenReturn(MultiPartyScenario.ONE_V_ONE);
            String expectedTemplate = "resp-template-claimant-action";
            when(notificationsProperties.getRespondentSolicitorDefResponseSpecWithClaimantAction()).thenReturn(expectedTemplate);

            String actual = helper.getRespondentTemplate(caseData);
            assertEquals(expectedTemplate, actual);

        }
    }

    @Test
    void shouldReturnDefaultRespondentTemplate_whenOtherConditions() {
        CaseData caseData = mock(CaseData.class);
        when(caseData.getDefenceAdmitPartPaymentTimeRouteRequired()).thenReturn(null);
        when(caseData.getRespondent1ClaimResponseTypeForSpec()).thenReturn(null);
        try (MockedStatic<MultiPartyScenario> mockedStatic = mockStatic(MultiPartyScenario.class)) {
            mockedStatic
                .when(() -> MultiPartyScenario.getMultiPartyScenario(caseData))
                .thenReturn(MultiPartyScenario.ONE_V_ONE);
            String expectedTemplate = "default-respondent-template";
            when(notificationsProperties.getRespondentSolicitorDefendantResponseForSpec()).thenReturn(expectedTemplate);

            String actual = helper.getRespondentTemplate(caseData);
            assertEquals(expectedTemplate, actual);
        }
    }
}
