package uk.gov.hmcts.reform.civil.handler.callback.user.createsdocallbackhandlertests;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.civil.handler.callback.user.createsdocallbackhandler.fieldsnihl.NihlFieldValidator;
import uk.gov.hmcts.reform.civil.handler.callback.user.createsdocallbackhandler.fieldsnihl.ValidateFieldsNihl;
import uk.gov.hmcts.reform.civil.model.CaseData;

import java.util.List;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ValidateFieldsNihlTest {

    @Mock
    private List<NihlFieldValidator> nihlFieldValidators;

    @InjectMocks
    private ValidateFieldsNihl validateFieldsNihl;

    @Test
    void shouldValidateFieldsNihl() {
        CaseData caseData = CaseData.builder().build();
        List<String> errors = validateFieldsNihl.validateFieldsNihl(caseData);

        nihlFieldValidators.forEach(builder -> verify(builder, times(1)).validate(caseData, errors));
    }

}
