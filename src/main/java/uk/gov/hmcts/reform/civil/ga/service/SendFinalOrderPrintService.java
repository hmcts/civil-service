package uk.gov.hmcts.reform.civil.ga.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.documentmanagement.DocumentDownloadException;
import uk.gov.hmcts.reform.civil.documentmanagement.DocumentManagementService;
import uk.gov.hmcts.reform.civil.documentmanagement.model.CaseDocument;
import uk.gov.hmcts.reform.civil.documentmanagement.model.Document;
import uk.gov.hmcts.reform.civil.documentmanagement.model.DocumentType;
import uk.gov.hmcts.reform.civil.documentmanagement.model.PDF;
import uk.gov.hmcts.reform.civil.ga.model.GeneralApplicationCaseData;
import uk.gov.hmcts.reform.civil.ga.model.docmosis.PostOrderCoverLetter;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.model.docmosis.DocmosisDocument;
import uk.gov.hmcts.reform.civil.model.documents.DocumentMetaData;
import uk.gov.hmcts.reform.civil.service.BulkPrintService;
import uk.gov.hmcts.reform.civil.service.CoreCaseDataService;
import uk.gov.hmcts.reform.civil.service.docmosis.DocumentGeneratorService;
import uk.gov.hmcts.reform.civil.service.documentmanagement.DocumentDownloadService;
import uk.gov.hmcts.reform.civil.service.flowstate.FlowFlag;
import uk.gov.hmcts.reform.civil.stitch.service.CivilStitchService;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static java.util.Optional.ofNullable;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.SEND_TRANSLATED_ORDER_TO_LIP_APPLICANT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.SEND_TRANSLATED_ORDER_TO_LIP_RESPONDENT;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;
import static uk.gov.hmcts.reform.civil.service.docmosis.DocmosisTemplates.POST_ORDER_COVER_LETTER_LIP;

@Service
@RequiredArgsConstructor
@Slf4j
public class SendFinalOrderPrintService {

    private final BulkPrintService bulkPrintService;
    private final DocumentDownloadService documentDownloadService;
    private final DocumentManagementService documentManagementService;
    private final DocumentGeneratorService documentGeneratorService;

    private final CaseDetailsConverter caseDetailsConverter;
    private final CoreCaseDataService coreCaseDataService;

    private final CivilStitchService civilStitchService;

    private static final String FINAL_ORDER_PACK_LETTER_TYPE = "final-order-document-pack";
    private static final String TRANSLATED_ORDER_PACK_LETTER_TYPE = "translated-order-document-pack";
    @Value("${stitching.enabled}")
    private boolean stitchEnabled;

    public void sendJudgeFinalOrderToPrintForLIP(String authorisation,
                                                 Document postJudgeOrderDocument,
                                                 GeneralApplicationCaseData caseData,
                                                 GeneralApplicationCaseData civilCaseData,
                                                 FlowFlag lipUserType) {

        List<String> recipients = new ArrayList<>();
        boolean parentClaimantIsApplicant = caseData.identifyParentClaimantIsApplicant(caseData);

        String documentUrl = postJudgeOrderDocument.getDocumentUrl();
        String documentId = documentUrl.substring(documentUrl.lastIndexOf("/") + 1);

        byte[] letterContent;

        try {
            letterContent = documentDownloadService.downloadDocument(authorisation, documentId).file().getInputStream().readAllBytes();
        } catch (IOException e) {
            log.error("Failed getting letter content for Pip Stitched Letter ");
            throw new DocumentDownloadException(postJudgeOrderDocument.getDocumentFileName(), e);
        }

        if (lipUserType.equals(FlowFlag.POST_JUDGE_ORDER_LIP_APPLICANT) && Objects.nonNull(caseData.getClaimant1PartyName())) {
            recipients.add(caseData.getPartyName(parentClaimantIsApplicant, lipUserType, civilCaseData));

        }

        if (lipUserType.equals(FlowFlag.POST_JUDGE_ORDER_LIP_RESPONDENT)
            && Objects.nonNull(caseData.getDefendant1PartyName())) {
            recipients.add(caseData.getPartyName(parentClaimantIsApplicant, lipUserType, civilCaseData));
        }

        List<String> bulkPrintFileNames = new ArrayList<>();
        bulkPrintFileNames.add(postJudgeOrderDocument.getDocumentFileName());
        bulkPrintService.printLetter(letterContent, caseData.getGeneralAppParentCaseLink().getCaseReference(),
                civilCaseData.getLegacyCaseReference(),
                SendFinalOrderPrintService.FINAL_ORDER_PACK_LETTER_TYPE, recipients, bulkPrintFileNames);
    }

