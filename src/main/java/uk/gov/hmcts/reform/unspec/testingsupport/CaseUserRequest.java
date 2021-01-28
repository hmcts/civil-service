package uk.gov.hmcts.reform.unspec.testingsupport;

import lombok.Data;

@Data
public class CaseUserRequest {

    private String caseId;
    private String userId;
    private String[] caseRoles;
}
