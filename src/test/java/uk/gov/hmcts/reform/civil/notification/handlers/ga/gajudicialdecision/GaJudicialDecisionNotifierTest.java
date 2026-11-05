package uk.gov.hmcts.reform.civil.notification.handlers.ga.gajudicialdecision;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.model.OrganisationPolicy;
import uk.gov.hmcts.reform.ccd.model.Organisation;
import uk.gov.hmcts.reform.civil.model.IdamUserDetails;
import uk.gov.hmcts.reform.civil.model.PaymentDetails;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.enums.CaseState;
import uk.gov.hmcts.reform.civil.ga.enums.dq.GAJudgeDecisionOption;
import uk.gov.hmcts.reform.civil.ga.enums.dq.GAJudgeMakeAnOrderOption;
import uk.gov.hmcts.reform.civil.ga.enums.dq.GAJudgeWrittenRepresentationsOptions;
import uk.gov.hmcts.reform.civil.ga.handler.callback.camunda.notification.GaNotificationHandler;
import uk.gov.hmcts.reform.civil.ga.model.GeneralApplicationCaseData;
import uk.gov.hmcts.reform.civil.ga.model.genapplication.GAApproveConsentOrder;
import uk.gov.hmcts.reform.civil.ga.model.genapplication.GAJudicialDecision;
import uk.gov.hmcts.reform.civil.ga.model.genapplication.GAJudicialMakeAnOrder;
import uk.gov.hmcts.reform.civil.ga.model.genapplication.GAJudicialRequestMoreInfo;
import uk.gov.hmcts.reform.civil.ga.model.genapplication.GAJudicialWrittenRepresentations;
import uk.gov.hmcts.reform.civil.ga.model.genapplication.GeneralApplicationPbaDetails;
import uk.gov.hmcts.reform.civil.ga.service.GaForLipService;
import uk.gov.hmcts.reform.civil.ga.service.GaCoreCaseDataService;
import uk.gov.hmcts.reform.civil.ga.service.JudicialNotificationService;
import uk.gov.hmcts.reform.civil.ga.service.SolicitorEmailValidation;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.model.BusinessProcess;
import uk.gov.hmcts.reform.civil.model.GeneralAppParentCaseLink;
import uk.gov.hmcts.reform.civil.model.genapplication.GAApplicationType;
import uk.gov.hmcts.reform.civil.model.genapplication.GAInformOtherParty;
import uk.gov.hmcts.reform.civil.model.genapplication.GARespondentOrderAgreement;
import uk.gov.hmcts.reform.civil.model.genapplication.GASolicitorDetailsGAspec;
import uk.gov.hmcts.reform.civil.notification.handlers.EmailDTO;
import uk.gov.hmcts.reform.civil.notification.handlers.GaNotifierFactory;
import uk.gov.hmcts.reform.civil.notify.NotificationService;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.notify.NotificationsSignatureConfiguration;
import uk.gov.hmcts.reform.civil.service.CaseTaskTrackingService;
import uk.gov.hmcts.reform.civil.service.CoreCaseDataService;
import uk.gov.hmcts.reform.civil.service.DeadlinesCalculator;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.Time;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.RETURNS_DEFAULTS;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;
import static uk.gov.hmcts.reform.civil.enums.dq.GeneralApplicationTypes.EXTEND_TIME;
import static uk.gov.hmcts.reform.civil.enums.dq.GeneralApplicationTypes.STRIKE_OUT;
import static uk.gov.hmcts.reform.civil.ga.enums.dq.GAJudgeRequestMoreInfoOption.REQUEST_MORE_INFORMATION;
import static uk.gov.hmcts.reform.civil.ga.enums.dq.GAJudgeRequestMoreInfoOption.SEND_APP_TO_OTHER_PARTY;
import static uk.gov.hmcts.reform.civil.utils.ElementUtils.element;

class GaJudicialDecisionNotifierTest {

