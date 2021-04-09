package uk.gov.hmcts.reform.unspec.model.robotics;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class EventDetails {

    private String miscText;
}
