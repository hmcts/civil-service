package uk.gov.hmcts.reform.unspec.validation;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.unspec.enums.ServiceMethodType;
import uk.gov.hmcts.reform.unspec.model.CaseData;
import uk.gov.hmcts.reform.unspec.model.ServiceMethod;
import uk.gov.hmcts.reform.unspec.service.DeadlinesCalculator;

import java.time.LocalDate;
import javax.validation.ConstraintValidatorContext;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class HasDeemedDateOfServiceTheSameAsOrAfterIssueDateValidatorTest {

    public static final LocalDate NOW = LocalDate.now();

    @Mock
    ConstraintValidatorContext constraintValidatorContext;
    @Mock
    DeadlinesCalculator deadlinesCalculator;

    @InjectMocks
    private HasDeemedDateOfServiceTheSameAsOrAfterIssueDateValidator validator;

    @Test
    void shouldReturnFalse_whenDeemedDateOfServiceIsBeforeIssueDate() {
        when(deadlinesCalculator.calculateDeemedDateOfService(any(LocalDate.class), any()))
            .thenReturn(NOW.minusDays(5));
        assertFalse(validator.isValid(buildCaseDataWithServiceDateOf(NOW.minusDays(5)), constraintValidatorContext));
    }

    @Test
    void shouldReturnTrue_whenDeemedDateOfServiceIsOnIssueDate() {
        when(deadlinesCalculator.calculateDeemedDateOfService(any(LocalDate.class), any()))
            .thenReturn(NOW);
        assertTrue(validator.isValid(buildCaseDataWithServiceDateOf(NOW), constraintValidatorContext));
    }

    @Test
    void shouldReturnTrue_whenDeemedDateOfServiceIsAfterIssueDate() {
        when(deadlinesCalculator.calculateDeemedDateOfService(any(LocalDate.class), any()))
            .thenReturn(NOW.plusDays(5));
        assertTrue(validator.isValid(buildCaseDataWithServiceDateOf(NOW.plusDays(5)), constraintValidatorContext));
    }

    private CaseData buildCaseDataWithServiceDateOf(LocalDate serviceDate) {
        return CaseData.builder()
            .claimIssuedDate(NOW)
            .serviceMethodToRespondentSolicitor1(ServiceMethod.builder().type(ServiceMethodType.POST).build())
            .serviceDateToRespondentSolicitor1(serviceDate)
            .build();
    }
}
