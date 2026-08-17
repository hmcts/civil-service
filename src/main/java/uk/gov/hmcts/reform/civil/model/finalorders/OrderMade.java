package uk.gov.hmcts.reform.civil.model.finalorders;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;
import uk.gov.hmcts.reform.civil.model.FinalOrderMadeRadioList;
import uk.gov.hmcts.reform.civil.model.SingleDateHeardFinalOrders;
import uk.gov.hmcts.reform.civil.model.DateRangeHeardFinalOrders;
import uk.gov.hmcts.reform.civil.model.BespokeDateHeardFinalOrders;
import uk.gov.hmcts.ccd.sdk.api.ComplexType;

@ComplexType(name = "FinalOrderMadeDatesDropdown", generate = true)
@Accessors(chain = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderMade {

    @CCD(
            label = " ",
            showCondition = "finalOrderMadeRadioList=\"SINGLE_DATE\"",
            searchable = false,
            typeParameterClass = SingleDateHeardFinalOrders.class
    )
    private DatesFinalOrders singleDateSelection;
    @CCD(
            label = " ",
            showCondition = "finalOrderMadeRadioList=\"DATE_RANGE\"",
            searchable = false,
            typeParameterClass = DateRangeHeardFinalOrders.class
    )
    private DatesFinalOrders dateRangeSelection;
    @CCD(
            label = " ",
            showCondition = "finalOrderMadeRadioList=\"BESPOKE_RANGE\"",
            searchable = false,
            typeParameterClass = BespokeDateHeardFinalOrders.class
    )
    private DatesFinalOrders bespokeRangeSelection;

  // ==== ccd-definition-converter: synthesised definition-only fields (retrofit) ====
  @CCD(label = "#### Enter date(s) of hearing", searchable = false, typeOverride = FieldType.Label)
  private String finalOrderMadeRadioListLabel;
  @CCD(label = " ", searchable = false)
  private FinalOrderMadeRadioList finalOrderMadeRadioList;
  // ==== end synthesised definition-only fields ====
}

