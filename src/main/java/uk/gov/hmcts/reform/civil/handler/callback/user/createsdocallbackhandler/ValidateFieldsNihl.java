package uk.gov.hmcts.reform.civil.handler.callback.user.createsdocallbackhandler;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.model.CaseData;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static uk.gov.hmcts.reform.civil.constants.CreateSDOText.ERROR_MESSAGE_DATE_MUST_BE_IN_THE_FUTURE;
import static uk.gov.hmcts.reform.civil.constants.CreateSDOText.ERROR_MESSAGE_NUMBER_CANNOT_BE_LESS_THAN_ZERO;

@Component
@RequiredArgsConstructor
public class ValidateFieldsNihl {

    private static final Logger logger = LoggerFactory.getLogger(ValidateFieldsNihl.class);

    public List<String> validateFieldsNihl(CaseData caseData) {
        logger.info("Validating NIHL fields");
        ArrayList<String> errors = new ArrayList<>();
        validateStandardDisclosureDate(caseData, errors);
        validateInspectionDate(caseData, errors);
        validateWitnessDeadlineDate(caseData, errors);
        validateAddendumReportDate(caseData, errors);
        validateClaimantShallUndergoDate(caseData, errors);
        validateServiceReportDate(caseData, errors);
        validateDefendantMayAskDate(caseData, errors);
        validateQuestionsShallBeAnsweredDate(caseData, errors);
        validateApplicationToRelyDetailsDate(caseData, errors);
        validatePermissionToRelyOnExpertDate(caseData, errors);
        validateJointMeetingOfExpertsDate(caseData, errors);
        validateInstructionOfTheExpertDate(caseData, errors);
        validateExpertReportDate(caseData, errors);
        validateWrittenQuestionsDate(caseData, errors);
        validateRepliesDate(caseData, errors);
        validateWrittenQuestionsDateToEntExpert(caseData, errors);
        validateQuestionsShallBeAnsweredDateToEntExpert(caseData, errors);
        validateScheduleOfLossClaimantDate(caseData, errors);
        validateScheduleOfLossDefendantDate(caseData, errors);
        validateTrialFirstOpenDateAfter(caseData, errors);
        validateTrialWindowListFrom(caseData, errors);
        validateTrialWindowDateTo(caseData, errors);
        validateImportantNotesDate(caseData, errors);
        validateWitnessClaimantCount(caseData, errors);
        validateWitnessDefendantCount(caseData, errors);
        logger.info("Validation completed with {} errors", errors.size());
        return errors;
    }

    private void validateStandardDisclosureDate(CaseData caseData, List<String> errors) {
        if (caseData.getSdoR2DisclosureOfDocuments() != null && caseData.getSdoR2DisclosureOfDocuments().getStandardDisclosureDate() != null) {
            logger.debug("Validating Standard Disclosure Date");
            validateFutureDate(caseData.getSdoR2DisclosureOfDocuments().getStandardDisclosureDate()).ifPresent(error -> {
                logger.warn("Standard Disclosure Date validation failed: {}", error);
                errors.add(error);
            });
        }
    }

    private void validateInspectionDate(CaseData caseData, List<String> errors) {
        if (caseData.getSdoR2DisclosureOfDocuments() != null && caseData.getSdoR2DisclosureOfDocuments().getInspectionDate() != null) {
            logger.debug("Validating Inspection Date");
            validateFutureDate(caseData.getSdoR2DisclosureOfDocuments().getInspectionDate()).ifPresent(error -> {
                logger.warn("Inspection Date validation failed: {}", error);
                errors.add(error);
            });
        }
    }

    private void validateWitnessDeadlineDate(CaseData caseData, List<String> errors) {
        if (caseData.getSdoR2WitnessesOfFact() != null && caseData.getSdoR2WitnessesOfFact().getSdoWitnessDeadlineDate() != null) {
            logger.debug("Validating Witness Deadline Date");
            validateFutureDate(caseData.getSdoR2WitnessesOfFact().getSdoWitnessDeadlineDate()).ifPresent(error -> {
                logger.warn("Witness Deadline Date validation failed: {}", error);
                errors.add(error);
            });
        }
    }

