package uk.gov.hmcts.reform.civil.service.sdo;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.constants.SdoR2UiConstantFastTrack;
import uk.gov.hmcts.reform.civil.enums.sdo.FastTrackMethod;
import uk.gov.hmcts.reform.civil.enums.sdo.IncludeInOrderToggle;
import uk.gov.hmcts.reform.civil.enums.sdo.OrderDetailsPagesSectionsToggle;
import uk.gov.hmcts.reform.civil.enums.sdo.SmallClaimsMethod;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.sdo.SdoR2WelshLanguageUsage;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SdoTrackDefaultsService {

    private final SdoJourneyToggleService sdoJourneyToggleService;
    private final SdoChecklistService sdoChecklistService;
    private final SdoDisposalOrderDefaultsService sdoDisposalOrderDefaultsService;
    private final SdoFastTrackOrderDefaultsService sdoFastTrackOrderDefaultsService;
    private final SdoSmallClaimsOrderDefaultsService sdoSmallClaimsOrderDefaultsService;
    private final SdoExpertEvidenceFieldsService sdoExpertEvidenceFieldsService;
    private final SdoDisclosureOfDocumentsFieldsService sdoDisclosureOfDocumentsFieldsService;
    private final SdoJudgementDeductionService sdoJudgementDeductionService;

    private static final List<IncludeInOrderToggle> INCLUDE_IN_ORDER_TOGGLE = List.of(IncludeInOrderToggle.INCLUDE);

    public void applyBaseTrackDefaults(CaseData caseData, CaseData.CaseDataBuilder<?, ?> updatedData) {
        initialiseTrackDefaults(updatedData);
        sdoJourneyToggleService.applyJourneyFlags(caseData, updatedData);

        List<OrderDetailsPagesSectionsToggle> checkList = List.of(OrderDetailsPagesSectionsToggle.SHOW);
        sdoChecklistService.applyOrderChecklists(caseData, updatedData, checkList);
        sdoJudgementDeductionService.populateJudgementDeductionValues(caseData, updatedData);

        sdoDisposalOrderDefaultsService.populateDisposalOrderDetails(updatedData);
        sdoFastTrackOrderDefaultsService.populateFastTrackOrderDetails(updatedData);
        sdoSmallClaimsOrderDefaultsService.populateSmallClaimsOrderDetails(caseData, updatedData, checkList);

        sdoExpertEvidenceFieldsService.populateFastTrackExpertEvidence(updatedData);
        sdoDisclosureOfDocumentsFieldsService.populateFastTrackDisclosureOfDocuments(updatedData);
    }

    public void applyR2Defaults(CaseData caseData, CaseData.CaseDataBuilder<?, ?> updatedData) {
        sdoChecklistService.applyR2Checklists(caseData, updatedData, INCLUDE_IN_ORDER_TOGGLE);
        updatedData.sdoR2FastTrackUseOfWelshLanguage(SdoR2WelshLanguageUsage.builder().description(
            SdoR2UiConstantFastTrack.WELSH_LANG_DESCRIPTION).build());
        updatedData.sdoR2SmallClaimsUseOfWelshLanguage(SdoR2WelshLanguageUsage.builder().description(
            SdoR2UiConstantFastTrack.WELSH_LANG_DESCRIPTION).build());
        updatedData.sdoR2DisposalHearingUseOfWelshLanguage(SdoR2WelshLanguageUsage.builder().description(
            SdoR2UiConstantFastTrack.WELSH_LANG_DESCRIPTION).build());
    }

    public List<IncludeInOrderToggle> defaultIncludeInOrderToggle() {
        return INCLUDE_IN_ORDER_TOGGLE;
    }

    private void initialiseTrackDefaults(CaseData.CaseDataBuilder<?, ?> updatedData) {
        updatedData
            .smallClaimsMethod(SmallClaimsMethod.smallClaimsMethodInPerson)
            .fastTrackMethod(FastTrackMethod.fastTrackMethodInPerson);
    }

}
