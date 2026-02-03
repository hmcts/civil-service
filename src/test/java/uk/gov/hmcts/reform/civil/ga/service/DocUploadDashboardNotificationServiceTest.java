package uk.gov.hmcts.reform.civil.ga.service;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.documentmanagement.model.Document;
import uk.gov.hmcts.reform.civil.enums.dq.GeneralApplicationTypes;
import uk.gov.hmcts.reform.civil.ga.client.DashboardApiClient;
import uk.gov.hmcts.reform.civil.ga.model.GeneralApplicationCaseData;
import uk.gov.hmcts.reform.civil.ga.model.genapplication.UploadDocumentByType;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.model.genapplication.GAApplicationType;
import uk.gov.hmcts.reform.civil.model.genapplication.GASolicitorDetailsGAspec;
import uk.gov.hmcts.reform.civil.model.genapplication.GAUrgencyRequirement;
import uk.gov.hmcts.reform.civil.sampledata.GeneralApplicationCaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.dashboard.data.ScenarioRequestParams;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_APPLICANT_PROCEED_OFFLINE_APPLICANT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_APPLICANT_PROCEED_OFFLINE_RESPONDENT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_GENERAL_APPLICATION_RESPONSE_SUBMITTED_APPLICANT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_AAA6_GENERAL_APPLICATION_RESPONSE_SUBMITTED_RESPONDENT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_OTHER_PARTY_UPLOADED_DOC_APPLICANT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.dashboardnotifications.DashboardScenarios.SCENARIO_OTHER_PARTY_UPLOADED_DOC_RESPONDENT;
import static uk.gov.hmcts.reform.civil.sampledata.GeneralApplicationCaseDataBuilder.STRING_CONSTANT;
import static uk.gov.hmcts.reform.civil.utils.ElementUtils.element;

@ExtendWith(MockitoExtension.class)
public class DocUploadDashboardNotificationServiceTest {

    private static final String DUMMY_EMAIL = "test@gmail.com";
    @Mock
    DashboardApiClient dashboardApiClient;
    @Mock
    FeatureToggleService featureToggleService;
    @Mock
    GaForLipService gaForLipService;
    @Mock
    GaDashboardNotificationsParamsMapper mapper;
    @InjectMocks
    private DocUploadDashboardNotificationService docUploadDashboardNotificationService;

    @Nested
    class AboutToSubmitCallback {

        @Test
        void shouldCreateDashboardNotificationWhenLipApplicantUploadDoc() {

            List<Element<UploadDocumentByType>> uploadDocumentByApplicant = new ArrayList<>();
            uploadDocumentByApplicant.add(element(UploadDocumentByType.builder()
                                                      .documentType("Witness")
                                                      .additionalDocument(Document.builder()
                                                                              .documentFileName("witness_document.pdf")
                                                                              .documentUrl("http://dm-store:8080")
                                                                              .documentBinaryUrl(
                                                                                  "http://dm-store:8080/documents")
                                                                              .build()).build()));
            HashMap<String, Object> scenarioParams = new HashMap<>();
            when(gaForLipService.isLipResp(any(GeneralApplicationCaseData.class))).thenReturn(true);
            when(mapper.mapCaseDataToParams(any())).thenReturn(scenarioParams);

            GeneralApplicationCaseData caseData = GeneralApplicationCaseDataBuilder.builder()
                .atStateClaimDraft()
                .ccdCaseReference(1678356749555475L)
                .build().toBuilder()
                .respondent2SameLegalRepresentative(NO)
                .applicationIsUncloakedOnce(YES)
                .parentClaimantIsApplicant(YES)
                .uploadDocument(uploadDocumentByApplicant)
                .claimant1PartyName("Mr. John Rambo")
                .defendant1PartyName("Mr. Sole Trader")
                .generalAppConsentOrder(YES)
                .isGaApplicantLip(YES)
                .isGaRespondentOneLip(YES)
                .generalAppApplnSolicitor(new GASolicitorDetailsGAspec().setId(STRING_CONSTANT).setForename(
                        "GAApplnSolicitor")
                                              .setEmail(DUMMY_EMAIL).setOrganisationIdentifier("1"))

                .build();

            docUploadDashboardNotificationService.createDashboardNotification(
                caseData,
                "Applicant",
                "BEARER_TOKEN",
                false
            );

            verify(dashboardApiClient).recordScenario(
                caseData.getCcdCaseReference().toString(),
                SCENARIO_OTHER_PARTY_UPLOADED_DOC_RESPONDENT.getScenario(),
                "BEARER_TOKEN",
                ScenarioRequestParams.builder().params(scenarioParams).build()
            );
        }

