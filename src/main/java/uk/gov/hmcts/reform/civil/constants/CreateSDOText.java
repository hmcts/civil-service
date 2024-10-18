package uk.gov.hmcts.reform.civil.constants;

public class CreateSDOText {

    public static final String CONFIRMATION_HEADER_SDO = "# Your order has been issued"
        + "%n## Claim number: %s";

    public static final String CONFIRMATION_SUMMARY_1_V_1 = "<br/>The Directions Order has been sent to:"
        + "<br/>%n%n<strong>Claimant 1</strong>%n"
        + "<br/>%s"
        + "<br/>%n%n<strong>Defendant 1</strong>%n"
        + "<br/>%s";

    public static final String CONFIRMATION_SUMMARY_2_V_1 = "<br/>The Directions Order has been sent to:"
        + "<br/>%n%n<strong>Claimant 1</strong>%n"
        + "<br/>%s"
        + "<br/>%n%n<strong>Claimant 2</strong>%n"
        + "<br/>%s"
        + "<br/>%n%n<strong>Defendant 1</strong>%n"
        + "<br/>%s";
    public static final String CONFIRMATION_SUMMARY_1_V_2 = "<br/>The Directions Order has been sent to:"
        + "<br/>%n%n<strong>Claimant 1</strong>%n"
        + "<br/>%s"
        + "<br/>%n%n<strong>Defendant 1</strong>%n"
        + "<br/>%s"
        + "<br/>%n%n<strong>Defendant 2</strong>%n"
        + "<br/>%s";
    public static final String UPON_CONSIDERING =
        "Upon considering the claim form, particulars of claim, statements of case and Directions questionnaires";
    public static final String HEARING_TIME_TEXT_AFTER =
        "The claimant must by no later than 4 weeks before the hearing date, pay the court the "
            + "required hearing fee or submit a fully completed application for Help with Fees. \nIf the "
            + "claimant fails to pay the fee or obtain a fee exemption by that time the claim will be "
            + "struck without further order.";

    public static final String WITNESS_STATEMENT = "This witness statement is limited to 10 pages per party, including any appendices.";
    public static final String LATER_THAN_FOUR_PM = "later than 4pm on";
    public static final String CLAIMANT_EVIDENCE = "and the claimant's evidence in reply if so advised to be uploaded by 4pm on";

    public static final String HEARING_CHANNEL_SDO = "HearingChannel";
    public static final String SPEC_SERVICE_ID = "AAA6";
    public static final String UNSPEC_SERVICE_ID = "AAA7";

    public static final String FEEDBACK_LINK = "<p>%s"
        + " <a href='https://www.smartsurvey.co.uk/s/QKJTVU//' target=_blank>here</a></p>";

    public static final String ERROR_MESSAGE_DATE_MUST_BE_IN_THE_FUTURE = "Date must be in the future";
    public static final String ERROR_MESSAGE_NUMBER_CANNOT_BE_LESS_THAN_ZERO = "The number entered cannot be less than zero";

    private CreateSDOText() {
    }
}
