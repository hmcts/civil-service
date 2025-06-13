package uk.gov.hmcts.reform.civil.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.documentmanagement.model.CaseDocument;
import uk.gov.hmcts.reform.civil.documentmanagement.model.DocumentType;
import uk.gov.hmcts.reform.civil.enums.dq.Language;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.service.docmosis.CoverLetterAppendService;
import uk.gov.hmcts.reform.civil.service.documentmanagement.DocumentDownloadService;

import java.util.ArrayList;
import java.util.List;

import static java.util.Objects.nonNull;
import static uk.gov.hmcts.reform.civil.documentmanagement.model.DocumentType.DECISION_MADE_ON_APPLICATIONS;
import static uk.gov.hmcts.reform.civil.documentmanagement.model.DocumentType.HEARING_NOTICE;
import static uk.gov.hmcts.reform.civil.enums.dq.Language.WELSH;
import static uk.gov.hmcts.reform.civil.enums.dq.Language.ENGLISH;
import static uk.gov.hmcts.reform.civil.enums.dq.Language.BOTH;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.caseevents.SendDroOrderToLipBulkPrintCallbackHandler.TASK_ID_CLAIMANT_DRO;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.caseevents.SendDroOrderToLipBulkPrintCallbackHandler.TASK_ID_DEFENDANT_DRO;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.caseevents.SendHearingToLiPCallbackHandler.TASK_ID_CLAIMANT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.caseevents.SendHearingToLiPCallbackHandler.TASK_ID_CLAIMANT_HMC;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.caseevents.SendHearingToLiPCallbackHandler.TASK_ID_DEFENDANT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.caseevents.SendHearingToLiPCallbackHandler.TASK_ID_DEFENDANT_HMC;

@Service
@RequiredArgsConstructor
@Slf4j
public class SendHearingBulkPrintService {

    private static final String DECISION_PACK_LETTER_TYPE = "decision-reconsider-document-pack";
    private final BulkPrintService bulkPrintService;
    private final DocumentDownloadService documentDownloadService;
    private final CoverLetterAppendService coverLetterAppendService;
    private static final String HEARING_PACK_LETTER_TYPE = "hearing-document-pack";
    private final FeatureToggleService featureToggleService;

    public void sendHearingToLIP(String authorisation, CaseData caseData, String task, boolean welshDocument) {
        List<CaseDocument> caseDocuments = new ArrayList<>();
        Language language = determineLanguageForBulkPrint(caseData, task, welshDocument);
        if (checkHearingDocumentAvailable(caseData) || checkWelshHearingDocumentAvailable(caseData)) {
            switch (language) {
                case ENGLISH -> caseDocuments.add(caseData.getHearingDocuments().get(0).getValue());
                case WELSH -> {
                    caseDocuments.add(caseData.getHearingDocumentsWelsh().get(0).getValue());
                }
                case BOTH -> {
                    caseDocuments.add(caseData.getHearingDocuments().get(0).getValue());
                    caseDocuments.add(caseData.getHearingDocumentsWelsh().get(0).getValue());            }
                default -> {
                }
            }
        }
        printLetterService(authorisation, caseData, task, caseDocuments, HEARING_NOTICE, HEARING_PACK_LETTER_TYPE);
    }

    public void sendDecisionReconsiderationToLip(String authorisation, CaseData caseData, String task) {
        List<CaseDocument> caseDocuments = new ArrayList<>();
        Language language = determineLanguageForBulkPrint(caseData, task, true);
        switch (language) {
            case ENGLISH -> caseData.getDecisionReconsiderationDocument().map(Element::getValue).ifPresent(caseDocuments::add);
            case WELSH -> caseData.getTranslatedDecisionReconsiderationDocument().map(Element::getValue).ifPresent(caseDocuments::add);
            case BOTH -> {
                caseData.getDecisionReconsiderationDocument().map(Element::getValue).ifPresent(caseDocuments::add);
                caseData.getTranslatedDecisionReconsiderationDocument().map(Element::getValue).ifPresent(caseDocuments::add);
            }
            default -> { }
        }
        printLetterService(authorisation, caseData, task, caseDocuments, DECISION_MADE_ON_APPLICATIONS, DECISION_PACK_LETTER_TYPE);
    }

    public void printLetterService(String authorisation, CaseData caseData, String task, List<CaseDocument> caseDocuments, DocumentType documentType, String letterType) {
        if (!caseDocuments.isEmpty()) {
            byte[] letterContent;
            Party recipient = isDefendantPrint(task) ? caseData.getRespondent1() : caseData.getApplicant1();
            letterContent = coverLetterAppendService.makeDocumentMailable(caseData, authorisation, recipient, documentType,
                                                                          caseDocuments.toArray(new CaseDocument[0]));
            List<String> recipients = List.of(recipient.getPartyName());
            bulkPrintService.printLetter(letterContent, caseData.getLegacyCaseReference(),
                                         caseData.getLegacyCaseReference(), letterType, recipients);

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
        return task.equals(TASK_ID_DEFENDANT) || task.equals(TASK_ID_DEFENDANT_HMC) || task.equals(TASK_ID_DEFENDANT_DRO);
    }

    private Language determineLanguageForBulkPrint(CaseData caseData, String taskId, boolean welshDocument) {
        //TODO: refactor this method when Welsh feature goes live
        if (!featureToggleService.isGaForWelshEnabled() && !welshDocument) {
            return ENGLISH;
        } else if (!featureToggleService.isGaForWelshEnabled() && welshDocument) {
            return BOTH;
        }

        if (TASK_ID_CLAIMANT.equals(taskId)
            || TASK_ID_CLAIMANT_HMC.equals(taskId)
            || TASK_ID_CLAIMANT_DRO.equals(taskId)) {
            return getClaimantLanguagePreference(caseData);
        } else {
            return getDefendantLanguagePreference(caseData);
        }
    }

    public Language getClaimantLanguagePreference(CaseData caseData) {

        if (caseData.getApplicant1DQ() != null
            && caseData.getApplicant1DQ().getApplicant1DQLanguage() != null
            && (caseData.getApplicant1DQ().getApplicant1DQLanguage().getDocuments() != null)) {
            return caseData.getApplicant1DQ().getApplicant1DQLanguage().getDocuments();
        } else {
            return switch (caseData.getClaimantBilingualLanguagePreference()) {
                case "WELSH" -> WELSH;
                case "BOTH" -> BOTH;
                default -> ENGLISH;
            };
        }
    }

    public Language getDefendantLanguagePreference(CaseData caseData) {

        if (caseData.getRespondent1DQ() != null
            && caseData.getRespondent1DQ().getRespondent1DQLanguage() != null
            && (caseData.getRespondent1DQ().getRespondent1DQLanguage().getDocuments() != null)) {
            return caseData.getRespondent1DQ().getRespondent1DQLanguage().getDocuments();
        } else {
            return switch (caseData.getDefendantBilingualLanguagePreference()) {
                case "WELSH" -> WELSH;
                case "BOTH" -> BOTH;
                default -> ENGLISH;
            };
        }
    }
}
