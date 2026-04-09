package uk.gov.hmcts.reform.civil.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;

@Data
@NoArgsConstructor
@Accessors(chain = true)
public class PartnerAndDependentsLRspec {

    private YesOrNo liveWithPartnerRequired;
    private YesOrNo partnerAgedOver;
    private YesOrNo haveAnyChildrenRequired;
    private YesOrNo receiveDisabilityPayments;
    private ChildrenByAgeGroupLRspec howManyChildrenByAgeGroup;
    private YesOrNo supportedAnyoneFinancialRequired;
    private String supportPeopleNumber;
    private String supportPeopleDetails;

    @JsonIgnore
    public boolean hasPartner() {
        return YesOrNo.YES == liveWithPartnerRequired;
    }
}
