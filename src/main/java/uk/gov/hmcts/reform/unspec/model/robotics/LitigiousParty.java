package uk.gov.hmcts.reform.unspec.model.robotics;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
@Builder
public class LitigiousParty {

    private String litigiousPartyType;
    private String litigiousPartyName;
    private RoboticsAddresses litigiousPartyAddresses;
    private String litigiousPartyContactDX;
    private String litigiousPartyContactTelephoneNumber;
    private String litigiousPartyContactFaxNumber;
    private String litigiousPartyContactEmailAddress;
    private String litigiousPartyPreferredMethodOfCommunication;
    private String litigiousPartyWelshTranslation;
    private String litigiousPartyReference;
    private String litigiousPartyDateOfService;
    private String litigiousPartyLastDateForService;
    private LocalDate litigiousPartyDateOfBirth;
    private String litigiousPartySolicitorPartyOrganisationID;
}
