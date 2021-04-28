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
import static uk.gov.hmcts.reform.unspec.callback.CallbackType.MID;
import static uk.gov.hmcts.reform.unspec.callback.CallbackType.SUBMITTED;

@SpringBootTest(classes = {
    AddOrAmendClaimDocumentsCallbackHandler.class,
    JacksonAutoConfiguration.class,
    CaseDetailsConverter.class
})
class AddOrAmendClaimDocumentsCallbackHandlerTest extends BaseCallbackHandlerTest {

    @Autowired
    private AddOrAmendClaimDocumentsCallbackHandler handler;

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
        class SubmittedCallback {

            @Test
            void shouldReturnExpectedSubmittedCallbackResponse_whenInvoked() {
                CaseData caseData = CaseDataBuilder.builder().atStateClaimCreated().build();
                CallbackParams params = callbackParamsOf(caseData, SUBMITTED);
                SubmittedCallbackResponse response = (SubmittedCallbackResponse) handler.handle(params);

                assertThat(response).usingRecursiveComparison().isEqualTo(
                    SubmittedCallbackResponse.builder()
                        .confirmationHeader(format("# Documents uploaded successfully%n## Claim number: 000DC001"))
                        .confirmationBody("<br />")
                        .build());
            }
        }
    }
}
