package uk.gov.hmcts.reform.civil.helpers.hearingsmappings;

import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.caseflags.FlagDetail;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.model.hearingvalues.CaseFlags;
import uk.gov.hmcts.reform.civil.model.caseflags.PartyFlags;
import uk.gov.hmcts.reform.civil.model.hearingvalues.PartyFlagsModel;

import java.util.ArrayList;
import java.util.List;

import static uk.gov.hmcts.reform.civil.utils.CaseFlagsHearingsUtils.getAllActiveFlags;
import static uk.gov.hmcts.reform.civil.utils.CaseFlagsHearingsUtils.getAllHearingRelevantCaseFlags;

public class CaseFlagsMapper {

    private static final String ACTIVE_STATUS = "Active";

    private CaseFlagsMapper() {
        //NO-OP
    }

    public static CaseFlags getCaseFlags(CaseData caseData) {
        List<PartyFlags> allActiveFlags = getAllActiveFlags(caseData);
        getAllHearingRelevantCaseFlags(allActiveFlags);

        if (allActiveFlags.isEmpty()) {
            return CaseFlags.builder()
                .flags(List.of(PartyFlagsModel.builder()
                           .build()))
                .build();
        }

        List<PartyFlagsModel> partyFlagsModelList = new ArrayList<>();

        for (PartyFlags activeFlag : allActiveFlags) {
            String partyName = activeFlag.getPartyName();
            for (Element<FlagDetail> flagDetail : activeFlag.getDetails()) {
                PartyFlagsModel partyFlagModel = PartyFlagsModel.builder()
                    .partyID(activeFlag.getPartyId())
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
}
