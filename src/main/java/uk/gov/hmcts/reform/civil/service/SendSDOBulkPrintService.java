package uk.gov.hmcts.reform.civil.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.documentmanagement.model.CaseDocument;
import uk.gov.hmcts.reform.civil.enums.dq.Language;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.service.docmosis.sdo.SdoCoverLetterAppendService;
import uk.gov.hmcts.reform.civil.model.docmosis.common.Party;

import java.util.ArrayList;
import java.util.List;

import static uk.gov.hmcts.reform.civil.documentmanagement.model.DocumentType.SDO_ORDER;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.caseevents.SendSDOToLiPDefendantCallbackHandler.TASK_ID_CLAIMANT;
import static uk.gov.hmcts.reform.civil.handler.callback.camunda.caseevents.SendSDOToLiPDefendantCallbackHandler.TASK_ID_DEFENDANT;

@Service
@RequiredArgsConstructor
@Slf4j
public class SendSDOBulkPrintService {

    private final BulkPrintService bulkPrintService;
    private final SdoCoverLetterAppendService sdoCoverLetterAppendService;
    private final FeatureToggleService featureToggleService;
    private static final String SDO_ORDER_PACK_LETTER_TYPE = "sdo-order-pack";

    public void sendSDOOrderToLIP(String authorisation, CaseData caseData, String taskId) {
        if (caseData.getSystemGeneratedCaseDocuments() != null && !caseData.getSystemGeneratedCaseDocuments().isEmpty()) {
            Language language = determineLanguageForBulkPrint(caseData, taskId);
            List<CaseDocument> caseDocuments = new ArrayList<>();
            switch (language) {
                case ENGLISH -> caseData.getSDODocument().map(Element::getValue).ifPresent(caseDocuments::add);
                case WELSH -> caseData.getTranslatedSDODocument().map(Element::getValue).ifPresent(caseDocuments::add);
                case BOTH -> {
                    caseData.getSDODocument().map(Element::getValue).ifPresent(caseDocuments::add);
                    caseData.getTranslatedSDODocument().map(Element::getValue).ifPresent(caseDocuments::add);
                }
                default -> { }
            }

            if (!caseDocuments.isEmpty()) {
                byte[] letterContent;
                Party recipientDetails = getPartyDetails(taskId, caseData);

                letterContent = sdoCoverLetterAppendService.makeSdoDocumentMailable(caseData, authorisation, recipientDetails, SDO_ORDER,
                                                                                    caseDocuments.toArray(new CaseDocument[0]));

                List<String> recipients = getRecipientsList(caseData, taskId);
                bulkPrintService.printLetter(letterContent, caseData.getLegacyCaseReference(),
                                             caseData.getLegacyCaseReference(), SDO_ORDER_PACK_LETTER_TYPE, recipients);
            }
        }
    }

    private Language determineLanguageForBulkPrint(CaseData caseData, String taskId) {
        if (!featureToggleService.isGaForWelshEnabled()) {
            return Language.ENGLISH;
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

    private List<String> getRecipientsList(CaseData caseData, String taskId) {
        return TASK_ID_DEFENDANT.equals(taskId) ? List.of(caseData.getRespondent1().getPartyName())
            : List.of(caseData.getApplicant1().getPartyName());
    }

    private Party getPartyDetails(String taskId, CaseData caseData) {
        return TASK_ID_DEFENDANT.equals(taskId) ? getPartyDetails(caseData.getRespondent1()) : getPartyDetails(caseData.getApplicant1());
    }

    private Party getPartyDetails(uk.gov.hmcts.reform.civil.model.Party party) {
        return Party.builder()
            .name(party.getPartyName())
            .primaryAddress(party.getPrimaryAddress())
            .build();
    }
}
