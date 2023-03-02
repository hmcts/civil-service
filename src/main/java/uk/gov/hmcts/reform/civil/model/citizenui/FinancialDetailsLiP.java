package uk.gov.hmcts.reform.civil.model.citizenui;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FinancialDetailsLiP {

    private YesOrNo partnerPensionLiP;
    private YesOrNo partnerDisabilityLiP;
    private YesOrNo partnerSevereDisabilityLiP;
    private String childrenEducationLiP;
}
