package uk.gov.hmcts.reform.civil.service.directionsorder;

/**
 * Central store for specialist narrative fragments (Scott schedules, clinical bundles, etc.)
 * so SDO and DJ generators/tasks can share identical text without duplicating literals.
 */
public final class DirectionsOrderSpecialistTextLibrary {

    private DirectionsOrderSpecialistTextLibrary() {
        // utility
    }

    /* Shared judges’ recital text */
    private static final String JUDGES_RECITAL_STATEMENTS_OF_CASE_BASE =
        "Upon considering the statements of case and the information provided by the parties";
    public static final String JUDGES_RECITAL_STATEMENTS_OF_CASE_WITH_COMMA =
        JUDGES_RECITAL_STATEMENTS_OF_CASE_BASE + ",";
    public static final String JUDGES_RECITAL_STATEMENTS_OF_CASE_WITH_PERIOD =
        JUDGES_RECITAL_STATEMENTS_OF_CASE_BASE + ".";
    public static final String DISPOSAL_JUDGES_RECITAL_CLAIM_FORM =
        "Upon considering the claim form, particulars of claim, statements of case and Directions questionnaires";

    /* Scott schedule: building disputes */
    public static final String BUILDING_SCHEDULE_INTRO_SDO =
        "The claimant must prepare a Scott Schedule of the defects, items of damage, "
            + "or any other relevant matters";
    public static final String BUILDING_SCHEDULE_INTRO_DJ =
        "The claimant must prepare a Scott Schedule of the defects, items of damage "
            + "or any other relevant matters";
    public static final String BUILDING_SCHEDULE_COLUMNS_SDO =
            """
                    The columns should be headed:
                      •  Item
                      •  Alleged defect
                      •  Claimant’s costing
                      •  Defendant’s response
                      •  Defendant’s costing
                      •  Reserved for Judge’s use""";
    public static final String BUILDING_SCHEDULE_COLUMNS_DJ =
            """
                    The columns should be headed:\s
                     - Item\s
                     - Alleged Defect\s
                     - Claimant's costing
                     - Defendant's\
                     response
                     - Defendant's costing\s
                     - Reserved for Judge's use""";
    public static final String BUILDING_SCHEDULE_CLAIMANT_INSTRUCTION =
        "The claimant must upload to the Digital Portal the Scott Schedule with the relevant columns completed by 4pm on";
    public static final String BUILDING_SCHEDULE_DEFENDANT_INSTRUCTION =
        "The defendant must upload to the Digital Portal an amended version of the Scott Schedule "
            + "with the relevant columns in response completed by 4pm on";

    /* Scott schedule: housing disrepair */
    public static final String HOUSING_SCHEDULE_INTRO_SDO =
        "The claimant must prepare a Scott Schedule of the items in disrepair.";
    public static final String HOUSING_SCHEDULE_INTRO_DJ =
        "The claimant must prepare a Scott Schedule of the items in disrepair";
    public static final String HOUSING_SCHEDULE_COLUMNS_SDO =
            """
                    The columns should be headed:
                      •  Item
                      •  Alleged disrepair
                      •  Defendant’s response
                      •  Reserved for Judge’s use""";
    public static final String HOUSING_SCHEDULE_COLUMNS_DJ =
            """
                    The columns should be headed:\s
                     - Item\s
                     - Alleged disrepair\s
                     - Defendant's Response\s
                     - \
                    Reserved for Judge's Use""";
    public static final String HOUSING_SCHEDULE_CLAIMANT_INSTRUCTION =
        "The claimant must upload to the Digital Portal the Scott Schedule with the relevant "
            + "columns completed by 4pm on";
    public static final String HOUSING_SCHEDULE_DEFENDANT_INSTRUCTION =
        "The defendant must upload to the Digital Portal the amended Scott Schedule with the "
            + "relevant columns in response completed by 4pm on";