    public void sendJudgeTranslatedOrderToPrintForLIP(String authorisation,
                                                      Document originalDocument,
                                                      Document translatedDocument,
                                                      GeneralApplicationCaseData caseData,
                                                      CaseEvent caseEvent) {

        GeneralApplicationCaseData civilCaseData = caseDetailsConverter
            .toGeneralApplicationCaseData(coreCaseDataService
                            .getCase(Long.parseLong(caseData.getGeneralAppParentCaseLink().getCaseReference())));

        DocmosisDocument coverLetter = generate(caseData, civilCaseData, caseEvent);

        CaseDocument coverLetterCaseDocument = documentManagementService.uploadDocument(
            authorisation,
            new PDF(
                POST_ORDER_COVER_LETTER_LIP.getDocumentTitle(),
                coverLetter.getBytes(),
                DocumentType.POST_ORDER_COVER_LETTER_LIP
            )
        );

        if (stitchEnabled) {
            stitchAndSendDocument(
                authorisation,
                originalDocument,
                translatedDocument,
                caseData,
                caseEvent,
                coverLetterCaseDocument,
                civilCaseData
            );
        }
    }

    private void stitchAndSendDocument(String authorisation, Document originalDocument, Document translatedDocument, GeneralApplicationCaseData caseData, CaseEvent caseEvent,
                                       CaseDocument coverLetterCaseDocument, GeneralApplicationCaseData civilCaseData) {
        List<DocumentMetaData> documentMetaDataList
            = stitchCoverLetterAndOrderDocuments(coverLetterCaseDocument, originalDocument, translatedDocument);

        CaseDocument stitchedDocument = civilStitchService.generateStitchedCaseDocument(
            documentMetaDataList, coverLetterCaseDocument.getDocumentName(), caseData.getCcdCaseReference(),
            DocumentType.POST_ORDER_COVER_LETTER_LIP, authorisation);

        String documentUrl = stitchedDocument.getDocumentLink().getDocumentUrl();
        String documentId = documentUrl.substring(documentUrl.lastIndexOf("/") + 1);

        byte[] letterContent;

        try {
            letterContent = documentDownloadService.downloadDocument(
                authorisation,
                documentId
            ).file().getInputStream().readAllBytes();
        } catch (IOException e) {
            log.error("Failed getting letter content for Pip Stitched Letter ");
            throw new DocumentDownloadException(stitchedDocument.getDocumentLink().getDocumentFileName(), e);
        }
        List<String> recipients = getRecipients(caseData, caseEvent, civilCaseData);
        List<String> bulkPrintFilenames = new ArrayList<>();
        bulkPrintFilenames.add(stitchedDocument.getDocumentLink().getDocumentFileName());
        bulkPrintService.printLetter(letterContent, caseData.getGeneralAppParentCaseLink().getCaseReference(),
                                     civilCaseData.getLegacyCaseReference(),
                                     SendFinalOrderPrintService.TRANSLATED_ORDER_PACK_LETTER_TYPE, recipients, bulkPrintFilenames);
    }

    private List<String> getRecipients(GeneralApplicationCaseData caseData, CaseEvent caseEvent, GeneralApplicationCaseData civilCaseData) {
        List<String> recipients = new ArrayList<>();
        if (caseEvent == SEND_TRANSLATED_ORDER_TO_LIP_APPLICANT) {
            String applicant = caseData.getParentClaimantIsApplicant() == YES
                ? civilCaseData.getApplicant1().getPartyName()
                : civilCaseData.getRespondent1().getPartyName();
            recipients.add(applicant);
        }

        if (caseEvent == SEND_TRANSLATED_ORDER_TO_LIP_RESPONDENT) {
            String respondent = caseData.getParentClaimantIsApplicant() == YES
                ? civilCaseData.getRespondent1().getPartyName()
                : civilCaseData.getApplicant1().getPartyName();
            recipients.add(respondent);
        }
        return recipients;
    }

