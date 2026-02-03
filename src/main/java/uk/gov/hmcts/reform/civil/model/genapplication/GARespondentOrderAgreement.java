package uk.gov.hmcts.reform.civil.model.genapplication;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;

@Data
@NoArgsConstructor
@Accessors(chain = true)
public class GARespondentOrderAgreement {

    private YesOrNo hasAgreed;

    @JsonCreator
    GARespondentOrderAgreement(@JsonProperty("hasAgreed") YesOrNo hasAgreed) {
        this.hasAgreed = hasAgreed;
    }
}