    private static final LocalDateTime NOW = LocalDateTime.of(2026, 11, 4, 12, 0);
    private static final LocalDateTime DEADLINE = NOW.plusDays(7);
    private final NotificationService legacyNotify = mock(NotificationService.class);
    private final NotificationService notify = mock(NotificationService.class);
    private final NotificationsProperties templates = mock(NotificationsProperties.class, invocation ->
        invocation.getMethod().getReturnType() == String.class ? invocation.getMethod().getName()
            : RETURNS_DEFAULTS.answer(invocation));
    private final NotificationsSignatureConfiguration signatures = mock(NotificationsSignatureConfiguration.class, invocation ->
        invocation.getMethod().getReturnType() == String.class ? invocation.getMethod().getName()
            : RETURNS_DEFAULTS.answer(invocation));
    private final GaForLipService lips = mock(GaForLipService.class);
    private final FeatureToggleService toggles = mock(FeatureToggleService.class);
    private final DeadlinesCalculator deadlines = mock(DeadlinesCalculator.class);
    private final Time time = mock(Time.class);
    private final CoreCaseDataService ccd = mock(CoreCaseDataService.class);
    private final CaseDetailsConverter converter = mock(CaseDetailsConverter.class);
    private final SolicitorEmailValidation validation = mock(SolicitorEmailValidation.class);
    private final List<EmailDTO> legacyEmails = new ArrayList<>();
    private final List<EmailDTO> actualEmails = new ArrayList<>();

    static Stream<Arguments> decisions() {
        return Stream.of("CONCURRENT", "SEQUENTIAL", "NULL_CONCURRENT", "NULL_SEQUENTIAL", "HEARING", "FREE_FORM", "APPROVE", "DISMISS", "DIRECTIONS",
                         "REQUEST", "UNCLOAK", "UNCLOAK_CONSENT", "ADDITIONAL_PAYMENT", "ADDITIONAL_PAYMENT_EXISTING", "STRIKE_SPEC", "STRIKE_UNSPEC", "CONSENT")
            .flatMap(decision -> Stream.of(false, true).flatMap(translated -> Stream.of(0, 1, 2, 3)
                .map(mode -> Arguments.of(decision, translated, mode))));
    }

    @ParameterizedTest
    @MethodSource("decisions")
    void shouldMatchLegacyEmailsAndPersistedDeadline(String decision, boolean translated, int mode) {
        final GeneralApplicationCaseData data = caseData(decision, translated);
        configure(mode);
        capture(legacyNotify, legacyEmails);
        capture(notify, actualEmails);
        JudicialNotificationService legacy = new JudicialNotificationService(templates, legacyNotify, deadlines,
            converter, ccd, lips, validation, toggles, signatures);
        String camundaEvent = data.getBusinessProcess().getCamundaEvent();
        data.getBusinessProcess().setActivityId("StartApplicantNotificationProcessMakeDecision");
        GeneralApplicationCaseData afterApplicant = legacy.sendNotification(data.copy().build(), "applicant");
        afterApplicant.getBusinessProcess().setActivityId("StartRespondentNotificationProcessMakeDecision");
        GeneralApplicationCaseData afterRespondent = legacy.sendNotification(afterApplicant, "respondent");

        GeneralApplicationCaseData migrated = data.copy().businessProcess(new BusinessProcess()
            .setCamundaEvent(camundaEvent).setActivityId(GaJudicialDecisionNotifier.TASK_ID)).build();
        var notifier = notifier();
        var factory = new GaNotifierFactory(notifier);
        var handler = new GaNotificationHandler(factory, mock(GaCoreCaseDataService.class), new ObjectMapper().registerModule(new JavaTimeModule()));
        var params = new CallbackParams().type(ABOUT_TO_SUBMIT).caseData(migrated)
            .request(CallbackRequest.builder().eventId(CaseEvent.NOTIFY_EVENT.name()).build());
        var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
        GeneralApplicationCaseData saved = new ObjectMapper().registerModule(new JavaTimeModule())
            .convertValue(response.getData(), GeneralApplicationCaseData.class);

        assertThat(factory.getNotifier(GaJudicialDecisionNotifier.TASK_ID)).isSameAs(notifier);
        assertThat(actualEmails).containsExactlyInAnyOrderElementsOf(legacyEmails)
            .hasSize(decision.startsWith("UNCLOAK") ? 1 : 3);
        assertThat(migrated.getGeneralAppNotificationDeadlineDate())
            .isEqualTo(afterRespondent.getGeneralAppNotificationDeadlineDate());
        assertThat(saved.getGeneralAppNotificationDeadlineDate())
            .isEqualTo(afterRespondent.getGeneralAppNotificationDeadlineDate());
        assertThat(migrated.getNotificationSummary()).contains("general-apps-judicial-notification-make-decision-1000");
        // Two legacy callbacks fetched the parent; the new framework fetches it once.
        verify(ccd, times(3)).getCase(1000L);
        if ("ADDITIONAL_PAYMENT".equals(decision)) {
            verify(deadlines).calculateApplicantResponseDeadline(NOW, 5);
            assertThat(migrated.getGeneralAppNotificationDeadlineDate()).isEqualTo(DEADLINE);
        }
    }

