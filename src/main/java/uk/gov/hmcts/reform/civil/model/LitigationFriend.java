package uk.gov.hmcts.reform.civil.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.caseflags.Flags;
import uk.gov.hmcts.reform.civil.model.common.Element;

import java.util.List;

@Data
@Builder(toBuilder = true)
@JsonDeserialize(builder = LitigationFriend.LitigationFriendBuilder.class)
@JsonIgnoreProperties(ignoreUnknown = true)
@NoArgsConstructor
@AllArgsConstructor
public class LitigationFriend {

    private String partyID;

    // CIV-5557 to be removed
    private String fullName;
    private String firstName;
    private String lastName;
    private String emailAddress;
    private String phoneNumber;
    private YesOrNo hasSameAddressAsLitigant;
    private Address primaryAddress;
    private List<Element<DocumentWithRegex>> certificateOfSuitability;
    private Flags flags;

    @JsonPOJOBuilder(withPrefix = "")
    public static class LitigationFriendBuilder {

    }
}
