package uk.gov.hmcts.reform.civil.model.dq;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Witness {

    private final String name;
    private final String reasonForWitness;
}
