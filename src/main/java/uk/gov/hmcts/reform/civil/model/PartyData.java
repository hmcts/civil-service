package uk.gov.hmcts.reform.civil.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import uk.gov.hmcts.reform.civil.enums.PartyRole;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@Accessors(chain = true)
public class PartyData {

    private PartyRole role;
    private Party details;
    private LocalDateTime timeExtensionDate;
    private LocalDate solicitorAgreedDeadlineExtension;

    public PartyData copy() {
        return new PartyData()
            .setRole(role)
            .setDetails(details)
            .setTimeExtensionDate(timeExtensionDate)
            .setSolicitorAgreedDeadlineExtension(solicitorAgreedDeadlineExtension);
    }
}
