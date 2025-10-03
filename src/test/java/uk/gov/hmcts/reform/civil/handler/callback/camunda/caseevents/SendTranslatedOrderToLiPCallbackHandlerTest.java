package uk.gov.hmcts.reform.civil.handler.callback.camunda.caseevents;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.documentmanagement.model.CaseDocument;
import uk.gov.hmcts.reform.civil.documentmanagement.model.Document;
import uk.gov.hmcts.reform.civil.documentmanagement.model.DocumentType;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.citizenui.RespondentLiPResponse;
import uk.gov.hmcts.reform.civil.model.citizenui.TranslatedDocument;
import uk.gov.hmcts.reform.civil.model.citizenui.TranslatedDocumentType;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.sampledata.CallbackParamsBuilder;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.CoreCaseDataService;
import uk.gov.hmcts.reform.civil.service.SendFinalOrderPrintService;

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
        assertThat(handler.camundaActivityId(CallbackParams.builder().build())).isEqualTo(TASK_ID);
    }

    @Nested
    class AboutToSubmitCallback {

        @Test
        void shouldSendTranslatedOrderLetterToLipApplicantWhenInvoked() {
            Document printDocument = Document.builder().build();
            CaseDetails parentCaseDetails = CaseDetails.builder().build();
            CaseData parentCaseData = CaseData.builder()
                .claimantBilingualLanguagePreference("WELSH").build();
            when(coreCaseDataService.getCase(anyLong())).thenReturn(parentCaseDetails);
            when(caseDetailsConverter.toCaseData(parentCaseDetails)).thenReturn(parentCaseData);
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDraft().withNoticeCaseData();
            caseData = caseData.toBuilder()
                .parentCaseReference(caseData.getCcdCaseReference().toString())
                .originalDocumentsBulkPrint(List.of(Element.<CaseDocument>builder()
                                                        .value(CaseDocument.builder()
                                                                   .documentLink(printDocument)
                                                                   .documentType(DocumentType.GENERAL_ORDER).build()).build().getValue()))
                .translatedDocumentsBulkPrint(List.of(Element.<TranslatedDocument>builder()
                                                 .value(TranslatedDocument.builder()
                                                            .file(printDocument)
                                                            .documentType(TranslatedDocumentType.GENERAL_ORDER).build()).build()))
                .applicantBilingualLanguagePreferenceGA(YesOrNo.YES)
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
            Document printDocument = Document.builder().build();
            CaseDetails parentCaseDetails = CaseDetails.builder().build();
            CaseData parentCaseData = CaseData.builder().build();
            when(coreCaseDataService.getCase(anyLong())).thenReturn(parentCaseDetails);
            when(caseDetailsConverter.toCaseData(parentCaseDetails)).thenReturn(parentCaseData);
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDraft().withNoticeCaseData();
            caseData = caseData.toBuilder()
                .parentCaseReference(caseData.getCcdCaseReference().toString())
                .originalDocumentsBulkPrint(List.of(Element.<CaseDocument>builder()
                                                          .value(CaseDocument.builder()
                                                                     .documentLink(printDocument)
                                                                     .documentType(DocumentType.GENERAL_ORDER).build()).build().getValue()))
                .translatedDocumentsBulkPrint(List.of(Element.<TranslatedDocument>builder()
                                                          .value(TranslatedDocument.builder()
                                                                     .file(printDocument)
                                                                     .documentType(TranslatedDocumentType.GENERAL_ORDER).build()).build()))
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
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDraft().withNoticeCaseData();
            caseData = caseData.toBuilder()
                .parentCaseReference(caseData.getCcdCaseReference().toString())
                .translatedDocumentsBulkPrint(List.of(Element.<TranslatedDocument>builder()
                                                          .value(TranslatedDocument.builder()
                                                                     .documentType(TranslatedDocumentType.APPLICATION_SUMMARY_DOCUMENT).build()).build()))
                .applicantBilingualLanguagePreferenceGA(YesOrNo.YES)
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
            Document printDocument = Document.builder().build();
            CaseDetails parentCaseDetails = CaseDetails.builder().build();
            CaseData parentCaseData = CaseData.builder()
                .respondent1LiPResponseGA(RespondentLiPResponse.builder().respondent1ResponseLanguage("BOTH").build()).build();
            when(coreCaseDataService.getCase(anyLong())).thenReturn(parentCaseDetails);
            when(caseDetailsConverter.toCaseData(parentCaseDetails)).thenReturn(parentCaseData);
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDraft().withNoticeCaseData();
            caseData = caseData.toBuilder()
                .parentCaseReference(caseData.getCcdCaseReference().toString())
                .originalDocumentsBulkPrint(List.of(Element.<CaseDocument>builder()
                                                        .value(CaseDocument.builder()
                                                                   .documentLink(printDocument)
                                                                   .documentType(DocumentType.GENERAL_ORDER).build()).build().getValue()))
                .translatedDocumentsBulkPrint(List.of(Element.<TranslatedDocument>builder()
                                                          .value(TranslatedDocument.builder()
                                                                     .file(printDocument)
                                                                     .documentType(TranslatedDocumentType.GENERAL_ORDER).build()).build()))
                .respondentBilingualLanguagePreferenceGA(YesOrNo.YES)
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
            Document printDocument = Document.builder().build();
            CaseDetails parentCaseDetails = CaseDetails.builder().build();
            CaseData parentCaseData = CaseData.builder()
                .claimantBilingualLanguagePreference("BOTH").build();
            when(coreCaseDataService.getCase(anyLong())).thenReturn(parentCaseDetails);
            when(caseDetailsConverter.toCaseData(parentCaseDetails)).thenReturn(parentCaseData);
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDraft().withNoticeCaseData();
            caseData = caseData.toBuilder()
                .parentCaseReference(caseData.getCcdCaseReference().toString())
                .originalDocumentsBulkPrint(List.of(Element.<CaseDocument>builder()
                                                        .value(CaseDocument.builder()
                                                                   .documentLink(printDocument)
                                                                   .documentType(DocumentType.GENERAL_ORDER).build()).build().getValue()))
                .translatedDocumentsBulkPrint(List.of(Element.<TranslatedDocument>builder()
                                                          .value(TranslatedDocument.builder()
                                                                     .file(printDocument)
                                                                     .documentType(TranslatedDocumentType.GENERAL_ORDER).build()).build()))
                .respondentBilingualLanguagePreferenceGA(YesOrNo.YES)
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
            CaseData caseData = CaseDataBuilder.builder().atStateClaimDraft().withNoticeCaseData();
            caseData = caseData.toBuilder()
                .parentCaseReference(caseData.getCcdCaseReference().toString())
                .translatedDocumentsBulkPrint(List.of(Element.<TranslatedDocument>builder()
                                                 .value(TranslatedDocument.builder()
                                                            .documentType(TranslatedDocumentType.GENERAL_ORDER).build()).build()))
                .applicantBilingualLanguagePreferenceGA(YesOrNo.YES)
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
        CaseData caseData = CaseDataBuilder.builder().atStateClaimDraft().withNoticeCaseData();
        caseData = caseData.toBuilder()
            .parentCaseReference(caseData.getCcdCaseReference().toString())
            .applicantBilingualLanguagePreferenceGA(YesOrNo.YES)
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
        CaseData caseData = CaseDataBuilder.builder().atStateClaimDraft().withNoticeCaseData();
        caseData = caseData.toBuilder()
            .parentCaseReference(caseData.getCcdCaseReference().toString())
            .translatedDocumentsBulkPrint(List.of())
            .applicantBilingualLanguagePreferenceGA(YesOrNo.YES)
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
