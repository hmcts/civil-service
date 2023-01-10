package uk.gov.hmcts.reform.civil.model.docmosis.dq;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Expert {

    private final String name;
    private final String firstName;
    private final String lastName;
    private final String phoneNumber;
    private final String emailAddress;
    private final String fieldOfExpertise;
    private final String whyRequired;
    private final String formattedCost;
}
