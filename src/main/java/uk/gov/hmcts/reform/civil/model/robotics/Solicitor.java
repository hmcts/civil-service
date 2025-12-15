package uk.gov.hmcts.reform.civil.model.robotics;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;

@Accessors(chain = true)
@Data
@SuperBuilder
@AllArgsConstructor
public class Solicitor {

    @JsonProperty("ID")
    private String id;
    @JsonProperty("organisationID")
    private String organisationId;
    private String name;
    private RoboticsAddresses addresses;
    private String contactDX;
    private String contactTelephoneNumber;
    private String contactFaxNumber;
    private String contactEmailAddress;
    private String preferredMethodOfCommunication;
    private String reference;
    @JsonProperty("isPayee")
    private boolean isPayee;
}
