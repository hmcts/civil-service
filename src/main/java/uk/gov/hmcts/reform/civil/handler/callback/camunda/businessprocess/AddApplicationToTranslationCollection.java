package uk.gov.hmcts.reform.civil.handler.callback.camunda.businessprocess;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.civil.callback.Callback;
import uk.gov.hmcts.reform.civil.callback.CallbackHandler;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.ga.model.GeneralApplicationCaseData;
import uk.gov.hmcts.reform.civil.service.ParentCaseUpdateHelper;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.ADD_APPLICATION_TO_TRANSLATION_COLLECTION;

@Service
@RequiredArgsConstructor
public class AddApplicationToTranslationCollection extends CallbackHandler {

    private static final List<CaseEvent> EVENTS = Collections.singletonList(ADD_APPLICATION_TO_TRANSLATION_COLLECTION);
    private final ParentCaseUpdateHelper parentCaseUpdateHelper;

    @Override
    protected Map<String, Callback> callbacks() {
        return Map.of(

            callbackKey(ABOUT_TO_SUBMIT), this::addApplicationToTranslationCollection
        );
    }

    @Override
    public List<CaseEvent> handledEvents() {
        return EVENTS;
    }

    private CallbackResponse addApplicationToTranslationCollection(CallbackParams callbackParams) {
        GeneralApplicationCaseData gaCaseData = callbackParams.getGaCaseData();
        Objects.requireNonNull(gaCaseData, "gaCaseData must be present on CallbackParams");
        parentCaseUpdateHelper.updateCollectionForWelshApplication(gaCaseData);
        return AboutToStartOrSubmitCallbackResponse.builder()
            .build();
    }
}
