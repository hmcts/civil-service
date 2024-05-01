package uk.gov.hmcts.reform.civil.service.mediation;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Builder
@EqualsAndHashCode
@Data
public class MediationUnavailability {

    private String dateFrom;
    private String dateTo;
}
