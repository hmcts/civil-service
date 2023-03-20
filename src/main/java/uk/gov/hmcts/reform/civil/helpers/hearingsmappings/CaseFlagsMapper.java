package uk.gov.hmcts.reform.civil.helpers.hearingsmappings;

import org.postgresql.core.Tuple;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.caseflags.FlagDetail;
import uk.gov.hmcts.reform.civil.model.caseflags.Flags;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.model.hearingvalues.CaseFlags;
import uk.gov.hmcts.reform.civil.model.hearingvalues.IndividualDetailsModel;
import uk.gov.hmcts.reform.civil.model.hearingvalues.PartyFlagsModel;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static uk.gov.hmcts.reform.civil.utils.CaseFlagsHearingsUtils.getAllActiveFlags;
import static uk.gov.hmcts.reform.civil.utils.CaseFlagsHearingsUtils.getAllHearingRelevantCaseFlags;

public class CaseFlagsMapper {

    private static final String ACTIVE_STATUS = "Active";

    private CaseFlagsMapper() {
        //NO-OP
    }

    public static CaseFlags getCaseFlags(CaseData caseData) {
        List<Flags> allActiveFlags = getAllActiveFlags(caseData);
        getAllHearingRelevantCaseFlags(allActiveFlags);

        if (allActiveFlags.isEmpty()) {
            return CaseFlags.builder()
                .flags(List.of(PartyFlagsModel.builder()
                           .build()))
                .build();
        }

        List<PartyFlagsModel> partyFlagsModelList = new ArrayList<>();

        for (Flags activeFlag : allActiveFlags) {
            String partyName = activeFlag.getPartyName();
            for (Element<FlagDetail> flagDetail : activeFlag.getDetails()) {
                PartyFlagsModel partyFlagModel = PartyFlagsModel.builder()
                    .partyID("") // todo civ-7029
                    .partyName(partyName)
                    .flagParentId("")
                    .flagId(flagDetail.getValue().getFlagCode())
                    .flagDescription(flagDetail.getValue().getName())
                    .flagStatus(ACTIVE_STATUS)
                    .build();
                partyFlagsModelList.add(partyFlagModel);
            }
        }

        return CaseFlags.builder()
            .flags(partyFlagsModelList)
            .build();
    }

    private static List<String> VULNERABLE_FLAGS = List.of("PF0002", "RA0033", "RA0026");
    private static List<String> ADDITIONAL_SECURITY_FLAGS = List.of("PF0007");
    private static List<String> LANGUAGE_INTERPRETER_FLAGS = List.of("PF0015");
    private static List<String> REASONABLE_ADJUSTMENT_FLAGS = List.of("RA", "SM");
    private static List<String> DETAINED_INDIVIDUAL_FLAGS = List.of("RA0042");

    private boolean hasFlags(List<FlagDetail> partyFlags, List<String> flagCodeFilter) {
        return getActiveFlags(partyFlags, flagCodeFilter).stream().count() > 0;
    }

    private List<FlagDetail> getActiveFlags(List<FlagDetail> partyFlags, List<String> flagCodeFilter) {
        return partyFlags.stream().filter(
            flag -> flag.getStatus().equals(ACTIVE_STATUS)
                && flagCodeFilter.contains(flag.getFlagCode())).collect(Collectors.toList());
    }

    private List<FlagDetail> getActiveHearingFlags(List<FlagDetail> partyFlags, List<String> flagCodeFilter) {
        return getActiveFlags(partyFlags, flagCodeFilter).stream().filter(
            flag -> YesOrNo.YES.equals(flag.getHearingRelevant())).collect(Collectors.toList());
    }

    private boolean hasHearingRelevantFlags(List<FlagDetail> partyFlags, List<String> flagCodeFilter) {
        return getActiveHearingFlags(partyFlags, flagCodeFilter).stream().count() > 0;
    }

    private boolean matchesFlag(FlagDetail flag, List<String> flagsFilter) {
        return flag.getStatus().equals(ACTIVE_STATUS)
            && flagsFilter.contains(flag.getFlagCode());
    }

//    private boolean exactMatch(List<String> flagsFilter) {
//        return flagsFilter.stream().filter(flagCode -> flagCode.length() < 6).toArray().length > 0;
//    }



}
