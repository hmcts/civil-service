package uk.gov.hmcts.reform.unspec.handler.callback.user;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;
import uk.gov.hmcts.reform.unspec.callback.CallbackParams;
import uk.gov.hmcts.reform.unspec.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.unspec.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.unspec.model.CaseData;
import uk.gov.hmcts.reform.unspec.model.ServedDocumentFiles;
import uk.gov.hmcts.reform.unspec.sampledata.CaseDataBuilder;

import static java.lang.String.format;
import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.unspec.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.unspec.callback.CallbackType.MID;
import static uk.gov.hmcts.reform.unspec.callback.CallbackType.SUBMITTED;
import static uk.gov.hmcts.reform.unspec.callback.CaseEvent.NOTIFY_DEFENDANT_OF_CLAIM_DETAILS;

@SpringBootTest(classes = {
    NotifyClaimDetailsCallbackHandler.class,
    JacksonAutoConfiguration.class,
    CaseDetailsConverter.class
})
class NotifyClaimDetailsCallbackHandlerTest extends BaseCallbackHandlerTest {

    @Autowired
    private NotifyClaimDetailsCallbackHandler handler;

    @Nested
    class MidEventParticularsOfClaimCallback {

        private final String pageId = "particulars-of-claim";
        private final CaseData.CaseDataBuilder caseDataBuilder =
            CaseDataBuilder.builder().atStateClaimDraft().build().toBuilder();

        @Test
        void shouldReturnErrors_whenNoDocuments() {
            CaseData caseData = caseDataBuilder.build();
            CallbackParams params = callbackParamsOf(caseData, MID, pageId);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getErrors()).containsOnly("You must add Particulars of claim details");
        }

        @Test
        void shouldReturnErrors_whenParticularsOfClaimFieldsAreInErrorState() {
            CaseData caseData = caseDataBuilder.servedDocumentFiles(ServedDocumentFiles.builder().build()).build();
            CallbackParams params = callbackParamsOf(caseData, MID, pageId);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getErrors()).containsOnly("You must add Particulars of claim details");
        }

        @Test
        void shouldReturnNoErrors_whenParticularOfClaimsFieldsAreValid() {
            CaseData caseData = caseDataBuilder.servedDocumentFiles(ServedDocumentFiles.builder()
                                                                        .particularsOfClaimText("Some string")
                                                                        .build()).build();
            CallbackParams params = callbackParamsOf(caseData, MID, pageId);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getErrors()).isEmpty();
        }

        @Nested
        class AboutToSubmit {

            @Test
            void shouldUpdateBusinessProcess_whenInvoked() {
                CaseData caseData = CaseDataBuilder.builder().atStateAwaitingCaseDetailsNotification().build();
                CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);
                var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

                assertThat(response.getData())
                    .extracting("businessProcess")
                    .extracting("camundaEvent", "status")
                    .containsOnly(NOTIFY_DEFENDANT_OF_CLAIM_DETAILS.name(), "READY");
            }
        }

        @Nested
        class SubmittedCallback {

            private static final String CONFIRMATION_SUMMARY = "<br />What happens next\n\n"
                + "The defendant legal representative's organisation has been notified of the claim details.\n\n"
                + "They must respond by %s. Your account will be updated and you will be sent an email.";

            @Test
            void shouldReturnExpectedSubmittedCallbackResponse_whenInvoked() {
                CaseData caseData = CaseDataBuilder.builder().atStateAwaitingCaseDetailsNotification().build();
                CallbackParams params = callbackParamsOf(caseData, SUBMITTED);
                SubmittedCallbackResponse response = (SubmittedCallbackResponse) handler.handle(params);

                assertThat(response).usingRecursiveComparison().isEqualTo(
                    SubmittedCallbackResponse.builder()
                        .confirmationHeader("# Defendant notified")
                        .confirmationBody(format(CONFIRMATION_SUMMARY, "DATE"))
                        .build());
            }
        }
    }
}
