package uk.gov.hmcts.reform.civil.handler.callback.user.task;

import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.civil.callback.Callback;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;

public interface CaseTask extends Callback {

    CallbackResponse execute(CallbackParams callbackParams);

}
