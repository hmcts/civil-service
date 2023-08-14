package uk.gov.hmcts.reform.cmc.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AcceptPaymentMethod {
    private YesOrNo accept;
}
