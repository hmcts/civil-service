package uk.gov.hmcts.reform.unspec.model.dq;

import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.unspec.enums.YesOrNo;
import uk.gov.hmcts.reform.unspec.model.common.Element;

import java.util.List;

@Data
@Builder
public class Witnesses {

    private final YesOrNo witnessesToAppear;
    private final List<Element<Witness>> details;
}
