package uk.gov.hmcts.reform.civil.ga.handler.callback.camunda.caseevents;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.civil.callback.Callback;
import uk.gov.hmcts.reform.civil.callback.CallbackHandler;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.ga.callback.GeneralApplicationCallbackHandler;
import uk.gov.hmcts.reform.civil.ga.model.GeneralApplicationCaseData;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.model.citizenui.TranslatedDocument;
import uk.gov.hmcts.reform.civil.model.citizenui.TranslatedDocumentType;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.documentmanagement.model.CaseDocument;
import uk.gov.hmcts.reform.civil.documentmanagement.model.Document;
import uk.gov.hmcts.reform.civil.service.CoreCaseDataService;
import uk.gov.hmcts.reform.civil.ga.service.SendFinalOrderPrintService;

import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.civil.callback.CallbackParams.Params.BEARER_TOKEN;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.SEND_TRANSLATED_ORDER_TO_LIP_APPLICANT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.SEND_TRANSLATED_ORDER_TO_LIP_RESPONDENT;
import static uk.gov.hmcts.reform.civil.model.citizenui.TranslatedDocumentType.APPROVE_OR_EDIT_ORDER;
import static uk.gov.hmcts.reform.civil.model.citizenui.TranslatedDocumentType.DISMISSAL_ORDER;
import static uk.gov.hmcts.reform.civil.model.citizenui.TranslatedDocumentType.GENERAL_ORDER;
import static uk.gov.hmcts.reform.civil.model.citizenui.TranslatedDocumentType.HEARING_NOTICE;
import static uk.gov.hmcts.reform.civil.model.citizenui.TranslatedDocumentType.HEARING_ORDER;
import static uk.gov.hmcts.reform.civil.model.citizenui.TranslatedDocumentType.JUDGES_DIRECTIONS_ORDER;
import static uk.gov.hmcts.reform.civil.model.citizenui.TranslatedDocumentType.REQUEST_FOR_MORE_INFORMATION_ORDER;
import static uk.gov.hmcts.reform.civil.model.citizenui.TranslatedDocumentType.WRITTEN_REPRESENTATIONS_ORDER_CONCURRENT;
import static uk.gov.hmcts.reform.civil.model.citizenui.TranslatedDocumentType.WRITTEN_REPRESENTATIONS_ORDER_SEQUENTIAL;

@Service
@RequiredArgsConstructor
public class SendTranslatedOrderToLiPCallbackHandler extends CallbackHandler implements GeneralApplicationCallbackHandler {

    private static final List<CaseEvent> EVENTS = List.of(SEND_TRANSLATED_ORDER_TO_LIP_APPLICANT, SEND_TRANSLATED_ORDER_TO_LIP_RESPONDENT);

    private final CoreCaseDataService coreCaseDataService;
    private final CaseDetailsConverter caseDetailsConverter;
    private final SendFinalOrderPrintService sendFinalOrderPrintService;

    private static final List<String> ENGLISH_TYPES = List.of("ENGLISH", "BOTH");
    private static final List<String> WELSH_TYPES = List.of("WELSH", "BOTH");

    private static final List<TranslatedDocumentType> POST_TRANSLATED_DOCUMENT_TYPES = List.of(
        REQUEST_FOR_MORE_INFORMATION_ORDER,
        HEARING_ORDER,
        GENERAL_ORDER,
        DISMISSAL_ORDER,
        JUDGES_DIRECTIONS_ORDER,
        WRITTEN_REPRESENTATIONS_ORDER_SEQUENTIAL,
        WRITTEN_REPRESENTATIONS_ORDER_CONCURRENT,
        HEARING_NOTICE,
        APPROVE_OR_EDIT_ORDER
    );

    @Value("${print.service.enabled}")
    public boolean printServiceEnabled;

    @Override
    protected Map<String, Callback> callbacks() {
        return Map.of(callbackKey(ABOUT_TO_SUBMIT), this::sendTranslatedOrderLetter);
    }

    @Override
    public List<CaseEvent> handledEvents() {
        return EVENTS;
    }

    private CallbackResponse sendTranslatedOrderLetter(CallbackParams callbackParams) {
        GeneralApplicationCaseData caseData = callbackParams.getGeneralApplicationCaseData();
        CaseEvent caseEvent = CaseEvent.valueOf(callbackParams.getRequest().getEventId());
        if (printServiceEnabled && isDocumentCorrectType(caseData)) {
            CaseDocument originalCaseDocument = caseData.getOriginalDocumentsBulkPrint().get(caseData.getOriginalDocumentsBulkPrint().size() - 1).getValue();
            TranslatedDocument translatedCaseDocument = caseData.getTranslatedDocumentsBulkPrint().get(caseData.getTranslatedDocumentsBulkPrint().size() - 1)
                .getValue();
            GeneralApplicationCaseData parentCaseData = getParentCaseData(caseData);
            Document originalDocument = shouldPrintInLanguage(caseData, parentCaseData, caseEvent, ENGLISH_TYPES) ? originalCaseDocument.getDocumentLink() : null;
            Document translatedDocument = shouldPrintInLanguage(caseData, parentCaseData, caseEvent, WELSH_TYPES) ? translatedCaseDocument.getFile() : null;
            sendFinalOrderPrintService.sendJudgeTranslatedOrderToPrintForLIP(
                callbackParams.getParams().get(BEARER_TOKEN).toString(),
                originalDocument,
                translatedDocument,
                caseData,
                caseEvent);
        }
        return AboutToStartOrSubmitCallbackResponse.builder()
            .build();
    }

    private boolean shouldPrintInLanguage(GeneralApplicationCaseData caseData, GeneralApplicationCaseData parentCaseData, CaseEvent caseEvent, List<String> languageTypes) {
        boolean isClaimant = (caseEvent == SEND_TRANSLATED_ORDER_TO_LIP_APPLICANT && caseData.getParentClaimantIsApplicant() == YesOrNo.YES)
            || (caseEvent == SEND_TRANSLATED_ORDER_TO_LIP_RESPONDENT && caseData.getParentClaimantIsApplicant() == YesOrNo.NO);
        String claimantLanguage = parentCaseData.getClaimantBilingualLanguagePreference() != null ? parentCaseData.getClaimantBilingualLanguagePreference() : "ENGLISH";
        String defendantLanguage = parentCaseData.getDefendantBilingualLanguagePreference() != null ? parentCaseData.getDefendantBilingualLanguagePreference() : "ENGLISH";
        if (isClaimant) {
            return languageTypes.contains(claimantLanguage);
        } else {
            return languageTypes.contains(defendantLanguage);
        }
    }

    protected GeneralApplicationCaseData getParentCaseData(GeneralApplicationCaseData caseData) {
        CaseDetails caseDetails = coreCaseDataService.getCase(Long.parseLong(caseData.getParentCaseReference()));
        return caseDetailsConverter.toGeneralApplicationCaseData(caseDetails);
    }

    private boolean isDocumentCorrectType(GeneralApplicationCaseData caseData) {
        List<Element<TranslatedDocument>> translatedDocuments = caseData.getTranslatedDocumentsBulkPrint();
        if (translatedDocuments == null || translatedDocuments.size() == 0) {
            return false;
        }
        TranslatedDocumentType documentType = translatedDocuments.get(translatedDocuments.size() - 1).getValue().getDocumentType();
        return POST_TRANSLATED_DOCUMENT_TYPES.contains(documentType);
    }
}