    public static final String HOUSING_DISREPAIR_CLAUSE_A =
        "a)   The Claimant has permission to rely on the written expert surveying report uploaded to the Portal "
            + "with the Particulars of Claim.";
    public static final String HOUSING_DISREPAIR_CLAUSE_B =
        "b)   The Defendant has permission to rely on the written report of an expert surveyor "
            + "and if that report has not already been [served/uploaded] then it must be served by 4pm";
    public static final String HOUSING_DISREPAIR_CLAUSE_C_BEFORE_DATE =
        "c)   Where the Defendant has served an expert report, the parties' experts "
            + "shall liaise to seek to narrow the scope of disagreement between them and shall, by 4pm";
    public static final String HOUSING_DISREPAIR_CLAUSE_C_AFTER_DATE =
        ", serve on each party a joint statement setting out areas of agreement, "
            + "areas of disagreement and brief reasons for any areas of disagreement.";
    public static final String HOUSING_DISREPAIR_CLAUSE_D =
        "d)   The claimant shall upload the joint statement by no later than 7 days of service "
            + "of the same in accordance with paragraph c.";
    public static final String HOUSING_DISREPAIR_CLAUSE_E =
        "e)   Any party seeking to rely on oral expert evidence must make an application for "
            + "permission within 14 days of the submission of the above.";

    /* Fast track & DJ – disclosure, schedules, and bundle text */
    public static final String FAST_TRACK_DISCLOSURE_STANDARD_SDO =
        "Standard disclosure shall be provided by the parties by uploading to the Digital Portal their list of documents by 4pm on";
    public static final String FAST_TRACK_DISCLOSURE_STANDARD_DJ =
        "Standard disclosure shall be provided by the parties by uploading to the digital portal their lists of documents by 4pm on";
    public static final String FAST_TRACK_DISCLOSURE_INSPECTION =
        "Any request to inspect a document, or for a copy of a document, shall be made directly to the other party by 4pm on";
    public static final String FAST_TRACK_DISCLOSURE_REQUESTS_WITHIN_SEVEN_DAYS_SDO =
        "Requests will be complied with within 7 days of the receipt of the request.";
    public static final String FAST_TRACK_DISCLOSURE_REQUESTS_WITHIN_SEVEN_DAYS_DJ =
        "Requests will be complied with within 7 days of the receipt of the request";
    public static final String FAST_TRACK_DISCLOSURE_UPLOAD_PREFIX =
        "Each party must upload to the Digital Portal copies of those documents on which they wish to rely at trial";
    public static final String FAST_TRACK_DISCLOSURE_UPLOAD_DEADLINE = "by 4pm on";
    public static final String FAST_TRACK_SCHEDULE_CLAIMANT_UPLOAD =
        "The claimant must upload to the Digital Portal an up-to-date schedule of loss by 4pm on";
    public static final String FAST_TRACK_SCHEDULE_DEFENDANT_UPLOAD =
        "If the defendant wants to challenge this claim, upload to the Digital Portal counter-schedule of loss by 4pm on";
    public static final String FAST_TRACK_SCHEDULE_FUTURE_LOSS_SDO =
        "If there is a claim for future pecuniary loss and the parties have not already set out their case on "
            + "periodical payments, they must do so in the respective schedule and counter-schedule.";
    public static final String FAST_TRACK_SCHEDULE_FUTURE_LOSS_DJ =
        "If there is a claim for future pecuniary loss and the parties have not already set out their case on "
            + "periodical payments. then they must do so in the respective schedule and counter-schedule";
    public static final String FAST_TRACK_TRIAL_TIME_ALLOWED_SDO = "The time provisionally allowed for this trial is";
    public static final String FAST_TRACK_TRIAL_TIME_ALLOWED_DJ = "The time provisionally allowed for the trial is";
    public static final String FAST_TRACK_TRIAL_TIME_WARNING_SDO =
        "If either party considers that the time estimate is insufficient, they must inform the court within "
            + "7 days of the date stated on this order.";
    public static final String FAST_TRACK_TRIAL_TIME_WARNING_DJ =
        "If either party considers that the time estimates is insufficient, they must inform the court within "
            + "7 days of the date of this order.";
    public static final String FAST_TRACK_TRIAL_HEARING_HELP_TEXT =
        "If either party considers that the time estimate is insufficient, they must inform the court within "
            + "7 days of the date of this order.";
    public static final String FAST_TRACK_TRIAL_BUNDLE_NOTICE =
        "At least 7 days before the trial, the claimant must upload to the Digital Portal";
    public static final String FAST_TRACK_TRIAL_MANUAL_BUNDLE_GUIDANCE =
        "Not more than seven nor less than three clear days before the trial, the claimant must file at court and serve "
            + "an indexed and paginated bundle of documents which complies with the requirements of Rule 39.5 Civil "
            + "Procedure Rules and which complies with requirements of PD32. The parties must endeavour to agree the "
            + "contents of the bundle before it is filed. The bundle will include a case summary and a chronology.";

