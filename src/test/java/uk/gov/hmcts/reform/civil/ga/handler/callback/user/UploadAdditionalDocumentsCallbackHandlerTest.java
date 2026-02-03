package uk.gov.hmcts.reform.civil.ga.handler.callback.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.enums.BusinessProcessStatus;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.ga.handler.GeneralApplicationBaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.ga.model.GeneralApplicationCaseData;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.documentmanagement.model.CaseDocument;
import uk.gov.hmcts.reform.civil.documentmanagement.model.Document;
import uk.gov.hmcts.reform.civil.model.genapplication.GAInformOtherParty;
import uk.gov.hmcts.reform.civil.model.genapplication.GASolicitorDetailsGAspec;
import uk.gov.hmcts.reform.civil.model.genapplication.GAUrgencyRequirement;
import uk.gov.hmcts.reform.civil.ga.model.genapplication.UploadDocumentByType;
import uk.gov.hmcts.reform.civil.ga.service.DocUploadDashboardNotificationService;
import uk.gov.hmcts.reform.civil.ga.service.GaForLipService;
import uk.gov.hmcts.reform.civil.sampledata.GeneralApplicationCaseDataBuilder;
import uk.gov.hmcts.reform.civil.utils.AssignCategoryId;
import uk.gov.hmcts.reform.idam.client.IdamClient;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static java.lang.String.format;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.SUBMITTED;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.UPLOAD_ADDL_DOCUMENTS;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;
import static uk.gov.hmcts.reform.civil.sampledata.GeneralApplicationCaseDataBuilder.STRING_CONSTANT;
import static uk.gov.hmcts.reform.civil.utils.ElementUtils.element;

@SuppressWarnings({"checkstyle:EmptyLineSeparator", "checkstyle:Indentation"})
@SpringBootTest(classes = {
    UploadAdditionalDocumentsCallbackHandler.class,
    JacksonAutoConfiguration.class})
class UploadAdditionalDocumentsCallbackHandlerTest extends GeneralApplicationBaseCallbackHandlerTest {

    @Autowired
    UploadAdditionalDocumentsCallbackHandler handler;
    @Autowired
    ObjectMapper objectMapper;
    @MockBean
    IdamClient idamClient;

    @MockBean
    CaseDetailsConverter caseDetailsConverter;

    @MockBean
    AssignCategoryId assignCategoryId;
    @MockBean
    DocUploadDashboardNotificationService docUploadDashboardNotificationService;
    @MockBean
    GaForLipService gaForLipService;

    List<Element<CaseDocument>> documents = new ArrayList<>();

    private static final String DUMMY_EMAIL = "test@gmail.com";

    @BeforeEach
    public void setUp() throws IOException {
        when(gaForLipService.isGaForLip(any(GeneralApplicationCaseData.class))).thenReturn(false);

        when(idamClient.getUserInfo(anyString())).thenReturn(UserInfo.builder()
                                                                 .sub(DUMMY_EMAIL)
                                                                 .uid(STRING_CONSTANT)
                                                                 .build());

    }

    @Test
    void handleEventsReturnsTheExpectedCallbackEvent() {
        assertThat(handler.handledEvents()).contains(UPLOAD_ADDL_DOCUMENTS);
    }

    @Nested
    class AboutToSubmit {

