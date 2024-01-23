package uk.gov.hmcts.reform.civil.model.sdo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class SdoR2DisclosureOfDocuments {

    private String standardDisclosureTxt;
    private Date standardDisclosureDate;
    private String inspectionTxt;
    private Date inspectionDate;
    private String requestsWillBeCompiledLabel;

}
