package uk.gov.hmcts.reform.civil.handler.callback.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;
import uk.gov.hmcts.reform.civil.callback.CallbackHandler;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.caseprogression.UploadEvidenceExpert;
import uk.gov.hmcts.reform.civil.model.caseprogression.UploadEvidenceWitness;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.google.common.collect.Lists.newArrayList;
import static java.util.Map.entry;
import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_START;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.MID;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.SUBMITTED;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.EVIDENCE_UPLOAD;
import static uk.gov.hmcts.reform.civil.utils.ElementUtils.element;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = {
    EvidenceUploadHandler.class,
    JacksonAutoConfiguration.class
})
public class EvidenceUploadHandlerTest extends BaseCallbackHandlerTest {

    @Autowired
    private EvidenceUploadHandler handler;

    @Autowired
    private final ObjectMapper mapper = new ObjectMapper();

    private UploadEvidenceExpert uploadEvidenceDate = new UploadEvidenceExpert();
    private UploadEvidenceWitness uploadEvidenceDate2 = new UploadEvidenceWitness();

    @Test
    void givenAboutToStartThenReturnsAboutToStartOrSubmitCallbackResponse() {
        CaseData caseData = CaseDataBuilder.builder().build();
        CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_START);

        CallbackResponse response = handler.handle(params);

        assertThat(response).isInstanceOf(AboutToStartOrSubmitCallbackResponse.class);
    }

    @Nested
    class MidEventValidateValuesCallback {
        private static final String PAGE_ID = "validateValues";

        @Test
        void shouldReturnError_whenExpertOption1UploadDateFuture() {
            List<Element<UploadEvidenceExpert>> date = newArrayList();
            date.add(0, element(uploadEvidenceDate.toBuilder()
                                                .expertOption1UploadDate(LocalDate.now().plusWeeks(1)).build()));

            CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
                .documentUploadExpert1(date)
                .build();
            CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getErrors()).contains("Invalid date: date entered must not be in the future.");
        }

        @Test
        void shouldNotReturnError_whenExpertOption1UploadDatePastOrPresent() {
            List<Element<UploadEvidenceExpert>> date = newArrayList();
            date.add(0, element(uploadEvidenceDate.toBuilder()
                                                .expertOption1UploadDate(LocalDate.now()).build()));

            CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
                .documentUploadExpert1(date)
                .build();
            CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getErrors()).isEmpty();
        }

        @Test
        void shouldReturnError_whenExpertOption2UploadDateFuture() {
            List<Element<UploadEvidenceExpert>> date = newArrayList();
            date.add(0, element(uploadEvidenceDate.toBuilder()
                                    .expertOption2UploadDate(LocalDate.now().plusWeeks(1)).build()));

            CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
                .documentUploadExpert2(date)
                .build();
            CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getErrors()).contains("Invalid date: date entered must not be in the future.");
        }

        @Test
        void shouldReturnError_whenExpertOption3UploadDateFuture() {
            List<Element<UploadEvidenceExpert>> date = newArrayList();
            date.add(0, element(uploadEvidenceDate.toBuilder()
                                    .expertOption3UploadDate(LocalDate.now().plusWeeks(1)).build()));

            CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
                .documentUploadExpert3(date)
                .build();
            CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getErrors()).contains("Invalid date: date entered must not be in the future.");
        }

        @Test
        void shouldReturnError_whenExpertOption4UploadDateFuture() {
            List<Element<UploadEvidenceExpert>> date = newArrayList();
            date.add(0, element(uploadEvidenceDate.toBuilder()
                                    .expertOption4UploadDate(LocalDate.now().plusWeeks(1)).build()));

            CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
                .documentUploadExpert4(date)
                .build();
            CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getErrors()).contains("Invalid date: date entered must not be in the future.");
        }

        @Test
        void shouldReturnError_whenWitnessOption3UploadDateInFuture() {
            List<Element<UploadEvidenceWitness>> date = newArrayList();
            date.add(0, element(uploadEvidenceDate2.toBuilder()
                                    .witnessOption3UploadDate(LocalDate.now().plusWeeks(1)).build()));

            CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
                .documentUploadWitness3(date)
                .build();
            CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getErrors()).contains("Invalid date: date entered must not be in the future.");
        }

        @Test
        void shouldReturnError_whenOneDateIsInFuture() {
            //documentUploadWitness1 represents a collection so can have multiple dates entered at any time,
            // these dates should all should be in past, otherwise an error will be populated
            List<Element<UploadEvidenceWitness>> date = newArrayList();
            date.add(0, element(uploadEvidenceDate2.toBuilder()
                                    .witnessOption1UploadDate(LocalDate.now().minusWeeks(1)).build()));
            date.add(1, element(uploadEvidenceDate2.toBuilder()
                                    .witnessOption1UploadDate(LocalDate.now().minusWeeks(1)).build()));
            //dates above represent valid past dates, date below represents invalid future date.
            date.add(2, element(uploadEvidenceDate2.toBuilder()
                                    .witnessOption1UploadDate(LocalDate.now().plusWeeks(1)).build()));

            CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
                .documentUploadWitness1(date)
                .build();
            CallbackParams params = callbackParamsOf(caseData, MID, PAGE_ID);
            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            assertThat(response.getErrors()).contains("Invalid date: date entered must not be in the future.");
        }
    }

    @Test
    void shouldCallExternalTask_whenAboutToSubmit() {
        CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
            .build();
        CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

        var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
        CaseData updatedData = mapper.convertValue(response.getData(), CaseData.class);

        assertThat(updatedData.getCaseDocumentUploadDate().equals(LocalDateTime.now()));
    }

    @Test
    void givenSubmittedThenReturnsSubmittedCallbackResponse() {
        String header = "# Documents uploaded";
        String body = "You can continue uploading documents or return later. To upload more documents, "
            + "go to Next step and select \"Document Upload\".";
        CaseData caseData = CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
            .build();
        CallbackParams params = callbackParamsOf(caseData, SUBMITTED);
        SubmittedCallbackResponse response = (SubmittedCallbackResponse) handler.handle(params);
        assertThat(response).usingRecursiveComparison().isEqualTo(SubmittedCallbackResponse.builder()
                                                                      .confirmationHeader(header)
                                                                      .confirmationBody(body)
                                                                      .build());
    }

    @Test
    void whenRegisterCalledThenReturnEvidenceUploadCaseEvent() {
        Map<String, CallbackHandler> registerTarget = new HashMap<>();
        handler.register(registerTarget);

        assertThat(registerTarget).containsExactly(entry(EVIDENCE_UPLOAD.name(), handler));
    }

}

