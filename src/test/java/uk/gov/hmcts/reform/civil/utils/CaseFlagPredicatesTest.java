package uk.gov.hmcts.reform.civil.utils;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.civil.model.caseflags.FlagDetail;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;

public class CaseFlagPredicatesTest {
    @Test
    void isActive_shouldReturnTrueForActiveFlagDetails() {
        FlagDetail inactiveFlagDetail = FlagDetail.builder().status("INACTIVE").build();
        FlagDetail activeFlagDetail = FlagDetail.builder().status("ACTIVE").build();

        assertFalse(CaseFlagPredicates.isActive().test(inactiveFlagDetail));
        assertTrue(CaseFlagPredicates.isActive().test(activeFlagDetail));
    }

    @Test
    void isHearingRelevant_shouldReturnTrueForHearingRelevantFlagDetails() {
        FlagDetail nonHearingRelevantFlagDetail = FlagDetail.builder().hearingRelevant(NO).build();
        FlagDetail hearingRelevantFlagDetail = FlagDetail.builder().hearingRelevant(YES).build();

        assertFalse(CaseFlagPredicates.isHearingRelevant().test(nonHearingRelevantFlagDetail));
        assertTrue(CaseFlagPredicates.isHearingRelevant().test(hearingRelevantFlagDetail));
    }

    @Test
    void hasVulnerableFlag_shouldReturnTrueForVulnerableFlagDetails() {
        FlagDetail nonVulnerableFlagDetail = FlagDetail.builder().flagCode("PF0007").build();
        FlagDetail vulnerableFlagDetail = FlagDetail.builder().flagCode("PF0002").build();

        assertFalse(CaseFlagPredicates.hasVulnerableFlag().test(nonVulnerableFlagDetail));
        assertTrue(CaseFlagPredicates.hasVulnerableFlag().test(vulnerableFlagDetail));
    }

    @Test
    void hasAdditionalSecurityFlag_shouldReturnTrueForAdditionalSecurityFlagDetails() {
        FlagDetail nonAdditionalSecurityFlagDetail = FlagDetail.builder().flagCode("PF0002").build();
        FlagDetail additionalSecurityFlagDetail = FlagDetail.builder().flagCode("PF0007").build();

        assertFalse(CaseFlagPredicates.hasAdditionalSecurityFlag().test(nonAdditionalSecurityFlagDetail));
        assertTrue(CaseFlagPredicates.hasAdditionalSecurityFlag().test(additionalSecurityFlagDetail));
    }

    @Test
    void hasLanguageInterpreterFlag_shouldReturnTrueForLanguageInterpreterFlagDetails() {
        FlagDetail nonLanguageInterpreterFlagDetail = FlagDetail.builder().flagCode("PF0002").build();
        FlagDetail languageInterpreterFlagDetail = FlagDetail.builder().flagCode("PF0015").build();

        assertFalse(CaseFlagPredicates.hasLanguageInterpreterFlag().test(nonLanguageInterpreterFlagDetail));
        assertTrue(CaseFlagPredicates.hasLanguageInterpreterFlag().test(languageInterpreterFlagDetail));
    }

    @Test
    void getReasonableAdjustmentFlagCodes_shouldReturnTrueForReasonableAdjustmentFlagDetails() {
        FlagDetail nonReasonableAdjustmentFlagDetail = FlagDetail.builder().flagCode("PF0002").build();
        FlagDetail reasonableAdjustmentFlagDetail = FlagDetail.builder().flagCode("RA0033").build();
        FlagDetail reasonableAdjustmentFlagDetail2 = FlagDetail.builder().flagCode("SM0033").build();

        assertFalse(CaseFlagPredicates.getReasonableAdjustmentFlagCodes().test(nonReasonableAdjustmentFlagDetail));
        assertTrue(CaseFlagPredicates.getReasonableAdjustmentFlagCodes().test(reasonableAdjustmentFlagDetail));
        assertTrue(CaseFlagPredicates.getReasonableAdjustmentFlagCodes().test(reasonableAdjustmentFlagDetail2));
    }

    @Test
    void isDetainedIndividual_shouldReturnTrueForDetainedIndividualFlagDetails() {
        FlagDetail nonDetainedIndividualFlagDetail = FlagDetail.builder().flagCode("PF0002").build();
        FlagDetail detainedIndividualFlagDetail = FlagDetail.builder().flagCode("PF0019").build();

        assertFalse(CaseFlagPredicates.isDetainedIndividual().test(nonDetainedIndividualFlagDetail));
        assertTrue(CaseFlagPredicates.isDetainedIndividual().test(detainedIndividualFlagDetail));
    }
}
