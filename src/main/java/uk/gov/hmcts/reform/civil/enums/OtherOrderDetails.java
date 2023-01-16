package uk.gov.hmcts.reform.civil.enums;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder(toBuilder = true)
public class OtherOrderDetails {

    private final String createdBy;
    private final String orderCreatedDate;
    private final LocalDateTime orderAmendedDate;
    private final String orderMadeDate;
    private final String orderRecipients;
}
