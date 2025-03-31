package uk.gov.hmcts.reform.civil.handler.callback.user;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.TestPropertySource;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.enums.BusinessProcessStatus;
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.model.BusinessProcess;
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
import static uk.gov.hmcts.reform.civil.utils.ElementUtils.wrapElements;

@ExtendWith(MockitoExtension.class)
class AddCaseNoteCallbackHandlerTest extends BaseCallbackHandlerTest {

    @InjectMocks
    private AddCaseNoteCallbackHandler handler;

    @Spy
    private ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    @Mock
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
    @TestPropertySource(properties = {
        "azure.service-bus.ccd-events-topic.enabled=true"
    })
    class AboutToSubmitServiceBusEnabled {

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
        }
    }

    @Nested
    @TestPropertySource(properties = {
        "azure.service-bus.ccd-events-topic.enabled=false"
    })
    class AboutToSubmitServiceBusDisabled {

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

            var businessProcess = objectMapper.convertValue(response.getData().get("businessProcess"), BusinessProcess.class);

            assertThat(businessProcess.getStatus()).isEqualTo(BusinessProcessStatus.READY);
            assertThat(businessProcess.getCamundaEvent()).isEqualTo(CaseEvent.ADD_CASE_NOTE.name());
        }
    }
}