        @Test
        void shouldCreateDashboardNotificationWhenLRApplicantUploadDoc() {

            List<Element<UploadDocumentByType>> uploadDocumentByApplicant = new ArrayList<>();
            uploadDocumentByApplicant.add(element(UploadDocumentByType.builder()
                                                      .documentType("Witness")
                                                      .additionalDocument(Document.builder()
                                                                              .documentFileName("witness_document.pdf")
                                                                              .documentUrl("http://dm-store:8080")
                                                                              .documentBinaryUrl(
                                                                                  "http://dm-store:8080/documents")
                                                                              .build()).build()));
            HashMap<String, Object> scenarioParams = new HashMap<>();
            when(gaForLipService.isLipApp(any(GeneralApplicationCaseData.class))).thenReturn(true);
            when(mapper.mapCaseDataToParams(any())).thenReturn(scenarioParams);

            GeneralApplicationCaseData caseData = GeneralApplicationCaseDataBuilder.builder()
                .atStateClaimDraft()
                .ccdCaseReference(1678356749555475L)
                .build().toBuilder()
                .respondent2SameLegalRepresentative(NO)
                .applicationIsUncloakedOnce(YES)
                .parentClaimantIsApplicant(YES)
                .uploadDocument(uploadDocumentByApplicant)
                .claimant1PartyName("Mr. John Rambo")
                .defendant1PartyName("Mr. Sole Trader")
                .generalAppConsentOrder(YES)
                .isGaRespondentOneLip(NO)
                .generalAppUrgencyRequirement(new GAUrgencyRequirement().setGeneralAppUrgency(YES))
                .generalAppApplnSolicitor(new GASolicitorDetailsGAspec().setId(STRING_CONSTANT).setForename(
                        "GAApplnSolicitor")
                                              .setEmail(DUMMY_EMAIL).setOrganisationIdentifier("1"))

                .build();

            docUploadDashboardNotificationService.createDashboardNotification(
                caseData,
                "Respondent One",
                "BEARER_TOKEN",
                true
            );

            verify(dashboardApiClient).recordScenario(
                caseData.getCcdCaseReference().toString(),
                SCENARIO_OTHER_PARTY_UPLOADED_DOC_APPLICANT.getScenario(),
                "BEARER_TOKEN",
                ScenarioRequestParams.builder().params(scenarioParams).build()
            );

            verify(dashboardApiClient).recordScenario(
                caseData.getCcdCaseReference().toString(),
                SCENARIO_AAA6_GENERAL_APPLICATION_RESPONSE_SUBMITTED_APPLICANT.getScenario(),
                "BEARER_TOKEN",
                ScenarioRequestParams.builder().params(scenarioParams).build()
            );
        }

