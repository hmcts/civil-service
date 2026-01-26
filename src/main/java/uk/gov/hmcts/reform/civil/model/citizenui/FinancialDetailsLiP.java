package uk.gov.hmcts.reform.civil.model.citizenui;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class FinancialDetailsLiP {

    private YesOrNo partnerPensionLiP;
    private YesOrNo partnerDisabilityLiP;
    private YesOrNo partnerSevereDisabilityLiP;
    private String childrenEducationLiP;
}
