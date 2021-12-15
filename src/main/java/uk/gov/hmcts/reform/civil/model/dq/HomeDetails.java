package uk.gov.hmcts.reform.civil.model.dq;

import lombok.Data;
import uk.gov.hmcts.reform.civil.enums.HomeTypeOptionLRspec;

@Data
public class HomeDetails {

    /**
     * type of home.
     */
    private final HomeTypeOptionLRspec type;

    /**
     * home description when type is "other".
     */
    private final String typeOtherDetails;
}
