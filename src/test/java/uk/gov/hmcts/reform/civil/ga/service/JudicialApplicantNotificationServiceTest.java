package uk.gov.hmcts.reform.civil.ga.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.annotation.DirtiesContext;
import uk.gov.hmcts.reform.civil.ga.handler.callback.camunda.notification.NotificationDataGA;
import uk.gov.hmcts.reform.civil.ga.model.GeneralApplicationCaseData;
import uk.gov.hmcts.reform.civil.ga.model.genapplication.GeneralApplicationPbaDetails;
import uk.gov.hmcts.reform.civil.notify.NotificationService;
import uk.gov.hmcts.reform.civil.notify.NotificationsSignatureConfiguration;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.enums.CaseState;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.ga.enums.dq.GAJudgeDecisionOption;
import uk.gov.hmcts.reform.civil.ga.enums.dq.GAJudgeMakeAnOrderOption;
import uk.gov.hmcts.reform.civil.ga.enums.dq.GAJudgeRequestMoreInfoOption;
import uk.gov.hmcts.reform.civil.ga.enums.dq.GAJudgeWrittenRepresentationsOptions;
import uk.gov.hmcts.reform.civil.enums.dq.GeneralApplicationTypes;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.model.BusinessProcess;
import uk.gov.hmcts.reform.civil.model.GeneralAppParentCaseLink;
import uk.gov.hmcts.reform.civil.model.PaymentDetails;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.model.genapplication.GAApplicationType;
import uk.gov.hmcts.reform.civil.model.genapplication.GAInformOtherParty;
import uk.gov.hmcts.reform.civil.ga.model.genapplication.GAJudicialDecision;
import uk.gov.hmcts.reform.civil.ga.model.genapplication.GAJudicialMakeAnOrder;
import uk.gov.hmcts.reform.civil.ga.model.genapplication.GAJudicialRequestMoreInfo;
import uk.gov.hmcts.reform.civil.ga.model.genapplication.GAJudicialWrittenRepresentations;
import uk.gov.hmcts.reform.civil.model.genapplication.GARespondentOrderAgreement;
import uk.gov.hmcts.reform.civil.model.genapplication.GASolicitorDetailsGAspec;
import uk.gov.hmcts.reform.civil.model.genapplication.GAUrgencyRequirement;
import uk.gov.hmcts.reform.civil.service.CoreCaseDataService;
import uk.gov.hmcts.reform.civil.service.DeadlinesCalculator;
import uk.gov.hmcts.reform.civil.service.Time;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.enums.CaseState.ORDER_MADE;
import static uk.gov.hmcts.reform.civil.enums.PaymentStatus.SUCCESS;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;
import static uk.gov.hmcts.reform.civil.ga.enums.dq.GAJudgeMakeAnOrderOption.APPROVE_OR_EDIT;
import static uk.gov.hmcts.reform.civil.ga.enums.dq.GAJudgeMakeAnOrderOption.GIVE_DIRECTIONS_WITHOUT_HEARING;
import static uk.gov.hmcts.reform.civil.ga.enums.dq.GAJudgeRequestMoreInfoOption.REQUEST_MORE_INFORMATION;
import static uk.gov.hmcts.reform.civil.ga.enums.dq.GAJudgeRequestMoreInfoOption.SEND_APP_TO_OTHER_PARTY;
import static uk.gov.hmcts.reform.civil.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.civil.utils.ElementUtils.wrapElements;
import static uk.gov.hmcts.reform.civil.ga.utils.EmailFooterUtils.RAISE_QUERY_LR;
import static uk.gov.hmcts.reform.civil.ga.utils.JudicialDecisionNotificationUtil.isNotificationCriteriaSatisfied;

