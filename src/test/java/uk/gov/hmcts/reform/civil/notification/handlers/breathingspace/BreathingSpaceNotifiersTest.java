package uk.gov.hmcts.reform.civil.notification.handlers.breathingspace;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.model.OrganisationPolicy;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.BreathingSpaceEnterNotificationHandler;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.BreathingSpaceLiftedNotificationHandler;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationHandler;
import uk.gov.hmcts.reform.civil.model.BusinessProcess;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.model.StatementOfTruth;
import uk.gov.hmcts.reform.civil.notification.handlers.Notifier;
import uk.gov.hmcts.reform.civil.notification.handlers.NotifierFactory;
import uk.gov.hmcts.reform.civil.notify.NotificationService;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.notify.NotificationsSignatureConfiguration;
import uk.gov.hmcts.reform.civil.prd.model.Organisation;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.CaseTaskTrackingService;
import uk.gov.hmcts.reform.civil.service.CoreCaseDataService;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.OrganisationService;
import uk.gov.hmcts.reform.civil.service.flowstate.SimpleStateFlowEngine;
import uk.gov.hmcts.reform.civil.stateflow.StateFlow;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.RETURNS_DEFAULTS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockingDetails;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowFlag.ONE_RESPONDENT_REPRESENTATIVE;
import static uk.gov.hmcts.reform.civil.service.flowstate.FlowFlag.TWO_RESPONDENT_REPRESENTATIVES;

@SuppressWarnings("removal")
class BreathingSpaceNotifiersTest {

    private final NotificationService legacyService = mock(NotificationService.class);
    private final NotificationService notificationService = mock(NotificationService.class);
    private final CaseTaskTrackingService tracking = mock(CaseTaskTrackingService.class);
    private final NotificationsProperties templates = mock(NotificationsProperties.class, invocation ->
        invocation.getMethod().getReturnType() == String.class ? invocation.getMethod().getName()
            : RETURNS_DEFAULTS.answer(invocation));
    private final NotificationsSignatureConfiguration signatures = mock(NotificationsSignatureConfiguration.class, invocation ->
        invocation.getMethod().getReturnType() == String.class ? invocation.getMethod().getName()
            : RETURNS_DEFAULTS.answer(invocation));
    private final FeatureToggleService toggles = mock(FeatureToggleService.class);
    private final OrganisationService organisations = mock(OrganisationService.class);
    private final SimpleStateFlowEngine engine = mock(SimpleStateFlowEngine.class);
    private final StateFlow flow = mock(StateFlow.class);
    private final BreathingSpaceEmailHelper helper = new BreathingSpaceEmailHelper(organisations, signatures, toggles);

    @ParameterizedTest
    @CsvSource({
        "true,false,false,false", "true,true,false,true", "true,true,true,false", "true,true,true,true",
        "false,false,false,false", "false,false,false,true", "false,false,true,false", "false,false,true,true"
    })
    void shouldDispatchUnchangedLegacyEmails(boolean enter, boolean twoRepresentatives,
                                            boolean applicantOrganisationMissing, boolean queryManagement) {
        CaseData data = caseData();
        when(organisations.findOrganisationById("applicant"))
            .thenReturn(applicantOrganisationMissing ? Optional.empty() : Optional.of(new Organisation().setName("Claimant firm")));
        when(organisations.findOrganisationById("respondent1")).thenReturn(Optional.of(new Organisation().setName("Defendant firm")));
        when(organisations.findOrganisationById("respondent2")).thenReturn(Optional.of(new Organisation().setName("Second firm")));
        when(toggles.isPublicQueryManagementEnabled(data)).thenReturn(queryManagement);
        assertParity(data, enter, twoRepresentatives);
    }

    @ParameterizedTest
    @CsvSource(value = {"NULL,YES", "NULL,NO", "'',YES"}, nullValues = "NULL")
    void shouldPreserveRespondentTwoAddressFallback(String email, YesOrNo sameRepresentative) {
        when(organisations.findOrganisationById(any())).thenReturn(Optional.of(new Organisation().setName("Firm")));
        CaseData data = caseData().toBuilder().respondentSolicitor2EmailAddress(email)
            .respondent2SameLegalRepresentative(sameRepresentative).build();
        assertParity(data, true, true);
    }

    @Test
    void shouldPreserveEnterPropertiesWithoutOrganisationPolicies() {
        CaseData data = caseData().toBuilder().applicant1OrganisationPolicy(null)
            .respondent1OrganisationPolicy(null).respondent2OrganisationPolicy(null).build();
        assertParity(data, true, true);
    }

    @Test
    void shouldNotRequireStatementOfTruthWhenLiftedApplicantOrganisationIsPresent() {
        when(organisations.findOrganisationById(any())).thenReturn(Optional.of(new Organisation().setName("Firm")));
        assertParity(caseData().toBuilder().applicantSolicitor1ClaimStatementOfTruth(null).build(), false, false);
    }

