package uk.gov.hmcts.reform.civil.model.dq;


import lombok.Builder;

import uk.gov.hmcts.reform.civil.enums.HomeTypeOptionLRspec;

/**
 * @param type             type of home.
 * @param typeOtherDetails home description when type is "other".
 */

@Builder
public record HomeDetails(HomeTypeOptionLRspec type, String typeOtherDetails) {

}
