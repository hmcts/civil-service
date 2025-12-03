package uk.gov.hmcts.reform.civil.helpers;

import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

import java.util.Optional;

import static uk.gov.hmcts.reform.civil.CaseDefinitionConstants.CASE_TYPE;
import static uk.gov.hmcts.reform.civil.CaseDefinitionConstants.GENERALAPPLICATION_CASE_TYPE;

public final class CaseTypeIdentifier {

    private CaseTypeIdentifier() {
        // Utility class, no instances
    }

    public static boolean isGeneralApplication(final CaseDetails caseDetails) {
        return Optional.ofNullable(caseDetails)
            .map(CaseDetails::getCaseTypeId)
            .map(id -> id.equals(GENERALAPPLICATION_CASE_TYPE))
            .orElse(false);
    }

    public static boolean isCivil(final CaseDetails caseDetails) {
        return Optional.ofNullable(caseDetails)
            .map(CaseDetails::getCaseTypeId)
            .map(id -> id.equals(CASE_TYPE))
            .orElse(false);
    }
}
