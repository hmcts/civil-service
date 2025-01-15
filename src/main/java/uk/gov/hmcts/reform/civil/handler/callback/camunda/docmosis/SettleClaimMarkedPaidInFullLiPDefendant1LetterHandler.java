package uk.gov.hmcts.reform.civil.handler.callback.camunda.docmosis;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.civil.callback.Callback;
import uk.gov.hmcts.reform.civil.callback.CallbackHandler;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.documentmanagement.DocumentDownloadException;
import uk.gov.hmcts.reform.civil.documentmanagement.model.CaseDocument;
import uk.gov.hmcts.reform.civil.documentmanagement.model.DocumentType;
import uk.gov.hmcts.reform.civil.enums.DocCategory;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.documents.DocumentMetaData;
import uk.gov.hmcts.reform.civil.service.BulkPrintService;
import uk.gov.hmcts.reform.civil.service.docmosis.settleanddiscontinue.SettleClaimMarkedPaidInFullDefendantLiPLetterGenerator;
import uk.gov.hmcts.reform.civil.service.documentmanagement.DocumentDownloadService;
import uk.gov.hmcts.reform.civil.stitch.service.CivilStitchService;
import uk.gov.hmcts.reform.civil.utils.AssignCategoryId;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.civil.callback.CallbackParams.Params.BEARER_TOKEN;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.SEND_SETTLE_CLAIM_PAID_IN_FULL_LETTER_TO_LIP_DEFENDANT1;
import static uk.gov.hmcts.reform.civil.service.docmosis.DocmosisTemplates.SETTLE_CLAIM_MARKED_PAID_IN_FULL_LIP_DEFENDANT_LETTER;
import static uk.gov.hmcts.reform.civil.service.docmosis.DocmosisTemplates.SETTLE_CLAIM_MARKED_PAID_IN_FULL_LIP_DEFENDANT_LETTER_BILINGUAL;
import static uk.gov.hmcts.reform.civil.service.docmosis.DocmosisTemplates.SETTLE_CLAIM_MARKED_PAID_IN_FULL_LIP_DEFENDANT_LETTER_BILINGUAL_2;
import static uk.gov.hmcts.reform.civil.service.docmosis.DocmosisTemplates.SETTLE_CLAIM_MARKED_PAID_IN_FULL_LIP_DEFENDANT_LETTER_BILINGUAL_3;

@Slf4j
@RequiredArgsConstructor
@Service
public class SettleClaimMarkedPaidInFullLiPDefendant1LetterHandler extends CallbackHandler {

    private static final List<CaseEvent> EVENTS = List.of(
        SEND_SETTLE_CLAIM_PAID_IN_FULL_LETTER_TO_LIP_DEFENDANT1);
    public static final String TASK_ID = "SendSettleClaimPaidInFullLetterLipDef";
    private final SettleClaimMarkedPaidInFullDefendantLiPLetterGenerator lipLetterGenerator;
    private static final String SETTLE_CLAIM_PAID_IN_FULL_LETTER = "settle-claim-paid-in-full-letter";

    private final CivilStitchService civilStitchService;
    private final DocumentDownloadService documentDownloadService;
    private final BulkPrintService bulkPrintService;
    private final AssignCategoryId assignCategoryId;
    private final ObjectMapper objectMapper;

    @Override
    protected Map<String, Callback> callbacks() {
        return Map.of(
            callbackKey(ABOUT_TO_SUBMIT),
            this::sendSettleClaimPaidInFullLetterToLiPDefendant1
        );
    }

    @Override
    public String camundaActivityId(CallbackParams callbackParams) {
        return TASK_ID;
    }

    @Override
    public List<CaseEvent> handledEvents() {
        return EVENTS;
    }

