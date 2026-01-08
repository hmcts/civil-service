package uk.gov.hmcts.reform.civil.model.dq;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FutureApplications {

    private YesOrNo intentionToMakeFutureApplications;
    private String whatWillFutureApplicationsBeMadeFor;
}
