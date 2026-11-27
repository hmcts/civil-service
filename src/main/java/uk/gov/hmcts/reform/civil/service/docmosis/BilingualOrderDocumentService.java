package uk.gov.hmcts.reform.civil.service.docmosis;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.documentmanagement.model.CaseDocument;
import uk.gov.hmcts.reform.civil.documentmanagement.model.DocumentType;
import uk.gov.hmcts.reform.civil.enums.dq.Language;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.model.documents.DocumentMetaData;
import uk.gov.hmcts.reform.civil.stitch.service.CivilStitchService;
import uk.gov.hmcts.reform.civil.utils.LanguageUtils;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import static java.util.Objects.nonNull;
import static uk.gov.hmcts.reform.civil.documentmanagement.model.DocumentType.FINAL_ORDER_BILINGUAL_DOCUMENT;
import static uk.gov.hmcts.reform.civil.documentmanagement.model.DocumentType.FINAL_ORDER_TRANSLATED_DOCUMENT;
import static uk.gov.hmcts.reform.civil.documentmanagement.model.DocumentType.HEARING_NOTICE_BILINGUAL_DOCUMENT;
import static uk.gov.hmcts.reform.civil.documentmanagement.model.DocumentType.JUDGE_FINAL_ORDER;
import static uk.gov.hmcts.reform.civil.documentmanagement.model.DocumentType.SDO_BILINGUAL_DOCUMENT;
import static uk.gov.hmcts.reform.civil.utils.ElementUtils.element;

