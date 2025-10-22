package uk.gov.hmcts.reform.civil.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.experimental.SuperBuilder;
import uk.gov.hmcts.reform.civil.model.common.MappableObject;

@SuperBuilder(toBuilder = true)
@Data
public class BaseCaseData implements MappableObject {

    @JsonIgnore
    private String ccdCaseType;
}
