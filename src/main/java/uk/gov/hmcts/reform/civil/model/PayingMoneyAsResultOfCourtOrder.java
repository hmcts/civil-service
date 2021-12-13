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
public class PayingMoneyAsResultOfCourtOrder {

    private final YesOrNo payingDetailsRequired;
    private final List<Element<PayingMoneyDetails>> payingMoneyDetails;

}




