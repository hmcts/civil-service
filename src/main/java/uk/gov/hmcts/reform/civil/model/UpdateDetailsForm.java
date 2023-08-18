package uk.gov.hmcts.reform.civil.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.common.DynamicList;
import uk.gov.hmcts.reform.civil.model.common.Element;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateDetailsForm {

    @JsonProperty("partyChosen")
    private DynamicList partyChosen;

    @JsonProperty("additionalUnavailableDates")
    private List<Element<UnavailableDate>> additionalUnavailableDates;

    @JsonProperty("hidePartyChoice")
    private YesOrNo hidePartyChoice;
}
