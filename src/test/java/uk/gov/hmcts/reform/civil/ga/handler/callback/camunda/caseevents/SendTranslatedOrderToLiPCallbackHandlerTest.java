package uk.gov.hmcts.reform.civil.ga.handler.callback.camunda.caseevents;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.ga.model.GeneralApplicationCaseData;
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.model.citizenui.RespondentLiPResponse;
import uk.gov.hmcts.reform.civil.model.citizenui.TranslatedDocument;
import uk.gov.hmcts.reform.civil.model.citizenui.TranslatedDocumentType;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.documentmanagement.model.CaseDocument;
import uk.gov.hmcts.reform.civil.documentmanagement.model.Document;
import uk.gov.hmcts.reform.civil.documentmanagement.model.DocumentType;
import uk.gov.hmcts.reform.civil.sampledata.CallbackParamsBuilder;
import uk.gov.hmcts.reform.civil.sampledata.GeneralApplicationCaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.CoreCaseDataService;
import uk.gov.hmcts.reform.civil.ga.service.SendFinalOrderPrintService;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.SEND_TRANSLATED_ORDER_TO_LIP_APPLICANT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.SEND_TRANSLATED_ORDER_TO_LIP_RESPONDENT;

@ExtendWith(MockitoExtension.class)
public class SendTranslatedOrderToLiPCallbackHandlerTest extends BaseCallbackHandlerTest {

    @Mock
    private SendFinalOrderPrintService sendOrderPrintService;
    @Mock
    private CoreCaseDataService coreCaseDataService;
    @Mock
    private CaseDetailsConverter caseDetailsConverter;
    @InjectMocks
    private SendTranslatedOrderToLiPCallbackHandler handler;
    private static final String TASK_ID = "default";

    @Test
    void handleEventsReturnsTheExpectedCallbackEventS() {
        assertThat(handler.handledEvents()).contains(SEND_TRANSLATED_ORDER_TO_LIP_APPLICANT, SEND_TRANSLATED_ORDER_TO_LIP_RESPONDENT);
    }

    @Test
    void shouldReturnCorrectTaskId() {
        assertThat(handler.camundaActivityId(new CallbackParams())).isEqualTo(TASK_ID);
    }

    @Nested
    class AboutToSubmitCallback {

        @Test
        void shouldSendTranslatedOrderLetterToLipApplicantWhenInvoked() {
            Document printDocument = new Document();
            CaseDetails parentCaseDetails = CaseDetails.builder().build();
            GeneralApplicationCaseData parentCaseData = new GeneralApplicationCaseData()
                .claimantBilingualLanguagePreference("WELSH").build();
            when(coreCaseDataService.getCase(anyLong())).thenReturn(parentCaseDetails);
            when(caseDetailsConverter.toGeneralApplicationCaseData(parentCaseDetails)).thenReturn(parentCaseData);
            GeneralApplicationCaseData caseData = GeneralApplicationCaseDataBuilder.builder().atStateClaimDraft().withNoticeCaseData();
            caseData = caseData.copy()
                .parentCaseReference(caseData.getCcdCaseReference().toString())
                .originalDocumentsBulkPrint(List.of(Element.<CaseDocument>builder()
                                                        .value(new CaseDocument()
                                                                   .setDocumentLink(printDocument)
                                                                   .setDocumentType(DocumentType.GENERAL_ORDER)).build()))
                .translatedDocumentsBulkPrint(List.of(Element.<TranslatedDocument>builder()
                                                 .value(new TranslatedDocument()
                                                            .setFile(printDocument)
                                                            .setDocumentType(TranslatedDocumentType.GENERAL_ORDER)).build()))
                .applicantBilingualLanguagePreference(YesOrNo.YES)
                .isGaApplicantLip(YesOrNo.YES)
                .parentClaimantIsApplicant(YesOrNo.YES)
                .build();

            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).request(
                CallbackRequest.builder().eventId(SEND_TRANSLATED_ORDER_TO_LIP_APPLICANT.name())
                    .build()
            ).build();
            handler.printServiceEnabled = true;

            handler.handle(params);

            verify(sendOrderPrintService, times(1))
                .sendJudgeTranslatedOrderToPrintForLIP(any(), eq(null), eq(printDocument), eq(caseData), eq(SEND_TRANSLATED_ORDER_TO_LIP_APPLICANT));
        }

