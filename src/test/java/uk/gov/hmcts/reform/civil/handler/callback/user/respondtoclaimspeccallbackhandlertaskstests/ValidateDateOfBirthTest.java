package uk.gov.hmcts.reform.civil.handler.callback.user.respondtoclaimspeccallbackhandlertaskstests;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.validation.DateOfBirthValidator;
import uk.gov.hmcts.reform.civil.validation.PostcodeValidator;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;

@ExtendWith(MockitoExtension.class)
public class ValidateDateOfBirthTest {

    @InjectMocks
    private ValidateDateOfBirth validateDateOfBirth;

    @Mock
    private DateOfBirthValidator dateOfBirthValidator;

    @Mock
    private PostcodeValidator postcodeValidator;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private RespondToClaimSpecUtils respondToClaimSpecUtils;

    @Mock
    private CallbackParams callbackParams;

    private CaseData caseData;

    @BeforeEach
    void setUp() {
        caseData = CaseData.builder().build();
        when(callbackParams.getCaseData()).thenReturn(caseData);
    }

    @Test
    void shouldReturnErrorsWhenDateOfBirthIsInvalid() {
        Party respondent = Party.builder().build();
        when(dateOfBirthValidator.validate(respondent)).thenReturn(Collections.singletonList("Invalid date of birth"));
        when(callbackParams.getCaseData()).thenReturn(caseData.toBuilder().respondent1(respondent).build());

        AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) validateDateOfBirth.execute(callbackParams);

        assertThat(response.getErrors()).contains("Invalid date of birth");
    }

    @Test
    void shouldReturnNoErrorsWhenDateOfBirthIsValid() {
        Party respondent = Party.builder().build();
        when(dateOfBirthValidator.validate(respondent)).thenReturn(Collections.emptyList());
        when(callbackParams.getCaseData()).thenReturn(caseData.toBuilder().respondent1(respondent).build());

        AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) validateDateOfBirth.execute(callbackParams);

        assertThat(response.getErrors()).isEmpty();
    }

    @Test
    void shouldReturnErrorsWhenCorrespondenceAddressIsInvalid() {
        when(postcodeValidator.validate(null)).thenReturn(Collections.singletonList("Invalid postcode"));
        caseData = caseData.toBuilder()
            .isRespondent1(YES)
            .specAoSRespondentCorrespondenceAddressRequired(NO)
            .build();
        when(callbackParams.getCaseData()).thenReturn(caseData);

        AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) validateDateOfBirth.execute(callbackParams);

        assertThat(response.getErrors()).contains("Invalid postcode");
    }

    @Test
    void shouldReturnNoErrorsWhenCorrespondenceAddressIsValid() {
        when(callbackParams.getCaseData()).thenReturn(caseData.toBuilder().isRespondent1(YES).build());

        AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) validateDateOfBirth.execute(callbackParams);

        assertThat(response.getErrors()).isEmpty();
    }

    @Test
    void shouldReturnErrorsWhenRespondent1IsNullAndRespondent2IsNotNull() {
        Party respondent2 = Party.builder().build();
        when(dateOfBirthValidator.validate(respondent2)).thenReturn(Collections.singletonList("Invalid date of birth"));
        when(callbackParams.getCaseData()).thenReturn(caseData.toBuilder().respondent2(respondent2).build());

        AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) validateDateOfBirth.execute(callbackParams);

        assertThat(response.getErrors()).contains("Invalid date of birth");
    }

    @Test
    void shouldReturnNoErrorsWhenRespondent1IsNotNullAndRespondent2IsNull() {
        Party respondent1 = Party.builder().build();
        when(dateOfBirthValidator.validate(respondent1)).thenReturn(Collections.emptyList());
        when(callbackParams.getCaseData()).thenReturn(caseData.toBuilder().respondent1(respondent1).build());

        AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) validateDateOfBirth.execute(callbackParams);

        assertThat(response.getErrors()).isEmpty();
    }

    @Test
    void shouldReturnErrorsWhenBothRespondentsAreNull() {
        when(callbackParams.getCaseData()).thenReturn(caseData);

        AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) validateDateOfBirth.execute(callbackParams);

        assertThat(response.getErrors()).isEmpty();
    }

    @Test
    void shouldReturnErrorsWhenSpecAoSRespondent2CorrespondenceAddressRequiredIsNO() {
        when(postcodeValidator.validate(null)).thenReturn(Collections.singletonList("Invalid postcode"));
        caseData = caseData.toBuilder()
            .isRespondent2(YES)
            .specAoSRespondent2CorrespondenceAddressRequired(NO)
            .build();
        when(callbackParams.getCaseData()).thenReturn(caseData);

        AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) validateDateOfBirth.execute(callbackParams);

        assertThat(response.getErrors()).contains("Invalid postcode");
    }

    @Test
    void shouldReturnNoErrorsWhenSpecAoSRespondentCorrespondenceAddressRequiredIsYES() {
        caseData = caseData.toBuilder()
            .isRespondent1(YES)
            .specAoSRespondentCorrespondenceAddressRequired(YES)
            .build();
        when(callbackParams.getCaseData()).thenReturn(caseData);

        AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) validateDateOfBirth.execute(callbackParams);

        assertThat(response.getErrors()).isEmpty();
    }

    @Test
    void shouldReturnNoErrorsWhenSpecAoSRespondent2CorrespondenceAddressRequiredIsYES() {
        caseData = caseData.toBuilder()
            .isRespondent2(YES)
            .specAoSRespondent2CorrespondenceAddressRequired(YES)
            .build();
        when(callbackParams.getCaseData()).thenReturn(caseData);

        AboutToStartOrSubmitCallbackResponse response = (AboutToStartOrSubmitCallbackResponse) validateDateOfBirth.execute(callbackParams);

        assertThat(response.getErrors()).isEmpty();
    }
}
