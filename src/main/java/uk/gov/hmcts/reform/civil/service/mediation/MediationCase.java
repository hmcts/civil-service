package uk.gov.hmcts.reform.civil.service.mediation;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class MediationCase {

    private Long ccdCaseNumber;
    private String casemanCaseNumber;
    private String caseTitle;
    private boolean caseFlags;
    private BigDecimal claimValue;
    private List<MediationLitigant> litigants;

}
