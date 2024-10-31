package uk.gov.hmcts.reform.civil.handler.callback.user.respondtoclaimspeccallbackhandler;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.enums.RespondentResponseTypeSpec;
import uk.gov.hmcts.reform.civil.handler.callback.user.task.CaseTask;
import uk.gov.hmcts.reform.civil.model.CaseData;

import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;

@Component
@RequiredArgsConstructor
@Slf4j
public class HandleRespondentResponseTypeForSpec implements CaseTask {

    private final ObjectMapper objectMapper;

    public CallbackResponse execute(CallbackParams callbackParams) {
        CaseData caseData = callbackParams.getCaseData();
        Long caseId = caseData.getCcdCaseReference();

        log.info("Executing HandleRespondentResponseTypeForSpec for caseId: {}", caseId);

        if (caseData.getRespondent1ClaimResponseTypeForSpec() != RespondentResponseTypeSpec.FULL_ADMISSION
            || caseData.getRespondent2ClaimResponseTypeForSpec() != RespondentResponseTypeSpec.FULL_ADMISSION) {
            log.info("Setting specDefenceFullAdmittedRequired to NO for caseId: {}", caseId);
            caseData = caseData.toBuilder().specDefenceFullAdmittedRequired(NO).build();
        }

        log.info("Completed HandleRespondentResponseTypeForSpec for caseId: {}", caseId);

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseData.toMap(objectMapper))
            .build();
    }
}
