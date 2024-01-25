package uk.gov.hmcts.reform.civil.model.docmosis.claimantresponse;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ApplicantResponsePaymentPlan {
    IMMEDIATELY("Pay immediately"),
    SET_DATE("By a set date"),
    REPAYMENT_PLAN("By instalments");

    private final String displayedValue;
}
