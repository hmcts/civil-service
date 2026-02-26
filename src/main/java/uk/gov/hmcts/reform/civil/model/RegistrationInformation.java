package uk.gov.hmcts.reform.civil.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegistrationInformation {

    private String registrationType;
    private LocalDateTime judgmentDateTime;

}
