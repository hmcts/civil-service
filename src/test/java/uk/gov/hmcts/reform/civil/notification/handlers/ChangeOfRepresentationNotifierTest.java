package uk.gov.hmcts.reform.civil.notification.handlers;

import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.enums.dq.Language;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Fee;
import uk.gov.hmcts.reform.civil.model.common.DynamicList;
import uk.gov.hmcts.reform.civil.model.common.DynamicListElement;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.prd.model.Organisation;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.sampledata.PartyBuilder;
import uk.gov.hmcts.reform.civil.service.OrganisationService;
import uk.gov.hmcts.reform.civil.service.flowstate.SimpleStateFlowEngine;
import uk.gov.hmcts.reform.civil.stateflow.StateFlow;
import uk.gov.hmcts.reform.civil.stateflow.model.State;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CASEMAN_REF;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CASE_NAME;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CCD_REF;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CLAIMANT_NAME;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CLAIM_16_DIGIT_NUMBER;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CLAIM_NUMBER;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CLAIM_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.COURT_LOCATION;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.DEFENDANT_NAME_INTERIM;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.FORMER_SOL;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.HEARING_DATE;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.HEARING_DUE_DATE;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.HEARING_FEE;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.HEARING_TIME;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.ISSUE_DATE;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.LEGAL_ORG_NAME;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.LEGAL_REP_NAME_WITH_SPACE;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.NEW_SOL;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.OTHER_SOL_NAME;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.PARTY_REFERENCES;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.REFERENCE;
import static uk.gov.hmcts.reform.civil.helpers.DateFormatHelper.DATE;
import static uk.gov.hmcts.reform.civil.helpers.DateFormatHelper.formatLocalDate;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowFlag.DEFENDANT_NOC_ONLINE;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowFlag.TWO_RESPONDENT_REPRESENTATIVES;
import static uk.gov.hmcts.reform.civil.utils.NotificationUtils.buildPartiesReferencesEmailSubject;

@ExtendWith(MockitoExtension.class)
class ChangeOfRepresentationNotifierTest {

    @Mock
    private NotificationsProperties notificationsProperties;

    @Mock
    private OrganisationService organisationService;

    @Mock
    private SimpleStateFlowEngine stateFlowEngine;

    @Mock
    private StateFlow stateFlow;

    @InjectMocks
    private ChangeOfRepresentationNotifier changeOfRepresentationNotifier;

    public static final Long CASE_ID = 1594901956117591L;
    public static final String TEMPLATE_REFERENCE = "notice-of-change-000DC001";
    private static final String CASE_TITLE = "Mr. John Rambo v Mr. Sole Trader, Mr. John Rambo";
    private static final String PREVIOUS_SOL = "Previous solicitor";
    private static final String NEW_SOLICITOR = "New solicitor";
    private static final String OTHER_SOLICITOR = "Other solicitor";
    private static final String OTHER_SOLICITOR_2 = "Other solicitor2";
    private static final String CLAIMANT_LIP_TEMPLATE = "claimant-lip-template-id";
    private static final String NEW_SOL_TEMPLATE = "new-sol-template-id";
    private static final String CLAIMANT_SOL_HEARING_FEE_UNPAID_TEMPLATE = "claimant-sol-hearing-fee-unpaid-id";
    private static final String PREVIOUS_SOL_TEMPLATE = "former-sol-template-id";
    private static final String OTHER_SOL_TEMPLATE = "other-sol-template-id";
    private static final String CLAIMANT_LIP_WELSH_TEMPLATE = "claimant-lip-welsh-template-id";

    @Nested
    class ProcessRespondent1NoC {

        private CaseData caseData = createCaseDataWithRespondent1();

        @Test
        void shouldReturnFormerSolAndOtherSol1ToNotify_InCaseOfAPartyNoCForLRvLRClaim() {
            prepareMocksForSolicitors();
            when(organisationService.findOrganisationById("QWERTY A"))
                .thenReturn(Optional.of(Organisation.builder().name("Other solicitor").build()));

            Set<EmailDTO> emailsToNotify = changeOfRepresentationNotifier.getPartiesToNotify(caseData);

            assertNotNull(emailsToNotify);
            Set<EmailDTO> expectedEmailDTO = createFormerAndOtherSol1ExpectedEmailDTO(caseData);
            assertThat(emailsToNotify.size()).isEqualTo(2);
            assertThat(emailsToNotify).containsAll(expectedEmailDTO);
        }

