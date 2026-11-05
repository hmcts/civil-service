package uk.gov.hmcts.reform.civil.notification.handlers.ga.gatranslateddocumentuploaded;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.model.OrganisationPolicy;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.enums.CaseState;
import uk.gov.hmcts.reform.civil.ga.handler.callback.camunda.notification.GaNotificationHandler;
import uk.gov.hmcts.reform.civil.ga.handler.callback.camunda.notification.TranslatedDocumentUploadedApplicantNotificationHandler;
import uk.gov.hmcts.reform.civil.ga.handler.callback.camunda.notification.TranslatedDocumentUploadedRespondentNotificationHandler;
import uk.gov.hmcts.reform.civil.ga.model.GeneralApplicationCaseData;
import uk.gov.hmcts.reform.civil.ga.service.GaCoreCaseDataService;
import uk.gov.hmcts.reform.civil.ga.service.GaForLipService;
import uk.gov.hmcts.reform.civil.ga.service.SolicitorEmailValidation;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.model.BusinessProcess;
import uk.gov.hmcts.reform.civil.model.GeneralAppParentCaseLink;
import uk.gov.hmcts.reform.civil.model.IdamUserDetails;
import uk.gov.hmcts.reform.civil.model.genapplication.GAInformOtherParty;
import uk.gov.hmcts.reform.civil.model.genapplication.GASolicitorDetailsGAspec;
import uk.gov.hmcts.reform.civil.notification.handlers.EmailDTO;
import uk.gov.hmcts.reform.civil.notification.handlers.GaNotifierFactory;
import uk.gov.hmcts.reform.civil.notify.NotificationService;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.notify.NotificationsSignatureConfiguration;
import uk.gov.hmcts.reform.civil.prd.model.Organisation;
import uk.gov.hmcts.reform.civil.service.CaseTaskTrackingService;
import uk.gov.hmcts.reform.civil.service.CoreCaseDataService;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.OrganisationService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.RETURNS_DEFAULTS;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.NOTIFY_APPLICANT_TRANSLATED_DOCUMENT_UPLOADED_GA;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.NOTIFY_EVENT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.NOTIFY_RESPONDENT_TRANSLATED_DOCUMENT_UPLOADED_GA;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;
import static uk.gov.hmcts.reform.civil.utils.ElementUtils.element;

@SuppressWarnings("removal")
class GaTranslatedDocumentUploadedNotifierTest {

    private final NotificationService legacyService = mock(NotificationService.class);
    private final NotificationService notificationService = mock(NotificationService.class);
    private final CoreCaseDataService ccd = mock(CoreCaseDataService.class);
    private final CaseDetailsConverter converter = mock(CaseDetailsConverter.class);
    private final OrganisationService organisations = mock(OrganisationService.class);
    private final FeatureToggleService toggles = mock(FeatureToggleService.class);
    private final GaForLipService lips = new GaForLipService();
    private final SolicitorEmailValidation validator = mock(SolicitorEmailValidation.class);
    private final NotificationsProperties templates = mock(NotificationsProperties.class, invocation ->
        invocation.getMethod().getReturnType() == String.class ? invocation.getMethod().getName()
            : RETURNS_DEFAULTS.answer(invocation));
    private final NotificationsSignatureConfiguration signatures = mock(NotificationsSignatureConfiguration.class, invocation ->
        invocation.getMethod().getReturnType() == String.class ? invocation.getMethod().getName()
            : RETURNS_DEFAULTS.answer(invocation));
    private final List<EmailDTO> legacyEmails = new ArrayList<>();
    private final List<EmailDTO> actualEmails = new ArrayList<>();

    static Stream<Arguments> parties() {
        return Stream.of("WITH_NOTICE", "WITHOUT_NOTICE", "CONSENT", "UNCLOAKED", "NOT_CLOAKED")
            .flatMap(route -> Stream.of(0, 1, 2, 3).flatMap(representation -> Stream.of(0, 1, 2, 3)
                .flatMap(language -> Stream.of(false, true).map(queryManagement ->
                    Arguments.of(route, representation, language, queryManagement)))));
    }

    @ParameterizedTest
    @MethodSource("parties")
    void shouldPreserveLegacyEmails(String route, int representation, int language, boolean queryManagement) {
        configure(queryManagement);
        var data = caseData(route, representation, language).parentClaimantIsApplicant(queryManagement ? YES : NO).build();
        capture(legacyService, legacyEmails);
        capture(notificationService, actualEmails);
        legacyApplicant().handle(params(data, NOTIFY_APPLICANT_TRANSLATED_DOCUMENT_UPLOADED_GA.name()));
        legacyRespondent().handle(params(data, NOTIFY_RESPONDENT_TRANSLATED_DOCUMENT_UPLOADED_GA.name()));
        var notifier = notifier(validator);
        var factory = new GaNotifierFactory(notifier);
        var handler = new GaNotificationHandler(factory, mock(GaCoreCaseDataService.class), mapper());
        var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params(data, NOTIFY_EVENT.name()));

