package uk.gov.hmcts.reform.civil.helpers.hearingsmappings;

import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.caseflags.FlagDetail;
import uk.gov.hmcts.reform.civil.utils.CaseFlagPredicates;

import java.util.List;
import java.util.stream.Collectors;

import static uk.gov.hmcts.reform.civil.utils.CaseFlagUtils.filter;
import static uk.gov.hmcts.reform.civil.utils.CaseFlagUtils.getAllCaseFlags;

public class CaseFlagsToHearingValueMapper {

    private CaseFlagsToHearingValueMapper() {
        //NO-OP
    }

    public static boolean hasVulnerableFlag(List<FlagDetail> flagDetails) {
        return filter(
            flagDetails,
            CaseFlagPredicates.isActive(),
            CaseFlagPredicates.hasVulnerableFlag()
        ).stream().count() > 0;
    }

    public static List<String> getVulnerabilityDetails(List<FlagDetail> flagDetails) {
        return filter(
            flagDetails,
            CaseFlagPredicates.isActive(),
            CaseFlagPredicates.isHearingRelevant(),
            CaseFlagPredicates.hasVulnerableFlag()
        ).stream().map(flagDetail -> String.format(flagDetail.getFlagComment() != null ? "%s - %s;" : "%s;",
                                           flagDetail.getName(),
                                           flagDetail.getFlagComment()))
            .collect(Collectors.toList());
    }

    public static boolean getAdditionalSecurity(List<FlagDetail> flagDetails) {
        return filter(
            flagDetails,
            CaseFlagPredicates.isActive(),
            CaseFlagPredicates.hasAdditionalSecurityFlag()
        ).stream().count() > 0;
    }

    public static String getInterpreterLanguage(List<FlagDetail> flagDetails) {
        var flags = filter(flagDetails, CaseFlagPredicates.isActive(), CaseFlagPredicates.isHearingRelevant(),
                           CaseFlagPredicates.hasLanguageInterpreterFlag()
        );
        return flags.stream().count() > 0 ? flags.get(0).getSubTypeValue() : null;
    }

    public static String getCustodyStatus(List<FlagDetail> flagDetails) {
        return filter(
            flagDetails,
            CaseFlagPredicates.isActive(),
            CaseFlagPredicates.isDetainedIndividual()
        ).stream().count() > 0 ? "C" : null;
    }

    public static boolean hasCaseInterpreterRequiredFlag(CaseData caseData) {
        return filter(
            getAllCaseFlags(caseData),
            CaseFlagPredicates.isActive(),
            CaseFlagPredicates.isHearingRelevant(),
            CaseFlagPredicates.hasCaseInterpreterRequiredFlag()
        ).stream().count() > 0;
    }

    public static List<String> getReasonableAdjustments(List<FlagDetail> flagDetails) {
        return filter(
            flagDetails,
            CaseFlagPredicates.isActive(),
            CaseFlagPredicates.isHearingRelevant(),
            CaseFlagPredicates.hasReasonableAdjustmentFlagCodes()
        ).stream().map(FlagDetail::getFlagCode)
            .collect(Collectors.toList());
    }
}