    /* Clinical negligence retention/bundle text */
    public static final String CLINICAL_DOCUMENTS_HEADING = "Documents should be retained as follows:";
    public static final String CLINICAL_PARTIES_SDO =
        "a) The parties must retain all electronically stored documents relating to the issues in this claim.";
    public static final String CLINICAL_PARTIES_DJ =
        "the parties must retain all electronically stored documents relating to the issues in this Claim.";
    public static final String CLINICAL_NOTES_SDO =
        "b) the defendant must retain the original clinical notes relating to the issues in this claim. "
            + "The defendant must give facilities for inspection by the claimant, the claimant's legal "
            + "advisers and experts of these original notes on 7 days written notice.";
    public static final String CLINICAL_NOTES_DJ =
        "the defendant must retain the original clinical notes relating to the issues in this Claim. "
            + "The defendant must give facilities for inspection by the claimant, "
            + "the claimant's legal advisers and experts of these original notes on 7 days written notice.";
    public static final String CLINICAL_BUNDLE_SDO =
        "c) Legible copies of the medical and educational records of the claimant "
            + "are to be placed in a separate paginated bundle by the claimant's "
            + "solicitors and kept up to date. All references to medical notes are to be made by reference "
            + "to the pages in that bundle.";
    public static final String CLINICAL_BUNDLE_DJ =
        "Legible copies of the medical and educational records of the claimant are to be placed in a"
            + " separate paginated bundle by the claimant’s solicitors and kept up to date. All references "
            + "to medical notes are to be made by reference to the pages in that bundle";

    /* Credit hire: SDO narrative */
    public static final String CREDIT_HIRE_DISCLOSURE_SDO =
            """
                    If impecuniosity is alleged by the claimant and not admitted by the defendant, the claimant's \
                    disclosure as ordered earlier in this Order must include:
                    a) Evidence of all income from all sources for a period of 3 months prior to the \
                    commencement of hire until the earlier of:
                     \
                         i) 3 months after cessation of hire
                         ii) the repair or replacement of the claimant's vehicle
                    b) Copies of all bank, credit card, and saving account statements for a period of 3 months \
                    prior to the commencement of hire until the earlier of:
                         i) 3 months after cessation of hire
                         ii) the repair or replacement of the claimant's vehicle
                    c) Evidence of any loan, overdraft or other credit facilities available to the claimant.""";
    public static final String CREDIT_HIRE_BASIC_RATE_EVIDENCE_SDO =
        "If the parties fail to agree basic hire rates pursuant to the paragraph above, each party may rely "
            + "upon written evidence by way of witness statement of one witness to provide evidence of basic "
            + "hire rates available within the claimant's geographical location, from a mainstream supplier, "
            + "or a local reputable supplier if none is available.";
    public static final String CREDIT_HIRE_BASIC_RATE_EVIDENCE_WITH_LIABILITY =
        "If the parties fail to agree rates subject to liability and/or other issues pursuant to the paragraph "
            + "above, each party may rely upon written evidence by way of witness statement of one witness to "
            + "provide evidence of basic hire rates available within the claimant's geographical location, "
            + "from a mainstream supplier, or a local reputable supplier if none is available.";
    public static final String CREDIT_HIRE_BASIC_RATE_EVIDENCE_WITH_LIABILITY_DJ =
        "If the parties fail to agree rates subject to liability and/or other issues pursuant to the paragraph above, "
            + "each party may rely upon the written evidence by way of witness statement of one witness to provide "
            + "evidence of basic hire rates available within the claimant’s geographical location from a mainstream "
            + "supplier, or a local reputable supplier if none is available.";
    public static final String CREDIT_HIRE_DEFENDANT_UPLOAD_SDO =
        "The defendant's evidence is to be uploaded to the Digital Portal by 4pm on";
    public static final String CREDIT_HIRE_STATEMENT_PROMPT_SDO =
            """
                    The claimant must upload to the Digital Portal a witness statement addressing
                    a) the need to hire a replacement vehicle; and
                    b) impecuniosity""";
    public static final String CREDIT_HIRE_NON_COMPLIANCE_SDO =
        "A failure to comply with the paragraph above will result in the claimant being debarred from asserting "
            + "need or relying on impecuniosity as the case may be at the final hearing, "
            + "save with permission of the Trial Judge.";
    public static final String CREDIT_HIRE_PARTIES_LIAISE =
        "The parties are to liaise and use reasonable endeavours to agree the basic hire rate no later than 4pm on";
    public static final String CREDIT_HIRE_CLAIMANT_EVIDENCE_SDO =
        "and the claimant's evidence in reply if so advised to be uploaded by 4pm on";
    public static final String CREDIT_HIRE_WITNESS_LIMIT_SDO =
        "This witness statement is limited to 10 pages per party, including any appendices.";