        @Test
        void shouldReturnFormerSolandotherSol1AndSol2ToNotify_InCaseOfAPartyNoCForLrvLrLrClaim() {
            prepareMocksForSolicitors();
            when(stateFlow.isFlagSet(TWO_RESPONDENT_REPRESENTATIVES)).thenReturn(true);
            when(organisationService.findOrganisationById("QWERTY A"))
                .thenReturn(Optional.of(Organisation.builder().name("Other solicitor").build()));
            when(organisationService.findOrganisationById("QWERTY R2"))
                .thenReturn(Optional.of(Organisation.builder().name("Other solicitor2").build()));

            Set<EmailDTO> expectedEmailDTO = new java.util.HashSet<>(createFormerAndOtherSol1ExpectedEmailDTO(caseData));
            expectedEmailDTO.add(getOtherSol2ExpectedEmailDTO(caseData));

            Set<EmailDTO> emailsToNotify = changeOfRepresentationNotifier.getPartiesToNotify(caseData);
            assertThat(emailsToNotify.size()).isEqualTo(3);
            assertThat(emailsToNotify).containsAll(expectedEmailDTO);
        }

        @Test
        void shouldReturnApplicantSolToNotifyUnpaidHearingFee_InCaseOfAClaimInHearingReadiness() {
            prepareMocksForSolicitors();
            when(organisationService.findOrganisationById("QWERTY A"))
                .thenReturn(Optional.of(Organisation.builder().name("Other solicitor").build()));
            when(stateFlow.getState()).thenReturn(State.from("MAIN.IN_HEARING_READINESS"));
            when(notificationsProperties.getHearingFeeUnpaidNoc()).thenReturn(CLAIMANT_SOL_HEARING_FEE_UNPAID_TEMPLATE);
            caseData = updateCaseDataWithHearingFee(caseData);
            Set<EmailDTO> emailsToNotify = changeOfRepresentationNotifier.getPartiesToNotify(caseData);
            Set<EmailDTO> expectedEmailDTO = createExpectedEmailDTOWithHearingFee(caseData);
            assertThat(emailsToNotify.size()).isEqualTo(3);
            assertThat(emailsToNotify).containsAll(expectedEmailDTO);
        }
    }

    @Nested
    class ProcessRespondent1LRNoCWhenClaimantIsLip {

        private CaseData caseData = createCaseDataForClaimantLip();

        @Test
        void shouldReturnClaimantLipOldAndNewDefendantSolToNotify() {
            prepareMocksForLipvLR();
            when(notificationsProperties.getNotifyClaimantLipForDefendantRepresentedTemplate()).thenReturn(
                CLAIMANT_LIP_TEMPLATE);

            Set<EmailDTO> emailsToNotify = changeOfRepresentationNotifier.getPartiesToNotify(caseData);
            assertNotNull(emailsToNotify);

            Set<EmailDTO> expectedEmailDTO = createExpectedEmailDTOForLip(caseData, false);
            assertThat(emailsToNotify).containsAll(expectedEmailDTO);
        }

        @Test
        void shouldReturnClaimantLipWelshOldAndNewDefendantSolToNotify() {
            prepareMocksForLipvLR();
            when(notificationsProperties.getNotifyClaimantLipBilingualAfterDefendantNOC()).thenReturn(
                CLAIMANT_LIP_WELSH_TEMPLATE);
            // Add bilingual flag for claimant
            caseData = caseData.toBuilder().claimantBilingualLanguagePreference(Language.BOTH.toString()).build();
            Set<EmailDTO> welshEmailsToNotify = changeOfRepresentationNotifier.getPartiesToNotify(caseData);
            assertNotNull(welshEmailsToNotify);

            Set<EmailDTO> welshExpectedEmailDTO = createExpectedEmailDTOForLip(caseData, true);
            assertThat(welshEmailsToNotify).containsAll(welshExpectedEmailDTO);
        }
    }

