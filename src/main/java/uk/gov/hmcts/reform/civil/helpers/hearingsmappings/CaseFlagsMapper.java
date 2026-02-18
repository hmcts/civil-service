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
            CaseFlags caseFlags = new CaseFlags();
            PartyFlagsModel emptyFlag = new PartyFlagsModel();
            caseFlags.setFlags(List.of(emptyFlag));
            return caseFlags;
        }

        List<PartyFlagsModel> partyFlagsModelList = new ArrayList<>();

        for (PartyFlags activeFlag : allActiveFlags) {
            String partyName = activeFlag.getPartyName();
            for (Element<FlagDetail> flagDetail : activeFlag.getDetails()) {
                PartyFlagsModel partyFlagModel = new PartyFlagsModel();
                partyFlagModel.setPartyID(activeFlag.getPartyId());
                partyFlagModel.setPartyName(partyName);
                partyFlagModel.setFlagParentId("");
                partyFlagModel.setFlagId(flagDetail.getValue().getFlagCode());
                partyFlagModel.setFlagDescription(flagDetail.getValue().getName());
                partyFlagModel.setFlagStatus(ACTIVE_STATUS);
                partyFlagModel.setDateTimeCreated(flagDetail.getValue().getDateTimeCreated());
                partyFlagModel.setDateTimeModified(flagDetail.getValue().getDateTimeModified());
                partyFlagsModelList.add(partyFlagModel);
            }
        }

        CaseFlags caseFlags = new CaseFlags();
        caseFlags.setFlags(partyFlagsModelList);
        return caseFlags;
    }
}
