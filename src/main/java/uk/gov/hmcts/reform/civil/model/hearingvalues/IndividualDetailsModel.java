package uk.gov.hmcts.reform.civil.model.hearingvalues;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude
@Builder(toBuilder = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class IndividualDetailsModel {

    private String title;
    private String firstName;
    private String lastName;
    private String preferredHearingChannel;
    private String interpreterLanguage;
    private List<String> reasonableAdjustments;
    private boolean vulnerableFlag;
    private String vulnerabilityDetails;
    private List<String> hearingChannelEmail;
    private List<String> hearingChannelPhone;
    private List<RelatedPartiesModel> relatedParties;
    private String custodyStatus;
}
