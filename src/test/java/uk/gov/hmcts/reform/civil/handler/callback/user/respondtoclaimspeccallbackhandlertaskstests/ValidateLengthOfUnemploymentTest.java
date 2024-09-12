package uk.gov.hmcts.reform.civil.handler.callback.user.respondtoclaimspeccallbackhandlertaskstests;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.LengthOfUnemploymentComplexTypeLRspec;
import uk.gov.hmcts.reform.civil.model.UnemployedComplexTypeLRspec;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ValidateLengthOfUnemploymentTest {

    @InjectMocks
    private ValidateLengthOfUnemployment validateLengthOfUnemployment;

    @Mock
    private CallbackParams callbackParams;

    private CaseData caseData;

    @BeforeEach
    void setUp() {
        caseData = CaseData.builder().build();
        when(callbackParams.getCaseData()).thenReturn(caseData);
    }

    @Test
    void shouldReturnErrorsWhenLengthOfUnemploymentIsInvalid() {
        LengthOfUnemploymentComplexTypeLRspec lengthOfUnemployment = LengthOfUnemploymentComplexTypeLRspec.builder()
            .numberOfYearsInUnemployment("1.5")
            .numberOfMonthsInUnemployment("2.5")
            .build();
        UnemployedComplexTypeLRspec respondToClaimAdmitPartUnemployedLRspec = UnemployedComplexTypeLRspec.builder()
            .lengthOfUnemployment(lengthOfUnemployment)
            .build();
        caseData = caseData.toBuilder().respondToClaimAdmitPartUnemployedLRspec(respondToClaimAdmitPartUnemployedLRspec).build();
        when(callbackParams.getCaseData()).thenReturn(caseData);

        AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) validateLengthOfUnemployment.execute(callbackParams);

        List<String> expectedErrors = Collections.singletonList("Length of time unemployed must be a whole number, for example, 10.");
        assertThat(response.getErrors()).isEqualTo(expectedErrors);
    }

    @Test
    void shouldReturnNoErrorsWhenLengthOfUnemploymentIsValid() {
        LengthOfUnemploymentComplexTypeLRspec lengthOfUnemployment = LengthOfUnemploymentComplexTypeLRspec.builder()
            .numberOfYearsInUnemployment("2")
            .numberOfMonthsInUnemployment("3")
            .build();
        UnemployedComplexTypeLRspec respondToClaimAdmitPartUnemployedLRspec = UnemployedComplexTypeLRspec.builder()
            .lengthOfUnemployment(lengthOfUnemployment)
            .build();
        caseData = caseData.toBuilder().respondToClaimAdmitPartUnemployedLRspec(respondToClaimAdmitPartUnemployedLRspec).build();
        when(callbackParams.getCaseData()).thenReturn(caseData);

        AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) validateLengthOfUnemployment.execute(callbackParams);

        assertThat(response.getErrors()).isEmpty();
    }
}
