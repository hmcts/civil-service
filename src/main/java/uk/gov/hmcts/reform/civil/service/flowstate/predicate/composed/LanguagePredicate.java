package uk.gov.hmcts.reform.civil.service.flowstate.predicate.composed;

import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.flowstate.predicate.CaseDataPredicate;
import uk.gov.hmcts.reform.civil.service.flowstate.predicate.annotations.BusinessRule;

import java.util.function.Predicate;

public final class LanguagePredicate {

    @BusinessRule(
        group = "Language",
        summary = "Respondent response marked bilingual",
        description = "Respondent indicated their response is bilingual (translated documents may be present)")
    public static final Predicate<CaseData> responseIsBilingual = CaseDataPredicate.Language.isBilingualFlag;

    @BusinessRule(
        group = "Language",
        summary = "Only initial respondent response bilingual",
        description = "Response marked bilingual and language-preference change has not been made")
    public static final Predicate<CaseData> onlyInitialResponseIsBilingual = CaseDataPredicate.Language.isBilingualFlag
        .and(CaseDataPredicate.Language.hasChangePreference.negate());

    private LanguagePredicate() {
    }

}
