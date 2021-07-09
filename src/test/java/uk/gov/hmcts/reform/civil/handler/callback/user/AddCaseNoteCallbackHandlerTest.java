package uk.gov.hmcts.reform.civil.handler.callback.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.CaseNote;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.sampledata.CallbackParamsBuilder;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;
import uk.gov.hmcts.reform.civil.service.CaseNoteService;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.callback.CallbackParams.Params.BEARER_TOKEN;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_START;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;
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

    @Nested
    class AboutToSubmit {

        private CaseNote caseNote(LocalDate date, String createdBy, String note) {
            return CaseNote.builder()
                .date(date)
                .createdBy(createdBy)
                .note(note)
                .build();
        }

        @Test
        void shouldAddCaseNoteToList_whenInvoked() {

            CaseNote caseNote = caseNote(LocalDate.of(2021, 7, 5), "John Doe", "Existing case note");
            CaseNote expectedCaseNote = caseNote(LocalDate.now(), "John Smith", "Example case note");
            List<Element<CaseNote>> updatedCaseNotes = wrapElements(caseNote, expectedCaseNote);

            CaseData caseData = CaseData.builder()
                .caseNote("Example case note")
                .caseNotes(wrapElements(caseNote))
                .build();

            CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

            when(caseNoteService.buildCaseNote(params.getParams().get(BEARER_TOKEN).toString(), "Example case note"))
                .thenReturn(expectedCaseNote);
            when(caseNoteService.addNoteToList(expectedCaseNote, caseData.getCaseNotes())).thenReturn(updatedCaseNotes);

            var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

            verify(caseNoteService).buildCaseNote(params.getParams().get(BEARER_TOKEN).toString(), "Example case note");
            verify(caseNoteService).addNoteToList(expectedCaseNote, caseData.getCaseNotes());

            assertThat(response.getData())
                .extracting("caseNote")
                .isNull();

            assertThat(response.getData())
                .extracting("caseNotes")
                .isEqualTo(wrapElements(updatedCaseNotes));
        }
    }
}