        @Test
        void shouldCreateDashboardNotificationWhenLipRespondentUploadDoc() {

            List<Element<UploadDocumentByType>> uploadDocumentByApplicant = new ArrayList<>();
            uploadDocumentByApplicant.add(element(UploadDocumentByType.builder()
                                                      .documentType("Witness")
                                                      .additionalDocument(Document.builder()
                                                                              .documentFileName("witness_document.pdf")
                                                                              .documentUrl("http://dm-store:8080")
                                                                              .documentBinaryUrl(
                                                                                  "http://dm-store:8080/documents")
                                                                              .build()).build()));
            List<Element<GASolicitorDetailsGAspec>> gaRespSolicitors = new ArrayList<>();
            gaRespSolicitors.add(element(new GASolicitorDetailsGAspec()
                                             .setId(STRING_CONSTANT)
                                             .setEmail(DUMMY_EMAIL)
                                             .setOrganisationIdentifier("2")));

            HashMap<String, Object> scenarioParams = new HashMap<>();
            when(gaForLipService.isLipApp(any(GeneralApplicationCaseData.class))).thenReturn(true);
            when(mapper.mapCaseDataToParams(any())).thenReturn(scenarioParams);

            GeneralApplicationCaseData caseData = GeneralApplicationCaseDataBuilder.builder()
                .atStateClaimDraft()
                .ccdCaseReference(1678356749555475L)
                .build().toBuilder()
                .respondent2SameLegalRepresentative(NO)
                .applicationIsUncloakedOnce(YES)
                .parentClaimantIsApplicant(YES)
                .uploadDocument(uploadDocumentByApplicant)
                .claimant1PartyName("Mr. John Rambo")
                .defendant1PartyName("Mr. Sole Trader")
                .generalAppConsentOrder(YES)
                .isGaApplicantLip(YES)
                .isGaRespondentOneLip(YES)
                .generalAppApplnSolicitor(new GASolicitorDetailsGAspec().setId("123456789").setForename("GAApplnSolicitor")
                                              .setEmail(DUMMY_EMAIL).setOrganisationIdentifier("1"))
                .generalAppRespondentSolicitors(gaRespSolicitors)

                .build();

            docUploadDashboardNotificationService.createDashboardNotification(
                caseData,
                "Respondent One",
                "BEARER_TOKEN",
                false
            );

            verify(dashboardApiClient).recordScenario(
                caseData.getCcdCaseReference().toString(),
                SCENARIO_OTHER_PARTY_UPLOADED_DOC_APPLICANT.getScenario(),
                "BEARER_TOKEN",
                ScenarioRequestParams.builder().params(scenarioParams).build()
            );
        }

        @Test
        void shouldCreateDashboardNotificationWhenConsentOrder() {

            List<Element<UploadDocumentByType>> uploadDocumentByApplicant = new ArrayList<>();
            uploadDocumentByApplicant.add(element(UploadDocumentByType.builder()
                                                      .documentType("Witness")
                                                      .additionalDocument(Document.builder()
                                                                              .documentFileName("witness_document.pdf")
                                                                              .documentUrl("http://dm-store:8080")
                                                                              .documentBinaryUrl(
                                                                                  "http://dm-store:8080/documents")
                                                                              .build()).build()));
            List<Element<GASolicitorDetailsGAspec>> gaRespSolicitors = new ArrayList<>();
            gaRespSolicitors.add(element(new GASolicitorDetailsGAspec()
                                             .setId(STRING_CONSTANT)
                                             .setEmail(DUMMY_EMAIL)
                                             .setOrganisationIdentifier("2")));

            HashMap<String, Object> scenarioParams = new HashMap<>();
            when(gaForLipService.isLipApp(any(GeneralApplicationCaseData.class))).thenReturn(true);
            when(mapper.mapCaseDataToParams(any())).thenReturn(scenarioParams);

            GeneralApplicationCaseData caseData = GeneralApplicationCaseDataBuilder.builder()
                .atStateClaimDraft()
                .ccdCaseReference(1678356749555475L)
                .build().toBuilder()
                .respondent2SameLegalRepresentative(NO)
                .generalAppConsentOrder(YES)
                .parentClaimantIsApplicant(YES)
                .uploadDocument(uploadDocumentByApplicant)
                .claimant1PartyName("Mr. John Rambo")
                .defendant1PartyName("Mr. Sole Trader")
                .generalAppConsentOrder(YES)
                .isGaApplicantLip(YES)
                .isGaRespondentOneLip(YES)
                .generalAppApplnSolicitor(new GASolicitorDetailsGAspec().setId("123456789").setForename("GAApplnSolicitor")
                                              .setEmail(DUMMY_EMAIL).setOrganisationIdentifier("1"))
                .generalAppRespondentSolicitors(gaRespSolicitors)

                .build();

            docUploadDashboardNotificationService.createDashboardNotification(
                caseData,
                "Respondent One",
                "BEARER_TOKEN",
                false
            );

            verify(dashboardApiClient).recordScenario(
                caseData.getCcdCaseReference().toString(),
                SCENARIO_OTHER_PARTY_UPLOADED_DOC_APPLICANT.getScenario(),
                "BEARER_TOKEN",
                ScenarioRequestParams.builder().params(scenarioParams).build()
            );
        }

