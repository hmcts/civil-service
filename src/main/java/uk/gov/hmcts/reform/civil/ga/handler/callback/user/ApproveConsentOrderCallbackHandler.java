package uk.gov.hmcts.reform.civil.ga.handler.callback.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;
import uk.gov.hmcts.reform.civil.callback.Callback;
import uk.gov.hmcts.reform.civil.callback.CallbackHandler;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.documentmanagement.model.CaseDocument;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.enums.dq.GeneralApplicationTypes;
import uk.gov.hmcts.reform.civil.ga.callback.GeneralApplicationCallbackHandler;
import uk.gov.hmcts.reform.civil.ga.model.GeneralApplicationCaseData;
import uk.gov.hmcts.reform.civil.ga.model.genapplication.GAApproveConsentOrder;
import uk.gov.hmcts.reform.civil.ga.service.docmosis.consentorder.ConsentOrderGenerator;
import uk.gov.hmcts.reform.civil.model.BusinessProcess;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.civil.callback.CallbackParams.Params.BEARER_TOKEN;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_START;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.MID;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.SUBMITTED;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.APPROVE_CONSENT_ORDER;
import static uk.gov.hmcts.reform.civil.enums.dq.GeneralApplicationTypes.STAY_THE_CLAIM;

@Slf4j
@Service
@RequiredArgsConstructor
public class ApproveConsentOrderCallbackHandler extends CallbackHandler implements GeneralApplicationCallbackHandler {

    public static final String ORDER_DATE_IN_PAST = "The date, by which the order to end"
        + " should be given, cannot be in past.";
    private static final List<CaseEvent> EVENTS = Collections.singletonList(APPROVE_CONSENT_ORDER);
    private final ConsentOrderGenerator consentOrderGenerator;
    private final ObjectMapper objectMapper;

    @Override
    protected Map<String, Callback> callbacks() {
        return Map.of(
            callbackKey(ABOUT_TO_START), this::validateApplicationType,
            callbackKey(MID, "populate-consent-order-doc"), this::populateConsentOrder,
            callbackKey(ABOUT_TO_SUBMIT), this::setBusinessProcess,
            callbackKey(SUBMITTED), this::buildConfirmation
        );
    }

    private CallbackResponse validateApplicationType(CallbackParams callbackParams) {
        GeneralApplicationCaseData caseData = callbackParams.getGeneralApplicationCaseData();
        GeneralApplicationCaseData caseDataBuilder = caseData.copy();
        List<GeneralApplicationTypes> validGATypes = List.of(STAY_THE_CLAIM);
        GAApproveConsentOrder gaApproveConsentOrder = new GAApproveConsentOrder();
        if (caseData.getGeneralAppDetailsOfOrder() != null) {
            gaApproveConsentOrder.setConsentOrderDescription(caseData.getGeneralAppDetailsOfOrder());
            gaApproveConsentOrder.setShowConsentOrderDate(YesOrNo.NO);
        }

        if (caseData.getGeneralAppType().getTypes().stream().anyMatch(validGATypes::contains)) {
            gaApproveConsentOrder.setShowConsentOrderDate(YesOrNo.YES);
            gaApproveConsentOrder.setIsOrderProcessedByStayScheduler(YesOrNo.NO);
        }
        List<String> errors = new ArrayList<>();
        caseDataBuilder.approveConsentOrder(gaApproveConsentOrder);
        return AboutToStartOrSubmitCallbackResponse.builder()
            .errors(errors)
            .data(caseDataBuilder.build().toMap(objectMapper))
            .build();
    }

    private CallbackResponse populateConsentOrder(CallbackParams callbackParams) {
        GeneralApplicationCaseData caseData = callbackParams.getGeneralApplicationCaseData();
        List<String> errors = new ArrayList<>();
        GeneralApplicationCaseData caseDataBuilder = caseData.copy();
        if (caseData.getApproveConsentOrder() != null
            && caseData.getApproveConsentOrder().getShowConsentOrderDate() != null
            && caseData.getApproveConsentOrder().getShowConsentOrderDate().equals(YesOrNo.YES)
            && caseData.getApproveConsentOrder().getConsentOrderDateToEnd() != null
            && LocalDate.now().isAfter(caseData.getApproveConsentOrder().getConsentOrderDateToEnd())) {
            errors.add(ORDER_DATE_IN_PAST);
        }

        CaseDocument consentOrderDocument = null;

        consentOrderDocument = consentOrderGenerator.generate(
            caseDataBuilder.build(),
            callbackParams.getParams().get(BEARER_TOKEN).toString()
        );

        caseDataBuilder.consentOrderDocPreview(consentOrderDocument.getDocumentLink());

        return AboutToStartOrSubmitCallbackResponse.builder()
            .errors(errors)
            .data(caseDataBuilder.build().toMap(objectMapper))
            .build();

    }

    private CallbackResponse setBusinessProcess(CallbackParams callbackParams) {
        GeneralApplicationCaseData caseData = callbackParams.getGeneralApplicationCaseData();
        GeneralApplicationCaseData dataBuilder = caseData.copy();
        dataBuilder.businessProcess(BusinessProcess.readyGa(APPROVE_CONSENT_ORDER)).build();
        log.info("Set business process to APPROVE_CONSENT_ORDER for caseId: {}", caseData.getCcdCaseReference());
        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(dataBuilder.build().toMap(objectMapper))
            .build();
    }

    private CallbackResponse buildConfirmation(CallbackParams callbackParams) {

        String confirmationHeader = "# Your order has been made";
        String body = "<br/><br/>";
        return SubmittedCallbackResponse.builder()
            .confirmationHeader(confirmationHeader)
            .confirmationBody(body)
            .build();
    }

    @Override
    public List<CaseEvent> handledEvents() {
        return EVENTS;
    }
}
