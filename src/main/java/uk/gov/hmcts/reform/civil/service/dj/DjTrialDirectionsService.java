package uk.gov.hmcts.reform.civil.service.dj;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.defaultjudgment.TrialHearingNotes;
import uk.gov.hmcts.reform.civil.model.sdo.TrialOrderMadeWithoutHearingDJ;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class DjTrialDirectionsService {

    private final DjTrialNarrativeService trialNarrativeService;
    private final DjTrialDeadlineService trialDeadlineService;
    private final DjSpecialistDirectionsService specialistDirectionsService;
    private final DjWelshLanguageService welshLanguageService;

    public void populateTrialDirections(CaseData.CaseDataBuilder<?, ?> caseDataBuilder, String judgeNameTitle) {
        caseDataBuilder.trialHearingJudgesRecitalDJ(trialNarrativeService.buildJudgesRecital(judgeNameTitle));
        caseDataBuilder.trialHearingDisclosureOfDocumentsDJ(trialNarrativeService.buildDisclosureOfDocuments());
        caseDataBuilder.trialHearingWitnessOfFactDJ(trialNarrativeService.buildWitnessOfFact());
        caseDataBuilder.trialHearingSchedulesOfLossDJ(trialNarrativeService.buildSchedulesOfLoss());
        caseDataBuilder.trialHearingTrialDJ(trialNarrativeService.buildTrialHearingTrial());
        caseDataBuilder.trialHearingTimeDJ(trialNarrativeService.buildTrialHearingTime());

        LocalDate trialOrderDeadline = trialDeadlineService.plusWorkingDays(5);
        caseDataBuilder.trialOrderMadeWithoutHearingDJ(
            TrialOrderMadeWithoutHearingDJ.builder()
                .input(welshLanguageService.buildOrderMadeWithoutHearingText(trialOrderDeadline))
                .build());

        caseDataBuilder.trialHearingNotesDJ(trialNarrativeService.buildTrialHearingNotes());

        specialistDirectionsService.populateSpecialistDirections(caseDataBuilder);

        caseDataBuilder.sdoR2TrialWelshLanguageDJ(
            welshLanguageService.buildWelshUsage());

        caseDataBuilder.trialHearingDisclosureOfDocumentsDJ(
            trialNarrativeService.buildUpdatedDisclosureOfDocuments());
    }
}
