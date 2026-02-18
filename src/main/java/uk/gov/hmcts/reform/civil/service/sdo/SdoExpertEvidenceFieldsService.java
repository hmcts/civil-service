package uk.gov.hmcts.reform.civil.service.sdo;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.sdo.FastTrackPersonalInjury;

@Service
@RequiredArgsConstructor
public class SdoExpertEvidenceFieldsService {

    private final SdoDeadlineService sdoDeadlineService;

    public void populateFastTrackExpertEvidence(CaseData caseData) {
        FastTrackPersonalInjury expertEvidence = new FastTrackPersonalInjury()
            .setInput1("The Claimant has permission to rely upon the written expert evidence already uploaded to the"
                + " Digital Portal with the particulars of claim")
            .setInput2("The Defendant(s) may ask questions of the Claimant's expert which must be sent to the expert "
                + "directly and uploaded to the Digital Portal by 4pm on")
            .setDate2(sdoDeadlineService.nextWorkingDayFromNowDays(14))
            .setInput3("The answers to the questions shall be answered by the Expert by")
            .setDate3(sdoDeadlineService.nextWorkingDayFromNowDays(42))
            .setInput4("and uploaded to the Digital Portal by the party who has asked the question by")
            .setDate4(sdoDeadlineService.nextWorkingDayFromNowDays(49));

        caseData.setFastTrackPersonalInjury(expertEvidence);
    }
}
