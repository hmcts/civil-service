package uk.gov.hmcts.reform.civil.service.validation;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import uk.gov.hmcts.reform.civil.model.genapplication.GAHearingDetails;
import uk.gov.hmcts.reform.civil.model.genapplication.GAUnavailabilityDates;
import uk.gov.hmcts.reform.civil.model.genapplication.GAUrgencyRequirement;
import uk.gov.hmcts.reform.civil.referencedata.model.LocationRefData;
import uk.gov.hmcts.reform.civil.sampledata.LocationRefSampleDataBuilder;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;
import static uk.gov.hmcts.reform.civil.service.InitiateGeneralApplicationServiceConstants.INVALID_UNAVAILABILITY_RANGE;
import static uk.gov.hmcts.reform.civil.service.validation.GeneralApplicationValidatorConstants.INVALID_TRIAL_DATE_RANGE;
import static uk.gov.hmcts.reform.civil.service.validation.GeneralApplicationValidatorConstants.TRIAL_DATE_FROM_REQUIRED;
import static uk.gov.hmcts.reform.civil.service.validation.GeneralApplicationValidatorConstants.UNAVAILABLE_DATE_RANGE_MISSING;
import static uk.gov.hmcts.reform.civil.service.validation.GeneralApplicationValidatorConstants.UNAVAILABLE_FROM_MUST_BE_PROVIDED;
import static uk.gov.hmcts.reform.civil.service.validation.GeneralApplicationValidatorConstants.URGENCY_DATE_CANNOT_BE_IN_PAST;
import static uk.gov.hmcts.reform.civil.service.validation.GeneralApplicationValidatorConstants.URGENCY_DATE_REQUIRED;
import static uk.gov.hmcts.reform.civil.service.validation.GeneralApplicationValidatorConstants.URGENCY_DATE_SHOULD_NOT_BE_PROVIDED;
import static uk.gov.hmcts.reform.civil.utils.ElementUtils.wrapElements;

@SpringBootTest(classes = {
    GeneralApplicationValidator.class
})
class GeneralApplicationValidatorTest extends LocationRefSampleDataBuilder {

    @Autowired
    GeneralApplicationValidator service;

    //Urgency Date validation
    @Test
    void shouldReturnErrors_whenApplicationIsUrgentButConsiderationDateIsNotProvided() {
        GAUrgencyRequirement urgencyRequirement = GAUrgencyRequirement.builder()
            .generalAppUrgency(YES)
            .urgentAppConsiderationDate(null)
            .build();

        List<String> errors = service.validateUrgencyDates(urgencyRequirement);

        assertThat(errors).isNotEmpty().contains(URGENCY_DATE_REQUIRED);
    }

    @Test
    void shouldReturnErrors_whenApplicationIsNotUrgentButConsiderationDateIsProvided() {
        GAUrgencyRequirement urgencyRequirement = GAUrgencyRequirement.builder()
            .generalAppUrgency(NO)
            .urgentAppConsiderationDate(LocalDate.now())
            .build();

        List<String> errors = service.validateUrgencyDates(urgencyRequirement);

        assertThat(errors).isNotEmpty().contains(URGENCY_DATE_SHOULD_NOT_BE_PROVIDED);
    }

    @Test
    void shouldReturnErrors_whenUrgencyConsiderationDateIsInPastForUrgentApplication() {
        GAUrgencyRequirement urgencyRequirement = GAUrgencyRequirement.builder()
            .generalAppUrgency(YES)
            .urgentAppConsiderationDate(LocalDate.now().minusDays(1))
            .build();

        List<String> errors = service.validateUrgencyDates(urgencyRequirement);

        assertThat(errors).isNotEmpty().contains(URGENCY_DATE_CANNOT_BE_IN_PAST);
    }

    @Test
    void shouldNotCauseAnyErrors_whenUrgencyConsiderationDateIsInFutureForUrgentApplication() {
        GAUrgencyRequirement urgencyRequirement = GAUrgencyRequirement.builder()
            .generalAppUrgency(YES)
            .urgentAppConsiderationDate(LocalDate.now())
            .build();

        List<String> errors = service.validateUrgencyDates(urgencyRequirement);

        assertThat(errors).isEmpty();
    }

    @Test
    void shouldNotCauseAnyErrors_whenApplicationIsNotUrgentAndConsiderationDateIsNotProvided() {
        GAUrgencyRequirement urgencyRequirement = GAUrgencyRequirement.builder()
            .generalAppUrgency(NO)
            .urgentAppConsiderationDate(null)
            .build();

        List<String> errors = service.validateUrgencyDates(urgencyRequirement);

        assertThat(errors).isEmpty();
    }

