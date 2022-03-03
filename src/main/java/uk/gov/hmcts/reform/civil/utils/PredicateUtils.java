package uk.gov.hmcts.reform.civil.utils;

import uk.gov.hmcts.reform.civil.model.CaseData;

import java.util.function.Predicate;

public class PredicateUtils {

    private PredicateUtils() {
        //NO-OP
    }

    public static final Predicate<CaseData> defendant1ExtensionExists = caseData ->
        caseData.getRespondent1TimeExtensionDate() != null;

    public static final Predicate<CaseData> defendant2ExtensionExists = caseData ->
        caseData.getRespondent2TimeExtensionDate() != null;
}
