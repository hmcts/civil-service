package uk.gov.hmcts.reform.civil.model.genapplication;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import lombok.Setter;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;

@Setter
@Data
@Builder(toBuilder = true)
public class GARespondentOrderAgreement {

    private final YesOrNo hasAgreed;

    @JsonCreator
    GARespondentOrderAgreement(@JsonProperty("hasAgreed") YesOrNo hasAgreed) {
        this.hasAgreed = hasAgreed;
    }
}
