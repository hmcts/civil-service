package uk.gov.hmcts.reform.civil.notification.handlers.stayupdaterequested;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.ArgumentCaptor;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotifyClaimantStayUpdateRequestedHandler;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotifyDefendantStayUpdateRequestedHandler;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.IdamUserDetails;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.model.citizenui.CaseDataLiP;
import uk.gov.hmcts.reform.civil.model.citizenui.RespondentLiPResponse;
import uk.gov.hmcts.reform.civil.notification.handlers.NotifierFactory;
import uk.gov.hmcts.reform.civil.notify.NotificationService;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.notify.NotificationsSignatureConfiguration;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.CaseTaskTrackingService;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.flowstate.SimpleStateFlowEngine;
import uk.gov.hmcts.reform.civil.stateflow.StateFlow;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.RETURNS_DEFAULTS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowFlag.ONE_RESPONDENT_REPRESENTATIVE;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowFlag.TWO_RESPONDENT_REPRESENTATIVES;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowFlag.UNREPRESENTED_DEFENDANT_ONE;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowFlag.UNREPRESENTED_DEFENDANT_TWO;

@SuppressWarnings("removal")
class StayUpdateRequestedNotifierTest {

    private final NotificationService legacyService = mock(NotificationService.class);
    private final NotificationService notificationService = mock(NotificationService.class);
    private final CaseTaskTrackingService trackingService = mock(CaseTaskTrackingService.class);
    private final NotificationsProperties templates = mock(NotificationsProperties.class, invocation ->
        invocation.getMethod().getReturnType() == String.class ? invocation.getMethod().getName()
            : RETURNS_DEFAULTS.answer(invocation));
    private final NotificationsSignatureConfiguration signatures = mock(NotificationsSignatureConfiguration.class,
        invocation -> invocation.getMethod().getReturnType() == String.class ? invocation.getMethod().getName()
            : RETURNS_DEFAULTS.answer(invocation));
    private final FeatureToggleService featureToggleService = mock(FeatureToggleService.class);
    private final SimpleStateFlowEngine engine = mock(SimpleStateFlowEngine.class);
    private final StateFlow flow = mock(StateFlow.class);
    private StayUpdateRequestedNotifier notifier;

    @BeforeEach
    void setUp() {
        when(engine.evaluate(any(CaseData.class))).thenReturn(flow);
        var helper = new StayUpdateRequestedEmailHelper(templates, signatures, featureToggleService);
        notifier = new StayUpdateRequestedNotifier(notificationService, trackingService, new StayUpdateRequestedAllPartiesEmailGenerator(
            new StayUpdateRequestedClaimantEmailDTOGenerator(helper),
            new StayUpdateRequestedDefendantEmailDTOGenerator(helper),
            new StayUpdateRequestedDefendantTwoEmailDTOGenerator(helper, engine)));
    }

    @ParameterizedTest
    @CsvSource({
        "false, false, false, false, false, false",
        "false, false, true, false, false, false",
        "true, false, false, false, false, false",
        "true, false, false, true, false, false",
        "true, true, false, true, false, false",
        "true, true, false, true, true, false",
        "false, false, true, false, true, true",
        "true, true, true, true, true, true"
    })
    void shouldPreserveLegacyEmails(boolean claimantLip, boolean claimantWelsh, boolean defendantLip,
                                   boolean defendantWelsh, boolean twoRepresentatives, boolean queryManagement) {
        CaseData data = caseData(claimantLip, claimantWelsh, defendantLip, defendantWelsh);
        when(flow.isFlagSet(TWO_RESPONDENT_REPRESENTATIVES)).thenReturn(twoRepresentatives);
        when(featureToggleService.isPublicQueryManagementEnabled(data)).thenReturn(queryManagement);
        sendLegacy(data, CaseEvent.NOTIFY_CLAIMANT_STAY_UPDATE_REQUESTED);
        sendLegacy(data, CaseEvent.NOTIFY_DEFENDANT_STAY_UPDATE_REQUESTED);
        if (twoRepresentatives) {
            sendLegacy(data, CaseEvent.NOTIFY_DEFENDANT2_STAY_UPDATE_REQUESTED);
        }

        assertThat(new NotifierFactory(notifier).getNotifier("StayUpdateRequestedNotifyParties")).isSameAs(notifier);
        notifier.notifyParties(data, "NOTIFY_EVENT", "StayUpdateRequestedNotifyParties");

        assertThat(emails(notificationService, twoRepresentatives ? 3 : 2))
            .containsExactlyInAnyOrderElementsOf(emails(legacyService, twoRepresentatives ? 3 : 2));
        verifyNoInteractions(trackingService);
    }

    @ParameterizedTest
    @CsvSource({"false, false, false, false, 2", "false, false, true, false, 3",
        "false, false, false, true, 3", "true, false, true, false, 2", "false, true, false, true, 2"})
    void shouldPreserveGatewayRecipientSelection(boolean oneRepresentative, boolean unrepresentedOne,
                                                boolean twoRepresentatives, boolean unrepresentedTwo, int count) {
        when(flow.isFlagSet(ONE_RESPONDENT_REPRESENTATIVE)).thenReturn(oneRepresentative);
        when(flow.isFlagSet(UNREPRESENTED_DEFENDANT_ONE)).thenReturn(unrepresentedOne);
        when(flow.isFlagSet(TWO_RESPONDENT_REPRESENTATIVES)).thenReturn(twoRepresentatives);
        when(flow.isFlagSet(UNREPRESENTED_DEFENDANT_TWO)).thenReturn(unrepresentedTwo);
        notifier.notifyParties(caseData(false, false, false, false), "NOTIFY_EVENT", "StayUpdateRequestedNotifyParties");
        assertThat(emails(notificationService, count)).hasSize(count);
    }

