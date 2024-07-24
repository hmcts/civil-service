package uk.gov.hmcts.reform.civil.model.dq;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;

@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class FurtherInformation {

    private YesOrNo futureApplications;
    private YesOrNo intentionToMakeFutureApplications;
    private String reasonForFutureApplications;
    private String otherInformationForJudge;
}
