package uk.gov.hmcts.reform.civil.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.caseflags.Flags;
import uk.gov.hmcts.reform.civil.model.common.Element;

import java.util.List;

@Data
@Builder(toBuilder = true)
@JsonDeserialize(builder = LitigationFriend.LitigationFriendBuilder.class)
@JsonIgnoreProperties(ignoreUnknown = true)
public class LitigationFriend {

    private final String partyID;

    // CIV-5557 to be removed
    private final String fullName;
    private final String firstName;
    private final String lastName;
    private final String emailAddress;
    private final String phoneNumber;
    private final YesOrNo hasSameAddressAsLitigant;
    private final Address primaryAddress;
    private final List<Element<DocumentWithRegex>> certificateOfSuitability;
    private final Flags flags;

    @JsonPOJOBuilder(withPrefix = "")
    public static class LitigationFriendBuilder {

    }
}
