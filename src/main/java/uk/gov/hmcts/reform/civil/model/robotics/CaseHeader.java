package uk.gov.hmcts.reform.civil.model.robotics;

import lombok.Data;

@Data
public class CaseHeader {

    private String caseNumber;
    private String owningCourtCode;
    private String owningCourtName;
    private String caseType;
    private String preferredCourtCode;
    private String preferredCourtName;
    private String caseAllocatedTo;
}
