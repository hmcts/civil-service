package uk.gov.hmcts.reform.civil.handler.callback.user.respondtoclaimspeccallbackhandlertaskstests;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackResponse;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.enums.RespondentResponseTypeSpec;
import uk.gov.hmcts.reform.civil.model.CaseData;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;

@ExtendWith(MockitoExtension.class)
public class HandleRespondentResponseTypeForSpecTest {

    @InjectMocks
    private HandleRespondentResponseTypeForSpec handleRespondentResponseTypeForSpec;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        handleRespondentResponseTypeForSpec = new HandleRespondentResponseTypeForSpec(objectMapper);
    }

    @Test
    void shouldSetSpecDefenceFullAdmittedRequiredToNoWhenNotFullAdmission() {
        CaseData caseData = CaseData.builder()
            .respondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.PART_ADMISSION)
            .respondent2ClaimResponseTypeForSpec(RespondentResponseTypeSpec.PART_ADMISSION)
            .build();
        CallbackParams callbackParams = CallbackParams.builder().caseData(caseData).build();

        CallbackResponse response = handleRespondentResponseTypeForSpec.execute(callbackParams);

        Map<String, Object> responseData = ((AboutToStartOrSubmitCallbackResponse) response).getData();
        CaseData updatedCaseData = objectMapper.convertValue(responseData, CaseData.class);
        assertEquals(NO, updatedCaseData.getSpecDefenceFullAdmittedRequired());
    }

    @Test
    void shouldNotChangeSpecDefenceFullAdmittedRequiredWhenFullAdmission() {
        CaseData caseData = CaseData.builder()
            .respondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.FULL_ADMISSION)
            .respondent2ClaimResponseTypeForSpec(RespondentResponseTypeSpec.FULL_ADMISSION)
            .specDefenceFullAdmittedRequired(YES)
            .build();
        CallbackParams callbackParams = CallbackParams.builder().caseData(caseData).build();

        CallbackResponse response = handleRespondentResponseTypeForSpec.execute(callbackParams);

        Map<String, Object> responseData = ((AboutToStartOrSubmitCallbackResponse) response).getData();
        CaseData updatedCaseData = objectMapper.convertValue(responseData, CaseData.class);
        assertEquals(YES, updatedCaseData.getSpecDefenceFullAdmittedRequired());
    }

    @Test
    void shouldSetSpecDefenceFullAdmittedRequiredToNoWhenRespondent2ResponseTypeIsNull() {
        CaseData caseData = CaseData.builder()
            .respondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.PART_ADMISSION)
            .respondent2ClaimResponseTypeForSpec(null)
            .build();
        CallbackParams callbackParams = CallbackParams.builder().caseData(caseData).build();

        CallbackResponse response = handleRespondentResponseTypeForSpec.execute(callbackParams);

        Map<String, Object> responseData = ((AboutToStartOrSubmitCallbackResponse) response).getData();
        CaseData updatedCaseData = objectMapper.convertValue(responseData, CaseData.class);
        assertEquals(NO, updatedCaseData.getSpecDefenceFullAdmittedRequired());
    }

    @Test
    void shouldSetSpecDefenceFullAdmittedRequiredToNoWhenRespondent2ResponseTypeIsNotFullAdmission() {
        CaseData caseData = CaseData.builder()
            .respondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.FULL_ADMISSION)
            .respondent2ClaimResponseTypeForSpec(RespondentResponseTypeSpec.PART_ADMISSION)
            .build();
        CallbackParams callbackParams = CallbackParams.builder().caseData(caseData).build();

        CallbackResponse response = handleRespondentResponseTypeForSpec.execute(callbackParams);

        Map<String, Object> responseData = ((AboutToStartOrSubmitCallbackResponse) response).getData();
        CaseData updatedCaseData = objectMapper.convertValue(responseData, CaseData.class);
        assertEquals(NO, updatedCaseData.getSpecDefenceFullAdmittedRequired());
    }
}
