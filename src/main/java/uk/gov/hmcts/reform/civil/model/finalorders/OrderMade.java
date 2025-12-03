package uk.gov.hmcts.reform.civil.model.finalorders;

import lombok.AllArgsConstructor;
import lombok.experimental.Accessors;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Accessors(chain = true)
@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class OrderMade {

    private DatesFinalOrders singleDateSelection;
    private DatesFinalOrders dateRangeSelection;
    private DatesFinalOrders bespokeRangeSelection;

}

