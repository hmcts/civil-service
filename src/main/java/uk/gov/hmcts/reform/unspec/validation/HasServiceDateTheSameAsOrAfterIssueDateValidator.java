package uk.gov.hmcts.reform.unspec.validation;

import uk.gov.hmcts.reform.unspec.model.CaseData;
import uk.gov.hmcts.reform.unspec.validation.interfaces.HasServiceDateTheSameAsOrAfterIssueDate;

import java.time.LocalDate;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class HasServiceDateTheSameAsOrAfterIssueDateValidator implements
    ConstraintValidator<HasServiceDateTheSameAsOrAfterIssueDate, CaseData> {

    @Override
    public boolean isValid(CaseData value, ConstraintValidatorContext context) {
        LocalDate serviceDate;
        if (value.getServiceMethod().requiresDateEntry()) {
            serviceDate = value.getServiceDate();
        } else {
            serviceDate = value.getServiceDateAndTime().toLocalDate();
        }

        return serviceDate.equals(value.getClaimIssuedDate()) || serviceDate.isAfter(value.getClaimIssuedDate());
    }
}
