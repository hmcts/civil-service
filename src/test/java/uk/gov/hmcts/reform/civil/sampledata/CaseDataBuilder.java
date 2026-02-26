package uk.gov.hmcts.reform.civil.sampledata;

import uk.gov.hmcts.reform.ccd.model.ChangeOrganisationApprovalStatus;
import uk.gov.hmcts.reform.ccd.model.Organisation;
import uk.gov.hmcts.reform.ccd.model.OrganisationPolicy;
import uk.gov.hmcts.reform.civil.crd.model.Category;
import uk.gov.hmcts.reform.civil.documentmanagement.model.CaseDocument;
import uk.gov.hmcts.reform.civil.documentmanagement.model.Document;
import uk.gov.hmcts.reform.civil.enums.AllocatedTrack;
import uk.gov.hmcts.reform.civil.enums.BusinessProcessStatus;
import uk.gov.hmcts.reform.civil.enums.CaseCategory;
import uk.gov.hmcts.reform.civil.enums.CaseRole;
import uk.gov.hmcts.reform.civil.enums.CaseState;
import uk.gov.hmcts.reform.civil.enums.ClaimType;
import uk.gov.hmcts.reform.civil.enums.ClaimTypeUnspec;
import uk.gov.hmcts.reform.civil.enums.ConfirmationToggle;
import uk.gov.hmcts.reform.civil.enums.DJPaymentTypeSelection;
import uk.gov.hmcts.reform.civil.enums.DecisionOnRequestReconsiderationOptions;
import uk.gov.hmcts.reform.civil.enums.ExpertReportsSent;
import uk.gov.hmcts.reform.civil.enums.FeeType;
import uk.gov.hmcts.reform.civil.enums.MediationDecision;
import uk.gov.hmcts.reform.civil.enums.MultiPartyScenario;
import uk.gov.hmcts.reform.civil.enums.PaymentFrequencyClaimantResponseLRspec;
import uk.gov.hmcts.reform.civil.enums.PaymentFrequencyLRspec;
import uk.gov.hmcts.reform.civil.enums.PaymentStatus;
import uk.gov.hmcts.reform.civil.enums.PaymentType;
import uk.gov.hmcts.reform.civil.enums.PersonalInjuryType;
import uk.gov.hmcts.reform.civil.enums.ReasonForProceedingOnPaper;
import uk.gov.hmcts.reform.civil.enums.RepaymentFrequencyDJ;
import uk.gov.hmcts.reform.civil.enums.RespondentResponsePartAdmissionPaymentTimeLRspec;
import uk.gov.hmcts.reform.civil.enums.RespondentResponseType;
import uk.gov.hmcts.reform.civil.enums.RespondentResponseTypeSpec;
import uk.gov.hmcts.reform.civil.enums.ResponseIntention;
import uk.gov.hmcts.reform.civil.enums.TimelineUploadTypeSpec;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.enums.dj.DisposalHearingBundleType;
import uk.gov.hmcts.reform.civil.enums.dj.DisposalHearingFinalDisposalHearingTimeEstimate;
import uk.gov.hmcts.reform.civil.enums.dj.DisposalHearingMethodDJ;
import uk.gov.hmcts.reform.civil.enums.dq.GeneralApplicationTypes;
import uk.gov.hmcts.reform.civil.enums.dq.Language;
import uk.gov.hmcts.reform.civil.enums.dq.SupportRequirements;
import uk.gov.hmcts.reform.civil.enums.dq.UnavailableDateType;
import uk.gov.hmcts.reform.civil.enums.hearing.HearingDuration;
import uk.gov.hmcts.reform.civil.enums.hearing.ListingOrRelisting;
import uk.gov.hmcts.reform.civil.enums.mediation.MediationUnsuccessfulReason;
import uk.gov.hmcts.reform.civil.enums.sdo.AddOrRemoveToggle;
import uk.gov.hmcts.reform.civil.enums.sdo.DateToShowToggle;
import uk.gov.hmcts.reform.civil.enums.sdo.DisposalHearingMethod;
import uk.gov.hmcts.reform.civil.enums.sdo.FastTrackHearingTimeEstimate;
import uk.gov.hmcts.reform.civil.enums.sdo.TrialHearingTimeEstimateDJ;
import uk.gov.hmcts.reform.civil.handler.callback.user.spec.show.ResponseOneVOneShowTag;
import uk.gov.hmcts.reform.civil.model.Address;
import uk.gov.hmcts.reform.civil.model.Bundle;
import uk.gov.hmcts.reform.civil.model.BusinessProcess;
import uk.gov.hmcts.reform.civil.model.CCJPaymentDetails;
import uk.gov.hmcts.reform.civil.model.CaseData;
import uk.gov.hmcts.reform.civil.model.CaseNote;
import uk.gov.hmcts.reform.civil.model.CertificateOfService;
import uk.gov.hmcts.reform.civil.model.ChangeOfRepresentation;
import uk.gov.hmcts.reform.civil.model.ClaimProceedsInCaseman;
import uk.gov.hmcts.reform.civil.model.ClaimProceedsInCasemanLR;
import uk.gov.hmcts.reform.civil.model.ClaimValue;
import uk.gov.hmcts.reform.civil.model.CloseClaim;
import uk.gov.hmcts.reform.civil.model.CorrectEmail;
import uk.gov.hmcts.reform.civil.model.CourtLocation;
import uk.gov.hmcts.reform.civil.model.DefendantPinToPostLRspec;
import uk.gov.hmcts.reform.civil.model.Fee;
import uk.gov.hmcts.reform.civil.model.FixedCosts;
import uk.gov.hmcts.reform.civil.model.FlightDelayDetails;
import uk.gov.hmcts.reform.civil.model.HearingDates;
import uk.gov.hmcts.reform.civil.model.HearingSupportRequirementsDJ;
import uk.gov.hmcts.reform.civil.model.IdValue;
import uk.gov.hmcts.reform.civil.model.IdamUserDetails;
import uk.gov.hmcts.reform.civil.model.LengthOfUnemploymentComplexTypeLRspec;
import uk.gov.hmcts.reform.civil.model.LitigationFriend;
import uk.gov.hmcts.reform.civil.model.Mediation;
import uk.gov.hmcts.reform.civil.model.MediationAgreementDocument;
import uk.gov.hmcts.reform.civil.model.MediationSuccessful;
import uk.gov.hmcts.reform.civil.model.PartnerAndDependentsLRspec;
import uk.gov.hmcts.reform.civil.model.Party;
import uk.gov.hmcts.reform.civil.model.PartyFlagStructure;
import uk.gov.hmcts.reform.civil.model.PaymentBySetDate;
import uk.gov.hmcts.reform.civil.model.PaymentDetails;
import uk.gov.hmcts.reform.civil.model.RepaymentPlanLRspec;
import uk.gov.hmcts.reform.civil.model.RespondToClaim;
import uk.gov.hmcts.reform.civil.model.RespondToClaimAdmitPartLRspec;
import uk.gov.hmcts.reform.civil.model.Respondent1EmployerDetailsLRspec;
import uk.gov.hmcts.reform.civil.model.ResponseDocument;
import uk.gov.hmcts.reform.civil.model.SRPbaDetails;
import uk.gov.hmcts.reform.civil.model.ServedDocumentFiles;
import uk.gov.hmcts.reform.civil.model.SmallClaimMedicalLRspec;
import uk.gov.hmcts.reform.civil.model.SolicitorOrganisationDetails;
import uk.gov.hmcts.reform.civil.model.SolicitorReferences;
import uk.gov.hmcts.reform.civil.model.StatementOfTruth;
import uk.gov.hmcts.reform.civil.model.UnavailableDate;
import uk.gov.hmcts.reform.civil.model.UnemployedComplexTypeLRspec;
import uk.gov.hmcts.reform.civil.model.UpdateDetailsForm;
import uk.gov.hmcts.reform.civil.model.UpholdingPreviousOrderReason;
import uk.gov.hmcts.reform.civil.model.breathing.BreathingSpaceEnterInfo;
import uk.gov.hmcts.reform.civil.model.breathing.BreathingSpaceInfo;
import uk.gov.hmcts.reform.civil.model.breathing.BreathingSpaceLiftInfo;
import uk.gov.hmcts.reform.civil.model.breathing.BreathingSpaceType;
import uk.gov.hmcts.reform.civil.model.caseflags.FlagDetail;
import uk.gov.hmcts.reform.civil.model.caseflags.Flags;
import uk.gov.hmcts.reform.civil.model.caseprogression.RevisedHearingRequirements;
import uk.gov.hmcts.reform.civil.model.citizenui.AdditionalLipPartyDetails;
import uk.gov.hmcts.reform.civil.model.citizenui.CaseDataLiP;
import uk.gov.hmcts.reform.civil.model.citizenui.ClaimantMediationLip;
import uk.gov.hmcts.reform.civil.model.citizenui.FeePaymentOutcomeDetails;
import uk.gov.hmcts.reform.civil.model.citizenui.HelpWithFeesDetails;
import uk.gov.hmcts.reform.civil.model.citizenui.HelpWithFeesMoreInformation;
import uk.gov.hmcts.reform.civil.model.citizenui.MediationLiPCarm;
import uk.gov.hmcts.reform.civil.model.citizenui.RespondentLiPResponse;
import uk.gov.hmcts.reform.civil.model.common.DynamicList;
import uk.gov.hmcts.reform.civil.model.common.DynamicListElement;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.model.defaultjudgment.CaseLocationCivil;
import uk.gov.hmcts.reform.civil.model.defaultjudgment.DisposalHearingBundleDJ;
import uk.gov.hmcts.reform.civil.model.defaultjudgment.DisposalHearingFinalDisposalHearingDJ;
import uk.gov.hmcts.reform.civil.model.defaultjudgment.DisposalHearingJudgesRecitalDJ;
import uk.gov.hmcts.reform.civil.model.defaultjudgment.TrialHearingJudgesRecital;
import uk.gov.hmcts.reform.civil.model.defaultjudgment.TrialHearingTrial;
import uk.gov.hmcts.reform.civil.model.defaultjudgment.TrialHearingWitnessOfFact;
import uk.gov.hmcts.reform.civil.model.dq.Applicant1DQ;
import uk.gov.hmcts.reform.civil.model.dq.Applicant2DQ;
import uk.gov.hmcts.reform.civil.model.dq.DisclosureOfElectronicDocuments;
import uk.gov.hmcts.reform.civil.model.dq.DisclosureOfNonElectronicDocuments;
import uk.gov.hmcts.reform.civil.model.dq.Expert;
import uk.gov.hmcts.reform.civil.model.dq.ExpertDetails;
import uk.gov.hmcts.reform.civil.model.dq.Experts;
import uk.gov.hmcts.reform.civil.model.dq.FileDirectionsQuestionnaire;
import uk.gov.hmcts.reform.civil.model.dq.FixedRecoverableCosts;
import uk.gov.hmcts.reform.civil.model.dq.FurtherInformation;
import uk.gov.hmcts.reform.civil.model.dq.Hearing;
import uk.gov.hmcts.reform.civil.model.dq.HearingSupport;
import uk.gov.hmcts.reform.civil.model.dq.RequestedCourt;
import uk.gov.hmcts.reform.civil.model.dq.Respondent1DQ;
import uk.gov.hmcts.reform.civil.model.dq.Respondent2DQ;
import uk.gov.hmcts.reform.civil.model.dq.VulnerabilityQuestions;
import uk.gov.hmcts.reform.civil.model.dq.WelshLanguageRequirements;
import uk.gov.hmcts.reform.civil.model.dq.Witness;
import uk.gov.hmcts.reform.civil.model.dq.Witnesses;
import uk.gov.hmcts.reform.civil.model.genapplication.CaseLink;
import uk.gov.hmcts.reform.civil.model.genapplication.GAApplicationType;
import uk.gov.hmcts.reform.civil.model.genapplication.GAApplicationTypeLR;
import uk.gov.hmcts.reform.civil.model.genapplication.GAHearingDateGAspec;
import uk.gov.hmcts.reform.civil.model.genapplication.GeneralApplication;
import uk.gov.hmcts.reform.civil.model.genapplication.GeneralApplicationsDetails;
import uk.gov.hmcts.reform.civil.model.interestcalc.InterestClaimFromType;
import uk.gov.hmcts.reform.civil.model.interestcalc.InterestClaimOptions;
import uk.gov.hmcts.reform.civil.model.interestcalc.InterestClaimUntilType;
import uk.gov.hmcts.reform.civil.model.interestcalc.SameRateInterestSelection;
import uk.gov.hmcts.reform.civil.model.judgmentonline.JudgmentAddress;
import uk.gov.hmcts.reform.civil.model.judgmentonline.JudgmentDetails;
import uk.gov.hmcts.reform.civil.model.judgmentonline.JudgmentInstalmentDetails;
import uk.gov.hmcts.reform.civil.model.judgmentonline.JudgmentPaidInFull;
import uk.gov.hmcts.reform.civil.model.judgmentonline.JudgmentPaymentPlan;
import uk.gov.hmcts.reform.civil.model.judgmentonline.JudgmentRecordedReason;
import uk.gov.hmcts.reform.civil.model.judgmentonline.JudgmentState;
import uk.gov.hmcts.reform.civil.model.judgmentonline.PaymentFrequency;
import uk.gov.hmcts.reform.civil.model.judgmentonline.PaymentPlanSelection;
import uk.gov.hmcts.reform.civil.model.mediation.MediationAvailability;
import uk.gov.hmcts.reform.civil.model.mediation.MediationContactInformation;
import uk.gov.hmcts.reform.civil.model.mediation.MediationDocumentsReferredInStatement;
import uk.gov.hmcts.reform.civil.model.mediation.MediationDocumentsType;
import uk.gov.hmcts.reform.civil.model.mediation.MediationNonAttendanceStatement;
import uk.gov.hmcts.reform.civil.model.mediation.UploadMediationDocumentsForm;
import uk.gov.hmcts.reform.civil.model.noc.ChangeOrganisationRequest;
import uk.gov.hmcts.reform.civil.model.querymanagement.CaseMessage;
import uk.gov.hmcts.reform.civil.model.querymanagement.CaseQueriesCollection;
import uk.gov.hmcts.reform.civil.model.sdo.DisposalHearingFinalDisposalHearingTimeDJ;
import uk.gov.hmcts.reform.civil.model.sdo.DisposalHearingHearingTime;
import uk.gov.hmcts.reform.civil.model.sdo.DisposalHearingOrderMadeWithoutHearingDJ;
import uk.gov.hmcts.reform.civil.model.sdo.DisposalOrderWithoutHearing;
import uk.gov.hmcts.reform.civil.model.sdo.FastTrackHearingTime;
import uk.gov.hmcts.reform.civil.model.sdo.FastTrackOrderWithoutJudgement;
import uk.gov.hmcts.reform.civil.model.sdo.FastTrackWitnessOfFact;
import uk.gov.hmcts.reform.civil.model.sdo.ReasonForReconsideration;
import uk.gov.hmcts.reform.civil.model.sdo.ReasonNotSuitableSDO;
import uk.gov.hmcts.reform.civil.model.sdo.SdoR2FastTrackCreditHire;
import uk.gov.hmcts.reform.civil.model.sdo.SdoR2FastTrackCreditHireDetails;
import uk.gov.hmcts.reform.civil.model.sdo.SmallClaimsFlightDelay;
import uk.gov.hmcts.reform.civil.model.sdo.SmallClaimsWitnessStatement;
import uk.gov.hmcts.reform.civil.model.sdo.TrialHearingTimeDJ;
import uk.gov.hmcts.reform.civil.model.sdo.TrialOrderMadeWithoutHearingDJ;
import uk.gov.hmcts.reform.civil.model.transferonlinecase.NotSuitableSdoOptions;
import uk.gov.hmcts.reform.civil.model.transferonlinecase.TocTransferCaseReason;
import uk.gov.hmcts.reform.civil.referencedata.model.LocationRefData;
import uk.gov.hmcts.reform.civil.service.flowstate.FlowState;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

import static java.time.LocalDate.now;
import static uk.gov.hmcts.reform.civil.enums.AllocatedTrack.FAST_CLAIM;
import static uk.gov.hmcts.reform.civil.enums.AllocatedTrack.INTERMEDIATE_CLAIM;
import static uk.gov.hmcts.reform.civil.enums.AllocatedTrack.MULTI_CLAIM;
import static uk.gov.hmcts.reform.civil.enums.AllocatedTrack.SMALL_CLAIM;
import static uk.gov.hmcts.reform.civil.enums.CaseCategory.SPEC_CLAIM;
import static uk.gov.hmcts.reform.civil.enums.CaseCategory.UNSPEC_CLAIM;
import static uk.gov.hmcts.reform.civil.enums.CaseState.AWAITING_APPLICANT_INTENTION;
import static uk.gov.hmcts.reform.civil.enums.CaseState.AWAITING_CASE_DETAILS_NOTIFICATION;
import static uk.gov.hmcts.reform.civil.enums.CaseState.AWAITING_RESPONDENT_ACKNOWLEDGEMENT;
import static uk.gov.hmcts.reform.civil.enums.CaseState.All_FINAL_ORDERS_ISSUED;
import static uk.gov.hmcts.reform.civil.enums.CaseState.CASE_DISMISSED;
import static uk.gov.hmcts.reform.civil.enums.CaseState.CASE_ISSUED;
import static uk.gov.hmcts.reform.civil.enums.CaseState.CASE_PROGRESSION;
import static uk.gov.hmcts.reform.civil.enums.CaseState.HEARING_READINESS;
import static uk.gov.hmcts.reform.civil.enums.CaseState.JUDICIAL_REFERRAL;
import static uk.gov.hmcts.reform.civil.enums.CaseState.PENDING_CASE_ISSUED;
import static uk.gov.hmcts.reform.civil.enums.CaseState.PREPARE_FOR_HEARING_CONDUCT_HEARING;
import static uk.gov.hmcts.reform.civil.enums.CaseState.PROCEEDS_IN_HERITAGE_SYSTEM;
import static uk.gov.hmcts.reform.civil.enums.ComplexityBand.BAND_1;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.ONE_V_ONE;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.ONE_V_TWO_ONE_LEGAL_REP;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.ONE_V_TWO_TWO_LEGAL_REP;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.TWO_V_ONE;
import static uk.gov.hmcts.reform.civil.enums.PaymentStatus.FAILED;
import static uk.gov.hmcts.reform.civil.enums.PaymentStatus.SUCCESS;
import static uk.gov.hmcts.reform.civil.enums.PersonalInjuryType.ROAD_ACCIDENT;
import static uk.gov.hmcts.reform.civil.enums.ResponseIntention.FULL_DEFENCE;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;
import static uk.gov.hmcts.reform.civil.enums.dq.GeneralApplicationTypes.STRIKE_OUT;
import static uk.gov.hmcts.reform.civil.enums.dq.HearingLength.MORE_THAN_DAY;
import static uk.gov.hmcts.reform.civil.enums.dq.HearingLength.ONE_DAY;
import static uk.gov.hmcts.reform.civil.enums.dq.UnavailableDateType.DATE_RANGE;
import static uk.gov.hmcts.reform.civil.enums.dq.UnavailableDateType.SINGLE_DATE;
import static uk.gov.hmcts.reform.civil.enums.hearing.HearingDuration.MINUTES_120;
import static uk.gov.hmcts.reform.civil.enums.sdo.DisposalHearingFinalDisposalHearingTimeEstimate.FIFTEEN_MINUTES;
import static uk.gov.hmcts.reform.civil.model.mediation.MediationDocumentsType.NON_ATTENDANCE_STATEMENT;
import static uk.gov.hmcts.reform.civil.model.mediation.MediationDocumentsType.REFERRED_DOCUMENTS;
import static uk.gov.hmcts.reform.civil.service.directionsorder.DirectionsOrderSpecialistTextLibrary.DISPOSAL_FINAL_HEARING_LISTING_DJ;
import static uk.gov.hmcts.reform.civil.service.directionsorder.DirectionsOrderSpecialistTextLibrary.FAST_TRACK_TRIAL_BUNDLE_NOTICE;
import static uk.gov.hmcts.reform.civil.service.directionsorder.DirectionsOrderSpecialistTextLibrary.FLIGHT_DELAY_LEGAL_ARGUMENTS_NOTICE;
import static uk.gov.hmcts.reform.civil.service.directionsorder.DirectionsOrderSpecialistTextLibrary.FLIGHT_DELAY_RELATED_CLAIMS_NOTICE;
import static uk.gov.hmcts.reform.civil.service.docmosis.dj.DefaultJudgmentOrderFormGenerator.DISPOSAL_HEARING;
import static uk.gov.hmcts.reform.civil.utils.ElementUtils.wrapElements;

public class CaseDataBuilder {

    final String reason = """
        Lorem ipsum dolor sit amet, consectetuer adipiscing elit.
        felis, ultricies nec, pellentesque eu, pretium quis, sem.
        venenatis vitae, justo. Nullam dictum felis eu pede molli
        consequat vitae, eleifend ac, enim. Aliquam lorem ante, d
        nisi vel augue. Curabitur ullamcorper ultricies nisi. Nam
        quam nunc, blandit vel, luctus pulvinar, hendrerit id, lo
        faucibus tincidunt. Duis leo. Sed fringilla mauris sit am
        vulputate eleifend sapien. Vestibulum purus quam, sceleri
        primis in faucibus orci luctus et ultrices posuere cubili
        ipsum. Sed aliquam ultrices mauris. Integer ante arcu, ac
        volutpat pretium libero. Cras id dui. Aenean ut eros et n
        Phasellus nec sem in justo pellentesque facilisis. Etiam\s
        non, euismod vitae, posuere imperdiet, leo. Maecenas male
        ante ipsum primis in faucibus orci luctus et ultrices pos
        vestibulum elit. Aenean tellus metus, bibendum sed, posue
        cursus feugiat, nunc augue blandit nunc, eu sollicitudin\s
        venenatis condimentum, sem libero volutpat nibh, nec pell
        tincidunt libero. Phasellus dolor. Maecenas vestibulum mo
        posuere eget, vestibulum et, tempor auctor, justo. In ac\s
        rhoncus pede. Pellentesque habitant morbi tristique senec
        In hac habitasse platea dictumst. Curabitur at lacus ac v
        sem. Pellentesque libero tortor, tincidunt et, tincidunt\s
        leo quis pede. Donec interdum, metus et hendrerit aliquet
        venenatis vulputate lorem. Morbi nec metus. Phasellus bla
        fermentum eu, tincidunt eu, varius ut, felis. In auctor l
        Nullam cursus lacinia erat. Praesent blandit laoreet nibh
        orci leo non est. Quisque id mi. Ut tincidunt tincidunt e
        habitasse platea dictumst. Fusce a quam. Etiam ut purus m
        Sed augue ipsum, egestas nec, vestibulum et, malesuada ad
        fermentum libero. Praesent nonummy mi in odio. Nunc inter
        Lorem ipsum dolor sit amet, consectetuer adipiscing elit.
        felis, ultricies nec, pellentesque eu, pretium quis, sem.
        venenatis vitae, justo. Nullam dictum felis eu pede molli
        consequat vitae, eleifend ac, enim. Aliquam lorem ante, d
        nisi vel augue. Curabitur ullamcorper ultricies nisi. Nam
        quam nunc, blandit vel, luctus pulvinar, hendrerit id, lo
        faucibus tincidunt. Duis leo. Sed fringilla mauris sit am
        vulputate eleifend sapien. Vestibulum purus quam, sceleri
        primis in faucibus orci luctus et ultrices posuere cubili
        ipsum. Sed aliquam ultrices mauris. Integer ante arcu, ac
        volutpat pretium libero. Cras id dui. Aenean ut eros et n
        Phasellus nec sem in justo pellentesque facilisis. Etiam\s
        non, euismod vitae, posuere imperdiet, leo. Maecenas male
        ante ipsum primis in faucibus orci luctus et ultrices pos
        vestibulum elit. Aenean tellus metus, bibendum sed, posue
        cursus feugiat, nunc augue blandit nunc, eu sollicitudin\s
        venenatis condimentum, sem libero volutpat nibh, nec pell
        tincidunt libero. Phasellus dolor. Maecenas vestibulum mo
        posuere eget, vestibulum et, tempor auctor, justo. In ac\s
        rhoncus pede. Pellentesque habitant morbi tristique senec
        In hac habitasse platea dictumst. Curabitur at lacus ac v
        sem. Pellentesque libero tortor, tincidunt et, tincidunt\s
        leo quis pede. Donec interdum, metus et hendrerit aliquet
        venenatis vulputate lorem. Morbi nec metus. Phasellus bla
        fermentum eu, tincidunt eu, varius ut, felis. In auctor l
        Nullam cursus lacinia erat. Praesent blandit laoreet nibh
        orci leo non est. Quisque id mi. Ut tincidunt tincidunt e
        habitasse platea dictumst. Fusce a quam. Etiam ut purus m
        Sed augue ipsum, egestas nec, vestibulum et, malesuada ad
        fermentum libero. Praesent nonummy mi in odio. Nunc inter
        fermentum eu, tincidunt eu, varius ut, felis. In auctor l
        Nullam cursus lacinia erat. Praesent blandit laoreet nibh
        orci leo non est. Quisque id mi. Ut tincidunt tincidunt e
        habitasse platea dictumst. Fusce a quam. Etiam ut purus m
        Sed augue ipsum, egestas nec, vestibulum et, malesuada ad
        fermentum libero. Praesent nonummy mi in odio. Nunc inter
        fermentum libero. Praesent nonummy mi in odio. Nunc inter
        Sed augue ipsum, egestas nec, vestibulum et, malesuada ad
        fermentum libero. Praesent nonummy mi in odio. Nunc inter
        fermentum libero. Praesent nonummy mi in odio. Nunc inter
        fermentum libero.Praesent nonummy mi in odio au.
        """;

    public static final String LEGACY_CASE_REFERENCE = "000DC001";
    public static final Long CASE_ID = 1594901956117591L;
    public static final LocalDateTime SUBMITTED_DATE_TIME = LocalDateTime.now();
    public static final LocalDateTime RESPONSE_DEADLINE = SUBMITTED_DATE_TIME.toLocalDate().plusDays(14)
        .atTime(23, 59, 59);
    public static final LocalDateTime APPLICANT_RESPONSE_DEADLINE = SUBMITTED_DATE_TIME.plusDays(120);
    public static final LocalDate CLAIM_ISSUED_DATE = now();
    public static final LocalDateTime DEADLINE = LocalDate.now().atStartOfDay().plusDays(14);
    public static final LocalDate PAST_DATE = now().minusDays(1);
    public static final LocalDateTime NOTIFICATION_DEADLINE = LocalDate.now().atStartOfDay().plusDays(14);
    public static final BigDecimal FAST_TRACK_CLAIM_AMOUNT = BigDecimal.valueOf(10001);
    public static final LocalDate FUTURE_DATE = LocalDate.now().plusYears(1);
    public static final String CUSTOMER_REFERENCE = "12345";

    // Create Claim
    protected String caseNameHmctsInternal;
    protected Long ccdCaseReference;
    protected SolicitorReferences solicitorReferences;
    protected String respondentSolicitor2Reference;
    protected CourtLocation courtLocation;
    protected LocationRefData locationRefData;
    protected Party applicant1;
    protected Party applicant2;
    protected YesOrNo applicant1Represented;
    protected YesOrNo applicant1LitigationFriendRequired;
    protected YesOrNo applicant1AcceptFullAdmitPaymentPlanSpec;
    protected YesOrNo applicant2LitigationFriendRequired;
    protected Party respondent1;
    protected Party respondent2;
    protected YesOrNo respondent1Represented;
    protected YesOrNo respondent2Represented;
    protected IdamUserDetails defendantUserDetails;
    protected YesOrNo defendant1LIPAtClaimIssued;
    protected YesOrNo defendant2LIPAtClaimIssued;
    protected String respondentSolicitor1EmailAddress;
    protected String respondentSolicitor2EmailAddress;
    protected ClaimValue claimValue;
    protected YesOrNo uploadParticularsOfClaim;
    protected ClaimType claimType;
    protected ClaimTypeUnspec claimTypeUnSpec;
    protected String claimTypeOther;
    protected PersonalInjuryType personalInjuryType;
    protected String personalInjuryTypeOther;
    protected DynamicList applicantSolicitor1PbaAccounts;
    protected Fee claimFee;
    protected Fee hearingFee;
    protected StatementOfTruth applicantSolicitor1ClaimStatementOfTruth;
    protected StatementOfTruth uiStatementOfTruth;
    protected String paymentReference;
    protected String legacyCaseReference;
    protected AllocatedTrack allocatedTrack;
    protected String responseClaimTrack;
    protected CaseState ccdState;
    protected List<Element<CaseDocument>> systemGeneratedCaseDocuments;
    protected List<Element<CaseDocument>> gaDraftDocument;
    protected PaymentDetails claimIssuedPaymentDetails;
    protected PaymentDetails paymentDetails;
    protected PaymentDetails hearingFeePaymentDetails;
    protected CorrectEmail applicantSolicitor1CheckEmail;
    protected IdamUserDetails applicantSolicitor1UserDetails;
    //Deadline extension
    protected LocalDate respondentSolicitor1AgreedDeadlineExtension;
    protected LocalDate respondentSolicitor2AgreedDeadlineExtension;
    //Acknowledge Claim
    protected ResponseIntention respondent1ClaimResponseIntentionType;
    protected ResponseIntention respondent2ClaimResponseIntentionType;
    // Defendant Response Defendant 1
    protected RespondentResponseType respondent1ClaimResponseType;
    protected ResponseDocument respondent1ClaimResponseDocument;
    protected ResponseDocument respondentSharedClaimResponseDocument;
    protected Respondent1DQ respondent1DQ;
    protected Respondent2DQ respondent2DQ;
    protected Applicant1DQ applicant1DQ;
    protected Applicant2DQ applicant2DQ;
    // Defendant Response Defendant 2
    protected RespondentResponseType respondent2ClaimResponseType;
    protected ResponseDocument respondent2ClaimResponseDocument;
    protected YesOrNo respondentResponseIsSame;
    protected DynamicList defendantDetails;
    // Defendant Response 2 Applicants
    protected RespondentResponseType respondent1ClaimResponseTypeToApplicant2;
    protected RespondentResponseTypeSpec claimant1ClaimResponseTypeForSpec;
    protected RespondentResponseTypeSpec claimant2ClaimResponseTypeForSpec;
    // Claimant Response
    protected YesOrNo applicant1ProceedWithClaim;
    protected YesOrNo applicant1ProceedWithClaimSpec2v1;
    protected YesOrNo applicant2ProceedWithClaimMultiParty2v1;
    protected YesOrNo applicant1ProceedWithClaimMultiParty2v1;
    protected YesOrNo applicant1ProceedWithClaimAgainstRespondent1MultiParty1v2;
    protected YesOrNo applicant1ProceedWithClaimAgainstRespondent2MultiParty1v2;
    protected ResponseDocument applicant1DefenceResponseDocument;
    protected ResponseDocument applicant2DefenceResponseDocument;
    protected BusinessProcess businessProcess;
    protected PaymentType applicant1RepaymentOptionForDefendantSpec;
    protected PaymentBySetDate applicant1RequestedPaymentDateForDefendantSpec;
    protected BigDecimal applicant1SuggestInstalmentsPaymentAmountForDefendantSpec;
    protected PaymentFrequencyClaimantResponseLRspec applicant1SuggestInstalmentsRepaymentFrequencyForDefendantSpec;
    protected LocalDate applicant1SuggestInstalmentsFirstRepaymentDateForDefendantSpec;

    //Case proceeds in caseman
    protected ClaimProceedsInCaseman claimProceedsInCaseman;
    protected ClaimProceedsInCasemanLR claimProceedsInCasemanLR;

    protected CloseClaim withdrawClaim;
    protected CloseClaim discontinueClaim;
    protected YesOrNo respondent1OrgRegistered;
    protected YesOrNo respondent2OrgRegistered;
    protected OrganisationPolicy applicant1OrganisationPolicy;
    protected OrganisationPolicy respondent1OrganisationPolicy;
    protected OrganisationPolicy respondent2OrganisationPolicy;
    protected YesOrNo addApplicant2;
    protected YesOrNo addRespondent2;
    protected CaseCategory caseAccessCategory;

    protected YesOrNo specRespondent1Represented;
    protected YesOrNo specRespondent2Represented;

    protected YesOrNo respondent2SameLegalRepresentative;
    protected LitigationFriend respondent1LitigationFriend;
    protected LitigationFriend respondent2LitigationFriend;
    protected LitigationFriend applicant1LitigationFriend;
    protected LitigationFriend applicant2LitigationFriend;
    protected LitigationFriend genericLitigationFriend;
    protected BreathingSpaceInfo breathing;
    protected BreathingSpaceEnterInfo enter;
    protected BreathingSpaceLiftInfo lift;

    protected List<Element<CaseNote>> caseNotes;

    protected String notificationSummary;

    //dates
    protected LocalDateTime submittedDate;
    protected LocalDateTime paymentSuccessfulDate;
    protected LocalDate issueDate;
    protected LocalDateTime claimNotificationDeadline;
    protected LocalDateTime claimNotificationDate;
    protected LocalDateTime claimDetailsNotificationDeadline;
    protected LocalDateTime addLegalRepDeadlineDefendant1;
    protected LocalDateTime addLegalRepDeadlineDefendant2;
    protected ServedDocumentFiles servedDocumentFiles;
    protected LocalDateTime claimDetailsNotificationDate;
    protected LocalDateTime respondent1ResponseDeadline;
    protected LocalDateTime respondent2ResponseDeadline;
    protected LocalDateTime claimDismissedDeadline;
    protected LocalDateTime respondent1TimeExtensionDate;
    protected LocalDateTime respondent2TimeExtensionDate;
    protected LocalDateTime respondent1AcknowledgeNotificationDate;
    protected LocalDateTime respondent2AcknowledgeNotificationDate;
    protected LocalDateTime respondent1ResponseDate;
    protected LocalDateTime respondent2ResponseDate;
    protected LocalDateTime applicant1ResponseDeadline;
    protected LocalDateTime applicant1ResponseDate;
    protected LocalDateTime applicant2ResponseDate;
    protected LocalDateTime takenOfflineDate;
    protected LocalDateTime takenOfflineByStaffDate;
    protected LocalDateTime unsuitableSDODate;
    protected LocalDateTime claimDismissedDate;
    protected LocalDateTime caseDismissedHearingFeeDueDate;
    protected LocalDate hearingDate;
    private InterestClaimOptions interestClaimOptions;
    private YesOrNo claimInterest;
    private SameRateInterestSelection sameRateInterestSelection;
    private InterestClaimFromType interestClaimFrom;
    private InterestClaimUntilType interestClaimUntil;
    private BigDecimal totalClaimAmount;
    private LocalDate interestFromSpecificDate;
    private BigDecimal breakDownInterestTotal;
    protected LocalDateTime respondent1LitigationFriendDate;
    protected LocalDateTime respondent2LitigationFriendDate;
    protected DynamicList defendantSolicitorNotifyClaimOptions;
    protected DynamicList defendantSolicitorNotifyClaimDetailsOptions;
    protected DynamicList selectLitigationFriend;
    protected LocalDateTime respondent1LitigationFriendCreatedDate;
    protected LocalDateTime respondent2LitigationFriendCreatedDate;

    public SRPbaDetails srPbaDetails;

    protected SolicitorOrganisationDetails respondentSolicitor1OrganisationDetails;
    protected SolicitorOrganisationDetails respondentSolicitor2OrganisationDetails;
    protected Address applicantSolicitor1ServiceAddress;
    protected Address respondentSolicitor1ServiceAddress;
    protected Address respondentSolicitor2ServiceAddress;
    protected YesOrNo respondentSolicitor1ServiceAddressRequired;
    protected YesOrNo respondentSolicitor2ServiceAddressRequired;
    protected YesOrNo isRespondent1;
    protected YesOrNo isRespondent2;
    private List<IdValue<Bundle>> caseBundles;
    private RespondToClaim respondToClaim;
    private RespondentResponseTypeSpec respondent1ClaimResponseTypeForSpec;
    private YesOrNo defendantSingleResponseToBothClaimants;
    private RespondentResponseTypeSpec respondent2ClaimResponseTypeForSpec;
    private UnemployedComplexTypeLRspec respondToClaimAdmitPartUnemployedLRspec;
    private RespondToClaimAdmitPartLRspec respondToClaimAdmitPartLRspec;
    private Respondent1EmployerDetailsLRspec responseClaimAdmitPartEmployer;
    private YesOrNo respondent1MediationRequired;
    private YesOrNo respondent2MediationRequired;
    private PartnerAndDependentsLRspec respondent1PartnerAndDependent;
    private PartnerAndDependentsLRspec respondent2PartnerAndDependent;
    private ReasonNotSuitableSDO reasonNotSuitableSDO;
    private RepaymentPlanLRspec respondent1RepaymentPlan;
    private RepaymentPlanLRspec respondent2RepaymentPlan;
    private YesOrNo applicantsProceedIntention;
    private SmallClaimMedicalLRspec applicant1ClaimMediationSpecRequired;
    private SmallClaimMedicalLRspec applicantMPClaimMediationSpecRequired;
    private Mediation mediation;
    private YesOrNo specAoSApplicantCorrespondenceAddressRequired;
    private Address specAoSApplicantCorrespondenceAddressDetails;
    private YesOrNo specAoSRespondent2HomeAddressRequired;
    private YesOrNo specAoSRespondentCorrespondenceAddressRequired;
    private Address specAoSRespondentCorrespondenceAddressDetails;
    private Address specAoSRespondent2HomeAddressDetails;
    private YesOrNo respondent1DQWitnessesRequiredSpec;
    private List<Element<Witness>> respondent1DQWitnessesDetailsSpec;

    private String respondent1OrganisationIDCopy;
    private String respondent2OrganisationIDCopy;
    private String caseManagementOrderSelection;
    private LocalDateTime addLegalRepDeadline;
    private DefendantPinToPostLRspec respondent1PinToPostLRspec;
    private DisposalHearingMethodDJ trialHearingMethodDJ;
    private DynamicList hearingMethodValuesDisposalHearingDJ;
    private DynamicList hearingMethodValuesTrialHearingDJ;
    private DisposalHearingMethodDJ disposalHearingMethodDJ;
    private DynamicList trialHearingMethodInPersonDJ;
    private DisposalHearingBundleDJ disposalHearingBundleDJ;
    private DisposalHearingFinalDisposalHearingDJ disposalHearingFinalDisposalHearingDJ;
    private TrialHearingTrial trialHearingTrialDJ;
    private LocalDate hearingDueDate;
    private DisposalHearingJudgesRecitalDJ disposalHearingJudgesRecitalDJ;
    private TrialHearingJudgesRecital trialHearingJudgesRecitalDJ;
    private CaseLocationCivil caseManagementLocation;
    private DisposalHearingOrderMadeWithoutHearingDJ disposalHearingOrderMadeWithoutHearingDJ;
    private DisposalHearingFinalDisposalHearingTimeDJ disposalHearingFinalDisposalHearingTimeDJ;
    private DJPaymentTypeSelection paymentTypeSelection;
    private LocalDate paymentSetDate;
    private RepaymentFrequencyDJ repaymentFrequency;
    private LocalDate repaymentDate;
    private String repaymentSuggestion;

    private YesOrNo generalAppVaryJudgementType;
    private Document generalAppN245FormUpload;
    private GAApplicationType generalAppType;
    private GAApplicationTypeLR generalAppTypeLR;
    private GAHearingDateGAspec generalAppHearingDate;

    private ChangeOfRepresentation changeOfRepresentation;
    private ChangeOrganisationRequest changeOrganisationRequest;

    private String unassignedCaseListDisplayOrganisationReferences;
    private String caseListDisplayDefendantSolicitorReferences;
    private CertificateOfService cosNotifyClaimDefendant1;
    private CertificateOfService cosNotifyClaimDefendant2;
    private CertificateOfService cosNotifyClaimDetails1;
    private CertificateOfService cosNotifyClaimDetails2;

    private FastTrackHearingTime fastTrackHearingTime;
    private List<DateToShowToggle> fastTrackTrialDateToToggle;
    private FastTrackOrderWithoutJudgement fastTrackOrderWithoutJudgement;

    private DisposalOrderWithoutHearing disposalOrderWithoutHearing;
    private DisposalHearingHearingTime disposalHearingHearingTime;

    private TrialHearingTimeDJ trialHearingTimeDJ;
    private TrialOrderMadeWithoutHearingDJ trialOrderMadeWithoutHearingDJ;
    private List<Element<GeneralApplication>> generalApplications;
    private List<Element<GeneralApplicationsDetails>> generalApplicationsDetails;

    private BigDecimal totalInterest;
    private YesOrNo applicant1AcceptAdmitAmountPaidSpec;

    private YesOrNo applicant1AcceptPartAdmitPaymentPlanSpec;

    private BigDecimal respondToAdmittedClaimOwingAmountPounds;
    //Trial Readiness
    private HearingDuration hearingDuration;
    private YesOrNo trialReadyApplicant;
    private YesOrNo trialReadyRespondent1;
    private YesOrNo trialReadyRespondent2;

    private RevisedHearingRequirements applicantRevisedHearingRequirements;
    private RevisedHearingRequirements respondent1RevisedHearingRequirements;
    private RevisedHearingRequirements respondent2RevisedHearingRequirements;

    private YesOrNo applicant1PartAdmitIntentionToSettleClaimSpec;
    private YesOrNo applicant1PartAdmitConfirmAmountPaidSpec;

    private CCJPaymentDetails ccjPaymentDetails;
    private DisposalHearingMethod disposalHearingMethod;
    private DynamicList hearingMethodValuesDisposalHearing;
    private DynamicList hearingMethodValuesFastTrack;
    private DynamicList hearingMethodValuesSmallClaims;

    private List<Element<PartyFlagStructure>> applicantExperts;
    private List<Element<PartyFlagStructure>> applicantWitnesses;
    private List<Element<PartyFlagStructure>> respondent1Experts;
    private List<Element<PartyFlagStructure>> respondent1Witnesses;
    private List<Element<PartyFlagStructure>> respondent2Experts;
    private List<Element<PartyFlagStructure>> respondent2Witnesses;
    private CaseDataLiP caseDataLiP;
    private YesOrNo claimant2ResponseFlag;
    private TimelineUploadTypeSpec specClaimResponseTimelineList;
    private TimelineUploadTypeSpec specClaimResponseTimelineList2;
    private YesOrNo defenceAdmitPartEmploymentTypeRequired;
    private YesOrNo specDefenceFullAdmitted2Required;
    private RespondentResponsePartAdmissionPaymentTimeLRspec defenceAdmitPartPaymentTimeRouteRequired;
    private ResponseOneVOneShowTag showResponseOneVOneFlag;
    private SmallClaimsWitnessStatement smallClaimsWitnessStatement;
    private SmallClaimsFlightDelay smallClaimsFlightDelay;
    private FastTrackWitnessOfFact fastTrackWitnessOfFact;
    private TrialHearingWitnessOfFact trialHearingWitnessOfFactDJ;

    private HearingSupportRequirementsDJ hearingSupportRequirementsDJ;
    private List<Element<CaseDocument>> defaultJudgmentDocuments = new ArrayList<>();
    private IdamUserDetails claimantUserDetails;

    private UpdateDetailsForm updateDetailsForm;

    private TocTransferCaseReason tocTransferCaseReason;

    private NotSuitableSdoOptions notSuitableSdoOptions;

    private List<Element<PartyFlagStructure>> applicant1LRIndividuals;
    private List<Element<PartyFlagStructure>> respondent1LRIndividuals;
    private List<Element<PartyFlagStructure>> respondent2LRIndividuals;

    private List<Element<PartyFlagStructure>> applicant1OrgIndividuals;
    private List<Element<PartyFlagStructure>> applicant2OrgIndividuals;
    private List<Element<PartyFlagStructure>> respondent1OrgIndividuals;
    private List<Element<PartyFlagStructure>> respondent2OrgIndividuals;

    protected String hearingReference;
    protected ListingOrRelisting listingOrRelisting;

    private YesOrNo drawDirectionsOrderRequired;

    private DynamicList transferCourtLocationList;
    private String reasonForTransfer;

    private YesOrNo isFlightDelayClaim;
    private FlightDelayDetails flightDelayDetails;
    private ReasonForReconsideration reasonForReconsiderationApplicant;
    private ReasonForReconsideration reasonForReconsiderationRespondent1;
    private ReasonForReconsideration reasonForReconsiderationRespondent2;
    private LocalDateTime respondent1RespondToSettlementAgreementDeadline;

    private UploadMediationDocumentsForm uploadDocumentsForm;

    private YesOrNo responseClaimExpertSpecRequired;
    private YesOrNo responseClaimExpertSpecRequired2;
    private YesOrNo applicantMPClaimExpertSpecRequired;
    private YesOrNo applicant1ClaimExpertSpecRequired;
    private DecisionOnRequestReconsiderationOptions decisionOnRequestReconsiderationOptions;
    private UpholdingPreviousOrderReason upholdingPreviousOrderReason;

    private HelpWithFeesMoreInformation helpWithFeesMoreInformationClaimIssue;
    private HelpWithFeesMoreInformation helpWithFeesMoreInformationHearing;

    private FeePaymentOutcomeDetails feePaymentOutcomeDetails;

    private List<Element<MediationNonAttendanceStatement>> res1MediationNonAttendanceDocs;
    private List<Element<MediationDocumentsReferredInStatement>> res1MediationDocumentsReferred;

    private FeeType hwfFeeType;
    private HelpWithFeesDetails claimIssuedHwfDetails;
    private HelpWithFeesDetails hearingHwfDetails;

    private YesOrNo eaCourtLocation;

    private Flags caseFlags;

    private MediationContactInformation app1MediationContactInfo;
    private MediationAvailability app1MediationAvailability;
    private MediationContactInformation resp1MediationContactInfo;
    private MediationContactInformation resp2MediationContactInfo;
    private MediationAvailability resp1MediationAvailability;
    private MediationAvailability resp2MediationAvailability;

    private SdoR2FastTrackCreditHire sdoR2FastTrackCreditHire;
    private SdoR2FastTrackCreditHireDetails sdoR2FastTrackCreditHireDetails;
    private String claimantBilingualLanguagePreference;
    private JudgmentPaidInFull judgmentPaidInFull;
    private YesOrNo anyRepresented;
    private FixedCosts fixedCosts;

    private String partialPaymentAmount;
    private LocalDate nextDeadline;

    private CaseQueriesCollection queries;

    public CaseDataBuilder claimantBilingualLanguagePreference(String claimantBilingualLanguagePreference) {
        this.claimantBilingualLanguagePreference = claimantBilingualLanguagePreference;
        return this;
    }

    public CaseDataBuilder queries(CaseQueriesCollection queries) {
        this.queries = queries;
        return this;
    }

    public CaseDataBuilder fixedCosts(FixedCosts fixedCosts) {
        this.fixedCosts = fixedCosts;
        return this;
    }

    public CaseDataBuilder helpWithFeesMoreInformationClaimIssue(HelpWithFeesMoreInformation helpWithFeesMoreInformationClaimIssue) {
        this.helpWithFeesMoreInformationClaimIssue = helpWithFeesMoreInformationClaimIssue;
        return this;
    }

    public CaseDataBuilder helpWithFeesMoreInformationHearing(HelpWithFeesMoreInformation helpWithFeesMoreInformationHearing) {
        this.helpWithFeesMoreInformationHearing = helpWithFeesMoreInformationHearing;
        return this;
    }

    public CaseDataBuilder applicant1AcceptFullAdmitPaymentPlanSpec(YesOrNo applicant1AcceptFullAdmitPaymentPlanSpec) {
        this.applicant1AcceptFullAdmitPaymentPlanSpec = applicant1AcceptFullAdmitPaymentPlanSpec;
        return this;
    }

    public CaseDataBuilder sameRateInterestSelection(SameRateInterestSelection sameRateInterestSelection) {
        this.sameRateInterestSelection = sameRateInterestSelection;
        return this;
    }

    public CaseDataBuilder responseClaimExpertSpecRequired(YesOrNo responseClaimExpertSpecRequired) {
        this.responseClaimExpertSpecRequired = responseClaimExpertSpecRequired;
        return this;
    }

    public CaseDataBuilder responseClaimExpertSpecRequired2(YesOrNo responseClaimExpertSpecRequired2) {
        this.responseClaimExpertSpecRequired2 = responseClaimExpertSpecRequired2;
        return this;
    }

    public CaseDataBuilder applicant1ClaimExpertSpecRequired(YesOrNo applicant1ClaimExpertSpecRequired) {
        this.applicant1ClaimExpertSpecRequired = applicant1ClaimExpertSpecRequired;
        return this;
    }

    public CaseDataBuilder applicantMPClaimExpertSpecRequired(YesOrNo applicantMPClaimExpertSpecRequired) {
        this.applicantMPClaimExpertSpecRequired = applicantMPClaimExpertSpecRequired;
        return this;
    }

    public CaseDataBuilder generalAppVaryJudgementType(YesOrNo generalAppVaryJudgementType) {
        this.generalAppVaryJudgementType = generalAppVaryJudgementType;
        return this;
    }

    public CaseDataBuilder generalAppType(GAApplicationType generalAppType) {
        this.generalAppType = generalAppType;
        return this;
    }

    public CaseDataBuilder generalAppTypeLR(GAApplicationTypeLR generalAppTypeLR) {
        this.generalAppTypeLR = generalAppTypeLR;
        return this;
    }

    public CaseDataBuilder generalAppHearingDate(GAHearingDateGAspec generalAppHearingDate) {
        this.generalAppHearingDate = generalAppHearingDate;
        return this;
    }

    public CaseDataBuilder generalAppN245FormUpload(Document generalAppN245FormUpload) {
        this.generalAppN245FormUpload = generalAppN245FormUpload;
        return this;
    }

    public CaseDataBuilder breakDownInterestTotal(BigDecimal breakDownInterestTotal) {
        this.breakDownInterestTotal = breakDownInterestTotal;
        return this;
    }

    public CaseDataBuilder interestFromSpecificDate(LocalDate interestFromSpecificDate) {
        this.interestFromSpecificDate = interestFromSpecificDate;
        return this;
    }

    public CaseDataBuilder totalClaimAmount(BigDecimal totalClaimAmount) {
        this.totalClaimAmount = totalClaimAmount;
        return this;
    }

    public CaseDataBuilder partialPaymentAmount(String partialPaymentAmount) {
        this.partialPaymentAmount = partialPaymentAmount;
        return this;
    }

    public CaseDataBuilder interestClaimOptions(InterestClaimOptions interestClaimOptions) {
        this.interestClaimOptions = interestClaimOptions;
        return this;
    }

    public CaseDataBuilder interestClaimFrom(InterestClaimFromType interestClaimFrom) {
        this.interestClaimFrom = interestClaimFrom;
        return this;
    }

    public CaseDataBuilder interestClaimUntil(InterestClaimUntilType interestClaimUntil) {
        this.interestClaimUntil = interestClaimUntil;
        return this;
    }

    public CaseDataBuilder claimInterest(YesOrNo claimInterest) {
        this.claimInterest = claimInterest;
        return this;
    }

    //workaround fields
    protected Party respondent1Copy;
    protected Party respondent2Copy;

    public CaseDataBuilder respondent1ResponseDeadline(LocalDateTime deadline) {
        this.respondent1ResponseDeadline = deadline;
        return this;
    }

    public CaseDataBuilder respondent2ResponseDeadline(LocalDateTime deadline) {
        this.respondent2ResponseDeadline = deadline;
        return this;
    }

    public CaseDataBuilder respondent1AcknowledgeNotificationDate(LocalDateTime dateTime) {
        this.respondent1AcknowledgeNotificationDate = dateTime;
        return this;
    }

    public CaseDataBuilder respondent2AcknowledgeNotificationDate(LocalDateTime dateTime) {
        this.respondent2AcknowledgeNotificationDate = dateTime;
        return this;
    }

    public CaseDataBuilder applicantSolicitor1ServiceAddress(Address applicantSolicitor1ServiceAddress) {
        this.applicantSolicitor1ServiceAddress = applicantSolicitor1ServiceAddress;
        return this;
    }

    public CaseDataBuilder respondentSolicitor1ServiceAddress(Address respondentSolicitor1ServiceAddress) {
        this.respondentSolicitor1ServiceAddress = respondentSolicitor1ServiceAddress;
        return this;
    }

    public CaseDataBuilder respondentSolicitor2ServiceAddress(Address respondentSolicitor2ServiceAddress) {
        this.respondentSolicitor2ServiceAddress = respondentSolicitor2ServiceAddress;
        return this;
    }

    public CaseDataBuilder respondentSolicitor1ServiceAddressRequired(YesOrNo respondentSolicitor1ServiceAddressRequired) {
        this.respondentSolicitor1ServiceAddressRequired = respondentSolicitor1ServiceAddressRequired;
        return this;
    }

    public CaseDataBuilder respondentSolicitor2ServiceAddressRequired(YesOrNo respondentSolicitor2ServiceAddressRequired) {
        this.respondentSolicitor2ServiceAddressRequired = respondentSolicitor2ServiceAddressRequired;
        return this;
    }

    public CaseDataBuilder respondentSolicitor1EmailAddress(String respondentSolicitor1EmailAddress) {
        this.respondentSolicitor1EmailAddress = respondentSolicitor1EmailAddress;
        return this;
    }

    public CaseDataBuilder respondentSolicitor2EmailAddress(String respondentSolicitor2EmailAddress) {
        this.respondentSolicitor2EmailAddress = respondentSolicitor2EmailAddress;
        return this;
    }

    public CaseDataBuilder isRespondent1(YesOrNo isRespondent1) {
        this.isRespondent1 = isRespondent1;
        return this;
    }

    public CaseDataBuilder isRespondent2(YesOrNo isRespondent2) {
        this.isRespondent2 = isRespondent2;
        return this;
    }

    public CaseDataBuilder respondentSolicitor1AgreedDeadlineExtension(LocalDate extensionDate) {
        this.respondentSolicitor1AgreedDeadlineExtension = extensionDate;
        return this;
    }

    public CaseDataBuilder respondentSolicitor2AgreedDeadlineExtension(LocalDate extensionDate) {
        this.respondentSolicitor2AgreedDeadlineExtension = extensionDate;
        return this;
    }

    public CaseDataBuilder respondent1TimeExtensionDate(LocalDateTime extensionDate) {
        this.respondent1TimeExtensionDate = extensionDate;
        return this;
    }

    public CaseDataBuilder respondent2TimeExtensionDate(LocalDateTime extensionDate) {
        this.respondent2TimeExtensionDate = extensionDate;
        return this;
    }

    public CaseDataBuilder respondent2(Party party) {
        this.respondent2 = party;
        return this;
    }

    public CaseDataBuilder caseNotes(CaseNote caseNote) {
        this.caseNotes = wrapElements(caseNote);
        return this;
    }

    public CaseDataBuilder notificationSummary(String notificationSummary) {
        this.notificationSummary = notificationSummary;
        return this;
    }

    public CaseDataBuilder respondent1OrganisationIDCopy(String id) {
        this.respondent1OrganisationIDCopy = id;
        return this;
    }

    public CaseDataBuilder respondent2OrganisationIDCopy(String id) {
        this.respondent2OrganisationIDCopy = id;
        return this;
    }

    public CaseDataBuilder cosNotifyClaimDefendant1(CertificateOfService cosNotifyClaimDefendant) {
        this.cosNotifyClaimDefendant1 = cosNotifyClaimDefendant;
        return this;
    }

    public CaseDataBuilder cosNotifyClaimDefendant2(CertificateOfService cosNotifyClaimDefendant) {
        this.cosNotifyClaimDefendant2 = cosNotifyClaimDefendant;
        return this;
    }

    public CaseDataBuilder respondent1DQWithFixedRecoverableCosts() {
        respondent1DQ = respondent1DQ.copy()
            .setRespondent1DQFixedRecoverableCosts(new FixedRecoverableCosts()
                .setIsSubjectToFixedRecoverableCostRegime(YES)
                .setBand(BAND_1)
                .setComplexityBandingAgreed(YES)
                .setReasons("Good reason"));
        return this;
    }

    public CaseDataBuilder respondent1DQWithFixedRecoverableCostsIntermediate(Document document) {
        respondent1DQ = respondent1DQ.copy()
            .setRespondent1DQFixedRecoverableCostsIntermediate(new FixedRecoverableCosts()
                .setIsSubjectToFixedRecoverableCostRegime(YES)
                .setBand(BAND_1)
                .setComplexityBandingAgreed(YES)
                .setReasons("Good reason")
                .setFrcSupportingDocument(document));
        return this;
    }

    public CaseDataBuilder respondent1DQWithFixedRecoverableCostsIntermediate() {
        respondent1DQ = respondent1DQ.copy()
            .setRespondent1DQFixedRecoverableCostsIntermediate(new FixedRecoverableCosts()
                .setIsSubjectToFixedRecoverableCostRegime(YES)
                .setBand(BAND_1)
                .setComplexityBandingAgreed(YES)
                .setReasons("Good reason")
                .setFrcSupportingDocument(
                    DocumentBuilder.builder()
                        .setDocumentName("frc-doc1")
                        .build()));
        return this;
    }

    public CaseDataBuilder respondent1DQ() {
        respondent1DQ = new Respondent1DQ()
            .setRespondent1DQFileDirectionsQuestionnaire(new FileDirectionsQuestionnaire()
                .setExplainedToClient(List.of("CONFIRM"))
                .setOneMonthStayRequested(YES)
                .setReactionProtocolCompliedWith(YES))
            .setRespondent1DQDisclosureOfElectronicDocuments(new DisclosureOfElectronicDocuments()
                .setReachedAgreement(YES))
            .setRespondent1DQDisclosureOfNonElectronicDocuments(new DisclosureOfNonElectronicDocuments()
                .setDirectionsForDisclosureProposed(NO))
            .setRespondent1DQExperts(new Experts().setExpertRequired(NO))
            .setRespondent1DQWitnesses(new Witnesses().setWitnessesToAppear(NO))
            .setRespondent1DQHearing(new Hearing()
                .setHearingLength(ONE_DAY)
                .setUnavailableDatesRequired(NO))
            .setRespondent1DQHearingSupport(new HearingSupport()
                .setSupportRequirements(YES)
                .setSupportRequirementsAdditional("Additional support needed")
                .setRequirements(List.of()))
            .setRespondent1DQFurtherInformation(new FurtherInformation().setFutureApplications(NO))
            .setRespondent1DQLanguage(new WelshLanguageRequirements())
            .setRespondent1DQVulnerabilityQuestions(new VulnerabilityQuestions()
                .setVulnerabilityAdjustmentsRequired(NO))
            .setRespondent1DQStatementOfTruth(new StatementOfTruth().setName("John Doe").setRole("Solicitor"))
            .setRespondent1DQDraftDirections(DocumentBuilder.builder().setDocumentName("defendant1-directions.pdf").build());
        return this;
    }

    public CaseDataBuilder respondent1DQ(Respondent1DQ respondent1DQ) {
        this.respondent1DQ = respondent1DQ;
        return this;
    }

    public CaseDataBuilder respondent1DQWithoutSotAndExperts() {
        respondent1DQ = new Respondent1DQ()
            .setRespondent1DQFileDirectionsQuestionnaire(new FileDirectionsQuestionnaire()
                .setExplainedToClient(List.of("CONFIRM"))
                .setOneMonthStayRequested(YES)
                .setReactionProtocolCompliedWith(YES))
            .setRespondent1DQDisclosureOfElectronicDocuments(new DisclosureOfElectronicDocuments()
                .setReachedAgreement(YES))
            .setRespondent1DQDisclosureOfNonElectronicDocuments(new DisclosureOfNonElectronicDocuments()
                .setDirectionsForDisclosureProposed(NO))
            .setRespondent1DQWitnesses(new Witnesses().setWitnessesToAppear(NO))
            .setRespondent1DQHearing(new Hearing()
                .setHearingLength(ONE_DAY)
                .setUnavailableDatesRequired(NO))
            .setRespondent1DQHearingSupport(new HearingSupport().setRequirements(List.of()))
            .setRespondent1DQFurtherInformation(new FurtherInformation().setFutureApplications(NO))
            .setRespondent1DQLanguage(new WelshLanguageRequirements())
            .setRespondent1DQVulnerabilityQuestions(new VulnerabilityQuestions()
                .setVulnerabilityAdjustmentsRequired(NO))
            .setRespondent1DQDraftDirections(DocumentBuilder.builder().setDocumentName("defendant1-directions.pdf").build());
        return this;
    }

    public CaseDataBuilder respondent1DQWithLocation() {
        respondent1DQ = new Respondent1DQ()
            .setRespondent1DQFileDirectionsQuestionnaire(new FileDirectionsQuestionnaire()
                .setExplainedToClient(List.of("CONFIRM"))
                .setOneMonthStayRequested(YES)
                .setReactionProtocolCompliedWith(YES))
            .setRespondent1DQDisclosureOfElectronicDocuments(new DisclosureOfElectronicDocuments()
                .setReachedAgreement(YES))
            .setRespondent1DQDisclosureOfNonElectronicDocuments(new DisclosureOfNonElectronicDocuments()
                .setDirectionsForDisclosureProposed(NO))
            .setRespondent1DQExperts(new Experts().setExpertRequired(NO))
            .setRespondent1DQWitnesses(new Witnesses().setWitnessesToAppear(NO))
            .setRespondent1DQHearing(new Hearing()
                .setHearingLength(ONE_DAY)
                .setUnavailableDatesRequired(NO))
            .setRespondent1DQRequestedCourt(new RequestedCourt()
                .setResponseCourtCode("444")
                .setResponseCourtName("Court name 444")
                .setReasonForHearingAtSpecificCourt("Reason of Respondent 1 to choose court")
                .setCaseLocation(new CaseLocationCivil()
                                     .setBaseLocation("dummy base").setRegion("dummy region")))
            .setRespondent1DQHearingSupport(new HearingSupport().setRequirements(List.of()))
            .setRespondent1DQFurtherInformation(new FurtherInformation().setFutureApplications(NO))
            .setRespondent1DQLanguage(new WelshLanguageRequirements())
            .setRespondent1DQVulnerabilityQuestions(new VulnerabilityQuestions()
                .setVulnerabilityAdjustmentsRequired(NO))
            .setRespondent1DQStatementOfTruth(new StatementOfTruth().setName("John Doe").setRole("Solicitor"))
            .setRespondent1DQDraftDirections(DocumentBuilder.builder().setDocumentName("defendant1-directions.pdf").build());
        return this;
    }

    public CaseDataBuilder respondent1DQWithLocationAndWithoutExperts() {
        respondent1DQ = new Respondent1DQ()
            .setRespondent1DQFileDirectionsQuestionnaire(new FileDirectionsQuestionnaire()
                .setExplainedToClient(List.of("CONFIRM"))
                .setOneMonthStayRequested(YES)
                .setReactionProtocolCompliedWith(YES))
            .setRespondent1DQDisclosureOfElectronicDocuments(new DisclosureOfElectronicDocuments()
                .setReachedAgreement(YES))
            .setRespondent1DQDisclosureOfNonElectronicDocuments(new DisclosureOfNonElectronicDocuments()
                .setDirectionsForDisclosureProposed(NO))
            .setRespondent1DQWitnesses(new Witnesses().setWitnessesToAppear(NO))
            .setRespondent1DQHearing(new Hearing()
                .setHearingLength(ONE_DAY)
                .setUnavailableDatesRequired(NO))
            .setRespondent1DQRequestedCourt(new RequestedCourt()
                .setResponseCourtCode("444")
                .setCaseLocation(new CaseLocationCivil()
                                     .setBaseLocation("dummy base").setRegion("dummy region")))
            .setRespondent1DQHearingSupport(new HearingSupport().setRequirements(List.of()))
            .setRespondent1DQFurtherInformation(new FurtherInformation().setFutureApplications(NO))
            .setRespondent1DQLanguage(new WelshLanguageRequirements())
            .setRespondent1DQVulnerabilityQuestions(new VulnerabilityQuestions()
                .setVulnerabilityAdjustmentsRequired(NO))
            .setRespondent1DQStatementOfTruth(new StatementOfTruth().setName("John Doe").setRole("Solicitor"))
            .setRespondent1DQDraftDirections(DocumentBuilder.builder().setDocumentName("defendant1-directions.pdf").build());
        return this;
    }

    public CaseDataBuilder respondent1DQWithUnavailableDates() {
        UnavailableDate unavailableDate = UnavailableDate.builder()
            .date(LocalDate.now().plusDays(1))
            .unavailableDateType(SINGLE_DATE)
            .build();
        this.respondent1DQ = new Respondent1DQ()
            .setRespondent1DQHearing(new Hearing()
                .setHearingLength(ONE_DAY)
                .setUnavailableDatesRequired(YES)
                .setUnavailableDates(wrapElements(List.of(unavailableDate))));
        return this;
    }

    public CaseDataBuilder respondent1DQWithUnavailableDateRange() {
        UnavailableDate unavailableDate = UnavailableDate.builder()
            .fromDate(LocalDate.now().plusDays(1))
            .toDate(LocalDate.now().plusDays(2))
            .unavailableDateType(UnavailableDateType.DATE_RANGE)
            .build();
        this.respondent1DQ = new Respondent1DQ()
            .setRespondent1DQHearing(new Hearing()
                .setHearingLength(MORE_THAN_DAY)
                .setUnavailableDatesRequired(YES)
                .setUnavailableDates(wrapElements(List.of(unavailableDate))));
        return this;
    }

    public CaseDataBuilder respondent2DQWithUnavailableDateRange() {
        UnavailableDate unavailableDate = UnavailableDate.builder()
            .fromDate(LocalDate.now().plusDays(1))
            .toDate(LocalDate.now().plusDays(2))
            .unavailableDateType(UnavailableDateType.DATE_RANGE)
            .build();
        this.respondent2DQ = new Respondent2DQ()
            .setRespondent2DQHearing(new Hearing()
                .setHearingLength(MORE_THAN_DAY)
                .setUnavailableDatesRequired(YES)
                .setUnavailableDates(wrapElements(List.of(unavailableDate))));
        return this;
    }

    public CaseDataBuilder applicant1DQWithUnavailableDateRange() {
        UnavailableDate unavailableDate = UnavailableDate.builder()
            .fromDate(LocalDate.now().plusDays(1))
            .toDate(LocalDate.now().plusDays(2))
            .unavailableDateType(UnavailableDateType.DATE_RANGE)
            .build();
        this.applicant1DQ = new Applicant1DQ()
            .setApplicant1DQHearing(new Hearing()
                .setHearingLength(MORE_THAN_DAY)
                .setUnavailableDatesRequired(YES)
                .setUnavailableDates(wrapElements(List.of(unavailableDate))));
        return this;
    }

    public CaseDataBuilder applicant1DQWithUnavailableDate() {
        UnavailableDate unavailableDate = UnavailableDate.builder()
            .date(LocalDate.now().plusDays(1))
            .unavailableDateType(SINGLE_DATE)
            .build();
        this.applicant1DQ = new Applicant1DQ()
            .setApplicant1DQHearing(new Hearing()
                .setHearingLength(ONE_DAY)
                .setUnavailableDatesRequired(YES)
                .setUnavailableDates(wrapElements(List.of(unavailableDate))));
        return this;
    }

    public CaseDataBuilder respondent2DQWithLocation() {
        respondent2DQ = new Respondent2DQ()
            .setRespondent2DQFileDirectionsQuestionnaire(new FileDirectionsQuestionnaire()
                .setExplainedToClient(List.of("CONFIRM"))
                .setOneMonthStayRequested(YES)
                .setReactionProtocolCompliedWith(YES))
            .setRespondent2DQDisclosureOfElectronicDocuments(new DisclosureOfElectronicDocuments()
                .setReachedAgreement(YES))
            .setRespondent2DQDisclosureOfNonElectronicDocuments(new DisclosureOfNonElectronicDocuments()
                .setDirectionsForDisclosureProposed(NO))
            .setRespondent2DQExperts(new Experts().setExpertRequired(NO))
            .setRespondent2DQWitnesses(new Witnesses().setWitnessesToAppear(NO))
            .setRespondent2DQHearing(new Hearing()
                .setHearingLength(ONE_DAY)
                .setUnavailableDatesRequired(NO))
            .setRespondent2DQRequestedCourt(new RequestedCourt()
                .setResponseCourtCode("444")
                .setResponseCourtName("Court name 444")
                .setReasonForHearingAtSpecificCourt("Reason of Respondent 2 to choose court")
                .setCaseLocation(new CaseLocationCivil()
                                     .setBaseLocation("dummy base").setRegion("dummy region")))
            .setRespondent2DQHearingSupport(new HearingSupport().setRequirements(List.of()))
            .setRespondent2DQFurtherInformation(new FurtherInformation().setFutureApplications(NO))
            .setRespondent2DQLanguage(new WelshLanguageRequirements())
            .setRespondent2DQVulnerabilityQuestions(new VulnerabilityQuestions()
                .setVulnerabilityAdjustmentsRequired(NO))
            .setRespondent2DQStatementOfTruth(new StatementOfTruth().setName("John Doe").setRole("Solicitor"))
            .setRespondent2DQDraftDirections(DocumentBuilder.builder().setDocumentName("defendant1-directions.pdf").build());
        return this;
    }

    public CaseDataBuilder respondent2DQWithLocationAndWithoutExperts() {
        respondent2DQ = new Respondent2DQ()
            .setRespondent2DQFileDirectionsQuestionnaire(new FileDirectionsQuestionnaire()
                .setExplainedToClient(List.of("CONFIRM"))
                .setOneMonthStayRequested(YES)
                .setReactionProtocolCompliedWith(YES))
            .setRespondent2DQDisclosureOfElectronicDocuments(new DisclosureOfElectronicDocuments()
                .setReachedAgreement(YES))
            .setRespondent2DQDisclosureOfNonElectronicDocuments(new DisclosureOfNonElectronicDocuments()
                .setDirectionsForDisclosureProposed(NO))
            .setRespondent2DQWitnesses(new Witnesses().setWitnessesToAppear(NO))
            .setRespondent2DQHearing(new Hearing()
                .setHearingLength(ONE_DAY)
                .setUnavailableDatesRequired(NO))
            .setRespondent2DQRequestedCourt(new RequestedCourt()
                .setResponseCourtCode("444")
                .setCaseLocation(new CaseLocationCivil()
                                     .setBaseLocation("dummy base").setRegion("dummy region")))
            .setRespondent2DQHearingSupport(new HearingSupport().setRequirements(List.of()))
            .setRespondent2DQFurtherInformation(new FurtherInformation().setFutureApplications(NO))
            .setRespondent2DQLanguage(new WelshLanguageRequirements())
            .setRespondent2DQVulnerabilityQuestions(new VulnerabilityQuestions()
                .setVulnerabilityAdjustmentsRequired(NO))
            .setRespondent2DQStatementOfTruth(new StatementOfTruth().setName("John Doe").setRole("Solicitor"))
            .setRespondent2DQDraftDirections(DocumentBuilder.builder().setDocumentName("defendant1-directions.pdf").build());
        return this;
    }

    public CaseDataBuilder respondent2DQWithFixedRecoverableCosts() {
        respondent2DQ = respondent2DQ.copy()
            .setRespondent2DQFixedRecoverableCosts(new FixedRecoverableCosts()
                .setIsSubjectToFixedRecoverableCostRegime(YES)
                .setBand(BAND_1)
                .setComplexityBandingAgreed(YES)
                .setReasons("Good reason"));
        return this;
    }

    public CaseDataBuilder respondent2DQWithFixedRecoverableCostsIntermediate() {
        respondent2DQ = respondent2DQ.copy()
            .setRespondent2DQFixedRecoverableCostsIntermediate(new FixedRecoverableCosts()
                .setIsSubjectToFixedRecoverableCostRegime(YES)
                .setBand(BAND_1)
                .setComplexityBandingAgreed(YES)
                .setReasons("Good reason")
                .setFrcSupportingDocument(
                    DocumentBuilder.builder()
                        .setDocumentName("frc-doc1")
                        .build()));
        return this;
    }

    public CaseDataBuilder respondent2DQ() {
        respondent2DQ = new Respondent2DQ()
            .setRespondent2DQFileDirectionsQuestionnaire(new FileDirectionsQuestionnaire()
                .setExplainedToClient(List.of("CONFIRM"))
                .setOneMonthStayRequested(YES)
                .setReactionProtocolCompliedWith(YES))
            .setRespondent2DQDisclosureOfElectronicDocuments(new DisclosureOfElectronicDocuments()
                .setReachedAgreement(YES))
            .setRespondent2DQDisclosureOfNonElectronicDocuments(new DisclosureOfNonElectronicDocuments()
                .setDirectionsForDisclosureProposed(NO))
            .setRespondent2DQExperts(new Experts().setExpertRequired(NO))
            .setRespondent2DQWitnesses(new Witnesses().setWitnessesToAppear(NO))
            .setRespondent2DQHearing(new Hearing()
                .setHearingLength(ONE_DAY)
                .setUnavailableDatesRequired(NO))
            .setRespondent2DQRequestedCourt(new RequestedCourt())
            .setRespondent2DQHearingSupport(new HearingSupport().setRequirements(List.of()))
            .setRespondent2DQFurtherInformation(new FurtherInformation().setFutureApplications(NO))
            .setRespondent2DQVulnerabilityQuestions(new VulnerabilityQuestions()
                .setVulnerabilityAdjustmentsRequired(NO))
            .setRespondent2DQLanguage(new WelshLanguageRequirements())
            .setRespondent2DQStatementOfTruth(new StatementOfTruth().setName("Jane Doe").setRole("Solicitor"))
            .setRespondent2DQDraftDirections(DocumentBuilder.builder().setDocumentName("defendant2-directions.pdf").build());
        return this;
    }

    public CaseDataBuilder respondent2DQ(Respondent2DQ respondent2DQ) {
        this.respondent2DQ = respondent2DQ;
        return this;
    }

    public CaseDataBuilder respondent2DQWithoutSotAndExperts() {
        respondent2DQ = new Respondent2DQ()
            .setRespondent2DQFileDirectionsQuestionnaire(new FileDirectionsQuestionnaire()
                .setExplainedToClient(List.of("CONFIRM"))
                .setOneMonthStayRequested(YES)
                .setReactionProtocolCompliedWith(YES))
            .setRespondent2DQDisclosureOfElectronicDocuments(new DisclosureOfElectronicDocuments()
                .setReachedAgreement(YES))
            .setRespondent2DQDisclosureOfNonElectronicDocuments(new DisclosureOfNonElectronicDocuments()
                .setDirectionsForDisclosureProposed(NO))
            .setRespondent2DQWitnesses(new Witnesses().setWitnessesToAppear(NO))
            .setRespondent2DQHearing(new Hearing()
                .setHearingLength(ONE_DAY)
                .setUnavailableDatesRequired(NO))
            .setRespondent2DQRequestedCourt(new RequestedCourt())
            .setRespondent2DQHearingSupport(new HearingSupport().setRequirements(List.of()))
            .setRespondent2DQFurtherInformation(new FurtherInformation().setFutureApplications(NO))
            .setRespondent2DQVulnerabilityQuestions(new VulnerabilityQuestions()
                .setVulnerabilityAdjustmentsRequired(NO))
            .setRespondent2DQLanguage(new WelshLanguageRequirements())
            .setRespondent2DQDraftDirections(DocumentBuilder.builder().setDocumentName("defendant2-directions.pdf").build());
        return this;
    }

    public CaseDataBuilder applicant1DQ() {
        applicant1DQ = new Applicant1DQ()
            .setApplicant1DQFileDirectionsQuestionnaire(new FileDirectionsQuestionnaire()
                .setExplainedToClient(List.of("OTHER"))
                .setOneMonthStayRequested(NO)
                .setReactionProtocolCompliedWith(YES))
            .setApplicant1DQDisclosureOfElectronicDocuments(new DisclosureOfElectronicDocuments()
                .setReachedAgreement(YES))
            .setApplicant1DQDisclosureOfNonElectronicDocuments(new DisclosureOfNonElectronicDocuments()
                .setDirectionsForDisclosureProposed(NO))
            .setApplicant1DQExperts(new Experts().setExpertRequired(NO))
            .setApplicant1DQWitnesses(new Witnesses().setWitnessesToAppear(NO))
            .setApplicant1DQHearing(new Hearing()
                .setHearingLength(ONE_DAY)
                .setUnavailableDatesRequired(NO))
            .setApplicant1DQRequestedCourt(new RequestedCourt())
            .setApplicant1DQHearingSupport(new HearingSupport()
                .setSupportRequirements(YES)
                .setSupportRequirementsAdditional("Additional support needed")
                .setRequirements(List.of()))
            .setApplicant1DQFurtherInformation(new FurtherInformation().setFutureApplications(NO))
            .setApplicant1DQLanguage(new WelshLanguageRequirements())
            .setApplicant1DQVulnerabilityQuestions(new VulnerabilityQuestions()
                .setVulnerabilityAdjustmentsRequired(NO))
            .setApplicant1DQStatementOfTruth(new StatementOfTruth().setName("Bob Jones").setRole("Solicitor"));
        return this;
    }

    public CaseDataBuilder applicant1DQ(Applicant1DQ applicant1DQ) {
        this.applicant1DQ = applicant1DQ;
        return this;
    }

    public CaseDataBuilder applicant1DQWithExperts() {
        Applicant1DQ updatedApplicant1DQ = applicant1DQ != null
            ? applicant1DQ.copy() : applicant1DQ().build().getApplicant1DQ().copy();
        updatedApplicant1DQ.setApplicant1DQExperts(
            new uk.gov.hmcts.reform.civil.model.dq.Experts()
                .setExpertRequired(YES)
                .setExpertReportsSent(ExpertReportsSent.NO)
                .setJointExpertSuitable(NO)
                .setDetails(
                    wrapElements(new uk.gov.hmcts.reform.civil.model.dq.Expert()
                        .setFirstName("Expert")
                        .setLastName("One")
                        .setPhoneNumber("01482764322")
                        .setEmailAddress("fast.claim.expert1@example.com")
                        .setWhyRequired("Good reasons")
                        .setFieldOfExpertise("Some field")
                        .setEstimatedCost(BigDecimal.valueOf(10000))
                    )
                )
        );

        applicant1DQ = updatedApplicant1DQ;
        return this;
    }

    public CaseDataBuilder applicant1DQWithWitnesses() {
        Applicant1DQ updatedApplicant1DQ = applicant1DQ != null
            ? applicant1DQ.copy() : applicant1DQ().build().getApplicant1DQ().copy();
        updatedApplicant1DQ.setApplicant1DQWitnesses(
            new Witnesses()
                .setWitnessesToAppear(YES)
                .setDetails(wrapElements(
                    new Witness()
                        .setFirstName("Witness")
                        .setLastName("One")
                        .setPhoneNumber("01482764322")
                        .setEmailAddress("witness.one@example.com")
                        .setReasonForWitness("Saw something")
                )));

        applicant1DQ = updatedApplicant1DQ;
        return this;
    }

    public CaseDataBuilder applicant1DQWithHearingSupport() {
        Applicant1DQ updatedApplicant1DQ = applicant1DQ != null
            ? applicant1DQ.copy() : applicant1DQ().build().getApplicant1DQ().copy();

        updatedApplicant1DQ.setApplicant1DQHearingSupport(
            new HearingSupport()
                .setSupportRequirements(YES)
                .setSupportRequirementsAdditional("Support requirements works!!!")
        );

        applicant1DQ = updatedApplicant1DQ;
        return this;
    }

    public CaseDataBuilder respondent1DQSmallClaimExperts(ExpertDetails experts, YesOrNo expertsRequired) {
        Respondent1DQ updatedRespondent1DQ = this.respondent1DQ != null
            ? this.respondent1DQ.copy() : respondent1DQ().build().getRespondent1DQ().copy();
        ExpertDetails expertDetails = experts != null
            ? experts
            : new ExpertDetails()
                .setExpertName("Mr Expert Defendant")
                .setFirstName("Expert")
                .setLastName("Defendant")
                .setPhoneNumber("07123456789")
                .setEmailAddress("test@email.com")
                .setFieldofExpertise("Roofing")
                .setEstimatedCost(new BigDecimal(434));

        updatedRespondent1DQ.setRespondToClaimExperts(expertDetails);
        respondent1DQ = updatedRespondent1DQ;

        this.responseClaimExpertSpecRequired(expertsRequired != null ? expertsRequired : YES);
        return this;
    }

    public CaseDataBuilder respondent2DQSmallClaimExperts(ExpertDetails experts, YesOrNo expertsRequired) {
        Respondent2DQ updatedRespondent2DQ = this.respondent2DQ != null
            ? this.respondent2DQ.copy() : respondent2DQ().build().getRespondent2DQ().copy();
        ExpertDetails expertDetails = experts != null
            ? experts
            : new ExpertDetails()
                .setExpertName("Mr Expert Defendant")
                .setFirstName("Expert")
                .setLastName("Defendant")
                .setPhoneNumber("07123456789")
                .setEmailAddress("test@email.com")
                .setFieldofExpertise("Roofing")
                .setEstimatedCost(new BigDecimal(434));

        updatedRespondent2DQ.setRespondToClaimExperts2(expertDetails);
        respondent2DQ = updatedRespondent2DQ;

        this.responseClaimExpertSpecRequired2(expertsRequired != null ? expertsRequired : YES);
        return this;
    }

    public CaseDataBuilder applicant1DQSmallClaimExperts(ExpertDetails experts, YesOrNo expertsRequired) {
        Applicant1DQ updatedApplicant1DQ = applicant1DQ != null
            ? applicant1DQ.copy() : applicant1DQ().build().getApplicant1DQ().copy();

        ExpertDetails expertDetails = experts != null
            ? experts
            : new ExpertDetails()
                .setExpertName("Mr Expert Defendant")
                .setFirstName("Expert")
                .setLastName("Defendant")
                .setPhoneNumber("07123456789")
                .setEmailAddress("test@email.com")
                .setFieldofExpertise("Roofing")
                .setEstimatedCost(new BigDecimal(434));

        updatedApplicant1DQ.setApplicant1RespondToClaimExperts(expertDetails);
        this.applicant1ClaimExpertSpecRequired(expertsRequired != null ? expertsRequired : YES);

        applicant1DQ = updatedApplicant1DQ;
        return this;
    }

    public CaseDataBuilder applicant2DQSmallClaimExperts() {
        return applicant2DQSmallClaimExperts(null, null);
    }

    public CaseDataBuilder applicant2DQSmallClaimExperts(ExpertDetails experts, YesOrNo expertsRequired) {
        Applicant2DQ updatedApplicant2DQ = applicant2DQ != null
            ? applicant2DQ.copy() : applicant2DQ().build().getApplicant2DQ().copy();

        ExpertDetails expertDetails = experts != null
            ? experts
            : new ExpertDetails()
                .setExpertName("Mr Expert Defendant")
                .setFirstName("Expert")
                .setLastName("Defendant")
                .setPhoneNumber("07123456789")
                .setEmailAddress("test@email.com")
                .setFieldofExpertise("Roofing")
                .setEstimatedCost(new BigDecimal(434));

        updatedApplicant2DQ.setApplicant2RespondToClaimExperts(expertDetails);
        this.applicantMPClaimExpertSpecRequired(expertsRequired != null ? expertsRequired : YES);

        applicant2DQ = updatedApplicant2DQ;
        return this;
    }

    public CaseDataBuilder noApplicant2DQSmallClaimExperts() {
        Applicant2DQ updatedApplicant2DQ = applicant2DQ != null
            ? applicant2DQ.copy() : applicant2DQ().build().getApplicant2DQ().copy();
        applicant2DQ = updatedApplicant2DQ
            .setApplicant2DQExperts(new Experts().setExpertRequired(NO));
        this.applicantMPClaimExpertSpecRequired(NO);
        return this;
    }

    public CaseDataBuilder applicant1DQWithLocation() {
        applicant1DQ = new Applicant1DQ()
            .setApplicant1DQFileDirectionsQuestionnaire(new FileDirectionsQuestionnaire()
                .setExplainedToClient(List.of("OTHER"))
                .setOneMonthStayRequested(NO)
                .setReactionProtocolCompliedWith(YES))
            .setApplicant1DQDisclosureOfElectronicDocuments(new DisclosureOfElectronicDocuments()
                .setReachedAgreement(YES))
            .setApplicant1DQDisclosureOfNonElectronicDocuments(new DisclosureOfNonElectronicDocuments()
                .setDirectionsForDisclosureProposed(NO))
            .setApplicant1DQExperts(new Experts().setExpertRequired(NO))
            .setApplicant1DQWitnesses(new Witnesses().setWitnessesToAppear(NO))
            .setApplicant1DQHearing(new Hearing()
                .setHearingLength(ONE_DAY)
                .setUnavailableDatesRequired(NO))
            .setApplicant1DQRequestedCourt(new RequestedCourt()
                .setResponseCourtCode("court4")
                .setCaseLocation(new CaseLocationCivil()
                                     .setBaseLocation("00000").setRegion("dummy region")))
            .setApplicant1DQHearingSupport(new HearingSupport().setRequirements(List.of()))
            .setApplicant1DQFurtherInformation(new FurtherInformation().setFutureApplications(NO))
            .setApplicant1DQLanguage(new WelshLanguageRequirements())
            .setApplicant1DQVulnerabilityQuestions(new VulnerabilityQuestions()
                .setVulnerabilityAdjustmentsRequired(NO))
            .setApplicant1DQStatementOfTruth(new StatementOfTruth().setName("Bob Jones").setRole("Solicitor"));
        return this;
    }

    public CaseDataBuilder applicant1DQWithFixedRecoverableCosts() {
        applicant1DQ = applicant1DQ.copy()
            .setApplicant1DQFixedRecoverableCosts(new FixedRecoverableCosts()
                .setIsSubjectToFixedRecoverableCostRegime(YES)
                .setBand(BAND_1)
                .setComplexityBandingAgreed(YES)
                .setReasons("Good reason"));
        return this;
    }

    public CaseDataBuilder applicant2DQWithFixedRecoverableCosts() {
        applicant2DQ = applicant2DQ.copy()
            .setApplicant2DQFixedRecoverableCosts(new FixedRecoverableCosts()
                .setIsSubjectToFixedRecoverableCostRegime(YES)
                .setBand(BAND_1)
                .setComplexityBandingAgreed(YES)
                .setReasons("Good reason"));
        return this;
    }

    public CaseDataBuilder applicant1DQWithFixedRecoverableCostsIntermediate() {
        applicant1DQ = applicant1DQ.copy()
            .setApplicant1DQFixedRecoverableCostsIntermediate(new FixedRecoverableCosts()
                .setIsSubjectToFixedRecoverableCostRegime(YES)
                .setBand(BAND_1)
                .setComplexityBandingAgreed(YES)
                .setReasons("Good reason")
                .setFrcSupportingDocument(
                    DocumentBuilder.builder()
                        .setDocumentName("frc-doc1")
                        .build()));
        return this;
    }

    public CaseDataBuilder applicant1DQWithLocationWithoutExperts() {
        applicant1DQ = new Applicant1DQ()
            .setApplicant1DQFileDirectionsQuestionnaire(new FileDirectionsQuestionnaire()
                .setExplainedToClient(List.of("OTHER"))
                .setOneMonthStayRequested(NO)
                .setReactionProtocolCompliedWith(YES))
            .setApplicant1DQDisclosureOfElectronicDocuments(new DisclosureOfElectronicDocuments()
                .setReachedAgreement(YES))
            .setApplicant1DQDisclosureOfNonElectronicDocuments(new DisclosureOfNonElectronicDocuments()
                .setDirectionsForDisclosureProposed(NO))
            .setApplicant1DQWitnesses(new Witnesses().setWitnessesToAppear(NO))
            .setApplicant1DQHearing(new Hearing()
                .setHearingLength(ONE_DAY)
                .setUnavailableDatesRequired(NO))
            .setApplicant1DQRequestedCourt(new RequestedCourt()
                .setResponseCourtCode("court4")
                .setCaseLocation(new CaseLocationCivil()
                                     .setBaseLocation("dummy base").setRegion("dummy region")))
            .setApplicant1DQHearingSupport(new HearingSupport().setRequirements(List.of()))
            .setApplicant1DQFurtherInformation(new FurtherInformation().setFutureApplications(NO))
            .setApplicant1DQLanguage(new WelshLanguageRequirements())
            .setApplicant1DQVulnerabilityQuestions(new VulnerabilityQuestions()
                .setVulnerabilityAdjustmentsRequired(NO))
            .setApplicant1DQStatementOfTruth(new StatementOfTruth().setName("Bob Jones").setRole("Solicitor"));
        return this;
    }

    public CaseDataBuilder applicant2DQWithLocation() {
        applicant2DQ = new Applicant2DQ()
            .setApplicant2DQFileDirectionsQuestionnaire(new FileDirectionsQuestionnaire()
                .setExplainedToClient(List.of("OTHER"))
                .setOneMonthStayRequested(NO)
                .setReactionProtocolCompliedWith(YES))
            .setApplicant2DQDisclosureOfElectronicDocuments(new DisclosureOfElectronicDocuments()
                .setReachedAgreement(YES))
            .setApplicant2DQDisclosureOfNonElectronicDocuments(new DisclosureOfNonElectronicDocuments()
                .setDirectionsForDisclosureProposed(NO))
            .setApplicant2DQExperts(new Experts().setExpertRequired(NO))
            .setApplicant2DQWitnesses(new Witnesses().setWitnessesToAppear(NO))
            .setApplicant2DQHearing(new Hearing()
                .setHearingLength(ONE_DAY)
                .setUnavailableDatesRequired(NO))
            .setApplicant2DQRequestedCourt(new RequestedCourt()
                .setResponseCourtCode("court4")
                .setCaseLocation(new CaseLocationCivil()
                                     .setBaseLocation("dummy base").setRegion("dummy region")))
            .setApplicant2DQHearingSupport(new HearingSupport().setRequirements(List.of()))
            .setApplicant2DQFurtherInformation(new FurtherInformation().setFutureApplications(NO))
            .setApplicant2DQLanguage(new WelshLanguageRequirements())
            .setApplicant2DQVulnerabilityQuestions(new VulnerabilityQuestions()
                .setVulnerabilityAdjustmentsRequired(NO))
            .setApplicant2DQStatementOfTruth(new StatementOfTruth().setName("Bob Jones").setRole("Solicitor"));
        return this;
    }

    public CaseDataBuilder applicant2DQ() {
        applicant2DQ = new Applicant2DQ()
            .setApplicant2DQFileDirectionsQuestionnaire(new FileDirectionsQuestionnaire()
                .setExplainedToClient(List.of("OTHER"))
                .setOneMonthStayRequested(NO)
                .setReactionProtocolCompliedWith(YES))
            .setApplicant2DQDisclosureOfElectronicDocuments(new DisclosureOfElectronicDocuments()
                .setReachedAgreement(YES))
            .setApplicant2DQDisclosureOfNonElectronicDocuments(new DisclosureOfNonElectronicDocuments()
                .setDirectionsForDisclosureProposed(NO))
            .setApplicant2DQExperts(new Experts().setExpertRequired(NO))
            .setApplicant2DQWitnesses(new Witnesses().setWitnessesToAppear(NO))
            .setApplicant2DQHearing(new Hearing()
                .setHearingLength(ONE_DAY)
                .setUnavailableDatesRequired(NO))
            .setApplicant2DQRequestedCourt(new RequestedCourt())
            .setApplicant2DQHearingSupport(new HearingSupport()
                .setSupportRequirements(YES)
                .setSupportRequirementsAdditional("Additional support needed")
                .setRequirements(List.of()))
            .setApplicant2DQFurtherInformation(new FurtherInformation().setFutureApplications(NO))
            .setApplicant2DQLanguage(new WelshLanguageRequirements())
            .setApplicant2DQStatementOfTruth(new StatementOfTruth().setName("Bob Jones").setRole("Solicitor"));
        return this;
    }

    public CaseDataBuilder applicant2DQ(Applicant2DQ applicant2DQ) {
        this.applicant2DQ = applicant2DQ;
        return this;
    }

    public CaseDataBuilder respondentSolicitor1OrganisationDetails(SolicitorOrganisationDetails organisationDetails) {
        this.respondentSolicitor1OrganisationDetails = organisationDetails;
        return this;
    }

    public CaseDataBuilder respondentSolicitor2OrganisationDetails(SolicitorOrganisationDetails organisationDetails) {
        this.respondentSolicitor2OrganisationDetails = organisationDetails;
        return this;
    }

    public CaseDataBuilder applicant1ProceedWithClaim(YesOrNo yesOrNo) {
        this.applicant1ProceedWithClaim = yesOrNo;
        return this;
    }

    public CaseDataBuilder applicant1ProceedWithClaimSpec2v1(YesOrNo yesOrNo) {
        this.applicant1ProceedWithClaimSpec2v1 = yesOrNo;
        return this;
    }

    public CaseDataBuilder applicant1ProceedWithClaimMultiParty2v1(YesOrNo yesOrNo) {
        this.applicant1ProceedWithClaimMultiParty2v1 = yesOrNo;
        return this;
    }

    public CaseDataBuilder applicant2ProceedWithClaimMultiParty2v1(YesOrNo yesOrNo) {
        this.applicant2ProceedWithClaimMultiParty2v1 = yesOrNo;
        return this;
    }

    public CaseDataBuilder applicantsProceedIntention(YesOrNo yesOrNo) {
        this.applicantsProceedIntention = yesOrNo;
        return this;
    }

    public CaseDataBuilder applicant1ProceedWithClaimAgainstRespondent1MultiParty1v2(YesOrNo yesOrNo) {
        this.applicant1ProceedWithClaimAgainstRespondent1MultiParty1v2 = yesOrNo;
        return this;
    }

    public CaseDataBuilder applicant1ProceedWithClaimAgainstRespondent2MultiParty1v2(YesOrNo yesOrNo) {
        this.applicant1ProceedWithClaimAgainstRespondent2MultiParty1v2 = yesOrNo;
        return this;
    }

    public CaseDataBuilder respondent1ClaimResponseIntentionType(ResponseIntention responseIntention) {
        this.respondent1ClaimResponseIntentionType = responseIntention;
        return this;
    }

    public CaseDataBuilder respondent2ClaimResponseIntentionType(ResponseIntention responseIntention) {
        this.respondent2ClaimResponseIntentionType = responseIntention;
        return this;
    }

    public CaseDataBuilder claimValue(ClaimValue claimValue) {
        this.claimValue = claimValue;
        return this;
    }

    public CaseDataBuilder uploadParticularsOfClaim(YesOrNo uploadParticularsOfClaim) {
        this.uploadParticularsOfClaim = uploadParticularsOfClaim;
        return this;
    }

    public CaseDataBuilder issueDate(LocalDate issueDate) {
        this.issueDate = issueDate;
        return this;
    }

    public CaseDataBuilder hearingReferenceNumber(String hearingReference) {
        this.hearingReference = hearingReference;
        return this;
    }

    public CaseDataBuilder listingOrRelisting(ListingOrRelisting listingOrRelisting) {
        this.listingOrRelisting = listingOrRelisting;
        return this;
    }

    public CaseDataBuilder takenOfflineDate(LocalDateTime takenOfflineDate) {
        this.takenOfflineDate = takenOfflineDate;
        return this;
    }

    public CaseDataBuilder hearingDate(LocalDate hearingDate) {
        this.hearingDate = hearingDate;
        return this;
    }

    public CaseDataBuilder systemGeneratedCaseDocuments(List<Element<CaseDocument>> systemGeneratedCaseDocuments) {
        this.systemGeneratedCaseDocuments = systemGeneratedCaseDocuments;
        return this;
    }

    public CaseDataBuilder applicant1(Party party) {
        this.applicant1 = party;
        return this;
    }

    public CaseDataBuilder paymentTypeSelection(DJPaymentTypeSelection paymentTypeSelection) {
        this.paymentTypeSelection = paymentTypeSelection;
        return this;
    }

    public CaseDataBuilder paymentSetDate(LocalDate paymentSetDate) {
        this.paymentSetDate = paymentSetDate;
        return this;
    }

    public CaseDataBuilder repaymentFrequency(RepaymentFrequencyDJ repaymentFrequency) {
        this.repaymentFrequency = repaymentFrequency;
        return this;
    }

    public CaseDataBuilder repaymentDate(LocalDate repaymentDate) {
        this.repaymentDate = repaymentDate;
        return this;
    }

    public CaseDataBuilder repaymentSuggestion(String repaymentSuggestion) {
        this.repaymentSuggestion = repaymentSuggestion;
        return this;
    }

    public CaseDataBuilder applicant2(Party party) {
        this.applicant2 = party;
        return this;
    }

    public CaseDataBuilder respondent1(Party party) {
        this.respondent1 = party;
        return this;
    }

    public CaseDataBuilder legacyCaseReference(String legacyCaseReference) {
        this.legacyCaseReference = legacyCaseReference;
        return this;
    }

    public CaseDataBuilder respondent1Represented(YesOrNo isRepresented) {
        this.respondent1Represented = isRepresented;
        return this;
    }

    public CaseDataBuilder respondent2Represented(YesOrNo isRepresented) {
        this.respondent2Represented = isRepresented;
        return this;
    }

    public CaseDataBuilder defendantUserDetails(IdamUserDetails defendantUserDetails) {
        this.defendantUserDetails = defendantUserDetails;
        return this;
    }

    public CaseDataBuilder applicant1Represented(YesOrNo isRepresented) {
        this.applicant1Represented = isRepresented;
        return this;
    }

    public CaseDataBuilder defendant1LIPAtClaimIssued(YesOrNo defendant1LIPAtClaimIssued) {
        this.defendant1LIPAtClaimIssued = defendant1LIPAtClaimIssued;
        return this;
    }

    public CaseDataBuilder defendant2LIPAtClaimIssued(YesOrNo defendant2LIPAtClaimIssued) {
        this.defendant2LIPAtClaimIssued = defendant2LIPAtClaimIssued;
        return this;
    }

    public CaseDataBuilder respondent1OrgRegistered(YesOrNo respondent1OrgRegistered) {
        this.respondent1OrgRegistered = respondent1OrgRegistered;
        return this;
    }

    public CaseDataBuilder claimDetailsNotificationDate(LocalDateTime localDate) {
        this.claimDetailsNotificationDate = localDate;
        return this;
    }

    public CaseDataBuilder respondent2OrgRegistered(YesOrNo respondent2OrgRegistered) {
        this.respondent2OrgRegistered = respondent2OrgRegistered;
        return this;
    }

    public CaseDataBuilder claimProceedsInCaseman(ClaimProceedsInCaseman claimProceedsInCaseman) {
        this.claimProceedsInCaseman = claimProceedsInCaseman;
        return this;
    }

    public CaseDataBuilder claimProceedsInCasemanLR(ClaimProceedsInCasemanLR claimProceedsInCasemanLR) {
        this.claimProceedsInCasemanLR = claimProceedsInCasemanLR;
        return this;
    }

    public CaseDataBuilder applicant1OrganisationPolicy(OrganisationPolicy applicant1OrganisationPolicy) {
        this.applicant1OrganisationPolicy = applicant1OrganisationPolicy;
        return this;
    }

    public CaseDataBuilder respondent1OrganisationPolicy(OrganisationPolicy respondent1OrganisationPolicy) {
        this.respondent1OrganisationPolicy = respondent1OrganisationPolicy;
        return this;
    }

    public CaseDataBuilder respondent2OrganisationPolicy(OrganisationPolicy respondent2OrganisationPolicy) {
        this.respondent2OrganisationPolicy = respondent2OrganisationPolicy;
        return this;
    }

    public CaseDataBuilder addRespondent2(YesOrNo addRespondent2) {
        this.addRespondent2 = addRespondent2;
        return this;
    }

    public CaseDataBuilder addApplicant2(YesOrNo addApplicant2) {
        this.addApplicant2 = addApplicant2;
        return this;
    }

    public CaseDataBuilder addApplicant2() {
        this.addApplicant2 = YES;
        return this;
    }

    public CaseDataBuilder applicant1RepaymentOptionForDefendantSpec(PaymentType applicant1RepaymentOptionForDefendantSpec) {
        this.applicant1RepaymentOptionForDefendantSpec = applicant1RepaymentOptionForDefendantSpec;
        return this;
    }

    public CaseDataBuilder respondent1ClaimResponseTypeForSpec(RespondentResponseTypeSpec respondentResponseTypeSpec) {
        this.respondent1ClaimResponseTypeForSpec = respondentResponseTypeSpec;
        return this;
    }

    public CaseDataBuilder respondent2ClaimResponseTypeForSpec(RespondentResponseTypeSpec respondentResponseTypeSpec) {
        this.respondent2ClaimResponseTypeForSpec = respondentResponseTypeSpec;
        return this;
    }

    public CaseDataBuilder respondent1ClaimResponseType(RespondentResponseType respondent1ClaimResponseType) {
        this.respondent1ClaimResponseType = respondent1ClaimResponseType;
        return this;
    }

    public CaseDataBuilder respondent2ClaimResponseType(RespondentResponseType respondent2ClaimResponseType) {
        this.respondent2ClaimResponseType = respondent2ClaimResponseType;
        return this;
    }

    public CaseDataBuilder setRespondent1LitigationFriendCreatedDate(LocalDateTime createdDate) {
        this.respondent1LitigationFriendCreatedDate = createdDate;
        return this;
    }

    public CaseDataBuilder setRespondent1LitigationFriendDate(LocalDateTime date) {
        this.respondent1LitigationFriendDate = date;
        return this;
    }

    public CaseDataBuilder respondent2SameLegalRepresentative(YesOrNo respondent2SameLegalRepresentative) {
        this.respondent2SameLegalRepresentative = respondent2SameLegalRepresentative;
        return this;
    }

    public CaseDataBuilder caseReference(Long ccdCaseReference) {
        this.ccdCaseReference = ccdCaseReference;
        return this;
    }

    public CaseDataBuilder claimNotificationDeadline(LocalDateTime deadline) {
        this.claimNotificationDeadline = deadline;
        return this;
    }

    public CaseDataBuilder claimDismissedDate(LocalDateTime date) {
        this.claimDismissedDate = date;
        return this;
    }

    public CaseDataBuilder caseDismissedHearingFeeDueDate(LocalDateTime date) {
        this.caseDismissedHearingFeeDueDate = date;
        return this;
    }

    public CaseDataBuilder addLegalRepDeadline(LocalDateTime date) {
        this.addLegalRepDeadline = date;
        return this;
    }

    public CaseDataBuilder takenOfflineByStaffDate(LocalDateTime takenOfflineByStaffDate) {
        this.takenOfflineByStaffDate = takenOfflineByStaffDate;
        return this;
    }

    public CaseDataBuilder extensionDate(LocalDate extensionDate) {
        this.respondentSolicitor1AgreedDeadlineExtension = extensionDate;
        return this;
    }

    public CaseDataBuilder uiStatementOfTruth(StatementOfTruth statementOfTruth) {
        this.uiStatementOfTruth = statementOfTruth;
        return this;
    }

    public CaseDataBuilder defendantSolicitorNotifyClaimOptions(String defaultValue) {
        this.defendantSolicitorNotifyClaimOptions = DynamicList.builder()
            .value(DynamicListElement.builder()
                .label(defaultValue)
                .build())
            .build();
        return this;
    }

    public CaseDataBuilder defendantSolicitorNotifyClaimDetailsOptions(String defaultValue) {
        this.defendantSolicitorNotifyClaimDetailsOptions = DynamicList.builder()
            .value(DynamicListElement.builder()
                .label(defaultValue)
                .build())
            .build();
        return this;
    }

    public CaseDataBuilder selectLitigationFriend(String defaultValue) {
        this.selectLitigationFriend = DynamicList.builder()
            .value(DynamicListElement.builder()
                .label(defaultValue)
                .build())
            .build();
        return this;
    }

    public CaseDataBuilder respondent1ResponseDate(LocalDateTime date) {
        this.respondent1ResponseDate = date;
        return this;
    }

    public CaseDataBuilder respondent2ResponseDate(LocalDateTime date) {
        this.respondent2ResponseDate = date;
        return this;
    }

    public CaseDataBuilder applicant1ResponseDate(LocalDateTime date) {
        this.applicant1ResponseDate = date;
        return this;
    }

    public CaseDataBuilder reasonNotSuitableSDO(ReasonNotSuitableSDO reasonNotSuitableSDO) {
        this.reasonNotSuitableSDO = reasonNotSuitableSDO;
        return this;
    }

    public CaseDataBuilder defaultJudgmentDocuments(List<Element<CaseDocument>> defaultJudgmentDocuments) {
        this.defaultJudgmentDocuments = defaultJudgmentDocuments;
        return this;
    }

    public CaseDataBuilder atState(FlowState.Main flowState) {
        return atState(flowState, ONE_V_ONE);
    }

    public CaseDataBuilder atState(FlowState.Main flowState, MultiPartyScenario mpScenario) {
        switch (flowState) {
            case DRAFT:
                return atStateClaimDraft();
            case CLAIM_SUBMITTED:
                return atStateClaimSubmitted();
            case CLAIM_ISSUED_PAYMENT_SUCCESSFUL:
                return atStatePaymentSuccessful();
            case CLAIM_ISSUED_PAYMENT_FAILED:
                return atStateClaimIssuedPaymentFailed();
            case PENDING_CLAIM_ISSUED:
                return atStatePendingClaimIssued();
            case PENDING_CLAIM_ISSUED_UNREGISTERED_DEFENDANT:
                return atStatePendingClaimIssuedUnregisteredDefendant();
            case PENDING_CLAIM_ISSUED_UNREPRESENTED_DEFENDANT:
                return atStatePendingClaimIssuedUnrepresentedDefendant();
            case PENDING_CLAIM_ISSUED_UNREPRESENTED_UNREGISTERED_DEFENDANT:
                return atStatePendingClaimIssuedUnrepresentedUnregisteredDefendant();
            case CLAIM_ISSUED:
                return atStateClaimIssued();
            case CLAIM_NOTIFIED:
                return atStateClaimNotified();
            case TAKEN_OFFLINE_AFTER_CLAIM_DETAILS_NOTIFIED:
                return atStateProceedsOfflineAfterClaimDetailsNotified();
            case TAKEN_OFFLINE_AFTER_CLAIM_NOTIFIED:
                return atStateProceedsOfflineAfterClaimNotified();
            case CLAIM_DETAILS_NOTIFIED:
                return atStateClaimDetailsNotified();
            case CLAIM_DETAILS_NOTIFIED_TIME_EXTENSION:
                return atStateClaimDetailsNotifiedTimeExtension();
            case NOTIFICATION_ACKNOWLEDGED:
                return atStateNotificationAcknowledged();
            case NOTIFICATION_ACKNOWLEDGED_TIME_EXTENSION:
                return atStateNotificationAcknowledgedRespondent1TimeExtension();
            case AWAITING_RESPONSES_FULL_DEFENCE_RECEIVED:
                return atStateAwaitingResponseFullDefenceReceived();
            case AWAITING_RESPONSES_NOT_FULL_DEFENCE_OR_FULL_ADMIT_RECEIVED:
                return atStateAwaitingResponseNotFullDefenceReceived();
            case FULL_DEFENCE:
                return atStateRespondentFullDefenceAfterNotificationAcknowledgement();
            case FULL_ADMISSION:
                return atStateRespondentFullAdmissionAfterNotificationAcknowledged();
            case PART_ADMISSION:
                return atStateRespondentPartAdmissionAfterNotificationAcknowledgement();
            case COUNTER_CLAIM:
                return atStateRespondentCounterClaim();
            case FULL_DEFENCE_PROCEED:
                return atStateApplicantRespondToDefenceAndProceed(mpScenario);
            case FULL_DEFENCE_NOT_PROCEED:
                return atStateApplicantRespondToDefenceAndNotProceed();
            case TAKEN_OFFLINE_UNREPRESENTED_DEFENDANT:
                return atStateClaimIssuedUnrepresentedDefendants();
            case TAKEN_OFFLINE_UNREGISTERED_DEFENDANT:
                return atStateProceedsOfflineUnregisteredDefendants();
            case TAKEN_OFFLINE_UNREPRESENTED_UNREGISTERED_DEFENDANT:
                return atStateProceedsOfflineUnrepresentedDefendant1UnregisteredDefendant2();
            case TAKEN_OFFLINE_BY_STAFF:
                return atStateTakenOfflineByStaff();
            case CLAIM_DISMISSED_PAST_CLAIM_DISMISSED_DEADLINE:
                return atStateClaimDismissed();
            case CLAIM_DISMISSED_PAST_CLAIM_DETAILS_NOTIFICATION_DEADLINE:
                return atStateClaimDismissedPastClaimDetailsNotificationDeadline();
            case PAST_APPLICANT_RESPONSE_DEADLINE_AWAITING_CAMUNDA:
                return atStatePastApplicantResponseDeadline();
            case TAKEN_OFFLINE_PAST_APPLICANT_RESPONSE_DEADLINE:
                return atStateTakenOfflinePastApplicantResponseDeadline();
            case CLAIM_DISMISSED_PAST_CLAIM_NOTIFICATION_DEADLINE:
                return atStateClaimDismissedPastClaimNotificationDeadline();
            case TAKEN_OFFLINE_SDO_NOT_DRAWN:
                return atStateTakenOfflineSDONotDrawn(mpScenario);
            case TAKEN_OFFLINE_AFTER_SDO:
                return atStateTakenOfflineAfterSDO(mpScenario);
            default:
                throw new IllegalArgumentException("Invalid internal state: " + flowState);
        }
    }

    public CaseDataBuilder atStateClaimPastClaimNotificationDeadline() {
        atStateClaimIssued();
        ccdState = CASE_DISMISSED;
        claimNotificationDeadline = LocalDateTime.now().minusDays(1);
        return this;
    }

    public CaseDataBuilder atStateClaimDismissedPastClaimNotificationDeadline() {
        atStateClaimPastClaimNotificationDeadline();
        ccdState = CASE_DISMISSED;
        claimDismissedDate = LocalDateTime.now();
        return this;
    }

    public CaseDataBuilder atStateClaimPastClaimDetailsNotificationDeadline() {
        atStateClaimNotified();
        claimDetailsNotificationDeadline = LocalDateTime.now().minusDays(5);
        return this;
    }

    public CaseDataBuilder atStateClaimDismissedPastClaimDetailsNotificationDeadline() {
        atStateClaimPastClaimDetailsNotificationDeadline();
        ccdState = CASE_DISMISSED;
        claimDismissedDate = claimNotificationDate.plusDays(1);
        return this;
    }

    public CaseDataBuilder atStateClaimDismissedPastHearingFeeDueDeadline() {
        return atStateClaimDismissedPastHearingFeeDueDeadline(ONE_V_ONE);
    }

    public CaseDataBuilder atStateClaimDismissedPastHearingFeeDueDeadline(MultiPartyScenario mpScenario) {
        atStateHearingFeeDueUnpaid(mpScenario);
        ccdState = CASE_DISMISSED;
        caseDismissedHearingFeeDueDate = LocalDateTime.now();
        hearingDate = hearingDueDate.plusWeeks(2);
        return this;
    }

    public CaseDataBuilder atStateClaimIssuedUnrepresentedDefendants() {
        atStatePendingClaimIssuedUnrepresentedDefendant();
        respondent2 = PartyBuilder.builder().individual().build().toBuilder().partyID("res-2-party-id").build();
        ccdState = PROCEEDS_IN_HERITAGE_SYSTEM;
        takenOfflineDate = LocalDateTime.now();
        respondentSolicitor1OrganisationDetails = null;
        addRespondent2 = YES;
        respondent1OrganisationPolicy = new OrganisationPolicy()
            .setOrgPolicyCaseAssignedRole("[RESPONDENTSOLICITORONE]");
        respondent2OrganisationPolicy = new OrganisationPolicy()
            .setOrgPolicyCaseAssignedRole("[RESPONDENTSOLICITORTWO]");
        respondent1OrgRegistered = null;
        respondent2OrgRegistered = null;
        return this;
    }

    public CaseDataBuilder atStateClaimIssued1v1UnrepresentedDefendant() {
        atStateClaimIssuedUnrepresentedDefendants();
        addRespondent2 = NO;
        respondent2 = null;
        respondent2Represented = null;
        respondent2OrganisationPolicy = null;
        respondent2OrgRegistered = null;
        return this;
    }

    public CaseDataBuilder atStateClaimIssued1v1UnrepresentedDefendantSpec() {
        atStateClaimIssuedUnrepresentedDefendants();
        ccdState = AWAITING_RESPONDENT_ACKNOWLEDGEMENT;
        addRespondent2 = NO;
        respondent2 = null;
        respondent2Represented = null;
        respondent2OrganisationPolicy = null;
        return this;
    }

    public CaseDataBuilder atStateClaimIssued1v2UnrepresentedDefendant() {
        atStateClaimIssuedUnrepresentedDefendants();
        addRespondent2 = YES;
        respondent2 = PartyBuilder.builder().individual().build().toBuilder().partyID("res-2-party-id").build();
        takenOfflineDate = LocalDateTime.now();
        respondentSolicitor2OrganisationDetails = null;
        respondent2OrganisationPolicy = null;
        return this;
    }

    public CaseDataBuilder atStateClaimIssuedUnrepresentedDefendant1() {
        atStatePendingClaimIssuedUnrepresentedDefendant();
        addRespondent2 = YES;
        ccdState = PROCEEDS_IN_HERITAGE_SYSTEM;
        takenOfflineDate = LocalDateTime.now();
        respondentSolicitor1OrganisationDetails = null;
        respondent2Represented = YES;
        respondent2OrgRegistered = YES;
        respondentSolicitor1OrganisationDetails = null;
        defendant1LIPAtClaimIssued = YES;
        respondent1OrganisationPolicy = new OrganisationPolicy()
            .setOrgPolicyCaseAssignedRole("[RESPONDENTSOLICITORONE]");
        respondent2OrganisationPolicy = new OrganisationPolicy()
            .setOrganisation(new Organisation().setOrganisationID("QWERTY R2"))
            .setOrgPolicyCaseAssignedRole("[RESPONDENTSOLICITORTWO]");
        return this;
    }

    public CaseDataBuilder atStateClaimIssuedUnrepresentedDefendant2() {
        atStatePendingClaimIssuedUnrepresentedDefendant();
        addRespondent2 = YES;
        respondent2 = PartyBuilder.builder().individual().build().toBuilder().partyID("res-2-party-id").build();
        ccdState = PROCEEDS_IN_HERITAGE_SYSTEM;
        takenOfflineDate = LocalDateTime.now();
        respondentSolicitor2OrganisationDetails = null;
        respondent1Represented = YES;
        respondent1OrgRegistered = YES;
        respondentSolicitor1OrganisationDetails = null;
        respondent1OrganisationPolicy = new OrganisationPolicy()
            .setOrganisation(new Organisation().setOrganisationID("QWERTY R"))
            .setOrgPolicyCaseAssignedRole("[RESPONDENTSOLICITORONE]");
        respondent2OrganisationPolicy = new OrganisationPolicy()
            .setOrgPolicyCaseAssignedRole("[RESPONDENTSOLICITORTWO]");
        return this;
    }

    public CaseDataBuilder atStateProceedsOfflineUnregisteredDefendants() {
        atStatePendingClaimIssuedUnregisteredDefendant();
        addRespondent2 = YES;
        respondent2 = PartyBuilder.builder().individual().build().toBuilder().partyID("res-2-party-id").build();
        ccdState = PROCEEDS_IN_HERITAGE_SYSTEM;
        takenOfflineDate = LocalDateTime.now();
        respondent1OrganisationPolicy = new OrganisationPolicy()
            .setOrgPolicyCaseAssignedRole("[RESPONDENTSOLICITORONE]");
        respondent2OrganisationPolicy = new OrganisationPolicy()
            .setOrgPolicyCaseAssignedRole("[RESPONDENTSOLICITORTWO]");
        respondent1OrgRegistered = NO;
        respondent2OrgRegistered = NO;
        respondent1Represented = YES;
        respondent2Represented = YES;
        respondent2SameLegalRepresentative = NO;

        respondentSolicitor1OrganisationDetails = new SolicitorOrganisationDetails()
            .setEmail("testorg@email.com")
            .setOrganisationName("test org name")
            .setFax("123123123")
            .setDx("test org dx")
            .setPhoneNumber("0123456789")
            .setAddress(AddressBuilder.defaults().build())
            ;

        respondentSolicitor2OrganisationDetails = new SolicitorOrganisationDetails()
            .setEmail("testorg@email.com")
            .setOrganisationName("test org name")
            .setFax("123123123")
            .setDx("test org dx")
            .setPhoneNumber("0123456789")
            .setAddress(AddressBuilder.defaults().build())
            ;
        return this;
    }

    public CaseDataBuilder atStateProceedsOffline1v1UnregisteredDefendant() {
        atStateProceedsOfflineUnregisteredDefendants();
        addRespondent2 = NO;
        respondent2 = null;
        respondent2Represented = null;
        respondent2OrgRegistered = null;
        return this;
    }

    public CaseDataBuilder atStateProceedsOfflineUnregisteredDefendant1() {
        atStatePendingClaimIssuedUnregisteredDefendant();
        ccdState = PROCEEDS_IN_HERITAGE_SYSTEM;
        takenOfflineDate = LocalDateTime.now();
        respondent1OrganisationPolicy = new OrganisationPolicy()
            .setOrgPolicyCaseAssignedRole("[RESPONDENTSOLICITORONE]");
        respondent1Represented = YES;
        respondent1OrgRegistered = NO;
        respondent2SameLegalRepresentative = NO;

        respondentSolicitor1OrganisationDetails = new SolicitorOrganisationDetails()
            .setEmail("testorg@email.com")
            .setOrganisationName("test org name")
            .setFax("123123123")
            .setDx("test org dx")
            .setPhoneNumber("0123456789")
            .setAddress(AddressBuilder.defaults().build())
            ;
        return this;
    }

    public CaseDataBuilder atStateProceedsOfflineUnregisteredDefendant2() {
        atStatePendingClaimIssuedUnregisteredDefendant();
        respondent2 = PartyBuilder.builder().individual().build().toBuilder().partyID("res-2-party-id").build();
        ccdState = PROCEEDS_IN_HERITAGE_SYSTEM;
        takenOfflineDate = LocalDateTime.now();
        respondent1OrgRegistered = YES;
        respondent2OrganisationPolicy = new OrganisationPolicy()
            .setOrgPolicyCaseAssignedRole("[RESPONDENTSOLICITORTWO]");
        respondent2Represented = YES;
        respondent2OrgRegistered = NO;
        respondent2SameLegalRepresentative = NO;

        respondentSolicitor1OrganisationDetails = new SolicitorOrganisationDetails()
            .setEmail("testorg@email.com")
            .setOrganisationName("test org name")
            .setFax("123123123")
            .setDx("test org dx")
            .setPhoneNumber("0123456789")
            .setAddress(AddressBuilder.defaults().build())
            ;
        return this;
    }

    public CaseDataBuilder atStateProceedsOfflineSameUnregisteredDefendant() {
        atStatePendingClaimIssuedUnregisteredDefendant();
        respondent2 = PartyBuilder.builder().individual().build().toBuilder().partyID("res-2-party-id").build();
        ccdState = PROCEEDS_IN_HERITAGE_SYSTEM;
        takenOfflineDate = LocalDateTime.now();
        respondent1OrgRegistered = NO;
        respondent2OrganisationPolicy = null;
        respondent2Represented = YES;
        respondent2SameLegalRepresentative = YES;

        respondentSolicitor1OrganisationDetails = new SolicitorOrganisationDetails()
            .setEmail("testorg@email.com")
            .setOrganisationName("test org name")
            .setFax("123123123")
            .setDx("test org dx")
            .setPhoneNumber("0123456789")
            .setAddress(AddressBuilder.defaults().build())
            ;
        return this;
    }

    public CaseDataBuilder atStateClaimDetailsNotifiedWithNoticeOfChangeRespondent1() {
        atStateClaimDetailsNotified();
        changeOfRepresentation(false, false, "New-sol-id", "Previous-sol-id", "previous-solicitor@example.com");
        return this;
    }

    public CaseDataBuilder atStateClaimDetailsNotifiedWithNoticeOfChangeLip() {
        atStateClaimDetailsNotified();
        changeOfRepresentation(false, false, "New-sol-id", null, null);
        return this;
    }

    public CaseDataBuilder atStateClaimDetailsNotifiedWithNoticeOfChangeOtherSol1Lip() {
        atStateClaimDetailsNotified();
        changeOfRepresentation(true, false, "New-sol-id", null, null);
        respondent1OrganisationPolicy = null;
        return this;
    }

    public CaseDataBuilder atStateClaimDetailsNotifiedWithNoticeOfChangeOtherSol2Lip() {
        atStateClaimDetailsNotified();
        changeOfRepresentation(true, false, "New-sol-id", null, null);
        respondent2OrganisationPolicy = null;
        return this;
    }

    public CaseDataBuilder atStateProceedsOfflineUnrepresentedDefendant1UnregisteredDefendant2() {
        atStatePendingClaimIssuedUnrepresentedDefendant();
        addRespondent2 = YES;
        respondent2 = PartyBuilder.builder().individual().build().toBuilder().partyID("res-2-party-id").build();
        ccdState = PROCEEDS_IN_HERITAGE_SYSTEM;
        takenOfflineDate = LocalDateTime.now();
        respondent2Represented = YES;
        respondent2OrgRegistered = NO;
        respondent2SameLegalRepresentative = NO;

        respondent1OrganisationPolicy = new OrganisationPolicy()
            .setOrgPolicyCaseAssignedRole("[RESPONDENTSOLICITORONE]");
        respondent2OrganisationPolicy = new OrganisationPolicy()
            .setOrgPolicyCaseAssignedRole("[RESPONDENTSOLICITORTWO]");

        respondent1Represented = NO;
        respondent1OrgRegistered = null;

        respondentSolicitor2OrganisationDetails = new SolicitorOrganisationDetails()
            .setEmail("testorg2@email.com")
            .setOrganisationName("test org name 2")
            .setFax("123123123")
            .setDx("test org dx 2")
            .setPhoneNumber("0123456789")
            .setAddress(AddressBuilder.defaults().build())
            ;
        return this;
    }

    public CaseDataBuilder atStateProceedsOfflineUnregisteredDefendant1UnrepresentedDefendant2() {
        respondent2 = PartyBuilder.builder().individual().build().toBuilder().partyID("res-2-party-id").build();
        atStatePendingClaimIssuedUnrepresentedDefendant();
        addRespondent2 = YES;
        ccdState = PROCEEDS_IN_HERITAGE_SYSTEM;
        takenOfflineDate = LocalDateTime.now();
        respondent1Represented = YES;
        respondent1OrgRegistered = NO;
        respondent2SameLegalRepresentative = NO;

        respondent1OrganisationPolicy = new OrganisationPolicy()
            .setOrgPolicyCaseAssignedRole("[RESPONDENTSOLICITORONE]");
        respondent2OrganisationPolicy = new OrganisationPolicy()
            .setOrgPolicyCaseAssignedRole("[RESPONDENTSOLICITORTWO]");

        respondent2OrgRegistered = null;

        respondentSolicitor1OrganisationDetails = new SolicitorOrganisationDetails()
            .setEmail("testorg2@email.com")
            .setOrganisationName("test org name 2")
            .setFax("123123123")
            .setDx("test org dx 2")
            .setPhoneNumber("0123456789")
            .setAddress(AddressBuilder.defaults().build())
            ;
        return this;
    }

    public CaseDataBuilder atStateClaimDiscontinued() {
        atStateClaimDetailsNotified();
        return discontinueClaim();
    }

    public CaseDataBuilder discontinueClaim() {
        this.ccdState = CASE_DISMISSED;
        this.discontinueClaim = new CloseClaim()
            .setDate(LocalDate.now())
            .setReason("My reason");
        return this;
    }

    public CaseDataBuilder discontinueClaim(CloseClaim closeClaim) {
        this.discontinueClaim = closeClaim;
        return this;
    }

    public CaseDataBuilder discontinueClaimFrom(FlowState.Main flowState) {
        atState(flowState);
        this.ccdState = CASE_DISMISSED;
        this.discontinueClaim = new CloseClaim()
            .setDate(LocalDate.now())
            .setReason("My reason");
        return this;
    }

    public CaseDataBuilder atStateClaimWithdrawn() {
        atStateClaimDetailsNotified();
        return withdrawClaim();
    }

    public CaseDataBuilder withdrawClaim(CloseClaim closeClaim) {
        this.withdrawClaim = closeClaim;
        return this;
    }

    public CaseDataBuilder withdrawClaim() {
        this.ccdState = CASE_DISMISSED;
        this.withdrawClaim = new CloseClaim()
            .setDate(LocalDate.now())
            .setReason("My reason");
        return this;
    }

    public CaseDataBuilder withdrawClaimFrom(FlowState.Main flowState) {
        atState(flowState);
        this.ccdState = CASE_DISMISSED;
        this.withdrawClaim = new CloseClaim()
            .setDate(LocalDate.now())
            .setReason("My reason");
        return this;
    }

    public CaseDataBuilder claimDismissedDeadline(LocalDateTime date) {
        this.claimDismissedDeadline = date;
        return this;
    }

    public CaseDataBuilder courtLocation_missing() {
        this.courtLocation = null;
        return this;
    }

    public CaseDataBuilder courtLocation_old() {
        this.courtLocation = new CourtLocation()
            .setApplicantPreferredCourt("127");
        return this;
    }

    public CaseDataBuilder courtLocation() {
        this.courtLocation = new CourtLocation()
            .setApplicantPreferredCourt("127")
            .setCaseLocation(new CaseLocationCivil()
                .setRegion("2")
                .setBaseLocation("000000")
                );
        return this;
    }

    public CaseDataBuilder atStateClaimDraftMock() {
        CaseDataBuilder caseDataBuilder = atStateClaimDraft();
        caseDataBuilder.caseManagementLocation(new CaseLocationCivil().setRegion("2").setBaseLocation("41112"));
        caseDataBuilder.applicant1DQ(new Applicant1DQ()
                                         .setApplicant1DQRequestedCourt(new RequestedCourt()
                                                                         .setResponseCourtCode("court4")
                                                                         .setCaseLocation(new CaseLocationCivil()
                                                                                              .setBaseLocation("dummy base")
                                                                                              .setRegion("dummy region"))
                                                                         .setResponseCourtName("testCourt")
                                                                         .setResponseCourtCode("0000")
                                                                         .setReasonForHearingAtSpecificCourt("reason")
                                                                         .setOtherPartyPreferredSite("site")));
        caseDataBuilder.respondent1DQ(new Respondent1DQ()
                                          .setRespondent1DQRequestedCourt(new RequestedCourt()
                                                                           .setResponseCourtCode("court4")
                                                                           .setCaseLocation(new CaseLocationCivil()
                                                                                                .setBaseLocation("dummy base")
                                                                                                .setRegion("dummy region"))
                                                                           .setResponseCourtName("testCourt")
                                                                           .setResponseCourtCode("0000")
                                                                           .setReasonForHearingAtSpecificCourt("reason")
                                                                           .setOtherPartyPreferredSite("site")));
        return caseDataBuilder;
    }

    public CaseDataBuilder atStateClaimDraft() {
        solicitorReferences = new SolicitorReferences()
            .setApplicantSolicitor1Reference("12345")
            .setRespondentSolicitor1Reference("6789")
            ;
        courtLocation = new CourtLocation()
            .setApplicantPreferredCourt("214320")
            .setApplicantPreferredCourtLocationList(
                DynamicList.builder().value(DynamicListElement.builder().label("sitename").build()).build())
            .setCaseLocation(new CaseLocationCivil()
                .setRegion("10")
                .setBaseLocation("214320")
                );
        uploadParticularsOfClaim = NO;
        claimValue = new ClaimValue()
            .setStatementOfValueInPennies(BigDecimal.valueOf(10000000));
        claimType = ClaimType.PERSONAL_INJURY;
        claimTypeUnSpec = ClaimTypeUnspec.CLINICAL_NEGLIGENCE;
        personalInjuryType = ROAD_ACCIDENT;
        applicantSolicitor1PbaAccounts = DynamicList.builder()
            .value(DynamicListElement.builder().label("PBA0077597").build())
            .build();
        claimFee = new Fee()
            .setVersion("1")
            .setCode("CODE")
            .setCalculatedAmountInPence(BigDecimal.valueOf(100))
            ;
        applicant1 = PartyBuilder.builder().individual().build().toBuilder().partyID("app-1-party-id").build();
        respondent1 = PartyBuilder.builder().soleTrader().build().toBuilder().partyID("res-1-party-id").build();
        respondent1Represented = YES;
        respondent1OrgRegistered = YES;
        respondent2OrgRegistered = YES;
        applicant1OrganisationPolicy = new OrganisationPolicy()
            .setOrganisation(new Organisation().setOrganisationID("QWERTY A"));
        respondent1OrganisationPolicy = new OrganisationPolicy()
            .setOrganisation(new Organisation().setOrganisationID("QWERTY R"))
            .setOrgPolicyCaseAssignedRole("[RESPONDENTSOLICITORONE]");
        respondent2OrganisationPolicy = new OrganisationPolicy()
            .setOrganisation(new Organisation().setOrganisationID("QWERTY R2"))
            .setOrgPolicyCaseAssignedRole("[RESPONDENTSOLICITORTWO]");
        respondentSolicitor1EmailAddress = "respondentsolicitor@example.com";
        respondentSolicitor2EmailAddress = "respondentsolicitor2@example.com";
        applicantSolicitor1UserDetails = new IdamUserDetails().setEmail("applicantsolicitor@example.com");
        applicantSolicitor1ClaimStatementOfTruth = StatementOfTruthBuilder.defaults();
        applicantSolicitor1CheckEmail = new CorrectEmail().setEmail("hmcts.civil@gmail.com").setCorrect(YES);
        return this;
    }

    public CaseDataBuilder atStateClaimDraftLip() {
        solicitorReferences = new SolicitorReferences()
            .setApplicantSolicitor1Reference("12345")
            .setRespondentSolicitor1Reference("6789")
            ;
        courtLocation = new CourtLocation()
            .setApplicantPreferredCourt("214320")
            .setApplicantPreferredCourtLocationList(
                DynamicList.builder().value(DynamicListElement.builder().label("sitename").build()).build())
            .setCaseLocation(new CaseLocationCivil()
                .setRegion("10")
                .setBaseLocation("214320")
                );
        uploadParticularsOfClaim = NO;
        claimValue = new ClaimValue()
            .setStatementOfValueInPennies(BigDecimal.valueOf(10000000));
        claimType = ClaimType.PERSONAL_INJURY;
        claimTypeUnSpec = ClaimTypeUnspec.CLINICAL_NEGLIGENCE;
        personalInjuryType = ROAD_ACCIDENT;
        applicantSolicitor1PbaAccounts = DynamicList.builder()
            .value(DynamicListElement.builder().label("PBA0077597").build())
            .build();
        claimFee = new Fee()
            .setVersion("1")
            .setCode("CODE")
            .setCalculatedAmountInPence(BigDecimal.valueOf(100))
            ;
        applicant1 = PartyBuilder.builder().individual().build().toBuilder().partyID("app-1-party-id").build();
        respondent1 = PartyBuilder.builder().soleTrader().build().toBuilder().partyID("res-1-party-id").build();
        respondent1Represented = YES;
        respondent1OrgRegistered = YES;
        respondent2OrgRegistered = YES;
        applicant1OrganisationPolicy = new OrganisationPolicy()
            .setOrganisation(new Organisation().setOrganisationID("QWERTY A"));
        respondent1OrganisationPolicy = new OrganisationPolicy()
            .setOrganisation(new Organisation().setOrganisationID("QWERTY R"))
            .setOrgPolicyCaseAssignedRole("[DEFENDANT]");
        respondent2OrganisationPolicy = new OrganisationPolicy()
            .setOrganisation(new Organisation().setOrganisationID("QWERTY R2"))
            .setOrgPolicyCaseAssignedRole("[RESPONDENTSOLICITORTWO]");
        respondentSolicitor1EmailAddress = "respondentsolicitor@example.com";
        respondentSolicitor2EmailAddress = "respondentsolicitor2@example.com";
        applicantSolicitor1UserDetails = new IdamUserDetails().setEmail("applicantsolicitor@example.com");
        applicantSolicitor1ClaimStatementOfTruth = StatementOfTruthBuilder.defaults();
        applicantSolicitor1CheckEmail = new CorrectEmail().setEmail("hmcts.civil@gmail.com").setCorrect(YES);
        return this;
    }

    public CaseDataBuilder multiPartyClaimTwoDefendantSolicitorsUnregistered() {
        atStateClaimDraft();
        respondent1OrganisationPolicy = null;
        respondent1Represented = YES;
        respondent1OrgRegistered = NO;

        addRespondent2 = YES;
        respondent2OrganisationPolicy = null;
        respondent2Represented = YES;
        respondent2OrgRegistered = NO;
        respondent2SameLegalRepresentative = NO;
        return this;
    }

    public CaseDataBuilder multiPartyClaimTwoDefendantLips() {
        atStateClaimDraft();
        respondent1OrganisationPolicy = null;
        defendant1LIPAtClaimIssued = YES;
        respondent1Represented = NO;
        respondent2Represented = NO;
        addRespondent2 = YES;
        respondent2OrganisationPolicy = null;
        respondent2SameLegalRepresentative = NO;
        defendant2LIPAtClaimIssued = YES;
        return this;
    }

    public CaseDataBuilder multiPartyClaimTwoDefendant1Lip1Lr() {
        atStateClaimDraft();
        respondent1OrganisationPolicy = null;
        defendant1LIPAtClaimIssued = YES;

        addRespondent2 = YES;
        respondent2OrganisationPolicy = null;
        respondent2SameLegalRepresentative = NO;
        defendant2LIPAtClaimIssued = NO;
        return this;
    }

    public CaseDataBuilder multiPartyClaimTwoDefendant1Lr1Lip() {
        atStateClaimDraft();
        respondent1OrganisationPolicy = null;
        defendant1LIPAtClaimIssued = NO;

        addRespondent2 = YES;
        respondent2OrganisationPolicy = null;
        respondent2SameLegalRepresentative = NO;
        defendant2LIPAtClaimIssued = YES;
        return this;
    }

    public CaseDataBuilder atStateClaimSubmitted() {
        atStateClaimDraft();
        legacyCaseReference = LEGACY_CASE_REFERENCE;
        allocatedTrack = FAST_CLAIM;
        ccdState = PENDING_CASE_ISSUED;
        ccdCaseReference = CASE_ID;
        submittedDate = SUBMITTED_DATE_TIME;
        claimIssuedPaymentDetails = new PaymentDetails().setCustomerReference("12345");
        return this;
    }

    public CaseDataBuilder atStateClaimSubmittedSmallClaim() {
        atStateClaimDraft();
        legacyCaseReference = LEGACY_CASE_REFERENCE;
        allocatedTrack = SMALL_CLAIM;
        ccdState = PENDING_CASE_ISSUED;
        ccdCaseReference = CASE_ID;
        submittedDate = SUBMITTED_DATE_TIME;
        totalClaimAmount = BigDecimal.valueOf(800);
        claimIssuedPaymentDetails = new PaymentDetails().setCustomerReference("12345");
        return this;
    }

    public CaseDataBuilder atStateClaimSubmittedMultiClaim() {
        atStateClaimDraft();
        legacyCaseReference = LEGACY_CASE_REFERENCE;
        allocatedTrack = MULTI_CLAIM;
        ccdState = PENDING_CASE_ISSUED;
        ccdCaseReference = CASE_ID;
        submittedDate = SUBMITTED_DATE_TIME;
        claimIssuedPaymentDetails = new PaymentDetails().setCustomerReference("12345");
        return this;
    }

    public CaseDataBuilder atStateClaimSubmittedSpec() {
        atStateClaimDraft();
        legacyCaseReference = LEGACY_CASE_REFERENCE;
        allocatedTrack = FAST_CLAIM;
        ccdState = PENDING_CASE_ISSUED;
        ccdCaseReference = CASE_ID;
        submittedDate = SUBMITTED_DATE_TIME;
        caseAccessCategory = SPEC_CLAIM;
        claimIssuedPaymentDetails = new PaymentDetails().setCustomerReference("12345");
        return this;
    }

    public CaseDataBuilder atStateClaimSubmittedOneRespondentRepresentative() {
        atStateClaimSubmitted();
        addRespondent2 = NO;
        defendant1LIPAtClaimIssued = NO;
        return this;
    }

    public CaseDataBuilder atStateClaimSubmittedTwoRespondentRepresentatives() {
        atStateClaimSubmitted();
        addRespondent2 = YES;
        respondent2SameLegalRepresentative = NO;
        return this;
    }

    public CaseDataBuilder atStateClaimSubmittedTwoRespondentRepresentativesBothUnregistered() {
        atStateClaimSubmitted();
        addRespondent2 = YES;
        respondent2SameLegalRepresentative = NO;
        respondent2Represented = YES;
        respondent1OrgRegistered = NO;
        respondent2OrgRegistered = NO;
        return this;
    }

    public CaseDataBuilder atStateClaimSubmittedRespondent1Unregistered() {
        atStateClaimSubmitted();
        respondent1OrgRegistered = NO;

        addRespondent2 = YES;
        respondent2SameLegalRepresentative = NO;
        respondent2Represented = YES;
        respondent2OrgRegistered = YES;
        return this;
    }

    public CaseDataBuilder atStateClaimSubmittedNoRespondentRepresented() {
        atStateClaimSubmitted();
        addRespondent2 = YES;
        respondent2SameLegalRepresentative = NO;
        respondent1OrgRegistered = null;
        respondent2OrgRegistered = null;
        respondent1Represented = NO;
        respondent2Represented = NO;
        respondent2 = PartyBuilder.builder().individual().build().toBuilder().partyID("res-2-party-id").build();
        return this;
    }

    public CaseDataBuilder atStateClaimSubmitted1v1AndNoRespondentRepresented() {
        atStateClaimSubmitted();
        addRespondent2 = NO;
        respondent2SameLegalRepresentative = NO;
        respondent1Represented = NO;
        respondent2Represented = NO;
        return this;
    }

    public CaseDataBuilder atStateClaimSubmitted1v2AndOnlyFirstRespondentIsRepresented() {
        atStateClaimSubmitted();
        addRespondent2 = YES;
        respondent2SameLegalRepresentative = NO;
        respondent1Represented = YES;
        respondent1OrgRegistered = YES;
        respondent2Represented = NO;
        respondent2 = PartyBuilder.builder().individual().build().toBuilder().partyID("res-2-party-id").build();
        return this;
    }

    public CaseDataBuilder atStateClaimSubmitted1v2AndOnlySecondRespondentIsRepresented() {
        atStateClaimSubmitted();
        addRespondent2 = YES;
        respondent2SameLegalRepresentative = NO;
        respondent1Represented = NO;
        respondent2Represented = YES;
        respondent2OrgRegistered = YES;
        respondent2 = PartyBuilder.builder().individual().build().toBuilder().partyID("res-2-party-id").build();
        return this;
    }

    public CaseDataBuilder atStateClaimIssued1v2AndSameRepresentative() {
        atStatePaymentSuccessful();
        respondent1Represented = YES;
        respondent1OrgRegistered = YES;
        addRespondent2 = YES;
        respondent2Represented = YES;
        respondent2SameLegalRepresentative = YES;
        respondent1OrganisationPolicy =
            new OrganisationPolicy()
                .setOrganisation(new Organisation().setOrganisationID("org1"))
                .setOrgPolicyCaseAssignedRole("[RESPONDENTSOLICITORONE]")
                .setOrgPolicyReference("org1PolicyReference");
        return this;
    }

    public CaseDataBuilder atStateClaimIssued1v2AndSameUnregisteredRepresentative() {
        atStateClaimIssued1v2AndSameRepresentative();
        respondent1OrgRegistered = NO;
        return this;
    }

    public CaseDataBuilder atStateClaimSubmitted2v1RespondentUnrepresented() {
        atStateClaimSubmitted();
        multiPartyClaimTwoApplicants();
        respondent1Represented = NO;
        respondent1OrgRegistered = null;
        return this;
    }

    public CaseDataBuilder atStateClaimSubmitted2v1RespondentRegistered() {
        atStateClaimSubmitted();
        multiPartyClaimTwoApplicants();
        respondent1Represented = YES;
        respondent1OrgRegistered = YES;
        return this;
    }

    public CaseDataBuilder atStateClaimSubmitted2v1RespondentUnregistered() {
        atStateClaimSubmitted();
        multiPartyClaimTwoApplicants();
        respondent1Represented = YES;
        respondent1OrgRegistered = NO;
        return this;
    }

    public CaseDataBuilder atStateClaimIssued1v2AndBothDefendantsDefaultJudgment() {
        defendantDetails = DynamicList.builder()
            .value(DynamicListElement.builder().label("Both Defendants").build())
            .build();
        return this;
    }

    public CaseDataBuilder atStateClaimIssued1v2AndOneDefendantDefaultJudgment() {
        defendantDetails = DynamicList.builder()
            .value(DynamicListElement.builder().label("Mr. Sole Trader").build())
            .build();
        return this;
    }

    public CaseDataBuilder atRespondToClaimWithSingleUnAvailabilityDate() {

        return this;
    }

    public CaseDataBuilder atStateSdoFastTrackTrial() {
        fastTrackHearingTime = new FastTrackHearingTime()
            .setHelpText1("If either party considers that the time estimate is insufficient, "
                + "they must inform the court within 7 days of the date of this order.")
            .setHearingDuration(FastTrackHearingTimeEstimate.ONE_HOUR)
            .setDateFrom(LocalDate.parse("2022-01-01"))
            .setDateTo(LocalDate.parse("2022-01-02"))
            .setDateToToggle(List.of(DateToShowToggle.SHOW));
        fastTrackOrderWithoutJudgement = new FastTrackOrderWithoutJudgement()
            .setInput(String.format("Each party has the right to apply "
                    + "to have this Order set aside or varied. Any such application must be "
                    + "received by the Court (together with the appropriate fee) by 4pm "
                    + "on %s.",
                LocalDate.parse("2022-01-30")));
        return this;
    }

    public CaseDataBuilder atStateSdoFastTrackCreditHire() {
        sdoR2FastTrackCreditHireDetails = new SdoR2FastTrackCreditHireDetails();
        sdoR2FastTrackCreditHireDetails.setInput2("The claimant must upload to the Digital Portal a witness statement addressing\n"
            + "a) the need to hire a replacement vehicle; and\n"
            + "b) impecuniosity");
        sdoR2FastTrackCreditHireDetails.setDate1(LocalDate.parse("2022-01-01"));
        sdoR2FastTrackCreditHireDetails.setInput3("A failure to comply with the paragraph above will result in the claimant being debarred from "
            + "asserting need or relying on impecuniosity as the case may be at the final hearing, "
            + "save with permission of the Trial Judge.");
        sdoR2FastTrackCreditHireDetails.setInput4("The parties are to liaise and use reasonable endeavours to agree the basic hire rate no "
            + "later than 4pm on");
        sdoR2FastTrackCreditHireDetails.setDate2(LocalDate.parse("2022-01-02"));
        sdoR2FastTrackCreditHire = new SdoR2FastTrackCreditHire();
        sdoR2FastTrackCreditHire.setInput1("If impecuniosity is alleged by the claimant and not admitted by the defendant, the claimant's "
            + "disclosure as ordered earlier in this Order must include:\n"
            + "a) Evidence of all income from all sources for a period of 3 months prior to the "
            + "commencement of hire until the earlier of:\n "
            + "     i) 3 months after cessation of hire\n"
            + "     ii) the repair or replacement of the claimant's vehicle\n"
            + "b) Copies of all bank, credit card, and saving account statements for a period of 3 months "
            + "prior to the commencement of hire until the earlier of:\n"
            + "     i) 3 months after cessation of hire\n"
            + "     ii) the repair or replacement of the claimant's vehicle\n"
            + "c) Evidence of any loan, overdraft or other credit facilities available to the claimant.");
        sdoR2FastTrackCreditHire.setInput5("If the parties fail to agree rates subject to liability and/or other issues pursuant to the "
            + "paragraph above, each party may rely upon written evidence by way of witness statement of "
            + "one witness to provide evidence of basic hire rates available within the claimant's "
            + "geographical location, from a mainstream supplier, or a local reputable supplier if none "
            + "is available.");
        sdoR2FastTrackCreditHire.setInput6("The defendant's evidence is to be uploaded to the Digital Portal by 4pm on");
        sdoR2FastTrackCreditHire.setDate3(LocalDate.parse("2022-01-01"));
        sdoR2FastTrackCreditHire.setInput7("and the claimant's evidence in reply if so advised to be uploaded by 4pm on");
        sdoR2FastTrackCreditHire.setDate4(LocalDate.parse("2022-01-02"));
        sdoR2FastTrackCreditHire.setInput8("This witness statement is limited to 10 pages per party, including any appendices.");
        sdoR2FastTrackCreditHire.setDetailsShowToggle(List.of(AddOrRemoveToggle.ADD));
        sdoR2FastTrackCreditHire.setSdoR2FastTrackCreditHireDetails(sdoR2FastTrackCreditHireDetails);
        return this;
    }

    public CaseDataBuilder atStateSdoDisposal() {
        disposalOrderWithoutHearing = new DisposalOrderWithoutHearing()
            .setInput(String.format(
                "Each party has the right to apply to have this Order set "
                    + "aside or varied. Any such application must be received "
                    + "by the Court (together with the appropriate fee) "
                    + "by 4pm on %s.", LocalDate.parse("2022-01-30")));
        disposalHearingHearingTime = new DisposalHearingHearingTime()
            .setInput("This claim will be listed for final disposal before a judge on the first available date after")
            .setTime(FIFTEEN_MINUTES)
            .setDateFrom(LocalDate.parse("2022-01-02"));
        return this;
    }

    public CaseDataBuilder atStateClaimIssuedDisposalHearing() {
        caseManagementOrderSelection = DISPOSAL_HEARING;

        disposalHearingJudgesRecitalDJ = new DisposalHearingJudgesRecitalDJ()
            .setJudgeNameTitle("test name");
        return this;
    }

    public CaseDataBuilder atStateClaimIssuedTrialHearing() {
        caseManagementOrderSelection = "TRIAL_HEARING";

        trialHearingJudgesRecitalDJ = new TrialHearingJudgesRecital()
            .setJudgeNameTitle("test name");
        return this;
    }

    public CaseDataBuilder atStateClaimantRequestsDJWithUnavailableDates() {
        HearingDates singleDate = new HearingDates()
            .setHearingUnavailableFrom(LocalDate.of(2023, 8, 20))
            .setHearingUnavailableUntil(LocalDate.of(2023, 8, 20))
            ;

        HearingDates dateRange = new HearingDates()
            .setHearingUnavailableFrom(LocalDate.of(2023, 8, 20))
            .setHearingUnavailableUntil(LocalDate.of(2023, 8, 22))
            ;

        this.hearingSupportRequirementsDJ = new HearingSupportRequirementsDJ()
            .setHearingUnavailableDates(YES)
            .setHearingDates(wrapElements(List.of(singleDate, dateRange)))
            ;

        this.defaultJudgmentDocuments.addAll(wrapElements(new CaseDocument()
            .setDocumentName("test")
            .setCreatedDatetime(LocalDateTime.now())));
        return this;
    }

    private DynamicList getHearingMethodList(String key, String value) {
        Category category = new Category()
            .setCategoryKey("HearingChannel")
            .setKey(key)
            .setValueEn(value)
            .setActiveFlag("Y");
        DynamicList hearingMethodList = DynamicList.fromList(List.of(category), Category::getValueEn, null, false);
        hearingMethodList.setValue(hearingMethodList.getListItems().get(0));
        return hearingMethodList;
    }

    public CaseDataBuilder atStateClaimIssuedDisposalHearingSDOInPersonHearing() {
        DynamicList hearingMethodList = getHearingMethodList("INTER", "In Person");
        hearingMethodValuesDisposalHearing = hearingMethodList;
        return this;
    }

    public CaseDataBuilder atStateClaimIssuedDisposalHearingSDOTelephoneHearing() {
        DynamicList hearingMethodList = getHearingMethodList("TEL", "Telephone");
        hearingMethodValuesDisposalHearing = hearingMethodList;
        return this;
    }

    public CaseDataBuilder atStateClaimIssuedDisposalHearingSDOVideoHearing() {
        DynamicList hearingMethodList = getHearingMethodList("VID", "Video");
        hearingMethodValuesDisposalHearing = hearingMethodList;
        return this;
    }

    public CaseDataBuilder atStateClaimIssuedFastTrackSDOInPersonHearing() {
        DynamicList hearingMethodList = getHearingMethodList("INTER", "In Person");
        hearingMethodValuesFastTrack = hearingMethodList;
        return this;
    }

    public CaseDataBuilder atStateClaimIssuedFastTrackSDOTelephoneHearing() {
        DynamicList hearingMethodList = getHearingMethodList("TEL", "Telephone");
        hearingMethodValuesFastTrack = hearingMethodList;
        return this;
    }

    public CaseDataBuilder atStateClaimIssuedFastTrackSDOVideoHearing() {
        DynamicList hearingMethodList = getHearingMethodList("VID", "Video");
        hearingMethodValuesFastTrack = hearingMethodList;
        return this;
    }

    public CaseDataBuilder atStateClaimIssuedSmallClaimsSDOInPersonHearing() {
        DynamicList hearingMethodList = getHearingMethodList("INTER", "In Person");
        hearingMethodValuesSmallClaims = hearingMethodList;
        return this;
    }

    public CaseDataBuilder atStateClaimIssuedSmallClaimsSDOTelephoneHearing() {
        DynamicList hearingMethodList = getHearingMethodList("TEL", "Telephone");
        hearingMethodValuesSmallClaims = hearingMethodList;
        return this;
    }

    public CaseDataBuilder atStateClaimIssuedSmallClaimsSDOVideoHearing() {
        DynamicList hearingMethodList = getHearingMethodList("VID", "Video");
        hearingMethodValuesSmallClaims = hearingMethodList;
        return this;
    }

    public CaseDataBuilder atStateClaimIssuedTrialSDOInPersonHearingNew() {
        DynamicList hearingMethodList = getHearingMethodList("INTER", "In Person");
        hearingMethodValuesTrialHearingDJ = hearingMethodList;
        return this;
    }

    public CaseDataBuilder atStateClaimIssuedTrialDJInPersonHearingNew() {
        DynamicList hearingMethodList = getHearingMethodList("INTER", "In Person");
        hearingMethodValuesTrialHearingDJ = hearingMethodList;
        return this;
    }

    public CaseDataBuilder atStateClaimIssuedTrialSDOInPersonHearing() {
        trialHearingMethodDJ = DisposalHearingMethodDJ.disposalHearingMethodInPerson;
        return this;
    }

    public CaseDataBuilder atStateClaimIssuedTrialSDOTelephoneHearingNew() {
        DynamicList hearingMethodList = getHearingMethodList("TEL", "Telephone");
        hearingMethodValuesTrialHearingDJ = hearingMethodList;
        return this;
    }

    public CaseDataBuilder atStateClaimIssuedTrialSDOTelephoneHearing() {
        trialHearingMethodDJ = DisposalHearingMethodDJ.disposalHearingMethodTelephoneHearing;
        return this;
    }

    public CaseDataBuilder atStateClaimIssuedTrialSDOVideoHearingNew() {
        DynamicList hearingMethodList = getHearingMethodList("VID", "Video");
        hearingMethodValuesTrialHearingDJ = hearingMethodList;
        return this;
    }

    public CaseDataBuilder atStateClaimIssuedTrialSDOVideoHearing() {
        trialHearingMethodDJ = DisposalHearingMethodDJ.disposalHearingMethodVideoConferenceHearing;
        return this;
    }

    public CaseDataBuilder atStateClaimIssuedTrialLocationInPerson() {
        trialHearingMethodInPersonDJ = DynamicList.builder().value(
            DynamicListElement.builder().label("Court 1").build()).build();
        return this;
    }

    public CaseDataBuilder atStateClaimIssuedCaseManagementLocationInPerson() {
        caseManagementLocation = new CaseLocationCivil().setBaseLocation("0123").setRegion("0321");
        return this;
    }

    public CaseDataBuilder atStateSdoTrialDj() {
        List<DateToShowToggle> dateToShowTrue = List.of(DateToShowToggle.SHOW);
        trialHearingTimeDJ = new TrialHearingTimeDJ()
            .setHelpText1("If either party considers that the time estimate is insufficient, "
                + "they must inform the court within 7 days of the date of this order.")
            .setHelpText2("Not more than seven nor less than three clear days before the trial, "
                + "the claimant must file at court and serve an indexed and paginated bundle of "
                + "documents which complies with the requirements of Rule 39.5 Civil Procedure Rules "
                + "and which complies with requirements of PD32. The parties must endeavour to agree "
                + "the contents of the bundle before it is filed. The bundle will include a case "
                + "summary and a chronology.")
            .setHearingTimeEstimate(TrialHearingTimeEstimateDJ.ONE_HOUR)
            .setDateToToggle(dateToShowTrue)
            .setDate1(LocalDate.now().plusWeeks(22))
            .setDate2(LocalDate.now().plusWeeks(30));
        trialOrderMadeWithoutHearingDJ = new TrialOrderMadeWithoutHearingDJ()
            .setInput("This order has been made without a hearing. "
                + "Each party has the right to apply to have this Order "
                + "set aside or varied. Any such application must be "
                + "received by the Court "
                + "(together with the appropriate fee) by 4pm on 01 12 2022.");
        return this;
    }

    public CaseDataBuilder atStateClaimIssuedTrialHearingInfo() {
        trialHearingTrialDJ = new TrialHearingTrial()
            .setInput1("The time provisionally allowed for the trial is")
            .setDate1(LocalDate.now().plusWeeks(22))
            .setDate2(LocalDate.now().plusWeeks(34))
            .setInput2("If either party considers that the time estimates is"
                + " insufficient, they must inform the court within "
                + "7 days of the date of this order.")
            .setInput3(FAST_TRACK_TRIAL_BUNDLE_NOTICE + " ")
            .setType(List.of(DisposalHearingBundleType.DOCUMENTS));
        return this;
    }

    public CaseDataBuilder atStateClaimIssuedDisposalHearingInPerson() {
        disposalHearingBundleDJ = new DisposalHearingBundleDJ()
            .setInput("The claimant must lodge at court at least 7 "
                + "days before the disposal")
            .setType(List.of(DisposalHearingBundleType.DOCUMENTS));
        disposalHearingFinalDisposalHearingDJ = new DisposalHearingFinalDisposalHearingDJ()
            .setInput(DISPOSAL_FINAL_HEARING_LISTING_DJ)
            .setDate(LocalDate.now().plusWeeks(16))
            .setTime(DisposalHearingFinalDisposalHearingTimeEstimate.THIRTY_MINUTES);
        return this;
    }

    public CaseDataBuilder atStateClaimIssuedDisposalHearingInPersonDJ() {
        disposalHearingFinalDisposalHearingTimeDJ = new DisposalHearingFinalDisposalHearingTimeDJ()
            .setInput("This claim be listed for final "
                + "disposal before a Judge on the first "
                + "available date after.")
            .setDate(LocalDate.now().plusWeeks(16))
            .setTime(uk.gov.hmcts.reform.civil.enums.sdo.DisposalHearingFinalDisposalHearingTimeEstimate.THIRTY_MINUTES);
        return this;
    }

    public CaseDataBuilder atStateClaimIssuedDisposalSDOInPerson() {
        DynamicList hearingMethodList = getHearingMethodList("INTER", "In Person");
        hearingMethodValuesDisposalHearingDJ = hearingMethodList;
        return this;
    }

    public CaseDataBuilder atStateClaimIssuedDisposalSDOTelephoneCall() {
        DynamicList hearingMethodList = getHearingMethodList("TEL", "Telephone");
        hearingMethodValuesDisposalHearingDJ = hearingMethodList;
        return this;
    }

    public CaseDataBuilder atStateClaimIssuedDisposalSDOVideoCallNew() {
        DynamicList hearingMethodList = getHearingMethodList("VID", "Video");
        hearingMethodValuesDisposalHearingDJ = hearingMethodList;
        return this;
    }

    public CaseDataBuilder atStateClaimIssuedDisposalDJVideoCallNew() {
        DynamicList hearingMethodList = getHearingMethodList("VID", "Video");
        hearingMethodValuesDisposalHearingDJ = hearingMethodList;
        return this;
    }

    public CaseDataBuilder atStateClaimIssuedDisposalSDOVideoCall() {
        disposalHearingMethodDJ = DisposalHearingMethodDJ.disposalHearingMethodVideoConferenceHearing;
        return this;
    }

    public CaseDataBuilder atStateClaimSubmitted1v2Respondent2OrgNotRegistered() {
        atStateClaimIssued();
        respondent1Represented = YES;
        respondent1OrgRegistered = YES;
        addRespondent2 = YES;
        respondent2SameLegalRepresentative = NO;
        respondent2Represented = YES;
        respondent2OrgRegistered = NO;
        return this;
    }

    public CaseDataBuilder atStateClaimIssuedPaymentFailed() {
        atStateClaimSubmitted();

        claimIssuedPaymentDetails = new PaymentDetails()
            .setStatus(FAILED)
            .setErrorMessage("Your account is deleted")
            .setErrorCode("CA-E0004")
            ;
        return this;
    }

    public CaseDataBuilder atStatePaymentFailed() {
        atStateClaimSubmitted();

        paymentDetails = new PaymentDetails()
            .setStatus(FAILED)
            .setErrorMessage("Your account is deleted")
            .setErrorCode("CA-E0004")
            ;
        return this;
    }

    public CaseDataBuilder atStatePaymentSuccessful() {
        atStateClaimSubmitted();
        claimIssuedPaymentDetails = new PaymentDetails()
            .setStatus(SUCCESS)
            .setReference("RC-1604-0739-2145-4711")
            ;
        paymentReference = "12345";
        paymentSuccessfulDate = LocalDateTime.now();
        return this;
    }

    public CaseDataBuilder atStatePaymentSuccessfulWithoutPaymentSuccessDate() {
        atStateClaimSubmitted();
        claimIssuedPaymentDetails = new PaymentDetails()
            .setStatus(SUCCESS)
            .setReference("RC-1604-0739-2145-4711")
            ;
        paymentReference = "12345";
        return this;
    }

    public CaseDataBuilder atStatePaymentSuccessfulWithCopyOrganisationOnly() {
        atStatePaymentSuccessful();
        respondent1OrganisationIDCopy = respondent1OrganisationPolicy.getOrganisation().getOrganisationID();
        respondent1OrganisationPolicy = new OrganisationPolicy()
            .setOrganisation(new Organisation())
            .setOrgPolicyReference(respondent1OrganisationPolicy.getOrgPolicyReference())
            .setOrgPolicyCaseAssignedRole(respondent1OrganisationPolicy.getOrgPolicyCaseAssignedRole())
            .setPreviousOrganisations(respondent1OrganisationPolicy.getPreviousOrganisations());
        return this;
    }

    public CaseDataBuilder atStatePendingClaimIssued() {
        atStatePaymentSuccessful();
        issueDate = CLAIM_ISSUED_DATE;
        return this;
    }

    public CaseDataBuilder atStatePendingClaimIssuedUnregisteredDefendant() {
        atStatePaymentSuccessful();
        issueDate = CLAIM_ISSUED_DATE;
        respondent1Represented = YES;
        respondent1OrgRegistered = NO;
        respondent2OrgRegistered = NO;
        respondent1OrganisationPolicy = new OrganisationPolicy()
            .setOrgPolicyCaseAssignedRole("[RESPONDENTSOLICITORONE]");
        respondent2OrganisationPolicy = new OrganisationPolicy()
            .setOrgPolicyCaseAssignedRole("[RESPONDENTSOLICITORTWO]");
        return this;
    }

    public CaseDataBuilder atStatePendingClaimIssuedUnrepresentedDefendant() {
        atStatePaymentSuccessful();
        issueDate = CLAIM_ISSUED_DATE;
        respondent1Represented = NO;
        respondent1OrgRegistered = NO;
        respondent2Represented = NO;
        respondent2OrgRegistered = NO;

        respondent1OrganisationPolicy = new OrganisationPolicy()
            .setOrgPolicyCaseAssignedRole("[RESPONDENTSOLICITORONE]");
        respondent2OrganisationPolicy = new OrganisationPolicy()
            .setOrgPolicyCaseAssignedRole("[RESPONDENTSOLICITORTWO]");
        return this;
    }

    public CaseDataBuilder atStatePendingClaimIssuedUnrepresentedUnregisteredDefendant() {
        atStatePaymentSuccessful();
        issueDate = CLAIM_ISSUED_DATE;
        respondent1Represented = YES;
        respondent1OrgRegistered = NO;
        respondent2Represented = NO;
        respondent2OrgRegistered = NO;
        respondent2 = PartyBuilder.builder().individual().build().toBuilder().partyID("res-2-party-id").build();
        respondent1OrganisationPolicy = new OrganisationPolicy()
            .setOrgPolicyCaseAssignedRole("[RESPONDENTSOLICITORONE]");
        respondent2OrganisationPolicy = new OrganisationPolicy()
            .setOrgPolicyCaseAssignedRole("[RESPONDENTSOLICITORTWO]");
        return this;
    }

    public CaseDataBuilder atStateClaimIssued() {
        atStatePendingClaimIssued();
        claimNotificationDeadline = NOTIFICATION_DEADLINE;
        ccdState = CASE_ISSUED;
        buildHmctsInternalCaseName();
        return this;
    }

    public CaseDataBuilder atStateClaimIssuedCompanyClaimant() {
        atStatePendingClaimIssued();
        claimNotificationDeadline = NOTIFICATION_DEADLINE;
        ccdState = CASE_ISSUED;
        applicant1 = PartyBuilder.builder().company().build().toBuilder().partyID("app-1-party-id").build();
        buildHmctsInternalCaseName();
        return this;
    }

    public CaseDataBuilder atStateClaimIssued1v1LiP() {
        atStatePendingClaimIssued();
        ccdState = CASE_ISSUED;
        respondent1Represented = NO;
        respondent1OrganisationPolicy = new OrganisationPolicy()
            .setOrgPolicyCaseAssignedRole(CaseRole.RESPONDENTSOLICITORONE.getFormattedName());
        addLegalRepDeadline = DEADLINE;
        return this;
    }

    public CaseDataBuilder atStateClaimIssued1v1LiPBilingual() {
        atStateClaimIssued1v1LiP();
        this.applicant1Represented = NO;
        this.claimantBilingualLanguagePreference = Language.BOTH.toString();
        this.caseDataLiP = new CaseDataLiP()
            .setRespondent1LiPResponse(new RespondentLiPResponse().setRespondent1ResponseLanguage(Language.BOTH.toString()));
        setClaimTypeToSpecClaim();
        return this;
    }

    public CaseDataBuilder atStateClaimIssued1v2Respondent2LiP() {
        atStatePendingClaimIssued();
        ccdState = CASE_ISSUED;
        respondent2Represented = NO;
        respondent2OrganisationPolicy = new OrganisationPolicy()
            .setOrgPolicyCaseAssignedRole(CaseRole.RESPONDENTSOLICITORTWO.getFormattedName());
        addLegalRepDeadline = DEADLINE;
        return this;
    }

    public CaseDataBuilder changeOrganisationRequestField(boolean isApplicant, boolean isRespondent2Replaced,
                                                          String newOrgID, String oldOrgId, String email) {
        String caseRole = isApplicant ? CaseRole.APPLICANTSOLICITORONE.getFormattedName() :
            isRespondent2Replaced ? CaseRole.RESPONDENTSOLICITORTWO.getFormattedName() :
                CaseRole.RESPONDENTSOLICITORONE.getFormattedName();
        ChangeOrganisationRequest request = new ChangeOrganisationRequest();
        request.setRequestTimestamp(LocalDateTime.now());
        request.setCreatedBy(email);
        request.setCaseRoleId(DynamicList.builder()
            .value(DynamicListElement.builder()
                .code(caseRole)
                .label(caseRole)
                .build())
            .build());
        request.setOrganisationToAdd(new Organisation().setOrganisationID(newOrgID));
        request.setOrganisationToRemove(new Organisation().setOrganisationID(oldOrgId));
        request.setApprovalStatus(ChangeOrganisationApprovalStatus.APPROVED);
        changeOrganisationRequest = request;
        return this;
    }

    public CaseDataBuilder changeOfRepresentation(boolean isApplicant, boolean isRespondent2Replaced,
                                                  String newOrgID, String oldOrgId, String formerSolicitorEmail) {
        String caseRole = isApplicant ? CaseRole.APPLICANTSOLICITORONE.getFormattedName() :
            isRespondent2Replaced ? CaseRole.RESPONDENTSOLICITORTWO.getFormattedName() :
                CaseRole.RESPONDENTSOLICITORONE.getFormattedName();
        ChangeOfRepresentation newChange = new ChangeOfRepresentation()
            .setCaseRole(caseRole)
            .setOrganisationToAddID(newOrgID)
            .setOrganisationToRemoveID(oldOrgId)
            .setTimestamp(LocalDateTime.now());
        if (formerSolicitorEmail != null) {
            newChange.setFormerRepresentationEmailAddress(formerSolicitorEmail);
        }
        changeOfRepresentation = newChange;
        return this;
    }

    public CaseDataBuilder updateOrgPolicyAfterNoC(boolean isApplicant, boolean isRespondent2, String newOrgId) {
        if (isApplicant) {
            applicant1OrganisationPolicy = new OrganisationPolicy()
                .setOrganisation(new Organisation().setOrganisationID(newOrgId))
                .setOrgPolicyCaseAssignedRole(CaseRole.APPLICANTSOLICITORONE.getFormattedName());
        } else {
            if (isRespondent2) {
                respondent2OrganisationPolicy = new OrganisationPolicy()
                    .setOrganisation(new Organisation().setOrganisationID(newOrgId))
                    .setOrgPolicyCaseAssignedRole(CaseRole.RESPONDENTSOLICITORTWO.getFormattedName());
            } else {
                respondent1OrganisationPolicy = new OrganisationPolicy()
                    .setOrganisation(new Organisation().setOrganisationID(newOrgId))
                    .setOrgPolicyCaseAssignedRole(CaseRole.RESPONDENTSOLICITORONE.getFormattedName());
            }
        }
        return this;
    }

    public CaseDataBuilder atStateClaimNotified() {
        atStateClaimIssued();
        claimNotificationDate = issueDate.plusDays(1).atStartOfDay();
        claimDetailsNotificationDeadline = DEADLINE;
        ccdState = AWAITING_CASE_DETAILS_NOTIFICATION;
        servedDocumentFiles = new ServedDocumentFiles().setParticularsOfClaimText("test");
        return this;
    }

    public CaseDataBuilder atStateClaimNotified_1v1() {
        atStateClaimNotified();
        defendantSolicitorNotifyClaimOptions = null;
        return this;
    }

    public CaseDataBuilder atStateClaimNotified_1v2_andNotifyBothSolicitors() {
        atStateClaimNotified();
        multiPartyClaimTwoDefendantSolicitors();
        defendantSolicitorNotifyClaimOptions("Both");
        return this;
    }

    public CaseDataBuilder atStateClaimNotified_1v2_andNotifyOnlyOneSolicitor() {
        atStateClaimNotified();
        multiPartyClaimTwoDefendantSolicitors();
        defendantSolicitorNotifyClaimOptions("Defendant One: Solicitor A");
        return this;
    }

    public CaseDataBuilder atStateClaimNotified1v1LiP(CertificateOfService certificateOfService) {
        atStatePendingClaimIssued();
        ccdState = CASE_ISSUED;
        respondent1Represented = NO;
        respondent1OrganisationPolicy = new OrganisationPolicy()
            .setOrgPolicyCaseAssignedRole(CaseRole.RESPONDENTSOLICITORONE.getFormattedName());
        defendant1LIPAtClaimIssued = YES;
        legacyCaseReference = LEGACY_CASE_REFERENCE;
        cosNotifyClaimDefendant1 = certificateOfService;
        claimDetailsNotificationDeadline = DEADLINE;
        return this;
    }

    public CaseDataBuilder atStateClaimNotified1v2RespondentLiP() {
        atStatePendingClaimIssued();
        ccdState = CASE_ISSUED;
        respondent2Represented = NO;
        respondent2OrganisationPolicy = new OrganisationPolicy()
            .setOrgPolicyCaseAssignedRole(CaseRole.RESPONDENTSOLICITORTWO.getFormattedName());
        legacyCaseReference = LEGACY_CASE_REFERENCE;
        claimDetailsNotificationDeadline = DEADLINE;
        defendant2LIPAtClaimIssued = YES;
        return this;
    }

    public CaseDataBuilder atStateProceedsOfflineAfterClaimNotified() {
        atStateClaimNotified_1v2_andNotifyOnlyOneSolicitor();
        ccdState = PROCEEDS_IN_HERITAGE_SYSTEM;
        return this;
    }

    public CaseDataBuilder atStateProceedsOfflineAfterClaimDetailsNotified() {
        atStateClaimDetailsNotified_1v2_andNotifyOnlyOneSolicitor();
        ccdState = PROCEEDS_IN_HERITAGE_SYSTEM;
        return this;
    }

    public CaseDataBuilder atStateClaimDetailsNotified() {
        atStateClaimNotified();
        claimDetailsNotificationDate = claimNotificationDate.plusDays(1);
        claimDismissedDeadline = LocalDateTime.now().plusMonths(6);
        respondent1ResponseDeadline = RESPONSE_DEADLINE;
        ccdState = AWAITING_RESPONDENT_ACKNOWLEDGEMENT;
        return this;
    }

    public CaseDataBuilder atStateClaimDetailsNotified1v1() {
        atStateClaimNotified();
        claimDetailsNotificationDate = claimNotificationDate.plusDays(1);
        claimDismissedDeadline = LocalDateTime.now().plusMonths(6);
        respondent1ResponseDeadline = RESPONSE_DEADLINE;
        ccdState = AWAITING_RESPONDENT_ACKNOWLEDGEMENT;
        respondent2OrgRegistered = null;
        respondent2Represented = null;
        addRespondent2 = null;
        return this;
    }

    public CaseDataBuilder atStatePastResponseDeadline() {
        atStateClaimDetailsNotified1v1();
        respondent1ResponseDeadline = LocalDateTime.now().minusDays(1);
        return this;
    }

    public CaseDataBuilder atStateClaimDetailsNotified_1v2_andNotifyBothSolicitors() {
        atStateClaimDetailsNotified();
        multiPartyClaimTwoDefendantSolicitors();
        defendantSolicitorNotifyClaimDetailsOptions("Both");
        return this;
    }

    public CaseDataBuilder atStateClaimDetailsNotified_1v2_andNotifyOnlyOneSolicitor() {
        atStateClaimDetailsNotified();
        multiPartyClaimTwoDefendantSolicitors();
        defendantSolicitorNotifyClaimDetailsOptions("Defendant One: Solicitor");
        return this;
    }

    public CaseDataBuilder atStateAwaitingResponseFullDefenceReceived() {
        atStateClaimDetailsNotified();
        respondent1ClaimResponseType = RespondentResponseType.FULL_DEFENCE;
        applicant1ResponseDeadline = APPLICANT_RESPONSE_DEADLINE;
        respondent1ResponseDate = claimDetailsNotificationDate.plusDays(1);
        respondent1ClaimResponseDocument = new ResponseDocument(DocumentBuilder.builder().setDocumentName("defendant-response.pdf").build());
        respondent1DQ();
        claimDetailsNotificationDate = claimNotificationDate.plusDays(1);
        claimDismissedDeadline = LocalDateTime.now().plusMonths(6);
        respondent1ResponseDeadline = RESPONSE_DEADLINE;
        ccdState = AWAITING_RESPONDENT_ACKNOWLEDGEMENT;
        return this;
    }

    public CaseDataBuilder atStateAwaitingResponseNotFullDefenceReceived() {
        atStateAwaitingResponseNotFullDefenceReceived(RespondentResponseType.FULL_ADMISSION);
        return this;
    }

    public CaseDataBuilder atStateAwaitingResponseNotFullDefenceReceived(RespondentResponseType responseType) {
        atStateClaimDetailsNotified();
        respondent1ClaimResponseType = responseType;
        applicant1ResponseDeadline = APPLICANT_RESPONSE_DEADLINE;
        respondent1ResponseDate = claimDetailsNotificationDate.plusDays(1);
        claimDetailsNotificationDate = claimNotificationDate.plusDays(1);
        claimDismissedDeadline = LocalDateTime.now().plusMonths(6);
        respondent1ResponseDeadline = RESPONSE_DEADLINE;
        ccdState = AWAITING_RESPONDENT_ACKNOWLEDGEMENT;
        return this;
    }

    public CaseDataBuilder atStateAddLitigationFriend_1v2_SameSolicitor() {
        atStateClaimDetailsNotified();
        multiPartyClaimOneDefendantSolicitor();
        addGenericRespondentLitigationFriend();
        return this;
    }

    public CaseDataBuilder atStateAddRespondent1LitigationFriend_1v2_SameSolicitor() {
        atStateClaimDetailsNotified();
        multiPartyClaimOneDefendantSolicitor();
        addRespondent1LitigationFriend();
        return this;
    }

    public CaseDataBuilder atStateAddRespondent2LitigationFriend_1v2_SameSolicitor() {
        atStateClaimDetailsNotified();
        multiPartyClaimOneDefendantSolicitor();
        addRespondent2LitigationFriend();
        return this;
    }

    public CaseDataBuilder atStateAddRespondent1LitigationFriend_1v2_DiffSolicitor() {
        return atStateAddRespondent1LitigationFriend_1v2_SameSolicitor()
            .respondent2SameLegalRepresentative(NO);
    }

    public CaseDataBuilder atStateAddRespondent2LitigationFriend_1v2_DiffSolicitor() {
        return atStateAddRespondent2LitigationFriend_1v2_SameSolicitor()
            .respondent2SameLegalRepresentative(NO);
    }

    public CaseDataBuilder atStateClaimDetailsNotifiedTimeExtension() {
        atStateClaimDetailsNotified();
        respondent1ResponseDeadline = RESPONSE_DEADLINE;
        respondent1TimeExtensionDate = claimDetailsNotificationDate.plusDays(1);
        respondentSolicitor1AgreedDeadlineExtension = LocalDate.now();
        respondent1ResponseDate = null;
        return this;
    }

    public CaseDataBuilder atState1v2DifferentSolicitorClaimDetailsRespondent1NotifiedTimeExtension() {
        atStateClaimDetailsNotifiedTimeExtension();
        respondent2 = PartyBuilder.builder().individual().build().toBuilder().partyID("res-2-party-id").build();
        respondent2SameLegalRepresentative = NO;
        return this;
    }

    public CaseDataBuilder atState1v2DifferentSolicitorClaimDetailsRespondent2NotifiedTimeExtension() {
        atStateClaimDetailsNotified();
        respondent2 = PartyBuilder.builder().individual().build().toBuilder().partyID("res-2-party-id").build();
        respondent2SameLegalRepresentative = NO;
        respondent2ResponseDeadline = RESPONSE_DEADLINE;
        respondent2TimeExtensionDate = claimDetailsNotificationDate.plusDays(1);
        respondentSolicitor2AgreedDeadlineExtension = LocalDate.now();
        return this;
    }

    public CaseDataBuilder atState1v2SameSolicitorClaimDetailsRespondentNotifiedTimeExtension() {
        atStateClaimDetailsNotifiedTimeExtension();
        respondent2 = PartyBuilder.builder().individual().build().toBuilder().partyID("res-2-party-id").build();
        respondent2SameLegalRepresentative = YES;
        return this;
    }

    public CaseDataBuilder atStateClaimDetailsNotifiedTimeExtension_Defendent2() {
        atStateClaimDetailsNotified();
        respondent2ResponseDeadline = RESPONSE_DEADLINE;
        respondent2TimeExtensionDate = claimDetailsNotificationDate.plusDays(1);
        respondentSolicitor2AgreedDeadlineExtension = LocalDate.now();
        return this;
    }

    public CaseDataBuilder atStateClaimDetailsNotifiedTimeExtension1v2() {
        atStateClaimDetailsNotified();
        respondent1ResponseDeadline = RESPONSE_DEADLINE;
        respondent1TimeExtensionDate = claimDetailsNotificationDate.plusDays(1);
        respondentSolicitor1AgreedDeadlineExtension = LocalDate.now();
        respondent2ResponseDeadline = RESPONSE_DEADLINE;
        respondent2TimeExtensionDate = claimDetailsNotificationDate.plusDays(1);
        respondentSolicitor2AgreedDeadlineExtension = LocalDate.now();
        return this;
    }

    public CaseDataBuilder atStateTakenOfflineByStaff() {
        atStateClaimIssued();
        takenOfflineByStaff();
        return this;
    }

    public CaseDataBuilder atStateTakenOfflineByStaffSpec() {
        atStateClaimIssued();
        takenOfflineByStaffSpec();
        return this;
    }

    public CaseDataBuilder atStateTakenOfflineByStaffSpec1v2SS() {
        atStateClaimIssued();
        multiPartyClaimTwoDefendantSameSolicitorsSpec();
        takenOfflineByStaffSpec();
        return this;
    }

    public CaseDataBuilder atStateTakenOfflineByStaffAfterClaimNotified() {
        atStateClaimNotified();
        takenOfflineByStaff();
        takenOfflineByStaffDate = claimNotificationDate.plusDays(1);
        return this;
    }

    public CaseDataBuilder atStateTakenOfflineByStaffAfterClaimDetailsNotified() {
        atStateClaimDetailsNotified();
        takenOfflineByStaff();
        return this;
    }

    public CaseDataBuilder atStateTakenOfflineByStaffAfterClaimDetailsNotifiedExtension() {
        atStateClaimDetailsNotifiedTimeExtension();
        takenOfflineByStaff();
        takenOfflineByStaffDate = respondent1TimeExtensionDate.plusDays(1);
        return this;
    }

    public CaseDataBuilder atStateTakenOfflineByStaffAfterNotificationAcknowledged() {
        atStateNotificationAcknowledged();
        takenOfflineByStaff();
        takenOfflineByStaffDate = respondent1AcknowledgeNotificationDate.plusDays(1);
        return this;
    }

    public CaseDataBuilder atStateTakenOfflineByStaffAfterDefendantResponse() {
        atStateRespondentFullDefenceAfterNotificationAcknowledgement();
        takenOfflineByStaff();
        takenOfflineByStaffDate = respondent1ResponseDate.plusDays(1);
        return this;
    }

    public CaseDataBuilder atStateTakenOfflineByStaffAfterNotificationAcknowledgeExtension() {
        atStateNotificationAcknowledgedRespondent1TimeExtension();
        takenOfflineByStaff();
        takenOfflineByStaffDate = respondent1TimeExtensionDate.plusDays(1);
        return this;
    }

    public CaseDataBuilder atStateTakenOfflineByStaffAfterNotificationAcknowledgeExtension1v2() {
        atStateNotificationAcknowledged1v2SameSolicitor();
        atStateClaimDetailsNotifiedTimeExtension1v2();
        multiPartyClaimTwoDefendantSolicitors();
        atStateNotificationAcknowledgedTimeExtension_1v2DS();
        takenOfflineByStaff();
        takenOfflineByStaffDate = respondent1TimeExtensionDate.plusDays(1);
        return this;
    }

    public CaseDataBuilder takenOfflineByStaff() {
        claimProceedsInCaseman = new ClaimProceedsInCaseman()
            .setDate(LocalDate.now())
            .setReason(ReasonForProceedingOnPaper.APPLICATION);
        takenOfflineByStaffDate = LocalDateTime.now();
        return this;
    }

    public CaseDataBuilder takenOfflineByStaffSpec() {
        claimProceedsInCasemanLR = new ClaimProceedsInCasemanLR()
            .setDate(LocalDate.now())
            .setReason(ReasonForProceedingOnPaper.APPLICATION);
        takenOfflineByStaffDate = LocalDateTime.now();
        return this;
    }

    public CaseDataBuilder atStateSpec1v1ClaimSubmitted() {
        submittedDate = LocalDateTime.now().plusDays(1);
        addRespondent2 = NO;
        atStatePaymentSuccessful();
        atStatePendingClaimIssued();
        atStateClaimIssued();
        respondent1ResponseDate = LocalDateTime.now();
        return this;
    }

    public CaseDataBuilder atStateSpec2v1ClaimSubmitted() {
        submittedDate = LocalDateTime.now().plusDays(1);
        addRespondent2 = NO;
        atStatePaymentSuccessful();
        atStatePendingClaimIssued();
        atStateClaimIssued();
        multiPartyClaimTwoApplicants();
        respondent1ResponseDate = LocalDateTime.now();
        return this;
    }

    public CaseDataBuilder atStateSpec1v2ClaimSubmitted() {
        submittedDate = LocalDateTime.now().plusDays(1);
        atStatePaymentSuccessful();
        atStatePendingClaimIssued();
        atStateClaimIssued();
        multiPartyClaimOneDefendantSolicitor();
        respondent1ResponseDate = LocalDateTime.now();
        return this;
    }

    public CaseDataBuilder atStateRespondent1v1FullAdmissionSpec() {
        respondent1ClaimResponseTypeForSpec = RespondentResponseTypeSpec.FULL_ADMISSION;
        return this;
    }

    public CaseDataBuilder atStateRespondent1v1FullDefenceSpec() {
        respondent1ClaimResponseTypeForSpec = RespondentResponseTypeSpec.FULL_DEFENCE;
        return this;
    }

    public CaseDataBuilder atStateRespondent2v1FullAdmission() {
        claimant1ClaimResponseTypeForSpec = RespondentResponseTypeSpec.FULL_ADMISSION;
        claimant2ClaimResponseTypeForSpec = RespondentResponseTypeSpec.FULL_ADMISSION;
        return this;
    }

    public CaseDataBuilder atStateRespondent2v1FullDefence() {
        claimant1ClaimResponseTypeForSpec = RespondentResponseTypeSpec.FULL_DEFENCE;
        claimant2ClaimResponseTypeForSpec = RespondentResponseTypeSpec.FULL_DEFENCE;
        return this;
    }

    public CaseDataBuilder atStateRespondent2v1PartAdmission() {
        claimant1ClaimResponseTypeForSpec = RespondentResponseTypeSpec.PART_ADMISSION;
        claimant2ClaimResponseTypeForSpec = RespondentResponseTypeSpec.PART_ADMISSION;
        return this;
    }

    public CaseDataBuilder atStateRespondent2v1CounterClaim() {
        claimant1ClaimResponseTypeForSpec = RespondentResponseTypeSpec.COUNTER_CLAIM;
        claimant2ClaimResponseTypeForSpec = RespondentResponseTypeSpec.COUNTER_CLAIM;
        return this;
    }

    public CaseDataBuilder atStateRespondent2v1SecondFullDefence_FirstPartAdmission() {
        claimant1ClaimResponseTypeForSpec = RespondentResponseTypeSpec.PART_ADMISSION;
        claimant2ClaimResponseTypeForSpec = RespondentResponseTypeSpec.FULL_DEFENCE;
        return this;
    }

    public CaseDataBuilder atStateRespondent2v1FirstFullDefence_SecondPartAdmission() {
        claimant1ClaimResponseTypeForSpec = RespondentResponseTypeSpec.FULL_DEFENCE;
        claimant2ClaimResponseTypeForSpec = RespondentResponseTypeSpec.PART_ADMISSION;
        return this;
    }

    public CaseDataBuilder atStateRespondent2v1BothNotFullDefence_PartAdmissionX2() {
        claimant1ClaimResponseTypeForSpec = RespondentResponseTypeSpec.PART_ADMISSION;
        claimant2ClaimResponseTypeForSpec = RespondentResponseTypeSpec.PART_ADMISSION;
        return this;
    }

    public CaseDataBuilder atStateBothClaimantv1BothNotFullDefence_PartAdmissionX2() {
        claimant1ClaimResponseTypeForSpec = RespondentResponseTypeSpec.PART_ADMISSION;
        claimant2ClaimResponseTypeForSpec = RespondentResponseTypeSpec.FULL_ADMISSION;
        return this;
    }

    public CaseDataBuilder atStateRespondent2v1BothNotFullDefence_CounterClaimX2() {
        claimant1ClaimResponseTypeForSpec = RespondentResponseTypeSpec.COUNTER_CLAIM;
        claimant2ClaimResponseTypeForSpec = RespondentResponseTypeSpec.COUNTER_CLAIM;
        return this;
    }

    public CaseDataBuilder atStateRespondent1v2FullAdmission() {
        respondent1ClaimResponseTypeForSpec = RespondentResponseTypeSpec.FULL_ADMISSION;
        respondent2ClaimResponseTypeForSpec = RespondentResponseTypeSpec.FULL_ADMISSION;
        return this;
    }

    public CaseDataBuilder atStateRespondent1v2AdmitAll_AdmitPart() {
        respondent1ClaimResponseTypeForSpec = RespondentResponseTypeSpec.FULL_ADMISSION;
        respondent2ClaimResponseTypeForSpec = RespondentResponseTypeSpec.PART_ADMISSION;
        return this;
    }

    public CaseDataBuilder atStateRespondent1v2FullDefence_AdmitPart() {
        respondent1ClaimResponseTypeForSpec = RespondentResponseTypeSpec.FULL_DEFENCE;
        respondent2ClaimResponseTypeForSpec = RespondentResponseTypeSpec.PART_ADMISSION;
        return this;
    }

    public CaseDataBuilder atStateRespondent1v2FullDefence_AdmitFull() {
        respondent1ClaimResponseTypeForSpec = RespondentResponseTypeSpec.FULL_DEFENCE;
        respondent2ClaimResponseTypeForSpec = RespondentResponseTypeSpec.FULL_ADMISSION;
        return this;
    }

    public CaseDataBuilder atStateRespondent1v2AdmintPart_FullDefence() {
        respondent1ClaimResponseTypeForSpec = RespondentResponseTypeSpec.PART_ADMISSION;
        respondent2ClaimResponseTypeForSpec = RespondentResponseTypeSpec.FULL_DEFENCE;
        return this;
    }

    public CaseDataBuilder atStateRespondentFullDefenceWithHearingSupport() {
        atStateRespondentRespondToClaim(RespondentResponseType.FULL_DEFENCE);
        respondent1DQ = new Respondent1DQ()
            .setRespondent1DQRequestedCourt(
                new RequestedCourt()
                    .setResponseCourtCode("121")
                    .setReasonForHearingAtSpecificCourt("test")
                    .setCaseLocation(new CaseLocationCivil()
                                         .setRegion("2")
                                         .setBaseLocation("000000")))
            .setRespondent1DQHearingSupport(new HearingSupport()
                .setRequirements(List.of(SupportRequirements.values()))
                .setLanguageToBeInterpreted("English")
                .setSignLanguageRequired("Spanish")
                .setOtherSupport("other support")
                .setSupportRequirements(YES)
                .setSupportRequirementsAdditional("additional support"));
        respondent1ClaimResponseDocument = new ResponseDocument(DocumentBuilder.builder().setDocumentName("defendant1-defence.pdf").build());
        respondent1ResponseDate = LocalDateTime.now().minusDays(1);
        return this;
    }

    public CaseDataBuilder atStateRespondentFullDefence() {
        atStateRespondentRespondToClaim(RespondentResponseType.FULL_DEFENCE);
        respondent1ClaimResponseDocument = new ResponseDocument(DocumentBuilder.builder().setDocumentName("defendant1-defence.pdf").build());
        respondent1DQWithLocation();
        respondent1ResponseDate = LocalDateTime.now().minusDays(1);
        return this;
    }

    public CaseDataBuilder atStateClaimantFullDefence() {
        atStateRespondentRespondToClaim(RespondentResponseType.FULL_DEFENCE);
        respondentSharedClaimResponseDocument = new ResponseDocument(DocumentBuilder.builder().setDocumentName("defendant1-defence.pdf").build());
        applicant1DQWithLocation();
        applicant1ResponseDate = LocalDateTime.now().minusDays(1);
        return this;
    }

    public CaseDataBuilder atStateRespondentFullDefenceRespondent2() {
        atStateRespondent2RespondToClaim(RespondentResponseType.FULL_DEFENCE);
        respondent2 = PartyBuilder.builder().individual().build().toBuilder().partyID("res-2-party-id").build();
        respondent2ClaimResponseDocument = new ResponseDocument(DocumentBuilder.builder().setDocumentName("defendant-response.pdf").build());
        respondent2DQWithLocation();
        respondent2ResponseDate = LocalDateTime.now().minusDays(1);
        return this;
    }

    public CaseDataBuilder atStateRespondent2RespondToClaim(RespondentResponseType respondentResponseType) {
        atStateNotificationAcknowledged();
        respondent2ClaimResponseType = respondentResponseType;
        applicant1ResponseDeadline = APPLICANT_RESPONSE_DEADLINE;
        ccdState = AWAITING_APPLICANT_INTENTION;
        return this;
    }

    public CaseDataBuilder atStateRespondentFullDefence_1v2_BothPartiesFullDefenceResponses() {
        atStateRespondentFullDefence();
        respondent2ResponseDeadline = RESPONSE_DEADLINE;
        respondent2ClaimResponseType = RespondentResponseType.FULL_DEFENCE;
        respondent2ResponseDate = LocalDateTime.now();
        respondent2ClaimResponseDocument = new ResponseDocument(DocumentBuilder.builder().setDocumentName("defendant2-defence.pdf").build());
        respondent2DQ();
        return this;
    }

    public CaseDataBuilder atStateRespondentFullDefence_1v2_Resp1FullDefenceAndResp2CounterClaim() {
        atStateRespondentFullDefence();
        respondent2ClaimResponseType = RespondentResponseType.COUNTER_CLAIM;
        respondent2ResponseDate = LocalDateTime.now();
        respondent2ClaimResponseDocument = new ResponseDocument(DocumentBuilder.builder().setDocumentName("defendant-response.pdf").build());
        respondent2DQ();
        return this;
    }

    public CaseDataBuilder atStateRespondentFullDefence_1v2_Resp1CounterClaimAndResp2FullDefence() {
        atStateRespondentFullDefence();
        defendantSingleResponseToBothClaimants = NO;
        respondent1ClaimResponseType = RespondentResponseType.COUNTER_CLAIM;
        respondent1ResponseDate = LocalDateTime.now();
        respondent2ClaimResponseType = RespondentResponseType.FULL_DEFENCE;
        respondent2ResponseDate = LocalDateTime.now();
        respondent2ClaimResponseDocument = new ResponseDocument(DocumentBuilder.builder().setDocumentName("defendant-response.pdf").build());
        respondent2DQ();
        return this;
    }

    public CaseDataBuilder atStateDivergentResponseWithFullDefence1v2SameSol_NotSingleDQ() {
        atStateRespondentFullDefence();
        respondent2ClaimResponseType = RespondentResponseType.COUNTER_CLAIM;
        respondent2ResponseDate = LocalDateTime.now();
        respondent2ClaimResponseDocument = new ResponseDocument(DocumentBuilder.builder().setDocumentName("defendant-response.pdf").build());
        respondent2DQ();
        respondentResponseIsSame(NO);

        return this;
    }

    public CaseDataBuilder atStateDivergentResponseWithRespondent2FullDefence1v2SameSol_NotSingleDQ() {
        atStateRespondentFullAdmission();
        respondent2ClaimResponseType = RespondentResponseType.FULL_ADMISSION;
        respondent2ResponseDate = LocalDateTime.now();
        respondent2ClaimResponseDocument = new ResponseDocument(DocumentBuilder.builder().setDocumentName("defendant-response.pdf").build());
        respondent2DQ();
        respondentResponseIsSame(NO);

        return this;
    }

    public CaseDataBuilder atStateDivergentResponse_1v2_Resp1FullAdmissionAndResp2CounterClaim() {
        atStateRespondentFullAdmission();
        respondent2ClaimResponseType = RespondentResponseType.COUNTER_CLAIM;
        respondent2ResponseDate = LocalDateTime.now();
        respondent2ClaimResponseDocument = new ResponseDocument(DocumentBuilder.builder().setDocumentName("defendant-response.pdf").build());
        return this;
    }

    public CaseDataBuilder atStateFullAdmission_1v2_BothRespondentSolicitorsSubmitFullAdmissionResponse() {
        atStateRespondentFullAdmission();
        respondent2ClaimResponseType = RespondentResponseType.FULL_ADMISSION;
        respondent2ResponseDate = LocalDateTime.now();
        respondent2ClaimResponseDocument = new ResponseDocument(DocumentBuilder.builder().setDocumentName("defendant-response.pdf").build());
        return this;
    }

    public CaseDataBuilder atStateApplicantRespondToDefenceAndNotProceed_1v2_DiffSol() {
        atStateApplicantRespondToDefenceAndNotProceed_1v2();
        respondent1ClaimResponseTypeForSpec = RespondentResponseTypeSpec.FULL_DEFENCE;
        respondent2ClaimResponseTypeForSpec = RespondentResponseTypeSpec.FULL_DEFENCE;
        return this;
    }

    public CaseDataBuilder atStateRespondentFullDefenceSpec_1v2_BothPartiesFullDefenceResponses() {
        atStateRespondentFullDefenceSpec();
        respondent1ClaimResponseTypeForSpec = RespondentResponseTypeSpec.FULL_DEFENCE;
        respondent2ClaimResponseTypeForSpec = RespondentResponseTypeSpec.FULL_DEFENCE;
        respondent2ResponseDate = LocalDateTime.now();
        respondent2ClaimResponseDocument = new ResponseDocument(DocumentBuilder.builder().setDocumentName("defendant2-defence.pdf").build());
        respondent2DQ();
        return this;
    }

    public CaseDataBuilder atStateRespondentFullDefenceSpec() {
        atStateRespondentRespondToClaimSpec(RespondentResponseTypeSpec.FULL_DEFENCE);
        respondent1ClaimResponseDocument = new ResponseDocument(DocumentBuilder.builder().setDocumentName("defendant1-defence.pdf").build());
        respondent1DQ();
        respondent1ResponseDate = LocalDateTime.now().plusDays(1);
        return this;
    }

    public CaseDataBuilder atStateRespondentRespondToClaimSpec(RespondentResponseTypeSpec respondentResponseTypeSpec) {
        atStateNotificationAcknowledged();
        respondent1ClaimResponseTypeForSpec = respondentResponseTypeSpec;
        applicant1ResponseDeadline = APPLICANT_RESPONSE_DEADLINE;
        respondent1ResponseDate = respondent1AcknowledgeNotificationDate.plusDays(1);
        ccdState = AWAITING_APPLICANT_INTENTION;
        return this;
    }

    public CaseDataBuilder atStateRespondentFullDefenceAfterNotificationAcknowledgement() {
        atStateRespondentRespondToClaim(RespondentResponseType.FULL_DEFENCE);
        respondent1ClaimResponseDocument = new ResponseDocument(DocumentBuilder.builder().setDocumentName("defendant-response.pdf").build());
        respondent1DQ();
        respondent1ResponseDate = respondent1AcknowledgeNotificationDate.plusDays(3);
        caseManagementLocation = new CaseLocationCivil().setBaseLocation("11111").setRegion("2");
        return this;
    }

    public CaseDataBuilder atStateTwoRespondentsFullDefenceAfterNotificationAcknowledgement() {
        atStateRespondentRespondToClaim1v2(RespondentResponseType.FULL_DEFENCE, RespondentResponseType.FULL_DEFENCE);
        respondent1ClaimResponseDocument = new ResponseDocument(DocumentBuilder.builder().setDocumentName("defendant-response.pdf").build());
        respondent2ClaimResponseDocument = new ResponseDocument(DocumentBuilder.builder().setDocumentName("defendant-response.pdf").build());
        respondent1DQ();
        respondent2DQ();
        respondent1ResponseDate = respondent1AcknowledgeNotificationDate.plusDays(1);
        respondent2ResponseDate = respondent2AcknowledgeNotificationDate.plusDays(1);
        return this;
    }

    public CaseDataBuilder atStateRespondentFullDefenceFastTrack() {
        atStateRespondentRespondToClaimFastTrack(RespondentResponseType.FULL_DEFENCE);
        respondent1ClaimResponseDocument = new ResponseDocument(DocumentBuilder.builder().setDocumentName("defendant-response.pdf").build());
        respondent1DQ();
        respondent1ResponseDate = LocalDateTime.now();
        return this;
    }

    public CaseDataBuilder atStateRespondentAdmitPartOfClaimFastTrack() {
        atStateRespondentRespondToClaimFastTrack(RespondentResponseType.PART_ADMISSION);
        respondent1ClaimResponseDocument = new ResponseDocument(DocumentBuilder.builder().setDocumentName("defendant-response.pdf").build());
        respondent1DQ();
        respondent1ResponseDate = LocalDateTime.now();
        respondent1ClaimResponseTypeForSpec = RespondentResponseTypeSpec.PART_ADMISSION;
        return this;
    }

    public CaseDataBuilder atStateRespondentFullDefenceAfterAcknowledgementTimeExtension() {
        atStateNotificationAcknowledgedRespondent1TimeExtension();
        respondent1ClaimResponseType = RespondentResponseType.FULL_DEFENCE;
        applicant1ResponseDeadline = APPLICANT_RESPONSE_DEADLINE;
        //respondent1ResponseDate = respondent1AcknowledgeNotificationDate.plusDays(1);
        ccdState = AWAITING_APPLICANT_INTENTION;
        respondent1ClaimResponseDocument = new ResponseDocument(DocumentBuilder.builder().setDocumentName("defendant-response.pdf").build());
        respondent1DQ();
        respondent1ResponseDate = respondent1AcknowledgeNotificationDate.plusDays(1);
        return this;
    }

    public CaseDataBuilder atStateRespondentFullDefenceAfterNotifyDetails() {
        atStateClaimDetailsNotified();
        respondent1ClaimResponseType = RespondentResponseType.FULL_DEFENCE;
        applicant1ResponseDeadline = APPLICANT_RESPONSE_DEADLINE;
        respondent1ResponseDate = LocalDateTime.now();
        ccdState = AWAITING_APPLICANT_INTENTION;
        respondent1ClaimResponseDocument = new ResponseDocument(DocumentBuilder.builder().setDocumentName("defendant-response.pdf").build());
        respondent1DQ();
        respondent1ResponseDate = LocalDateTime.now();
        return this;
    }

    public CaseDataBuilder atStateRespondentFullDefenceAfterNotifyClaimDetails() {
        atStateClaimDetailsNotified();
        respondent1ClaimResponseType = RespondentResponseType.FULL_DEFENCE;
        applicant1ResponseDeadline = APPLICANT_RESPONSE_DEADLINE;
        ccdState = AWAITING_APPLICANT_INTENTION;
        respondent1ClaimResponseDocument = new ResponseDocument(DocumentBuilder.builder().setDocumentName("defendant-response.pdf").build());
        respondent1DQ();
        respondent1ResponseDate = claimDetailsNotificationDate.plusDays(1);
        return this;
    }

    public CaseDataBuilder atStateRespondentFullDefenceAfterNotifyClaimDetailsAwaiting2ndRespondentResponse() {
        atStateClaimDetailsNotified();
        respondent1ClaimResponseType = RespondentResponseType.FULL_DEFENCE;
        respondent1ResponseDate = LocalDateTime.now();
        respondent1ClaimResponseDocument = new ResponseDocument(DocumentBuilder.builder().setDocumentName("defendant-response.pdf").build());
        respondent1DQ();
        respondent2ResponseDeadline = RESPONSE_DEADLINE.plusDays(2);
        return this;
    }

    public CaseDataBuilder atStateRespondentFullDefenceAfterNotifyClaimDetailsAwaiting1stRespondentResponse() {
        atStateClaimDetailsNotified();
        respondent2ClaimResponseType = RespondentResponseType.FULL_DEFENCE;
        respondent2ResponseDate = LocalDateTime.now();
        respondent2ClaimResponseDocument = new ResponseDocument(DocumentBuilder.builder().setDocumentName("defendant-response.pdf").build());
        respondent2DQ();
        return this;
    }

    public CaseDataBuilder atStateRespondentFullDefenceAfterNotifyClaimDetailsTimeExtension() {
        atStateClaimDetailsNotifiedTimeExtension();
        respondent1ClaimResponseType = RespondentResponseType.FULL_DEFENCE;
        applicant1ResponseDeadline = APPLICANT_RESPONSE_DEADLINE;
        respondent1ResponseDate = LocalDateTime.now();
        ccdState = AWAITING_APPLICANT_INTENTION;
        respondent1ClaimResponseDocument = new ResponseDocument(DocumentBuilder.builder().setDocumentName("defendant-response.pdf").build());
        respondent1DQ();
        respondent1ResponseDate = respondent1TimeExtensionDate.plusDays(1);
        return this;
    }

    public CaseDataBuilder atStateTwoRespondentsFullDefenceAfterNotifyClaimDetailsTimeExtension() {
        atStateClaimDetailsNotifiedTimeExtension1v2();
        applicant1ResponseDeadline = APPLICANT_RESPONSE_DEADLINE;
        respondent1ClaimResponseType = RespondentResponseType.FULL_DEFENCE;
        respondent1ResponseDate = LocalDateTime.now();
        respondent2ClaimResponseType = RespondentResponseType.FULL_DEFENCE;
        respondent2ResponseDate = LocalDateTime.now();
        ccdState = AWAITING_APPLICANT_INTENTION;
        respondent1ClaimResponseDocument = new ResponseDocument(DocumentBuilder.builder().setDocumentName("defendant-response.pdf").build());
        respondent1DQ();
        respondent1ResponseDate = respondent1TimeExtensionDate.plusDays(1);

        respondent2ClaimResponseDocument = new ResponseDocument(DocumentBuilder.builder().setDocumentName("defendant-response.pdf").build());
        respondent2DQ();
        respondent2ResponseDate = respondent1TimeExtensionDate.plusDays(1);
        return this;
    }

    public CaseDataBuilder atStateRespondentFullAdmission() {
        atStateRespondentRespondToClaim(RespondentResponseType.FULL_ADMISSION);
        takenOfflineDate = LocalDateTime.now();
        respondent1ResponseDate = LocalDateTime.now();
        return this;
    }

    public CaseDataBuilder atStateRespondentFullAdmissionSpec() {
        atStateRespondentRespondToClaimSpec(RespondentResponseTypeSpec.FULL_ADMISSION);
        takenOfflineDate = LocalDateTime.now();
        respondent1ResponseDate = LocalDateTime.now();
        return this;
    }

    public CaseDataBuilder atStateBothRespondentsSameResponse(RespondentResponseType respondentResponseType) {
        atStateClaimDetailsNotified();
        respondent1ClaimResponseType = respondentResponseType;
        respondent1ResponseDate = LocalDateTime.now().plusDays(3);
        respondent2Responds(respondentResponseType);
        respondent2ResponseDate = LocalDateTime.now().plusDays(4);
        return this;
    }

    public CaseDataBuilder atStateBothRespondentsSameResponse1v2SameSolicitor(RespondentResponseType responseType) {
        atStateClaimDetailsNotified();
        respondent1ClaimResponseType = responseType;
        respondent2ClaimResponseType = responseType;
        respondentResponseIsSame(YES);
        respondent1ResponseDate = LocalDateTime.now().plusDays(3);
        respondent2ResponseDate = respondent1ResponseDate;
        respondent2ClaimResponseIntentionType = respondent1ClaimResponseIntentionType;
        return this;
    }

    public CaseDataBuilder includesApplicantCitizenQuery(OffsetDateTime queryCreationDatetime) {
        CaseMessage applicantQuery = new CaseMessage();
        applicantQuery.setId("app-query-id");
        applicantQuery.setIsHearingRelated(YES);
        applicantQuery.setCreatedOn(queryCreationDatetime);
        List<Element<CaseMessage>> caseMessages = new ArrayList<>();
        caseMessages.add(Element.<CaseMessage>builder()
                                .id(UUID.randomUUID())
                                .value(applicantQuery)
                                .build());
        CaseQueriesCollection caseQueries = new CaseQueriesCollection();
        caseQueries.setPartyName("Claimant");
        caseQueries.setRoleOnCase("applicant-citizen");
        caseQueries.setCaseMessages(caseMessages);
        this.queries = caseQueries;
        return this;
    }

    public CaseDataBuilder includesApplicantCitizenQueryResponse(OffsetDateTime queryCreationDatetime) {
        includesApplicantCitizenQuery(queryCreationDatetime);
        CaseMessage applicantResponse = new CaseMessage();
        applicantResponse.setId("app-response-id");
        applicantResponse.setIsHearingRelated(NO);
        applicantResponse.setCreatedOn(queryCreationDatetime.plusHours(3));
        applicantResponse.setParentId("app-query-id");
        this.queries.setCaseMessages(
            Stream.concat(
                this.queries.getCaseMessages().stream(),
                List.of(Element.<CaseMessage>builder()
                            .id(UUID.randomUUID())
                            .value(applicantResponse).build()).stream()
            ).toList());
        return this;
    }

    public CaseDataBuilder includesApplicantCitizenQueryFollowUp(OffsetDateTime queryCreationDatetime) {
        includesApplicantCitizenQueryResponse(queryCreationDatetime);
        CaseMessage applicantFollowUp = new CaseMessage();
        applicantFollowUp.setId("app-followup-id");
        applicantFollowUp.setIsHearingRelated(NO);
        applicantFollowUp.setCreatedOn(queryCreationDatetime.plusHours(5));
        applicantFollowUp.setParentId("app-query-id");
        this.queries.setCaseMessages(
                Stream.concat(
                    this.queries.getCaseMessages().stream(),
                    List.of(Element.<CaseMessage>builder()
                                .id(UUID.randomUUID())
                                .value(applicantFollowUp).build()).stream()
                ).toList());
        return this;
    }

    public CaseDataBuilder includesRespondentCitizenQuery(OffsetDateTime queryCreationDatetime) {
        CaseMessage respondentQuery = new CaseMessage();
        respondentQuery.setId("res-query-id");
        respondentQuery.setIsHearingRelated(YES);
        respondentQuery.setCreatedOn(queryCreationDatetime);
        List<Element<CaseMessage>> caseMessages = new ArrayList<>();
        caseMessages.add(Element.<CaseMessage>builder()
                             .id(UUID.randomUUID())
                             .value(respondentQuery)
                             .build());
        CaseQueriesCollection caseQueries = new CaseQueriesCollection();
        caseQueries.setPartyName("Defendant");
        caseQueries.setRoleOnCase("respondent-citizen");
        caseQueries.setCaseMessages(caseMessages);
        this.queries = caseQueries;
        return this;
    }

    public CaseDataBuilder includesRespondentCitizenQueryResponse(OffsetDateTime queryCreationDatetime) {
        includesRespondentCitizenQuery(queryCreationDatetime);
        CaseMessage respondentResponse = new CaseMessage();
        respondentResponse.setId("res-response-id");
        respondentResponse.setIsHearingRelated(NO);
        respondentResponse.setCreatedOn(queryCreationDatetime.plusHours(3));
        respondentResponse.setParentId("res-query-id");
        this.queries.setCaseMessages(
            Stream.concat(
                this.queries.getCaseMessages().stream(),
                List.of(Element.<CaseMessage>builder()
                            .id(UUID.randomUUID())
                            .value(respondentResponse).build()).stream()
            ).toList());
        return this;
    }

    public CaseDataBuilder includesRespondentCitizenQueryFollowUp(OffsetDateTime queryCreationDatetime) {
        includesRespondentCitizenQueryResponse(queryCreationDatetime);
        CaseMessage respondentFollowUp = new CaseMessage();
        respondentFollowUp.setId("res-followup-id");
        respondentFollowUp.setIsHearingRelated(NO);
        respondentFollowUp.setCreatedOn(queryCreationDatetime.plusHours(5));
        respondentFollowUp.setParentId("res-query-id");
        this.queries.setCaseMessages(
                Stream.concat(
                    this.queries.getCaseMessages().stream(),
                    List.of(Element.<CaseMessage>builder()
                                .id(UUID.randomUUID())
                                .value(respondentFollowUp).build()).stream()
                ).toList());
        return this;
    }

    public CaseDataBuilder atState1v2SameSolicitorDivergentResponse(RespondentResponseType respondent1Response,
                                                                    RespondentResponseType respondent2Response) {
        atStateClaimDetailsNotified();
        respondent1ClaimResponseType = respondent1Response;
        respondent2Responds(respondent2Response);
        respondent1ResponseDate = LocalDateTime.now().plusDays(3);
        respondentResponseIsSame(NO);
        if (caseAccessCategory != SPEC_CLAIM) {
            // at least in spec claims, respondent2 response date is null by front-end
            respondent2ResponseDate = respondent1ResponseDate;
        } else {
            respondent1ClaimResponseTypeForSpec = RespondentResponseTypeSpec
                .valueOf(respondent1Response.name());
            respondent2ClaimResponseTypeForSpec = RespondentResponseTypeSpec
                .valueOf(respondent2ClaimResponseType.name());
        }
        return this;
    }

    public CaseDataBuilder atState1v2DivergentResponse(RespondentResponseType respondent1Response,
                                                       RespondentResponseType respondent2Response) {
        atStateClaimDetailsNotified();
        respondent1ClaimResponseType = respondent1Response;
        respondent1ResponseDate = LocalDateTime.now().plusDays(3);
        respondent2Responds(respondent2Response);
        respondent2ResponseDate = LocalDateTime.now().plusDays(4);
        return this;
    }

    public CaseDataBuilder atState1v2DivergentResponseSpec(RespondentResponseTypeSpec respondent1Response,
                                                           RespondentResponseTypeSpec respondent2Response) {
        respondent1ClaimResponseTypeForSpec = respondent1Response;
        respondent1ResponseDate = LocalDateTime.now().plusDays(1);
        respondent2RespondsSpec(respondent2Response);
        respondent2ResponseDate = LocalDateTime.now().plusDays(2);
        return this;
    }

    public CaseDataBuilder atStateRespondentFullAdmissionAfterNotificationAcknowledged() {
        atStateRespondentRespondToClaim(RespondentResponseType.FULL_ADMISSION);
        respondent1ResponseDate = respondent1AcknowledgeNotificationDate.plusDays(1);
        takenOfflineDate = LocalDateTime.now();
        return this;
    }

    public CaseDataBuilder addEnterBreathingSpace() {
        this.enter = new BreathingSpaceEnterInfo()
            .setType(BreathingSpaceType.STANDARD)
            .setReference("12345")
            .setStart(LocalDate.now());

        this.breathing = new BreathingSpaceInfo()
            .setEnter(this.enter);

        return this;
    }

    public CaseDataBuilder addEnterMentalHealthBreathingSpace() {
        this.enter = new BreathingSpaceEnterInfo()
            .setType(BreathingSpaceType.MENTAL_HEALTH)
            .setReference("12345")
            .setStart(LocalDate.now());

        this.breathing = new BreathingSpaceInfo()
            .setEnter(this.enter);

        return this;
    }

    public CaseDataBuilder addEnterMentalHealthBreathingSpaceNoOptionalData() {
        this.enter = new BreathingSpaceEnterInfo()
            .setType(BreathingSpaceType.MENTAL_HEALTH)
            .setReference(null)
            .setStart(null);

        this.breathing = new BreathingSpaceInfo()
            .setEnter(this.enter);

        return this;
    }

    public CaseDataBuilder addLiftBreathingSpace() {
        this.enter = new BreathingSpaceEnterInfo()
            .setType(BreathingSpaceType.STANDARD)
            .setReference("12345")
            .setStart(LocalDate.now());
        this.lift = new BreathingSpaceLiftInfo()
            .setExpectedEnd(LocalDate.now());

        this.breathing = new BreathingSpaceInfo()
            .setEnter(this.enter)
            .setLift(this.lift);

        return this;
    }

    public CaseDataBuilder addLiftBreathingSpaceWithoutOptionalData() {
        this.enter = new BreathingSpaceEnterInfo()
            .setType(BreathingSpaceType.STANDARD)
            .setReference(null)
            .setStart(null);
        this.lift = new BreathingSpaceLiftInfo()
            .setExpectedEnd(null);

        this.breathing = new BreathingSpaceInfo()
            .setEnter(this.enter)
            .setLift(this.lift);

        return this;
    }

    public CaseDataBuilder addLiftMentalBreathingSpace() {
        this.enter = new BreathingSpaceEnterInfo()
            .setType(BreathingSpaceType.MENTAL_HEALTH)
            .setReference("12345")
            .setStart(LocalDate.now());
        this.lift = new BreathingSpaceLiftInfo()
            .setExpectedEnd(LocalDate.now());

        this.breathing = new BreathingSpaceInfo()
            .setEnter(this.enter)
            .setLift(this.lift);

        return this;
    }

    public CaseDataBuilder addLiftMentalBreathingSpaceNoOptionalData() {
        this.enter = new BreathingSpaceEnterInfo()
            .setType(BreathingSpaceType.MENTAL_HEALTH)
            .setReference(null)
            .setStart(null);
        this.lift = new BreathingSpaceLiftInfo()
            .setExpectedEnd(null);

        this.breathing = new BreathingSpaceInfo()
            .setEnter(this.enter)
            .setLift(this.lift);

        return this;
    }

    public CaseDataBuilder addEnterBreathingSpaceWithoutOptionalData() {
        this.enter = new BreathingSpaceEnterInfo()
            .setType(BreathingSpaceType.STANDARD)
            .setReference(null)
            .setStart(null);

        this.breathing = new BreathingSpaceInfo()
            .setEnter(this.enter);

        return this;
    }

    public CaseDataBuilder atStateRespondentFullAdmissionAfterNotifyDetails() {
        atStateClaimDetailsNotified();
        respondent1ClaimResponseType = RespondentResponseType.FULL_ADMISSION;
        applicant1ResponseDeadline = APPLICANT_RESPONSE_DEADLINE;
        ccdState = AWAITING_APPLICANT_INTENTION;
        respondent1ResponseDate = claimDetailsNotificationDate.plusDays(1);
        takenOfflineDate = LocalDateTime.now();
        return this;
    }

    public CaseDataBuilder addEnterBreathingSpaceWithOnlyReferenceInfo() {
        this.enter = new BreathingSpaceEnterInfo()
            .setType(BreathingSpaceType.STANDARD)
            .setReference("12345")
            .setStart(null);

        this.breathing = new BreathingSpaceInfo()
            .setEnter(this.enter);

        return this;
    }

    public CaseDataBuilder atStateRespondentPartAdmission() {
        atStateRespondentRespondToClaim(RespondentResponseType.PART_ADMISSION);
        takenOfflineDate = LocalDateTime.now();
        respondent1ResponseDate = LocalDateTime.now();
        return this;
    }

    public CaseDataBuilder atStateRespondentPartAdmissionSpec() {
        atStateRespondentRespondToClaimSpec(RespondentResponseTypeSpec.PART_ADMISSION);
        takenOfflineDate = LocalDateTime.now();
        respondent1ResponseDate = LocalDateTime.now();
        return this;
    }

    public CaseDataBuilder atStateRespondentFullAdmissionAfterAcknowledgementTimeExtension() {
        atStateNotificationAcknowledgedRespondent1TimeExtension();
        respondent1ClaimResponseType = RespondentResponseType.FULL_ADMISSION;
        applicant1ResponseDeadline = APPLICANT_RESPONSE_DEADLINE;
        ccdState = AWAITING_APPLICANT_INTENTION;
        respondent1ClaimResponseDocument = new ResponseDocument(DocumentBuilder.builder().setDocumentName("defendant-response.pdf").build());
        respondent1ResponseDate = respondent1AcknowledgeNotificationDate.plusDays(1);
        return this;
    }

    public CaseDataBuilder atStateRespondentPartAdmissionAfterNotificationAcknowledgement() {
        atStateRespondentRespondToClaim(RespondentResponseType.PART_ADMISSION);
        takenOfflineDate = LocalDateTime.now();
        respondent1ResponseDate = respondent1AcknowledgeNotificationDate.plusDays(1);
        return this;
    }

    public CaseDataBuilder atStateRespondentPartAdmissionAfterNotifyDetails() {
        atStateClaimDetailsNotified();
        respondent1ClaimResponseType = RespondentResponseType.PART_ADMISSION;
        applicant1ResponseDeadline = APPLICANT_RESPONSE_DEADLINE;
        respondent1ResponseDate = claimDetailsNotificationDate.plusDays(1);
        ccdState = AWAITING_APPLICANT_INTENTION;
        takenOfflineDate = LocalDateTime.now();
        return this;
    }

    public CaseDataBuilder atStateRespondentPartAdmissionAfterAcknowledgementTimeExtension() {
        atStateNotificationAcknowledgedRespondent1TimeExtension();
        respondent1ClaimResponseType = RespondentResponseType.PART_ADMISSION;
        applicant1ResponseDeadline = APPLICANT_RESPONSE_DEADLINE;
        respondent1ResponseDate = respondent1AcknowledgeNotificationDate.plusDays(1);
        ccdState = AWAITING_APPLICANT_INTENTION;
        respondent1ClaimResponseDocument = new ResponseDocument(DocumentBuilder.builder().setDocumentName("defendant-response.pdf").build());
        return this;
    }

    public CaseDataBuilder atStateRespondentCounterClaim() {
        atStateRespondentRespondToClaim(RespondentResponseType.COUNTER_CLAIM);
        takenOfflineDate = LocalDateTime.now();
        respondent1ResponseDate = respondent1AcknowledgeNotificationDate.plusDays(1);
        return this;
    }

    public CaseDataBuilder atStateRespondentCounterClaimSpec() {
        atStateRespondentRespondToClaimSpec(RespondentResponseTypeSpec.COUNTER_CLAIM);
        takenOfflineDate = LocalDateTime.now();
        respondent1ResponseDate = respondent1AcknowledgeNotificationDate.plusDays(1);
        return this;
    }

    public CaseDataBuilder atStateRespondent1CounterClaimAfterNotifyDetails() {
        atStateClaimDetailsNotified();
        respondent1ClaimResponseType = RespondentResponseType.COUNTER_CLAIM;
        applicant1ResponseDeadline = APPLICANT_RESPONSE_DEADLINE;
        respondent1ResponseDate = claimDetailsNotificationDate.plusDays(1);
        ccdState = AWAITING_APPLICANT_INTENTION;
        takenOfflineDate = LocalDateTime.now();
        return this;
    }

    public CaseDataBuilder atStateRespondent2CounterClaimAfterNotifyDetails() {
        atStateClaimDetailsNotified();
        respondent2 = Party.builder().partyName("Respondent 2").build();
        respondent2ClaimResponseType = RespondentResponseType.COUNTER_CLAIM;
        applicant1ResponseDeadline = APPLICANT_RESPONSE_DEADLINE;
        respondent2ResponseDate = claimDetailsNotificationDate.plusDays(1);
        ccdState = AWAITING_APPLICANT_INTENTION;
        takenOfflineDate = LocalDateTime.now();
        return this;
    }

    public CaseDataBuilder atStateRespondentCounterClaimAfterAcknowledgementTimeExtension() {
        atStateNotificationAcknowledgedRespondent1TimeExtension();
        respondent1ClaimResponseType = RespondentResponseType.COUNTER_CLAIM;
        applicant1ResponseDeadline = APPLICANT_RESPONSE_DEADLINE;
        respondent1ResponseDate = respondent1AcknowledgeNotificationDate.plusDays(1);
        ccdState = AWAITING_APPLICANT_INTENTION;
        respondent1ClaimResponseDocument = new ResponseDocument(DocumentBuilder.builder().setDocumentName("defendant-response.pdf").build());
        return this;
    }

    public CaseDataBuilder atStateRespondent1FullAdmissionAfterNotifyDetails() {
        atStateClaimDetailsNotified();
        respondent1ClaimResponseType = RespondentResponseType.FULL_ADMISSION;
        applicant1ResponseDeadline = APPLICANT_RESPONSE_DEADLINE;
        respondent1ResponseDate = claimDetailsNotificationDate.plusDays(1);
        ccdState = AWAITING_APPLICANT_INTENTION;
        takenOfflineDate = LocalDateTime.now();
        return this;
    }

    public CaseDataBuilder atStateRespondent2FullAdmissionAfterNotifyDetails() {
        atStateClaimDetailsNotified();
        respondent2 = Party.builder().partyName("Respondent 2").build();
        respondent2ClaimResponseType = RespondentResponseType.FULL_ADMISSION;
        applicant1ResponseDeadline = APPLICANT_RESPONSE_DEADLINE;
        respondent2ResponseDate = claimDetailsNotificationDate.plusDays(1);
        ccdState = AWAITING_APPLICANT_INTENTION;
        takenOfflineDate = LocalDateTime.now();
        return this;
    }

    public CaseDataBuilder atStateRespondentRespondToClaim(RespondentResponseType respondentResponseType) {
        atStateNotificationAcknowledged();
        respondent1ClaimResponseType = respondentResponseType;
        applicant1ResponseDeadline = APPLICANT_RESPONSE_DEADLINE;
        respondent1ResponseDate = respondent1AcknowledgeNotificationDate.plusDays(1);
        ccdState = AWAITING_APPLICANT_INTENTION;
        return this;
    }

    public CaseDataBuilder atStateRespondentRespondToClaim1v2(RespondentResponseType respondent1ResponseType,
                                                              RespondentResponseType respondent2ResponseType) {
        atStateNotificationAcknowledged_1v2_BothDefendants();
        respondent1ClaimResponseType = respondent1ResponseType;
        respondent1ResponseDate = respondent1AcknowledgeNotificationDate.plusDays(1);
        respondent2ClaimResponseType = respondent2ResponseType;
        applicant1ResponseDeadline = APPLICANT_RESPONSE_DEADLINE;
        respondent2ResponseDate = respondent2AcknowledgeNotificationDate.plusDays(1);
        ccdState = AWAITING_APPLICANT_INTENTION;
        return this;
    }

    public CaseDataBuilder atStateProceedsOfflineAdmissionOrCounterClaim() {
        atStateRespondentFullDefenceAfterNotificationAcknowledgement();
        ccdState = PROCEEDS_IN_HERITAGE_SYSTEM;
        return this;
    }

    public CaseDataBuilder atStateRespondentRespondToClaimFastTrack(RespondentResponseType respondentResponseType) {
        atStateNotificationAcknowledged();
        respondToClaim = new RespondToClaim().setHowMuchWasPaid(FAST_TRACK_CLAIM_AMOUNT);
        totalClaimAmount = FAST_TRACK_CLAIM_AMOUNT;
        respondent1ClaimResponseType = respondentResponseType;
        applicant1ResponseDeadline = APPLICANT_RESPONSE_DEADLINE;
        respondent1ResponseDate = LocalDateTime.now();
        ccdState = AWAITING_APPLICANT_INTENTION;
        return this;
    }

    public CaseDataBuilder atStatePastClaimDismissedDeadline() {
        atStateClaimDetailsNotified();
        claimDismissedDeadline = LocalDateTime.now().minusDays(5);
        return this;
    }

    public CaseDataBuilder atStateHearingDateScheduled() {
        atStateHearingFeeDuePaid();
        hearingDate = LocalDate.now().plusWeeks(3).plusDays(1);
        hearingFeePaymentDetails = new PaymentDetails().setStatus(SUCCESS);
        ccdState = HEARING_READINESS;
        return this;
    }

    public CaseDataBuilder atStatePastClaimDismissedDeadline_1v2() {
        atStateClaimDetailsNotified_1v2_andNotifyBothSolicitors();
        claimDismissedDeadline = LocalDateTime.now().minusDays(5);
        return this;
    }

    public CaseDataBuilder atStateClaimDismissed() {
        atStatePastClaimDismissedDeadline();
        ccdState = CASE_DISMISSED;
        claimDismissedDate = LocalDateTime.now();
        return this;
    }

    public CaseDataBuilder atState2v1Applicant1NotProceedApplicant2Proceeds() {
        atStateRespondentFullDefenceAfterNotificationAcknowledgement();
        applicant1ProceedWithClaimMultiParty2v1 = NO;
        applicant2ProceedWithClaimMultiParty2v1 = YES;
        applicant2DefenceResponseDocument = new ResponseDocument(DocumentBuilder.builder().setDocumentName("claimant-response.pdf").build());
        applicant2DQ();
        applicant2ResponseDate = respondent1ResponseDate.plusDays(1);
        uiStatementOfTruth = new StatementOfTruth().setName("John Smith").setRole("Solicitor");
        return this;
    }

    public CaseDataBuilder atStateApplicantRespondToDefenceAndProceed() {
        return atStateApplicantRespondToDefenceAndProceed(ONE_V_ONE);
    }

    public CaseDataBuilder atStateApplicantRespondToDefenceAndProceed(MultiPartyScenario mpScenario) {
        atStateRespondentFullDefenceAfterNotificationAcknowledgement();
        applicant1DefenceResponseDocument = new ResponseDocument(DocumentBuilder.builder().setDocumentName("claimant-response.pdf").build());
        applicant1DQ();
        applicant1ResponseDate = respondent1ResponseDate.plusDays(2);
        uiStatementOfTruth = new StatementOfTruth().setName("John Smith").setRole("Solicitor");
        caseManagementLocation = new CaseLocationCivil().setBaseLocation("00000").setRegion("4");
        switch (mpScenario) {
            case ONE_V_TWO_ONE_LEGAL_REP: {
                respondent2SameLegalRepresentative = YES;
                return atStateRespondentFullDefenceRespondent2();
            }
            case ONE_V_TWO_TWO_LEGAL_REP: {
                respondent2SameLegalRepresentative = NO;
                return atStateRespondentFullDefenceRespondent2();
            }
            case ONE_V_ONE: {
                applicant1ProceedWithClaim = YES;
                return this;
            }
            default: {
                return this;
            }
        }
    }

    public CaseDataBuilder atStateTrialReadyCheck(MultiPartyScenario mpScenario) {
        atStateApplicantRespondToDefenceAndProceed(mpScenario);
        hearingDate = LocalDate.now().plusWeeks(5).plusDays(6);
        hearingDuration = MINUTES_120;
        ccdState = PREPARE_FOR_HEARING_CONDUCT_HEARING;

        if (mpScenario == ONE_V_TWO_TWO_LEGAL_REP) {
            solicitorReferences = new SolicitorReferences()
                .setApplicantSolicitor1Reference("123456")
                .setRespondentSolicitor1Reference("123456")
                .setRespondentSolicitor2Reference("123456");
            return this;
        }

        return this;
    }

    public CaseDataBuilder atStateTrialReadyCheck() {
        atStateHearingFeeDuePaid();
        ccdState = PREPARE_FOR_HEARING_CONDUCT_HEARING;
        hearingDate = LocalDate.now().plusWeeks(5).plusDays(5);
        hearingDuration = MINUTES_120;
        return this;
    }

    public CaseDataBuilder atCaseProgressionCheck() {
        atStateHearingFeeDuePaid();
        ccdState = CASE_PROGRESSION;
        hearingDate = LocalDate.now().plusWeeks(5).plusDays(5);
        hearingDuration = MINUTES_120;
        return this;
    }

    public CaseDataBuilder atAllFinalOrdersIssuedCheck() {
        atStateHearingFeeDuePaid();
        ccdState = All_FINAL_ORDERS_ISSUED;
        hearingDate = LocalDate.now().plusWeeks(5).plusDays(5);
        hearingDuration = MINUTES_120;
        return this;
    }

    public CaseDataBuilder atStateDecisionOutcome() {
        atStateRespondentFullDefenceSpec();
        hearingDate = LocalDate.now().plusWeeks(5).plusDays(5);
        hearingDuration = MINUTES_120;
        ccdState = PREPARE_FOR_HEARING_CONDUCT_HEARING;
        return this;
    }

    public CaseDataBuilder atStateTrialReadyCheckLiP(boolean hasEmailAddress) {
        atStateHearingFeeDuePaid().setClaimTypeToSpecClaim();
        respondent2 = PartyBuilder.builder().individual().build().toBuilder().partyID("res-2-party-id").build();
        if (!hasEmailAddress) {
            applicant1 = applicant1.toBuilder().partyEmail(null).build();
            respondent1 = respondent1.toBuilder().partyEmail("").build();
            respondent2 = respondent2.toBuilder().partyEmail("").build();
        }
        legacyCaseReference = "000MC001";
        ccdState = PREPARE_FOR_HEARING_CONDUCT_HEARING;
        hearingDate = LocalDate.now().plusWeeks(5).plusDays(5);
        hearingDuration = MINUTES_120;
        applicant1Represented = NO;
        respondent1Represented = NO;
        respondent2Represented = NO;
        return this;
    }

    public CaseDataBuilder atStateTrialReadyApplicant() {
        atStateTrialReadyCheck();
        trialReadyApplicant = YES;
        applicantRevisedHearingRequirements = new RevisedHearingRequirements()
            .setRevisedHearingRequirements(YES)
            .setRevisedHearingComments("Changes requested.");

        return this;
    }

    public CaseDataBuilder atStateTrialNotReadyApplicant() {
        atStateTrialReadyCheck();
        trialReadyApplicant = NO;
        applicantRevisedHearingRequirements = new RevisedHearingRequirements()
            .setRevisedHearingRequirements(YES)
            .setRevisedHearingComments("Changes requested.");

        return this;
    }

    public CaseDataBuilder atStateTrialReadyRespondent1() {
        atStateTrialReadyCheck();
        trialReadyRespondent1 = YES;
        respondent1RevisedHearingRequirements = new RevisedHearingRequirements()
            .setRevisedHearingRequirements(YES)
            .setRevisedHearingComments("Changes requested.");
        return this;
    }

    public CaseDataBuilder atStateTrialNotReadyRespondent1() {
        atStateTrialReadyCheck();
        trialReadyRespondent1 = NO;
        respondent1RevisedHearingRequirements = new RevisedHearingRequirements()
            .setRevisedHearingRequirements(YES)
            .setRevisedHearingComments("Changes requested.");
        return this;
    }

    public CaseDataBuilder atStateTrialReadyRespondent2() {
        atStateTrialReadyCheck();
        trialReadyRespondent2 = YES;
        applicantRevisedHearingRequirements = new RevisedHearingRequirements()
            .setRevisedHearingRequirements(YES)
            .setRevisedHearingComments("Changes requested.");
        return this;
    }

    public CaseDataBuilder atStateTrialNotReadyRespondent2() {
        atStateTrialReadyCheck();
        trialReadyRespondent2 = NO;
        applicantRevisedHearingRequirements = new RevisedHearingRequirements()
            .setRevisedHearingRequirements(YES)
            .setRevisedHearingComments("Changes requested.");
        return this;
    }

    public CaseDataBuilder atStateApplicantRespondToDefenceAndNotProceed() {
        atStateRespondentFullDefenceAfterNotificationAcknowledgement();
        applicant1ProceedWithClaim = NO;
        applicant1ResponseDate = respondent1ResponseDate.plusDays(1);
        uiStatementOfTruth = new StatementOfTruth().setName("John Smith").setRole("Solicitor");
        caseManagementLocation = new CaseLocationCivil().setBaseLocation("00000").setRegion("4");
        return this;
    }

    public CaseDataBuilder atState1v2SameSolicitorDivergentResponseSpec(RespondentResponseTypeSpec respondent1Response,
                                                                        RespondentResponseTypeSpec respondent2Response) {
        atStateNotificationAcknowledged();
        respondent1ClaimResponseTypeForSpec = respondent1Response;
        respondent2RespondsSpec(respondent2Response);
        respondent1ResponseDate = LocalDateTime.now().plusDays(1);
        respondentResponseIsSame(NO);
        respondent2ResponseDate = respondent1ResponseDate;
        applicant1ResponseDeadline = APPLICANT_RESPONSE_DEADLINE;
        return this;
    }

    public CaseDataBuilder atState1v2DifferentSolicitorDivergentResponseSpec(
        RespondentResponseTypeSpec respondent1Response,
        RespondentResponseTypeSpec respondent2Response) {
        atStateNotificationAcknowledged();
        respondent1ClaimResponseTypeForSpec = respondent1Response;
        respondent2RespondsSpec(respondent2Response);
        respondent1ResponseDate = LocalDateTime.now().plusDays(1);
        respondentResponseIsSame(NO);
        respondent2ResponseDate = respondent1ResponseDate;
        applicant1ResponseDeadline = APPLICANT_RESPONSE_DEADLINE;
        return this;
    }

    public CaseDataBuilder respondent2RespondsSpec(RespondentResponseTypeSpec responseType) {
        this.respondent2ClaimResponseTypeForSpec = responseType;
        this.respondent2ResponseDate = LocalDateTime.now().plusDays(1);
        return this;
    }

    public CaseDataBuilder atStateApplicantRespondToDefenceAndProceedVsBothDefendants_1v2() {
        atStateRespondentFullDefenceAfterNotificationAcknowledgement();
        applicant1ProceedWithClaimAgainstRespondent1MultiParty1v2 = YES;
        applicant1ProceedWithClaimAgainstRespondent2MultiParty1v2 = YES;
        applicant1DefenceResponseDocument = new ResponseDocument(DocumentBuilder.builder().setDocumentName("claimant-response.pdf").build());
        applicant1DQ();
        applicant1ResponseDate = respondent1ResponseDate.plusDays(1);
        respondent2ResponseDate = respondent1ResponseDate.plusDays(1);
        respondent2ClaimResponseIntentionType = FULL_DEFENCE;
        uiStatementOfTruth = new StatementOfTruth().setName("John Smith").setRole("Solicitor");
        return this;
    }

    public CaseDataBuilder setMultiTrackClaim() {
        allocatedTrack = MULTI_CLAIM;
        return this;
    }

    public CaseDataBuilder setIntermediateTrackClaim() {
        allocatedTrack = INTERMEDIATE_CLAIM;
        return this;
    }

    public CaseDataBuilder setFastTrackClaim() {
        allocatedTrack = FAST_CLAIM;
        return this;
    }

    public CaseDataBuilder setSmallTrackClaim() {
        allocatedTrack = SMALL_CLAIM;
        return this;
    }

    public CaseDataBuilder atStateApplicantRespondToDefenceAndProceedVsDefendant1Only_1v2() {
        atStateRespondentFullDefenceAfterNotificationAcknowledgement();
        applicant1ProceedWithClaimAgainstRespondent1MultiParty1v2 = YES;
        applicant1ProceedWithClaimAgainstRespondent2MultiParty1v2 = NO;
        applicant1DefenceResponseDocument = new ResponseDocument(DocumentBuilder.builder().setDocumentName("claimant-response.pdf").build());
        applicant1DQ();
        applicant1ResponseDate = respondent1ResponseDate.plusDays(1);
        respondent2ClaimResponseIntentionType = FULL_DEFENCE;
        uiStatementOfTruth = new StatementOfTruth().setName("John Smith").setRole("Solicitor");
        return this;
    }

    public CaseDataBuilder atStateApplicantRespondToDefenceAndProceedVsDefendant2Only_1v2() {
        atStateRespondentFullDefenceAfterNotificationAcknowledgement();
        applicant1ProceedWithClaimAgainstRespondent1MultiParty1v2 = NO;
        applicant1ProceedWithClaimAgainstRespondent2MultiParty1v2 = YES;
        applicant1DefenceResponseDocument = new ResponseDocument(DocumentBuilder.builder().setDocumentName("claimant-response.pdf").build());
        applicant1DQ();
        applicant1ResponseDate = respondent1ResponseDate.plusDays(1);
        respondent2ClaimResponseIntentionType = FULL_DEFENCE;
        uiStatementOfTruth = new StatementOfTruth().setName("John Smith").setRole("Solicitor");
        return this;
    }

    public CaseDataBuilder atStateApplicantRespondToDefenceAndNotProceed_1v2() {
        atStateRespondentFullDefenceAfterNotificationAcknowledgement();
        applicant1ProceedWithClaimAgainstRespondent1MultiParty1v2 = NO;
        applicant1ProceedWithClaimAgainstRespondent2MultiParty1v2 = NO;
        applicant1ResponseDate = respondent1ResponseDate.plusDays(1);
        uiStatementOfTruth = new StatementOfTruth().setName("John Smith").setRole("Solicitor");
        return this;
    }

    public CaseDataBuilder atStateBothApplicantsRespondToDefenceAndProceed_2v1() {
        atStateRespondentFullDefenceAfterNotificationAcknowledgement();
        respondent1ClaimResponseType = RespondentResponseType.FULL_DEFENCE;
        respondent1ClaimResponseTypeToApplicant2 = RespondentResponseType.FULL_DEFENCE;
        applicant1ProceedWithClaimMultiParty2v1 = YES;
        applicant2ProceedWithClaimMultiParty2v1 = YES;

        applicant1DefenceResponseDocument = new ResponseDocument(DocumentBuilder.builder().setDocumentName("claimant-response-1.pdf").build());
        applicant1DQ();
        applicant1ResponseDate = respondent1ResponseDate.plusDays(1);
        applicant2DefenceResponseDocument = new ResponseDocument(DocumentBuilder.builder().setDocumentName("claimant-response-1.pdf").build());
        applicant2DQ();
        applicant2ResponseDate = respondent1ResponseDate.plusDays(1);
        uiStatementOfTruth = new StatementOfTruth().setName("John Smith").setRole("Solicitor");
        return this;
    }

    public CaseDataBuilder atStateBothApplicantsRespondToDefenceAndProceed_2v1_SPEC() {
        atStateRespondentFullDefenceAfterNotificationAcknowledgement();
        defendantSingleResponseToBothClaimants = YES;
        respondent1ClaimResponseTypeForSpec = RespondentResponseTypeSpec.FULL_DEFENCE;
        applicant1ProceedWithClaimSpec2v1 = YES;
        applicant1ProceedWithClaimMultiParty2v1 = YES;
        applicant2ProceedWithClaimMultiParty2v1 = YES;

        applicant1DefenceResponseDocument = new ResponseDocument(DocumentBuilder.builder().setDocumentName("claimant-response-1.pdf").build());
        applicant1DQ();
        applicant1ResponseDate = respondent1ResponseDate.plusDays(1);
        applicant2DefenceResponseDocument = new ResponseDocument(DocumentBuilder.builder().setDocumentName("claimant-response-1.pdf").build());
        applicant2DQ();
        applicant2ResponseDate = respondent1ResponseDate.plusDays(1);
        uiStatementOfTruth = new StatementOfTruth().setName("John Smith").setRole("Solicitor");
        return this;
    }

    public CaseDataBuilder atStateApplicant1RespondToDefenceAndProceed_2v1() {
        atStateRespondentFullDefenceAfterNotificationAcknowledgement();
        respondent1ClaimResponseType = RespondentResponseType.FULL_DEFENCE;
        respondent1ClaimResponseTypeToApplicant2 = RespondentResponseType.FULL_DEFENCE;
        applicant1ProceedWithClaimMultiParty2v1 = YES;
        applicant2ProceedWithClaimMultiParty2v1 = NO;
        applicant1ResponseDate = respondent1ResponseDate.plusDays(1);
        applicant1DefenceResponseDocument = new ResponseDocument(DocumentBuilder.builder().setDocumentName("claimant-response-1.pdf").build());
        applicant1DQ();
        applicant2ResponseDate = respondent1ResponseDate.plusDays(1);
        uiStatementOfTruth = new StatementOfTruth().setName("John Smith").setRole("Solicitor");
        return this;
    }

    public CaseDataBuilder atStateApplicant2RespondToDefenceAndProceed_2v1() {
        atStateRespondentFullDefenceAfterNotificationAcknowledgement();
        respondent1ClaimResponseType = RespondentResponseType.FULL_DEFENCE;
        respondent1ClaimResponseTypeToApplicant2 = RespondentResponseType.FULL_DEFENCE;
        applicant1ProceedWithClaimMultiParty2v1 = NO;
        applicant1ResponseDate = respondent1ResponseDate.plusDays(1);
        applicant2ProceedWithClaimMultiParty2v1 = YES;
        applicant2DefenceResponseDocument = new ResponseDocument(DocumentBuilder.builder().setDocumentName("claimant-response-1.pdf").build());
        applicant2DQ();
        applicant2ResponseDate = respondent1ResponseDate.plusDays(1);
        uiStatementOfTruth = new StatementOfTruth().setName("John Smith").setRole("Solicitor");
        return this;
    }

    public CaseDataBuilder atStateApplicantRespondToDefenceAndNotProceed_2v1() {
        atStateRespondentFullDefenceAfterNotificationAcknowledgement();
        respondent1ClaimResponseType = RespondentResponseType.FULL_DEFENCE;
        respondent1ClaimResponseTypeToApplicant2 = RespondentResponseType.FULL_DEFENCE;
        applicant1ProceedWithClaimMultiParty2v1 = NO;
        applicant2ProceedWithClaimMultiParty2v1 = NO;
        applicant1ResponseDate = respondent1ResponseDate.plusDays(1);
        applicant2ResponseDate = respondent1ResponseDate.plusDays(1);
        uiStatementOfTruth = new StatementOfTruth().setName("John Smith").setRole("Solicitor");
        return this;
    }

    public CaseDataBuilder atStateNotificationAcknowledged1v2SameSolicitor() {
        atStateClaimDetailsNotified();
        respondent1ClaimResponseIntentionType = FULL_DEFENCE;
        respondent1AcknowledgeNotificationDate = claimDetailsNotificationDate.plusDays(1);
        respondent2ClaimResponseIntentionType = FULL_DEFENCE;
        respondent2AcknowledgeNotificationDate = claimDetailsNotificationDate.plusDays(1);
        respondent1ResponseDeadline = RESPONSE_DEADLINE;
        return this;
    }

    public CaseDataBuilder atStateNotificationAcknowledged() {
        atStateClaimDetailsNotified();
        respondent1ClaimResponseIntentionType = FULL_DEFENCE;
        respondent1AcknowledgeNotificationDate = claimDetailsNotificationDate.plusDays(1);
        respondent1ResponseDeadline = RESPONSE_DEADLINE;
        return this;
    }

    public CaseDataBuilder atReconsiderationUpheld() {
        decisionOnRequestReconsiderationOptions = DecisionOnRequestReconsiderationOptions.YES;
        upholdingPreviousOrderReason = new UpholdingPreviousOrderReason("Reason to upheld ");
        return this;
    }

    public CaseDataBuilder atStateDisposalHearingOrderMadeWithoutHearing() {
        disposalHearingOrderMadeWithoutHearingDJ =
            new DisposalHearingOrderMadeWithoutHearingDJ().setInput("test");
        return this;
    }

    public CaseDataBuilder atStateNotificationAcknowledged_1v2_BothDefendants() {
        atStateClaimDetailsNotified_1v2_andNotifyBothSolicitors();
        respondent1ClaimResponseIntentionType = FULL_DEFENCE;
        respondent1AcknowledgeNotificationDate = claimDetailsNotificationDate.plusDays(1);
        respondent1ResponseDeadline = RESPONSE_DEADLINE;
        respondent2ClaimResponseIntentionType = FULL_DEFENCE;
        respondent2AcknowledgeNotificationDate = claimDetailsNotificationDate.plusDays(1);
        respondent2ResponseDeadline = RESPONSE_DEADLINE;
        return this;
    }

    public CaseDataBuilder atStateNotificationAcknowledgedRespondent2() {
        atStateClaimDetailsNotified_1v2_andNotifyBothSolicitors();
        respondent2ClaimResponseIntentionType = FULL_DEFENCE;
        respondent2AcknowledgeNotificationDate = claimDetailsNotificationDate.plusDays(1);
        respondent2ResponseDeadline = RESPONSE_DEADLINE;
        return this;
    }

    public CaseDataBuilder atStateNotificationAcknowledgedRespondent2Only() {
        atStateNotificationAcknowledgedRespondent2();
        respondent1AcknowledgeNotificationDate = null;
        return this;
    }

    public CaseDataBuilder atDeadlinePassedAfterStateNotificationAcknowledged() {
        atStateNotificationAcknowledged();
        this.claimDismissedDate = respondent1AcknowledgeNotificationDate.plusDays(1);
        this.claimDismissedDeadline = LocalDateTime.now().minusDays(1);
        return this;
    }

    public CaseDataBuilder atDeadlinePassedAfterStateNotificationAcknowledgedTimeExtension() {
        atStateNotificationAcknowledgedRespondent1TimeExtension();
        this.claimDismissedDate = respondent1TimeExtensionDate.plusDays(1);
        this.claimDismissedDeadline = LocalDateTime.now().minusDays(1);
        return this;
    }

    public CaseDataBuilder atDeadlinePassedAfterStateClaimDetailsNotifiedExtension() {
        atStateClaimDetailsNotifiedTimeExtension();
        this.claimDismissedDate = respondent1TimeExtensionDate.plusDays(1);
        this.claimDismissedDeadline = LocalDateTime.now().minusDays(1);
        return this;
    }

    public CaseDataBuilder atDeadlinePassedAfterStateClaimDetailsNotified() {
        atStateClaimDismissedPastClaimDetailsNotificationDeadline();
        this.claimDismissedDeadline = LocalDateTime.now().minusDays(1);
        return this;
    }

    public CaseDataBuilder atStateNotificationAcknowledgedRespondent1TimeExtension(int numberOfHoursAfterCurrentDate) {
        atStateNotificationAcknowledged();
        respondent1TimeExtensionDate = respondent1AcknowledgeNotificationDate.plusHours(numberOfHoursAfterCurrentDate);
        respondentSolicitor1AgreedDeadlineExtension = LocalDate.now();
        respondent1ResponseDeadline = RESPONSE_DEADLINE;
        return this;
    }

    public CaseDataBuilder atStateNotificationAcknowledgedRespondent1TimeExtension() {
        return atStateNotificationAcknowledgedRespondent1TimeExtension(1);
    }

    public CaseDataBuilder atStateNotificationAcknowledgedTimeExtension_1v2DS() {
        atStateNotificationAcknowledged_1v2_BothDefendants();
        respondent1TimeExtensionDate = respondent1AcknowledgeNotificationDate.plusHours(1);
        respondentSolicitor1AgreedDeadlineExtension = LocalDate.now();
        respondent1ResponseDeadline = RESPONSE_DEADLINE;
        respondent2TimeExtensionDate = respondent2AcknowledgeNotificationDate.plusHours(1);
        respondentSolicitor2AgreedDeadlineExtension = LocalDate.now();
        respondent2ResponseDeadline = RESPONSE_DEADLINE;
        return this;
    }

    public CaseDataBuilder atStateNotificationAcknowledgedTimeExtensionRespondent1_1v2DS() {
        atStateNotificationAcknowledged_1v2_BothDefendants();
        respondent1TimeExtensionDate = respondent1AcknowledgeNotificationDate.plusHours(1);
        respondentSolicitor1AgreedDeadlineExtension = LocalDate.now();
        respondent1ResponseDeadline = RESPONSE_DEADLINE;
        respondentSolicitor2AgreedDeadlineExtension = LocalDate.now();
        respondent2ResponseDeadline = RESPONSE_DEADLINE;
        return this;
    }

    public CaseDataBuilder atStateNotificationAcknowledgedTimeExtensionRespondent2_1v2DS() {
        atStateNotificationAcknowledged_1v2_BothDefendants();
        respondentSolicitor1AgreedDeadlineExtension = LocalDate.now();
        respondent1ResponseDeadline = RESPONSE_DEADLINE;
        respondent2TimeExtensionDate = respondent2AcknowledgeNotificationDate.plusHours(1);
        respondentSolicitor2AgreedDeadlineExtension = LocalDate.now();
        respondent2ResponseDeadline = RESPONSE_DEADLINE;
        return this;
    }

    public CaseDataBuilder atStateNotificationAcknowledgedRespondent2TimeExtension(int numberOfHoursAfterCurrentDate) {
        atStateNotificationAcknowledged();
        addRespondent2 = YES;
        respondent2SameLegalRepresentative = NO;
        respondent2 = PartyBuilder.builder().individual().build().toBuilder().partyID("res-2-party-id").build();
        respondent2TimeExtensionDate = respondent1AcknowledgeNotificationDate.plusHours(numberOfHoursAfterCurrentDate);
        respondentSolicitor2AgreedDeadlineExtension = LocalDate.now();
        respondent2ResponseDeadline = RESPONSE_DEADLINE;
        return this;
    }

    public CaseDataBuilder atStateNotificationAcknowledgedRespondent2TimeExtension() {
        return atStateNotificationAcknowledgedRespondent2TimeExtension(5);
    }

    public CaseDataBuilder atStatePastApplicantResponseDeadline() {
        atStateRespondentFullDefenceAfterNotificationAcknowledgement();
        applicant1ResponseDeadline = LocalDateTime.now().minusDays(1);
        applicant1ResponseDate = null;
        return this;
    }

    public CaseDataBuilder atStateTakenOfflinePastApplicantResponseDeadline() {
        atStatePastApplicantResponseDeadline();
        takenOfflineDate = respondent1ResponseDate.plusDays(1);
        return this;
    }

    public CaseDataBuilder atStateTakenOfflineDefendant1NocDeadlinePassed() {
        atStateClaimIssued1v1UnrepresentedDefendant();

        takenOfflineDate = LocalDateTime.now().plusDays(1);
        addLegalRepDeadlineDefendant1 = LocalDateTime.now();
        return this;
    }

    public CaseDataBuilder atStateTakenOfflineDefendant2NocDeadlinePassed() {
        atStateClaimIssued1v2UnrepresentedDefendant();

        takenOfflineDate = LocalDateTime.now().plusDays(1);
        addLegalRepDeadlineDefendant2 = LocalDateTime.now();
        return this;
    }

    public CaseDataBuilder atStateHearingFeeDueUnpaid() {
        return atStateHearingFeeDueUnpaid(ONE_V_ONE);
    }

    public CaseDataBuilder atStateHearingFeeDueUnpaid(MultiPartyScenario mpScenario) {
        atStateApplicantRespondToDefenceAndProceed(mpScenario);
        hearingDueDate = LocalDate.now().minusDays(1);
        hearingFeePaymentDetails = new PaymentDetails().setStatus(FAILED);
        ccdState = HEARING_READINESS;
        return this;
    }

    public CaseDataBuilder atStateNoHearingFeeDue() {
        atStateApplicantRespondToDefenceAndProceed();
        hearingDueDate = null;
        hearingFeePaymentDetails = null;
        ccdState = HEARING_READINESS;
        return this;
    }

    public CaseDataBuilder atStateHearingFeeDuePaid() {
        atStateApplicantRespondToDefenceAndProceed();
        hearingDueDate = now().minusDays(1);
        hearingFeePaymentDetails = new PaymentDetails().setStatus(SUCCESS);
        ccdState = HEARING_READINESS;
        return this;
    }

    public CaseDataBuilder atStateHearingFeeDuePaidWithHwf() {
        atStateApplicantRespondToDefenceAndProceed();
        hearingDueDate = now().minusDays(1);
        hearingFeePaymentDetails = null;
        ccdState = HEARING_READINESS;
        return this;
    }

    public CaseDataBuilder atStateBeforeTakenOfflineSDONotDrawn() {

        atStateApplicantRespondToDefenceAndProceed();

        ccdState = JUDICIAL_REFERRAL;
        reasonNotSuitableSDO = new ReasonNotSuitableSDO();
        reasonNotSuitableSDO.setInput("unforeseen complexities");
        unsuitableSDODate = applicant1ResponseDate.plusDays(1);
        return this;
    }

    public CaseDataBuilder atStateBeforeTakenOfflineSDONotDrawnOverLimit() {

        atStateApplicantRespondToDefenceAndProceed();
        ccdState = JUDICIAL_REFERRAL;
        reasonNotSuitableSDO = new ReasonNotSuitableSDO();
        reasonNotSuitableSDO.setInput(reason);
        unsuitableSDODate = applicant1ResponseDate.plusDays(1);
        return this;
    }

    public CaseDataBuilder atStateBeforeTransferCaseSDONotDrawn() {

        atStateApplicantRespondToDefenceAndProceed();

        ccdState = JUDICIAL_REFERRAL;
        notSuitableSdoOptions = NotSuitableSdoOptions.CHANGE_LOCATION;

        tocTransferCaseReason = new TocTransferCaseReason()
            .setReasonForCaseTransferJudgeTxt("unforeseen complexities");
        unsuitableSDODate = applicant1ResponseDate.plusDays(1);
        return this;
    }

    public CaseDataBuilder atStateBeforeTransferCaseSDONotDrawnOverLimit() {

        atStateApplicantRespondToDefenceAndProceed();

        ccdState = JUDICIAL_REFERRAL;
        notSuitableSdoOptions = NotSuitableSdoOptions.CHANGE_LOCATION;

        tocTransferCaseReason = new TocTransferCaseReason()
            .setReasonForCaseTransferJudgeTxt(reason);
        unsuitableSDODate = applicant1ResponseDate.plusDays(1);
        return this;
    }

    public CaseDataBuilder atStateTakenOfflineSDONotDrawn(MultiPartyScenario mpScenario) {

        atStateApplicantRespondToDefenceAndProceed(mpScenario);
        if (mpScenario == ONE_V_TWO_ONE_LEGAL_REP) {
            atStateApplicantRespondToDefenceAndProceedVsBothDefendants_1v2();
        } else if (mpScenario == TWO_V_ONE) {
            atStateBothApplicantsRespondToDefenceAndProceed_2v1();
        }

        ccdState = PROCEEDS_IN_HERITAGE_SYSTEM;
        takenOfflineDate = applicant1ResponseDate.plusDays(1);

        reasonNotSuitableSDO = new ReasonNotSuitableSDO();
        reasonNotSuitableSDO.setInput("unforeseen complexities");
        unsuitableSDODate = applicant1ResponseDate.plusDays(1);

        return this;
    }

    public CaseDataBuilder atStateTakenOfflineSDONotDrawnAfterClaimDetailsNotified(MultiPartyScenario mpScenario, boolean isReason) {
        if (mpScenario == ONE_V_TWO_ONE_LEGAL_REP || mpScenario == ONE_V_TWO_TWO_LEGAL_REP) {
            respondent2 = PartyBuilder.builder().soleTrader().build().toBuilder().partyID("res-2-party-id").build();
            atStateClaimDetailsNotified1v1().respondent2Copy(respondent2).build();
            respondent2SameLegalRepresentative = mpScenario == ONE_V_TWO_ONE_LEGAL_REP ? YES : NO;
        } else {
            atStateClaimDetailsNotified1v1();
        }

        ccdState = PROCEEDS_IN_HERITAGE_SYSTEM;
        takenOfflineDate = LocalDateTime.now();

        reasonNotSuitableSDO = new ReasonNotSuitableSDO();
        reasonNotSuitableSDO.setInput(isReason ? "unforeseen complexities" : "");
        unsuitableSDODate = LocalDateTime.now();

        return this;
    }

    public CaseDataBuilder atStateTakenOfflineSDONotDrawnAfterClaimDetailsNotifiedExtension(boolean isReason) {
        atStateClaimDetailsNotified1v1();
        respondent1TimeExtensionDate = LocalDateTime.now();
        ccdState = PROCEEDS_IN_HERITAGE_SYSTEM;
        takenOfflineDate = LocalDateTime.now();

        reasonNotSuitableSDO = new ReasonNotSuitableSDO();
        reasonNotSuitableSDO.setInput(isReason ? "unforeseen complexities" : "");
        unsuitableSDODate = LocalDateTime.now();

        return this;
    }

    public CaseDataBuilder atStateTakenOfflineSDONotDrawnAfterNotificationAcknowledged(MultiPartyScenario mpScenario, boolean isReason) {
        if (mpScenario == ONE_V_TWO_ONE_LEGAL_REP || mpScenario == ONE_V_TWO_TWO_LEGAL_REP) {
            respondent2 = PartyBuilder.builder().soleTrader().build().toBuilder().partyID("res-2-party-id").build();
            atStateNotificationAcknowledged_1v2_BothDefendants().respondent2Copy(respondent2).build();
            respondent2SameLegalRepresentative = mpScenario == ONE_V_TWO_ONE_LEGAL_REP ? YES : NO;
        } else {
            atStateNotificationAcknowledged();
        }

        ccdState = PROCEEDS_IN_HERITAGE_SYSTEM;
        takenOfflineDate = LocalDateTime.now();

        reasonNotSuitableSDO = new ReasonNotSuitableSDO();
        reasonNotSuitableSDO.setInput(isReason ? "unforeseen complexities" : "");
        unsuitableSDODate = LocalDateTime.now();

        return this;
    }

    public CaseDataBuilder atStateTakenOfflineSDONotDrawnAfterNotificationAcknowledgedTimeExtension(MultiPartyScenario mpScenario, boolean isReason) {
        if (mpScenario == ONE_V_TWO_ONE_LEGAL_REP || mpScenario == ONE_V_TWO_TWO_LEGAL_REP) {
            respondent2 = PartyBuilder.builder().soleTrader().build().toBuilder().partyID("res-2-party-id").build();
            atStateNotificationAcknowledged_1v2_BothDefendants().respondent2Copy(respondent2).build();
            respondent2SameLegalRepresentative = mpScenario == ONE_V_TWO_ONE_LEGAL_REP ? YES : NO;
            respondent1TimeExtensionDate = LocalDateTime.now();
            respondent2TimeExtensionDate = LocalDateTime.now();
        } else {
            atDeadlinePassedAfterStateNotificationAcknowledgedTimeExtension();
        }

        ccdState = PROCEEDS_IN_HERITAGE_SYSTEM;
        takenOfflineDate = LocalDateTime.now();

        reasonNotSuitableSDO = new ReasonNotSuitableSDO();
        reasonNotSuitableSDO.setInput(isReason ? "unforeseen complexities" : "");
        unsuitableSDODate = LocalDateTime.now();

        return this;
    }

    public CaseDataBuilder atStateTakenOfflineAfterSDO(MultiPartyScenario mpScenario) {

        atStateApplicantRespondToDefenceAndProceed(mpScenario);
        if (mpScenario == ONE_V_TWO_ONE_LEGAL_REP) {
            atStateApplicantRespondToDefenceAndProceedVsBothDefendants_1v2();
        } else if (mpScenario == TWO_V_ONE) {
            atStateBothApplicantsRespondToDefenceAndProceed_2v1();
        }

        drawDirectionsOrderRequired = NO;
        ccdState = PROCEEDS_IN_HERITAGE_SYSTEM;
        takenOfflineDate = applicant1ResponseDate.plusDays(1);
        return this;
    }

    public CaseDataBuilder atStateTakenOfflineByStaffAfterSDO(MultiPartyScenario mpScenario) {
        atStateApplicantRespondToDefenceAndProceed(mpScenario);
        if (mpScenario == ONE_V_TWO_ONE_LEGAL_REP) {
            atStateApplicantRespondToDefenceAndProceedVsBothDefendants_1v2();
        } else if (mpScenario == TWO_V_ONE) {
            atStateBothApplicantsRespondToDefenceAndProceed_2v1();
        }

        drawDirectionsOrderRequired = NO;
        ccdState = PROCEEDS_IN_HERITAGE_SYSTEM;
        takenOfflineDate = applicant1ResponseDate.plusDays(1);
        takenOfflineByStaffDate = applicant1ResponseDate.plusDays(1);
        return this;
    }

    public CaseDataBuilder atStateApplicantProceedAllMediation(MultiPartyScenario mpScenario) {

        applicant1ClaimMediationSpecRequired = new SmallClaimMedicalLRspec(YES);
        applicantMPClaimMediationSpecRequired = new SmallClaimMedicalLRspec(YES);
        respondent1MediationRequired = YES;
        respondent2MediationRequired = YES;
        responseClaimTrack = SMALL_CLAIM.name();
        caseAccessCategory = SPEC_CLAIM;

        atStateApplicantRespondToDefenceAndProceed(mpScenario);

        if (mpScenario == ONE_V_ONE) {
            atStateRespondentFullDefenceSpec();
        } else if (mpScenario == ONE_V_TWO_ONE_LEGAL_REP) {
            atStateApplicantRespondToDefenceAndProceedVsBothDefendants_1v2();
            atStateRespondentFullDefenceSpec();
        } else if (mpScenario == ONE_V_TWO_TWO_LEGAL_REP) {
            atStateApplicantRespondToDefenceAndProceedVsBothDefendants_1v2();
            atState1v2DifferentSolicitorDivergentResponseSpec(
                RespondentResponseTypeSpec.FULL_DEFENCE,
                RespondentResponseTypeSpec.FULL_DEFENCE
            );
        } else if (mpScenario == TWO_V_ONE) {
            applicant1ProceedWithClaimSpec2v1 = YES;
            atStateBothApplicantsRespondToDefenceAndProceed_2v1_SPEC();
        }

        return this;
    }

    public CaseDataBuilder atStateMediationUnsuccessful(MultiPartyScenario mpScenario) {
        atStateApplicantProceedAllMediation(mpScenario);
        applicantsProceedIntention = YES;
        caseDataLiP = new CaseDataLiP()
            .setApplicant1ClaimMediationSpecRequiredLip(
                new ClaimantMediationLip()
                    .setHasAgreedFreeMediation(MediationDecision.Yes));

        mediation = new Mediation().setUnsuccessfulMediationReason("Unsuccessful");

        return this;
    }

    public CaseDataBuilder atStateMediationUnsuccessfulCarm(MultiPartyScenario mpScenario) {
        atStateApplicantProceedAllMediation(mpScenario);
        applicantsProceedIntention = YES;
        caseDataLiP = new CaseDataLiP()
            .setApplicant1ClaimMediationSpecRequiredLip(
                new ClaimantMediationLip()
                    .setHasAgreedFreeMediation(MediationDecision.Yes));

        mediation = new Mediation().setMediationUnsuccessfulReasonsMultiSelect(
            List.of(MediationUnsuccessfulReason.NOT_CONTACTABLE_CLAIMANT_ONE));

        return this;
    }

    public CaseDataBuilder atStateLipClaimantDoesNotSettle() {
        caseDataLiP = new CaseDataLiP()
            .setApplicant1SettleClaim(NO);
        return this;
    }

    public CaseDataBuilder atStateMediationSuccessful(MultiPartyScenario mpScenario) {
        atStateApplicantProceedAllMediation(mpScenario);
        applicantsProceedIntention = YES;
        caseDataLiP = new CaseDataLiP()
            .setApplicant1ClaimMediationSpecRequiredLip(
                new ClaimantMediationLip()
                    .setHasAgreedFreeMediation(MediationDecision.Yes));

        mediation = new Mediation().setMediationSuccessful(new MediationSuccessful().setMediationSettlementAgreedAt(now())
                .setMediationAgreement(new MediationAgreementDocument().setName("mediation")
                    )
                )
            ;

        return this;
    }

    public CaseDataBuilder mediation(Mediation mediation) {
        this.mediation = mediation;
        return this;
    }

    public CaseDataBuilder businessProcess(BusinessProcess businessProcess) {
        this.businessProcess = businessProcess;
        return this;
    }

    public CaseDataBuilder applicant2ResponseDate(LocalDateTime applicant2ResponseDate) {
        this.applicant2ResponseDate = applicant2ResponseDate;
        return this;
    }

    public CaseDataBuilder caseBundles(List<IdValue<Bundle>> caseBundles) {
        this.caseBundles = caseBundles;
        return this;
    }

    public CaseDataBuilder applicantSolicitor1UserDetails(IdamUserDetails applicantSolicitor1UserDetails) {
        this.applicantSolicitor1UserDetails = applicantSolicitor1UserDetails;
        return this;
    }

    public CaseDataBuilder addApplicant1LitigationFriend() {
        this.applicant1LitigationFriend = new LitigationFriend().setPartyID("app-1-litfriend-party-id")
            .setFullName("Mr Applicant Litigation Friend")
            .setFirstName("Applicant")
            .setLastName("Litigation Friend")
            .setPrimaryAddress(AddressBuilder.defaults().build())
            .setHasSameAddressAsLitigant(YES)
            .setCertificateOfSuitability(List.of())
            ;
        this.applicant1LitigationFriendRequired = YES;
        return this;
    }

    public CaseDataBuilder addApplicant2LitigationFriend() {
        this.applicant2LitigationFriend = new LitigationFriend().setPartyID("app-2-litfriend-party-id")
            .setFullName("Mr Applicant Litigation Friend")
            .setFirstName("Applicant Two")
            .setLastName("Litigation Friend")
            .setPrimaryAddress(AddressBuilder.defaults().build())
            .setHasSameAddressAsLitigant(YES)
            .setCertificateOfSuitability(List.of())
            ;
        this.applicant2LitigationFriendRequired = YES;
        return this;
    }

    public CaseDataBuilder addRespondent1LitigationFriend() {
        this.respondent1LitigationFriend = new LitigationFriend().setPartyID("res-1-litfriend-party-id")
            .setFullName("Mr Litigation Friend")
            .setFirstName("Litigation")
            .setLastName("Friend")
            .setPrimaryAddress(AddressBuilder.defaults().build())
            .setHasSameAddressAsLitigant(YES)
            .setCertificateOfSuitability(List.of())
            ;
        LocalDateTime tomrrowsDateTime = LocalDateTime.now().plusDays(1);
        this.respondent1LitigationFriendDate = tomrrowsDateTime;
        this.respondent1LitigationFriendCreatedDate = tomrrowsDateTime;
        return this;
    }

    public CaseDataBuilder addGenericRespondentLitigationFriend() {
        this.genericLitigationFriend = new LitigationFriend().setFullName("Mr Litigation Friend")
            ;
        return this;
    }

    public CaseDataBuilder addRespondent2LitigationFriend() {
        this.respondent2LitigationFriend = new LitigationFriend().setPartyID("res-2-litfriend-party-id")
            .setFullName("Mr Litigation Friend")
            .setFirstName("Litigation")
            .setLastName("Friend")
            .setPrimaryAddress(AddressBuilder.defaults().build())
            .setHasSameAddressAsLitigant(YES)
            .setCertificateOfSuitability(List.of())
            ;
        LocalDateTime tomrrowsDateTime = LocalDateTime.now().plusDays(1);
        this.respondent2LitigationFriendDate = tomrrowsDateTime;
        this.respondent2LitigationFriendCreatedDate = tomrrowsDateTime;
        return this;
    }

    public CaseDataBuilder addBothRespondent1LitigationFriend() {
        this.respondent1LitigationFriend = new LitigationFriend().setFullName("Mr Litigation Friend")
            ;
        this.respondent2LitigationFriend = new LitigationFriend().setFullName("Mr Litigation Friend 2")
            ;
        return this;
    }

    public CaseDataBuilder getGeneralApplicationWithStrikeOut(final String litigiousPartyID) {
        List<GeneralApplicationTypes> types = Arrays.asList(STRIKE_OUT);
        List<Element<GeneralApplication>> generalApplicationValues = wrapElements(
            GeneralApplication.builder()
                .applicantPartyName("partyName")
                .litigiousPartyID(litigiousPartyID)
                .generalAppDateDeadline(DEADLINE)
                .generalAppSubmittedDateGAspec(SUBMITTED_DATE_TIME)
                .generalAppType(GAApplicationType.builder()
                    .types(types)
                    .build())

                .caseLink(CaseLink.builder().caseReference("12345678").build())
                .businessProcess(new BusinessProcess()
                    .setCamundaEvent("NotifyRoboticsOnCaseHandedOffline"))
                .build());

        this.generalApplications = generalApplicationValues;
        return this;
    }

    public CaseDataBuilder getGeneralStrikeOutApplicationsDetailsWithCaseState(final String caseState) {
        List<Element<GeneralApplicationsDetails>> generalApplicationsDetails = wrapElements(
            GeneralApplicationsDetails.builder()
                .generalApplicationType(STRIKE_OUT.getDisplayedValue())
                .caseState(caseState)
                .generalAppSubmittedDateGAspec(SUBMITTED_DATE_TIME)
                .caseLink(CaseLink.builder().caseReference("12345678").build())
                .build()
        );

        this.generalApplicationsDetails = generalApplicationsDetails;
        return this;
    }

    public CaseDataBuilder multiPartyClaimTwoDefendantSolicitors() {
        this.addRespondent2 = YES;
        this.respondent2 = PartyBuilder.builder().individual().build().toBuilder().partyID("res-2-party-id").build();
        this.respondent2Represented = YES;
        this.respondent2SameLegalRepresentative = NO;
        this.respondentSolicitor2Reference = "01234";
        this.solicitorReferences = new SolicitorReferences()
            .setApplicantSolicitor1Reference("12345")
            .setRespondentSolicitor1Reference("6789")
            .setRespondentSolicitor2Reference("01234")
            ;
        return this;
    }

    public CaseDataBuilder multiPartyClaimTwoDefendantsLiP() {
        this.addRespondent2 = YES;
        this.respondent2 = PartyBuilder.builder().individual().build().toBuilder().partyID("res-2-party-id").build();
        this.respondent2Represented = NO;
        this.respondent1Represented = NO;
        return this;
    }

    public CaseDataBuilder multiPartyClaimTwoDefendantSolicitorsForSdoMP() {
        this.addRespondent2 = YES;
        this.respondent2 = PartyBuilder.builder().individual().build().toBuilder().partyID("res-2-party-id").build();
        this.respondent2SameLegalRepresentative = NO;
        this.respondentSolicitor2Reference = "01234";
        respondent2ClaimResponseType = RespondentResponseType.FULL_DEFENCE;
        caseManagementLocation = new CaseLocationCivil().setBaseLocation("00000").setRegion("4");
        return this;
    }

    public CaseDataBuilder multiPartyClaimOneDefendantSolicitor() {
        this.addRespondent2 = YES;
        this.respondent2 = PartyBuilder.builder().individual().build().toBuilder().partyID("res-2-party-id").build();
        this.respondent2SameLegalRepresentative = YES;
        return this;
    }

    public CaseDataBuilder multiPartyClaimTwoDefendantSolicitorsSpec() {
        this.addRespondent2 = YES;
        this.respondent2 = PartyBuilder.builder().individual().build().toBuilder().partyID("res-2-party-id").build();
        this.respondent2SameLegalRepresentative = NO;
        this.respondentSolicitor2Reference = "01234";
        this.specRespondent1Represented = YES;
        this.specRespondent2Represented = YES;
        return this;
    }

    public CaseDataBuilder multiPartyClaimTwoDefendantSameSolicitorsSpec() {
        this.addRespondent2 = YES;
        this.respondent2 = PartyBuilder.builder().individual().build();
        this.respondent2SameLegalRepresentative = YES;
        this.respondentSolicitor2Reference = "01234";
        this.specRespondent1Represented = YES;
        this.specRespondent2Represented = YES;
        return this;
    }

    public CaseDataBuilder multiPartyClaimOneClaimant1ClaimResponseType() {
        this.claimant1ClaimResponseTypeForSpec = RespondentResponseTypeSpec.FULL_ADMISSION;
        return this;
    }

    public CaseDataBuilder respondent1ClaimResponseTypeToApplicant2Spec() {
        this.respondent1ClaimResponseTypeForSpec = RespondentResponseTypeSpec.FULL_ADMISSION;
        return this;
    }

    public CaseDataBuilder multiPartyClaimTwoApplicants() {
        this.addApplicant2 = YES;
        this.applicant2 = PartyBuilder.builder().individual("Jason").build()
            .toBuilder().partyID("app-2-party-id").build();
        return this;
    }

    private List<CaseData> get2v1DifferentResponseCase() {
        Party applicant1 = Party.builder().build();
        Party applicant2 = Party.builder().build();
        List<CaseData> cases = new ArrayList<>();
        for (RespondentResponseTypeSpec r1 : RespondentResponseTypeSpec.values()) {
            for (RespondentResponseTypeSpec r2 : RespondentResponseTypeSpec.values()) {
                if (!r1.equals(r2)) {
                    cases.add(CaseData.builder()
                        .applicant1(applicant1)
                        .applicant2(applicant2)
                        .claimant1ClaimResponseTypeForSpec(r1)
                        .claimant2ClaimResponseTypeForSpec(r2)
                        .build());
                }
            }
        }
        return cases;
    }

    public CaseDataBuilder setClaimTypeToSpecClaim() {
        this.caseAccessCategory = SPEC_CLAIM;
        return this;
    }

    public CaseDataBuilder setClaimTypeToUnspecClaim() {
        this.caseAccessCategory = UNSPEC_CLAIM;
        return this;
    }

    public CaseDataBuilder setClaimNotificationDate() {
        claimNotificationDate = issueDate.plusDays(1).atStartOfDay();
        return this;
    }

    public CaseDataBuilder respondent2Responds(RespondentResponseType responseType) {
        this.respondent2ClaimResponseType = responseType;
        this.respondent2ResponseDate = LocalDateTime.now().plusDays(3);
        return this;
    }

    public CaseDataBuilder respondent2Responds1v2SameSol(RespondentResponseType responseType) {
        this.respondent2ClaimResponseType = responseType;
        this.respondent2ResponseDate = respondent1ResponseDate;
        return this;
    }

    public CaseDataBuilder respondent2Responds1v2DiffSol(RespondentResponseType responseType) {
        this.respondent2ClaimResponseType = responseType;
        this.respondent2ResponseDate = respondent1ResponseDate.plusDays(1);
        return this;
    }

    public CaseDataBuilder respondent1ClaimResponseTypeToApplicant2(RespondentResponseType responseType) {
        this.respondent1ClaimResponseTypeToApplicant2 = responseType;
        return this;
    }

    public CaseDataBuilder respondent1ClaimResponseTypeToApplicant1(RespondentResponseType responseType) {
        this.respondent1ClaimResponseType = responseType;
        respondent1DQ();
        return this;
    }

    public CaseDataBuilder respondentResponseIsSame(YesOrNo isSame) {
        this.respondentResponseIsSame = isSame;
        return this;
    }

    public CaseDataBuilder respondent1Copy(Party party) {
        this.respondent1Copy = party;
        return this;
    }

    public CaseDataBuilder respondent2Copy(Party party) {
        this.respondent2Copy = party;
        return this;
    }

    public CaseDataBuilder generateYearsAndMonthsIncorrectInput() {
        atStateRespondentRespondToClaimFastTrack(RespondentResponseType.PART_ADMISSION);
        respondent1ClaimResponseDocument = new ResponseDocument(DocumentBuilder.builder().setDocumentName("defendant-response.pdf").build());
        respondent1DQ();
        respondent1ResponseDate = LocalDateTime.now();
        respondent1ClaimResponseTypeForSpec = RespondentResponseTypeSpec.PART_ADMISSION;

        respondToClaimAdmitPartUnemployedLRspec = new UnemployedComplexTypeLRspec(
            "No",
            new LengthOfUnemploymentComplexTypeLRspec()
                .setNumberOfMonthsInUnemployment("1.5")
                .setNumberOfYearsInUnemployment("2.6"),
            null
        );

        return this;
    }

    public CaseDataBuilder generatePaymentDateForAdmitPartResponse() {
        atStateRespondentRespondToClaimFastTrack(RespondentResponseType.PART_ADMISSION);
        respondent1ClaimResponseDocument = new ResponseDocument(DocumentBuilder.builder().setDocumentName("defendant-response.pdf").build());
        respondent1DQ();
        respondent1ResponseDate = LocalDateTime.now();
        respondent1ClaimResponseTypeForSpec = RespondentResponseTypeSpec.PART_ADMISSION;

        respondToClaimAdmitPartLRspec = new RespondToClaimAdmitPartLRspec()
            .setWhenWillThisAmountBePaid(PAST_DATE)
            ;

        return this;
    }

    public CaseDataBuilder generateRepaymentDateForAdmitPartResponse() {
        atStateRespondentRespondToClaimFastTrack(RespondentResponseType.PART_ADMISSION);
        respondent1ClaimResponseDocument = new ResponseDocument(DocumentBuilder.builder().setDocumentName("defendant-response.pdf").build());
        respondent1DQ();

        respondent1RepaymentPlan = new RepaymentPlanLRspec().setPaymentAmount(BigDecimal.valueOf(9000))
            .setRepaymentFrequency(PaymentFrequencyLRspec.ONCE_ONE_MONTH).setFirstRepaymentDate(FUTURE_DATE);

        respondent2RepaymentPlan = new RepaymentPlanLRspec().setPaymentAmount(BigDecimal.valueOf(9000))
            .setRepaymentFrequency(PaymentFrequencyLRspec.ONCE_ONE_MONTH).setFirstRepaymentDate(FUTURE_DATE);

        return this;
    }

    public CaseDataBuilder generateDefendant2RepaymentDateForAdmitPartResponse() {
        atStateRespondentRespondToClaimFastTrack(RespondentResponseType.PART_ADMISSION);
        respondent2ClaimResponseDocument = new ResponseDocument(DocumentBuilder.builder().setDocumentName("defendant-response.pdf").build());
        respondent2DQ();

        respondent2RepaymentPlan = new RepaymentPlanLRspec().setPaymentAmount(BigDecimal.valueOf(9000))
            .setRepaymentFrequency(PaymentFrequencyLRspec.ONCE_ONE_MONTH).setFirstRepaymentDate(FUTURE_DATE);

        return this;
    }

    public CaseDataBuilder receiveUpdatePaymentRequest() {
        atStateRespondentFullDefence();
        this.hearingFeePaymentDetails = new PaymentDetails()
            .setCustomerReference("RC-1604-0739-2145-4711")
            ;

        return this;
    }

    public CaseDataBuilder buildHmctsInternalCaseName() {
        String applicant2Name = applicant2 != null ? " and " + applicant2.getPartyName() : "";
        String respondent2Name = respondent2 != null ? " and " + respondent2.getPartyName() : "";

        this.caseNameHmctsInternal = String.format("%s%s v %s%s", applicant1.getPartyName(),
            applicant2Name, respondent1.getPartyName(), respondent2Name);
        return this;
    }

    public CaseDataBuilder atSpecAoSApplicantCorrespondenceAddressRequired(
        YesOrNo specAoSApplicantCorrespondenceAddressRequired) {
        this.specAoSApplicantCorrespondenceAddressRequired = specAoSApplicantCorrespondenceAddressRequired;
        return this;
    }

    public CaseDataBuilder atSpecAoSApplicantCorrespondenceAddressDetails(
        Address specAoSApplicantCorrespondenceAddressDetails) {
        this.specAoSApplicantCorrespondenceAddressDetails = specAoSApplicantCorrespondenceAddressDetails;
        return this;
    }

    public CaseDataBuilder addRespondent1PinToPostLRspec(DefendantPinToPostLRspec respondent1PinToPostLRspec) {
        this.respondent1PinToPostLRspec = respondent1PinToPostLRspec;
        return this;
    }

    public CaseDataBuilder atSpecAoSRespondentCorrespondenceAddressRequired(
        YesOrNo specAosRespondentCorrespondenceAddressRequired) {
        this.specAoSRespondentCorrespondenceAddressRequired = specAosRespondentCorrespondenceAddressRequired;
        return this;
    }

    public CaseDataBuilder atSpecAoSRespondentCorrespondenceAddressDetails(
        Address specAoSRespondentCorrespondenceAddressDetails) {
        this.specAoSRespondentCorrespondenceAddressDetails = specAoSRespondentCorrespondenceAddressDetails;
        return this;
    }

    public CaseDataBuilder atSpecAoSRespondent2HomeAddressRequired(YesOrNo specAoSRespondent2HomeAddressRequired) {
        this.specAoSRespondent2HomeAddressRequired = specAoSRespondent2HomeAddressRequired;
        return this;
    }

    public CaseDataBuilder atSpecAoSRespondent2HomeAddressDetails(Address specAoSRespondent2HomeAddressDetails) {
        this.specAoSRespondent2HomeAddressDetails = specAoSRespondent2HomeAddressDetails;
        return this;
    }

    public CaseDataBuilder atStateClaimDetailsNotified_1v2_andNotifyBothCoS() {
        atStateClaimDetailsNotified();
        multiPartyClaimTwoDefendantLips();
        respondent2 = PartyBuilder.builder().soleTrader().build();
        return this;
    }

    public CaseDataBuilder atStateClaimDetailsNotified_1v2_1Lip_1Lr() {
        atStateClaimDetailsNotified();
        multiPartyClaimTwoDefendant1Lip1Lr();
        respondent2 = PartyBuilder.builder().soleTrader().build().toBuilder().partyID("res-2-party-id").build();
        return this;
    }

    public CaseDataBuilder atStateClaimDetailsNotified_1v2_1Lr_1Lip() {
        atStateClaimDetailsNotified();
        multiPartyClaimTwoDefendant1Lr1Lip();
        respondent2 = PartyBuilder.builder().soleTrader().build().toBuilder().partyID("res-2-party-id").build();
        return this;
    }

    public CaseDataBuilder respondent1DQWitnessesRequiredSpec(YesOrNo respondent1DQWitnessesRequiredSpec) {
        this.respondent1DQWitnessesRequiredSpec = respondent1DQWitnessesRequiredSpec;
        return this;
    }

    public CaseDataBuilder respondent1DQWitnessesDetailsSpec(List<Element<Witness>> respondent1DQWitnessesDetailsSpec) {
        this.respondent1DQWitnessesDetailsSpec = respondent1DQWitnessesDetailsSpec;
        return this;
    }

    public CaseDataBuilder caseAccessCategory(CaseCategory caseAccessCategory) {
        this.caseAccessCategory = caseAccessCategory;
        return this;
    }

    public CaseDataBuilder caseManagementLocation(CaseLocationCivil caseManagementLocation) {
        this.caseManagementLocation = caseManagementLocation;
        return this;
    }

    public CaseDataBuilder removeSolicitorReferences() {
        this.solicitorReferences = null;
        this.respondentSolicitor2Reference = null;
        return this;
    }

    public CaseDataBuilder transferCourtLocationList(DynamicList transferCourtLocationList) {
        this.transferCourtLocationList = transferCourtLocationList;
        return this;
    }

    public CaseDataBuilder reasonForTransfer(String reasonForTransfer) {
        this.reasonForTransfer = reasonForTransfer;
        return this;
    }

    public CaseDataBuilder flightDelay(FlightDelayDetails flightDelayDetails) {
        this.flightDelayDetails = flightDelayDetails;
        return this;
    }

    public CaseDataBuilder isFlightDelayClaim(YesOrNo isFlightDelayClaim) {
        this.isFlightDelayClaim = isFlightDelayClaim;
        return this;
    }

    public CaseDataBuilder reasonForReconsiderationApplicant(ReasonForReconsideration reasonForReconsideration) {
        this.reasonForReconsiderationApplicant = reasonForReconsideration;
        return this;
    }

    public CaseDataBuilder reasonForReconsiderationRespondent1(ReasonForReconsideration reasonForReconsideration) {
        this.reasonForReconsiderationRespondent1 = reasonForReconsideration;
        return this;
    }

    public CaseDataBuilder reasonForReconsiderationRespondent2(ReasonForReconsideration reasonForReconsideration) {
        this.reasonForReconsiderationRespondent2 = reasonForReconsideration;
        return this;
    }

    public CaseData buildMakePaymentsCaseData() {
        Organisation orgId = new Organisation().setOrganisationID("OrgId");

        return build().toBuilder()
            .ccdCaseReference(1644495739087775L)
            .claimFee(new Fee().setCalculatedAmountInPence(BigDecimal.valueOf(100)).setCode("CODE"))
            .claimIssuedPBADetails(
                new SRPbaDetails()
                    .setFee(
                        new Fee()
                            .setCode("FE203")
                            .setCalculatedAmountInPence(BigDecimal.valueOf(27500))
                            .setVersion("1")
                            )
                    .setServiceReqReference(CUSTOMER_REFERENCE))
            .applicant1OrganisationPolicy(new OrganisationPolicy().setOrganisation(orgId))
            .build();
    }

    public CaseData buildCuiCaseDataWithFee() {
        return build().toBuilder()
            .ccdCaseReference(1644495739087775L)
            .claimFee(new Fee().setCalculatedAmountInPence(BigDecimal.valueOf(100)).setCode("CODE"))
            .build();
    }

    public CaseData buildMakePaymentsCaseDataWithoutClaimIssuedPbaDetails() {
        Organisation orgId = new Organisation().setOrganisationID("OrgId");

        return build().toBuilder()
            .ccdCaseReference(1644495739087775L)
            .applicant1OrganisationPolicy(new OrganisationPolicy().setOrganisation(orgId))
            .build();
    }

    public CaseData buildMakePaymentsCaseDataWithoutServiceRequestReference() {
        Organisation orgId = new Organisation().setOrganisationID("OrgId");

        return build().toBuilder()
            .ccdCaseReference(1644495739087775L)
            .claimIssuedPBADetails(
                new SRPbaDetails()
                    .setFee(
                        new Fee()
                            .setCode("FE203")
                            .setCalculatedAmountInPence(BigDecimal.valueOf(27500))
                            .setVersion("1")
                            ))
            .applicant1OrganisationPolicy(new OrganisationPolicy().setOrganisation(orgId))
            .build();
    }

    public CaseData buildMakePaymentsCaseDataWithHearingDueDateWithHearingFeePBADetails() {
        Organisation orgId = new Organisation().setOrganisationID("OrgId");

        return build().toBuilder()
            .ccdCaseReference(1644495739087775L)
            .claimIssuedPBADetails(
                new SRPbaDetails()
                    .setFee(
                        new Fee()
                            .setCode("FE203")
                            .setCalculatedAmountInPence(BigDecimal.valueOf(27500))
                            .setVersion("1")
                            )
                    .setServiceReqReference(CUSTOMER_REFERENCE))
            .applicant1OrganisationPolicy(new OrganisationPolicy().setOrganisation(orgId))
            .hearingDueDate(LocalDate.now().plusWeeks(2))
            .hearingFeePBADetails(new SRPbaDetails()
                .setFee(
                    new Fee()
                        .setCode("FE203")
                        .setCalculatedAmountInPence(BigDecimal.valueOf(27500))
                        .setVersion("1")
                        )
                .setServiceReqReference(CUSTOMER_REFERENCE))
            .build();
    }

    public CaseData withHearingFeePBADetailsPaymentFailed() {
        return build().toBuilder()
            .ccdCaseReference(1644495739087775L)
            .hearingFeePBADetails(new SRPbaDetails()
                .setFee(
                    new Fee()
                        .setCode("FE203")
                        .setCalculatedAmountInPence(BigDecimal.valueOf(27500))
                        .setVersion("1")
                        )
                .setPaymentDetails(new PaymentDetails()
                    .setStatus(FAILED)
                    )
                .setServiceReqReference(CUSTOMER_REFERENCE))
            .build();
    }

    public CaseData withHearingFeePBADetailsPaymentSuccess() {
        return build().toBuilder()
            .ccdCaseReference(1644495739087775L)
            .hearingFeePBADetails(new SRPbaDetails()
                .setFee(
                    new Fee()
                        .setCode("FE203")
                        .setCalculatedAmountInPence(BigDecimal.valueOf(27500))
                        .setVersion("1")
                        )
                .setPaymentDetails(new PaymentDetails()
                    .setStatus(SUCCESS)
                    )
                .setServiceReqReference(CUSTOMER_REFERENCE))
            .build();
    }

    public CaseData withHearingFeePBADetailsNoPaymentStatus() {
        return build().toBuilder()
            .ccdCaseReference(1644495739087775L)
            .claimValue(new ClaimValue()
                .setStatementOfValueInPennies(BigDecimal.valueOf(10800)))
            .allocatedTrack(SMALL_CLAIM)
            .applicant1OrganisationPolicy(new OrganisationPolicy().setOrganisation(
                new Organisation().setOrganisationID("OrgId")))
            .build();
    }

    public CaseData buildMakePaymentsCaseDataWithHearingDate() {
        Organisation orgId = new Organisation().setOrganisationID("OrgId");

        return build().toBuilder()
            .ccdCaseReference(1644495739087775L)
            .claimIssuedPBADetails(
                new SRPbaDetails()
                    .setFee(
                        new Fee()
                            .setCode("FE203")
                            .setCalculatedAmountInPence(BigDecimal.valueOf(27500))
                            .setVersion("1")
                            )
                    .setServiceReqReference(CUSTOMER_REFERENCE))
            .applicant1OrganisationPolicy(new OrganisationPolicy().setOrganisation(orgId))
            .hearingDate(LocalDate.now().plusWeeks(2))
            .hearingDueDate(LocalDate.now().plusWeeks(2))
            .build();
    }

    public CaseData buildMakePaymentsCaseDataWithHearingDueDateWithoutClaimIssuedPbaDetails() {
        Organisation orgId = new Organisation().setOrganisationID("OrgId");

        return build().toBuilder()
            .ccdCaseReference(1644495739087775L)
            .claimIssuedPBADetails(
                new SRPbaDetails()
                    .setFee(
                        new Fee()
                            .setCode("FE203")
                            .setCalculatedAmountInPence(BigDecimal.valueOf(27500))
                            .setVersion("1")
                            )
                    .setServiceReqReference(CUSTOMER_REFERENCE))
            .applicant1OrganisationPolicy(new OrganisationPolicy().setOrganisation(orgId))
            .hearingDate(LocalDate.now().plusWeeks(2))
            .hearingDueDate(LocalDate.now().plusWeeks(2))
            .build();
    }

    public CaseData buildMakePaymentsCaseDataWithHearingDateWithoutClaimIssuedPbaDetails() {
        uk.gov.hmcts.reform.ccd.model.Organisation orgId = new uk.gov.hmcts.reform.ccd.model.Organisation().setOrganisationID("OrgId");

        return build().toBuilder()
            .ccdCaseReference(1644495739087775L)
            .applicant1OrganisationPolicy(new OrganisationPolicy().setOrganisation(orgId))
            .hearingDueDate(LocalDate.now().plusWeeks(2))
            .build();
    }

    public CaseData buildMakePaymentsCaseDataWithHearingDateWithHearingFeePBADetails() {
        uk.gov.hmcts.reform.ccd.model.Organisation orgId = new uk.gov.hmcts.reform.ccd.model.Organisation().setOrganisationID("OrgId");

        return build().toBuilder()
            .ccdCaseReference(1644495739087775L)
            .claimIssuedPBADetails(
                new SRPbaDetails()
                    .setFee(
                        new Fee()
                            .setCode("FE203")
                            .setCalculatedAmountInPence(BigDecimal.valueOf(27500))
                            .setVersion("1")
                            )
                    .setServiceReqReference(CUSTOMER_REFERENCE))
            .applicant1OrganisationPolicy(new OrganisationPolicy().setOrganisation(orgId))
            .hearingDate(LocalDate.now().plusWeeks(2))
            .hearingFeePBADetails(new SRPbaDetails()
                .setFee(
                    new Fee()
                        .setCode("FE203")
                        .setCalculatedAmountInPence(BigDecimal.valueOf(27500))
                        .setVersion("1")
                        )
                .setServiceReqReference(CUSTOMER_REFERENCE))
            .build();
    }

    public CaseData buildClaimIssuedPaymentCaseData() {
        Organisation orgId = new Organisation().setOrganisationID("OrgId");

        return build().toBuilder()
            .ccdCaseReference(1644495739087775L)
            .ccdState(PENDING_CASE_ISSUED)
            .claimFee(
                new Fee()
                    .setCode("FE203")
                    .setCalculatedAmountInPence(BigDecimal.valueOf(27500))
                    .setVersion("1")
                    )
            .applicant1OrganisationPolicy(new OrganisationPolicy().setOrganisation(orgId))
            .applicant1(Party.builder()
                .individualFirstName("First name")
                .individualLastName("Second name")
                .type(Party.Type.INDIVIDUAL)
                .partyName("test").build())
            .build();
    }

    public CaseData buildClaimIssuedPaymentCaseDataWithPba(String pbaAccountNumber) {
        return this.buildClaimIssuedPaymentCaseData().toBuilder()
            .applicantSolicitor1PbaAccounts(DynamicList.builder()
                .value(DynamicListElement.dynamicElement(pbaAccountNumber)).build())
            .paymentReference("RC-1234-1234-1234-1234")
            .build();
    }

    public CaseData buildPaymentFailureCaseData() {
        Organisation orgId = new Organisation().setOrganisationID("OrgId");

        return build().toBuilder()
            .ccdCaseReference(1644495739087775L)
            .ccdCaseReference(1644495739087775L)
            .legacyCaseReference("000DC001")
            .businessProcess(new BusinessProcess().setStatus(BusinessProcessStatus.READY))
            .claimIssuedPBADetails(
                new SRPbaDetails()
                    .setPaymentDetails(new PaymentDetails()
                        .setStatus(PaymentStatus.FAILED)
                        .setReference("RC-1658-4258-2679-9795")
                        .setCustomerReference(CUSTOMER_REFERENCE)
                        )
                    .setFee(
                        new Fee()
                            .setCode("FE203")
                            .setCalculatedAmountInPence(BigDecimal.valueOf(27500))
                            .setVersion("1")
                            )
                    .setServiceReqReference(CUSTOMER_REFERENCE))
            .applicant1OrganisationPolicy(new OrganisationPolicy().setOrganisation(orgId))
            .build();
    }

    public CaseData buildPaymentSuccessfulCaseData() {
        Organisation orgId = new Organisation().setOrganisationID("OrgId");

        return build().toBuilder()
            .ccdCaseReference(1644495739087775L)
            .ccdCaseReference(1644495739087775L)
            .legacyCaseReference("000DC001")
            .businessProcess(new BusinessProcess().setStatus(BusinessProcessStatus.READY))
            .claimIssuedPBADetails(
                new SRPbaDetails()
                    .setPaymentSuccessfulDate(LocalDateTime.of(
                        LocalDate.of(2020, 01, 01),
                        LocalTime.of(12, 00, 00)
                    ))
                    .setPaymentDetails(new PaymentDetails()
                        .setStatus(PaymentStatus.SUCCESS)
                        .setReference("RC-1234-1234-1234-1234")
                        .setCustomerReference(CUSTOMER_REFERENCE)
                        )
                    .setFee(
                        new Fee()
                            .setCode("FE203")
                            .setCalculatedAmountInPence(BigDecimal.valueOf(27500))
                            .setVersion("1")
                            )
                    .setServiceReqReference(CUSTOMER_REFERENCE))
            .applicant1OrganisationPolicy(new OrganisationPolicy().setOrganisation(orgId))
            .build();
    }

    public CaseData buildJudmentOnlineCaseDataWithPaymentByInstalment() {
        return build().toBuilder()
            .ccdState(All_FINAL_ORDERS_ISSUED)
            .joJudgmentRecordReason(JudgmentRecordedReason.JUDGE_ORDER)
            .respondent1(PartyBuilder.builder().individual().build())
            .joInstalmentDetails(new JudgmentInstalmentDetails()
                .setStartDate(LocalDate.of(2022, 12, 12))
                .setAmount("120")
                .setPaymentFrequency(PaymentFrequency.MONTHLY))
            .joAmountOrdered("1200")
            .joAmountCostOrdered("1100")
            .joPaymentPlan(new JudgmentPaymentPlan().setType(PaymentPlanSelection.PAY_IN_INSTALMENTS))
            .joOrderMadeDate(LocalDate.of(2022, 12, 12))
            .caseManagementLocation(new CaseLocationCivil().setBaseLocation("0123").setRegion("0321"))
            .joIsRegisteredWithRTL(YES).build();
    }

    public CaseData buildJudgmentOnlineCaseDataWithDeterminationMeans() {
        return build().toBuilder()
            .ccdState(CaseState.All_FINAL_ORDERS_ISSUED)
            .respondent1(PartyBuilder.builder().individual().build())
            .joJudgmentRecordReason(JudgmentRecordedReason.DETERMINATION_OF_MEANS)
            .joInstalmentDetails(new JudgmentInstalmentDetails()
                .setStartDate(LocalDate.of(2022, 12, 12))
                .setAmount("120")
                .setPaymentFrequency(PaymentFrequency.MONTHLY))
            .joAmountOrdered("1200")
            .joAmountCostOrdered("1100")
            .joPaymentPlan(new JudgmentPaymentPlan().setType(PaymentPlanSelection.PAY_IN_INSTALMENTS))
            .joOrderMadeDate(LocalDate.of(2022, 12, 12))
            .joIsRegisteredWithRTL(YES).build();
    }

    public CaseData buildJudmentOnlineCaseDataWithConfirmationForReferToJudgeDefenceReceived() {
        return build().toBuilder()
            .ccdState(CaseState.All_FINAL_ORDERS_ISSUED)
            .confirmReferToJudgeDefenceReceived(List.of(ConfirmationToggle.CONFIRM)).build();
    }

    public CaseData buildJudmentOnlineCaseDataWithPaymentImmediately() {
        return build().toBuilder()
            .ccdState(CaseState.All_FINAL_ORDERS_ISSUED)
            .joJudgmentRecordReason(JudgmentRecordedReason.JUDGE_ORDER)
            .joAmountOrdered("1200")
            .joAmountCostOrdered("1100")
            .joPaymentPlan(new JudgmentPaymentPlan().setType(PaymentPlanSelection.PAY_IMMEDIATELY))
            .joOrderMadeDate(LocalDate.of(2022, 12, 12))
            .caseManagementLocation(new CaseLocationCivil().setBaseLocation("0123").setRegion("0321"))
            .respondent1(PartyBuilder.builder().soleTrader().build())
            .joIsRegisteredWithRTL(YES).build();
    }

    public CaseData buildJudmentOnlineCaseDataWithPaymentImmediatelyWithOldAddress() {
        Address oldAddress = new Address();
        oldAddress.setAddressLine1("Line 1 test again for more than 35 characters");
        oldAddress.setAddressLine2("Line 1 test again for more than 35 characters");
        oldAddress.setAddressLine3("Line 1 test again for more than 35 characters");
        oldAddress.setCounty("Line 1 test again for more than 35 characters");
        oldAddress.setPostCode("Line 1 test again for more than 35 characters");
        oldAddress.setPostTown("Line 1 test again for more than 35 characters");

        return build().toBuilder()
            .ccdState(All_FINAL_ORDERS_ISSUED)
            .joJudgmentRecordReason(JudgmentRecordedReason.JUDGE_ORDER)
            .joAmountOrdered("1200")
            .joAmountCostOrdered("1100")
            .joPaymentPlan(new JudgmentPaymentPlan().setType(PaymentPlanSelection.PAY_IMMEDIATELY))
            .joOrderMadeDate(LocalDate.of(2022, 12, 12))
            .caseManagementLocation(new CaseLocationCivil().setBaseLocation("0123").setRegion("0321"))
            .respondent1(Party.builder()
                .type(Party.Type.INDIVIDUAL)
                .individualTitle("Mr.")
                .individualFirstName("Alex")
                .individualLastName(
                    "Richards Extra long name which exceeds 70 characters need to be trimmed down")
                .partyName(
                    "Mr. Alex Richards Extra long name which exceeds 70 characters need to be trimmed down")
                .partyEmail("respondent1@gmail.com")
                .primaryAddress(oldAddress)
                .build())
            .joIsRegisteredWithRTL(YES).build();
    }

    public CaseData buildJudgmentOnlineCaseDataWithPaymentByDate() {
        return build().toBuilder()
            .ccdState(CaseState.All_FINAL_ORDERS_ISSUED)
            .respondent1(PartyBuilder.builder().organisation().build())
            .joJudgmentRecordReason(JudgmentRecordedReason.JUDGE_ORDER)
            .joAmountOrdered("1200")
            .joAmountCostOrdered("1100")
            .joPaymentPlan(new JudgmentPaymentPlan()
                .setType(PaymentPlanSelection.PAY_BY_DATE)
                .setPaymentDeadlineDate(LocalDate.of(2023, 12, 12)))
            .joOrderMadeDate(LocalDate.of(2022, 12, 12))
            .joIsRegisteredWithRTL(YES)
            .caseManagementLocation(new CaseLocationCivil().setBaseLocation("0123").setRegion("0321"))
            .build();
    }

    public CaseData buildJudgmentOnlineCaseDataWithPaymentByDate_Multi_party() {
        return build().toBuilder()
            .ccdState(CaseState.All_FINAL_ORDERS_ISSUED)
            .respondent1(PartyBuilder.builder().organisation().build())
            .addRespondent2(YES)
            .respondent2(PartyBuilder.builder().individual().build())
            .joJudgmentRecordReason(JudgmentRecordedReason.JUDGE_ORDER)
            .joAmountOrdered("1200")
            .joAmountCostOrdered("1100")
            .joPaymentPlan(new JudgmentPaymentPlan()
                .setType(PaymentPlanSelection.PAY_BY_DATE)
                .setPaymentDeadlineDate(LocalDate.of(2023, 12, 12)))
            .joOrderMadeDate(LocalDate.of(2022, 12, 12))
            .joIsRegisteredWithRTL(YES)
            .caseManagementLocation(new CaseLocationCivil().setBaseLocation("0123").setRegion("0321"))
            .build();
    }

    public CaseData buildJudgmentOnlineCaseWithMarkJudgementPaidAfter31Days() {
        JudgmentPaidInFull paidInFull = new JudgmentPaidInFull();
        paidInFull.setDateOfFullPaymentMade(LocalDate.now().plusDays(35));
        paidInFull.setConfirmFullPaymentMade(List.of("CONFIRMED"));

        return build().toBuilder()
            .ccdState(CaseState.All_FINAL_ORDERS_ISSUED)
            .respondent1(PartyBuilder.builder().soleTrader().build())
            .joOrderMadeDate(LocalDate.of(2023, 3, 1))
            .joJudgmentPaidInFull(paidInFull)
            .joIsRegisteredWithRTL(YES)
            .activeJudgment(new JudgmentDetails().setIssueDate(LocalDate.now()))
            .locationName("Barnet Court")
            .legacyCaseReference("000MC015")
            .build();
    }

    public CaseData buildJudgmentOnlineCaseWithMarkJudgementPaidWithin31Days() {
        JudgmentPaidInFull paidInFull = new JudgmentPaidInFull();
        paidInFull.setDateOfFullPaymentMade(LocalDate.now().plusDays(15));
        paidInFull.setConfirmFullPaymentMade(List.of("CONFIRMED"));

        return build().toBuilder()
            .ccdState(CaseState.All_FINAL_ORDERS_ISSUED)
            .respondent1(PartyBuilder.builder().soleTrader().build())
            .joOrderMadeDate(LocalDate.of(2023, 3, 1))
            .joJudgmentPaidInFull(paidInFull)
            .joIsRegisteredWithRTL(YES)
            .caseManagementLocation(new CaseLocationCivil()
                .setBaseLocation("231596")
                .setRegion("2"))
            .legacyCaseReference("000MC015")
            .activeJudgment(new JudgmentDetails().setIssueDate(LocalDate.now()))
            .build();
    }

    public CaseData buildJudgmentOnlineCaseWithMarkJudgementPaidWithin31DaysForCosc() {

        CaseData caseData = buildJudgmentOnlineCaseWithMarkJudgementPaidWithin31Days();
        JudgmentDetails activeJudgment = new JudgmentDetails()
            .setDefendant1Name("Test name")
            .setDefendant1Address(new JudgmentAddress())
            .setFullyPaymentMadeDate(LocalDate.now().plusDays(15))
            .setState(JudgmentState.SATISFIED)
            .setTotalAmount("90000")
            .setIssueDate(LocalDate.now());
        caseData.setActiveJudgment(activeJudgment);
        return caseData;
    }

    public CaseData buildJudgmentOnlineCaseWithMarkJudgementPaidAfter31DaysForCosc() {
        CaseData caseData = buildJudgmentOnlineCaseWithMarkJudgementPaidAfter31Days();
        JudgmentDetails activeJudgment = new JudgmentDetails()
            .setDefendant1Name("Test name")
            .setDefendant1Address(new JudgmentAddress())
            .setFullyPaymentMadeDate(null)
            .setState(JudgmentState.CANCELLED)
            .setTotalAmount("90000")
            .setIssueDate(LocalDate.now());
        caseData.setActiveJudgment(activeJudgment);
        return caseData;
    }

    public CaseData getDefaultJudgment1v1Case() {
        atStateNotificationAcknowledged();
        return build().toBuilder()
            .respondent1ResponseDeadline(LocalDateTime.now().minusDays(15))
            .partialPayment(YesOrNo.YES)
            .paymentTypeSelection(DJPaymentTypeSelection.IMMEDIATELY)
            .partialPaymentAmount("10")
            .totalClaimAmount(BigDecimal.valueOf(1010))
            .paymentConfirmationDecisionSpec(YesOrNo.YES)
            .partialPayment(YesOrNo.YES)
            .caseManagementLocation(new CaseLocationCivil().setBaseLocation("0123").setRegion("0321"))
            .defendantDetailsSpec(DynamicList.builder()
                .value(DynamicListElement.builder()
                    .label("Test User")
                    .build())
                .build())
            .build();
    }

    public CaseData getDefaultJudgment1v2DivergentCase() {
        return CaseDataBuilder.builder().atStateNotificationAcknowledged().build().toBuilder()
            .respondent1ResponseDeadline(LocalDateTime.now().minusDays(15))
            .applicant1(PartyBuilder.builder().individual().build())
            .respondent1(PartyBuilder.builder().individual().build())
            .respondent2(PartyBuilder.builder().individual().build())
            .addRespondent2(YesOrNo.YES)
            .respondent2SameLegalRepresentative(YesOrNo.YES)
            .partialPaymentAmount("10")
            .totalClaimAmount(BigDecimal.valueOf(1010))
            .paymentTypeSelection(DJPaymentTypeSelection.IMMEDIATELY)
            .paymentConfirmationDecisionSpec(YesOrNo.YES)
            .partialPayment(YesOrNo.YES)
            .caseManagementLocation(new CaseLocationCivil().setBaseLocation("0123").setRegion("0321"))
            .defendantDetailsSpec(DynamicList.builder()
                .value(DynamicListElement.builder()
                    .label("John Smith")
                    .build())
                .build())
            .build();
    }

    public CaseData getDefaultJudgment1v1CaseJudgmentPaid() {
        JudgmentPaidInFull paidInFull = new JudgmentPaidInFull();
        paidInFull.setDateOfFullPaymentMade(LocalDate.now().plusDays(15));
        paidInFull.setConfirmFullPaymentMade(List.of("CONFIRMED"));

        return build().toBuilder()
            .respondent1ResponseDeadline(LocalDateTime.now().minusDays(15))
            .partialPayment(YesOrNo.YES)
            .paymentTypeSelection(DJPaymentTypeSelection.IMMEDIATELY)
            .partialPaymentAmount("10")
            .totalClaimAmount(BigDecimal.valueOf(1010))
            .paymentConfirmationDecisionSpec(YesOrNo.YES)
            .partialPayment(YesOrNo.YES)
            .caseManagementLocation(new CaseLocationCivil().setBaseLocation("0123").setRegion("0321"))
            .defendantDetailsSpec(DynamicList.builder()
                .value(DynamicListElement.builder()
                    .label("Test User")
                    .build())
                .build())
            .joJudgmentPaidInFull(paidInFull)
            .activeJudgment(new JudgmentDetails().setIssueDate(LocalDate.now()))
            .build();
    }

    public CaseDataBuilder setUnassignedCaseListDisplayOrganisationReferences() {
        this.unassignedCaseListDisplayOrganisationReferences = "Organisation references String";
        return this;
    }

    public CaseDataBuilder setCaseListDisplayDefendantSolicitorReferences(boolean isOneDefendantSolicitor) {
        if (!isOneDefendantSolicitor) {
            this.caseListDisplayDefendantSolicitorReferences =
                this.solicitorReferences.getRespondentSolicitor1Reference() + this.respondentSolicitor2Reference;
        } else {
            this.caseListDisplayDefendantSolicitorReferences =
                this.solicitorReferences.getRespondentSolicitor1Reference();
        }
        return this;
    }

    public CaseDataBuilder setCoSClaimDetailsWithDate(boolean setCos1, boolean setCos2,
                                                      LocalDate cos1Date, LocalDate deemed1Date, LocalDate cos2Date, LocalDate deemed2Date,
                                                      boolean file1, boolean file2) {
        List<Element<Document>> files = wrapElements(new Document()
            .setDocumentUrl("fake-url")
            .setDocumentFileName("file-name")
            .setDocumentBinaryUrl("binary-url"));
        List<Element<Document>> files2 = wrapElements(new Document()
            .setDocumentUrl("fake-url2")
            .setDocumentFileName("file-name2")
            .setDocumentBinaryUrl("binary-url2"));
        ArrayList<String> cosUIStatement = new ArrayList<>();
        cosUIStatement.add("CERTIFIED");
        if (setCos1) {
            CertificateOfService cos1 = new CertificateOfService()
                .setCosDateOfServiceForDefendant(cos1Date)
                .setCosDateDeemedServedForDefendant(deemed1Date);
            if (file1) {
                cos1.setCosEvidenceDocument(files);
            }
            this.cosNotifyClaimDetails1 = cos1;
        }
        if (setCos2) {
            CertificateOfService cos2 = new CertificateOfService()
                .setCosDateOfServiceForDefendant(cos2Date)
                .setCosDateDeemedServedForDefendant(deemed2Date);
            if (file2) {
                cos2.setCosEvidenceDocument(files2);
            }
            this.cosNotifyClaimDetails2 = cos2;
        }
        return this;
    }

    public CaseDataBuilder ccjPaymentDetails(CCJPaymentDetails ccjPaymentDetails) {
        this.ccjPaymentDetails = ccjPaymentDetails;
        return this;
    }

    public CaseDataBuilder specRespondent1Represented(YesOrNo specRespondent1Represented) {
        this.specRespondent1Represented = specRespondent1Represented;
        return this;
    }

    public CaseDataBuilder claimFee(Fee fee) {
        this.claimFee = fee;
        return this;
    }

    public CaseDataBuilder hearingFee(Fee fee) {
        this.hearingFee = fee;
        return this;
    }

    public CaseDataBuilder totalInterest(BigDecimal interest) {
        this.totalInterest = interest;
        return this;
    }

    public CaseDataBuilder applicant1AcceptAdmitAmountPaidSpec(YesOrNo isPaymemtAccepted) {
        this.applicant1AcceptAdmitAmountPaidSpec = isPaymemtAccepted;
        return this;
    }

    public CaseDataBuilder applicant1AcceptPartAdmitPaymentPlanSpec(YesOrNo isPartPaymentAccepted) {
        this.applicant1AcceptPartAdmitPaymentPlanSpec = isPartPaymentAccepted;
        return this;
    }

    public CaseDataBuilder respondToAdmittedClaimOwingAmountPounds(BigDecimal admitedCliaimAmount) {
        this.respondToAdmittedClaimOwingAmountPounds = admitedCliaimAmount;
        return this;
    }

    public CaseDataBuilder addApplicant1ExpertsAndWitnesses() {
        this.applicant1DQ = applicant1DQ.copy()
            .setApplicant1DQExperts(new Experts()
                .setExpertRequired(YES)
                .setDetails(wrapElements(
                    new Expert()
                        .setFirstName("Applicant")
                        .setLastName("Expert")
                )))
            .setApplicant1DQWitnesses(new Witnesses()
                .setWitnessesToAppear(YES)
                .setDetails(wrapElements(
                    new Witness()
                        .setFirstName("Applicant")
                        .setLastName("Witness")
                )));
        this.applicantExperts = wrapElements(new PartyFlagStructure()
            .setPartyID("app-1-expert-party-id")
            .setFirstName("Applicant")
            .setLastName("Expert")
            );
        this.applicantWitnesses = wrapElements(new PartyFlagStructure()
            .setPartyID("app-1-witness-party-id")
            .setFirstName("Applicant")
            .setLastName("Witness")
            );
        return this;
    }

    public CaseDataBuilder addApplicant2ExpertsAndWitnesses() {
        this.applicant2DQ = applicant2DQ.copy()
            .setApplicant2DQExperts(new Experts()
                .setExpertRequired(YES)
                .setDetails(wrapElements(
                    new Expert()
                        .setFirstName("Applicant Two")
                        .setLastName("Expert")
                )))
            .setApplicant2DQWitnesses(new Witnesses()
                .setWitnessesToAppear(YES)
                .setDetails(wrapElements(
                    new Witness()
                        .setFirstName("Applicant Two")
                        .setLastName("Witness")
                )));
        this.applicantExperts = wrapElements(new PartyFlagStructure()
            .setPartyID("app-2-expert-party-id")
            .setFirstName("Applicant Two")
            .setLastName("Expert")
            );
        this.applicantWitnesses = wrapElements(new PartyFlagStructure()
            .setPartyID("app-2-witness-party-id")
            .setFirstName("Applicant Two")
            .setLastName("Witness")
            );
        return this;
    }

    public CaseDataBuilder addRespondent1ExpertsAndWitnesses() {
        this.respondent1DQ = respondent1DQ.copy()
            .setRespondent1DQExperts(new Experts()
                .setExpertRequired(YES)
                .setDetails(wrapElements(
                    new Expert()
                        .setFirstName("Respondent")
                        .setLastName("Expert")
                )))
            .setRespondent1DQWitnesses(new Witnesses()
                .setWitnessesToAppear(YES)
                .setDetails(wrapElements(
                    new Witness()
                        .setFirstName("Respondent")
                        .setLastName("Witness")
                )));
        this.respondent1Experts = wrapElements(new PartyFlagStructure()
            .setPartyID("res-1-expert-party-id")
            .setFirstName("Respondent")
            .setLastName("Expert")
            );
        this.respondent1Witnesses = wrapElements(new PartyFlagStructure()
            .setPartyID("res-1-witness-party-id")
            .setFirstName("Respondent")
            .setLastName("Witness")
            );
        return this;
    }

    public CaseDataBuilder addRespondent2ExpertsAndWitnesses() {
        this.respondent2DQ = respondent2DQ.copy()
            .setRespondent2DQExperts(new Experts()
                .setExpertRequired(YES)
                .setDetails(wrapElements(
                    new Expert()
                        .setFirstName("Respondent Two")
                        .setLastName("Expert")
                )))
            .setRespondent2DQWitnesses(new Witnesses()
                .setWitnessesToAppear(YES)
                .setDetails(wrapElements(
                    new Witness()
                        .setFirstName("Respondent Two")
                        .setLastName("Witness")
                )));
        this.respondent2Experts = wrapElements(new PartyFlagStructure()
            .setPartyID("res-2-expert-party-id")
            .setFirstName("Respondent Two")
            .setLastName("Expert")
            );
        this.respondent2Witnesses = wrapElements(new PartyFlagStructure()
            .setPartyID("res-2-witness-party-id")
            .setFirstName("Respondent Two")
            .setLastName("Witness")
            );
        return this;
    }

    public CaseDataBuilder withCaseLevelFlags() {
        this.caseFlags = new Flags()
            .setDetails(wrapElements(List.of(
                new FlagDetail()
                    .setFlagCode("123")
                    .setStatus("Active"),
                new FlagDetail()
                    .setFlagCode("456")
                    .setStatus("Inactive"))));
        return this;
    }

    public CaseDataBuilder withApplicant1Flags() {
        return withApplicant1Flags(flagDetails());
    }

    public CaseDataBuilder withApplicant1Flags(List<Element<FlagDetail>> flags) {
        this.applicant1 = applicant1.toBuilder()
            .partyID("app-1-party-id")
            .flags(new Flags()
                .setPartyName(applicant1.getPartyName())
                .setRoleOnCase("Claimant 1")
                .setDetails(flags)
                ).build();
        return this;
    }

    public CaseDataBuilder withApplicant1WitnessFlags() {
        this.applicantWitnesses = wrapElements(new PartyFlagStructure()
            .setFirstName("W first")
            .setLastName("W last")
            .setFlags(new Flags()
                .setPartyName("W First W Last")
                .setRoleOnCase("Claimant 1 Witness")
                .setDetails(flagDetails()))
            );
        return this;
    }

    public CaseDataBuilder withApplicant1ExpertFlags() {
        this.applicantExperts = wrapElements(new PartyFlagStructure()
            .setFirstName("E first")
            .setLastName("E last")
            .setFlags(new Flags()
                .setPartyName("E First E Last")
                .setRoleOnCase("Claimant 1 Expert")
                .setDetails(flagDetails()))
            );
        return this;
    }

    public CaseDataBuilder withApplicant1LitigationFriendFlags() {
        this.applicant1LitigationFriend = applicant1LitigationFriend.copy()
            .setFlags(new Flags()
                .setPartyName(applicant1LitigationFriend.getFullName())
                .setRoleOnCase("Claimant 1 Litigation Friend")
                .setDetails(flagDetails()));
        return this;
    }

    public CaseDataBuilder withApplicant2Flags() {
        this.applicant2 = applicant2.toBuilder()
            .partyID("app-2-party-id")
            .flags(new Flags()
                .setPartyName(applicant2.getPartyName())
                .setRoleOnCase("Claimant 2")
                .setDetails(flagDetails()))
            .build();
        return this;
    }

    public CaseDataBuilder withApplicant2WitnessFlags() {
        this.applicantWitnesses = wrapElements(new PartyFlagStructure()
            .setFirstName("W first")
            .setLastName("W last")
            .setFlags(new Flags()
                .setPartyName("W First W Last")
                .setRoleOnCase("Claimant 2 Witness")
                .setDetails(flagDetails()))
            );
        return this;
    }

    public CaseDataBuilder withApplicant2ExpertFlags() {
        this.applicantExperts = wrapElements(new PartyFlagStructure()
            .setFirstName("E first")
            .setLastName("E last")
            .setFlags(new Flags()
                .setPartyName("E First E Last")
                .setRoleOnCase("Claimant 2 Expert")
                .setDetails(flagDetails()))
            );
        return this;
    }

    public CaseDataBuilder withApplicant2LitigationFriendFlags() {
        this.applicant2LitigationFriend = applicant2LitigationFriend.copy()
            .setFlags(new Flags()
                .setPartyName(applicant2LitigationFriend.getFullName())
                .setRoleOnCase("Claimant 2 Litigation Friend")
                .setDetails(flagDetails()));
        return this;
    }

    public CaseDataBuilder withRespondent1LitigationFriendFlags() {
        return withRespondent1LitigationFriendFlags(flagDetails());
    }

    public CaseDataBuilder withRespondent1LitigationFriendFlags(List<Element<FlagDetail>> flags) {
        this.respondent1LitigationFriend = respondent1LitigationFriend.copy()
            .setPartyID("res-1-litfriend-party-id")
            .setFlags(new Flags()
                .setPartyName(respondent1LitigationFriend.getFullName())
                .setRoleOnCase("Defendant 1 Litigation Friend")
                .setDetails(flags));
        return this;
    }

    public CaseDataBuilder withRespondent1Flags() {
        return withRespondent1Flags(flagDetails());
    }

    public CaseDataBuilder withRespondent1Flags(List<Element<FlagDetail>> flags) {
        this.respondent1 = respondent1.toBuilder()
            .partyID("res-1-party-id")
            .flags(new Flags()
                .setPartyName(respondent1.getPartyName())
                .setRoleOnCase("Defendant 1")
                .setDetails(flags))
            .build();
        return this;
    }

    public CaseDataBuilder withRespondent1WitnessFlags() {
        this.respondent1Witnesses = wrapElements(
            new PartyFlagStructure()
                .setPartyID("res-1-witness-party-id")
                .setFirstName("W first")
                .setLastName("W last")
                .setFlags(new Flags()
                    .setPartyName("W First W Last")
                    .setRoleOnCase("Defendant 1 Witness")
                    .setDetails(flagDetails()))
                );
        return this;
    }

    public CaseDataBuilder withRespondent1ExpertFlags() {
        this.respondent1Experts = wrapElements(
            new PartyFlagStructure()
                .setPartyID("res-1-expert-party-id")
                .setFirstName("E first")
                .setLastName("E last")
                .setFlags(new Flags()
                    .setPartyName("E First E Last")
                    .setRoleOnCase("Defendant 1 Expert")
                    .setDetails(flagDetails()))
                );
        return this;
    }

    public CaseDataBuilder withRespondent2Flags() {
        this.respondent2 = respondent2.toBuilder()
            .partyID("res-2-party-id")
            .flags(new Flags()
                .setPartyName(respondent2.getPartyName())
                .setRoleOnCase("Defendant 2")
                .setDetails(flagDetails()))
            .build();
        return this;
    }

    public CaseDataBuilder withRespondent2ExpertFlags() {
        this.respondent2Experts = wrapElements(new PartyFlagStructure()
            .setFirstName("E first")
            .setLastName("E last")
            .setFlags(new Flags()
                .setPartyName("E First E Last")
                .setRoleOnCase("Defendant 2 Expert")
                .setDetails(flagDetails()))
            );
        return this;
    }

    public CaseDataBuilder withRespondent2WitnessFlags() {
        this.respondent2Witnesses = wrapElements(new PartyFlagStructure()
            .setFirstName("W first")
            .setLastName("W last")
            .setFlags(new Flags()
                .setPartyName("W First W Last")
                .setRoleOnCase("Defendant 2 Witness")
                .setDetails(flagDetails()))
            );
        return this;
    }

    public CaseDataBuilder withRespondent2LitigationFriendFlags() {
        this.respondent2LitigationFriend = respondent2LitigationFriend.copy()
            .setFlags(new Flags()
                .setPartyName(respondent2LitigationFriend.getFullName())
                .setRoleOnCase("Defendant 2 Litigation Friend")
                .setDetails(flagDetails()));
        return this;
    }

    public CaseDataBuilder withApplicant1LRIndividualFlags() {
        this.applicant1LRIndividuals = wrapElements(
            new PartyFlagStructure()
                .setPartyID("app-1-lr-individual-party-id")
                .setFirstName("First")
                .setLastName("Last")
                .setFlags(new Flags()
                    .setPartyName("First Last")
                    .setRoleOnCase("App 1 Lr Individual")
                    .setDetails(flagDetails()))
                );
        return this;
    }

    public CaseDataBuilder withRespondent1LRIndividualFlags() {
        this.respondent1LRIndividuals = wrapElements(
            new PartyFlagStructure()
                .setPartyID("res-1-lr-individual-party-id")
                .setFirstName("First")
                .setLastName("Last")
                .setFlags(new Flags()
                    .setPartyName("First Last")
                    .setRoleOnCase("Res 1 Lr Individual")
                    .setDetails(flagDetails()))
                );
        return this;
    }

    public CaseDataBuilder withRespondent2LRIndividualFlags() {
        this.respondent2LRIndividuals = wrapElements(
            new PartyFlagStructure()
                .setPartyID("res-2-lr-individual-party-id")
                .setFirstName("First")
                .setLastName("Last")
                .setFlags(new Flags()
                    .setPartyName("First Last")
                    .setRoleOnCase("Res 2 Lr Individual")
                    .setDetails(flagDetails()))
                );
        return this;
    }

    public CaseDataBuilder withApplicant1OrgIndividualFlags() {
        this.applicant1OrgIndividuals = wrapElements(
            new PartyFlagStructure()
                .setPartyID("app-1-org-individual-party-id")
                .setFirstName("First")
                .setLastName("Last")
                .setFlags(new Flags()
                    .setPartyName("First Last")
                    .setRoleOnCase("App 1 Org Individual")
                    .setDetails(flagDetails()))
                );
        return this;
    }

    public CaseDataBuilder withApplicant2OrgIndividualFlags() {
        this.applicant2OrgIndividuals = wrapElements(
            new PartyFlagStructure()
                .setPartyID("app-2-org-individual-party-id")
                .setFirstName("First")
                .setLastName("Last")
                .setFlags(new Flags()
                    .setPartyName("First Last")
                    .setRoleOnCase("App 2 Org Individual")
                    .setDetails(flagDetails()))
                );
        return this;
    }

    public CaseDataBuilder withRespondent1OrgIndividualFlags() {
        this.respondent1OrgIndividuals = wrapElements(
            new PartyFlagStructure()
                .setPartyID("res-1-org-individual-party-id")
                .setFirstName("First")
                .setLastName("Last")
                .setFlags(new Flags()
                    .setPartyName("First Last")
                    .setRoleOnCase("Res 1 Org Individual")
                    .setDetails(flagDetails()))
                );
        return this;
    }

    public CaseDataBuilder withRespondent2OrgIndividualFlags() {
        this.respondent2OrgIndividuals = wrapElements(
            new PartyFlagStructure()
                .setPartyID("res-2-org-individual-party-id")
                .setFirstName("First")
                .setLastName("Last")
                .setFlags(new Flags()
                    .setPartyName("First Last")
                    .setRoleOnCase("Res 2 Org Individual")
                    .setDetails(flagDetails()))
                );
        return this;
    }

    public List<Element<FlagDetail>> flagDetails() {
        FlagDetail details1 = new FlagDetail()
            .setName("Vulnerable user")
            .setFlagComment("comment")
            .setFlagCode("AB001")
            .setHearingRelevant(YES)
            .setStatus("Active")
            .setDateTimeCreated(LocalDateTime.of(2024, 1, 1, 9, 0, 0))
            .setDateTimeModified(LocalDateTime.of(2024, 2, 1, 12, 0, 0));

        FlagDetail details2 = new FlagDetail()
            .setName("Flight risk")
            .setFlagComment("comment")
            .setFlagCode("SM001")
            .setHearingRelevant(YES)
            .setStatus("Active")
            .setDateTimeCreated(LocalDateTime.of(2024, 1, 1, 9, 0, 0))
            .setDateTimeModified(LocalDateTime.of(2024, 2, 1, 12, 0, 0));

        FlagDetail details3 = new FlagDetail()
            .setName("Audio/Video evidence")
            .setFlagComment("comment")
            .setFlagCode("RA001")
            .setHearingRelevant(NO)
            .setStatus("Active")
            .setDateTimeCreated(LocalDateTime.of(2024, 1, 1, 9, 0, 0))
            .setDateTimeModified(LocalDateTime.of(2024, 2, 1, 12, 0, 0));

        FlagDetail details4 = new FlagDetail()
            .setName("Other")
            .setFlagComment("comment")
            .setFlagCode("AB001")
            .setHearingRelevant(YES)
            .setStatus("Inactive")
            .setDateTimeCreated(LocalDateTime.of(2024, 1, 1, 9, 0, 0))
            .setDateTimeModified(LocalDateTime.of(2024, 2, 1, 12, 0, 0));

        return wrapElements(details1, details2, details3, details4);
    }

    public CaseDataBuilder applicant1PartAdmitIntentionToSettleClaimSpec(YesOrNo intentionToSettle) {
        this.applicant1PartAdmitIntentionToSettleClaimSpec = intentionToSettle;
        return this;
    }

    public CaseDataBuilder responseClaimTrack(String claimType) {
        this.responseClaimTrack = claimType;
        return this;
    }

    public CaseDataBuilder setClaimantMediationFlag(YesOrNo response) {
        respondent1MediationRequired = response;
        return this;
    }

    public CaseDataBuilder setDefendantMediationFlag(YesOrNo response) {
        respondent1MediationRequired = response;
        return this;
    }

    public CaseDataBuilder setDefendant2MediationFlag(YesOrNo response) {
        respondent2MediationRequired = response;
        return this;
    }

    public CaseDataBuilder applicant1PartAdmitConfirmAmountPaidSpec(YesOrNo confirmation) {
        this.applicant1PartAdmitConfirmAmountPaidSpec = confirmation;
        return this;
    }

    public CaseDataBuilder defendantSingleResponseToBothClaimants(YesOrNo response) {
        this.defendantSingleResponseToBothClaimants = response;
        return this;
    }

    public CaseDataBuilder caseDataLip(CaseDataLiP caseDataLiP) {
        this.caseDataLiP = caseDataLiP;
        return this;
    }

    public CaseDataBuilder feePaymentOutcomeDetails(FeePaymentOutcomeDetails details) {
        this.feePaymentOutcomeDetails = details;
        return this;
    }

    public CaseDataBuilder specClaim1v1LrVsLip() {
        this.caseAccessCategory = SPEC_CLAIM;
        this.respondent1Represented = NO;
        this.ccdCaseReference = CASE_ID;
        return this;
    }

    public CaseDataBuilder specClaim1v1LrVsLipBilingual() {
        this.caseAccessCategory = SPEC_CLAIM;
        this.respondent1Represented = NO;
        this.ccdCaseReference = CASE_ID;
        this.caseDataLiP = new CaseDataLiP()
            .setRespondent1LiPResponse(new RespondentLiPResponse().setRespondent1ResponseLanguage(Language.BOTH.toString()));
        setClaimTypeToSpecClaim();
        return this;
    }

    public CaseDataBuilder specClaim1v1LipvLr() {
        atStateClaimDraft();
        legacyCaseReference(LEGACY_CASE_REFERENCE);
        this.caseAccessCategory = SPEC_CLAIM;
        this.respondent1Represented = YES;
        this.applicant1Represented = NO;
        this.claimantBilingualLanguagePreference = Language.ENGLISH.toString();
        this.ccdCaseReference = CASE_ID;
        return this;
    }

    public CaseDataBuilder specClaim1v1LipvLrBilingual() {
        specClaim1v1LipvLr();
        this.claimantBilingualLanguagePreference = Language.BOTH.toString();
        return this;
    }

    public CaseDataBuilder enableRespondent2ResponseFlag() {
        this.claimant2ResponseFlag = YES;
        return this;
    }

    public CaseDataBuilder setSpecClaimResponseTimelineList(TimelineUploadTypeSpec timelineUploadTypeSpec) {
        this.specClaimResponseTimelineList = timelineUploadTypeSpec;
        return this;
    }

    public CaseDataBuilder setSpecClaimResponseTimelineList2(TimelineUploadTypeSpec timelineUploadTypeSpec2) {
        this.specClaimResponseTimelineList2 = timelineUploadTypeSpec2;
        return this;
    }

    public CaseDataBuilder setDefenceAdmitPartEmploymentTypeRequired(YesOrNo yesOrNo) {
        this.defenceAdmitPartEmploymentTypeRequired = defenceAdmitPartEmploymentTypeRequired;
        return this;
    }

    public CaseDataBuilder specDefenceFullAdmitted2Required(YesOrNo yesOrNo) {
        this.specDefenceFullAdmitted2Required = specDefenceFullAdmitted2Required;
        return this;
    }

    public CaseDataBuilder defenceAdmitPartPaymentTimeRouteRequired(
        RespondentResponsePartAdmissionPaymentTimeLRspec respondentResponsePartAdmissionPaymentTimeLRspec) {
        this.defenceAdmitPartPaymentTimeRouteRequired = respondentResponsePartAdmissionPaymentTimeLRspec;
        return this;
    }

    public CaseDataBuilder showResponseOneVOneFlag(ResponseOneVOneShowTag showResponseOneVOneFlag) {
        this.showResponseOneVOneFlag = showResponseOneVOneFlag;
        return this;
    }

    public CaseDataBuilder claimantUserDetails(IdamUserDetails claimantUserDetails) {
        this.claimantUserDetails = claimantUserDetails;
        return this;
    }

    public CaseDataBuilder updateDetailsForm(UpdateDetailsForm form) {
        this.updateDetailsForm = form;
        return this;
    }

    public CaseDataBuilder atSmallClaimsWitnessStatementWithNegativeInputs() {
        atStateClaimNotified();
        SmallClaimsWitnessStatement witnessStatement = new SmallClaimsWitnessStatement();
        witnessStatement.setInput2("-3");
        witnessStatement.setInput3("-3");
        this.smallClaimsWitnessStatement = witnessStatement;

        return this;
    }

    public CaseDataBuilder atFastTrackWitnessOfFactWithNegativeInputs() {
        atStateClaimNotified();
        this.fastTrackWitnessOfFact = new FastTrackWitnessOfFact()
            .setInput2("-3")
            .setInput3("-3");

        return this;
    }

    public CaseDataBuilder atSmallClaimsWitnessStatementWithPositiveInputs() {
        atStateClaimNotified();
        SmallClaimsWitnessStatement witnessStatement = new SmallClaimsWitnessStatement();
        witnessStatement.setInput2("3");
        witnessStatement.setInput3("3");
        this.smallClaimsWitnessStatement = witnessStatement;

        return this;
    }

    public CaseDataBuilder atSmallSmallClaimsFlightDelayInputs() {
        atStateClaimNotified();
        this.smallClaimsFlightDelay = new SmallClaimsFlightDelay();
        this.smallClaimsFlightDelay.setRelatedClaimsInput(FLIGHT_DELAY_RELATED_CLAIMS_NOTICE);
        this.smallClaimsFlightDelay.setLegalDocumentsInput(FLIGHT_DELAY_LEGAL_ARGUMENTS_NOTICE);

        return this;
    }

    public CaseDataBuilder atFastTrackWitnessOfFactWithPositiveInputs() {
        atStateClaimNotified();
        this.fastTrackWitnessOfFact = new FastTrackWitnessOfFact()
            .setInput2("3")
            .setInput3("3");

        return this;
    }

    public CaseDataBuilder atTrialHearingWitnessOfFactWithNegativeInputs() {
        atStateClaimNotified();
        this.trialHearingWitnessOfFactDJ = new TrialHearingWitnessOfFact()
            .setInput2("-3")
            .setInput3("-3");

        return this;
    }

    public CaseDataBuilder atStatePriorToRespondToSettlementAgreementDeadline() {
        this.respondent1RespondToSettlementAgreementDeadline = LocalDateTime.now().plusDays(1);
        return this;
    }

    public CaseDataBuilder atStatePastRespondToSettlementAgreementDeadline() {
        this.respondent1RespondToSettlementAgreementDeadline = LocalDateTime.now().minusDays(1);
        return this;
    }

    public CaseDataBuilder addApplicantLRIndividual(String firstName, String lastName) {
        List<Element<PartyFlagStructure>> individual =
            wrapElements(new PartyFlagStructure()
                .setPartyID("app-lr-ind-party-id")
                .setFirstName(firstName)
                .setLastName(lastName)
                .setEmail("abc@def.ghi")
                .setPhone("07777777777")
                );
        if (this.applicant1LRIndividuals != null && !this.applicant1LRIndividuals.isEmpty()) {
            this.applicant1LRIndividuals.addAll(individual);
        } else {
            this.applicant1LRIndividuals = individual;
        }
        return this;
    }

    public CaseDataBuilder addRespondent1LRIndividual(String firstName, String lastName) {
        List<Element<PartyFlagStructure>> individual =
            wrapElements(new PartyFlagStructure()
                .setPartyID("res-1-lr-ind-party-id")
                .setFirstName(firstName)
                .setLastName(lastName)
                .setEmail("abc@def.ghi")
                .setPhone("07777777777")
                );
        if (this.respondent1LRIndividuals != null && !this.respondent1LRIndividuals.isEmpty()) {
            this.respondent1LRIndividuals.addAll(individual);
        } else {
            this.respondent1LRIndividuals = individual;
        }
        return this;
    }

    public CaseDataBuilder addRespondent2LRIndividual(String firstName, String lastName) {
        List<Element<PartyFlagStructure>> individual =
            wrapElements(new PartyFlagStructure()
                .setPartyID("res-2-lr-ind-party-id")
                .setFirstName(firstName)
                .setLastName(lastName)
                .setEmail("abc@def.ghi")
                .setPhone("07777777777")
                );
        if (this.respondent2LRIndividuals != null && !this.respondent2LRIndividuals.isEmpty()) {
            this.respondent2LRIndividuals.addAll(individual);
        } else {
            this.respondent2LRIndividuals = individual;
        }
        return this;
    }

    public CaseDataBuilder addApplicant1OrgIndividual(String firstName, String lastName) {
        List<Element<PartyFlagStructure>> individual =
            wrapElements(new PartyFlagStructure()
                .setPartyID("app-1-org-ind-party-id")
                .setFirstName(firstName)
                .setLastName(lastName)
                .setEmail("abc@def.ghi")
                .setPhone("07777777777")
                );
        if (this.applicant1OrgIndividuals != null && !this.applicant1OrgIndividuals.isEmpty()) {
            this.applicant1OrgIndividuals.addAll(individual);
        } else {
            this.applicant1OrgIndividuals = individual;
        }
        return this;
    }

    public CaseDataBuilder addApplicant2OrgIndividual(String firstName, String lastName) {
        List<Element<PartyFlagStructure>> individual =
            wrapElements(new PartyFlagStructure()
                .setPartyID("app-2-org-ind-party-id")
                .setFirstName(firstName)
                .setLastName(lastName)
                .setEmail("abc@def.ghi")
                .setPhone("07777777777")
                );
        if (this.applicant2OrgIndividuals != null && !this.applicant2OrgIndividuals.isEmpty()) {
            this.applicant2OrgIndividuals.addAll(individual);
        } else {
            this.applicant2OrgIndividuals = individual;
        }
        return this;
    }

    public CaseDataBuilder addRespondent1OrgIndividual(String firstName, String lastName) {
        List<Element<PartyFlagStructure>> individual =
            wrapElements(new PartyFlagStructure()
                .setPartyID("res-1-org-ind-party-id")
                .setFirstName(firstName)
                .setLastName(lastName)
                .setEmail("abc@def.ghi")
                .setPhone("07777777777")
                );
        if (this.respondent1OrgIndividuals != null && !this.respondent1OrgIndividuals.isEmpty()) {
            this.respondent1OrgIndividuals.addAll(individual);
        } else {
            this.respondent1OrgIndividuals = individual;
        }
        return this;
    }

    public CaseDataBuilder addRespondent2OrgIndividual(String firstName, String lastName) {
        List<Element<PartyFlagStructure>> individual =
            wrapElements(new PartyFlagStructure()
                .setPartyID("res-2-org-ind-party-id")
                .setFirstName(firstName)
                .setLastName(lastName)
                .setEmail("abc@def.ghi")
                .setPhone("07777777777")
                );
        if (this.respondent2OrgIndividuals != null && !this.respondent2OrgIndividuals.isEmpty()) {
            this.respondent2OrgIndividuals.addAll(individual);
        } else {
            this.respondent2OrgIndividuals = individual;
        }
        return this;
    }

    public CaseDataBuilder addLiPApplicant1MediationInfo(boolean alternateInfo) {
        if (alternateInfo) {
            if (this.caseDataLiP != null) {
                this.caseDataLiP.setApplicant1LiPResponseCarm(new MediationLiPCarm()
                    .setIsMediationContactNameCorrect(NO)
                    .setAlternativeMediationContactPerson("Alt contact person")
                    .setIsMediationEmailCorrect(NO)
                    .setAlternativeMediationEmail("altemail@mediation.com")
                    .setIsMediationPhoneCorrect(NO)
                    .setAlternativeMediationTelephone("07222222222")
                    .setHasUnavailabilityNextThreeMonths(YES)
                    .setUnavailableDatesForMediation(getMediationUnavailableDates()));
            } else {
                this.caseDataLiP = new CaseDataLiP()
                    .setApplicant1LiPResponseCarm(new MediationLiPCarm()
                        .setIsMediationContactNameCorrect(NO)
                        .setAlternativeMediationContactPerson("Alt contact person")
                        .setIsMediationEmailCorrect(NO)
                        .setAlternativeMediationEmail("altemail@mediation.com")
                        .setIsMediationPhoneCorrect(NO)
                        .setAlternativeMediationTelephone("07222222222")
                        .setHasUnavailabilityNextThreeMonths(YES)
                        .setUnavailableDatesForMediation(getMediationUnavailableDates()));
            }
        } else {
            if (this.caseDataLiP != null) {
                this.caseDataLiP
                    .setApplicant1LiPResponseCarm(new MediationLiPCarm()
                        .setIsMediationContactNameCorrect(YES)
                        .setIsMediationEmailCorrect(YES)
                        .setIsMediationPhoneCorrect(YES)
                        .setHasUnavailabilityNextThreeMonths(YES)
                        .setUnavailableDatesForMediation(getMediationUnavailableDates()))
                    .setApplicant1AdditionalLipPartyDetails(new AdditionalLipPartyDetails()
                        .setContactPerson("Lip contact person"));
            } else {
                this.caseDataLiP = new CaseDataLiP()
                    .setApplicant1LiPResponseCarm(new MediationLiPCarm()
                        .setIsMediationContactNameCorrect(YES)
                        .setIsMediationEmailCorrect(YES)
                        .setIsMediationPhoneCorrect(YES)
                        .setHasUnavailabilityNextThreeMonths(YES)
                        .setUnavailableDatesForMediation(getMediationUnavailableDates()))
                    .setApplicant1AdditionalLipPartyDetails(new AdditionalLipPartyDetails()
                        .setContactPerson("Lip contact person"));
            }
        }
        return this;
    }

    public CaseDataBuilder addLiPRespondent1MediationInfo(boolean alternateInfo) {
        if (alternateInfo) {
            if (this.caseDataLiP != null) {
                this.caseDataLiP.setRespondent1MediationLiPResponseCarm(new MediationLiPCarm()
                    .setIsMediationContactNameCorrect(NO)
                    .setAlternativeMediationContactPerson("Alt contact person")
                    .setIsMediationEmailCorrect(NO)
                    .setAlternativeMediationEmail("altemail@mediation.com")
                    .setIsMediationPhoneCorrect(NO)
                    .setAlternativeMediationTelephone("07222222222")
                    .setHasUnavailabilityNextThreeMonths(YES)
                    .setUnavailableDatesForMediation(getMediationUnavailableDates()));
            } else {
                this.caseDataLiP = new CaseDataLiP()
                    .setRespondent1MediationLiPResponseCarm(new MediationLiPCarm()
                        .setIsMediationContactNameCorrect(NO)
                        .setAlternativeMediationContactPerson("Alt contact person")
                        .setIsMediationEmailCorrect(NO)
                        .setAlternativeMediationEmail("altemail@mediation.com")
                        .setIsMediationPhoneCorrect(NO)
                        .setAlternativeMediationTelephone("07222222222")
                        .setHasUnavailabilityNextThreeMonths(YES)
                        .setUnavailableDatesForMediation(getMediationUnavailableDates()));
            }
        } else {
            if (this.caseDataLiP != null) {
                this.caseDataLiP
                    .setRespondent1MediationLiPResponseCarm(new MediationLiPCarm()
                        .setIsMediationContactNameCorrect(YES)
                        .setIsMediationEmailCorrect(YES)
                        .setIsMediationPhoneCorrect(YES)
                        .setHasUnavailabilityNextThreeMonths(YES)
                        .setUnavailableDatesForMediation(getMediationUnavailableDates()))
                    .setRespondent1LiPResponse(new RespondentLiPResponse()
                        .setRespondent1LiPContactPerson("Lip contact person"));
            } else {
                this.caseDataLiP = new CaseDataLiP()
                    .setRespondent1MediationLiPResponseCarm(new MediationLiPCarm()
                        .setIsMediationContactNameCorrect(YES)
                        .setIsMediationEmailCorrect(YES)
                        .setIsMediationPhoneCorrect(YES)
                        .setHasUnavailabilityNextThreeMonths(YES)
                        .setUnavailableDatesForMediation(getMediationUnavailableDates()))
                    .setRespondent1LiPResponse(new RespondentLiPResponse()
                        .setRespondent1LiPContactPerson("Lip contact person"));
            }
        }
        return this;
    }

    public CaseDataBuilder addApplicant1MediationInfo() {
        MediationContactInformation info = new MediationContactInformation("Contact", "person", "Contact.person@mediation.com", "07888888888");
        this.app1MediationContactInfo = info;

        return this;
    }

    public CaseDataBuilder addApplicant1MediationAvailability() {
        MediationAvailability availability = new MediationAvailability();
        availability.setIsMediationUnavailablityExists(YES);
        availability.setUnavailableDatesForMediation(getMediationUnavailableDates());
        this.app1MediationAvailability = availability;
        return this;
    }

    public CaseDataBuilder addRespondent1MediationInfo() {
        MediationContactInformation info = new MediationContactInformation("Contact", "person", "Contact.person@mediation.com", "07888888888");
        this.resp1MediationContactInfo = info;

        return this;
    }

    public CaseDataBuilder addRespondent1MediationAvailability() {
        MediationAvailability availability = new MediationAvailability();
        availability.setIsMediationUnavailablityExists(YES);
        availability.setUnavailableDatesForMediation(getMediationUnavailableDates());
        this.resp1MediationAvailability = availability;
        return this;
    }

    public CaseDataBuilder addRespondent2MediationInfo() {
        MediationContactInformation info = new MediationContactInformation("Contact", "person", "Contact.person@mediation.com", "07888888888");
        this.resp2MediationContactInfo = info;

        return this;
    }

    public CaseDataBuilder addRespondent2MediationAvailability() {
        MediationAvailability availability = new MediationAvailability();
        availability.setIsMediationUnavailablityExists(YES);
        availability.setUnavailableDatesForMediation(getMediationUnavailableDates());
        this.resp2MediationAvailability = availability;
        return this;
    }

    private List<Element<UnavailableDate>> getMediationUnavailableDates() {
        return wrapElements(List.of(
            UnavailableDate.builder()
                .unavailableDateType(SINGLE_DATE)
                .date(LocalDate.of(2024, 6, 1))
                .build(),
            UnavailableDate.builder()
                .unavailableDateType(SINGLE_DATE)
                .date(LocalDate.of(2024, 6, 7))
                .build(),
            UnavailableDate.builder()
                .unavailableDateType(DATE_RANGE)
                .fromDate(LocalDate.of(2024, 6, 10))
                .toDate(LocalDate.of(2024, 6, 15))
                .build(),
            UnavailableDate.builder()
                .unavailableDateType(DATE_RANGE)
                .fromDate(LocalDate.of(2024, 6, 20))
                .toDate(LocalDate.of(2024, 6, 25)).build()));
    }

    public CaseDataBuilder applicant1RequestedPaymentDateForDefendantSpec(PaymentBySetDate repaymentBySetDate) {
        this.applicant1RequestedPaymentDateForDefendantSpec = repaymentBySetDate;
        return this;
    }

    public CaseDataBuilder eaCourtLocation(YesOrNo eaCourtLocation) {
        this.eaCourtLocation = eaCourtLocation;
        return this;
    }

    public CaseDataBuilder applicant1SuggestInstalmentsPaymentAmountForDefendantSpec(BigDecimal suggestedInstallmentPayment) {
        this.applicant1SuggestInstalmentsPaymentAmountForDefendantSpec = suggestedInstallmentPayment;
        return this;
    }

    public CaseDataBuilder applicant1SuggestInstalmentsRepaymentFrequencyForDefendantSpec(PaymentFrequencyClaimantResponseLRspec repaymentFrequency) {
        this.applicant1SuggestInstalmentsRepaymentFrequencyForDefendantSpec = repaymentFrequency;
        return this;
    }

    public CaseDataBuilder applicant1SuggestInstalmentsFirstRepaymentDateForDefendantSpec(LocalDate firstRepaymentDate) {
        this.applicant1SuggestInstalmentsFirstRepaymentDateForDefendantSpec = firstRepaymentDate;
        return this;
    }

    public CaseDataBuilder uploadMediationByDocumentTypes(List<MediationDocumentsType> documentTypes) {
        if (documentTypes.contains(NON_ATTENDANCE_STATEMENT)) {
            this.res1MediationNonAttendanceDocs = buildMediationNonAttendanceStatement();
        } else if (documentTypes.contains(REFERRED_DOCUMENTS)) {
            this.res1MediationDocumentsReferred = buildMediationDocumentsReferred();
        }
        return this;
    }

    public CaseDataBuilder uploadMediationDocumentsChooseOptions(String partyChosen, List<MediationDocumentsType> documentTypes) {
        List<Element<MediationNonAttendanceStatement>> mediationNonAttendanceStatement;
        List<Element<MediationDocumentsReferredInStatement>> documentsReferred;
        if (documentTypes.contains(NON_ATTENDANCE_STATEMENT)) {
            mediationNonAttendanceStatement = buildMediationNonAttendanceStatement();
        } else {
            mediationNonAttendanceStatement = null;
        }
        if (documentTypes.contains(REFERRED_DOCUMENTS)) {
            documentsReferred = buildMediationDocumentsReferred();
        } else {
            documentsReferred = null;
        }
        UploadMediationDocumentsForm form = new UploadMediationDocumentsForm();
        form.setUploadMediationDocumentsPartyChosen(DynamicList.builder()
            .value(DynamicListElement.builder()
                .code(partyChosen)
                .build())
            .build());
        form.setMediationDocumentsType(documentTypes);
        form.setDocumentsReferredForm(documentsReferred);
        form.setNonAttendanceStatementForm(mediationNonAttendanceStatement);
        this.uploadDocumentsForm = form;
        return this;
    }

    public List<Element<MediationNonAttendanceStatement>> buildMediationNonAttendanceStatement() {
        MediationNonAttendanceStatement statement = new MediationNonAttendanceStatement();
        statement.setYourName("My name");
        statement.setDocument(new Document()
            .setDocumentFileName("Mediation non attendance"));
        statement.setDocumentDate(LocalDate.of(2023, 4, 2));
        statement.setDocumentUploadedDatetime(LocalDateTime.of(2023, 1, 1, 1, 1, 1));
        return wrapElements(statement);
    }

    private List<Element<MediationDocumentsReferredInStatement>> buildMediationDocumentsReferred() {
        MediationDocumentsReferredInStatement statement = new MediationDocumentsReferredInStatement();
        statement.setDocumentType("type");
        statement.setDocument(new Document()
            .setDocumentFileName("Referred documents"));
        statement.setDocumentDate(LocalDate.of(2023, 4, 2));
        statement.setDocumentUploadedDatetime(LocalDateTime.of(2023, 1, 1, 1, 1, 1));
        return wrapElements(statement);
    }

    public CaseDataBuilder atStateRespondent1v1BilingualFlagSet() {
        respondent1ClaimResponseTypeForSpec = RespondentResponseTypeSpec.FULL_ADMISSION;
        ccdState = AWAITING_RESPONDENT_ACKNOWLEDGEMENT;
        return this;
    }

    public CaseDataBuilder hwfFeeType(FeeType feeType) {
        hwfFeeType = feeType;
        return this;
    }

    public CaseDataBuilder claimIssuedHwfDetails(HelpWithFeesDetails details) {
        this.claimIssuedHwfDetails = details;
        return this;
    }

    public CaseDataBuilder hearingHwfDetails(HelpWithFeesDetails details) {
        this.hearingHwfDetails = details;
        return this;
    }

    public static CaseDataBuilder builder() {
        return new CaseDataBuilder();
    }

    public CaseData build() {
        return CaseData.builder()
            // Create Claim
            .caseNameHmctsInternal(caseNameHmctsInternal)
            .legacyCaseReference(legacyCaseReference)
            .defendantUserDetails(defendantUserDetails)
            .helpWithFeesMoreInformationClaimIssue(helpWithFeesMoreInformationClaimIssue)
            .helpWithFeesMoreInformationHearing(helpWithFeesMoreInformationHearing)
            .allocatedTrack(allocatedTrack)
            .generalAppType(generalAppType)
            .generalAppTypeLR(generalAppTypeLR)
            .generalAppVaryJudgementType(generalAppVaryJudgementType)
            .generalAppN245FormUpload(generalAppN245FormUpload)
            .generalAppHearingDate(generalAppHearingDate)
            .solicitorReferences(solicitorReferences)
            .courtLocation(courtLocation)
            .claimValue(claimValue)
            .uploadParticularsOfClaim(uploadParticularsOfClaim)
            .claimType(claimType)
            .claimTypeUnSpec(claimTypeUnSpec)
            .claimTypeOther(claimTypeOther)
            .personalInjuryType(personalInjuryType)
            .personalInjuryTypeOther(personalInjuryTypeOther)
            .applicantSolicitor1PbaAccounts(applicantSolicitor1PbaAccounts)
            .claimFee(claimFee)
            .hearingFee(hearingFee)
            .applicant1(applicant1)
            .applicant2(applicant2)
            .applicant1Represented(applicant1Represented)
            .respondent1(respondent1)
            .respondent2(respondent2)
            .respondent1Represented(respondent1Represented)
            .respondent2Represented(respondent2Represented)
            .respondent1OrgRegistered(respondent1OrgRegistered)
            .respondent2OrgRegistered(respondent2OrgRegistered)
            .respondentSolicitor1EmailAddress(respondentSolicitor1EmailAddress)
            .respondentSolicitor2EmailAddress(respondentSolicitor2EmailAddress)
            .applicantSolicitor1ClaimStatementOfTruth(applicantSolicitor1ClaimStatementOfTruth)
            .claimIssuedPaymentDetails(claimIssuedPaymentDetails)
            .paymentDetails(paymentDetails)
            .hearingFeePaymentDetails(hearingFeePaymentDetails)
            .paymentReference(paymentReference)
            .applicantSolicitor1CheckEmail(applicantSolicitor1CheckEmail)
            .applicantSolicitor1UserDetails(applicantSolicitor1UserDetails)
            .interestClaimOptions(interestClaimOptions)
            .claimInterest(claimInterest)
            .sameRateInterestSelection(sameRateInterestSelection)
            .interestClaimFrom(interestClaimFrom)
            .interestClaimUntil(interestClaimUntil)
            .interestFromSpecificDate(interestFromSpecificDate)
            .breakDownInterestTotal(breakDownInterestTotal)
            .totalClaimAmount(totalClaimAmount)
            //Deadline extension
            .respondentSolicitor1AgreedDeadlineExtension(respondentSolicitor1AgreedDeadlineExtension)
            .respondentSolicitor2AgreedDeadlineExtension(respondentSolicitor2AgreedDeadlineExtension)
            // Acknowledge Claim
            .respondent1ClaimResponseIntentionType(respondent1ClaimResponseIntentionType)
            .respondent2ClaimResponseIntentionType(respondent2ClaimResponseIntentionType)
            // Defendant Response Defendant 1
            .respondent1ClaimResponseType(respondent1ClaimResponseType)
            .respondent1ClaimResponseDocument(respondent1ClaimResponseDocument)
            // Defendant Response Defendant 2
            .respondent2ClaimResponseType(respondent2ClaimResponseType)
            .respondent2ClaimResponseDocument(respondent2ClaimResponseDocument)
            .respondentResponseIsSame(respondentResponseIsSame)
            // Defendant Response 2 Applicants
            .respondent1ClaimResponseTypeToApplicant2(respondent1ClaimResponseTypeToApplicant2)
            // Claimant Response
            .applicant1ProceedWithClaim(applicant1ProceedWithClaim)
            .applicant1ProceedWithClaimMultiParty2v1(applicant1ProceedWithClaimMultiParty2v1)
            .applicant2ProceedWithClaimMultiParty2v1(applicant2ProceedWithClaimMultiParty2v1)
            .applicant1DefenceResponseDocument(applicant1DefenceResponseDocument)
            .claimantDefenceResDocToDefendant2(applicant2DefenceResponseDocument)
            .defendantDetails(defendantDetails)
            .applicant1RepaymentOptionForDefendantSpec(applicant1RepaymentOptionForDefendantSpec)
            .applicant1RequestedPaymentDateForDefendantSpec(applicant1RequestedPaymentDateForDefendantSpec)
            .applicant1SuggestInstalmentsPaymentAmountForDefendantSpec(applicant1SuggestInstalmentsPaymentAmountForDefendantSpec)
            .applicant1SuggestInstalmentsRepaymentFrequencyForDefendantSpec(applicant1SuggestInstalmentsRepaymentFrequencyForDefendantSpec)
            .applicant1SuggestInstalmentsFirstRepaymentDateForDefendantSpec(applicant1SuggestInstalmentsFirstRepaymentDateForDefendantSpec)
            //Case procceds in Caseman
            .claimProceedsInCaseman(claimProceedsInCaseman)
            .claimProceedsInCasemanLR(claimProceedsInCasemanLR)

            .ccdState(ccdState)
            .businessProcess(businessProcess)
            .ccdCaseReference(ccdCaseReference)
            .systemGeneratedCaseDocuments(systemGeneratedCaseDocuments)
            .withdrawClaim(withdrawClaim)
            .discontinueClaim(discontinueClaim)
            .respondent1DQ(respondent1DQ)
            .respondent2DQ(respondent2DQ)
            .applicant1DQ(applicant1DQ)
            .applicant2DQ(applicant2DQ)
            .respondent2DQ(respondent2DQ)
            .respondentSolicitor1OrganisationDetails(respondentSolicitor1OrganisationDetails)
            .applicant1OrganisationPolicy(applicant1OrganisationPolicy)
            .respondent1OrganisationPolicy(respondent1OrganisationPolicy)
            .respondent2OrganisationPolicy(respondent2OrganisationPolicy)
            .addApplicant2(addApplicant2)
            .addRespondent2(addRespondent2)
            .respondent2SameLegalRepresentative(respondent2SameLegalRepresentative)
            .respondent1LitigationFriend(respondent1LitigationFriend)
            .applicant1LitigationFriend(applicant1LitigationFriend)
            .applicant1LitigationFriendRequired(applicant1LitigationFriendRequired)
            .applicant1AcceptFullAdmitPaymentPlanSpec(applicant1AcceptFullAdmitPaymentPlanSpec)
            .applicant2LitigationFriend(applicant2LitigationFriend)
            .applicant2LitigationFriendRequired(applicant2LitigationFriendRequired)
            .respondent1LitigationFriendDate(respondent1LitigationFriendDate)
            .respondent1LitigationFriendCreatedDate(respondent1LitigationFriendCreatedDate)
            .respondent2LitigationFriend(respondent2LitigationFriend)
            .respondent2LitigationFriendDate(respondent2LitigationFriendDate)
            .respondent2LitigationFriendCreatedDate(respondent2LitigationFriendCreatedDate)
            .genericLitigationFriend(genericLitigationFriend)
            //dates
            .submittedDate(submittedDate)
            .issueDate(issueDate)
            .claimNotificationDate(claimNotificationDate)
            .claimDetailsNotificationDate(claimDetailsNotificationDate)
            .paymentSuccessfulDate(paymentSuccessfulDate)
            .claimNotificationDeadline(claimNotificationDeadline)
            .claimDetailsNotificationDate(claimDetailsNotificationDate)
            .claimDetailsNotificationDeadline(claimDetailsNotificationDeadline)
            .servedDocumentFiles(servedDocumentFiles)
            .respondent1ResponseDeadline(respondent1ResponseDeadline)
            .respondent2ResponseDeadline(respondent2ResponseDeadline)
            .claimDismissedDeadline(claimDismissedDeadline)
            .respondent1TimeExtensionDate(respondent1TimeExtensionDate)
            .respondent2TimeExtensionDate(respondent2TimeExtensionDate)
            .respondent1AcknowledgeNotificationDate(respondent1AcknowledgeNotificationDate)
            .respondent2AcknowledgeNotificationDate(respondent2AcknowledgeNotificationDate)
            .respondent1ResponseDate(respondent1ResponseDate)
            .respondent2ResponseDate(respondent2ResponseDate)
            .applicant1ResponseDate(applicant1ResponseDate)
            .applicant2ResponseDate(applicant2ResponseDate)
            .applicant1ResponseDeadline(applicant1ResponseDeadline)
            .takenOfflineDate(takenOfflineDate)
            .takenOfflineByStaffDate(takenOfflineByStaffDate)
            .unsuitableSDODate(unsuitableSDODate)
            .claimDismissedDate(claimDismissedDate)
            .caseDismissedHearingFeeDueDate(caseDismissedHearingFeeDueDate)
            .addLegalRepDeadline(addLegalRepDeadline)
            .addLegalRepDeadlineRes1(addLegalRepDeadlineDefendant1)
            .addLegalRepDeadlineRes2(addLegalRepDeadlineDefendant2)
            .applicantSolicitor1ServiceAddress(applicantSolicitor1ServiceAddress)
            .respondentSolicitor1ServiceAddress(respondentSolicitor1ServiceAddress)
            .respondentSolicitor2ServiceAddress(respondentSolicitor2ServiceAddress)
            .isRespondent1(isRespondent1)
            .isRespondent2(isRespondent2)
            .defendantSolicitorNotifyClaimOptions(defendantSolicitorNotifyClaimOptions)
            .defendantSolicitorNotifyClaimDetailsOptions(defendantSolicitorNotifyClaimDetailsOptions)
            .selectLitigationFriend(selectLitigationFriend)
            .caseNotes(caseNotes)
            .notificationSummary(notificationSummary)
            .hearingDueDate(hearingDueDate)
            .hearingDate(hearingDate)
            //ui field
            .uiStatementOfTruth(uiStatementOfTruth)
            .caseAccessCategory(caseAccessCategory == null ? UNSPEC_CLAIM : caseAccessCategory)
            .caseBundles(caseBundles)
            .respondToClaim(respondToClaim)
            //spec route
            .respondent1ClaimResponseTypeForSpec(respondent1ClaimResponseTypeForSpec)
            .respondToAdmittedClaim(respondToClaim)
            .responseClaimAdmitPartEmployer(responseClaimAdmitPartEmployer)
            //case progression
            .hearingFeePaymentDetails(hearingFeePaymentDetails)
            .hearingDuration(hearingDuration)
            .trialReadyApplicant(trialReadyApplicant)
            .trialReadyRespondent1(trialReadyRespondent1)
            .trialReadyRespondent2(trialReadyRespondent2)
            //workaround fields
            .respondent1Copy(respondent1Copy)
            .respondent2Copy(respondent2Copy)
            .respondToClaimAdmitPartUnemployedLRspec(respondToClaimAdmitPartUnemployedLRspec)
            .respondToClaimAdmitPartLRspec(respondToClaimAdmitPartLRspec)
            .respondent1PartnerAndDependent(respondent1PartnerAndDependent)
            .respondent2PartnerAndDependent(respondent2PartnerAndDependent)
            .respondent1RepaymentPlan(respondent1RepaymentPlan)
            .respondent2RepaymentPlan(respondent2RepaymentPlan)
            .applicantsProceedIntention(applicantsProceedIntention)
            .applicant1ProceedWithClaimAgainstRespondent1MultiParty1v2(
                applicant1ProceedWithClaimAgainstRespondent1MultiParty1v2)
            .applicant1ProceedWithClaimAgainstRespondent2MultiParty1v2(
                applicant1ProceedWithClaimAgainstRespondent2MultiParty1v2)
            .claimant1ClaimResponseTypeForSpec(claimant1ClaimResponseTypeForSpec)
            .claimant2ClaimResponseTypeForSpec(claimant2ClaimResponseTypeForSpec)
            .respondent1ClaimResponseTypeForSpec(respondent1ClaimResponseTypeForSpec)
            .respondent2ClaimResponseTypeForSpec(respondent2ClaimResponseTypeForSpec)
            .responseClaimTrack(responseClaimTrack)
            .applicant1ClaimMediationSpecRequired(applicant1ClaimMediationSpecRequired)
            .applicantMPClaimMediationSpecRequired(applicantMPClaimMediationSpecRequired)
            .responseClaimMediationSpecRequired(respondent1MediationRequired)
            .responseClaimMediationSpec2Required(respondent2MediationRequired)
            .mediation(mediation)
            .respondentSolicitor2Reference(respondentSolicitor2Reference)
            .claimant1ClaimResponseTypeForSpec(claimant1ClaimResponseTypeForSpec)
            .claimant2ClaimResponseTypeForSpec(claimant2ClaimResponseTypeForSpec)
            .respondent1ClaimResponseTypeForSpec(respondent1ClaimResponseTypeForSpec)
            .respondent2ClaimResponseTypeForSpec(respondent2ClaimResponseTypeForSpec)
            .specAoSApplicantCorrespondenceAddressRequired(specAoSApplicantCorrespondenceAddressRequired)
            .specAoSRespondentCorrespondenceAddressRequired(specAoSRespondentCorrespondenceAddressRequired)
            .specAoSApplicantCorrespondenceAddressdetails(specAoSApplicantCorrespondenceAddressDetails)
            .specAoSRespondentCorrespondenceAddressdetails(specAoSRespondentCorrespondenceAddressDetails)
            .specAoSRespondent2HomeAddressRequired(specAoSRespondent2HomeAddressRequired)
            .specAoSRespondent2HomeAddressDetails(specAoSRespondent2HomeAddressDetails)
            .respondent1DQWitnessesRequiredSpec(respondent1DQWitnessesRequiredSpec)
            .respondent1DQWitnessesDetailsSpec(respondent1DQWitnessesDetailsSpec)
            .applicant1ProceedWithClaimSpec2v1(applicant1ProceedWithClaimSpec2v1)
            .respondent1OrganisationIDCopy(respondent1OrganisationIDCopy)
            .respondent2OrganisationIDCopy(respondent2OrganisationIDCopy)
            .specRespondent1Represented(specRespondent1Represented)
            .specRespondent2Represented(specRespondent2Represented)
            .defendantSingleResponseToBothClaimants(defendantSingleResponseToBothClaimants)
            .breathing(breathing)
            .caseManagementOrderSelection(caseManagementOrderSelection)
            .generalApplications(generalApplications)
            .gaDetailsMasterCollection(generalApplicationsDetails)
            .respondent1PinToPostLRspec(respondent1PinToPostLRspec)
            .trialHearingMethodDJ(trialHearingMethodDJ)
            .hearingMethodValuesDisposalHearingDJ(hearingMethodValuesDisposalHearingDJ)
            .hearingMethodValuesTrialHearingDJ(hearingMethodValuesTrialHearingDJ)
            .disposalHearingMethodDJ(disposalHearingMethodDJ)
            .trialHearingMethodInPersonDJ(trialHearingMethodInPersonDJ)
            .disposalHearingBundleDJ(disposalHearingBundleDJ)
            .disposalHearingFinalDisposalHearingDJ(disposalHearingFinalDisposalHearingDJ)
            .trialHearingTrialDJ(trialHearingTrialDJ)
            .disposalHearingJudgesRecitalDJ(disposalHearingJudgesRecitalDJ)
            .trialHearingJudgesRecitalDJ(trialHearingJudgesRecitalDJ)
            .claimIssuedPBADetails(srPbaDetails)
            .changeOfRepresentation(changeOfRepresentation)
            .changeOrganisationRequestField(changeOrganisationRequest)
            .unassignedCaseListDisplayOrganisationReferences(unassignedCaseListDisplayOrganisationReferences)
            .caseListDisplayDefendantSolicitorReferences(caseListDisplayDefendantSolicitorReferences)
            .caseManagementLocation(caseManagementLocation)
            .disposalHearingOrderMadeWithoutHearingDJ(disposalHearingOrderMadeWithoutHearingDJ)
            .hearingDate(hearingDate)
            .cosNotifyClaimDefendant1(cosNotifyClaimDefendant1)
            .cosNotifyClaimDefendant2(cosNotifyClaimDefendant2)
            .defendant1LIPAtClaimIssued(defendant1LIPAtClaimIssued)
            .defendant2LIPAtClaimIssued(defendant2LIPAtClaimIssued)
            //Unsuitable for SDO
            .reasonNotSuitableSDO(reasonNotSuitableSDO)
            .fastTrackHearingTime(fastTrackHearingTime)
            .fastTrackOrderWithoutJudgement(fastTrackOrderWithoutJudgement)
            .fastTrackTrialDateToToggle(fastTrackTrialDateToToggle)
            .disposalHearingHearingTime(disposalHearingHearingTime)
            .disposalOrderWithoutHearing(disposalOrderWithoutHearing)
            .disposalHearingOrderMadeWithoutHearingDJ(disposalHearingOrderMadeWithoutHearingDJ)
            .disposalHearingFinalDisposalHearingTimeDJ(disposalHearingFinalDisposalHearingTimeDJ)
            .trialHearingTimeDJ(trialHearingTimeDJ)
            .trialOrderMadeWithoutHearingDJ(trialOrderMadeWithoutHearingDJ)
            //Certificate of Service
            .cosNotifyClaimDetails1(cosNotifyClaimDetails1)
            .cosNotifyClaimDetails2(cosNotifyClaimDetails2)
            .ccjPaymentDetails(ccjPaymentDetails)
            .totalInterest(totalInterest)
            .applicant1AcceptAdmitAmountPaidSpec(applicant1AcceptAdmitAmountPaidSpec)
            .applicant1AcceptPartAdmitPaymentPlanSpec(applicant1AcceptPartAdmitPaymentPlanSpec)
            .respondToAdmittedClaimOwingAmountPounds(respondToAdmittedClaimOwingAmountPounds)
            .hearingMethodValuesDisposalHearing(hearingMethodValuesDisposalHearing)
            .hearingMethodValuesFastTrack(hearingMethodValuesFastTrack)
            .hearingMethodValuesSmallClaims(hearingMethodValuesSmallClaims)
            .applicantExperts(applicantExperts)
            .applicantWitnesses(applicantWitnesses)
            .respondent1Experts(respondent1Experts)
            .respondent1Witnesses(respondent1Witnesses)
            .respondent2Experts(respondent2Experts)
            .respondent2Witnesses(respondent2Witnesses)
            .respondentSolicitor1ServiceAddressRequired(respondentSolicitor1ServiceAddressRequired)
            .respondentSolicitor2ServiceAddressRequired(respondentSolicitor2ServiceAddressRequired)
            .applicant1PartAdmitIntentionToSettleClaimSpec(applicant1PartAdmitIntentionToSettleClaimSpec)
            .applicant1PartAdmitConfirmAmountPaidSpec(applicant1PartAdmitConfirmAmountPaidSpec)
            .applicant1Represented(applicant1Represented)
            .caseDataLiP(caseDataLiP)
            .claimant2ResponseFlag(claimant2ResponseFlag)
            .specClaimResponseTimelineList(specClaimResponseTimelineList)
            .specClaimResponseTimelineList2(specClaimResponseTimelineList2)
            .defenceAdmitPartEmploymentTypeRequired(defenceAdmitPartEmploymentTypeRequired)
            .defenceAdmitPartPaymentTimeRouteRequired(defenceAdmitPartPaymentTimeRouteRequired)
            .specDefenceFullAdmitted2Required(specDefenceFullAdmitted2Required)
            .showResponseOneVOneFlag(showResponseOneVOneFlag)
            .hearingSupportRequirementsDJ(hearingSupportRequirementsDJ)
            .hearingReferenceNumber(hearingReference)
            .listingOrRelisting(listingOrRelisting)
            .claimantUserDetails(claimantUserDetails)
            .updateDetailsForm(updateDetailsForm)
            .defaultJudgmentDocuments(defaultJudgmentDocuments)
            .smallClaimsWitnessStatement(smallClaimsWitnessStatement)
            .smallClaimsFlightDelay(smallClaimsFlightDelay)
            .fastTrackWitnessOfFact(fastTrackWitnessOfFact)
            .trialHearingWitnessOfFactDJ(trialHearingWitnessOfFactDJ)
            //Transfer Online Case
            .notSuitableSdoOptions(notSuitableSdoOptions)
            .tocTransferCaseReason(tocTransferCaseReason)
            .drawDirectionsOrderRequired(drawDirectionsOrderRequired)
            .transferCourtLocationList(transferCourtLocationList)
            .reasonForTransfer(reasonForTransfer)
            .respondent1RespondToSettlementAgreementDeadline(respondent1RespondToSettlementAgreementDeadline)
            .applicant1LRIndividuals(applicant1LRIndividuals)
            .respondent1LRIndividuals(respondent1LRIndividuals)
            .respondent2LRIndividuals(respondent2LRIndividuals)
            .applicant1OrgIndividuals(applicant1OrgIndividuals)
            .applicant2OrgIndividuals(applicant2OrgIndividuals)
            .respondent1OrgIndividuals(respondent1OrgIndividuals)
            .respondent2OrgIndividuals(respondent2OrgIndividuals)
            .flightDelayDetails(flightDelayDetails)
            .uploadMediationDocumentsForm(uploadDocumentsForm)
            .responseClaimExpertSpecRequired(responseClaimExpertSpecRequired)
            .responseClaimExpertSpecRequired2(responseClaimExpertSpecRequired2)
            .applicant1ClaimExpertSpecRequired(applicant1ClaimExpertSpecRequired)
            .applicantMPClaimExpertSpecRequired(applicantMPClaimExpertSpecRequired)
            .isFlightDelayClaim(isFlightDelayClaim)
            .reasonForReconsiderationApplicant(reasonForReconsiderationApplicant)
            .reasonForReconsiderationRespondent1(reasonForReconsiderationRespondent1)
            .reasonForReconsiderationRespondent2(reasonForReconsiderationRespondent2)
            .eaCourtLocation(eaCourtLocation)
            .upholdingPreviousOrderReason(upholdingPreviousOrderReason)
            .decisionOnRequestReconsiderationOptions(decisionOnRequestReconsiderationOptions)
            .hwfFeeType(FeeType.CLAIMISSUED)
            .feePaymentOutcomeDetails(feePaymentOutcomeDetails)
            .res1MediationNonAttendanceDocs(res1MediationNonAttendanceDocs)
            .res1MediationDocumentsReferred(res1MediationDocumentsReferred)
            .hwfFeeType(hwfFeeType)
            .claimIssuedHwfDetails(claimIssuedHwfDetails)
            .hearingHwfDetails(hearingHwfDetails)
            .caseFlags(caseFlags)
            .app1MediationContactInfo(app1MediationContactInfo)
            .app1MediationAvailability(app1MediationAvailability)
            .resp1MediationContactInfo(resp1MediationContactInfo)
            .resp2MediationContactInfo(resp2MediationContactInfo)
            .resp1MediationAvailability(resp1MediationAvailability)
            .resp2MediationAvailability(resp2MediationAvailability)
            .sdoR2FastTrackCreditHire(sdoR2FastTrackCreditHire)
            .claimantBilingualLanguagePreference(claimantBilingualLanguagePreference)
            .paymentTypeSelection(paymentTypeSelection)
            .repaymentSuggestion(repaymentSuggestion)
            .paymentSetDate(paymentSetDate)
            .repaymentFrequency(repaymentFrequency)
            .repaymentDate(repaymentDate)
            .joJudgmentPaidInFull(judgmentPaidInFull)
            .anyRepresented(anyRepresented)
            .partialPaymentAmount(partialPaymentAmount)
            .nextDeadline(nextDeadline)
            .fixedCosts(fixedCosts)
            .queries(queries)
            .build();
    }

    public CaseDataBuilder atStateLipClaimSettled() {
        atStateRespondentRespondToClaimSpec(RespondentResponseTypeSpec.PART_ADMISSION);
        ccdState = CaseState.CASE_SETTLED;
        return this;
    }

    public CaseDataBuilder judgmentPaidInFull(JudgmentPaidInFull judgmentPaidInFull) {
        this.judgmentPaidInFull = judgmentPaidInFull;
        return this;
    }

    public CaseDataBuilder anyRepresented(YesOrNo anyRepresented) {
        this.anyRepresented = anyRepresented;
        return this;
    }

    public CaseDataBuilder nextDeadline(LocalDate nextDeadline) {
        this.nextDeadline = nextDeadline;
        return this;
    }

    public CaseDataBuilder gaDraftDocument(List<Element<CaseDocument>> singletonList) {
        this.gaDraftDocument = singletonList;
        return this;
    }

    public CaseDataBuilder ccdCaseReference(long ref) {
        this.ccdCaseReference = ref;
        return this;
    }
}