    private void validateAddendumReportDate(CaseData caseData, List<String> errors) {
        if (caseData.getSdoR2AddendumReport() != null && caseData.getSdoR2AddendumReport().getSdoAddendumReportDate() != null) {
            logger.debug("Validating Addendum Report Date");
            validateFutureDate(caseData.getSdoR2AddendumReport().getSdoAddendumReportDate()).ifPresent(error -> {
                logger.warn("Addendum Report Date validation failed: {}", error);
                errors.add(error);
            });
        }
    }

    private void validateClaimantShallUndergoDate(CaseData caseData, List<String> errors) {
        if (caseData.getSdoR2FurtherAudiogram() != null && caseData.getSdoR2FurtherAudiogram().getSdoClaimantShallUndergoDate() != null) {
            logger.debug("Validating Claimant Shall Undergo Date");
            validateFutureDate(caseData.getSdoR2FurtherAudiogram().getSdoClaimantShallUndergoDate()).ifPresent(error -> {
                logger.warn("Claimant Shall Undergo Date validation failed: {}", error);
                errors.add(error);
            });
        }
    }

    private void validateServiceReportDate(CaseData caseData, List<String> errors) {
        if (caseData.getSdoR2FurtherAudiogram() != null && caseData.getSdoR2FurtherAudiogram().getSdoServiceReportDate() != null) {
            logger.debug("Validating Service Report Date");
            validateFutureDate(caseData.getSdoR2FurtherAudiogram().getSdoServiceReportDate()).ifPresent(error -> {
                logger.warn("Service Report Date validation failed: {}", error);
                errors.add(error);
            });
        }
    }

    private void validateDefendantMayAskDate(CaseData caseData, List<String> errors) {
        if (caseData.getSdoR2QuestionsClaimantExpert() != null && caseData.getSdoR2QuestionsClaimantExpert().getSdoDefendantMayAskDate() != null) {
            logger.debug("Validating Defendant May Ask Date");
            validateFutureDate(caseData.getSdoR2QuestionsClaimantExpert().getSdoDefendantMayAskDate()).ifPresent(error -> {
                logger.warn("Defendant May Ask Date validation failed: {}", error);
                errors.add(error);
            });
        }
    }

    private void validateQuestionsShallBeAnsweredDate(CaseData caseData, List<String> errors) {
        if (caseData.getSdoR2QuestionsClaimantExpert() != null && caseData.getSdoR2QuestionsClaimantExpert().getSdoQuestionsShallBeAnsweredDate() != null) {
            logger.debug("Validating Questions Shall Be Answered Date");
            validateFutureDate(caseData.getSdoR2QuestionsClaimantExpert().getSdoQuestionsShallBeAnsweredDate()).ifPresent(error -> {
                logger.warn("Questions Shall Be Answered Date validation failed: {}", error);
                errors.add(error);
            });
        }
    }

    private void validateApplicationToRelyDetailsDate(CaseData caseData, List<String> errors) {
        if (caseData.getSdoR2QuestionsClaimantExpert() != null
            && caseData.getSdoR2QuestionsClaimantExpert().getSdoApplicationToRelyOnFurther() != null
            && caseData.getSdoR2QuestionsClaimantExpert().getSdoApplicationToRelyOnFurther().getApplicationToRelyOnFurtherDetails() != null
            && caseData.getSdoR2QuestionsClaimantExpert().getSdoApplicationToRelyOnFurther().getApplicationToRelyOnFurtherDetails().getApplicationToRelyDetailsDate() != null) {
            logger.debug("Validating Application To Rely Details Date");
            validateFutureDate(caseData.getSdoR2QuestionsClaimantExpert()
                                   .getSdoApplicationToRelyOnFurther()
                                   .getApplicationToRelyOnFurtherDetails()
                                   .getApplicationToRelyDetailsDate())
                .ifPresent(error -> {
                    logger.warn("Application To Rely Details Date validation failed: {}", error);
                    errors.add(error);
                });
        }
    }