        assertThat(factory.getNotifier(GaTranslatedDocumentUploadedNotifier.TASK_ID)).isSameAs(notifier);
        assertThat(actualEmails).containsExactlyInAnyOrderElementsOf(legacyEmails);
        assertThat(actualEmails).hasSize("WITHOUT_NOTICE".equals(route) ? 1 : 3);
        assertThat(actualEmails).allSatisfy(email -> assertThat(email.getReference())
            .isEqualTo("translated-document-uploaded-applicant-notification-2000"));
        var saved = mapper().convertValue(response.getData(), GeneralApplicationCaseData.class);
        assertThat(saved.getNotificationSummary()).contains("applicant@example.com");
        assertThat(saved.getBusinessProcess()).isEqualTo(data.getBusinessProcess());
        verify(ccd, times(3)).getCase(1000L);
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void shouldNotNotifyMissingRespondents(boolean nullList) {
        configure(false);
        var data = caseData("WITH_NOTICE", 0, 0).generalAppRespondentSolicitors(nullList ? null : List.of()).build();
        capture(legacyService, legacyEmails);
        capture(notificationService, actualEmails);
        legacyApplicant().handle(params(data, NOTIFY_APPLICANT_TRANSLATED_DOCUMENT_UPLOADED_GA.name()));
        legacyRespondent().handle(params(data, NOTIFY_RESPONDENT_TRANSLATED_DOCUMENT_UPLOADED_GA.name()));
        notifier(validator).notifyParties(data, "NOTIFY_EVENT", GaTranslatedDocumentUploadedNotifier.TASK_ID);
        assertThat(actualEmails).containsExactlyElementsOf(legacyEmails).hasSize(1);
    }

    @Test
    void shouldAvoidDuplicateEmailsForTheSameRespondentSolicitor() {
        configure(false);
        var data = caseData("WITH_NOTICE", 0, 0);
        data.getGeneralAppRespondentSolicitors().get(1).setValue(data.getGeneralAppRespondentSolicitors().getFirst().getValue());
        capture(notificationService, actualEmails);
        notifier(validator).notifyParties(data, "NOTIFY_EVENT", GaTranslatedDocumentUploadedNotifier.TASK_ID);
        assertThat(actualEmails).hasSize(2);
    }

    @Test
    void shouldPreserveFailureWhenTheApplicantOrganisationIsMissing() {
        configure(false);
        when(organisations.findOrganisationById("applicant-org")).thenReturn(Optional.empty());
        var data = caseData("WITH_NOTICE", 0, 0);
        assertThatThrownBy(() -> legacyApplicant().handle(params(data, NOTIFY_APPLICANT_TRANSLATED_DOCUMENT_UPLOADED_GA.name())))
            .isInstanceOf(RuntimeException.class);
        assertThatThrownBy(() -> notifier(validator).notifyParties(data, "NOTIFY_EVENT", GaTranslatedDocumentUploadedNotifier.TASK_ID))
            .isInstanceOf(RuntimeException.class);
    }

