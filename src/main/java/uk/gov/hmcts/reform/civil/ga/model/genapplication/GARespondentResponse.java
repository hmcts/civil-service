package uk.gov.hmcts.reform.civil.ga.model.genapplication;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.common.MappableObject;
import uk.gov.hmcts.reform.civil.model.genapplication.GAHearingDetails;

@Data
@Accessors(chain = true)
@NoArgsConstructor
public class GARespondentResponse implements MappableObject {

    private GAHearingDetails gaHearingDetails;
    private YesOrNo generalAppRespondent1Representative;
    private String gaRespondentDetails;
    private String gaRespondentResponseReason;

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
