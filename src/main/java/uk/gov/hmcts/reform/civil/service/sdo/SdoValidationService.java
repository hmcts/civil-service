package uk.gov.hmcts.reform.civil.service.sdo;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.enums.sdo.HearingOnRadioOptions;
import uk.gov.hmcts.reform.civil.model.CaseData;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
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

        return errors;
    }

    private Optional<String> validateNegativeWitness(String first, String second) {
        if (first != null && second != null) {
            try {
                int number1 = Integer.parseInt(first);
                int number2 = Integer.parseInt(second);
                if (number1 < 0 || number2 < 0) {
                    return Optional.of(ERROR_NUMBER_LESS_THAN_ZERO);
                }
            } catch (NumberFormatException ignored) {
                // CCD should enforce numeric values; if it does not, fall through without blocking submission.
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
            && caseData.getSdoR2SmallClaimsWitnessStatements().getIsRestrictWitness() == YesOrNo.YES) {
            if (caseData.getSdoR2SmallClaimsWitnessStatements().getSdoR2SmallClaimsRestrictWitness() != null) {
                Integer noWitnessClaimant = caseData.getSdoR2SmallClaimsWitnessStatements()
                    .getSdoR2SmallClaimsRestrictWitness().getNoOfWitnessClaimant();
                Integer noWitnessDefendant = caseData.getSdoR2SmallClaimsWitnessStatements()
                    .getSdoR2SmallClaimsRestrictWitness().getNoOfWitnessDefendant();
                Optional.ofNullable(noWitnessClaimant).flatMap(this::validateGreaterThanZero).ifPresent(errors::add);
                Optional.ofNullable(noWitnessDefendant).flatMap(this::validateGreaterThanZero).ifPresent(errors::add);
            }
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

        if (caseData.getSdoR2DisclosureOfDocuments() != null) {
            if (caseData.getSdoR2DisclosureOfDocuments().getStandardDisclosureDate() != null) {
                validateFutureDate(caseData.getSdoR2DisclosureOfDocuments().getStandardDisclosureDate())
                    .ifPresent(errors::add);
            }
            if (caseData.getSdoR2DisclosureOfDocuments().getInspectionDate() != null) {
                validateFutureDate(caseData.getSdoR2DisclosureOfDocuments().getInspectionDate())
                    .ifPresent(errors::add);
            }
        }
        if (caseData.getSdoR2WitnessesOfFact() != null
            && caseData.getSdoR2WitnessesOfFact().getSdoWitnessDeadlineDate() != null) {
            validateFutureDate(caseData.getSdoR2WitnessesOfFact().getSdoWitnessDeadlineDate()).ifPresent(errors::add);
        }
        if (caseData.getSdoR2AddendumReport() != null
            && caseData.getSdoR2AddendumReport().getSdoAddendumReportDate() != null) {
            validateFutureDate(caseData.getSdoR2AddendumReport().getSdoAddendumReportDate()).ifPresent(errors::add);
        }
        if (caseData.getSdoR2FurtherAudiogram() != null) {
            if (caseData.getSdoR2FurtherAudiogram().getSdoClaimantShallUndergoDate() != null) {
                validateFutureDate(caseData.getSdoR2FurtherAudiogram().getSdoClaimantShallUndergoDate()).ifPresent(errors::add);
            }
            if (caseData.getSdoR2FurtherAudiogram().getSdoServiceReportDate() != null) {
                validateFutureDate(caseData.getSdoR2FurtherAudiogram().getSdoServiceReportDate()).ifPresent(errors::add);
            }
        }
        if (caseData.getSdoR2QuestionsClaimantExpert() != null
            && caseData.getSdoR2QuestionsClaimantExpert().getSdoApplicationToRelyOnFurther() != null
            && caseData.getSdoR2QuestionsClaimantExpert().getSdoApplicationToRelyOnFurther().getApplicationToRelyOnFurtherDetails() != null
            && caseData.getSdoR2QuestionsClaimantExpert().getSdoApplicationToRelyOnFurther()
            .getApplicationToRelyOnFurtherDetails().getApplicationToRelyDetailsDate() != null) {
            validateFutureDate(caseData.getSdoR2QuestionsClaimantExpert().getSdoApplicationToRelyOnFurther()
                .getApplicationToRelyOnFurtherDetails().getApplicationToRelyDetailsDate()).ifPresent(errors::add);
        }

        if (caseData.getSdoR2QuestionsClaimantExpert() != null
            && caseData.getSdoR2QuestionsClaimantExpert().getSdoQuestionsShallBeAnsweredDate() != null) {
            validateFutureDate(caseData.getSdoR2QuestionsClaimantExpert().getSdoQuestionsShallBeAnsweredDate())
                .ifPresent(errors::add);
        }
        if (caseData.getSdoR2PermissionToRelyOnExpert() != null) {
            if (caseData.getSdoR2PermissionToRelyOnExpert().getSdoPermissionToRelyOnExpertDate() != null) {
                validateFutureDate(caseData.getSdoR2PermissionToRelyOnExpert().getSdoPermissionToRelyOnExpertDate()).ifPresent(errors::add);
            }
            if (caseData.getSdoR2PermissionToRelyOnExpert().getSdoJointMeetingOfExpertsDate() != null) {
                validateFutureDate(caseData.getSdoR2PermissionToRelyOnExpert().getSdoJointMeetingOfExpertsDate()).ifPresent(errors::add);
            }
        }

        if (caseData.getSdoR2QuestionsClaimantExpert() != null
            && caseData.getSdoR2QuestionsClaimantExpert().getSdoDefendantMayAskDate() != null) {
            validateFutureDate(caseData.getSdoR2QuestionsClaimantExpert().getSdoDefendantMayAskDate())
                .ifPresent(errors::add);
        }

        if (caseData.getSdoR2EvidenceAcousticEngineer() != null) {
            if (caseData.getSdoR2EvidenceAcousticEngineer().getSdoInstructionOfTheExpertDate() != null) {
                validateFutureDate(caseData.getSdoR2EvidenceAcousticEngineer().getSdoInstructionOfTheExpertDate()).ifPresent(errors::add);
            }
            if (caseData.getSdoR2EvidenceAcousticEngineer().getSdoExpertReportDate() != null) {
                validateFutureDate(caseData.getSdoR2EvidenceAcousticEngineer().getSdoExpertReportDate()).ifPresent(errors::add);
            }
            if (caseData.getSdoR2EvidenceAcousticEngineer().getSdoWrittenQuestionsDate() != null) {
                validateFutureDate(caseData.getSdoR2EvidenceAcousticEngineer().getSdoWrittenQuestionsDate()).ifPresent(errors::add);
            }
            if (caseData.getSdoR2EvidenceAcousticEngineer().getSdoRepliesDate() != null) {
                validateFutureDate(caseData.getSdoR2EvidenceAcousticEngineer().getSdoRepliesDate()).ifPresent(errors::add);
            }
        }

        if (caseData.getSdoR2QuestionsToEntExpert() != null) {
            if (caseData.getSdoR2QuestionsToEntExpert().getSdoWrittenQuestionsDate() != null) {
                validateFutureDate(caseData.getSdoR2QuestionsToEntExpert().getSdoWrittenQuestionsDate()).ifPresent(errors::add);
            }
            if (caseData.getSdoR2QuestionsToEntExpert().getSdoQuestionsShallBeAnsweredDate() != null) {
                validateFutureDate(caseData.getSdoR2QuestionsToEntExpert().getSdoQuestionsShallBeAnsweredDate()).ifPresent(errors::add);
            }
        }

        if (caseData.getSdoR2ScheduleOfLoss() != null
            && caseData.getSdoR2ScheduleOfLoss().getSdoR2ScheduleOfLossClaimantDate() != null) {
            validateFutureDate(caseData.getSdoR2ScheduleOfLoss().getSdoR2ScheduleOfLossClaimantDate())
                .ifPresent(errors::add);
        }
        if (caseData.getSdoR2ScheduleOfLoss() != null
            && caseData.getSdoR2ScheduleOfLoss().getSdoR2ScheduleOfLossDefendantDate() != null) {
            validateFutureDate(caseData.getSdoR2ScheduleOfLoss().getSdoR2ScheduleOfLossDefendantDate())
                .ifPresent(errors::add);
        }

        if (caseData.getSdoR2Trial() != null) {
            if (caseData.getSdoR2Trial().getSdoR2TrialFirstOpenDateAfter() != null
                && caseData.getSdoR2Trial().getSdoR2TrialFirstOpenDateAfter().getListFrom() != null) {
                validateFutureDate(caseData.getSdoR2Trial().getSdoR2TrialFirstOpenDateAfter().getListFrom()).ifPresent(errors::add);
            }
            if (caseData.getSdoR2Trial().getSdoR2TrialWindow() != null) {
                validateFutureDate(caseData.getSdoR2Trial().getSdoR2TrialWindow().getListFrom()).ifPresent(errors::add);
                validateFutureDate(caseData.getSdoR2Trial().getSdoR2TrialWindow().getDateTo()).ifPresent(errors::add);
            }
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
}
