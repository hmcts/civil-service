package uk.gov.hmcts.reform.civil.handler.callback.user;


import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.autoconfigure.validation.ValidationAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CallbackType;
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.CaseNote;
import uk.gov.hmcts.reform.civil.sampledata.CallbackParamsBuilder;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.sampledata.CaseDetailsBuilder;
import uk.gov.hmcts.reform.civil.service.CaseNoteService;
import uk.gov.hmcts.reform.idam.client.IdamClient;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;

import java.time.LocalDate;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_START;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.utils.ElementUtils.unwrapElements;
import static uk.gov.hmcts.reform.civil.utils.ElementUtils.wrapElements;

@SpringBootTest(classes = {
    AddCaseNoteCallbackHandler.class,
    JacksonAutoConfiguration.class,
    ValidationAutoConfiguration.class,
    CaseNoteService.class
})

class AddCaseNoteCallbackHandlerTest extends BaseCallbackHandlerTest {

    @Autowired
    private AddCaseNoteCallbackHandler handler;

    @Autowired
    private CaseNoteService caseNoteService;

    @Autowired
    private ObjectMapper mapper;

    @MockBean
    private IdamClient idamClient;

    @Nested
    class AboutToStartCallback {



        @Test
        void shouldReturnNoError_WhenAboutToStartIsInvoked() {
            CaseDetails caseDetails = CaseDetailsBuilder.builder().atStateCaseIssued().build();
            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_START, caseDetails).build();

            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                .handle(params);

            assertThat(response.getErrors()).isNull();
        }
    }

    @Nested
    class AboutToSubmit {

        @BeforeEach
        void setup() {
            given(idamClient.getUserDetails(any())).willReturn(UserDetails.builder()
                                                                   .forename("John")
                                                                   .surname("Smith")
                                                                   .build());
        }

        private CaseNote caseNote(LocalDate date, String createdBy, String note) {
            return CaseNote.builder()
                .date(date)
                .createdBy(createdBy)
                .note(note)
                .build();
        }
//Come back to this after
//        @Test
//        void shouldAddCaseNoteToList() {
//            CaseNote caseNote = caseNote(LocalDate.of(2021, 7, 5), "John Doe", "Existing note");
//
//            CaseDetails caseDetails = CaseDetails.builder()
//                .data(Map.of(
//                    "caseNote", "Example case note",
//                    "caseNotes", wrapElements(caseNote)))
//                .build();
//            CaseData caseData = mapper.convertValue(caseDetails.getData(), CaseData.class);
//
//
//
//            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);
//
//
//
//            assertThat(caseData.getCaseNote()).isNull();
//            assertThat(unwrapElements(caseData.getCaseNotes()))
//                .containsExactly(caseNote, caseNote(LocalDate.now(), "John Smith", "Example case note"));
//        }
    }

    @Nested
    class SubmittedCallback {

        @Test
        void shouldReturnEmptyResponse_whenInvoked() {
            CaseData caseData = CaseDataBuilder.builder().atStateProceedsOfflineAdmissionOrCounterClaim().build();
            CallbackParams params = callbackParamsOf(caseData, CallbackType.SUBMITTED);

            SubmittedCallbackResponse response = (SubmittedCallbackResponse) handler.handle(params);

            assertThat(response).isEqualTo(SubmittedCallbackResponse.builder().build());
        }
    }
}