@SpringBootTest(classes = {
    JudicialNotificationService.class,
    JacksonAutoConfiguration.class
})
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class JudicialApplicantNotificationServiceTest {

    @MockBean
    private NotificationsProperties notificationsProperties;
    @MockBean
    private Time time;
    @MockBean
    private DeadlinesCalculator deadlinesCalculator;
    @Autowired
    private JudicialNotificationService judicialNotificationService;

    @MockBean
    private NotificationService notificationService;

    @MockBean
    private SolicitorEmailValidation solicitorEmailValidation;

    @MockBean
    private CaseDetailsConverter caseDetailsConverter;

    @MockBean
    private CoreCaseDataService coreCaseDataService;

    @MockBean
    private JudicialDecisionHelper judicialDecisionHelper;

    @MockBean
    private FeatureToggleService featureToggleService;
    @MockBean
    private GaForLipService gaForLipService;

    @MockBean
    private NotificationsSignatureConfiguration configuration;

    private static final String APPLICANT = "applicant";
    private static final String RESPONDENT = "respondent";

    private static final String RESPONDENT_EMAIL = "respondent@email.com";

    private static final Long CASE_REFERENCE = 111111L;
    private static final String DUMMY_EMAIL = "hmcts.civil@gmail.com";
    private static final String DUMMY_DATE = "2022-11-12";
    private static final String ORG_ID = "1";
    private static final String ID = "1";
    private static final String SAMPLE_TEMPLATE = "general-application-apps-judicial-notification-template-id";
    private static final String SAMPLE_LIP_TEMPLATE = "general-application-apps-judicial-notification-template-lip-id";
    private static final String LIP_APPLN_TEMPLATE = "ga-judicial-notification-applicant-template-lip-id";
    private static final String LIP_APPLN_WELSH_TEMPLATE = "ga-judicial-notification-applicant-welsh-template-lip-id";
    private static final String JUDGES_DECISION = "MAKE_DECISION";
    private static final String PARTY_REFERENCE = "Claimant Reference: Not provided - Defendant Reference: Not provided";
    private final LocalDateTime responseDate = LocalDateTime.now();
    private final LocalDateTime deadline = LocalDateTime.now().plusDays(5);

    @BeforeEach
    void setup() {
        when(caseDetailsConverter.toGeneralApplicationCaseData(any())).thenReturn(GeneralApplicationCaseData.builder().ccdState(CaseState.CASE_PROGRESSION).build());
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
        when(notificationsProperties.getLipGeneralAppApplicantEmailTemplate())
            .thenReturn(LIP_APPLN_TEMPLATE);
        when(notificationsProperties.getLipGeneralAppApplicantEmailTemplateInWelsh())
            .thenReturn(LIP_APPLN_WELSH_TEMPLATE);
        when(notificationsProperties.getJudgeFreeFormOrderApplicantEmailTemplate())
            .thenReturn(SAMPLE_TEMPLATE);
        when(notificationsProperties.getJudgeFreeFormOrderRespondentEmailTemplate())
            .thenReturn(SAMPLE_TEMPLATE);
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
            when(gaForLipService.isLipApp(any())).thenReturn(true);
            when(configuration.getSpecUnspecContact()).thenReturn("Email for Specified Claims: contactocmc@justice.gov.uk "
                                                                      + "\n Email for Damages Claims: damagesclaims@justice.gov.uk");
        }

        public Map<String, String> customProp = new HashMap<>();

        @Test
        void sendNotificationApplicantConcurrentWrittenRep() {
            when(solicitorEmailValidation.validateSolicitorEmail(any(), any()))
                .thenReturn(caseDataForConcurrentWrittenOption(YES, NO));
            when(caseDetailsConverter.toGeneralApplicationCaseData(any())).thenReturn(GeneralApplicationCaseData.builder().build());
            when(gaForLipService.isLipApp(any())).thenReturn(true);
            judicialNotificationService.sendNotification(caseDataForConcurrentWrittenOption(YES, NO), APPLICANT);
            verify(notificationService, times(1)).sendMail(
                DUMMY_EMAIL,
                "ga-judicial-notification-applicant-template-lip-id",
                notificationPropertiesSummeryJudgement(),
                "general-apps-judicial-notification-make-decision-" + CASE_REFERENCE
            );
        }

        @Test
        void sendNotificationApplicantSequentialWrittenRep() {
            when(solicitorEmailValidation.validateSolicitorEmail(any(), any()))
                .thenReturn(caseDataForSequentialWrittenOption(YES, NO));
            when(caseDetailsConverter.toGeneralApplicationCaseData(any())).thenReturn(GeneralApplicationCaseData.builder().build());
            when(gaForLipService.isLipApp(any())).thenReturn(true);
            GeneralApplicationCaseData caseData = caseDataForSequentialWrittenOption(YES, NO);

            judicialNotificationService.sendNotification(caseData.toBuilder().applicantBilingualLanguagePreference(YES)
                                                             .build(), APPLICANT);
            verify(notificationService, times(1)).sendMail(
                DUMMY_EMAIL,
                "ga-judicial-notification-applicant-template-lip-id",
                notificationPropertiesSummeryJudgement(),
                "general-apps-judicial-notification-make-decision-" + CASE_REFERENCE
            );
        }

        @Test
        void sendNotificationInWelshApplicantSequentialWrittenRep() {
            when(solicitorEmailValidation.validateSolicitorEmail(any(), any()))
                .thenReturn(caseDataForSequentialWrittenOption(YES, NO).toBuilder()
                                .applicantBilingualLanguagePreference(YES).build());
            GeneralApplicationCaseData claimRespondentResponseLan = GeneralApplicationCaseData.builder().claimantBilingualLanguagePreference("WELSH")
                .applicantBilingualLanguagePreference(YES).build();
            when(caseDetailsConverter.toGeneralApplicationCaseData(any())).thenReturn(claimRespondentResponseLan);
            when(gaForLipService.isLipApp(any())).thenReturn(true);
            GeneralApplicationCaseData caseData = caseDataForSequentialWrittenOption(YES, NO);
            GeneralApplicationCaseData updatedCasedata = caseData.toBuilder().applicantBilingualLanguagePreference(YES)
                .build();
            judicialNotificationService.sendNotification(updatedCasedata, APPLICANT);
            verify(notificationService, times(1)).sendMail(
                DUMMY_EMAIL,
                LIP_APPLN_WELSH_TEMPLATE,
                notificationPropertiesSummeryJudgement(),
                "general-apps-judicial-notification-make-decision-" + CASE_REFERENCE
            );
        }

        @Test
        void notificationUncloakShouldSendForDismissal_ApplicantLIP() {
            when(caseDetailsConverter.toGeneralApplicationCaseData(any())).thenReturn(GeneralApplicationCaseData.builder().ccdState(CaseState.CASE_PROGRESSION).build());
            when(solicitorEmailValidation.validateSolicitorEmail(any(), any()))
                .thenReturn(caseDataForJudgeDismissal(NO, NO, NO, YES, NO));
            when(gaForLipService.isLipApp(any())).thenReturn(true);

            judicialNotificationService.sendNotification(caseDataForJudgeDismissal(NO, NO, NO,  YES, NO), APPLICANT);

            verify(notificationService, times(1)).sendMail(
                DUMMY_EMAIL,
                "ga-judicial-notification-applicant-template-lip-id",
                notificationPropertiesSummeryJudgement(),
                "general-apps-judicial-notification-make-decision-" + CASE_REFERENCE
            );
        }

        @Test
        void notificationCloakShouldSendForDismissal_ApplicantLIP() {
            when(caseDetailsConverter.toGeneralApplicationCaseData(any())).thenReturn(GeneralApplicationCaseData.builder().ccdState(CaseState.CASE_PROGRESSION).build());
            when(solicitorEmailValidation.validateSolicitorEmail(any(), any()))
                .thenReturn(caseDataForJudgeDismissal(NO, NO, YES, YES, NO));
            when(gaForLipService.isLipApp(any())).thenReturn(true);

            judicialNotificationService.sendNotification(caseDataForJudgeDismissal(NO, NO, NO,  YES, NO), APPLICANT);

            verify(notificationService, times(1)).sendMail(
                DUMMY_EMAIL,
                "ga-judicial-notification-applicant-template-lip-id",
                notificationPropertiesSummeryJudgement(),
                "general-apps-judicial-notification-make-decision-" + CASE_REFERENCE
            );
        }

        @Test
        void notificationInWelshShouldSendForDismissal_ApplicantLIP() {
            GeneralApplicationCaseData claimRespondentResponseLan = GeneralApplicationCaseData.builder().claimantBilingualLanguagePreference("WELSH")
                .applicantBilingualLanguagePreference(YES).build();
            when(caseDetailsConverter.toGeneralApplicationCaseData(any())).thenReturn(claimRespondentResponseLan);
            GeneralApplicationCaseData updatedCaseData = caseDataForJudgeDismissal(NO, NO, NO, YES, NO).toBuilder().applicantBilingualLanguagePreference(YES).build();
            when(solicitorEmailValidation.validateSolicitorEmail(any(), any()))
                .thenReturn(updatedCaseData);
            when(gaForLipService.isLipApp(any())).thenReturn(true);

            judicialNotificationService.sendNotification(updatedCaseData, APPLICANT);

            verify(notificationService, times(1)).sendMail(
                DUMMY_EMAIL,
                "ga-judicial-notification-applicant-welsh-template-lip-id",
                notificationPropertiesSummeryJudgement(),
                "general-apps-judicial-notification-make-decision-" + CASE_REFERENCE
            );
        }

        @Test
        void notificationShouldSend_LipRespondent_When_JudicialDirectionOrderRep_unCloaks() {

            GeneralApplicationCaseData caseData
                = caseDataForJudicialDirectionOrderOfApplicationWhenRespondentsArePresentInList(NO,
                                                                                                NO, NO, YES, YES, NO);

            when(solicitorEmailValidation.validateSolicitorEmail(any(), any()))
                .thenReturn(caseData);
            when(caseDetailsConverter.toGeneralApplicationCaseData(any())).thenReturn(GeneralApplicationCaseData.builder().ccdCaseReference(CASE_REFERENCE).build());
            when(gaForLipService.isLipApp(any())).thenReturn(true);
            judicialNotificationService.sendNotification(caseData, APPLICANT);

            verify(notificationService, times(1)).sendMail(
                DUMMY_EMAIL, "ga-judicial-notification-applicant-template-lip-id",
                notificationPropertiesSummeryJudgement(), "general-apps-judicial-notification-make-decision-" + CASE_REFERENCE
            );
        }

        @Test
        void notificationShouldSend_LipRespondent_When_JudicialDirectionOrderRep() {

            GeneralApplicationCaseData caseData
                = caseDataForJudicialDirectionOrderOfApplicationWhenRespondentsArePresentInList(NO,
                                                                                                NO, YES, YES, YES, NO);
            when(caseDetailsConverter.toGeneralApplicationCaseData(any())).thenReturn(GeneralApplicationCaseData.builder().ccdState(CaseState.CASE_PROGRESSION).build());
            when(gaForLipService.isLipApp(any())).thenReturn(true);
            when(solicitorEmailValidation.validateSolicitorEmail(any(), any()))
                .thenReturn(caseData);

            judicialNotificationService.sendNotification(caseData, APPLICANT);

            verify(notificationService, times(1)).sendMail(
                DUMMY_EMAIL, "ga-judicial-notification-applicant-template-lip-id",
                notificationPropertiesSummeryJudgement(), "general-apps-judicial-notification-make-decision-" + CASE_REFERENCE
            );
        }

        @Test
        void notificationShouldSendSendToLipRespondent_IfApplicationUncloakedForApproveOrEdit() {
            when(caseDetailsConverter.toGeneralApplicationCaseData(any())).thenReturn(GeneralApplicationCaseData.builder().ccdState(CaseState.CASE_PROGRESSION).build());
            when(solicitorEmailValidation.validateSolicitorEmail(any(), any()))
                .thenReturn(caseDataWithSolicitorDataOnlyForApplicationUncloakedJudgeApproveOrEdit(
                    YES, NO, NO));
            when(gaForLipService.isLipApp(any())).thenReturn(true);
            judicialNotificationService.sendNotification(
                caseDataWithSolicitorDataOnlyForApplicationUncloakedJudgeApproveOrEdit(
                    YES, NO, NO), APPLICANT);

            verify(notificationService, times(1)).sendMail(
                DUMMY_EMAIL,
                "ga-judicial-notification-applicant-template-lip-id",
                notificationPropertiesSummeryJudgement(),
                "general-apps-judicial-notification-make-decision-" + CASE_REFERENCE
            );
        }

        @Test
        void notificationShouldSendSendToLipRespondent_IfApplicationForApproveOrEdit() {
            when(caseDetailsConverter.toGeneralApplicationCaseData(any())).thenReturn(GeneralApplicationCaseData.builder().ccdState(CaseState.CASE_PROGRESSION).build());
            when(solicitorEmailValidation.validateSolicitorEmail(any(), any()))
                .thenReturn(caseDataWithSolicitorDataOnlyForApplicationUncloakedJudgeApproveOrEdit(
                    YES, NO, YES));
            when(gaForLipService.isLipApp(any())).thenReturn(true);
            judicialNotificationService.sendNotification(
                caseDataWithSolicitorDataOnlyForApplicationUncloakedJudgeApproveOrEdit(
                    YES, NO, YES), APPLICANT);

            verify(notificationService, times(1)).sendMail(
                DUMMY_EMAIL,
                "ga-judicial-notification-applicant-template-lip-id",
                notificationPropertiesSummeryJudgement(),
                "general-apps-judicial-notification-make-decision-" + CASE_REFERENCE
            );
        }

        @Test
        void shouldSendNotification_LipRespondent_UncloakedApplication() {

            GeneralApplicationCaseData caseData
                = caseDataForJudicialRequestForInformationOfApplication(NO, NO, NO,
                                                                        SEND_APP_TO_OTHER_PARTY)
                .toBuilder().generalAppType(new GAApplicationType(applicationTypeSummeryJudgement())).build();

            when(solicitorEmailValidation.validateSolicitorEmail(any(), any()))
                .thenReturn(caseData);
            when(gaForLipService.isLipApp(any())).thenReturn(true);
            when(caseDetailsConverter.toGeneralApplicationCaseData(any())).thenReturn(GeneralApplicationCaseData.builder().build());

            judicialNotificationService.sendNotification(caseData, APPLICANT);

            verify(notificationService, times(1)).sendMail(
                DUMMY_EMAIL,
                "ga-judicial-notification-applicant-template-lip-id",
                notificationPropertiesSummeryJudgement(),
                "general-apps-judicial-notification-make-decision-" + CASE_REFERENCE
            );
        }

        @Test
        void shouldSendNotification_LipRespondent_Application() {

            GeneralApplicationCaseData caseData
                = caseDataForJudicialRequestForInformationOfApplication(NO, NO, YES,
                                                                        SEND_APP_TO_OTHER_PARTY)
                .toBuilder().generalAppType(new GAApplicationType(applicationTypeSummeryJudgement())).build();

            when(solicitorEmailValidation.validateSolicitorEmail(any(), any()))
                .thenReturn(caseData);
            when(caseDetailsConverter.toGeneralApplicationCaseData(any())).thenReturn(GeneralApplicationCaseData.builder().build());
            when(gaForLipService.isLipApp(any())).thenReturn(true);
            judicialNotificationService.sendNotification(caseData, APPLICANT);

            verify(notificationService, times(1)).sendMail(
                DUMMY_EMAIL,
                "ga-judicial-notification-applicant-template-lip-id",
                notificationPropertiesSummeryJudgement(),
                "general-apps-judicial-notification-make-decision-" + CASE_REFERENCE
            );
        }

        @Test
        void shouldSendNotification_LipRespondent_Application_WhenRequestMoreInfo() {

            GeneralApplicationCaseData caseData
                = caseDataForJudicialRequestForInformationOfApplication(YES, YES, YES,
                                                                        REQUEST_MORE_INFORMATION)
                .toBuilder()
                .generalAppConsentOrder(NO)
                .ccdState(CaseState.APPLICATION_ADD_PAYMENT).generalAppType(new GAApplicationType(applicationTypeVaryOrder())).build();

            when(solicitorEmailValidation.validateSolicitorEmail(any(), any()))
                .thenReturn(caseData);
            when(caseDetailsConverter.toGeneralApplicationCaseData(any())).thenReturn(caseData);
            when(gaForLipService.isLipResp(any())).thenReturn(true);

            judicialNotificationService.sendNotification(caseData, RESPONDENT);

            verify(notificationService, times(1)).sendMail(
                DUMMY_EMAIL,

                "general-application-apps-judicial-notification-template-lip-id",
                notificationPropertiesVaryOrder(),
                "general-apps-judicial-notification-make-decision-" + CASE_REFERENCE
            );
        }

        @Test
        void shouldSendNotification_LipRespondent_Application_WhenRequestMoreInfo_WhenNoLIpInvolved() {

            GeneralApplicationCaseData caseData
                = caseDataForJudicialRequestForInformationOfApplication(YES, YES, YES,
                                                                        REQUEST_MORE_INFORMATION)
                .toBuilder()
                .generalAppConsentOrder(NO)
                .ccdState(CaseState.APPLICATION_ADD_PAYMENT).generalAppType(new GAApplicationType(applicationTypeVaryOrder())).build();

            when(solicitorEmailValidation.validateSolicitorEmail(any(), any()))
                .thenReturn(caseData);
            when(caseDetailsConverter.toGeneralApplicationCaseData(any())).thenReturn(caseData);
            when(gaForLipService.isLipResp(any())).thenReturn(false);

            judicialNotificationService.sendNotification(caseData, RESPONDENT);

            verify(notificationService, times(1)).sendMail(
                DUMMY_EMAIL,

                "general-application-apps-judicial-notification-template-id",
                notificationPropertiesVaryOrder(),
                "general-apps-judicial-notification-make-decision-" + CASE_REFERENCE
            );
        }

        @Test
        void notificationShouldSendListForHearing() {

            when(solicitorEmailValidation.validateSolicitorEmail(any(), any()))
                .thenReturn(caseDataListForHearing());
            when(caseDetailsConverter.toGeneralApplicationCaseData(any())).thenReturn(GeneralApplicationCaseData.builder().ccdCaseReference(CASE_REFERENCE).build());
            when(gaForLipService.isLipApp(any())).thenReturn(true);
            judicialNotificationService.sendNotification(caseDataListForHearing(), APPLICANT);
            verify(notificationService).sendMail(
                DUMMY_EMAIL,
                "ga-judicial-notification-applicant-template-lip-id",
                notificationPropertiesSummeryJudgement(),
                "general-apps-judicial-notification-make-decision-" + CASE_REFERENCE
            );
        }

        @Test
        void notificationShouldSendApplicantForFreeFormOrder() {

            when(solicitorEmailValidation.validateSolicitorEmail(any(), any()))
                .thenReturn(caseDataFreeFormOrder());
            when(caseDetailsConverter.toGeneralApplicationCaseData(any())).thenReturn(GeneralApplicationCaseData.builder().build());
            when(gaForLipService.isLipApp(any())).thenReturn(true);
            judicialNotificationService.sendNotification(caseDataFreeFormOrder(), APPLICANT);
            verify(notificationService).sendMail(
                DUMMY_EMAIL,
                "ga-judicial-notification-applicant-template-lip-id",
                notificationPropertiesSummeryJudgement(),
                "general-apps-judicial-notification-make-decision-" + CASE_REFERENCE
            );
        }

        @Test
        void notificationShouldSendRespondentForFreeFormOrder() {

            when(solicitorEmailValidation.validateSolicitorEmail(any(), any()))
                .thenReturn(caseDataFreeFormOrder());
            when(caseDetailsConverter.toGeneralApplicationCaseData(any())).thenReturn(GeneralApplicationCaseData.builder().build());
            when(gaForLipService.isLipResp(any())).thenReturn(true);
            judicialNotificationService.sendNotification(caseDataFreeFormOrder(), RESPONDENT);
            verify(notificationService).sendMail(
                DUMMY_EMAIL,
                "general-application-apps-judicial-notification-template-lip-id",
                notificationPropertiesSummeryJudgement(),
                "general-apps-judicial-notification-make-decision-" + CASE_REFERENCE
            );
        }

        @Test
        void notificationShouldSendRespondentForFreeFormOrder_ifNotWithNoticeOrConsent() {
            GeneralApplicationCaseData caseData = caseDataFreeFormOrder();
            caseData = caseData.toBuilder()
                .generalAppInformOtherParty(new GAInformOtherParty())
                .generalAppRespondentAgreement(new GARespondentOrderAgreement().setHasAgreed(NO)).build();
            when(solicitorEmailValidation.validateSolicitorEmail(any(), any()))
                .thenReturn(caseData);
            when(caseDetailsConverter.toGeneralApplicationCaseData(any())).thenReturn(GeneralApplicationCaseData.builder().build());
            when(gaForLipService.isLipResp(any())).thenReturn(true);
            judicialNotificationService.sendNotification(caseData, RESPONDENT);
            verify(notificationService).sendMail(
                DUMMY_EMAIL,
                "general-application-apps-judicial-notification-template-lip-id",
                notificationPropertiesSummeryJudgement(),
                "general-apps-judicial-notification-make-decision-" + CASE_REFERENCE
            );
        }

        @Test
        void notificationShouldSendRespondentForFreeFormOrder_ifMadeWithNotice() {
            GeneralApplicationCaseData caseData = caseDataFreeFormOrder();
            caseData = caseData.toBuilder()
                .generalAppInformOtherParty(new GAInformOtherParty())
                .generalAppRespondentAgreement(new GARespondentOrderAgreement().setHasAgreed(NO))
                .applicationIsUncloakedOnce(YES)
                .build();
            when(solicitorEmailValidation.validateSolicitorEmail(any(), any()))
                .thenReturn(caseData);
            when(caseDetailsConverter.toGeneralApplicationCaseData(any())).thenReturn(GeneralApplicationCaseData.builder().build());
            when(gaForLipService.isLipResp(any())).thenReturn(true);
            judicialNotificationService.sendNotification(caseData, RESPONDENT);
            verify(notificationService).sendMail(
                DUMMY_EMAIL,
                "general-application-apps-judicial-notification-template-lip-id",
                notificationPropertiesSummeryJudgement(),
                "general-apps-judicial-notification-make-decision-" + CASE_REFERENCE
            );
        }

        private GeneralApplicationCaseData caseDataListForHearing() {
            return GeneralApplicationCaseData.builder()
                .ccdCaseReference(CASE_REFERENCE)
                .judicialDecision(GAJudicialDecision.builder()
                                      .decision(GAJudgeDecisionOption.LIST_FOR_A_HEARING).build())
                .isGaApplicantLip(YES)
                .isGaRespondentOneLip(NO)
                .applicantPartyName("App")
                .claimant1PartyName("CL")
                .defendant1PartyName("DEF")
                .judicialDecisionMakeOrder(GAJudicialMakeAnOrder.builder()
                                               .makeAnOrder(GAJudgeMakeAnOrderOption.DISMISS_THE_APPLICATION).build())
                .generalAppApplnSolicitor(new GASolicitorDetailsGAspec()
                                              .setEmail(DUMMY_EMAIL))
                .businessProcess(BusinessProcess.builder().camundaEvent(JUDGES_DECISION).build())
                .generalAppParentCaseLink(GeneralAppParentCaseLink.builder()
                                              .caseReference(CASE_REFERENCE.toString()).build())
                .generalAppType(new GAApplicationType(applicationTypeSummeryJudgement()))
                .isMultiParty(NO)
                .build();
        }

        private GeneralApplicationCaseData caseDataFreeFormOrder() {
            return GeneralApplicationCaseData.builder()
                .judicialDecision(GAJudicialDecision.builder()
                                      .decision(GAJudgeDecisionOption.FREE_FORM_ORDER).build())
                .generalAppRespondentSolicitors(respondentSolicitors())
                .ccdCaseReference(CASE_REFERENCE)
                .isGaApplicantLip(YES)
                .isGaRespondentOneLip(NO)
                .applicantPartyName("App")
                .claimant1PartyName("CL")
                .defendant1PartyName("DEF")
                .generalAppInformOtherParty(new GAInformOtherParty().setIsWithNotice(YES))
                .generalAppApplnSolicitor(new GASolicitorDetailsGAspec()
                                              .setEmail(DUMMY_EMAIL))
                .businessProcess(BusinessProcess.builder().camundaEvent(JUDGES_DECISION).build())
                .generalAppParentCaseLink(GeneralAppParentCaseLink.builder()
                                              .caseReference(CASE_REFERENCE.toString()).build())
                .generalAppType(new GAApplicationType(applicationTypeSummeryJudgement()))
                .isMultiParty(NO)
                .ccdState(CaseState.APPLICATION_SUBMITTED_AWAITING_JUDICIAL_DECISION)
                .businessProcess(BusinessProcess.builder().camundaEvent(JUDGES_DECISION)
                                     .activityId("StartRespondentNotificationProcessMakeDecision")
                                     .build())
                .build();
        }

        private GeneralApplicationCaseData caseDataForJudicialRequestForInformationOfApplication(
            YesOrNo isRespondentOrderAgreement, YesOrNo isWithNotice, YesOrNo isCloaked,
            GAJudgeRequestMoreInfoOption gaJudgeRequestMoreInfoOption) {

            return GeneralApplicationCaseData.builder()
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
                .generalAppRespondentAgreement(new GARespondentOrderAgreement()
                                                   .setHasAgreed(isRespondentOrderAgreement))
                .generalAppInformOtherParty(new GAInformOtherParty().setIsWithNotice(isWithNotice))
                .generalAppApplnSolicitor(new GASolicitorDetailsGAspec()
                                              .setEmail(DUMMY_EMAIL))
                .isGaApplicantLip(YES)
                .isGaRespondentOneLip(NO)
                .applicantPartyName("App")
                .claimant1PartyName("CL")
                .defendant1PartyName("DEF")
                .businessProcess(BusinessProcess.builder().camundaEvent(JUDGES_DECISION).build())
                .generalAppParentCaseLink(GeneralAppParentCaseLink.builder()
                                              .caseReference(CASE_REFERENCE.toString()).build())
                .generalAppType(new GAApplicationType(applicationTypeToStayTheClaim()))
                .generalAppPBADetails(GeneralApplicationPbaDetails.builder().build())
                .build();

        }

        private GeneralApplicationCaseData caseDataWithSolicitorDataOnlyForApplicationUncloakedJudgeApproveOrEdit(YesOrNo orderAgreement,
                                                                                                YesOrNo isWithNotice,
                                                                                                YesOrNo isCloaked) {
            return GeneralApplicationCaseData.builder()
                .applicationIsCloaked(isCloaked)
                .ccdCaseReference(CASE_REFERENCE)
                .generalAppRespondentSolicitors(respondentSolicitors())
                .judicialDecisionMakeOrder(GAJudicialMakeAnOrder.builder()
                                               .makeAnOrder(GAJudgeMakeAnOrderOption.APPROVE_OR_EDIT).build())
                .judicialDecision(GAJudicialDecision.builder()
                                      .decision(GAJudgeDecisionOption.MAKE_AN_ORDER).build())
                .generalAppApplnSolicitor(new GASolicitorDetailsGAspec()
                                              .setEmail(DUMMY_EMAIL))
                .isGaRespondentOneLip(NO)
                .isGaApplicantLip(YES)

                .businessProcess(BusinessProcess.builder().camundaEvent(JUDGES_DECISION).build())
                .generalAppParentCaseLink(GeneralAppParentCaseLink.builder()
                                              .caseReference(CASE_REFERENCE.toString()).build())
                .generalAppRespondentAgreement(new GARespondentOrderAgreement().setHasAgreed(orderAgreement))
                .generalAppInformOtherParty(new GAInformOtherParty().setIsWithNotice(isWithNotice))
                .generalAppType(new GAApplicationType(applicationTypeSummeryJudgement()))
                .isMultiParty(NO)
                .applicantPartyName("App")
                .claimant1PartyName("CL")
                .defendant1PartyName("DEF")
                .build();
        }

        private GeneralApplicationCaseData caseDataForJudicialDirectionOrderOfApplicationWhenRespondentsArePresentInList(
            YesOrNo orderAgreement,
            YesOrNo isWithNotice,
            YesOrNo isCloaked,
            YesOrNo isGaLip,
            YesOrNo isApplicantLip,
            YesOrNo isRespondentLip) {
            return GeneralApplicationCaseData.builder()
                .ccdCaseReference(CASE_REFERENCE)
                .generalAppRespondentAgreement(new GARespondentOrderAgreement().setHasAgreed(orderAgreement))
                .generalAppInformOtherParty(new GAInformOtherParty().setIsWithNotice(isWithNotice))
                .applicationIsCloaked(isCloaked)
                .isGaApplicantLip(isApplicantLip)
                .applicantPartyName("App")
                .claimant1PartyName("CL")
                .defendant1PartyName("DEF")
                .isGaRespondentOneLip(isRespondentLip)
                .judicialDecision(GAJudicialDecision.builder().decision(GAJudgeDecisionOption.MAKE_AN_ORDER).build())
                .judicialDecisionMakeOrder(GAJudicialMakeAnOrder.builder()
                                               .makeAnOrder(GIVE_DIRECTIONS_WITHOUT_HEARING).build())
                .generalAppRespondentSolicitors(isGaLip.equals(YES) ? lipRespondent() : respondentSolicitors())
                .generalAppApplnSolicitor(new GASolicitorDetailsGAspec()
                                              .setEmail(DUMMY_EMAIL))
                .businessProcess(BusinessProcess.builder().camundaEvent(JUDGES_DECISION).build())
                .generalAppParentCaseLink(GeneralAppParentCaseLink.builder()
                                              .caseReference(CASE_REFERENCE.toString()).build())
                .generalAppType(new GAApplicationType(applicationTypeSummeryJudgement()))
                .generalAppPBADetails(GeneralApplicationPbaDetails.builder().build())
                .isMultiParty(NO)
                .build();
        }

        private GeneralApplicationCaseData caseDataForJudgeDismissal(YesOrNo orderAgreement, YesOrNo isWithNotice, YesOrNo isCloaked,
                                                   YesOrNo isLipApplicant, YesOrNo isLipRespondent) {
            return GeneralApplicationCaseData.builder()
                .ccdCaseReference(CASE_REFERENCE)
                .generalAppRespondentAgreement(new GARespondentOrderAgreement().setHasAgreed(orderAgreement))
                .generalAppInformOtherParty(new GAInformOtherParty().setIsWithNotice(isWithNotice))
                .applicationIsCloaked(isCloaked)
                .isGaApplicantLip(isLipApplicant)
                .isGaRespondentOneLip(isLipRespondent)
                .applicantPartyName("App")
                .claimant1PartyName("CL")
                .defendant1PartyName("DEF")
                .generalAppRespondentSolicitors(respondentSolicitors())
                .judicialDecision(GAJudicialDecision.builder()
                                      .decision(GAJudgeDecisionOption.MAKE_AN_ORDER).build())
                .judicialDecisionMakeOrder(GAJudicialMakeAnOrder.builder()
                                               .makeAnOrder(GAJudgeMakeAnOrderOption.DISMISS_THE_APPLICATION).build())
                .generalAppApplnSolicitor(new GASolicitorDetailsGAspec()
                                              .setEmail(DUMMY_EMAIL))
                .businessProcess(BusinessProcess.builder().camundaEvent(JUDGES_DECISION).build())
                .generalAppParentCaseLink(GeneralAppParentCaseLink.builder()
                                              .caseReference(CASE_REFERENCE.toString()).build())
                .generalAppType(new GAApplicationType(applicationTypeSummeryJudgement()))
                .generalAppPBADetails(GeneralApplicationPbaDetails.builder().build())
                .isMultiParty(NO)
                .build();
        }

        private Map<String, String> notificationPropertiesSummeryJudgement() {

            customProp.put(NotificationDataGA.CASE_REFERENCE, CASE_REFERENCE.toString());
            customProp.put(NotificationDataGA.GENAPP_REFERENCE, CASE_REFERENCE.toString());
            customProp.put(NotificationDataGA.GA_APPLICATION_TYPE, GeneralApplicationTypes.SUMMARY_JUDGEMENT.getDisplayedValue());
            customProp.put(NotificationDataGA.PARTY_REFERENCE, PARTY_REFERENCE);
            customProp.put(NotificationDataGA.WELSH_CONTACT, "E-bost: ymholiadaucymraeg@justice.gov.uk");
            customProp.put(NotificationDataGA.WELSH_HMCTS_SIGNATURE, "Hawliadau am Arian yn y Llys Sifil Ar-lein \n Gwasanaeth Llysoedd a Thribiwnlysoedd EF");
            customProp.put(NotificationDataGA.WELSH_OPENING_HOURS, "Dydd Llun i ddydd Iau, 9am – 5pm, dydd Gwener, 9am – 4.30pm");
            customProp.put(NotificationDataGA.WELSH_PHONE_CONTACT, "Ffôn: 0300 303 5174");
            customProp.put(NotificationDataGA.SPEC_CONTACT, "Email: contactocmc@justice.gov.uk");
            customProp.put(NotificationDataGA.SPEC_UNSPEC_CONTACT, "Email for Specified Claims: contactocmc@justice.gov.uk "
                + "\n Email for Damages Claims: damagesclaims@justice.gov.uk");
            customProp.put(NotificationDataGA.HMCTS_SIGNATURE, "Online Civil Claims \n HM Courts & Tribunal Service");
            customProp.put(NotificationDataGA.OPENING_HOURS, "Monday to Friday, 8.30am to 5pm");
            customProp.put(NotificationDataGA.PHONE_CONTACT, "For anything related to hearings, call 0300 123 5577 "
                + "\n For all other matters, call 0300 123 7050");
            return customProp;
        }

        private Map<String, String> notificationPropertiesVaryOrder() {

            customProp.put(NotificationDataGA.CASE_REFERENCE, CASE_REFERENCE.toString());
            customProp.put(NotificationDataGA.GENAPP_REFERENCE, CASE_REFERENCE.toString());
            customProp.put(NotificationDataGA.GA_APPLICATION_TYPE,
                           GeneralApplicationTypes.VARY_ORDER.getDisplayedValue());
            customProp.put(NotificationDataGA.PARTY_REFERENCE, PARTY_REFERENCE);
            customProp.put(NotificationDataGA.WELSH_CONTACT, "E-bost: ymholiadaucymraeg@justice.gov.uk");
            customProp.put(NotificationDataGA.WELSH_HMCTS_SIGNATURE, "Hawliadau am Arian yn y Llys Sifil Ar-lein \n Gwasanaeth Llysoedd a Thribiwnlysoedd EF");
            customProp.put(NotificationDataGA.WELSH_OPENING_HOURS, "Dydd Llun i ddydd Iau, 9am – 5pm, dydd Gwener, 9am – 4.30pm");
            customProp.put(NotificationDataGA.WELSH_PHONE_CONTACT, "Ffôn: 0300 303 5174");
            customProp.put(NotificationDataGA.SPEC_CONTACT, "Email: contactocmc@justice.gov.uk");
            customProp.put(NotificationDataGA.SPEC_UNSPEC_CONTACT, "Email for Specified Claims: contactocmc@justice.gov.uk "
                + "\n Email for Damages Claims: damagesclaims@justice.gov.uk");
            customProp.put(NotificationDataGA.HMCTS_SIGNATURE, "Online Civil Claims \n HM Courts & Tribunal Service");
            customProp.put(NotificationDataGA.OPENING_HOURS, "Monday to Friday, 8.30am to 5pm");
            customProp.put(NotificationDataGA.PHONE_CONTACT, "For anything related to hearings, call 0300 123 5577 "
                + "\n For all other matters, call 0300 123 7050");
            return customProp;
        }

        private GeneralApplicationCaseData caseDataForConcurrentWrittenOption(YesOrNo isGaApplicantLip, YesOrNo isGaRespondentOneLip) {
            return
                GeneralApplicationCaseData.builder()
                    .ccdCaseReference(CASE_REFERENCE)
                    .isGaApplicantLip(isGaApplicantLip)
                    .isGaRespondentOneLip(isGaRespondentOneLip)
                    .applicantPartyName("App")
                    .claimant1PartyName("CL")
                    .defendant1PartyName("DEF")
                    .generalAppRespondentSolicitors(respondentSolicitors())
                    .judicialDecision(GAJudicialDecision.builder()
                                          .decision(GAJudgeDecisionOption.LIST_FOR_A_HEARING).build())
                    .generalAppApplnSolicitor(new GASolicitorDetailsGAspec()
                                                  .setEmail(DUMMY_EMAIL))
                    .businessProcess(BusinessProcess.builder().camundaEvent(JUDGES_DECISION).build())
                    .generalAppParentCaseLink(GeneralAppParentCaseLink.builder()
                                                  .caseReference(CASE_REFERENCE.toString()).build())
                    .judicialDecisionMakeAnOrderForWrittenRepresentations(
                        GAJudicialWrittenRepresentations.builder().writtenOption(
                            GAJudgeWrittenRepresentationsOptions.CONCURRENT_REPRESENTATIONS).build())
                    .generalAppType(new GAApplicationType(applicationTypeSummeryJudgement()))
                    .judicialConcurrentDateText(DUMMY_DATE)
                    .isMultiParty(NO)
                    .build();
        }

        private GeneralApplicationCaseData caseDataForSequentialWrittenOption(YesOrNo isGaApplicantLip, YesOrNo isGaRespondentOneLip) {
            return GeneralApplicationCaseData.builder()
                .ccdCaseReference(CASE_REFERENCE)
                .isGaApplicantLip(isGaApplicantLip)
                .isGaRespondentOneLip(isGaRespondentOneLip)
                .applicantPartyName("App")
                .claimant1PartyName("CL")
                .defendant1PartyName("DEF")
                .generalAppRespondentSolicitors(respondentSolicitors())
                .parentClaimantIsApplicant(YES)
                .judicialDecision(GAJudicialDecision.builder()
                                      .decision(GAJudgeDecisionOption.LIST_FOR_A_HEARING).build())
                .generalAppRespondentSolicitors(respondentSolicitors())
                .generalAppApplnSolicitor(new GASolicitorDetailsGAspec()
                                              .setEmail(DUMMY_EMAIL))
                .businessProcess(BusinessProcess.builder().camundaEvent(JUDGES_DECISION).build())
                .generalAppParentCaseLink(GeneralAppParentCaseLink.builder()
                                              .caseReference(CASE_REFERENCE.toString()).build())
                .judicialDecisionMakeAnOrderForWrittenRepresentations(
                    GAJudicialWrittenRepresentations.builder().writtenOption(
                        GAJudgeWrittenRepresentationsOptions.SEQUENTIAL_REPRESENTATIONS).build())
                .generalAppType(new GAApplicationType(applicationTypeSummeryJudgement()))
                .isMultiParty(NO)
                .build();
        }

        private List<Element<GASolicitorDetailsGAspec>> respondentSolicitors() {
            return Collections.singletonList(element(new GASolicitorDetailsGAspec().setId(ID)
                                                         .setForename("respondent")
                                                         .setSurname(Optional.of("solicitor"))
                                                         .setEmail(DUMMY_EMAIL).setOrganisationIdentifier(ORG_ID)));
        }
    }

    @Nested
    class AboutToSubmitCallback {

        @BeforeEach
        public void setup() {
            when(gaForLipService.isLipApp(any())).thenReturn(false);
        }

        @Test
        void shouldNotSentNotification_WhenNotificationCriteria_NotMet() {

            GeneralApplicationCaseData caseData = caseDataForConcurrentWrittenOption().toBuilder().businessProcess(null).build();
            when(solicitorEmailValidation.validateSolicitorEmail(any(), any()))
                .thenReturn(caseData);

            judicialNotificationService.sendNotification(caseData, APPLICANT);
            verify(notificationService, never()).sendMail(any(), any(), any(), any());
        }

        @Test
        void notificationShouldSendThriceForConcurrentWrittenRepsWhenInvoked() {

            when(solicitorEmailValidation.validateSolicitorEmail(any(), any()))
                .thenReturn(caseDataForConcurrentWrittenOption());

            judicialNotificationService.sendNotification(caseDataForConcurrentWrittenOption(), APPLICANT);
            verify(notificationService, times(1)).sendMail(
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

            judicialNotificationService.sendNotification(caseDataForSequentialWrittenOption(), APPLICANT);
            verify(notificationService, times(1)).sendMail(
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

            judicialNotificationService
                .sendNotification(caseDataForConcurrentWrittenRepRespondentNotPresent(), APPLICANT);
            verify(notificationService, times(1)).sendMail(
                DUMMY_EMAIL,
                "general-application-apps-judicial-notification-template-id",
                notificationPropertiesSummeryJudgementConcurrent(),
                "general-apps-judicial-notification-make-decision-" + CASE_REFERENCE
            );
        }

        @Test
        void notificationShouldSendForDismissal() {

            when(solicitorEmailValidation.validateSolicitorEmail(any(), any()))
                .thenReturn(caseDataForJudgeDismissal(NO, YES, NO, NO, NO, NO));

            judicialNotificationService.sendNotification(caseDataForJudgeDismissal(NO, YES, NO,  NO, NO, NO), APPLICANT);
            verify(notificationService, times(1)).sendMail(
                DUMMY_EMAIL,
                "general-application-apps-judicial-notification-template-id",
                notificationPropertiesToStrikeOut(),
                "general-apps-judicial-notification-make-decision-" + CASE_REFERENCE
            );
        }

        @Test
        void notificationShouldSendForDismissal_when_consentOrder() {
            GeneralApplicationCaseData caseData = caseDataForJudgeDismissal(YES, YES, YES,  NO, NO, NO).toBuilder()
                .generalAppConsentOrder(NO).build();

            when(solicitorEmailValidation.validateSolicitorEmail(any(), any()))
                .thenReturn(caseData);

            judicialNotificationService
                .sendNotification(caseData, APPLICANT);

            judicialNotificationService
                .sendNotification(caseData, RESPONDENT);

            verify(notificationService, times(3)).sendMail(
                DUMMY_EMAIL,
                "general-application-apps-judicial-notification-template-id",
                notificationPropertiesToStrikeOut(),
                "general-apps-judicial-notification-make-decision-" + CASE_REFERENCE
            );
        }

        @Test
        void notificationShouldSendForDismissal_when_withoutConsentOrder() {
            GeneralApplicationCaseData caseData = caseDataForJudgeDismissal(NO, NO, YES,  NO, NO, NO);

            when(solicitorEmailValidation.validateSolicitorEmail(any(), any()))
                .thenReturn(caseData);

            judicialNotificationService
                .sendNotification(caseData, APPLICANT);

            judicialNotificationService
                .sendNotification(caseData, RESPONDENT);

            verify(notificationService, times(3)).sendMail(
                DUMMY_EMAIL,
                "general-application-apps-judicial-notification-template-id",
                notificationPropertiesToStrikeOut(),
                "general-apps-judicial-notification-make-decision-" + CASE_REFERENCE
            );
        }

        @Test
        void notificationUncloakShouldSendForDismissal() {

            when(solicitorEmailValidation.validateSolicitorEmail(any(), any()))
                .thenReturn(caseDataForJudgeDismissal(NO, NO, NO,  NO, NO, NO));

            judicialNotificationService.sendNotification(caseDataForJudgeDismissal(NO, NO, NO,  NO, NO, NO), APPLICANT);
            verify(notificationService, times(1)).sendMail(
                DUMMY_EMAIL,
                "general-application-apps-judicial-notification-template-id",
                notificationPropertiesToStrikeOut(),
                "general-apps-judicial-notification-make-decision-" + CASE_REFERENCE
            );
        }

        @Test
        void notificationShouldSendListForHearing() {

            when(solicitorEmailValidation.validateSolicitorEmail(any(), any()))
                .thenReturn(caseDataListForHearing());

            judicialNotificationService.sendNotification(caseDataListForHearing(), APPLICANT);
            verify(notificationService).sendMail(
                DUMMY_EMAIL,
                "general-application-apps-judicial-notification-template-id",
                notificationPropertiesToExtendTime(),
                "general-apps-judicial-notification-make-decision-" + CASE_REFERENCE
            );
        }

        @Test
        void notificationShouldSendToApplicant_IfApplicationCloakedWithApplicantSolicitor() {

            when(solicitorEmailValidation.validateSolicitorEmail(any(), any()))
                .thenReturn(caseDataForCloakedApplicationWithSolicitorDataOnly());

            judicialNotificationService
                .sendNotification(caseDataForCloakedApplicationWithSolicitorDataOnly(), APPLICANT);
            verify(notificationService).sendMail(
                DUMMY_EMAIL,
                "general-application-apps-judicial-notification-template-id",
                notificationPropertiesSummeryJudgement(),
                "general-apps-judicial-notification-make-decision-" + CASE_REFERENCE
            );
        }

        @Test
        void notificationShouldSendSendToApplicant_IfApplicationUncloakedForApproveOrEdit() {

            when(solicitorEmailValidation.validateSolicitorEmail(any(), any()))
                .thenReturn(caseDataWithSolicitorDataOnlyForApplicationUncloakedJudgeApproveOrEdit(YES, YES, NO, NO, NO, NO));

            judicialNotificationService.sendNotification(
                caseDataWithSolicitorDataOnlyForApplicationUncloakedJudgeApproveOrEdit(YES, YES, NO, NO, NO, NO), APPLICANT);
            verify(notificationService).sendMail(
                DUMMY_EMAIL,
                "general-application-apps-judicial-notification-template-id",
                notificationPropertiesToStayTheClaim(),
                "general-apps-judicial-notification-make-decision-" + CASE_REFERENCE
            );
        }

        @Test
        void notificationShouldSendSendToApplicant_and_respondent_IfApplicationApproveOrEditForConsentOrder() {
            GeneralApplicationCaseData caseData = caseDataWithSolicitorDataOnlyForApplicationUncloakedJudgeApproveOrEdit(YES, YES, NO, NO, NO, NO)
                .toBuilder().generalAppConsentOrder(NO).build();

            when(solicitorEmailValidation.validateSolicitorEmail(any(), any())).thenReturn(caseData);

            judicialNotificationService.sendNotification(caseData, APPLICANT);
            judicialNotificationService.sendNotification(caseData, RESPONDENT);

            verify(notificationService, times(3)).sendMail(
                DUMMY_EMAIL,
                "general-application-apps-judicial-notification-template-id",
                notificationPropertiesToStayTheClaim(),
                "general-apps-judicial-notification-make-decision-" + CASE_REFERENCE
            );
        }

        @Test
        void notificationShouldSendSendTo_applicantAndRespondent_ifApplicationApproveOrEditFor_withoutConsentOrder() {
            GeneralApplicationCaseData caseData = caseDataWithSolicitorDataOnlyForApplicationUncloakedJudgeApproveOrEdit(NO, NO, YES, NO, NO, NO);

            when(solicitorEmailValidation.validateSolicitorEmail(any(), any())).thenReturn(caseData);

            judicialNotificationService.sendNotification(caseData, APPLICANT);
            judicialNotificationService.sendNotification(caseData, RESPONDENT);

            verify(notificationService, times(3)).sendMail(
                DUMMY_EMAIL,
                "general-application-apps-judicial-notification-template-id",
                notificationPropertiesToStayTheClaim(),
                "general-apps-judicial-notification-make-decision-" + CASE_REFERENCE
            );
        }

        @Test
        void notificationShouldSendIfApplicationIsToAmendStatementOfCase() {

            when(solicitorEmailValidation.validateSolicitorEmail(any(), any()))
                .thenReturn(caseDataForAmendStatementOfClaim());

            judicialNotificationService.sendNotification(caseDataForAmendStatementOfClaim(), APPLICANT);
            verify(notificationService, times(1)).sendMail(
                DUMMY_EMAIL,
                "general-application-apps-judicial-notification-template-id",
                notificationPropertiesToAmendStatementOfCase(),
                "general-apps-judicial-notification-make-decision-" + CASE_REFERENCE
            );
        }

        @Test
        void notificationShouldSendIfJudicialApproval() {

            when(solicitorEmailValidation.validateSolicitorEmail(any(), any()))
                .thenReturn(caseDataForJudicialApprovalOfApplication(NO, YES));

            judicialNotificationService.sendNotification(caseDataForJudicialApprovalOfApplication(NO, YES), APPLICANT);
            verify(notificationService, times(1)).sendMail(
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
                        GeneralApplicationPbaDetails.builder()
                            .additionalPaymentDetails(buildAdditionalPaymentSuccessData())
                            .build())
                                .build());

            judicialNotificationService.sendNotification(
                caseDataForJudicialApprovalOfApplication(NO, NO).toBuilder()
                    .generalAppPBADetails(GeneralApplicationPbaDetails.builder()
                                              .additionalPaymentDetails(buildAdditionalPaymentSuccessData())
                                              .build())
                    .build(), APPLICANT);
            verify(notificationService, times(1)).sendMail(
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

            judicialNotificationService.sendNotification(caseDataForJudicialApprovalOfApplication(NO, NO), APPLICANT);
            verify(notificationService, times(1)).sendMail(
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

            judicialNotificationService
                .sendNotification(caseDataForJudicialDirectionOrderOfApplication(YES, NO), APPLICANT);
            verify(notificationService, times(1)).sendMail(
                DUMMY_EMAIL,
                "general-application-apps-judicial-notification-template-id",
                notificationPropertiesToAmendStatementOfCase(),
                "general-apps-judicial-notification-make-decision-" + CASE_REFERENCE
            );
        }

        @Test
        void notificationShouldSend_ApplicantRespondent_When_JudicialDirectionOrderRep_withConsentOrder() {

            GeneralApplicationCaseData caseData
                = caseDataForJudicialDirectionOrderOfApplicationWhenRespondentsArePresentInList(YES,
                                                                                                YES, YES, NO, NO, NO)
                .toBuilder()
                .generalAppConsentOrder(YES)
                .applicationIsCloaked(null)
                .build();

            when(solicitorEmailValidation.validateSolicitorEmail(any(), any()))
                .thenReturn(caseData);

            judicialNotificationService.sendNotification(caseData, APPLICANT);
            judicialNotificationService.sendNotification(caseData, RESPONDENT);

            verify(notificationService, times(3)).sendMail(
                DUMMY_EMAIL,
                "general-application-apps-judicial-notification-template-id",
                notificationPropertiesToAmendStatementOfCase(),
                "general-apps-judicial-notification-make-decision-" + CASE_REFERENCE
            );
        }

        @Test
        void notificationShouldSend_Applicant_When_JudicialDirectionOrderRep_ConsentOrder_withRespondentNull() {

            GeneralApplicationCaseData caseData
                = caseDataForJudicialDirectionOrderOfApplicationWhenRespondentsArePresentInList(YES,
                                                                                                YES, YES, NO, NO, NO)
                .toBuilder()
                .generalAppConsentOrder(YES)
                .generalAppRespondentSolicitors(null)
                .build();

            when(solicitorEmailValidation.validateSolicitorEmail(any(), any()))
                .thenReturn(caseData);

            judicialNotificationService.sendNotification(caseData, APPLICANT);
            judicialNotificationService.sendNotification(caseData, RESPONDENT);

            verify(notificationService, times(1)).sendMail(
                DUMMY_EMAIL,
                "general-application-apps-judicial-notification-template-id",
                notificationPropertiesToAmendStatementOfCase(),
                "general-apps-judicial-notification-make-decision-" + CASE_REFERENCE
            );
        }

        @Test
        void notificationShouldSendIfJudicialDirectionOrderRepArePresentInList() {

            when(solicitorEmailValidation.validateSolicitorEmail(any(), any()))
                .thenReturn(
                    caseDataForJudicialDirectionOrderOfApplicationWhenRespondentsArePresentInList(NO, YES, NO, NO, NO, NO));

            judicialNotificationService
                .sendNotification(
                    caseDataForJudicialDirectionOrderOfApplicationWhenRespondentsArePresentInList(
                        NO, YES, NO, NO, NO, NO), APPLICANT);
            verify(notificationService, times(1)).sendMail(
                DUMMY_EMAIL,
                "general-application-apps-judicial-notification-template-id",
                notificationPropertiesToAmendStatementOfCase(),
                "general-apps-judicial-notification-make-decision-" + CASE_REFERENCE
            );
        }

        @Test
        void notificationUncloakShouldSendIfJudicialDirectionOrderRepArePresentInList() {

            when(solicitorEmailValidation.validateSolicitorEmail(any(), any()))
                .thenReturn(
                    caseDataForJudicialDirectionOrderOfApplicationWhenRespondentsArePresentInList(
                        NO, NO, NO, NO, NO, NO));

            judicialNotificationService
                .sendNotification(
                    caseDataForJudicialDirectionOrderOfApplicationWhenRespondentsArePresentInList(
                        NO, NO, NO, NO, NO, NO), APPLICANT);
            verify(notificationService, times(1)).sendMail(
                DUMMY_EMAIL,
                "general-application-apps-judicial-notification-template-id",
                notificationPropertiesToAmendStatementOfCase(),
                "general-apps-judicial-notification-make-decision-" + CASE_REFERENCE
            );
        }

        @Test
        void notificationShouldSendIfApplicationUncloakedDismissed_WithoutRespondentEmail() {

            when(solicitorEmailValidation.validateSolicitorEmail(any(), any()))
                .thenReturn(caseDataForApplicationUncloakedIsDismissed());

            judicialNotificationService.sendNotification(caseDataForApplicationUncloakedIsDismissed(), APPLICANT);
            verify(notificationService).sendMail(
                DUMMY_EMAIL,
                "general-application-apps-judicial-notification-template-id",
                notificationPropertiesToStrikeOut(),
                "general-apps-judicial-notification-make-decision-" + CASE_REFERENCE
            );
        }

        @Test
        void notificationShouldSendIfSequentialWrittenRepsArePresentInList() {

            when(solicitorEmailValidation.validateSolicitorEmail(any(), any()))
                .thenReturn(caseDataForSequentialWrittenRepInList());

            judicialNotificationService.sendNotification(caseDataForSequentialWrittenRepInList(), APPLICANT);
            verify(notificationService, times(1)).sendMail(
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

            judicialNotificationService
                .sendNotification(caseDataForApplicationsApprovedWhenRespondentsAreInList(NO, YES), APPLICANT);
            verify(notificationService, times(1)).sendMail(
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

            judicialNotificationService
                .sendNotification(caseDataForApplicationsApprovedWhenRespondentsAreInList(NO, NO), APPLICANT);
            verify(notificationService, times(1)).sendMail(
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
                        .toBuilder().generalAppPBADetails(GeneralApplicationPbaDetails.builder()
                                                              .additionalPaymentDetails(
                                                                  buildAdditionalPaymentSuccessData())
                                                              .build())
                        .build());

            GeneralApplicationCaseData caseData = caseDataForApplicationsApprovedWhenRespondentsAreInList(NO, NO)
                .toBuilder().generalAppPBADetails(
                    GeneralApplicationPbaDetails.builder()
                        .additionalPaymentDetails(buildAdditionalPaymentSuccessData())
                        .build())
                .build();

            judicialNotificationService.sendNotification(caseData, APPLICANT);
            verify(notificationService, times(1)).sendMail(
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

            judicialNotificationService
                .sendNotification(
                    caseDataForApplicationsApprovedStrikeOutWhenRespondentsAreInList(
                        NO, YES, NO, "UNSPEC_CLAIM"), APPLICANT);
            verify(notificationService, times(1)).sendMail(
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

            judicialNotificationService
                .sendNotification(
                    caseDataForApplicationsApprovedStrikeOutWhenRespondentsAreInList(
                        NO, YES, NO, "SPEC_CLAIM"), APPLICANT);

            verify(notificationService, times(1)).sendMail(
                DUMMY_EMAIL,
                "general-application-apps-judicial-notification-template-id",
                notificationPropertiesToStrikeOut(),
                "general-apps-judicial-notification-make-decision-" + CASE_REFERENCE
            );
        }

        @Test
        void notificationShouldSendForApplicationListForHearingWhenRespondentsAreAvailableInList() {

            when(solicitorEmailValidation.validateSolicitorEmail(any(), any()))
                .thenReturn(caseDataForListForHearingRespondentsAreInList());

            judicialNotificationService.sendNotification(caseDataForListForHearingRespondentsAreInList(), APPLICANT);
            verify(notificationService, times(1)).sendMail(
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

            judicialNotificationService.sendNotification(
                caseDataForCaseDismissedByJudgeRespondentsAreInList(NO, YES, NO), APPLICANT);

            verify(notificationService, times(1)).sendMail(
                DUMMY_EMAIL,
                "general-application-apps-judicial-notification-template-id",
                notificationPropertiesSummeryJudgementConcurrent(),
                "general-apps-judicial-notification-make-decision-" + CASE_REFERENCE
            );
        }

        @Test
        void notificationShouldSend_ApplicantRespondent_When_ApplicationIsDismissed_withConsentOrder() {

            GeneralApplicationCaseData caseData = caseDataForCaseDismissedByJudgeRespondentsAreInList(YES, YES, YES)
                .toBuilder()
                .generalAppConsentOrder(YES)
                .build();

            when(solicitorEmailValidation.validateSolicitorEmail(any(), any()))
                .thenReturn(caseData);

            judicialNotificationService.sendNotification(caseData, APPLICANT);
            judicialNotificationService.sendNotification(caseData, RESPONDENT);

            verify(notificationService, times(3)).sendMail(
                DUMMY_EMAIL,
                "general-application-apps-judicial-notification-template-id",
                notificationPropertiesSummeryJudgementConcurrent(),
                "general-apps-judicial-notification-make-decision-" + CASE_REFERENCE
            );
        }

        @Test
        void notificationShouldSend_ApplicantAndRespondent_When_ApplicationIsDismissed_withOutConsentOrder() {

            GeneralApplicationCaseData caseData = caseDataForCaseDismissedByJudgeRespondentsAreInList(NO, NO, YES)
                .toBuilder()
                .build();

            when(solicitorEmailValidation.validateSolicitorEmail(any(), any()))
                .thenReturn(caseData);

            judicialNotificationService.sendNotification(caseData, APPLICANT);
            judicialNotificationService.sendNotification(caseData, RESPONDENT);

            verify(notificationService, times(3)).sendMail(
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

            judicialNotificationService
                .sendNotification(
                    caseDataForApplicationsApprovedStrikeOutWhenRespondentsAreInList(
                        NO, NO, NO, "UNSPEC_CLAIM"), APPLICANT);

            verify(notificationService, times(1)).sendMail(
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

            judicialNotificationService
                .sendNotification(
                    caseDataForApplicationsApprovedStrikeOutWhenRespondentsAreInList(
                        NO, NO, NO, "SPEC_CLAIM"), APPLICANT);

            verify(notificationService, times(1)).sendMail(
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
                        .generalAppPBADetails(GeneralApplicationPbaDetails.builder()
                                                  .additionalPaymentDetails(buildAdditionalPaymentSuccessData())
                                                  .build())
                        .build()
                );

            GeneralApplicationCaseData caseData = caseDataForCaseDismissedByJudgeRespondentsAreInList(NO, NO, YES)
                .toBuilder().generalAppPBADetails(
                    GeneralApplicationPbaDetails.builder()
                        .additionalPaymentDetails(buildAdditionalPaymentSuccessData())
                        .build())
                .build();

            judicialNotificationService.sendNotification(caseData, APPLICANT);
            verify(notificationService, times(1)).sendMail(
                DUMMY_EMAIL,
                "general-application-apps-judicial-notification-template-id",
                notificationPropertiesSummeryJudgementConcurrent(),
                "general-apps-judicial-notification-make-decision-" + CASE_REFERENCE
            );
        }

        @Test
        void notificationShouldSendWhenJudgeApprovesOrderApplicationIsCloak() {

            when(solicitorEmailValidation.validateSolicitorEmail(any(), any()))
                .thenReturn(caseDataForJudgeApprovedOrderCloakWhenRespondentsArePresentInList());

            judicialNotificationService
                .sendNotification(caseDataForJudgeApprovedOrderCloakWhenRespondentsArePresentInList(), APPLICANT);
            verify(notificationService).sendMail(
                DUMMY_EMAIL,
                "general-application-apps-judicial-notification-template-id",
                notificationPropertiesSummeryJudgementConcurrent(),
                "general-apps-judicial-notification-make-decision-" + CASE_REFERENCE
            );
        }

        @Test
        void notificationShouldSendWhenJudgeApprovesOrderDamageApplicationIsCloak() {

            when(solicitorEmailValidation.validateSolicitorEmail(any(), any()))
                .thenReturn(
                    caseDataForApprovedCloakStrikeOutWhenRespondentsArePresentInList("UNSPEC_CLAIM"));

            judicialNotificationService
                .sendNotification(caseDataForApprovedCloakStrikeOutWhenRespondentsArePresentInList(
                    "UNSPEC_CLAIM"), APPLICANT);

            verify(notificationService).sendMail(
                DUMMY_EMAIL,
                "general-application-apps-judicial-notification-template-id",
                notificationPropertiesToStrikeOut(),
                "general-apps-judicial-notification-make-decision-" + CASE_REFERENCE
            );
        }

        @Test
        void notificationShouldSendWhenJudgeApprovesOrderOcmcApplicationIsCloak() {

            when(solicitorEmailValidation.validateSolicitorEmail(any(), any()))
                .thenReturn(
                    caseDataForApprovedCloakStrikeOutWhenRespondentsArePresentInList("SPEC_CLAIM"));

            judicialNotificationService
                .sendNotification(
                    caseDataForApprovedCloakStrikeOutWhenRespondentsArePresentInList(
                        "SPEC_CLAIM"), APPLICANT);

            verify(notificationService).sendMail(
                DUMMY_EMAIL,
                "general-application-apps-judicial-notification-template-id",
                notificationPropertiesToStrikeOut(),
                "general-apps-judicial-notification-make-decision-" + CASE_REFERENCE
            );
        }

        @Test
        void notificationShouldSendWhenJudgeDismissedTheApplicationIsCloak() {

            when(solicitorEmailValidation.validateSolicitorEmail(any(), any()))
                .thenReturn(caseDataForJudgeDismissTheApplicationCloakWhenRespondentsArePresentInList());

            judicialNotificationService
                .sendNotification(
                    caseDataForJudgeDismissTheApplicationCloakWhenRespondentsArePresentInList(), APPLICANT);
            verify(notificationService).sendMail(
                DUMMY_EMAIL,
                "general-application-apps-judicial-notification-template-id",
                notificationPropertiesSummeryJudgementConcurrent(),
                "general-apps-judicial-notification-make-decision-" + CASE_REFERENCE
            );
        }

        @Test
        void notificationShouldSendApplicantLRForFreeFormOrder() {

            when(solicitorEmailValidation.validateSolicitorEmail(any(), any()))
                .thenReturn(caseDataFreeFormOrder());
            when(caseDetailsConverter.toGeneralApplicationCaseData(any())).thenReturn(GeneralApplicationCaseData.builder().build());
            when(gaForLipService.isLipApp(any())).thenReturn(false);
            judicialNotificationService.sendNotification(caseDataFreeFormOrder(), APPLICANT);
            verify(notificationService).sendMail(
                DUMMY_EMAIL,
                "general-application-apps-judicial-notification-template-id",
                notificationPropertiesSummeryJudgement(),
                "general-apps-judicial-notification-make-decision-" + CASE_REFERENCE
            );
        }

        @Test
        void notificationShouldSendRespondentLRForFreeFormOrder() {

            when(solicitorEmailValidation.validateSolicitorEmail(any(), any()))
                .thenReturn(caseDataFreeFormOrder());
            when(caseDetailsConverter.toGeneralApplicationCaseData(any())).thenReturn(GeneralApplicationCaseData.builder().build());
            when(gaForLipService.isLipResp(any())).thenReturn(false);
            judicialNotificationService.sendNotification(caseDataFreeFormOrder(), RESPONDENT);
            verify(notificationService, times(2)).sendMail(
                DUMMY_EMAIL,
                "general-application-apps-judicial-notification-template-id",
                notificationPropertiesSummeryJudgement(),
                "general-apps-judicial-notification-make-decision-" + CASE_REFERENCE
            );
        }

        private GeneralApplicationCaseData caseDataForConcurrentWrittenOption() {
            return
                GeneralApplicationCaseData.builder()
                    .ccdCaseReference(CASE_REFERENCE)
                    .generalAppRespondentSolicitors(respondentSolicitors())
                    .judicialDecision(GAJudicialDecision.builder()
                                          .decision(GAJudgeDecisionOption.LIST_FOR_A_HEARING).build())
                    .generalAppRespondentSolicitors(respondentSolicitors())
                    .generalAppApplnSolicitor(new GASolicitorDetailsGAspec()
                                                  .setEmail(DUMMY_EMAIL))
                    .businessProcess(BusinessProcess.builder().camundaEvent(JUDGES_DECISION).build())
                    .generalAppParentCaseLink(GeneralAppParentCaseLink.builder()
                                                  .caseReference(CASE_REFERENCE.toString()).build())
                    .judicialDecisionMakeAnOrderForWrittenRepresentations(
                        GAJudicialWrittenRepresentations.builder().writtenOption(
                            GAJudgeWrittenRepresentationsOptions.CONCURRENT_REPRESENTATIONS).build())
                    .generalAppType(new GAApplicationType(applicationTypeSummeryJudgement()))
                    .judicialConcurrentDateText(DUMMY_DATE)
                    .isMultiParty(NO)
                    .build();
        }

        private GeneralApplicationCaseData caseDataForSequentialWrittenOption() {
            return GeneralApplicationCaseData.builder()
                .ccdCaseReference(CASE_REFERENCE)
                .generalAppRespondentSolicitors(respondentSolicitors())
                .judicialDecision(GAJudicialDecision.builder()
                                      .decision(GAJudgeDecisionOption.LIST_FOR_A_HEARING).build())
                .generalAppRespondentSolicitors(respondentSolicitors())
                .generalAppApplnSolicitor(new GASolicitorDetailsGAspec()
                                              .setEmail(DUMMY_EMAIL))
                .businessProcess(BusinessProcess.builder().camundaEvent(JUDGES_DECISION).build())
                .generalAppParentCaseLink(GeneralAppParentCaseLink.builder()
                                              .caseReference(CASE_REFERENCE.toString()).build())
                .judicialDecisionMakeAnOrderForWrittenRepresentations(
                    GAJudicialWrittenRepresentations.builder().writtenOption(
                        GAJudgeWrittenRepresentationsOptions.SEQUENTIAL_REPRESENTATIONS).build())
                .generalAppType(new GAApplicationType(applicationTypeToGetReliefFromSanctions()))
                .isMultiParty(NO)
                .build();
        }

        private GeneralApplicationCaseData caseDataForConcurrentWrittenRepRespondentNotPresent() {
            return GeneralApplicationCaseData.builder()
                .ccdCaseReference(CASE_REFERENCE)
                .generalAppApplnSolicitor(new GASolicitorDetailsGAspec()
                                              .setEmail(DUMMY_EMAIL))
                .generalAppRespondentSolicitors(respondentSolicitors())
                .businessProcess(BusinessProcess.builder().camundaEvent(JUDGES_DECISION).build())
                .generalAppParentCaseLink(GeneralAppParentCaseLink.builder()
                                              .caseReference(CASE_REFERENCE.toString()).build())
                .judicialDecisionMakeAnOrderForWrittenRepresentations(
                    GAJudicialWrittenRepresentations.builder().writtenOption(
                        GAJudgeWrittenRepresentationsOptions.CONCURRENT_REPRESENTATIONS).build())
                .generalAppType(new GAApplicationType(applicationTypeSummeryJudgement()))
                .judicialConcurrentDateText(DUMMY_DATE)
                .isMultiParty(NO)
                .build();
        }

        private GeneralApplicationCaseData caseDataForJudgeDismissal(YesOrNo orderAgreement, YesOrNo isWithNotice, YesOrNo isCloaked,
                                                   YesOrNo isLipApplicant, YesOrNo isLipRespondent, YesOrNo isLipApp) {
            return GeneralApplicationCaseData.builder()
                .ccdCaseReference(CASE_REFERENCE)
                .generalAppRespondentAgreement(new GARespondentOrderAgreement().setHasAgreed(orderAgreement))
                .generalAppInformOtherParty(new GAInformOtherParty().setIsWithNotice(isWithNotice))
                .applicationIsCloaked(isCloaked)
                .isGaApplicantLip(isLipApplicant)
                .isGaRespondentOneLip(isLipRespondent)
                .applicantPartyName("App")
                .claimant1PartyName("CL")
                .defendant1PartyName("DEF")
                .generalAppRespondentSolicitors(isLipApp.equals(YES) ? lipRespondent() : respondentSolicitors())
                .judicialDecision(GAJudicialDecision.builder()
                                      .decision(GAJudgeDecisionOption.MAKE_AN_ORDER).build())
                .judicialDecisionMakeOrder(GAJudicialMakeAnOrder.builder()
                                               .makeAnOrder(GAJudgeMakeAnOrderOption.DISMISS_THE_APPLICATION).build())
                .generalAppApplnSolicitor(new GASolicitorDetailsGAspec()
                                              .setEmail(DUMMY_EMAIL))
                .businessProcess(BusinessProcess.builder().camundaEvent(JUDGES_DECISION).build())
                .generalAppParentCaseLink(GeneralAppParentCaseLink.builder()
                                              .caseReference(CASE_REFERENCE.toString()).build())
                .generalAppType(new GAApplicationType(applicationTypeToStrikeOut()))
                .generalAppPBADetails(GeneralApplicationPbaDetails.builder().build())
                .isMultiParty(NO)
                .build();
        }

        private GeneralApplicationCaseData caseDataListForHearing() {
            return GeneralApplicationCaseData.builder()
                .ccdCaseReference(CASE_REFERENCE)
                .judicialDecision(GAJudicialDecision.builder()
                                      .decision(GAJudgeDecisionOption.LIST_FOR_A_HEARING).build())
                .judicialDecisionMakeOrder(GAJudicialMakeAnOrder.builder()
                                               .makeAnOrder(GAJudgeMakeAnOrderOption.DISMISS_THE_APPLICATION).build())
                .generalAppApplnSolicitor(new GASolicitorDetailsGAspec()
                                              .setEmail(DUMMY_EMAIL))
                .businessProcess(BusinessProcess.builder().camundaEvent(JUDGES_DECISION).build())
                .generalAppParentCaseLink(GeneralAppParentCaseLink.builder()
                                              .caseReference(CASE_REFERENCE.toString()).build())
                .generalAppType(new GAApplicationType(applicationTypeToExtendTheClaim()))
                .isMultiParty(NO)
                .build();
        }

        private GeneralApplicationCaseData caseDataForCloakedApplicationWithSolicitorDataOnly() {
            return GeneralApplicationCaseData.builder()
                .ccdCaseReference(CASE_REFERENCE)
                .applicationIsCloaked(YES)
                .isMultiParty(NO)
                .judicialDecisionMakeOrder(GAJudicialMakeAnOrder.builder()
                                               .makeAnOrder(GIVE_DIRECTIONS_WITHOUT_HEARING).build())
                .judicialDecision(GAJudicialDecision.builder()
                                      .decision(GAJudgeDecisionOption.MAKE_AN_ORDER).build())
                .generalAppApplnSolicitor(new GASolicitorDetailsGAspec()
                                              .setEmail(DUMMY_EMAIL))
                .businessProcess(BusinessProcess.builder().camundaEvent(JUDGES_DECISION).build())
                .generalAppParentCaseLink(GeneralAppParentCaseLink.builder()
                                              .caseReference(CASE_REFERENCE.toString()).build())
                .generalAppType(new GAApplicationType(applicationTypeSummeryJudgement()))
                .build();
        }

        private GeneralApplicationCaseData caseDataWithSolicitorDataOnlyForApplicationUncloakedJudgeApproveOrEdit(YesOrNo orderAgreement,
                                                                                                YesOrNo isWithNotice,
                                                                                                YesOrNo isCloaked,
                                                                                                YesOrNo isGaLip,
                                                                                                YesOrNo isApplicantLip,
                                                                                                YesOrNo isRespondentLip) {
            return GeneralApplicationCaseData.builder()
                .ccdCaseReference(CASE_REFERENCE)
                .applicationIsCloaked(isCloaked)
                .generalAppRespondentSolicitors(isGaLip.equals(YES) ? lipRespondent() : respondentSolicitors())
                .judicialDecisionMakeOrder(GAJudicialMakeAnOrder.builder()
                                               .makeAnOrder(GAJudgeMakeAnOrderOption.APPROVE_OR_EDIT).build())
                .judicialDecision(GAJudicialDecision.builder()
                                      .decision(GAJudgeDecisionOption.MAKE_AN_ORDER).build())
                .generalAppApplnSolicitor(new GASolicitorDetailsGAspec()
                                              .setEmail(DUMMY_EMAIL))
                .isGaRespondentOneLip(isRespondentLip)
                .isGaApplicantLip(isApplicantLip)

                .businessProcess(BusinessProcess.builder().camundaEvent(JUDGES_DECISION).build())
                .generalAppParentCaseLink(GeneralAppParentCaseLink.builder()
                                              .caseReference(CASE_REFERENCE.toString()).build())
                .generalAppRespondentAgreement(new GARespondentOrderAgreement().setHasAgreed(orderAgreement))
                .generalAppInformOtherParty(new GAInformOtherParty().setIsWithNotice(isWithNotice))
                .generalAppType(new GAApplicationType(applicationTypeToStayTheClaim()))
                .isMultiParty(NO)
                .applicantPartyName("App")
                .claimant1PartyName("CL")
                .defendant1PartyName("DEF")
                .build();
        }

        private GeneralApplicationCaseData caseDataForApplicationUncloakedIsDismissed() {
            return GeneralApplicationCaseData.builder()
                .ccdCaseReference(CASE_REFERENCE)
                .applicationIsCloaked(NO)
                .generalAppRespondentAgreement(new GARespondentOrderAgreement().setHasAgreed(NO))
                .generalAppInformOtherParty(new GAInformOtherParty().setIsWithNotice(YES))
                .judicialDecisionMakeOrder(GAJudicialMakeAnOrder.builder()
                                               .makeAnOrder(GAJudgeMakeAnOrderOption.DISMISS_THE_APPLICATION).build())
                .judicialDecision(GAJudicialDecision.builder()
                                      .decision(GAJudgeDecisionOption.MAKE_AN_ORDER).build())
                .generalAppApplnSolicitor(new GASolicitorDetailsGAspec()
                                              .setEmail(DUMMY_EMAIL))
                .businessProcess(BusinessProcess.builder().camundaEvent(JUDGES_DECISION).build())
                .generalAppParentCaseLink(GeneralAppParentCaseLink.builder()
                                              .caseReference(CASE_REFERENCE.toString()).build())
                .isMultiParty(NO)
                .generalAppType(new GAApplicationType(applicationTypeToStrikeOut()))
                .build();
        }

        private GeneralApplicationCaseData caseDataForAmendStatementOfClaim() {
            return GeneralApplicationCaseData.builder()
                .ccdCaseReference(CASE_REFERENCE)
                .judicialDecision(GAJudicialDecision.builder()
                                      .decision(GAJudgeDecisionOption.LIST_FOR_A_HEARING).build())
                .isMultiParty(NO)
                .judicialDecisionMakeOrder(GAJudicialMakeAnOrder.builder()
                                               .makeAnOrder(GAJudgeMakeAnOrderOption.DISMISS_THE_APPLICATION).build())
                .generalAppApplnSolicitor(new GASolicitorDetailsGAspec()
                                              .setEmail(DUMMY_EMAIL))
                .businessProcess(BusinessProcess.builder().camundaEvent(JUDGES_DECISION).build())
                .generalAppParentCaseLink(GeneralAppParentCaseLink.builder()
                                              .caseReference(CASE_REFERENCE.toString()).build())
                .generalAppType(new GAApplicationType(applicationTypeToAmendStatmentOfClaim()))
                .build();
        }

        private GeneralApplicationCaseData caseDataForJudicialApprovalOfApplication(YesOrNo orderAgreement, YesOrNo isWithNotice) {
            return GeneralApplicationCaseData.builder()
                .ccdCaseReference(CASE_REFERENCE)
                .isMultiParty(NO)
                .generalAppRespondentAgreement(new GARespondentOrderAgreement().setHasAgreed(orderAgreement))
                .generalAppInformOtherParty(new GAInformOtherParty().setIsWithNotice(isWithNotice))
                .judicialDecisionMakeOrder(GAJudicialMakeAnOrder.builder()
                                               .makeAnOrder(GAJudgeMakeAnOrderOption.APPROVE_OR_EDIT).build())
                .generalAppRespondentSolicitors(respondentSolicitors())
                .generalAppApplnSolicitor(new GASolicitorDetailsGAspec()
                                              .setEmail(DUMMY_EMAIL))
                .businessProcess(BusinessProcess.builder().camundaEvent(JUDGES_DECISION).build())
                .generalAppParentCaseLink(GeneralAppParentCaseLink.builder()
                                              .caseReference(CASE_REFERENCE.toString()).build())
                .generalAppType(new GAApplicationType(applicationTypeToAmendStatmentOfClaim()))
                .generalAppPBADetails(GeneralApplicationPbaDetails.builder().build())
                .build();
        }

        private GeneralApplicationCaseData caseDataForJudicialDirectionOrderOfApplication(YesOrNo orderAgreement, YesOrNo isWithNotice) {
            return GeneralApplicationCaseData.builder()
                .ccdCaseReference(CASE_REFERENCE)
                .generalAppRespondentAgreement(new GARespondentOrderAgreement().setHasAgreed(orderAgreement))
                .generalAppInformOtherParty(new GAInformOtherParty().setIsWithNotice(isWithNotice))
                .judicialDecisionMakeOrder(GAJudicialMakeAnOrder.builder()
                                               .makeAnOrder(GIVE_DIRECTIONS_WITHOUT_HEARING).build())
                .generalAppRespondentSolicitors(respondentSolicitors())
                .generalAppApplnSolicitor(new GASolicitorDetailsGAspec()
                                              .setEmail(DUMMY_EMAIL))
                .businessProcess(BusinessProcess.builder().camundaEvent(JUDGES_DECISION).build())
                .generalAppParentCaseLink(GeneralAppParentCaseLink.builder()
                                              .caseReference(CASE_REFERENCE.toString()).build())
                .generalAppType(new GAApplicationType(applicationTypeToAmendStatmentOfClaim()))
                .generalAppPBADetails(GeneralApplicationPbaDetails.builder().build())
                .isMultiParty(NO)
                .build();
        }

        private GeneralApplicationCaseData caseDataForJudicialDirectionOrderOfApplicationWhenRespondentsArePresentInList(
            YesOrNo orderAgreement,
            YesOrNo isWithNotice,
            YesOrNo isCloaked,
            YesOrNo isGaLip,
            YesOrNo isApplicantLip,
            YesOrNo isRespondentLip) {
            return GeneralApplicationCaseData.builder()
                .ccdCaseReference(CASE_REFERENCE)
                .generalAppRespondentAgreement(new GARespondentOrderAgreement().setHasAgreed(orderAgreement))
                .generalAppInformOtherParty(new GAInformOtherParty().setIsWithNotice(isWithNotice))
                .applicationIsCloaked(isCloaked)
                .isGaApplicantLip(isApplicantLip)
                .applicantPartyName("App")
                .claimant1PartyName("CL")
                .defendant1PartyName("DEF")
                .isGaRespondentOneLip(isRespondentLip)
                .judicialDecisionMakeOrder(GAJudicialMakeAnOrder.builder()
                                               .makeAnOrder(GIVE_DIRECTIONS_WITHOUT_HEARING).build())
                .generalAppRespondentSolicitors(isGaLip.equals(YES) ? lipRespondent() : respondentSolicitors())
                .generalAppApplnSolicitor(new GASolicitorDetailsGAspec()
                                              .setEmail(DUMMY_EMAIL))
                .businessProcess(BusinessProcess.builder().camundaEvent(JUDGES_DECISION).build())
                .generalAppParentCaseLink(GeneralAppParentCaseLink.builder()
                                              .caseReference(CASE_REFERENCE.toString()).build())
                .generalAppType(new GAApplicationType(applicationTypeToAmendStatmentOfClaim()))
                .generalAppPBADetails(GeneralApplicationPbaDetails.builder().build())
                .isMultiParty(NO)
                .build();
        }

        private GeneralApplicationCaseData caseDataForApplicationsApprovedStrikeOutWhenRespondentsAreInList(
            YesOrNo orderAgreement,
            YesOrNo isWithNotice, YesOrNo isCloaked, String superClaimType) {
            return
                GeneralApplicationCaseData.builder()
                    .ccdCaseReference(CASE_REFERENCE)
                    .isMultiParty(NO)
                    .generalAppRespondentSolicitors(respondentSolicitors())
                    .generalAppSuperClaimType(superClaimType)
                    .generalAppRespondentAgreement(new GARespondentOrderAgreement()
                                                       .setHasAgreed(orderAgreement))
                    .generalAppInformOtherParty(new GAInformOtherParty().setIsWithNotice(isWithNotice))
                    .applicationIsCloaked(isCloaked)
                    .generalAppApplnSolicitor(new GASolicitorDetailsGAspec()
                                                  .setEmail(DUMMY_EMAIL))
                    .businessProcess(BusinessProcess.builder().camundaEvent(JUDGES_DECISION).build())
                    .generalAppParentCaseLink(GeneralAppParentCaseLink.builder()
                                                  .caseReference(CASE_REFERENCE.toString()).build())
                    .judicialDecisionMakeOrder(GAJudicialMakeAnOrder.builder()
                                                   .makeAnOrder(
                                                       GAJudgeMakeAnOrderOption.APPROVE_OR_EDIT).build())
                    .generalAppType(new GAApplicationType(applicationTypeToStrikeOut()))
                    .judicialConcurrentDateText(DUMMY_DATE)
                    .generalAppPBADetails(GeneralApplicationPbaDetails.builder().build())
                    .build();
        }

        private GeneralApplicationCaseData caseDataForSequentialWrittenRepInList() {
            return
                GeneralApplicationCaseData.builder()
                    .ccdCaseReference(CASE_REFERENCE)
                    .judicialDecision(GAJudicialDecision.builder()
                                          .decision(GAJudgeDecisionOption.LIST_FOR_A_HEARING).build())
                    .generalAppRespondentSolicitors(respondentSolicitors())
                    .generalAppApplnSolicitor(new GASolicitorDetailsGAspec()
                                                  .setEmail(DUMMY_EMAIL))
                    .businessProcess(BusinessProcess.builder().camundaEvent(JUDGES_DECISION).build())
                    .generalAppParentCaseLink(GeneralAppParentCaseLink.builder()
                                                  .caseReference(CASE_REFERENCE.toString()).build())
                    .judicialDecisionMakeAnOrderForWrittenRepresentations(
                        GAJudicialWrittenRepresentations.builder().writtenOption(
                            GAJudgeWrittenRepresentationsOptions.SEQUENTIAL_REPRESENTATIONS).build())
                    .generalAppType(new GAApplicationType(applicationTypeSummeryJudgement()))
                    .judicialConcurrentDateText(DUMMY_DATE)
                    .isMultiParty(NO)
                    .generalAppPBADetails(GeneralApplicationPbaDetails.builder().build())
                    .build();
        }

        private GeneralApplicationCaseData caseDataForApplicationsApprovedWhenRespondentsAreInList(YesOrNo orderAgreement,
                                                                                 YesOrNo isWithNotice) {
            return
                GeneralApplicationCaseData.builder()
                    .ccdCaseReference(CASE_REFERENCE)
                    .generalAppRespondentSolicitors(respondentSolicitors())
                    .generalAppRespondentAgreement(new GARespondentOrderAgreement()
                                                       .setHasAgreed(orderAgreement))
                    .generalAppInformOtherParty(new GAInformOtherParty().setIsWithNotice(isWithNotice))
                    .generalAppApplnSolicitor(new GASolicitorDetailsGAspec()
                                                  .setEmail(DUMMY_EMAIL))
                    .businessProcess(BusinessProcess.builder().camundaEvent(JUDGES_DECISION).build())
                    .generalAppParentCaseLink(GeneralAppParentCaseLink.builder()
                                                  .caseReference(CASE_REFERENCE.toString()).build())
                    .judicialDecisionMakeOrder(GAJudicialMakeAnOrder.builder()
                                                   .makeAnOrder(
                                                       GAJudgeMakeAnOrderOption.APPROVE_OR_EDIT).build())
                    .generalAppType(new GAApplicationType(applicationTypeSummeryJudgement()))
                    .judicialConcurrentDateText(DUMMY_DATE)
                    .generalAppPBADetails(GeneralApplicationPbaDetails.builder().build())
                    .isMultiParty(NO)
                    .build();
        }

        private GeneralApplicationCaseData caseDataForListForHearingRespondentsAreInList() {
            return
                GeneralApplicationCaseData.builder()
                    .ccdCaseReference(CASE_REFERENCE)
                    .generalAppRespondentSolicitors(respondentSolicitors())
                    .generalAppApplnSolicitor(new GASolicitorDetailsGAspec()
                                                  .setEmail(DUMMY_EMAIL))
                    .businessProcess(BusinessProcess.builder().camundaEvent(JUDGES_DECISION).build())
                    .generalAppParentCaseLink(GeneralAppParentCaseLink.builder()
                                                  .caseReference(CASE_REFERENCE.toString()).build())
                    .judicialDecision(GAJudicialDecision.builder()
                                          .decision(GAJudgeDecisionOption.LIST_FOR_A_HEARING).build())
                    .generalAppType(new GAApplicationType(applicationTypeSummeryJudgement()))
                    .judicialConcurrentDateText(DUMMY_DATE)
                    .isMultiParty(NO)
                    .build();
        }

        private GeneralApplicationCaseData caseDataForCaseDismissedByJudgeRespondentsAreInList(YesOrNo orderAgreement,
                                                                             YesOrNo isWithNotice,
                                                                             YesOrNo isCloaked) {
            return
                GeneralApplicationCaseData.builder()
                    .ccdCaseReference(CASE_REFERENCE)
                    .generalAppRespondentAgreement(new GARespondentOrderAgreement()
                                                       .setHasAgreed(orderAgreement))
                    .generalAppInformOtherParty(new GAInformOtherParty().setIsWithNotice(isWithNotice))
                    .applicationIsCloaked(isCloaked)
                    .generalAppRespondentSolicitors(respondentSolicitors())
                    .generalAppApplnSolicitor(new GASolicitorDetailsGAspec()
                                                  .setEmail(DUMMY_EMAIL))
                    .businessProcess(BusinessProcess.builder().camundaEvent(JUDGES_DECISION).build())
                    .generalAppParentCaseLink(GeneralAppParentCaseLink.builder()
                                                  .caseReference(CASE_REFERENCE.toString()).build())
                    .judicialDecision(GAJudicialDecision.builder()
                                          .decision(GAJudgeDecisionOption.MAKE_AN_ORDER).build())
                    .judicialDecisionMakeOrder(GAJudicialMakeAnOrder.builder().makeAnOrder(
                        GAJudgeMakeAnOrderOption.DISMISS_THE_APPLICATION
                    ).build())
                    .generalAppType(new GAApplicationType(applicationTypeSummeryJudgement()))
                    .judicialConcurrentDateText(DUMMY_DATE)
                    .generalAppPBADetails(GeneralApplicationPbaDetails.builder().build())
                    .isMultiParty(NO)
                    .build();
        }

        private GeneralApplicationCaseData caseDataForApprovedCloakStrikeOutWhenRespondentsArePresentInList(String superClaimType) {
            return
                GeneralApplicationCaseData.builder()
                    .ccdCaseReference(CASE_REFERENCE)
                    .generalAppRespondentSolicitors(respondentSolicitors())
                    .generalAppSuperClaimType(superClaimType)
                    .generalAppApplnSolicitor(new GASolicitorDetailsGAspec()
                                                  .setEmail(DUMMY_EMAIL))
                    .businessProcess(BusinessProcess.builder().camundaEvent(JUDGES_DECISION).build())
                    .generalAppParentCaseLink(GeneralAppParentCaseLink.builder()
                                                  .caseReference(CASE_REFERENCE.toString()).build())
                    .judicialDecisionMakeOrder(GAJudicialMakeAnOrder.builder().makeAnOrder(
                        GAJudgeMakeAnOrderOption.APPROVE_OR_EDIT
                    ).build())
                    .judicialDecision(GAJudicialDecision.builder()
                                          .decision(GAJudgeDecisionOption.MAKE_AN_ORDER).build())
                    .generalAppType(new GAApplicationType(applicationTypeToStrikeOut()))
                    .applicationIsCloaked(YES)
                    .judicialConcurrentDateText(DUMMY_DATE)
                    .isMultiParty(NO)
                    .build();
        }

        private GeneralApplicationCaseData caseDataForJudgeApprovedOrderCloakWhenRespondentsArePresentInList() {
            return
                GeneralApplicationCaseData.builder()
                    .ccdCaseReference(CASE_REFERENCE)
                    .generalAppRespondentSolicitors(respondentSolicitors())
                    .generalAppApplnSolicitor(new GASolicitorDetailsGAspec()
                                                  .setEmail(DUMMY_EMAIL))
                    .businessProcess(BusinessProcess.builder().camundaEvent(JUDGES_DECISION).build())
                    .generalAppParentCaseLink(GeneralAppParentCaseLink.builder()
                                                  .caseReference(CASE_REFERENCE.toString()).build())
                    .judicialDecisionMakeOrder(GAJudicialMakeAnOrder.builder().makeAnOrder(
                        GAJudgeMakeAnOrderOption.APPROVE_OR_EDIT
                    ).build())
                    .judicialDecision(GAJudicialDecision.builder()
                                          .decision(GAJudgeDecisionOption.MAKE_AN_ORDER).build())
                    .generalAppType(new GAApplicationType(applicationTypeSummeryJudgement()))
                    .applicationIsCloaked(YES)
                    .judicialConcurrentDateText(DUMMY_DATE)
                    .isMultiParty(NO)
                    .build();
        }

        private GeneralApplicationCaseData caseDataForJudgeDismissTheApplicationCloakWhenRespondentsArePresentInList() {
            return
                GeneralApplicationCaseData.builder()
                    .ccdCaseReference(CASE_REFERENCE)
                    .generalAppRespondentSolicitors(respondentSolicitors())
                    .generalAppApplnSolicitor(new GASolicitorDetailsGAspec()
                                                  .setEmail(DUMMY_EMAIL))
                    .businessProcess(BusinessProcess.builder().camundaEvent(JUDGES_DECISION).build())
                    .generalAppParentCaseLink(GeneralAppParentCaseLink.builder()
                                                  .caseReference(CASE_REFERENCE.toString()).build())
                    .judicialDecisionMakeOrder(GAJudicialMakeAnOrder.builder().makeAnOrder(
                        GAJudgeMakeAnOrderOption.DISMISS_THE_APPLICATION
                    ).build())
                    .judicialDecision(GAJudicialDecision.builder()
                                          .decision(GAJudgeDecisionOption.MAKE_AN_ORDER).build())
                    .generalAppType(new GAApplicationType(applicationTypeSummeryJudgement()))
                    .applicationIsCloaked(YES)
                    .isMultiParty(NO)
                    .judicialConcurrentDateText(DUMMY_DATE)
                    .build();
        }

        private GeneralApplicationCaseData caseDataFreeFormOrder() {
            return GeneralApplicationCaseData.builder()
                .judicialDecision(GAJudicialDecision.builder()
                                      .decision(GAJudgeDecisionOption.FREE_FORM_ORDER).build())
                .generalAppRespondentSolicitors(respondentSolicitors())
                .ccdCaseReference(CASE_REFERENCE)
                .isGaApplicantLip(NO)
                .isGaRespondentOneLip(NO)
                .applicantPartyName("App")
                .claimant1PartyName("CL")
                .defendant1PartyName("DEF")
                .generalAppInformOtherParty(new GAInformOtherParty().setIsWithNotice(YES))
                .generalAppApplnSolicitor(new GASolicitorDetailsGAspec()
                                              .setEmail(DUMMY_EMAIL))
                .businessProcess(BusinessProcess.builder().camundaEvent(JUDGES_DECISION).build())
                .generalAppParentCaseLink(GeneralAppParentCaseLink.builder()
                                              .caseReference(CASE_REFERENCE.toString()).build())
                .generalAppType(new GAApplicationType(applicationTypeSummeryJudgement()))
                .isMultiParty(NO)
                .ccdState(CaseState.APPLICATION_SUBMITTED_AWAITING_JUDICIAL_DECISION)
                .businessProcess(BusinessProcess.builder().camundaEvent(JUDGES_DECISION)
                                     .activityId("StartRespondentNotificationProcessMakeDecision")
                                     .build())
                .build();
        }

        private Map<String, String> notificationPropertiesSummeryJudgement() {
            HashMap<String, String> properties = new HashMap<>(Map.of(
                NotificationDataGA.CASE_REFERENCE, CASE_REFERENCE.toString(),
                NotificationDataGA.GENAPP_REFERENCE, CASE_REFERENCE.toString(),
                NotificationDataGA.PARTY_REFERENCE, PARTY_REFERENCE,
                NotificationDataGA.GA_APPLICATION_TYPE,
                GeneralApplicationTypes.SUMMARY_JUDGEMENT.getDisplayedValue()
            ));
            properties.put(NotificationDataGA.WELSH_CONTACT, "E-bost: ymholiadaucymraeg@justice.gov.uk");
            properties.put(NotificationDataGA.WELSH_HMCTS_SIGNATURE, "Hawliadau am Arian yn y Llys Sifil Ar-lein \n Gwasanaeth Llysoedd a Thribiwnlysoedd EF");
            properties.put(NotificationDataGA.WELSH_OPENING_HOURS, "Dydd Llun i ddydd Iau, 9am – 5pm, dydd Gwener, 9am – 4.30pm");
            properties.put(NotificationDataGA.WELSH_PHONE_CONTACT, "Ffôn: 0300 303 5174");
            properties.put(NotificationDataGA.SPEC_CONTACT, "Email: contactocmc@justice.gov.uk");
            properties.put(NotificationDataGA.SPEC_UNSPEC_CONTACT, RAISE_QUERY_LR);
            properties.put(NotificationDataGA.HMCTS_SIGNATURE, "Online Civil Claims \n HM Courts & Tribunal Service");
            properties.put(NotificationDataGA.OPENING_HOURS, "Monday to Friday, 8.30am to 5pm");
            properties.put(NotificationDataGA.PHONE_CONTACT, "For anything related to hearings, call 0300 123 5577 "
                + "\n For all other matters, call 0300 123 7050");
            return properties;
        }

        private Map<String, String> notificationPropertiesSummeryJudgementConcurrent() {
            HashMap<String, String> properties = new HashMap<>(Map.of(
                NotificationDataGA.CASE_REFERENCE, CASE_REFERENCE.toString(),
                NotificationDataGA.GENAPP_REFERENCE, CASE_REFERENCE.toString(),
                NotificationDataGA.PARTY_REFERENCE, PARTY_REFERENCE,
                NotificationDataGA.GA_APPLICATION_TYPE,
                GeneralApplicationTypes.SUMMARY_JUDGEMENT.getDisplayedValue()
            ));
            properties.put(NotificationDataGA.WELSH_CONTACT, "E-bost: ymholiadaucymraeg@justice.gov.uk");
            properties.put(NotificationDataGA.WELSH_HMCTS_SIGNATURE, "Hawliadau am Arian yn y Llys Sifil Ar-lein \n Gwasanaeth Llysoedd a Thribiwnlysoedd EF");
            properties.put(NotificationDataGA.WELSH_OPENING_HOURS, "Dydd Llun i ddydd Iau, 9am – 5pm, dydd Gwener, 9am – 4.30pm");
            properties.put(NotificationDataGA.WELSH_PHONE_CONTACT, "Ffôn: 0300 303 5174");
            properties.put(NotificationDataGA.SPEC_CONTACT, "Email: contactocmc@justice.gov.uk");
            properties.put(NotificationDataGA.SPEC_UNSPEC_CONTACT, RAISE_QUERY_LR);
            properties.put(NotificationDataGA.HMCTS_SIGNATURE, "Online Civil Claims \n HM Courts & Tribunal Service");
            properties.put(NotificationDataGA.OPENING_HOURS, "Monday to Friday, 8.30am to 5pm");
            properties.put(NotificationDataGA.PHONE_CONTACT, "For anything related to hearings, call 0300 123 5577 "
                + "\n For all other matters, call 0300 123 7050");
            return properties;
        }

        private Map<String, String> notificationPropertiesToStrikeOut() {
            HashMap<String, String> properties = new HashMap<>(Map.of(
                NotificationDataGA.CASE_REFERENCE, CASE_REFERENCE.toString(),
                NotificationDataGA.GENAPP_REFERENCE, CASE_REFERENCE.toString(),
                NotificationDataGA.PARTY_REFERENCE, PARTY_REFERENCE,
                NotificationDataGA.GA_APPLICATION_TYPE,
                GeneralApplicationTypes.STRIKE_OUT.getDisplayedValue()
            ));
            properties.put(NotificationDataGA.WELSH_CONTACT, "E-bost: ymholiadaucymraeg@justice.gov.uk");
            properties.put(NotificationDataGA.WELSH_HMCTS_SIGNATURE, "Hawliadau am Arian yn y Llys Sifil Ar-lein \n Gwasanaeth Llysoedd a Thribiwnlysoedd EF");
            properties.put(NotificationDataGA.WELSH_OPENING_HOURS, "Dydd Llun i ddydd Iau, 9am – 5pm, dydd Gwener, 9am – 4.30pm");
            properties.put(NotificationDataGA.WELSH_PHONE_CONTACT, "Ffôn: 0300 303 5174");
            properties.put(NotificationDataGA.SPEC_CONTACT, "Email: contactocmc@justice.gov.uk");
            properties.put(NotificationDataGA.SPEC_UNSPEC_CONTACT, RAISE_QUERY_LR);
            properties.put(NotificationDataGA.HMCTS_SIGNATURE, "Online Civil Claims \n HM Courts & Tribunal Service");
            properties.put(NotificationDataGA.OPENING_HOURS, "Monday to Friday, 8.30am to 5pm");
            properties.put(NotificationDataGA.PHONE_CONTACT, "For anything related to hearings, call 0300 123 5577 "
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
                NotificationDataGA.GENAPP_REFERENCE, CASE_REFERENCE.toString(),
                NotificationDataGA.PARTY_REFERENCE, PARTY_REFERENCE,
                NotificationDataGA.GA_APPLICATION_TYPE,
                GeneralApplicationTypes.EXTEND_TIME.getDisplayedValue()
            ));
            properties.put(NotificationDataGA.WELSH_CONTACT, "E-bost: ymholiadaucymraeg@justice.gov.uk");
            properties.put(NotificationDataGA.WELSH_HMCTS_SIGNATURE, "Hawliadau am Arian yn y Llys Sifil Ar-lein \n Gwasanaeth Llysoedd a Thribiwnlysoedd EF");
            properties.put(NotificationDataGA.WELSH_OPENING_HOURS, "Dydd Llun i ddydd Iau, 9am – 5pm, dydd Gwener, 9am – 4.30pm");
            properties.put(NotificationDataGA.WELSH_PHONE_CONTACT, "Ffôn: 0300 303 5174");
            properties.put(NotificationDataGA.SPEC_CONTACT, "Email: contactocmc@justice.gov.uk");
            properties.put(NotificationDataGA.SPEC_UNSPEC_CONTACT, RAISE_QUERY_LR);
            properties.put(NotificationDataGA.HMCTS_SIGNATURE, "Online Civil Claims \n HM Courts & Tribunal Service");
            properties.put(NotificationDataGA.OPENING_HOURS, "Monday to Friday, 8.30am to 5pm");
            properties.put(NotificationDataGA.PHONE_CONTACT, "For anything related to hearings, call 0300 123 5577 "
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
                NotificationDataGA.PARTY_REFERENCE, PARTY_REFERENCE,
                NotificationDataGA.GA_APPLICATION_TYPE,
                GeneralApplicationTypes.AMEND_A_STMT_OF_CASE.getDisplayedValue()
            ));
            properties.put(NotificationDataGA.WELSH_CONTACT, "E-bost: ymholiadaucymraeg@justice.gov.uk");
            properties.put(NotificationDataGA.WELSH_HMCTS_SIGNATURE, "Hawliadau am Arian yn y Llys Sifil Ar-lein \n Gwasanaeth Llysoedd a Thribiwnlysoedd EF");
            properties.put(NotificationDataGA.WELSH_OPENING_HOURS, "Dydd Llun i ddydd Iau, 9am – 5pm, dydd Gwener, 9am – 4.30pm");
            properties.put(NotificationDataGA.WELSH_PHONE_CONTACT, "Ffôn: 0300 303 5174");
            properties.put(NotificationDataGA.SPEC_CONTACT, "Email: contactocmc@justice.gov.uk");
            properties.put(NotificationDataGA.SPEC_UNSPEC_CONTACT, RAISE_QUERY_LR);
            properties.put(NotificationDataGA.HMCTS_SIGNATURE, "Online Civil Claims \n HM Courts & Tribunal Service");
            properties.put(NotificationDataGA.OPENING_HOURS, "Monday to Friday, 8.30am to 5pm");
            properties.put(NotificationDataGA.PHONE_CONTACT, "For anything related to hearings, call 0300 123 5577 "
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
                NotificationDataGA.PARTY_REFERENCE, PARTY_REFERENCE,
                NotificationDataGA.GA_APPLICATION_TYPE,
                GeneralApplicationTypes.RELIEF_FROM_SANCTIONS.getDisplayedValue()
            ));
            properties.put(NotificationDataGA.WELSH_CONTACT, "E-bost: ymholiadaucymraeg@justice.gov.uk");
            properties.put(NotificationDataGA.WELSH_HMCTS_SIGNATURE, "Hawliadau am Arian yn y Llys Sifil Ar-lein \n Gwasanaeth Llysoedd a Thribiwnlysoedd EF");
            properties.put(NotificationDataGA.WELSH_OPENING_HOURS, "Dydd Llun i ddydd Iau, 9am – 5pm, dydd Gwener, 9am – 4.30pm");
            properties.put(NotificationDataGA.WELSH_PHONE_CONTACT, "Ffôn: 0300 303 5174");
            properties.put(NotificationDataGA.SPEC_CONTACT, "Email: contactocmc@justice.gov.uk");
            properties.put(NotificationDataGA.SPEC_UNSPEC_CONTACT, RAISE_QUERY_LR);
            properties.put(NotificationDataGA.HMCTS_SIGNATURE, "Online Civil Claims \n HM Courts & Tribunal Service");
            properties.put(NotificationDataGA.OPENING_HOURS, "Monday to Friday, 8.30am to 5pm");
            properties.put(NotificationDataGA.PHONE_CONTACT, "For anything related to hearings, call 0300 123 5577 "
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

        @BeforeEach
        public void setup() {
            when(gaForLipService.isLipApp(any())).thenReturn(false);
        }

        @Test
        void notificationShouldSend_IfWithoutNotice_ApplicationContinuesToBeCloaked() {
            GeneralApplicationCaseData caseData = caseDataForJudicialRequestForInformationOfApplication(NO, NO, YES, NO, NO,
                    SEND_APP_TO_OTHER_PARTY).toBuilder().isMultiParty(NO).build();

            when(time.now()).thenReturn(responseDate);
            when(caseDetailsConverter.toGeneralApplicationCaseData(any())).thenReturn(GeneralApplicationCaseData.builder().ccdState(CaseState.CASE_PROGRESSION).build());
            when(solicitorEmailValidation.validateSolicitorEmail(any(), any()))
                .thenReturn(caseData);

            var responseCaseData = judicialNotificationService.sendNotification(caseData, APPLICANT);

            assertThat(responseCaseData.getJudicialDecisionRequestMoreInfo().getDeadlineForMoreInfoSubmission())
                .isEqualTo(deadline.toString());

            verify(notificationService, times(1)).sendMail(
                DUMMY_EMAIL,
                "general-application-apps-judicial-notification-template-id",
                notificationPropertiesToStayTheClaim(),
                "general-apps-judicial-notification-make-decision-" + CASE_REFERENCE
            );
        }

        @Test
        void notificationShouldSend_IfWithNotice() {
            GeneralApplicationCaseData caseData = caseDataForJudicialRequestForInformationOfApplication(NO, YES, NO, NO, NO,
                                                                                      REQUEST_MORE_INFORMATION);

            when(caseDetailsConverter.toGeneralApplicationCaseData(any())).thenReturn(GeneralApplicationCaseData.builder().ccdState(CaseState.CASE_PROGRESSION).build());
            when(solicitorEmailValidation.validateSolicitorEmail(any(), any())).thenReturn(caseData);

            judicialNotificationService.sendNotification(caseData, APPLICANT);

            verify(notificationService, times(1)).sendMail(
                DUMMY_EMAIL,
                "general-application-apps-judicial-notification-template-id",
                notificationPropertiesToStayTheClaim(),
                "general-apps-judicial-notification-make-decision-" + CASE_REFERENCE
            );
        }

        @Test
        void notificationShouldSend_ApplicantRespondent_When_RequestForInformation_withConsentOrder() {

            GeneralApplicationCaseData caseData = caseDataForJudicialRequestForInformationOfApplication(YES, YES, YES, NO, NO,
                                                                                      REQUEST_MORE_INFORMATION)
                .toBuilder()
                .generalAppConsentOrder(YES)
                .ccdState(CaseState.APPLICATION_SUBMITTED_AWAITING_JUDICIAL_DECISION)
                .build();

            when(caseDetailsConverter.toGeneralApplicationCaseData(any())).thenReturn(GeneralApplicationCaseData.builder().ccdState(CaseState.CASE_PROGRESSION).build());
            when(solicitorEmailValidation.validateSolicitorEmail(any(), any()))
                .thenReturn(caseData);

            judicialNotificationService.sendNotification(caseData, APPLICANT);
            judicialNotificationService.sendNotification(caseData, RESPONDENT);

            verify(notificationService, times(3)).sendMail(
                DUMMY_EMAIL,
                "general-application-apps-judicial-notification-template-id",
                notificationPropertiesToStayTheClaim(),
                "general-apps-judicial-notification-make-decision-" + CASE_REFERENCE
            );
        }

        @Test
        void notificationShouldSend_ApplicantAndRespondent_When_RequestForInformation_withOutConsentOrder() {

            GeneralApplicationCaseData caseData = caseDataForJudicialRequestForInformationOfApplication(NO, NO, YES, NO, NO,
                                                                                      REQUEST_MORE_INFORMATION);
            caseData = caseData.toBuilder().ccdState(CaseState.APPLICATION_SUBMITTED_AWAITING_JUDICIAL_DECISION).build();

            when(caseDetailsConverter.toGeneralApplicationCaseData(any())).thenReturn(GeneralApplicationCaseData.builder().ccdState(CaseState.CASE_PROGRESSION).build());
            when(solicitorEmailValidation.validateSolicitorEmail(any(), any()))
                .thenReturn(caseData);

            judicialNotificationService.sendNotification(caseData, APPLICANT);
            judicialNotificationService.sendNotification(caseData, RESPONDENT);

            verify(notificationService, times(3)).sendMail(
                DUMMY_EMAIL,
                "general-application-apps-judicial-notification-template-id",
                notificationPropertiesToStayTheClaim(),
                "general-apps-judicial-notification-make-decision-" + CASE_REFERENCE
            );
        }

        @Test
        void sendNotificationOnlyToClaimant_IfRespondentNotPresent_RequestMoreInfo() {

            GeneralApplicationCaseData caseData = caseDataForJudicialRequestForInformationOfApplication(
                NO, YES, NO, NO, NO, REQUEST_MORE_INFORMATION).toBuilder()
                .isMultiParty(NO)
                .generalAppRespondentSolicitors(List.of()).build();

            when(caseDetailsConverter.toGeneralApplicationCaseData(any())).thenReturn(GeneralApplicationCaseData.builder().ccdState(CaseState.CASE_PROGRESSION).build());
            when(solicitorEmailValidation.validateSolicitorEmail(any(), any()))
                .thenReturn(caseData);

            judicialNotificationService.sendNotification(caseData, APPLICANT);

            verify(notificationService, times(1)).sendMail(
                DUMMY_EMAIL,
                "general-application-apps-judicial-notification-template-id",
                notificationPropertiesToStayTheClaim(),
                "general-apps-judicial-notification-make-decision-" + CASE_REFERENCE
            );
        }

        @Test
        void shouldSendAdditionalPaymentNotification_UncloakedApplication_BeforeAdditionalPaymentMade() {

            GeneralApplicationCaseData caseData = caseDataForJudicialRequestForInformationOfApplication(NO, NO, NO, NO, NO,
                                                                                      SEND_APP_TO_OTHER_PARTY)
                .toBuilder().isMultiParty(NO).build();

            when(caseDetailsConverter.toGeneralApplicationCaseData(any())).thenReturn(GeneralApplicationCaseData.builder().ccdState(CaseState.CASE_PROGRESSION).build());
            when(solicitorEmailValidation.validateSolicitorEmail(any(), any()))
                .thenReturn(caseData);

            judicialNotificationService.sendNotification(caseData, APPLICANT);

            verify(notificationService, times(1)).sendMail(
                DUMMY_EMAIL,
                "general-application-apps-judicial-notification-template-id",
                notificationPropertiesToStayTheClaim(),
                "general-apps-judicial-notification-make-decision-" + CASE_REFERENCE
            );
        }

        @Test
        void shouldSendAdditionalPaymentNotification_Lip_UncloakedApplication_BeforeAdditionalPaymentMade() {

            GeneralApplicationCaseData caseData = caseDataForJudicialRequestForInformationOfApplication(NO, NO, NO, YES, NO,
                                                                                      SEND_APP_TO_OTHER_PARTY);
            when(solicitorEmailValidation.validateSolicitorEmail(any(), any()))
                .thenReturn(caseData);
            when(caseDetailsConverter.toGeneralApplicationCaseData(any())).thenReturn(GeneralApplicationCaseData.builder().build());
            when(gaForLipService.isLipApp(any())).thenReturn(true);
            judicialNotificationService.sendNotification(caseData, APPLICANT);

            verify(notificationService, times(1)).sendMail(
                DUMMY_EMAIL,
                "ga-judicial-notification-applicant-template-lip-id",
                notificationPropertiesToStayTheClaimLip(),
                "general-apps-judicial-notification-make-decision-" + CASE_REFERENCE
            );
        }

        @Test
        void shouldSendAdditionalPaymentNotification_Lip_UncloakedApplication_BeforeAdditionalPaymentMade_WhenDefendantMakes_Claim() {
            when(gaForLipService.isLipResp(any())).thenReturn(true);
            when(gaForLipService.isGaForLip(any())).thenReturn(true);
            when(gaForLipService.isLipApp(any())).thenReturn(true);
            GeneralApplicationCaseData caseData = caseDataForJudicialRequestForInformationOfApplication(NO, YES, NO, YES, YES,
                                                                                      REQUEST_MORE_INFORMATION
            ).toBuilder().ccdState(ORDER_MADE)
                .businessProcess(BusinessProcess.builder().camundaEvent(JUDGES_DECISION)
                                     .activityId("StartRespondentNotificationProcessMakeDecision")
                                     .build()).judicialDecision(GAJudicialDecision.builder()
                                                                    .decision(GAJudgeDecisionOption.MAKE_AN_ORDER)
                                                                    .build())
                .judicialDecisionMakeOrder(GAJudicialMakeAnOrder.builder().makeAnOrder(APPROVE_OR_EDIT)
                                               .build()).parentClaimantIsApplicant(NO).build();

            when(solicitorEmailValidation.validateSolicitorEmail(any(), any()))
                .thenReturn(caseData);
            when(caseDetailsConverter.toGeneralApplicationCaseData(any())).thenReturn(GeneralApplicationCaseData.builder().build());

            judicialNotificationService.sendNotification(caseData, RESPONDENT);

            HashMap<String, String> properties = new HashMap<>(Map.of(
                "ClaimantvDefendant", "CL v DEF",
                "claimReferenceNumber", "111111",
                "GenAppclaimReferenceNumber", "111111",
                "generalAppType", "Stay the claim",
                "partyReferences", "Claimant Reference: Not provided - Defendant Reference: Not provided",
                "respondentName", "CL"
            ));
            properties.put(NotificationDataGA.WELSH_CONTACT, "E-bost: ymholiadaucymraeg@justice.gov.uk");
            properties.put(NotificationDataGA.WELSH_HMCTS_SIGNATURE, "Hawliadau am Arian yn y Llys Sifil Ar-lein \n Gwasanaeth Llysoedd a Thribiwnlysoedd EF");
            properties.put(NotificationDataGA.WELSH_OPENING_HOURS, "Dydd Llun i ddydd Iau, 9am – 5pm, dydd Gwener, 9am – 4.30pm");
            properties.put(NotificationDataGA.WELSH_PHONE_CONTACT, "Ffôn: 0300 303 5174");
            properties.put(NotificationDataGA.SPEC_CONTACT, "Email: contactocmc@justice.gov.uk");
            properties.put(NotificationDataGA.SPEC_UNSPEC_CONTACT, "Email for Specified Claims: contactocmc@justice.gov.uk "
                + "\n Email for Damages Claims: damagesclaims@justice.gov.uk");
            properties.put(NotificationDataGA.HMCTS_SIGNATURE, "Online Civil Claims \n HM Courts & Tribunal Service");
            properties.put(NotificationDataGA.OPENING_HOURS, "Monday to Friday, 8.30am to 5pm");
            properties.put(NotificationDataGA.PHONE_CONTACT, "For anything related to hearings, call 0300 123 5577 "
                + "\n For all other matters, call 0300 123 7050");

            verify(notificationService, times(1)).sendMail(
                DUMMY_EMAIL,
                "general-application-apps-judicial-notification-template-lip-id",
                properties,
                "general-apps-judicial-notification-make-decision-" + CASE_REFERENCE
            );
        }

    }

    @Test
    void testIsNotificationCriteriaSatisfied() {
        assertThat(isNotificationCriteriaSatisfied(getCaseData(NO, NO, null))).isFalse();
        assertThat(isNotificationCriteriaSatisfied(getCaseData(NO, NO, RESPONDENT_EMAIL))).isFalse();
        assertThat(isNotificationCriteriaSatisfied(getCaseData(NO, NO, null))).isFalse();
        assertThat(isNotificationCriteriaSatisfied(getCaseData(NO, YES, null))).isFalse();
        assertThat(isNotificationCriteriaSatisfied(getCaseData(NO, YES, RESPONDENT_EMAIL))).isTrue();
        assertThat(isNotificationCriteriaSatisfied(getCaseData(YES, NO, RESPONDENT_EMAIL))).isFalse();
        assertThat(isNotificationCriteriaSatisfied(getCaseData(YES, NO, null))).isFalse();
        assertThat(isNotificationCriteriaSatisfied(getCaseData(YES, NO, RESPONDENT_EMAIL))).isFalse();
        assertThat(isNotificationCriteriaSatisfied(getCaseData(YES, YES, null))).isFalse();
        assertThat(isNotificationCriteriaSatisfied(getCaseData(YES, YES, RESPONDENT_EMAIL))).isFalse();
    }

    private GeneralApplicationCaseData getCaseData(YesOrNo isUrgent, YesOrNo informOtherParty, String recipient) {
        return GeneralApplicationCaseData.builder()
            .generalAppUrgencyRequirement(new GAUrgencyRequirement().setGeneralAppUrgency(isUrgent))
            .generalAppInformOtherParty(new GAInformOtherParty().setIsWithNotice(informOtherParty))
            .generalAppRespondentSolicitors(wrapElements(new GASolicitorDetailsGAspec()
                                                             .setEmail(recipient)))
            .build();
    }

    private GeneralApplicationCaseData caseDataForJudicialRequestForInformationOfApplication(
        YesOrNo isRespondentOrderAgreement, YesOrNo isWithNotice, YesOrNo isCloaked,
        YesOrNo isLipApp, YesOrNo isLipRespondent, GAJudgeRequestMoreInfoOption gaJudgeRequestMoreInfoOption) {
        GASolicitorDetailsGAspec lr = new GASolicitorDetailsGAspec().setEmail(DUMMY_EMAIL);
        GASolicitorDetailsGAspec lip = new GASolicitorDetailsGAspec().setEmail(DUMMY_EMAIL)
                .setForename("LipF").setSurname(Optional.of("LipS"));
        return GeneralApplicationCaseData.builder()
            .ccdCaseReference(CASE_REFERENCE)
            .generalAppRespondentSolicitors(isLipRespondent.equals(YES) ? lipRespondent() : respondentSolicitors())
            .applicationIsCloaked(isCloaked)
            .isMultiParty(NO)
            .judicialDecision(GAJudicialDecision.builder()
                                  .decision(GAJudgeDecisionOption.REQUEST_MORE_INFO).build())
            .judicialDecisionRequestMoreInfo(GAJudicialRequestMoreInfo.builder()
                                                 .requestMoreInfoOption(gaJudgeRequestMoreInfoOption)
                                                 .judgeRequestMoreInfoText("Test")
                                                 .judgeRequestMoreInfoByDate(LocalDate.now())
                                                 .deadlineForMoreInfoSubmission(deadline).build())
            .generalAppRespondentAgreement(new GARespondentOrderAgreement()
                                               .setHasAgreed(isRespondentOrderAgreement))
            .generalAppInformOtherParty(new GAInformOtherParty().setIsWithNotice(isWithNotice))
            .generalAppApplnSolicitor(isLipApp.equals(YES) ? lip : lr)
            .isGaApplicantLip(isLipApp)
            .isGaRespondentOneLip(isLipRespondent)
            .applicantPartyName("App")
            .claimant1PartyName("CL")
            .defendant1PartyName("DEF")
            .businessProcess(BusinessProcess.builder().camundaEvent(JUDGES_DECISION).build())
            .generalAppParentCaseLink(GeneralAppParentCaseLink.builder()
                                          .caseReference(CASE_REFERENCE.toString()).build())
            .generalAppType(new GAApplicationType(applicationTypeToStayTheClaim()))
            .generalAppPBADetails(GeneralApplicationPbaDetails.builder().build())
            .build();

    }

    private Map<String, String> notificationPropertiesToStayTheClaim() {
        HashMap<String, String> properties = new HashMap<>();
        properties.put(NotificationDataGA.CASE_REFERENCE, CASE_REFERENCE.toString());
        properties.put(NotificationDataGA.GENAPP_REFERENCE, CASE_REFERENCE.toString());
        properties.put(NotificationDataGA.GA_APPLICATION_TYPE, GeneralApplicationTypes.STAY_THE_CLAIM.getDisplayedValue());
        properties.put(NotificationDataGA.PARTY_REFERENCE, PARTY_REFERENCE);
        properties.put(NotificationDataGA.WELSH_CONTACT, "E-bost: ymholiadaucymraeg@justice.gov.uk");
        properties.put(NotificationDataGA.WELSH_HMCTS_SIGNATURE, "Hawliadau am Arian yn y Llys Sifil Ar-lein \n Gwasanaeth Llysoedd a Thribiwnlysoedd EF");
        properties.put(NotificationDataGA.WELSH_OPENING_HOURS, "Dydd Llun i ddydd Iau, 9am – 5pm, dydd Gwener, 9am – 4.30pm");
        properties.put(NotificationDataGA.WELSH_PHONE_CONTACT, "Ffôn: 0300 303 5174");
        properties.put(NotificationDataGA.SPEC_CONTACT, "Email: contactocmc@justice.gov.uk");
        properties.put(NotificationDataGA.SPEC_UNSPEC_CONTACT, RAISE_QUERY_LR);
        properties.put(NotificationDataGA.HMCTS_SIGNATURE, "Online Civil Claims \n HM Courts & Tribunal Service");
        properties.put(NotificationDataGA.OPENING_HOURS, "Monday to Friday, 8.30am to 5pm");
        properties.put(NotificationDataGA.PHONE_CONTACT, "For anything related to hearings, call 0300 123 5577 "
            + "\n For all other matters, call 0300 123 7050");
        return properties;
    }

    public Map<String, String> notificationPropertiesToStayTheClaimLip() {
        HashMap<String, String> properties = new HashMap<>(Map.of(
            NotificationDataGA.CASE_REFERENCE, CASE_REFERENCE.toString(),
            NotificationDataGA.GENAPP_REFERENCE, CASE_REFERENCE.toString(),
            NotificationDataGA.GA_APPLICATION_TYPE, GeneralApplicationTypes.STAY_THE_CLAIM.getDisplayedValue(),
            NotificationDataGA.PARTY_REFERENCE, PARTY_REFERENCE
        ));
        properties.put(NotificationDataGA.WELSH_CONTACT, "E-bost: ymholiadaucymraeg@justice.gov.uk");
        properties.put(NotificationDataGA.WELSH_HMCTS_SIGNATURE, "Hawliadau am Arian yn y Llys Sifil Ar-lein \n Gwasanaeth Llysoedd a Thribiwnlysoedd EF");
        properties.put(NotificationDataGA.WELSH_OPENING_HOURS, "Dydd Llun i ddydd Iau, 9am – 5pm, dydd Gwener, 9am – 4.30pm");
        properties.put(NotificationDataGA.WELSH_PHONE_CONTACT, "Ffôn: 0300 303 5174");
        properties.put(NotificationDataGA.SPEC_CONTACT, "Email: contactocmc@justice.gov.uk");
        properties.put(NotificationDataGA.SPEC_UNSPEC_CONTACT, "Email for Specified Claims: contactocmc@justice.gov.uk "
            + "\n Email for Damages Claims: damagesclaims@justice.gov.uk");
        properties.put(NotificationDataGA.HMCTS_SIGNATURE, "Online Civil Claims \n HM Courts & Tribunal Service");
        properties.put(NotificationDataGA.OPENING_HOURS, "Monday to Friday, 8.30am to 5pm");
        properties.put(NotificationDataGA.PHONE_CONTACT, "For anything related to hearings, call 0300 123 5577 "
            + "\n For all other matters, call 0300 123 7050");
        return properties;
    }

    private List<Element<GASolicitorDetailsGAspec>> respondentSolicitors() {
        return Arrays.asList(element(new GASolicitorDetailsGAspec().setId(ID)
                                         .setForename("respondent")
                                         .setSurname(Optional.of("solicitor"))
                                         .setEmail(DUMMY_EMAIL).setOrganisationIdentifier(ORG_ID)),
                             element(new GASolicitorDetailsGAspec().setId(ID)
                                         .setEmail(DUMMY_EMAIL).setOrganisationIdentifier(ORG_ID))
        );
    }

    private List<Element<GASolicitorDetailsGAspec>> lipRespondent() {
        return Collections.singletonList(element(new GASolicitorDetailsGAspec().setId(ID)
                                                     .setForename("respondent")
                                                     .setSurname(Optional.of("lip"))
                                                     .setEmail(DUMMY_EMAIL)));
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

    public List<GeneralApplicationTypes> applicationTypeSummeryJudgement() {
        return List.of(
            GeneralApplicationTypes.SUMMARY_JUDGEMENT
        );
    }

    public List<GeneralApplicationTypes> applicationTypeVaryOrder() {
        return List.of(
            GeneralApplicationTypes.VARY_ORDER
        );
    }
}
