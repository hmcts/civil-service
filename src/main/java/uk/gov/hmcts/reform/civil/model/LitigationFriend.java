package uk.gov.hmcts.reform.civil.model;

import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.common.Element;

import java.util.List;

@Data
@Builder
public class LitigationFriend {

    private final String fullName;
    private final YesOrNo hasSameAddressAsLitigant;
    private final Address primaryAddress;
    private final List<Element<DocumentWithRegex>> certificateOfSuitability;
    private final List<Element<DocumentWithRegex>> certificateOfSuitabilityNew;
}