        @Test
        void shouldSendEnglishOrderLetterToLipApplicantIfEnglishPreference() {
            Document printDocument = new Document();
            CaseDetails parentCaseDetails = CaseDetails.builder().build();
            GeneralApplicationCaseData parentCaseData = new GeneralApplicationCaseData().build();
            when(coreCaseDataService.getCase(anyLong())).thenReturn(parentCaseDetails);
            when(caseDetailsConverter.toGeneralApplicationCaseData(parentCaseDetails)).thenReturn(parentCaseData);
            GeneralApplicationCaseData caseData = GeneralApplicationCaseDataBuilder.builder().atStateClaimDraft().withNoticeCaseData();
            caseData = caseData.copy()
                .parentCaseReference(caseData.getCcdCaseReference().toString())
                .originalDocumentsBulkPrint(List.of(Element.<CaseDocument>builder()
                                                          .value(new CaseDocument()
                                                                     .setDocumentLink(printDocument)
                                                                     .setDocumentType(DocumentType.GENERAL_ORDER)).build()))
                .translatedDocumentsBulkPrint(List.of(Element.<TranslatedDocument>builder()
                                                          .value(new TranslatedDocument()
                                                                     .setFile(printDocument)
                                                                     .setDocumentType(TranslatedDocumentType.GENERAL_ORDER)).build()))
                .isGaApplicantLip(YesOrNo.YES)
                .parentClaimantIsApplicant(YesOrNo.YES)
                .build();

            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).request(
                CallbackRequest.builder().eventId(SEND_TRANSLATED_ORDER_TO_LIP_APPLICANT.name())
                    .build()
            ).build();
            handler.printServiceEnabled = true;

            handler.handle(params);