        @Test
        void shouldSetUpReadyBusinessProcessWhenJudgeUncloaked() {

            List<Element<UploadDocumentByType>> uploadDocumentByApplicant = new ArrayList<>();
            uploadDocumentByApplicant.add(element(UploadDocumentByType.builder()
                                                      .documentType("Witness")
                                                      .additionalDocument(Document.builder()
                                                                              .documentFileName("witness_document.pdf")
                                                                              .documentUrl("http://dm-store:8080")
                                                                              .documentBinaryUrl("http://dm-store:8080/documents")
                                                                              .build()).build()));
            GeneralApplicationCaseData caseData = GeneralApplicationCaseDataBuilder.builder()
                .atStateClaimDraft()
                .ccdCaseReference(1678356749555475L)
                .build().toBuilder()
                .respondent2SameLegalRepresentative(YesOrNo.NO)
                .applicationIsUncloakedOnce(YES)
                .parentClaimantIsApplicant(YES)
                .generalAppApplnSolicitor(new GASolicitorDetailsGAspec().setId(STRING_CONSTANT).setForename("GAApplnSolicitor")
                                              .setEmail(DUMMY_EMAIL).setOrganisationIdentifier("1"))
                .uploadDocument(uploadDocumentByApplicant)
                .claimant1PartyName("Mr. John Rambo")
                .defendant1PartyName("Mr. Sole Trader")
                .build();
            when(caseDetailsConverter.toGeneralApplicationCaseData(any())).thenReturn(caseData);
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            GeneralApplicationCaseData responseCaseData = objectMapper.convertValue(response.getData(), GeneralApplicationCaseData.class);

            assertThat(responseCaseData.getBusinessProcess().getStatus()).isEqualTo(BusinessProcessStatus.READY);
            assertThat(responseCaseData.getBusinessProcess().getCamundaEvent()).isEqualTo(UPLOAD_ADDL_DOCUMENTS.toString());
            assertThat(responseCaseData.getIsDocumentVisible()).isEqualTo(YES);
        }

        @Test
        void shouldSetUpReadyBusinessProcessWhenJudgeCloakedApplication() {

            List<Element<UploadDocumentByType>> uploadDocumentByApplicant = new ArrayList<>();
            uploadDocumentByApplicant.add(element(UploadDocumentByType.builder()
                                                      .documentType("witness")
                                                      .additionalDocument(Document.builder()
                                                                              .documentFileName("witness_document.pdf")
                                                                              .documentUrl("http://dm-store:8080")
                                                                              .documentBinaryUrl("http://dm-store:8080/documents")
                                                                              .build()).build()));
            GeneralApplicationCaseData caseData = GeneralApplicationCaseDataBuilder.builder()
                .atStateClaimDraft()
                .ccdCaseReference(1678356749555475L)
                .build().toBuilder()
                .respondent2SameLegalRepresentative(YesOrNo.NO)
                .applicationIsCloaked(NO)
                .parentClaimantIsApplicant(YES)
                .generalAppApplnSolicitor(new GASolicitorDetailsGAspec().setId(STRING_CONSTANT).setForename("GAApplnSolicitor")
                                              .setEmail(DUMMY_EMAIL).setOrganisationIdentifier("1"))
                .uploadDocument(uploadDocumentByApplicant)
                .claimant1PartyName("Mr. John Rambo")
                .defendant1PartyName("Mr. Sole Trader")
                .build();
            when(caseDetailsConverter.toGeneralApplicationCaseData(any())).thenReturn(caseData);
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            GeneralApplicationCaseData responseCaseData = objectMapper.convertValue(response.getData(), GeneralApplicationCaseData.class);

            assertThat(responseCaseData.getBusinessProcess().getStatus()).isEqualTo(BusinessProcessStatus.READY);
            assertThat(responseCaseData.getBusinessProcess().getCamundaEvent()).isEqualTo(UPLOAD_ADDL_DOCUMENTS.toString());
            assertThat(responseCaseData.getIsDocumentVisible()).isEqualTo(YES);
        }