    @Test
    void shouldKeepRefreshedEmailsOnTheReturnedCase() {
        configure(0);
        capture(notify, actualEmails);
        GeneralApplicationCaseData data = caseData("HEARING", false);
        data.getGeneralAppApplnSolicitor().setOrganisationIdentifier("applicant-org");
        var parent = new GeneralApplicationCaseData().ccdState(CaseState.CASE_PROGRESSION)
            .applicant1OrganisationPolicy(new OrganisationPolicy()
                .setOrganisation(new Organisation().setOrganisationID("applicant-org")))
            .applicantSolicitor1UserDetails(new IdamUserDetails().setEmail("refreshed@example.com")).build();
        when(converter.toGeneralApplicationCaseData(any())).thenReturn(parent);
        var helper = new GaJudicialDecisionEmailHelper(templates, signatures, toggles, lips, deadlines, time);
        var parties = new GaJudicialDecisionAllPartiesEmailGenerator(
            new GaJudicialDecisionApplicantEmailGenerator(helper), new GaJudicialDecisionRespondentsEmailGenerator(helper),
            converter, ccd, new SolicitorEmailValidation(lips), helper);
        var notifier = new GaJudicialDecisionNotifier(notify, mock(CaseTaskTrackingService.class), parties);
        data.getBusinessProcess().setActivityId(GaJudicialDecisionNotifier.TASK_ID);
        var handler = new GaNotificationHandler(new GaNotifierFactory(notifier), mock(GaCoreCaseDataService.class),
            new ObjectMapper().registerModule(new JavaTimeModule()));
        var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(new CallbackParams()
            .type(ABOUT_TO_SUBMIT).caseData(data).request(CallbackRequest.builder().eventId("NOTIFY_EVENT").build()));
        GeneralApplicationCaseData saved = new ObjectMapper().registerModule(new JavaTimeModule())
            .convertValue(response.getData(), GeneralApplicationCaseData.class);
        assertThat(saved.getGeneralAppApplnSolicitor().getEmail()).isEqualTo("refreshed@example.com");
        assertThat(actualEmails).extracting(EmailDTO::getTargetEmail).contains("refreshed@example.com")
            .doesNotContain("applicant@example.com");
    }

    @Test
    void shouldSkipAnUnrecognisedDecision() {
        configure(0);
        GeneralApplicationCaseData data = caseData("HEARING", false)
            .businessProcess(new BusinessProcess().setCamundaEvent("OTHER_EVENT")).build();
        assertThat(generator().getPartiesToNotify(data, GaJudicialDecisionNotifier.TASK_ID)).isEmpty();
    }

