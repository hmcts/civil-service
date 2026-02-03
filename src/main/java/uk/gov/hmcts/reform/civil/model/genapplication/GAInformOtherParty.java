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