        @Test
        void shouldSetUpReadyBusinessProcessWhenJudgeIsNotUncloakedAndInformOtherPartyIsYes() {

            List<Element<UploadDocumentByType>> uploadDocumentByApplicant = new ArrayList<>();
            uploadDocumentByApplicant.add(element(UploadDocumentByType.builder()
                                                      .documentType("witness")
                                                      .additionalDocument(Document.builder()
                                                                              .documentFileName("witness_document.pdf")
                                                                              .documentUrl("http://dm-store:8080")
                                                                              .documentBinaryUrl("http://dm-store:8080/documents")
                                                                              .build()).build()));
            GeneralApplicationCaseData caseData = GeneralApplicationCaseDataBuilder.builder()
                .atStateClaimDraft()
                .ccdCaseReference(1678356749555475L)
                .build().toBuilder()
                .respondent2SameLegalRepresentative(YesOrNo.NO)
                .generalAppInformOtherParty(new GAInformOtherParty().setIsWithNotice(YES))
                .parentClaimantIsApplicant(YES)
                .generalAppApplnSolicitor(new GASolicitorDetailsGAspec().setId(STRING_CONSTANT).setForename("GAApplnSolicitor")
                                              .setEmail(DUMMY_EMAIL).setOrganisationIdentifier("1"))
                .uploadDocument(uploadDocumentByApplicant)
                .claimant1PartyName("Mr. John Rambo")
                .defendant1PartyName("Mr. Sole Trader")
                .build();
            when(caseDetailsConverter.toGeneralApplicationCaseData(any())).thenReturn(caseData);
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            GeneralApplicationCaseData responseCaseData = objectMapper.convertValue(response.getData(), GeneralApplicationCaseData.class);

            assertThat(responseCaseData.getBusinessProcess().getStatus()).isEqualTo(BusinessProcessStatus.READY);
            assertThat(responseCaseData.getIsDocumentVisible()).isEqualTo(YesOrNo.YES);
        }

        @Test
        void shouldSetUpReadyBusinessProcessWhenApplicationIsUrgent() {

            List<Element<UploadDocumentByType>> uploadDocumentByApplicant = new ArrayList<>();
            uploadDocumentByApplicant.add(element(UploadDocumentByType.builder()
                                                      .documentType("witness")
                                                      .additionalDocument(Document.builder()
                                                                              .documentFileName("witness_document.pdf")
                                                                              .documentUrl("http://dm-store:8080")
                                                                              .documentBinaryUrl("http://dm-store:8080/documents")
                                                                              .build()).build()));
            List<Element<GASolicitorDetailsGAspec>> gaApplAddlSolicitors = new ArrayList<>();
            gaApplAddlSolicitors.add(element(new GASolicitorDetailsGAspec()
                                                 .setId("id1")
                                                 .setEmail(DUMMY_EMAIL)
                                                 .setOrganisationIdentifier("1")));
            GeneralApplicationCaseData caseData = GeneralApplicationCaseDataBuilder.builder()
                .atStateClaimDraft()
                .ccdCaseReference(1678356749555475L)
                .build().toBuilder()
                .respondent2SameLegalRepresentative(YesOrNo.NO)
                .generalAppUrgencyRequirement(new GAUrgencyRequirement().setGeneralAppUrgency(YES))
                .parentClaimantIsApplicant(YES)
                .generalAppApplnSolicitor(new GASolicitorDetailsGAspec().setId(STRING_CONSTANT).setForename("GAApplnSolicitor")
                                              .setEmail(DUMMY_EMAIL).setOrganisationIdentifier("1"))
                .generalAppApplicantAddlSolicitors(gaApplAddlSolicitors)
                .uploadDocument(uploadDocumentByApplicant)
                .claimant1PartyName("Mr. John Rambo")
                .defendant1PartyName("Mr. Sole Trader")
                .build();
            when(caseDetailsConverter.toGeneralApplicationCaseData(any())).thenReturn(caseData);
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            GeneralApplicationCaseData responseCaseData = objectMapper.convertValue(response.getData(), GeneralApplicationCaseData.class);

            assertThat(responseCaseData.getBusinessProcess().getStatus()).isEqualTo(BusinessProcessStatus.READY);
            assertThat(responseCaseData.getIsDocumentVisible()).isEqualTo(YesOrNo.NO);
        }

