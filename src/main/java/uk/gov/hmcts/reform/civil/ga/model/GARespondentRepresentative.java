package uk.gov.hmcts.reform.civil.ga.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;

@Setter
@Data
@Accessors(chain = true)
@NoArgsConstructor
public class GARespondentRepresentative {

    private YesOrNo generalAppRespondent1Representative;

    @JsonCreator
    GARespondentRepresentative(@JsonProperty("hasAgreed")
                                   YesOrNo generalAppRespondent1Representative) {
        this.generalAppRespondent1Representative = generalAppRespondent1Representative;
    }
}
