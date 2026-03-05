package uk.gov.hmcts.reform.civil.ras.model;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class RoleRequest {

    private String assignerId;
    private String process;
    private String reference;
    private boolean replaceExisting = false;

}
