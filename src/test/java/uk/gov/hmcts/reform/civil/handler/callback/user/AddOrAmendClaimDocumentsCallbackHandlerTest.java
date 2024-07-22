package uk.gov.hmcts.reform.civil.handler.callback.user;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.documentmanagement.model.Document;
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.ServedDocumentFiles;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.ExitSurveyContentService;

import static java.lang.String.format;
import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.MID;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.SUBMITTED;
import static uk.gov.hmcts.reform.civil.utils.ElementUtils.wrapElements;

@ExtendWith(MockitoExtension.class)
class AddOrAmendClaimDocumentsCallbackHandlerTest extends BaseCallbackHandlerTest {

    @InjectMocks
    private AddOrAmendClaimDocumentsCallbackHandler handler;

    @Mock
    private ExitSurveyContentService exitSurveyContentService;

    private static final String PAGE_ID = "particulars-of-claim";
    private final CaseData.CaseDataBuilder<?, ?> caseDataBuilder = CaseDataBuilder.builder()
        .atStateClaimDraft()
        .build()
        .toBuilder();

    @Nested
    class MidEventParticularsOfClaimCallback {

        @Test
        void shouldNotReturnErrors_whenNoDocuments() {
            CaseData caseData = caseDataBuilder.build();
            CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);

            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getErrors()).isEmpty();
        }

        @Test
        void shouldNotReturnErrors_whenParticularsOfClaimFieldsAreEmpty() {
            CaseData caseData = caseDataBuilder
                .servedDocumentFiles(ServedDocumentFiles.builder().build())
                .build();
            CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);

            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getErrors()).isEmpty();
        }

        @Test
        void shouldReturnNoErrors_whenParticularOfClaimsFieldsAreValid() {
            CaseData caseData = caseDataBuilder
                .servedDocumentFiles(ServedDocumentFiles.builder()
                                         .particularsOfClaimText("Some string")
                                         .build())
                .build();
            CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);

            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getErrors()).isEmpty();
        }

        @Test
        void shouldReturnError_whenParticularOfClaimsTextAndDocumentSubmitted() {
            CaseData caseData = caseDataBuilder
                .servedDocumentFiles(ServedDocumentFiles.builder()
                                         .particularsOfClaimText("Some string")
                                         .particularsOfClaimDocument(wrapElements(Document.builder().build()))
                                         .build())
                .build();
            CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);

            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getErrors()).containsOnly(
                "You need to either upload 1 Particulars of claim only or enter the "
                    + "Particulars of claim text in the field provided. You cannot do both."
            );
        }

        @Test
        void shouldReturnNoErrors_whenOnlyParticularOfClaimsTextSubmitted() {
            CaseData caseData = caseDataBuilder
                .servedDocumentFiles(ServedDocumentFiles.builder()
                                         .particularsOfClaimText("Some string")
                                         .build())
                .build();
            CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);

            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getErrors()).isEmpty();
        }
    }

    @Nested
    class SubmittedCallback {

        @Test
        void shouldReturnExpectedSubmittedCallbackResponse_whenInvoked() {
            CaseData caseData = CaseDataBuilder.builder()
                .atStateClaimDetailsNotified()
                .build();
            CallbackParams params = callbackParamsOf(caseData, SUBMITTED);

            SubmittedCallbackResponse response = (SubmittedCallbackResponse) handler.handle(params);

            SubmittedCallbackResponse expectedResponse = SubmittedCallbackResponse.builder()
                .confirmationHeader(format("# Documents uploaded successfully%n## Claim number: 000DC001"))
                .confirmationBody(exitSurveyContentService.applicantSurvey())
                .build();

            assertThat(response).usingRecursiveComparison().isEqualTo(expectedResponse);
        }
    }
}
