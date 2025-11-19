package uk.gov.hmcts.reform.civil.ga.handler.callback.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import uk.gov.hmcts.reform.civil.callback.CallbackHandler;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.ga.callback.GeneralApplicationCallbackHandler;
import uk.gov.hmcts.reform.civil.ga.service.GaPaymentRequestUpdateCallbackService;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.ga.service.HwfNotificationService;

import java.util.List;

abstract class HWFCallbackHandlerBase extends CallbackHandler  implements GeneralApplicationCallbackHandler {

    protected final ObjectMapper objectMapper;
    protected final List<CaseEvent> events;
    protected final GaPaymentRequestUpdateCallbackService paymentRequestUpdateCallbackService;
    protected final HwfNotificationService hwfNotificationService;
    protected final FeatureToggleService featureToggleService;

    public HWFCallbackHandlerBase(ObjectMapper objectMapper,
                                  List<CaseEvent> events,
                                  GaPaymentRequestUpdateCallbackService paymentRequestUpdateCallbackService,
                                  HwfNotificationService hwfNotificationService, FeatureToggleService featureToggleService) {
        this.objectMapper = objectMapper;
        this.events = events;
        this.paymentRequestUpdateCallbackService = paymentRequestUpdateCallbackService;
        this.hwfNotificationService = hwfNotificationService;
        this.featureToggleService = featureToggleService;
    }

    public HWFCallbackHandlerBase(ObjectMapper objectMapper,
                                  List<CaseEvent> events) {
        this.objectMapper = objectMapper;
        this.events = events;
        this.paymentRequestUpdateCallbackService = null;
        this.hwfNotificationService = null;
        this.featureToggleService = null;
    }

    @Override
    public List<CaseEvent> handledEvents() {
        return events;
    }

}
