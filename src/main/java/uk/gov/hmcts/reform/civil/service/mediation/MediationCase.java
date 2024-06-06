package uk.gov.hmcts.reform.civil.service.mediation;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder(toBuilder = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MediationCase {

    private Long ccdCaseNumber;
    private String casemanCaseNumber;
    private String caseTitle;
    private boolean caseFlags;
    private BigDecimal claimValue;
    private List<MediationLitigant> litigants;

}
