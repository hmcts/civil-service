package uk.gov.hmcts.reform.civil.model.dq;

import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;

@Data
@Builder
public class FutureApplications {

    private final YesOrNo intentionToMakeFutureApplications;
    private final String whatWillFutureApplicationsBeMadeFor;
}
