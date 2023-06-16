package uk.gov.hmcts.reform.civil.model.dq;

import lombok.Builder;
import uk.gov.hmcts.reform.civil.enums.HomeTypeOptionLRspec;

@Builder
public record HomeDetails(HomeTypeOptionLRspec type, String typeOtherDetails) {

}
