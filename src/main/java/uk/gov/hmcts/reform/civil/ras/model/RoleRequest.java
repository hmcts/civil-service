package uk.gov.hmcts.reform.civil.ras.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RoleRequest {

    private String assignerId;
    private String process;
    private String reference;

    @Builder.Default
    private boolean replaceExisting = false;

}
