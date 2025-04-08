package uk.gov.hmcts.reform.civil.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum QueryCollectionType {
    APPLICANT_SOLICITOR_QUERIES,
    RESPONDENT_SOLICITOR_ONE_QUERIES,
    RESPONDENT_SOLICITOR_TWO_QUERIES,
    CLAIMANT
}
