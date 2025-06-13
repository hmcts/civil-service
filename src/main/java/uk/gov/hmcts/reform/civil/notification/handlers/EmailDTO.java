package uk.gov.hmcts.reform.civil.notification.handlers;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class EmailDTO {

    private String targetEmail;
    private String emailTemplate;
    private Map<String, String> parameters;
    private String reference;
}
