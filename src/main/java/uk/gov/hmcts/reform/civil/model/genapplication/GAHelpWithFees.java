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
public class GAHelpWithFees {

    private YesOrNo helpWithFee;
    private String helpWithFeesReferenceNumber;

    @JsonCreator
    GAHelpWithFees(@JsonProperty("isWithNotice") YesOrNo helpWithFee,
                   @JsonProperty("helpWithFeesReferenceNumber") String helpWithFeesReferenceNumber) {
        this.helpWithFee = helpWithFee;
        this.helpWithFeesReferenceNumber = helpWithFeesReferenceNumber;
    }
}
