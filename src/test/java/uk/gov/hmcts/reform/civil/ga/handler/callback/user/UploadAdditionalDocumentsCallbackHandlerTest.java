package uk.gov.hmcts.reform.civil.ga.handler.callback.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.enums.BusinessProcessStatus;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.ga.handler.GeneralApplicationBaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.ga.model.GeneralApplicationCaseData;
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
import uk.gov.hmcts.reform.civil.testutils.ObjectMapperFactory;
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

@ExtendWith(MockitoExtension.class)
class UploadAdditionalDocumentsCallbackHandlerTest extends GeneralApplicationBaseCallbackHandlerTest {

    @Spy
    private ObjectMapper objectMapper = ObjectMapperFactory.instance();

    @Mock
    private CaseDetailsConverter caseDetailsConverter;

    @InjectMocks
    private UploadAdditionalDocumentsCallbackHandler handler;

    @Mock
    private IdamClient idamClient;

    @Mock
    private AssignCategoryId assignCategoryId;

    @Mock
    private DocUploadDashboardNotificationService docUploadDashboardNotificationService;

    @Mock
    private GaForLipService gaForLipService;

    private static final String DUMMY_EMAIL = "test@gmail.com";

    @Test
    void handleEventsReturnsTheExpectedCallbackEvent() {
        assertThat(handler.handledEvents()).contains(UPLOAD_ADDL_DOCUMENTS);
    }

    @Nested
    class AboutToSubmit {

        @BeforeEach
        public void setUp() throws IOException {
            when(idamClient.getUserInfo(anyString())).thenReturn(UserInfo.builder()
                                                                     .sub(DUMMY_EMAIL)
                                                                     .uid(STRING_CONSTANT)
                                                                     .build());
        }

