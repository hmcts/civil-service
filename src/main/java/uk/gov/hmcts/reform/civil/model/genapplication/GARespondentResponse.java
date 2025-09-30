package uk.gov.hmcts.reform.civil.model.genapplication;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.common.MappableObject;

@Data
@Builder(toBuilder = true)
public class GARespondentResponse implements MappableObject {

    private GAHearingDetails gaHearingDetails;
    private final YesOrNo generalAppRespondent1Representative;
    private final String gaRespondentDetails;
    private final String gaRespondentResponseReason;

    @JsonCreator
    GARespondentResponse(@JsonProperty("gaHearingDetails") GAHearingDetails gaHearingDetails,
                         @JsonProperty("generalAppRespondent1Representative")
                             YesOrNo generalAppRespondent1Representative,
                         @JsonProperty("gaRespondentDetails") String gaRespondentDetails,
                         @JsonProperty("gaRespondentResponseReason") String gaRespondentResponseReason) {
        this.gaHearingDetails = gaHearingDetails;
        this.generalAppRespondent1Representative = generalAppRespondent1Representative;
        this.gaRespondentDetails = gaRespondentDetails;
        this.gaRespondentResponseReason = gaRespondentResponseReason;
    }
}
