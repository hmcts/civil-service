package uk.gov.hmcts.reform.civil.model.transferonlinecase;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.civil.model.common.DynamicList;

@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class TocNewCourtLocation {

    private String reasonForTransfer;
    private DynamicList responseCourtLocationList;
}
