package uk.gov.hmcts.reform.civil.ga.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import uk.gov.hmcts.reform.civil.enums.CaseState;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDataContent;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.ga.handler.callback.camunda.notification.DocUploadNotifyHandler;
import uk.gov.hmcts.reform.civil.testutils.ObjectMapperFactory;
import org.mockito.ArgumentCaptor;
import uk.gov.hmcts.reform.civil.ga.enums.dq.GAJudgeDecisionOption;
import uk.gov.hmcts.reform.civil.ga.enums.dq.GAJudgeWrittenRepresentationsOptions;
import uk.gov.hmcts.reform.civil.ga.model.GeneralApplicationCaseData;
import uk.gov.hmcts.reform.civil.ga.model.genapplication.GAJudicialDecision;
import uk.gov.hmcts.reform.civil.ga.model.genapplication.GAJudicialRequestMoreInfo;
import uk.gov.hmcts.reform.civil.ga.model.genapplication.GAJudicialWrittenRepresentations;
import uk.gov.hmcts.reform.civil.ga.model.genapplication.GeneralApplicationPbaDetails;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.model.BusinessProcess;
import uk.gov.hmcts.reform.civil.model.GeneralAppParentCaseLink;
import uk.gov.hmcts.reform.civil.model.genapplication.GAInformOtherParty;
import uk.gov.hmcts.reform.civil.model.genapplication.GASolicitorDetailsGAspec;
import uk.gov.hmcts.reform.civil.notification.handlers.EmailDTO;
import uk.gov.hmcts.reform.civil.notification.handlers.NotificationSummaryParser;
import uk.gov.hmcts.reform.civil.notify.NotificationException;
import uk.gov.hmcts.reform.civil.notify.NotificationService;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.notify.NotificationsSignatureConfiguration;
import uk.gov.hmcts.reform.civil.sampledata.GeneralApplicationCaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.CoreCaseDataService;
import uk.gov.hmcts.reform.civil.service.DeadlinesCalculator;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.OrganisationService;

import java.time.LocalDateTime;
import java.time.LocalDate;
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
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.SUBMITTED;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.GA_EVIDENCE_UPLOAD_CHECK;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.RECORD_NOTIFICATIONS;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;
import static uk.gov.hmcts.reform.civil.ga.enums.dq.GAJudgeRequestMoreInfoOption.SEND_APP_TO_OTHER_PARTY;
import static uk.gov.hmcts.reform.civil.utils.ElementUtils.element;

class GaDirectNotificationSummaryTest {

    private final NotificationService legacyMail = mock(NotificationService.class);
    private final NotificationService recordedMail = mock(NotificationService.class);
    private final NotificationsProperties templates = mock(NotificationsProperties.class, invocation ->
        invocation.getMethod().getReturnType() == String.class ? invocation.getMethod().getName() : RETURNS_DEFAULTS.answer(invocation));
    private final NotificationsSignatureConfiguration signatures = mock(NotificationsSignatureConfiguration.class, invocation ->
        invocation.getMethod().getReturnType() == String.class ? invocation.getMethod().getName() : RETURNS_DEFAULTS.answer(invocation));
    private final GaForLipService lips = new GaForLipService();
    private final CoreCaseDataService ccd = mock(CoreCaseDataService.class);
    private final CaseDetailsConverter converter = mock(CaseDetailsConverter.class);
    private final FeatureToggleService toggles = mock(FeatureToggleService.class);
    private final OrganisationService organisations = mock(OrganisationService.class);
    private final DeadlinesCalculator deadlines = mock(DeadlinesCalculator.class);
    private final SolicitorEmailValidation validator = mock(SolicitorEmailValidation.class);

    static Stream<Arguments> journeys() {
        return Stream.of(false, true).flatMap(fee -> Stream.of(0, 1, 2, 3).flatMap(mode ->
            Stream.of(false, true).flatMap(welsh -> Stream.of(false, true).map(notice -> Arguments.of(fee, mode, welsh, notice)))));
    }

