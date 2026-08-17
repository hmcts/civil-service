package uk.gov.hmcts.reform.civil.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.common.Element;

import java.util.List;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;
import uk.gov.hmcts.ccd.sdk.api.ComplexType;

@ComplexType(name = "PaymentUponCourtOrderLRspec", generate = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class PaymentUponCourtOrder {

    @CCD(label = " ", searchable = false, typeOverride = FieldType.YesOrNo)
    private YesOrNo payingDetailsRequired;
    @CCD(
            label = " ",
            showCondition = "payingDetailsRequired = \"Yes\"",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "PayingExtraInfo"
    )
    private List<Element<PayingMoneyDetails>> payingMoneyDetails;

}