        @Test
        void shouldSetUpReadyBusinessProcessWhenJudgeUncloaked() {
            when(gaForLipService.isGaForLip(any(GeneralApplicationCaseData.class))).thenReturn(false);

            List<Element<UploadDocumentByType>> uploadDocumentByApplicant = new ArrayList<>();
            uploadDocumentByApplicant.add(element(new UploadDocumentByType()
                                                      .setDocumentType("Witness")
                                                      .setAdditionalDocument(new Document()
                                                                              .setDocumentFileName("witness_document.pdf")
                                                                              .setDocumentUrl("http://dm-store:8080")
                                                                              .setDocumentBinaryUrl("http://dm-store:8080/documents"))));
            GeneralApplicationCaseData caseData = GeneralApplicationCaseDataBuilder.builder()
                .atStateClaimDraft()
                .ccdCaseReference(1678356749555475L)
                .build().copy()
                .respondent2SameLegalRepresentative(YesOrNo.NO)
                .applicationIsUncloakedOnce(YES)
                .parentClaimantIsApplicant(YES)
                .generalAppApplnSolicitor(GASolicitorDetailsGAspec.builder().id(STRING_CONSTANT).forename("GAApplnSolicitor")
                                              .email(DUMMY_EMAIL).organisationIdentifier("1").build())
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
            when(gaForLipService.isGaForLip(any(GeneralApplicationCaseData.class))).thenReturn(false);

            List<Element<UploadDocumentByType>> uploadDocumentByApplicant = new ArrayList<>();
            uploadDocumentByApplicant.add(element(new UploadDocumentByType()
                                                      .setDocumentType("witness bundle")
                                                      .setAdditionalDocument(new Document()
                                                                              .setDocumentFileName("witness_document.pdf")
                                                                              .setDocumentUrl("http://dm-store:8080")
                                                                              .setDocumentBinaryUrl("http://dm-store:8080/documents"))));
            GeneralApplicationCaseData caseData = GeneralApplicationCaseDataBuilder.builder()
                .atStateClaimDraft()
                .ccdCaseReference(1678356749555475L)
                .build().copy()
                .respondent2SameLegalRepresentative(YesOrNo.NO)
                .applicationIsCloaked(NO)
                .parentClaimantIsApplicant(YES)
                .generalAppApplnSolicitor(GASolicitorDetailsGAspec.builder().id(STRING_CONSTANT).forename("GAApplnSolicitor")
                                              .email(DUMMY_EMAIL).organisationIdentifier("1").build())
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
            when(gaForLipService.isGaForLip(any(GeneralApplicationCaseData.class))).thenReturn(false);

            List<Element<UploadDocumentByType>> uploadDocumentByApplicant = new ArrayList<>();
            uploadDocumentByApplicant.add(element(new UploadDocumentByType()
                                                      .setDocumentType("witness bundle")
                                                      .setAdditionalDocument(new Document()
                                                                              .setDocumentFileName("witness_document.pdf")
                                                                              .setDocumentUrl("http://dm-store:8080")
                                                                              .setDocumentBinaryUrl("http://dm-store:8080/documents"))));
            GeneralApplicationCaseData caseData = GeneralApplicationCaseDataBuilder.builder()
                .atStateClaimDraft()
                .ccdCaseReference(1678356749555475L)
                .build().copy()
                .respondent2SameLegalRepresentative(YesOrNo.NO)
                .generalAppInformOtherParty(GAInformOtherParty.builder().isWithNotice(YES).build())
                .parentClaimantIsApplicant(YES)
                .generalAppApplnSolicitor(GASolicitorDetailsGAspec.builder().id(STRING_CONSTANT).forename("GAApplnSolicitor")
                                              .email(DUMMY_EMAIL).organisationIdentifier("1").build())
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
            uploadDocumentByApplicant.add(element(new UploadDocumentByType()
                                                      .setDocumentType("witness")
                                                      .setAdditionalDocument(new Document()
                                                                              .setDocumentFileName("witness_document.pdf")
                                                                              .setDocumentUrl("http://dm-store:8080")
                                                                              .setDocumentBinaryUrl("http://dm-store:8080/documents"))));
            List<Element<GASolicitorDetailsGAspec>> gaApplAddlSolicitors = new ArrayList<>();
            gaApplAddlSolicitors.add(element(GASolicitorDetailsGAspec.builder()
                                                 .id("id1")
                                                 .email(DUMMY_EMAIL)
                                                 .organisationIdentifier("1").build()));
            GeneralApplicationCaseData caseData = GeneralApplicationCaseDataBuilder.builder()
                .atStateClaimDraft()
                .ccdCaseReference(1678356749555475L)
                .build().copy()
                .respondent2SameLegalRepresentative(YesOrNo.NO)
                .generalAppUrgencyRequirement(GAUrgencyRequirement.builder().generalAppUrgency(YES).build())
                .parentClaimantIsApplicant(YES)
                .generalAppApplnSolicitor(GASolicitorDetailsGAspec.builder().id(STRING_CONSTANT).forename("GAApplnSolicitor")
                                              .email(DUMMY_EMAIL).organisationIdentifier("1").build())
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
            when(gaForLipService.isGaForLip(any(GeneralApplicationCaseData.class))).thenReturn(false);

            List<Element<UploadDocumentByType>> uploadDocumentByApplicant = new ArrayList<>();
            uploadDocumentByApplicant.add(element(new UploadDocumentByType()
                                                      .setDocumentType("witness")
                                                      .setAdditionalDocument(new Document()
                                                                              .setDocumentFileName("witness_document.pdf")
                                                                              .setDocumentUrl("http://dm-store:8080")
                                                                              .setDocumentBinaryUrl("http://dm-store:8080/documents"))));
            List<Element<GASolicitorDetailsGAspec>> gaApplAddlSolicitors = new ArrayList<>();
            gaApplAddlSolicitors.add(element(GASolicitorDetailsGAspec.builder()
                                                 .id(STRING_CONSTANT)
                                                 .email(DUMMY_EMAIL)
                                                 .organisationIdentifier("1").build()));
            GeneralApplicationCaseData caseData = GeneralApplicationCaseDataBuilder.builder()
                .atStateClaimDraft()
                .ccdCaseReference(1678356749555475L)
                .build().copy()
                .respondent2SameLegalRepresentative(YesOrNo.NO)
                .generalAppUrgencyRequirement(GAUrgencyRequirement.builder().generalAppUrgency(YES).build())
                .parentClaimantIsApplicant(YES)
                .generalAppApplnSolicitor(GASolicitorDetailsGAspec.builder().id("id1").forename("GAApplnSolicitor")
                                              .email(DUMMY_EMAIL).organisationIdentifier("1").build())
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
            uploadDocumentByRespondent.add(element(new UploadDocumentByType()
                                                       .setDocumentType("witness")
                                                       .setAdditionalDocument(new Document()
                                                                               .setDocumentFileName("witness_document.pdf")
                                                                               .setDocumentUrl("http://dm-store:8080")
                                                                               .setDocumentBinaryUrl("http://dm-store:8080/documents"))));
            List<Element<GASolicitorDetailsGAspec>> gaRespSolicitors = new ArrayList<>();
            gaRespSolicitors.add(element(GASolicitorDetailsGAspec.builder()
                                             .id(STRING_CONSTANT)
                                             .email(DUMMY_EMAIL)
                                             .organisationIdentifier("2").build()));
            GeneralApplicationCaseData caseData = GeneralApplicationCaseDataBuilder.builder()
                .atStateClaimDraft()
                .ccdCaseReference(1678356749555475L)
                .build().copy()
                .respondent2SameLegalRepresentative(YesOrNo.NO)
                .isMultiParty(NO)
                .generalAppUrgencyRequirement(GAUrgencyRequirement.builder().generalAppUrgency(NO).build())
                .parentClaimantIsApplicant(NO)
                .generalAppApplnSolicitor(GASolicitorDetailsGAspec.builder().id("id").forename("GAApplnSolicitor")
                                              .email(DUMMY_EMAIL).organisationIdentifier("1").build())
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
            when(gaForLipService.isGaForLip(any(GeneralApplicationCaseData.class))).thenReturn(false);

            List<Element<UploadDocumentByType>> uploadDocumentByApplicant = new ArrayList<>();
            uploadDocumentByApplicant.add(element(new UploadDocumentByType()
                                                      .setDocumentType("witness")
                                                      .setAdditionalDocument(new Document()
                                                                              .setDocumentFileName("witness_document.pdf")
                                                                              .setDocumentUrl("http://dm-store:8080")
                                                                              .setDocumentBinaryUrl("http://dm-store:8080/documents"))));

            List<Element<GASolicitorDetailsGAspec>> gaRespSolicitors = new ArrayList<>();
            gaRespSolicitors.add(element(GASolicitorDetailsGAspec.builder()
                                             .id("id11")
                                             .email(DUMMY_EMAIL)
                                             .organisationIdentifier("2").build()));
            gaRespSolicitors.add(element(GASolicitorDetailsGAspec.builder()
                                             .id(STRING_CONSTANT)
                                             .email(DUMMY_EMAIL)
                                             .organisationIdentifier("2").build()));
            gaRespSolicitors.add(element(GASolicitorDetailsGAspec.builder()
                                             .id("id3")
                                             .email(DUMMY_EMAIL)
                                             .organisationIdentifier("2").build()));
            gaRespSolicitors.add(element(GASolicitorDetailsGAspec.builder()
                                             .id("222")
                                             .email(DUMMY_EMAIL)
                                             .organisationIdentifier("3").build()));
            GeneralApplicationCaseData caseData = GeneralApplicationCaseDataBuilder.builder()
                .atStateClaimDraft()
                .ccdCaseReference(1678356749555475L)
                .build().copy()
                .respondent2SameLegalRepresentative(YesOrNo.NO)
                .generalAppConsentOrder(YES)
                .generalAppRespondentSolicitors(gaRespSolicitors)
                .parentClaimantIsApplicant(NO)
                .isMultiParty(NO)
                .generalAppApplnSolicitor(GASolicitorDetailsGAspec.builder().id("2").forename("GAApplnSolicitor")
                                              .email(DUMMY_EMAIL).organisationIdentifier("1").build())
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
            when(gaForLipService.isGaForLip(any(GeneralApplicationCaseData.class))).thenReturn(false);

            List<Element<UploadDocumentByType>> uploadDocumentByApplicant = new ArrayList<>();
            uploadDocumentByApplicant.add(element(new UploadDocumentByType()
                                                      .setDocumentType("witness")
                                                      .setAdditionalDocument(new Document()
                                                                              .setDocumentFileName("witness_document.pdf")
                                                                              .setDocumentUrl("http://dm-store:8080")
                                                                              .setDocumentBinaryUrl("http://dm-store:8080/documents"))));

            List<Element<GASolicitorDetailsGAspec>> gaRespSolicitors = new ArrayList<>();
            gaRespSolicitors.add(element(GASolicitorDetailsGAspec.builder()
                                             .id("222")
                                             .email(DUMMY_EMAIL)
                                             .organisationIdentifier("2").build()));
            gaRespSolicitors.add(element(GASolicitorDetailsGAspec.builder()
                                             .id("id1")
                                             .email(DUMMY_EMAIL)
                                             .organisationIdentifier("2").build()));
            gaRespSolicitors.add(element(GASolicitorDetailsGAspec.builder()
                                             .id("id3")
                                             .email(DUMMY_EMAIL)
                                             .organisationIdentifier("2").build()));
            gaRespSolicitors.add(element(GASolicitorDetailsGAspec.builder()
                                             .id(STRING_CONSTANT)
                                             .email(DUMMY_EMAIL)
                                             .organisationIdentifier("3").build()));
            gaRespSolicitors.add(element(GASolicitorDetailsGAspec.builder()
                                             .id("id33")
                                             .email(DUMMY_EMAIL)
                                             .organisationIdentifier("3").build()));
            GeneralApplicationCaseData caseData = GeneralApplicationCaseDataBuilder.builder()
                .atStateClaimDraft()
                .ccdCaseReference(1678356749555475L)
                .build().copy()
                .respondent2SameLegalRepresentative(YesOrNo.NO)
                .generalAppConsentOrder(YES)
                .generalAppRespondentSolicitors(gaRespSolicitors)
                .parentClaimantIsApplicant(NO)
                .isMultiParty(YES)
                .generalAppApplnSolicitor(GASolicitorDetailsGAspec.builder().id("2").forename("GAApplnSolicitor")
                                              .email(DUMMY_EMAIL).organisationIdentifier("1").build())
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
            when(gaForLipService.isGaForLip(any(GeneralApplicationCaseData.class))).thenReturn(false);

            List<Element<UploadDocumentByType>> uploadDocumentByApplicant = new ArrayList<>();
            uploadDocumentByApplicant.add(element(new UploadDocumentByType()
                                                      .setDocumentType("witness")
                                                      .setAdditionalDocument(new Document()
                                                                              .setDocumentFileName("witness_document.pdf")
                                                                              .setDocumentUrl("http://dm-store:8080")
                                                                              .setDocumentBinaryUrl("http://dm-store:8080/documents"))));
            List<Element<CaseDocument>> documentsCollection = new ArrayList<>();
            documentsCollection.add(element(new CaseDocument().setCreatedBy("civil")
                                                .setDocumentLink(new Document()
                                                                  .setDocumentFileName("witness_document.pdf")
                                                                  .setDocumentUrl("http://dm-store:8080")
                                                                  .setDocumentBinaryUrl("http://dm-store:8080/documents"))
                                                .setDocumentName("witness_document.docx")
                                                .setCreatedDatetime(LocalDateTime.now())));

            List<Element<GASolicitorDetailsGAspec>> gaRespSolicitors = new ArrayList<>();
            gaRespSolicitors.add(element(GASolicitorDetailsGAspec.builder()
                                             .id("222")
                                             .email(DUMMY_EMAIL)
                                             .organisationIdentifier("2").build()));
            gaRespSolicitors.add(element(GASolicitorDetailsGAspec.builder()
                                             .id(STRING_CONSTANT)
                                             .email(DUMMY_EMAIL)
                                             .organisationIdentifier("3").build()));
            GeneralApplicationCaseData caseData = GeneralApplicationCaseDataBuilder.builder()
                .atStateClaimDraft()
                .ccdCaseReference(1678356749555475L)
                .build().copy()
                .respondent2SameLegalRepresentative(YesOrNo.NO)
                .generalAppConsentOrder(YES)
                .generalAppRespondentSolicitors(gaRespSolicitors)
                .parentClaimantIsApplicant(NO)
                .gaAddlDocStaff(documentsCollection)
                .gaAddlDoc(documentsCollection)
                .gaAddlDocRespondentSolTwo(documentsCollection)
                .isMultiParty(YES)
                .generalAppApplnSolicitor(GASolicitorDetailsGAspec.builder().id("2").forename("GAApplnSolicitor")
                                              .email(DUMMY_EMAIL).organisationIdentifier("1").build())
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
            when(gaForLipService.isGaForLip(any(GeneralApplicationCaseData.class))).thenReturn(false);

            List<Element<UploadDocumentByType>> uploadDocumentByApplicant = new ArrayList<>();
            uploadDocumentByApplicant.add(element(new UploadDocumentByType()
                                                      .setDocumentType("witness")
                                                      .setAdditionalDocument(new Document()
                                                                              .setDocumentFileName("witness_document.pdf")
                                                                              .setDocumentUrl("http://dm-store:8080")
                                                                              .setDocumentBinaryUrl("http://dm-store:8080/documents"))));
            GeneralApplicationCaseData caseData = GeneralApplicationCaseDataBuilder.builder()
                .atStateClaimDraft()
                .ccdCaseReference(1678356749555475L)
                .build().copy()
                .respondent2SameLegalRepresentative(YesOrNo.NO)
                .parentClaimantIsApplicant(YES)
                .generalAppApplnSolicitor(GASolicitorDetailsGAspec.builder().id(STRING_CONSTANT).forename("GAApplnSolicitor")
                                              .email(DUMMY_EMAIL).organisationIdentifier("1").build())
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
            when(gaForLipService.isGaForLip(any(GeneralApplicationCaseData.class))).thenReturn(false);

            List<Element<UploadDocumentByType>> uploadDocumentByApplicant = new ArrayList<>();
            uploadDocumentByApplicant.add(element(new UploadDocumentByType()
                                                      .setDocumentType("bundle")
                                                      .setAdditionalDocument(new Document()
                                                                              .setDocumentFileName("witness_document.pdf")
                                                                              .setDocumentUrl("http://dm-store:8080")
                                                                              .setDocumentBinaryUrl("http://dm-store:8080/documents"))));

            List<Element<GASolicitorDetailsGAspec>> gaRespSolicitors = new ArrayList<>();
            gaRespSolicitors.add(element(GASolicitorDetailsGAspec.builder()
                                             .id("222")
                                             .email(DUMMY_EMAIL)
                                             .organisationIdentifier("2").build()));
            gaRespSolicitors.add(element(GASolicitorDetailsGAspec.builder()
                                             .id(STRING_CONSTANT)
                                             .email(DUMMY_EMAIL)
                                             .organisationIdentifier("3").build()));
            GeneralApplicationCaseData caseData = GeneralApplicationCaseDataBuilder.builder()
                .atStateClaimDraft()
                .ccdCaseReference(1678356749555475L)
                .build().copy()
                .respondent2SameLegalRepresentative(YesOrNo.NO)
                .generalAppConsentOrder(YES)
                .generalAppRespondentSolicitors(gaRespSolicitors)
                .parentClaimantIsApplicant(NO)
                .isMultiParty(YES)
                .generalAppApplnSolicitor(GASolicitorDetailsGAspec.builder().id("2").forename("GAApplnSolicitor")
                                              .email(DUMMY_EMAIL).organisationIdentifier("1").build())
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
                .build().copy()
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
