package uk.gov.hmcts.reform.civil.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.annotation.DirtiesContext;
import uk.gov.hmcts.reform.civil.enums.CaseState;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.enums.dq.GAJudgeDecisionOption;
import uk.gov.hmcts.reform.civil.enums.dq.GAJudgeMakeAnOrderOption;
import uk.gov.hmcts.reform.civil.enums.dq.GAJudgeRequestMoreInfoOption;
import uk.gov.hmcts.reform.civil.enums.dq.GAJudgeWrittenRepresentationsOptions;
import uk.gov.hmcts.reform.civil.enums.dq.GeneralApplicationTypes;
import uk.gov.hmcts.reform.civil.enums.dq.Language;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationDataGA;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.model.BusinessProcess;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.GeneralAppParentCaseLink;
import uk.gov.hmcts.reform.civil.model.PaymentDetails;
import uk.gov.hmcts.reform.civil.model.citizenui.RespondentLiPResponse;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.model.genapplication.GAApplicationType;
import uk.gov.hmcts.reform.civil.model.genapplication.GAInformOtherParty;
import uk.gov.hmcts.reform.civil.model.genapplication.GAJudicialDecision;
import uk.gov.hmcts.reform.civil.model.genapplication.GAJudicialMakeAnOrder;
import uk.gov.hmcts.reform.civil.model.genapplication.GAJudicialRequestMoreInfo;
import uk.gov.hmcts.reform.civil.model.genapplication.GAJudicialWrittenRepresentations;
import uk.gov.hmcts.reform.civil.model.genapplication.GAPbaDetails;
import uk.gov.hmcts.reform.civil.model.genapplication.GARespondentOrderAgreement;
import uk.gov.hmcts.reform.civil.model.genapplication.GASolicitorDetailsGAspec;
import uk.gov.hmcts.reform.civil.notify.NotificationService;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.notify.NotificationsSignatureConfiguration;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.enums.CaseState.APPLICATION_ADD_PAYMENT;
import static uk.gov.hmcts.reform.civil.enums.CaseState.APPLICATION_SUBMITTED_AWAITING_JUDICIAL_DECISION;
import static uk.gov.hmcts.reform.civil.enums.PaymentStatus.SUCCESS;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;
import static uk.gov.hmcts.reform.civil.enums.dq.GAJudgeMakeAnOrderOption.GIVE_DIRECTIONS_WITHOUT_HEARING;
import static uk.gov.hmcts.reform.civil.enums.dq.GAJudgeRequestMoreInfoOption.REQUEST_MORE_INFORMATION;
import static uk.gov.hmcts.reform.civil.enums.dq.GAJudgeRequestMoreInfoOption.SEND_APP_TO_OTHER_PARTY;
import static uk.gov.hmcts.reform.civil.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.civil.utils.EmailFooterUtils.RAISE_QUERY_LR;