    @Test
    void shouldUseRefreshedAddressesWithoutPersistingThemAsTheLegacyCallbacksDid() {
        configure(false);
        var parent = new GeneralApplicationCaseData().ccdCaseReference(1000L).ccdState(CaseState.CASE_PROGRESSION)
            .applicant1OrganisationPolicy(new OrganisationPolicy().setOrganisation(
                new uk.gov.hmcts.reform.ccd.model.Organisation().setOrganisationID("applicant-org")))
            .applicantSolicitor1UserDetails(new IdamUserDetails().setEmail("refreshed@example.com")).build();
        when(converter.toGeneralApplicationCaseData(any())).thenReturn(parent);
        var data = caseData("WITH_NOTICE", 0, 0);
        var realValidator = new SolicitorEmailValidation(lips);
        var handler = new GaNotificationHandler(new GaNotifierFactory(notifier(realValidator)),
            mock(GaCoreCaseDataService.class), mapper());
        capture(notificationService, actualEmails);
        var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params(data, "NOTIFY_EVENT"));
        var saved = mapper().convertValue(response.getData(), GeneralApplicationCaseData.class);
        assertThat(actualEmails).extracting(EmailDTO::getTargetEmail).contains("refreshed@example.com")
            .doesNotContain("applicant@example.com");
        assertThat(saved.getGeneralAppApplnSolicitor().getEmail()).isEqualTo("applicant@example.com");
    }

    private GaTranslatedDocumentUploadedNotifier notifier(SolicitorEmailValidation emailValidator) {
        var applicant = new GaTranslatedDocumentUploadedApplicantEmailGenerator(templates, lips, organisations, toggles, signatures);
        var respondents = new GaTranslatedDocumentUploadedRespondentsEmailGenerator(templates, lips, organisations, toggles, signatures);
        var parties = new GaTranslatedDocumentUploadedAllPartiesEmailGenerator(applicant, respondents, converter, ccd, emailValidator);
        return new GaTranslatedDocumentUploadedNotifier(notificationService, mock(CaseTaskTrackingService.class), parties);
    }

    private TranslatedDocumentUploadedApplicantNotificationHandler legacyApplicant() {
        return new TranslatedDocumentUploadedApplicantNotificationHandler(legacyService, templates, converter, lips, ccd,
            validator, organisations, toggles, signatures);
    }

    private TranslatedDocumentUploadedRespondentNotificationHandler legacyRespondent() {
        return new TranslatedDocumentUploadedRespondentNotificationHandler(legacyService, templates, converter, lips, ccd,
            validator, organisations, toggles, signatures);
    }

    private void configure(boolean queryManagement) {
        when(ccd.getCase(1000L)).thenReturn(CaseDetails.builder().build());
        when(converter.toGeneralApplicationCaseData(any())).thenReturn(new GeneralApplicationCaseData()
            .ccdCaseReference(1000L).ccdState(CaseState.CASE_PROGRESSION).build());
        when(validator.validateSolicitorEmail(any(), any())).thenAnswer(invocation ->
            ((GeneralApplicationCaseData) invocation.getArgument(1)).copy().build());
        when(organisations.findOrganisationById(any())).thenAnswer(invocation ->
            Optional.of(new Organisation().setName("Organisation " + invocation.getArgument(0))));
        when(toggles.isPublicQueryManagementEnabledGa(any())).thenAnswer(invocation ->
            ((GeneralApplicationCaseData) invocation.getArgument(0)).getCcdCaseReference() == 1000L
                ? !queryManagement : queryManagement);
    }

    private GeneralApplicationCaseData caseData(String route, int representation, int language) {
        var data = new GeneralApplicationCaseData().ccdCaseReference(2000L).parentCaseReference("1000")
            .generalAppParentCaseLink(new GeneralAppParentCaseLink("1000"))
            .businessProcess(new BusinessProcess().setCamundaEvent("UPLOAD_TRANSLATED_DOCUMENT_GA_LIP")
                .setActivityId(GaTranslatedDocumentUploadedNotifier.TASK_ID))
            .generalAppApplnSolicitor(new GASolicitorDetailsGAspec().setEmail("applicant@example.com")
                .setOrganisationIdentifier("applicant-org"))
            .generalAppRespondentSolicitors(List.of(element(new GASolicitorDetailsGAspec().setEmail("respondent1@example.com")
                .setOrganisationIdentifier("respondent1-org")), element(new GASolicitorDetailsGAspec().setEmail("respondent2@example.com")
                .setOrganisationIdentifier("respondent2-org"))))
            .isMultiParty(YES).isGaApplicantLip((representation & 1) != 0 ? YES : NO)
            .isGaRespondentOneLip((representation & 2) != 0 ? YES : NO).isGaRespondentTwoLip(NO)
            .applicant1Represented((representation & 1) != 0 ? NO : YES)
            .respondent1Represented((representation & 2) != 0 ? NO : YES)
            .applicantBilingualLanguagePreference((language & 1) != 0 ? YES : NO)
            .respondentBilingualLanguagePreference((language & 2) != 0 ? YES : NO)
            .claimant1PartyName("Claimant").defendant1PartyName("Defendant One").defendant2PartyName("Defendant Two")
            .applicantPartyName("Applicant").parentClaimantIsApplicant(YES)
            .generalAppInformOtherParty(new GAInformOtherParty().setIsWithNotice("WITH_NOTICE".equals(route) ? YES : NO));
        switch (route) {
            case "CONSENT" -> data.generalAppConsentOrder(YES);
            case "UNCLOAKED" -> data.applicationIsUncloakedOnce(YES);
            case "NOT_CLOAKED" -> data.applicationIsCloaked(NO);
            default -> { /* The application notice setting determines the remaining routes. */ }
        }
        return data.build();
    }

    private CallbackParams params(GeneralApplicationCaseData data, String event) {
        return new CallbackParams().type(ABOUT_TO_SUBMIT).caseData(data)
            .request(CallbackRequest.builder().eventId(event).build());
    }

    private ObjectMapper mapper() {
        return new ObjectMapper().registerModule(new JavaTimeModule());
    }

    private void capture(NotificationService service, List<EmailDTO> emails) {
        doAnswer(invocation -> {
            emails.add(new EmailDTO(invocation.getArgument(0), invocation.getArgument(1),
                new HashMap<>(invocation.<Map<String, String>>getArgument(2)), invocation.getArgument(3)));
            return null;
        }).when(service).sendMail(any(), any(), any(), any());
    }
}
