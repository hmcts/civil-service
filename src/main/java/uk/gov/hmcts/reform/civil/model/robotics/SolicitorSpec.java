package uk.gov.hmcts.reform.civil.model.robotics;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.SuperBuilder;

@SuperBuilder
public class SolicitorSpec extends Solicitor {

    public RoboticsAddresses getCorrespondenceAddresses() {
        return correspondenceAddresses;
    }

    public void setCorrespondenceAddresses(RoboticsAddresses correspondenceAddresses) {
        this.correspondenceAddresses = correspondenceAddresses;
    }

    private RoboticsAddresses correspondenceAddresses;
}