    private void prepareMocksForSolicitors() {
        when(stateFlowEngine.evaluate(any(CaseData.class))).thenReturn(stateFlow);
        when(stateFlow.getState()).thenReturn(State.from("state1"));
        when(stateFlow.isFlagSet(TWO_RESPONDENT_REPRESENTATIVES)).thenReturn(false);
        when(notificationsProperties.getNoticeOfChangeFormerSolicitor()).thenReturn(PREVIOUS_SOL_TEMPLATE);
        when(notificationsProperties.getNoticeOfChangeOtherParties()).thenReturn(OTHER_SOL_TEMPLATE);
        when(organisationService.findOrganisationById("Previous-sol-id"))
            .thenReturn(Optional.of(Organisation.builder().name("Previous solicitor").build()));
        when(organisationService.findOrganisationById("New-sol-id"))
            .thenReturn(Optional.of(Organisation.builder().name("New solicitor").build()));
    }

    private void prepareMocksForLipvLR() {
        when(stateFlowEngine.evaluate(any(CaseData.class))).thenReturn(stateFlow);
        when(stateFlow.getState()).thenReturn(State.from("state1"));
        when(stateFlow.isFlagSet(TWO_RESPONDENT_REPRESENTATIVES)).thenReturn(false);
        when(stateFlow.isFlagSet(DEFENDANT_NOC_ONLINE)).thenReturn(true);
        when(notificationsProperties.getNoticeOfChangeFormerSolicitor()).thenReturn(
            PREVIOUS_SOL_TEMPLATE);
        when(notificationsProperties.getNotifyNewDefendantSolicitorNOC()).thenReturn(
            NEW_SOL_TEMPLATE);
        when(organisationService.findOrganisationById("Previous-sol-id"))
            .thenReturn(Optional.of(Organisation.builder().name(PREVIOUS_SOL).build()));
        when(organisationService.findOrganisationById("New-sol-id"))
            .thenReturn(Optional.of(Organisation.builder().name(NEW_SOLICITOR).build()));
    }

    private Set<EmailDTO> createFormerAndOtherSol1ExpectedEmailDTO(CaseData caseData) {
        Map<String, String> notifySolProperties = new HashMap<>(getNoCPropertiesForLR(caseData));
        notifySolProperties.put(OTHER_SOL_NAME, OTHER_SOLICITOR);

        EmailDTO formerRespondent1Solicitor = createEmailDTO("previous-solicitor@example.com", PREVIOUS_SOL_TEMPLATE, notifySolProperties);
        EmailDTO otherSolicitor = createEmailDTO("applicantsolicitor@example.com", OTHER_SOL_TEMPLATE, notifySolProperties);

        return Set.of(formerRespondent1Solicitor, otherSolicitor);
    }

    private EmailDTO getOtherSol2ExpectedEmailDTO(CaseData caseData) {
        Map<String, String> notifySolProperties = new HashMap<>(getNoCPropertiesForLR(caseData));
        notifySolProperties.put(OTHER_SOL_NAME, OTHER_SOLICITOR_2);
        return EmailDTO.builder()
            .targetEmail("respondentsolicitor2@example.com")
            .emailTemplate(OTHER_SOL_TEMPLATE)
            .parameters(notifySolProperties)
            .reference(TEMPLATE_REFERENCE)
            .build();
    }

    private Set<EmailDTO> createExpectedEmailDTOWithHearingFee(CaseData caseData) {
        Map<String, String> notifySolProperties = new HashMap<>(getNoCPropertiesForLR(caseData));
        notifySolProperties.put(OTHER_SOL_NAME, OTHER_SOLICITOR);

        EmailDTO claimantSolHearingFeeUnpaid = createEmailDTO("applicantsolicitor@example.com",
                                                              CLAIMANT_SOL_HEARING_FEE_UNPAID_TEMPLATE,
                                                              getPropsForClaimantSolicitorIfClaimIsInHR(caseData));

        return Set.of(createEmailDTO("previous-solicitor@example.com", PREVIOUS_SOL_TEMPLATE, notifySolProperties),
                      createEmailDTO("applicantsolicitor@example.com", OTHER_SOL_TEMPLATE, notifySolProperties),
                      claimantSolHearingFeeUnpaid);
    }

