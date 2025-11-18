package uk.gov.hmcts.reform.civil.model.docmosis.casepogression;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import uk.gov.hmcts.reform.civil.model.common.MappableObject;

@Getter
@Builder
@AllArgsConstructor
@EqualsAndHashCode
public class CourtOfficerOrderForm implements MappableObject {

    private String caseNumber;
    private String claimant1Name;
    private String claimant2Name;
    private String defendant1Name;
    private String defendant2Name;
    private String claimantNum;
    private String defendantNum;
    private String claimantReference;
    private String defendantReference;
    private String courtLocation;
    private String courtName;
    private String ordered;
}
