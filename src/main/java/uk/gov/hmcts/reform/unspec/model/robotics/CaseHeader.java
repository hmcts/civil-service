package uk.gov.hmcts.reform.unspec.model.robotics;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CaseHeader {

    private String caseNumber;
    private String owningCourtCode;
    private String owningCourtName;
    private String caseType;
    private String preferredCourtCode;
    private String preferredCourtName;
    private String caseAllocatedTo;
}
