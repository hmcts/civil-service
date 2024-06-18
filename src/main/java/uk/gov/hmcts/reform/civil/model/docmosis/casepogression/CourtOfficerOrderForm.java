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

    private final String caseNumber;
    private final String claimant1Name;
    private final String claimant2Name;
    private final String defendant1Name;
    private final String defendant2Name;
    private final String claimantNum;
    private final String defendantNum;
    private final String claimantReference;
    private final String defendantReference;
    private final String courtLocation;
    private final String courtName;
    private final String ordered;
}