    /* Credit hire: DJ narrative */
    public static final String CREDIT_HIRE_DISCLOSURE_DJ =
            """
                    If impecuniosity is alleged by the claimant and not admitted by the defendant, the claimant's \
                    disclosure as ordered earlier in this order must include:
                    a. Evidence of all income from all sources for a period of 3 months prior to the \
                    commencement of hire until the earlier of\s
                        i) 3 months after cessation of hire or\s
                        ii) \
                    the repair or replacement of the claimant's vehicle;
                    b. Copy statements of all bank, credit card and savings account statements for a period of 3 months \
                    prior to the commencement of hire until the earlier of\s
                        i) 3 months after cessation of hire \
                    or\s
                        ii) the repair or replacement of the claimant's vehicle;
                    c. Evidence of any loan, overdraft or other credit facilities available to the claimant""";
    public static final String CREDIT_HIRE_DEFENDANT_UPLOAD_DJ =
        "The defendant’s evidence is to be uploaded to the Digital Portal by 4pm on";
    public static final String CREDIT_HIRE_STATEMENT_PROMPT_DJ =
            """
                    The claimant must upload to the Digital Portal a witness \
                    statement addressing\s
                    a) the need to hire a replacement \
                    vehicle; and\s
                    b) impecuniosity""";
    public static final String CREDIT_HIRE_STATEMENT_DEADLINE_DJ =
        "This statement must be uploaded to the Digital Portal by 4pm on";
    public static final String CREDIT_HIRE_NON_COMPLIANCE_DJ =
        "A failure to comply will result in the claimant being "
            + "debarred from asserting need or relying on impecuniosity "
            + "as the case may be at the final hearing, unless they "
            + "have the permission of the trial Judge.";
    public static final String CREDIT_HIRE_CLAIMANT_EVIDENCE_DJ =
        "and the claimant’s evidence in reply if "
            + "so advised is to be uploaded by 4pm on";
    public static final String CREDIT_HIRE_WITNESS_LIMIT_DJ =
        "This witness statement is limited to 10 pages per party "
            + "(to include any appendices).";

    /* Personal injury (SDO vs DJ variants share question/answer text) */
    public static final String PERSONAL_INJURY_PERMISSION_SDO =
        "The claimant has permission to rely upon the written expert evidence already uploaded to "
            + "the Digital Portal with the particulars of claim and in addition has permission to rely upon"
            + " any associated correspondence or updating report which is uploaded to the Digital Portal by"
            + " 4pm on";
    public static final String PERSONAL_INJURY_PERMISSION_DJ =
        "The claimant has permission to rely upon the written "
            + "expert evidence already uploaded to the Digital"
            + " Portal with the particulars of claim and in addition "
            + "has permission to rely upon any associated "
            + "correspondence or updating report which is uploaded "
            + "to the Digital Portal by 4pm on";
    public static final String PERSONAL_INJURY_QUESTIONS =
        "Any questions which are to be addressed to an expert must be sent to the expert directly "
            + "and uploaded to the Digital Portal by 4pm on";
    public static final String PERSONAL_INJURY_ANSWERS =
        "The answers to the questions shall be answered by the Expert by";
    public static final String PERSONAL_INJURY_UPLOAD =
        "and uploaded to the Digital Portal by";

    /* Road traffic accident */
    public static final String ROAD_TRAFFIC_ACCIDENT_UPLOAD_SDO =
        "Photographs and/or a plan of the accident location shall be prepared and agreed by the "
            + "parties and uploaded to the Digital Portal by 4pm on";
    public static final String ROAD_TRAFFIC_ACCIDENT_UPLOAD_DJ =
        "Photographs and/or a plan of the accident location "
            + "shall be prepared "
            + "and agreed by the parties and uploaded to the"
            + " Digital Portal by 4pm on";
    public static final String ROAD_TRAFFIC_ACCIDENT_SMALL_CLAIMS =
        "Photographs and/or a plan of the accident location shall be prepared and agreed by the parties"
            + " and uploaded to the Digital Portal no later than 21 days before the hearing.";

