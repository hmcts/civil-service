package uk.gov.hmcts.reform.civil.service.sdo;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.enums.sdo.SmallClaimsMethodTelephoneHearing;
import uk.gov.hmcts.reform.civil.enums.sdo.SmallClaimsMethodVideoConferenceHearing;
import uk.gov.hmcts.reform.civil.enums.sdo.SmallClaimsTimeEstimate;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.SmallClaimsMediation;
import uk.gov.hmcts.reform.civil.model.sdo.SmallClaimsHearing;
import uk.gov.hmcts.reform.civil.model.sdo.SdoR2SmallClaimsMediation;

import java.util.Locale;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class SdoSmallClaimsTemplateFieldService {

    private static final String MINUTES = " minutes";
    private static final String OTHER = "Other";
    private final SdoMediationSectionService mediationSectionService;

    public String getHearingTimeLabel(CaseData caseData) {
        SmallClaimsHearing hearing = caseData.getSmallClaimsHearing();
        if (Optional.ofNullable(hearing)
            .map(SmallClaimsHearing::getTime)
            .map(SmallClaimsTimeEstimate::getLabel).isEmpty()) {
            return "";
        }

        if (OTHER.equals(hearing.getTime().getLabel())) {
            StringBuilder otherLength = new StringBuilder();
            if (hearing.getOtherHours() != null) {
                otherLength.append(hearing.getOtherHours().toString().trim()).append(" hours ");
            }
            if (hearing.getOtherMinutes() != null) {
                otherLength.append(hearing.getOtherMinutes().toString().trim()).append(MINUTES);
            }
            return otherLength.toString().trim();
        }

        return hearing.getTime().getLabel().toLowerCase(Locale.ROOT);
    }

    public String getMethodTelephoneHearingLabel(CaseData caseData) {
        SmallClaimsMethodTelephoneHearing value = caseData.getSmallClaimsMethodTelephoneHearing();
        return value != null ? value.getLabel() : "";
    }

    public String getMethodVideoConferenceHearingLabel(CaseData caseData) {
        SmallClaimsMethodVideoConferenceHearing value = caseData.getSmallClaimsMethodVideoConferenceHearing();
        return value != null ? value.getLabel() : "";
    }

    public boolean showMediationSection(CaseData caseData, boolean carmEnabled) {
        return standardMediation(caseData, carmEnabled).show();
    }

    public String getMediationText(CaseData caseData) {
        return standardMediation(caseData, true).text();
    }

    public boolean showMediationSectionDrh(CaseData caseData, boolean carmEnabled) {
        return drhMediation(caseData, carmEnabled).show();
    }

    public String getMediationTextDrh(CaseData caseData) {
        return drhMediation(caseData, true).text();
    }

    private SdoMediationSectionService.MediationSection standardMediation(CaseData caseData, boolean carmEnabled) {
        SmallClaimsMediation mediation = caseData.getSmallClaimsMediationSectionStatement();
        return mediationSectionService.resolve(mediation, carmEnabled, SmallClaimsMediation::getInput);
    }

    private SdoMediationSectionService.MediationSection drhMediation(CaseData caseData, boolean carmEnabled) {
        SdoR2SmallClaimsMediation mediation = caseData.getSdoR2SmallClaimsMediationSectionStatement();
        return mediationSectionService.resolve(mediation, carmEnabled, SdoR2SmallClaimsMediation::getInput);
    }
}
