package uk.gov.hmcts.reform.civil.notification.handlers;

import org.apache.ibatis.annotations.Case;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.enums.dq.Language;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.notify.NotificationService;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.prd.model.Organisation;
import uk.gov.hmcts.reform.civil.sampledata.PartyBuilder;
import uk.gov.hmcts.reform.civil.service.OrganisationService;
import uk.gov.hmcts.reform.civil.service.flowstate.SimpleStateFlowEngine;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.stateflow.StateFlow;
import uk.gov.hmcts.reform.civil.stateflow.model.State;
import uk.gov.hmcts.reform.civil.utils.NocNotificationUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CASEMAN_REF;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CASE_NAME;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CCD_REF;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CLAIMANT_NAME;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CLAIM_16_DIGIT_NUMBER;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.CLAIM_NUMBER;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.DEFENDANT_NAME_INTERIM;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.FORMER_SOL;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.ISSUE_DATE;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.LEGAL_REP_NAME_WITH_SPACE;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.NEW_SOL;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.OTHER_SOL_NAME;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.PARTY_REFERENCES;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.REFERENCE;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData.RESPONDENT_NAME;
import static uk.gov.hmcts.reform.civil.helpers.DateFormatHelper.DATE;
import static uk.gov.hmcts.reform.civil.helpers.DateFormatHelper.formatLocalDate;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowFlag.DEFENDANT_NOC_ONLINE;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowFlag.TWO_RESPONDENT_REPRESENTATIVES;

@ExtendWith(MockitoExtension.class)
class ChangeOfRepresentationNotifierTest {

    @Mock
    private NotificationService notificationService;

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
    private static final String PREVIOUS_SOL = "Previous solicitor";
    private static final String PREVIOUS_SOL_TEMPLATE = "former-sol-template-id";
    private static final String CASE_TITLE = "Mr. John Rambo v Mr. Sole Trader, Mr. John Rambo";
    private static final String NEW_SOLICITOR = "New solicitor";
    private static final String OTHER_SOLICITOR = "Other solicitor";
    private static final String OTHER_SOLICITOR_2 = "Other solicitor2";
    private static final String OTHER_SOL_TEMPLATE = "other-sol-template-id";
    private static final String CLAIMANT_LIP_TEMPLATE = "claimant-lip-template-id";
    private static final String NEW_SOL_TEMPLATE = "new-sol-template-id";
    private static final String CLAIMANT_LIP_WELSH_TEMPLATE = "claimant-lip-welsh-template-id";

    private static final String NOTIFY_FORMER_SOLICITOR = "NOTIFY_FORMER_SOLICITOR";
    private static final String NOTIFY_OTHER_SOLICITOR_1 = "NOTIFY_OTHER_SOLICITOR_1";
    private static final String NOTIFY_OTHER_SOLICITOR_2 = "NOTIFY_OTHER_SOLICITOR_2";
    private static final String NOTIFY_NEW_DEFENDANT_SOLICITOR = "NOTIFY_NEW_DEFENDANT_SOLICITOR";
    private static final String NOTIFY_APPLICANT_SOLICITOR_FOR_HEARING_FEE_AFTER_NOC = "NOTIFY_APPLICANT_SOLICITOR_FOR_HEARING_FEE_AFTER_NOC";


    @Nested
    class ProcessRespondent1NoC {

        CaseData caseData = CaseDataBuilder.builder()
            .atStateClaimDraft()
            .atStateClaimDetailsNotifiedWithNoticeOfChangeRespondent1()
            .respondent2(PartyBuilder.builder().individual().build())
            .applicant1Represented(YesOrNo.NO)
            .build();

        @Test
        void shouldReturnTrueWhenOrganisationToRemoveIsNull() {
            caseData.getChangeOfRepresentation().setOrganisationToRemoveID(null);
            assertTrue(changeOfRepresentationNotifier.shouldSkipThisNotification(caseData, NOTIFY_FORMER_SOLICITOR));
        }

        @Test
        void shouldReturnFalseWhenOrganisationToRemoveIsPresent() {
            caseData.getChangeOfRepresentation().setOrganisationToRemoveID("org123");
            assertFalse(changeOfRepresentationNotifier.shouldSkipThisNotification(caseData, NOTIFY_FORMER_SOLICITOR));
        }

