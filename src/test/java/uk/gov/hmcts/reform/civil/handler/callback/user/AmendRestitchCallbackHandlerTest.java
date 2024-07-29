package uk.gov.hmcts.reform.civil.handler.callback.user;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.platform.commons.util.StringUtils;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CallbackType;

import static org.junit.jupiter.api.Assertions.*;

class AmendRestitchCallbackHandlerTest {

    private AmendRestitchCallbackHandler handler = new AmendRestitchCallbackHandler();

    @Test
    void confirmationPage() {
        CallbackParams params = CallbackParams.builder()
            .type(CallbackType.SUBMITTED)
            .build();
        SubmittedCallbackResponse response = (SubmittedCallbackResponse) handler.handle(params);
        Assertions.assertTrue(response.getConfirmationHeader().contains("# The bundle has been restitched"));
        Assertions.assertTrue(response.getConfirmationHeader().contains("## All parties have been notified"));
        Assertions.assertTrue(StringUtils.isNotBlank(response.getConfirmationBody()));
    }
}
