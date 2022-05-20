package uk.gov.hmcts.reform.civil.validation.interfaces;

import org.junit.Before;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.civil.callback.CallbackParams;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.Address;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.validation.PostcodeValidator;

import java.util.List;

public class DefendantAddressValidatorTest {

    private PostcodeValidator postcodeValidator = Mockito.mock(PostcodeValidator.class);

    private DefendantAddressValidator validator = new DefendantAddressValidator() {
        @Override
        public AboutToStartOrSubmitCallbackResponse validateCorrespondenceApplicantAddress(
            CallbackParams callbackParams,
            PostcodeValidator postcodeValidator) {
            return DefendantAddressValidator.super.validateCorrespondenceApplicantAddress(
                callbackParams,
                postcodeValidator
            );
        }
    };

    @Before
    public void prepare() {
        Mockito.reset(postcodeValidator);
    }

    @Test
    void doNothing_whenAddressCorrect() {
        CaseData caseData = CaseData.builder().build();
        CallbackParams params = CallbackParams.builder()
            .caseData(caseData)
            .build();

        AboutToStartOrSubmitCallbackResponse response = validator
            .validateCorrespondenceApplicantAddress(
                params,
                postcodeValidator
            );

        Assertions.assertNotNull(response);
        Mockito.verifyNoInteractions(postcodeValidator);
    }

    @Test
    void validatePostCode_whenAddressIsNotCorrect() {
        CaseData caseData = CaseData.builder()
            .specAoSApplicantCorrespondenceAddressRequired(YesOrNo.NO)
            .specAoSApplicantCorrespondenceAddressdetails(
                Address.builder().postCode("postcode").build()
            )
            .build();
        CallbackParams params = CallbackParams.builder()
            .caseData(caseData)
            .build();

        List<String> errors = List.of("error 1");
        Mockito.when(postcodeValidator.validatePostCodeForDefendant("postcode"))
            .thenReturn(errors);

        AboutToStartOrSubmitCallbackResponse response = validator
            .validateCorrespondenceApplicantAddress(
                params,
                postcodeValidator
            );

        Assertions.assertNotNull(response);
        Mockito.verify(postcodeValidator).validatePostCodeForDefendant("postcode");
        Assertions.assertEquals(errors, response.getErrors());
    }
}
