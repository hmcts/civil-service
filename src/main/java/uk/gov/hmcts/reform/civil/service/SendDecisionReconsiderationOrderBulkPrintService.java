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

import java.util.ArrayList;
import java.util.List;

import static uk.gov.hmcts.reform.civil.documentmanagement.model.DocumentType.DECISION_MADE_ON_APPLICATIONS;
import static uk.gov.hmcts.reform.civil.enums.dq.Language.BOTH;
import static uk.gov.hmcts.reform.civil.enums.dq.Language.ENGLISH;
import static uk.gov.hmcts.reform.civil.enums.dq.Language.WELSH;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.caseevents.SendDroOrderToLipBulkPrintCallbackHandler.TASK_ID_CLAIMANT_DRO;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.caseevents.SendDroOrderToLipBulkPrintCallbackHandler.TASK_ID_DEFENDANT_DRO;

@Service
@RequiredArgsConstructor
@Slf4j
public class SendDecisionReconsiderationOrderBulkPrintService {

    private final BulkPrintService bulkPrintService;
    private final CoverLetterAppendService coverLetterAppendService;
    private static final String DECISION_PACK_LETTER_TYPE = "decision-reconsider-document-pack";
    private final FeatureToggleService featureToggleService;

    public void sendDecisionReconsiderationToLip(String authorisation, CaseData caseData, String task) {
        List<CaseDocument> caseDocuments = new ArrayList<>();
        Language language = determineLanguageForBulkPrint(caseData, task);
        switch (language) {
            case ENGLISH -> caseData.getDecisionReconsiderationDocument().map(Element::getValue).ifPresent(caseDocuments::add);
            case WELSH -> caseData.getTranslatedDecisionReconsiderationDocument().map(Element::getValue).ifPresent(caseDocuments::add);
            case BOTH -> {
                caseData.getDecisionReconsiderationDocument().map(Element::getValue).ifPresent(caseDocuments::add);
                caseData.getTranslatedDecisionReconsiderationDocument().map(Element::getValue).ifPresent(caseDocuments::add);           }
            default -> { }
        }

        if (!caseDocuments.isEmpty()) {

            byte[] letterContent;
            Party recipient = isDefendantPrint(task) ? caseData.getRespondent1() : caseData.getApplicant1();
            letterContent = coverLetterAppendService.makeDocumentMailable(caseData, authorisation, recipient, DECISION_MADE_ON_APPLICATIONS,
                                                          caseDocuments.toArray(new CaseDocument[0]));
            List<String> recipients = List.of(recipient.getPartyName());
            bulkPrintService.printLetter(letterContent, caseData.getLegacyCaseReference(),
                                         caseData.getLegacyCaseReference(), DECISION_PACK_LETTER_TYPE, recipients);
        }
    }

    private boolean isDefendantPrint(String task) {
        return task.equals(TASK_ID_DEFENDANT_DRO);
    }

    private Language determineLanguageForBulkPrint(CaseData caseData, String taskId) {

        if (!featureToggleService.isGaForWelshEnabled()) {
            return Language.ENGLISH;
        }

        if (TASK_ID_CLAIMANT_DRO.equals(taskId)) {
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
        } else {
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
}
