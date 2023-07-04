package uk.gov.hmcts.reform.civil.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.model.CaseData;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class PaymentDateService {

    private final DeadlinesCalculator deadlinesCalculator;

    public LocalDate getPaymentDateAdmittedClaim(CaseData caseData) {
        if (caseData.getRespondToClaimAdmitPartLRspec() != null
            && caseData.getRespondToClaimAdmitPartLRspec().getWhenWillThisAmountBePaid() != null) {
            return caseData.getRespondToClaimAdmitPartLRspec().getWhenWillThisAmountBePaid();
        }
        if (caseData.getRespondToAdmittedClaim() != null
            && caseData.getRespondToAdmittedClaim().getWhenWasThisAmountPaid() != null) {
            return caseData.getRespondToAdmittedClaim().getWhenWasThisAmountPaid();
        }
        if (caseData.getRespondent1ResponseDate() != null) {
            return deadlinesCalculator.calculateRespondentPaymentDateAdmittedClaim(caseData.getRespondent1ResponseDate());
        }
        return null;
    }
}
