package uk.gov.hmcts.reform.civil.notification.handlers.coscapplicationafterpayment;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.notification.handlers.AllPartiesEmailGenerator;

import java.util.List;

@Component
public class CoscApplicationAfterPaymentAllPartiesEmailGenerator extends AllPartiesEmailGenerator {

    public CoscApplicationAfterPaymentAllPartiesEmailGenerator(CoscApplicationAfterPaymentAppSolOneEmailDTOGenerator applicant) {
        super(List.of(applicant));
    }
}