    @ParameterizedTest
    @CsvSource(value = {"NULL", "''"}, nullValues = "NULL")
    void shouldPreserveLegacyNotificationsWithMissingSolicitorEmails(String email) {
        CaseData data = caseData(false, false, false, false).toBuilder()
            .applicantSolicitor1UserDetails(new IdamUserDetails().setEmail(email))
            .respondentSolicitor1EmailAddress(email).respondentSolicitor2EmailAddress(email).build();
        assertLegacyParityForAllParties(data);
    }

    @ParameterizedTest
    @CsvSource(value = {"NULL", "''"}, nullValues = "NULL")
    void shouldPreserveLipEmailAndSolicitorFallback(String email) {
        CaseData data = caseData(true, true, true, true).toBuilder()
            .claimantUserDetails(new IdamUserDetails().setEmail(email))
            .respondent1(party("Defendant", email)).build();
        assertLegacyParityForAllParties(data);
    }

    private void assertLegacyParityForAllParties(CaseData data) {
        when(flow.isFlagSet(TWO_RESPONDENT_REPRESENTATIVES)).thenReturn(true);
        sendLegacy(data, CaseEvent.NOTIFY_CLAIMANT_STAY_UPDATE_REQUESTED);
        sendLegacy(data, CaseEvent.NOTIFY_DEFENDANT_STAY_UPDATE_REQUESTED);
        sendLegacy(data, CaseEvent.NOTIFY_DEFENDANT2_STAY_UPDATE_REQUESTED);
        notifier.notifyParties(data, "NOTIFY_EVENT", "StayUpdateRequestedNotifyParties");
        assertThat(emails(notificationService, 3)).containsExactlyInAnyOrderElementsOf(emails(legacyService, 3));
        verifyNoInteractions(trackingService);
    }

    private CaseData caseData(boolean claimantLip, boolean claimantWelsh, boolean defendantLip, boolean defendantWelsh) {
        return CaseDataBuilder.builder().atStateClaimDetailsNotified().build().toBuilder()
            .applicant1(party("Claimant", "claimant@example.com"))
            .respondent1(party("Defendant", "defendant@example.com"))
            .respondent2(party("Second", "second@example.com"))
            .addRespondent2(YesOrNo.YES).respondent2SameLegalRepresentative(YesOrNo.NO)
            .applicant1Represented(claimantLip ? YesOrNo.NO : YesOrNo.YES)
            .respondent1Represented(defendantLip ? YesOrNo.NO : YesOrNo.YES)
            .claimantBilingualLanguagePreference(claimantWelsh ? "BOTH" : "ENGLISH")
            .caseDataLiP(new CaseDataLiP().setRespondent1LiPResponse(new RespondentLiPResponse()
                .setRespondent1ResponseLanguage(defendantWelsh ? "BOTH" : "ENGLISH")))
            .claimantUserDetails(new IdamUserDetails().setEmail("claimant@example.com"))
            .applicantSolicitor1UserDetails(new IdamUserDetails().setEmail("claimant-sol@example.com"))
            .respondentSolicitor1EmailAddress("defendant-sol@example.com")
            .respondentSolicitor2EmailAddress("second-sol@example.com")
            .build();
    }

    private Party party(String name, String email) {
        return new Party().setType(Party.Type.INDIVIDUAL).setIndividualFirstName(name)
            .setIndividualLastName("Example").setPartyEmail(email);
    }

    private void sendLegacy(CaseData data, CaseEvent event) {
        CallbackParams params = new CallbackParams().caseData(data)
            .request(CallbackRequest.builder().eventId(event.name()).build());
        if (event == CaseEvent.NOTIFY_CLAIMANT_STAY_UPDATE_REQUESTED) {
            new NotifyClaimantStayUpdateRequestedHandler(legacyService, templates, signatures, featureToggleService)
                .sendNotification(params);
        } else {
            new NotifyDefendantStayUpdateRequestedHandler(legacyService, templates, signatures, featureToggleService)
                .sendNotification(params);
        }
    }

    @SuppressWarnings("unchecked")
    private List<List<Object>> emails(NotificationService service, int count) {
        ArgumentCaptor<String> recipient = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> template = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Map<String, String>> properties = ArgumentCaptor.forClass(Map.class);
        ArgumentCaptor<String> reference = ArgumentCaptor.forClass(String.class);
        verify(service, times(count)).sendMail(recipient.capture(), template.capture(), properties.capture(), reference.capture());
        return IntStream.range(0, count).mapToObj(i -> Arrays.<Object>asList(
            recipient.getAllValues().get(i), template.getAllValues().get(i), properties.getAllValues().get(i),
            reference.getAllValues().get(i))).toList();
    }
}
