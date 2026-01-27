package uk.gov.hmcts.reform.civil.model.dq;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.UnavailableDate;
import uk.gov.hmcts.reform.civil.model.common.Element;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class SmallClaimHearing {

    private YesOrNo unavailableDatesRequired;
    private List<Element<UnavailableDate>> smallClaimUnavailableDate;

    public SmallClaimHearing copy() {
        return new SmallClaimHearing()
            .setUnavailableDatesRequired(unavailableDatesRequired)
            .setSmallClaimUnavailableDate(smallClaimUnavailableDate);
    }

}