    private Set<EmailDTO> createExpectedEmailDTOForLip(CaseData caseData, boolean isWelsh) {
        Map<String, String> notifyClaimLipProps = getPropsForClaimantLip();

        Map<String, String> notifySolProperties = new HashMap<>(getNoCPropertiesForLR(caseData));
        notifySolProperties.put(OTHER_SOL_NAME, "LiP");
        notifySolProperties.put(PARTY_REFERENCES, "Claimant reference: 12345 - Defendant reference: 6789");
        notifySolProperties.put(CASE_NAME, "Mr. John Rambo v Mr. Sole Trader");

        EmailDTO claimantLipWelsh = createEmailDTO("rambo@email.com",
                                                   isWelsh ? CLAIMANT_LIP_WELSH_TEMPLATE : CLAIMANT_LIP_TEMPLATE,
                                                   notifyClaimLipProps);
        EmailDTO otherSolicitorLR = createEmailDTO("previous-solicitor@example.com", PREVIOUS_SOL_TEMPLATE, notifySolProperties);
        EmailDTO newSolicitorLR = createEmailDTO("respondentsolicitor@example.com", NEW_SOL_TEMPLATE, notifySolProperties);

        return Set.of(claimantLipWelsh, otherSolicitorLR, newSolicitorLR);
    }

    private EmailDTO createEmailDTO(String targetEmail, String template, Map<String, String> parameters) {
        return EmailDTO.builder()
            .targetEmail(targetEmail)
            .emailTemplate(template)
            .parameters(parameters)
            .reference(TEMPLATE_REFERENCE)
            .build();
    }

    // Test data builders
    private CaseData createCaseDataWithRespondent1() {
        return CaseDataBuilder.builder()
            .atStateClaimDraft()
            .atStateClaimDetailsNotifiedWithNoticeOfChangeRespondent1()
            .respondent2(PartyBuilder.builder().individual().build())
            .applicant1Represented(YesOrNo.NO)
            .build();
    }

    private CaseData createCaseDataForClaimantLip() {
        return CaseDataBuilder.builder().atStateClaimDetailsNotifiedWithNoticeOfChangeRespondent1()
            .applicant1Represented(YesOrNo.NO)
            .build();
    }

    private CaseData updateCaseDataWithHearingFee(CaseData caseData) {
        return caseData.toBuilder()
            .hearingLocation(DynamicList.builder().value(DynamicListElement.builder().label("County Court").build()).build())
            .hearingDate(LocalDate.of(1990, 2, 20))
            .hearingTimeHourMinute("1215")
            .hearingFee(Fee.builder().calculatedAmountInPence(BigDecimal.valueOf(30000)).build())
            .hearingDueDate(LocalDate.of(1990, 2, 20))
            .build();
    }

    @NotNull
    private Map<String, String> getNoCPropertiesForLR(CaseData caseData) {
        return Map.of(
            CASE_NAME, CASE_TITLE,
            ISSUE_DATE, formatLocalDate(caseData.getIssueDate(), DATE),
            CCD_REF, CASE_ID.toString(),
            FORMER_SOL, "Previous solicitor",
            NEW_SOL, NEW_SOLICITOR,
            PARTY_REFERENCES, "Claimant reference: 12345 - Defendant 1 reference: 6789 - Defendant 2 reference: Not provided",
            CASEMAN_REF, "000DC001",
            LEGAL_REP_NAME_WITH_SPACE, "New solicitor",
            REFERENCE, CASE_ID.toString()
        );
    }

    @NotNull
    private Map<String, String> getPropsForClaimantLip() {
        return Map.of(
            CLAIMANT_NAME, "Mr. John Rambo",
            CLAIM_16_DIGIT_NUMBER, CASE_ID.toString(),
            DEFENDANT_NAME_INTERIM, "Mr. Sole Trader",
            CLAIM_NUMBER, "000DC001"
        );
    }

    @NotNull
    private Map<String, String> getPropsForClaimantSolicitorIfClaimIsInHR(CaseData caseData) {
        return Map.of(
            CLAIM_REFERENCE_NUMBER, CASE_ID.toString(),
            LEGAL_ORG_NAME, "Other solicitor",
            HEARING_DATE, formatLocalDate(caseData.getHearingDate(), DATE),
            COURT_LOCATION, "County Court",
            HEARING_TIME, "1215",
            HEARING_FEE, "£300.00",
            HEARING_DUE_DATE, formatLocalDate(caseData.getHearingDueDate(), DATE),
            PARTY_REFERENCES, buildPartiesReferencesEmailSubject(caseData),
            CASEMAN_REF, "000DC001"
        );
    }
}
