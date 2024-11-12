package uk.gov.hmcts.reform.civil.handler.callback.user.createsdocallbackhandler.generatesdoorder;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.civil.enums.sdo.HearingOnRadioOptions;
import uk.gov.hmcts.reform.civil.helpers.sdo.SdoHelper;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.service.FeatureToggleService;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static uk.gov.hmcts.reform.civil.constants.CreateSDOText.ERROR_MESSAGE_DATE_MUST_BE_IN_THE_FUTURE;
import static uk.gov.hmcts.reform.civil.constants.CreateSDOText.ERROR_MESSAGE_NUMBER_CANNOT_BE_LESS_THAN_ZERO;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;

@Slf4j
@Component
@RequiredArgsConstructor
public class WitnessStatementsValidator implements GenerateSdoOrderValidator {

    private final FeatureToggleService featureToggleService;

    @Override
    public void validate(CaseData caseData, List<String> errors) {
        if (nonNull(caseData.getSmallClaimsWitnessStatement())) {
            log.debug("Validating Small Claims Witness Statement");
            validateAndAddWitnessErrors(
                    caseData.getSmallClaimsWitnessStatement().getInput2(),
                    caseData.getSmallClaimsWitnessStatement().getInput3(),
                    errors
            );
        } else if (nonNull(caseData.getFastTrackWitnessOfFact())) {
            log.debug("Validating Fast Track Witness of Fact");
            validateAndAddWitnessErrors(
                    caseData.getFastTrackWitnessOfFact().getInput2(),
                    caseData.getFastTrackWitnessOfFact().getInput3(),
                    errors
            );
        } else if (isSdoR2EnabledForDRHSmallClaim(caseData)) {
            log.debug("Validating DRH Fields for SdoR2 Small Claim");
            errors.addAll(validateDRHFields(caseData));
        }
    }

    private void validateAndAddWitnessErrors(String input1, String input2, List<String> errors) {
        log.debug("Validating witness inputs");
        String errorMessage = validateNegativeWitness(input1, input2);
        if (!errorMessage.isEmpty()) {
            log.warn("Validation error: {}", errorMessage);
            errors.add(errorMessage);
        }
    }

    private boolean isSdoR2EnabledForDRHSmallClaim(CaseData caseData) {
        boolean enabled = featureToggleService.isSdoR2Enabled() && SdoHelper.isSDOR2ScreenForDRHSmallClaim(caseData);
        log.debug("SdoR2 enabled for DRH Small Claim: {}", enabled);
        return enabled;
    }

    private List<String> validateDRHFields(CaseData caseData) {
        log.debug("Validating DRH fields");
        List<String> errors = new ArrayList<>();
        LocalDate today = LocalDate.now();
        validatePpiDate(caseData, today, errors);
        validateNoOfWitnessClaimant(caseData, errors);
        validateNoOfWitnessDefendant(caseData, errors);
        validateTrialOnOptionsOpenDate(caseData, today, errors);
        validateTrialOnOptionsHearingWindowDateTo(caseData, today, errors);
        validateTrialOnOptionsHearingWindowListFrom(caseData, today, errors);
        validateImpNotesDate(caseData, today, errors);
        return errors;
    }

    private String validateNegativeWitness(String inputValue1, String inputValue2) {
        log.debug("Validating negative witness values");
        final String errorMessage = "";
        if (isInputsNonNull(inputValue1, inputValue2)) {
            int number1 = Integer.parseInt(inputValue1);
            int number2 = Integer.parseInt(inputValue2);
            if (isNumberNegative(number1, number2)) {
                log.warn("Negative witness numbers detected: {}, {}", number1, number2);
                return ERROR_MESSAGE_NUMBER_CANNOT_BE_LESS_THAN_ZERO;
            }
        }
        return errorMessage;
    }

    private void validatePpiDate(CaseData caseData, LocalDate today, List<String> errors) {
        if (Objects.nonNull(caseData.getSdoR2SmallClaimsPPI()) && Objects.nonNull(caseData.getSdoR2SmallClaimsPPI().getPpiDate())) {
            log.debug("Validating PPI date");
            validateFutureDate(caseData.getSdoR2SmallClaimsPPI().getPpiDate(), today).ifPresent(errors::add);
        }
    }

