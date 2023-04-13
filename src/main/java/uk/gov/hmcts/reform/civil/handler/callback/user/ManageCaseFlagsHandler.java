package uk.gov.hmcts.reform.civil.handler.callback.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.civil.callback.Callback;
import uk.gov.hmcts.reform.civil.callback.CallbackHandler;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.caseflags.FlagDetail;
import uk.gov.hmcts.reform.civil.model.common.Element;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.MANAGE_CASE_FLAGS;

@Service
@RequiredArgsConstructor
public class ManageCaseFlagsHandler extends CallbackHandler {

    private static final List<CaseEvent> EVENTS =  List.of(MANAGE_CASE_FLAGS);
    private final ObjectMapper objectMapper;

    private Map<String, Callback> callbackMap = Map.of(callbackKey(ABOUT_TO_SUBMIT), this::updateUrgentFlag);

    @Override
    protected Map<String, Callback> callbacks() {
        return callbackMap;
    }

    @Override
    public List<CaseEvent> handledEvents() {
        return EVENTS;
    }

    private CallbackResponse updateUrgentFlag(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        CaseData.CaseDataBuilder updatedData = caseData.toBuilder();
        List<Element<FlagDetail>> urgentFlags = null;
        if (caseData.getCaseFlags() != null) {
            urgentFlags = caseData.getCaseFlags().getDetails().stream()
                .filter(details -> (details.getValue().getFlagCode().equals("CF0007")
                    && details.getValue().getStatus().equals("Active"))).collect(Collectors.toList());
            updatedData.urgentFlag(urgentFlags.isEmpty() ? YesOrNo.NO : YesOrNo.YES);
        } else {
            updatedData.urgentFlag(YesOrNo.NO);
        }

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(updatedData.build().toMap(objectMapper))
            .build();

    }
}
