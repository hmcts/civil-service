package uk.gov.hmcts.reform.civil.model.citizenui;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class FeePaymentOutcomeDetails {

    private YesOrNo hwfNumberAvailable;
    private String  hwfNumberForFeePaymentOutcome;
    private YesOrNo hwfFullRemissionGrantedForClaimIssue;
    private YesOrNo hwfFullRemissionGrantedForHearingFee;
    private List<String> hwfOutstandingFeePaymentDoneForClaimIssue;
    private List<String> hwfOutstandingFeePaymentDoneForHearingFee;
    private YesOrNo hwfFullRemissionGrantedForGa;
    private YesOrNo hwfFullRemissionGrantedForAdditionalFee;
    private List<String> hwfOutstandingFeePaymentDoneForGa;
    private List<String> hwfOutstandingFeePaymentDoneForAdditional;

    public FeePaymentOutcomeDetails copy() {
        return new FeePaymentOutcomeDetails(
            hwfNumberAvailable,
            hwfNumberForFeePaymentOutcome,
            hwfFullRemissionGrantedForClaimIssue,
            hwfFullRemissionGrantedForHearingFee,
            hwfOutstandingFeePaymentDoneForClaimIssue,
            hwfOutstandingFeePaymentDoneForHearingFee,
            hwfFullRemissionGrantedForGa,
            hwfFullRemissionGrantedForAdditionalFee,
            hwfOutstandingFeePaymentDoneForGa,
            hwfOutstandingFeePaymentDoneForAdditional
        );
    }
}
