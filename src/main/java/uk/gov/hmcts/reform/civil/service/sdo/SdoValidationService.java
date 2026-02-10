package uk.gov.hmcts.reform.civil.service.sdo;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.enums.sdo.HearingOnRadioOptions;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.sdo.SdoR2AddendumReport;
import uk.gov.hmcts.reform.civil.model.sdo.SdoR2ApplicationToRelyOnFurther;
import uk.gov.hmcts.reform.civil.model.sdo.SdoR2ApplicationToRelyOnFurtherDetails;
import uk.gov.hmcts.reform.civil.model.sdo.SdoR2DisclosureOfDocuments;
import uk.gov.hmcts.reform.civil.model.sdo.SdoR2FurtherAudiogram;
import uk.gov.hmcts.reform.civil.model.sdo.SdoR2WitnessOfFact;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

@Service
@RequiredArgsConstructor
@Slf4j
public class SdoValidationService {

    private static final String ERROR_NUMBER_LESS_THAN_ZERO = "The number entered cannot be less than zero";
    private static final String ERROR_DATE_MUST_BE_IN_FUTURE = "Date must be in the future";

    private final SdoCaseClassificationService caseClassificationService;

    public List<String> validate(CaseData caseData) {
        List<String> errors = new ArrayList<>();

        if (caseData.getSmallClaimsWitnessStatement() != null) {
            String inputValue1 = caseData.getSmallClaimsWitnessStatement().getInput2();
            String inputValue2 = caseData.getSmallClaimsWitnessStatement().getInput3();
            validateNegativeWitness(inputValue1, inputValue2).ifPresent(errors::add);
        } else if (caseData.getFastTrackWitnessOfFact() != null) {
            String inputValue1 = caseData.getFastTrackWitnessOfFact().getInput2();
            String inputValue2 = caseData.getFastTrackWitnessOfFact().getInput3();
            validateNegativeWitness(inputValue1, inputValue2).ifPresent(errors::add);
        } else if (caseClassificationService.isDrhSmallClaim(caseData)) {
            errors.addAll(validateDrhFields(caseData));
        }

        if (caseClassificationService.isNihlFastTrack(caseData)) {
            errors.addAll(validateNihl(caseData));
        }

        errors.addAll(validatePpiDates(caseData));
        errors.addAll(validateHousingDisrepairDates(caseData));

        log.info("Validation complete for caseId {}, total errors {}", caseData.getCcdCaseReference(), errors.size());
        return errors;
    }

    private Optional<String> validateNegativeWitness(String first, String second) {
        if (first != null && second != null) {
            int number1 = Integer.parseInt(first);
            int number2 = Integer.parseInt(second);
            if (number1 < 0 || number2 < 0) {
                return Optional.of(ERROR_NUMBER_LESS_THAN_ZERO);
            }
        }
        return Optional.empty();
    }

    private List<String> validateDrhFields(CaseData caseData) {
        ArrayList<String> errors = new ArrayList<>();
        LocalDate today = LocalDate.now();

        if (caseData.getSdoR2SmallClaimsPPI() != null && caseData.getSdoR2SmallClaimsPPI().getPpiDate() != null) {
            validateFutureDate(caseData.getSdoR2SmallClaimsPPI().getPpiDate(), today).ifPresent(errors::add);
        }

        if (caseData.getSdoR2SmallClaimsWitnessStatements() != null
            && caseData.getSdoR2SmallClaimsWitnessStatements().getIsRestrictWitness() == YesOrNo.YES
            && caseData.getSdoR2SmallClaimsWitnessStatements().getSdoR2SmallClaimsRestrictWitness() != null) {
            Integer noWitnessClaimant = caseData.getSdoR2SmallClaimsWitnessStatements()
                .getSdoR2SmallClaimsRestrictWitness().getNoOfWitnessClaimant();
            Integer noWitnessDefendant = caseData.getSdoR2SmallClaimsWitnessStatements()
                .getSdoR2SmallClaimsRestrictWitness().getNoOfWitnessDefendant();
            Optional.ofNullable(noWitnessClaimant).flatMap(this::validateGreaterThanZero).ifPresent(errors::add);
            Optional.ofNullable(noWitnessDefendant).flatMap(this::validateGreaterThanZero).ifPresent(errors::add);
        }

        if (caseData.getSdoR2SmallClaimsHearing() != null) {
            if (caseData.getSdoR2SmallClaimsHearing().getTrialOnOptions() == HearingOnRadioOptions.OPEN_DATE
                && caseData.getSdoR2SmallClaimsHearing().getSdoR2SmallClaimsHearingFirstOpenDateAfter() != null) {
                validateFutureDate(caseData.getSdoR2SmallClaimsHearing()
                    .getSdoR2SmallClaimsHearingFirstOpenDateAfter().getListFrom(), today).ifPresent(errors::add);
            }

            if (caseData.getSdoR2SmallClaimsHearing().getTrialOnOptions() == HearingOnRadioOptions.HEARING_WINDOW
                && caseData.getSdoR2SmallClaimsHearing().getSdoR2SmallClaimsHearingWindow() != null) {
                validateFutureDate(caseData.getSdoR2SmallClaimsHearing().getSdoR2SmallClaimsHearingWindow().getDateTo(), today)
                    .ifPresent(errors::add);
                validateFutureDate(caseData.getSdoR2SmallClaimsHearing().getSdoR2SmallClaimsHearingWindow().getListFrom(), today)
                    .ifPresent(errors::add);
            }
        }

        if (caseData.getSdoR2SmallClaimsImpNotes() != null) {
            validateFutureDate(caseData.getSdoR2SmallClaimsImpNotes().getDate(), today).ifPresent(errors::add);
        }
        return errors;
    }

