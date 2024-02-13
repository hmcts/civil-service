package uk.gov.hmcts.reform.civil.handler.callback.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.helpers.CaseDetailsConverter;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.citizenui.CaseDataLiP;
import uk.gov.hmcts.reform.civil.model.citizenui.HelpWithFees;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;

@SpringBootTest(classes = {
    InvalidHwFCallbackHandler.class,
    JacksonAutoConfiguration.class,
    CaseDetailsConverter.class
})
public class InvalidHwFCallbackHandlerTest extends BaseCallbackHandlerTest {

    @Autowired
    private InvalidHwFCallbackHandler handler;

    @Autowired
    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    void shouldSuccessfullyCreateEvent() {
        CaseData caseData = CaseDataBuilder.builder()
            .caseDataLip(CaseDataLiP.builder()
                             .helpWithFees(HelpWithFees.builder()
                                               .helpWithFeesReferenceNumber("123")
                                               .build())
                             .build())
            .build();

        CallbackParams params = callbackParamsOf(caseData, ABOUT_TO_SUBMIT);

        var response = (AboutToStartOrSubmitCallbackResponse) handler.handle(params);

        assertThat(response.getErrors()).isNull();
        CaseData responseCaseData = mapper.convertValue(response.getData(), CaseData.class);

        assertThat(responseCaseData.getHwFeesDetails().getInvalidHelpWithFee()).isEqualTo(YesOrNo.YES);
    }

}
