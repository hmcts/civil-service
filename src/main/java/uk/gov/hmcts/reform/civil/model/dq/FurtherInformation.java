package uk.gov.hmcts.reform.civil.model.dq;

import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;

@Data
@lombok.AllArgsConstructor
@lombok.NoArgsConstructor
@Builder(toBuilder = true)
public class FurtherInformation {

    private YesOrNo futureApplications;
    private YesOrNo intentionToMakeFutureApplications;
    private String reasonForFutureApplications;
    private String otherInformationForJudge;
}
