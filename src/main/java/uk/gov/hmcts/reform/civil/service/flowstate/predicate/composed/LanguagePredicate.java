package uk.gov.hmcts.reform.civil.service.flowstate.predicate.composed;

import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.flowstate.predicate.CaseDataPredicate;
import uk.gov.hmcts.reform.civil.service.flowstate.predicate.annotations.BusinessRule;

import java.util.function.Predicate;

@SuppressWarnings("java:S1214")
public interface LanguagePredicate {

    @BusinessRule(
        group = "Language",
        summary = "Claimant is bilingual",
        description = "Claimant has bilingual language preference")
    Predicate<CaseData> claimantIsBilingual = CaseDataPredicate.Language.isClaimantBilingual;

    @BusinessRule(
        group = "Language",
        summary = "Respondent response marked bilingual",
        description = "Respondent indicated their response is bilingual (translated documents may be present)")
    Predicate<CaseData> respondentIsBilingual = CaseDataPredicate.Language.isRespondentBilingual;

    @BusinessRule(
        group = "Language",
        summary = "Only initial respondent response bilingual",
        description = "Response marked bilingual and language-preference change has not been made")
    Predicate<CaseData> onlyInitialResponseIsBilingual = CaseDataPredicate.Language.isRespondentBilingual
        .and(CaseDataPredicate.Language.hasChangePreference.negate());

}
