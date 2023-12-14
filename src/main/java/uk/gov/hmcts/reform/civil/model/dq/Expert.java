package uk.gov.hmcts.reform.civil.model.dq;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder(toBuilder = true)
public class Expert {

    private final String partyID;
    private final String name;
    private final String firstName;
    private final String lastName;
    private final String phoneNumber;
    private final String emailAddress;
    private final String fieldOfExpertise;
    private final String whyRequired;
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private final BigDecimal estimatedCost;
    private final String eventAdded;
    private final LocalDate dateAdded;

    public static Expert fromSmallClaimExpertDetails(ExpertDetails expertDetails) {
        return Expert.builder()
            .name(expertDetails.getExpertName())
            .firstName(expertDetails.getFirstName())
            .lastName(expertDetails.getLastName())
            .phoneNumber(expertDetails.getPhoneNumber())
            .emailAddress(expertDetails.getEmailAddress())
            .fieldOfExpertise(expertDetails.getFieldofExpertise())
            .whyRequired(expertDetails.getWhyRequired())
            .estimatedCost(expertDetails.getEstimatedCost())
            .build();
    }
}