    private void validateNoOfWitnessClaimant(CaseData caseData, List<String> errors) {
        if (Objects.nonNull(caseData.getSdoR2SmallClaimsWitnessStatements())
                && caseData.getSdoR2SmallClaimsWitnessStatements().getIsRestrictWitness() == YES
                && nonNull(caseData.getSdoR2SmallClaimsWitnessStatements().getSdoR2SmallClaimsRestrictWitness().getNoOfWitnessClaimant())) {
            log.debug("Validating number of claimant witnesses");
            validateGreaterThanZero(caseData.getSdoR2SmallClaimsWitnessStatements().getSdoR2SmallClaimsRestrictWitness().getNoOfWitnessClaimant()).ifPresent(errors::add);
        }
    }

    private void validateNoOfWitnessDefendant(CaseData caseData, List<String> errors) {
        if (Objects.nonNull(caseData.getSdoR2SmallClaimsWitnessStatements())
                && caseData.getSdoR2SmallClaimsWitnessStatements().getIsRestrictWitness() == YES
                && nonNull(caseData.getSdoR2SmallClaimsWitnessStatements().getSdoR2SmallClaimsRestrictWitness().getNoOfWitnessDefendant())) {
            log.debug("Validating number of defendant witnesses");
            validateGreaterThanZero(caseData.getSdoR2SmallClaimsWitnessStatements().getSdoR2SmallClaimsRestrictWitness().getNoOfWitnessDefendant()).ifPresent(errors::add);
        }
    }

    private void validateTrialOnOptionsOpenDate(CaseData caseData, LocalDate today, List<String> errors) {
        if (Objects.nonNull(caseData.getSdoR2SmallClaimsHearing())
                && caseData.getSdoR2SmallClaimsHearing().getTrialOnOptions() == HearingOnRadioOptions.OPEN_DATE) {
            log.debug("Validating Trial On Options Open Date");
            validateFutureDate(
                    caseData.getSdoR2SmallClaimsHearing().getSdoR2SmallClaimsHearingFirstOpenDateAfter().getListFrom(),
                    today
            ).ifPresent(errors::add);
        }
    }

    private void validateTrialOnOptionsHearingWindowDateTo(CaseData caseData, LocalDate today, List<String> errors) {
        if (Objects.nonNull(caseData.getSdoR2SmallClaimsHearing())
                && caseData.getSdoR2SmallClaimsHearing().getTrialOnOptions() == HearingOnRadioOptions.HEARING_WINDOW) {
            log.debug("Validating Trial On Options Hearing Window Date To");
            validateFutureDate(
                    caseData.getSdoR2SmallClaimsHearing().getSdoR2SmallClaimsHearingWindow().getDateTo(),
                    today
            ).ifPresent(errors::add);
        }
    }

    private void validateTrialOnOptionsHearingWindowListFrom(CaseData caseData, LocalDate today, List<String> errors) {
        if (Objects.nonNull(caseData.getSdoR2SmallClaimsHearing())
                && caseData.getSdoR2SmallClaimsHearing().getTrialOnOptions() == HearingOnRadioOptions.HEARING_WINDOW) {
            log.debug("Validating Trial On Options Hearing Window List From");
            validateFutureDate(
                    caseData.getSdoR2SmallClaimsHearing().getSdoR2SmallClaimsHearingWindow().getListFrom(),
                    today
            ).ifPresent(errors::add);
        }
    }

    private void validateImpNotesDate(CaseData caseData, LocalDate today, List<String> errors) {
        if (Objects.nonNull(caseData.getSdoR2SmallClaimsImpNotes())) {
            log.debug("Validating Important Notes Date");
            validateFutureDate(caseData.getSdoR2SmallClaimsImpNotes().getDate(), today).ifPresent(errors::add);
        }
    }

    private Optional<String> validateFutureDate(LocalDate date, LocalDate today) {
        log.debug("Validating if date {} is after today {}", date, today);
        if (date.isAfter(today)) {
            return Optional.empty();
        }
        log.warn("Date {} is not in the future", date);
        return Optional.of(ERROR_MESSAGE_DATE_MUST_BE_IN_THE_FUTURE);
    }

    private Optional<String> validateGreaterThanZero(int count) {
        log.debug("Validating if count {} is greater than zero", count);
        if (count < 0) {
            log.warn("Count {} is less than zero", count);
            return Optional.of(ERROR_MESSAGE_NUMBER_CANNOT_BE_LESS_THAN_ZERO);
        }
        return Optional.empty();
    }

    private boolean nonNull(Object object) {
        return object != null;
    }

    private boolean isInputsNonNull(String input1, String input2) {
        return input1 != null && input2 != null;
    }

    private boolean isNumberNegative(int num1, int num2) {
        return num1 < 0 || num2 < 0;
    }

}