        @Test
        void shouldCreateResponseDashboardNotificationWhenConsentOrderForRespondent() {

            List<Element<UploadDocumentByType>> uploadDocumentByApplicant = new ArrayList<>();
            uploadDocumentByApplicant.add(element(UploadDocumentByType.builder()
                                                      .documentType("Witness")
                                                      .additionalDocument(Document.builder()
                                                                              .documentFileName("witness_document.pdf")
                                                                              .documentUrl("http://dm-store:8080")
                                                                              .documentBinaryUrl(
                                                                                  "http://dm-store:8080/documents")
                                                                              .build()).build()));
            List<Element<GASolicitorDetailsGAspec>> gaRespSolicitors = new ArrayList<>();
            gaRespSolicitors.add(element(new GASolicitorDetailsGAspec()
                                             .setId(STRING_CONSTANT)
                                             .setEmail(DUMMY_EMAIL)
                                             .setOrganisationIdentifier("2")));

            HashMap<String, Object> scenarioParams = new HashMap<>();
            when(gaForLipService.isLipResp(any(GeneralApplicationCaseData.class))).thenReturn(true);
            when(mapper.mapCaseDataToParams(any())).thenReturn(scenarioParams);

            GeneralApplicationCaseData caseData = GeneralApplicationCaseDataBuilder.builder()
                .atStateClaimDraft()
                .ccdCaseReference(1678356749555475L)
                .build().toBuilder()
                .respondent2SameLegalRepresentative(NO)
                .generalAppConsentOrder(YES)
                .parentClaimantIsApplicant(YES)
                .uploadDocument(uploadDocumentByApplicant)
                .claimant1PartyName("Mr. John Rambo")
                .defendant1PartyName("Mr. Sole Trader")
                .generalAppConsentOrder(YES)
                .isGaApplicantLip(YES)
                .isGaRespondentOneLip(YES)
                .generalAppApplnSolicitor(new GASolicitorDetailsGAspec().setId("123456789").setForename("GAApplnSolicitor")
                                              .setEmail(DUMMY_EMAIL).setOrganisationIdentifier("1"))
                .generalAppRespondentSolicitors(gaRespSolicitors)

                .build();

            docUploadDashboardNotificationService.createResponseDashboardNotification(
                caseData,
                "RESPONDENT",
                "BEARER_TOKEN"
            );

            verify(dashboardApiClient).recordScenario(
                caseData.getCcdCaseReference().toString(),
                SCENARIO_AAA6_GENERAL_APPLICATION_RESPONSE_SUBMITTED_RESPONDENT.getScenario(),
                "BEARER_TOKEN",
                ScenarioRequestParams.builder().params(scenarioParams).build()
            );
        }

        @Test
        void shouldCreateOfflineResponseDashboardNotificationWhenVaryJudgmentForRespondent() {

            List<Element<GASolicitorDetailsGAspec>> gaRespSolicitors = new ArrayList<>();
            List<GeneralApplicationTypes> generalAppType = new ArrayList<>();
            generalAppType.add(GeneralApplicationTypes.VARY_PAYMENT_TERMS_OF_JUDGMENT);
            gaRespSolicitors.add(element(new GASolicitorDetailsGAspec()
                                             .setId(STRING_CONSTANT)
                                             .setEmail(DUMMY_EMAIL)
                                             .setOrganisationIdentifier("2")));

            HashMap<String, Object> scenarioParams = new HashMap<>();
            when(mapper.mapCaseDataToParams(any())).thenReturn(scenarioParams);

            GeneralApplicationCaseData caseData = GeneralApplicationCaseDataBuilder.builder()
                .atStateClaimDraft()
                .ccdCaseReference(1678356749555475L)
                .build().toBuilder()
                .respondent2SameLegalRepresentative(NO)
                .generalAppConsentOrder(YES)
                .parentCaseReference("1678356789555475")
                .parentClaimantIsApplicant(NO)
                .claimant1PartyName("Mr. John Rambo")
                .defendant1PartyName("Mr. Sole Trader")
                .generalAppConsentOrder(YES)
                .isGaApplicantLip(YES)
                .isGaRespondentOneLip(YES)
                .generalAppType(new GAApplicationType(generalAppType))
                .generalAppApplnSolicitor(new GASolicitorDetailsGAspec().setId("123456789").setForename("GAApplnSolicitor")
                                              .setEmail(DUMMY_EMAIL).setOrganisationIdentifier("1"))
                .generalAppRespondentSolicitors(gaRespSolicitors)

                .build();

            docUploadDashboardNotificationService.createOfflineResponseDashboardNotification(
                caseData,
                "RESPONDENT",
                "BEARER_TOKEN"
            );

            verify(dashboardApiClient).recordScenario(
                caseData.getCcdCaseReference().toString(),
                SCENARIO_AAA6_APPLICANT_PROCEED_OFFLINE_RESPONDENT.getScenario(),
                "BEARER_TOKEN",
                ScenarioRequestParams.builder().params(scenarioParams).build()
            );
        }

