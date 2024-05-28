package uk.gov.hmcts.reform.civil.constants;

public final class SdoR2UiConstantSmallClaim {

    private SdoR2UiConstantSmallClaim() {
        //To satisfy sonarQube
    }

    public static final String JUDGE_RECITAL = "Upon considering the statements of case and the information provided by the parties.";
    public static final String PPI_DESCRIPTION = """
        send to the Claimant(s):
        a) A schedule of the following amounts itemised by date;
        i) PPI premium charged,
        ii) Commission receivable by the Defendant(s),
        iii) Rate of commission as a percentage of the premium charged,
        iv) Totals and average commission rates, in respect of the whole period to which the claim relates,
        v) Contractual interest associated with each such premium charged.
        b) A copy of the credit agreement;
        c) A copy of the PPI policy application and agreement;
        d) Any cancellation notices;
        e) Any correspondence in connection with a complaint by the Claimant(s);
        f) A copy of any correspondence as to redress if not included in (e);
        g) The amount, if any, of redress (under the FCA scheme or otherwise) and the date it was paid.

        The parties shall endeavour to agree a calculation of the sums in issue and upload to the Digital Portal the agreed calculation.\
         In default of agreement each party shall upload to the Digital Portal its own calculation not later than 7 days before any hearing.\
         The calculation must show the amount which is intended to remove any unfairness from the relationship, broken down into:
        a) The principal sum claimed;
        b) The amount of contractual interest already paid on that sum;
        c) The amount of discretionary interest sought under to Section 69 County Courts Act 1984, if claimed, at rates of 1%, 2%, 4% and 8% per annum.""";
    public static final String UPLOAD_DOC_DESCRIPTION = "Each party must upload to the Digital Portal copies of all documents which they wish the court to consider" +
        " when reaching " +
        "its decision not less than 14 days before the hearing. " +
        "The Court may refuse to consider any document which has not been uploaded to the Digital Portal by the above date.";
    public static final String WITNESS_STATEMENT_TEXT = "Each party must upload to the Digital Portal copies of all " +
        "witness statements of the witnesses upon whose evidence they " +
        "intend to rely at the hearing not less than 14 days before the hearing.";
    public static final String WITNESS_DESCRIPTION_TEXT = """
        a) Start with the name of the case and the claim number;
        b) State the full name and address of the witness;
        c) Set out the witness’s evidence clearly in numbered paragraphs on numbered pages;
        d) End with this paragraph: ‘I believe that the facts stated in this witness statement are true.\
        I understand that proceedings for contempt of court may be brought against anyone who makes, or causes to be made,\
        a false statement in a document verified by a statement of truth without an honest belief in its truth’;
        e) Be signed by the witness and dated;
        f) If a witness is unable to read the statement there must be a \
        certificate that it has been read or interpreted to the witness by a suitably qualified person and at the final hearing there must be an independent\
         interpreter who will not be provided by the Court.

        The Judge may refuse to allow a witness to give evidence or consider any statement of any witness whose statement\
         has not been uploaded to the Digital Portal in accordance with the paragraphs above.

        A witness whose statement has been uploaded in accordance with the above must attend the hearing. If they do not attend,\
        it will be for the Court to decide how much reliance, if any, to place on their evidence.""";
    public static final String RESTRICT_WITNESS_TEXT = "For this limitation, a party is counted as a witness.";
    public static final String RESTRICT_NUMBER_PAGES_TEXT1 = "Each witness statement should be no more than";
    public static final String RESTRICT_NUMBER_PAGES_TEXT2 = "pages of A4 (including exhibits). Statements should" +
        " be double spaced using a font size of 12.";
    public static final String BUNDLE_TEXT = "The Claimant's solicitor shall bring to the court, on the day of the hearing, a paper copy of the hearing bundle.";
    public static final String IMP_NOTES_TEXT = "This order has been made without hearing. Each party has the right to apply to have this Order set aside or varied." +
        " Any such application must be received by the Court" +
        " (together with the appropriate fee) by 4pm on";
    public static final String CARM_MEDIATION_TEXT = "If you failed to attend a mediation appointment,"
        + " then the judge at the hearing may impose a sanction. "
        + "This could require you to pay costs, or could result in your claim or defence being dismissed. "
        + "You should deliver to every other party, and to the court, your explanation for non-attendance, "
        + "with any supporting documents, at least 14 days before the hearing. "
        + "Any other party who wishes to comment on the failure to attend the mediation appointment should "
        + "deliver their comments,"
        + " with any supporting documents, to all parties and to the court at least "
        + "14 days before the hearing.";
}