    /* Small claims – generic Digital Portal paragraphs */
    public static final String SMALL_CLAIMS_DOCUMENTS_UPLOAD =
        "Each party must upload to the Digital Portal copies of all documents which they wish the"
            + " court to consider when reaching its decision not less than 21 days before the hearing.";
    public static final String SMALL_CLAIMS_DOCUMENTS_WARNING =
        "The court may refuse to consider any document which has not been uploaded to the Digital "
            + "Portal by the above date.";
    public static final String SMALL_CLAIMS_WITNESS_DEADLINE =
        "Witness statements shall be uploaded to the Digital Portal by 4pm on";
    public static final String SMALL_CLAIMS_WITNESS_LATE_WARNING =
        "Evidence will not be permitted at trial from a witness whose statement has not been uploaded"
            + " in accordance with this Order. Evidence not uploaded, or uploaded late, will not be permitted"
            + " except with permission from the Court";
    public static final String SMALL_CLAIMS_HEARING_FEE_WARNING =
        "The claimant must by no later than 4 weeks before the hearing date, pay the court the "
            + "required hearing fee or submit a fully completed application for Help with Fees. \nIf the "
            + "claimant fails to pay the fee or obtain a fee exemption by that time the claim will be "
            + "struck without further order.";
    public static final String SMALL_CLAIMS_HEARING_LISTING_NOTICE =
        "The hearing of the claim will be on a date to be notified to you by a separate notification. "
            + "The hearing will have a time estimate of";

    /* Witness statement pagination text */
    public static final String WITNESS_COUNT_LIMIT_NOTE =
        "For this limitation, a party is counted as a witness.";
    public static final String WITNESS_COUNT_LIMIT_NOTE_DJ =
        "For this limitation, a party is counted as witness.";
    public static final String WITNESS_PAGE_LIMIT_PREFIX =
        "Each witness statement should be no more than";
    public static final String WITNESS_PAGE_LIMIT_SUFFIX =
        "pages of A4 (including exhibits). Statements should be double spaced using a font size of 12.";
    public static final String WITNESS_PAGE_LIMIT_SUFFIX_DJ =
        "A4 pages. Statements should be double spaced using a font size of 12.";

    /* Trial witnesses (DJ) */
    public static final String TRIAL_WITNESS_STATEMENT_UPLOAD_NOTICE =
        "Each party must upload to the Digital Portal copies of the statements of all witnesses of fact "
            + "on whom they intend to rely.";

