package uk.gov.hmcts.reform.civil.handler.callback.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.citizenui.CaseDataLiP;
import uk.gov.hmcts.reform.civil.model.citizenui.HelpWithFees;
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.civil.callback.CallbackType.ABOUT_TO_SUBMIT;

@ExtendWith(MockitoExtension.class)
public class InvalidHwFCallbackHandlerTest extends BaseCallbackHandlerTest {

    private InvalidHwFCallbackHandler handler;

    private ObjectMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new ObjectMapper();
        handler = new InvalidHwFCallbackHandler(mapper);
    }

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
