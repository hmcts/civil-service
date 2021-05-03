package uk.gov.hmcts.reform.civil.model.dq;

import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.common.Element;

import java.util.List;

@Data
@Builder
public class Witnesses {

    private final YesOrNo witnessesToAppear;
    private final List<Element<Witness>> details;
}