/**
 * OCCC-827: resolve EN / CY / stitched-BOTH documents for LiP dashboard links,
 * and create stitched bilingual PDFs (English then Welsh) after translation upload.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BilingualOrderDocumentService {

    private static final String ENGLISH_LABEL = "English document";
    private static final String WELSH_LABEL = "Welsh document";

    private final CivilStitchService civilStitchService;

    public Optional<String> resolveSdoDocumentUrl(CaseData caseData, boolean isClaimant, boolean welshEnabled) {
        Language language = LanguageUtils.determineLanguageForBulkPrint(caseData, isClaimant, welshEnabled);
        return switch (language) {
            case ENGLISH -> binaryUrl(caseData.getSDODocument());
            case WELSH -> binaryUrl(caseData.getTranslatedSDODocument());
            case BOTH -> binaryUrl(caseData.getBilingualSDODocument());
        };
    }

    public Optional<String> resolveFinalOrderDocumentUrl(CaseData caseData, boolean isClaimant, boolean welshEnabled) {
        Language language = LanguageUtils.determineLanguageForBulkPrint(caseData, isClaimant, welshEnabled);
        return switch (language) {
            case ENGLISH -> binaryUrl(latestFinalOrderOfType(caseData, JUDGE_FINAL_ORDER));
            case WELSH -> binaryUrl(latestFinalOrderOfType(caseData, FINAL_ORDER_TRANSLATED_DOCUMENT));
            case BOTH -> binaryUrl(latestFinalOrderOfType(caseData, FINAL_ORDER_BILINGUAL_DOCUMENT));
        };
    }

    public Optional<String> resolveHearingNoticeDocumentUrl(CaseData caseData, boolean isClaimant, boolean welshEnabled) {
        Language language = LanguageUtils.determineLanguageForBulkPrint(caseData, isClaimant, welshEnabled);
        return switch (language) {
            case ENGLISH -> binaryUrl(caseData.getHearingFormDocument());
            case WELSH -> binaryUrl(caseData.getTranslatedHearingNoticeDocument());
            case BOTH -> binaryUrl(caseData.getBilingualHearingNoticeDocument());
        };
    }

    /**
     * When at least one party prefers BOTH, stitch EN then CY SDO into a bilingual document
     * and append it to systemGeneratedCaseDocuments.
     */
    public void stitchSdoIfRequired(CaseData caseData, String authorisation) {
        if (!anyPartyPrefersBoth(caseData)) {
            return;
        }
        Optional<Element<CaseDocument>> english = caseData.getSDODocument();
        Optional<Element<CaseDocument>> welsh = caseData.getTranslatedSDODocument();
        if (english.isEmpty() || welsh.isEmpty()) {
            return;
        }
        if (caseData.getBilingualSDODocument().isPresent()) {
            return;
        }
        CaseDocument stitched = stitch(
            english.get().getValue(),
            welsh.get().getValue(),
            caseData,
            authorisation,
            SDO_BILINGUAL_DOCUMENT,
            "Bilingual_SDO.pdf"
        );
        List<Element<CaseDocument>> docs = nonNull(caseData.getSystemGeneratedCaseDocuments())
            ? new ArrayList<>(caseData.getSystemGeneratedCaseDocuments())
            : new ArrayList<>();
        docs.add(element(stitched));
        caseData.setSystemGeneratedCaseDocuments(docs);
    }

    /**
     * When at least one party prefers BOTH, stitch EN then CY final order into a bilingual document
     * and append it to finalOrderDocumentCollection.
     */
    public void stitchFinalOrderIfRequired(CaseData caseData, String authorisation) {
        if (!anyPartyPrefersBoth(caseData)) {
            return;
        }
        Optional<Element<CaseDocument>> english = latestFinalOrderOfType(caseData, JUDGE_FINAL_ORDER);
        Optional<Element<CaseDocument>> welsh = latestFinalOrderOfType(caseData, FINAL_ORDER_TRANSLATED_DOCUMENT);
        if (english.isEmpty() || welsh.isEmpty()) {
            return;
        }
        if (latestFinalOrderOfType(caseData, FINAL_ORDER_BILINGUAL_DOCUMENT).isPresent()) {
            return;
        }
        CaseDocument stitched = stitch(
            english.get().getValue(),
            welsh.get().getValue(),
            caseData,
            authorisation,
            FINAL_ORDER_BILINGUAL_DOCUMENT,
            "Bilingual_Final_Order.pdf"
        );
        List<Element<CaseDocument>> docs = nonNull(caseData.getFinalOrderDocumentCollection())
            ? new ArrayList<>(caseData.getFinalOrderDocumentCollection())
            : new ArrayList<>();
        docs.add(element(stitched));
        caseData.setFinalOrderDocumentCollection(docs);
    }

    /**
     * When at least one party prefers BOTH, stitch EN then CY hearing notice into a bilingual document
     * and append it to hearingDocuments.
     */
    public void stitchHearingNoticeIfRequired(CaseData caseData, String authorisation) {
        if (!anyPartyPrefersBoth(caseData)) {
            return;
        }
        Optional<Element<CaseDocument>> english = caseData.getHearingFormDocument();
        Optional<Element<CaseDocument>> welsh = caseData.getTranslatedHearingNoticeDocument();
        if (english.isEmpty() || welsh.isEmpty()) {
            return;
        }
        if (caseData.getBilingualHearingNoticeDocument().isPresent()) {
            return;
        }
        CaseDocument stitched = stitch(
            english.get().getValue(),
            welsh.get().getValue(),
            caseData,
            authorisation,
            HEARING_NOTICE_BILINGUAL_DOCUMENT,
            "Bilingual_Hearing_Notice.pdf"
        );
        List<Element<CaseDocument>> docs = nonNull(caseData.getHearingDocuments())
            ? new ArrayList<>(caseData.getHearingDocuments())
            : new ArrayList<>();
        docs.add(element(stitched));
        caseData.setHearingDocuments(docs);
    }

    private CaseDocument stitch(CaseDocument english,
                                CaseDocument welsh,
                                CaseData caseData,
                                String authorisation,
                                DocumentType documentType,
                                String documentName) {
        List<DocumentMetaData> metaData = new ArrayList<>();
        metaData.add(new DocumentMetaData(english.getDocumentLink(), ENGLISH_LABEL, LocalDate.now().toString()));
        metaData.add(new DocumentMetaData(welsh.getDocumentLink(), WELSH_LABEL, LocalDate.now().toString()));
        log.info("Stitching bilingual {} for case {}", documentType, caseData.getCcdCaseReference());
        return civilStitchService.generateStitchedCaseDocument(
            metaData,
            documentName,
            caseData.getCcdCaseReference(),
            documentType,
            authorisation
        );
    }

    private boolean anyPartyPrefersBoth(CaseData caseData) {
        return LanguageUtils.determineLanguageForBulkPrint(caseData, true, true) == Language.BOTH
            || LanguageUtils.determineLanguageForBulkPrint(caseData, false, true) == Language.BOTH;
    }

    private Optional<String> binaryUrl(Optional<Element<CaseDocument>> document) {
        return document.map(doc -> doc.getValue().getDocumentLink().getDocumentBinaryUrl());
    }

    private Optional<Element<CaseDocument>> latestFinalOrderOfType(CaseData caseData, DocumentType documentType) {
        if (caseData.getFinalOrderDocumentCollection() == null) {
            return Optional.empty();
        }
        return caseData.getFinalOrderDocumentCollection().stream()
            .filter(doc -> doc.getValue() != null && documentType.equals(doc.getValue().getDocumentType()))
            .max(Comparator.comparing(doc -> doc.getValue().getCreatedDatetime()));
    }
}
