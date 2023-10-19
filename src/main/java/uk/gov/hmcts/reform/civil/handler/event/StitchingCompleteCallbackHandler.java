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
import uk.gov.hmcts.reform.civil.model.CaseData;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.asyncStitchingComplete;

@Slf4j
@Service
@RequiredArgsConstructor
public class StitchingCompleteCallbackHandler extends CallbackHandler {

    private static final List<CaseEvent> EVENTS = List.of(asyncStitchingComplete);
    private final ObjectMapper objectMapper;

    @Override
    protected Map<String, Callback> callbacks() {
        return Map.of(
                callbackKey(ABOUT_TO_SUBMIT), this::triggerUpdateBundleCategoryId
        );
    }

    @Override
    public List<CaseEvent> handledEvents() {
        return EVENTS;
    }

    private CallbackResponse triggerUpdateBundleCategoryId(final CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        caseData.getCaseBundles().forEach(bundleIdValue -> bundleIdValue
                .getValue().getStitchedDocument().ifPresent(
                        (document) -> {
                            if (Objects.isNull(document.getCategoryID())) {
                                document.setCategoryID(DocCategory.BUNDLES.getValue());
                            }
                        }
                ));
        return AboutToStartOrSubmitCallbackResponse.builder()
                .data(caseData.toMap(objectMapper))
                .build();
    }
}
