package uk.gov.hmcts.reform.civil.model.dq;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class FurtherInformation {

    private YesOrNo futureApplications;
    private YesOrNo intentionToMakeFutureApplications;
    private String reasonForFutureApplications;
    private String otherInformationForJudge;
}
