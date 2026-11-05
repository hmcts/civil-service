package uk.gov.hmcts.reform.civil.notification.handlers.ga.gaapplicationcreated;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.enums.CaseState;
import uk.gov.hmcts.reform.civil.enums.PaymentStatus;
import uk.gov.hmcts.reform.civil.ga.handler.callback.camunda.notification.GaNotificationHandler;
import uk.gov.hmcts.reform.civil.ga.handler.callback.camunda.notification.GeneralApplicationCreationNotificationHandler;
import uk.gov.hmcts.reform.civil.ga.model.GeneralApplicationCaseData;
import uk.gov.hmcts.reform.civil.ga.model.genapplication.GeneralApplicationPbaDetails;
import uk.gov.hmcts.reform.civil.ga.service.GaCoreCaseDataService;
import uk.gov.hmcts.reform.civil.ga.service.GaForLipService;
import uk.gov.hmcts.reform.civil.ga.service.GeneralApplicationCreationNotificationService;
import uk.gov.hmcts.reform.civil.ga.service.SolicitorEmailValidation;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.model.BusinessProcess;
import uk.gov.hmcts.reform.civil.model.Fee;
import uk.gov.hmcts.reform.civil.model.GeneralAppParentCaseLink;
import uk.gov.hmcts.reform.civil.model.PaymentDetails;
import uk.gov.hmcts.reform.civil.model.genapplication.GAInformOtherParty;
import uk.gov.hmcts.reform.civil.model.genapplication.GASolicitorDetailsGAspec;
import uk.gov.hmcts.reform.civil.model.genapplication.GAUrgencyRequirement;
import uk.gov.hmcts.reform.civil.notification.handlers.EmailDTO;
import uk.gov.hmcts.reform.civil.notification.handlers.GaNotifierFactory;
import uk.gov.hmcts.reform.civil.notification.handlers.ga.GaNotificationContext;
import uk.gov.hmcts.reform.civil.notify.NotificationService;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.notify.NotificationsSignatureConfiguration;
import uk.gov.hmcts.reform.civil.service.CaseTaskTrackingService;
import uk.gov.hmcts.reform.civil.service.CoreCaseDataService;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.GeneralAppFeesService;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.RETURNS_DEFAULTS;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;
import static uk.gov.hmcts.reform.civil.utils.ElementUtils.element;

@SuppressWarnings("removal")
class GaApplicationCreatedNotifierTest {

    private final ObjectMapper mapper = new ObjectMapper().registerModule(new JavaTimeModule());
    private final NotificationService legacyMail = mock(NotificationService.class);
    private final NotificationService newMail = mock(NotificationService.class);
    private final CoreCaseDataService ccd = mock(CoreCaseDataService.class);
    private final GaCoreCaseDataService gaCcd = mock(GaCoreCaseDataService.class);
    private final CaseDetailsConverter converter = mock(CaseDetailsConverter.class);
    private final SolicitorEmailValidation validator = mock(SolicitorEmailValidation.class);
    private final GeneralAppFeesService fees = mock(GeneralAppFeesService.class);
    private final GaForLipService lips = new GaForLipService();
    private final FeatureToggleService toggles = mock(FeatureToggleService.class);
    private final NotificationsProperties templates = mock(NotificationsProperties.class, invocation ->
        invocation.getMethod().getReturnType() == String.class ? invocation.getMethod().getName()
            : RETURNS_DEFAULTS.answer(invocation));
    private final NotificationsSignatureConfiguration signatures = mock(NotificationsSignatureConfiguration.class, invocation ->
        invocation.getMethod().getReturnType() == String.class ? invocation.getMethod().getName()
            : RETURNS_DEFAULTS.answer(invocation));

    static Stream<Arguments> scenarios() {
        return Stream.of("WITH_NOTICE", "WITHOUT_NOTICE", "UNCLOAKED", "NOT_CLOAKED")
            .flatMap(route -> Stream.of("NORMAL", "URGENT", "MISSING").flatMap(urgency ->
                Stream.of("FREE", "PAID", "UNPAID", "NO_PBA").flatMap(payment ->
                    Stream.of(0, 1, 2, 3).flatMap(party -> Stream.of(false, true).flatMap(pending ->
                        Stream.of(false, true).map(free -> Arguments.of(route, urgency, payment, party, pending, free)))))));
    }

