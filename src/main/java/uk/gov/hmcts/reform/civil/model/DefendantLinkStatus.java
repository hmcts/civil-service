package uk.gov.hmcts.reform.civil.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class DefendantLinkStatus {

    private boolean isOcmcCase;
    private boolean linked;
}
