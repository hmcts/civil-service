package uk.gov.hmcts.reform.unspec.model;

import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.unspec.enums.ServedDocuments;
import uk.gov.hmcts.reform.unspec.enums.ServiceMethod;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class CaseData {

    private final ClaimValue claimValue;
    private final ServiceMethod serviceMethod;
    private final LocalDate serviceDate;
    private final LocalDate deemedDateOfService;
    private final LocalDateTime responseDeadline;
    private final List<ServedDocuments> servedDocuments;
}
