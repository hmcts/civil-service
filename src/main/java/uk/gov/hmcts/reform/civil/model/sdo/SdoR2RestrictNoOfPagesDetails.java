package uk.gov.hmcts.reform.civil.model.sdo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class SdoR2RestrictNoOfPagesDetails {

    private String witnessShouldNotMoreThanTxt;
    private Integer noOfPages;
    private String fontDetails;


}