    @ParameterizedTest
    @MethodSource("scenarios")
    void shouldMatchLegacyCallback(String route, String urgency, String payment, int party, boolean pending, boolean free) {
        var parent = new GeneralApplicationCaseData().ccdCaseReference(1000L).ccdState(CaseState.CASE_PROGRESSION).build();
        when(converter.toGeneralApplicationCaseData(any())).thenReturn(parent);
        when(validator.validateSolicitorEmail(any(), any())).thenAnswer(invocation -> {
            var copy = ((GeneralApplicationCaseData) invocation.getArgument(1)).copy().build();
            copy.setGeneralAppRespondentSolicitors(List.of(element(new GASolicitorDetailsGAspec().setEmail("fresh1@example.com")),
                element(new GASolicitorDetailsGAspec().setEmail("fresh2@example.com"))));
            return copy;
        });
        when(fees.isFreeApplication(any())).thenReturn(free);
        when(toggles.isPublicQueryManagementEnabledGa(any())).thenReturn(free);
        var data = data(route, urgency, payment, party, pending);
        var legacyEmails = new ArrayList<EmailDTO>();
        var actualEmails = new ArrayList<EmailDTO>();
        capture(legacyMail, legacyEmails);
        capture(newMail, actualEmails);
        var service = service();
        var legacy = new GeneralApplicationCreationNotificationHandler(mapper, service, fees);
        legacy.handle(params(data, "NOTIFY_GENERAL_APPLICATION_RESPONDENT"));
        var respondents = new GaApplicationCreatedRespondentsEmailGenerator(templates, lips, service);
        var parties = new GaApplicationCreatedAllPartiesEmailGenerator(respondents, converter, ccd, validator, fees);
        var notifier = new GaApplicationCreatedNotifier(newMail, mock(CaseTaskTrackingService.class), parties);
        var factory = new GaNotifierFactory(notifier);
        var handler = new GaNotificationHandler(factory, gaCcd, mapper);
        var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params(data, "NOTIFY_EVENT"));

        assertThat(factory.getNotifier(GaApplicationCreatedNotifier.TASK_ID)).isSameAs(notifier);
        boolean sends = (free || !pending) && !"WITHOUT_NOTICE".equals(route)
            && ("NORMAL".equals(urgency) || "FREE".equals(payment) || "PAID".equals(payment));
        assertThat(actualEmails).containsExactlyInAnyOrderElementsOf(legacyEmails)
            .hasSize(sends ? 2 : 0);
        var saved = mapper.convertValue(response.getData(), GeneralApplicationCaseData.class);
        assertThat(saved.getGeneralAppRespondentSolicitors()).isEqualTo(data.getGeneralAppRespondentSolicitors());
        assertThat(saved.getBusinessProcess()).isEqualTo(data.getBusinessProcess());
        if (sends) {
            assertThat(saved.getNotificationSummary()).contains("fresh1@example.com", "fresh2@example.com");
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {"NULL_LIST", "EMPTY_LIST", "EMPTY_FIRST_EMAIL", "NULL_FIRST_EMAIL", "INVALID_LATER_ENTRIES", "SAME_SOLICITOR"})
    void shouldPreserveRecipientSelection(String scenario) {
        var parent = new GeneralApplicationCaseData().ccdState(CaseState.CASE_PROGRESSION).build();
        var data = data("WITH_NOTICE", "NORMAL", "FREE", 0, false);
        var respondents = new ArrayList<>(data.getGeneralAppRespondentSolicitors());
        switch (scenario) {
            case "NULL_LIST" -> data.setGeneralAppRespondentSolicitors(null);
            case "EMPTY_LIST" -> data.setGeneralAppRespondentSolicitors(List.of());
            case "EMPTY_FIRST_EMAIL" -> respondents.getFirst().getValue().setEmail("");
            case "NULL_FIRST_EMAIL" -> respondents.getFirst().getValue().setEmail(null);
            case "INVALID_LATER_ENTRIES" -> {
                respondents.add(null);
                respondents.add(new uk.gov.hmcts.reform.civil.model.common.Element<>());
                respondents.add(element(new GASolicitorDetailsGAspec()));
            }
            case "SAME_SOLICITOR" -> respondents.add(element(respondents.getFirst().getValue()));
            default -> throw new IllegalArgumentException(scenario);
        }
        if (!"NULL_LIST".equals(scenario) && !"EMPTY_LIST".equals(scenario)) {
            data.setGeneralAppRespondentSolicitors(respondents);
        }
        when(converter.toGeneralApplicationCaseData(any())).thenReturn(parent);
        when(validator.validateSolicitorEmail(any(), any())).thenReturn(data);
        var legacyEmails = new ArrayList<EmailDTO>();
        capture(legacyMail, legacyEmails);
        var service = service();
        service.sendNotification(data);
        var generator = new GaApplicationCreatedRespondentsEmailGenerator(templates, lips, service);
        var context = new GaNotificationContext(data, parent);
        var actual = generator.getShouldNotify(context)
            ? generator.buildEmailDTOs(context, GaApplicationCreatedNotifier.TASK_ID) : java.util.Set.<EmailDTO>of();
        assertThat(actual).containsExactlyInAnyOrderElementsOf(new java.util.HashSet<>(legacyEmails))
            .hasSize("INVALID_LATER_ENTRIES".equals(scenario) || "SAME_SOLICITOR".equals(scenario) ? 1 : 0);
    }

