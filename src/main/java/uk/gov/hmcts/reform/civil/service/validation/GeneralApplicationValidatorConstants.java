package uk.gov.hmcts.reform.civil.service.validation;

public interface GeneralApplicationValidatorConstants {

    String INVALID_UNAVAILABILITY_RANGE = "Unavailability Date From cannot be after "
        + "Unavailability Date to. Please enter valid range.";
    String URGENCY_DATE_REQUIRED = "Details of urgency consideration date required.";
    String URGENCY_DATE_SHOULD_NOT_BE_PROVIDED = "Urgency consideration date should not be "
        + "provided for a non-urgent application.";
    String URGENCY_DATE_CANNOT_BE_IN_PAST = "The date entered cannot be in the past.";
    String TRIAL_DATE_FROM_REQUIRED = "Please enter the Date from if the trial has been fixed";
    String INVALID_TRIAL_DATE_RANGE = "Trial Date From cannot be after Trial Date to. "
        + "Please enter valid range.";
    String UNAVAILABLE_DATE_RANGE_MISSING = "Please provide at least one valid Date from if you "
        + "cannot attend hearing within next 3 months.";
    String UNAVAILABLE_FROM_MUST_BE_PROVIDED = "If you selected option to be unavailable then "
        + "you must provide at least one valid Date from";
}