    private List<DocumentMetaData> stitchCoverLetterAndOrderDocuments(CaseDocument coverLetterCaseDocument, Document originalDocument, Document translatedDocument) {
        List<DocumentMetaData> documentMetaDataList = new ArrayList<>();

        documentMetaDataList.add(new DocumentMetaData(coverLetterCaseDocument.getDocumentLink(),
                                                      "Post order cover letter",
                                                      LocalDate.now().toString()));
        if (originalDocument != null) {
            documentMetaDataList.add(new DocumentMetaData(
                originalDocument,
                "Judge order",
                LocalDate.now().toString()
            ));
        }
        if (translatedDocument != null) {
            documentMetaDataList.add(new DocumentMetaData(
                translatedDocument,
                "Translated judge order",
                LocalDate.now().toString()
            ));
        }

        return documentMetaDataList;
    }

    private boolean identifyParentClaimantIsApplicant(GeneralApplicationCaseData caseData) {
        return caseData.getParentClaimantIsApplicant() == null
            || YES.equals(caseData.getParentClaimantIsApplicant());
    }

    private DocmosisDocument generate(GeneralApplicationCaseData caseData, GeneralApplicationCaseData civilCaseData, CaseEvent caseEvent) {

        return documentGeneratorService.generateDocmosisDocument(
            getTemplateData(caseData, civilCaseData, caseEvent),
            POST_ORDER_COVER_LETTER_LIP
        );
    }

    public PostOrderCoverLetter getTemplateData(GeneralApplicationCaseData caseData, GeneralApplicationCaseData civilCaseData, CaseEvent caseEvent) {
        boolean parentClaimantIsApplicant = identifyParentClaimantIsApplicant(caseData);

        return new PostOrderCoverLetter()
            .setCaseNumber(caseData.getGeneralAppParentCaseLink().getCaseReference())
            .setPartyName(getPartyName(parentClaimantIsApplicant, caseEvent, civilCaseData))
            .setPartyAddressAddressLine1(partyAddressAddressLine1(parentClaimantIsApplicant, caseEvent, civilCaseData))
            .setPartyAddressAddressLine2(partyAddressAddressLine2(parentClaimantIsApplicant, caseEvent, civilCaseData))
            .setPartyAddressAddressLine3(partyAddressAddressLine3(parentClaimantIsApplicant, caseEvent, civilCaseData))
            .setPartyAddressPostCode(partyAddressPostCode(parentClaimantIsApplicant, caseEvent, civilCaseData))
            .setPartyAddressPostTown(partyAddressPostTown(parentClaimantIsApplicant, caseEvent, civilCaseData));
    }

    private String getPartyName(boolean parentClaimantIsApplicant, CaseEvent caseEvent, GeneralApplicationCaseData civilCaseData) {

        if (caseEvent == SEND_TRANSLATED_ORDER_TO_LIP_APPLICANT) {
            return parentClaimantIsApplicant
                ? civilCaseData.getApplicant1().getPartyName()
                : civilCaseData.getRespondent1().getPartyName();
        } else {
            return parentClaimantIsApplicant
                ? civilCaseData.getRespondent1().getPartyName()
                : civilCaseData.getApplicant1().getPartyName();
        }
    }

    private String partyAddressAddressLine1(boolean parentClaimantIsApplicant, CaseEvent caseEvent, GeneralApplicationCaseData civilCaseData) {

        if (caseEvent == SEND_TRANSLATED_ORDER_TO_LIP_APPLICANT) {
            return parentClaimantIsApplicant
                ? ofNullable(civilCaseData.getApplicant1().getPrimaryAddress().getAddressLine1())
                .orElse(StringUtils.EMPTY)
                : ofNullable(civilCaseData.getRespondent1().getPrimaryAddress().getAddressLine1())
                .orElse(StringUtils.EMPTY);
        } else {
            return parentClaimantIsApplicant
                ? ofNullable(civilCaseData.getRespondent1().getPrimaryAddress().getAddressLine1())
                .orElse(StringUtils.EMPTY)
                : ofNullable(civilCaseData.getApplicant1().getPrimaryAddress().getAddressLine1())
                .orElse(StringUtils.EMPTY);
        }
    }

