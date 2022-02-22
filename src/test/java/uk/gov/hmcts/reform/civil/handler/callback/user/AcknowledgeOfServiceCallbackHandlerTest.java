package uk.gov.hmcts.reform.civil.handler.callback.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Assert;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.callback.CallbackType;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.handler.callback.BaseCallbackHandlerTest;
import uk.gov.hmcts.reform.civil.model.Address;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.DeadlinesCalculator;
import uk.gov.hmcts.reform.civil.service.ExitSurveyContentService;
import uk.gov.hmcts.reform.civil.service.Time;
import uk.gov.hmcts.reform.civil.validation.DateOfBirthValidator;
import uk.gov.hmcts.reform.civil.validation.PostcodeValidator;

import java.util.Collections;
import java.util.List;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = {
    AcknowledgeOfServiceCallbackHandler.class,
    ExitSurveyContentService.class,
    DateOfBirthValidator.class,
    DeadlinesCalculator.class,
    ObjectMapper.class,
    PostcodeValidator.class,
    Time.class
})
public class AcknowledgeOfServiceCallbackHandlerTest extends BaseCallbackHandlerTest {

    @Autowired
    private AcknowledgeOfServiceCallbackHandler handler;

    @MockBean
    private ExitSurveyContentService exitSurveyContentService;
    @MockBean
    private DateOfBirthValidator dateOfBirthValidator;
    @MockBean
    private DeadlinesCalculator deadlinesCalculator;
    @MockBean
    private ObjectMapper objectMapper;
    @MockBean
    private PostcodeValidator postcodeValidator;
    @MockBean
    private Time time;

    @Test
    void midSpecCorrespondenceAddress_checkAddressIfWasIncorrect() {
        String postCode = "postCode";
        CaseData caseData = CaseData.builder()
            .specAoSApplicantCorrespondenceAddressRequired(YesOrNo.NO)
            .specAoSApplicantCorrespondenceAddressdetails(Address.builder()
                                                              .postCode(postCode)
                                                              .build())
            .build();
        CallbackParams params = callbackParamsOf(caseData, CallbackType.MID, "specCorrespondenceAddress");
        CallbackRequest request = CallbackRequest.builder()
            .eventId("ACKNOWLEDGEMENT_OF_SERVICE")
            .build();
        params = params.toBuilder().request(request).build();

        List<String> errors = Collections.singletonList("error 1");
        Mockito.when(postcodeValidator.validatePostCodeForDefendant(postCode)).thenReturn(errors);

        CallbackResponse response = handler.handle(params);
        Assert.assertEquals(errors, ((AboutToStartOrSubmitCallbackResponse) response).getErrors());
    }
}
