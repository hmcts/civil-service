package uk.gov.hmcts.reform.civil.model.citizenui;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;

import java.util.List;

@Data
@Builder(toBuilder = true)
@Accessors(chain = true)
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
}
