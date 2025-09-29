package uk.gov.hmcts.reform.civil.handler.callback.camunda.noticeofchange;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;

@ExtendWith(MockitoExtension.class)
class ClearFormerSolicitorInfoAfterNoCHandlerTest extends BaseCallbackHandlerTest {

    @InjectMocks
    private ClearFormerSolicitorInfoAfterNoCHandler handler;

    @Mock
    private ObjectMapper mapper;

    private CaseData caseData;

    @BeforeEach
    void setUp() {
        mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        handler = new ClearFormerSolicitorInfoAfterNoCHandler(mapper);
        caseData = CaseDataBuilder.builder()
            .atStateClaimDetailsNotifiedWithNoticeOfChangeRespondent1()
            .build();
    }

    @Test
    void shouldUpdateSolicitorDetails_afterNoCSubmittedByRespondentSolicitor1In1v2DiffSolicitorToDiffSolicitor() {
        // Ensure that the former solicitor email exists before the callback
        assertNotNull(caseData.getChangeOfRepresentation().getFormerRepresentationEmailAddress());

        CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

        AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
            .handle(params);

        CaseData updatedCaseData = mapper.convertValue(response.getData(), CaseData.class);

        // Assert that the former solicitor email is cleared in the updated case data
        assertNull(updatedCaseData.getChangeOfRepresentation().getFormerRepresentationEmailAddress());
    }
}