        @Test
        void shouldCreateOfflineResponseDashboardNotificationWhenVaryJudgmentForApplicant() {

            List<Element<GASolicitorDetailsGAspec>> gaRespSolicitors = new ArrayList<>();
            List<GeneralApplicationTypes> generalAppType = new ArrayList<>();
            generalAppType.add(GeneralApplicationTypes.VARY_PAYMENT_TERMS_OF_JUDGMENT);
            gaRespSolicitors.add(element(new GASolicitorDetailsGAspec()
                                             .setId(STRING_CONSTANT)
                                             .setEmail(DUMMY_EMAIL)
                                             .setOrganisationIdentifier("2")));

            HashMap<String, Object> scenarioParams = new HashMap<>();
            when(mapper.mapCaseDataToParams(any())).thenReturn(scenarioParams);

            GeneralApplicationCaseData caseData = GeneralApplicationCaseDataBuilder.builder()
                .atStateClaimDraft()
                .ccdCaseReference(1678356749555475L)
                .build().toBuilder()
                .respondent2SameLegalRepresentative(NO)
                .generalAppConsentOrder(YES)
                .parentClaimantIsApplicant(YES)
                .claimant1PartyName("Mr. John Rambo")
                .defendant1PartyName("Mr. Sole Trader")
                .generalAppConsentOrder(YES)
                .isGaApplicantLip(YES)
                .isGaRespondentOneLip(YES)
                .parentCaseReference("1678356746785475")
                .generalAppType(new GAApplicationType(generalAppType))
                .generalAppApplnSolicitor(new GASolicitorDetailsGAspec().setId("123456789").setForename("GAApplnSolicitor")
                                              .setEmail(DUMMY_EMAIL).setOrganisationIdentifier("1"))
                .generalAppRespondentSolicitors(gaRespSolicitors)

                .build();

            docUploadDashboardNotificationService.createOfflineResponseDashboardNotification(
                caseData,
                "APPLICANT",
                "BEARER_TOKEN"
            );

            verify(dashboardApiClient).recordScenario(
                caseData.getCcdCaseReference().toString(),

                SCENARIO_AAA6_APPLICANT_PROCEED_OFFLINE_APPLICANT.getScenario(),
                "BEARER_TOKEN",
                ScenarioRequestParams.builder().params(scenarioParams).build()
            );
        }

