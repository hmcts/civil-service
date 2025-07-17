package uk.gov.hmcts.reform.civil.notification.handlers.claimantresponseconfirmsnottoproceedlip;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.model.citizenui.CaseDataLiP;
import uk.gov.hmcts.reform.civil.model.citizenui.RespondentLiPResponse;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.utils.PartyUtils;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.constants.SpecJourneyConstantLRSpec.DISPUTES_THE_CLAIM;
import static uk.gov.hmcts.reform.civil.enums.CaseCategory.SPEC_CLAIM;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;
import static uk.gov.hmcts.reform.civil.enums.dq.Language.BOTH;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CLAIM_LEGAL_ORG_NAME_SPEC;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CLAIM_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.RESPONDENT_NAME;
import static uk.gov.hmcts.reform.civil.handler.callback.user.spec.show.ResponseOneVOneShowTag.ONE_V_ONE_PART_ADMIT_PAY_IMMEDIATELY;

public class ClaimantConfirmsNotToProceedLipDefendantEmailDTOGeneratorTest {

    @InjectMocks
    private ClaimantConfirmsNotToProceedLipDefendantEmailDTOGenerator emailDTOGenerator;

    @Mock
    private NotificationsProperties notificationsProperties;

    @Mock
    private FeatureToggleService featureToggleService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void shouldReturnCorrectEmailTemplateWhenCaseIsPartAdmitPayImmediatelyAccepted() {
        CaseData caseData = CaseData.builder()
            .caseAccessCategory(SPEC_CLAIM)
            .applicant1AcceptAdmitAmountPaidSpec(YES)
            .showResponseOneVOneFlag(ONE_V_ONE_PART_ADMIT_PAY_IMMEDIATELY)
            .build();
        String expectedTemplateId = "template-id";

        when(notificationsProperties.getNotifyRespondentLipPartAdmitPayImmediatelyAcceptedSpec()).thenReturn(expectedTemplateId);

        String actualTemplateId = emailDTOGenerator.getEmailTemplateId(caseData);

        assertThat(actualTemplateId).isEqualTo(expectedTemplateId);
    }

    @Test
    void shouldReturnCorrectEmailTemplateWhenLipVLipEnabledAndClaimantDontWantToProceedWithFulLDefenceFD() {
        CaseData caseData = CaseData.builder()
            .defenceRouteRequired(DISPUTES_THE_CLAIM)
            .applicant1ProceedWithClaim(NO)
            .build();
        String expectedTemplateId = "template-id";

        when(notificationsProperties.getRespondent1LipClaimUpdatedTemplate()).thenReturn(expectedTemplateId);
        when(featureToggleService.isLipVLipEnabled()).thenReturn(true);

        String actualTemplateId = emailDTOGenerator.getEmailTemplateId(caseData);

        assertThat(actualTemplateId).isEqualTo(expectedTemplateId);
    }

    @Test
    void shouldReturnCorrectEmailTemplateWhenLipVLipEnabledAndClaimantDontWantToProceedWithFulLDefenceFDBilingual() {
        CaseData caseData = CaseData.builder()
            .defenceRouteRequired(DISPUTES_THE_CLAIM)
            .applicant1ProceedWithClaim(NO)
            .caseDataLiP(
                CaseDataLiP.builder()
                    .respondent1LiPResponse(
                        RespondentLiPResponse.builder()
                            .respondent1ResponseLanguage(BOTH.toString())
                            .build()
                    ).build())
            .build();
        String expectedTemplateId = "template-id";

        when(notificationsProperties.getNotifyDefendantTranslatedDocumentUploaded()).thenReturn(expectedTemplateId);
        when(featureToggleService.isLipVLipEnabled()).thenReturn(true);

        String actualTemplateId = emailDTOGenerator.getEmailTemplateId(caseData);

        assertThat(actualTemplateId).isEqualTo(expectedTemplateId);
    }

    @Test
    void shouldReturnCorrectEmailTemplateWhenLipVLipDisabledAndNotClaimantDontWantToProceedWithFulLDefence() {
        CaseData caseData = CaseData.builder().build();
        String expectedTemplateId = "template-id";

        when(notificationsProperties.getClaimantSolicitorConfirmsNotToProceed()).thenReturn(expectedTemplateId);
        when(featureToggleService.isLipVLipEnabled()).thenReturn(false);

        String actualTemplateId = emailDTOGenerator.getEmailTemplateId(caseData);

        assertThat(actualTemplateId).isEqualTo(expectedTemplateId);
    }

    @Test
    void shouldReturnCorrectEmailTemplateWhenLipVLipEnabledAndNotClaimantDontWantToProceedWithFulLDefence() {
        CaseData caseData = CaseData.builder().build();
        String expectedTemplateId = "template-id";

        when(notificationsProperties.getClaimantSolicitorConfirmsNotToProceed()).thenReturn(expectedTemplateId);
        when(featureToggleService.isLipVLipEnabled()).thenReturn(true);

        String actualTemplateId = emailDTOGenerator.getEmailTemplateId(caseData);

        assertThat(actualTemplateId).isEqualTo(expectedTemplateId);
    }

    @Test
    void shouldReturnCorrectEmailTemplateWhenLipVLipDisabledAndClaimantDontWantToProceedWithFulLDefence() {
        CaseData caseData = CaseData.builder()
            .defenceRouteRequired(DISPUTES_THE_CLAIM)
            .applicant1ProceedWithClaim(NO)
            .build();
        String expectedTemplateId = "template-id";

        when(notificationsProperties.getClaimantSolicitorConfirmsNotToProceed()).thenReturn(expectedTemplateId);
        when(featureToggleService.isLipVLipEnabled()).thenReturn(false);

        String actualTemplateId = emailDTOGenerator.getEmailTemplateId(caseData);

        assertThat(actualTemplateId).isEqualTo(expectedTemplateId);
    }