    @Test
    void shouldGiveOneRepresentativeBranchPrecedence() {
        CaseData data = caseData();
        when(engine.evaluate(data)).thenReturn(flow);
        when(flow.isFlagSet(ONE_RESPONDENT_REPRESENTATIVE)).thenReturn(true);
        when(flow.isFlagSet(TWO_RESPONDENT_REPRESENTATIVES)).thenReturn(true);
        assertThat(new BreathingSpaceEnterRespSolTwoEmailDTOGenerator(organisations, helper, templates, engine)
                       .getShouldNotify(data)).isFalse();
    }

    private void assertParity(CaseData data, boolean enter, boolean twoRepresentatives) {
        Notifier notifier;
        String taskId;
        if (enter) {
            when(engine.evaluate(data)).thenReturn(flow);
            when(flow.isFlagSet(ONE_RESPONDENT_REPRESENTATIVE)).thenReturn(!twoRepresentatives);
            when(flow.isFlagSet(TWO_RESPONDENT_REPRESENTATIVES)).thenReturn(twoRepresentatives);
            notifier = new BreathingSpaceEnterNotifier(notificationService, tracking, new BreathingSpaceEnterAllPartiesEmailGenerator(
                new BreathingSpaceEnterAppSolOneEmailDTOGenerator(organisations, helper, templates),
                new BreathingSpaceEnterRespSolOneEmailDTOGenerator(organisations, helper, templates),
                new BreathingSpaceEnterRespSolTwoEmailDTOGenerator(organisations, helper, templates, engine)));
            taskId = "BreathingSpaceEnterNotifyParties";
            var legacy = new BreathingSpaceEnterNotificationHandler(legacyService, templates, organisations, signatures, toggles);
            legacy.handle(params(data, CaseEvent.NOTIFY_APPLICANT_SOLICITOR1_BREATHING_SPACE_ENTER));
            legacy.handle(params(data, CaseEvent.NOTIFY_RESPONDENT_SOLICITOR1_BREATHING_SPACE_ENTER));
            if (twoRepresentatives) {
                legacy.handle(params(data, CaseEvent.NOTIFY_RESPONDENT_SOLICITOR2_BREATHING_SPACE_ENTER));
            }
        } else {
            notifier = new BreathingSpaceLiftedNotifier(notificationService, tracking, new BreathingSpaceLiftedAllPartiesEmailGenerator(
                new BreathingSpaceLiftedAppSolOneEmailDTOGenerator(organisations, helper, templates),
                new BreathingSpaceLiftedRespSolOneEmailDTOGenerator(organisations, helper, templates)));
            taskId = "BreathingSpaceLiftedNotifyParties";
            var legacy = new BreathingSpaceLiftedNotificationHandler(legacyService, templates, organisations, signatures, toggles);
            legacy.handle(params(data, CaseEvent.NOTIFY_APPLICANT_SOLICITOR1_BREATHING_SPACE_LIFTED));
            legacy.handle(params(data, CaseEvent.NOTIFY_RESPONDENT_SOLICITOR1_BREATHING_SPACE_LIFTED));
        }
        data.setBusinessProcess(new BusinessProcess().setActivityId(taskId));
        var factory = new NotifierFactory(notifier);
        assertThat(factory.getNotifier(taskId)).isSameAs(notifier);
        new NotificationHandler(factory, mock(CoreCaseDataService.class), new ObjectMapper().registerModule(new JavaTimeModule()))
            .handle(params(data, CaseEvent.NOTIFY_EVENT));
        assertThat(emails(legacyService)).hasSize(enter && twoRepresentatives ? 3 : 2);
        assertThat(emails(notificationService)).containsExactlyInAnyOrderElementsOf(emails(legacyService));
        verifyNoInteractions(tracking);
    }

    private CallbackParams params(CaseData data, CaseEvent event) {
        return new CallbackParams().type(ABOUT_TO_SUBMIT).caseData(data)
            .request(CallbackRequest.builder().eventId(event.name()).build());
    }

    private List<List<Object>> emails(NotificationService service) {
        return mockingDetails(service).getInvocations().stream()
            .filter(invocation -> invocation.getMethod().getName().equals("sendMail"))
            .map(invocation -> Arrays.asList(invocation.getArguments())).toList();
    }

    private CaseData caseData() {
        return CaseDataBuilder.builder().atStateClaimIssued().build().toBuilder()
            .applicant1OrganisationPolicy(policy("applicant"))
            .respondent1OrganisationPolicy(policy("respondent1"))
            .respondent2OrganisationPolicy(policy("respondent2"))
            .respondent2(new Party().setType(Party.Type.COMPANY).setCompanyName("Second Defendant"))
            .applicantSolicitor1ClaimStatementOfTruth(new StatementOfTruth().setName("Claimant fallback"))
            .respondentSolicitor2EmailAddress("second@example.com").build();
    }

    private OrganisationPolicy policy(String id) {
        return new OrganisationPolicy().setOrganisation(new uk.gov.hmcts.reform.ccd.model.Organisation().setOrganisationID(id));
    }
}