    private List<String> validateNihl(CaseData caseData) {
        ArrayList<String> errors = new ArrayList<>();

        validateHearingDate(caseData.getSdoR2DisclosureOfDocuments(), errors,
                SdoR2DisclosureOfDocuments::getStandardDisclosureDate,
                SdoR2DisclosureOfDocuments::getInspectionDate
        );

        validateHearingDate(caseData.getSdoR2WitnessesOfFact(), errors,
                SdoR2WitnessOfFact::getSdoWitnessDeadlineDate
        );

        validateHearingDate(caseData.getSdoR2AddendumReport(), errors,
                SdoR2AddendumReport::getSdoAddendumReportDate
        );

        validateHearingDate(caseData.getSdoR2FurtherAudiogram(), errors,
                SdoR2FurtherAudiogram::getSdoClaimantShallUndergoDate,
                SdoR2FurtherAudiogram::getSdoServiceReportDate
        );
        var claimantExpert = caseData.getSdoR2QuestionsClaimantExpert();
        if (claimantExpert != null) {
            Optional.ofNullable(claimantExpert.getSdoApplicationToRelyOnFurther())
                    .map(SdoR2ApplicationToRelyOnFurther::getApplicationToRelyOnFurtherDetails)
                    .map(SdoR2ApplicationToRelyOnFurtherDetails::getApplicationToRelyDetailsDate).flatMap(this::validateFutureDate).ifPresent(errors::add);

            validateDates(errors,
                claimantExpert.getSdoQuestionsShallBeAnsweredDate(),
                claimantExpert.getSdoDefendantMayAskDate()
            );
        }

        var permissionToRely = caseData.getSdoR2PermissionToRelyOnExpert();
        if (permissionToRely != null) {
            validateDates(errors,
                permissionToRely.getSdoPermissionToRelyOnExpertDate(),
                permissionToRely.getSdoJointMeetingOfExpertsDate()
            );
        }

        var acousticEvidence = caseData.getSdoR2EvidenceAcousticEngineer();
        if (acousticEvidence != null) {
            validateDates(errors,
                acousticEvidence.getSdoInstructionOfTheExpertDate(),
                acousticEvidence.getSdoExpertReportDate(),
                acousticEvidence.getSdoWrittenQuestionsDate(),
                acousticEvidence.getSdoRepliesDate()
            );
        }

        var entExpert = caseData.getSdoR2QuestionsToEntExpert();
        if (entExpert != null) {
            validateDates(errors,
                entExpert.getSdoWrittenQuestionsDate(),
                entExpert.getSdoQuestionsShallBeAnsweredDate()
            );
        }

        var scheduleOfLoss = caseData.getSdoR2ScheduleOfLoss();
        if (scheduleOfLoss != null) {
            validateDates(errors,
                scheduleOfLoss.getSdoR2ScheduleOfLossClaimantDate(),
                scheduleOfLoss.getSdoR2ScheduleOfLossDefendantDate()
            );
        }

        var trial = caseData.getSdoR2Trial();
        if (trial != null) {
            Optional.ofNullable(trial.getSdoR2TrialFirstOpenDateAfter())
                    .map(uk.gov.hmcts.reform.civil.model.sdo.SdoR2TrialFirstOpenDateAfter::getListFrom).flatMap(this::validateFutureDate).ifPresent(errors::add);

            Optional.ofNullable(trial.getSdoR2TrialWindow())
                .ifPresent(window -> validateDates(errors, window.getListFrom(), window.getDateTo()));
        }

        if (caseData.getSdoR2ImportantNotesDate() != null) {
            validateFutureDate(caseData.getSdoR2ImportantNotesDate()).ifPresent(errors::add);
        }

        if (caseData.getSdoR2WitnessesOfFact() != null
            && caseData.getSdoR2WitnessesOfFact().getSdoR2RestrictWitness() != null
            && caseData.getSdoR2WitnessesOfFact().getSdoR2RestrictWitness().getRestrictNoOfWitnessDetails() != null) {
            Integer claimantCount = caseData.getSdoR2WitnessesOfFact().getSdoR2RestrictWitness()
                .getRestrictNoOfWitnessDetails().getNoOfWitnessClaimant();
            Integer defendantCount = caseData.getSdoR2WitnessesOfFact().getSdoR2RestrictWitness()
                .getRestrictNoOfWitnessDetails().getNoOfWitnessDefendant();
            Optional.ofNullable(claimantCount).flatMap(this::validateGreaterThanZero).ifPresent(errors::add);
            Optional.ofNullable(defendantCount).flatMap(this::validateGreaterThanZero).ifPresent(errors::add);
        }

        return errors;
    }