            verify(sendOrderPrintService, times(1))
                .sendJudgeTranslatedOrderToPrintForLIP(any(), eq(printDocument), eq(null), eq(caseData), eq(SEND_TRANSLATED_ORDER_TO_LIP_APPLICANT));
        }

        @Test
        void shouldNotSendTranslatedOrderLetterToLipApplicantIfNotOrderDocument() {
            GeneralApplicationCaseData caseData = GeneralApplicationCaseDataBuilder.builder().atStateClaimDraft().withNoticeCaseData();
            caseData = caseData.copy()
                .parentCaseReference(caseData.getCcdCaseReference().toString())
                .translatedDocumentsBulkPrint(List.of(Element.<TranslatedDocument>builder()
                                                          .value(new TranslatedDocument()
                                                                     .setDocumentType(TranslatedDocumentType.APPLICATION_SUMMARY_DOCUMENT)).build()))
                .applicantBilingualLanguagePreference(YesOrNo.YES)
                .isGaApplicantLip(YesOrNo.YES)
                .parentClaimantIsApplicant(YesOrNo.YES)
                .build();

            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).request(
                CallbackRequest.builder().eventId(SEND_TRANSLATED_ORDER_TO_LIP_APPLICANT.name())
                    .build()
            ).build();
            handler.printServiceEnabled = true;

            handler.handle(params);

            verifyNoInteractions(sendOrderPrintService);
        }

        @Test
        void shouldSendTranslatedOrderLetterToLipRespondentWhenInvoked() {
            Document printDocument = new Document();
            CaseDetails parentCaseDetails = CaseDetails.builder().build();
            GeneralApplicationCaseData parentCaseData = new GeneralApplicationCaseData()
                .respondent1LiPResponse(new RespondentLiPResponse().setRespondent1ResponseLanguage("BOTH")).build();
            when(coreCaseDataService.getCase(anyLong())).thenReturn(parentCaseDetails);
            when(caseDetailsConverter.toGeneralApplicationCaseData(parentCaseDetails)).thenReturn(parentCaseData);
            GeneralApplicationCaseData caseData = GeneralApplicationCaseDataBuilder.builder().atStateClaimDraft().withNoticeCaseData();
            caseData = caseData.copy()
                .parentCaseReference(caseData.getCcdCaseReference().toString())
                .originalDocumentsBulkPrint(List.of(Element.<CaseDocument>builder()
                                                        .value(new CaseDocument()
                                                                   .setDocumentLink(printDocument)
                                                                   .setDocumentType(DocumentType.GENERAL_ORDER)).build()))
                .translatedDocumentsBulkPrint(List.of(Element.<TranslatedDocument>builder()
                                                          .value(new TranslatedDocument()
                                                                     .setFile(printDocument)
                                                                     .setDocumentType(TranslatedDocumentType.GENERAL_ORDER)).build()))
                .respondentBilingualLanguagePreference(YesOrNo.YES)
                .isGaApplicantLip(YesOrNo.YES)
                .parentClaimantIsApplicant(YesOrNo.YES)
                .build();

            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).request(
                CallbackRequest.builder().eventId(SEND_TRANSLATED_ORDER_TO_LIP_RESPONDENT.name())
                    .build()
            ).build();
            handler.printServiceEnabled = true;

            handler.handle(params);

            verify(sendOrderPrintService, times(1))
                .sendJudgeTranslatedOrderToPrintForLIP(any(), eq(printDocument), eq(printDocument), eq(caseData), eq(SEND_TRANSLATED_ORDER_TO_LIP_RESPONDENT));
        }

        @Test
        void shouldSendTranslatedOrderLetterToLipRespondentIfApplicantIsDefendantWhenInvoked() {
            Document printDocument = new Document();
            CaseDetails parentCaseDetails = CaseDetails.builder().build();
            GeneralApplicationCaseData parentCaseData = new GeneralApplicationCaseData()
                .claimantBilingualLanguagePreference("BOTH").build();
            when(coreCaseDataService.getCase(anyLong())).thenReturn(parentCaseDetails);
            when(caseDetailsConverter.toGeneralApplicationCaseData(parentCaseDetails)).thenReturn(parentCaseData);
            GeneralApplicationCaseData caseData = GeneralApplicationCaseDataBuilder.builder().atStateClaimDraft().withNoticeCaseData();
            caseData = caseData.copy()
                .parentCaseReference(caseData.getCcdCaseReference().toString())
                .originalDocumentsBulkPrint(List.of(Element.<CaseDocument>builder()
                                                        .value(new CaseDocument()
                                                                   .setDocumentLink(printDocument)
                                                                   .setDocumentType(DocumentType.GENERAL_ORDER)).build()))
                .translatedDocumentsBulkPrint(List.of(Element.<TranslatedDocument>builder()
                                                          .value(new TranslatedDocument()
                                                                     .setFile(printDocument)
                                                                     .setDocumentType(TranslatedDocumentType.GENERAL_ORDER)).build()))
                .respondentBilingualLanguagePreference(YesOrNo.YES)
                .isGaApplicantLip(YesOrNo.YES)
                .parentClaimantIsApplicant(YesOrNo.NO)
                .build();

            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).request(
                CallbackRequest.builder().eventId(SEND_TRANSLATED_ORDER_TO_LIP_RESPONDENT.name())
                    .build()
            ).build();
            handler.printServiceEnabled = true;

            handler.handle(params);

            verify(sendOrderPrintService, times(1))
                .sendJudgeTranslatedOrderToPrintForLIP(any(), eq(printDocument), eq(printDocument), eq(caseData), eq(SEND_TRANSLATED_ORDER_TO_LIP_RESPONDENT));
        }

        @Test
        void shouldNotSendTranslatedOrderLetterToLipApplicantIfPrintServiceNotEnabled() {
            GeneralApplicationCaseData caseData = GeneralApplicationCaseDataBuilder.builder().atStateClaimDraft().withNoticeCaseData();
            caseData = caseData.copy()
                .parentCaseReference(caseData.getCcdCaseReference().toString())
                .translatedDocumentsBulkPrint(List.of(Element.<TranslatedDocument>builder()
                                                 .value(new TranslatedDocument()
                                                            .setDocumentType(TranslatedDocumentType.GENERAL_ORDER)).build()))
                .applicantBilingualLanguagePreference(YesOrNo.YES)
                .isGaApplicantLip(YesOrNo.YES)
                .parentClaimantIsApplicant(YesOrNo.YES)
                .build();

            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).request(
                CallbackRequest.builder().eventId(SEND_TRANSLATED_ORDER_TO_LIP_APPLICANT.name())
                    .build()
            ).build();

            handler.handle(params);

            verifyNoInteractions(sendOrderPrintService);
        }
    }

    @Test
    void shouldNotSendTranslatedOrderLetterToLipApplicantIfNullDocuments() {
        GeneralApplicationCaseData caseData = GeneralApplicationCaseDataBuilder.builder().atStateClaimDraft().withNoticeCaseData();
        caseData = caseData.copy()
            .parentCaseReference(caseData.getCcdCaseReference().toString())
            .applicantBilingualLanguagePreference(YesOrNo.YES)
            .isGaApplicantLip(YesOrNo.YES)
            .parentClaimantIsApplicant(YesOrNo.YES)
            .build();

        CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).request(
            CallbackRequest.builder().eventId(SEND_TRANSLATED_ORDER_TO_LIP_APPLICANT.name())
                .build()
        ).build();
        handler.printServiceEnabled = true;

        handler.handle(params);

        verifyNoInteractions(sendOrderPrintService);
    }

    @Test
    void shouldNotSendTranslatedOrderLetterToLipApplicantIfEmptyDocuments() {
        GeneralApplicationCaseData caseData = GeneralApplicationCaseDataBuilder.builder().atStateClaimDraft().withNoticeCaseData();
        caseData = caseData.copy()
            .parentCaseReference(caseData.getCcdCaseReference().toString())
            .translatedDocumentsBulkPrint(List.of())
            .applicantBilingualLanguagePreference(YesOrNo.YES)
            .isGaApplicantLip(YesOrNo.YES)
            .parentClaimantIsApplicant(YesOrNo.YES)
            .build();

        CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_SUBMIT, caseData).request(
            CallbackRequest.builder().eventId(SEND_TRANSLATED_ORDER_TO_LIP_APPLICANT.name())
                .build()
        ).build();
        handler.printServiceEnabled = true;

        handler.handle(params);

        verifyNoInteractions(sendOrderPrintService);
    }
}
