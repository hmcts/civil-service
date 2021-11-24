package uk.gov.hmcts.reform.civil.model.dq;

import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.SmallClaimUnavailableDate;
import uk.gov.hmcts.reform.civil.model.common.Element;

import java.util.List;

@Data
@Builder(toBuilder = true)
public class SmallClaimHearing {

    private final YesOrNo unavailableDatesRequired;
    private final List<Element<SmallClaimUnavailableDate>> smallClaimUnavailableDate;

}
