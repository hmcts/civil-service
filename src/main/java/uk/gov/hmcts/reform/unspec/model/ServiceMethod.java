package uk.gov.hmcts.reform.unspec.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.unspec.enums.ServiceMethodType;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ServiceMethod {

    private final ServiceMethodType type;
    private final String dxNumber;
    private final String faxNumber;
    private final String email;
    private final String other;

    public boolean requiresDateEntry() {
        return this.type.requiresDateEntry();
    }

    public LocalDate getDeemedDateOfService(LocalDateTime serviceTime) {
        return this.type.getDeemedDateOfService(serviceTime);
    }
}