        @Test
        void shouldReturnAllRelevantSolicitorsToNotify() {
            when(stateFlowEngine.evaluate(any(CaseData.class))).thenReturn(stateFlow);
            when(stateFlow.getState()).thenReturn(State.from("state1"));
            when(stateFlow.isFlagSet(TWO_RESPONDENT_REPRESENTATIVES)).thenReturn(true);
            when(notificationsProperties.getNoticeOfChangeFormerSolicitor()).thenReturn(PREVIOUS_SOL_TEMPLATE);
            when(notificationsProperties.getNoticeOfChangeOtherParties()).thenReturn(OTHER_SOL_TEMPLATE);
            when(organisationService.findOrganisationById("Previous-sol-id"))
                .thenReturn(Optional.of(Organisation.builder().name(PREVIOUS_SOL).build()));
            when(organisationService.findOrganisationById("New-sol-id"))
                .thenReturn(Optional.of(Organisation.builder().name(NEW_SOLICITOR).build()));
            when(organisationService.findOrganisationById("QWERTY A"))
                .thenReturn(Optional.of(Organisation.builder().name(OTHER_SOLICITOR).build()));
            when(organisationService.findOrganisationById("QWERTY R2"))
                .thenReturn(Optional.of(Organisation.builder().name(OTHER_SOLICITOR_2).build()));

            Map<String, String> notifySolProperties = new HashMap<>(getProperties(caseData));
            notifySolProperties.put(OTHER_SOL_NAME, OTHER_SOLICITOR);

            final EmailDTO formerRespondent1Solicitor = EmailDTO.builder()
                .targetEmail("previous-solicitor@example.com")
                .emailTemplate(PREVIOUS_SOL_TEMPLATE)
                .parameters(notifySolProperties)
                .reference(TEMPLATE_REFERENCE)
                .build();

            //Applicant solicitor in this case
            final EmailDTO otherSolicitor = EmailDTO.builder()
                .targetEmail("applicantsolicitor@example.com")
                .emailTemplate(OTHER_SOL_TEMPLATE)
                .parameters(notifySolProperties)
                .reference(TEMPLATE_REFERENCE)
                .build();


            Map<String, String> notifyOtherSol2Properties = new HashMap<>(getProperties(caseData));
            notifyOtherSol2Properties.put(OTHER_SOL_NAME, OTHER_SOLICITOR_2);

            //Respondent2 solicitor in this case
            final EmailDTO otherSolicitor2 = EmailDTO.builder()
                .targetEmail("respondentsolicitor2@example.com")
                .emailTemplate(OTHER_SOL_TEMPLATE)
                .parameters(notifyOtherSol2Properties)
                .reference(TEMPLATE_REFERENCE)
                .build();

            final Set<EmailDTO> emailsToNotify = changeOfRepresentationNotifier.getPartiesToNotify(caseData);
            assertNotNull(emailsToNotify);

            final Set<EmailDTO> expectedEmailDTO = Set.of(formerRespondent1Solicitor, otherSolicitor, otherSolicitor2);
            assertThat(emailsToNotify).containsAll(expectedEmailDTO);
        }
    }

    @NotNull
    private Map<String, String> getProperties(CaseData caseData) {
        return Map.of(
            CASE_NAME, CASE_TITLE,
            ISSUE_DATE, formatLocalDate(caseData.getIssueDate(), DATE),
            CCD_REF, CASE_ID.toString(),
            FORMER_SOL, PREVIOUS_SOL,
            NEW_SOL, NEW_SOLICITOR,
            PARTY_REFERENCES, "Claimant reference: 12345 - Defendant 1 reference: 6789 - Defendant 2 reference: Not provided",
            CASEMAN_REF, "000DC001",
            LEGAL_REP_NAME_WITH_SPACE, "New solicitor",
            REFERENCE, CASE_ID.toString()
        );
    }

    @Nested
    class ProcessRespondent1LRNoCWhenClaimantIsLip {
        CaseData caseData = CaseDataBuilder.builder().atStateClaimDetailsNotifiedWithNoticeOfChangeRespondent1()
            .applicant1Represented(YesOrNo.NO)
            .build();


