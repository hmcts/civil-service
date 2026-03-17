package uk.gov.hmcts.reform.civil.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.common.DynamicList;
import uk.gov.hmcts.reform.civil.model.common.Element;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class UpdateDetailsForm {

    private DynamicList partyChosen;
    private String partyChosenId;
    private String partyChosenType;
    private YesOrNo hidePartyChoice;
    private List<Element<UnavailableDate>> additionalUnavailableDates;
    private List<Element<UpdatePartyDetailsForm>> updateExpertsDetailsForm;
    private List<Element<UpdatePartyDetailsForm>> updateWitnessesDetailsForm;
    private List<Element<UpdatePartyDetailsForm>> updateLRIndividualsForm;
    private List<Element<UpdatePartyDetailsForm>> updateOrgIndividualsForm;
    private YesOrNo manageContactDetailsEventUsed;
}
