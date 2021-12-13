package uk.gov.hmcts.reform.civil.model.dq;

import lombok.Data;
import uk.gov.hmcts.reform.civil.enums.HomeTypeOptionSpec;

import javax.validation.constraints.NotNull;

@Data
@ValidHomeType
public class HomeDetails {

    /**
     * type of home.
     */
    @NotNull
    private final HomeTypeOptionSpec type;

    /**
     * home description when type is "other".
     */
    private final String typeOtherDetails;
}