        @Test
        void shouldReturnAllLipVLRRelevantSolicitorsToNotify() {
            when(stateFlowEngine.evaluate(any(CaseData.class))).thenReturn(stateFlow);
            when(stateFlow.getState()).thenReturn(State.from("state1"));
            when(stateFlow.isFlagSet(TWO_RESPONDENT_REPRESENTATIVES)).thenReturn(false);
            when(stateFlow.isFlagSet(DEFENDANT_NOC_ONLINE)).thenReturn(true);

            when(notificationsProperties.getNoticeOfChangeFormerSolicitor()).thenReturn(
                PREVIOUS_SOL_TEMPLATE);

            when(notificationsProperties.getNotifyNewDefendantSolicitorNOC()).thenReturn(
                NEW_SOL_TEMPLATE);

            when(notificationsProperties.getNotifyClaimantLipForDefendantRepresentedTemplate()).thenReturn(
                CLAIMANT_LIP_TEMPLATE);

            when(notificationsProperties.getNotifyClaimantLipBilingualAfterDefendantNOC()).thenReturn(
                CLAIMANT_LIP_WELSH_TEMPLATE);

            when(organisationService.findOrganisationById("Previous-sol-id"))
                .thenReturn(Optional.of(Organisation.builder().name(PREVIOUS_SOL).build()));

            when(organisationService.findOrganisationById("New-sol-id"))
                .thenReturn(Optional.of(Organisation.builder().name(NEW_SOLICITOR).build()));

            Map<String, String> notifyClaimLipProps = new HashMap<>(getPropsForClaimantLip());

            //Applicant Lip in this case
            final EmailDTO claimantLip = EmailDTO.builder()
                .targetEmail("rambo@email.com")
                .emailTemplate(CLAIMANT_LIP_TEMPLATE)
                .parameters(notifyClaimLipProps)
                .reference(TEMPLATE_REFERENCE)
                .build();

            Map<String, String> notifySolProperties = new HashMap<>(getProperties(caseData));
            notifySolProperties.put(OTHER_SOL_NAME, "LiP");
            notifySolProperties.put(PARTY_REFERENCES, "Claimant reference: 12345 - Defendant reference: 6789");
            notifySolProperties.put(CASE_NAME, "Mr. John Rambo v Mr. Sole Trader");

            //Old respondent1(Lip v LR) in this case
            final EmailDTO otherSolicitorLR = EmailDTO.builder()
                .targetEmail("previous-solicitor@example.com")
                .emailTemplate(PREVIOUS_SOL_TEMPLATE)
                .parameters(notifySolProperties)
                .reference(TEMPLATE_REFERENCE)
                .build();

            //New respondent1(Lip v LR) in this case
            final EmailDTO newSolicitorLR = EmailDTO.builder()
                .targetEmail("respondentsolicitor@example.com")
                .emailTemplate(NEW_SOL_TEMPLATE)
                .parameters(notifySolProperties)
                .reference(TEMPLATE_REFERENCE)
                .build();

            final Set<EmailDTO> emailsToNotify = changeOfRepresentationNotifier.getPartiesToNotify(caseData);
            assertNotNull(emailsToNotify);

            final Set<EmailDTO> expectedEmailDTO = Set.of(claimantLip, otherSolicitorLR, newSolicitorLR);
            assertThat(emailsToNotify).containsAll(expectedEmailDTO);

            caseData = caseData.toBuilder().claimantBilingualLanguagePreference(Language.BOTH.toString()).build();

            //Applicant Lip in this case
            final EmailDTO claimantLipWelsh = EmailDTO.builder()
                .targetEmail("rambo@email.com")
                .emailTemplate(CLAIMANT_LIP_WELSH_TEMPLATE)
                .parameters(notifyClaimLipProps)
                .reference(TEMPLATE_REFERENCE)
                .build();

            final Set<EmailDTO> welshEmailsToNotify = changeOfRepresentationNotifier.getPartiesToNotify(caseData);
            assertNotNull(emailsToNotify);

            final Set<EmailDTO> welshExpectedEmailDTO = Set.of(claimantLipWelsh, otherSolicitorLR, newSolicitorLR);
            assertThat(welshEmailsToNotify).containsAll(welshExpectedEmailDTO);

        }
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
    private Map<String, String> getPropsForClaimantSolicitorIfClaimIsInHR() {
        return Map.of(

        );
    }
}
