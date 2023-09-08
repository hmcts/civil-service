package uk.gov.hmcts.reform.civil.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.common.DynamicList;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.model.dq.Expert;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import static uk.gov.hmcts.reform.civil.utils.ElementUtils.wrapElements;
@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class UpdateDetailsForm {

    private DynamicList partyChosen;
    private String partyChosenId;
    private String partyChosenType;
    private YesOrNo hidePartyChoice;
    private List<Element<UnavailableDate>> additionalUnavailableDates;
    private List<Element<UpdatePartyDetailsForm>> updateExpertsDetailsForm;
    private List<Element<UpdatePartyDetailsForm>> updateWitnessesDetailsForm;
}