    @Test
    void shouldReturnCorrectReferenceTemplate() {
        String referenceTemplate = emailDTOGenerator.getReferenceTemplate();

        assertThat(referenceTemplate).isEqualTo("claimant-confirms-not-to-proceed-respondent-notification-%s");
    }

    @Test
    void shouldReturnCorrectCustomPropertiesWhenLipVLipEnabledAndClaimantDontWantToProceedWithFulLDefenceFD() {
        Party party = Party.builder().build();
        String legacyCaseNumber = "legacyCaseNumber";
        CaseData caseData = CaseData.builder()
            .defenceRouteRequired(DISPUTES_THE_CLAIM)
            .applicant1ProceedWithClaim(NO)
            .legacyCaseReference(legacyCaseNumber)
            .respondent1(party)
            .build();

        when(featureToggleService.isLipVLipEnabled()).thenReturn(true);

        String partyName = "partyName";
        MockedStatic<PartyUtils> partyUtilsMockedStatic = Mockito.mockStatic(PartyUtils.class);
        partyUtilsMockedStatic.when(() -> PartyUtils.getPartyNameBasedOnType(party)).thenReturn(partyName);

        Map<String, String> properties = new HashMap<>();
        Map<String, String> updatedProperties = emailDTOGenerator.addCustomProperties(properties, caseData);

        partyUtilsMockedStatic.close();

        assertThat(updatedProperties.size()).isEqualTo(2);
        assertThat(updatedProperties).containsEntry(CLAIM_REFERENCE_NUMBER, legacyCaseNumber);
        assertThat(updatedProperties).containsEntry(RESPONDENT_NAME, partyName);
    }

    @Test
    void shouldReturnCorrectCustomPropertiesWhenCaseIsPartAdmitPayImmediatelyAccepted() {
        Party party = Party.builder().build();
        String legacyCaseNumber = "legacyCaseNumber";
        CaseData caseData = CaseData.builder()
            .caseAccessCategory(SPEC_CLAIM)
            .applicant1AcceptAdmitAmountPaidSpec(YES)
            .showResponseOneVOneFlag(ONE_V_ONE_PART_ADMIT_PAY_IMMEDIATELY)
            .legacyCaseReference(legacyCaseNumber)
            .respondent1(party)
            .build();

        String partyName = "partyName";
        MockedStatic<PartyUtils> partyUtilsMockedStatic = Mockito.mockStatic(PartyUtils.class);
        partyUtilsMockedStatic.when(() -> PartyUtils.getPartyNameBasedOnType(party)).thenReturn(partyName);

        Map<String, String> properties = new HashMap<>();
        Map<String, String> updatedProperties = emailDTOGenerator.addCustomProperties(properties, caseData);

        partyUtilsMockedStatic.close();

        assertThat(updatedProperties.size()).isEqualTo(2);
        assertThat(updatedProperties).containsEntry(CLAIM_REFERENCE_NUMBER, legacyCaseNumber);
        assertThat(updatedProperties).containsEntry(RESPONDENT_NAME, partyName);
    }

    @Test
    void shouldReturnCorrectCustomPropertiesWhenIsNotPartAdmitPayImmediatelyAcceptedAndNotClaimantDontWantToProceedWithFulLDefenceFD() {
        Party party = Party.builder().build();
        CaseData caseData = CaseData.builder().respondent1(party).build();

        String partyName = "partyName";
        MockedStatic<PartyUtils> partyUtilsMockedStatic = Mockito.mockStatic(PartyUtils.class);
        partyUtilsMockedStatic.when(() -> PartyUtils.getPartyNameBasedOnType(party, false)).thenReturn(partyName);

        Map<String, String> properties = new HashMap<>();
        Map<String, String> updatedProperties = emailDTOGenerator.addCustomProperties(properties, caseData);

        partyUtilsMockedStatic.close();

        assertThat(updatedProperties.size()).isEqualTo(1);
        assertThat(updatedProperties).containsEntry(CLAIM_LEGAL_ORG_NAME_SPEC, partyName);
    }

    @Test
    void shouldReturnCorrectCustomPropertiesWhenIsNotPartAdmitPayImmediatelyAcceptedAndLipVLipDisabled() {
        Party party = Party.builder().build();
        CaseData caseData = CaseData.builder()
            .defenceRouteRequired(DISPUTES_THE_CLAIM)
            .applicant1ProceedWithClaim(NO)
            .respondent1(party)
            .build();

        String partyName = "partyName";
        MockedStatic<PartyUtils> partyUtilsMockedStatic = Mockito.mockStatic(PartyUtils.class);
        partyUtilsMockedStatic.when(() -> PartyUtils.getPartyNameBasedOnType(party, false)).thenReturn(partyName);

        when(featureToggleService.isLipVLipEnabled()).thenReturn(false);

        Map<String, String> properties = new HashMap<>();
        Map<String, String> updatedProperties = emailDTOGenerator.addCustomProperties(properties, caseData);

        partyUtilsMockedStatic.close();

        assertThat(updatedProperties.size()).isEqualTo(1);
        assertThat(updatedProperties).containsEntry(CLAIM_LEGAL_ORG_NAME_SPEC, partyName);
    }
}
