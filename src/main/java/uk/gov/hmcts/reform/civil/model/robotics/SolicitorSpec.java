package uk.gov.hmcts.reform.civil.model.robotics;

public class SolicitorSpec extends Solicitor {

    public RoboticsAddresses getCorrespondenceAddresses() {
        return correspondenceAddresses;
    }

    public void setCorrespondenceAddresses(RoboticsAddresses correspondenceAddresses) {
        this.correspondenceAddresses = correspondenceAddresses;
    }

    private RoboticsAddresses correspondenceAddresses;
}
