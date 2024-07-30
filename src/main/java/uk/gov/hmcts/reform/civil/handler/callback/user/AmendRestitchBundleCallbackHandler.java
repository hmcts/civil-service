package uk.gov.hmcts.reform.civil.handler.callback.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;
import uk.gov.hmcts.reform.civil.callback.Callback;
import uk.gov.hmcts.reform.civil.callback.CallbackHandler;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.handler.event.BundleCreationTriggerEventHandler;
import uk.gov.hmcts.reform.civil.model.BusinessProcess;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;
import uk.gov.hmcts.reform.civil.service.bundle.BundleCreationService;

import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_START;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.MID;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.SUBMITTED;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.AMEND_RESTITCH_BUNDLE;

@Service
@RequiredArgsConstructor
public class AmendRestitchBundleCallbackHandler extends CallbackHandler {

    private static final List<CaseEvent> EVENTS = List.of(AMEND_RESTITCH_BUNDLE);

    private final ObjectMapper mapper;
    private final BundleCreationService bundleCreationService;
    private final BundleCreationTriggerEventHandler bundleCreationEventHandler;

    private final FeatureToggleService featureToggleService;

    @Override
    protected Map<String, Callback> callbacks() {
        return featureToggleService.isCaseEventsEnabled()
            ? Map.of(
            callbackKey(ABOUT_TO_START), this::emptyCallbackResponse,
            callbackKey(MID, "create-bundle"), this::startBundleCreation,
            callbackKey(ABOUT_TO_SUBMIT), this::amendRestitchBundle,
            callbackKey(SUBMITTED), this::buildConfirmation
            )
            : Map.of(
            callbackKey(ABOUT_TO_START), this::emptyCallbackResponse,
            callbackKey(MID, "create-bundle"), this::emptyCallbackResponse,
            callbackKey(ABOUT_TO_SUBMIT), this::emptyCallbackResponse,
            callbackKey(SUBMITTED), this::emptyCallbackResponse
        );
    }

    @Override
    public List<CaseEvent> handledEvents() {
        return EVENTS;
    }

    private CallbackResponse startBundleCreation(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        CaseData.CaseDataBuilder<?, ?> caseDataBuilder = caseData.toBuilder();

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseDataBuilder.build().toMap(mapper))
            .build();
    }

    private SubmittedCallbackResponse buildConfirmation(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();

        return SubmittedCallbackResponse.builder()
            .confirmationHeader("Placeholder")
            .confirmationBody("Placeholder")
            .build();
    }

    private CallbackResponse amendRestitchBundle(CallbackParams callbackParams) {

        CaseData caseData = callbackParams.getCaseData();
        CaseData.CaseDataBuilder dataBuilder = caseData.toBuilder();

        dataBuilder.businessProcess(BusinessProcess.ready(AMEND_RESTITCH_BUNDLE));

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(dataBuilder.build().toMap(mapper))
            .build();
    }
}