    /* NIHL-specific addendum / audiogram text */
    public static final String NIHL_ADDENDUM_REPORT_PERMISSION =
        "The Claimant has permission to rely upon an addendum report from their expert ENT surgeon which"
            + " must be uploaded to the Digital Portal by 4pm on";
    public static final String NIHL_FURTHER_AUDIOGRAM_REQUEST =
        "The Claimant shall undergo a single further audiogram at the written request of any Defendant."
            + " Such request to be made no later than 4pm on";
    public static final String NIHL_FURTHER_AUDIOGRAM_SERVICE =
        "The further audiogram shall be arranged and paid for by the Defendant requesting it. The Defendant"
            + " shall serve a copy of the further audiogram on the Claimant and upload it to the Digital Portal"
            + " by 4pm on";
    public static final String NIHL_CLAIMANT_PERMISSION_TO_RELY =
        "The Claimant has permission to rely upon the written expert evidence already uploaded to the Digital Portal "
            + "with the particulars of claim.";
    public static final String NIHL_DEFENDANT_MAY_ASK =
        "The Defendant(s) may ask questions of the Claimant's expert which must be sent to the expert directly and "
            + "uploaded to the Digital Portal by 4pm on";
    public static final String NIHL_QUESTIONS_SHALL_BE_ANSWERED =
        "The questions shall be answered by the expert by";
    public static final String NIHL_UPLOAD_TO_DIGITAL_PORTAL =
        "by the party asking within 7 days of receipt.";
    public static final String NIHL_APPLICATION_TO_RELY_DETAILS =
        "Any application by the Defendant for permission to rely on further expert medical evidence shall be made by "
            + "4pm on";
    public static final String NIHL_PERMISSION_TO_RELY_ON_EXPERT =
        "The Defendant has permission to rely on written expert evidence from a consultant ENT surgeon. Such report "
            + "shall be uploaded to the Digital Portal by 4pm on";
    public static final String NIHL_JOINT_MEETING_OF_EXPERTS =
        "The experts instructed by each party shall discuss their reports and shall prepare a schedule of agreement "
            + "and disagreement which shall be provided to the parties by 4pm on";
    public static final String NIHL_UPLOAD_WITHIN_SEVEN_DAYS =
        "by the Claimant within 7 days of receipt.";
    public static final String NIHL_EVIDENCE_ACOUSTIC_ENGINEER =
        "The parties have permission to rely on the jointly instructed written evidence of an expert acoustic engineer.";
    public static final String NIHL_INSTRUCTION_OF_EXPERT =
        "The expert shall be agreed and instructed by";
    public static final String NIHL_INSTRUCTION_OF_EXPERT_FOLLOWUP =
        "If no expert has been instructed by the date the Claimant must apply to court by 4pm the following day for "
            + "further directions.";
    public static final String NIHL_EXPERT_REPORT =
        "The expert will report to the instructing parties by";
    public static final String NIHL_EXPERT_REPORT_UPLOAD =
        "by the Claimant within 7 days of receipt.";
    public static final String NIHL_WRITTEN_QUESTIONS =
        "Written questions may be posed by any party directly to the single jointly instructed expert by";
    public static final String NIHL_WRITTEN_QUESTIONS_UPLOAD =
        "by the same date.";
    public static final String NIHL_REPLIES =
        "send the answers to questions to the asking party by";
    public static final String NIHL_REPLIES_UPLOAD =
        "by that party within 7 days of receipt.";
    public static final String NIHL_SERVICE_OF_ORDER =
        "A copy of this order must be served on the expert by the Claimant with the expert's instructions.";
    public static final String NIHL_ENT_WRITTEN_QUESTIONS =
        "The parties may put written questions of an ENT engineering expert for whom permission has been given by 4pm on";
    public static final String NIHL_ENT_WRITTEN_QUESTIONS_UPLOAD =
        "and shall upload the same to the Digital Portal by the same date.\nSuch questions shall be limited to issues "
            + "arising from the single jointly instructed expert engineer's report and any answers to questions put to "
            + "the engineer.";
    public static final String NIHL_ENT_QUESTIONS_SHALL_BE_ANSWERED =
        "Such questions shall be answered by the ENT expert by";
    public static final String NIHL_ENT_UPLOAD_WITHIN_SEVEN_DAYS =
        "within 7 days of receipt.";
    public static final String NIHL_UPLOAD_OF_DOCUMENTS =
        "Each party must upload to the Digital Portal copies of those documents on which they wish to rely at trial "
            + "21 days before the hearing.";

    /* Small claims: flight delay */
    public static final String FLIGHT_DELAY_RELATED_CLAIMS_NOTICE =
            """
                    In the event that the Claimant(s) or Defendant(s) are aware if other\s
                    claims relating to the same flight they must notify the court\s
                    where the claim is being managed within 14 days of receipt of\s
                    this Order providing all relevant details of those claims including\s
                    case number(s), hearing date(s) and copy final substantive order(s)\s
                    if any, to assist the Court with ongoing case management which may\s
                    include the cases being heard together.""";
    public static final String FLIGHT_DELAY_LEGAL_ARGUMENTS_NOTICE =
            """
                    Any arguments as to the law to be applied to this claim, together with\s
                    copies of legal authorities or precedents relied on, shall be uploaded\s
                    to the Digital Portal not later than 3 full working days before the\s
                    final hearing date.""";

