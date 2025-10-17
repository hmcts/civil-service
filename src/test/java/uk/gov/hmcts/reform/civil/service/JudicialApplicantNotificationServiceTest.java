package uk.gov.hmcts.reform.civil.service;

import com.fasterxml.jackson.databind.ObjectMapper;
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
import uk.gov.hmcts.reform.civil.ga.model.GeneralApplicationCaseData;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationData;
import uk.gov.hmcts.reform.civil.handler.callback.camunda.notification.NotificationDataGA;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.model.BusinessProcess;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.GeneralAppParentCaseLink;
import uk.gov.hmcts.reform.civil.model.PaymentDetails;
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
import uk.gov.hmcts.reform.civil.model.genapplication.GAUrgencyRequirement;
import uk.gov.hmcts.reform.civil.notify.NotificationService;
import uk.gov.hmcts.reform.civil.notify.NotificationsProperties;
import uk.gov.hmcts.reform.civil.notify.NotificationsSignatureConfiguration;
import uk.gov.hmcts.reform.civil.sampledata.GeneralApplicationCaseDataBuilder;

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
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.enums.CaseState.ORDER_MADE;
import static uk.gov.hmcts.reform.civil.enums.PaymentStatus.SUCCESS;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;
import static uk.gov.hmcts.reform.civil.enums.dq.GAJudgeMakeAnOrderOption.APPROVE_OR_EDIT;
import static uk.gov.hmcts.reform.civil.enums.dq.GAJudgeMakeAnOrderOption.GIVE_DIRECTIONS_WITHOUT_HEARING;
import static uk.gov.hmcts.reform.civil.enums.dq.GAJudgeRequestMoreInfoOption.REQUEST_MORE_INFORMATION;
import static uk.gov.hmcts.reform.civil.enums.dq.GAJudgeRequestMoreInfoOption.SEND_APP_TO_OTHER_PARTY;
import static uk.gov.hmcts.reform.civil.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.civil.utils.ElementUtils.wrapElements;
import static uk.gov.hmcts.reform.civil.utils.EmailFooterUtils.RAISE_QUERY_LR;
import static uk.gov.hmcts.reform.civil.utils.JudicialDecisionNotificationUtil.isNotificationCriteriaSatisfied;

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
    @Autowired
    private ObjectMapper objectMapper;

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
    private LocalDateTime responseDate = LocalDateTime.now();
    private LocalDateTime deadline = LocalDateTime.now().plusDays(5);

    @BeforeEach
    void setup() {
        when(featureToggleService.isGaForLipsEnabled()).thenReturn(true);
        when(gaForLipService.isGaForLip(any(GeneralApplicationCaseData.class))).thenReturn(false);
        when(gaForLipService.isLipAppGa(any(GeneralApplicationCaseData.class))).thenReturn(false);
        when(gaForLipService.isLipRespGa(any(GeneralApplicationCaseData.class))).thenReturn(false);
        when(caseDetailsConverter.toCaseDataGA(any())).thenReturn(baseGaCaseData().toBuilder().ccdState(CaseState.CASE_PROGRESSION).build());
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
        when(solicitorEmailValidation.validateSolicitorEmail(
            any(CaseData.class),
            any(GeneralApplicationCaseData.class)
        )).thenAnswer(invocation -> invocation.getArgument(1));
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
            when(caseDetailsConverter.toCaseDataGA(any())).thenReturn(baseGaCaseData());
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
            when(caseDetailsConverter.toCaseDataGA(any())).thenReturn(baseGaCaseData());
            when(gaForLipService.isLipApp(any())).thenReturn(true);
            CaseData caseData = caseDataForSequentialWrittenOption(YES, NO);

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
            CaseData claimRespondentResponseLan = rebuildToGa(baseGaCaseData().toBuilder().claimantBilingualLanguagePreference("WELSH")
                .applicantBilingualLanguagePreference(YES).build());
            when(caseDetailsConverter.toCaseDataGA(any())).thenReturn(claimRespondentResponseLan);
            when(gaForLipService.isLipApp(any())).thenReturn(true);
            CaseData caseData = caseDataForSequentialWrittenOption(YES, NO);
            CaseData updatedCasedata = caseData.toBuilder().applicantBilingualLanguagePreference(YES)
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
            when(caseDetailsConverter.toCaseDataGA(any())).thenReturn(baseGaCaseData().toBuilder().ccdState(CaseState.CASE_PROGRESSION).build());
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
            when(caseDetailsConverter.toCaseDataGA(any())).thenReturn(baseGaCaseData().toBuilder().ccdState(CaseState.CASE_PROGRESSION).build());
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
            CaseData claimRespondentResponseLan = rebuildToGa(baseGaCaseData().toBuilder().claimantBilingualLanguagePreference("WELSH")
                .applicantBilingualLanguagePreference(YES).build());
            when(caseDetailsConverter.toCaseDataGA(any())).thenReturn(claimRespondentResponseLan);
            CaseData updatedCaseData = caseDataForJudgeDismissal(NO, NO, NO,  YES, NO).toBuilder().applicantBilingualLanguagePreference(YES).build();
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

            CaseData caseData
                = caseDataForJudicialDirectionOrderOfApplicationWhenRespondentsArePresentInList(NO,
                                                                                                NO, NO, YES, YES, NO);
            when(caseDetailsConverter.toCaseDataGA(any())).thenReturn(baseGaCaseData().toBuilder().ccdCaseReference(CASE_REFERENCE).build());
            when(gaForLipService.isLipApp(any())).thenReturn(true);
            judicialNotificationService.sendNotification(caseData, APPLICANT);

            verify(notificationService, times(1)).sendMail(
                DUMMY_EMAIL, "ga-judicial-notification-applicant-template-lip-id",
                notificationPropertiesSummeryJudgement(), "general-apps-judicial-notification-make-decision-" + CASE_REFERENCE
            );
        }

        @Test
        void notificationShouldSend_LipRespondent_When_JudicialDirectionOrderRep() {

            CaseData caseData
                = caseDataForJudicialDirectionOrderOfApplicationWhenRespondentsArePresentInList(NO,
                                                                                                NO, YES, YES, YES, NO);
            when(caseDetailsConverter.toCaseDataGA(any())).thenReturn(baseGaCaseData().toBuilder().ccdState(CaseState.CASE_PROGRESSION).build());
            when(gaForLipService.isLipApp(any())).thenReturn(true);

            judicialNotificationService.sendNotification(caseData, APPLICANT);

            verify(notificationService, times(1)).sendMail(
                DUMMY_EMAIL, "ga-judicial-notification-applicant-template-lip-id",
                notificationPropertiesSummeryJudgement(), "general-apps-judicial-notification-make-decision-" + CASE_REFERENCE
            );
        }

        @Test
        void notificationShouldSendSendToLipRespondent_IfApplicationUncloakedForApproveOrEdit() {
            when(caseDetailsConverter.toCaseDataGA(any())).thenReturn(baseGaCaseData().toBuilder().ccdState(CaseState.CASE_PROGRESSION).build());
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
            when(caseDetailsConverter.toCaseDataGA(any())).thenReturn(baseGaCaseData().toBuilder().ccdState(CaseState.CASE_PROGRESSION).build());
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

            CaseData caseData
                = caseDataForJudicialRequestForInformationOfApplication(NO, NO, NO,
                                                                        SEND_APP_TO_OTHER_PARTY)
                .toBuilder().generalAppType(GAApplicationType.builder()
                                            .types(applicationTypeSummeryJudgement()).build()).build();
            when(gaForLipService.isLipApp(any())).thenReturn(true);
            when(caseDetailsConverter.toCaseDataGA(any())).thenReturn(baseGaCaseData());

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

            CaseData caseData
                = caseDataForJudicialRequestForInformationOfApplication(NO, NO, YES,
                                                                        SEND_APP_TO_OTHER_PARTY)
                .toBuilder().generalAppType(GAApplicationType.builder()
                                            .types(applicationTypeSummeryJudgement()).build()).build();
            when(caseDetailsConverter.toCaseDataGA(any())).thenReturn(baseGaCaseData());
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

            CaseData caseData
                = caseDataForJudicialRequestForInformationOfApplication(YES, YES, YES,
                                                                        REQUEST_MORE_INFORMATION)
                .toBuilder()
                .generalAppConsentOrder(NO)
                .ccdState(CaseState.APPLICATION_ADD_PAYMENT).generalAppType(GAApplicationType.builder()
                                            .types(applicationTypeVaryOrder()).build()).build();
            when(caseDetailsConverter.toCaseDataGA(any())).thenReturn(caseData);
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

            CaseData caseData
                = caseDataForJudicialRequestForInformationOfApplication(YES, YES, YES,
                                                                        REQUEST_MORE_INFORMATION)
                .toBuilder()
                .generalAppConsentOrder(NO)
                .ccdState(CaseState.APPLICATION_ADD_PAYMENT).generalAppType(GAApplicationType.builder()
                                                                            .types(applicationTypeVaryOrder()).build()).build();
            when(caseDetailsConverter.toCaseDataGA(any())).thenReturn(caseData);
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
            when(caseDetailsConverter.toCaseDataGA(any())).thenReturn(baseGaCaseData().toBuilder().ccdCaseReference(CASE_REFERENCE).build());
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
            when(caseDetailsConverter.toCaseDataGA(any())).thenReturn(baseGaCaseData());
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
            when(caseDetailsConverter.toCaseDataGA(any())).thenReturn(baseGaCaseData());
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
            CaseData caseData = caseDataFreeFormOrder();
            caseData = caseData.toBuilder()
                .generalAppInformOtherParty(GAInformOtherParty.builder().build())
                .generalAppRespondentAgreement(GARespondentOrderAgreement.builder().hasAgreed(NO).build()).build();
            when(caseDetailsConverter.toCaseDataGA(any())).thenReturn(baseGaCaseData());
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
            CaseData caseData = caseDataFreeFormOrder();
            caseData = caseData.toBuilder()
                .generalAppInformOtherParty(GAInformOtherParty.builder().build())
                .generalAppRespondentAgreement(GARespondentOrderAgreement.builder().hasAgreed(NO).build())
                .applicationIsUncloakedOnce(YES)
                .build();
            when(caseDetailsConverter.toCaseDataGA(any())).thenReturn(baseGaCaseData());
            when(gaForLipService.isLipResp(any())).thenReturn(true);
            judicialNotificationService.sendNotification(caseData, RESPONDENT);
            verify(notificationService).sendMail(
                DUMMY_EMAIL,
                "general-application-apps-judicial-notification-template-lip-id",
                notificationPropertiesSummeryJudgement(),
                "general-apps-judicial-notification-make-decision-" + CASE_REFERENCE
            );
        }

        private CaseData caseDataListForHearing() {
            return rebuildToGa(baseGaCaseData().toBuilder()
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
                .generalAppApplnSolicitor(GASolicitorDetailsGAspec.builder()
                                          .email(DUMMY_EMAIL).build())
                .businessProcess(BusinessProcess.builder().camundaEvent(JUDGES_DECISION).build())
                .generalAppParentCaseLink(GeneralAppParentCaseLink.builder()
                                          .caseReference(CASE_REFERENCE.toString()).build())
                .generalAppType(GAApplicationType.builder()
                                .types(applicationTypeSummeryJudgement()).build())
                .isMultiParty(NO)
                .build());
        }

        private CaseData caseDataFreeFormOrder() {
            return rebuildToGa(baseGaCaseData().toBuilder()
                .judicialDecision(GAJudicialDecision.builder()
                                  .decision(GAJudgeDecisionOption.FREE_FORM_ORDER).build())
                .generalAppRespondentSolicitors(respondentSolicitors())
                .ccdCaseReference(CASE_REFERENCE)
                .isGaApplicantLip(YES)
                .isGaRespondentOneLip(NO)
                .applicantPartyName("App")
                .claimant1PartyName("CL")
                .defendant1PartyName("DEF")
                .generalAppInformOtherParty(GAInformOtherParty.builder().isWithNotice(YES).build())
                .generalAppApplnSolicitor(GASolicitorDetailsGAspec.builder()
                                          .email(DUMMY_EMAIL).build())
                .businessProcess(BusinessProcess.builder().camundaEvent(JUDGES_DECISION).build())
                .generalAppParentCaseLink(GeneralAppParentCaseLink.builder()
                                          .caseReference(CASE_REFERENCE.toString()).build())
                .generalAppType(GAApplicationType.builder()
                                .types(applicationTypeSummeryJudgement()).build())
                .isMultiParty(NO)
                .ccdState(CaseState.APPLICATION_SUBMITTED_AWAITING_JUDICIAL_DECISION)
                .businessProcess(BusinessProcess.builder().camundaEvent(JUDGES_DECISION)
                                 .activityId("StartRespondentNotificationProcessMakeDecision")
                                 .build())
                .build());
        }

        private CaseData caseDataForJudicialRequestForInformationOfApplication(
            YesOrNo isRespondentOrderAgreement, YesOrNo isWithNotice, YesOrNo isCloaked,
            GAJudgeRequestMoreInfoOption gaJudgeRequestMoreInfoOption) {

            return rebuildToGa(baseGaCaseData().toBuilder()
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
                .isGaApplicantLip(YES)
                .isGaRespondentOneLip(NO)
                .applicantPartyName("App")
                .claimant1PartyName("CL")
                .defendant1PartyName("DEF")
                .businessProcess(BusinessProcess.builder().camundaEvent(JUDGES_DECISION).build())
                .generalAppParentCaseLink(GeneralAppParentCaseLink.builder()
                                          .caseReference(CASE_REFERENCE.toString()).build())
                .generalAppType(GAApplicationType.builder()
                                .types(applicationTypeToStayTheClaim()).build())
                .generalAppPBADetails(GAPbaDetails.builder().build())
                .build());

        }

        private CaseData caseDataWithSolicitorDataOnlyForApplicationUncloakedJudgeApproveOrEdit(YesOrNo orderAgreement,
                                                                                                YesOrNo isWithNotice,
                                                                                                YesOrNo isCloaked) {
            return rebuildToGa(baseGaCaseData().toBuilder()
                .applicationIsCloaked(isCloaked)
                .ccdCaseReference(CASE_REFERENCE)
                .generalAppRespondentSolicitors(respondentSolicitors())
                .judicialDecisionMakeOrder(GAJudicialMakeAnOrder.builder()
                                           .makeAnOrder(GAJudgeMakeAnOrderOption.APPROVE_OR_EDIT).build())
                .judicialDecision(GAJudicialDecision.builder()
                                  .decision(GAJudgeDecisionOption.MAKE_AN_ORDER).build())
                .generalAppApplnSolicitor(GASolicitorDetailsGAspec.builder()
                                          .email(DUMMY_EMAIL).build())
                .isGaRespondentOneLip(NO)
                .isGaApplicantLip(YES)

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
                .build());
        }

        private CaseData caseDataForJudicialDirectionOrderOfApplicationWhenRespondentsArePresentInList(
            YesOrNo orderAgreement,
            YesOrNo isWithNotice,
            YesOrNo isCloaked,
            YesOrNo isGaLip,
            YesOrNo isApplicantLip,
            YesOrNo isRespondentLip) {
            return rebuildToGa(baseGaCaseData().toBuilder()
                .ccdCaseReference(CASE_REFERENCE)
                .generalAppRespondentAgreement(GARespondentOrderAgreement.builder().hasAgreed(orderAgreement).build())
                .generalAppInformOtherParty(GAInformOtherParty.builder().isWithNotice(isWithNotice).build())
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
                .generalAppApplnSolicitor(GASolicitorDetailsGAspec.builder()
                                          .email(DUMMY_EMAIL).build())
                .businessProcess(BusinessProcess.builder().camundaEvent(JUDGES_DECISION).build())
                .generalAppParentCaseLink(GeneralAppParentCaseLink.builder()
                                          .caseReference(CASE_REFERENCE.toString()).build())
                .generalAppType(GAApplicationType.builder()
                                .types(applicationTypeSummeryJudgement()).build())
                .generalAppPBADetails(GAPbaDetails.builder().build())
                .isMultiParty(NO)
                .build());
        }

        private CaseData caseDataForJudgeDismissal(YesOrNo orderAgreement, YesOrNo isWithNotice, YesOrNo isCloaked,
                                                   YesOrNo isLipApplicant, YesOrNo isLipRespondent) {
            return rebuildToGa(baseGaCaseData().toBuilder()
                .ccdCaseReference(CASE_REFERENCE)
                .generalAppRespondentAgreement(GARespondentOrderAgreement.builder().hasAgreed(orderAgreement).build())
                .generalAppInformOtherParty(GAInformOtherParty.builder().isWithNotice(isWithNotice).build())
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
                .generalAppApplnSolicitor(GASolicitorDetailsGAspec.builder()
                                          .email(DUMMY_EMAIL).build())
                .businessProcess(BusinessProcess.builder().camundaEvent(JUDGES_DECISION).build())
                .generalAppParentCaseLink(GeneralAppParentCaseLink.builder()
                                          .caseReference(CASE_REFERENCE.toString()).build())
                .generalAppType(GAApplicationType.builder()
                                .types(applicationTypeSummeryJudgement()).build())
                .generalAppPBADetails(GAPbaDetails.builder().build())
                .isMultiParty(NO)
                .build());
        }

        private Map<String, String> notificationPropertiesSummeryJudgement() {

            customProp.put(NotificationDataGA.CASE_REFERENCE, CASE_REFERENCE.toString());
            customProp.put(NotificationDataGA.GENAPP_REFERENCE, CASE_REFERENCE.toString());
            customProp.put(NotificationDataGA.GA_APPLICATION_TYPE, GeneralApplicationTypes.SUMMARY_JUDGEMENT.getDisplayedValue());
            customProp.put(NotificationDataGA.PARTY_REFERENCE, PARTY_REFERENCE.toString());
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

        private Map<String, String> notificationPropertiesVaryOrder() {

            customProp.put(NotificationDataGA.CASE_REFERENCE, CASE_REFERENCE.toString());
            customProp.put(NotificationDataGA.GENAPP_REFERENCE, CASE_REFERENCE.toString());
            customProp.put(NotificationDataGA.GA_APPLICATION_TYPE,
                           GeneralApplicationTypes.VARY_ORDER.getDisplayedValue());
            customProp.put(NotificationDataGA.PARTY_REFERENCE, PARTY_REFERENCE.toString());
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
            return rebuildToGa(baseGaCaseData().toBuilder()
                .ccdCaseReference(CASE_REFERENCE)
                .isGaApplicantLip(isGaApplicantLip)
                .isGaRespondentOneLip(isGaRespondentOneLip)
                .applicantPartyName("App")
                .claimant1PartyName("CL")
                .defendant1PartyName("DEF")
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
                .build());
        }

        private CaseData caseDataForSequentialWrittenOption(YesOrNo isGaApplicantLip, YesOrNo isGaRespondentOneLip) {
            return rebuildToGa(baseGaCaseData().toBuilder()
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
                .build());
        }

        private List<Element<GASolicitorDetailsGAspec>> respondentSolicitors() {
            return Arrays.asList(element(GASolicitorDetailsGAspec.builder().id(ID)
                                         .forename("respondent")
                                         .surname(Optional.of("solicitor"))
                                         .email(DUMMY_EMAIL).organisationIdentifier(ORG_ID).build()));
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

            CaseData caseData = caseDataForConcurrentWrittenOption().toBuilder().businessProcess(null).build();

            judicialNotificationService.sendNotification(caseData, APPLICANT);
            verify(notificationService, never()).sendMail(any(), any(), any(), any());
        }

        @Test
        void notificationShouldSendThriceForConcurrentWrittenRepsWhenInvoked() {

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
            CaseData caseData = caseDataForJudgeDismissal(YES, YES, YES,  NO, NO, NO).toBuilder()
                .generalAppConsentOrder(NO).build();

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
            CaseData caseData = caseDataForJudgeDismissal(NO, NO, YES,  NO, NO, NO);

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
            CaseData caseData = caseDataWithSolicitorDataOnlyForApplicationUncloakedJudgeApproveOrEdit(YES, YES, NO, NO, NO, NO)
                .toBuilder().generalAppConsentOrder(NO).build();

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
            CaseData caseData = caseDataWithSolicitorDataOnlyForApplicationUncloakedJudgeApproveOrEdit(NO, NO, YES, NO, NO, NO);

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

            judicialNotificationService.sendNotification(
                caseDataForJudicialApprovalOfApplication(NO, NO).toBuilder()
                .generalAppPBADetails(GAPbaDetails.builder()
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

            CaseData caseData
                = caseDataForJudicialDirectionOrderOfApplicationWhenRespondentsArePresentInList(YES,
                                                                                                YES, YES, NO, NO, NO)
                .toBuilder()
                .generalAppConsentOrder(YES)
                .applicationIsCloaked(null)
                .build();

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

            CaseData caseData
                = caseDataForJudicialDirectionOrderOfApplicationWhenRespondentsArePresentInList(YES,
                                                                                                YES, YES, NO, NO, NO)
                .toBuilder()
                .generalAppConsentOrder(YES)
                .generalAppRespondentSolicitors(null)
                .build();

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

            CaseData caseData = caseDataForApplicationsApprovedWhenRespondentsAreInList(NO, NO)
                .toBuilder().generalAppPBADetails(
                    GAPbaDetails.builder()
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

            CaseData caseData = caseDataForCaseDismissedByJudgeRespondentsAreInList(YES, YES, YES)
                .toBuilder()
                .generalAppConsentOrder(YES)
                .build();

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

            CaseData caseData = caseDataForCaseDismissedByJudgeRespondentsAreInList(NO, NO, YES)
                .toBuilder()
                .build();

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

            CaseData caseData = caseDataForCaseDismissedByJudgeRespondentsAreInList(NO, NO, YES)
                .toBuilder().generalAppPBADetails(
                    GAPbaDetails.builder()
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
            when(caseDetailsConverter.toCaseDataGA(any())).thenReturn(baseGaCaseData());
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
            when(caseDetailsConverter.toCaseDataGA(any())).thenReturn(baseGaCaseData());
            when(gaForLipService.isLipResp(any())).thenReturn(false);
            judicialNotificationService.sendNotification(caseDataFreeFormOrder(), RESPONDENT);
            verify(notificationService, times(2)).sendMail(
                DUMMY_EMAIL,
                "general-application-apps-judicial-notification-template-id",
                notificationPropertiesSummeryJudgement(),
                "general-apps-judicial-notification-make-decision-" + CASE_REFERENCE
            );
        }

        private CaseData caseDataForConcurrentWrittenOption() {
            return rebuildToGa(baseGaCaseData().toBuilder()
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
                            GAJudgeWrittenRepresentationsOptions.CONCURRENT_REPRESENTATIONS).build())
                .generalAppType(GAApplicationType.builder()
                                    .types(applicationTypeSummeryJudgement()).build())
                .judicialConcurrentDateText(DUMMY_DATE)
                .isMultiParty(NO)
                .build());
        }

        private CaseData caseDataForSequentialWrittenOption() {
            return rebuildToGa(baseGaCaseData().toBuilder()
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
                .build());
        }

        private CaseData caseDataForConcurrentWrittenRepRespondentNotPresent() {
            return rebuildToGa(baseGaCaseData().toBuilder()
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
                .build());
        }

        private CaseData caseDataForJudgeDismissal(YesOrNo orderAgreement, YesOrNo isWithNotice, YesOrNo isCloaked,
                                                   YesOrNo isLipApplicant, YesOrNo isLipRespondent, YesOrNo isLipApp) {
            return rebuildToGa(baseGaCaseData().toBuilder()
                .ccdCaseReference(CASE_REFERENCE)
                .generalAppRespondentAgreement(GARespondentOrderAgreement.builder().hasAgreed(orderAgreement).build())
                .generalAppInformOtherParty(GAInformOtherParty.builder().isWithNotice(isWithNotice).build())
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
                .generalAppApplnSolicitor(GASolicitorDetailsGAspec.builder()
                                          .email(DUMMY_EMAIL).build())
                .businessProcess(BusinessProcess.builder().camundaEvent(JUDGES_DECISION).build())
                .generalAppParentCaseLink(GeneralAppParentCaseLink.builder()
                                          .caseReference(CASE_REFERENCE.toString()).build())
                .generalAppType(GAApplicationType.builder()
                                .types(applicationTypeToStrikeOut()).build())
                .generalAppPBADetails(GAPbaDetails.builder().build())
                .isMultiParty(NO)
                .build());
        }

        private CaseData caseDataListForHearing() {
            return rebuildToGa(baseGaCaseData().toBuilder()
                .ccdCaseReference(CASE_REFERENCE)
                .judicialDecision(GAJudicialDecision.builder()
                                  .decision(GAJudgeDecisionOption.LIST_FOR_A_HEARING).build())
                .judicialDecisionMakeOrder(GAJudicialMakeAnOrder.builder()
                                           .makeAnOrder(GAJudgeMakeAnOrderOption.DISMISS_THE_APPLICATION).build())
                .generalAppApplnSolicitor(GASolicitorDetailsGAspec.builder()
                                          .email(DUMMY_EMAIL).build())
                .businessProcess(BusinessProcess.builder().camundaEvent(JUDGES_DECISION).build())
                .generalAppParentCaseLink(GeneralAppParentCaseLink.builder()
                                          .caseReference(CASE_REFERENCE.toString()).build())
                .generalAppType(GAApplicationType.builder()
                                .types(applicationTypeToExtendTheClaim()).build())
                .isMultiParty(NO)
                .build());
        }

        private CaseData caseDataForCloakedApplicationWithSolicitorDataOnly() {
            return rebuildToGa(baseGaCaseData().toBuilder()
                .ccdCaseReference(CASE_REFERENCE)
                .applicationIsCloaked(YES)
                .isMultiParty(NO)
                .judicialDecisionMakeOrder(GAJudicialMakeAnOrder.builder()
                                           .makeAnOrder(GIVE_DIRECTIONS_WITHOUT_HEARING).build())
                .judicialDecision(GAJudicialDecision.builder()
                                  .decision(GAJudgeDecisionOption.MAKE_AN_ORDER).build())
                .generalAppApplnSolicitor(GASolicitorDetailsGAspec.builder()
                                          .email(DUMMY_EMAIL).build())
                .businessProcess(BusinessProcess.builder().camundaEvent(JUDGES_DECISION).build())
                .generalAppParentCaseLink(GeneralAppParentCaseLink.builder()
                                          .caseReference(CASE_REFERENCE.toString()).build())
                .generalAppType(GAApplicationType.builder()
                                .types(applicationTypeSummeryJudgement()).build())
                .build());
        }

        private CaseData caseDataWithSolicitorDataOnlyForApplicationUncloakedJudgeApproveOrEdit(YesOrNo orderAgreement,
                                                                                                YesOrNo isWithNotice,
                                                                                                YesOrNo isCloaked,
                                                                                                YesOrNo isGaLip,
                                                                                                YesOrNo isApplicantLip,
                                                                                                YesOrNo isRespondentLip) {
            return rebuildToGa(baseGaCaseData().toBuilder()
                .ccdCaseReference(CASE_REFERENCE)
                .applicationIsCloaked(isCloaked)
                .generalAppRespondentSolicitors(isGaLip.equals(YES) ? lipRespondent() : respondentSolicitors())
                .judicialDecisionMakeOrder(GAJudicialMakeAnOrder.builder()
                                           .makeAnOrder(GAJudgeMakeAnOrderOption.APPROVE_OR_EDIT).build())
                .judicialDecision(GAJudicialDecision.builder()
                                  .decision(GAJudgeDecisionOption.MAKE_AN_ORDER).build())
                .generalAppApplnSolicitor(GASolicitorDetailsGAspec.builder()
                                          .email(DUMMY_EMAIL).build())
                .isGaRespondentOneLip(isRespondentLip)
                .isGaApplicantLip(isApplicantLip)

                .businessProcess(BusinessProcess.builder().camundaEvent(JUDGES_DECISION).build())
                .generalAppParentCaseLink(GeneralAppParentCaseLink.builder()
                                          .caseReference(CASE_REFERENCE.toString()).build())
                .generalAppRespondentAgreement(GARespondentOrderAgreement.builder().hasAgreed(orderAgreement).build())
                .generalAppInformOtherParty(GAInformOtherParty.builder().isWithNotice(isWithNotice).build())
                .generalAppType(GAApplicationType.builder()
                                .types(applicationTypeToStayTheClaim()).build())
                .isMultiParty(NO)
                .applicantPartyName("App")
                .claimant1PartyName("CL")
                .defendant1PartyName("DEF")
                .build());
        }

        private CaseData caseDataForApplicationUncloakedIsDismissed() {
            return rebuildToGa(baseGaCaseData().toBuilder()
                .ccdCaseReference(CASE_REFERENCE)
                .applicationIsCloaked(NO)
                .generalAppRespondentAgreement(GARespondentOrderAgreement.builder().hasAgreed(NO).build())
                .generalAppInformOtherParty(GAInformOtherParty.builder().isWithNotice(YES).build())
                .judicialDecisionMakeOrder(GAJudicialMakeAnOrder.builder()
                                           .makeAnOrder(GAJudgeMakeAnOrderOption.DISMISS_THE_APPLICATION).build())
                .judicialDecision(GAJudicialDecision.builder()
                                  .decision(GAJudgeDecisionOption.MAKE_AN_ORDER).build())
                .generalAppApplnSolicitor(GASolicitorDetailsGAspec.builder()
                                          .email(DUMMY_EMAIL).build())
                .businessProcess(BusinessProcess.builder().camundaEvent(JUDGES_DECISION).build())
                .generalAppParentCaseLink(GeneralAppParentCaseLink.builder()
                                          .caseReference(CASE_REFERENCE.toString()).build())
                .isMultiParty(NO)
                .generalAppType(GAApplicationType.builder()
                                .types(applicationTypeToStrikeOut()).build())
                .build());
        }

        private CaseData caseDataForAmendStatementOfClaim() {
            return rebuildToGa(baseGaCaseData().toBuilder()
                .ccdCaseReference(CASE_REFERENCE)
                .judicialDecision(GAJudicialDecision.builder()
                                  .decision(GAJudgeDecisionOption.LIST_FOR_A_HEARING).build())
                .isMultiParty(NO)
                .judicialDecisionMakeOrder(GAJudicialMakeAnOrder.builder()
                                           .makeAnOrder(GAJudgeMakeAnOrderOption.DISMISS_THE_APPLICATION).build())
                .generalAppApplnSolicitor(GASolicitorDetailsGAspec.builder()
                                          .email(DUMMY_EMAIL).build())
                .businessProcess(BusinessProcess.builder().camundaEvent(JUDGES_DECISION).build())
                .generalAppParentCaseLink(GeneralAppParentCaseLink.builder()
                                          .caseReference(CASE_REFERENCE.toString()).build())
                .generalAppType(GAApplicationType.builder()
                                .types(applicationTypeToAmendStatmentOfClaim()).build())
                .build());
        }

        private CaseData caseDataForJudicialApprovalOfApplication(YesOrNo orderAgreement, YesOrNo isWithNotice) {
            return rebuildToGa(baseGaCaseData().toBuilder()
                .ccdCaseReference(CASE_REFERENCE)
                .isMultiParty(NO)
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
                .build());
        }

        private CaseData caseDataForJudicialDirectionOrderOfApplication(YesOrNo orderAgreement, YesOrNo isWithNotice) {
            return rebuildToGa(baseGaCaseData().toBuilder()
                .ccdCaseReference(CASE_REFERENCE)
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
                .isMultiParty(NO)
                .build());
        }

        private CaseData caseDataForJudicialDirectionOrderOfApplicationWhenRespondentsArePresentInList(
            YesOrNo orderAgreement,
            YesOrNo isWithNotice,
            YesOrNo isCloaked,
            YesOrNo isGaLip,
            YesOrNo isApplicantLip,
            YesOrNo isRespondentLip) {
            return rebuildToGa(baseGaCaseData().toBuilder()
                .ccdCaseReference(CASE_REFERENCE)
                .generalAppRespondentAgreement(GARespondentOrderAgreement.builder().hasAgreed(orderAgreement).build())
                .generalAppInformOtherParty(GAInformOtherParty.builder().isWithNotice(isWithNotice).build())
                .applicationIsCloaked(isCloaked)
                .isGaApplicantLip(isApplicantLip)
                .applicantPartyName("App")
                .claimant1PartyName("CL")
                .defendant1PartyName("DEF")
                .isGaRespondentOneLip(isRespondentLip)
                .judicialDecisionMakeOrder(GAJudicialMakeAnOrder.builder()
                                           .makeAnOrder(GIVE_DIRECTIONS_WITHOUT_HEARING).build())
                .generalAppRespondentSolicitors(isGaLip.equals(YES) ? lipRespondent() : respondentSolicitors())
                .generalAppApplnSolicitor(GASolicitorDetailsGAspec.builder()
                                          .email(DUMMY_EMAIL).build())
                .businessProcess(BusinessProcess.builder().camundaEvent(JUDGES_DECISION).build())
                .generalAppParentCaseLink(GeneralAppParentCaseLink.builder()
                                          .caseReference(CASE_REFERENCE.toString()).build())
                .generalAppType(GAApplicationType.builder()
                                .types(applicationTypeToAmendStatmentOfClaim()).build())
                .generalAppPBADetails(GAPbaDetails.builder().build())
                .isMultiParty(NO)
                .build());
        }

        private CaseData caseDataForApplicationsApprovedStrikeOutWhenRespondentsAreInList(
            YesOrNo orderAgreement,
            YesOrNo isWithNotice, YesOrNo isCloaked, String superClaimType) {
            return rebuildToGa(baseGaCaseData().toBuilder()
                .ccdCaseReference(CASE_REFERENCE)
                .isMultiParty(NO)
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
                .build());
        }

        private CaseData caseDataForSequentialWrittenRepInList() {
            return rebuildToGa(baseGaCaseData().toBuilder()
                .ccdCaseReference(CASE_REFERENCE)
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
                .isMultiParty(NO)
                .generalAppPBADetails(GAPbaDetails.builder().build())
                .build());
        }

        private CaseData caseDataForApplicationsApprovedWhenRespondentsAreInList(YesOrNo orderAgreement,
                                                                                 YesOrNo isWithNotice) {
            return rebuildToGa(baseGaCaseData().toBuilder()
                .ccdCaseReference(CASE_REFERENCE)
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
                .isMultiParty(NO)
                .build());
        }

        private CaseData caseDataForListForHearingRespondentsAreInList() {
            return rebuildToGa(baseGaCaseData().toBuilder()
                .ccdCaseReference(CASE_REFERENCE)
                .generalAppRespondentSolicitors(respondentSolicitors())
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
                .build());
        }

        private CaseData caseDataForCaseDismissedByJudgeRespondentsAreInList(YesOrNo orderAgreement,
                                                                             YesOrNo isWithNotice,
                                                                             YesOrNo isCloaked) {
            return rebuildToGa(baseGaCaseData().toBuilder()
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
                .judicialDecision(GAJudicialDecision.builder()
                                      .decision(GAJudgeDecisionOption.MAKE_AN_ORDER).build())
                .judicialDecisionMakeOrder(GAJudicialMakeAnOrder.builder().makeAnOrder(
                        GAJudgeMakeAnOrderOption.DISMISS_THE_APPLICATION
                    ).build())
                .generalAppType(GAApplicationType.builder()
                                    .types(applicationTypeSummeryJudgement()).build())
                .judicialConcurrentDateText(DUMMY_DATE)
                .generalAppPBADetails(GAPbaDetails.builder().build())
                .isMultiParty(NO)
                .build());
        }

        private CaseData caseDataForApprovedCloakStrikeOutWhenRespondentsArePresentInList(String superClaimType) {
            return rebuildToGa(baseGaCaseData().toBuilder()
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
                .isMultiParty(NO)
                .build());
        }

        private CaseData caseDataForJudgeApprovedOrderCloakWhenRespondentsArePresentInList() {
            return rebuildToGa(baseGaCaseData().toBuilder()
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
                .isMultiParty(NO)
                .build());
        }

        private CaseData caseDataForJudgeDismissTheApplicationCloakWhenRespondentsArePresentInList() {
            return rebuildToGa(baseGaCaseData().toBuilder()
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
                .isMultiParty(NO)
                .judicialConcurrentDateText(DUMMY_DATE)
                .build());
        }

        private CaseData caseDataFreeFormOrder() {
            return rebuildToGa(baseGaCaseData().toBuilder()
                .judicialDecision(GAJudicialDecision.builder()
                                  .decision(GAJudgeDecisionOption.FREE_FORM_ORDER).build())
                .generalAppRespondentSolicitors(respondentSolicitors())
                .ccdCaseReference(CASE_REFERENCE)
                .isGaApplicantLip(NO)
                .isGaRespondentOneLip(NO)
                .applicantPartyName("App")
                .claimant1PartyName("CL")
                .defendant1PartyName("DEF")
                .generalAppInformOtherParty(GAInformOtherParty.builder().isWithNotice(YES).build())
                .generalAppApplnSolicitor(GASolicitorDetailsGAspec.builder()
                                          .email(DUMMY_EMAIL).build())
                .businessProcess(BusinessProcess.builder().camundaEvent(JUDGES_DECISION).build())
                .generalAppParentCaseLink(GeneralAppParentCaseLink.builder()
                                          .caseReference(CASE_REFERENCE.toString()).build())
                .generalAppType(GAApplicationType.builder()
                                .types(applicationTypeSummeryJudgement()).build())
                .isMultiParty(NO)
                .ccdState(CaseState.APPLICATION_SUBMITTED_AWAITING_JUDICIAL_DECISION)
                .businessProcess(BusinessProcess.builder().camundaEvent(JUDGES_DECISION)
                                 .activityId("StartRespondentNotificationProcessMakeDecision")
                                 .build())
                .build());
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

        private Map<String, String> notificationPropertiesSummeryJudgementConcurrent() {
            HashMap<String, String> properties = new HashMap<>(Map.of(
                NotificationDataGA.CASE_REFERENCE, CASE_REFERENCE.toString(),
                NotificationDataGA.GENAPP_REFERENCE, CASE_REFERENCE.toString(),
                NotificationDataGA.PARTY_REFERENCE, PARTY_REFERENCE,
                NotificationDataGA.GA_APPLICATION_TYPE,
                GeneralApplicationTypes.SUMMARY_JUDGEMENT.getDisplayedValue()
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
                NotificationDataGA.PARTY_REFERENCE, PARTY_REFERENCE,
                NotificationDataGA.GA_APPLICATION_TYPE,
                GeneralApplicationTypes.STRIKE_OUT.getDisplayedValue()
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
                NotificationDataGA.GENAPP_REFERENCE, CASE_REFERENCE.toString(),
                NotificationDataGA.PARTY_REFERENCE, PARTY_REFERENCE,
                NotificationDataGA.GA_APPLICATION_TYPE,
                GeneralApplicationTypes.EXTEND_TIME.getDisplayedValue()
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
                NotificationDataGA.PARTY_REFERENCE, PARTY_REFERENCE,
                NotificationDataGA.GA_APPLICATION_TYPE,
                GeneralApplicationTypes.RELIEF_FROM_SANCTIONS.getDisplayedValue()
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

        @BeforeEach
        public void setup() {
            when(gaForLipService.isLipApp(any())).thenReturn(false);
        }

        @Test
        void notificationShouldSend_IfWithoutNotice_ApplicationContinuesToBeCloaked() {
            CaseData caseData = caseDataForJudicialRequestForInformationOfApplication(NO, NO, YES, NO, NO,
                    SEND_APP_TO_OTHER_PARTY).toBuilder().isMultiParty(NO).build();

            when(time.now()).thenReturn(responseDate);
            when(caseDetailsConverter.toCaseDataGA(any())).thenReturn(baseGaCaseData().toBuilder().ccdState(CaseState.CASE_PROGRESSION).build());

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
            CaseData caseData = caseDataForJudicialRequestForInformationOfApplication(NO, YES, NO, NO, NO,
                                                                                      REQUEST_MORE_INFORMATION);

            when(caseDetailsConverter.toCaseDataGA(any())).thenReturn(baseGaCaseData().toBuilder().ccdState(CaseState.CASE_PROGRESSION).build());

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

            CaseData caseData = caseDataForJudicialRequestForInformationOfApplication(YES, YES, YES, NO, NO,
                                                                                      REQUEST_MORE_INFORMATION)
                .toBuilder()
                .generalAppConsentOrder(YES)
                .ccdState(CaseState.APPLICATION_SUBMITTED_AWAITING_JUDICIAL_DECISION)
                .build();

            when(caseDetailsConverter.toCaseDataGA(any())).thenReturn(baseGaCaseData().toBuilder().ccdState(CaseState.CASE_PROGRESSION).build());

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

            CaseData caseData = caseDataForJudicialRequestForInformationOfApplication(NO, NO, YES, NO, NO,
                                                                                      REQUEST_MORE_INFORMATION);
            caseData = caseData.toBuilder().ccdState(CaseState.APPLICATION_SUBMITTED_AWAITING_JUDICIAL_DECISION).build();

            when(caseDetailsConverter.toCaseDataGA(any())).thenReturn(baseGaCaseData().toBuilder().ccdState(CaseState.CASE_PROGRESSION).build());

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

            CaseData caseData = caseDataForJudicialRequestForInformationOfApplication(
                NO, YES, NO, NO, NO, REQUEST_MORE_INFORMATION).toBuilder()
                .isMultiParty(NO)
                .generalAppRespondentSolicitors(Arrays.asList()).build();

            when(caseDetailsConverter.toCaseDataGA(any())).thenReturn(baseGaCaseData().toBuilder().ccdState(CaseState.CASE_PROGRESSION).build());

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

            CaseData caseData = caseDataForJudicialRequestForInformationOfApplication(NO, NO, NO, NO, NO,
                                                                                      SEND_APP_TO_OTHER_PARTY)
                .toBuilder().isMultiParty(NO).build();

            when(caseDetailsConverter.toCaseDataGA(any())).thenReturn(baseGaCaseData().toBuilder().ccdState(CaseState.CASE_PROGRESSION).build());

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

            CaseData caseData = caseDataForJudicialRequestForInformationOfApplication(NO, NO, NO, YES, NO,
                                                                                      SEND_APP_TO_OTHER_PARTY);
            when(caseDetailsConverter.toCaseDataGA(any())).thenReturn(baseGaCaseData());
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
            when(featureToggleService.isGaForLipsEnabled()).thenReturn(true);
            when(gaForLipService.isLipResp(any())).thenReturn(true);
            when(gaForLipService.isGaForLip(any(CaseData.class))).thenReturn(true);
            when(gaForLipService.isLipApp(any())).thenReturn(true);
            when(caseDetailsConverter.toCaseDataGA(any())).thenReturn(baseGaCaseData());
            when(gaForLipService.isGaForLip(any(GeneralApplicationCaseData.class))).thenReturn(true);
            when(gaForLipService.isLipRespGa(any(GeneralApplicationCaseData.class))).thenReturn(true);

            CaseData caseData = caseDataForJudicialRequestForInformationOfApplication(NO, YES, NO, YES, YES,
                                                                                      REQUEST_MORE_INFORMATION
            ).toBuilder().ccdState(ORDER_MADE)
                .businessProcess(BusinessProcess.builder().camundaEvent(JUDGES_DECISION)
                                 .activityId("StartRespondentNotificationProcessMakeDecision")
                                 .build()).judicialDecision(GAJudicialDecision.builder()
                                                                .decision(GAJudgeDecisionOption.MAKE_AN_ORDER)
                                                                .build())
                .judicialDecisionMakeOrder(GAJudicialMakeAnOrder.builder().makeAnOrder(APPROVE_OR_EDIT)
                                           .build()).parentClaimantIsApplicant(NO).build();

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

    private CaseData getCaseData(YesOrNo isUrgent, YesOrNo informOtherParty, String recipient) {
        return rebuildToGa(baseGaCaseData().toBuilder()
            .generalAppUrgencyRequirement(GAUrgencyRequirement.builder().generalAppUrgency(isUrgent).build())
            .generalAppInformOtherParty(GAInformOtherParty.builder().isWithNotice(informOtherParty).build())
            .generalAppRespondentSolicitors(wrapElements(GASolicitorDetailsGAspec.builder()
                                                         .email(recipient).build()))
            .applicationIsCloaked(null)
            .applicationIsUncloakedOnce(null)
            .build());
    }

    private CaseData caseDataForJudicialRequestForInformationOfApplication(
        YesOrNo isRespondentOrderAgreement, YesOrNo isWithNotice, YesOrNo isCloaked,
        YesOrNo isLipApp, YesOrNo isLipRespondent, GAJudgeRequestMoreInfoOption gaJudgeRequestMoreInfoOption) {
        GASolicitorDetailsGAspec lr = GASolicitorDetailsGAspec.builder().email(DUMMY_EMAIL).build();
        GASolicitorDetailsGAspec lip = GASolicitorDetailsGAspec.builder().email(DUMMY_EMAIL)
                .forename("LipF").surname(Optional.of("LipS")).build();
        return rebuildToGa(baseGaCaseData().toBuilder()
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
            .generalAppRespondentAgreement(GARespondentOrderAgreement.builder()
                                           .hasAgreed(isRespondentOrderAgreement).build())
            .generalAppInformOtherParty(GAInformOtherParty.builder().isWithNotice(isWithNotice).build())
            .generalAppApplnSolicitor(isLipApp.equals(YES) ? lip : lr)
            .isGaApplicantLip(isLipApp)
            .isGaRespondentOneLip(isLipRespondent)
            .applicantPartyName("App")
            .claimant1PartyName("CL")
            .defendant1PartyName("DEF")
            .businessProcess(BusinessProcess.builder().camundaEvent(JUDGES_DECISION).build())
            .generalAppParentCaseLink(GeneralAppParentCaseLink.builder()
                                      .caseReference(CASE_REFERENCE.toString()).build())
            .generalAppType(GAApplicationType.builder()
                            .types(applicationTypeToStayTheClaim()).build())
            .generalAppPBADetails(GAPbaDetails.builder().build())
            .build());

    }

    private Map<String, String> notificationPropertiesToStayTheClaim() {
        HashMap<String, String> properties = new HashMap<>();
        properties.put(NotificationDataGA.CASE_REFERENCE, CASE_REFERENCE.toString());
        properties.put(NotificationDataGA.GENAPP_REFERENCE, CASE_REFERENCE.toString());
        properties.put(NotificationDataGA.GA_APPLICATION_TYPE, GeneralApplicationTypes.STAY_THE_CLAIM.getDisplayedValue());
        properties.put(NotificationDataGA.PARTY_REFERENCE, PARTY_REFERENCE);
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

    public Map<String, String> notificationPropertiesToStayTheClaimLip() {
        HashMap<String, String> properties = new HashMap<>(Map.of(
            NotificationDataGA.CASE_REFERENCE, CASE_REFERENCE.toString(),
            NotificationDataGA.GENAPP_REFERENCE, CASE_REFERENCE.toString(),
            NotificationDataGA.GA_APPLICATION_TYPE, GeneralApplicationTypes.STAY_THE_CLAIM.getDisplayedValue(),
            NotificationDataGA.PARTY_REFERENCE, PARTY_REFERENCE.toString()
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

    private List<Element<GASolicitorDetailsGAspec>> respondentSolicitors() {
        return Arrays.asList(element(GASolicitorDetailsGAspec.builder().id(ID)
                                     .forename("respondent")
                                     .surname(Optional.of("solicitor"))
                                     .email(DUMMY_EMAIL).organisationIdentifier(ORG_ID).build()),
                             element(GASolicitorDetailsGAspec.builder().id(ID)
                                     .email(DUMMY_EMAIL).organisationIdentifier(ORG_ID).build())
        );
    }

    private List<Element<GASolicitorDetailsGAspec>> lipRespondent() {
        return Arrays.asList(element(GASolicitorDetailsGAspec.builder().id(ID)
                                     .forename("respondent")
                                     .surname(Optional.of("lip"))
                                     .email(DUMMY_EMAIL).build()));
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

    private CaseData baseGaCaseData() {
        GeneralApplicationCaseData gaCaseData = GeneralApplicationCaseDataBuilder.builder().build();
        CaseData base = objectMapper.convertValue(gaCaseData, CaseData.class);
        return base.toBuilder()
            .generalAppParentCaseLink(GeneralAppParentCaseLink.builder()
                                      .caseReference(CASE_REFERENCE.toString()).build())
            .generalAppApplnSolicitor(GASolicitorDetailsGAspec.builder().email(DUMMY_EMAIL).build())
            .build();
    }

    private CaseData rebuildToGa(CaseData caseData) {
        Long ccdCaseReference = caseData.getCcdCaseReference();
        CaseState ccdState = caseData.getCcdState();
        GeneralApplicationCaseData gaCaseData = objectMapper.convertValue(caseData, GeneralApplicationCaseData.class);
        CaseData rebuilt = objectMapper.convertValue(gaCaseData, CaseData.class);
        return rebuilt.toBuilder()
            .ccdCaseReference(ccdCaseReference)
            .ccdState(ccdState)
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
