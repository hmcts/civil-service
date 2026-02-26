package uk.gov.hmcts.reform.civil.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.common.Element;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class PaymentUponCourtOrder {

    private YesOrNo payingDetailsRequired;
    private List<Element<PayingMoneyDetails>> payingMoneyDetails;

}



