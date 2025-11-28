package uk.gov.hmcts.reform.civil.service.flowstate.predicate;

import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.flowstate.predicate.annotations.BusinessRule;

import java.util.function.Predicate;

@SuppressWarnings("java:S1214")
public non-sealed interface LanguagePredicate extends CaseDataPredicate {

    @BusinessRule(
        group = "Language",
        summary = "Claimant is bilingual",
        description = "Claimant has bilingual language preference")
    Predicate<CaseData> claimantIsBilingual = CaseDataPredicate.Language.isClaimantBilingual;

    @BusinessRule(
        group = "Language",
        summary = "Respondent response marked bilingual",
        description = "Respondent indicated their response is bilingual")
    Predicate<CaseData> respondentIsBilingual = CaseDataPredicate.Language.isRespondentBilingual;

    @BusinessRule(
        group = "Language",
        summary = "Respondent response language is bilingual (initial only)",
        description = "Respondent marked their response as bilingual but has not requested ongoing bilingual processing")
    Predicate<CaseData> onlyInitialResponseIsBilingual = CaseDataPredicate.Language.isRespondentBilingual
        .and(CaseDataPredicate.Language.hasChangePreference.negate());

}
