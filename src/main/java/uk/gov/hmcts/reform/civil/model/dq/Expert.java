package uk.gov.hmcts.reform.civil.model.dq;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
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
        return new Expert()
            .setName(expertDetails.getExpertName())
            .setFirstName(expertDetails.getFirstName())
            .setLastName(expertDetails.getLastName())
            .setPhoneNumber(expertDetails.getPhoneNumber())
            .setEmailAddress(expertDetails.getEmailAddress())
            .setFieldOfExpertise(expertDetails.getFieldofExpertise())
            .setWhyRequired(expertDetails.getWhyRequired())
            .setEstimatedCost(expertDetails.getEstimatedCost());
    }
}