        @Test
        void shouldSetUpReadyBusinessProcessWhenApplicationIsUrgentAddedToApplicantAddlUser() {

            List<Element<UploadDocumentByType>> uploadDocumentByApplicant = new ArrayList<>();
            uploadDocumentByApplicant.add(element(UploadDocumentByType.builder()
                                                      .documentType("witness")
                                                      .additionalDocument(Document.builder()
                                                                              .documentFileName("witness_document.pdf")
                                                                              .documentUrl("http://dm-store:8080")
                                                                              .documentBinaryUrl("http://dm-store:8080/documents")
                                                                              .build()).build()));
            List<Element<GASolicitorDetailsGAspec>> gaApplAddlSolicitors = new ArrayList<>();
            gaApplAddlSolicitors.add(element(new GASolicitorDetailsGAspec()
                                                 .setId(STRING_CONSTANT)
                                                 .setEmail(DUMMY_EMAIL)
                                                 .setOrganisationIdentifier("1")));
            GeneralApplicationCaseData caseData = GeneralApplicationCaseDataBuilder.builder()
                .atStateClaimDraft()
                .ccdCaseReference(1678356749555475L)
                .build().toBuilder()
                .respondent2SameLegalRepresentative(YesOrNo.NO)
                .generalAppUrgencyRequirement(new GAUrgencyRequirement().setGeneralAppUrgency(YES))
                .parentClaimantIsApplicant(YES)
                .generalAppApplnSolicitor(new GASolicitorDetailsGAspec().setId("id1").setForename("GAApplnSolicitor")
                                              .setEmail(DUMMY_EMAIL).setOrganisationIdentifier("1"))
                .generalAppApplicantAddlSolicitors(gaApplAddlSolicitors)
                .uploadDocument(uploadDocumentByApplicant)
                .claimant1PartyName("Mr. John Rambo")
                .defendant1PartyName("Mr. Sole Trader")
                .build();
            when(caseDetailsConverter.toGeneralApplicationCaseData(any())).thenReturn(caseData);
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            GeneralApplicationCaseData responseCaseData = objectMapper.convertValue(response.getData(), GeneralApplicationCaseData.class);

            assertThat(responseCaseData.getBusinessProcess().getStatus()).isEqualTo(BusinessProcessStatus.READY);
            assertThat(responseCaseData.getIsDocumentVisible()).isEqualTo(YesOrNo.NO);
        }

        @Test
        void shouldSetUpReadyBusinessProcessWhenApplicationIsNonUrgentAndAddedToRespCollection() {

            when(gaForLipService.isGaForLip(any(GeneralApplicationCaseData.class))).thenReturn(true);
            List<Element<UploadDocumentByType>> uploadDocumentByRespondent = new ArrayList<>();
            uploadDocumentByRespondent.add(element(UploadDocumentByType.builder()
                                                       .documentType("witness")
                                                       .additionalDocument(Document.builder()
                                                                               .documentFileName("witness_document.pdf")
                                                                               .documentUrl("http://dm-store:8080")
                                                                               .documentBinaryUrl("http://dm-store:8080/documents")
                                                                               .build()).build()));
            List<Element<GASolicitorDetailsGAspec>> gaRespSolicitors = new ArrayList<>();
            gaRespSolicitors.add(element(new GASolicitorDetailsGAspec()
                                             .setId(STRING_CONSTANT)
                                             .setEmail(DUMMY_EMAIL)
                                             .setOrganisationIdentifier("2")));
            GeneralApplicationCaseData caseData = GeneralApplicationCaseDataBuilder.builder()
                .atStateClaimDraft()
                .ccdCaseReference(1678356749555475L)
                .build().toBuilder()
                .respondent2SameLegalRepresentative(YesOrNo.NO)
                .isMultiParty(NO)
                .generalAppUrgencyRequirement(new GAUrgencyRequirement().setGeneralAppUrgency(NO))
                .parentClaimantIsApplicant(NO)
                .generalAppApplnSolicitor(new GASolicitorDetailsGAspec().setId("id").setForename("GAApplnSolicitor")
                                              .setEmail(DUMMY_EMAIL).setOrganisationIdentifier("1"))
                .uploadDocument(uploadDocumentByRespondent)
                .generalAppRespondentSolicitors(gaRespSolicitors)
                .claimant1PartyName("Mr. John Rambo")
                .defendant1PartyName("Mr. Sole Trader")
                .build();
            when(caseDetailsConverter.toGeneralApplicationCaseData(any())).thenReturn(caseData);
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            GeneralApplicationCaseData responseCaseData = objectMapper.convertValue(response.getData(), GeneralApplicationCaseData.class);

            assertThat(responseCaseData.getBusinessProcess().getStatus()).isEqualTo(BusinessProcessStatus.READY);
            assertThat(responseCaseData.getIsDocumentVisible()).isEqualTo(YesOrNo.YES);
            assertThat(responseCaseData.getGaAddlDocRespondentSol().size()).isEqualTo(1);
            verify(docUploadDashboardNotificationService).createDashboardNotification(any(GeneralApplicationCaseData.class), anyString(), anyString(), anyBoolean());
        }