        @Test
        void shouldCreateResponseDashboardNotificationWhenConsentOrderForApplicant() {

            List<Element<UploadDocumentByType>> uploadDocumentByApplicant = new ArrayList<>();
            uploadDocumentByApplicant.add(element(UploadDocumentByType.builder()
                                                      .documentType("Witness")
                                                      .additionalDocument(Document.builder()
                                                                              .documentFileName("witness_document.pdf")
                                                                              .documentUrl("http://dm-store:8080")
                                                                              .documentBinaryUrl(
                                                                                  "http://dm-store:8080/documents")
                                                                              .build()).build()));
            List<Element<GASolicitorDetailsGAspec>> gaRespSolicitors = new ArrayList<>();
            gaRespSolicitors.add(element(new GASolicitorDetailsGAspec()
                                             .setId(STRING_CONSTANT)
                                             .setEmail(DUMMY_EMAIL)
                                             .setOrganisationIdentifier("2")));

            HashMap<String, Object> scenarioParams = new HashMap<>();
            when(gaForLipService.isLipApp(any(GeneralApplicationCaseData.class))).thenReturn(true);
            when(mapper.mapCaseDataToParams(any())).thenReturn(scenarioParams);

            GeneralApplicationCaseData caseData = GeneralApplicationCaseDataBuilder.builder()
                .atStateClaimDraft()
                .ccdCaseReference(1678356749555475L)
                .build().toBuilder()
                .respondent2SameLegalRepresentative(NO)
                .generalAppConsentOrder(YES)
                .parentClaimantIsApplicant(YES)
                .uploadDocument(uploadDocumentByApplicant)
                .claimant1PartyName("Mr. John Rambo")
                .defendant1PartyName("Mr. Sole Trader")
                .generalAppConsentOrder(YES)
                .isGaApplicantLip(YES)
                .isGaRespondentOneLip(YES)
                .generalAppApplnSolicitor(new GASolicitorDetailsGAspec().setId("123456789").setForename("GAApplnSolicitor")
                                              .setEmail(DUMMY_EMAIL).setOrganisationIdentifier("1"))
                .generalAppRespondentSolicitors(gaRespSolicitors)

                .build();

            docUploadDashboardNotificationService.createResponseDashboardNotification(
                caseData,
                "APPLICANT",
                "BEARER_TOKEN"
            );

            verify(dashboardApiClient).recordScenario(
                caseData.getCcdCaseReference().toString(),
                SCENARIO_AAA6_GENERAL_APPLICATION_RESPONSE_SUBMITTED_APPLICANT.getScenario(),
                "BEARER_TOKEN",
                ScenarioRequestParams.builder().params(scenarioParams).build()
            );
        }

        @Test
        void shouldNotCreateOfflineResponseDashboardNotificationWhenConsentOrderForApplicant() {

            List<Element<UploadDocumentByType>> uploadDocumentByApplicant = new ArrayList<>();
            uploadDocumentByApplicant.add(element(UploadDocumentByType.builder()
                                                      .documentType("Witness")
                                                      .additionalDocument(Document.builder()
                                                                              .documentFileName("witness_document.pdf")
                                                                              .documentUrl("http://dm-store:8080")
                                                                              .documentBinaryUrl(
                                                                                  "http://dm-store:8080/documents")
                                                                              .build()).build()));
            List<Element<GASolicitorDetailsGAspec>> gaRespSolicitors = new ArrayList<>();
            gaRespSolicitors.add(element(new GASolicitorDetailsGAspec()
                                             .setId(STRING_CONSTANT)
                                             .setEmail(DUMMY_EMAIL)
                                             .setOrganisationIdentifier("2")));

            HashMap<String, Object> scenarioParams = new HashMap<>();

            GeneralApplicationCaseData caseData = GeneralApplicationCaseDataBuilder.builder()
                .atStateClaimDraft()
                .ccdCaseReference(1678356749555475L)
                .build().toBuilder()
                .respondent2SameLegalRepresentative(NO)
                .parentCaseReference("1678356767855475")
                .generalAppConsentOrder(NO)
                .parentClaimantIsApplicant(YES)
                .uploadDocument(uploadDocumentByApplicant)
                .claimant1PartyName("Mr. John Rambo")
                .defendant1PartyName("Mr. Sole Trader")
                .isGaApplicantLip(NO)
                .isGaRespondentOneLip(NO)
                .generalAppApplnSolicitor(new GASolicitorDetailsGAspec().setId("123456789").setForename("GAApplnSolicitor")
                                              .setEmail(DUMMY_EMAIL).setOrganisationIdentifier("1"))
                .generalAppRespondentSolicitors(gaRespSolicitors)

                .build();

            docUploadDashboardNotificationService.createOfflineResponseDashboardNotification(
                caseData,
                "CLAIMANT",
                "BEARER_TOKEN"
            );

            verifyNoInteractions(dashboardApiClient);
        }