    //Trial Dates validations
    @Test
    void shouldReturnErrors_whenTrialIsScheduledButTrialDateFromIsNull() {
        GAHearingDetails hearingDetails = GAHearingDetails.builder()
            .trialRequiredYesOrNo(YES)
            .trialDateFrom(null)
            .trialDateTo(null)
            .unavailableTrialRequiredYesOrNo(YES)
            .generalAppUnavailableDates(getValidUnavailableDateList())
            .build();

        List<String> errors = service.validateHearingScreen(hearingDetails);

        assertThat(errors).isNotEmpty().contains(TRIAL_DATE_FROM_REQUIRED);
    }

    @Test
    void shouldReturnErrors_whenTrialIsScheduledAndTrialDateFromIsProvidedWithTrialDateToBeforeIt() {
        GAHearingDetails hearingDetails = GAHearingDetails.builder()
            .trialRequiredYesOrNo(YES)
            .trialDateFrom(LocalDate.now())
            .trialDateTo(LocalDate.now().minusDays(1))
            .unavailableTrialRequiredYesOrNo(YES)
            .generalAppUnavailableDates(getValidUnavailableDateList())
            .build();

        List<String> errors = service.validateHearingScreen(hearingDetails);

        assertThat(errors).isNotEmpty().contains(INVALID_TRIAL_DATE_RANGE);
    }

    @Test
    void shouldNotReturnErrors_whenTrialIsScheduledAndTrialDateFromIsProvidedWithNullTrialDateTo() {
        GAHearingDetails hearingDetails = GAHearingDetails.builder()
            .trialRequiredYesOrNo(YES)
            .trialDateFrom(LocalDate.now())
            .trialDateTo(null)
            .unavailableTrialRequiredYesOrNo(YES)
            .generalAppUnavailableDates(getValidUnavailableDateList())
            .build();

        List<String> errors = service.validateHearingScreen(hearingDetails);

        assertThat(errors).isEmpty();
    }

    @Test
    void shouldNotReturnErrors_whenTrialIsScheduledAndTrialDateFromIsProvidedWithTrialDateToAfterIt() {
        GAHearingDetails hearingDetails = GAHearingDetails.builder()
            .trialRequiredYesOrNo(YES)
            .trialDateFrom(LocalDate.now())
            .trialDateTo(LocalDate.now().plusDays(1))
            .unavailableTrialRequiredYesOrNo(YES)
            .generalAppUnavailableDates(getValidUnavailableDateList())
            .build();

        List<String> errors = service.validateHearingScreen(hearingDetails);
        assertThat(errors).isEmpty();
    }

    @Test
    void shouldNotReturnErrors_whenTrialIsScheduledAndTrialDateFromIsProvidedAndTrialDateToAreSame() {
        GAHearingDetails hearingDetails = GAHearingDetails.builder()
            .trialRequiredYesOrNo(YES)
            .trialDateFrom(LocalDate.now())
            .trialDateTo(LocalDate.now())
            .unavailableTrialRequiredYesOrNo(YES)
            .generalAppUnavailableDates(getValidUnavailableDateList())
            .build();

        List<String> errors = service.validateHearingScreen(hearingDetails);

        assertThat(errors).isEmpty();
    }

    @Test
    void shouldNotReturnErrors_whenTrialIsNotScheduled() {
        GAHearingDetails hearingDetails = GAHearingDetails.builder()
            .trialRequiredYesOrNo(NO)
            .trialDateFrom(null)
            .trialDateTo(null)
            .unavailableTrialRequiredYesOrNo(YES)
            .generalAppUnavailableDates(getValidUnavailableDateList())
            .build();

        List<String> errors = service.validateHearingScreen(hearingDetails);

        assertThat(errors).isEmpty();
    }

    //Unavailability Dates validations
    @Test
    void shouldReturnErrors_whenUnavailabilityIsSetButNullDateRangeProvided() {
        GAHearingDetails hearingDetails = GAHearingDetails.builder()
            .trialRequiredYesOrNo(YES)
            .trialDateFrom(LocalDate.now())
            .trialDateTo(null)
            .unavailableTrialRequiredYesOrNo(YES)
            .generalAppUnavailableDates(null)
            .build();

        List<String> errors = service.validateHearingScreen(hearingDetails);

        assertThat(errors).isNotEmpty().contains(UNAVAILABLE_DATE_RANGE_MISSING);
    }

