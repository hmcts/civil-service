package uk.gov.hmcts.reform.civil.model.citizenui;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.civil.enums.FeeType;
import uk.gov.hmcts.reform.civil.enums.HWFFeeDetailsSummary;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HelpWithFeesDetails {

    private String noRemissionDetails;
    private HWFFeeDetailsSummary noRemissionDetailsSummary;
    private FeeType hwfFeeType;
}