        @Test
        void shouldSetUpReadyBusinessProcessWhenApplicationIsConsentOrderAndAddedToResp1Collection1v2() {

            List<Element<UploadDocumentByType>> uploadDocumentByApplicant = new ArrayList<>();
            uploadDocumentByApplicant.add(element(UploadDocumentByType.builder()
                                                      .documentType("witness")
                                                      .additionalDocument(Document.builder()
                                                                              .documentFileName("witness_document.pdf")
                                                                              .documentUrl("http://dm-store:8080")
                                                                              .documentBinaryUrl("http://dm-store:8080/documents")
                                                                              .build()).build()));

            List<Element<GASolicitorDetailsGAspec>> gaRespSolicitors = new ArrayList<>();
            gaRespSolicitors.add(element(new GASolicitorDetailsGAspec()
                                             .setId("id11")
                                             .setEmail(DUMMY_EMAIL)
                                             .setOrganisationIdentifier("2")));
            gaRespSolicitors.add(element(new GASolicitorDetailsGAspec()
                                             .setId(STRING_CONSTANT)
                                             .setEmail(DUMMY_EMAIL)
                                             .setOrganisationIdentifier("2")));
            gaRespSolicitors.add(element(new GASolicitorDetailsGAspec()
                                             .setId("id3")
                                             .setEmail(DUMMY_EMAIL)
                                             .setOrganisationIdentifier("2")));
            gaRespSolicitors.add(element(new GASolicitorDetailsGAspec()
                                             .setId("222")
                                             .setEmail(DUMMY_EMAIL)
                                             .setOrganisationIdentifier("3")));
            GeneralApplicationCaseData caseData = GeneralApplicationCaseDataBuilder.builder()
                .atStateClaimDraft()
                .ccdCaseReference(1678356749555475L)
                .build().toBuilder()
                .respondent2SameLegalRepresentative(YesOrNo.NO)
                .generalAppConsentOrder(YES)
                .generalAppRespondentSolicitors(gaRespSolicitors)
                .parentClaimantIsApplicant(NO)
                .isMultiParty(NO)
                .generalAppApplnSolicitor(new GASolicitorDetailsGAspec().setId("2").setForename("GAApplnSolicitor")
                                              .setEmail(DUMMY_EMAIL).setOrganisationIdentifier("1"))
                .uploadDocument(uploadDocumentByApplicant)
                .claimant1PartyName("Mr. John Rambo")
                .defendant1PartyName("Mr. Sole Trader")
                .build();
            when(caseDetailsConverter.toGeneralApplicationCaseData(any())).thenReturn(caseData);
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            GeneralApplicationCaseData responseCaseData = objectMapper.convertValue(response.getData(), GeneralApplicationCaseData.class);

            assertThat(responseCaseData.getBusinessProcess().getStatus()).isEqualTo(BusinessProcessStatus.READY);
            assertThat(responseCaseData.getIsDocumentVisible()).isEqualTo(YesOrNo.YES);
            assertThat(responseCaseData.getGaAddlDocRespondentSol().size()).isEqualTo(1);
        }