    @Test
    void shouldReturnErrors_whenUnavailabilityIsSetButDateRangeProvidedHasNullDateFrom() {
        GAUnavailabilityDates range1 = GAUnavailabilityDates.builder()
            .unavailableTrialDateFrom(null)
            .unavailableTrialDateTo(null)
            .build();

        GAHearingDetails hearingDetails = GAHearingDetails.builder()
            .trialRequiredYesOrNo(YES)
            .trialDateFrom(LocalDate.now())
            .trialDateTo(null)
            .unavailableTrialRequiredYesOrNo(YES)
            .generalAppUnavailableDates(wrapElements(range1))
            .build();

        List<String> errors = service.validateHearingScreen(hearingDetails);

        assertThat(errors).isNotEmpty().contains(UNAVAILABLE_FROM_MUST_BE_PROVIDED);
    }

    @Test
    void shouldReturnErrors_whenUnavailabilityIsSetButDateRangeProvidedHasDateFromAfterDateTo() {
        GAUnavailabilityDates range1 = GAUnavailabilityDates.builder()
            .unavailableTrialDateFrom(LocalDate.now().plusDays(1))
            .unavailableTrialDateTo(LocalDate.now())
            .build();

        GAHearingDetails hearingDetails = GAHearingDetails.builder()
            .trialRequiredYesOrNo(YES)
            .trialDateFrom(LocalDate.now())
            .trialDateTo(null)
            .unavailableTrialRequiredYesOrNo(YES)
            .generalAppUnavailableDates(wrapElements(range1))
            .build();

        List<String> errors = service.validateHearingScreen(hearingDetails);

        assertThat(errors).isNotEmpty().contains(INVALID_UNAVAILABILITY_RANGE.getValue());
    }

    @Test
    void shouldNotReturnErrors_whenUnavailabilityIsNotSet() {
        GAHearingDetails hearingDetails = GAHearingDetails.builder()
            .trialRequiredYesOrNo(NO)
            .trialDateFrom(null)
            .trialDateTo(null)
            .unavailableTrialRequiredYesOrNo(NO)
            .generalAppUnavailableDates(null)
            .build();

        List<String> errors = service.validateHearingScreen(hearingDetails);

        assertThat(errors).isEmpty();
    }

    @Test
    void shouldNotReturnErrors_whenUnavailabilityIsSetAndDateFromIsValidWithNullDateTo() {
        GAUnavailabilityDates range1 = GAUnavailabilityDates.builder()
            .unavailableTrialDateFrom(LocalDate.now())
            .unavailableTrialDateTo(null)
            .build();

        GAHearingDetails hearingDetails = GAHearingDetails.builder()
            .trialRequiredYesOrNo(YES)
            .trialDateFrom(LocalDate.now())
            .trialDateTo(null)
            .unavailableTrialRequiredYesOrNo(NO)
            .generalAppUnavailableDates(wrapElements(range1))
            .build();

        List<String> errors = service.validateHearingScreen(hearingDetails);

        assertThat(errors).isEmpty();
    }

    @Test
    void shouldNotReturnErrors_whenUnavailabilityIsSetAndDateFromIsValidWithSameDateTo() {
        GAUnavailabilityDates range1 = GAUnavailabilityDates.builder()
            .unavailableTrialDateFrom(LocalDate.now())
            .unavailableTrialDateTo(LocalDate.now())
            .build();
        GAHearingDetails hearingDetails = GAHearingDetails.builder()
            .trialRequiredYesOrNo(YES)
            .trialDateFrom(LocalDate.now())
            .trialDateTo(null)
            .unavailableTrialRequiredYesOrNo(NO)
            .generalAppUnavailableDates(wrapElements(range1))
            .build();

        List<String> errors = service.validateHearingScreen(hearingDetails);

        assertThat(errors).isEmpty();
    }

    @Test
    void shouldNotReturnErrors_whenUnavailabilityIsSetAndDateFromIsBeforeDateTo() {
        GAUnavailabilityDates range1 = GAUnavailabilityDates.builder()
            .unavailableTrialDateFrom(LocalDate.now())
            .unavailableTrialDateTo(LocalDate.now().plusDays(1))
            .build();
        GAHearingDetails hearingDetails = GAHearingDetails.builder()
            .trialRequiredYesOrNo(YES)
            .trialDateFrom(LocalDate.now())
            .trialDateTo(null)
            .unavailableTrialRequiredYesOrNo(NO)
            .generalAppUnavailableDates(wrapElements(range1))
            .build();

        List<String> errors = service.validateHearingScreen(hearingDetails);

        assertThat(errors).isEmpty();
    }

    @Override
    protected List<LocationRefData> getSampleCourLocationsRefObject() {
        return new ArrayList<>(List.of(
            LocationRefData.builder()
                .epimmsId("11111").siteName("locationOfRegion2").courtAddress(
                    "Prince William House, Peel Cross Road, Salford")
                .postcode("M5 4RR")
                .courtLocationCode("court1").build()
        ));
    }
}