    private void validatePermissionToRelyOnExpertDate(CaseData caseData, List<String> errors) {
        if (caseData.getSdoR2PermissionToRelyOnExpert() != null
            && caseData.getSdoR2PermissionToRelyOnExpert().getSdoPermissionToRelyOnExpertDate() != null) {
            logger.debug("Validating Permission To Rely On Expert Date");
            validateFutureDate(caseData.getSdoR2PermissionToRelyOnExpert().getSdoPermissionToRelyOnExpertDate()).ifPresent(error -> {
                logger.warn("Permission To Rely On Expert Date validation failed: {}", error);
                errors.add(error);
            });
        }
    }

    private void validateJointMeetingOfExpertsDate(CaseData caseData, List<String> errors) {
        if (caseData.getSdoR2PermissionToRelyOnExpert() != null
            && caseData.getSdoR2PermissionToRelyOnExpert().getSdoJointMeetingOfExpertsDate() != null) {
            logger.debug("Validating Joint Meeting Of Experts Date");
            validateFutureDate(caseData.getSdoR2PermissionToRelyOnExpert().getSdoJointMeetingOfExpertsDate()).ifPresent(error -> {
                logger.warn("Joint Meeting Of Experts Date validation failed: {}", error);
                errors.add(error);
            });
        }
    }

    private void validateInstructionOfTheExpertDate(CaseData caseData, List<String> errors) {
        if (caseData.getSdoR2EvidenceAcousticEngineer() != null
            && caseData.getSdoR2EvidenceAcousticEngineer().getSdoInstructionOfTheExpertDate() != null) {
            logger.debug("Validating Instruction Of The Expert Date");
            validateFutureDate(caseData.getSdoR2EvidenceAcousticEngineer().getSdoInstructionOfTheExpertDate()).ifPresent(error -> {
                logger.warn("Instruction Of The Expert Date validation failed: {}", error);
                errors.add(error);
            });
        }
    }

    private void validateExpertReportDate(CaseData caseData, List<String> errors) {
        if (caseData.getSdoR2EvidenceAcousticEngineer() != null
            && caseData.getSdoR2EvidenceAcousticEngineer().getSdoExpertReportDate() != null) {
            logger.debug("Validating Expert Report Date");
            validateFutureDate(caseData.getSdoR2EvidenceAcousticEngineer().getSdoExpertReportDate()).ifPresent(error -> {
                logger.warn("Expert Report Date validation failed: {}", error);
                errors.add(error);
            });
        }
    }

    private void validateWrittenQuestionsDate(CaseData caseData, List<String> errors) {
        if (caseData.getSdoR2EvidenceAcousticEngineer() != null
            && caseData.getSdoR2EvidenceAcousticEngineer().getSdoWrittenQuestionsDate() != null) {
            logger.debug("Validating Written Questions Date");
            validateFutureDate(caseData.getSdoR2EvidenceAcousticEngineer().getSdoWrittenQuestionsDate()).ifPresent(error -> {
                logger.warn("Written Questions Date validation failed: {}", error);
                errors.add(error);
            });
        }
    }

    private void validateRepliesDate(CaseData caseData, List<String> errors) {
        if (caseData.getSdoR2EvidenceAcousticEngineer() != null
            && caseData.getSdoR2EvidenceAcousticEngineer().getSdoRepliesDate() != null) {
            logger.debug("Validating Replies Date");
            validateFutureDate(caseData.getSdoR2EvidenceAcousticEngineer().getSdoRepliesDate()).ifPresent(error -> {
                logger.warn("Replies Date validation failed: {}", error);
                errors.add(error);
            });
        }
    }