    private Optional<String> validateFutureDate(LocalDate date) {
        return validateFutureDate(date, LocalDate.now());
    }

    private Optional<String> validateFutureDate(LocalDate date, LocalDate today) {
        if (date == null) {
            return Optional.empty();
        }
        if (date.isAfter(today)) {
            return Optional.empty();
        }
        return Optional.of(ERROR_DATE_MUST_BE_IN_FUTURE);
    }

    private Optional<String> validateGreaterThanZero(Integer count) {
        if (count != null && count < 0) {
            return Optional.of(ERROR_NUMBER_LESS_THAN_ZERO);
        }
        return Optional.empty();
    }

    @SafeVarargs
    private <T> void validateHearingDate(T data, List<String> errors, Function<T, LocalDate>... extractors) {
        if (data == null) {
            return;
        }
        for (Function<T, LocalDate> extractor : extractors) {
            validateFutureDate(extractor.apply(data)).ifPresent(errors::add);
        }
    }

    private void validateDates(List<String> errors, LocalDate... dates) {
        for (LocalDate date : dates) {
            validateFutureDate(date).ifPresent(errors::add);
        }
    }

    private List<String> validatePpiDates(CaseData caseData) {
        ArrayList<String> errors = new ArrayList<>();
        if (caseData.getSmallClaimsPPI() != null) {
            validateFutureDate(caseData.getSmallClaimsPPI().getPpiDate()).ifPresent(errors::add);
        }
        if (caseData.getFastTrackPPI() != null) {
            validateFutureDate(caseData.getFastTrackPPI().getPpiDate()).ifPresent(errors::add);
        }
        return errors;
    }

    private List<String> validateHousingDisrepairDates(CaseData caseData) {
        ArrayList<String> errors = new ArrayList<>();
        if (caseData.getSmallClaimsHousingDisrepair() != null) {
            validateFutureDate(caseData.getSmallClaimsHousingDisrepair().getFirstReportDateBy()).ifPresent(errors::add);
            validateFutureDate(caseData.getSmallClaimsHousingDisrepair().getJointStatementDateBy()).ifPresent(errors::add);
        }
        if (caseData.getFastTrackHousingDisrepair() != null) {
            validateFutureDate(caseData.getFastTrackHousingDisrepair().getFirstReportDateBy()).ifPresent(errors::add);
            validateFutureDate(caseData.getFastTrackHousingDisrepair().getJointStatementDateBy()).ifPresent(errors::add);
        }
        return errors;
    }
}
