package uk.gov.hmcts.reform.civil.model.dq;

import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.common.Element;

import java.util.List;

@Data
@lombok.AllArgsConstructor
@lombok.NoArgsConstructor
@Builder(toBuilder = true)
public class Witnesses {

    private YesOrNo witnessesToAppear;
    private List<Element<Witness>> details;
}
