package uk.gov.hmcts.reform.civil.model.dq;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class Expert {

    private String partyID;
    private String name;
    private String firstName;
    private String lastName;
    private String phoneNumber;
    private String emailAddress;
    private String fieldOfExpertise;
    private String whyRequired;
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private BigDecimal estimatedCost;
    private String eventAdded;
    private LocalDate dateAdded;

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
