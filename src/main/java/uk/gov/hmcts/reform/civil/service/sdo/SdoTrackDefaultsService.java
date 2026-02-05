package uk.gov.hmcts.reform.civil.service.sdo;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.constants.SdoR2UiConstantFastTrack;
import uk.gov.hmcts.reform.civil.constants.SdoR2UiConstantSmallClaim;
import uk.gov.hmcts.reform.civil.enums.sdo.FastTrackMethod;
import uk.gov.hmcts.reform.civil.enums.sdo.IncludeInOrderToggle;
import uk.gov.hmcts.reform.civil.enums.sdo.OrderDetailsPagesSectionsToggle;
import uk.gov.hmcts.reform.civil.enums.sdo.SmallClaimsMethod;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.sdo.PPI;
import uk.gov.hmcts.reform.civil.model.sdo.SdoR2WelshLanguageUsage;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static uk.gov.hmcts.reform.civil.handler.callback.user.CreateSDOCallbackHandler.DEFAULT_PENAL_NOTICE;

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

    public void applyBaseTrackDefaults(CaseData caseData) {
        initialiseTrackDefaults(caseData);
        sdoJourneyToggleService.applyJourneyFlags(caseData);

        List<OrderDetailsPagesSectionsToggle> checkList = List.of(OrderDetailsPagesSectionsToggle.SHOW);
        sdoChecklistService.applyOrderChecklists(caseData, checkList);
        sdoJudgementDeductionService.populateJudgementDeductionValues(caseData);

        sdoDisposalOrderDefaultsService.populateDisposalOrderDetails(caseData);
        sdoFastTrackOrderDefaultsService.populateFastTrackOrderDetails(caseData);
        sdoSmallClaimsOrderDefaultsService.populateSmallClaimsOrderDetails(caseData, checkList);

        sdoExpertEvidenceFieldsService.populateFastTrackExpertEvidence(caseData);
        sdoDisclosureOfDocumentsFieldsService.populateFastTrackDisclosureOfDocuments(caseData);

        populatePenalNoticeFields(caseData);
        populatePpiFields(caseData);
    }

    private void populatePenalNoticeFields(CaseData caseData) {
        caseData.setSmallClaimsPenalNotice(DEFAULT_PENAL_NOTICE);
        caseData.setFastTrackPenalNotice(DEFAULT_PENAL_NOTICE);
        caseData.setSmallClaimsPenalNoticeToggle(new ArrayList<>());
        caseData.setFastTrackPenalNoticeToggle(new ArrayList<>());
    }

    private void populatePpiFields(CaseData caseData) {
        PPI ppi = new PPI();
        ppi.setPpiDate(LocalDate.now().plusDays(28));
        ppi.setText(SdoR2UiConstantSmallClaim.PPI_DESCRIPTION);
        caseData.setSmallClaimsPPI(ppi);
        caseData.setFastTrackPPI(ppi);
    }

    public void applyR2Defaults(CaseData caseData) {
        sdoChecklistService.applyR2Checklists(caseData, INCLUDE_IN_ORDER_TOGGLE);
        caseData.setSdoR2FastTrackUseOfWelshLanguage(SdoR2WelshLanguageUsage.builder().description(
            SdoR2UiConstantFastTrack.WELSH_LANG_DESCRIPTION).build());
        caseData.setSdoR2SmallClaimsUseOfWelshLanguage(SdoR2WelshLanguageUsage.builder().description(
            SdoR2UiConstantFastTrack.WELSH_LANG_DESCRIPTION).build());
        caseData.setSdoR2DisposalHearingUseOfWelshLanguage(SdoR2WelshLanguageUsage.builder().description(
            SdoR2UiConstantFastTrack.WELSH_LANG_DESCRIPTION).build());
    }

    public List<IncludeInOrderToggle> defaultIncludeInOrderToggle() {
        return INCLUDE_IN_ORDER_TOGGLE;
    }

    private void initialiseTrackDefaults(CaseData caseData) {
        caseData.setSmallClaimsMethod(SmallClaimsMethod.smallClaimsMethodInPerson);
        caseData.setFastTrackMethod(FastTrackMethod.fastTrackMethodInPerson);
    }

}
