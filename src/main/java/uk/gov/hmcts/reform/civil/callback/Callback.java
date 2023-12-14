package uk.gov.hmcts.reform.civil.callback;

import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;

public interface Callback {

    CallbackResponse execute(CallbackParams params);
}
