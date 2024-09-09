package uk.gov.hmcts.reform.civil.handler.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.civil.callback.Callback;
import uk.gov.hmcts.reform.civil.callback.CallbackHandler;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.enums.DocCategory;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.Bundle;
import uk.gov.hmcts.reform.civil.model.BusinessProcess;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.IdValue;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_START;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.SUBMITTED;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.AMEND_RESTITCH_BUNDLE;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.BUNDLE_CREATION_NOTIFICATION;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.asyncStitchingComplete;

@Slf4j
@Service
@RequiredArgsConstructor
public class StitchingCompleteCallbackHandler extends CallbackHandler {

    private static final List<CaseEvent> EVENTS = List.of(asyncStitchingComplete);
    private static final String BUNDLE_CREATED_NOTIFICATION_EVENT = "BUNDLE_CREATED_NOTIFICATION";
    private static final String AMEND_RESTITCH_BUNDLE_EVENT = "AMEND_RESTITCH_BUNDLE";

    private final ObjectMapper objectMapper;

    private final FeatureToggleService featureToggleService;

    @Override
    protected Map<String, Callback> callbacks() {
        return Map.of(
            callbackKey(ABOUT_TO_START), this::emptyCallbackResponse,
            callbackKey(ABOUT_TO_SUBMIT), this::triggerUpdateBundleCategoryId,
            callbackKey(SUBMITTED), this::emptySubmittedCallbackResponse
        );
    }

    @Override
    public List<CaseEvent> handledEvents() {
        return EVENTS;
    }

    private CallbackResponse triggerUpdateBundleCategoryId(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        caseData.getCaseBundles().forEach(bundleIdValue -> bundleIdValue.getValue().getStitchedDocument().ifPresent(
            document -> {
                if (Objects.isNull(document.getCategoryID())) {
                    document.setCategoryID(DocCategory.BUNDLES.getValue());
                }
            }
        ));

        if (featureToggleService.isCaseEventsEnabled()) {
            YesOrNo hasBundleErrors = getLatestBundle(caseData)
                .map(bundle -> "FAILED".equalsIgnoreCase(bundle.getStitchStatus().orElse(null)) ? YesOrNo.YES : null)
                .orElse(null);
            caseData = caseData.toBuilder().bundleError(hasBundleErrors).build();

            String bundleEvent = caseData.getBundleEvent();
            if (hasBundleErrors == null && bundleEvent != null && (BUNDLE_CREATED_NOTIFICATION_EVENT.equals(bundleEvent) || AMEND_RESTITCH_BUNDLE_EVENT.equals(bundleEvent))) {
                CaseEvent processEvent = BUNDLE_CREATED_NOTIFICATION_EVENT.equals(bundleEvent) ? BUNDLE_CREATION_NOTIFICATION : AMEND_RESTITCH_BUNDLE;
                caseData = caseData.toBuilder().businessProcess(BusinessProcess.ready(processEvent)).build();
            }
        }

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseData.toMap(objectMapper))
            .build();
    }

    public static Optional<Bundle> getLatestBundle(CaseData caseData) {
        return caseData.getCaseBundles().stream()
            .map(IdValue::getValue)
            .max(Comparator.comparing(bundle -> bundle.getCreatedOn().orElse(null)));
    }
}
