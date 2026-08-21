package uk.gov.hmcts.reform.civil.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.api.ComplexType;

@ComplexType(name = "RegistrationTypeInformation", generate = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegistrationInformation {

    @CCD(label = "registration type", searchable = false)
    private String registrationType;
    @CCD(label = "Judgment created on", searchable = false)
    private LocalDateTime judgmentDateTime;

}