    @Test
    void shouldBuildTheSameFirstRespondentEmailThroughBothGeneratorMethods() {
        var data = data("WITH_NOTICE", "NORMAL", "FREE", 0, false);
        var context = new GaNotificationContext(data, new GeneralApplicationCaseData().build());
        var respondent = new GaApplicationCreatedRespondentsEmailGenerator(templates, lips, service());
        var email = respondent.buildEmailDTO(context, GaApplicationCreatedNotifier.TASK_ID);
        assertThat(respondent.buildEmailDTOs(context, GaApplicationCreatedNotifier.TASK_ID)).containsExactly(email);
        assertThat(email.getTargetEmail()).isEqualTo("old@example.com");
    }

    private GeneralApplicationCaseData data(String route, String urgency, String payment, int party, boolean pending) {
        var data = new GeneralApplicationCaseData().ccdCaseReference(2000L)
            .ccdState(pending ? CaseState.PENDING_APPLICATION_ISSUED : CaseState.APPLICATION_SUBMITTED_AWAITING_JUDICIAL_DECISION)
            .generalAppParentCaseLink(new GeneralAppParentCaseLink("1000"))
            .businessProcess(new BusinessProcess().setCamundaEvent("INITIATE_GENERAL_APPLICATION")
                .setActivityId(GaApplicationCreatedNotifier.TASK_ID))
            .generalAppRespondentSolicitors(List.of(element(new GASolicitorDetailsGAspec().setEmail("old@example.com"))))
            .isMultiParty(NO).isGaRespondentOneLip((party & 1) == 0 ? NO : YES)
            .respondentBilingualLanguagePreference((party & 2) == 0 ? NO : YES)
            .claimant1PartyName("Claimant").defendant1PartyName("Defendant")
            .parentClaimantIsApplicant((party & 2) == 0 ? NO : YES)
            .generalAppNotificationDeadlineDate(LocalDateTime.of(2026, 11, 10, 12, 0))
            .generalAppInformOtherParty(new GAInformOtherParty().setIsWithNotice("WITH_NOTICE".equals(route) ? YES : NO));
        if (!"MISSING".equals(urgency)) {
            data.generalAppUrgencyRequirement(new GAUrgencyRequirement().setGeneralAppUrgency("NORMAL".equals(urgency) ? NO : YES));
        }
        if (!"NO_PBA".equals(payment)) {
            data.generalAppPBADetails(new GeneralApplicationPbaDetails().setFee(new Fee().setCode("FREE".equals(payment) ? "FREE" : "FEE"))
                .setPaymentDetails(new PaymentDetails().setStatus("PAID".equals(payment) ? PaymentStatus.SUCCESS : PaymentStatus.FAILED)));
        }
        if ("UNCLOAKED".equals(route)) {
            data.applicationIsUncloakedOnce(YES);
        }
        if ("NOT_CLOAKED".equals(route)) {
            data.applicationIsCloaked(NO);
        }
        return data.build();
    }

    private GeneralApplicationCreationNotificationService service() {
        return new GeneralApplicationCreationNotificationService(mapper, legacyMail, templates, converter, gaCcd,
            lips, validator, templates, toggles, signatures);
    }

    private CallbackParams params(GeneralApplicationCaseData data, String event) {
        return new CallbackParams().type(ABOUT_TO_SUBMIT).caseData(data)
            .request(CallbackRequest.builder().eventId(event).build());
    }

    private void capture(NotificationService service, List<EmailDTO> emails) {
        doAnswer(invocation -> {
            emails.add(new EmailDTO(invocation.getArgument(0), invocation.getArgument(1),
                new HashMap<>(invocation.<Map<String, String>>getArgument(2)), invocation.getArgument(3)));
            return null;
        }).when(service).sendMail(any(), any(), any(), any());
    }
}