        @Test
        void shouldNotCreateOfflineResponseDashboardNotificationWhenConsentOrderForRespondent() {

            List<Element<UploadDocumentByType>> uploadDocumentByApplicant = new ArrayList<>();
            uploadDocumentByApplicant.add(element(UploadDocumentByType.builder()
                                                      .documentType("Witness")
                                                      .additionalDocument(Document.builder()
                                                                              .documentFileName("witness_document.pdf")
                                                                              .documentUrl("http://dm-store:8080")
                                                                              .documentBinaryUrl(
                                                                                  "http://dm-store:8080/documents")
                                                                              .build()).build()));
            List<Element<GASolicitorDetailsGAspec>> gaRespSolicitors = new ArrayList<>();
            gaRespSolicitors.add(element(new GASolicitorDetailsGAspec()
                                             .setId(STRING_CONSTANT)
                                             .setEmail(DUMMY_EMAIL)
                                             .setOrganisationIdentifier("2")));

            HashMap<String, Object> scenarioParams = new HashMap<>();

            GeneralApplicationCaseData caseData = GeneralApplicationCaseDataBuilder.builder()
                .atStateClaimDraft()
                .ccdCaseReference(1678356749555475L)
                .build().toBuilder()
                .respondent2SameLegalRepresentative(NO)
                .parentCaseReference("1678356767855475")
                .generalAppConsentOrder(NO)
                .parentClaimantIsApplicant(YES)
                .uploadDocument(uploadDocumentByApplicant)
                .claimant1PartyName("Mr. John Rambo")
                .defendant1PartyName("Mr. Sole Trader")
                .isGaApplicantLip(YES)
                .isGaRespondentOneLip(YES)
                .generalAppApplnSolicitor(new GASolicitorDetailsGAspec().setId("123456789").setForename("GAApplnSolicitor")
                                              .setEmail(DUMMY_EMAIL).setOrganisationIdentifier("1"))
                .generalAppRespondentSolicitors(gaRespSolicitors)

                .build();

            docUploadDashboardNotificationService.createOfflineResponseDashboardNotification(
                caseData,
                "DEFENDANT",
                "BEARER_TOKEN"
            );

            verifyNoInteractions(dashboardApiClient);
        }

        @Test
        void shouldNotCreateResponseDashboardNotificationWhenConsentOrderForApplicant() {

            List<Element<UploadDocumentByType>> uploadDocumentByApplicant = new ArrayList<>();
            uploadDocumentByApplicant.add(element(UploadDocumentByType.builder()
                                                      .documentType("Witness")
                                                      .additionalDocument(Document.builder()
                                                                              .documentFileName("witness_document.pdf")
                                                                              .documentUrl("http://dm-store:8080")
                                                                              .documentBinaryUrl(
                                                                                  "http://dm-store:8080/documents")
                                                                              .build()).build()));
            List<Element<GASolicitorDetailsGAspec>> gaRespSolicitors = new ArrayList<>();
            gaRespSolicitors.add(element(new GASolicitorDetailsGAspec()
                                             .setId(STRING_CONSTANT)
                                             .setEmail(DUMMY_EMAIL)
                                             .setOrganisationIdentifier("2")));

            HashMap<String, Object> scenarioParams = new HashMap<>();

            GeneralApplicationCaseData caseData = GeneralApplicationCaseDataBuilder.builder()
                .atStateClaimDraft()
                .ccdCaseReference(1678356749555475L)
                .build().toBuilder()
                .respondent2SameLegalRepresentative(NO)
                .generalAppConsentOrder(NO)
                .parentClaimantIsApplicant(YES)
                .uploadDocument(uploadDocumentByApplicant)
                .claimant1PartyName("Mr. John Rambo")
                .defendant1PartyName("Mr. Sole Trader")
                .isGaApplicantLip(YES)
                .isGaRespondentOneLip(YES)
                .generalAppApplnSolicitor(new GASolicitorDetailsGAspec().setId("123456789").setForename("GAApplnSolicitor")
                                              .setEmail(DUMMY_EMAIL).setOrganisationIdentifier("1"))
                .generalAppRespondentSolicitors(gaRespSolicitors)

                .build();

            docUploadDashboardNotificationService.createResponseDashboardNotification(
                caseData,
                "APPLICANT ONE",
                "BEARER_TOKEN"
            );

            verifyNoInteractions(dashboardApiClient);
        }

