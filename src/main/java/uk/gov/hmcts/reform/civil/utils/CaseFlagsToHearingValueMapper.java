package uk.gov.hmcts.reform.civil.utils;

import uk.gov.hmcts.reform.civil.model.caseflags.FlagDetail;

import java.util.List;
import java.util.stream.Collectors;

import static uk.gov.hmcts.reform.civil.utils.CaseFlagUtils.filter;

public class CaseFlagsToHearingValueMapper {

    //AC1
    public static boolean hasVulnerableFlag(List<FlagDetail> flagDetails) {
        return filter(flagDetails,CaseFlagPredicates.isActive(), CaseFlagPredicates.hasVulnerableFlag()).stream().count() > 1;
    }

    //AC2
    public static boolean hasAdditionalSecurityFlag(List<FlagDetail> flagDetails) {
        return filter(flagDetails, CaseFlagPredicates.isActive(), CaseFlagPredicates.hasAdditionalSecurityFlag()).stream().count() > 1;
    }

    //AC3
    public static String hasLanguageMisinterpretationFlag(List<FlagDetail> flagDetails) {
        //ToDo: Get child value
        return filter(flagDetails, CaseFlagPredicates.isActive(), CaseFlagPredicates.isHearingRelevant(),
                      CaseFlagPredicates.hasLanguageInterpreterFlag()).stream().findFirst().orElse(null).getName();
    }

    //AC4 AC6
    public static List<String> getReasonableAdjustmentFlagCodes(List<FlagDetail> flagDetails) {
        return filter(flagDetails, CaseFlagPredicates.isActive(), CaseFlagPredicates.getReasonableAdjustmentFlagCodes()).stream().map(flag -> flag.getFlagCode())
            .collect(Collectors.toList());
    }

    //AC5
    public static String getCustodyStatus(List<FlagDetail> flagDetails) {
        return filter(flagDetails, CaseFlagPredicates.isActive(), CaseFlagPredicates.isDetainedIndividual()).stream().count() > 1 ? "C" :  null;
    }
}
