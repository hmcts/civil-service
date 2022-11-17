package uk.gov.hmcts.reform.civil.handler.callback.user;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.client.CaseAssignmentApi;
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.launchdarkly.FeatureToggleService;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CallbackParams.Params.BEARER_TOKEN;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.SUBMITTED;

@SpringBootTest(classes = {
    NoticeOfChangeRequestCallbackHandler.class,
    JacksonAutoConfiguration.class
})
public class NoticeOfChangeRequestCallbackHandlerTest extends BaseCallbackHandlerTest {

    @MockBean
    private FeatureToggleService featureToggleService;

    @Autowired
    private NoticeOfChangeRequestCallbackHandler handler;

    @MockBean
    private CaseAssignmentApi caseAssignmentApi;

    @Autowired
    private AuthTokenGenerator authTokenGenerator;

    @Nested
    class SubmittedCallback {

        @Test
        void shouldReturnExpectedSubmittedCallbackResponse_whenInvoked() {
            CaseData caseData = CaseDataBuilder.builder().atStateClaimIssued().build();
            CallbackParams params = callbackParamsOf(caseData, SUBMITTED);

            when(caseAssignmentApi.checkNocApproval(params.getParams().get(BEARER_TOKEN).toString(),
                                                    authTokenGenerator.generate(),
                                                    params.getRequest())).thenReturn(SubmittedCallbackResponse.builder()
                                                                                                     .build());
            SubmittedCallbackResponse response = (SubmittedCallbackResponse) handler.handle(params);
            verify(caseAssignmentApi).checkNocApproval(params.getParams().get(BEARER_TOKEN).toString(),
                                                       authTokenGenerator.generate(),
                                                       params.getRequest());
        }
    }
}
