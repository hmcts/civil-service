package uk.gov.hmcts.reform.civil.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import lombok.Setter;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;

@Setter
@Data
@Builder(toBuilder = true)
public class GARespondentRepresentative {

    private final YesOrNo generalAppRespondent1Representative;

    @JsonCreator
    GARespondentRepresentative(@JsonProperty("hasAgreed")
                                   YesOrNo generalAppRespondent1Representative) {
        this.generalAppRespondent1Representative = generalAppRespondent1Representative;
    }
}