        @Test
        void shouldSetUpReadyBusinessProcessWhenApplicationIsConsentOrderAndAddedToResp2Collection1v2() {

            List<Element<UploadDocumentByType>> uploadDocumentByApplicant = new ArrayList<>();
            uploadDocumentByApplicant.add(element(UploadDocumentByType.builder()
                                                      .documentType("witness")
                                                      .additionalDocument(Document.builder()
                                                                              .documentFileName("witness_document.pdf")
                                                                              .documentUrl("http://dm-store:8080")
                                                                              .documentBinaryUrl("http://dm-store:8080/documents")
                                                                              .build()).build()));

            List<Element<GASolicitorDetailsGAspec>> gaRespSolicitors = new ArrayList<>();
            gaRespSolicitors.add(element(new GASolicitorDetailsGAspec()
                                             .setId("222")
                                             .setEmail(DUMMY_EMAIL)
                                             .setOrganisationIdentifier("2")));
            gaRespSolicitors.add(element(new GASolicitorDetailsGAspec()
                                             .setId("id1")
                                             .setEmail(DUMMY_EMAIL)
                                             .setOrganisationIdentifier("2")));
            gaRespSolicitors.add(element(new GASolicitorDetailsGAspec()
                                             .setId("id3")
                                             .setEmail(DUMMY_EMAIL)
                                             .setOrganisationIdentifier("2")));
            gaRespSolicitors.add(element(new GASolicitorDetailsGAspec()
                                             .setId(STRING_CONSTANT)
                                             .setEmail(DUMMY_EMAIL)
                                             .setOrganisationIdentifier("3")));
            gaRespSolicitors.add(element(new GASolicitorDetailsGAspec()
                                             .setId("id33")
                                             .setEmail(DUMMY_EMAIL)
                                             .setOrganisationIdentifier("3")));
            GeneralApplicationCaseData caseData = GeneralApplicationCaseDataBuilder.builder()
                .atStateClaimDraft()
                .ccdCaseReference(1678356749555475L)
                .build().toBuilder()
                .respondent2SameLegalRepresentative(YesOrNo.NO)
                .generalAppConsentOrder(YES)
                .generalAppRespondentSolicitors(gaRespSolicitors)
                .parentClaimantIsApplicant(NO)
                .isMultiParty(YES)
                .generalAppApplnSolicitor(new GASolicitorDetailsGAspec().setId("2").setForename("GAApplnSolicitor")
                                              .setEmail(DUMMY_EMAIL).setOrganisationIdentifier("1"))
                .uploadDocument(uploadDocumentByApplicant)
                .claimant1PartyName("Mr. John Rambo")
                .defendant1PartyName("Mr. Sole Trader")
                .build();
            when(caseDetailsConverter.toGeneralApplicationCaseData(any())).thenReturn(caseData);
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            GeneralApplicationCaseData responseCaseData = objectMapper.convertValue(response.getData(), GeneralApplicationCaseData.class);

            assertThat(responseCaseData.getBusinessProcess().getStatus()).isEqualTo(BusinessProcessStatus.READY);
            assertThat(responseCaseData.getIsDocumentVisible()).isEqualTo(YesOrNo.YES);
            assertThat(responseCaseData.getGaAddlDocRespondentSolTwo().size()).isEqualTo(1);
        }