    @ParameterizedTest
    @MethodSource("journeys")
    void shouldPreserveLegacyEmailsAndSummariseRecipients(boolean fee, int mode, boolean welsh, boolean notice) {
        configure();
        var data = data(mode, welsh, notice);
        var legacyEmails = new ArrayList<EmailDTO>();
        var recordedEmails = new ArrayList<EmailDTO>();
        capture(legacyMail, legacyEmails);
        capture(recordedMail, recordedEmails);
        if (fee) {
            judicial(legacyMail).sendNotification(data, "respondent");
        } else {
            evidence(legacyMail).notifyApplicantEvidenceUpload(data);
            if (notice) {
                evidence(legacyMail).notifyRespondentEvidenceUpload(data);
            }
        }
        String summary = fee ? judicial(recordedMail).sendNotificationAndGetSummary(data, "respondent")
            : evidence(recordedMail).notifyEvidenceUploadAndGetSummary(data);
        int expectedRecipients = fee ? 2 : 1;
        if (!fee && notice) {
            expectedRecipients += 2;
        }
        assertThat(recordedEmails).containsExactlyElementsOf(legacyEmails).hasSize(expectedRecipients);
        assertThat(NotificationSummaryParser.errors(summary)).isNull();
        for (var email : recordedEmails) {
            assertThat(NotificationSummaryParser.attempted(summary))
                .contains(email.getTargetEmail() + " : " + email.getReference() + " : " + email.getEmailTemplate());
        }
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void shouldRecordSendFailuresAndContinueWithOtherRecipients(boolean fee) {
        configure();
        var data = data(0, false, true);
        var emails = new ArrayList<EmailDTO>();
        capture(recordedMail, emails);
        doThrow(new NotificationException(new RuntimeException("Notify unavailable"))).when(recordedMail)
            .sendMail(eq("respondent1@example.com"), any(), any(), any());
        String summary = fee ? judicial(recordedMail).sendNotificationAndGetSummary(data, "respondent")
            : evidence(recordedMail).notifyEvidenceUploadAndGetSummary(data);
        assertThat(NotificationSummaryParser.attempted(summary)).contains("respondent1@example.com", "respondent2@example.com");
        assertThat(NotificationSummaryParser.errors(summary)).contains("respondent1@example.com", "Notify unavailable");
        assertThat(emails).extracting(EmailDTO::getTargetEmail).contains("respondent2@example.com");
    }

    @Test
    void shouldRecordPreparationFailuresAndStillNotifyRespondents() {
        configure();
        var data = data(0, false, true);
        when(organisations.findOrganisationById("applicant-org")).thenThrow(new RuntimeException("Organisation lookup failed"));
        String summary = evidence(recordedMail).notifyEvidenceUploadAndGetSummary(data);
        assertThat(NotificationSummaryParser.errors(summary)).contains("applicant", "Organisation lookup failed");
        assertThat(NotificationSummaryParser.attempted(summary)).contains("respondent1@example.com", "respondent2@example.com");
    }

    @Test
    void shouldRecordRespondentPreparationFailureAfterNotifyingApplicant() {
        configure();
        var parent = new GeneralApplicationCaseData().ccdState(CaseState.CASE_PROGRESSION)
            .legacyCaseReference("000MC001").build();
        when(converter.toGeneralApplicationCaseData(any())).thenReturn(parent)
            .thenThrow(new RuntimeException("Parent case unavailable for respondents"));
        var emails = new ArrayList<EmailDTO>();
        capture(recordedMail, emails);

        String summary = evidence(recordedMail).notifyEvidenceUploadAndGetSummary(data(0, false, true));

        assertThat(emails).extracting(EmailDTO::getTargetEmail).containsExactly("applicant@example.com");
        assertThat(NotificationSummaryParser.attempted(summary)).startsWith("applicant@example.com :").doesNotContain("respondent");
        assertThat(NotificationSummaryParser.errors(summary)).contains("respondent", "Parent case unavailable for respondents");
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   "})
    void shouldSkipMissingJudicialRecipientAndStillNotifyTheOtherRespondent(String email) {
        configure();
        var data = data(0, false, true);
        data.getGeneralAppRespondentSolicitors().getFirst().getValue().setEmail(email);
        var emails = new ArrayList<EmailDTO>();
        capture(recordedMail, emails);

        String summary = judicial(recordedMail).sendNotificationAndGetSummary(data, "respondent");

        assertThat(emails).extracting(EmailDTO::getTargetEmail).containsExactly("respondent2@example.com");
        assertThat(NotificationSummaryParser.attempted(summary)).startsWith("respondent2@example.com :").doesNotContain(" | ");
        assertThat(NotificationSummaryParser.errors(summary)).isNull();
    }

    @ParameterizedTest
    @ValueSource(strings = {"CONCURRENT", "SEQUENTIAL", "HEARING"})
    void shouldPreserveRepresentedRespondentEmailsForOtherJudicialDecisions(String decision) {
        configure();
        var data = data(0, false, true).copy().ccdState(CaseState.ORDER_MADE).build();
        String expectedTemplate;
        if ("HEARING".equals(decision)) {
            data.setJudicialDecision(new GAJudicialDecision().setDecision(GAJudgeDecisionOption.LIST_FOR_A_HEARING));
            expectedTemplate = "getJudgeListsForHearingRespondentEmailTemplate";
        } else {
            boolean concurrent = "CONCURRENT".equals(decision);
            data.setJudicialDecision(new GAJudicialDecision().setDecision(GAJudgeDecisionOption.MAKE_AN_ORDER));
            data.setJudicialDecisionMakeAnOrderForWrittenRepresentations(new GAJudicialWrittenRepresentations()
                .setWrittenOption(concurrent ? GAJudgeWrittenRepresentationsOptions.CONCURRENT_REPRESENTATIONS
                    : GAJudgeWrittenRepresentationsOptions.SEQUENTIAL_REPRESENTATIONS)
                .setWrittenConcurrentRepresentationsBy(LocalDate.of(2026, 11, 20))
                .setSequentialApplicantMustRespondWithin(LocalDate.of(2026, 11, 21)));
            expectedTemplate = concurrent ? "getWrittenRepConcurrentRepresentationRespondentEmailTemplate"
                : "getWrittenRepSequentialRepresentationRespondentEmailTemplate";
        }
        var legacyEmails = new ArrayList<EmailDTO>();
        var recordedEmails = new ArrayList<EmailDTO>();
        capture(legacyMail, legacyEmails);
        capture(recordedMail, recordedEmails);

        judicial(legacyMail).sendNotification(data, "respondent");
        String summary = judicial(recordedMail).sendNotificationAndGetSummary(data, "respondent");

        assertThat(recordedEmails).containsExactlyElementsOf(legacyEmails).hasSize(2)
            .allSatisfy(email -> assertThat(email.getEmailTemplate()).isEqualTo(expectedTemplate));
        assertThat(NotificationSummaryParser.errors(summary)).isNull();
        assertThat(NotificationSummaryParser.attempted(summary)).contains("respondent1@example.com", "respondent2@example.com");
    }

    @Test
    void shouldRecordJudicialPreparationFailures() {
        configure();
        when(converter.toGeneralApplicationCaseData(any())).thenThrow(new RuntimeException("Parent case unavailable"));
        String summary = judicial(recordedMail).sendNotificationAndGetSummary(data(0, false, true), "respondent");
        assertThat(NotificationSummaryParser.errors(summary)).contains("respondent", "Parent case unavailable");
    }

    @Test
    void shouldHandleEvidenceWithoutABusinessProcessAndIgnoreMissingAddresses() {
        configure();
        var data = data(0, false, true).copy().businessProcess(null).build();
        data.getGeneralAppApplnSolicitor().setEmail("   ");
        data.getGeneralAppRespondentSolicitors().getFirst().getValue().setEmail(null);
        var emails = new ArrayList<EmailDTO>();
        capture(recordedMail, emails);
        String summary = evidence(recordedMail).notifyEvidenceUploadAndGetSummary(data);
        assertThat(emails).extracting(EmailDTO::getTargetEmail).containsExactly("respondent2@example.com");
        assertThat(NotificationSummaryParser.attempted(summary)).startsWith("respondent2@example.com :").doesNotContain("null", " | ");
        assertThat(NotificationSummaryParser.errors(summary)).isNull();
    }

    @Test
    void shouldRecordAnActualEvidenceSendFailureAfterTheEvidenceEventIsSubmitted() {
        configure();
        var mapper = ObjectMapperFactory.instance();
        var data = data(0, false, true).copy().businessProcess(null).build();
        var gaCcd = mock(GaCoreCaseDataService.class);
        when(gaCcd.startGaUpdate("2000", RECORD_NOTIFICATIONS)).thenReturn(StartEventResponse.builder()
            .eventId(RECORD_NOTIFICATIONS.name()).caseDetails(CaseDetails.builder().data(new HashMap<>()).build()).build());
        doThrow(new NotificationException(new RuntimeException("Notify unavailable"))).when(recordedMail)
            .sendMail(eq("respondent1@example.com"), any(), any(), any());
        var handler = new DocUploadNotifyHandler(mapper, evidence(recordedMail), new GaNotificationHistoryService(gaCcd));
        var request = CallbackRequest.builder().eventId(GA_EVIDENCE_UPLOAD_CHECK.name()).build();
        var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(
            new CallbackParams().caseData(data).type(ABOUT_TO_SUBMIT).request(request));
        var saved = mapper.convertValue(response.getData(), GeneralApplicationCaseData.class)
            .ccdCaseReference(data.getCcdCaseReference()).build();
        handler.handle(new CallbackParams().caseData(saved).type(SUBMITTED).request(request));
        var content = ArgumentCaptor.forClass(CaseDataContent.class);
        verify(gaCcd).submitGaUpdate(eq("2000"), content.capture());
        assertThat(content.getValue().getEvent().getSummary())
            .contains("applicant@example.com", "respondent1@example.com", "respondent2@example.com")
            .doesNotContain("Attempted:", "|| Errors:");
        assertThat(content.getValue().getEvent().getDescription()).contains("respondent1@example.com", "Notify unavailable");
    }

    private void configure() {
        when(converter.toGeneralApplicationCaseData(any())).thenReturn(new GeneralApplicationCaseData()
            .ccdState(CaseState.CASE_PROGRESSION).legacyCaseReference("000MC001").build());
        when(validator.validateSolicitorEmail(any(), any())).thenAnswer(invocation ->
            ((GeneralApplicationCaseData) invocation.getArgument(1)).copy().build());
        when(deadlines.calculateApplicantResponseDeadline(any(), eq(5))).thenReturn(LocalDateTime.of(2026, 11, 20, 12, 0));
    }

    private GeneralApplicationCaseData data(int mode, boolean welsh, boolean notice) {
        return GeneralApplicationCaseDataBuilder.builder().judicialOrderMadeWithUncloakApplication(NO).build().copy()
            .ccdCaseReference(2000L).ccdState(CaseState.APPLICATION_ADD_PAYMENT)
            .generalAppParentCaseLink(new GeneralAppParentCaseLink("1000"))
            .generalAppApplnSolicitor(new GASolicitorDetailsGAspec().setEmail("applicant@example.com").setOrganisationIdentifier("applicant-org"))
            .generalAppRespondentSolicitors(List.of(element(new GASolicitorDetailsGAspec().setEmail("respondent1@example.com")),
                element(new GASolicitorDetailsGAspec().setEmail("respondent2@example.com"))))
            .isMultiParty(NO).isGaApplicantLip((mode & 1) == 0 ? NO : YES).isGaRespondentOneLip((mode & 2) == 0 ? NO : YES)
            .applicantBilingualLanguagePreference(welsh ? YES : NO).respondentBilingualLanguagePreference(welsh ? YES : NO)
            .applicantPartyName("Applicant").claimant1PartyName("Claimant").defendant1PartyName("Defendant")
            .parentClaimantIsApplicant(YES).applicationIsCloaked(YES).applicationIsUncloakedOnce(NO)
            .generalAppInformOtherParty(new GAInformOtherParty().setIsWithNotice(notice ? YES : NO))
            .judicialDecision(new GAJudicialDecision().setDecision(GAJudgeDecisionOption.REQUEST_MORE_INFO))
            .judicialDecisionRequestMoreInfo(new GAJudicialRequestMoreInfo().setRequestMoreInfoOption(SEND_APP_TO_OTHER_PARTY))
            .generalAppPBADetails(new GeneralApplicationPbaDetails())
            .businessProcess(new BusinessProcess().setCamundaEvent("MAKE_DECISION").setActivityId("legacy-respondent-task"))
            .build();
    }

    private DocUploadNotificationService evidence(NotificationService mail) {
        return new DocUploadNotificationService(mail, templates, lips, ccd, converter, toggles, signatures, organisations);
    }

    private JudicialNotificationService judicial(NotificationService mail) {
        return new JudicialNotificationService(templates, mail, deadlines, converter, ccd, lips, validator, toggles, signatures);
    }

    private void capture(NotificationService service, List<EmailDTO> emails) {
        doAnswer(invocation -> {
            emails.add(new EmailDTO(invocation.getArgument(0), invocation.getArgument(1),
                new HashMap<>(invocation.<Map<String, String>>getArgument(2)), invocation.getArgument(3)));
            return null;
        }).when(service).sendMail(any(), any(), any(), any());
    }
}
