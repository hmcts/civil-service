package uk.gov.hmcts.reform.civil.service.sdo;

/**
 * Shared textual fragments used across the track default builders.
 */
final class SdoTrackOrderText {

    static final String WITNESS_STATEMENT =
        "This witness statement is limited to 10 pages per party, including any appendices.";

    static final String LATER_THAN_FOUR_PM_TEXT = "later than 4pm on";

    static final String CLAIMANT_EVIDENCE_TEXT =
        "and the claimant's evidence in reply if so advised to be uploaded by 4pm on";

    static final String PARTIES_LIASE_TEXT =
        "The parties are to liaise and use reasonable endeavours to agree the basic hire rate no ";

    static final String HEARING_TIME_TEXT_AFTER =
        "The claimant must by no later than 4 weeks before the hearing date, pay the court the "
            + "required hearing fee or submit a fully completed application for Help with Fees. \nIf the "
            + "claimant fails to pay the fee or obtain a fee exemption by that time the claim will be "
            + "struck without further order.";

    private SdoTrackOrderText() {
        // Utility class
    }
}
