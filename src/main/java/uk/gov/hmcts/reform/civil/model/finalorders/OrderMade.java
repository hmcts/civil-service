package uk.gov.hmcts.reform.civil.model.finalorders;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Accessors(chain = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderMade {

    private DatesFinalOrders singleDateSelection;
    private DatesFinalOrders dateRangeSelection;
    private DatesFinalOrders bespokeRangeSelection;

}