        @Test
        void shouldSetUpReadyBusinessProcessWhenApplicationIsConsentOrderAndAddedToResp2Collection1v2MultipleCollection() {

            List<Element<UploadDocumentByType>> uploadDocumentByApplicant = new ArrayList<>();
            uploadDocumentByApplicant.add(element(UploadDocumentByType.builder()
                                                      .documentType("witness")
                                                      .additionalDocument(Document.builder()
                                                                              .documentFileName("witness_document.pdf")
                                                                              .documentUrl("http://dm-store:8080")
                                                                              .documentBinaryUrl("http://dm-store:8080/documents")
                                                                              .build()).build()));
            List<Element<CaseDocument>> documentsCollection = new ArrayList<>();
            documentsCollection.add(element(CaseDocument.builder().createdBy("civil")
                                                .documentLink(Document.builder()
                                                                  .documentFileName("witness_document.pdf")
                                                                  .documentUrl("http://dm-store:8080")
                                                                  .documentBinaryUrl("http://dm-store:8080/documents")
                                                                  .build())
                                                .documentName("witness_document.docx")
                                                .createdDatetime(LocalDateTime.now()).build()));

            List<Element<GASolicitorDetailsGAspec>> gaRespSolicitors = new ArrayList<>();
            gaRespSolicitors.add(element(new GASolicitorDetailsGAspec()
                                             .setId("222")
                                             .setEmail(DUMMY_EMAIL)
                                             .setOrganisationIdentifier("2")));
            gaRespSolicitors.add(element(new GASolicitorDetailsGAspec()
                                             .setId(STRING_CONSTANT)
                                             .setEmail(DUMMY_EMAIL)
                                             .setOrganisationIdentifier("3")));
            GeneralApplicationCaseData caseData = GeneralApplicationCaseDataBuilder.builder()
                .atStateClaimDraft()
                .ccdCaseReference(1678356749555475L)
                .build().toBuilder()
                .respondent2SameLegalRepresentative(YesOrNo.NO)
                .generalAppConsentOrder(YES)
                .generalAppRespondentSolicitors(gaRespSolicitors)
                .parentClaimantIsApplicant(NO)
                .gaAddlDocStaff(documentsCollection)
                .gaAddlDoc(documentsCollection)
                .gaAddlDocRespondentSolTwo(documentsCollection)
                .isMultiParty(YES)
                .generalAppApplnSolicitor(new GASolicitorDetailsGAspec().setId("2").setForename("GAApplnSolicitor")
                                              .setEmail(DUMMY_EMAIL).setOrganisationIdentifier("1"))
                .uploadDocument(uploadDocumentByApplicant)
                .claimant1PartyName("Mr. John Rambo")
                .defendant1PartyName("Mr. Sole Trader")
                .build();
            when(caseDetailsConverter.toGeneralApplicationCaseData(any())).thenReturn(caseData);
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            GeneralApplicationCaseData responseCaseData = objectMapper.convertValue(response.getData(), GeneralApplicationCaseData.class);

            assertThat(responseCaseData.getBusinessProcess().getStatus()).isEqualTo(BusinessProcessStatus.READY);
            assertThat(responseCaseData.getIsDocumentVisible()).isEqualTo(YesOrNo.YES);
            assertThat(responseCaseData.getGaAddlDocRespondentSolTwo().size()).isEqualTo(2);
        }

        @Test
        void shouldSetUpReadyBusinessProcessWhenApplicationIsNotConsentOrder() {

            List<Element<UploadDocumentByType>> uploadDocumentByApplicant = new ArrayList<>();
            uploadDocumentByApplicant.add(element(UploadDocumentByType.builder()
                                                      .documentType("witness")
                                                      .additionalDocument(Document.builder()
                                                                              .documentFileName("witness_document.pdf")
                                                                              .documentUrl("http://dm-store:8080")
                                                                              .documentBinaryUrl("http://dm-store:8080/documents")
                                                                              .build()).build()));
            GeneralApplicationCaseData caseData = GeneralApplicationCaseDataBuilder.builder()
                .atStateClaimDraft()
                .ccdCaseReference(1678356749555475L)
                .build().toBuilder()
                .respondent2SameLegalRepresentative(YesOrNo.NO)
                .parentClaimantIsApplicant(YES)
                .generalAppApplnSolicitor(new GASolicitorDetailsGAspec().setId(STRING_CONSTANT).setForename("GAApplnSolicitor")
                                              .setEmail(DUMMY_EMAIL).setOrganisationIdentifier("1"))
                .uploadDocument(uploadDocumentByApplicant)
                .claimant1PartyName("Mr. John Rambo")
                .defendant1PartyName("Mr. Sole Trader")
                .build();
            when(caseDetailsConverter.toGeneralApplicationCaseData(any())).thenReturn(caseData);
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            GeneralApplicationCaseData responseCaseData = objectMapper.convertValue(response.getData(), GeneralApplicationCaseData.class);

            assertThat(responseCaseData.getBusinessProcess().getStatus()).isEqualTo(BusinessProcessStatus.READY);
            assertThat(responseCaseData.getIsDocumentVisible()).isEqualTo(YesOrNo.NO);
        }

