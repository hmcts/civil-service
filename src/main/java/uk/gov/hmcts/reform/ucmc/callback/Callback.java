package uk.gov.hmcts.reform.ucmc.callback;

import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;

public interface Callback {
    CallbackResponse execute(CallbackParams params);
}
