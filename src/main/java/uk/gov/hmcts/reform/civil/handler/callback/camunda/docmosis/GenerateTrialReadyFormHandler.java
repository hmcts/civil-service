package uk.gov.hmcts.reform.civil.handler.callback.camunda.docmosis;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.civil.callback.Callback;
import uk.gov.hmcts.reform.civil.callback.CallbackHandler;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.documentmanagement.model.CaseDocument;
import uk.gov.hmcts.reform.civil.enums.CaseRole;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.CoreCaseUserService;
import uk.gov.hmcts.reform.civil.service.UserService;
import uk.gov.hmcts.reform.civil.service.docmosis.trialready.TrialReadyFormGenerator;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;

import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.civil.callback.CallbackParams.Params.BEARER_TOKEN;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.GENERATE_TRIAL_READY_FORM_APPLICANT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.GENERATE_TRIAL_READY_FORM_RESPONDENT1;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.GENERATE_TRIAL_READY_FORM_RESPONDENT2;
import static uk.gov.hmcts.reform.civil.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.civil.utils.NotificationUtils.isRespondent1;

@Service
@RequiredArgsConstructor
public class GenerateTrialReadyFormHandler extends CallbackHandler {

    private static final List<CaseEvent> EVENTS = List.of(
        GENERATE_TRIAL_READY_FORM_APPLICANT,
        GENERATE_TRIAL_READY_FORM_RESPONDENT1,
        GENERATE_TRIAL_READY_FORM_RESPONDENT2
    );
    private static final String TASK_ID_APPLICANT = "GenerateTrialReadyFormApplicant";
    private static final String TASK_ID_RESPONDENT1 = "GenerateTrialReadyFormRespondent1";
    private static final String TASK_ID_RESPONDENT2 = "GenerateTrialReadyFormRespondent2";

    private final TrialReadyFormGenerator trialReadyFormGenerator;

    private final ObjectMapper objectMapper;
    private final UserService userService;
    private final CoreCaseUserService coreCaseUserService;

    @Override
    protected Map<String, Callback> callbacks() {
        return Map.of(callbackKey(ABOUT_TO_SUBMIT), this::generateClaimForm);
    }

    @Override
    public List<CaseEvent> handledEvents() {
        return EVENTS;
    }

    @Override
    public String camundaActivityId(CallbackParams callbackParams) {
        if (isApplicant(callbackParams, GENERATE_TRIAL_READY_FORM_APPLICANT)) {
            return TASK_ID_APPLICANT;
        } else if (isRespondent1(callbackParams, GENERATE_TRIAL_READY_FORM_RESPONDENT1)) {
            return TASK_ID_RESPONDENT1;
        } else {
            return TASK_ID_RESPONDENT2;
        }
    }

    private CallbackResponse generateClaimForm(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        CaseData.CaseDataBuilder<?, ?> caseDataBuilder = caseData.toBuilder();

        buildDocument(callbackParams, caseDataBuilder, caseData);

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseDataBuilder.build().toMap(objectMapper))
            .build();
    }

    private void buildDocument(CallbackParams callbackParams, CaseData.CaseDataBuilder<?, ?> caseDataBuilder,
                               CaseData caseData) {
        CaseDocument caseDocument = trialReadyFormGenerator.generate(
            callbackParams.getCaseData(),
            callbackParams.getParams().get(BEARER_TOKEN).toString(),
            camundaActivityId(callbackParams),
            getUserRole(callbackParams)
        );
        var documents = caseData.getTrialReadyDocuments();
        documents.add(element(caseDocument));
        caseDataBuilder.trialReadyDocuments(documents);
    }

    private CaseRole getUserRole(CallbackParams callbackParams) {
        UserInfo userInfo = userService.getUserInfo(callbackParams.getParams().get(BEARER_TOKEN).toString());
        String ccdCaseReference = callbackParams.getCaseData().getCcdCaseReference().toString();
        for (CaseRole role : CaseRole.values()) {
            if (coreCaseUserService.userHasCaseRole(ccdCaseReference, userInfo.getUid(), role)) {
                return role;
            }
        }
        return null;
    }

    private boolean isApplicant(CallbackParams callbackParams, CaseEvent matchEvent) {
        CaseEvent caseEvent = CaseEvent.valueOf(callbackParams.getRequest().getEventId());
        return caseEvent.equals(matchEvent);
    }

}
