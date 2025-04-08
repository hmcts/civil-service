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

    private static final String NOTIFY_FORMER_SOLICITOR = "NOTIFY_FORMER_SOLICITOR";
    private static final String NOTIFY_OTHER_SOLICITOR_1 = "NOTIFY_OTHER_SOLICITOR_1";
    private static final String NOTIFY_OTHER_SOLICITOR_2 = "NOTIFY_OTHER_SOLICITOR_2";
    private static final String NOTIFY_NEW_DEFENDANT_SOLICITOR = "NOTIFY_NEW_DEFENDANT_SOLICITOR";
    private static final String NOTIFY_APPLICANT_SOLICITOR_FOR_HEARING_FEE_AFTER_NOC = "NOTIFY_APPLICANT_SOLICITOR_FOR_HEARING_FEE_AFTER_NOC";


    @Nested
    class NotifyFormerSolicitor {

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
        void shouldReturnTemplateIdForFormerSolicitor() {
            when(notificationsProperties.getNoticeOfChangeFormerSolicitor()).thenReturn("former-sol-template");
            String result = changeOfRepresentationNotifier.getTemplateId(caseData, NOTIFY_FORMER_SOLICITOR);
            assertEquals("former-sol-template", result);
        }

        @Test
        void shouldReturnFormerSolicitorEmail() {
            String result = changeOfRepresentationNotifier.getRecipientEmail(caseData, NOTIFY_FORMER_SOLICITOR);
            assertEquals("previous-solicitor@example.com", result);
        }

        @Test
        void shouldReturnFormerSolicitorToNotify() {
            when(stateFlowEngine.evaluate(any(CaseData.class))).thenReturn(stateFlow);
            when(stateFlow.getState()).thenReturn(State.from("state1"));
            when(stateFlow.isFlagSet(TWO_RESPONDENT_REPRESENTATIVES)).thenReturn(true);
            when(notificationsProperties.getNoticeOfChangeFormerSolicitor()).thenReturn("former-sol-template");
            when(organisationService.findOrganisationById("Previous-sol-id"))
                .thenReturn(Optional.of(Organisation.builder().name(PREVIOUS_SOL).build()));
            when(organisationService.findOrganisationById("New-sol-id"))
                .thenReturn(Optional.of(Organisation.builder().name(NEW_SOLICITOR).build()));
            when(organisationService.findOrganisationById("QWERTY A"))
                .thenReturn(Optional.of(Organisation.builder().name(OTHER_SOLICITOR).build()));
            when(organisationService.findOrganisationById("QWERTY R2"))
                .thenReturn(Optional.of(Organisation.builder().name(OTHER_SOLICITOR_2).build()));

            Map<String, String> notifyProperties = new HashMap<>(getProperties(caseData));
            notifyProperties.put(OTHER_SOL_NAME, OTHER_SOLICITOR);

            final EmailDTO formerSolicitor = EmailDTO.builder()
                .targetEmail("previous-solicitor@example.com")
                .emailTemplate("former-sol-template")
                .parameters(notifyProperties)
                .reference(TEMPLATE_REFERENCE)
                .build();

            final Set<EmailDTO> emailsToNotify = changeOfRepresentationNotifier.getPartiesToNotify(caseData);
            assertNotNull(emailsToNotify);

            final Set<EmailDTO> expectedEmailDTO = Set.of(formerSolicitor);
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

    @NotNull
    private Map<String, String> getPropsForClaimantLip() {
        return Map.of(
            CLAIMANT_NAME, "",
            CLAIM_16_DIGIT_NUMBER, CASE_ID.toString(),
            DEFENDANT_NAME_INTERIM, RESPONDENT_NAME,
            CLAIM_NUMBER, ""
        );
    }

    @NotNull
    private Map<String, String> getPropsForClaimantSolicitorIfClaimIsInHR() {
        return Map.of(

        );
    }
}
