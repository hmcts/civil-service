package uk.gov.hmcts.reform.unspec.callback;

import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;

public interface Callback {

    CallbackResponse execute(CallbackParams params);
}
