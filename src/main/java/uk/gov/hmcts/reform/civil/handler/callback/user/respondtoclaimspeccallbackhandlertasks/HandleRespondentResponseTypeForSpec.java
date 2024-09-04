package uk.gov.hmcts.reform.civil.handler.callback.user.respondtoclaimspeccallbackhandlertasks;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.CoreCaseUserService;
import uk.gov.hmcts.reform.civil.service.UserService;

import static uk.gov.hmcts.reform.civil.enums.RespondentResponseTypeSpec.FULL_ADMISSION;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;

@Component
@RequiredArgsConstructor
public class HandleRespondentResponseTypeForSpec {

    private final ObjectMapper objectMapper;
    private final UserService userService;
    private final CoreCaseUserService coreCaseUserService;

    public CallbackResponse handleRespondentResponseTypeForSpec(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        if (caseData.getRespondent1ClaimResponseTypeForSpec() != FULL_ADMISSION
            || caseData.getRespondent2ClaimResponseTypeForSpec() != FULL_ADMISSION) {
            caseData = caseData.toBuilder().specDefenceFullAdmittedRequired(NO).build();
        }
        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseData.toMap(objectMapper))
            .build();
    }
}
