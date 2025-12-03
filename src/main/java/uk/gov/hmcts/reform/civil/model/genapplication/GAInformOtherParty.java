package uk.gov.hmcts.reform.civil.model.genapplication;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Setter;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;

@Setter
@Data
@Builder(toBuilder = true)
@NoArgsConstructor
public class GAInformOtherParty {

    private YesOrNo isWithNotice;
    private String reasonsForWithoutNotice;

    @JsonCreator
    GAInformOtherParty(@JsonProperty("isWithNotice") YesOrNo isWithNotice,
                       @JsonProperty("reasonsForWithoutNotice") String reasonsForWithoutNotice) {
        this.isWithNotice = isWithNotice;
        this.reasonsForWithoutNotice = reasonsForWithoutNotice;
    }
}
