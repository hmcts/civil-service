package uk.gov.hmcts.reform.civil.service.dj;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.sdo.TrialOrderMadeWithoutHearingDJ;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class DjTrialDirectionsService {

    private final DjTrialNarrativeService trialNarrativeService;
    private final DjDeadlineService trialDeadlineService;
    private final DjSpecialistDirectionsService specialistDirectionsService;
    private final DjWelshLanguageService welshLanguageService;

    public void populateTrialDirections(CaseData caseData, String judgeNameTitle) {
        caseData.setTrialHearingJudgesRecitalDJ(trialNarrativeService.buildJudgesRecital(judgeNameTitle));
        caseData.setTrialHearingDisclosureOfDocumentsDJ(trialNarrativeService.buildDisclosureOfDocuments());
        caseData.setTrialHearingWitnessOfFactDJ(trialNarrativeService.buildWitnessOfFact());
        caseData.setTrialHearingSchedulesOfLossDJ(trialNarrativeService.buildSchedulesOfLoss());
        caseData.setTrialHearingTrialDJ(trialNarrativeService.buildTrialHearingTrial());
        caseData.setTrialHearingTimeDJ(trialNarrativeService.buildTrialHearingTime());

        LocalDate trialOrderDeadline = trialDeadlineService.workingDaysFromNow(5);
        caseData.setTrialOrderMadeWithoutHearingDJ(
            new TrialOrderMadeWithoutHearingDJ()
                .setInput(welshLanguageService.buildOrderMadeWithoutHearingText(trialOrderDeadline))
        );

        caseData.setTrialHearingNotesDJ(trialNarrativeService.buildTrialHearingNotes());

        specialistDirectionsService.populateSpecialistDirections(caseData);

        caseData.setSdoR2TrialWelshLanguageDJ(welshLanguageService.buildWelshUsage());

        caseData.setTrialHearingDisclosureOfDocumentsDJ(
            trialNarrativeService.buildUpdatedDisclosureOfDocuments());
    }
}
