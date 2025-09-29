package uk.gov.hmcts.reform.civil.handler.callback.camunda.noticeofchange;

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
import uk.gov.hmcts.reform.civil.enums.CaseRole;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.citizenui.CaseDataLiP;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.RESET_LANGUAGE_PREFERENCE;

@Slf4j
@Service
@RequiredArgsConstructor
public class ResetLanguagePreferenceCallbackHandler extends CallbackHandler {

    private static final List<CaseEvent> EVENTS = Collections.singletonList(RESET_LANGUAGE_PREFERENCE);

    public static final String TASK_ID = "ResetLanguagePreferenceAfterNoC";

    private final ObjectMapper objectMapper;

    @Override
    protected Map<String, Callback> callbacks() {
        return Map.of(
            callbackKey(ABOUT_TO_SUBMIT), this::resetLanguagePreference
        );
    }

    @Override
    public String camundaActivityId(CallbackParams callbackParams) {
        return TASK_ID;
    }

    @Override
    public List<CaseEvent> handledEvents() {
        return EVENTS;
    }

    private CallbackResponse resetLanguagePreference(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        CaseData.CaseDataBuilder<?, ?> caseDataBuilder = caseData.toBuilder();

        String caseRole = caseData.getChangeOfRepresentation().getCaseRole();
        if (CaseRole.APPLICANTSOLICITORONE.getFormattedName().equals(caseRole) && caseData.isClaimantBilingual()) {
            caseDataBuilder
                .claimantBilingualLanguagePreference(null)
                .claimantLanguagePreferenceDisplay(null);
        } else if (CaseRole.RESPONDENTSOLICITORONE.getFormattedName().equals(caseRole) && caseData.isRespondentResponseBilingual()) {
            CaseDataLiP caseDataLiP = caseData.getCaseDataLiP();
            caseDataBuilder
                .caseDataLiP(caseDataLiP.toBuilder()
                                 .respondent1LiPResponse(
                                     caseDataLiP.getRespondent1LiPResponse().toBuilder().respondent1ResponseLanguage(null).build()
                                 ).build())
                .defendantLanguagePreferenceDisplay(null);
        }
        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseDataBuilder.build().toMap(objectMapper))
            .build();
    }
}
