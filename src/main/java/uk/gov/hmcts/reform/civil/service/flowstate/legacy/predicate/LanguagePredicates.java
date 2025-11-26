package uk.gov.hmcts.reform.civil.service.flowstate.legacy.predicate;

import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.CaseDataParent;

import java.util.function.Predicate;

/**
 * Cohesive predicates about language.
 * Logic copied as-is from FlowPredicate to avoid functional changes.
 */
public final class LanguagePredicates {

    private LanguagePredicates() {
        // Utility class
    }

    public static final Predicate<CaseData> isRespondentResponseLangIsBilingual =
        CaseDataParent::isRespondentResponseBilingual;

    public static final Predicate<CaseData> onlyInitialRespondentResponseLangIsBilingual = caseData ->
        // dependent lip selected bilingual during response
        caseData.getChangeLanguagePreference() == null && caseData.isRespondentResponseBilingual();
}
