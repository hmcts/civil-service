package uk.gov.hmcts.reform.unspec.validation;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.unspec.enums.ServiceMethodType;
import uk.gov.hmcts.reform.unspec.model.CaseData;
import uk.gov.hmcts.reform.unspec.model.ServiceMethod;

import java.time.LocalDate;
import javax.validation.ConstraintValidatorContext;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MockitoExtension.class)
class HasServiceDateTheSameAsOrAfterIssueDateValidatorTest {

    public static final LocalDate NOW = LocalDate.now();

    @Mock
    ConstraintValidatorContext constraintValidatorContext;

    @InjectMocks
    private HasServiceDateTheSameAsOrAfterIssueDateValidator validator;

    @Test
    void shouldReturnFalse_whenServiceDateIsBeforeIssueDate() {
        assertFalse(validator.isValid(buildCaseDataWithServiceDateOf(NOW.minusDays(5)), constraintValidatorContext));
    }

    @Test
    void shouldReturnTrue_whenServiceDateIsOnIssueDate() {
        assertTrue(validator.isValid(buildCaseDataWithServiceDateOf(NOW), constraintValidatorContext));
    }

    @Test
    void shouldReturnTrue_whenServiceDateIsAfterIssueDate() {
        assertTrue(validator.isValid(buildCaseDataWithServiceDateOf(NOW.plusDays(5)), constraintValidatorContext));
    }

    private CaseData buildCaseDataWithServiceDateOf(LocalDate serviceDate) {
        return CaseData.builder()
            .claimIssuedDate(NOW)
            .serviceMethod(ServiceMethod.builder().type(ServiceMethodType.POST).build())
            .serviceDate(serviceDate)
            .build();
    }
}