    private void validateWrittenQuestionsDateToEntExpert(CaseData caseData, List<String> errors) {
        if (caseData.getSdoR2QuestionsToEntExpert() != null
            && caseData.getSdoR2QuestionsToEntExpert().getSdoWrittenQuestionsDate() != null) {
            logger.debug("Validating Written Questions Date To ENT Expert");
            validateFutureDate(caseData.getSdoR2QuestionsToEntExpert().getSdoWrittenQuestionsDate()).ifPresent(error -> {
                logger.warn("Written Questions Date To ENT Expert validation failed: {}", error);
                errors.add(error);
            });
        }
    }

    private void validateQuestionsShallBeAnsweredDateToEntExpert(CaseData caseData, List<String> errors) {
        if (caseData.getSdoR2QuestionsToEntExpert() != null
            && caseData.getSdoR2QuestionsToEntExpert().getSdoQuestionsShallBeAnsweredDate() != null) {
            logger.debug("Validating Questions Shall Be Answered Date To ENT Expert");
            validateFutureDate(caseData.getSdoR2QuestionsToEntExpert().getSdoQuestionsShallBeAnsweredDate()).ifPresent(error -> {
                logger.warn("Questions Shall Be Answered Date To ENT Expert validation failed: {}", error);
                errors.add(error);
            });
        }
    }

    private void validateScheduleOfLossClaimantDate(CaseData caseData, List<String> errors) {
        if (caseData.getSdoR2ScheduleOfLoss() != null
            && caseData.getSdoR2ScheduleOfLoss().getSdoR2ScheduleOfLossClaimantDate() != null) {
            logger.debug("Validating Schedule Of Loss Claimant Date");
            validateFutureDate(caseData.getSdoR2ScheduleOfLoss().getSdoR2ScheduleOfLossClaimantDate()).ifPresent(error -> {
                logger.warn("Schedule Of Loss Claimant Date validation failed: {}", error);
                errors.add(error);
            });
        }
    }

    private void validateScheduleOfLossDefendantDate(CaseData caseData, List<String> errors) {
        if (caseData.getSdoR2ScheduleOfLoss() != null
            && caseData.getSdoR2ScheduleOfLoss().getSdoR2ScheduleOfLossDefendantDate() != null) {
            logger.debug("Validating Schedule Of Loss Defendant Date");
            validateFutureDate(caseData.getSdoR2ScheduleOfLoss().getSdoR2ScheduleOfLossDefendantDate()).ifPresent(error -> {
                logger.warn("Schedule Of Loss Defendant Date validation failed: {}", error);
                errors.add(error);
            });
        }
    }

    private void validateTrialFirstOpenDateAfter(CaseData caseData, List<String> errors) {
        if (caseData.getSdoR2Trial() != null
            && caseData.getSdoR2Trial().getSdoR2TrialFirstOpenDateAfter() != null
            && caseData.getSdoR2Trial().getSdoR2TrialFirstOpenDateAfter().getListFrom() != null) {
            logger.debug("Validating Trial First Open Date After");
            validateFutureDate(caseData.getSdoR2Trial().getSdoR2TrialFirstOpenDateAfter().getListFrom()).ifPresent(error -> {
                logger.warn("Trial First Open Date After validation failed: {}", error);
                errors.add(error);
            });
        }
    }

    private void validateTrialWindowListFrom(CaseData caseData, List<String> errors) {
        if (caseData.getSdoR2Trial() != null
            && caseData.getSdoR2Trial().getSdoR2TrialWindow() != null
            && caseData.getSdoR2Trial().getSdoR2TrialWindow().getListFrom() != null) {
            logger.debug("Validating Trial Window List From");
            validateFutureDate(caseData.getSdoR2Trial().getSdoR2TrialWindow().getListFrom()).ifPresent(error -> {
                logger.warn("Trial Window List From validation failed: {}", error);
                errors.add(error);
            });
        }
    }