@SpringBootTest(classes = {
    JudicialNotificationService.class,
    JacksonAutoConfiguration.class,
    GaForLipService.class
})
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class JudicialDecisionRespondentNotificationHandlerTest {

    @MockBean
    private NotificationsProperties notificationsProperties;
    @MockBean
    private Time time;
    @MockBean
    private DeadlinesCalculator deadlinesCalculator;
    @Autowired
    private JudicialNotificationService judicialRespondentNotificationService;

    @MockBean
    private NotificationService notificationService;
    @MockBean
    private JudicialDecisionHelper judicialDecisionHelper;

    @MockBean
    private SolicitorEmailValidation solicitorEmailValidation;

    @MockBean
    private CaseDetailsConverter caseDetailsConverter;

    @MockBean
    private CoreCaseDataService coreCaseDataService;
    @MockBean
    private FeatureToggleService featureToggleService;
    @MockBean
    private NotificationsSignatureConfiguration configuration;
    @MockBean
    private GaForLipService gaForLipService;

    private static final String RESPONDENT = "respondent";

    private static final Long CASE_REFERENCE = 111111L;
    private static final String DUMMY_EMAIL = "hmcts.civil@gmail.com";
    private static final String DUMMY_DATE = "2022-11-12";
    private static final String CASE_EVENT = "START_RESPONDENT_NOTIFICATION_PROCESS_MAKE_DECISION";
    private static final String ORG_ID = "1";
    private static final String ID = "1";
    private static final String SAMPLE_TEMPLATE = "general-application-apps-judicial-notification-template-id";
    private static final String SAMPLE_LIP_TEMPLATE = "general-application-apps-judicial-notification-template-lip-id";
    private static final String LIP_RES_WELSH_TEMPLATE = "ga-judicial-notification-respondent-welsh-template-lip-id";
    private static final String PARTY_REFERENCE = "Claimant Reference: Not provided - Defendant Reference: Not provided";
    private static final String JUDGES_DECISION = "MAKE_DECISION";
    private LocalDateTime responseDate = LocalDateTime.now();
    private LocalDateTime deadline = LocalDateTime.now().plusDays(5);
    public static BusinessProcess businessProcess = BusinessProcess.builder()
        .camundaEvent(JUDGES_DECISION)
        .activityId("StartRespondentNotificationProcessMakeDecision")
        .build();

    @BeforeEach
    void setup() {
        when(featureToggleService.isGaForLipsEnabled()).thenReturn(true);
        when(caseDetailsConverter.toCaseDataGA(any())).thenReturn(CaseData.builder().ccdState(CaseState.CASE_PROGRESSION).build());
        when(notificationsProperties.getWrittenRepConcurrentRepresentationRespondentEmailTemplate())
            .thenReturn(SAMPLE_TEMPLATE);
        when(notificationsProperties.getWrittenRepConcurrentRepresentationApplicantEmailTemplate())
            .thenReturn(SAMPLE_TEMPLATE);
        when(notificationsProperties.getWrittenRepSequentialRepresentationRespondentEmailTemplate())
            .thenReturn(SAMPLE_TEMPLATE);
        when(notificationsProperties.getWrittenRepSequentialRepresentationApplicantEmailTemplate())
            .thenReturn(SAMPLE_TEMPLATE);
        when(notificationsProperties.getJudgeDismissesOrderApplicantEmailTemplate())
            .thenReturn(SAMPLE_TEMPLATE);
        when(notificationsProperties.getJudgeListsForHearingApplicantEmailTemplate())
            .thenReturn(SAMPLE_TEMPLATE);
        when(notificationsProperties.getJudgeDismissesOrderApplicantEmailTemplate())
            .thenReturn(SAMPLE_TEMPLATE);
        when(notificationsProperties.getWithNoticeUpdateRespondentEmailTemplate())
            .thenReturn(SAMPLE_TEMPLATE);
        when(notificationsProperties.getJudgeForApproveRespondentEmailTemplate())
            .thenReturn(SAMPLE_TEMPLATE);
        when(notificationsProperties.getJudgeForApprovedCaseApplicantEmailTemplate())
            .thenReturn(SAMPLE_TEMPLATE);
        when(notificationsProperties.getJudgeListsForHearingRespondentEmailTemplate())
            .thenReturn(SAMPLE_TEMPLATE);
        when(notificationsProperties.getJudgeDismissesOrderRespondentEmailTemplate())
            .thenReturn(SAMPLE_TEMPLATE);
        when(notificationsProperties.getJudgeForDirectionOrderApplicantEmailTemplate())
            .thenReturn(SAMPLE_TEMPLATE);
        when(notificationsProperties.getJudgeForDirectionOrderRespondentEmailTemplate())
            .thenReturn(SAMPLE_TEMPLATE);
        when(notificationsProperties.getJudgeRequestForInformationApplicantEmailTemplate())
            .thenReturn(SAMPLE_TEMPLATE);
        when(notificationsProperties.getJudgeRequestForInformationRespondentEmailTemplate())
            .thenReturn(SAMPLE_TEMPLATE);
        when(notificationsProperties.getJudgeUncloakApplicationEmailTemplate())
            .thenReturn(SAMPLE_TEMPLATE);
        when(notificationsProperties.getJudgeApproveOrderToStrikeOutDamages())
            .thenReturn(SAMPLE_TEMPLATE);
        when(notificationsProperties.getJudgeApproveOrderToStrikeOutOCMC())
            .thenReturn(SAMPLE_TEMPLATE);
        when(notificationsProperties.getGeneralApplicationRespondentEmailTemplate())
            .thenReturn(SAMPLE_TEMPLATE);
        when(notificationsProperties.getLipGeneralAppRespondentEmailTemplate())
            .thenReturn(SAMPLE_LIP_TEMPLATE);
        when(notificationsProperties.getLipGeneralAppRespondentEmailTemplateInWelsh())
            .thenReturn(LIP_RES_WELSH_TEMPLATE);
        when(gaForLipService.isLipApp(any())).thenReturn(false);
        when(configuration.getHmctsSignature()).thenReturn("Online Civil Claims \n HM Courts & Tribunal Service");
        when(configuration.getPhoneContact()).thenReturn("For anything related to hearings, call 0300 123 5577 "
                                                             + "\n For all other matters, call 0300 123 7050");
        when(configuration.getOpeningHours()).thenReturn("Monday to Friday, 8.30am to 5pm");
        when(configuration.getSpecUnspecContact()).thenReturn("Email for Specified Claims: contactocmc@justice.gov.uk "
                                                                  + "\n Email for Damages Claims: damagesclaims@justice.gov.uk");
        when(configuration.getWelshContact()).thenReturn("E-bost: ymholiadaucymraeg@justice.gov.uk");
        when(configuration.getSpecContact()).thenReturn("Email: contactocmc@justice.gov.uk");
        when(configuration.getWelshHmctsSignature()).thenReturn("Hawliadau am Arian yn y Llys Sifil Ar-lein \n Gwasanaeth Llysoedd a Thribiwnlysoedd EF");
        when(configuration.getWelshPhoneContact()).thenReturn("Ffôn: 0300 303 5174");
        when(configuration.getWelshOpeningHours()).thenReturn("Dydd Llun i ddydd Iau, 9am – 5pm, dydd Gwener, 9am – 4.30pm");
    }

    @Nested
    class SendNotificationLip {

        @BeforeEach
        public void setup() {
            when(gaForLipService.isGaForLip(any())).thenReturn(true);
            when(gaForLipService.isLipApp(any())).thenReturn(true);
            when(gaForLipService.isLipResp(any())).thenReturn(true);
        }

        public Map<String, String> customProp = new HashMap<>();

        @Test
        void sendNotificationRespondentConcurrentWrittenRep() {
            when(solicitorEmailValidation.validateSolicitorEmail(any(), any()))
                .thenReturn(caseDataForConcurrentWrittenOption(NO, YES)
                                .toBuilder().businessProcess(businessProcess).build());
            when(caseDetailsConverter.toCaseDataGA(any())).thenReturn(CaseData.builder().build());

            judicialRespondentNotificationService.sendNotification(caseDataForConcurrentWrittenOption(NO, YES), RESPONDENT)
                .toBuilder().businessProcess(businessProcess).build();
            verify(notificationService, times(1)).sendMail(
                DUMMY_EMAIL,
                "general-application-apps-judicial-notification-template-lip-id",
                notificationPropertiesSummeryJudgement(),
                "general-apps-judicial-notification-make-decision-" + CASE_REFERENCE
            );
        }

        @Test
        void sendNotificationRespondentConcurrentWrittenRep_AddlnPayment() {
            when(solicitorEmailValidation.validateSolicitorEmail(any(), any()))
                .thenReturn(caseDataForConcurrentWrittenOption(NO, YES)
                                .toBuilder().ccdState(APPLICATION_ADD_PAYMENT).build());
            when(caseDetailsConverter.toCaseDataGA(any())).thenReturn(CaseData.builder().build());

            judicialRespondentNotificationService.sendNotification(caseDataForConcurrentWrittenOption(NO, YES), RESPONDENT)
                .toBuilder().businessProcess(businessProcess).build();
            verify(notificationService, times(1)).sendMail(
                DUMMY_EMAIL,
                "general-application-apps-judicial-notification-template-lip-id",
                notificationPropertiesSummeryJudgement(),
                "general-apps-judicial-notification-make-decision-" + CASE_REFERENCE
            );
        }

        @Test
        void sendNotificationInWelshToRespondentConcurrentWrittenRep_AddlnPayment() {
            when(solicitorEmailValidation.validateSolicitorEmail(any(), any()))
                .thenReturn(caseDataForConcurrentWrittenOption(NO, YES)
                                .toBuilder().respondentBilingualLanguagePreferenceGA(YES)
                                .ccdState(APPLICATION_ADD_PAYMENT).build());

            CaseData claimRespondentResponseLan = CaseData.builder().respondent1LiPResponseGA(RespondentLiPResponse.builder().respondent1ResponseLanguage(
                Language.BOTH.toString()).build()).build();
            when(caseDetailsConverter.toCaseDataGA(any())).thenReturn(claimRespondentResponseLan);
            judicialRespondentNotificationService.sendNotification(caseDataForConcurrentWrittenOption(NO, YES), RESPONDENT)
                .toBuilder().businessProcess(businessProcess).build();
            verify(notificationService, times(1)).sendMail(
                DUMMY_EMAIL,
                LIP_RES_WELSH_TEMPLATE,
                notificationPropertiesSummeryJudgement(),
                "general-apps-judicial-notification-make-decision-" + CASE_REFERENCE
            );
        }

        @Test
        void sendNotificationRespondentSequentialWrittenRep() {
            when(solicitorEmailValidation.validateSolicitorEmail(any(), any()))
                .thenReturn(caseDataForSequentialWrittenOption(NO, YES).toBuilder().businessProcess(businessProcess)
                                .build());
            when(caseDetailsConverter.toCaseDataGA(any())).thenReturn(CaseData.builder().build());

            judicialRespondentNotificationService.sendNotification(caseDataForSequentialWrittenOption(NO, YES)
                                                             .toBuilder().businessProcess(businessProcess).build(),
                                                         RESPONDENT);
            verify(notificationService, times(1)).sendMail(
                DUMMY_EMAIL,
                "general-application-apps-judicial-notification-template-lip-id",
                notificationPropertiesSummeryJudgement(),
                "general-apps-judicial-notification-make-decision-" + CASE_REFERENCE
            );
        }

        @Test
        void notificationUncloakShouldSendForDismissal_RespondentLIP() {

            when(solicitorEmailValidation.validateSolicitorEmail(any(), any()))
                .thenReturn(caseDataForJudgeDismissal(NO, NO, NO, NO, YES)
                                .toBuilder().businessProcess(businessProcess).build());
            when(caseDetailsConverter.toCaseDataGA(any())).thenReturn(CaseData.builder().build());
            judicialRespondentNotificationService.sendNotification(caseDataForJudgeDismissal(NO, NO, NO,  NO, YES)
                                                             .toBuilder()
                                                             .businessProcess(businessProcess).build(), RESPONDENT);

            verify(notificationService, times(1)).sendMail(
                DUMMY_EMAIL,
                "general-application-apps-judicial-notification-template-lip-id",
                notificationPropertiesSummeryJudgement(),
                "general-apps-judicial-notification-make-decision-" + CASE_REFERENCE
            );
        }

        @Test
        void notificationShouldSend_LipRespondent_When_JudicialDirectionOrderRep_unCloaks() {

            CaseData caseData
                = caseDataForJudicialDirectionOrderOfApplicationWhenRespondentsArePresentInList(NO,
                                                                                                NO, YES, NO, YES)
                .toBuilder().businessProcess(businessProcess).build();
            when(caseDetailsConverter.toCaseDataGA(any())).thenReturn(CaseData.builder().build());
            when(solicitorEmailValidation.validateSolicitorEmail(any(), any()))
                .thenReturn(caseData);

            judicialRespondentNotificationService.sendNotification(caseData, RESPONDENT);

            verify(notificationService, times(1)).sendMail(
                DUMMY_EMAIL, "general-application-apps-judicial-notification-template-lip-id",
                notificationPropertiesSummeryJudgement(), "general-apps-judicial-notification-make-decision-" + CASE_REFERENCE
            );
        }

        @Test
        void notificationShouldSendSendToLipRespondent_IfApplicationUncloakedForApproveOrEdit() {

            when(solicitorEmailValidation.validateSolicitorEmail(any(), any()))
                .thenReturn(caseDataWithSolicitorDataOnlyForApplicationUncloakedJudgeApproveOrEdit(
                    YES, NO, NO).toBuilder().businessProcess(businessProcess).build());
            when(caseDetailsConverter.toCaseDataGA(any())).thenReturn(CaseData.builder().build());

            judicialRespondentNotificationService.sendNotification(
                caseDataWithSolicitorDataOnlyForApplicationUncloakedJudgeApproveOrEdit(
                    YES, NO, NO).toBuilder().businessProcess(businessProcess).build(), RESPONDENT);

            verify(notificationService, times(1)).sendMail(
                DUMMY_EMAIL,
                "general-application-apps-judicial-notification-template-lip-id",
                notificationPropertiesSummeryJudgement(),
                "general-apps-judicial-notification-make-decision-" + CASE_REFERENCE
            );
        }

        @Test
        void shouldSendNotification_LipRespondent_UncloakedApplication() {

            CaseData caseData
                = caseDataForJudicialRequestForInformationOfApplication(NO, NO, NO,
                                                                        SEND_APP_TO_OTHER_PARTY)
                .toBuilder().businessProcess(businessProcess)
                .ccdState(CaseState.APPLICATION_ADD_PAYMENT)
                .generalAppType(GAApplicationType.builder()
                                                .types(applicationTypeSummeryJudgement()).build()).build();

            when(solicitorEmailValidation.validateSolicitorEmail(any(), any()))
                .thenReturn(caseData);
            when(caseDetailsConverter.toCaseDataGA(any())).thenReturn(CaseData.builder().build());

            judicialRespondentNotificationService.sendNotification(caseData, RESPONDENT);

            verify(notificationService, times(1)).sendMail(
                DUMMY_EMAIL,
                "general-application-apps-judicial-notification-template-lip-id",
                notificationPropertiesSummeryJudgement(),
                "general-apps-judicial-notification-make-decision-" + CASE_REFERENCE
            );
        }

        @Test
        void notificationShouldSendListForHearing() {

            when(solicitorEmailValidation.validateSolicitorEmail(any(), any()))
                .thenReturn(caseDataListForHearing());
            when(caseDetailsConverter.toCaseDataGA(any())).thenReturn(CaseData.builder().build());

            judicialRespondentNotificationService.sendNotification(caseDataListForHearing(), RESPONDENT);
            verify(notificationService).sendMail(
                DUMMY_EMAIL,
                "general-application-apps-judicial-notification-template-lip-id",
                notificationPropertiesSummeryJudgement(),
                "general-apps-judicial-notification-make-decision-" + CASE_REFERENCE
            );
        }

        private CaseData caseDataListForHearing() {
            return CaseData.builder()
                .ccdCaseReference(CASE_REFERENCE)
                .judicialDecision(GAJudicialDecision.builder()
                                      .decision(GAJudgeDecisionOption.LIST_FOR_A_HEARING).build())
                .isGaApplicantLip(NO)
                .generalAppInformOtherParty(GAInformOtherParty.builder().isWithNotice(YES).build())
                .businessProcess(businessProcess)
                .isGaRespondentOneLip(YES)
                .ccdState(APPLICATION_SUBMITTED_AWAITING_JUDICIAL_DECISION)
                .generalAppRespondentSolicitors(respondentSolicitors())
                .applicantPartyName("App")
                .claimant1PartyName("CL")
                .defendant1PartyName("DEF")
                .judicialDecisionMakeOrder(GAJudicialMakeAnOrder.builder()
                                               .makeAnOrder(GAJudgeMakeAnOrderOption.DISMISS_THE_APPLICATION).build())
                .generalAppApplnSolicitor(GASolicitorDetailsGAspec.builder()
                                              .email(DUMMY_EMAIL).build())
                .generalAppParentCaseLink(GeneralAppParentCaseLink.builder()
                                              .caseReference(CASE_REFERENCE.toString()).build())
                .generalAppType(GAApplicationType.builder()
                                    .types(applicationTypeSummeryJudgement()).build())
                .isMultiParty(NO)
                .build();
        }

        private CaseData caseDataForJudicialRequestForInformationOfApplication(
            YesOrNo isRespondentOrderAgreement, YesOrNo isWithNotice, YesOrNo isCloaked,
            GAJudgeRequestMoreInfoOption gaJudgeRequestMoreInfoOption) {

            return CaseData.builder()
                .ccdCaseReference(CASE_REFERENCE)
                .generalAppRespondentSolicitors(respondentSolicitors())
                .applicationIsCloaked(isCloaked)
                .isMultiParty(NO)
                .judicialDecision(GAJudicialDecision.builder()
                                      .decision(GAJudgeDecisionOption.REQUEST_MORE_INFO).build())
                .judicialDecisionRequestMoreInfo(GAJudicialRequestMoreInfo.builder()
                                                     .requestMoreInfoOption(gaJudgeRequestMoreInfoOption)
                                                     .judgeRequestMoreInfoText("Test")
                                                     .judgeRequestMoreInfoByDate(LocalDate.now())
                                                     .deadlineForMoreInfoSubmission(deadline).build())
                .generalAppRespondentAgreement(GARespondentOrderAgreement.builder()
                                                   .hasAgreed(isRespondentOrderAgreement).build())
                .generalAppInformOtherParty(GAInformOtherParty.builder().isWithNotice(isWithNotice).build())
                .generalAppApplnSolicitor(GASolicitorDetailsGAspec.builder()
                                              .email(DUMMY_EMAIL).build())
                .isGaApplicantLip(NO)
                .isGaRespondentOneLip(YES)
                .applicantPartyName("App")
                .claimant1PartyName("CL")
                .defendant1PartyName("DEF")
                .businessProcess(BusinessProcess.builder().camundaEvent(JUDGES_DECISION).build())
                .generalAppParentCaseLink(GeneralAppParentCaseLink.builder()
                                              .caseReference(CASE_REFERENCE.toString()).build())
                .generalAppType(GAApplicationType.builder()
                                    .types(applicationTypeToStayTheClaim()).build())
                .generalAppPBADetails(GAPbaDetails.builder().build())
                .build();

        }

        private CaseData caseDataWithSolicitorDataOnlyForApplicationUncloakedJudgeApproveOrEdit(YesOrNo orderAgreement,
                                                                                                YesOrNo isWithNotice,
                                                                                                YesOrNo isCloaked) {
            return CaseData.builder()
                .ccdCaseReference(CASE_REFERENCE)
                .applicationIsCloaked(isCloaked)
                .generalAppRespondentSolicitors(respondentSolicitors())
                .judicialDecisionMakeOrder(GAJudicialMakeAnOrder.builder()
                                               .makeAnOrder(GAJudgeMakeAnOrderOption.APPROVE_OR_EDIT).build())
                .judicialDecision(GAJudicialDecision.builder()
                                      .decision(GAJudgeDecisionOption.MAKE_AN_ORDER).build())
                .generalAppApplnSolicitor(GASolicitorDetailsGAspec.builder()
                                              .email(DUMMY_EMAIL).build())
                .isGaRespondentOneLip(YES)
                .isGaApplicantLip(NO)
                .ccdState(APPLICATION_SUBMITTED_AWAITING_JUDICIAL_DECISION)
                .businessProcess(BusinessProcess.builder().camundaEvent(JUDGES_DECISION).build())
                .generalAppParentCaseLink(GeneralAppParentCaseLink.builder()
                                              .caseReference(CASE_REFERENCE.toString()).build())
                .generalAppRespondentAgreement(GARespondentOrderAgreement.builder().hasAgreed(orderAgreement).build())
                .generalAppInformOtherParty(GAInformOtherParty.builder().isWithNotice(isWithNotice).build())
                .generalAppType(GAApplicationType.builder()
                                    .types(applicationTypeSummeryJudgement()).build())
                .isMultiParty(NO)
                .applicantPartyName("App")
                .claimant1PartyName("CL")
                .defendant1PartyName("DEF")
                .build();
        }

        private CaseData caseDataForJudicialDirectionOrderOfApplicationWhenRespondentsArePresentInList(
            YesOrNo orderAgreement,
            YesOrNo isWithNotice,
            YesOrNo isCloaked,
            YesOrNo isApplicantLip,
            YesOrNo isRespondentLip) {
            return CaseData.builder()
                .ccdCaseReference(CASE_REFERENCE)
                .generalAppRespondentAgreement(GARespondentOrderAgreement.builder().hasAgreed(orderAgreement).build())
                .generalAppInformOtherParty(GAInformOtherParty.builder().isWithNotice(isWithNotice).build())
                .applicationIsCloaked(isCloaked)
                .isGaApplicantLip(isApplicantLip)
                .applicantPartyName("App")
                .claimant1PartyName("CL")
                .ccdState(APPLICATION_SUBMITTED_AWAITING_JUDICIAL_DECISION)
                .defendant1PartyName("DEF")
                .isGaRespondentOneLip(isRespondentLip)
                .judicialDecisionMakeOrder(GAJudicialMakeAnOrder.builder()
                                               .makeAnOrder(GIVE_DIRECTIONS_WITHOUT_HEARING).build())
                .generalAppRespondentSolicitors(respondentSolicitors())
                .generalAppApplnSolicitor(GASolicitorDetailsGAspec.builder()
                                              .email(DUMMY_EMAIL).build())
                .businessProcess(BusinessProcess.builder().camundaEvent(JUDGES_DECISION).build())
                .generalAppParentCaseLink(GeneralAppParentCaseLink.builder()
                                              .caseReference(CASE_REFERENCE.toString()).build())
                .generalAppType(GAApplicationType.builder()
                                    .types(applicationTypeSummeryJudgement()).build())
                .generalAppPBADetails(GAPbaDetails.builder().build())
                .isMultiParty(NO)
                .build();
        }

        private CaseData caseDataForJudgeDismissal(YesOrNo orderAgreement, YesOrNo isWithNotice, YesOrNo isCloaked,
                                                   YesOrNo isLipApplicant, YesOrNo isLipRespondent) {
            return CaseData.builder()
                .ccdCaseReference(CASE_REFERENCE)
                .generalAppRespondentAgreement(GARespondentOrderAgreement.builder().hasAgreed(orderAgreement).build())
                .generalAppInformOtherParty(GAInformOtherParty.builder().isWithNotice(isWithNotice).build())
                .applicationIsCloaked(isCloaked)
                .isGaApplicantLip(isLipApplicant)
                .ccdState(APPLICATION_SUBMITTED_AWAITING_JUDICIAL_DECISION)
                .isGaRespondentOneLip(isLipRespondent)
                .applicantPartyName("App")
                .claimant1PartyName("CL")
                .defendant1PartyName("DEF")
                .generalAppRespondentSolicitors(respondentSolicitors())
                .judicialDecision(GAJudicialDecision.builder()
                                      .decision(GAJudgeDecisionOption.MAKE_AN_ORDER).build())
                .judicialDecisionMakeOrder(GAJudicialMakeAnOrder.builder()
                                               .makeAnOrder(GAJudgeMakeAnOrderOption.DISMISS_THE_APPLICATION).build())
                .generalAppApplnSolicitor(GASolicitorDetailsGAspec.builder()
                                              .email(DUMMY_EMAIL).build())
                .businessProcess(BusinessProcess.builder().camundaEvent(JUDGES_DECISION).build())
                .generalAppParentCaseLink(GeneralAppParentCaseLink.builder()
                                              .caseReference(CASE_REFERENCE.toString()).build())
                .generalAppType(GAApplicationType.builder()
                                    .types(applicationTypeSummeryJudgement()).build())
                .generalAppPBADetails(GAPbaDetails.builder().build())
                .isMultiParty(NO)
                .build();
        }

        private Map<String, String> notificationPropertiesSummeryJudgement() {

            customProp.put(NotificationDataGA.CASE_REFERENCE, CASE_REFERENCE.toString());
            customProp.put(NotificationDataGA.GENAPP_REFERENCE, CASE_REFERENCE.toString());
            customProp.put(NotificationDataGA.CASE_TITLE, "CL v DEF");
            customProp.put(NotificationDataGA.GA_APPLICATION_TYPE,
                           GeneralApplicationTypes.SUMMARY_JUDGEMENT.getDisplayedValue());
            customProp.put(NotificationDataGA.PARTY_REFERENCE, PARTY_REFERENCE.toString());
            customProp.put(NotificationDataGA.GA_LIP_RESP_NAME, "DEF");
            customProp.put(NotificationDataGA.WELSH_CONTACT, "E-bost: ymholiadaucymraeg@justice.gov.uk");
            customProp.put(NotificationData.WELSH_HMCTS_SIGNATURE, "Hawliadau am Arian yn y Llys Sifil Ar-lein \n Gwasanaeth Llysoedd a Thribiwnlysoedd EF");
            customProp.put(NotificationData.WELSH_OPENING_HOURS, "Dydd Llun i ddydd Iau, 9am – 5pm, dydd Gwener, 9am – 4.30pm");
            customProp.put(NotificationData.WELSH_PHONE_CONTACT, "Ffôn: 0300 303 5174");
            customProp.put(NotificationDataGA.SPEC_CONTACT, "Email: contactocmc@justice.gov.uk");
            customProp.put(NotificationData.SPEC_UNSPEC_CONTACT, "Email for Specified Claims: contactocmc@justice.gov.uk "
                + "\n Email for Damages Claims: damagesclaims@justice.gov.uk");
            customProp.put(NotificationData.HMCTS_SIGNATURE, "Online Civil Claims \n HM Courts & Tribunal Service");
            customProp.put(NotificationData.OPENING_HOURS, "Monday to Friday, 8.30am to 5pm");
            customProp.put(NotificationData.PHONE_CONTACT, "For anything related to hearings, call 0300 123 5577 "
                + "\n For all other matters, call 0300 123 7050");
            return customProp;
        }

        private CaseData caseDataForConcurrentWrittenOption(YesOrNo isGaApplicantLip, YesOrNo isGaRespondentOneLip) {
            return
                CaseData.builder()
                    .ccdCaseReference(CASE_REFERENCE)
                    .isGaApplicantLip(isGaApplicantLip)
                    .isGaRespondentOneLip(isGaRespondentOneLip)
                    .applicantPartyName("App")
                    .claimant1PartyName("CL")
                    .ccdState(APPLICATION_SUBMITTED_AWAITING_JUDICIAL_DECISION)
                    .defendant1PartyName("DEF")
                    .parentClaimantIsApplicant(YES)
                    .generalAppRespondentSolicitors(respondentSolicitors())
                    .judicialDecision(GAJudicialDecision.builder()
                                          .decision(GAJudgeDecisionOption.LIST_FOR_A_HEARING).build())
                    .generalAppApplnSolicitor(GASolicitorDetailsGAspec.builder()
                                                  .email(DUMMY_EMAIL).build())
                    .businessProcess(BusinessProcess.builder().camundaEvent(JUDGES_DECISION).build())
                    .generalAppParentCaseLink(GeneralAppParentCaseLink.builder()
                                                  .caseReference(CASE_REFERENCE.toString()).build())
                    .judicialDecisionMakeAnOrderForWrittenRepresentations(
                        GAJudicialWrittenRepresentations.builder().writtenOption(
                            GAJudgeWrittenRepresentationsOptions.CONCURRENT_REPRESENTATIONS).build())
                    .generalAppType(GAApplicationType.builder()
                                        .types(applicationTypeSummeryJudgement()).build())
                    .judicialConcurrentDateText(DUMMY_DATE)
                    .isMultiParty(NO)
                    .build();
        }

        private CaseData caseDataForSequentialWrittenOption(YesOrNo isGaApplicantLip, YesOrNo isGaRespondentOneLip) {
            return CaseData.builder()
                .ccdCaseReference(CASE_REFERENCE)
                .isGaApplicantLip(isGaApplicantLip)
                .isGaRespondentOneLip(isGaRespondentOneLip)
                .applicantPartyName("App")
                .claimant1PartyName("CL")
                .ccdState(APPLICATION_SUBMITTED_AWAITING_JUDICIAL_DECISION)
                .defendant1PartyName("DEF")
                .generalAppRespondentSolicitors(respondentSolicitors())
                .judicialDecision(GAJudicialDecision.builder()
                                      .decision(GAJudgeDecisionOption.LIST_FOR_A_HEARING).build())
                .generalAppRespondentSolicitors(respondentSolicitors())
                .generalAppApplnSolicitor(GASolicitorDetailsGAspec.builder()
                                              .email(DUMMY_EMAIL).build())
                .businessProcess(BusinessProcess.builder().camundaEvent(JUDGES_DECISION).build())
                .generalAppParentCaseLink(GeneralAppParentCaseLink.builder()
                                              .caseReference(CASE_REFERENCE.toString()).build())
                .judicialDecisionMakeAnOrderForWrittenRepresentations(
                    GAJudicialWrittenRepresentations.builder().writtenOption(
                        GAJudgeWrittenRepresentationsOptions.SEQUENTIAL_REPRESENTATIONS).build())
                .generalAppType(GAApplicationType.builder()
                                    .types(applicationTypeSummeryJudgement()).build())
                .isMultiParty(NO)
                .build();
        }

        private List<Element<GASolicitorDetailsGAspec>> respondentSolicitors() {
            return Arrays.asList(element(GASolicitorDetailsGAspec.builder().id(ID)
                                             .forename("respondent")
                                             .surname(Optional.of("solicitor"))
                                             .email(DUMMY_EMAIL).organisationIdentifier(ORG_ID).build()));
        }

        public List<GeneralApplicationTypes> applicationTypeSummeryJudgement() {
            return List.of(
                GeneralApplicationTypes.SUMMARY_JUDGEMENT
            );
        }
    }

    @Nested
    class AboutToSubmitCallback {

        @Test
        void shouldNotSentNotification_WhenNotificationCriteria_NotMet() {

            CaseData caseData = caseDataForConcurrentWrittenOption().toBuilder().businessProcess(null).build();
            when(solicitorEmailValidation.validateSolicitorEmail(any(), any()))
                .thenReturn(caseData);

            judicialRespondentNotificationService.sendNotification(caseData, RESPONDENT);
            verify(notificationService, never()).sendMail(any(), any(), any(), any());
        }

        @Test
        void notificationShouldSendThriceForConcurrentWrittenRepsWhenInvoked() {

            when(solicitorEmailValidation.validateSolicitorEmail(any(), any()))
                .thenReturn(caseDataForConcurrentWrittenOption());

            judicialRespondentNotificationService.sendNotification(caseDataForConcurrentWrittenOption(), RESPONDENT);
            verify(notificationService, times(2)).sendMail(
                DUMMY_EMAIL,
                "general-application-apps-judicial-notification-template-id",
                notificationPropertiesSummeryJudgementConcurrent(),
                "general-apps-judicial-notification-make-decision-" + CASE_REFERENCE
            );
        }

        @Test
        void notificationShouldSendThriceForSequentialWrittenRepsWhenInvoked() {

            when(solicitorEmailValidation.validateSolicitorEmail(any(), any()))
                .thenReturn(caseDataForSequentialWrittenOption());

            judicialRespondentNotificationService.sendNotification(caseDataForSequentialWrittenOption(), RESPONDENT);
            verify(notificationService, times(2)).sendMail(
                DUMMY_EMAIL,
                "general-application-apps-judicial-notification-template-id",
                notificationPropertiesToGetReliefFromSanctions(),
                "general-apps-judicial-notification-make-decision-" + CASE_REFERENCE
            );
        }

        @Test
        void notificationShouldSendForConcurrentWrittenRepsWhenInvoked() {

            when(solicitorEmailValidation.validateSolicitorEmail(any(), any()))
                .thenReturn(caseDataForConcurrentWrittenRepRespondentNotPresent());

            judicialRespondentNotificationService
                .sendNotification(caseDataForConcurrentWrittenRepRespondentNotPresent(), RESPONDENT);
            verify(notificationService, times(2)).sendMail(
                DUMMY_EMAIL,
                "general-application-apps-judicial-notification-template-id",
                notificationPropertiesSummeryJudgementConcurrent(),
                "general-apps-judicial-notification-make-decision-" + CASE_REFERENCE
            );
        }

        @Test
        void notificationShouldSendForDismissal() {

            when(solicitorEmailValidation.validateSolicitorEmail(any(), any()))
                .thenReturn(caseDataForJudgeDismissal(NO, YES, NO));

            judicialRespondentNotificationService.sendNotification(caseDataForJudgeDismissal(NO, YES, NO), RESPONDENT);
            verify(notificationService, times(2)).sendMail(
                DUMMY_EMAIL,
                "general-application-apps-judicial-notification-template-id",
                notificationPropertiesToStrikeOut(),
                "general-apps-judicial-notification-make-decision-" + CASE_REFERENCE
            );
        }

        @Test
        void notificationUncloakShouldSendForDismissal() {

            when(solicitorEmailValidation.validateSolicitorEmail(any(), any()))
                .thenReturn(caseDataForJudgeDismissal(NO, NO, NO));

            judicialRespondentNotificationService.sendNotification(caseDataForJudgeDismissal(NO, NO, NO), RESPONDENT);
            verify(notificationService, times(2)).sendMail(
                DUMMY_EMAIL,
                "general-application-apps-judicial-notification-template-id",
                notificationPropertiesToStrikeOut(),
                "general-apps-judicial-notification-make-decision-" + CASE_REFERENCE
            );
        }

        @Test
        void notificationShouldSendIfJudicialApproval() {
            when(solicitorEmailValidation.validateSolicitorEmail(any(), any()))
                .thenReturn(caseDataForJudicialApprovalOfApplication(NO, YES));

            judicialRespondentNotificationService
                .sendNotification(caseDataForJudicialApprovalOfApplication(NO, YES), RESPONDENT);
            verify(notificationService, times(2)).sendMail(
                DUMMY_EMAIL,
                "general-application-apps-judicial-notification-template-id",
                notificationPropertiesToAmendStatementOfCase(),
                "general-apps-judicial-notification-make-decision-" + CASE_REFERENCE
            );
        }

        @Test
        void notificationShouldSendIfJudicialDirectionOrder_AfterAdditionalPaymentReceived() {

            when(solicitorEmailValidation.validateSolicitorEmail(any(), any()))
                .thenReturn(caseDataForJudicialDirectionOrderOfApplication(NO, NO).toBuilder().generalAppPBADetails(
                        GAPbaDetails.builder()
                            .additionalPaymentDetails(buildAdditionalPaymentSuccessData())
                            .build())
                                .build());

            judicialRespondentNotificationService.sendNotification(
                caseDataForJudicialApprovalOfApplication(NO, NO).toBuilder()
                    .ccdCaseReference(CASE_REFERENCE)
                    .generalAppPBADetails(GAPbaDetails.builder()
                                              .additionalPaymentDetails(buildAdditionalPaymentSuccessData())
                                              .build())
                    .build(), RESPONDENT);
            verify(notificationService, times(2)).sendMail(
                DUMMY_EMAIL,
                "general-application-apps-judicial-notification-template-id",
                notificationPropertiesToAmendStatementOfCase(),
                "general-apps-judicial-notification-make-decision-" + CASE_REFERENCE
            );
        }

        @Test
        void notificationShouldSendIfJudicialDirectionOrder() {

            when(solicitorEmailValidation.validateSolicitorEmail(any(), any()))
                .thenReturn(caseDataForJudicialDirectionOrderOfApplication(NO, NO));

            judicialRespondentNotificationService
                .sendNotification(caseDataForJudicialApprovalOfApplication(NO, NO), RESPONDENT);
            verify(notificationService, times(2)).sendMail(
                DUMMY_EMAIL,
                "general-application-apps-judicial-notification-template-id",
                notificationPropertiesToAmendStatementOfCase(),
                "general-apps-judicial-notification-make-decision-" + CASE_REFERENCE
            );
        }

        @Test
        void notificationUncloakShouldSendIfJudicialDirectionOrder() {

            when(solicitorEmailValidation.validateSolicitorEmail(any(), any()))
                .thenReturn(caseDataForJudicialDirectionOrderOfApplication(YES, NO));

            judicialRespondentNotificationService
                .sendNotification(caseDataForJudicialDirectionOrderOfApplication(YES, NO), RESPONDENT);
            verify(notificationService, times(2)).sendMail(
                DUMMY_EMAIL,
                "general-application-apps-judicial-notification-template-id",
                notificationPropertiesToAmendStatementOfCase(),
                "general-apps-judicial-notification-make-decision-" + CASE_REFERENCE
            );
        }

        @Test
        void notificationShouldSendIfJudicialDirectionOrderRepArePresentInList() {

            when(solicitorEmailValidation.validateSolicitorEmail(any(), any()))
                .thenReturn(caseDataForJudicialDirectionOrderOfApplicationWhenRespondentsArePresentInList(NO, YES, NO));

            judicialRespondentNotificationService
                .sendNotification(
                    caseDataForJudicialDirectionOrderOfApplicationWhenRespondentsArePresentInList(
                        NO, YES, NO), RESPONDENT);
            verify(notificationService, times(2)).sendMail(
                DUMMY_EMAIL,
                "general-application-apps-judicial-notification-template-id",
                notificationPropertiesToAmendStatementOfCase(),
                "general-apps-judicial-notification-make-decision-" + CASE_REFERENCE
            );
        }

        @Test
        void notificationUncloakShouldSendIfJudicialDirectionOrderRepArePresentInList() {

            when(solicitorEmailValidation.validateSolicitorEmail(any(), any()))
                .thenReturn(caseDataForJudicialDirectionOrderOfApplicationWhenRespondentsArePresentInList(NO, NO, NO));

            judicialRespondentNotificationService
                .sendNotification(
                    caseDataForJudicialDirectionOrderOfApplicationWhenRespondentsArePresentInList(
                        NO, NO, NO), RESPONDENT);
            verify(notificationService, times(2)).sendMail(
                DUMMY_EMAIL,
                "general-application-apps-judicial-notification-template-id",
                notificationPropertiesToAmendStatementOfCase(),
                "general-apps-judicial-notification-make-decision-" + CASE_REFERENCE
            );
        }

        @Test
        void notificationShouldSendIfSequentialWrittenRepsArePresentInList() {

            when(solicitorEmailValidation.validateSolicitorEmail(any(), any()))
                .thenReturn(caseDataForSequentialWrittenRepInList());

            judicialRespondentNotificationService.sendNotification(caseDataForSequentialWrittenRepInList(), RESPONDENT);
            verify(notificationService, times(2)).sendMail(
                DUMMY_EMAIL,
                "general-application-apps-judicial-notification-template-id",
                notificationPropertiesSummeryJudgementConcurrent(),
                "general-apps-judicial-notification-make-decision-" + CASE_REFERENCE
            );
        }

        @Test
        void notificationShouldSendForApplicationsApprovedWhenRespondentsAreInList() {
            when(solicitorEmailValidation.validateSolicitorEmail(any(), any()))
                .thenReturn(caseDataForApplicationsApprovedWhenRespondentsAreInList(NO, YES));

            judicialRespondentNotificationService
                .sendNotification(caseDataForApplicationsApprovedWhenRespondentsAreInList(NO, YES), RESPONDENT);
            verify(notificationService, times(2)).sendMail(
                DUMMY_EMAIL,
                "general-application-apps-judicial-notification-template-id",
                notificationPropertiesSummeryJudgementConcurrent(),
                "general-apps-judicial-notification-make-decision-" + CASE_REFERENCE
            );
        }

        @Test
        void shouldSendNotificationToRespondent_ForApplicationApprovedUncloaked_WhenRespondentsAreInList() {

            when(solicitorEmailValidation.validateSolicitorEmail(any(), any()))
                .thenReturn(caseDataForApplicationsApprovedWhenRespondentsAreInList(NO, NO));

            judicialRespondentNotificationService
                .sendNotification(caseDataForApplicationsApprovedWhenRespondentsAreInList(NO, NO), RESPONDENT);
            verify(notificationService, times(2)).sendMail(
                DUMMY_EMAIL,
                "general-application-apps-judicial-notification-template-id",
                notificationPropertiesSummeryJudgementConcurrent(),
                "general-apps-judicial-notification-make-decision-" + CASE_REFERENCE
            );
        }

        @Test
        void shouldSendNotification_WhenApplicationUncloaked() {

            when(solicitorEmailValidation.validateSolicitorEmail(any(), any()))
                .thenReturn(
                    caseDataForApplicationsApprovedWhenRespondentsAreInList(NO, NO)
                        .toBuilder()
                        .generalAppPBADetails(GAPbaDetails.builder()
                                                  .additionalPaymentDetails(buildAdditionalPaymentSuccessData())
                                                  .build())
                        .build());

            CaseData caseData = caseDataForApplicationsApprovedWhenRespondentsAreInList(NO, NO)
                .toBuilder().generalAppPBADetails(
                    GAPbaDetails.builder()
                        .additionalPaymentDetails(buildAdditionalPaymentSuccessData())
                        .build())
                .build();

            judicialRespondentNotificationService.sendNotification(caseData, RESPONDENT);
            verify(notificationService, times(2)).sendMail(
                DUMMY_EMAIL,
                "general-application-apps-judicial-notification-template-id",
                notificationPropertiesSummeryJudgementConcurrent(),
                "general-apps-judicial-notification-make-decision-" + CASE_REFERENCE
            );
        }

        @Test
        void notificationShouldSendApproveDamagesWhenRespondentsAreInList() {

            when(solicitorEmailValidation.validateSolicitorEmail(any(), any()))
                .thenReturn(
                    caseDataForApplicationsApprovedStrikeOutWhenRespondentsAreInList(
                        NO, YES, NO, "UNSPEC_CLAIM"));

            judicialRespondentNotificationService
                .sendNotification(
                    caseDataForApplicationsApprovedStrikeOutWhenRespondentsAreInList(
                        NO, YES, NO, "UNSPEC_CLAIM"), RESPONDENT);
            verify(notificationService, times(2)).sendMail(
                DUMMY_EMAIL,
                "general-application-apps-judicial-notification-template-id",
                notificationPropertiesToStrikeOut(),
                "general-apps-judicial-notification-make-decision-" + CASE_REFERENCE
            );
        }

        @Test
        void notificationShouldSendApproveOcmcWhenRespondentsAreInList() {

            when(solicitorEmailValidation.validateSolicitorEmail(any(), any()))
                .thenReturn(
                    caseDataForApplicationsApprovedStrikeOutWhenRespondentsAreInList(
                        NO, YES, NO, "SPEC_CLAIM"));

            judicialRespondentNotificationService
                .sendNotification(
                    caseDataForApplicationsApprovedStrikeOutWhenRespondentsAreInList(
                        NO, YES, NO, "SPEC_CLAIM"), RESPONDENT);

            verify(notificationService, times(2)).sendMail(
                DUMMY_EMAIL,
                "general-application-apps-judicial-notification-template-id",
                notificationPropertiesToStrikeOut(),
                "general-apps-judicial-notification-make-decision-" + CASE_REFERENCE
            );
        }

        @Test
        void notificationShouldSendForApplicationListForHearingWhenRespondentsAreAvailableInList() {

            when(solicitorEmailValidation.validateSolicitorEmail(any(), any()))
                .thenReturn(caseDataForListForHearingRespondentsAreInList(NO, YES));

            judicialRespondentNotificationService
                .sendNotification(caseDataForListForHearingRespondentsAreInList(NO, YES), RESPONDENT);
            verify(notificationService, times(2)).sendMail(
                DUMMY_EMAIL,
                "general-application-apps-judicial-notification-template-id",
                notificationPropertiesSummeryJudgementConcurrent(),
                "general-apps-judicial-notification-make-decision-" + CASE_REFERENCE
            );
        }

        @Test
        void notificationShouldSendForHearingWhenRespondents_WithoutNotice() {

            when(solicitorEmailValidation.validateSolicitorEmail(any(), any()))
                .thenReturn(caseDataForListForHearingRespondentsAreInList(NO, NO));

            judicialRespondentNotificationService
                .sendNotification(caseDataForListForHearingRespondentsAreInList(NO, NO), RESPONDENT);
            verify(notificationService, times(2)).sendMail(
                DUMMY_EMAIL,
                "general-application-apps-judicial-notification-template-id",
                notificationPropertiesSummeryJudgementConcurrent(),
                "general-apps-judicial-notification-make-decision-" + CASE_REFERENCE
            );
        }

        @Test
        void notificationShouldSendWhenApplicationIsDismissedByJudgeWhenRespondentsAreAvailableInList() {

            when(solicitorEmailValidation.validateSolicitorEmail(any(), any()))
                .thenReturn(caseDataForCaseDismissedByJudgeRespondentsAreInList(NO, YES, NO));

            judicialRespondentNotificationService.sendNotification(
                caseDataForCaseDismissedByJudgeRespondentsAreInList(NO, YES, NO), RESPONDENT);

            verify(notificationService, times(2)).sendMail(
                DUMMY_EMAIL,
                "general-application-apps-judicial-notification-template-id",
                notificationPropertiesSummeryJudgementConcurrent(),
                "general-apps-judicial-notification-make-decision-" + CASE_REFERENCE
            );
        }

        @Test
        void notificationUncloakShouldSendForApprovedDamageWhenRespondentsAreInList() {

            when(solicitorEmailValidation.validateSolicitorEmail(any(), any()))
                .thenReturn(
                    caseDataForApplicationsApprovedStrikeOutWhenRespondentsAreInList(
                        NO, NO, NO, "UNSPEC_CLAIM"));

            judicialRespondentNotificationService
                .sendNotification(
                    caseDataForApplicationsApprovedStrikeOutWhenRespondentsAreInList(
                        NO, NO, NO, "UNSPEC_CLAIM"), RESPONDENT);

            verify(notificationService, times(2)).sendMail(
                DUMMY_EMAIL,
                "general-application-apps-judicial-notification-template-id",
                notificationPropertiesToStrikeOut(),
                "general-apps-judicial-notification-make-decision-" + CASE_REFERENCE
            );
        }

        @Test
        void notificationUncloakShouldSendForApprovedOcmcWhenRespondentsAreInList() {

            when(solicitorEmailValidation.validateSolicitorEmail(any(), any()))
                .thenReturn(caseDataForApplicationsApprovedStrikeOutWhenRespondentsAreInList(
                    NO, NO, NO, "SPEC_CLAIM"));

            judicialRespondentNotificationService
                .sendNotification(
                    caseDataForApplicationsApprovedStrikeOutWhenRespondentsAreInList(
                        NO, NO, NO, "SPEC_CLAIM"), RESPONDENT);

            verify(notificationService, times(2)).sendMail(
                DUMMY_EMAIL,
                "general-application-apps-judicial-notification-template-id",
                notificationPropertiesToStrikeOut(),
                "general-apps-judicial-notification-make-decision-" + CASE_REFERENCE
            );
        }

        @Test
        void shouldSendNotification_WhenAdditionalPaymentReceived_JudgeDismissedApplicationUncloaked() {

            when(solicitorEmailValidation.validateSolicitorEmail(any(), any()))
                .thenReturn(
                    caseDataForCaseDismissedByJudgeRespondentsAreInList(NO, NO, YES).toBuilder()
                        .generalAppPBADetails(GAPbaDetails.builder()
                                                  .additionalPaymentDetails(buildAdditionalPaymentSuccessData())
                                                  .build())
                        .build()
                );

            CaseData caseData = caseDataForCaseDismissedByJudgeRespondentsAreInList(NO, NO, YES)
                .toBuilder().generalAppPBADetails(
                    GAPbaDetails.builder()
                        .additionalPaymentDetails(buildAdditionalPaymentSuccessData())
                        .build())
                .build();

            judicialRespondentNotificationService.sendNotification(caseData, RESPONDENT);
            verify(notificationService, times(2)).sendMail(
                DUMMY_EMAIL,
                "general-application-apps-judicial-notification-template-id",
                notificationPropertiesSummeryJudgementConcurrent(),
                "general-apps-judicial-notification-make-decision-" + CASE_REFERENCE
            );
        }

        private CaseData caseDataForConcurrentWrittenOption() {
            return
                CaseData.builder()
                    .ccdCaseReference(CASE_REFERENCE)
                    .isMultiParty(NO)
                    .generalAppRespondentSolicitors(respondentSolicitors())
                    .judicialDecision(GAJudicialDecision.builder()
                                          .decision(GAJudgeDecisionOption.LIST_FOR_A_HEARING).build())
                    .generalAppRespondentSolicitors(respondentSolicitors())
                    .generalAppApplnSolicitor(GASolicitorDetailsGAspec.builder()
                                                  .email(DUMMY_EMAIL).build())
                    .businessProcess(BusinessProcess.builder().camundaEvent(JUDGES_DECISION).build())
                    .generalAppParentCaseLink(GeneralAppParentCaseLink.builder()
                                                  .caseReference(CASE_REFERENCE.toString()).build())
                    .judicialDecisionMakeAnOrderForWrittenRepresentations(
                        GAJudicialWrittenRepresentations.builder().writtenOption(
                            GAJudgeWrittenRepresentationsOptions.CONCURRENT_REPRESENTATIONS).build())
                    .generalAppType(GAApplicationType.builder()
                                        .types(applicationTypeSummeryJudgement()).build())
                    .judicialConcurrentDateText(DUMMY_DATE)
                    .build();
        }

        private CaseData caseDataForSequentialWrittenOption() {
            return CaseData.builder()
                .ccdCaseReference(CASE_REFERENCE)
                .generalAppRespondentSolicitors(respondentSolicitors())
                .judicialDecision(GAJudicialDecision.builder()
                                      .decision(GAJudgeDecisionOption.LIST_FOR_A_HEARING).build())
                .generalAppRespondentSolicitors(respondentSolicitors())
                .generalAppApplnSolicitor(GASolicitorDetailsGAspec.builder()
                                              .email(DUMMY_EMAIL).build())
                .businessProcess(BusinessProcess.builder().camundaEvent(JUDGES_DECISION).build())
                .generalAppParentCaseLink(GeneralAppParentCaseLink.builder()
                                              .caseReference(CASE_REFERENCE.toString()).build())
                .judicialDecisionMakeAnOrderForWrittenRepresentations(
                    GAJudicialWrittenRepresentations.builder().writtenOption(
                        GAJudgeWrittenRepresentationsOptions.SEQUENTIAL_REPRESENTATIONS).build())
                .generalAppType(GAApplicationType.builder()
                                    .types(applicationTypeToGetReliefFromSanctions()).build())
                .isMultiParty(NO)
                .build();
        }

        private CaseData caseDataForConcurrentWrittenRepRespondentNotPresent() {
            return CaseData.builder()
                .ccdCaseReference(CASE_REFERENCE)
                .generalAppApplnSolicitor(GASolicitorDetailsGAspec.builder()
                                              .email(DUMMY_EMAIL).build())
                .generalAppRespondentSolicitors(respondentSolicitors())
                .businessProcess(BusinessProcess.builder().camundaEvent(JUDGES_DECISION).build())
                .generalAppParentCaseLink(GeneralAppParentCaseLink.builder()
                                              .caseReference(CASE_REFERENCE.toString()).build())
                .judicialDecisionMakeAnOrderForWrittenRepresentations(
                    GAJudicialWrittenRepresentations.builder().writtenOption(
                        GAJudgeWrittenRepresentationsOptions.CONCURRENT_REPRESENTATIONS).build())
                .generalAppType(GAApplicationType.builder()
                                    .types(applicationTypeSummeryJudgement()).build())
                .judicialConcurrentDateText(DUMMY_DATE)
                .isMultiParty(NO)
                .build();
        }

        private CaseData caseDataForJudgeDismissal(YesOrNo orderAgreement, YesOrNo isWithNotice, YesOrNo isCloaked) {
            return CaseData.builder()
                .ccdCaseReference(CASE_REFERENCE)
                .generalAppRespondentAgreement(GARespondentOrderAgreement.builder().hasAgreed(orderAgreement).build())
                .generalAppInformOtherParty(GAInformOtherParty.builder().isWithNotice(isWithNotice).build())
                .applicationIsCloaked(isCloaked)
                .generalAppRespondentSolicitors(respondentSolicitors())
                .judicialDecisionMakeOrder(GAJudicialMakeAnOrder.builder()
                                               .makeAnOrder(GAJudgeMakeAnOrderOption.DISMISS_THE_APPLICATION).build())
                .generalAppApplnSolicitor(GASolicitorDetailsGAspec.builder()
                                              .email(DUMMY_EMAIL).build())
                .businessProcess(BusinessProcess.builder().camundaEvent(JUDGES_DECISION).build())
                .generalAppParentCaseLink(GeneralAppParentCaseLink.builder()
                                              .caseReference(CASE_REFERENCE.toString()).build())
                .generalAppType(GAApplicationType.builder()
                                    .types(applicationTypeToStrikeOut()).build())
                .generalAppPBADetails(GAPbaDetails.builder().build())
                .isMultiParty(NO)
                .build();
        }

        private CaseData caseDataForJudicialApprovalOfApplication(YesOrNo orderAgreement, YesOrNo isWithNotice) {
            return CaseData.builder()
                .ccdCaseReference(CASE_REFERENCE)
                .generalAppRespondentAgreement(GARespondentOrderAgreement.builder().hasAgreed(orderAgreement).build())
                .generalAppInformOtherParty(GAInformOtherParty.builder().isWithNotice(isWithNotice).build())
                .judicialDecisionMakeOrder(GAJudicialMakeAnOrder.builder()
                                               .makeAnOrder(GAJudgeMakeAnOrderOption.APPROVE_OR_EDIT).build())
                .generalAppRespondentSolicitors(respondentSolicitors())
                .generalAppApplnSolicitor(GASolicitorDetailsGAspec.builder()
                                              .email(DUMMY_EMAIL).build())
                .businessProcess(BusinessProcess.builder().camundaEvent(JUDGES_DECISION).build())
                .generalAppParentCaseLink(GeneralAppParentCaseLink.builder()
                                              .caseReference(CASE_REFERENCE.toString()).build())
                .generalAppType(GAApplicationType.builder()
                                    .types(applicationTypeToAmendStatmentOfClaim()).build())
                .generalAppPBADetails(GAPbaDetails.builder().build())
                .isMultiParty(NO)
                .build();
        }

        private CaseData caseDataForJudicialDirectionOrderOfApplication(YesOrNo orderAgreement, YesOrNo isWithNotice) {
            return CaseData.builder()
                .ccdCaseReference(CASE_REFERENCE)
                .isMultiParty(NO)
                .generalAppRespondentAgreement(GARespondentOrderAgreement.builder().hasAgreed(orderAgreement).build())
                .generalAppInformOtherParty(GAInformOtherParty.builder().isWithNotice(isWithNotice).build())
                .judicialDecisionMakeOrder(GAJudicialMakeAnOrder.builder()
                                               .makeAnOrder(GIVE_DIRECTIONS_WITHOUT_HEARING).build())
                .generalAppRespondentSolicitors(respondentSolicitors())
                .generalAppApplnSolicitor(GASolicitorDetailsGAspec.builder()
                                              .email(DUMMY_EMAIL).build())
                .businessProcess(BusinessProcess.builder().camundaEvent(JUDGES_DECISION).build())
                .generalAppParentCaseLink(GeneralAppParentCaseLink.builder()
                                              .caseReference(CASE_REFERENCE.toString()).build())
                .generalAppType(GAApplicationType.builder()
                                    .types(applicationTypeToAmendStatmentOfClaim()).build())
                .generalAppPBADetails(GAPbaDetails.builder().build())
                .build();
        }

        private CaseData caseDataForJudicialDirectionOrderOfApplicationWhenRespondentsArePresentInList(
            YesOrNo orderAgreement,
            YesOrNo isWithNotice,
            YesOrNo isCloaked) {
            return CaseData.builder()
                .ccdCaseReference(CASE_REFERENCE)
                .generalAppRespondentAgreement(GARespondentOrderAgreement.builder().hasAgreed(orderAgreement).build())
                .generalAppInformOtherParty(GAInformOtherParty.builder().isWithNotice(isWithNotice).build())
                .applicationIsCloaked(isCloaked)
                .judicialDecisionMakeOrder(GAJudicialMakeAnOrder.builder()
                                               .makeAnOrder(GIVE_DIRECTIONS_WITHOUT_HEARING).build())
                .generalAppRespondentSolicitors(respondentSolicitors())
                .generalAppApplnSolicitor(GASolicitorDetailsGAspec.builder()
                                              .email(DUMMY_EMAIL).build())
                .businessProcess(BusinessProcess.builder().camundaEvent(JUDGES_DECISION).build())
                .generalAppParentCaseLink(GeneralAppParentCaseLink.builder()
                                              .caseReference(CASE_REFERENCE.toString()).build())
                .generalAppType(GAApplicationType.builder()
                                    .types(applicationTypeToAmendStatmentOfClaim()).build())
                .generalAppPBADetails(GAPbaDetails.builder().build())
                .isMultiParty(NO)
                .build();
        }

        private CaseData caseDataForApplicationsApprovedStrikeOutWhenRespondentsAreInList(
            YesOrNo orderAgreement,
            YesOrNo isWithNotice, YesOrNo isCloaked, String superClaimType) {
            return
                CaseData.builder()
                    .ccdCaseReference(CASE_REFERENCE)
                    .generalAppRespondentSolicitors(respondentSolicitors())
                    .generalAppSuperClaimType(superClaimType)
                    .generalAppRespondentAgreement(GARespondentOrderAgreement.builder()
                                                       .hasAgreed(orderAgreement).build())
                    .generalAppInformOtherParty(GAInformOtherParty.builder().isWithNotice(isWithNotice).build())
                    .applicationIsCloaked(isCloaked)
                    .generalAppApplnSolicitor(GASolicitorDetailsGAspec.builder()
                                                  .email(DUMMY_EMAIL).build())
                    .businessProcess(BusinessProcess.builder().camundaEvent(JUDGES_DECISION).build())
                    .generalAppParentCaseLink(GeneralAppParentCaseLink.builder()
                                                  .caseReference(CASE_REFERENCE.toString()).build())
                    .judicialDecisionMakeOrder(GAJudicialMakeAnOrder.builder()
                                                   .makeAnOrder(
                                                       GAJudgeMakeAnOrderOption.APPROVE_OR_EDIT).build())
                    .generalAppType(GAApplicationType.builder()
                                        .types(applicationTypeToStrikeOut()).build())
                    .judicialConcurrentDateText(DUMMY_DATE)
                    .generalAppPBADetails(GAPbaDetails.builder().build())
                    .isMultiParty(NO)
                    .build();
        }

        private CaseData caseDataForSequentialWrittenRepInList() {
            return
                CaseData.builder()
                    .ccdCaseReference(CASE_REFERENCE)
                    .isMultiParty(NO)
                    .judicialDecision(GAJudicialDecision.builder()
                                          .decision(GAJudgeDecisionOption.LIST_FOR_A_HEARING).build())
                    .generalAppRespondentSolicitors(respondentSolicitors())
                    .generalAppApplnSolicitor(GASolicitorDetailsGAspec.builder()
                                                  .email(DUMMY_EMAIL).build())
                    .businessProcess(BusinessProcess.builder().camundaEvent(JUDGES_DECISION).build())
                    .generalAppParentCaseLink(GeneralAppParentCaseLink.builder()
                                                  .caseReference(CASE_REFERENCE.toString()).build())
                    .judicialDecisionMakeAnOrderForWrittenRepresentations(
                        GAJudicialWrittenRepresentations.builder().writtenOption(
                            GAJudgeWrittenRepresentationsOptions.SEQUENTIAL_REPRESENTATIONS).build())
                    .generalAppType(GAApplicationType.builder()
                                        .types(applicationTypeSummeryJudgement()).build())
                    .judicialConcurrentDateText(DUMMY_DATE)
                    .generalAppPBADetails(GAPbaDetails.builder().build())
                    .build();
        }

        private CaseData caseDataForApplicationsApprovedWhenRespondentsAreInList(YesOrNo orderAgreement,
                                                                                 YesOrNo isWithNotice) {
            return
                CaseData.builder()
                    .ccdCaseReference(CASE_REFERENCE)
                    .isMultiParty(NO)
                    .generalAppRespondentSolicitors(respondentSolicitors())
                    .generalAppRespondentAgreement(GARespondentOrderAgreement.builder()
                                                       .hasAgreed(orderAgreement).build())
                    .generalAppInformOtherParty(GAInformOtherParty.builder().isWithNotice(isWithNotice).build())
                    .generalAppApplnSolicitor(GASolicitorDetailsGAspec.builder()
                                                  .email(DUMMY_EMAIL).build())
                    .businessProcess(BusinessProcess.builder().camundaEvent(JUDGES_DECISION).build())
                    .generalAppParentCaseLink(GeneralAppParentCaseLink.builder()
                                                  .caseReference(CASE_REFERENCE.toString()).build())
                    .judicialDecisionMakeOrder(GAJudicialMakeAnOrder.builder()
                                                   .makeAnOrder(
                                                       GAJudgeMakeAnOrderOption.APPROVE_OR_EDIT).build())
                    .generalAppType(GAApplicationType.builder()
                                        .types(applicationTypeSummeryJudgement()).build())
                    .judicialConcurrentDateText(DUMMY_DATE)
                    .generalAppPBADetails(GAPbaDetails.builder().build())
                    .build();
        }

        private CaseData caseDataForListForHearingRespondentsAreInList(YesOrNo hasAgreed, YesOrNo isWithNotice) {
            return
                CaseData.builder()
                    .ccdCaseReference(CASE_REFERENCE)
                    .generalAppRespondentSolicitors(respondentSolicitors())
                    .generalAppRespondentAgreement(GARespondentOrderAgreement.builder().hasAgreed(hasAgreed).build())
                    .generalAppInformOtherParty(GAInformOtherParty.builder().isWithNotice(isWithNotice).build())
                    .generalAppApplnSolicitor(GASolicitorDetailsGAspec.builder()
                                                  .email(DUMMY_EMAIL).build())
                    .businessProcess(BusinessProcess.builder().camundaEvent(JUDGES_DECISION).build())
                    .generalAppParentCaseLink(GeneralAppParentCaseLink.builder()
                                                  .caseReference(CASE_REFERENCE.toString()).build())
                    .judicialDecision(GAJudicialDecision.builder()
                                          .decision(GAJudgeDecisionOption.LIST_FOR_A_HEARING).build())
                    .generalAppType(GAApplicationType.builder()
                                        .types(applicationTypeSummeryJudgement()).build())
                    .judicialConcurrentDateText(DUMMY_DATE)
                    .isMultiParty(NO)
                    .build();
        }

        private CaseData caseDataForCaseDismissedByJudgeRespondentsAreInList(YesOrNo orderAgreement,
                                                                             YesOrNo isWithNotice,
                                                                             YesOrNo isCloaked) {
            return
                CaseData.builder()
                    .ccdCaseReference(CASE_REFERENCE)
                    .generalAppRespondentAgreement(GARespondentOrderAgreement.builder()
                                                       .hasAgreed(orderAgreement).build())
                    .generalAppInformOtherParty(GAInformOtherParty.builder().isWithNotice(isWithNotice).build())
                    .applicationIsCloaked(isCloaked)
                    .generalAppRespondentSolicitors(respondentSolicitors())
                    .generalAppApplnSolicitor(GASolicitorDetailsGAspec.builder()
                                                  .email(DUMMY_EMAIL).build())
                    .businessProcess(BusinessProcess.builder().camundaEvent(JUDGES_DECISION).build())
                    .generalAppParentCaseLink(GeneralAppParentCaseLink.builder()
                                                  .caseReference(CASE_REFERENCE.toString()).build())
                    .judicialDecisionMakeOrder(GAJudicialMakeAnOrder.builder().makeAnOrder(
                        GAJudgeMakeAnOrderOption.DISMISS_THE_APPLICATION
                    ).build())
                    .generalAppType(GAApplicationType.builder()
                                        .types(applicationTypeSummeryJudgement()).build())
                    .judicialConcurrentDateText(DUMMY_DATE)
                    .generalAppPBADetails(GAPbaDetails.builder().build())
                    .isMultiParty(NO)
                    .build();
        }

        private CaseData caseDataForApprovedCloakStrikeOutWhenRespondentsArePresentInList(String superClaimType) {
            return
                CaseData.builder()
                    .ccdCaseReference(CASE_REFERENCE)
                    .generalAppRespondentSolicitors(respondentSolicitors())
                    .generalAppSuperClaimType(superClaimType)
                    .generalAppApplnSolicitor(GASolicitorDetailsGAspec.builder()
                                                  .email(DUMMY_EMAIL).build())
                    .businessProcess(BusinessProcess.builder().camundaEvent(JUDGES_DECISION).build())
                    .generalAppParentCaseLink(GeneralAppParentCaseLink.builder()
                                                  .caseReference(CASE_REFERENCE.toString()).build())
                    .judicialDecisionMakeOrder(GAJudicialMakeAnOrder.builder().makeAnOrder(
                        GAJudgeMakeAnOrderOption.APPROVE_OR_EDIT
                    ).build())
                    .judicialDecision(GAJudicialDecision.builder()
                                          .decision(GAJudgeDecisionOption.MAKE_AN_ORDER).build())
                    .generalAppType(GAApplicationType.builder()
                                        .types(applicationTypeToStrikeOut()).build())
                    .applicationIsCloaked(YES)
                    .judicialConcurrentDateText(DUMMY_DATE)
                    .build();
        }

        private CaseData caseDataForJudgeApprovedOrderCloakWhenRespondentsArePresentInList() {
            return
                CaseData.builder()
                    .ccdCaseReference(CASE_REFERENCE)
                    .generalAppRespondentSolicitors(respondentSolicitors())
                    .generalAppApplnSolicitor(GASolicitorDetailsGAspec.builder()
                                                  .email(DUMMY_EMAIL).build())
                    .businessProcess(BusinessProcess.builder().camundaEvent(JUDGES_DECISION).build())
                    .generalAppParentCaseLink(GeneralAppParentCaseLink.builder()
                                                  .caseReference(CASE_REFERENCE.toString()).build())
                    .judicialDecisionMakeOrder(GAJudicialMakeAnOrder.builder().makeAnOrder(
                        GAJudgeMakeAnOrderOption.APPROVE_OR_EDIT
                    ).build())
                    .judicialDecision(GAJudicialDecision.builder()
                                          .decision(GAJudgeDecisionOption.MAKE_AN_ORDER).build())
                    .generalAppType(GAApplicationType.builder()
                                        .types(applicationTypeSummeryJudgement()).build())
                    .applicationIsCloaked(YES)
                    .judicialConcurrentDateText(DUMMY_DATE)
                    .build();
        }

        private CaseData caseDataForJudgeDismissTheApplicationCloakWhenRespondentsArePresentInList() {
            return
                CaseData.builder()
                    .ccdCaseReference(CASE_REFERENCE)
                    .generalAppRespondentSolicitors(respondentSolicitors())
                    .generalAppApplnSolicitor(GASolicitorDetailsGAspec.builder()
                                                  .email(DUMMY_EMAIL).build())
                    .businessProcess(BusinessProcess.builder().camundaEvent(JUDGES_DECISION).build())
                    .generalAppParentCaseLink(GeneralAppParentCaseLink.builder()
                                                  .caseReference(CASE_REFERENCE.toString()).build())
                    .judicialDecisionMakeOrder(GAJudicialMakeAnOrder.builder().makeAnOrder(
                        GAJudgeMakeAnOrderOption.DISMISS_THE_APPLICATION
                    ).build())
                    .judicialDecision(GAJudicialDecision.builder()
                                          .decision(GAJudgeDecisionOption.MAKE_AN_ORDER).build())
                    .generalAppType(GAApplicationType.builder()
                                        .types(applicationTypeSummeryJudgement()).build())
                    .applicationIsCloaked(YES)
                    .judicialConcurrentDateText(DUMMY_DATE)
                    .build();
        }

        private Map<String, String> notificationPropertiesSummeryJudgement() {
            return Map.of(
                NotificationDataGA.CASE_REFERENCE, CASE_REFERENCE.toString(),
                NotificationDataGA.GA_APPLICATION_TYPE,
                GeneralApplicationTypes.SUMMARY_JUDGEMENT.getDisplayedValue()
            );
        }

        private List<GeneralApplicationTypes> applicationTypeSummeryJudgement() {
            return List.of(
                GeneralApplicationTypes.SUMMARY_JUDGEMENT
            );
        }

        private Map<String, String> notificationPropertiesSummeryJudgementConcurrent() {
            HashMap<String, String> properties = new HashMap<>(Map.of(
                NotificationDataGA.CASE_REFERENCE, CASE_REFERENCE.toString(),
                NotificationDataGA.GENAPP_REFERENCE, CASE_REFERENCE.toString(),
                NotificationDataGA.GA_APPLICATION_TYPE,
                GeneralApplicationTypes.SUMMARY_JUDGEMENT.getDisplayedValue(),
                NotificationDataGA.PARTY_REFERENCE, PARTY_REFERENCE
            ));
            properties.put(NotificationDataGA.WELSH_CONTACT, "E-bost: ymholiadaucymraeg@justice.gov.uk");
            properties.put(NotificationData.WELSH_HMCTS_SIGNATURE, "Hawliadau am Arian yn y Llys Sifil Ar-lein \n Gwasanaeth Llysoedd a Thribiwnlysoedd EF");
            properties.put(NotificationData.WELSH_OPENING_HOURS, "Dydd Llun i ddydd Iau, 9am – 5pm, dydd Gwener, 9am – 4.30pm");
            properties.put(NotificationData.WELSH_PHONE_CONTACT, "Ffôn: 0300 303 5174");
            properties.put(NotificationDataGA.SPEC_CONTACT, "Email: contactocmc@justice.gov.uk");
            properties.put(NotificationData.SPEC_UNSPEC_CONTACT, RAISE_QUERY_LR);
            properties.put(NotificationData.HMCTS_SIGNATURE, "Online Civil Claims \n HM Courts & Tribunal Service");
            properties.put(NotificationData.OPENING_HOURS, "Monday to Friday, 8.30am to 5pm");
            properties.put(NotificationData.PHONE_CONTACT, "For anything related to hearings, call 0300 123 5577 "
                + "\n For all other matters, call 0300 123 7050");
            return properties;
        }

        private Map<String, String> notificationPropertiesToStrikeOut() {
            HashMap<String, String> properties = new HashMap<>(Map.of(
                NotificationDataGA.CASE_REFERENCE, CASE_REFERENCE.toString(),
                NotificationDataGA.GENAPP_REFERENCE, CASE_REFERENCE.toString(),
                NotificationDataGA.GA_APPLICATION_TYPE,
                GeneralApplicationTypes.STRIKE_OUT.getDisplayedValue(),
                NotificationDataGA.PARTY_REFERENCE, PARTY_REFERENCE
            ));
            properties.put(NotificationDataGA.WELSH_CONTACT, "E-bost: ymholiadaucymraeg@justice.gov.uk");
            properties.put(NotificationData.WELSH_HMCTS_SIGNATURE, "Hawliadau am Arian yn y Llys Sifil Ar-lein \n Gwasanaeth Llysoedd a Thribiwnlysoedd EF");
            properties.put(NotificationData.WELSH_OPENING_HOURS, "Dydd Llun i ddydd Iau, 9am – 5pm, dydd Gwener, 9am – 4.30pm");
            properties.put(NotificationData.WELSH_PHONE_CONTACT, "Ffôn: 0300 303 5174");
            properties.put(NotificationDataGA.SPEC_CONTACT, "Email: contactocmc@justice.gov.uk");
            properties.put(NotificationData.SPEC_UNSPEC_CONTACT, RAISE_QUERY_LR);
            properties.put(NotificationData.HMCTS_SIGNATURE, "Online Civil Claims \n HM Courts & Tribunal Service");
            properties.put(NotificationData.OPENING_HOURS, "Monday to Friday, 8.30am to 5pm");
            properties.put(NotificationData.PHONE_CONTACT, "For anything related to hearings, call 0300 123 5577 "
                + "\n For all other matters, call 0300 123 7050");
            return properties;
        }

        private List<GeneralApplicationTypes> applicationTypeToStrikeOut() {
            return List.of(
                GeneralApplicationTypes.STRIKE_OUT
            );
        }

        private Map<String, String> notificationPropertiesToExtendTime() {
            HashMap<String, String> properties = new HashMap<>(Map.of(
                NotificationDataGA.CASE_REFERENCE, CASE_REFERENCE.toString(),
                NotificationDataGA.GA_APPLICATION_TYPE,
                GeneralApplicationTypes.EXTEND_TIME.getDisplayedValue()
            ));
            properties.put(NotificationDataGA.WELSH_CONTACT, "E-bost: ymholiadaucymraeg@justice.gov.uk");
            properties.put(NotificationData.WELSH_HMCTS_SIGNATURE, "Hawliadau am Arian yn y Llys Sifil Ar-lein \n Gwasanaeth Llysoedd a Thribiwnlysoedd EF");
            properties.put(NotificationData.WELSH_OPENING_HOURS, "Dydd Llun i ddydd Iau, 9am – 5pm, dydd Gwener, 9am – 4.30pm");
            properties.put(NotificationData.WELSH_PHONE_CONTACT, "Ffôn: 0300 303 5174");
            properties.put(NotificationDataGA.SPEC_CONTACT, "Email: contactocmc@justice.gov.uk");
            properties.put(NotificationData.SPEC_UNSPEC_CONTACT, "Email for Specified Claims: contactocmc@justice.gov.uk "
                + "\n Email for Damages Claims: damagesclaims@justice.gov.uk");
            properties.put(NotificationData.HMCTS_SIGNATURE, "Online Civil Claims \n HM Courts & Tribunal Service");
            properties.put(NotificationData.OPENING_HOURS, "Monday to Friday, 8.30am to 5pm");
            properties.put(NotificationData.PHONE_CONTACT, "For anything related to hearings, call 0300 123 5577 "
                + "\n For all other matters, call 0300 123 7050");
            return properties;
        }

        private List<GeneralApplicationTypes> applicationTypeToExtendTheClaim() {
            return List.of(
                GeneralApplicationTypes.EXTEND_TIME
            );
        }

        private Map<String, String> notificationPropertiesToAmendStatementOfCase() {
            HashMap<String, String> properties = new HashMap<>(Map.of(
                NotificationDataGA.CASE_REFERENCE, CASE_REFERENCE.toString(),
                NotificationDataGA.GENAPP_REFERENCE, CASE_REFERENCE.toString(),
                NotificationDataGA.GA_APPLICATION_TYPE,
                GeneralApplicationTypes.AMEND_A_STMT_OF_CASE.getDisplayedValue(),
                NotificationDataGA.PARTY_REFERENCE, PARTY_REFERENCE
            ));
            properties.put(NotificationDataGA.WELSH_CONTACT, "E-bost: ymholiadaucymraeg@justice.gov.uk");
            properties.put(NotificationData.WELSH_HMCTS_SIGNATURE, "Hawliadau am Arian yn y Llys Sifil Ar-lein \n Gwasanaeth Llysoedd a Thribiwnlysoedd EF");
            properties.put(NotificationData.WELSH_OPENING_HOURS, "Dydd Llun i ddydd Iau, 9am – 5pm, dydd Gwener, 9am – 4.30pm");
            properties.put(NotificationData.WELSH_PHONE_CONTACT, "Ffôn: 0300 303 5174");
            properties.put(NotificationDataGA.SPEC_CONTACT, "Email: contactocmc@justice.gov.uk");
            properties.put(NotificationData.SPEC_UNSPEC_CONTACT, RAISE_QUERY_LR);
            properties.put(NotificationData.HMCTS_SIGNATURE, "Online Civil Claims \n HM Courts & Tribunal Service");
            properties.put(NotificationData.OPENING_HOURS, "Monday to Friday, 8.30am to 5pm");
            properties.put(NotificationData.PHONE_CONTACT, "For anything related to hearings, call 0300 123 5577 "
                + "\n For all other matters, call 0300 123 7050");
            return properties;
        }

        private List<GeneralApplicationTypes> applicationTypeToAmendStatmentOfClaim() {
            return List.of(
                GeneralApplicationTypes.AMEND_A_STMT_OF_CASE
            );
        }

        private Map<String, String> notificationPropertiesToGetReliefFromSanctions() {
            HashMap<String, String> properties = new HashMap<>(Map.of(
                NotificationDataGA.CASE_REFERENCE, CASE_REFERENCE.toString(),
                NotificationDataGA.GENAPP_REFERENCE, CASE_REFERENCE.toString(),
                NotificationDataGA.GA_APPLICATION_TYPE,
                GeneralApplicationTypes.RELIEF_FROM_SANCTIONS.getDisplayedValue(),
                NotificationDataGA.PARTY_REFERENCE, PARTY_REFERENCE
            ));
            properties.put(NotificationDataGA.WELSH_CONTACT, "E-bost: ymholiadaucymraeg@justice.gov.uk");
            properties.put(NotificationData.WELSH_HMCTS_SIGNATURE, "Hawliadau am Arian yn y Llys Sifil Ar-lein \n Gwasanaeth Llysoedd a Thribiwnlysoedd EF");
            properties.put(NotificationData.WELSH_OPENING_HOURS, "Dydd Llun i ddydd Iau, 9am – 5pm, dydd Gwener, 9am – 4.30pm");
            properties.put(NotificationData.WELSH_PHONE_CONTACT, "Ffôn: 0300 303 5174");
            properties.put(NotificationDataGA.SPEC_CONTACT, "Email: contactocmc@justice.gov.uk");
            properties.put(NotificationData.SPEC_UNSPEC_CONTACT, RAISE_QUERY_LR);
            properties.put(NotificationData.HMCTS_SIGNATURE, "Online Civil Claims \n HM Courts & Tribunal Service");
            properties.put(NotificationData.OPENING_HOURS, "Monday to Friday, 8.30am to 5pm");
            properties.put(NotificationData.PHONE_CONTACT, "For anything related to hearings, call 0300 123 5577 "
                + "\n For all other matters, call 0300 123 7050");
            return properties;
        }

        private List<GeneralApplicationTypes> applicationTypeToGetReliefFromSanctions() {
            return List.of(
                GeneralApplicationTypes.RELIEF_FROM_SANCTIONS
            );
        }

    }

    @Nested
    class RequestMoreInformation {

        @Test
        void notificationShouldSend_IfWithNotice() {
            CaseData caseData = caseDataForJudicialRequestForInformationOfApplication(NO, YES, NO,
                                                                                      REQUEST_MORE_INFORMATION);
            when(caseDetailsConverter.toCaseDataGA(any())).thenReturn(CaseData.builder().ccdState(CaseState.CASE_PROGRESSION).build());
            when(solicitorEmailValidation.validateSolicitorEmail(any(), any())).thenReturn(caseData);

            judicialRespondentNotificationService.sendNotification(caseData, RESPONDENT);

            verify(notificationService, times(2)).sendMail(
                DUMMY_EMAIL,
                "general-application-apps-judicial-notification-template-id",
                notificationPropertiesToStayTheClaim(),
                "general-apps-judicial-notification-make-decision-" + CASE_REFERENCE
            );
        }

        @Test
        void shouldNotSendAdditionalPaymentNotification_UncloakedApplication_BeforeAdditionalPaymentMade() {

            CaseData caseData = caseDataForJudicialRequestForInformationOfApplication(NO, NO, NO,
                                                                                      SEND_APP_TO_OTHER_PARTY);
            when(solicitorEmailValidation.validateSolicitorEmail(any(), any()))
                .thenReturn(caseData);

            judicialRespondentNotificationService.sendNotification(caseData, RESPONDENT);

            verifyNoInteractions(notificationService);
        }

        @Test
        void shouldSendNotification_UncloakedApplication_AfterAdditionalPaymentMade() {

            CaseData caseData = caseDataForJudicialRequestForInformationOfApplication(NO, NO, NO,
                                                                                      SEND_APP_TO_OTHER_PARTY
            ).toBuilder()
                .ccdState(CaseState.APPLICATION_ADD_PAYMENT)
                .generalAppPBADetails(GAPbaDetails.builder()
                                          .additionalPaymentDetails(buildAdditionalPaymentSuccessData())
                                          .build())
                .build();

            when(caseDetailsConverter.toCaseDataGA(any())).thenReturn(CaseData.builder().ccdState(CaseState.CASE_PROGRESSION).build());
            when(solicitorEmailValidation.validateSolicitorEmail(any(), any()))
                .thenReturn(caseData);

            when(time.now()).thenReturn(responseDate);
            when(deadlinesCalculator.calculateApplicantResponseDeadline(
                any(LocalDateTime.class), any(Integer.class))).thenReturn(deadline);

            var responseCaseData = judicialRespondentNotificationService
                .sendNotification(caseData, RESPONDENT);

            assertThat(responseCaseData.getGeneralAppNotificationDeadlineDate())
                .isEqualTo(deadline.toString());
        }
    }

    private CaseData caseDataForJudicialRequestForInformationOfApplication(
        YesOrNo isRespondentOrderAgreement, YesOrNo isWithNotice, YesOrNo isCloaked,
        GAJudgeRequestMoreInfoOption gaJudgeRequestMoreInfoOption) {

        return CaseData.builder()
            .ccdCaseReference(CASE_REFERENCE)
            .ccdState(APPLICATION_SUBMITTED_AWAITING_JUDICIAL_DECISION)
            .generalAppRespondentSolicitors(respondentSolicitors())
            .applicationIsCloaked(isCloaked)
            .judicialDecision(GAJudicialDecision.builder()
                                  .decision(GAJudgeDecisionOption.REQUEST_MORE_INFO).build())
            .judicialDecisionRequestMoreInfo(GAJudicialRequestMoreInfo.builder()
                                                 .requestMoreInfoOption(gaJudgeRequestMoreInfoOption)
                                                 .judgeRequestMoreInfoText("Test")
                                                 .judgeRequestMoreInfoByDate(LocalDate.now())
                                                 .deadlineForMoreInfoSubmission(deadline).build())
            .generalAppRespondentAgreement(GARespondentOrderAgreement.builder()
                                               .hasAgreed(isRespondentOrderAgreement).build())
            .generalAppInformOtherParty(GAInformOtherParty.builder().isWithNotice(isWithNotice).build())
            .generalAppApplnSolicitor(GASolicitorDetailsGAspec.builder()
                                          .email(DUMMY_EMAIL).build())
            .businessProcess(BusinessProcess.builder().camundaEvent(JUDGES_DECISION).build())
            .generalAppParentCaseLink(GeneralAppParentCaseLink.builder()
                                          .caseReference(CASE_REFERENCE.toString()).build())
            .generalAppType(GAApplicationType.builder()
                                .types(applicationTypeToStayTheClaim()).build())
            .generalAppPBADetails(GAPbaDetails.builder().build())
            .isMultiParty(NO)
            .build();

    }

    private Map<String, String> notificationPropertiesToStayTheClaim() {
        HashMap<String, String> properties = new HashMap<>(Map.of(
            NotificationDataGA.CASE_REFERENCE, CASE_REFERENCE.toString(),
            NotificationDataGA.GENAPP_REFERENCE, CASE_REFERENCE.toString(),
            NotificationDataGA.GA_APPLICATION_TYPE,
            GeneralApplicationTypes.STAY_THE_CLAIM.getDisplayedValue(),
            NotificationDataGA.PARTY_REFERENCE, PARTY_REFERENCE
        ));
        properties.put(NotificationDataGA.WELSH_CONTACT, "E-bost: ymholiadaucymraeg@justice.gov.uk");
        properties.put(NotificationData.WELSH_HMCTS_SIGNATURE, "Hawliadau am Arian yn y Llys Sifil Ar-lein \n Gwasanaeth Llysoedd a Thribiwnlysoedd EF");
        properties.put(NotificationData.WELSH_OPENING_HOURS, "Dydd Llun i ddydd Iau, 9am – 5pm, dydd Gwener, 9am – 4.30pm");
        properties.put(NotificationData.WELSH_PHONE_CONTACT, "Ffôn: 0300 303 5174");
        properties.put(NotificationDataGA.SPEC_CONTACT, "Email: contactocmc@justice.gov.uk");
        properties.put(NotificationData.SPEC_UNSPEC_CONTACT, RAISE_QUERY_LR);
        properties.put(NotificationData.HMCTS_SIGNATURE, "Online Civil Claims \n HM Courts & Tribunal Service");
        properties.put(NotificationData.OPENING_HOURS, "Monday to Friday, 8.30am to 5pm");
        properties.put(NotificationData.PHONE_CONTACT, "For anything related to hearings, call 0300 123 5577 "
            + "\n For all other matters, call 0300 123 7050");
        return properties;
    }

    private List<Element<GASolicitorDetailsGAspec>> respondentSolicitors() {
        return Arrays.asList(element(GASolicitorDetailsGAspec.builder().id(ID)
                                         .email(DUMMY_EMAIL).organisationIdentifier(ORG_ID).build()),
                             element(GASolicitorDetailsGAspec.builder().id(ID)
                                         .email(DUMMY_EMAIL).organisationIdentifier(ORG_ID).build())
        );
    }

    private PaymentDetails buildAdditionalPaymentSuccessData() {
        return PaymentDetails.builder()
            .status(SUCCESS)
            .customerReference(null)
            .reference("123445")
            .errorCode(null)
            .errorMessage(null)
            .build();
    }

    private List<GeneralApplicationTypes> applicationTypeToStayTheClaim() {
        return List.of(
            GeneralApplicationTypes.STAY_THE_CLAIM
        );
    }
}
