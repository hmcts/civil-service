package uk.gov.hmcts.reform.civil.model.dq;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.civil.enums.HomeTypeOptionLRspec;

/**
 * @param type             type of home.
 * @param typeOtherDetails home description when type is "other".
 */
@Data
@Builder
@AllArgsConstructor
public record HomeDetails(HomeTypeOptionLRspec type, String typeOtherDetails) {

}
