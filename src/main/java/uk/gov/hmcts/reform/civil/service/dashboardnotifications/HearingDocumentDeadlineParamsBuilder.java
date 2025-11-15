package uk.gov.hmcts.reform.civil.service.dashboardnotifications;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.sdo.DisposalHearingDisclosureOfDocuments;
import uk.gov.hmcts.reform.civil.model.sdo.FastTrackDisclosureOfDocuments;
import uk.gov.hmcts.reform.civil.service.sdo.SdoCaseClassificationService;
import uk.gov.hmcts.reform.civil.utils.DateUtils;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class HearingDocumentDeadlineParamsBuilder extends DashboardNotificationsParamsBuilder {

    private final SdoCaseClassificationService sdoCaseClassificationService;

    @Override
    public void addParams(CaseData caseData, HashMap<String, Object> params) {
        Optional<LocalDate> hearingDocumentDeadline = getHearingDocumentDeadline(caseData);
        hearingDocumentDeadline.ifPresent(date -> {
            params.put("sdoDocumentUploadRequestedDateEn", DateUtils.formatDate(date));
            params.put("sdoDocumentUploadRequestedDateCy", DateUtils.formatDateInWelsh(date, false));
        });
    }

    Optional<LocalDate> getHearingDocumentDeadline(CaseData caseData) {
        if (sdoCaseClassificationService.isSmallClaimsTrack(caseData)) {
            return Optional.empty();
        } else if (sdoCaseClassificationService.isFastTrack(caseData)) {
            return Optional.ofNullable(caseData.getFastTrackDisclosureOfDocuments())
                .map(FastTrackDisclosureOfDocuments::getDate3);
        } else {
            return Optional.ofNullable(caseData.getDisposalHearingDisclosureOfDocuments())
                .map(DisposalHearingDisclosureOfDocuments::getDate2);
        }
    }
}
