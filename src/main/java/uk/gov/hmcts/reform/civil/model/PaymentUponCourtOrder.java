package uk.gov.hmcts.reform.civil.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.common.Element;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
public class PaymentUponCourtOrder {

    private YesOrNo payingDetailsRequired;
    private List<Element<PayingMoneyDetails>> payingMoneyDetails;

}




