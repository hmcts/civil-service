package uk.gov.hmcts.reform.civil.handler.callback.user.respondtoclaimspeccallbackhandlertasks;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
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
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;

@ExtendWith(MockitoExtension.class)
class HandleRespondentResponseTypeForSpecTest {

    @InjectMocks
    private HandleRespondentResponseTypeForSpec handleRespondentResponseTypeForSpec;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
        handleRespondentResponseTypeForSpec = new HandleRespondentResponseTypeForSpec(objectMapper);
    }

    private CaseData execute(CaseData caseData) {
        CallbackParams callbackParams = new CallbackParams().caseData(caseData);
        CallbackResponse response = handleRespondentResponseTypeForSpec.execute(callbackParams);
        Map<String, Object> responseData = ((AboutToStartOrSubmitCallbackResponse) response).getData();
        return objectMapper.convertValue(responseData, CaseData.class);
    }

    @Test
    void shouldSetSpecDefenceFullAdmittedRequiredToNoWhenNotFullAdmission() {
        CaseData caseData = CaseDataBuilder.builder()
                .respondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.PART_ADMISSION)
                .respondent2ClaimResponseTypeForSpec(RespondentResponseTypeSpec.PART_ADMISSION)
                .build();

        assertEquals(NO, execute(caseData).getSpecDefenceFullAdmittedRequired());
    }

    @Test
    void shouldNotChangeSpecDefenceFullAdmittedRequiredWhenFullAdmission() {
        CaseData caseData = CaseDataBuilder.builder().build();
        caseData.setRespondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.FULL_ADMISSION);
        caseData.setRespondent2ClaimResponseTypeForSpec(RespondentResponseTypeSpec.FULL_ADMISSION);
        caseData.setSpecDefenceFullAdmittedRequired(YES);

        assertEquals(YES, execute(caseData).getSpecDefenceFullAdmittedRequired());
    }

    @Test
    void shouldSetSpecDefenceFullAdmittedRequiredToNoWhenRespondent2ResponseTypeIsNull() {
        CaseData caseData = CaseDataBuilder.builder()
                .respondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.PART_ADMISSION)
                .respondent2ClaimResponseTypeForSpec(null)
                .build();

        assertEquals(NO, execute(caseData).getSpecDefenceFullAdmittedRequired());
    }

    @Test
    void shouldNotOverwriteRespondent1FullAdmitPaidFlagWhenRespondent2IsNotFullAdmission() {
        CaseData caseData = CaseDataBuilder.builder()
                .respondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.FULL_ADMISSION)
                .respondent2ClaimResponseTypeForSpec(RespondentResponseTypeSpec.PART_ADMISSION)
                .build();
        caseData.setSpecDefenceFullAdmittedRequired(YES);

        CaseData updated = execute(caseData);

        assertEquals(YES, updated.getSpecDefenceFullAdmittedRequired());
        assertNull(updated.getSpecDefenceFullAdmitted2Required());
    }

    @Test
    void shouldSetSpecDefenceFullAdmitted2RequiredToNoWhenRespondent2IsNotFullAdmission() {
        CaseData caseData = CaseDataBuilder.builder()
                .isRespondent2(YES)
                .respondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec.FULL_ADMISSION)
                .respondent2ClaimResponseTypeForSpec(RespondentResponseTypeSpec.PART_ADMISSION)
                .build();
        caseData.setSpecDefenceFullAdmittedRequired(YES);

        CaseData updated = execute(caseData);

        assertEquals(YES, updated.getSpecDefenceFullAdmittedRequired());
        assertEquals(NO, updated.getSpecDefenceFullAdmitted2Required());
    }
}