    private CallbackResponse sendSettleClaimPaidInFullLetterToLiPDefendant1(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        CaseData.CaseDataBuilder<?, ?> caseDataBuilder = caseData.toBuilder();
        Long caseId = caseData.getCcdCaseReference();
        log.info("Generating mailable document for caseId {}", caseId);
        String auth = callbackParams.getParams().get(BEARER_TOKEN).toString();

        final CaseDocument englishDoc = lipLetterGenerator.generateAndPrintSettleClaimPaidInFullLetter(
            caseData, auth, SETTLE_CLAIM_MARKED_PAID_IN_FULL_LIP_DEFENDANT_LETTER);
        final CaseDocument welshDoc = lipLetterGenerator.generateAndPrintSettleClaimPaidInFullLetter(
            caseData, auth, SETTLE_CLAIM_MARKED_PAID_IN_FULL_LIP_DEFENDANT_LETTER_BILINGUAL);
        CaseDocument welshVariation2Doc = lipLetterGenerator.generateAndPrintSettleClaimPaidInFullLetter(
            caseData, auth, SETTLE_CLAIM_MARKED_PAID_IN_FULL_LIP_DEFENDANT_LETTER_BILINGUAL_2);
        CaseDocument welshVariation3Doc = lipLetterGenerator.generateAndPrintSettleClaimPaidInFullLetter(
            caseData, auth, SETTLE_CLAIM_MARKED_PAID_IN_FULL_LIP_DEFENDANT_LETTER_BILINGUAL_3);

        //save into casefileviewer
        log.info("Saving document for caseId {}", caseId);
        caseDataBuilder.respondent1NoticeOfDiscontinueAllPartyViewDoc(welshVariation2Doc);
        caseDataBuilder.respondent2NoticeOfDiscontinueAllPartyViewDoc(welshVariation3Doc);
        assignDiscontinuanceCategoryId(caseDataBuilder.build().getRespondent1NoticeOfDiscontinueAllPartyViewDoc());
        assignDiscontinuanceCategoryId(caseDataBuilder.build().getRespondent2NoticeOfDiscontinueAllPartyViewDoc());

        /*
        //convert to byte array
        log.info("Saving to byte array for caseId {}", caseId);
        byte[] engDocByte = lipLetterGenerator.convertToByte(englishDoc, auth);
        byte[] welshDocByte = lipLetterGenerator.convertToByte(welshDoc, auth);
        byte[] welshDoc2Byte = lipLetterGenerator.convertToByte(welshVariation2Doc, auth);
        byte[] welshDoc3Byte = lipLetterGenerator.convertToByte(welshVariation3Doc, auth);

         creating byteArray -> file
        File outputFile = new File("printSettleClaimPaidInFullLetteByteArray.pdf");
        try (FileOutputStream outputStream = new FileOutputStream(outputFile)) {
            outputStream.write(document);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }*/

        List<DocumentMetaData> documentMetaDataList = appendCoverToDocument(englishDoc, welshDoc);

        log.info("Calling civil stitch service from welsh cover letter for caseId {}", caseId);
        CaseDocument stitchedDocuments =
            civilStitchService.generateStitchedCaseDocument(documentMetaDataList,
                                                            welshDoc.getDocumentName(),
                                                            caseId,
                                                            // DocumentType.SETTLE_CLAIM_PAID_IN_FULL_LETTER,
                                                            DocumentType.NOTICE_OF_DISCONTINUANCE,
                                                            auth);

        log.info("welsh cover letter {} for caseId {}", stitchedDocuments, caseId);

        log.info("Saving stitched letter in applicant1NoticeOfDiscontinueAllPartyViewDoc for caseId {}", caseId);

        caseDataBuilder.applicant1NoticeOfDiscontinueAllPartyViewDoc(welshDoc);
        assignDiscontinuanceCategoryId(caseDataBuilder.build().getApplicant1NoticeOfDiscontinueAllPartyViewDoc());

        byte[] letterContent = getLetterContent(stitchedDocuments, auth, caseId);

        List<String> recipients = getRecipientsList(caseData);
        bulkPrintService.printLetter(letterContent, caseData.getLegacyCaseReference(),
                                     caseData.getLegacyCaseReference(), SETTLE_CLAIM_PAID_IN_FULL_LETTER, recipients);

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseDataBuilder.build().toMap(objectMapper))
            .build();
    }

    private List<String> getRecipientsList(CaseData caseData) {
        return List.of(caseData.getRespondent1().getPartyName());
    }

    private List<DocumentMetaData> appendCoverToDocument(CaseDocument coverLetter, CaseDocument... caseDocuments) {
        List<DocumentMetaData> documentMetaDataList = new ArrayList<>();

        documentMetaDataList.add(new DocumentMetaData(coverLetter.getDocumentLink(),
                                                      "Welsh letter",
                                                      LocalDate.now().toString()));

        Arrays.stream(caseDocuments).forEach(caseDocument -> documentMetaDataList.add(new DocumentMetaData(
            caseDocument.getDocumentLink(),
            "welsh letter to attach",
            LocalDate.now().toString()
        )));

        return documentMetaDataList;

    }

    private void assignDiscontinuanceCategoryId(CaseDocument caseDocument) {
        assignCategoryId.assignCategoryIdToCaseDocument(caseDocument, DocCategory.NOTICE_OF_DISCONTINUE.getValue());
    }

    //    private boolean isRespondent1Lip(CaseData caseData) {
    //        return YesOrNo.NO.equals(caseData.getRespondent1Represented());
    //    }

    private byte[] getLetterContent(CaseDocument mailableSdoDocument, String authorisation, Long caseId) {
        byte[] letterContent;
        try {
            String documentUrl = mailableSdoDocument.getDocumentLink().getDocumentUrl();
            String documentId = documentUrl.substring(documentUrl.lastIndexOf("/") + 1);
            letterContent = documentDownloadService.downloadDocument(authorisation, documentId).file().getInputStream().readAllBytes();
        } catch (Exception e) {
            log.error("Failed getting letter content for document to mail for caseId {}", caseId, e);
            throw new DocumentDownloadException(mailableSdoDocument.getDocumentLink().getDocumentFileName(), e);
        }
        return letterContent;
    }

    //    private void generateAndPrintSettleClaimPaidInFullLetter(CallbackParams callbackParams) {
    //        CaseData caseData = callbackParams.getCaseData();
    //        lipLetterGenerator.generateAndPrintSettleClaimPaidInFullLetter(caseData, callbackParams.getParams().get(BEARER_TOKEN).toString());
    //    }
}