        @Test
        void shouldPutBundleInBundleCollection() {

            List<Element<UploadDocumentByType>> uploadDocumentByApplicant = new ArrayList<>();
            uploadDocumentByApplicant.add(element(UploadDocumentByType.builder()
                                                      .documentType("bundle")
                                                      .additionalDocument(Document.builder()
                                                                              .documentFileName("witness_document.pdf")
                                                                              .documentUrl("http://dm-store:8080")
                                                                              .documentBinaryUrl("http://dm-store:8080/documents")
                                                                              .build()).build()));

            List<Element<GASolicitorDetailsGAspec>> gaRespSolicitors = new ArrayList<>();
            gaRespSolicitors.add(element(new GASolicitorDetailsGAspec()
                                             .setId("222")
                                             .setEmail(DUMMY_EMAIL)
                                             .setOrganisationIdentifier("2")));
            gaRespSolicitors.add(element(new GASolicitorDetailsGAspec()
                                             .setId(STRING_CONSTANT)
                                             .setEmail(DUMMY_EMAIL)
                                             .setOrganisationIdentifier("3")));
            GeneralApplicationCaseData caseData = GeneralApplicationCaseDataBuilder.builder()
                .atStateClaimDraft()
                .ccdCaseReference(1678356749555475L)
                .build().toBuilder()
                .respondent2SameLegalRepresentative(YesOrNo.NO)
                .generalAppConsentOrder(YES)
                .generalAppRespondentSolicitors(gaRespSolicitors)
                .parentClaimantIsApplicant(NO)
                .isMultiParty(YES)
                .generalAppApplnSolicitor(new GASolicitorDetailsGAspec().setId("2").setForename("GAApplnSolicitor")
                                              .setEmail(DUMMY_EMAIL).setOrganisationIdentifier("1"))
                .uploadDocument(uploadDocumentByApplicant)
                .claimant1PartyName("Mr. John Rambo")
                .defendant1PartyName("Mr. Sole Trader")
                .build();
            when(caseDetailsConverter.toGeneralApplicationCaseData(any())).thenReturn(caseData);
            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
            GeneralApplicationCaseData responseCaseData = objectMapper.convertValue(response.getData(), GeneralApplicationCaseData.class);

            assertThat(responseCaseData.getBusinessProcess().getStatus()).isEqualTo(BusinessProcessStatus.READY);
            assertThat(responseCaseData.getIsDocumentVisible()).isEqualTo(YesOrNo.YES);
            assertThat(responseCaseData.getGaAddlDocRespondentSolTwo()).isNull();
            assertThat(responseCaseData.getGaAddlDocBundle().size()).isEqualTo(1);
        }
    }

    @Nested
    class SubmittedCallback {
        @Test
        void shouldReturnExpectedSubmittedCallbackResponse_whenInvoked() {
            String body = "<br/> <br/>";
            String header = "### File has been uploaded successfully.";
            GeneralApplicationCaseData caseData = GeneralApplicationCaseDataBuilder.builder()
                .atStateClaimDraft()
                .ccdCaseReference(1678356749555475L)
                .build().toBuilder()
                .respondent2SameLegalRepresentative(YesOrNo.NO)
                .claimant1PartyName("Mr. John Rambo")
                .defendant1PartyName("Mr. Sole Trader")
                .build();
            CallbackParams params = callbackParamsOf(caseData, SUBMITTED);
            SubmittedCallbackResponse response = (SubmittedCallbackResponse) handler.handle(params);
            assertThat(response).usingRecursiveComparison().isEqualTo(
                SubmittedCallbackResponse.builder()
                    .confirmationHeader(format(header))
                    .confirmationBody(format(body))
                    .build());
        }
    }
}
