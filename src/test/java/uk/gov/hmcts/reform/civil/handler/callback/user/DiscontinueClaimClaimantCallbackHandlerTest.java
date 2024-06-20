package uk.gov.hmcts.reform.civil.handler.callback.user;

import org.junit.jupiter.api.Nested;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(classes = {
    DiscontinueClaimClaimantCallbackHandler.class,
})
class DiscontinueClaimClaimantCallbackHandlerTest {

    /*@Autowired
    private DiscontinueClaimClaimantCallbackHandler handler;*/

    @Nested
    class AboutToStartCallback {

        /*@Test
        void shouldReturnNoError_WhenAboutToStartIsInvoked() {
            CaseData caseData = CaseDataBuilder.builder().atStateClaimIssued().build();
            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_START, caseData).build();

            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                .handle(params);

            assertThat(response.getErrors()).isNull();
        }*/
    }
}
