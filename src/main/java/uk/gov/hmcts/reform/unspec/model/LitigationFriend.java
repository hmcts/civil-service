package uk.gov.hmcts.reform.unspec.model;

import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.unspec.enums.YesOrNo;
import uk.gov.hmcts.reform.unspec.model.common.Element;
import uk.gov.hmcts.reform.unspec.model.documents.Document;

import java.util.List;

@Data
@Builder
public class LitigationFriend {

    private final String fullName;
    private final YesOrNo hasSameAddressAsLitigant;
    private final Address primaryAddress;
    private final List<Element<Document>> certificateOfSuitability;
}