        @Test
        void shouldNotCreateResponseDashboardNotificationWhenConsentOrderForRespondent() {

            List<Element<UploadDocumentByType>> uploadDocumentByApplicant = new ArrayList<>();
            uploadDocumentByApplicant.add(element(UploadDocumentByType.builder()
                                                      .documentType("Witness")
                                                      .additionalDocument(Document.builder()
                                                                              .documentFileName("witness_document.pdf")
                                                                              .documentUrl("http://dm-store:8080")
                                                                              .documentBinaryUrl(
                                                                                  "http://dm-store:8080/documents")
                                                                              .build()).build()));
            List<Element<GASolicitorDetailsGAspec>> gaRespSolicitors = new ArrayList<>();
            gaRespSolicitors.add(element(new GASolicitorDetailsGAspec()
                                             .setId(STRING_CONSTANT)
                                             .setEmail(DUMMY_EMAIL)
                                             .setOrganisationIdentifier("2")));

            HashMap<String, Object> scenarioParams = new HashMap<>();
            GeneralApplicationCaseData caseData = GeneralApplicationCaseDataBuilder.builder()
                .atStateClaimDraft()
                .ccdCaseReference(1678356749555475L)
                .build().toBuilder()
                .respondent2SameLegalRepresentative(NO)
                .generalAppConsentOrder(NO)
                .parentClaimantIsApplicant(YES)
                .uploadDocument(uploadDocumentByApplicant)
                .claimant1PartyName("Mr. John Rambo")
                .defendant1PartyName("Mr. Sole Trader")
                .isGaApplicantLip(YES)
                .isGaRespondentOneLip(YES)
                .generalAppApplnSolicitor(new GASolicitorDetailsGAspec().setId("123456789").setForename("GAApplnSolicitor")
                                              .setEmail(DUMMY_EMAIL).setOrganisationIdentifier("1"))
                .generalAppRespondentSolicitors(gaRespSolicitors)

                .build();

            docUploadDashboardNotificationService.createResponseDashboardNotification(
                caseData,
                "DEFENDANT",
                "BEARER_TOKEN"
            );

            verifyNoInteractions(dashboardApiClient);
        }

        @Test
        void shouldNotCreateDashboardNotificationWhenRoleIsNotApplicantOrRespondent() {

            List<Element<UploadDocumentByType>> uploadDocumentByApplicant = new ArrayList<>();
            uploadDocumentByApplicant.add(element(UploadDocumentByType.builder()
                                                      .documentType("Witness")
                                                      .additionalDocument(Document.builder()
                                                                              .documentFileName("witness_document.pdf")
                                                                              .documentUrl("http://dm-store:8080")
                                                                              .documentBinaryUrl(
                                                                                  "http://dm-store:8080/documents")
                                                                              .build()).build()));
            GeneralApplicationCaseData caseData = GeneralApplicationCaseDataBuilder.builder()
                .atStateClaimDraft()
                .ccdCaseReference(1678356749555475L)
                .build().toBuilder()
                .respondent2SameLegalRepresentative(NO)
                .generalAppConsentOrder(YES)
                .parentClaimantIsApplicant(YES)
                .uploadDocument(uploadDocumentByApplicant)
                .claimant1PartyName("Mr. John Rambo")
                .defendant1PartyName("Mr. Sole Trader")
                .generalAppConsentOrder(YES)
                .isGaApplicantLip(YES)
                .isGaRespondentOneLip(YES)
                .generalAppApplnSolicitor(new GASolicitorDetailsGAspec().setId(STRING_CONSTANT).setForename(
                        "GAApplnSolicitor")
                                              .setEmail(DUMMY_EMAIL).setOrganisationIdentifier("1"))

                .build();

            docUploadDashboardNotificationService.createDashboardNotification(
                caseData,
                "Respondent Not",
                "BEARER_TOKEN",
                false
            );

            verifyNoInteractions(dashboardApiClient);
        }

    }
}
