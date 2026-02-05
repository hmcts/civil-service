package uk.gov.hmcts.reform.civil.service.docmosis.dj;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.enums.dj.DisposalHearingMethodDJ;
import uk.gov.hmcts.reform.civil.enums.dj.HearingMethodTelephoneHearingDJ;
import uk.gov.hmcts.reform.civil.enums.dj.HearingMethodVideoConferenceDJ;
import uk.gov.hmcts.reform.civil.model.CaseData;

import static uk.gov.hmcts.reform.civil.enums.dj.DisposalHearingMethodDJ.disposalHearingMethodInPerson;

/**
 * Encapsulates telephone/video organiser strings and "in person" guards for DJ templates.
 */
@Service
public class DjHearingMethodFieldService {

    public String resolveTelephoneOrganisedBy(CaseData caseData) {
        HearingMethodTelephoneHearingDJ disposalTelephone = caseData.getDisposalHearingMethodTelephoneHearingDJ();
        if (disposalTelephone != null) {
            return disposalTelephone.getLabel();
        }

        HearingMethodTelephoneHearingDJ trialTelephone = caseData.getTrialHearingMethodTelephoneHearingDJ();
        if (trialTelephone != null) {
            return trialTelephone.getLabel();
        }

        return null;
    }

    public String resolveVideoOrganisedBy(CaseData caseData) {
        HearingMethodVideoConferenceDJ disposalVideo = caseData.getDisposalHearingMethodVideoConferenceHearingDJ();
        if (disposalVideo != null) {
            return disposalVideo.getLabel();
        }

        HearingMethodVideoConferenceDJ trialVideo = caseData.getTrialHearingMethodVideoConferenceHearingDJ();
        if (trialVideo != null) {
            return trialVideo.getLabel();
        }

        return null;
    }

    public boolean isInPerson(DisposalHearingMethodDJ method) {
        return method != null && method.equals(disposalHearingMethodInPerson);
    }
}
