package uk.gov.hmcts.reform.civil.handler.callback.user;

import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import com.fasterxml.jackson.databind.ObjectMapper;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.model.CaseData;

import java.time.LocalDate;

import static org.junit.Assert.assertNull;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;

@SpringBootTest(classes = {
    ManageStayWATaskCallbackHandler.class,
    JacksonAutoConfiguration.class
})
class ManageStayWATaskCallbackHandlerTest extends BaseCallbackHandlerTest {

    @Autowired
    private ManageStayWATaskCallbackHandler handler;

    @Test
    void aboutToSubmitShouldClearManageStayUpdateRequestDateField() {
        CaseData caseData = CaseData.builder()
            .manageStayUpdateRequestDate(LocalDate.now())
            .build();

        CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

        var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

        ObjectMapper mapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .findAndRegisterModules();

        CaseData updatedData = mapper.convertValue(response.getData(), CaseData.class);

        assertNull(updatedData.getManageStayUpdateRequestDate());
    }

}
