package uk.gov.hmcts.reform.civil.utils;

import uk.gov.hmcts.reform.civil.model.CaseData;

import java.util.function.Predicate;

public class PredicateUtils {

    private PredicateUtils() {
        //NO-OP
    }

    private static final Predicate<CaseData> defendant2ExtensionOnly = caseData ->
        caseData.getRespondent1TimeExtensionDate() == null && caseData.getRespondent2TimeExtensionDate() != null;

    private static final Predicate<CaseData> defendant2ExtensionAfterDefendant1 = caseData ->
        caseData.getRespondent1TimeExtensionDate() != null
            && caseData.getRespondent2TimeExtensionDate() != null && caseData.getRespondent2TimeExtensionDate()
            .isAfter(caseData.getRespondent1TimeExtensionDate());

    public static final Predicate<CaseData> defendant2Extension = caseData ->
        (defendant2ExtensionOnly.or(defendant2ExtensionAfterDefendant1)).test(caseData);
}
