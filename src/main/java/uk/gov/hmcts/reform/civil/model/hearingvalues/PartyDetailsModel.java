package uk.gov.hmcts.reform.civil.model.hearingvalues;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.civil.enums.hearing.PartyType;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
@Builder(toBuilder = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PartyDetailsModel {
    private String partyID;
    private PartyType partyType;
    private String partyName;
    private String partyRole;
    private IndividualDetailsModel individualDetails;
    private OrganisationDetailsModel organisationDetails;
    private List<UnavailabilityDOWModel> unavailabilityDOW;
    private List<UnavailabilityRangeModel> unavailabilityRange;
    private String hearingSubChannel;
}
