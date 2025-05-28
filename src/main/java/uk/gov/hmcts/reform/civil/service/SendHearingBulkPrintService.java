package uk.gov.hmcts.reform.civil.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.documentmanagement.model.CaseDocument;
import uk.gov.hmcts.reform.civil.enums.dq.Language;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.service.docmosis.CoverLetterAppendService;
import uk.gov.hmcts.reform.civil.service.documentmanagement.DocumentDownloadService;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static java.util.Objects.nonNull;
import static uk.gov.hmcts.reform.civil.documentmanagement.model.DocumentType.HEARING_NOTICE;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.caseevents.SendHearingToLiPCallbackHandler.TASK_ID_DEFENDANT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.caseevents.SendHearingToLiPCallbackHandler.TASK_ID_DEFENDANT_HMC;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.caseevents.SendSDOToLiPDefendantCallbackHandler.TASK_ID_CLAIMANT;

@Service
@RequiredArgsConstructor
@Slf4j
public class SendHearingBulkPrintService {

    private final BulkPrintService bulkPrintService;
    private final DocumentDownloadService documentDownloadService;
    private final CoverLetterAppendService coverLetterAppendService;
    private static final String HEARING_PACK_LETTER_TYPE = "hearing-document-pack";
    private final FeatureToggleService featureToggleService;

    public void sendHearingToLIP(String authorisation, CaseData caseData, String task, boolean welshDocument) {
        List<CaseDocument> caseDocuments = new ArrayList<>();
        Language language = determineLanguageForBulkPrint(caseData, task, welshDocument);
        caseData.getSDODocument().map(Element::getValue).ifPresent(caseDocuments::add);
        CaseDocument caseDocumentEnglish = caseData.getHearingDocuments().get(0).getValue();
        switch (language) {
            case ENGLISH -> {
                if (Objects.nonNull(caseDocumentEnglish)) {
                    caseDocuments.add(caseDocumentEnglish);
                }
            }
            case WELSH -> caseDocuments.add(caseData.getHearingDocumentsWelsh().get(0).getValue());
            case BOTH -> {
                caseDocuments.add(caseData.getHearingDocuments().get(0).getValue());
                caseDocuments.add(caseData.getHearingDocumentsWelsh().get(0).getValue());
            }
            default -> { }
        }

        if (!caseDocuments.isEmpty()) {
            byte[] letterContent;
            letterContent = generateLetterContent(authorisation, caseData, task, caseDocuments.toArray(new CaseDocument[0]));
            Party recipient = isDefendantPrint(task) ? caseData.getRespondent1() : caseData.getApplicant1();
            List<String> recipients = List.of(recipient.getPartyName());
            bulkPrintService.printLetter(letterContent, caseData.getLegacyCaseReference(),
                                         caseData.getLegacyCaseReference(), HEARING_PACK_LETTER_TYPE, recipients);
        }
    }

    private boolean checkHearingDocumentAvailable(CaseData caseData) {
        return nonNull(caseData.getSystemGeneratedCaseDocuments())
            && !caseData.getSystemGeneratedCaseDocuments().isEmpty()
            && nonNull(caseData.getHearingDocuments())
            && !caseData.getHearingDocuments().isEmpty();
    }

    private boolean checkWelshHearingDocumentAvailable(CaseData caseData) {
        return nonNull(caseData.getSystemGeneratedCaseDocuments())
            && !caseData.getSystemGeneratedCaseDocuments().isEmpty()
            && nonNull(caseData.getHearingDocumentsWelsh())
            && !caseData.getHearingDocumentsWelsh().isEmpty();
    }

    private boolean isDefendantPrint(String task) {
        return task.equals(TASK_ID_DEFENDANT) || task.equals(TASK_ID_DEFENDANT_HMC);
    }

    private byte[] generateLetterContent(String authorisation, CaseData caseData, String task, CaseDocument... caseDocument) {
        byte[] letterContent;
        Party recipient = isDefendantPrint(task) ? caseData.getRespondent1() : caseData.getApplicant1();
        letterContent = coverLetterAppendService.makeDocumentMailable(caseData, authorisation, recipient, HEARING_NOTICE, caseDocument);
        return letterContent;
    }

    private Language determineLanguageForBulkPrint(CaseData caseData, String taskId, boolean welshDocument) {
        if (!featureToggleService.isGaForWelshEnabled() && !welshDocument) {
            return Language.ENGLISH;
        } else if (!featureToggleService.isGaForWelshEnabled() && welshDocument) {
            return Language.BOTH;
        }

        if (TASK_ID_CLAIMANT.equals(taskId)) {
            if (caseData.getApplicant1DQ() != null
                && caseData.getApplicant1DQ().getApplicant1DQLanguage() != null
                && (caseData.getApplicant1DQ().getApplicant1DQLanguage().getDocuments() != null)) {
                return caseData.getApplicant1DQ().getApplicant1DQLanguage().getDocuments();
            } else {
                return switch (caseData.getClaimantBilingualLanguagePreference()) {
                    case "WELSH" -> Language.WELSH;
                    case "BOTH" -> Language.BOTH;
                    default -> Language.ENGLISH;
                };
            }
        } else {
            if (caseData.getRespondent1DQ() != null
                && caseData.getRespondent1DQ().getRespondent1DQLanguage() != null
                && (caseData.getRespondent1DQ().getRespondent1DQLanguage().getDocuments() != null)) {
                return caseData.getRespondent1DQ().getRespondent1DQLanguage().getDocuments();
            } else {
                return switch (caseData.getDefendantBilingualLanguagePreference()) {
                    case "WELSH" -> Language.WELSH;
                    case "BOTH" -> Language.BOTH;
                    default -> Language.ENGLISH;
                };
            }
        }
    }
}
