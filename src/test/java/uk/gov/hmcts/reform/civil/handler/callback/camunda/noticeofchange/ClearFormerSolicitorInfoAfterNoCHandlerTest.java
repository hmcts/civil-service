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

import static org.junit.jupiter.api.Assertions.*;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;

@ExtendWith(MockitoExtension.class)
class ClearFormerSolicitorInfoAfterNoCHandlerTest extends BaseCallbackHandlerTest {

    @InjectMocks
    private ClearFormerSolicitorInfoAfterNoCHandler handler;

    @Mock
    private ObjectMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new ObjectMapper();
        handler = new ClearFormerSolicitorInfoAfterNoCHandler(mapper);
        mapper.registerModule(new JavaTimeModule());
    }

    @Test
    void shouldUpdateSolicitorDetails_afterNoCSubmittedByRespondentSolicitor1In1v2DiffSolicitorToDiffSolicitor() {
        CaseData caseData = CaseDataBuilder.builder()
            .atStateClaimDetailsNotifiedWithNoticeOfChangeRespondent1()
            .build();

        CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

        assertNotNull(caseData.getChangeOfRepresentation().getFormerRepresentationEmailAddress());

        AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) handler
            .handle(params);

        CaseData updatedCaseData = mapper.convertValue(response.getData(), CaseData.class);

        assertNull(updatedCaseData.getChangeOfRepresentation().getFormerRepresentationEmailAddress());
    }
}