    private void validateTrialWindowDateTo(CaseData caseData, List<String> errors) {
        if (caseData.getSdoR2Trial() != null
            && caseData.getSdoR2Trial().getSdoR2TrialWindow().getDateTo() != null) {
            logger.debug("Validating Trial Window Date To");
            validateFutureDate(caseData.getSdoR2Trial().getSdoR2TrialWindow().getDateTo()).ifPresent(error -> {
                logger.warn("Trial Window Date To validation failed: {}", error);
                errors.add(error);
            });
        }
    }

    private void validateImportantNotesDate(CaseData caseData, List<String> errors) {
        if (caseData.getSdoR2ImportantNotesDate() != null) {
            logger.debug("Validating Important Notes Date");
            validateFutureDate(caseData.getSdoR2ImportantNotesDate()).ifPresent(error -> {
                logger.warn("Important Notes Date validation failed: {}", error);
                errors.add(error);
            });
        }
    }

    private void validateWitnessClaimantCount(CaseData caseData, List<String> errors) {
        if (caseData.getSdoR2WitnessesOfFact() != null
            && caseData.getSdoR2WitnessesOfFact().getSdoR2RestrictWitness() != null
            && caseData.getSdoR2WitnessesOfFact().getSdoR2RestrictWitness().getRestrictNoOfWitnessDetails() != null
            && caseData.getSdoR2WitnessesOfFact().getSdoR2RestrictWitness().getRestrictNoOfWitnessDetails().getNoOfWitnessClaimant() != null) {
            logger.debug("Validating Witness Claimant Count");
            validateGreaterOrEqualZero(caseData.getSdoR2WitnessesOfFact()
                                           .getSdoR2RestrictWitness()
                                           .getRestrictNoOfWitnessDetails()
                                           .getNoOfWitnessClaimant())
                .ifPresent(error -> {
                    logger.warn("Witness Claimant Count validation failed: {}", error);
                    errors.add(error);
                });
        }
    }

    private void validateWitnessDefendantCount(CaseData caseData, List<String> errors) {
        if (caseData.getSdoR2WitnessesOfFact() != null
            && caseData.getSdoR2WitnessesOfFact().getSdoR2RestrictWitness() != null
            && caseData.getSdoR2WitnessesOfFact().getSdoR2RestrictWitness().getRestrictNoOfWitnessDetails() != null
            && caseData.getSdoR2WitnessesOfFact().getSdoR2RestrictWitness().getRestrictNoOfWitnessDetails().getNoOfWitnessDefendant() != null) {
            logger.debug("Validating Witness Defendant Count");
            validateGreaterOrEqualZero(caseData.getSdoR2WitnessesOfFact()
                                           .getSdoR2RestrictWitness()
                                           .getRestrictNoOfWitnessDetails()
                                           .getNoOfWitnessDefendant())
                .ifPresent(error -> {
                    logger.warn("Witness Defendant Count validation failed: {}", error);
                    errors.add(error);
                });
        }
    }

    private Optional<String> validateFutureDate(LocalDate date) {
        LocalDate today = LocalDate.now();
        logger.debug("Checking if date {} is after today {}", date, today);
        if (date.isAfter(today)) {
            return Optional.empty();
        }
        logger.warn("Date {} is not in the future", date);
        return Optional.of(ERROR_MESSAGE_DATE_MUST_BE_IN_THE_FUTURE);
    }

    private Optional<String> validateGreaterOrEqualZero(Integer quantity) {
        logger.debug("Checking if quantity {} is greater than or equal to zero", quantity);
        if (quantity < 0) {
            logger.warn("Quantity {} is less than zero", quantity);
            return Optional.of(ERROR_MESSAGE_NUMBER_CANNOT_BE_LESS_THAN_ZERO);
        }
        return Optional.empty();
    }
}
