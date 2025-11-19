package uk.gov.hmcts.reform.civil.model;

import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.civil.enums.PartyRole;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder(toBuilder = true)
public class PartyData {

    private PartyRole role;
    private Party details;
    private LocalDateTime timeExtensionDate;
    private LocalDate solicitorAgreedDeadlineExtension;
}