    private String partyAddressAddressLine2(boolean parentClaimantIsApplicant, CaseEvent caseEvent, GeneralApplicationCaseData civilCaseData) {
        if (caseEvent == SEND_TRANSLATED_ORDER_TO_LIP_APPLICANT) {
            return parentClaimantIsApplicant
                ? ofNullable(civilCaseData.getApplicant1().getPrimaryAddress().getAddressLine2())
                .orElse(StringUtils.EMPTY)
                : ofNullable(civilCaseData.getRespondent1().getPrimaryAddress().getAddressLine2())
                .orElse(StringUtils.EMPTY);
        } else {
            return parentClaimantIsApplicant
                ? ofNullable(civilCaseData.getRespondent1().getPrimaryAddress().getAddressLine2())
                .orElse(StringUtils.EMPTY)
                : ofNullable(civilCaseData.getApplicant1().getPrimaryAddress().getAddressLine2())
                .orElse(StringUtils.EMPTY);
        }
    }

    private String partyAddressAddressLine3(boolean parentClaimantIsApplicant, CaseEvent caseEvent, GeneralApplicationCaseData civilCaseData) {
        if (caseEvent == SEND_TRANSLATED_ORDER_TO_LIP_APPLICANT) {
            return parentClaimantIsApplicant
                ? ofNullable(civilCaseData.getApplicant1().getPrimaryAddress().getAddressLine3())
                .orElse(StringUtils.EMPTY)
                : ofNullable(civilCaseData.getRespondent1().getPrimaryAddress().getAddressLine3())
                .orElse(StringUtils.EMPTY);
        } else {
            return parentClaimantIsApplicant
                ? ofNullable(civilCaseData.getRespondent1().getPrimaryAddress().getAddressLine3())
                .orElse(StringUtils.EMPTY)
                : ofNullable(civilCaseData.getApplicant1().getPrimaryAddress().getAddressLine3())
                .orElse(StringUtils.EMPTY);
        }
    }

    private String partyAddressPostCode(boolean parentClaimantIsApplicant, CaseEvent caseEvent, GeneralApplicationCaseData civilCaseData) {
        if (caseEvent == SEND_TRANSLATED_ORDER_TO_LIP_APPLICANT) {
            return parentClaimantIsApplicant
                ? ofNullable(civilCaseData.getApplicant1().getPrimaryAddress().getPostCode())
                .orElse(StringUtils.EMPTY)
                : ofNullable(civilCaseData.getRespondent1().getPrimaryAddress().getPostCode())
                .orElse(StringUtils.EMPTY);
        } else {
            return parentClaimantIsApplicant
                ? ofNullable(civilCaseData.getRespondent1().getPrimaryAddress().getPostCode())
                .orElse(StringUtils.EMPTY)
                : ofNullable(civilCaseData.getApplicant1().getPrimaryAddress().getPostCode())
                .orElse(StringUtils.EMPTY);
        }
    }

    private String partyAddressPostTown(boolean parentClaimantIsApplicant, CaseEvent caseEvent, GeneralApplicationCaseData civilCaseData) {
        if (caseEvent == SEND_TRANSLATED_ORDER_TO_LIP_APPLICANT) {
            return parentClaimantIsApplicant
                ? ofNullable(civilCaseData.getApplicant1().getPrimaryAddress().getPostTown())
                .orElse(StringUtils.EMPTY)
                : ofNullable(civilCaseData.getRespondent1().getPrimaryAddress().getPostTown())
                .orElse(StringUtils.EMPTY);
        } else {
            return parentClaimantIsApplicant
                ? ofNullable(civilCaseData.getRespondent1().getPrimaryAddress().getPostTown())
                .orElse(StringUtils.EMPTY)
                : ofNullable(civilCaseData.getApplicant1().getPrimaryAddress().getPostTown())
                .orElse(StringUtils.EMPTY);
        }
    }
}
