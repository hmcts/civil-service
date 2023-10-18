package uk.gov.hmcts.reform.civil.handler.callback.user;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CallbackType;
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.CaseNote;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.sampledata.CallbackParamsBuilder;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.CaseNoteService;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CallbackParams.Params.BEARER_TOKEN;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_START;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.civil.callback.CaseEvent.ADD_CASE_NOTE;
import static uk.gov.hmcts.reform.civil.utils.ElementUtils.wrapElements;

@SpringBootTest(classes = {
    AddCaseNoteCallbackHandler.class,
    JacksonAutoConfiguration.class,
})
class AddCaseNoteCallbackHandlerTest extends BaseCallbackHandlerTest {

    @Autowired
    private AddCaseNoteCallbackHandler handler;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CaseNoteService caseNoteService;

    @Nested
    class AboutToStartCallback {

        @Test
        void shouldReturnNoError_WhenAboutToStartIsInvoked() {
            CaseData caseData = CaseDataBuilder.builder().atStateClaimIssued().build();
            CallbackParams params = CallbackParamsBuilder.builder().of(ABOUT_TO_START, caseData).build();

            AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
                .handle(params);

            assertThat(response.getErrors()).isNull();
        }
    }

    private CaseNote caseNote(LocalDateTime timeStamp, String createdBy, String note) {
        return CaseNote.builder()
            .createdOn(timeStamp)
            .createdBy(createdBy)
            .note(note)
            .build();
    }

    @Nested
    class AboutToSubmit {

        @Test
        void shouldAddCaseNoteToList_whenInvoked() {

            CaseNote caseNote = caseNote(LocalDateTime.of(2021, 7, 5, 0, 0, 0),
                                         "John Doe", "Existing case note");
            CaseNote expectedCaseNote = caseNote(LocalDateTime.now(), "John Smith", "Example case note");
            List<Element<CaseNote>> updatedCaseNotes = wrapElements(caseNote, expectedCaseNote);

            CaseData caseData = CaseData.builder()
                .caseNote("Example case note")
                .caseNotes(wrapElements(caseNote))
                .build();

            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

            when(caseNoteService.buildCaseNote(params.getParams().get(BEARER_TOKEN).toString(), "Example case note"))
                .thenReturn(expectedCaseNote);
            when(caseNoteService.addNoteToListStart(expectedCaseNote, caseData.getCaseNotes())).thenReturn(updatedCaseNotes);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            verify(caseNoteService).buildCaseNote(params.getParams().get(BEARER_TOKEN).toString(), "Example case note");
            verify(caseNoteService).addNoteToListStart(expectedCaseNote, caseData.getCaseNotes());

            assertThat(response.getData())
                .doesNotHaveToString("caseNote");

            assertThat(response.getData())
                .extracting("caseNotes")
                .isEqualTo(objectMapper.convertValue(updatedCaseNotes, new TypeReference<>() {
                }));

            assertThat(response.getData())
                .extracting("businessProcess")
                .extracting("camundaEvent", "status")
                .containsOnly(ADD_CASE_NOTE.name(), "READY");
        }
    }

    @Nested
    class SubmittedCallback {

        @Test
        void shouldReturnEmptyResponse_whenInvoked() {
            CaseData caseData = CaseDataBuilder.builder().atStateClaimIssued()
                .caseNotes(caseNote(LocalDateTime.of(2021, 7, 5, 0, 0, 0),
                                    "John Doe", "Existing case note"
                ))
                .build();
            CallbackParams params = callbackParamsOf(caseData, CallbackType.SUBMITTED);

            SubmittedCallbackResponse response = (SubmittedCallbackResponse) handler.handle(params);

            assertThat(response).isEqualTo(SubmittedCallbackResponse.builder().build());
        }
    }
}
