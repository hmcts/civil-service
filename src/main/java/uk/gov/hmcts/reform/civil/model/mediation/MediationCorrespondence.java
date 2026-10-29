package uk.gov.hmcts.reform.civil.model.mediation;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import uk.gov.hmcts.reform.civil.enums.mediation.MediationCorrespondenceType;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class MediationCorrespondence {

    private String partiesInvolved;
    private MediationCorrespondenceType correspondenceType;
    private LocalDate correspondenceDate;
    private String summary;
    private String createdBy;
    private LocalDateTime createdOn;
}