    /* Disposal hearings */
    public static final String DISPOSAL_DOCUMENTS_EXCHANGE =
        "The parties shall serve on each other copies of the documents upon which reliance is to be"
            + " placed at the disposal hearing by 4pm on";
    public static final String DISPOSAL_DOCUMENTS_UPLOAD =
        "The parties must upload to the Digital Portal copies of those documents which they wish the "
            + "court to consider when deciding the amount of damages, by 4pm on";
    public static final String DISPOSAL_WITNESS_UPLOAD =
        "The claimant must upload to the Digital Portal copies of the witness statements of all "
            + "witnesses of fact on whose evidence reliance is to be placed by 4pm on";
    public static final String DISPOSAL_WITNESS_CPR32_6 =
        "The provisions of CPR 32.6 apply to such evidence.";
    public static final String DISPOSAL_WITNESS_CPR32_7_DEADLINE =
        "Any application by the defendant in relation to CPR 32.7 must be made by 4pm on";
    public static final String DISPOSAL_WITNESS_TRIAL_NOTE_SDO =
        "and must be accompanied by proposed directions for allocation and listing for trial on quantum."
            + " This is because cross-examination will cause the hearing to exceed the 30-minute maximum"
            + " time estimate for a disposal hearing.";
    public static final String DISPOSAL_WITNESS_TRIAL_NOTE_DJ =
        "and must be accompanied by proposed directions for allocation and listing for trial on quantum."
            + " This is because cross-examination will cause the hearing to exceed the 30 minute maximum"
            + " time estimate for a disposal hearing.";
    public static final String DISPOSAL_SCHEDULE_CLAIMANT_UPLOAD_SDO =
        "If there is a claim for ongoing or future loss in the original schedule of losses, the "
            + "claimant must upload to the Digital Portal an up-to-date schedule of loss by 4pm on";
    public static final String DISPOSAL_SCHEDULE_CLAIMANT_SEND_DJ =
        "If there is a claim for ongoing or future loss in the original schedule of losses then the"
            + " claimant must send an up to date schedule of loss to the defendant by 4pm on the";
    public static final String DISPOSAL_SCHEDULE_COUNTER_SEND =
        "If the defendant wants to challenge this claim, they must send an up-to-date counter-schedule"
            + " of loss to the claimant by 4pm on";
    public static final String DISPOSAL_SCHEDULE_COUNTER_UPLOAD_SDO =
        "If the defendant want to challenge the sums claimed in the schedule of loss they must upload"
            + " to the Digital Portal an updated counter schedule of loss by 4pm on";
    public static final String DISPOSAL_SCHEDULE_COUNTER_UPLOAD_DJ =
        "If the defendant wants to challenge the sums claimed in the schedule of loss they must upload"
            + " to the Digital Portal an updated counter schedule of loss by 4pm on";
    public static final String DISPOSAL_SCHEDULE_FUTURE_LOSS =
        "If there is a claim for future pecuniary loss and the parties have not already set out their"
            + " case on periodical payments, they must do so in the respective schedule and counter-schedule.";
    public static final String DISPOSAL_BUNDLE_REQUIREMENT =
        "At least 7 days before the disposal hearing, the claimant must file and serve";
    public static final String DISPOSAL_FINAL_HEARING_LISTING_SDO =
        "This claim will be listed for final disposal before a judge on the first available date after";
    public static final String DISPOSAL_FINAL_HEARING_LISTING_DJ =
        "This claim will be listed for final disposal before a Judge on the first available date after";

    /* Order made without a hearing */
    public static final String ORDER_WITHOUT_HEARING_RECEIVED_BY_COURT_NO_ARTICLE =
        "This order has been made without hearing. Each party has the right to apply to have this Order "
            + "set aside or varied. Any such application must be received by the Court "
            + "(together with the appropriate fee) by 4pm on";
    public static final String ORDER_WITHOUT_HEARING_RECEIVED_BY_COURT_WITH_ARTICLE =
        "This order has been made without a hearing. Each party has the right to apply to have this Order "
            + "set aside or varied. Any such application must be received by the Court "
            + "(together with the appropriate fee) by 4pm on";
    public static final String ORDER_WITHOUT_HEARING_RECEIVED_BY_COURT_LOWERCASE =
        "This order has been made without a hearing. Each party has the right to apply to have this order "
            + "set aside or varied. Any such application must be received by the court "
            + "(together with the appropriate fee) by 4pm on";
    public static final String ORDER_WITHOUT_HEARING_RECEIVED_BY_COURT_BRIEF =
        "This Order has been made without a hearing. Each party has the right to apply to have this Order "
            + "set aside or varied. Any application must be received by the Court, together with the "
            + "appropriate fee by 4pm on";
    public static final String ORDER_WITHOUT_HEARING_UPLOAD_TO_PORTAL_SDO =
        "This Order has been made without a hearing. Each party has the right to apply to have this Order "
            + "set aside or varied. Any such application must be uploaded to the Digital Portal together "
            + "with the appropriate fee, by 4pm on";
    public static final String ORDER_WITHOUT_HEARING_UPLOAD_TO_PORTAL_DJ =
        "This order has been made without a hearing. Each party has the right to apply to have this order "
            + "set aside or varied. Any such application must be uploaded to the Digital Portal together "
            + "with payment of any appropriate fee, by 4pm on";
}
