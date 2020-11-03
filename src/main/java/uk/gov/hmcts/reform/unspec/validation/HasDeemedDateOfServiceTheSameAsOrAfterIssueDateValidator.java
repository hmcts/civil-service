package uk.gov.hmcts.reform.unspec.validation;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.unspec.model.CaseData;
import uk.gov.hmcts.reform.unspec.model.ServiceMethod;
import uk.gov.hmcts.reform.unspec.service.DeadlinesCalculator;
import uk.gov.hmcts.reform.unspec.validation.interfaces.HasDeemedDateOfServiceTheSameAsOrAfterIssueDate;

import java.time.LocalDate;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

@Component
@RequiredArgsConstructor
public class HasDeemedDateOfServiceTheSameAsOrAfterIssueDateValidator implements
    ConstraintValidator<HasDeemedDateOfServiceTheSameAsOrAfterIssueDate, CaseData> {

    private final DeadlinesCalculator deadlinesCalculator;

    @Override
    public boolean isValid(CaseData caseData, ConstraintValidatorContext context) {
        LocalDate serviceDate;
        ServiceMethod serviceMethod = caseData.getServiceMethodToRespondentSolicitor1();
        if (serviceMethod.requiresDateEntry()) {
            serviceDate = caseData.getServiceDateToRespondentSolicitor1();
        } else {
            serviceDate = caseData.getServiceDateTimeToRespondentSolicitor1().toLocalDate();
        }
        LocalDate deemedDateOfService = deadlinesCalculator.calculateDeemedDateOfService(
            serviceDate, serviceMethod.getType());
        return deemedDateOfService.equals(caseData.getClaimIssuedDate())
            || deemedDateOfService.isAfter(caseData.getClaimIssuedDate());
    }
}
