package uk.gov.hmcts.reform.civil.handler.callback.user.respondtoclaimspeccallbackhandlertasks;

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
import uk.gov.hmcts.reform.civil.sampledata.CaseDataBuilder;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ValidateLengthOfUnemploymentTest {

    @InjectMocks
    private ValidateLengthOfUnemployment validateLengthOfUnemployment;

    @Mock
    private CallbackParams callbackParams;

    private CaseData caseData;

    @BeforeEach
    void setUp() {
        caseData = CaseDataBuilder.builder().build();
        when(callbackParams.getCaseData()).thenReturn(caseData);
    }

    @Test
    void shouldReturnErrorsWhenLengthOfUnemploymentIsInvalid() {
        caseData.setRespondToClaimAdmitPartUnemployedLRspec(
                        buildUnemployedComplexType(buildLengthOfUnemployment("1.5", "2.5"))
        );
        when(callbackParams.getCaseData()).thenReturn(caseData);

        AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) validateLengthOfUnemployment.execute(callbackParams);

        List<String> expectedErrors = Collections.singletonList("Length of time unemployed must be a whole number, for example, 10.");
        assertThat(response.getErrors()).isEqualTo(expectedErrors);
    }

    @Test
    void shouldReturnNoErrorsWhenLengthOfUnemploymentIsValid() {
        caseData.setRespondToClaimAdmitPartUnemployedLRspec(
                        buildUnemployedComplexType(buildLengthOfUnemployment("2", "3"))
        );
        when(callbackParams.getCaseData()).thenReturn(caseData);

        AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) validateLengthOfUnemployment.execute(callbackParams);

        assertThat(response.getErrors()).isEmpty();
    }

    @Test
    void shouldReturnNoErrorsWhenRespondToClaimAdmitPartUnemployedLRspecIsNull() {
        caseData.setRespondToClaimAdmitPartUnemployedLRspec(null);
        when(callbackParams.getCaseData()).thenReturn(caseData);

        AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) validateLengthOfUnemployment.execute(callbackParams);

        assertThat(response.getErrors()).isEmpty();
    }

    @Test
    void shouldReturnNoErrorsWhenLengthOfUnemploymentIsNull() {
        caseData.setRespondToClaimAdmitPartUnemployedLRspec(
                        buildUnemployedComplexType(null)
        );
        when(callbackParams.getCaseData()).thenReturn(caseData);

        AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) validateLengthOfUnemployment.execute(callbackParams);

        assertThat(response.getErrors()).isEmpty();
    }

    @Test
    void shouldReturnErrorWhenNumberOfMonthsInUnemploymentIsNotWholeNumber() {
        caseData.setRespondToClaimAdmitPartUnemployedLRspec(
                        buildUnemployedComplexType(buildLengthOfUnemployment("2", "3.5"))
        );
        when(callbackParams.getCaseData()).thenReturn(caseData);

        AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) validateLengthOfUnemployment.execute(callbackParams);

        List<String> expectedErrors = Collections.singletonList("Length of time unemployed must be a whole number, for example, 10.");
        assertThat(response.getErrors()).isEqualTo(expectedErrors);
    }

    private LengthOfUnemploymentComplexTypeLRspec buildLengthOfUnemployment(String years, String months) {
        LengthOfUnemploymentComplexTypeLRspec lengthOfUnemploymentComplexTypeLRspec = new LengthOfUnemploymentComplexTypeLRspec();
        lengthOfUnemploymentComplexTypeLRspec.setNumberOfYearsInUnemployment(years);
        lengthOfUnemploymentComplexTypeLRspec.setNumberOfMonthsInUnemployment(months);
        return lengthOfUnemploymentComplexTypeLRspec;
    }

    private UnemployedComplexTypeLRspec buildUnemployedComplexType(LengthOfUnemploymentComplexTypeLRspec lengthOfUnemployment) {
        UnemployedComplexTypeLRspec unemployedComplexTypeLRspec = new UnemployedComplexTypeLRspec();
        unemployedComplexTypeLRspec.setLengthOfUnemployment(lengthOfUnemployment);
        return unemployedComplexTypeLRspec;
    }
}