    @Test
    void shouldNotSendDuplicateRespondentEmails() {
        configure(0);
        GeneralApplicationCaseData data = caseData("HEARING", false);
        data.getGeneralAppRespondentSolicitors().get(1).getValue().setEmail("respondent1@example.com");
        assertThat(generator().getPartiesToNotify(data, GaJudicialDecisionNotifier.TASK_ID)).hasSize(2);
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void shouldRetainDeadlineEvenWithoutRespondentEmails(boolean translated) {
        configure(0);
        GeneralApplicationCaseData data = caseData("ADDITIONAL_PAYMENT", translated)
            .generalAppRespondentSolicitors(List.of()).build();
        var emails = generator().getPartiesToNotify(data, GaJudicialDecisionNotifier.TASK_ID);
        assertThat(emails).hasSize(1);
        assertThat(data.getGeneralAppNotificationDeadlineDate()).isEqualTo(DEADLINE);
    }

    private GaJudicialDecisionNotifier notifier() {
        return new GaJudicialDecisionNotifier(notify, mock(CaseTaskTrackingService.class), generator());
    }

    private GaJudicialDecisionAllPartiesEmailGenerator generator() {
        var helper = new GaJudicialDecisionEmailHelper(templates, signatures, toggles, lips, deadlines, time);
        return new GaJudicialDecisionAllPartiesEmailGenerator(new GaJudicialDecisionApplicantEmailGenerator(helper),
            new GaJudicialDecisionRespondentsEmailGenerator(helper), converter, ccd, validation, helper);
    }

    private void configure(int mode) {
        when(lips.isLipApp(any())).thenReturn((mode & 1) != 0);
        when(lips.isLipResp(any())).thenReturn((mode & 2) != 0);
        when(lips.isGaForLip(any())).thenReturn(mode != 0);
        when(time.now()).thenReturn(NOW);
        when(deadlines.calculateApplicantResponseDeadline(any(), eq(5))).thenReturn(DEADLINE);
        when(ccd.getCase(1000L)).thenReturn(CaseDetails.builder().build());
        when(converter.toGeneralApplicationCaseData(any())).thenReturn(new GeneralApplicationCaseData()
            .ccdState(CaseState.CASE_PROGRESSION).build());
        when(validation.validateSolicitorEmail(any(), any())).thenAnswer(invocation ->
            ((GeneralApplicationCaseData) invocation.getArgument(1)).copy().build());
        when(toggles.isPublicQueryManagementEnabledGa(any())).thenReturn((mode & 1) != 0);
    }

    private GeneralApplicationCaseData caseData(String kind, boolean translated) {
        var data = new GeneralApplicationCaseData().ccdCaseReference(2000L).ccdState(CaseState.ORDER_MADE)
            .generalAppParentCaseLink(new GeneralAppParentCaseLink("1000"))
            .generalAppType(new GAApplicationType().setTypes(List.of(EXTEND_TIME)))
            .generalAppApplnSolicitor(new GASolicitorDetailsGAspec().setEmail("applicant@example.com"))
            .generalAppRespondentSolicitors(List.of(element(new GASolicitorDetailsGAspec().setEmail("respondent1@example.com")),
                element(new GASolicitorDetailsGAspec().setEmail("respondent2@example.com"))))
            .generalAppRespondentAgreement(new GARespondentOrderAgreement().setHasAgreed(NO))
            .generalAppInformOtherParty(new GAInformOtherParty().setIsWithNotice(YES))
            .generalAppPBADetails(new GeneralApplicationPbaDetails())
            .applicantPartyName("Applicant").claimant1PartyName("Claimant").defendant1PartyName("Defendant")
            .parentClaimantIsApplicant(YES).applicant1Represented(YES).respondent1Represented(YES)
            .applicantBilingualLanguagePreference(translated ? YES : NO)
            .respondentBilingualLanguagePreference(translated ? YES : NO)
            .generalAppSuperClaimType("SPEC_CLAIM")
            .businessProcess(new BusinessProcess().setCamundaEvent(translated
                ? "UPLOAD_TRANSLATED_DOCUMENT_JUDGE_DECISION" : "MAKE_DECISION"));
        data.judicialDecision(new GAJudicialDecision().setDecision(GAJudgeDecisionOption.MAKE_AN_ORDER));
        switch (kind) {
            case "CONCURRENT", "SEQUENTIAL", "NULL_CONCURRENT", "NULL_SEQUENTIAL" -> data.judicialDecisionMakeAnOrderForWrittenRepresentations(
                new GAJudicialWrittenRepresentations().setWrittenOption(kind.endsWith("CONCURRENT")
                    ? GAJudgeWrittenRepresentationsOptions.CONCURRENT_REPRESENTATIONS
                    : GAJudgeWrittenRepresentationsOptions.SEQUENTIAL_REPRESENTATIONS)
                    .setWrittenConcurrentRepresentationsBy(kind.startsWith("NULL") ? null : LocalDate.of(2026, 12, 1))
                    .setSequentialApplicantMustRespondWithin(kind.startsWith("NULL") ? null : LocalDate.of(2026, 12, 2)));
            case "HEARING" -> data.judicialDecision(new GAJudicialDecision().setDecision(GAJudgeDecisionOption.LIST_FOR_A_HEARING));
            case "FREE_FORM" -> data.judicialDecision(new GAJudicialDecision().setDecision(GAJudgeDecisionOption.FREE_FORM_ORDER));
            case "APPROVE", "STRIKE_SPEC", "STRIKE_UNSPEC" -> {
                data.judicialDecisionMakeOrder(new GAJudicialMakeAnOrder().setMakeAnOrder(GAJudgeMakeAnOrderOption.APPROVE_OR_EDIT));
                if (kind.startsWith("STRIKE")) {
                    data.generalAppType(new GAApplicationType().setTypes(List.of(STRIKE_OUT)))
                        .generalAppSuperClaimType("STRIKE_SPEC".equals(kind) ? "SPEC_CLAIM" : "UNSPEC_CLAIM");
                }
            }
            case "DISMISS" -> data.judicialDecisionMakeOrder(
                new GAJudicialMakeAnOrder().setMakeAnOrder(GAJudgeMakeAnOrderOption.DISMISS_THE_APPLICATION));
            case "DIRECTIONS" -> data.judicialDecisionMakeOrder(
                new GAJudicialMakeAnOrder().setMakeAnOrder(GAJudgeMakeAnOrderOption.GIVE_DIRECTIONS_WITHOUT_HEARING));
            case "REQUEST", "UNCLOAK", "UNCLOAK_CONSENT", "ADDITIONAL_PAYMENT", "ADDITIONAL_PAYMENT_EXISTING" -> {
                data.judicialDecision(new GAJudicialDecision().setDecision(GAJudgeDecisionOption.REQUEST_MORE_INFO))
                    .judicialDecisionRequestMoreInfo(new GAJudicialRequestMoreInfo()
                        .setRequestMoreInfoOption("REQUEST".equals(kind) ? REQUEST_MORE_INFORMATION : SEND_APP_TO_OTHER_PARTY)
                        .setJudgeRequestMoreInfoByDate(LocalDate.of(2026, 12, 3)));
                if (!"REQUEST".equals(kind)) {
                    data.generalAppInformOtherParty(new GAInformOtherParty().setIsWithNotice(NO));
                }
                if ("UNCLOAK_CONSENT".equals(kind)) {
                    data.generalAppConsentOrder(YES)
                        .generalAppRespondentAgreement(new GARespondentOrderAgreement().setHasAgreed(YES))
                        .generalAppInformOtherParty(new GAInformOtherParty().setIsWithNotice(YES));
                }
                if (kind.startsWith("ADDITIONAL_PAYMENT")) {
                    data.ccdState(CaseState.APPLICATION_ADD_PAYMENT);
                    if ("ADDITIONAL_PAYMENT_EXISTING".equals(kind)) {
                        data.generalAppPBADetails(new GeneralApplicationPbaDetails()
                            .setAdditionalPaymentDetails(new PaymentDetails()));
                    }
                }
            }
            case "CONSENT" -> data.businessProcess(new BusinessProcess().setCamundaEvent("APPROVE_CONSENT_ORDER"))
                .approveConsentOrder(new GAApproveConsentOrder());
            default -> throw new IllegalArgumentException(kind);
        }
        return data.build();
    }

    private void capture(NotificationService service, List<EmailDTO> emails) {
        doAnswer(invocation -> {
            EmailDTO email = new EmailDTO();
            email.setTargetEmail(invocation.getArgument(0));
            email.setEmailTemplate(invocation.getArgument(1));
            email.setParameters(new HashMap<>(invocation.<Map<String, String>>getArgument(2)));
            email.setReference(invocation.getArgument(3));
            emails.add(email);
            return null;
        }).when(service).sendMail(any(), any(), any(), any());
    }
}
