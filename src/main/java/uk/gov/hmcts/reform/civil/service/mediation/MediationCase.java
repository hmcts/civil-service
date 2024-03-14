package uk.gov.hmcts.reform.civil.service.mediation;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Builder
public class MediationCase {

    private Long ccdCaseNumber;
    private String casemanCaseNumber;
    private String caseTitle;
    private boolean caseFlags;
    private String claimValue;
    private List<MediationLitigant> litigants;

}
