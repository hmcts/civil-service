package uk.gov.hmcts.reform.civil.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import uk.gov.hmcts.reform.ccd.model.Organisation;
import uk.gov.hmcts.reform.ccd.model.OrganisationPolicy;
import uk.gov.hmcts.reform.civil.callback.CaseEvent;
import uk.gov.hmcts.reform.civil.constants.SpecJourneyConstantLRSpec;
import uk.gov.hmcts.reform.civil.documentmanagement.model.CaseDocument;
import uk.gov.hmcts.reform.civil.documentmanagement.model.Document;
import uk.gov.hmcts.reform.civil.documentmanagement.model.DocumentType;
import uk.gov.hmcts.reform.civil.enums.AllocatedTrack;
import uk.gov.hmcts.reform.civil.enums.CaseState;
import uk.gov.hmcts.reform.civil.enums.ClaimType;
import uk.gov.hmcts.reform.civil.enums.ClaimTypeUnspec;
import uk.gov.hmcts.reform.civil.enums.ConfirmationToggle;
import uk.gov.hmcts.reform.civil.enums.EmploymentTypeCheckboxFixedListLRspec;
import uk.gov.hmcts.reform.civil.enums.FeeType;
import uk.gov.hmcts.reform.civil.enums.MultiPartyResponseTypeFlags;
import uk.gov.hmcts.reform.civil.enums.MultiPartyScenario;
import uk.gov.hmcts.reform.civil.enums.PaymentType;
import uk.gov.hmcts.reform.civil.enums.PersonalInjuryType;
import uk.gov.hmcts.reform.civil.enums.RespondentResponsePartAdmissionPaymentTimeLRspec;
import uk.gov.hmcts.reform.civil.enums.RespondentResponseType;
import uk.gov.hmcts.reform.civil.enums.RespondentResponseTypeSpec;
import uk.gov.hmcts.reform.civil.enums.RespondentResponseTypeSpecPaidStatus;
import uk.gov.hmcts.reform.civil.enums.ResponseIntention;
import uk.gov.hmcts.reform.civil.enums.SuperClaimType;
import uk.gov.hmcts.reform.civil.enums.TimelineUploadTypeSpec;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.enums.settlediscontinue.SettleDiscontinueYesOrNoList;
import uk.gov.hmcts.reform.civil.handler.callback.user.spec.show.ResponseOneVOneShowTag;
import uk.gov.hmcts.reform.civil.model.breathing.BreathingSpaceInfo;
import uk.gov.hmcts.reform.civil.model.citizenui.CaseDataLiP;
import uk.gov.hmcts.reform.civil.model.citizenui.CertOfSC;
import uk.gov.hmcts.reform.civil.model.citizenui.ClaimantLiPResponse;
import uk.gov.hmcts.reform.civil.model.citizenui.HelpWithFees;
import uk.gov.hmcts.reform.civil.model.citizenui.HelpWithFeesDetails;
import uk.gov.hmcts.reform.civil.model.citizenui.ManageDocument;
import uk.gov.hmcts.reform.civil.model.citizenui.RespondentLiPResponse;
import uk.gov.hmcts.reform.civil.model.common.DynamicList;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.model.common.MappableObject;
import uk.gov.hmcts.reform.civil.model.defaultjudgment.CaseLocationCivil;
import uk.gov.hmcts.reform.civil.model.documentremoval.DocumentToKeepCollection;
import uk.gov.hmcts.reform.civil.model.dq.Applicant1DQ;
import uk.gov.hmcts.reform.civil.model.dq.Applicant2DQ;
import uk.gov.hmcts.reform.civil.model.dq.ExpertRequirements;
import uk.gov.hmcts.reform.civil.model.dq.RecurringExpenseLRspec;
import uk.gov.hmcts.reform.civil.model.dq.RecurringIncomeLRspec;
import uk.gov.hmcts.reform.civil.model.dq.Respondent1DQ;
import uk.gov.hmcts.reform.civil.model.dq.Respondent2DQ;
import uk.gov.hmcts.reform.civil.model.genapplication.GAApplicationType;
import uk.gov.hmcts.reform.civil.model.genapplication.GAApplicationTypeLR;
import uk.gov.hmcts.reform.civil.model.genapplication.GADetailsRespondentSol;
import uk.gov.hmcts.reform.civil.model.genapplication.GAHearingDateGAspec;
import uk.gov.hmcts.reform.civil.model.genapplication.GAHearingDetails;
import uk.gov.hmcts.reform.civil.model.genapplication.GAInformOtherParty;
import uk.gov.hmcts.reform.civil.model.genapplication.GAPbaDetails;
import uk.gov.hmcts.reform.civil.model.genapplication.GARespondentOrderAgreement;
import uk.gov.hmcts.reform.civil.model.genapplication.GASolicitorDetailsGAspec;
import uk.gov.hmcts.reform.civil.model.genapplication.GAStatementOfTruth;
import uk.gov.hmcts.reform.civil.model.genapplication.GAUrgencyRequirement;
import uk.gov.hmcts.reform.civil.model.genapplication.GeneralApplication;
import uk.gov.hmcts.reform.civil.model.genapplication.GeneralApplicationsDetails;
import uk.gov.hmcts.reform.civil.model.interestcalc.InterestClaimFromType;
import uk.gov.hmcts.reform.civil.model.interestcalc.InterestClaimOptions;
import uk.gov.hmcts.reform.civil.model.interestcalc.InterestClaimUntilType;
import uk.gov.hmcts.reform.civil.model.interestcalc.SameRateInterestSelection;
import uk.gov.hmcts.reform.civil.model.mediation.MediationAvailability;
import uk.gov.hmcts.reform.civil.model.mediation.MediationContactInformation;
import uk.gov.hmcts.reform.civil.model.sdo.OtherDetails;
import uk.gov.hmcts.reform.civil.model.taskmanagement.ClientContext;
import uk.gov.hmcts.reform.civil.model.welshenhancements.ChangeLanguagePreference;
import uk.gov.hmcts.reform.civil.model.welshenhancements.PreTranslationDocumentType;
import uk.gov.hmcts.reform.civil.model.welshenhancements.PreferredLanguage;
import uk.gov.hmcts.reform.civil.service.DeadlinesCalculator;
import uk.gov.hmcts.reform.civil.utils.MonetaryConversions;

import jakarta.validation.Valid;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static java.util.Optional.empty;
import static java.util.Optional.ofNullable;
import static uk.gov.hmcts.reform.civil.enums.AllocatedTrack.FAST_CLAIM;
import static uk.gov.hmcts.reform.civil.enums.AllocatedTrack.SMALL_CLAIM;
import static uk.gov.hmcts.reform.civil.enums.BusinessProcessStatus.FINISHED;
import static uk.gov.hmcts.reform.civil.enums.CaseCategory.SPEC_CLAIM;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.TWO_V_ONE;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.isOneVOne;
import static uk.gov.hmcts.reform.civil.enums.MultiPartyScenario.isOneVTwoTwoLegalRep;
import static uk.gov.hmcts.reform.civil.enums.PaymentStatus.SUCCESS;
import static uk.gov.hmcts.reform.civil.enums.RespondentResponseTypeSpec.FULL_ADMISSION;
import static uk.gov.hmcts.reform.civil.enums.RespondentResponseTypeSpec.FULL_DEFENCE;
import static uk.gov.hmcts.reform.civil.enums.RespondentResponseTypeSpec.PART_ADMISSION;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;
import static uk.gov.hmcts.reform.civil.helpers.DateFormatHelper.DATE_TIME_AT;
import static uk.gov.hmcts.reform.civil.helpers.DateFormatHelper.formatLocalDateTime;

@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@Data
public class CaseData extends CaseDataParent implements MappableObject {

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private  Long ccdCaseReference;
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private  CaseState ccdState;
    private  CaseState previousCCDState;
    private  String preStayState;
    private  String manageStayOption;
    private  LocalDate manageStayUpdateRequestDate;
    private  GAApplicationType generalAppType;
    private  GAApplicationTypeLR generalAppTypeLR;
    private  GARespondentOrderAgreement generalAppRespondentAgreement;
    private  GAPbaDetails generalAppPBADetails;
    private  String generalAppDetailsOfOrder;
    private  List<Element<String>> generalAppDetailsOfOrderColl;
    private  String generalAppReasonsOfOrder;
    private  List<Element<String>> generalAppReasonsOfOrderColl;
    private  YesOrNo generalAppAskForCosts;
    private  GAInformOtherParty generalAppInformOtherParty;
    private  GAUrgencyRequirement generalAppUrgencyRequirement;
    private  GAStatementOfTruth generalAppStatementOfTruth;
    private  GAHearingDetails generalAppHearingDetails;
    private  GASolicitorDetailsGAspec generalAppApplnSolicitor;
    private  SRPbaDetails hearingFeePBADetails;
    private  SRPbaDetails claimIssuedPBADetails;
    private  String applicantPartyName;
    private  CertOfSC certOfSC;
    private  String gaWaTrackLabel;
    private  String nextState;

    private  YesOrNo generalAppVaryJudgementType;
    private  YesOrNo generalAppParentClaimantIsApplicant;
    private  YesOrNo parentClaimantIsApplicant;
    private  GAHearingDateGAspec generalAppHearingDate;
    private  Document generalAppN245FormUpload;
    private  YesOrNo gaEaCourtLocation;

    private  List<Element<GASolicitorDetailsGAspec>> generalAppRespondentSolicitors = new ArrayList<>();

    private  List<Element<GeneralApplication>> generalApplications = new ArrayList<>();

    private  List<Element<GeneralApplicationsDetails>> claimantGaAppDetails;
    private  List<Element<GeneralApplicationsDetails>> gaDetailsMasterCollection;
    private  List<Element<GeneralApplicationsDetails>> gaDetailsTranslationCollection;
    private  List<Element<GADetailsRespondentSol>> respondentSolGaAppDetails;
    private  List<Element<GADetailsRespondentSol>> respondentSolTwoGaAppDetails;
    private  SolicitorReferences solicitorReferences;
    private  SolicitorReferences solicitorReferencesCopy;
    private  String respondentSolicitor2Reference;
    private  CourtLocation courtLocation;
    private  Party applicant1;
    private  Party applicant2;
    private  CorrectEmail applicantSolicitor1CheckEmail;
    private  IdamUserDetails applicantSolicitor1UserDetails;
    private  YesOrNo addApplicant2;
    private  YesOrNo addRespondent2;
    private  YesOrNo respondent2SameLegalRepresentative;
    private  Party respondent1;
    private  Party respondent1Copy;
    private  Party respondent2;
    private  Party respondent2Copy;
    private  Party respondent1DetailsForClaimDetailsTab;
    private  Party respondent2DetailsForClaimDetailsTab;
    private  YesOrNo respondent1Represented;
    private  YesOrNo respondent2Represented;
    private  YesOrNo respondent1OrgRegistered;
    private  YesOrNo respondent2OrgRegistered;
    private  String respondentSolicitor1EmailAddress;
    private  String respondentSolicitor2EmailAddress;
    private  YesOrNo uploadParticularsOfClaim;
    private  String detailsOfClaim;
    private  ClaimValue claimValue;
    private  Fee claimFee;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private  Fee otherRemedyFee;
    private  String serviceRequestReference;
    private  String paymentReference;
    private  DynamicList applicantSolicitor1PbaAccounts;
    private  ClaimTypeUnspec claimTypeUnSpec;
    private  ClaimType claimType;
    private HelpWithFees generalAppHelpWithFees;
    private  HelpWithFeesDetails claimIssuedHwfDetails;
    private  HelpWithFeesDetails hearingHwfDetails;
    private  FeeType hwfFeeType;
    private  SuperClaimType superClaimType;
    private  String claimTypeOther;
    private  PersonalInjuryType personalInjuryType;
    private  String personalInjuryTypeOther;
    private  StatementOfTruth applicantSolicitor1ClaimStatementOfTruth;
    private  StatementOfTruth uiStatementOfTruth;
    private  StatementOfTruth respondent1LiPStatementOfTruth;
    private  String legacyCaseReference;
    private  AllocatedTrack allocatedTrack;
    private  PaymentDetails paymentDetails;
    private  PaymentDetails claimIssuedPaymentDetails;
    private  PaymentDetails hearingFeePaymentDetails;
    private  OrganisationPolicy applicant1OrganisationPolicy;
    private  OrganisationPolicy applicant2OrganisationPolicy;
    private  OrganisationPolicy respondent1OrganisationPolicy;
    private  OrganisationPolicy respondent2OrganisationPolicy;
    private  SolicitorOrganisationDetails respondentSolicitor1OrganisationDetails;
    private  SolicitorOrganisationDetails respondentSolicitor2OrganisationDetails;
    private  YesOrNo applicantSolicitor1ServiceAddressRequired;
    private  Address applicantSolicitor1ServiceAddress;
    private  YesOrNo respondentSolicitor1ServiceAddressRequired;
    private  Address respondentSolicitor1ServiceAddress;
    private  YesOrNo respondentSolicitor2ServiceAddressRequired;
    private  Address respondentSolicitor2ServiceAddress;
    private  StatementOfTruth applicant1ServiceStatementOfTruthToRespondentSolicitor1;
    private  RespondentSolicitorDetails respondentSolicitorDetails;

    private  List<Element<CaseDocument>> systemGeneratedCaseDocuments = new ArrayList<>();

    private  List<Element<CaseDocument>> preTranslationDocuments = new ArrayList<>();
    private  List<Element<ManageDocument>> manageDocuments;
    private  Document specClaimTemplateDocumentFiles;
    private  Document specClaimDetailsDocumentFiles;
    private  List<Evidence> speclistYourEvidenceList;
    private  YesOrNo specApplicantCorrespondenceAddressRequired;
    private  Address specApplicantCorrespondenceAddressdetails;
    private  YesOrNo specRespondentCorrespondenceAddressRequired;
    private  Address specRespondentCorrespondenceAddressdetails;
    private  YesOrNo specAoSRespondent2HomeAddressRequired;
    private  Address specAoSRespondent2HomeAddressDetails;

    private  LocalDate respondentSolicitor1AgreedDeadlineExtension;
    private  LocalDate respondentSolicitor2AgreedDeadlineExtension;
    private  ResponseIntention respondent1ClaimResponseIntentionType;
    private  ResponseIntention respondent2ClaimResponseIntentionType;
    private  ResponseIntention respondent1ClaimResponseIntentionTypeApplicant2;
    private  ServedDocumentFiles servedDocumentFiles;

    private  YesOrNo respondentResponseIsSame;
    private  YesOrNo defendantSingleResponseToBothClaimants;
    private  RespondentResponseType respondent1ClaimResponseType;
    private  RespondentResponseType respondent2ClaimResponseType;
    private  RespondentResponseType respondent1ClaimResponseTypeToApplicant2;
    private  ResponseDocument respondent1ClaimResponseDocument;
    private  ResponseDocument respondent2ClaimResponseDocument;
    private  ResponseDocument respondentSharedClaimResponseDocument;
    private  CaseDocument respondent1GeneratedResponseDocument;
    private  CaseDocument respondent2GeneratedResponseDocument;
    private  LocalDate claimMovedToMediationOn;

    private  List<Element<CaseDocument>> defendantResponseDocuments = new ArrayList<>();

    private  YesOrNo applicant1ProceedWithClaim;
    private  YesOrNo applicant1ProceedWithClaimMultiParty2v1;
    private  YesOrNo applicant2ProceedWithClaimMultiParty2v1;
    private  YesOrNo applicant1ProceedWithClaimAgainstRespondent1MultiParty1v2;
    private  YesOrNo applicant1ProceedWithClaimAgainstRespondent2MultiParty1v2;
    private  YesOrNo applicant1ProceedWithClaimRespondent2;
    private  ResponseDocument applicant1DefenceResponseDocument;
    private  ResponseDocument claimantDefenceResDocToDefendant2;

    private  List<Element<CaseDocument>> claimantResponseDocuments = new ArrayList<>();

    private  List<Element<CaseDocument>> duplicateSystemGeneratedCaseDocs = new ArrayList<>();

    @JsonProperty("duplicateClaimantDefResponseDocs")
    private  List<Element<CaseDocument>> duplicateClaimantDefendantResponseDocs = new ArrayList<>();

    private  List<ClaimAmountBreakup> claimAmountBreakup;
    private  List<TimelineOfEvents> timelineOfEvents;
    /**
     * money amount in pounds.
     */
    private BigDecimal totalClaimAmount;
    private BigDecimal totalInterest;
    private BigDecimal totalClaimAmountPlusInterestAdmitPart;
    private BigDecimal totalClaimAmountPlusInterest;
    private BigDecimal defaultJudgementOverallTotal;
    private String totalClaimAmountPlusInterestAdmitPartString;
    private String totalClaimAmountPlusInterestString;
    private  YesOrNo claimInterest;
    private  InterestClaimOptions interestClaimOptions;
    private  SameRateInterestSelection sameRateInterestSelection;
    private  BigDecimal breakDownInterestTotal;
    private  String breakDownInterestDescription;
    private  InterestClaimFromType interestClaimFrom;
    private  InterestClaimUntilType interestClaimUntil;
    private  LocalDate interestFromSpecificDate;
    private  String interestFromSpecificDateDescription;
    private  String calculatedInterest;
    private  String specRespondentSolicitor1EmailAddress;
    private  YesOrNo specAoSApplicantCorrespondenceAddressRequired;
    private  Address specAoSApplicantCorrespondenceAddressdetails;
    private  YesOrNo specAoSRespondentCorrespondenceAddressRequired;
    private  Address specAoSRespondentCorrespondenceAddressdetails;
    private  YesOrNo specRespondent1Represented;
    private  YesOrNo specRespondent2Represented;
    private  List<TimelineOfEvents> specResponseTimelineOfEvents;
    private  List<TimelineOfEvents> specResponseTimelineOfEvents2;
    private  TimelineUploadTypeSpec specClaimResponseTimelineList;
    private  Document specResponseTimelineDocumentFiles;
    private  List<Evidence> specResponselistYourEvidenceList;
    private  List<Evidence> specResponselistYourEvidenceList2;

    private  String detailsOfWhyDoesYouDisputeTheClaim;
    private  String detailsOfWhyDoesYouDisputeTheClaim2;

    private  ResponseDocument respondent1SpecDefenceResponseDocument;
    private  ResponseDocument respondent2SpecDefenceResponseDocument;

    private  YesOrNo bundleError;
    private  String bundleEvent;
    private  YesOrNo fullAdmitNoPaymentSchedulerProcessed;

    @Override
    @JsonIgnore
    public CaseData copy() {
        return copyInto(new CaseData());
    }

    @JsonIgnore
    public CaseData build() {
        return this;
    }

    private void setFieldValue(String fieldName, Object value) {
        try {
            Field field = findField(getClass(), fieldName);
            if (field == null) {
                throw new IllegalArgumentException("Unknown field: " + fieldName);
            }
            boolean accessible = field.canAccess(this);
            field.setAccessible(true);
            field.set(this, value);
            field.setAccessible(accessible);
        } catch (IllegalAccessException e) {
            throw new IllegalStateException("Unable to set field " + fieldName, e);
        }
    }

    private Field findField(Class<?> type, String fieldName) {
        if (type == null || type == Object.class) {
            return null;
        }
        try {
            return type.getDeclaredField(fieldName);
        } catch (NoSuchFieldException ignored) {
            return findField(type.getSuperclass(), fieldName);
        }
    }

    @JsonIgnore
    public CaseData applicant2DQ(Object applicant2DQ) {
        setFieldValue("applicant2DQ", applicant2DQ);
        return this;
    }

    @JsonIgnore
    public CaseData orderSDODocumentDJCollection(Object orderSDODocumentDJCollection) {
        setFieldValue("orderSDODocumentDJCollection", orderSDODocumentDJCollection);
        return this;
    }

    @JsonIgnore
    public CaseData respondent1ResponseDeadlineChecked(Object respondent1ResponseDeadlineChecked) {
        setFieldValue("respondent1ResponseDeadlineChecked", respondent1ResponseDeadlineChecked);
        return this;
    }

    @JsonIgnore
    public CaseData trialHearingMethodInPersonDJ(Object trialHearingMethodInPersonDJ) {
        setFieldValue("trialHearingMethodInPersonDJ", trialHearingMethodInPersonDJ);
        return this;
    }

    @JsonIgnore
    public CaseData activeJudgment(Object activeJudgment) {
        setFieldValue("activeJudgment", activeJudgment);
        return this;
    }

    @JsonIgnore
    public CaseData addApplicant2(Object addApplicant2) {
        setFieldValue("addApplicant2", addApplicant2);
        return this;
    }

    @JsonIgnore
    public CaseData addRespondent2(Object addRespondent2) {
        setFieldValue("addRespondent2", addRespondent2);
        return this;
    }

    @JsonIgnore
    public CaseData allPartyNames(Object allPartyNames) {
        setFieldValue("allPartyNames", allPartyNames);
        return this;
    }

    @JsonIgnore
    public CaseData allocatedTrack(Object allocatedTrack) {
        setFieldValue("allocatedTrack", allocatedTrack);
        return this;
    }

    @JsonIgnore
    public CaseData app1MediationContactInfo(Object app1MediationContactInfo) {
        setFieldValue("app1MediationContactInfo", app1MediationContactInfo);
        return this;
    }

    @JsonIgnore
    public CaseData applicant1(Object applicant1) {
        setFieldValue("applicant1", applicant1);
        return this;
    }

    @JsonIgnore
    public CaseData applicant1DQ(Object applicant1DQ) {
        setFieldValue("applicant1DQ", applicant1DQ);
        return this;
    }

    @JsonIgnore
    public CaseData applicant1DefenceResponseDocumentSpec(Object applicant1DefenceResponseDocumentSpec) {
        setFieldValue("applicant1DefenceResponseDocumentSpec", applicant1DefenceResponseDocumentSpec);
        return this;
    }

    @JsonIgnore
    public CaseData applicant1FullDefenceConfirmAmountPaidSpec(Object applicant1FullDefenceConfirmAmountPaidSpec) {
        setFieldValue("applicant1FullDefenceConfirmAmountPaidSpec", applicant1FullDefenceConfirmAmountPaidSpec);
        return this;
    }

    @JsonIgnore
    public CaseData applicant1LRIndividuals(Object applicant1LRIndividuals) {
        setFieldValue("applicant1LRIndividuals", applicant1LRIndividuals);
        return this;
    }

    @JsonIgnore
    public CaseData applicant1LitigationFriend(Object applicant1LitigationFriend) {
        setFieldValue("applicant1LitigationFriend", applicant1LitigationFriend);
        return this;
    }

    @JsonIgnore
    public CaseData applicant1OrgIndividuals(Object applicant1OrgIndividuals) {
        setFieldValue("applicant1OrgIndividuals", applicant1OrgIndividuals);
        return this;
    }

    @JsonIgnore
    public CaseData applicant1OrganisationPolicy(Object applicant1OrganisationPolicy) {
        setFieldValue("applicant1OrganisationPolicy", applicant1OrganisationPolicy);
        return this;
    }

    @JsonIgnore
    public CaseData applicant1PartAdmitConfirmAmountPaidSpec(Object applicant1PartAdmitConfirmAmountPaidSpec) {
        setFieldValue("applicant1PartAdmitConfirmAmountPaidSpec", applicant1PartAdmitConfirmAmountPaidSpec);
        return this;
    }

    @JsonIgnore
    public CaseData applicant1PartAdmitIntentionToSettleClaimSpec(Object applicant1PartAdmitIntentionToSettleClaimSpec) {
        setFieldValue("applicant1PartAdmitIntentionToSettleClaimSpec", applicant1PartAdmitIntentionToSettleClaimSpec);
        return this;
    }

    @JsonIgnore
    public CaseData applicant1ProceedWithClaim(Object applicant1ProceedWithClaim) {
        setFieldValue("applicant1ProceedWithClaim", applicant1ProceedWithClaim);
        return this;
    }

    @JsonIgnore
    public CaseData applicant1ProceedWithClaimSpec2v1(Object applicant1ProceedWithClaimSpec2v1) {
        setFieldValue("applicant1ProceedWithClaimSpec2v1", applicant1ProceedWithClaimSpec2v1);
        return this;
    }

    @JsonIgnore
    public CaseData applicant1RepaymentOptionForDefendantSpec(Object applicant1RepaymentOptionForDefendantSpec) {
        setFieldValue("applicant1RepaymentOptionForDefendantSpec", applicant1RepaymentOptionForDefendantSpec);
        return this;
    }

    @JsonIgnore
    public CaseData applicant1Represented(Object applicant1Represented) {
        setFieldValue("applicant1Represented", applicant1Represented);
        return this;
    }

    @JsonIgnore
    public CaseData applicant1ResponseDeadline(Object applicant1ResponseDeadline) {
        setFieldValue("applicant1ResponseDeadline", applicant1ResponseDeadline);
        return this;
    }

    @JsonIgnore
    public CaseData applicant2(Object applicant2) {
        setFieldValue("applicant2", applicant2);
        return this;
    }

    @JsonIgnore
    public CaseData applicant2LitigationFriend(Object applicant2LitigationFriend) {
        setFieldValue("applicant2LitigationFriend", applicant2LitigationFriend);
        return this;
    }

    @JsonIgnore
    public CaseData applicant2OrgIndividuals(Object applicant2OrgIndividuals) {
        setFieldValue("applicant2OrgIndividuals", applicant2OrgIndividuals);
        return this;
    }

    @JsonIgnore
    public CaseData applicantDocsUploadedAfterBundle(Object applicantDocsUploadedAfterBundle) {
        setFieldValue("applicantDocsUploadedAfterBundle", applicantDocsUploadedAfterBundle);
        return this;
    }

    @JsonIgnore
    public CaseData applicantExperts(Object applicantExperts) {
        setFieldValue("applicantExperts", applicantExperts);
        return this;
    }

    @JsonIgnore
    public CaseData applicantPartyName(Object applicantPartyName) {
        setFieldValue("applicantPartyName", applicantPartyName);
        return this;
    }

    @JsonIgnore
    public CaseData applicantSolicitor1PbaAccounts(Object applicantSolicitor1PbaAccounts) {
        setFieldValue("applicantSolicitor1PbaAccounts", applicantSolicitor1PbaAccounts);
        return this;
    }

    @JsonIgnore
    public CaseData applicantSolicitor1UserDetails(Object applicantSolicitor1UserDetails) {
        setFieldValue("applicantSolicitor1UserDetails", applicantSolicitor1UserDetails);
        return this;
    }

    @JsonIgnore
    public CaseData applicantVRespondentText(Object applicantVRespondentText) {
        setFieldValue("applicantVRespondentText", applicantVRespondentText);
        return this;
    }

    @JsonIgnore
    public CaseData applicantWitnesses(Object applicantWitnesses) {
        setFieldValue("applicantWitnesses", applicantWitnesses);
        return this;
    }

    @JsonIgnore
    public CaseData assistedOrderCostList(Object assistedOrderCostList) {
        setFieldValue("assistedOrderCostList", assistedOrderCostList);
        return this;
    }

    @JsonIgnore
    public CaseData assistedOrderMakeAnOrderForCosts(Object assistedOrderMakeAnOrderForCosts) {
        setFieldValue("assistedOrderMakeAnOrderForCosts", assistedOrderMakeAnOrderForCosts);
        return this;
    }

    @JsonIgnore
    public CaseData businessProcess(Object businessProcess) {
        setFieldValue("businessProcess", businessProcess);
        return this;
    }

    @JsonIgnore
    public CaseData caseAccessCategory(Object caseAccessCategory) {
        setFieldValue("caseAccessCategory", caseAccessCategory);
        return this;
    }

    @JsonIgnore
    public CaseData caseDataLiP(Object caseDataLiP) {
        setFieldValue("caseDataLiP", caseDataLiP);
        return this;
    }

    @JsonIgnore
    public CaseData caseDismissedHearingFeeDueDate(Object caseDismissedHearingFeeDueDate) {
        setFieldValue("caseDismissedHearingFeeDueDate", caseDismissedHearingFeeDueDate);
        return this;
    }

    @JsonIgnore
    public CaseData caseFlags(Object caseFlags) {
        setFieldValue("caseFlags", caseFlags);
        return this;
    }

    @JsonIgnore
    public CaseData caseManagementLocation(Object caseManagementLocation) {
        setFieldValue("caseManagementLocation", caseManagementLocation);
        return this;
    }

    @JsonIgnore
    public CaseData caseNameHmctsInternal(Object caseNameHmctsInternal) {
        setFieldValue("caseNameHmctsInternal", caseNameHmctsInternal);
        return this;
    }

    @JsonIgnore
    public CaseData caseNamePublic(Object caseNamePublic) {
        setFieldValue("caseNamePublic", caseNamePublic);
        return this;
    }

    @JsonIgnore
    public CaseData caseNoteType(Object caseNoteType) {
        setFieldValue("caseNoteType", caseNoteType);
        return this;
    }

    @JsonIgnore
    public CaseData caseNotes(Object caseNotes) {
        setFieldValue("caseNotes", caseNotes);
        return this;
    }

    @JsonIgnore
    public CaseData ccdCaseReference(Object ccdCaseReference) {
        setFieldValue("ccdCaseReference", ccdCaseReference);
        return this;
    }

    @JsonIgnore
    public CaseData ccdState(Object ccdState) {
        setFieldValue("ccdState", ccdState);
        return this;
    }

    @JsonIgnore
    public CaseData ccjPaymentDetails(Object ccjPaymentDetails) {
        setFieldValue("ccjPaymentDetails", ccjPaymentDetails);
        return this;
    }

    @JsonIgnore
    public CaseData changeOfRepresentation(Object changeOfRepresentation) {
        setFieldValue("changeOfRepresentation", changeOfRepresentation);
        return this;
    }

    @JsonIgnore
    public CaseData claimAmountBreakup(Object claimAmountBreakup) {
        setFieldValue("claimAmountBreakup", claimAmountBreakup);
        return this;
    }

    @JsonIgnore
    public CaseData claimDetailsNotificationDeadline(Object claimDetailsNotificationDeadline) {
        setFieldValue("claimDetailsNotificationDeadline", claimDetailsNotificationDeadline);
        return this;
    }

    @JsonIgnore
    public CaseData claimDismissedDate(Object claimDismissedDate) {
        setFieldValue("claimDismissedDate", claimDismissedDate);
        return this;
    }

    @JsonIgnore
    public CaseData claimFee(Object claimFee) {
        setFieldValue("claimFee", claimFee);
        return this;
    }

    @JsonIgnore
    public CaseData claimIssuedHwfDetails(Object claimIssuedHwfDetails) {
        setFieldValue("claimIssuedHwfDetails", claimIssuedHwfDetails);
        return this;
    }

    @JsonIgnore
    public CaseData claimNotificationDate(Object claimNotificationDate) {
        setFieldValue("claimNotificationDate", claimNotificationDate);
        return this;
    }

    @JsonIgnore
    public CaseData claimValue(Object claimValue) {
        setFieldValue("claimValue", claimValue);
        return this;
    }

    @JsonIgnore
    public CaseData claimantBilingualLanguagePreference(Object claimantBilingualLanguagePreference) {
        setFieldValue("claimantBilingualLanguagePreference", claimantBilingualLanguagePreference);
        return this;
    }

    @JsonIgnore
    public CaseData claimantGaAppDetails(Object claimantGaAppDetails) {
        setFieldValue("claimantGaAppDetails", claimantGaAppDetails);
        return this;
    }

    @JsonIgnore
    public CaseData claimantUserDetails(Object claimantUserDetails) {
        setFieldValue("claimantUserDetails", claimantUserDetails);
        return this;
    }

    @JsonIgnore
    public CaseData claimsTrack(Object claimsTrack) {
        setFieldValue("claimsTrack", claimsTrack);
        return this;
    }

    @JsonIgnore
    public CaseData confirmOrderGivesPermission(Object confirmOrderGivesPermission) {
        setFieldValue("confirmOrderGivesPermission", confirmOrderGivesPermission);
        return this;
    }

    @JsonIgnore
    public CaseData contactDetailsUpdatedEvent(Object contactDetailsUpdatedEvent) {
        setFieldValue("contactDetailsUpdatedEvent", contactDetailsUpdatedEvent);
        return this;
    }

    @JsonIgnore
    public CaseData cosNotifyClaimDetails1(Object cosNotifyClaimDetails1) {
        setFieldValue("cosNotifyClaimDetails1", cosNotifyClaimDetails1);
        return this;
    }

    @JsonIgnore
    public CaseData courtOfficerFurtherHearingComplex(Object courtOfficerFurtherHearingComplex) {
        setFieldValue("courtOfficerFurtherHearingComplex", courtOfficerFurtherHearingComplex);
        return this;
    }

    @JsonIgnore
    public CaseData courtOfficerOrdered(Object courtOfficerOrdered) {
        setFieldValue("courtOfficerOrdered", courtOfficerOrdered);
        return this;
    }

    @JsonIgnore
    public CaseData defenceAdmitPartPaymentTimeRouteRequired(Object defenceAdmitPartPaymentTimeRouteRequired) {
        setFieldValue("defenceAdmitPartPaymentTimeRouteRequired", defenceAdmitPartPaymentTimeRouteRequired);
        return this;
    }

    @JsonIgnore
    public CaseData defenceRouteRequired(Object defenceRouteRequired) {
        setFieldValue("defenceRouteRequired", defenceRouteRequired);
        return this;
    }

    @JsonIgnore
    public CaseData defendant1LIPAtClaimIssued(Object defendant1LIPAtClaimIssued) {
        setFieldValue("defendant1LIPAtClaimIssued", defendant1LIPAtClaimIssued);
        return this;
    }

    @JsonIgnore
    public CaseData defendant2LIPAtClaimIssued(Object defendant2LIPAtClaimIssued) {
        setFieldValue("defendant2LIPAtClaimIssued", defendant2LIPAtClaimIssued);
        return this;
    }

    @JsonIgnore
    public CaseData defendantDetails(Object defendantDetails) {
        setFieldValue("defendantDetails", defendantDetails);
        return this;
    }

    @JsonIgnore
    public CaseData defendantSolicitorNotifyClaimDetailsOptions(Object defendantSolicitorNotifyClaimDetailsOptions) {
        setFieldValue("defendantSolicitorNotifyClaimDetailsOptions", defendantSolicitorNotifyClaimDetailsOptions);
        return this;
    }

    @JsonIgnore
    public CaseData defendantSolicitorNotifyClaimOptions(Object defendantSolicitorNotifyClaimOptions) {
        setFieldValue("defendantSolicitorNotifyClaimOptions", defendantSolicitorNotifyClaimOptions);
        return this;
    }

    @JsonIgnore
    public CaseData defendantUserDetails(Object defendantUserDetails) {
        setFieldValue("defendantUserDetails", defendantUserDetails);
        return this;
    }

    @JsonIgnore
    public CaseData disposalHearingAddNewDirectionsDJ(Object disposalHearingAddNewDirectionsDJ) {
        setFieldValue("disposalHearingAddNewDirectionsDJ", disposalHearingAddNewDirectionsDJ);
        return this;
    }

    @JsonIgnore
    public CaseData disposalHearingBundleDJ(Object disposalHearingBundleDJ) {
        setFieldValue("disposalHearingBundleDJ", disposalHearingBundleDJ);
        return this;
    }

    @JsonIgnore
    public CaseData disposalHearingFinalDisposalHearingDJ(Object disposalHearingFinalDisposalHearingDJ) {
        setFieldValue("disposalHearingFinalDisposalHearingDJ", disposalHearingFinalDisposalHearingDJ);
        return this;
    }

    @JsonIgnore
    public CaseData disposalHearingHearingNotes(Object disposalHearingHearingNotes) {
        setFieldValue("disposalHearingHearingNotes", disposalHearingHearingNotes);
        return this;
    }

    @JsonIgnore
    public CaseData disposalHearingHearingNotesDJ(Object disposalHearingHearingNotesDJ) {
        setFieldValue("disposalHearingHearingNotesDJ", disposalHearingHearingNotesDJ);
        return this;
    }

    @JsonIgnore
    public CaseData disposalHearingHearingTime(Object disposalHearingHearingTime) {
        setFieldValue("disposalHearingHearingTime", disposalHearingHearingTime);
        return this;
    }

    @JsonIgnore
    public CaseData disposalHearingMethodInPersonDJ(Object disposalHearingMethodInPersonDJ) {
        setFieldValue("disposalHearingMethodInPersonDJ", disposalHearingMethodInPersonDJ);
        return this;
    }

    @JsonIgnore
    public CaseData disposalHearingMethodTelephoneHearingDJ(Object disposalHearingMethodTelephoneHearingDJ) {
        setFieldValue("disposalHearingMethodTelephoneHearingDJ", disposalHearingMethodTelephoneHearingDJ);
        return this;
    }

    @JsonIgnore
    public CaseData disposalHearingMethodVideoConferenceHearingDJ(Object disposalHearingMethodVideoConferenceHearingDJ) {
        setFieldValue("disposalHearingMethodVideoConferenceHearingDJ", disposalHearingMethodVideoConferenceHearingDJ);
        return this;
    }

    @JsonIgnore
    public CaseData drawDirectionsOrderRequired(Object drawDirectionsOrderRequired) {
        setFieldValue("drawDirectionsOrderRequired", drawDirectionsOrderRequired);
        return this;
    }

    @JsonIgnore
    public CaseData fastTrackHearingNotes(Object fastTrackHearingNotes) {
        setFieldValue("fastTrackHearingNotes", fastTrackHearingNotes);
        return this;
    }

    @JsonIgnore
    public CaseData feePaymentOutcomeDetails(Object feePaymentOutcomeDetails) {
        setFieldValue("feePaymentOutcomeDetails", feePaymentOutcomeDetails);
        return this;
    }

    @JsonIgnore
    public CaseData finalOrderAllocateToTrack(Object finalOrderAllocateToTrack) {
        setFieldValue("finalOrderAllocateToTrack", finalOrderAllocateToTrack);
        return this;
    }

    @JsonIgnore
    public CaseData finalOrderAppealComplex(Object finalOrderAppealComplex) {
        setFieldValue("finalOrderAppealComplex", finalOrderAppealComplex);
        return this;
    }

    @JsonIgnore
    public CaseData finalOrderDateHeardComplex(Object finalOrderDateHeardComplex) {
        setFieldValue("finalOrderDateHeardComplex", finalOrderDateHeardComplex);
        return this;
    }

    @JsonIgnore
    public CaseData finalOrderDownloadTemplateOptions(Object finalOrderDownloadTemplateOptions) {
        setFieldValue("finalOrderDownloadTemplateOptions", finalOrderDownloadTemplateOptions);
        return this;
    }

    @JsonIgnore
    public CaseData finalOrderFurtherHearingToggle(Object finalOrderFurtherHearingToggle) {
        setFieldValue("finalOrderFurtherHearingToggle", finalOrderFurtherHearingToggle);
        return this;
    }

    @JsonIgnore
    public CaseData finalOrderIntermediateTrackComplexityBand(Object finalOrderIntermediateTrackComplexityBand) {
        setFieldValue("finalOrderIntermediateTrackComplexityBand", finalOrderIntermediateTrackComplexityBand);
        return this;
    }

    @JsonIgnore
    public CaseData finalOrderRecitals(Object finalOrderRecitals) {
        setFieldValue("finalOrderRecitals", finalOrderRecitals);
        return this;
    }

    @JsonIgnore
    public CaseData finalOrderRepresentation(Object finalOrderRepresentation) {
        setFieldValue("finalOrderRepresentation", finalOrderRepresentation);
        return this;
    }

    @JsonIgnore
    public CaseData finalOrderSelection(Object finalOrderSelection) {
        setFieldValue("finalOrderSelection", finalOrderSelection);
        return this;
    }

    @JsonIgnore
    public CaseData finalOrderTrackAllocation(Object finalOrderTrackAllocation) {
        setFieldValue("finalOrderTrackAllocation", finalOrderTrackAllocation);
        return this;
    }

    @JsonIgnore
    public CaseData gaDetailsTranslationCollection(Object gaDetailsTranslationCollection) {
        setFieldValue("gaDetailsTranslationCollection", gaDetailsTranslationCollection);
        return this;
    }

    @JsonIgnore
    public CaseData gaDraftDocument(Object gaDraftDocument) {
        setFieldValue("gaDraftDocument", gaDraftDocument);
        return this;
    }

    @JsonIgnore
    public CaseData generalAppApplnSolicitor(Object generalAppApplnSolicitor) {
        setFieldValue("generalAppApplnSolicitor", generalAppApplnSolicitor);
        return this;
    }

    @JsonIgnore
    public CaseData generalAppEvidenceDocument(Object generalAppEvidenceDocument) {
        setFieldValue("generalAppEvidenceDocument", generalAppEvidenceDocument);
        return this;
    }

    @JsonIgnore
    public CaseData generalAppHearingDetails(Object generalAppHearingDetails) {
        setFieldValue("generalAppHearingDetails", generalAppHearingDetails);
        return this;
    }

    @JsonIgnore
    public CaseData generalAppHelpWithFees(Object generalAppHelpWithFees) {
        setFieldValue("generalAppHelpWithFees", generalAppHelpWithFees);
        return this;
    }

    @JsonIgnore
    public CaseData generalAppInformOtherParty(Object generalAppInformOtherParty) {
        setFieldValue("generalAppInformOtherParty", generalAppInformOtherParty);
        return this;
    }

    @JsonIgnore
    public CaseData generalAppPBADetails(Object generalAppPBADetails) {
        setFieldValue("generalAppPBADetails", generalAppPBADetails);
        return this;
    }

    @JsonIgnore
    public CaseData generalAppRespondentAgreement(Object generalAppRespondentAgreement) {
        setFieldValue("generalAppRespondentAgreement", generalAppRespondentAgreement);
        return this;
    }

    @JsonIgnore
    public CaseData generalAppType(Object generalAppType) {
        setFieldValue("generalAppType", generalAppType);
        return this;
    }

    @JsonIgnore
    public CaseData generalAppUrgencyRequirement(Object generalAppUrgencyRequirement) {
        setFieldValue("generalAppUrgencyRequirement", generalAppUrgencyRequirement);
        return this;
    }

    @JsonIgnore
    public CaseData hearingDate(Object hearingDate) {
        setFieldValue("hearingDate", hearingDate);
        return this;
    }

    @JsonIgnore
    public CaseData hearingDuration(Object hearingDuration) {
        setFieldValue("hearingDuration", hearingDuration);
        return this;
    }

    @JsonIgnore
    public CaseData hearingDurationMinti(Object hearingDurationMinti) {
        setFieldValue("hearingDurationMinti", hearingDurationMinti);
        return this;
    }

    @JsonIgnore
    public CaseData hearingFee(Object hearingFee) {
        setFieldValue("hearingFee", hearingFee);
        return this;
    }

    @JsonIgnore
    public CaseData hearingFeePaymentDetails(Object hearingFeePaymentDetails) {
        setFieldValue("hearingFeePaymentDetails", hearingFeePaymentDetails);
        return this;
    }

    @JsonIgnore
    public CaseData hearingHelpFeesReferenceNumber(Object hearingHelpFeesReferenceNumber) {
        setFieldValue("hearingHelpFeesReferenceNumber", hearingHelpFeesReferenceNumber);
        return this;
    }

    @JsonIgnore
    public CaseData hearingHwfDetails(Object hearingHwfDetails) {
        setFieldValue("hearingHwfDetails", hearingHwfDetails);
        return this;
    }

    @JsonIgnore
    public CaseData hearingLocation(Object hearingLocation) {
        setFieldValue("hearingLocation", hearingLocation);
        return this;
    }

    @JsonIgnore
    public CaseData hearingReferenceNumber(Object hearingReferenceNumber) {
        setFieldValue("hearingReferenceNumber", hearingReferenceNumber);
        return this;
    }

    @JsonIgnore
    public CaseData hearingTimeHourMinute(Object hearingTimeHourMinute) {
        setFieldValue("hearingTimeHourMinute", hearingTimeHourMinute);
        return this;
    }

    @JsonIgnore
    public CaseData hwfFeeType(Object hwfFeeType) {
        setFieldValue("hwfFeeType", hwfFeeType);
        return this;
    }

    @JsonIgnore
    public CaseData interestClaimOptions(Object interestClaimOptions) {
        setFieldValue("interestClaimOptions", interestClaimOptions);
        return this;
    }

    @JsonIgnore
    public CaseData interestFromSpecificDate(Object interestFromSpecificDate) {
        setFieldValue("interestFromSpecificDate", interestFromSpecificDate);
        return this;
    }

    @JsonIgnore
    public CaseData isApplicant1(Object isApplicant1) {
        setFieldValue("isApplicant1", isApplicant1);
        return this;
    }

    @JsonIgnore
    public CaseData isGaApplicantLip(Object isGaApplicantLip) {
        setFieldValue("isGaApplicantLip", isGaApplicantLip);
        return this;
    }

    @JsonIgnore
    public CaseData isGaRespondentOneLip(Object isGaRespondentOneLip) {
        setFieldValue("isGaRespondentOneLip", isGaRespondentOneLip);
        return this;
    }

    @JsonIgnore
    public CaseData isRespondent1(Object isRespondent1) {
        setFieldValue("isRespondent1", isRespondent1);
        return this;
    }

    @JsonIgnore
    public CaseData isRespondent2(Object isRespondent2) {
        setFieldValue("isRespondent2", isRespondent2);
        return this;
    }

    @JsonIgnore
    public CaseData issueDate(Object issueDate) {
        setFieldValue("issueDate", issueDate);
        return this;
    }

    @JsonIgnore
    public CaseData lastMessage(Object lastMessage) {
        setFieldValue("lastMessage", lastMessage);
        return this;
    }

    @JsonIgnore
    public CaseData legacyCaseReference(Object legacyCaseReference) {
        setFieldValue("legacyCaseReference", legacyCaseReference);
        return this;
    }

    @JsonIgnore
    public CaseData locationName(Object locationName) {
        setFieldValue("locationName", locationName);
        return this;
    }

    @JsonIgnore
    public CaseData mediation(Object mediation) {
        setFieldValue("mediation", mediation);
        return this;
    }

    @JsonIgnore
    public CaseData nextState(Object nextState) {
        setFieldValue("nextState", nextState);
        return this;
    }

    @JsonIgnore
    public CaseData notificationText(Object notificationText) {
        setFieldValue("notificationText", notificationText);
        return this;
    }

    @JsonIgnore
    public CaseData obligationWAFlag(Object obligationWAFlag) {
        setFieldValue("obligationWAFlag", obligationWAFlag);
        return this;
    }

    @JsonIgnore
    public CaseData orderMadeOnDetailsList(Object orderMadeOnDetailsList) {
        setFieldValue("orderMadeOnDetailsList", orderMadeOnDetailsList);
        return this;
    }

    @JsonIgnore
    public CaseData orderOnCourtInitiative(Object orderOnCourtInitiative) {
        setFieldValue("orderOnCourtInitiative", orderOnCourtInitiative);
        return this;
    }

    @JsonIgnore
    public CaseData orderSDODocumentDJ(Object orderSDODocumentDJ) {
        setFieldValue("orderSDODocumentDJ", orderSDODocumentDJ);
        return this;
    }

    @JsonIgnore
    public CaseData parentClaimantIsApplicant(Object parentClaimantIsApplicant) {
        setFieldValue("parentClaimantIsApplicant", parentClaimantIsApplicant);
        return this;
    }

    @JsonIgnore
    public CaseData partialPaymentAmount(Object partialPaymentAmount) {
        setFieldValue("partialPaymentAmount", partialPaymentAmount);
        return this;
    }

    @JsonIgnore
    public CaseData paymentConfirmationDecisionSpec(Object paymentConfirmationDecisionSpec) {
        setFieldValue("paymentConfirmationDecisionSpec", paymentConfirmationDecisionSpec);
        return this;
    }

    @JsonIgnore
    public CaseData previousCCDState(Object previousCCDState) {
        setFieldValue("previousCCDState", previousCCDState);
        return this;
    }

    @JsonIgnore
    public CaseData qmApplicantSolicitorQueries(Object qmApplicantSolicitorQueries) {
        setFieldValue("qmApplicantSolicitorQueries", qmApplicantSolicitorQueries);
        return this;
    }

    @JsonIgnore
    public CaseData qmLatestQuery(Object qmLatestQuery) {
        setFieldValue("qmLatestQuery", qmLatestQuery);
        return this;
    }

    @JsonIgnore
    public CaseData reasonForReconsiderationApplicant(Object reasonForReconsiderationApplicant) {
        setFieldValue("reasonForReconsiderationApplicant", reasonForReconsiderationApplicant);
        return this;
    }

    @JsonIgnore
    public CaseData reasonForReconsiderationRespondent1(Object reasonForReconsiderationRespondent1) {
        setFieldValue("reasonForReconsiderationRespondent1", reasonForReconsiderationRespondent1);
        return this;
    }

    @JsonIgnore
    public CaseData reasonNotSuitableSDO(Object reasonNotSuitableSDO) {
        setFieldValue("reasonNotSuitableSDO", reasonNotSuitableSDO);
        return this;
    }

    @JsonIgnore
    public CaseData repaymentSummaryObject(Object repaymentSummaryObject) {
        setFieldValue("repaymentSummaryObject", repaymentSummaryObject);
        return this;
    }

    @JsonIgnore
    public CaseData resp1MediationContactInfo(Object resp1MediationContactInfo) {
        setFieldValue("resp1MediationContactInfo", resp1MediationContactInfo);
        return this;
    }

    @JsonIgnore
    public CaseData resp2MediationContactInfo(Object resp2MediationContactInfo) {
        setFieldValue("resp2MediationContactInfo", resp2MediationContactInfo);
        return this;
    }

    @JsonIgnore
    public CaseData respondToClaimAdmitPartLRspec(Object respondToClaimAdmitPartLRspec) {
        setFieldValue("respondToClaimAdmitPartLRspec", respondToClaimAdmitPartLRspec);
        return this;
    }

    @JsonIgnore
    public CaseData respondent1(Object respondent1) {
        setFieldValue("respondent1", respondent1);
        return this;
    }

    @JsonIgnore
    public CaseData respondent1ClaimResponseType(Object respondent1ClaimResponseType) {
        setFieldValue("respondent1ClaimResponseType", respondent1ClaimResponseType);
        return this;
    }

    @JsonIgnore
    public CaseData respondent1ClaimResponseTypeForSpec(Object respondent1ClaimResponseTypeForSpec) {
        setFieldValue("respondent1ClaimResponseTypeForSpec", respondent1ClaimResponseTypeForSpec);
        return this;
    }

    @JsonIgnore
    public CaseData respondent1DQ(Object respondent1DQ) {
        setFieldValue("respondent1DQ", respondent1DQ);
        return this;
    }

    @JsonIgnore
    public CaseData respondent1DetailsForClaimDetailsTab(Object respondent1DetailsForClaimDetailsTab) {
        setFieldValue("respondent1DetailsForClaimDetailsTab", respondent1DetailsForClaimDetailsTab);
        return this;
    }

    @JsonIgnore
    public CaseData respondent1EmailAddress(Object respondent1EmailAddress) {
        setFieldValue("respondent1EmailAddress", respondent1EmailAddress);
        return this;
    }

    @JsonIgnore
    public CaseData respondent1Experts(Object respondent1Experts) {
        setFieldValue("respondent1Experts", respondent1Experts);
        return this;
    }

    @JsonIgnore
    public CaseData respondent1LRIndividuals(Object respondent1LRIndividuals) {
        setFieldValue("respondent1LRIndividuals", respondent1LRIndividuals);
        return this;
    }

    @JsonIgnore
    public CaseData respondent1LitigationFriend(Object respondent1LitigationFriend) {
        setFieldValue("respondent1LitigationFriend", respondent1LitigationFriend);
        return this;
    }

    @JsonIgnore
    public CaseData respondent1OrgIndividuals(Object respondent1OrgIndividuals) {
        setFieldValue("respondent1OrgIndividuals", respondent1OrgIndividuals);
        return this;
    }

    @JsonIgnore
    public CaseData respondent1OrganisationIDCopy(Object respondent1OrganisationIDCopy) {
        setFieldValue("respondent1OrganisationIDCopy", respondent1OrganisationIDCopy);
        return this;
    }

    @JsonIgnore
    public CaseData respondent1OrganisationPolicy(Object respondent1OrganisationPolicy) {
        setFieldValue("respondent1OrganisationPolicy", respondent1OrganisationPolicy);
        return this;
    }

    @JsonIgnore
    public CaseData respondent1RepaymentPlan(Object respondent1RepaymentPlan) {
        setFieldValue("respondent1RepaymentPlan", respondent1RepaymentPlan);
        return this;
    }

    @JsonIgnore
    public CaseData respondent1Represented(Object respondent1Represented) {
        setFieldValue("respondent1Represented", respondent1Represented);
        return this;
    }

    @JsonIgnore
    public CaseData respondent1RespondToSettlementAgreementDeadline(Object respondent1RespondToSettlementAgreementDeadline) {
        setFieldValue("respondent1RespondToSettlementAgreementDeadline", respondent1RespondToSettlementAgreementDeadline);
        return this;
    }

    @JsonIgnore
    public CaseData respondent1ResponseDate(Object respondent1ResponseDate) {
        setFieldValue("respondent1ResponseDate", respondent1ResponseDate);
        return this;
    }

    @JsonIgnore
    public CaseData respondent1ResponseDeadline(Object respondent1ResponseDeadline) {
        setFieldValue("respondent1ResponseDeadline", respondent1ResponseDeadline);
        return this;
    }

    @JsonIgnore
    public CaseData respondent1Witnesses(Object respondent1Witnesses) {
        setFieldValue("respondent1Witnesses", respondent1Witnesses);
        return this;
    }

    @JsonIgnore
    public CaseData respondent2(Object respondent2) {
        setFieldValue("respondent2", respondent2);
        return this;
    }

    @JsonIgnore
    public CaseData respondent2ClaimResponseTypeForSpec(Object respondent2ClaimResponseTypeForSpec) {
        setFieldValue("respondent2ClaimResponseTypeForSpec", respondent2ClaimResponseTypeForSpec);
        return this;
    }

    @JsonIgnore
    public CaseData respondent2DQ(Object respondent2DQ) {
        setFieldValue("respondent2DQ", respondent2DQ);
        return this;
    }

    @JsonIgnore
    public CaseData respondent2Experts(Object respondent2Experts) {
        setFieldValue("respondent2Experts", respondent2Experts);
        return this;
    }

    @JsonIgnore
    public CaseData respondent2LRIndividuals(Object respondent2LRIndividuals) {
        setFieldValue("respondent2LRIndividuals", respondent2LRIndividuals);
        return this;
    }

    @JsonIgnore
    public CaseData respondent2LitigationFriend(Object respondent2LitigationFriend) {
        setFieldValue("respondent2LitigationFriend", respondent2LitigationFriend);
        return this;
    }

    @JsonIgnore
    public CaseData respondent2OrgIndividuals(Object respondent2OrgIndividuals) {
        setFieldValue("respondent2OrgIndividuals", respondent2OrgIndividuals);
        return this;
    }

    @JsonIgnore
    public CaseData respondent2OrganisationIDCopy(Object respondent2OrganisationIDCopy) {
        setFieldValue("respondent2OrganisationIDCopy", respondent2OrganisationIDCopy);
        return this;
    }

    @JsonIgnore
    public CaseData respondent2OrganisationPolicy(Object respondent2OrganisationPolicy) {
        setFieldValue("respondent2OrganisationPolicy", respondent2OrganisationPolicy);
        return this;
    }

    @JsonIgnore
    public CaseData respondent2Represented(Object respondent2Represented) {
        setFieldValue("respondent2Represented", respondent2Represented);
        return this;
    }

    @JsonIgnore
    public CaseData respondent2ResponseDate(Object respondent2ResponseDate) {
        setFieldValue("respondent2ResponseDate", respondent2ResponseDate);
        return this;
    }

    @JsonIgnore
    public CaseData respondent2SameLegalRepresentative(Object respondent2SameLegalRepresentative) {
        setFieldValue("respondent2SameLegalRepresentative", respondent2SameLegalRepresentative);
        return this;
    }

    @JsonIgnore
    public CaseData respondent2Witnesses(Object respondent2Witnesses) {
        setFieldValue("respondent2Witnesses", respondent2Witnesses);
        return this;
    }

    @JsonIgnore
    public CaseData respondentSolicitor1EmailAddress(Object respondentSolicitor1EmailAddress) {
        setFieldValue("respondentSolicitor1EmailAddress", respondentSolicitor1EmailAddress);
        return this;
    }

    @JsonIgnore
    public CaseData respondentSolicitor2EmailAddress(Object respondentSolicitor2EmailAddress) {
        setFieldValue("respondentSolicitor2EmailAddress", respondentSolicitor2EmailAddress);
        return this;
    }

    @JsonIgnore
    public CaseData respondentSolicitor2Reference(Object respondentSolicitor2Reference) {
        setFieldValue("respondentSolicitor2Reference", respondentSolicitor2Reference);
        return this;
    }

    @JsonIgnore
    public CaseData responseClaimMediationSpecRequired(Object responseClaimMediationSpecRequired) {
        setFieldValue("responseClaimMediationSpecRequired", responseClaimMediationSpecRequired);
        return this;
    }

    @JsonIgnore
    public CaseData responseClaimTrack(Object responseClaimTrack) {
        setFieldValue("responseClaimTrack", responseClaimTrack);
        return this;
    }

    @JsonIgnore
    public CaseData sameRateInterestSelection(Object sameRateInterestSelection) {
        setFieldValue("sameRateInterestSelection", sameRateInterestSelection);
        return this;
    }

    @JsonIgnore
    public CaseData sdoAltDisputeResolution(Object sdoAltDisputeResolution) {
        setFieldValue("sdoAltDisputeResolution", sdoAltDisputeResolution);
        return this;
    }

    @JsonIgnore
    public CaseData sdoDJR2TrialCreditHire(Object sdoDJR2TrialCreditHire) {
        setFieldValue("sdoDJR2TrialCreditHire", sdoDJR2TrialCreditHire);
        return this;
    }

    @JsonIgnore
    public CaseData sdoHearingNotes(Object sdoHearingNotes) {
        setFieldValue("sdoHearingNotes", sdoHearingNotes);
        return this;
    }

    @JsonIgnore
    public CaseData sdoR2SmallClaimsHearing(Object sdoR2SmallClaimsHearing) {
        setFieldValue("sdoR2SmallClaimsHearing", sdoR2SmallClaimsHearing);
        return this;
    }

    @JsonIgnore
    public CaseData showResponseOneVOneFlag(Object showResponseOneVOneFlag) {
        setFieldValue("showResponseOneVOneFlag", showResponseOneVOneFlag);
        return this;
    }

    @JsonIgnore
    public CaseData smallClaimsMediationSectionStatement(Object smallClaimsMediationSectionStatement) {
        setFieldValue("smallClaimsMediationSectionStatement", smallClaimsMediationSectionStatement);
        return this;
    }

    @JsonIgnore
    public CaseData solicitorReferences(Object solicitorReferences) {
        setFieldValue("solicitorReferences", solicitorReferences);
        return this;
    }

    @JsonIgnore
    public CaseData specAoSApplicantCorrespondenceAddressRequired(Object specAoSApplicantCorrespondenceAddressRequired) {
        setFieldValue("specAoSApplicantCorrespondenceAddressRequired", specAoSApplicantCorrespondenceAddressRequired);
        return this;
    }

    @JsonIgnore
    public CaseData specRespondent1Represented(Object specRespondent1Represented) {
        setFieldValue("specRespondent1Represented", specRespondent1Represented);
        return this;
    }

    @JsonIgnore
    public CaseData specRespondent2Represented(Object specRespondent2Represented) {
        setFieldValue("specRespondent2Represented", specRespondent2Represented);
        return this;
    }

    @JsonIgnore
    public CaseData submittedDate(Object submittedDate) {
        setFieldValue("submittedDate", submittedDate);
        return this;
    }

    @JsonIgnore
    public CaseData takenOfflineByStaffDate(Object takenOfflineByStaffDate) {
        setFieldValue("takenOfflineByStaffDate", takenOfflineByStaffDate);
        return this;
    }

    @JsonIgnore
    public CaseData takenOfflineDate(Object takenOfflineDate) {
        setFieldValue("takenOfflineDate", takenOfflineDate);
        return this;
    }

    @JsonIgnore
    public CaseData taskManagementLocations(Object taskManagementLocations) {
        setFieldValue("taskManagementLocations", taskManagementLocations);
        return this;
    }

    @JsonIgnore
    public CaseData totalClaimAmount(Object totalClaimAmount) {
        setFieldValue("totalClaimAmount", totalClaimAmount);
        return this;
    }

    @JsonIgnore
    public CaseData trialBuildingDispute(Object trialBuildingDispute) {
        setFieldValue("trialBuildingDispute", trialBuildingDispute);
        return this;
    }

    @JsonIgnore
    public CaseData trialHearingHearingNotesDJ(Object trialHearingHearingNotesDJ) {
        setFieldValue("trialHearingHearingNotesDJ", trialHearingHearingNotesDJ);
        return this;
    }

    @JsonIgnore
    public CaseData trialHearingMethodTelephoneHearingDJ(Object trialHearingMethodTelephoneHearingDJ) {
        setFieldValue("trialHearingMethodTelephoneHearingDJ", trialHearingMethodTelephoneHearingDJ);
        return this;
    }

    @JsonIgnore
    public CaseData trialHearingMethodVideoConferenceHearingDJ(Object trialHearingMethodVideoConferenceHearingDJ) {
        setFieldValue("trialHearingMethodVideoConferenceHearingDJ", trialHearingMethodVideoConferenceHearingDJ);
        return this;
    }

    @JsonIgnore
    public CaseData trialHearingTimeDJ(Object trialHearingTimeDJ) {
        setFieldValue("trialHearingTimeDJ", trialHearingTimeDJ);
        return this;
    }

    @JsonIgnore
    public CaseData trialHearingWitnessOfFactDJ(Object trialHearingWitnessOfFactDJ) {
        setFieldValue("trialHearingWitnessOfFactDJ", trialHearingWitnessOfFactDJ);
        return this;
    }

    @JsonIgnore
    public CaseData trialReadyApplicant(Object trialReadyApplicant) {
        setFieldValue("trialReadyApplicant", trialReadyApplicant);
        return this;
    }

    @JsonIgnore
    public CaseData trialReadyNotified(Object trialReadyNotified) {
        setFieldValue("trialReadyNotified", trialReadyNotified);
        return this;
    }

    @JsonIgnore
    public CaseData trialReadyRespondent1(Object trialReadyRespondent1) {
        setFieldValue("trialReadyRespondent1", trialReadyRespondent1);
        return this;
    }

    @JsonIgnore
    public CaseData updateDetailsForm(Object updateDetailsForm) {
        setFieldValue("updateDetailsForm", updateDetailsForm);
        return this;
    }

    @JsonIgnore
    public CaseData addLegalRepDeadline(Object addLegalRepDeadline) {
        setFieldValue("addLegalRepDeadline", addLegalRepDeadline);
        return this;
    }

    @JsonIgnore
    public CaseData addLegalRepDeadlineRes1(Object addLegalRepDeadlineRes1) {
        setFieldValue("addLegalRepDeadlineRes1", addLegalRepDeadlineRes1);
        return this;
    }

    @JsonIgnore
    public CaseData addLegalRepDeadlineRes2(Object addLegalRepDeadlineRes2) {
        setFieldValue("addLegalRepDeadlineRes2", addLegalRepDeadlineRes2);
        return this;
    }

    @JsonIgnore
    public CaseData additionalInformationForJudge(Object additionalInformationForJudge) {
        setFieldValue("additionalInformationForJudge", additionalInformationForJudge);
        return this;
    }

    @JsonIgnore
    public CaseData additionalInformationForJudge2(Object additionalInformationForJudge2) {
        setFieldValue("additionalInformationForJudge2", additionalInformationForJudge2);
        return this;
    }

    @JsonIgnore
    public CaseData additionalInformationRTJ(Object additionalInformationRTJ) {
        setFieldValue("additionalInformationRTJ", additionalInformationRTJ);
        return this;
    }

    @JsonIgnore
    public CaseData allowOrderTrackAllocation(Object allowOrderTrackAllocation) {
        setFieldValue("allowOrderTrackAllocation", allowOrderTrackAllocation);
        return this;
    }

    @JsonIgnore
    public CaseData anyRepresented(Object anyRepresented) {
        setFieldValue("anyRepresented", anyRepresented);
        return this;
    }

    @JsonIgnore
    public CaseData app1MediationAvailability(Object app1MediationAvailability) {
        setFieldValue("app1MediationAvailability", app1MediationAvailability);
        return this;
    }

    @JsonIgnore
    public CaseData app1MediationDocumentsReferred(Object app1MediationDocumentsReferred) {
        setFieldValue("app1MediationDocumentsReferred", app1MediationDocumentsReferred);
        return this;
    }

    @JsonIgnore
    public CaseData app1MediationNonAttendanceDocs(Object app1MediationNonAttendanceDocs) {
        setFieldValue("app1MediationNonAttendanceDocs", app1MediationNonAttendanceDocs);
        return this;
    }

    @JsonIgnore
    public CaseData app2MediationDocumentsReferred(Object app2MediationDocumentsReferred) {
        setFieldValue("app2MediationDocumentsReferred", app2MediationDocumentsReferred);
        return this;
    }

    @JsonIgnore
    public CaseData app2MediationNonAttendanceDocs(Object app2MediationNonAttendanceDocs) {
        setFieldValue("app2MediationNonAttendanceDocs", app2MediationNonAttendanceDocs);
        return this;
    }

    @JsonIgnore
    public CaseData applicant1AcceptAdmitAmountPaidSpec(Object applicant1AcceptAdmitAmountPaidSpec) {
        setFieldValue("applicant1AcceptAdmitAmountPaidSpec", applicant1AcceptAdmitAmountPaidSpec);
        return this;
    }

    @JsonIgnore
    public CaseData applicant1AcceptFullAdmitPaymentPlanSpec(Object applicant1AcceptFullAdmitPaymentPlanSpec) {
        setFieldValue("applicant1AcceptFullAdmitPaymentPlanSpec", applicant1AcceptFullAdmitPaymentPlanSpec);
        return this;
    }

    @JsonIgnore
    public CaseData applicant1AcceptPartAdmitPaymentPlanSpec(Object applicant1AcceptPartAdmitPaymentPlanSpec) {
        setFieldValue("applicant1AcceptPartAdmitPaymentPlanSpec", applicant1AcceptPartAdmitPaymentPlanSpec);
        return this;
    }

    @JsonIgnore
    public CaseData applicant1ClaimExpertSpecRequired(Object applicant1ClaimExpertSpecRequired) {
        setFieldValue("applicant1ClaimExpertSpecRequired", applicant1ClaimExpertSpecRequired);
        return this;
    }

    @JsonIgnore
    public CaseData applicant1ClaimMediationSpecRequired(Object applicant1ClaimMediationSpecRequired) {
        setFieldValue("applicant1ClaimMediationSpecRequired", applicant1ClaimMediationSpecRequired);
        return this;
    }

    @JsonIgnore
    public CaseData applicant1ClaimWitnesses(Object applicant1ClaimWitnesses) {
        setFieldValue("applicant1ClaimWitnesses", applicant1ClaimWitnesses);
        return this;
    }

    @JsonIgnore
    public CaseData applicant1DQWitnessesSmallClaim(Object applicant1DQWitnessesSmallClaim) {
        setFieldValue("applicant1DQWitnessesSmallClaim", applicant1DQWitnessesSmallClaim);
        return this;
    }

    @JsonIgnore
    public CaseData applicant1DefenceResponseDocument(Object applicant1DefenceResponseDocument) {
        setFieldValue("applicant1DefenceResponseDocument", applicant1DefenceResponseDocument);
        return this;
    }

    @JsonIgnore
    public CaseData applicant1LitigationFriendRequired(Object applicant1LitigationFriendRequired) {
        setFieldValue("applicant1LitigationFriendRequired", applicant1LitigationFriendRequired);
        return this;
    }

    @JsonIgnore
    public CaseData applicant1NoticeOfDiscontinueAllPartyViewDoc(Object applicant1NoticeOfDiscontinueAllPartyViewDoc) {
        setFieldValue("applicant1NoticeOfDiscontinueAllPartyViewDoc", applicant1NoticeOfDiscontinueAllPartyViewDoc);
        return this;
    }

    @JsonIgnore
    public CaseData applicant1NoticeOfDiscontinueCWViewDoc(Object applicant1NoticeOfDiscontinueCWViewDoc) {
        setFieldValue("applicant1NoticeOfDiscontinueCWViewDoc", applicant1NoticeOfDiscontinueCWViewDoc);
        return this;
    }

    @JsonIgnore
    public CaseData applicant1ProceedWithClaimAgainstRespondent1MultiParty1v2(Object applicant1ProceedWithClaimAgainstRespondent1MultiParty1v2) {
        setFieldValue("applicant1ProceedWithClaimAgainstRespondent1MultiParty1v2", applicant1ProceedWithClaimAgainstRespondent1MultiParty1v2);
        return this;
    }

    @JsonIgnore
    public CaseData applicant1ProceedWithClaimAgainstRespondent2MultiParty1v2(Object applicant1ProceedWithClaimAgainstRespondent2MultiParty1v2) {
        setFieldValue("applicant1ProceedWithClaimAgainstRespondent2MultiParty1v2", applicant1ProceedWithClaimAgainstRespondent2MultiParty1v2);
        return this;
    }

    @JsonIgnore
    public CaseData applicant1ProceedWithClaimMultiParty2v1(Object applicant1ProceedWithClaimMultiParty2v1) {
        setFieldValue("applicant1ProceedWithClaimMultiParty2v1", applicant1ProceedWithClaimMultiParty2v1);
        return this;
    }

    @JsonIgnore
    public CaseData applicant1ProceedWithClaimRespondent2(Object applicant1ProceedWithClaimRespondent2) {
        setFieldValue("applicant1ProceedWithClaimRespondent2", applicant1ProceedWithClaimRespondent2);
        return this;
    }

    @JsonIgnore
    public CaseData applicant1RequestedPaymentDateForDefendantSpec(Object applicant1RequestedPaymentDateForDefendantSpec) {
        setFieldValue("applicant1RequestedPaymentDateForDefendantSpec", applicant1RequestedPaymentDateForDefendantSpec);
        return this;
    }

    @JsonIgnore
    public CaseData applicant1ResponseDate(Object applicant1ResponseDate) {
        setFieldValue("applicant1ResponseDate", applicant1ResponseDate);
        return this;
    }

    @JsonIgnore
    public CaseData applicant1ServiceStatementOfTruthToRespondentSolicitor1(Object applicant1ServiceStatementOfTruthToRespondentSolicitor1) {
        setFieldValue("applicant1ServiceStatementOfTruthToRespondentSolicitor1", applicant1ServiceStatementOfTruthToRespondentSolicitor1);
        return this;
    }

    @JsonIgnore
    public CaseData applicant1SuggestInstalmentsFirstRepaymentDateForDefendantSpec(Object applicant1SuggestInstalmentsFirstRepaymentDateForDefendantSpec) {
        setFieldValue("applicant1SuggestInstalmentsFirstRepaymentDateForDefendantSpec", applicant1SuggestInstalmentsFirstRepaymentDateForDefendantSpec);
        return this;
    }

    @JsonIgnore
    public CaseData applicant1SuggestInstalmentsPaymentAmountForDefendantSpec(Object applicant1SuggestInstalmentsPaymentAmountForDefendantSpec) {
        setFieldValue("applicant1SuggestInstalmentsPaymentAmountForDefendantSpec", applicant1SuggestInstalmentsPaymentAmountForDefendantSpec);
        return this;
    }

    @JsonIgnore
    public CaseData applicant1SuggestInstalmentsRepaymentFrequencyForDefendantSpec(Object applicant1SuggestInstalmentsRepaymentFrequencyForDefendantSpec) {
        setFieldValue("applicant1SuggestInstalmentsRepaymentFrequencyForDefendantSpec", applicant1SuggestInstalmentsRepaymentFrequencyForDefendantSpec);
        return this;
    }

    @JsonIgnore
    public CaseData applicant1SuggestPayImmediatelyPaymentDateForDefendantSpec(Object applicant1SuggestPayImmediatelyPaymentDateForDefendantSpec) {
        setFieldValue("applicant1SuggestPayImmediatelyPaymentDateForDefendantSpec", applicant1SuggestPayImmediatelyPaymentDateForDefendantSpec);
        return this;
    }

    @JsonIgnore
    public CaseData applicant1UnavailableDatesForTab(Object applicant1UnavailableDatesForTab) {
        setFieldValue("applicant1UnavailableDatesForTab", applicant1UnavailableDatesForTab);
        return this;
    }

    @JsonIgnore
    public CaseData applicant2LitigationFriendRequired(Object applicant2LitigationFriendRequired) {
        setFieldValue("applicant2LitigationFriendRequired", applicant2LitigationFriendRequired);
        return this;
    }

    @JsonIgnore
    public CaseData applicant2OrganisationPolicy(Object applicant2OrganisationPolicy) {
        setFieldValue("applicant2OrganisationPolicy", applicant2OrganisationPolicy);
        return this;
    }

    @JsonIgnore
    public CaseData applicant2ProceedWithClaimMultiParty2v1(Object applicant2ProceedWithClaimMultiParty2v1) {
        setFieldValue("applicant2ProceedWithClaimMultiParty2v1", applicant2ProceedWithClaimMultiParty2v1);
        return this;
    }

    @JsonIgnore
    public CaseData applicant2ResponseDate(Object applicant2ResponseDate) {
        setFieldValue("applicant2ResponseDate", applicant2ResponseDate);
        return this;
    }

    @JsonIgnore
    public CaseData applicant2UnavailableDatesForTab(Object applicant2UnavailableDatesForTab) {
        setFieldValue("applicant2UnavailableDatesForTab", applicant2UnavailableDatesForTab);
        return this;
    }

    @JsonIgnore
    public CaseData applicantAdditionalInformationForJudge(Object applicantAdditionalInformationForJudge) {
        setFieldValue("applicantAdditionalInformationForJudge", applicantAdditionalInformationForJudge);
        return this;
    }

    @JsonIgnore
    public CaseData applicantDefenceResponseDocumentAndDQFlag(Object applicantDefenceResponseDocumentAndDQFlag) {
        setFieldValue("applicantDefenceResponseDocumentAndDQFlag", applicantDefenceResponseDocumentAndDQFlag);
        return this;
    }

    @JsonIgnore
    public CaseData applicantHearingOtherComments(Object applicantHearingOtherComments) {
        setFieldValue("applicantHearingOtherComments", applicantHearingOtherComments);
        return this;
    }

    @JsonIgnore
    public CaseData applicantMPClaimExpertSpecRequired(Object applicantMPClaimExpertSpecRequired) {
        setFieldValue("applicantMPClaimExpertSpecRequired", applicantMPClaimExpertSpecRequired);
        return this;
    }

    @JsonIgnore
    public CaseData applicantMPClaimMediationSpecRequired(Object applicantMPClaimMediationSpecRequired) {
        setFieldValue("applicantMPClaimMediationSpecRequired", applicantMPClaimMediationSpecRequired);
        return this;
    }

    @JsonIgnore
    public CaseData applicantRevisedHearingRequirements(Object applicantRevisedHearingRequirements) {
        setFieldValue("applicantRevisedHearingRequirements", applicantRevisedHearingRequirements);
        return this;
    }

    @JsonIgnore
    public CaseData applicantSolicitor1CheckEmail(Object applicantSolicitor1CheckEmail) {
        setFieldValue("applicantSolicitor1CheckEmail", applicantSolicitor1CheckEmail);
        return this;
    }

    @JsonIgnore
    public CaseData applicantSolicitor1ClaimStatementOfTruth(Object applicantSolicitor1ClaimStatementOfTruth) {
        setFieldValue("applicantSolicitor1ClaimStatementOfTruth", applicantSolicitor1ClaimStatementOfTruth);
        return this;
    }

    @JsonIgnore
    public CaseData applicantSolicitor1PbaAccountsIsEmpty(Object applicantSolicitor1PbaAccountsIsEmpty) {
        setFieldValue("applicantSolicitor1PbaAccountsIsEmpty", applicantSolicitor1PbaAccountsIsEmpty);
        return this;
    }

    @JsonIgnore
    public CaseData applicantSolicitor1ServiceAddress(Object applicantSolicitor1ServiceAddress) {
        setFieldValue("applicantSolicitor1ServiceAddress", applicantSolicitor1ServiceAddress);
        return this;
    }

    @JsonIgnore
    public CaseData applicantSolicitor1ServiceAddressRequired(Object applicantSolicitor1ServiceAddressRequired) {
        setFieldValue("applicantSolicitor1ServiceAddressRequired", applicantSolicitor1ServiceAddressRequired);
        return this;
    }

    @JsonIgnore
    public CaseData applicantsProceedIntention(Object applicantsProceedIntention) {
        setFieldValue("applicantsProceedIntention", applicantsProceedIntention);
        return this;
    }

    @JsonIgnore
    public CaseData assistedOrderCostsBespoke(Object assistedOrderCostsBespoke) {
        setFieldValue("assistedOrderCostsBespoke", assistedOrderCostsBespoke);
        return this;
    }

    @JsonIgnore
    public CaseData assistedOrderCostsReserved(Object assistedOrderCostsReserved) {
        setFieldValue("assistedOrderCostsReserved", assistedOrderCostsReserved);
        return this;
    }

    @JsonIgnore
    public CaseData atLeastOneClaimResponseTypeForSpecIsFullDefence(Object atLeastOneClaimResponseTypeForSpecIsFullDefence) {
        setFieldValue("atLeastOneClaimResponseTypeForSpecIsFullDefence", atLeastOneClaimResponseTypeForSpecIsFullDefence);
        return this;
    }

    @JsonIgnore
    public CaseData bilingualHint(Object bilingualHint) {
        setFieldValue("bilingualHint", bilingualHint);
        return this;
    }

    @JsonIgnore
    public CaseData bothDefendants(Object bothDefendants) {
        setFieldValue("bothDefendants", bothDefendants);
        return this;
    }

    @JsonIgnore
    public CaseData bothDefendantsSpec(Object bothDefendantsSpec) {
        setFieldValue("bothDefendantsSpec", bothDefendantsSpec);
        return this;
    }

    @JsonIgnore
    public CaseData breakDownInterestDescription(Object breakDownInterestDescription) {
        setFieldValue("breakDownInterestDescription", breakDownInterestDescription);
        return this;
    }

    @JsonIgnore
    public CaseData breakDownInterestTotal(Object breakDownInterestTotal) {
        setFieldValue("breakDownInterestTotal", breakDownInterestTotal);
        return this;
    }

    @JsonIgnore
    public CaseData breathing(Object breathing) {
        setFieldValue("breathing", breathing);
        return this;
    }

    @JsonIgnore
    public CaseData bulkCustomerId(Object bulkCustomerId) {
        setFieldValue("bulkCustomerId", bulkCustomerId);
        return this;
    }

    @JsonIgnore
    public CaseData bundleError(Object bundleError) {
        setFieldValue("bundleError", bundleError);
        return this;
    }

    @JsonIgnore
    public CaseData bundleEvent(Object bundleEvent) {
        setFieldValue("bundleEvent", bundleEvent);
        return this;
    }

    @JsonIgnore
    public CaseData bundleEvidence(Object bundleEvidence) {
        setFieldValue("bundleEvidence", bundleEvidence);
        return this;
    }

    @JsonIgnore
    public CaseData calculatedInterest(Object calculatedInterest) {
        setFieldValue("calculatedInterest", calculatedInterest);
        return this;
    }

    @JsonIgnore
    public CaseData caseDocument1Name(Object caseDocument1Name) {
        setFieldValue("caseDocument1Name", caseDocument1Name);
        return this;
    }

    @JsonIgnore
    public CaseData caseDocumentUploadDate(Object caseDocumentUploadDate) {
        setFieldValue("caseDocumentUploadDate", caseDocumentUploadDate);
        return this;
    }

    @JsonIgnore
    public CaseData caseDocumentUploadDateRes(Object caseDocumentUploadDateRes) {
        setFieldValue("caseDocumentUploadDateRes", caseDocumentUploadDateRes);
        return this;
    }

    @JsonIgnore
    public CaseData caseListDisplayDefendantSolicitorReferences(Object caseListDisplayDefendantSolicitorReferences) {
        setFieldValue("caseListDisplayDefendantSolicitorReferences", caseListDisplayDefendantSolicitorReferences);
        return this;
    }

    @JsonIgnore
    public CaseData caseManagementCategory(Object caseManagementCategory) {
        setFieldValue("caseManagementCategory", caseManagementCategory);
        return this;
    }

    @JsonIgnore
    public CaseData caseManagementLocationTab(Object caseManagementLocationTab) {
        setFieldValue("caseManagementLocationTab", caseManagementLocationTab);
        return this;
    }

    @JsonIgnore
    public CaseData caseManagementOrderAdditional(Object caseManagementOrderAdditional) {
        setFieldValue("caseManagementOrderAdditional", caseManagementOrderAdditional);
        return this;
    }

    @JsonIgnore
    public CaseData caseManagementOrderSelection(Object caseManagementOrderSelection) {
        setFieldValue("caseManagementOrderSelection", caseManagementOrderSelection);
        return this;
    }

    @JsonIgnore
    public CaseData caseMessage(Object caseMessage) {
        setFieldValue("caseMessage", caseMessage);
        return this;
    }

    @JsonIgnore
    public CaseData caseNote(Object caseNote) {
        setFieldValue("caseNote", caseNote);
        return this;
    }

    @JsonIgnore
    public CaseData caseNoteTA(Object caseNoteTA) {
        setFieldValue("caseNoteTA", caseNoteTA);
        return this;
    }

    @JsonIgnore
    public CaseData caseNotesTA(Object caseNotesTA) {
        setFieldValue("caseNotesTA", caseNotesTA);
        return this;
    }

    @JsonIgnore
    public CaseData casePartyRequestForReconsideration(Object casePartyRequestForReconsideration) {
        setFieldValue("casePartyRequestForReconsideration", casePartyRequestForReconsideration);
        return this;
    }

    @JsonIgnore
    public CaseData caseProgAllocatedTrack(Object caseProgAllocatedTrack) {
        setFieldValue("caseProgAllocatedTrack", caseProgAllocatedTrack);
        return this;
    }

    @JsonIgnore
    public CaseData caseTypeFlag(Object caseTypeFlag) {
        setFieldValue("caseTypeFlag", caseTypeFlag);
        return this;
    }

    @JsonIgnore
    public CaseData ccdCaseType(Object ccdCaseType) {
        setFieldValue("ccdCaseType", ccdCaseType);
        return this;
    }

    @JsonIgnore
    public CaseData ccjJudgmentAmountShowInterest(Object ccjJudgmentAmountShowInterest) {
        setFieldValue("ccjJudgmentAmountShowInterest", ccjJudgmentAmountShowInterest);
        return this;
    }

    @JsonIgnore
    public CaseData certOfSC(Object certOfSC) {
        setFieldValue("certOfSC", certOfSC);
        return this;
    }

    @JsonIgnore
    public CaseData changeLanguagePreference(Object changeLanguagePreference) {
        setFieldValue("changeLanguagePreference", changeLanguagePreference);
        return this;
    }

    @JsonIgnore
    public CaseData changeOrganisationRequestField(Object changeOrganisationRequestField) {
        setFieldValue("changeOrganisationRequestField", changeOrganisationRequestField);
        return this;
    }

    @JsonIgnore
    public CaseData channel(Object channel) {
        setFieldValue("channel", channel);
        return this;
    }

    @JsonIgnore
    public CaseData claimAmountBreakupSummaryObject(Object claimAmountBreakupSummaryObject) {
        setFieldValue("claimAmountBreakupSummaryObject", claimAmountBreakupSummaryObject);
        return this;
    }

    @JsonIgnore
    public CaseData claimDeclarationDescription(Object claimDeclarationDescription) {
        setFieldValue("claimDeclarationDescription", claimDeclarationDescription);
        return this;
    }

    @JsonIgnore
    public CaseData claimDetailsNotificationDate(Object claimDetailsNotificationDate) {
        setFieldValue("claimDetailsNotificationDate", claimDetailsNotificationDate);
        return this;
    }

    @JsonIgnore
    public CaseData claimDismissedDeadline(Object claimDismissedDeadline) {
        setFieldValue("claimDismissedDeadline", claimDismissedDeadline);
        return this;
    }

    @JsonIgnore
    public CaseData claimFixedCostsExist(Object claimFixedCostsExist) {
        setFieldValue("claimFixedCostsExist", claimFixedCostsExist);
        return this;
    }

    @JsonIgnore
    public CaseData claimFixedCostsOnEntryDJ(Object claimFixedCostsOnEntryDJ) {
        setFieldValue("claimFixedCostsOnEntryDJ", claimFixedCostsOnEntryDJ);
        return this;
    }

    @JsonIgnore
    public CaseData claimInterest(Object claimInterest) {
        setFieldValue("claimInterest", claimInterest);
        return this;
    }

    @JsonIgnore
    public CaseData claimIssuedHwfForTab(Object claimIssuedHwfForTab) {
        setFieldValue("claimIssuedHwfForTab", claimIssuedHwfForTab);
        return this;
    }

    @JsonIgnore
    public CaseData claimIssuedPBADetails(Object claimIssuedPBADetails) {
        setFieldValue("claimIssuedPBADetails", claimIssuedPBADetails);
        return this;
    }

    @JsonIgnore
    public CaseData claimIssuedPaymentDetails(Object claimIssuedPaymentDetails) {
        setFieldValue("claimIssuedPaymentDetails", claimIssuedPaymentDetails);
        return this;
    }

    @JsonIgnore
    public CaseData claimMovedToMediationOn(Object claimMovedToMediationOn) {
        setFieldValue("claimMovedToMediationOn", claimMovedToMediationOn);
        return this;
    }

    @JsonIgnore
    public CaseData claimNotificationDeadline(Object claimNotificationDeadline) {
        setFieldValue("claimNotificationDeadline", claimNotificationDeadline);
        return this;
    }

    @JsonIgnore
    public CaseData claimProceedsInCaseman(Object claimProceedsInCaseman) {
        setFieldValue("claimProceedsInCaseman", claimProceedsInCaseman);
        return this;
    }

    @JsonIgnore
    public CaseData claimProceedsInCasemanLR(Object claimProceedsInCasemanLR) {
        setFieldValue("claimProceedsInCasemanLR", claimProceedsInCasemanLR);
        return this;
    }

    @JsonIgnore
    public CaseData claimStarted(Object claimStarted) {
        setFieldValue("claimStarted", claimStarted);
        return this;
    }

    @JsonIgnore
    public CaseData claimType(Object claimType) {
        setFieldValue("claimType", claimType);
        return this;
    }

    @JsonIgnore
    public CaseData claimTypeOther(Object claimTypeOther) {
        setFieldValue("claimTypeOther", claimTypeOther);
        return this;
    }

    @JsonIgnore
    public CaseData claimTypeUnSpec(Object claimTypeUnSpec) {
        setFieldValue("claimTypeUnSpec", claimTypeUnSpec);
        return this;
    }

    @JsonIgnore
    public CaseData claimant1ClaimResponseTypeForSpec(Object claimant1ClaimResponseTypeForSpec) {
        setFieldValue("claimant1ClaimResponseTypeForSpec", claimant1ClaimResponseTypeForSpec);
        return this;
    }

    @JsonIgnore
    public CaseData claimant2ClaimResponseTypeForSpec(Object claimant2ClaimResponseTypeForSpec) {
        setFieldValue("claimant2ClaimResponseTypeForSpec", claimant2ClaimResponseTypeForSpec);
        return this;
    }

    @JsonIgnore
    public CaseData claimant2ResponseFlag(Object claimant2ResponseFlag) {
        setFieldValue("claimant2ResponseFlag", claimant2ResponseFlag);
        return this;
    }

    @JsonIgnore
    public CaseData claimantDefenceResDocToDefendant2(Object claimantDefenceResDocToDefendant2) {
        setFieldValue("claimantDefenceResDocToDefendant2", claimantDefenceResDocToDefendant2);
        return this;
    }

    @JsonIgnore
    public CaseData claimantLanguagePreferenceDisplay(Object claimantLanguagePreferenceDisplay) {
        setFieldValue("claimantLanguagePreferenceDisplay", claimantLanguagePreferenceDisplay);
        return this;
    }

    @JsonIgnore
    public CaseData claimantResponseDocumentToDefendant2Flag(Object claimantResponseDocumentToDefendant2Flag) {
        setFieldValue("claimantResponseDocumentToDefendant2Flag", claimantResponseDocumentToDefendant2Flag);
        return this;
    }

    @JsonIgnore
    public CaseData claimantResponseScenarioFlag(Object claimantResponseScenarioFlag) {
        setFieldValue("claimantResponseScenarioFlag", claimantResponseScenarioFlag);
        return this;
    }

    @JsonIgnore
    public CaseData claimantTrialReadyDocumentCreated(Object claimantTrialReadyDocumentCreated) {
        setFieldValue("claimantTrialReadyDocumentCreated", claimantTrialReadyDocumentCreated);
        return this;
    }

    @JsonIgnore
    public CaseData claimantWhoIsDiscontinuing(Object claimantWhoIsDiscontinuing) {
        setFieldValue("claimantWhoIsDiscontinuing", claimantWhoIsDiscontinuing);
        return this;
    }

    @JsonIgnore
    public CaseData claimantWhoIsSettling(Object claimantWhoIsSettling) {
        setFieldValue("claimantWhoIsSettling", claimantWhoIsSettling);
        return this;
    }

    @JsonIgnore
    public CaseData claimantsConsentToDiscontinuance(Object claimantsConsentToDiscontinuance) {
        setFieldValue("claimantsConsentToDiscontinuance", claimantsConsentToDiscontinuance);
        return this;
    }

    @JsonIgnore
    public CaseData clientContext(Object clientContext) {
        setFieldValue("clientContext", clientContext);
        return this;
    }

    @JsonIgnore
    public CaseData coSCApplicationStatus(Object coSCApplicationStatus) {
        setFieldValue("coSCApplicationStatus", coSCApplicationStatus);
        return this;
    }

    @JsonIgnore
    public CaseData confirmListingTickBox(Object confirmListingTickBox) {
        setFieldValue("confirmListingTickBox", confirmListingTickBox);
        return this;
    }

    @JsonIgnore
    public CaseData confirmReferToJudgeDefenceReceived(Object confirmReferToJudgeDefenceReceived) {
        setFieldValue("confirmReferToJudgeDefenceReceived", confirmReferToJudgeDefenceReceived);
        return this;
    }

    @JsonIgnore
    public CaseData consentOrderDocClaimant(Object consentOrderDocClaimant) {
        setFieldValue("consentOrderDocClaimant", consentOrderDocClaimant);
        return this;
    }

    @JsonIgnore
    public CaseData consentOrderDocRespondentSol(Object consentOrderDocRespondentSol) {
        setFieldValue("consentOrderDocRespondentSol", consentOrderDocRespondentSol);
        return this;
    }

    @JsonIgnore
    public CaseData consentOrderDocRespondentSolTwo(Object consentOrderDocRespondentSolTwo) {
        setFieldValue("consentOrderDocRespondentSolTwo", consentOrderDocRespondentSolTwo);
        return this;
    }

    @JsonIgnore
    public CaseData consentOrderDocStaff(Object consentOrderDocStaff) {
        setFieldValue("consentOrderDocStaff", consentOrderDocStaff);
        return this;
    }

    @JsonIgnore
    public CaseData consentOrderDocument(Object consentOrderDocument) {
        setFieldValue("consentOrderDocument", consentOrderDocument);
        return this;
    }

    @JsonIgnore
    public CaseData cosNotifyClaimDefendant1(Object cosNotifyClaimDefendant1) {
        setFieldValue("cosNotifyClaimDefendant1", cosNotifyClaimDefendant1);
        return this;
    }

    @JsonIgnore
    public CaseData cosNotifyClaimDefendant2(Object cosNotifyClaimDefendant2) {
        setFieldValue("cosNotifyClaimDefendant2", cosNotifyClaimDefendant2);
        return this;
    }

    @JsonIgnore
    public CaseData cosNotifyClaimDetails2(Object cosNotifyClaimDetails2) {
        setFieldValue("cosNotifyClaimDetails2", cosNotifyClaimDetails2);
        return this;
    }

    @JsonIgnore
    public CaseData coscSchedulerDeadline(Object coscSchedulerDeadline) {
        setFieldValue("coscSchedulerDeadline", coscSchedulerDeadline);
        return this;
    }

    @JsonIgnore
    public CaseData courtLocation(Object courtLocation) {
        setFieldValue("courtLocation", courtLocation);
        return this;
    }

    @JsonIgnore
    public CaseData courtOfficerGiveReasonsYesNo(Object courtOfficerGiveReasonsYesNo) {
        setFieldValue("courtOfficerGiveReasonsYesNo", courtOfficerGiveReasonsYesNo);
        return this;
    }

    @JsonIgnore
    public CaseData courtPermissionNeeded(Object courtPermissionNeeded) {
        setFieldValue("courtPermissionNeeded", courtPermissionNeeded);
        return this;
    }

    @JsonIgnore
    public CaseData courtStaffNextSteps(Object courtStaffNextSteps) {
        setFieldValue("courtStaffNextSteps", courtStaffNextSteps);
        return this;
    }

    @JsonIgnore
    public CaseData currentDatebox(Object currentDatebox) {
        setFieldValue("currentDatebox", currentDatebox);
        return this;
    }

    @JsonIgnore
    public CaseData currentDateboxDefendantSpec(Object currentDateboxDefendantSpec) {
        setFieldValue("currentDateboxDefendantSpec", currentDateboxDefendantSpec);
        return this;
    }

    @JsonIgnore
    public CaseData currentDefendant(Object currentDefendant) {
        setFieldValue("currentDefendant", currentDefendant);
        return this;
    }

    @JsonIgnore
    public CaseData currentDefendantName(Object currentDefendantName) {
        setFieldValue("currentDefendantName", currentDefendantName);
        return this;
    }

    @JsonIgnore
    public CaseData dashboardNotificationTypeOrder(Object dashboardNotificationTypeOrder) {
        setFieldValue("dashboardNotificationTypeOrder", dashboardNotificationTypeOrder);
        return this;
    }

    @JsonIgnore
    public CaseData dateOfApplication(Object dateOfApplication) {
        setFieldValue("dateOfApplication", dateOfApplication);
        return this;
    }

    @JsonIgnore
    public CaseData decisionOnReconsiderationDocument(Object decisionOnReconsiderationDocument) {
        setFieldValue("decisionOnReconsiderationDocument", decisionOnReconsiderationDocument);
        return this;
    }

    @JsonIgnore
    public CaseData decisionOnRequestReconsiderationOptions(Object decisionOnRequestReconsiderationOptions) {
        setFieldValue("decisionOnRequestReconsiderationOptions", decisionOnRequestReconsiderationOptions);
        return this;
    }

    @JsonIgnore
    public CaseData defaultJudgementOverallTotal(Object defaultJudgementOverallTotal) {
        setFieldValue("defaultJudgementOverallTotal", defaultJudgementOverallTotal);
        return this;
    }

    @JsonIgnore
    public CaseData defenceAdmitPartEmploymentType2Required(Object defenceAdmitPartEmploymentType2Required) {
        setFieldValue("defenceAdmitPartEmploymentType2Required", defenceAdmitPartEmploymentType2Required);
        return this;
    }

    @JsonIgnore
    public CaseData defenceAdmitPartEmploymentTypeRequired(Object defenceAdmitPartEmploymentTypeRequired) {
        setFieldValue("defenceAdmitPartEmploymentTypeRequired", defenceAdmitPartEmploymentTypeRequired);
        return this;
    }

    @JsonIgnore
    public CaseData defenceAdmitPartPaymentTimeRouteGeneric(Object defenceAdmitPartPaymentTimeRouteGeneric) {
        setFieldValue("defenceAdmitPartPaymentTimeRouteGeneric", defenceAdmitPartPaymentTimeRouteGeneric);
        return this;
    }

    @JsonIgnore
    public CaseData defenceAdmitPartPaymentTimeRouteRequired2(Object defenceAdmitPartPaymentTimeRouteRequired2) {
        setFieldValue("defenceAdmitPartPaymentTimeRouteRequired2", defenceAdmitPartPaymentTimeRouteRequired2);
        return this;
    }

    @JsonIgnore
    public CaseData defenceRouteRequired2(Object defenceRouteRequired2) {
        setFieldValue("defenceRouteRequired2", defenceRouteRequired2);
        return this;
    }

    @JsonIgnore
    public CaseData defendantDetailsSpec(Object defendantDetailsSpec) {
        setFieldValue("defendantDetailsSpec", defendantDetailsSpec);
        return this;
    }

    @JsonIgnore
    public CaseData defendantLanguagePreferenceDisplay(Object defendantLanguagePreferenceDisplay) {
        setFieldValue("defendantLanguagePreferenceDisplay", defendantLanguagePreferenceDisplay);
        return this;
    }

    @JsonIgnore
    public CaseData defendantSingleResponseToBothClaimants(Object defendantSingleResponseToBothClaimants) {
        setFieldValue("defendantSingleResponseToBothClaimants", defendantSingleResponseToBothClaimants);
        return this;
    }

    @JsonIgnore
    public CaseData defendantTrialReadyDocumentCreated(Object defendantTrialReadyDocumentCreated) {
        setFieldValue("defendantTrialReadyDocumentCreated", defendantTrialReadyDocumentCreated);
        return this;
    }

    @JsonIgnore
    public CaseData detailsOfClaim(Object detailsOfClaim) {
        setFieldValue("detailsOfClaim", detailsOfClaim);
        return this;
    }

    @JsonIgnore
    public CaseData detailsOfDirection(Object detailsOfDirection) {
        setFieldValue("detailsOfDirection", detailsOfDirection);
        return this;
    }

    @JsonIgnore
    public CaseData detailsOfWhyDoesYouDisputeTheClaim(Object detailsOfWhyDoesYouDisputeTheClaim) {
        setFieldValue("detailsOfWhyDoesYouDisputeTheClaim", detailsOfWhyDoesYouDisputeTheClaim);
        return this;
    }

    @JsonIgnore
    public CaseData detailsOfWhyDoesYouDisputeTheClaim2(Object detailsOfWhyDoesYouDisputeTheClaim2) {
        setFieldValue("detailsOfWhyDoesYouDisputeTheClaim2", detailsOfWhyDoesYouDisputeTheClaim2);
        return this;
    }

    @JsonIgnore
    public CaseData directionOrderDocClaimant(Object directionOrderDocClaimant) {
        setFieldValue("directionOrderDocClaimant", directionOrderDocClaimant);
        return this;
    }

    @JsonIgnore
    public CaseData directionOrderDocRespondentSol(Object directionOrderDocRespondentSol) {
        setFieldValue("directionOrderDocRespondentSol", directionOrderDocRespondentSol);
        return this;
    }

    @JsonIgnore
    public CaseData directionOrderDocRespondentSolTwo(Object directionOrderDocRespondentSolTwo) {
        setFieldValue("directionOrderDocRespondentSolTwo", directionOrderDocRespondentSolTwo);
        return this;
    }

    @JsonIgnore
    public CaseData directionOrderDocStaff(Object directionOrderDocStaff) {
        setFieldValue("directionOrderDocStaff", directionOrderDocStaff);
        return this;
    }

    @JsonIgnore
    public CaseData directionOrderDocument(Object directionOrderDocument) {
        setFieldValue("directionOrderDocument", directionOrderDocument);
        return this;
    }

    @JsonIgnore
    public CaseData disabilityPremiumPayments(Object disabilityPremiumPayments) {
        setFieldValue("disabilityPremiumPayments", disabilityPremiumPayments);
        return this;
    }

    @JsonIgnore
    public CaseData disclosureSelectionEvidence(Object disclosureSelectionEvidence) {
        setFieldValue("disclosureSelectionEvidence", disclosureSelectionEvidence);
        return this;
    }

    @JsonIgnore
    public CaseData disclosureSelectionEvidenceRes(Object disclosureSelectionEvidenceRes) {
        setFieldValue("disclosureSelectionEvidenceRes", disclosureSelectionEvidenceRes);
        return this;
    }

    @JsonIgnore
    public CaseData discontinueClaim(Object discontinueClaim) {
        setFieldValue("discontinueClaim", discontinueClaim);
        return this;
    }

    @JsonIgnore
    public CaseData discontinuingAgainstOneDefendant(Object discontinuingAgainstOneDefendant) {
        setFieldValue("discontinuingAgainstOneDefendant", discontinuingAgainstOneDefendant);
        return this;
    }

    @JsonIgnore
    public CaseData dismissalOrderDocClaimant(Object dismissalOrderDocClaimant) {
        setFieldValue("dismissalOrderDocClaimant", dismissalOrderDocClaimant);
        return this;
    }

    @JsonIgnore
    public CaseData dismissalOrderDocRespondentSol(Object dismissalOrderDocRespondentSol) {
        setFieldValue("dismissalOrderDocRespondentSol", dismissalOrderDocRespondentSol);
        return this;
    }

    @JsonIgnore
    public CaseData dismissalOrderDocRespondentSolTwo(Object dismissalOrderDocRespondentSolTwo) {
        setFieldValue("dismissalOrderDocRespondentSolTwo", dismissalOrderDocRespondentSolTwo);
        return this;
    }

    @JsonIgnore
    public CaseData dismissalOrderDocStaff(Object dismissalOrderDocStaff) {
        setFieldValue("dismissalOrderDocStaff", dismissalOrderDocStaff);
        return this;
    }

    @JsonIgnore
    public CaseData dismissalOrderDocument(Object dismissalOrderDocument) {
        setFieldValue("dismissalOrderDocument", dismissalOrderDocument);
        return this;
    }

    @JsonIgnore
    public CaseData disposalHearingAddNewDirections(Object disposalHearingAddNewDirections) {
        setFieldValue("disposalHearingAddNewDirections", disposalHearingAddNewDirections);
        return this;
    }

    @JsonIgnore
    public CaseData disposalHearingBundle(Object disposalHearingBundle) {
        setFieldValue("disposalHearingBundle", disposalHearingBundle);
        return this;
    }

    @JsonIgnore
    public CaseData disposalHearingBundleDJToggle(Object disposalHearingBundleDJToggle) {
        setFieldValue("disposalHearingBundleDJToggle", disposalHearingBundleDJToggle);
        return this;
    }

    @JsonIgnore
    public CaseData disposalHearingBundleToggle(Object disposalHearingBundleToggle) {
        setFieldValue("disposalHearingBundleToggle", disposalHearingBundleToggle);
        return this;
    }

    @JsonIgnore
    public CaseData disposalHearingClaimSettlingDJToggle(Object disposalHearingClaimSettlingDJToggle) {
        setFieldValue("disposalHearingClaimSettlingDJToggle", disposalHearingClaimSettlingDJToggle);
        return this;
    }

    @JsonIgnore
    public CaseData disposalHearingClaimSettlingToggle(Object disposalHearingClaimSettlingToggle) {
        setFieldValue("disposalHearingClaimSettlingToggle", disposalHearingClaimSettlingToggle);
        return this;
    }

    @JsonIgnore
    public CaseData disposalHearingCostsDJToggle(Object disposalHearingCostsDJToggle) {
        setFieldValue("disposalHearingCostsDJToggle", disposalHearingCostsDJToggle);
        return this;
    }

    @JsonIgnore
    public CaseData disposalHearingCostsToggle(Object disposalHearingCostsToggle) {
        setFieldValue("disposalHearingCostsToggle", disposalHearingCostsToggle);
        return this;
    }

    @JsonIgnore
    public CaseData disposalHearingDisclosureOfDocuments(Object disposalHearingDisclosureOfDocuments) {
        setFieldValue("disposalHearingDisclosureOfDocuments", disposalHearingDisclosureOfDocuments);
        return this;
    }

    @JsonIgnore
    public CaseData disposalHearingDisclosureOfDocumentsDJ(Object disposalHearingDisclosureOfDocumentsDJ) {
        setFieldValue("disposalHearingDisclosureOfDocumentsDJ", disposalHearingDisclosureOfDocumentsDJ);
        return this;
    }

    @JsonIgnore
    public CaseData disposalHearingDisclosureOfDocumentsDJToggle(Object disposalHearingDisclosureOfDocumentsDJToggle) {
        setFieldValue("disposalHearingDisclosureOfDocumentsDJToggle", disposalHearingDisclosureOfDocumentsDJToggle);
        return this;
    }

    @JsonIgnore
    public CaseData disposalHearingDisclosureOfDocumentsToggle(Object disposalHearingDisclosureOfDocumentsToggle) {
        setFieldValue("disposalHearingDisclosureOfDocumentsToggle", disposalHearingDisclosureOfDocumentsToggle);
        return this;
    }

    @JsonIgnore
    public CaseData disposalHearingFinalDisposalHearing(Object disposalHearingFinalDisposalHearing) {
        setFieldValue("disposalHearingFinalDisposalHearing", disposalHearingFinalDisposalHearing);
        return this;
    }

    @JsonIgnore
    public CaseData disposalHearingFinalDisposalHearingDJToggle(Object disposalHearingFinalDisposalHearingDJToggle) {
        setFieldValue("disposalHearingFinalDisposalHearingDJToggle", disposalHearingFinalDisposalHearingDJToggle);
        return this;
    }

    @JsonIgnore
    public CaseData disposalHearingFinalDisposalHearingTimeDJ(Object disposalHearingFinalDisposalHearingTimeDJ) {
        setFieldValue("disposalHearingFinalDisposalHearingTimeDJ", disposalHearingFinalDisposalHearingTimeDJ);
        return this;
    }

    @JsonIgnore
    public CaseData disposalHearingFinalDisposalHearingToggle(Object disposalHearingFinalDisposalHearingToggle) {
        setFieldValue("disposalHearingFinalDisposalHearingToggle", disposalHearingFinalDisposalHearingToggle);
        return this;
    }

    @JsonIgnore
    public CaseData disposalHearingJudgementDeductionValue(Object disposalHearingJudgementDeductionValue) {
        setFieldValue("disposalHearingJudgementDeductionValue", disposalHearingJudgementDeductionValue);
        return this;
    }

    @JsonIgnore
    public CaseData disposalHearingJudgesRecital(Object disposalHearingJudgesRecital) {
        setFieldValue("disposalHearingJudgesRecital", disposalHearingJudgesRecital);
        return this;
    }

    @JsonIgnore
    public CaseData disposalHearingJudgesRecitalDJ(Object disposalHearingJudgesRecitalDJ) {
        setFieldValue("disposalHearingJudgesRecitalDJ", disposalHearingJudgesRecitalDJ);
        return this;
    }

    @JsonIgnore
    public CaseData disposalHearingMedicalEvidence(Object disposalHearingMedicalEvidence) {
        setFieldValue("disposalHearingMedicalEvidence", disposalHearingMedicalEvidence);
        return this;
    }

    @JsonIgnore
    public CaseData disposalHearingMedicalEvidenceDJ(Object disposalHearingMedicalEvidenceDJ) {
        setFieldValue("disposalHearingMedicalEvidenceDJ", disposalHearingMedicalEvidenceDJ);
        return this;
    }

    @JsonIgnore
    public CaseData disposalHearingMedicalEvidenceDJToggle(Object disposalHearingMedicalEvidenceDJToggle) {
        setFieldValue("disposalHearingMedicalEvidenceDJToggle", disposalHearingMedicalEvidenceDJToggle);
        return this;
    }

    @JsonIgnore
    public CaseData disposalHearingMedicalEvidenceToggle(Object disposalHearingMedicalEvidenceToggle) {
        setFieldValue("disposalHearingMedicalEvidenceToggle", disposalHearingMedicalEvidenceToggle);
        return this;
    }

    @JsonIgnore
    public CaseData disposalHearingMethod(Object disposalHearingMethod) {
        setFieldValue("disposalHearingMethod", disposalHearingMethod);
        return this;
    }

    @JsonIgnore
    public CaseData disposalHearingMethodDJ(Object disposalHearingMethodDJ) {
        setFieldValue("disposalHearingMethodDJ", disposalHearingMethodDJ);
        return this;
    }

    @JsonIgnore
    public CaseData disposalHearingMethodInPerson(Object disposalHearingMethodInPerson) {
        setFieldValue("disposalHearingMethodInPerson", disposalHearingMethodInPerson);
        return this;
    }

    @JsonIgnore
    public CaseData disposalHearingMethodTelephoneHearing(Object disposalHearingMethodTelephoneHearing) {
        setFieldValue("disposalHearingMethodTelephoneHearing", disposalHearingMethodTelephoneHearing);
        return this;
    }

    @JsonIgnore
    public CaseData disposalHearingMethodToggle(Object disposalHearingMethodToggle) {
        setFieldValue("disposalHearingMethodToggle", disposalHearingMethodToggle);
        return this;
    }

    @JsonIgnore
    public CaseData disposalHearingMethodVideoConferenceHearing(Object disposalHearingMethodVideoConferenceHearing) {
        setFieldValue("disposalHearingMethodVideoConferenceHearing", disposalHearingMethodVideoConferenceHearing);
        return this;
    }

    @JsonIgnore
    public CaseData disposalHearingNotes(Object disposalHearingNotes) {
        setFieldValue("disposalHearingNotes", disposalHearingNotes);
        return this;
    }

    @JsonIgnore
    public CaseData disposalHearingNotesDJ(Object disposalHearingNotesDJ) {
        setFieldValue("disposalHearingNotesDJ", disposalHearingNotesDJ);
        return this;
    }

    @JsonIgnore
    public CaseData disposalHearingOrderMadeWithoutHearingDJ(Object disposalHearingOrderMadeWithoutHearingDJ) {
        setFieldValue("disposalHearingOrderMadeWithoutHearingDJ", disposalHearingOrderMadeWithoutHearingDJ);
        return this;
    }

    @JsonIgnore
    public CaseData disposalHearingQuestionsToExperts(Object disposalHearingQuestionsToExperts) {
        setFieldValue("disposalHearingQuestionsToExperts", disposalHearingQuestionsToExperts);
        return this;
    }

    @JsonIgnore
    public CaseData disposalHearingQuestionsToExpertsDJ(Object disposalHearingQuestionsToExpertsDJ) {
        setFieldValue("disposalHearingQuestionsToExpertsDJ", disposalHearingQuestionsToExpertsDJ);
        return this;
    }

    @JsonIgnore
    public CaseData disposalHearingQuestionsToExpertsDJToggle(Object disposalHearingQuestionsToExpertsDJToggle) {
        setFieldValue("disposalHearingQuestionsToExpertsDJToggle", disposalHearingQuestionsToExpertsDJToggle);
        return this;
    }

    @JsonIgnore
    public CaseData disposalHearingQuestionsToExpertsToggle(Object disposalHearingQuestionsToExpertsToggle) {
        setFieldValue("disposalHearingQuestionsToExpertsToggle", disposalHearingQuestionsToExpertsToggle);
        return this;
    }

    @JsonIgnore
    public CaseData disposalHearingSchedulesOfLoss(Object disposalHearingSchedulesOfLoss) {
        setFieldValue("disposalHearingSchedulesOfLoss", disposalHearingSchedulesOfLoss);
        return this;
    }

    @JsonIgnore
    public CaseData disposalHearingSchedulesOfLossDJ(Object disposalHearingSchedulesOfLossDJ) {
        setFieldValue("disposalHearingSchedulesOfLossDJ", disposalHearingSchedulesOfLossDJ);
        return this;
    }

    @JsonIgnore
    public CaseData disposalHearingSchedulesOfLossDJToggle(Object disposalHearingSchedulesOfLossDJToggle) {
        setFieldValue("disposalHearingSchedulesOfLossDJToggle", disposalHearingSchedulesOfLossDJToggle);
        return this;
    }

    @JsonIgnore
    public CaseData disposalHearingSchedulesOfLossToggle(Object disposalHearingSchedulesOfLossToggle) {
        setFieldValue("disposalHearingSchedulesOfLossToggle", disposalHearingSchedulesOfLossToggle);
        return this;
    }

    @JsonIgnore
    public CaseData disposalHearingStandardDisposalOrder(Object disposalHearingStandardDisposalOrder) {
        setFieldValue("disposalHearingStandardDisposalOrder", disposalHearingStandardDisposalOrder);
        return this;
    }

    @JsonIgnore
    public CaseData disposalHearingStandardDisposalOrderDJToggle(Object disposalHearingStandardDisposalOrderDJToggle) {
        setFieldValue("disposalHearingStandardDisposalOrderDJToggle", disposalHearingStandardDisposalOrderDJToggle);
        return this;
    }

    @JsonIgnore
    public CaseData disposalHearingWitnessOfFact(Object disposalHearingWitnessOfFact) {
        setFieldValue("disposalHearingWitnessOfFact", disposalHearingWitnessOfFact);
        return this;
    }

    @JsonIgnore
    public CaseData disposalHearingWitnessOfFactDJ(Object disposalHearingWitnessOfFactDJ) {
        setFieldValue("disposalHearingWitnessOfFactDJ", disposalHearingWitnessOfFactDJ);
        return this;
    }

    @JsonIgnore
    public CaseData disposalHearingWitnessOfFactDJToggle(Object disposalHearingWitnessOfFactDJToggle) {
        setFieldValue("disposalHearingWitnessOfFactDJToggle", disposalHearingWitnessOfFactDJToggle);
        return this;
    }

    @JsonIgnore
    public CaseData disposalHearingWitnessOfFactToggle(Object disposalHearingWitnessOfFactToggle) {
        setFieldValue("disposalHearingWitnessOfFactToggle", disposalHearingWitnessOfFactToggle);
        return this;
    }

    @JsonIgnore
    public CaseData disposalOrderWithoutHearing(Object disposalOrderWithoutHearing) {
        setFieldValue("disposalOrderWithoutHearing", disposalOrderWithoutHearing);
        return this;
    }

    @JsonIgnore
    public CaseData documentAndName(Object documentAndName) {
        setFieldValue("documentAndName", documentAndName);
        return this;
    }

    @JsonIgnore
    public CaseData documentAndNameToAdd(Object documentAndNameToAdd) {
        setFieldValue("documentAndNameToAdd", documentAndNameToAdd);
        return this;
    }

    @JsonIgnore
    public CaseData documentAndNote(Object documentAndNote) {
        setFieldValue("documentAndNote", documentAndNote);
        return this;
    }

    @JsonIgnore
    public CaseData documentAndNoteToAdd(Object documentAndNoteToAdd) {
        setFieldValue("documentAndNoteToAdd", documentAndNoteToAdd);
        return this;
    }

    @JsonIgnore
    public CaseData documentAnswers(Object documentAnswers) {
        setFieldValue("documentAnswers", documentAnswers);
        return this;
    }

    @JsonIgnore
    public CaseData documentAnswersApp2(Object documentAnswersApp2) {
        setFieldValue("documentAnswersApp2", documentAnswersApp2);
        return this;
    }

    @JsonIgnore
    public CaseData documentAnswersRes(Object documentAnswersRes) {
        setFieldValue("documentAnswersRes", documentAnswersRes);
        return this;
    }

    @JsonIgnore
    public CaseData documentAnswersRes2(Object documentAnswersRes2) {
        setFieldValue("documentAnswersRes2", documentAnswersRes2);
        return this;
    }

    @JsonIgnore
    public CaseData documentAuthorities(Object documentAuthorities) {
        setFieldValue("documentAuthorities", documentAuthorities);
        return this;
    }

    @JsonIgnore
    public CaseData documentAuthoritiesApp2(Object documentAuthoritiesApp2) {
        setFieldValue("documentAuthoritiesApp2", documentAuthoritiesApp2);
        return this;
    }

    @JsonIgnore
    public CaseData documentAuthoritiesRes(Object documentAuthoritiesRes) {
        setFieldValue("documentAuthoritiesRes", documentAuthoritiesRes);
        return this;
    }

    @JsonIgnore
    public CaseData documentAuthoritiesRes2(Object documentAuthoritiesRes2) {
        setFieldValue("documentAuthoritiesRes2", documentAuthoritiesRes2);
        return this;
    }

    @JsonIgnore
    public CaseData documentCaseSummary(Object documentCaseSummary) {
        setFieldValue("documentCaseSummary", documentCaseSummary);
        return this;
    }

    @JsonIgnore
    public CaseData documentCaseSummaryApp2(Object documentCaseSummaryApp2) {
        setFieldValue("documentCaseSummaryApp2", documentCaseSummaryApp2);
        return this;
    }

    @JsonIgnore
    public CaseData documentCaseSummaryRes(Object documentCaseSummaryRes) {
        setFieldValue("documentCaseSummaryRes", documentCaseSummaryRes);
        return this;
    }

    @JsonIgnore
    public CaseData documentCaseSummaryRes2(Object documentCaseSummaryRes2) {
        setFieldValue("documentCaseSummaryRes2", documentCaseSummaryRes2);
        return this;
    }

    @JsonIgnore
    public CaseData documentCosts(Object documentCosts) {
        setFieldValue("documentCosts", documentCosts);
        return this;
    }

    @JsonIgnore
    public CaseData documentCostsApp2(Object documentCostsApp2) {
        setFieldValue("documentCostsApp2", documentCostsApp2);
        return this;
    }

    @JsonIgnore
    public CaseData documentCostsRes(Object documentCostsRes) {
        setFieldValue("documentCostsRes", documentCostsRes);
        return this;
    }

    @JsonIgnore
    public CaseData documentCostsRes2(Object documentCostsRes2) {
        setFieldValue("documentCostsRes2", documentCostsRes2);
        return this;
    }

    @JsonIgnore
    public CaseData documentDisclosureList(Object documentDisclosureList) {
        setFieldValue("documentDisclosureList", documentDisclosureList);
        return this;
    }

    @JsonIgnore
    public CaseData documentDisclosureListApp2(Object documentDisclosureListApp2) {
        setFieldValue("documentDisclosureListApp2", documentDisclosureListApp2);
        return this;
    }

    @JsonIgnore
    public CaseData documentDisclosureListRes(Object documentDisclosureListRes) {
        setFieldValue("documentDisclosureListRes", documentDisclosureListRes);
        return this;
    }

    @JsonIgnore
    public CaseData documentDisclosureListRes2(Object documentDisclosureListRes2) {
        setFieldValue("documentDisclosureListRes2", documentDisclosureListRes2);
        return this;
    }

    @JsonIgnore
    public CaseData documentEvidenceForTrial(Object documentEvidenceForTrial) {
        setFieldValue("documentEvidenceForTrial", documentEvidenceForTrial);
        return this;
    }

    @JsonIgnore
    public CaseData documentEvidenceForTrialApp2(Object documentEvidenceForTrialApp2) {
        setFieldValue("documentEvidenceForTrialApp2", documentEvidenceForTrialApp2);
        return this;
    }

    @JsonIgnore
    public CaseData documentEvidenceForTrialRes(Object documentEvidenceForTrialRes) {
        setFieldValue("documentEvidenceForTrialRes", documentEvidenceForTrialRes);
        return this;
    }

    @JsonIgnore
    public CaseData documentEvidenceForTrialRes2(Object documentEvidenceForTrialRes2) {
        setFieldValue("documentEvidenceForTrialRes2", documentEvidenceForTrialRes2);
        return this;
    }

    @JsonIgnore
    public CaseData documentExpertReport(Object documentExpertReport) {
        setFieldValue("documentExpertReport", documentExpertReport);
        return this;
    }

    @JsonIgnore
    public CaseData documentExpertReportApp2(Object documentExpertReportApp2) {
        setFieldValue("documentExpertReportApp2", documentExpertReportApp2);
        return this;
    }

    @JsonIgnore
    public CaseData documentExpertReportRes(Object documentExpertReportRes) {
        setFieldValue("documentExpertReportRes", documentExpertReportRes);
        return this;
    }

    @JsonIgnore
    public CaseData documentExpertReportRes2(Object documentExpertReportRes2) {
        setFieldValue("documentExpertReportRes2", documentExpertReportRes2);
        return this;
    }

    @JsonIgnore
    public CaseData documentForDisclosure(Object documentForDisclosure) {
        setFieldValue("documentForDisclosure", documentForDisclosure);
        return this;
    }

    @JsonIgnore
    public CaseData documentForDisclosureApp2(Object documentForDisclosureApp2) {
        setFieldValue("documentForDisclosureApp2", documentForDisclosureApp2);
        return this;
    }

    @JsonIgnore
    public CaseData documentForDisclosureRes(Object documentForDisclosureRes) {
        setFieldValue("documentForDisclosureRes", documentForDisclosureRes);
        return this;
    }

    @JsonIgnore
    public CaseData documentForDisclosureRes2(Object documentForDisclosureRes2) {
        setFieldValue("documentForDisclosureRes2", documentForDisclosureRes2);
        return this;
    }

    @JsonIgnore
    public CaseData documentHearsayNotice(Object documentHearsayNotice) {
        setFieldValue("documentHearsayNotice", documentHearsayNotice);
        return this;
    }

    @JsonIgnore
    public CaseData documentHearsayNoticeApp2(Object documentHearsayNoticeApp2) {
        setFieldValue("documentHearsayNoticeApp2", documentHearsayNoticeApp2);
        return this;
    }

    @JsonIgnore
    public CaseData documentHearsayNoticeRes(Object documentHearsayNoticeRes) {
        setFieldValue("documentHearsayNoticeRes", documentHearsayNoticeRes);
        return this;
    }

    @JsonIgnore
    public CaseData documentHearsayNoticeRes2(Object documentHearsayNoticeRes2) {
        setFieldValue("documentHearsayNoticeRes2", documentHearsayNoticeRes2);
        return this;
    }

    @JsonIgnore
    public CaseData documentJointStatement(Object documentJointStatement) {
        setFieldValue("documentJointStatement", documentJointStatement);
        return this;
    }

    @JsonIgnore
    public CaseData documentJointStatementApp2(Object documentJointStatementApp2) {
        setFieldValue("documentJointStatementApp2", documentJointStatementApp2);
        return this;
    }

    @JsonIgnore
    public CaseData documentJointStatementRes(Object documentJointStatementRes) {
        setFieldValue("documentJointStatementRes", documentJointStatementRes);
        return this;
    }

    @JsonIgnore
    public CaseData documentJointStatementRes2(Object documentJointStatementRes2) {
        setFieldValue("documentJointStatementRes2", documentJointStatementRes2);
        return this;
    }

    @JsonIgnore
    public CaseData documentQuestions(Object documentQuestions) {
        setFieldValue("documentQuestions", documentQuestions);
        return this;
    }

    @JsonIgnore
    public CaseData documentQuestionsApp2(Object documentQuestionsApp2) {
        setFieldValue("documentQuestionsApp2", documentQuestionsApp2);
        return this;
    }

    @JsonIgnore
    public CaseData documentQuestionsRes(Object documentQuestionsRes) {
        setFieldValue("documentQuestionsRes", documentQuestionsRes);
        return this;
    }

    @JsonIgnore
    public CaseData documentQuestionsRes2(Object documentQuestionsRes2) {
        setFieldValue("documentQuestionsRes2", documentQuestionsRes2);
        return this;
    }

    @JsonIgnore
    public CaseData documentReferredInStatement(Object documentReferredInStatement) {
        setFieldValue("documentReferredInStatement", documentReferredInStatement);
        return this;
    }

    @JsonIgnore
    public CaseData documentReferredInStatementApp2(Object documentReferredInStatementApp2) {
        setFieldValue("documentReferredInStatementApp2", documentReferredInStatementApp2);
        return this;
    }

    @JsonIgnore
    public CaseData documentReferredInStatementRes(Object documentReferredInStatementRes) {
        setFieldValue("documentReferredInStatementRes", documentReferredInStatementRes);
        return this;
    }

    @JsonIgnore
    public CaseData documentReferredInStatementRes2(Object documentReferredInStatementRes2) {
        setFieldValue("documentReferredInStatementRes2", documentReferredInStatementRes2);
        return this;
    }

    @JsonIgnore
    public CaseData documentSkeletonArgument(Object documentSkeletonArgument) {
        setFieldValue("documentSkeletonArgument", documentSkeletonArgument);
        return this;
    }

    @JsonIgnore
    public CaseData documentSkeletonArgumentApp2(Object documentSkeletonArgumentApp2) {
        setFieldValue("documentSkeletonArgumentApp2", documentSkeletonArgumentApp2);
        return this;
    }

    @JsonIgnore
    public CaseData documentSkeletonArgumentRes(Object documentSkeletonArgumentRes) {
        setFieldValue("documentSkeletonArgumentRes", documentSkeletonArgumentRes);
        return this;
    }

    @JsonIgnore
    public CaseData documentSkeletonArgumentRes2(Object documentSkeletonArgumentRes2) {
        setFieldValue("documentSkeletonArgumentRes2", documentSkeletonArgumentRes2);
        return this;
    }

    @JsonIgnore
    public CaseData documentToKeepCollection(Object documentToKeepCollection) {
        setFieldValue("documentToKeepCollection", documentToKeepCollection);
        return this;
    }

    @JsonIgnore
    public CaseData documentWitnessStatement(Object documentWitnessStatement) {
        setFieldValue("documentWitnessStatement", documentWitnessStatement);
        return this;
    }

    @JsonIgnore
    public CaseData documentWitnessStatementApp2(Object documentWitnessStatementApp2) {
        setFieldValue("documentWitnessStatementApp2", documentWitnessStatementApp2);
        return this;
    }

    @JsonIgnore
    public CaseData documentWitnessStatementRes(Object documentWitnessStatementRes) {
        setFieldValue("documentWitnessStatementRes", documentWitnessStatementRes);
        return this;
    }

    @JsonIgnore
    public CaseData documentWitnessStatementRes2(Object documentWitnessStatementRes2) {
        setFieldValue("documentWitnessStatementRes2", documentWitnessStatementRes2);
        return this;
    }

    @JsonIgnore
    public CaseData documentWitnessSummary(Object documentWitnessSummary) {
        setFieldValue("documentWitnessSummary", documentWitnessSummary);
        return this;
    }

    @JsonIgnore
    public CaseData documentWitnessSummaryApp2(Object documentWitnessSummaryApp2) {
        setFieldValue("documentWitnessSummaryApp2", documentWitnessSummaryApp2);
        return this;
    }

    @JsonIgnore
    public CaseData documentWitnessSummaryRes(Object documentWitnessSummaryRes) {
        setFieldValue("documentWitnessSummaryRes", documentWitnessSummaryRes);
        return this;
    }

    @JsonIgnore
    public CaseData documentWitnessSummaryRes2(Object documentWitnessSummaryRes2) {
        setFieldValue("documentWitnessSummaryRes2", documentWitnessSummaryRes2);
        return this;
    }

    @JsonIgnore
    public CaseData drawDirectionsOrder(Object drawDirectionsOrder) {
        setFieldValue("drawDirectionsOrder", drawDirectionsOrder);
        return this;
    }

    @JsonIgnore
    public CaseData drawDirectionsOrderSmallClaims(Object drawDirectionsOrderSmallClaims) {
        setFieldValue("drawDirectionsOrderSmallClaims", drawDirectionsOrderSmallClaims);
        return this;
    }

    @JsonIgnore
    public CaseData drawDirectionsOrderSmallClaimsAdditionalDirections(Object drawDirectionsOrderSmallClaimsAdditionalDirections) {
        setFieldValue("drawDirectionsOrderSmallClaimsAdditionalDirections", drawDirectionsOrderSmallClaimsAdditionalDirections);
        return this;
    }

    @JsonIgnore
    public CaseData eaCourtLocation(Object eaCourtLocation) {
        setFieldValue("eaCourtLocation", eaCourtLocation);
        return this;
    }

    @JsonIgnore
    public CaseData enableUploadEvent(Object enableUploadEvent) {
        setFieldValue("enableUploadEvent", enableUploadEvent);
        return this;
    }

    @JsonIgnore
    public CaseData eventDescriptionRTJ(Object eventDescriptionRTJ) {
        setFieldValue("eventDescriptionRTJ", eventDescriptionRTJ);
        return this;
    }

    @JsonIgnore
    public CaseData evidenceUploadNotificationSent(Object evidenceUploadNotificationSent) {
        setFieldValue("evidenceUploadNotificationSent", evidenceUploadNotificationSent);
        return this;
    }

    @JsonIgnore
    public CaseData evidenceUploadOptions(Object evidenceUploadOptions) {
        setFieldValue("evidenceUploadOptions", evidenceUploadOptions);
        return this;
    }

    @JsonIgnore
    public CaseData expertJointFlag(Object expertJointFlag) {
        setFieldValue("expertJointFlag", expertJointFlag);
        return this;
    }

    @JsonIgnore
    public CaseData expertReportFlag(Object expertReportFlag) {
        setFieldValue("expertReportFlag", expertReportFlag);
        return this;
    }

    @JsonIgnore
    public CaseData expertSelectionEvidence(Object expertSelectionEvidence) {
        setFieldValue("expertSelectionEvidence", expertSelectionEvidence);
        return this;
    }

    @JsonIgnore
    public CaseData expertSelectionEvidenceRes(Object expertSelectionEvidenceRes) {
        setFieldValue("expertSelectionEvidenceRes", expertSelectionEvidenceRes);
        return this;
    }

    @JsonIgnore
    public CaseData expertSelectionEvidenceSmallClaim(Object expertSelectionEvidenceSmallClaim) {
        setFieldValue("expertSelectionEvidenceSmallClaim", expertSelectionEvidenceSmallClaim);
        return this;
    }

    @JsonIgnore
    public CaseData expertSelectionEvidenceSmallClaimRes(Object expertSelectionEvidenceSmallClaimRes) {
        setFieldValue("expertSelectionEvidenceSmallClaimRes", expertSelectionEvidenceSmallClaimRes);
        return this;
    }

    @JsonIgnore
    public CaseData fastClaims(Object fastClaims) {
        setFieldValue("fastClaims", fastClaims);
        return this;
    }

    @JsonIgnore
    public CaseData fastTrackAddNewDirections(Object fastTrackAddNewDirections) {
        setFieldValue("fastTrackAddNewDirections", fastTrackAddNewDirections);
        return this;
    }

    @JsonIgnore
    public CaseData fastTrackAllocation(Object fastTrackAllocation) {
        setFieldValue("fastTrackAllocation", fastTrackAllocation);
        return this;
    }

    @JsonIgnore
    public CaseData fastTrackAltDisputeResolutionToggle(Object fastTrackAltDisputeResolutionToggle) {
        setFieldValue("fastTrackAltDisputeResolutionToggle", fastTrackAltDisputeResolutionToggle);
        return this;
    }

    @JsonIgnore
    public CaseData fastTrackBuildingDispute(Object fastTrackBuildingDispute) {
        setFieldValue("fastTrackBuildingDispute", fastTrackBuildingDispute);
        return this;
    }

    @JsonIgnore
    public CaseData fastTrackClinicalNegligence(Object fastTrackClinicalNegligence) {
        setFieldValue("fastTrackClinicalNegligence", fastTrackClinicalNegligence);
        return this;
    }

    @JsonIgnore
    public CaseData fastTrackCostsToggle(Object fastTrackCostsToggle) {
        setFieldValue("fastTrackCostsToggle", fastTrackCostsToggle);
        return this;
    }

    @JsonIgnore
    public CaseData fastTrackCreditHire(Object fastTrackCreditHire) {
        setFieldValue("fastTrackCreditHire", fastTrackCreditHire);
        return this;
    }

    @JsonIgnore
    public CaseData fastTrackDisclosureOfDocuments(Object fastTrackDisclosureOfDocuments) {
        setFieldValue("fastTrackDisclosureOfDocuments", fastTrackDisclosureOfDocuments);
        return this;
    }

    @JsonIgnore
    public CaseData fastTrackDisclosureOfDocumentsToggle(Object fastTrackDisclosureOfDocumentsToggle) {
        setFieldValue("fastTrackDisclosureOfDocumentsToggle", fastTrackDisclosureOfDocumentsToggle);
        return this;
    }

    @JsonIgnore
    public CaseData fastTrackHearingTime(Object fastTrackHearingTime) {
        setFieldValue("fastTrackHearingTime", fastTrackHearingTime);
        return this;
    }

    @JsonIgnore
    public CaseData fastTrackHousingDisrepair(Object fastTrackHousingDisrepair) {
        setFieldValue("fastTrackHousingDisrepair", fastTrackHousingDisrepair);
        return this;
    }

    @JsonIgnore
    public CaseData fastTrackJudgementDeductionValue(Object fastTrackJudgementDeductionValue) {
        setFieldValue("fastTrackJudgementDeductionValue", fastTrackJudgementDeductionValue);
        return this;
    }

    @JsonIgnore
    public CaseData fastTrackJudgesRecital(Object fastTrackJudgesRecital) {
        setFieldValue("fastTrackJudgesRecital", fastTrackJudgesRecital);
        return this;
    }

    @JsonIgnore
    public CaseData fastTrackMethod(Object fastTrackMethod) {
        setFieldValue("fastTrackMethod", fastTrackMethod);
        return this;
    }

    @JsonIgnore
    public CaseData fastTrackMethodInPerson(Object fastTrackMethodInPerson) {
        setFieldValue("fastTrackMethodInPerson", fastTrackMethodInPerson);
        return this;
    }

    @JsonIgnore
    public CaseData fastTrackMethodTelephoneHearing(Object fastTrackMethodTelephoneHearing) {
        setFieldValue("fastTrackMethodTelephoneHearing", fastTrackMethodTelephoneHearing);
        return this;
    }

    @JsonIgnore
    public CaseData fastTrackMethodToggle(Object fastTrackMethodToggle) {
        setFieldValue("fastTrackMethodToggle", fastTrackMethodToggle);
        return this;
    }

    @JsonIgnore
    public CaseData fastTrackMethodVideoConferenceHearing(Object fastTrackMethodVideoConferenceHearing) {
        setFieldValue("fastTrackMethodVideoConferenceHearing", fastTrackMethodVideoConferenceHearing);
        return this;
    }

    @JsonIgnore
    public CaseData fastTrackNotes(Object fastTrackNotes) {
        setFieldValue("fastTrackNotes", fastTrackNotes);
        return this;
    }

    @JsonIgnore
    public CaseData fastTrackOrderWithoutJudgement(Object fastTrackOrderWithoutJudgement) {
        setFieldValue("fastTrackOrderWithoutJudgement", fastTrackOrderWithoutJudgement);
        return this;
    }

    @JsonIgnore
    public CaseData fastTrackPPI(Object fastTrackPPI) {
        setFieldValue("fastTrackPPI", fastTrackPPI);
        return this;
    }

    @JsonIgnore
    public CaseData fastTrackPenalNotice(Object fastTrackPenalNotice) {
        setFieldValue("fastTrackPenalNotice", fastTrackPenalNotice);
        return this;
    }

    @JsonIgnore
    public CaseData fastTrackPenalNoticeToggle(Object fastTrackPenalNoticeToggle) {
        setFieldValue("fastTrackPenalNoticeToggle", fastTrackPenalNoticeToggle);
        return this;
    }

    @JsonIgnore
    public CaseData fastTrackPersonalInjury(Object fastTrackPersonalInjury) {
        setFieldValue("fastTrackPersonalInjury", fastTrackPersonalInjury);
        return this;
    }

    @JsonIgnore
    public CaseData fastTrackRoadTrafficAccident(Object fastTrackRoadTrafficAccident) {
        setFieldValue("fastTrackRoadTrafficAccident", fastTrackRoadTrafficAccident);
        return this;
    }

    @JsonIgnore
    public CaseData fastTrackSchedulesOfLoss(Object fastTrackSchedulesOfLoss) {
        setFieldValue("fastTrackSchedulesOfLoss", fastTrackSchedulesOfLoss);
        return this;
    }

    @JsonIgnore
    public CaseData fastTrackSchedulesOfLossToggle(Object fastTrackSchedulesOfLossToggle) {
        setFieldValue("fastTrackSchedulesOfLossToggle", fastTrackSchedulesOfLossToggle);
        return this;
    }

    @JsonIgnore
    public CaseData fastTrackSettlementToggle(Object fastTrackSettlementToggle) {
        setFieldValue("fastTrackSettlementToggle", fastTrackSettlementToggle);
        return this;
    }

    @JsonIgnore
    public CaseData fastTrackTrial(Object fastTrackTrial) {
        setFieldValue("fastTrackTrial", fastTrackTrial);
        return this;
    }

    @JsonIgnore
    public CaseData fastTrackTrialBundleToggle(Object fastTrackTrialBundleToggle) {
        setFieldValue("fastTrackTrialBundleToggle", fastTrackTrialBundleToggle);
        return this;
    }

    @JsonIgnore
    public CaseData fastTrackTrialDateToToggle(Object fastTrackTrialDateToToggle) {
        setFieldValue("fastTrackTrialDateToToggle", fastTrackTrialDateToToggle);
        return this;
    }

    @JsonIgnore
    public CaseData fastTrackTrialToggle(Object fastTrackTrialToggle) {
        setFieldValue("fastTrackTrialToggle", fastTrackTrialToggle);
        return this;
    }

    @JsonIgnore
    public CaseData fastTrackVariationOfDirectionsToggle(Object fastTrackVariationOfDirectionsToggle) {
        setFieldValue("fastTrackVariationOfDirectionsToggle", fastTrackVariationOfDirectionsToggle);
        return this;
    }

    @JsonIgnore
    public CaseData fastTrackWitnessOfFact(Object fastTrackWitnessOfFact) {
        setFieldValue("fastTrackWitnessOfFact", fastTrackWitnessOfFact);
        return this;
    }

    @JsonIgnore
    public CaseData fastTrackWitnessOfFactToggle(Object fastTrackWitnessOfFactToggle) {
        setFieldValue("fastTrackWitnessOfFactToggle", fastTrackWitnessOfFactToggle);
        return this;
    }

    @JsonIgnore
    public CaseData featureToggleWA(Object featureToggleWA) {
        setFieldValue("featureToggleWA", featureToggleWA);
        return this;
    }

    @JsonIgnore
    public CaseData finalOrderAppealToggle(Object finalOrderAppealToggle) {
        setFieldValue("finalOrderAppealToggle", finalOrderAppealToggle);
        return this;
    }

    @JsonIgnore
    public CaseData finalOrderDocument(Object finalOrderDocument) {
        setFieldValue("finalOrderDocument", finalOrderDocument);
        return this;
    }

    @JsonIgnore
    public CaseData finalOrderDownloadTemplateDocument(Object finalOrderDownloadTemplateDocument) {
        setFieldValue("finalOrderDownloadTemplateDocument", finalOrderDownloadTemplateDocument);
        return this;
    }

    @JsonIgnore
    public CaseData finalOrderFurtherHearingComplex(Object finalOrderFurtherHearingComplex) {
        setFieldValue("finalOrderFurtherHearingComplex", finalOrderFurtherHearingComplex);
        return this;
    }

    @JsonIgnore
    public CaseData finalOrderGiveReasonsComplex(Object finalOrderGiveReasonsComplex) {
        setFieldValue("finalOrderGiveReasonsComplex", finalOrderGiveReasonsComplex);
        return this;
    }

    @JsonIgnore
    public CaseData finalOrderGiveReasonsYesNo(Object finalOrderGiveReasonsYesNo) {
        setFieldValue("finalOrderGiveReasonsYesNo", finalOrderGiveReasonsYesNo);
        return this;
    }

    @JsonIgnore
    public CaseData finalOrderJudgeHeardFrom(Object finalOrderJudgeHeardFrom) {
        setFieldValue("finalOrderJudgeHeardFrom", finalOrderJudgeHeardFrom);
        return this;
    }

    @JsonIgnore
    public CaseData finalOrderJudgePapers(Object finalOrderJudgePapers) {
        setFieldValue("finalOrderJudgePapers", finalOrderJudgePapers);
        return this;
    }

    @JsonIgnore
    public CaseData finalOrderMadeSelection(Object finalOrderMadeSelection) {
        setFieldValue("finalOrderMadeSelection", finalOrderMadeSelection);
        return this;
    }

    @JsonIgnore
    public CaseData finalOrderOrderedThatText(Object finalOrderOrderedThatText) {
        setFieldValue("finalOrderOrderedThatText", finalOrderOrderedThatText);
        return this;
    }

    @JsonIgnore
    public CaseData finalOrderRecitalsRecorded(Object finalOrderRecitalsRecorded) {
        setFieldValue("finalOrderRecitalsRecorded", finalOrderRecitalsRecorded);
        return this;
    }

    @JsonIgnore
    public CaseData finalOrderTrackToggle(Object finalOrderTrackToggle) {
        setFieldValue("finalOrderTrackToggle", finalOrderTrackToggle);
        return this;
    }

    @JsonIgnore
    public CaseData fixedCosts(Object fixedCosts) {
        setFieldValue("fixedCosts", fixedCosts);
        return this;
    }

    @JsonIgnore
    public CaseData flightDelayDetails(Object flightDelayDetails) {
        setFieldValue("flightDelayDetails", flightDelayDetails);
        return this;
    }

    @JsonIgnore
    public CaseData freeFormHearingNotes(Object freeFormHearingNotes) {
        setFieldValue("freeFormHearingNotes", freeFormHearingNotes);
        return this;
    }

    @JsonIgnore
    public CaseData freeFormOrderedTextArea(Object freeFormOrderedTextArea) {
        setFieldValue("freeFormOrderedTextArea", freeFormOrderedTextArea);
        return this;
    }

    @JsonIgnore
    public CaseData freeFormRecordedTextArea(Object freeFormRecordedTextArea) {
        setFieldValue("freeFormRecordedTextArea", freeFormRecordedTextArea);
        return this;
    }

    @JsonIgnore
    public CaseData fullAdmissionAndFullAmountPaid(Object fullAdmissionAndFullAmountPaid) {
        setFieldValue("fullAdmissionAndFullAmountPaid", fullAdmissionAndFullAmountPaid);
        return this;
    }

    @JsonIgnore
    public CaseData fullAdmitNoPaymentSchedulerProcessed(Object fullAdmitNoPaymentSchedulerProcessed) {
        setFieldValue("fullAdmitNoPaymentSchedulerProcessed", fullAdmitNoPaymentSchedulerProcessed);
        return this;
    }

    @JsonIgnore
    public CaseData gaAddlDoc(Object gaAddlDoc) {
        setFieldValue("gaAddlDoc", gaAddlDoc);
        return this;
    }

    @JsonIgnore
    public CaseData gaAddlDocBundle(Object gaAddlDocBundle) {
        setFieldValue("gaAddlDocBundle", gaAddlDocBundle);
        return this;
    }

    @JsonIgnore
    public CaseData gaAddlDocClaimant(Object gaAddlDocClaimant) {
        setFieldValue("gaAddlDocClaimant", gaAddlDocClaimant);
        return this;
    }

    @JsonIgnore
    public CaseData gaAddlDocRespondentSol(Object gaAddlDocRespondentSol) {
        setFieldValue("gaAddlDocRespondentSol", gaAddlDocRespondentSol);
        return this;
    }

    @JsonIgnore
    public CaseData gaAddlDocRespondentSolTwo(Object gaAddlDocRespondentSolTwo) {
        setFieldValue("gaAddlDocRespondentSolTwo", gaAddlDocRespondentSolTwo);
        return this;
    }

    @JsonIgnore
    public CaseData gaAddlDocStaff(Object gaAddlDocStaff) {
        setFieldValue("gaAddlDocStaff", gaAddlDocStaff);
        return this;
    }

    @JsonIgnore
    public CaseData gaDetailsMasterCollection(Object gaDetailsMasterCollection) {
        setFieldValue("gaDetailsMasterCollection", gaDetailsMasterCollection);
        return this;
    }

    @JsonIgnore
    public CaseData gaDraftDocClaimant(Object gaDraftDocClaimant) {
        setFieldValue("gaDraftDocClaimant", gaDraftDocClaimant);
        return this;
    }

    @JsonIgnore
    public CaseData gaDraftDocRespondentSol(Object gaDraftDocRespondentSol) {
        setFieldValue("gaDraftDocRespondentSol", gaDraftDocRespondentSol);
        return this;
    }

    @JsonIgnore
    public CaseData gaDraftDocRespondentSolTwo(Object gaDraftDocRespondentSolTwo) {
        setFieldValue("gaDraftDocRespondentSolTwo", gaDraftDocRespondentSolTwo);
        return this;
    }

    @JsonIgnore
    public CaseData gaDraftDocStaff(Object gaDraftDocStaff) {
        setFieldValue("gaDraftDocStaff", gaDraftDocStaff);
        return this;
    }

    @JsonIgnore
    public CaseData gaEaCourtLocation(Object gaEaCourtLocation) {
        setFieldValue("gaEaCourtLocation", gaEaCourtLocation);
        return this;
    }

    @JsonIgnore
    public CaseData gaEvidenceDocClaimant(Object gaEvidenceDocClaimant) {
        setFieldValue("gaEvidenceDocClaimant", gaEvidenceDocClaimant);
        return this;
    }

    @JsonIgnore
    public CaseData gaEvidenceDocRespondentSol(Object gaEvidenceDocRespondentSol) {
        setFieldValue("gaEvidenceDocRespondentSol", gaEvidenceDocRespondentSol);
        return this;
    }

    @JsonIgnore
    public CaseData gaEvidenceDocRespondentSolTwo(Object gaEvidenceDocRespondentSolTwo) {
        setFieldValue("gaEvidenceDocRespondentSolTwo", gaEvidenceDocRespondentSolTwo);
        return this;
    }

    @JsonIgnore
    public CaseData gaEvidenceDocStaff(Object gaEvidenceDocStaff) {
        setFieldValue("gaEvidenceDocStaff", gaEvidenceDocStaff);
        return this;
    }

    @JsonIgnore
    public CaseData gaRespDocClaimant(Object gaRespDocClaimant) {
        setFieldValue("gaRespDocClaimant", gaRespDocClaimant);
        return this;
    }

    @JsonIgnore
    public CaseData gaRespDocRespondentSol(Object gaRespDocRespondentSol) {
        setFieldValue("gaRespDocRespondentSol", gaRespDocRespondentSol);
        return this;
    }

    @JsonIgnore
    public CaseData gaRespDocRespondentSolTwo(Object gaRespDocRespondentSolTwo) {
        setFieldValue("gaRespDocRespondentSolTwo", gaRespDocRespondentSolTwo);
        return this;
    }

    @JsonIgnore
    public CaseData gaRespDocStaff(Object gaRespDocStaff) {
        setFieldValue("gaRespDocStaff", gaRespDocStaff);
        return this;
    }

    @JsonIgnore
    public CaseData gaRespDocument(Object gaRespDocument) {
        setFieldValue("gaRespDocument", gaRespDocument);
        return this;
    }

    @JsonIgnore
    public CaseData gaRespondDoc(Object gaRespondDoc) {
        setFieldValue("gaRespondDoc", gaRespondDoc);
        return this;
    }

    @JsonIgnore
    public CaseData gaWaTrackLabel(Object gaWaTrackLabel) {
        setFieldValue("gaWaTrackLabel", gaWaTrackLabel);
        return this;
    }

    @JsonIgnore
    public CaseData generalAppAskForCosts(Object generalAppAskForCosts) {
        setFieldValue("generalAppAskForCosts", generalAppAskForCosts);
        return this;
    }

    @JsonIgnore
    public CaseData generalAppDetailsOfOrder(Object generalAppDetailsOfOrder) {
        setFieldValue("generalAppDetailsOfOrder", generalAppDetailsOfOrder);
        return this;
    }

    @JsonIgnore
    public CaseData generalAppDetailsOfOrderColl(Object generalAppDetailsOfOrderColl) {
        setFieldValue("generalAppDetailsOfOrderColl", generalAppDetailsOfOrderColl);
        return this;
    }

    @JsonIgnore
    public CaseData generalAppHearingDate(Object generalAppHearingDate) {
        setFieldValue("generalAppHearingDate", generalAppHearingDate);
        return this;
    }

    @JsonIgnore
    public CaseData generalAppN245FormUpload(Object generalAppN245FormUpload) {
        setFieldValue("generalAppN245FormUpload", generalAppN245FormUpload);
        return this;
    }

    @JsonIgnore
    public CaseData generalAppParentClaimantIsApplicant(Object generalAppParentClaimantIsApplicant) {
        setFieldValue("generalAppParentClaimantIsApplicant", generalAppParentClaimantIsApplicant);
        return this;
    }

    @JsonIgnore
    public CaseData generalAppReasonsOfOrder(Object generalAppReasonsOfOrder) {
        setFieldValue("generalAppReasonsOfOrder", generalAppReasonsOfOrder);
        return this;
    }

    @JsonIgnore
    public CaseData generalAppReasonsOfOrderColl(Object generalAppReasonsOfOrderColl) {
        setFieldValue("generalAppReasonsOfOrderColl", generalAppReasonsOfOrderColl);
        return this;
    }

    @JsonIgnore
    public CaseData generalAppStatementOfTruth(Object generalAppStatementOfTruth) {
        setFieldValue("generalAppStatementOfTruth", generalAppStatementOfTruth);
        return this;
    }

    @JsonIgnore
    public CaseData generalAppTypeLR(Object generalAppTypeLR) {
        setFieldValue("generalAppTypeLR", generalAppTypeLR);
        return this;
    }

    @JsonIgnore
    public CaseData generalAppVaryJudgementType(Object generalAppVaryJudgementType) {
        setFieldValue("generalAppVaryJudgementType", generalAppVaryJudgementType);
        return this;
    }

    @JsonIgnore
    public CaseData generalOrderDocClaimant(Object generalOrderDocClaimant) {
        setFieldValue("generalOrderDocClaimant", generalOrderDocClaimant);
        return this;
    }

    @JsonIgnore
    public CaseData generalOrderDocRespondentSol(Object generalOrderDocRespondentSol) {
        setFieldValue("generalOrderDocRespondentSol", generalOrderDocRespondentSol);
        return this;
    }

    @JsonIgnore
    public CaseData generalOrderDocRespondentSolTwo(Object generalOrderDocRespondentSolTwo) {
        setFieldValue("generalOrderDocRespondentSolTwo", generalOrderDocRespondentSolTwo);
        return this;
    }

    @JsonIgnore
    public CaseData generalOrderDocStaff(Object generalOrderDocStaff) {
        setFieldValue("generalOrderDocStaff", generalOrderDocStaff);
        return this;
    }

    @JsonIgnore
    public CaseData generalOrderDocument(Object generalOrderDocument) {
        setFieldValue("generalOrderDocument", generalOrderDocument);
        return this;
    }

    @JsonIgnore
    public CaseData genericLitigationFriend(Object genericLitigationFriend) {
        setFieldValue("genericLitigationFriend", genericLitigationFriend);
        return this;
    }

    @JsonIgnore
    public CaseData hearingDueDate(Object hearingDueDate) {
        setFieldValue("hearingDueDate", hearingDueDate);
        return this;
    }

    @JsonIgnore
    public CaseData hearingDurationInMinutesAHN(Object hearingDurationInMinutesAHN) {
        setFieldValue("hearingDurationInMinutesAHN", hearingDurationInMinutesAHN);
        return this;
    }

    @JsonIgnore
    public CaseData hearingDurationTextApplicant(Object hearingDurationTextApplicant) {
        setFieldValue("hearingDurationTextApplicant", hearingDurationTextApplicant);
        return this;
    }

    @JsonIgnore
    public CaseData hearingDurationTextRespondent1(Object hearingDurationTextRespondent1) {
        setFieldValue("hearingDurationTextRespondent1", hearingDurationTextRespondent1);
        return this;
    }

    @JsonIgnore
    public CaseData hearingDurationTextRespondent2(Object hearingDurationTextRespondent2) {
        setFieldValue("hearingDurationTextRespondent2", hearingDurationTextRespondent2);
        return this;
    }

    @JsonIgnore
    public CaseData hearingFeePBADetails(Object hearingFeePBADetails) {
        setFieldValue("hearingFeePBADetails", hearingFeePBADetails);
        return this;
    }

    @JsonIgnore
    public CaseData hearingHwfForTab(Object hearingHwfForTab) {
        setFieldValue("hearingHwfForTab", hearingHwfForTab);
        return this;
    }

    @JsonIgnore
    public CaseData hearingListedDynamicList(Object hearingListedDynamicList) {
        setFieldValue("hearingListedDynamicList", hearingListedDynamicList);
        return this;
    }

    @JsonIgnore
    public CaseData hearingLocationCourtName(Object hearingLocationCourtName) {
        setFieldValue("hearingLocationCourtName", hearingLocationCourtName);
        return this;
    }

    @JsonIgnore
    public CaseData hearingMethod(Object hearingMethod) {
        setFieldValue("hearingMethod", hearingMethod);
        return this;
    }

    @JsonIgnore
    public CaseData hearingMethodValuesDisposalHearing(Object hearingMethodValuesDisposalHearing) {
        setFieldValue("hearingMethodValuesDisposalHearing", hearingMethodValuesDisposalHearing);
        return this;
    }

    @JsonIgnore
    public CaseData hearingMethodValuesDisposalHearingDJ(Object hearingMethodValuesDisposalHearingDJ) {
        setFieldValue("hearingMethodValuesDisposalHearingDJ", hearingMethodValuesDisposalHearingDJ);
        return this;
    }

    @JsonIgnore
    public CaseData hearingMethodValuesFastTrack(Object hearingMethodValuesFastTrack) {
        setFieldValue("hearingMethodValuesFastTrack", hearingMethodValuesFastTrack);
        return this;
    }

    @JsonIgnore
    public CaseData hearingMethodValuesSmallClaims(Object hearingMethodValuesSmallClaims) {
        setFieldValue("hearingMethodValuesSmallClaims", hearingMethodValuesSmallClaims);
        return this;
    }

    @JsonIgnore
    public CaseData hearingMethodValuesTrialHearingDJ(Object hearingMethodValuesTrialHearingDJ) {
        setFieldValue("hearingMethodValuesTrialHearingDJ", hearingMethodValuesTrialHearingDJ);
        return this;
    }

    @JsonIgnore
    public CaseData hearingNotes(Object hearingNotes) {
        setFieldValue("hearingNotes", hearingNotes);
        return this;
    }

    @JsonIgnore
    public CaseData hearingNoticeDocClaimant(Object hearingNoticeDocClaimant) {
        setFieldValue("hearingNoticeDocClaimant", hearingNoticeDocClaimant);
        return this;
    }

    @JsonIgnore
    public CaseData hearingNoticeDocRespondentSol(Object hearingNoticeDocRespondentSol) {
        setFieldValue("hearingNoticeDocRespondentSol", hearingNoticeDocRespondentSol);
        return this;
    }

    @JsonIgnore
    public CaseData hearingNoticeDocRespondentSolTwo(Object hearingNoticeDocRespondentSolTwo) {
        setFieldValue("hearingNoticeDocRespondentSolTwo", hearingNoticeDocRespondentSolTwo);
        return this;
    }

    @JsonIgnore
    public CaseData hearingNoticeDocStaff(Object hearingNoticeDocStaff) {
        setFieldValue("hearingNoticeDocStaff", hearingNoticeDocStaff);
        return this;
    }

    @JsonIgnore
    public CaseData hearingNoticeDocument(Object hearingNoticeDocument) {
        setFieldValue("hearingNoticeDocument", hearingNoticeDocument);
        return this;
    }

    @JsonIgnore
    public CaseData hearingNoticeList(Object hearingNoticeList) {
        setFieldValue("hearingNoticeList", hearingNoticeList);
        return this;
    }

    @JsonIgnore
    public CaseData hearingNoticeListOther(Object hearingNoticeListOther) {
        setFieldValue("hearingNoticeListOther", hearingNoticeListOther);
        return this;
    }

    @JsonIgnore
    public CaseData hearingOrderDocClaimant(Object hearingOrderDocClaimant) {
        setFieldValue("hearingOrderDocClaimant", hearingOrderDocClaimant);
        return this;
    }

    @JsonIgnore
    public CaseData hearingOrderDocRespondentSol(Object hearingOrderDocRespondentSol) {
        setFieldValue("hearingOrderDocRespondentSol", hearingOrderDocRespondentSol);
        return this;
    }

    @JsonIgnore
    public CaseData hearingOrderDocRespondentSolTwo(Object hearingOrderDocRespondentSolTwo) {
        setFieldValue("hearingOrderDocRespondentSolTwo", hearingOrderDocRespondentSolTwo);
        return this;
    }

    @JsonIgnore
    public CaseData hearingOrderDocStaff(Object hearingOrderDocStaff) {
        setFieldValue("hearingOrderDocStaff", hearingOrderDocStaff);
        return this;
    }

    @JsonIgnore
    public CaseData hearingOrderDocument(Object hearingOrderDocument) {
        setFieldValue("hearingOrderDocument", hearingOrderDocument);
        return this;
    }

    @JsonIgnore
    public CaseData hearingSelection(Object hearingSelection) {
        setFieldValue("hearingSelection", hearingSelection);
        return this;
    }

    @JsonIgnore
    public CaseData hearingSupportRequirementsDJ(Object hearingSupportRequirementsDJ) {
        setFieldValue("hearingSupportRequirementsDJ", hearingSupportRequirementsDJ);
        return this;
    }

    @JsonIgnore
    public CaseData helpWithFeesMoreInformationClaimIssue(Object helpWithFeesMoreInformationClaimIssue) {
        setFieldValue("helpWithFeesMoreInformationClaimIssue", helpWithFeesMoreInformationClaimIssue);
        return this;
    }

    @JsonIgnore
    public CaseData helpWithFeesMoreInformationHearing(Object helpWithFeesMoreInformationHearing) {
        setFieldValue("helpWithFeesMoreInformationHearing", helpWithFeesMoreInformationHearing);
        return this;
    }

    @JsonIgnore
    public CaseData historicJudgment(Object historicJudgment) {
        setFieldValue("historicJudgment", historicJudgment);
        return this;
    }

    @JsonIgnore
    public CaseData hmcEaCourtLocation(Object hmcEaCourtLocation) {
        setFieldValue("hmcEaCourtLocation", hmcEaCourtLocation);
        return this;
    }

    @JsonIgnore
    public CaseData information(Object information) {
        setFieldValue("information", information);
        return this;
    }

    @JsonIgnore
    public CaseData interestClaimFrom(Object interestClaimFrom) {
        setFieldValue("interestClaimFrom", interestClaimFrom);
        return this;
    }

    @JsonIgnore
    public CaseData interestClaimUntil(Object interestClaimUntil) {
        setFieldValue("interestClaimUntil", interestClaimUntil);
        return this;
    }

    @JsonIgnore
    public CaseData interestFromSpecificDateDescription(Object interestFromSpecificDateDescription) {
        setFieldValue("interestFromSpecificDateDescription", interestFromSpecificDateDescription);
        return this;
    }

    @JsonIgnore
    public CaseData isClaimDeclarationAdded(Object isClaimDeclarationAdded) {
        setFieldValue("isClaimDeclarationAdded", isClaimDeclarationAdded);
        return this;
    }

    @JsonIgnore
    public CaseData isDiscontinuingAgainstBothDefendants(Object isDiscontinuingAgainstBothDefendants) {
        setFieldValue("isDiscontinuingAgainstBothDefendants", isDiscontinuingAgainstBothDefendants);
        return this;
    }

    @JsonIgnore
    public CaseData isFinalOrder(Object isFinalOrder) {
        setFieldValue("isFinalOrder", isFinalOrder);
        return this;
    }

    @JsonIgnore
    public CaseData isFlightDelayClaim(Object isFlightDelayClaim) {
        setFieldValue("isFlightDelayClaim", isFlightDelayClaim);
        return this;
    }

    @JsonIgnore
    public CaseData isGaRespondentTwoLip(Object isGaRespondentTwoLip) {
        setFieldValue("isGaRespondentTwoLip", isGaRespondentTwoLip);
        return this;
    }

    @JsonIgnore
    public CaseData isHumanRightsActIssues(Object isHumanRightsActIssues) {
        setFieldValue("isHumanRightsActIssues", isHumanRightsActIssues);
        return this;
    }

    @JsonIgnore
    public CaseData isMintiLipCase(Object isMintiLipCase) {
        setFieldValue("isMintiLipCase", isMintiLipCase);
        return this;
    }

    @JsonIgnore
    public CaseData isPermissionGranted(Object isPermissionGranted) {
        setFieldValue("isPermissionGranted", isPermissionGranted);
        return this;
    }

    @JsonIgnore
    public CaseData isReferToJudgeClaim(Object isReferToJudgeClaim) {
        setFieldValue("isReferToJudgeClaim", isReferToJudgeClaim);
        return this;
    }

    @JsonIgnore
    public CaseData isSdoR2NewScreen(Object isSdoR2NewScreen) {
        setFieldValue("isSdoR2NewScreen", isSdoR2NewScreen);
        return this;
    }

    @JsonIgnore
    public CaseData joAmountCostOrdered(Object joAmountCostOrdered) {
        setFieldValue("joAmountCostOrdered", joAmountCostOrdered);
        return this;
    }

    @JsonIgnore
    public CaseData joAmountOrdered(Object joAmountOrdered) {
        setFieldValue("joAmountOrdered", joAmountOrdered);
        return this;
    }

    @JsonIgnore
    public CaseData joCoscRpaStatus(Object joCoscRpaStatus) {
        setFieldValue("joCoscRpaStatus", joCoscRpaStatus);
        return this;
    }

    @JsonIgnore
    public CaseData joCosts(Object joCosts) {
        setFieldValue("joCosts", joCosts);
        return this;
    }

    @JsonIgnore
    public CaseData joDJCreatedDate(Object joDJCreatedDate) {
        setFieldValue("joDJCreatedDate", joDJCreatedDate);
        return this;
    }

    @JsonIgnore
    public CaseData joDefendantMarkedPaidInFullIssueDate(Object joDefendantMarkedPaidInFullIssueDate) {
        setFieldValue("joDefendantMarkedPaidInFullIssueDate", joDefendantMarkedPaidInFullIssueDate);
        return this;
    }

    @JsonIgnore
    public CaseData joDefendantName1(Object joDefendantName1) {
        setFieldValue("joDefendantName1", joDefendantName1);
        return this;
    }

    @JsonIgnore
    public CaseData joDefendantName2(Object joDefendantName2) {
        setFieldValue("joDefendantName2", joDefendantName2);
        return this;
    }

    @JsonIgnore
    public CaseData joFullyPaymentMadeDate(Object joFullyPaymentMadeDate) {
        setFieldValue("joFullyPaymentMadeDate", joFullyPaymentMadeDate);
        return this;
    }

    @JsonIgnore
    public CaseData joInstalmentDetails(Object joInstalmentDetails) {
        setFieldValue("joInstalmentDetails", joInstalmentDetails);
        return this;
    }

    @JsonIgnore
    public CaseData joIsDisplayInJudgmentTab(Object joIsDisplayInJudgmentTab) {
        setFieldValue("joIsDisplayInJudgmentTab", joIsDisplayInJudgmentTab);
        return this;
    }

    @JsonIgnore
    public CaseData joIsLiveJudgmentExists(Object joIsLiveJudgmentExists) {
        setFieldValue("joIsLiveJudgmentExists", joIsLiveJudgmentExists);
        return this;
    }

    @JsonIgnore
    public CaseData joIsRegisteredWithRTL(Object joIsRegisteredWithRTL) {
        setFieldValue("joIsRegisteredWithRTL", joIsRegisteredWithRTL);
        return this;
    }

    @JsonIgnore
    public CaseData joIssueDate(Object joIssueDate) {
        setFieldValue("joIssueDate", joIssueDate);
        return this;
    }

    @JsonIgnore
    public CaseData joIssuedDate(Object joIssuedDate) {
        setFieldValue("joIssuedDate", joIssuedDate);
        return this;
    }

    @JsonIgnore
    public CaseData joJudgementByAdmissionIssueDate(Object joJudgementByAdmissionIssueDate) {
        setFieldValue("joJudgementByAdmissionIssueDate", joJudgementByAdmissionIssueDate);
        return this;
    }

    @JsonIgnore
    public CaseData joJudgmentPaidInFull(Object joJudgmentPaidInFull) {
        setFieldValue("joJudgmentPaidInFull", joJudgmentPaidInFull);
        return this;
    }

    @JsonIgnore
    public CaseData joJudgmentRecordReason(Object joJudgmentRecordReason) {
        setFieldValue("joJudgmentRecordReason", joJudgmentRecordReason);
        return this;
    }

    @JsonIgnore
    public CaseData joMarkedPaidInFullIssueDate(Object joMarkedPaidInFullIssueDate) {
        setFieldValue("joMarkedPaidInFullIssueDate", joMarkedPaidInFullIssueDate);
        return this;
    }

    @JsonIgnore
    public CaseData joOrderMadeDate(Object joOrderMadeDate) {
        setFieldValue("joOrderMadeDate", joOrderMadeDate);
        return this;
    }

    @JsonIgnore
    public CaseData joOrderedAmount(Object joOrderedAmount) {
        setFieldValue("joOrderedAmount", joOrderedAmount);
        return this;
    }

    @JsonIgnore
    public CaseData joPaymentPlan(Object joPaymentPlan) {
        setFieldValue("joPaymentPlan", joPaymentPlan);
        return this;
    }

    @JsonIgnore
    public CaseData joPaymentPlanSelected(Object joPaymentPlanSelected) {
        setFieldValue("joPaymentPlanSelected", joPaymentPlanSelected);
        return this;
    }

    @JsonIgnore
    public CaseData joRepaymentAmount(Object joRepaymentAmount) {
        setFieldValue("joRepaymentAmount", joRepaymentAmount);
        return this;
    }

    @JsonIgnore
    public CaseData joRepaymentFrequency(Object joRepaymentFrequency) {
        setFieldValue("joRepaymentFrequency", joRepaymentFrequency);
        return this;
    }

    @JsonIgnore
    public CaseData joRepaymentStartDate(Object joRepaymentStartDate) {
        setFieldValue("joRepaymentStartDate", joRepaymentStartDate);
        return this;
    }

    @JsonIgnore
    public CaseData joRepaymentSummaryObject(Object joRepaymentSummaryObject) {
        setFieldValue("joRepaymentSummaryObject", joRepaymentSummaryObject);
        return this;
    }

    @JsonIgnore
    public CaseData joSetAsideApplicationDate(Object joSetAsideApplicationDate) {
        setFieldValue("joSetAsideApplicationDate", joSetAsideApplicationDate);
        return this;
    }

    @JsonIgnore
    public CaseData joSetAsideCreatedDate(Object joSetAsideCreatedDate) {
        setFieldValue("joSetAsideCreatedDate", joSetAsideCreatedDate);
        return this;
    }

    @JsonIgnore
    public CaseData joSetAsideDefenceReceivedDate(Object joSetAsideDefenceReceivedDate) {
        setFieldValue("joSetAsideDefenceReceivedDate", joSetAsideDefenceReceivedDate);
        return this;
    }

    @JsonIgnore
    public CaseData joSetAsideJudgmentErrorText(Object joSetAsideJudgmentErrorText) {
        setFieldValue("joSetAsideJudgmentErrorText", joSetAsideJudgmentErrorText);
        return this;
    }

    @JsonIgnore
    public CaseData joSetAsideOrderDate(Object joSetAsideOrderDate) {
        setFieldValue("joSetAsideOrderDate", joSetAsideOrderDate);
        return this;
    }

    @JsonIgnore
    public CaseData joSetAsideOrderType(Object joSetAsideOrderType) {
        setFieldValue("joSetAsideOrderType", joSetAsideOrderType);
        return this;
    }

    @JsonIgnore
    public CaseData joSetAsideReason(Object joSetAsideReason) {
        setFieldValue("joSetAsideReason", joSetAsideReason);
        return this;
    }

    @JsonIgnore
    public CaseData joShowRegisteredWithRTLOption(Object joShowRegisteredWithRTLOption) {
        setFieldValue("joShowRegisteredWithRTLOption", joShowRegisteredWithRTLOption);
        return this;
    }

    @JsonIgnore
    public CaseData joState(Object joState) {
        setFieldValue("joState", joState);
        return this;
    }

    @JsonIgnore
    public CaseData joTotalAmount(Object joTotalAmount) {
        setFieldValue("joTotalAmount", joTotalAmount);
        return this;
    }

    @JsonIgnore
    public CaseData lastMessageAllocatedTrack(Object lastMessageAllocatedTrack) {
        setFieldValue("lastMessageAllocatedTrack", lastMessageAllocatedTrack);
        return this;
    }

    @JsonIgnore
    public CaseData lastMessageJudgeLabel(Object lastMessageJudgeLabel) {
        setFieldValue("lastMessageJudgeLabel", lastMessageJudgeLabel);
        return this;
    }

    @JsonIgnore
    public CaseData lengthList(Object lengthList) {
        setFieldValue("lengthList", lengthList);
        return this;
    }

    @JsonIgnore
    public CaseData listingOrRelisting(Object listingOrRelisting) {
        setFieldValue("listingOrRelisting", listingOrRelisting);
        return this;
    }

    @JsonIgnore
    public CaseData litigantFriendSelection(Object litigantFriendSelection) {
        setFieldValue("litigantFriendSelection", litigantFriendSelection);
        return this;
    }

    @JsonIgnore
    public CaseData manageDocuments(Object manageDocuments) {
        setFieldValue("manageDocuments", manageDocuments);
        return this;
    }

    @JsonIgnore
    public CaseData manageStayOption(Object manageStayOption) {
        setFieldValue("manageStayOption", manageStayOption);
        return this;
    }

    @JsonIgnore
    public CaseData manageStayUpdateRequestDate(Object manageStayUpdateRequestDate) {
        setFieldValue("manageStayUpdateRequestDate", manageStayUpdateRequestDate);
        return this;
    }

    @JsonIgnore
    public CaseData markPaidConsent(Object markPaidConsent) {
        setFieldValue("markPaidConsent", markPaidConsent);
        return this;
    }

    @JsonIgnore
    public CaseData markPaidForAllClaimants(Object markPaidForAllClaimants) {
        setFieldValue("markPaidForAllClaimants", markPaidForAllClaimants);
        return this;
    }

    @JsonIgnore
    public CaseData mediationFileSentToMmt(Object mediationFileSentToMmt) {
        setFieldValue("mediationFileSentToMmt", mediationFileSentToMmt);
        return this;
    }

    @JsonIgnore
    public CaseData messageHistory(Object messageHistory) {
        setFieldValue("messageHistory", messageHistory);
        return this;
    }

    @JsonIgnore
    public CaseData messageReplyMetadata(Object messageReplyMetadata) {
        setFieldValue("messageReplyMetadata", messageReplyMetadata);
        return this;
    }

    @JsonIgnore
    public CaseData messages(Object messages) {
        setFieldValue("messages", messages);
        return this;
    }

    @JsonIgnore
    public CaseData messagesToReplyTo(Object messagesToReplyTo) {
        setFieldValue("messagesToReplyTo", messagesToReplyTo);
        return this;
    }

    @JsonIgnore
    public CaseData migrationId(Object migrationId) {
        setFieldValue("migrationId", migrationId);
        return this;
    }

    @JsonIgnore
    public CaseData multiPartyResponseTypeFlags(Object multiPartyResponseTypeFlags) {
        setFieldValue("multiPartyResponseTypeFlags", multiPartyResponseTypeFlags);
        return this;
    }

    @JsonIgnore
    public CaseData neitherCompanyNorOrganisation(Object neitherCompanyNorOrganisation) {
        setFieldValue("neitherCompanyNorOrganisation", neitherCompanyNorOrganisation);
        return this;
    }

    @JsonIgnore
    public CaseData nextDeadline(Object nextDeadline) {
        setFieldValue("nextDeadline", nextDeadline);
        return this;
    }

    @JsonIgnore
    public CaseData nextHearingDetails(Object nextHearingDetails) {
        setFieldValue("nextHearingDetails", nextHearingDetails);
        return this;
    }

    @JsonIgnore
    public CaseData notSuitableSdoOptions(Object notSuitableSdoOptions) {
        setFieldValue("notSuitableSdoOptions", notSuitableSdoOptions);
        return this;
    }

    @JsonIgnore
    public CaseData noteAdditionDateTime(Object noteAdditionDateTime) {
        setFieldValue("noteAdditionDateTime", noteAdditionDateTime);
        return this;
    }

    @JsonIgnore
    public CaseData notificationSummary(Object notificationSummary) {
        setFieldValue("notificationSummary", notificationSummary);
        return this;
    }

    @JsonIgnore
    public CaseData obligationData(Object obligationData) {
        setFieldValue("obligationData", obligationData);
        return this;
    }

    @JsonIgnore
    public CaseData obligationDatePresent(Object obligationDatePresent) {
        setFieldValue("obligationDatePresent", obligationDatePresent);
        return this;
    }

    @JsonIgnore
    public CaseData orderAfterHearingDate(Object orderAfterHearingDate) {
        setFieldValue("orderAfterHearingDate", orderAfterHearingDate);
        return this;
    }

    @JsonIgnore
    public CaseData orderMadeOnDetailsOrderCourt(Object orderMadeOnDetailsOrderCourt) {
        setFieldValue("orderMadeOnDetailsOrderCourt", orderMadeOnDetailsOrderCourt);
        return this;
    }

    @JsonIgnore
    public CaseData orderMadeOnDetailsOrderWithoutNotice(Object orderMadeOnDetailsOrderWithoutNotice) {
        setFieldValue("orderMadeOnDetailsOrderWithoutNotice", orderMadeOnDetailsOrderWithoutNotice);
        return this;
    }

    @JsonIgnore
    public CaseData orderOnCourtsList(Object orderOnCourtsList) {
        setFieldValue("orderOnCourtsList", orderOnCourtsList);
        return this;
    }

    @JsonIgnore
    public CaseData orderRequestedForReviewClaimant(Object orderRequestedForReviewClaimant) {
        setFieldValue("orderRequestedForReviewClaimant", orderRequestedForReviewClaimant);
        return this;
    }

    @JsonIgnore
    public CaseData orderRequestedForReviewDefendant(Object orderRequestedForReviewDefendant) {
        setFieldValue("orderRequestedForReviewDefendant", orderRequestedForReviewDefendant);
        return this;
    }

    @JsonIgnore
    public CaseData orderType(Object orderType) {
        setFieldValue("orderType", orderType);
        return this;
    }

    @JsonIgnore
    public CaseData orderWithoutNotice(Object orderWithoutNotice) {
        setFieldValue("orderWithoutNotice", orderWithoutNotice);
        return this;
    }

    @JsonIgnore
    public CaseData otherDetails(Object otherDetails) {
        setFieldValue("otherDetails", otherDetails);
        return this;
    }

    @JsonIgnore
    public CaseData otherRemedyFee(Object otherRemedyFee) {
        setFieldValue("otherRemedyFee", otherRemedyFee);
        return this;
    }

    @JsonIgnore
    public CaseData partAdmit1v1Defendant(Object partAdmit1v1Defendant) {
        setFieldValue("partAdmit1v1Defendant", partAdmit1v1Defendant);
        return this;
    }

    @JsonIgnore
    public CaseData partAdmitPaidValuePounds(Object partAdmitPaidValuePounds) {
        setFieldValue("partAdmitPaidValuePounds", partAdmitPaidValuePounds);
        return this;
    }

    @JsonIgnore
    public CaseData partAdmittedByEitherRespondents(Object partAdmittedByEitherRespondents) {
        setFieldValue("partAdmittedByEitherRespondents", partAdmittedByEitherRespondents);
        return this;
    }

    @JsonIgnore
    public CaseData partDiscontinuanceDetails(Object partDiscontinuanceDetails) {
        setFieldValue("partDiscontinuanceDetails", partDiscontinuanceDetails);
        return this;
    }

    @JsonIgnore
    public CaseData partialPayment(Object partialPayment) {
        setFieldValue("partialPayment", partialPayment);
        return this;
    }

    @JsonIgnore
    public CaseData paymentDetails(Object paymentDetails) {
        setFieldValue("paymentDetails", paymentDetails);
        return this;
    }

    @JsonIgnore
    public CaseData paymentReference(Object paymentReference) {
        setFieldValue("paymentReference", paymentReference);
        return this;
    }

    @JsonIgnore
    public CaseData paymentSetDate(Object paymentSetDate) {
        setFieldValue("paymentSetDate", paymentSetDate);
        return this;
    }

    @JsonIgnore
    public CaseData paymentSuccessfulDate(Object paymentSuccessfulDate) {
        setFieldValue("paymentSuccessfulDate", paymentSuccessfulDate);
        return this;
    }

    @JsonIgnore
    public CaseData paymentTypePBA(Object paymentTypePBA) {
        setFieldValue("paymentTypePBA", paymentTypePBA);
        return this;
    }

    @JsonIgnore
    public CaseData paymentTypePBASpec(Object paymentTypePBASpec) {
        setFieldValue("paymentTypePBASpec", paymentTypePBASpec);
        return this;
    }

    @JsonIgnore
    public CaseData paymentTypeSelection(Object paymentTypeSelection) {
        setFieldValue("paymentTypeSelection", paymentTypeSelection);
        return this;
    }

    @JsonIgnore
    public CaseData pcqId(Object pcqId) {
        setFieldValue("pcqId", pcqId);
        return this;
    }

    @JsonIgnore
    public CaseData permissionGrantedComplex(Object permissionGrantedComplex) {
        setFieldValue("permissionGrantedComplex", permissionGrantedComplex);
        return this;
    }

    @JsonIgnore
    public CaseData permissionGrantedDateCopy(Object permissionGrantedDateCopy) {
        setFieldValue("permissionGrantedDateCopy", permissionGrantedDateCopy);
        return this;
    }

    @JsonIgnore
    public CaseData permissionGrantedJudgeCopy(Object permissionGrantedJudgeCopy) {
        setFieldValue("permissionGrantedJudgeCopy", permissionGrantedJudgeCopy);
        return this;
    }

    @JsonIgnore
    public CaseData personalInjuryType(Object personalInjuryType) {
        setFieldValue("personalInjuryType", personalInjuryType);
        return this;
    }

    @JsonIgnore
    public CaseData personalInjuryTypeOther(Object personalInjuryTypeOther) {
        setFieldValue("personalInjuryTypeOther", personalInjuryTypeOther);
        return this;
    }

    @JsonIgnore
    public CaseData preStayState(Object preStayState) {
        setFieldValue("preStayState", preStayState);
        return this;
    }

    @JsonIgnore
    public CaseData preTranslationDocumentType(Object preTranslationDocumentType) {
        setFieldValue("preTranslationDocumentType", preTranslationDocumentType);
        return this;
    }

    @JsonIgnore
    public CaseData preTranslationGaDocsApplicant(Object preTranslationGaDocsApplicant) {
        setFieldValue("preTranslationGaDocsApplicant", preTranslationGaDocsApplicant);
        return this;
    }

    @JsonIgnore
    public CaseData preTranslationGaDocsRespondent(Object preTranslationGaDocsRespondent) {
        setFieldValue("preTranslationGaDocsRespondent", preTranslationGaDocsRespondent);
        return this;
    }

    @JsonIgnore
    public CaseData previewCourtOfficerOrder(Object previewCourtOfficerOrder) {
        setFieldValue("previewCourtOfficerOrder", previewCourtOfficerOrder);
        return this;
    }

    @JsonIgnore
    public CaseData publicFundingCostsProtection(Object publicFundingCostsProtection) {
        setFieldValue("publicFundingCostsProtection", publicFundingCostsProtection);
        return this;
    }

    @JsonIgnore
    public CaseData qmRespondentSolicitor1Queries(Object qmRespondentSolicitor1Queries) {
        setFieldValue("qmRespondentSolicitor1Queries", qmRespondentSolicitor1Queries);
        return this;
    }

    @JsonIgnore
    public CaseData qmRespondentSolicitor2Queries(Object qmRespondentSolicitor2Queries) {
        setFieldValue("qmRespondentSolicitor2Queries", qmRespondentSolicitor2Queries);
        return this;
    }

    @JsonIgnore
    public CaseData queries(Object queries) {
        setFieldValue("queries", queries);
        return this;
    }

    @JsonIgnore
    public CaseData reasonForReconsiderationRespondent2(Object reasonForReconsiderationRespondent2) {
        setFieldValue("reasonForReconsiderationRespondent2", reasonForReconsiderationRespondent2);
        return this;
    }

    @JsonIgnore
    public CaseData reasonForTransfer(Object reasonForTransfer) {
        setFieldValue("reasonForTransfer", reasonForTransfer);
        return this;
    }

    @JsonIgnore
    public CaseData registrationTypeRespondentOne(Object registrationTypeRespondentOne) {
        setFieldValue("registrationTypeRespondentOne", registrationTypeRespondentOne);
        return this;
    }

    @JsonIgnore
    public CaseData registrationTypeRespondentTwo(Object registrationTypeRespondentTwo) {
        setFieldValue("registrationTypeRespondentTwo", registrationTypeRespondentTwo);
        return this;
    }

    @JsonIgnore
    public CaseData repaymentDate(Object repaymentDate) {
        setFieldValue("repaymentDate", repaymentDate);
        return this;
    }

    @JsonIgnore
    public CaseData repaymentDue(Object repaymentDue) {
        setFieldValue("repaymentDue", repaymentDue);
        return this;
    }

    @JsonIgnore
    public CaseData repaymentFrequency(Object repaymentFrequency) {
        setFieldValue("repaymentFrequency", repaymentFrequency);
        return this;
    }

    @JsonIgnore
    public CaseData repaymentSuggestion(Object repaymentSuggestion) {
        setFieldValue("repaymentSuggestion", repaymentSuggestion);
        return this;
    }

    @JsonIgnore
    public CaseData requestForInfoDocClaimant(Object requestForInfoDocClaimant) {
        setFieldValue("requestForInfoDocClaimant", requestForInfoDocClaimant);
        return this;
    }

    @JsonIgnore
    public CaseData requestForInfoDocRespondentSol(Object requestForInfoDocRespondentSol) {
        setFieldValue("requestForInfoDocRespondentSol", requestForInfoDocRespondentSol);
        return this;
    }

    @JsonIgnore
    public CaseData requestForInfoDocRespondentSolTwo(Object requestForInfoDocRespondentSolTwo) {
        setFieldValue("requestForInfoDocRespondentSolTwo", requestForInfoDocRespondentSolTwo);
        return this;
    }

    @JsonIgnore
    public CaseData requestForInfoDocStaff(Object requestForInfoDocStaff) {
        setFieldValue("requestForInfoDocStaff", requestForInfoDocStaff);
        return this;
    }

    @JsonIgnore
    public CaseData requestForInformationDocument(Object requestForInformationDocument) {
        setFieldValue("requestForInformationDocument", requestForInformationDocument);
        return this;
    }

    @JsonIgnore
    public CaseData requestForReconsiderationDeadline(Object requestForReconsiderationDeadline) {
        setFieldValue("requestForReconsiderationDeadline", requestForReconsiderationDeadline);
        return this;
    }

    @JsonIgnore
    public CaseData requestForReconsiderationDeadlineChecked(Object requestForReconsiderationDeadlineChecked) {
        setFieldValue("requestForReconsiderationDeadlineChecked", requestForReconsiderationDeadlineChecked);
        return this;
    }

    @JsonIgnore
    public CaseData requestForReconsiderationDocument(Object requestForReconsiderationDocument) {
        setFieldValue("requestForReconsiderationDocument", requestForReconsiderationDocument);
        return this;
    }

    @JsonIgnore
    public CaseData requestForReconsiderationDocumentRes(Object requestForReconsiderationDocumentRes) {
        setFieldValue("requestForReconsiderationDocumentRes", requestForReconsiderationDocumentRes);
        return this;
    }

    @JsonIgnore
    public CaseData requestHearingNoticeDynamic(Object requestHearingNoticeDynamic) {
        setFieldValue("requestHearingNoticeDynamic", requestHearingNoticeDynamic);
        return this;
    }

    @JsonIgnore
    public CaseData requestedCourtForTabDetailsApp(Object requestedCourtForTabDetailsApp) {
        setFieldValue("requestedCourtForTabDetailsApp", requestedCourtForTabDetailsApp);
        return this;
    }

    @JsonIgnore
    public CaseData requestedCourtForTabDetailsRes1(Object requestedCourtForTabDetailsRes1) {
        setFieldValue("requestedCourtForTabDetailsRes1", requestedCourtForTabDetailsRes1);
        return this;
    }

    @JsonIgnore
    public CaseData requestedCourtForTabDetailsRes2(Object requestedCourtForTabDetailsRes2) {
        setFieldValue("requestedCourtForTabDetailsRes2", requestedCourtForTabDetailsRes2);
        return this;
    }

    @JsonIgnore
    public CaseData res1MediationDocumentsReferred(Object res1MediationDocumentsReferred) {
        setFieldValue("res1MediationDocumentsReferred", res1MediationDocumentsReferred);
        return this;
    }

    @JsonIgnore
    public CaseData res1MediationNonAttendanceDocs(Object res1MediationNonAttendanceDocs) {
        setFieldValue("res1MediationNonAttendanceDocs", res1MediationNonAttendanceDocs);
        return this;
    }

    @JsonIgnore
    public CaseData res2MediationDocumentsReferred(Object res2MediationDocumentsReferred) {
        setFieldValue("res2MediationDocumentsReferred", res2MediationDocumentsReferred);
        return this;
    }

    @JsonIgnore
    public CaseData res2MediationNonAttendanceDocs(Object res2MediationNonAttendanceDocs) {
        setFieldValue("res2MediationNonAttendanceDocs", res2MediationNonAttendanceDocs);
        return this;
    }

    @JsonIgnore
    public CaseData resp1MediationAvailability(Object resp1MediationAvailability) {
        setFieldValue("resp1MediationAvailability", resp1MediationAvailability);
        return this;
    }

    @JsonIgnore
    public CaseData resp2MediationAvailability(Object resp2MediationAvailability) {
        setFieldValue("resp2MediationAvailability", resp2MediationAvailability);
        return this;
    }

    @JsonIgnore
    public CaseData respondForImmediateOption(Object respondForImmediateOption) {
        setFieldValue("respondForImmediateOption", respondForImmediateOption);
        return this;
    }

    @JsonIgnore
    public CaseData respondToAdmittedClaim(Object respondToAdmittedClaim) {
        setFieldValue("respondToAdmittedClaim", respondToAdmittedClaim);
        return this;
    }

    @JsonIgnore
    public CaseData respondToAdmittedClaim2(Object respondToAdmittedClaim2) {
        setFieldValue("respondToAdmittedClaim2", respondToAdmittedClaim2);
        return this;
    }

    @JsonIgnore
    public CaseData respondToAdmittedClaimOwingAmount(Object respondToAdmittedClaimOwingAmount) {
        setFieldValue("respondToAdmittedClaimOwingAmount", respondToAdmittedClaimOwingAmount);
        return this;
    }

    @JsonIgnore
    public CaseData respondToAdmittedClaimOwingAmount2(Object respondToAdmittedClaimOwingAmount2) {
        setFieldValue("respondToAdmittedClaimOwingAmount2", respondToAdmittedClaimOwingAmount2);
        return this;
    }

    @JsonIgnore
    public CaseData respondToAdmittedClaimOwingAmountPounds(Object respondToAdmittedClaimOwingAmountPounds) {
        setFieldValue("respondToAdmittedClaimOwingAmountPounds", respondToAdmittedClaimOwingAmountPounds);
        return this;
    }

    @JsonIgnore
    public CaseData respondToAdmittedClaimOwingAmountPounds2(Object respondToAdmittedClaimOwingAmountPounds2) {
        setFieldValue("respondToAdmittedClaimOwingAmountPounds2", respondToAdmittedClaimOwingAmountPounds2);
        return this;
    }

    @JsonIgnore
    public CaseData respondToClaim(Object respondToClaim) {
        setFieldValue("respondToClaim", respondToClaim);
        return this;
    }

    @JsonIgnore
    public CaseData respondToClaim2(Object respondToClaim2) {
        setFieldValue("respondToClaim2", respondToClaim2);
        return this;
    }

    @JsonIgnore
    public CaseData respondToClaimAdmitPartEmploymentTypeLRspec(Object respondToClaimAdmitPartEmploymentTypeLRspec) {
        setFieldValue("respondToClaimAdmitPartEmploymentTypeLRspec", respondToClaimAdmitPartEmploymentTypeLRspec);
        return this;
    }

    @JsonIgnore
    public CaseData respondToClaimAdmitPartEmploymentTypeLRspec2(Object respondToClaimAdmitPartEmploymentTypeLRspec2) {
        setFieldValue("respondToClaimAdmitPartEmploymentTypeLRspec2", respondToClaimAdmitPartEmploymentTypeLRspec2);
        return this;
    }

    @JsonIgnore
    public CaseData respondToClaimAdmitPartEmploymentTypeLRspecGeneric(Object respondToClaimAdmitPartEmploymentTypeLRspecGeneric) {
        setFieldValue("respondToClaimAdmitPartEmploymentTypeLRspecGeneric", respondToClaimAdmitPartEmploymentTypeLRspecGeneric);
        return this;
    }

    @JsonIgnore
    public CaseData respondToClaimAdmitPartLRspec2(Object respondToClaimAdmitPartLRspec2) {
        setFieldValue("respondToClaimAdmitPartLRspec2", respondToClaimAdmitPartLRspec2);
        return this;
    }

    @JsonIgnore
    public CaseData respondToClaimAdmitPartUnemployedLRspec(Object respondToClaimAdmitPartUnemployedLRspec) {
        setFieldValue("respondToClaimAdmitPartUnemployedLRspec", respondToClaimAdmitPartUnemployedLRspec);
        return this;
    }

    @JsonIgnore
    public CaseData respondToClaimAdmitPartUnemployedLRspec2(Object respondToClaimAdmitPartUnemployedLRspec2) {
        setFieldValue("respondToClaimAdmitPartUnemployedLRspec2", respondToClaimAdmitPartUnemployedLRspec2);
        return this;
    }

    @JsonIgnore
    public CaseData respondToClaimExperts(Object respondToClaimExperts) {
        setFieldValue("respondToClaimExperts", respondToClaimExperts);
        return this;
    }

    @JsonIgnore
    public CaseData respondent1AcknowledgeNotificationDate(Object respondent1AcknowledgeNotificationDate) {
        setFieldValue("respondent1AcknowledgeNotificationDate", respondent1AcknowledgeNotificationDate);
        return this;
    }

    @JsonIgnore
    public CaseData respondent1ClaimResponseDocument(Object respondent1ClaimResponseDocument) {
        setFieldValue("respondent1ClaimResponseDocument", respondent1ClaimResponseDocument);
        return this;
    }

    @JsonIgnore
    public CaseData respondent1ClaimResponseDocumentSpec(Object respondent1ClaimResponseDocumentSpec) {
        setFieldValue("respondent1ClaimResponseDocumentSpec", respondent1ClaimResponseDocumentSpec);
        return this;
    }

    @JsonIgnore
    public CaseData respondent1ClaimResponseIntentionType(Object respondent1ClaimResponseIntentionType) {
        setFieldValue("respondent1ClaimResponseIntentionType", respondent1ClaimResponseIntentionType);
        return this;
    }

    @JsonIgnore
    public CaseData respondent1ClaimResponseIntentionTypeApplicant2(Object respondent1ClaimResponseIntentionTypeApplicant2) {
        setFieldValue("respondent1ClaimResponseIntentionTypeApplicant2", respondent1ClaimResponseIntentionTypeApplicant2);
        return this;
    }

    @JsonIgnore
    public CaseData respondent1ClaimResponsePaymentAdmissionForSpec(Object respondent1ClaimResponsePaymentAdmissionForSpec) {
        setFieldValue("respondent1ClaimResponsePaymentAdmissionForSpec", respondent1ClaimResponsePaymentAdmissionForSpec);
        return this;
    }

    @JsonIgnore
    public CaseData respondent1ClaimResponseTestForSpec(Object respondent1ClaimResponseTestForSpec) {
        setFieldValue("respondent1ClaimResponseTestForSpec", respondent1ClaimResponseTestForSpec);
        return this;
    }

    @JsonIgnore
    public CaseData respondent1ClaimResponseTypeToApplicant2(Object respondent1ClaimResponseTypeToApplicant2) {
        setFieldValue("respondent1ClaimResponseTypeToApplicant2", respondent1ClaimResponseTypeToApplicant2);
        return this;
    }

    @JsonIgnore
    public CaseData respondent1Copy(Object respondent1Copy) {
        setFieldValue("respondent1Copy", respondent1Copy);
        return this;
    }

    @JsonIgnore
    public CaseData respondent1CourtOrderDetails(Object respondent1CourtOrderDetails) {
        setFieldValue("respondent1CourtOrderDetails", respondent1CourtOrderDetails);
        return this;
    }

    @JsonIgnore
    public CaseData respondent1CourtOrderPayment(Object respondent1CourtOrderPayment) {
        setFieldValue("respondent1CourtOrderPayment", respondent1CourtOrderPayment);
        return this;
    }

    @JsonIgnore
    public CaseData respondent1CourtOrderPaymentOption(Object respondent1CourtOrderPaymentOption) {
        setFieldValue("respondent1CourtOrderPaymentOption", respondent1CourtOrderPaymentOption);
        return this;
    }

    @JsonIgnore
    public CaseData respondent1DQWitnessesDetailsSpec(Object respondent1DQWitnessesDetailsSpec) {
        setFieldValue("respondent1DQWitnessesDetailsSpec", respondent1DQWitnessesDetailsSpec);
        return this;
    }

    @JsonIgnore
    public CaseData respondent1DQWitnessesRequiredSpec(Object respondent1DQWitnessesRequiredSpec) {
        setFieldValue("respondent1DQWitnessesRequiredSpec", respondent1DQWitnessesRequiredSpec);
        return this;
    }

    @JsonIgnore
    public CaseData respondent1DQWitnessesSmallClaim(Object respondent1DQWitnessesSmallClaim) {
        setFieldValue("respondent1DQWitnessesSmallClaim", respondent1DQWitnessesSmallClaim);
        return this;
    }

    @JsonIgnore
    public CaseData respondent1DocumentURL(Object respondent1DocumentURL) {
        setFieldValue("respondent1DocumentURL", respondent1DocumentURL);
        return this;
    }

    @JsonIgnore
    public CaseData respondent1GeneratedResponseDocument(Object respondent1GeneratedResponseDocument) {
        setFieldValue("respondent1GeneratedResponseDocument", respondent1GeneratedResponseDocument);
        return this;
    }

    @JsonIgnore
    public CaseData respondent1HearingOtherComments(Object respondent1HearingOtherComments) {
        setFieldValue("respondent1HearingOtherComments", respondent1HearingOtherComments);
        return this;
    }

    @JsonIgnore
    public CaseData respondent1LiPStatementOfTruth(Object respondent1LiPStatementOfTruth) {
        setFieldValue("respondent1LiPStatementOfTruth", respondent1LiPStatementOfTruth);
        return this;
    }

    @JsonIgnore
    public CaseData respondent1LitigationFriendCreatedDate(Object respondent1LitigationFriendCreatedDate) {
        setFieldValue("respondent1LitigationFriendCreatedDate", respondent1LitigationFriendCreatedDate);
        return this;
    }

    @JsonIgnore
    public CaseData respondent1LitigationFriendDate(Object respondent1LitigationFriendDate) {
        setFieldValue("respondent1LitigationFriendDate", respondent1LitigationFriendDate);
        return this;
    }

    @JsonIgnore
    public CaseData respondent1LoanCreditDetails(Object respondent1LoanCreditDetails) {
        setFieldValue("respondent1LoanCreditDetails", respondent1LoanCreditDetails);
        return this;
    }

    @JsonIgnore
    public CaseData respondent1LoanCreditOption(Object respondent1LoanCreditOption) {
        setFieldValue("respondent1LoanCreditOption", respondent1LoanCreditOption);
        return this;
    }

    @JsonIgnore
    public CaseData respondent1NoticeOfDiscontinueAllPartyTranslatedDoc(Object respondent1NoticeOfDiscontinueAllPartyTranslatedDoc) {
        setFieldValue("respondent1NoticeOfDiscontinueAllPartyTranslatedDoc", respondent1NoticeOfDiscontinueAllPartyTranslatedDoc);
        return this;
    }

    @JsonIgnore
    public CaseData respondent1NoticeOfDiscontinueAllPartyViewDoc(Object respondent1NoticeOfDiscontinueAllPartyViewDoc) {
        setFieldValue("respondent1NoticeOfDiscontinueAllPartyViewDoc", respondent1NoticeOfDiscontinueAllPartyViewDoc);
        return this;
    }

    @JsonIgnore
    public CaseData respondent1NoticeOfDiscontinueCWViewDoc(Object respondent1NoticeOfDiscontinueCWViewDoc) {
        setFieldValue("respondent1NoticeOfDiscontinueCWViewDoc", respondent1NoticeOfDiscontinueCWViewDoc);
        return this;
    }

    @JsonIgnore
    public CaseData respondent1OrgRegistered(Object respondent1OrgRegistered) {
        setFieldValue("respondent1OrgRegistered", respondent1OrgRegistered);
        return this;
    }

    @JsonIgnore
    public CaseData respondent1OriginalDqDoc(Object respondent1OriginalDqDoc) {
        setFieldValue("respondent1OriginalDqDoc", respondent1OriginalDqDoc);
        return this;
    }

    @JsonIgnore
    public CaseData respondent1PartnerAndDependent(Object respondent1PartnerAndDependent) {
        setFieldValue("respondent1PartnerAndDependent", respondent1PartnerAndDependent);
        return this;
    }

    @JsonIgnore
    public CaseData respondent1PaymentDateToStringSpec(Object respondent1PaymentDateToStringSpec) {
        setFieldValue("respondent1PaymentDateToStringSpec", respondent1PaymentDateToStringSpec);
        return this;
    }

    @JsonIgnore
    public CaseData respondent1PinToPostLRspec(Object respondent1PinToPostLRspec) {
        setFieldValue("respondent1PinToPostLRspec", respondent1PinToPostLRspec);
        return this;
    }

    @JsonIgnore
    public CaseData respondent1RevisedHearingRequirements(Object respondent1RevisedHearingRequirements) {
        setFieldValue("respondent1RevisedHearingRequirements", respondent1RevisedHearingRequirements);
        return this;
    }

    @JsonIgnore
    public CaseData respondent1SpecDefenceResponseDocument(Object respondent1SpecDefenceResponseDocument) {
        setFieldValue("respondent1SpecDefenceResponseDocument", respondent1SpecDefenceResponseDocument);
        return this;
    }

    @JsonIgnore
    public CaseData respondent1TimeExtensionDate(Object respondent1TimeExtensionDate) {
        setFieldValue("respondent1TimeExtensionDate", respondent1TimeExtensionDate);
        return this;
    }

    @JsonIgnore
    public CaseData respondent1UnavailableDatesForTab(Object respondent1UnavailableDatesForTab) {
        setFieldValue("respondent1UnavailableDatesForTab", respondent1UnavailableDatesForTab);
        return this;
    }

    @JsonIgnore
    public CaseData respondent2AcknowledgeNotificationDate(Object respondent2AcknowledgeNotificationDate) {
        setFieldValue("respondent2AcknowledgeNotificationDate", respondent2AcknowledgeNotificationDate);
        return this;
    }

    @JsonIgnore
    public CaseData respondent2ClaimResponseDocument(Object respondent2ClaimResponseDocument) {
        setFieldValue("respondent2ClaimResponseDocument", respondent2ClaimResponseDocument);
        return this;
    }

    @JsonIgnore
    public CaseData respondent2ClaimResponseDocumentSpec(Object respondent2ClaimResponseDocumentSpec) {
        setFieldValue("respondent2ClaimResponseDocumentSpec", respondent2ClaimResponseDocumentSpec);
        return this;
    }

    @JsonIgnore
    public CaseData respondent2ClaimResponseIntentionType(Object respondent2ClaimResponseIntentionType) {
        setFieldValue("respondent2ClaimResponseIntentionType", respondent2ClaimResponseIntentionType);
        return this;
    }

    @JsonIgnore
    public CaseData respondent2ClaimResponseTestForSpec(Object respondent2ClaimResponseTestForSpec) {
        setFieldValue("respondent2ClaimResponseTestForSpec", respondent2ClaimResponseTestForSpec);
        return this;
    }

    @JsonIgnore
    public CaseData respondent2ClaimResponseType(Object respondent2ClaimResponseType) {
        setFieldValue("respondent2ClaimResponseType", respondent2ClaimResponseType);
        return this;
    }

    @JsonIgnore
    public CaseData respondent2Copy(Object respondent2Copy) {
        setFieldValue("respondent2Copy", respondent2Copy);
        return this;
    }

    @JsonIgnore
    public CaseData respondent2CourtOrderDetails(Object respondent2CourtOrderDetails) {
        setFieldValue("respondent2CourtOrderDetails", respondent2CourtOrderDetails);
        return this;
    }

    @JsonIgnore
    public CaseData respondent2CourtOrderPayment(Object respondent2CourtOrderPayment) {
        setFieldValue("respondent2CourtOrderPayment", respondent2CourtOrderPayment);
        return this;
    }

    @JsonIgnore
    public CaseData respondent2CourtOrderPaymentOption(Object respondent2CourtOrderPaymentOption) {
        setFieldValue("respondent2CourtOrderPaymentOption", respondent2CourtOrderPaymentOption);
        return this;
    }

    @JsonIgnore
    public CaseData respondent2DQCarerAllowanceCredit(Object respondent2DQCarerAllowanceCredit) {
        setFieldValue("respondent2DQCarerAllowanceCredit", respondent2DQCarerAllowanceCredit);
        return this;
    }

    @JsonIgnore
    public CaseData respondent2DQCarerAllowanceCreditFullAdmission(Object respondent2DQCarerAllowanceCreditFullAdmission) {
        setFieldValue("respondent2DQCarerAllowanceCreditFullAdmission", respondent2DQCarerAllowanceCreditFullAdmission);
        return this;
    }

    @JsonIgnore
    public CaseData respondent2DQWitnessesSmallClaim(Object respondent2DQWitnessesSmallClaim) {
        setFieldValue("respondent2DQWitnessesSmallClaim", respondent2DQWitnessesSmallClaim);
        return this;
    }

    @JsonIgnore
    public CaseData respondent2DetailsForClaimDetailsTab(Object respondent2DetailsForClaimDetailsTab) {
        setFieldValue("respondent2DetailsForClaimDetailsTab", respondent2DetailsForClaimDetailsTab);
        return this;
    }

    @JsonIgnore
    public CaseData respondent2DocumentGeneration(Object respondent2DocumentGeneration) {
        setFieldValue("respondent2DocumentGeneration", respondent2DocumentGeneration);
        return this;
    }

    @JsonIgnore
    public CaseData respondent2DocumentURL(Object respondent2DocumentURL) {
        setFieldValue("respondent2DocumentURL", respondent2DocumentURL);
        return this;
    }

    @JsonIgnore
    public CaseData respondent2GeneratedResponseDocument(Object respondent2GeneratedResponseDocument) {
        setFieldValue("respondent2GeneratedResponseDocument", respondent2GeneratedResponseDocument);
        return this;
    }

    @JsonIgnore
    public CaseData respondent2HearingOtherComments(Object respondent2HearingOtherComments) {
        setFieldValue("respondent2HearingOtherComments", respondent2HearingOtherComments);
        return this;
    }

    @JsonIgnore
    public CaseData respondent2LitigationFriendCreatedDate(Object respondent2LitigationFriendCreatedDate) {
        setFieldValue("respondent2LitigationFriendCreatedDate", respondent2LitigationFriendCreatedDate);
        return this;
    }

    @JsonIgnore
    public CaseData respondent2LitigationFriendDate(Object respondent2LitigationFriendDate) {
        setFieldValue("respondent2LitigationFriendDate", respondent2LitigationFriendDate);
        return this;
    }

    @JsonIgnore
    public CaseData respondent2LoanCreditDetails(Object respondent2LoanCreditDetails) {
        setFieldValue("respondent2LoanCreditDetails", respondent2LoanCreditDetails);
        return this;
    }

    @JsonIgnore
    public CaseData respondent2LoanCreditOption(Object respondent2LoanCreditOption) {
        setFieldValue("respondent2LoanCreditOption", respondent2LoanCreditOption);
        return this;
    }

    @JsonIgnore
    public CaseData respondent2NoticeOfDiscontinueAllPartyViewDoc(Object respondent2NoticeOfDiscontinueAllPartyViewDoc) {
        setFieldValue("respondent2NoticeOfDiscontinueAllPartyViewDoc", respondent2NoticeOfDiscontinueAllPartyViewDoc);
        return this;
    }

    @JsonIgnore
    public CaseData respondent2NoticeOfDiscontinueCWViewDoc(Object respondent2NoticeOfDiscontinueCWViewDoc) {
        setFieldValue("respondent2NoticeOfDiscontinueCWViewDoc", respondent2NoticeOfDiscontinueCWViewDoc);
        return this;
    }

    @JsonIgnore
    public CaseData respondent2OrgRegistered(Object respondent2OrgRegistered) {
        setFieldValue("respondent2OrgRegistered", respondent2OrgRegistered);
        return this;
    }

    @JsonIgnore
    public CaseData respondent2PartnerAndDependent(Object respondent2PartnerAndDependent) {
        setFieldValue("respondent2PartnerAndDependent", respondent2PartnerAndDependent);
        return this;
    }

    @JsonIgnore
    public CaseData respondent2RepaymentPlan(Object respondent2RepaymentPlan) {
        setFieldValue("respondent2RepaymentPlan", respondent2RepaymentPlan);
        return this;
    }

    @JsonIgnore
    public CaseData respondent2ResponseDeadline(Object respondent2ResponseDeadline) {
        setFieldValue("respondent2ResponseDeadline", respondent2ResponseDeadline);
        return this;
    }

    @JsonIgnore
    public CaseData respondent2RevisedHearingRequirements(Object respondent2RevisedHearingRequirements) {
        setFieldValue("respondent2RevisedHearingRequirements", respondent2RevisedHearingRequirements);
        return this;
    }

    @JsonIgnore
    public CaseData respondent2SpecDefenceResponseDocument(Object respondent2SpecDefenceResponseDocument) {
        setFieldValue("respondent2SpecDefenceResponseDocument", respondent2SpecDefenceResponseDocument);
        return this;
    }

    @JsonIgnore
    public CaseData respondent2TimeExtensionDate(Object respondent2TimeExtensionDate) {
        setFieldValue("respondent2TimeExtensionDate", respondent2TimeExtensionDate);
        return this;
    }

    @JsonIgnore
    public CaseData respondent2UnavailableDatesForTab(Object respondent2UnavailableDatesForTab) {
        setFieldValue("respondent2UnavailableDatesForTab", respondent2UnavailableDatesForTab);
        return this;
    }

    @JsonIgnore
    public CaseData respondentClaimResponseTypeForSpecGeneric(Object respondentClaimResponseTypeForSpecGeneric) {
        setFieldValue("respondentClaimResponseTypeForSpecGeneric", respondentClaimResponseTypeForSpecGeneric);
        return this;
    }

    @JsonIgnore
    public CaseData respondentDocsUploadedAfterBundle(Object respondentDocsUploadedAfterBundle) {
        setFieldValue("respondentDocsUploadedAfterBundle", respondentDocsUploadedAfterBundle);
        return this;
    }

    @JsonIgnore
    public CaseData respondentResponseIsSame(Object respondentResponseIsSame) {
        setFieldValue("respondentResponseIsSame", respondentResponseIsSame);
        return this;
    }

    @JsonIgnore
    public CaseData respondentResponsePcqId(Object respondentResponsePcqId) {
        setFieldValue("respondentResponsePcqId", respondentResponsePcqId);
        return this;
    }

    @JsonIgnore
    public CaseData respondentSharedClaimResponseDocument(Object respondentSharedClaimResponseDocument) {
        setFieldValue("respondentSharedClaimResponseDocument", respondentSharedClaimResponseDocument);
        return this;
    }

    @JsonIgnore
    public CaseData respondentSolGaAppDetails(Object respondentSolGaAppDetails) {
        setFieldValue("respondentSolGaAppDetails", respondentSolGaAppDetails);
        return this;
    }

    @JsonIgnore
    public CaseData respondentSolTwoGaAppDetails(Object respondentSolTwoGaAppDetails) {
        setFieldValue("respondentSolTwoGaAppDetails", respondentSolTwoGaAppDetails);
        return this;
    }

    @JsonIgnore
    public CaseData respondentSolicitor1AgreedDeadlineExtension(Object respondentSolicitor1AgreedDeadlineExtension) {
        setFieldValue("respondentSolicitor1AgreedDeadlineExtension", respondentSolicitor1AgreedDeadlineExtension);
        return this;
    }

    @JsonIgnore
    public CaseData respondentSolicitor1OrganisationDetails(Object respondentSolicitor1OrganisationDetails) {
        setFieldValue("respondentSolicitor1OrganisationDetails", respondentSolicitor1OrganisationDetails);
        return this;
    }

    @JsonIgnore
    public CaseData respondentSolicitor1ServiceAddress(Object respondentSolicitor1ServiceAddress) {
        setFieldValue("respondentSolicitor1ServiceAddress", respondentSolicitor1ServiceAddress);
        return this;
    }

    @JsonIgnore
    public CaseData respondentSolicitor1ServiceAddressRequired(Object respondentSolicitor1ServiceAddressRequired) {
        setFieldValue("respondentSolicitor1ServiceAddressRequired", respondentSolicitor1ServiceAddressRequired);
        return this;
    }

    @JsonIgnore
    public CaseData respondentSolicitor2AgreedDeadlineExtension(Object respondentSolicitor2AgreedDeadlineExtension) {
        setFieldValue("respondentSolicitor2AgreedDeadlineExtension", respondentSolicitor2AgreedDeadlineExtension);
        return this;
    }

    @JsonIgnore
    public CaseData respondentSolicitor2OrganisationDetails(Object respondentSolicitor2OrganisationDetails) {
        setFieldValue("respondentSolicitor2OrganisationDetails", respondentSolicitor2OrganisationDetails);
        return this;
    }

    @JsonIgnore
    public CaseData respondentSolicitor2ServiceAddress(Object respondentSolicitor2ServiceAddress) {
        setFieldValue("respondentSolicitor2ServiceAddress", respondentSolicitor2ServiceAddress);
        return this;
    }

    @JsonIgnore
    public CaseData respondentSolicitor2ServiceAddressRequired(Object respondentSolicitor2ServiceAddressRequired) {
        setFieldValue("respondentSolicitor2ServiceAddressRequired", respondentSolicitor2ServiceAddressRequired);
        return this;
    }

    @JsonIgnore
    public CaseData respondentSolicitorDetails(Object respondentSolicitorDetails) {
        setFieldValue("respondentSolicitorDetails", respondentSolicitorDetails);
        return this;
    }

    @JsonIgnore
    public CaseData responseClaimAdmitPartEmployer(Object responseClaimAdmitPartEmployer) {
        setFieldValue("responseClaimAdmitPartEmployer", responseClaimAdmitPartEmployer);
        return this;
    }

    @JsonIgnore
    public CaseData responseClaimAdmitPartEmployer2(Object responseClaimAdmitPartEmployer2) {
        setFieldValue("responseClaimAdmitPartEmployer2", responseClaimAdmitPartEmployer2);
        return this;
    }

    @JsonIgnore
    public CaseData responseClaimAdmitPartEmployerRespondent2(Object responseClaimAdmitPartEmployerRespondent2) {
        setFieldValue("responseClaimAdmitPartEmployerRespondent2", responseClaimAdmitPartEmployerRespondent2);
        return this;
    }

    @JsonIgnore
    public CaseData responseClaimCourtLocation2Required(Object responseClaimCourtLocation2Required) {
        setFieldValue("responseClaimCourtLocation2Required", responseClaimCourtLocation2Required);
        return this;
    }

    @JsonIgnore
    public CaseData responseClaimExpertSpecRequired(Object responseClaimExpertSpecRequired) {
        setFieldValue("responseClaimExpertSpecRequired", responseClaimExpertSpecRequired);
        return this;
    }

    @JsonIgnore
    public CaseData responseClaimExpertSpecRequired2(Object responseClaimExpertSpecRequired2) {
        setFieldValue("responseClaimExpertSpecRequired2", responseClaimExpertSpecRequired2);
        return this;
    }

    @JsonIgnore
    public CaseData responseClaimMediationSpec2Required(Object responseClaimMediationSpec2Required) {
        setFieldValue("responseClaimMediationSpec2Required", responseClaimMediationSpec2Required);
        return this;
    }

    @JsonIgnore
    public CaseData responseClaimMediationSpecLabelRes2(Object responseClaimMediationSpecLabelRes2) {
        setFieldValue("responseClaimMediationSpecLabelRes2", responseClaimMediationSpecLabelRes2);
        return this;
    }

    @JsonIgnore
    public CaseData responseClaimWitnesses(Object responseClaimWitnesses) {
        setFieldValue("responseClaimWitnesses", responseClaimWitnesses);
        return this;
    }

    @JsonIgnore
    public CaseData responseClaimWitnesses2(Object responseClaimWitnesses2) {
        setFieldValue("responseClaimWitnesses2", responseClaimWitnesses2);
        return this;
    }

    @JsonIgnore
    public CaseData responseToClaimAdmitPartWhyNotPayLRspec(Object responseToClaimAdmitPartWhyNotPayLRspec) {
        setFieldValue("responseToClaimAdmitPartWhyNotPayLRspec", responseToClaimAdmitPartWhyNotPayLRspec);
        return this;
    }

    @JsonIgnore
    public CaseData responseToClaimAdmitPartWhyNotPayLRspec2(Object responseToClaimAdmitPartWhyNotPayLRspec2) {
        setFieldValue("responseToClaimAdmitPartWhyNotPayLRspec2", responseToClaimAdmitPartWhyNotPayLRspec2);
        return this;
    }

    @JsonIgnore
    public CaseData sameSolicitorSameResponse(Object sameSolicitorSameResponse) {
        setFieldValue("sameSolicitorSameResponse", sameSolicitorSameResponse);
        return this;
    }

    @JsonIgnore
    public CaseData sdoFastTrackJudgesRecital(Object sdoFastTrackJudgesRecital) {
        setFieldValue("sdoFastTrackJudgesRecital", sdoFastTrackJudgesRecital);
        return this;
    }

    @JsonIgnore
    public CaseData sdoOrderDocument(Object sdoOrderDocument) {
        setFieldValue("sdoOrderDocument", sdoOrderDocument);
        return this;
    }

    @JsonIgnore
    public CaseData sdoR2AddNewDirection(Object sdoR2AddNewDirection) {
        setFieldValue("sdoR2AddNewDirection", sdoR2AddNewDirection);
        return this;
    }

    @JsonIgnore
    public CaseData sdoR2AddendumReport(Object sdoR2AddendumReport) {
        setFieldValue("sdoR2AddendumReport", sdoR2AddendumReport);
        return this;
    }

    @JsonIgnore
    public CaseData sdoR2DisclosureOfDocuments(Object sdoR2DisclosureOfDocuments) {
        setFieldValue("sdoR2DisclosureOfDocuments", sdoR2DisclosureOfDocuments);
        return this;
    }

    @JsonIgnore
    public CaseData sdoR2DisclosureOfDocumentsToggle(Object sdoR2DisclosureOfDocumentsToggle) {
        setFieldValue("sdoR2DisclosureOfDocumentsToggle", sdoR2DisclosureOfDocumentsToggle);
        return this;
    }

    @JsonIgnore
    public CaseData sdoR2DisposalHearingUseOfWelshLangToggleDJ(Object sdoR2DisposalHearingUseOfWelshLangToggleDJ) {
        setFieldValue("sdoR2DisposalHearingUseOfWelshLangToggleDJ", sdoR2DisposalHearingUseOfWelshLangToggleDJ);
        return this;
    }

    @JsonIgnore
    public CaseData sdoR2DisposalHearingUseOfWelshLanguage(Object sdoR2DisposalHearingUseOfWelshLanguage) {
        setFieldValue("sdoR2DisposalHearingUseOfWelshLanguage", sdoR2DisposalHearingUseOfWelshLanguage);
        return this;
    }

    @JsonIgnore
    public CaseData sdoR2DisposalHearingUseOfWelshToggle(Object sdoR2DisposalHearingUseOfWelshToggle) {
        setFieldValue("sdoR2DisposalHearingUseOfWelshToggle", sdoR2DisposalHearingUseOfWelshToggle);
        return this;
    }

    @JsonIgnore
    public CaseData sdoR2DisposalHearingWelshLanguageDJ(Object sdoR2DisposalHearingWelshLanguageDJ) {
        setFieldValue("sdoR2DisposalHearingWelshLanguageDJ", sdoR2DisposalHearingWelshLanguageDJ);
        return this;
    }

    @JsonIgnore
    public CaseData sdoR2DrhUseOfWelshIncludeInOrderToggle(Object sdoR2DrhUseOfWelshIncludeInOrderToggle) {
        setFieldValue("sdoR2DrhUseOfWelshIncludeInOrderToggle", sdoR2DrhUseOfWelshIncludeInOrderToggle);
        return this;
    }

    @JsonIgnore
    public CaseData sdoR2DrhUseOfWelshLanguage(Object sdoR2DrhUseOfWelshLanguage) {
        setFieldValue("sdoR2DrhUseOfWelshLanguage", sdoR2DrhUseOfWelshLanguage);
        return this;
    }

    @JsonIgnore
    public CaseData sdoR2EvidenceAcousticEngineer(Object sdoR2EvidenceAcousticEngineer) {
        setFieldValue("sdoR2EvidenceAcousticEngineer", sdoR2EvidenceAcousticEngineer);
        return this;
    }

    @JsonIgnore
    public CaseData sdoR2ExpertEvidence(Object sdoR2ExpertEvidence) {
        setFieldValue("sdoR2ExpertEvidence", sdoR2ExpertEvidence);
        return this;
    }

    @JsonIgnore
    public CaseData sdoR2FastTrackCreditHire(Object sdoR2FastTrackCreditHire) {
        setFieldValue("sdoR2FastTrackCreditHire", sdoR2FastTrackCreditHire);
        return this;
    }

    @JsonIgnore
    public CaseData sdoR2FastTrackUseOfWelshLanguage(Object sdoR2FastTrackUseOfWelshLanguage) {
        setFieldValue("sdoR2FastTrackUseOfWelshLanguage", sdoR2FastTrackUseOfWelshLanguage);
        return this;
    }

    @JsonIgnore
    public CaseData sdoR2FastTrackUseOfWelshToggle(Object sdoR2FastTrackUseOfWelshToggle) {
        setFieldValue("sdoR2FastTrackUseOfWelshToggle", sdoR2FastTrackUseOfWelshToggle);
        return this;
    }

    @JsonIgnore
    public CaseData sdoR2FastTrackWitnessOfFact(Object sdoR2FastTrackWitnessOfFact) {
        setFieldValue("sdoR2FastTrackWitnessOfFact", sdoR2FastTrackWitnessOfFact);
        return this;
    }

    @JsonIgnore
    public CaseData sdoR2FurtherAudiogram(Object sdoR2FurtherAudiogram) {
        setFieldValue("sdoR2FurtherAudiogram", sdoR2FurtherAudiogram);
        return this;
    }

    @JsonIgnore
    public CaseData sdoR2ImportantNotesDate(Object sdoR2ImportantNotesDate) {
        setFieldValue("sdoR2ImportantNotesDate", sdoR2ImportantNotesDate);
        return this;
    }

    @JsonIgnore
    public CaseData sdoR2ImportantNotesTxt(Object sdoR2ImportantNotesTxt) {
        setFieldValue("sdoR2ImportantNotesTxt", sdoR2ImportantNotesTxt);
        return this;
    }

    @JsonIgnore
    public CaseData sdoR2NihlUseOfWelshIncludeInOrderToggle(Object sdoR2NihlUseOfWelshIncludeInOrderToggle) {
        setFieldValue("sdoR2NihlUseOfWelshIncludeInOrderToggle", sdoR2NihlUseOfWelshIncludeInOrderToggle);
        return this;
    }

    @JsonIgnore
    public CaseData sdoR2NihlUseOfWelshLanguage(Object sdoR2NihlUseOfWelshLanguage) {
        setFieldValue("sdoR2NihlUseOfWelshLanguage", sdoR2NihlUseOfWelshLanguage);
        return this;
    }

    @JsonIgnore
    public CaseData sdoR2PermissionToRelyOnExpert(Object sdoR2PermissionToRelyOnExpert) {
        setFieldValue("sdoR2PermissionToRelyOnExpert", sdoR2PermissionToRelyOnExpert);
        return this;
    }

    @JsonIgnore
    public CaseData sdoR2QuestionsClaimantExpert(Object sdoR2QuestionsClaimantExpert) {
        setFieldValue("sdoR2QuestionsClaimantExpert", sdoR2QuestionsClaimantExpert);
        return this;
    }

    @JsonIgnore
    public CaseData sdoR2QuestionsToEntExpert(Object sdoR2QuestionsToEntExpert) {
        setFieldValue("sdoR2QuestionsToEntExpert", sdoR2QuestionsToEntExpert);
        return this;
    }

    @JsonIgnore
    public CaseData sdoR2ScheduleOfLoss(Object sdoR2ScheduleOfLoss) {
        setFieldValue("sdoR2ScheduleOfLoss", sdoR2ScheduleOfLoss);
        return this;
    }

    @JsonIgnore
    public CaseData sdoR2ScheduleOfLossToggle(Object sdoR2ScheduleOfLossToggle) {
        setFieldValue("sdoR2ScheduleOfLossToggle", sdoR2ScheduleOfLossToggle);
        return this;
    }

    @JsonIgnore
    public CaseData sdoR2SeparatorAddendumReportToggle(Object sdoR2SeparatorAddendumReportToggle) {
        setFieldValue("sdoR2SeparatorAddendumReportToggle", sdoR2SeparatorAddendumReportToggle);
        return this;
    }

    @JsonIgnore
    public CaseData sdoR2SeparatorEvidenceAcousticEngineerToggle(Object sdoR2SeparatorEvidenceAcousticEngineerToggle) {
        setFieldValue("sdoR2SeparatorEvidenceAcousticEngineerToggle", sdoR2SeparatorEvidenceAcousticEngineerToggle);
        return this;
    }

    @JsonIgnore
    public CaseData sdoR2SeparatorExpertEvidenceToggle(Object sdoR2SeparatorExpertEvidenceToggle) {
        setFieldValue("sdoR2SeparatorExpertEvidenceToggle", sdoR2SeparatorExpertEvidenceToggle);
        return this;
    }

    @JsonIgnore
    public CaseData sdoR2SeparatorFurtherAudiogramToggle(Object sdoR2SeparatorFurtherAudiogramToggle) {
        setFieldValue("sdoR2SeparatorFurtherAudiogramToggle", sdoR2SeparatorFurtherAudiogramToggle);
        return this;
    }

    @JsonIgnore
    public CaseData sdoR2SeparatorPermissionToRelyOnExpertToggle(Object sdoR2SeparatorPermissionToRelyOnExpertToggle) {
        setFieldValue("sdoR2SeparatorPermissionToRelyOnExpertToggle", sdoR2SeparatorPermissionToRelyOnExpertToggle);
        return this;
    }

    @JsonIgnore
    public CaseData sdoR2SeparatorQuestionsClaimantExpertToggle(Object sdoR2SeparatorQuestionsClaimantExpertToggle) {
        setFieldValue("sdoR2SeparatorQuestionsClaimantExpertToggle", sdoR2SeparatorQuestionsClaimantExpertToggle);
        return this;
    }

    @JsonIgnore
    public CaseData sdoR2SeparatorQuestionsToEntExpertToggle(Object sdoR2SeparatorQuestionsToEntExpertToggle) {
        setFieldValue("sdoR2SeparatorQuestionsToEntExpertToggle", sdoR2SeparatorQuestionsToEntExpertToggle);
        return this;
    }

    @JsonIgnore
    public CaseData sdoR2SeparatorUploadOfDocumentsToggle(Object sdoR2SeparatorUploadOfDocumentsToggle) {
        setFieldValue("sdoR2SeparatorUploadOfDocumentsToggle", sdoR2SeparatorUploadOfDocumentsToggle);
        return this;
    }

    @JsonIgnore
    public CaseData sdoR2SeparatorWitnessesOfFactToggle(Object sdoR2SeparatorWitnessesOfFactToggle) {
        setFieldValue("sdoR2SeparatorWitnessesOfFactToggle", sdoR2SeparatorWitnessesOfFactToggle);
        return this;
    }

    @JsonIgnore
    public CaseData sdoR2Settlement(Object sdoR2Settlement) {
        setFieldValue("sdoR2Settlement", sdoR2Settlement);
        return this;
    }

    @JsonIgnore
    public CaseData sdoR2SmallClaimsAddNewDirection(Object sdoR2SmallClaimsAddNewDirection) {
        setFieldValue("sdoR2SmallClaimsAddNewDirection", sdoR2SmallClaimsAddNewDirection);
        return this;
    }

    @JsonIgnore
    public CaseData sdoR2SmallClaimsHearingToggle(Object sdoR2SmallClaimsHearingToggle) {
        setFieldValue("sdoR2SmallClaimsHearingToggle", sdoR2SmallClaimsHearingToggle);
        return this;
    }

    @JsonIgnore
    public CaseData sdoR2SmallClaimsImpNotes(Object sdoR2SmallClaimsImpNotes) {
        setFieldValue("sdoR2SmallClaimsImpNotes", sdoR2SmallClaimsImpNotes);
        return this;
    }

    @JsonIgnore
    public CaseData sdoR2SmallClaimsJudgesRecital(Object sdoR2SmallClaimsJudgesRecital) {
        setFieldValue("sdoR2SmallClaimsJudgesRecital", sdoR2SmallClaimsJudgesRecital);
        return this;
    }

    @JsonIgnore
    public CaseData sdoR2SmallClaimsMediationSectionStatement(Object sdoR2SmallClaimsMediationSectionStatement) {
        setFieldValue("sdoR2SmallClaimsMediationSectionStatement", sdoR2SmallClaimsMediationSectionStatement);
        return this;
    }

    @JsonIgnore
    public CaseData sdoR2SmallClaimsMediationSectionToggle(Object sdoR2SmallClaimsMediationSectionToggle) {
        setFieldValue("sdoR2SmallClaimsMediationSectionToggle", sdoR2SmallClaimsMediationSectionToggle);
        return this;
    }

    @JsonIgnore
    public CaseData sdoR2SmallClaimsPPI(Object sdoR2SmallClaimsPPI) {
        setFieldValue("sdoR2SmallClaimsPPI", sdoR2SmallClaimsPPI);
        return this;
    }

    @JsonIgnore
    public CaseData sdoR2SmallClaimsPPIToggle(Object sdoR2SmallClaimsPPIToggle) {
        setFieldValue("sdoR2SmallClaimsPPIToggle", sdoR2SmallClaimsPPIToggle);
        return this;
    }

    @JsonIgnore
    public CaseData sdoR2SmallClaimsUploadDoc(Object sdoR2SmallClaimsUploadDoc) {
        setFieldValue("sdoR2SmallClaimsUploadDoc", sdoR2SmallClaimsUploadDoc);
        return this;
    }

    @JsonIgnore
    public CaseData sdoR2SmallClaimsUploadDocToggle(Object sdoR2SmallClaimsUploadDocToggle) {
        setFieldValue("sdoR2SmallClaimsUploadDocToggle", sdoR2SmallClaimsUploadDocToggle);
        return this;
    }

    @JsonIgnore
    public CaseData sdoR2SmallClaimsUseOfWelshLanguage(Object sdoR2SmallClaimsUseOfWelshLanguage) {
        setFieldValue("sdoR2SmallClaimsUseOfWelshLanguage", sdoR2SmallClaimsUseOfWelshLanguage);
        return this;
    }

    @JsonIgnore
    public CaseData sdoR2SmallClaimsUseOfWelshToggle(Object sdoR2SmallClaimsUseOfWelshToggle) {
        setFieldValue("sdoR2SmallClaimsUseOfWelshToggle", sdoR2SmallClaimsUseOfWelshToggle);
        return this;
    }

    @JsonIgnore
    public CaseData sdoR2SmallClaimsWitnessStatementOther(Object sdoR2SmallClaimsWitnessStatementOther) {
        setFieldValue("sdoR2SmallClaimsWitnessStatementOther", sdoR2SmallClaimsWitnessStatementOther);
        return this;
    }

    @JsonIgnore
    public CaseData sdoR2SmallClaimsWitnessStatements(Object sdoR2SmallClaimsWitnessStatements) {
        setFieldValue("sdoR2SmallClaimsWitnessStatements", sdoR2SmallClaimsWitnessStatements);
        return this;
    }

    @JsonIgnore
    public CaseData sdoR2SmallClaimsWitnessStatementsToggle(Object sdoR2SmallClaimsWitnessStatementsToggle) {
        setFieldValue("sdoR2SmallClaimsWitnessStatementsToggle", sdoR2SmallClaimsWitnessStatementsToggle);
        return this;
    }

    @JsonIgnore
    public CaseData sdoR2Trial(Object sdoR2Trial) {
        setFieldValue("sdoR2Trial", sdoR2Trial);
        return this;
    }

    @JsonIgnore
    public CaseData sdoR2TrialToggle(Object sdoR2TrialToggle) {
        setFieldValue("sdoR2TrialToggle", sdoR2TrialToggle);
        return this;
    }

    @JsonIgnore
    public CaseData sdoR2TrialUseOfWelshLangToggleDJ(Object sdoR2TrialUseOfWelshLangToggleDJ) {
        setFieldValue("sdoR2TrialUseOfWelshLangToggleDJ", sdoR2TrialUseOfWelshLangToggleDJ);
        return this;
    }

    @JsonIgnore
    public CaseData sdoR2TrialWelshLanguageDJ(Object sdoR2TrialWelshLanguageDJ) {
        setFieldValue("sdoR2TrialWelshLanguageDJ", sdoR2TrialWelshLanguageDJ);
        return this;
    }

    @JsonIgnore
    public CaseData sdoR2UploadOfDocuments(Object sdoR2UploadOfDocuments) {
        setFieldValue("sdoR2UploadOfDocuments", sdoR2UploadOfDocuments);
        return this;
    }

    @JsonIgnore
    public CaseData sdoR2WitnessesOfFact(Object sdoR2WitnessesOfFact) {
        setFieldValue("sdoR2WitnessesOfFact", sdoR2WitnessesOfFact);
        return this;
    }

    @JsonIgnore
    public CaseData sdoVariationOfDirections(Object sdoVariationOfDirections) {
        setFieldValue("sdoVariationOfDirections", sdoVariationOfDirections);
        return this;
    }

    @JsonIgnore
    public CaseData sdtRequestId(Object sdtRequestId) {
        setFieldValue("sdtRequestId", sdtRequestId);
        return this;
    }

    @JsonIgnore
    public CaseData sdtRequestIdFromSdt(Object sdtRequestIdFromSdt) {
        setFieldValue("sdtRequestIdFromSdt", sdtRequestIdFromSdt);
        return this;
    }

    @JsonIgnore
    public CaseData selectLitigationFriend(Object selectLitigationFriend) {
        setFieldValue("selectLitigationFriend", selectLitigationFriend);
        return this;
    }

    @JsonIgnore
    public CaseData selectedClaimantForDiscontinuance(Object selectedClaimantForDiscontinuance) {
        setFieldValue("selectedClaimantForDiscontinuance", selectedClaimantForDiscontinuance);
        return this;
    }

    @JsonIgnore
    public CaseData sendAndReplyOption(Object sendAndReplyOption) {
        setFieldValue("sendAndReplyOption", sendAndReplyOption);
        return this;
    }

    @JsonIgnore
    public CaseData sendMessageContent(Object sendMessageContent) {
        setFieldValue("sendMessageContent", sendMessageContent);
        return this;
    }

    @JsonIgnore
    public CaseData sendMessageMetadata(Object sendMessageMetadata) {
        setFieldValue("sendMessageMetadata", sendMessageMetadata);
        return this;
    }

    @JsonIgnore
    public CaseData servedDocumentFiles(Object servedDocumentFiles) {
        setFieldValue("servedDocumentFiles", servedDocumentFiles);
        return this;
    }

    @JsonIgnore
    public CaseData serviceRequestReference(Object serviceRequestReference) {
        setFieldValue("serviceRequestReference", serviceRequestReference);
        return this;
    }

    @JsonIgnore
    public CaseData setFastTrackFlag(Object setFastTrackFlag) {
        setFieldValue("setFastTrackFlag", setFastTrackFlag);
        return this;
    }

    @JsonIgnore
    public CaseData setRequestDJDamagesFlagForWA(Object setRequestDJDamagesFlagForWA) {
        setFieldValue("setRequestDJDamagesFlagForWA", setRequestDJDamagesFlagForWA);
        return this;
    }

    @JsonIgnore
    public CaseData setSmallClaimsFlag(Object setSmallClaimsFlag) {
        setFieldValue("setSmallClaimsFlag", setSmallClaimsFlag);
        return this;
    }

    @JsonIgnore
    public CaseData settleReason(Object settleReason) {
        setFieldValue("settleReason", settleReason);
        return this;
    }

    @JsonIgnore
    public CaseData severeDisabilityPremiumPayments(Object severeDisabilityPremiumPayments) {
        setFieldValue("severeDisabilityPremiumPayments", severeDisabilityPremiumPayments);
        return this;
    }

    @JsonIgnore
    public CaseData showCarmFields(Object showCarmFields) {
        setFieldValue("showCarmFields", showCarmFields);
        return this;
    }

    @JsonIgnore
    public CaseData showDJFixedCostsScreen(Object showDJFixedCostsScreen) {
        setFieldValue("showDJFixedCostsScreen", showDJFixedCostsScreen);
        return this;
    }

    @JsonIgnore
    public CaseData showHowToAddTimeLinePage(Object showHowToAddTimeLinePage) {
        setFieldValue("showHowToAddTimeLinePage", showHowToAddTimeLinePage);
        return this;
    }

    @JsonIgnore
    public CaseData showOldDJFixedCostsScreen(Object showOldDJFixedCostsScreen) {
        setFieldValue("showOldDJFixedCostsScreen", showOldDJFixedCostsScreen);
        return this;
    }

    @JsonIgnore
    public CaseData showOrderAfterHearingDatePage(Object showOrderAfterHearingDatePage) {
        setFieldValue("showOrderAfterHearingDatePage", showOrderAfterHearingDatePage);
        return this;
    }

    @JsonIgnore
    public CaseData smallClaimHearingInterpreterDescription(Object smallClaimHearingInterpreterDescription) {
        setFieldValue("smallClaimHearingInterpreterDescription", smallClaimHearingInterpreterDescription);
        return this;
    }

    @JsonIgnore
    public CaseData smallClaimHearingInterpreterDescription2(Object smallClaimHearingInterpreterDescription2) {
        setFieldValue("smallClaimHearingInterpreterDescription2", smallClaimHearingInterpreterDescription2);
        return this;
    }

    @JsonIgnore
    public CaseData smallClaimHearingInterpreterRequired(Object smallClaimHearingInterpreterRequired) {
        setFieldValue("smallClaimHearingInterpreterRequired", smallClaimHearingInterpreterRequired);
        return this;
    }

    @JsonIgnore
    public CaseData smallClaims(Object smallClaims) {
        setFieldValue("smallClaims", smallClaims);
        return this;
    }

    @JsonIgnore
    public CaseData smallClaimsAddNewDirections(Object smallClaimsAddNewDirections) {
        setFieldValue("smallClaimsAddNewDirections", smallClaimsAddNewDirections);
        return this;
    }

    @JsonIgnore
    public CaseData smallClaimsCreditHire(Object smallClaimsCreditHire) {
        setFieldValue("smallClaimsCreditHire", smallClaimsCreditHire);
        return this;
    }

    @JsonIgnore
    public CaseData smallClaimsDocuments(Object smallClaimsDocuments) {
        setFieldValue("smallClaimsDocuments", smallClaimsDocuments);
        return this;
    }

    @JsonIgnore
    public CaseData smallClaimsDocumentsToggle(Object smallClaimsDocumentsToggle) {
        setFieldValue("smallClaimsDocumentsToggle", smallClaimsDocumentsToggle);
        return this;
    }

    @JsonIgnore
    public CaseData smallClaimsFlightDelay(Object smallClaimsFlightDelay) {
        setFieldValue("smallClaimsFlightDelay", smallClaimsFlightDelay);
        return this;
    }

    @JsonIgnore
    public CaseData smallClaimsFlightDelayToggle(Object smallClaimsFlightDelayToggle) {
        setFieldValue("smallClaimsFlightDelayToggle", smallClaimsFlightDelayToggle);
        return this;
    }

    @JsonIgnore
    public CaseData smallClaimsHearing(Object smallClaimsHearing) {
        setFieldValue("smallClaimsHearing", smallClaimsHearing);
        return this;
    }

    @JsonIgnore
    public CaseData smallClaimsHearingDateToToggle(Object smallClaimsHearingDateToToggle) {
        setFieldValue("smallClaimsHearingDateToToggle", smallClaimsHearingDateToToggle);
        return this;
    }

    @JsonIgnore
    public CaseData smallClaimsHearingToggle(Object smallClaimsHearingToggle) {
        setFieldValue("smallClaimsHearingToggle", smallClaimsHearingToggle);
        return this;
    }

    @JsonIgnore
    public CaseData smallClaimsHousingDisrepair(Object smallClaimsHousingDisrepair) {
        setFieldValue("smallClaimsHousingDisrepair", smallClaimsHousingDisrepair);
        return this;
    }

    @JsonIgnore
    public CaseData smallClaimsJudgementDeductionValue(Object smallClaimsJudgementDeductionValue) {
        setFieldValue("smallClaimsJudgementDeductionValue", smallClaimsJudgementDeductionValue);
        return this;
    }

    @JsonIgnore
    public CaseData smallClaimsJudgesRecital(Object smallClaimsJudgesRecital) {
        setFieldValue("smallClaimsJudgesRecital", smallClaimsJudgesRecital);
        return this;
    }

    @JsonIgnore
    public CaseData smallClaimsMediationSectionToggle(Object smallClaimsMediationSectionToggle) {
        setFieldValue("smallClaimsMediationSectionToggle", smallClaimsMediationSectionToggle);
        return this;
    }

    @JsonIgnore
    public CaseData smallClaimsMethod(Object smallClaimsMethod) {
        setFieldValue("smallClaimsMethod", smallClaimsMethod);
        return this;
    }

    @JsonIgnore
    public CaseData smallClaimsMethodInPerson(Object smallClaimsMethodInPerson) {
        setFieldValue("smallClaimsMethodInPerson", smallClaimsMethodInPerson);
        return this;
    }

    @JsonIgnore
    public CaseData smallClaimsMethodTelephoneHearing(Object smallClaimsMethodTelephoneHearing) {
        setFieldValue("smallClaimsMethodTelephoneHearing", smallClaimsMethodTelephoneHearing);
        return this;
    }

    @JsonIgnore
    public CaseData smallClaimsMethodToggle(Object smallClaimsMethodToggle) {
        setFieldValue("smallClaimsMethodToggle", smallClaimsMethodToggle);
        return this;
    }

    @JsonIgnore
    public CaseData smallClaimsMethodVideoConferenceHearing(Object smallClaimsMethodVideoConferenceHearing) {
        setFieldValue("smallClaimsMethodVideoConferenceHearing", smallClaimsMethodVideoConferenceHearing);
        return this;
    }

    @JsonIgnore
    public CaseData smallClaimsNotes(Object smallClaimsNotes) {
        setFieldValue("smallClaimsNotes", smallClaimsNotes);
        return this;
    }

    @JsonIgnore
    public CaseData smallClaimsPPI(Object smallClaimsPPI) {
        setFieldValue("smallClaimsPPI", smallClaimsPPI);
        return this;
    }

    @JsonIgnore
    public CaseData smallClaimsPenalNotice(Object smallClaimsPenalNotice) {
        setFieldValue("smallClaimsPenalNotice", smallClaimsPenalNotice);
        return this;
    }

    @JsonIgnore
    public CaseData smallClaimsPenalNoticeToggle(Object smallClaimsPenalNoticeToggle) {
        setFieldValue("smallClaimsPenalNoticeToggle", smallClaimsPenalNoticeToggle);
        return this;
    }

    @JsonIgnore
    public CaseData smallClaimsRoadTrafficAccident(Object smallClaimsRoadTrafficAccident) {
        setFieldValue("smallClaimsRoadTrafficAccident", smallClaimsRoadTrafficAccident);
        return this;
    }

    @JsonIgnore
    public CaseData smallClaimsWitnessStatement(Object smallClaimsWitnessStatement) {
        setFieldValue("smallClaimsWitnessStatement", smallClaimsWitnessStatement);
        return this;
    }

    @JsonIgnore
    public CaseData smallClaimsWitnessStatementToggle(Object smallClaimsWitnessStatementToggle) {
        setFieldValue("smallClaimsWitnessStatementToggle", smallClaimsWitnessStatementToggle);
        return this;
    }

    @JsonIgnore
    public CaseData solicitorReferencesCopy(Object solicitorReferencesCopy) {
        setFieldValue("solicitorReferencesCopy", solicitorReferencesCopy);
        return this;
    }

    @JsonIgnore
    public CaseData specAoSApplicantCorrespondenceAddressdetails(Object specAoSApplicantCorrespondenceAddressdetails) {
        setFieldValue("specAoSApplicantCorrespondenceAddressdetails", specAoSApplicantCorrespondenceAddressdetails);
        return this;
    }

    @JsonIgnore
    public CaseData specAoSRespondent2CorrespondenceAddressRequired(Object specAoSRespondent2CorrespondenceAddressRequired) {
        setFieldValue("specAoSRespondent2CorrespondenceAddressRequired", specAoSRespondent2CorrespondenceAddressRequired);
        return this;
    }

    @JsonIgnore
    public CaseData specAoSRespondent2CorrespondenceAddressdetails(Object specAoSRespondent2CorrespondenceAddressdetails) {
        setFieldValue("specAoSRespondent2CorrespondenceAddressdetails", specAoSRespondent2CorrespondenceAddressdetails);
        return this;
    }

    @JsonIgnore
    public CaseData specAoSRespondent2HomeAddressDetails(Object specAoSRespondent2HomeAddressDetails) {
        setFieldValue("specAoSRespondent2HomeAddressDetails", specAoSRespondent2HomeAddressDetails);
        return this;
    }

    @JsonIgnore
    public CaseData specAoSRespondent2HomeAddressRequired(Object specAoSRespondent2HomeAddressRequired) {
        setFieldValue("specAoSRespondent2HomeAddressRequired", specAoSRespondent2HomeAddressRequired);
        return this;
    }

    @JsonIgnore
    public CaseData specAoSRespondentCorrespondenceAddressRequired(Object specAoSRespondentCorrespondenceAddressRequired) {
        setFieldValue("specAoSRespondentCorrespondenceAddressRequired", specAoSRespondentCorrespondenceAddressRequired);
        return this;
    }

    @JsonIgnore
    public CaseData specAoSRespondentCorrespondenceAddressdetails(Object specAoSRespondentCorrespondenceAddressdetails) {
        setFieldValue("specAoSRespondentCorrespondenceAddressdetails", specAoSRespondentCorrespondenceAddressdetails);
        return this;
    }

    @JsonIgnore
    public CaseData specApplicantCorrespondenceAddressRequired(Object specApplicantCorrespondenceAddressRequired) {
        setFieldValue("specApplicantCorrespondenceAddressRequired", specApplicantCorrespondenceAddressRequired);
        return this;
    }

    @JsonIgnore
    public CaseData specApplicantCorrespondenceAddressdetails(Object specApplicantCorrespondenceAddressdetails) {
        setFieldValue("specApplicantCorrespondenceAddressdetails", specApplicantCorrespondenceAddressdetails);
        return this;
    }

    @JsonIgnore
    public CaseData specClaimDetailsDocumentFiles(Object specClaimDetailsDocumentFiles) {
        setFieldValue("specClaimDetailsDocumentFiles", specClaimDetailsDocumentFiles);
        return this;
    }

    @JsonIgnore
    public CaseData specClaimResponseTimelineList(Object specClaimResponseTimelineList) {
        setFieldValue("specClaimResponseTimelineList", specClaimResponseTimelineList);
        return this;
    }

    @JsonIgnore
    public CaseData specClaimResponseTimelineList2(Object specClaimResponseTimelineList2) {
        setFieldValue("specClaimResponseTimelineList2", specClaimResponseTimelineList2);
        return this;
    }

    @JsonIgnore
    public CaseData specClaimTemplateDocumentFiles(Object specClaimTemplateDocumentFiles) {
        setFieldValue("specClaimTemplateDocumentFiles", specClaimTemplateDocumentFiles);
        return this;
    }

    @JsonIgnore
    public CaseData specDefenceAdmitted2Required(Object specDefenceAdmitted2Required) {
        setFieldValue("specDefenceAdmitted2Required", specDefenceAdmitted2Required);
        return this;
    }

    @JsonIgnore
    public CaseData specDefenceAdmittedRequired(Object specDefenceAdmittedRequired) {
        setFieldValue("specDefenceAdmittedRequired", specDefenceAdmittedRequired);
        return this;
    }

    @JsonIgnore
    public CaseData specDefenceFullAdmitted2Required(Object specDefenceFullAdmitted2Required) {
        setFieldValue("specDefenceFullAdmitted2Required", specDefenceFullAdmitted2Required);
        return this;
    }

    @JsonIgnore
    public CaseData specDefenceFullAdmittedRequired(Object specDefenceFullAdmittedRequired) {
        setFieldValue("specDefenceFullAdmittedRequired", specDefenceFullAdmittedRequired);
        return this;
    }

    @JsonIgnore
    public CaseData specDefenceRouteAdmittedAmountClaimed2Label(Object specDefenceRouteAdmittedAmountClaimed2Label) {
        setFieldValue("specDefenceRouteAdmittedAmountClaimed2Label", specDefenceRouteAdmittedAmountClaimed2Label);
        return this;
    }

    @JsonIgnore
    public CaseData specDefenceRouteUploadDocumentLabel3(Object specDefenceRouteUploadDocumentLabel3) {
        setFieldValue("specDefenceRouteUploadDocumentLabel3", specDefenceRouteUploadDocumentLabel3);
        return this;
    }

    @JsonIgnore
    public CaseData specDefendant1Debts(Object specDefendant1Debts) {
        setFieldValue("specDefendant1Debts", specDefendant1Debts);
        return this;
    }

    @JsonIgnore
    public CaseData specDefendant1SelfEmploymentDetails(Object specDefendant1SelfEmploymentDetails) {
        setFieldValue("specDefendant1SelfEmploymentDetails", specDefendant1SelfEmploymentDetails);
        return this;
    }

    @JsonIgnore
    public CaseData specDefendant2Debts(Object specDefendant2Debts) {
        setFieldValue("specDefendant2Debts", specDefendant2Debts);
        return this;
    }

    @JsonIgnore
    public CaseData specDefendant2SelfEmploymentDetails(Object specDefendant2SelfEmploymentDetails) {
        setFieldValue("specDefendant2SelfEmploymentDetails", specDefendant2SelfEmploymentDetails);
        return this;
    }

    @JsonIgnore
    public CaseData specDisputesOrPartAdmission(Object specDisputesOrPartAdmission) {
        setFieldValue("specDisputesOrPartAdmission", specDisputesOrPartAdmission);
        return this;
    }

    @JsonIgnore
    public CaseData specFullAdmissionOrPartAdmission(Object specFullAdmissionOrPartAdmission) {
        setFieldValue("specFullAdmissionOrPartAdmission", specFullAdmissionOrPartAdmission);
        return this;
    }

    @JsonIgnore
    public CaseData specFullAdmitPaid(Object specFullAdmitPaid) {
        setFieldValue("specFullAdmitPaid", specFullAdmitPaid);
        return this;
    }

    @JsonIgnore
    public CaseData specFullDefenceOrPartAdmission(Object specFullDefenceOrPartAdmission) {
        setFieldValue("specFullDefenceOrPartAdmission", specFullDefenceOrPartAdmission);
        return this;
    }

    @JsonIgnore
    public CaseData specFullDefenceOrPartAdmission1V1(Object specFullDefenceOrPartAdmission1V1) {
        setFieldValue("specFullDefenceOrPartAdmission1V1", specFullDefenceOrPartAdmission1V1);
        return this;
    }

    @JsonIgnore
    public CaseData specPaidLessAmountOrDisputesOrPartAdmission(Object specPaidLessAmountOrDisputesOrPartAdmission) {
        setFieldValue("specPaidLessAmountOrDisputesOrPartAdmission", specPaidLessAmountOrDisputesOrPartAdmission);
        return this;
    }

    @JsonIgnore
    public CaseData specPartAdmitPaid(Object specPartAdmitPaid) {
        setFieldValue("specPartAdmitPaid", specPartAdmitPaid);
        return this;
    }

    @JsonIgnore
    public CaseData specRespondent2CorrespondenceAddressRequired(Object specRespondent2CorrespondenceAddressRequired) {
        setFieldValue("specRespondent2CorrespondenceAddressRequired", specRespondent2CorrespondenceAddressRequired);
        return this;
    }

    @JsonIgnore
    public CaseData specRespondent2CorrespondenceAddressdetails(Object specRespondent2CorrespondenceAddressdetails) {
        setFieldValue("specRespondent2CorrespondenceAddressdetails", specRespondent2CorrespondenceAddressdetails);
        return this;
    }

    @JsonIgnore
    public CaseData specRespondentCorrespondenceAddressRequired(Object specRespondentCorrespondenceAddressRequired) {
        setFieldValue("specRespondentCorrespondenceAddressRequired", specRespondentCorrespondenceAddressRequired);
        return this;
    }

    @JsonIgnore
    public CaseData specRespondentCorrespondenceAddressdetails(Object specRespondentCorrespondenceAddressdetails) {
        setFieldValue("specRespondentCorrespondenceAddressdetails", specRespondentCorrespondenceAddressdetails);
        return this;
    }

    @JsonIgnore
    public CaseData specRespondentSolicitor1EmailAddress(Object specRespondentSolicitor1EmailAddress) {
        setFieldValue("specRespondentSolicitor1EmailAddress", specRespondentSolicitor1EmailAddress);
        return this;
    }

    @JsonIgnore
    public CaseData specResponseTimelineDocumentFiles(Object specResponseTimelineDocumentFiles) {
        setFieldValue("specResponseTimelineDocumentFiles", specResponseTimelineDocumentFiles);
        return this;
    }

    @JsonIgnore
    public CaseData specResponseTimelineOfEvents(Object specResponseTimelineOfEvents) {
        setFieldValue("specResponseTimelineOfEvents", specResponseTimelineOfEvents);
        return this;
    }

    @JsonIgnore
    public CaseData specResponseTimelineOfEvents2(Object specResponseTimelineOfEvents2) {
        setFieldValue("specResponseTimelineOfEvents2", specResponseTimelineOfEvents2);
        return this;
    }

    @JsonIgnore
    public CaseData specResponselistYourEvidenceList(Object specResponselistYourEvidenceList) {
        setFieldValue("specResponselistYourEvidenceList", specResponselistYourEvidenceList);
        return this;
    }

    @JsonIgnore
    public CaseData specResponselistYourEvidenceList2(Object specResponselistYourEvidenceList2) {
        setFieldValue("specResponselistYourEvidenceList2", specResponselistYourEvidenceList2);
        return this;
    }

    @JsonIgnore
    public CaseData speclistYourEvidenceList(Object speclistYourEvidenceList) {
        setFieldValue("speclistYourEvidenceList", speclistYourEvidenceList);
        return this;
    }

    @JsonIgnore
    public CaseData storedObligationData(Object storedObligationData) {
        setFieldValue("storedObligationData", storedObligationData);
        return this;
    }

    @JsonIgnore
    public CaseData superClaimType(Object superClaimType) {
        setFieldValue("superClaimType", superClaimType);
        return this;
    }

    @JsonIgnore
    public CaseData taskManagementLocationsTab(Object taskManagementLocationsTab) {
        setFieldValue("taskManagementLocationsTab", taskManagementLocationsTab);
        return this;
    }

    @JsonIgnore
    public CaseData timelineOfEvents(Object timelineOfEvents) {
        setFieldValue("timelineOfEvents", timelineOfEvents);
        return this;
    }

    @JsonIgnore
    public CaseData tocTransferCaseReason(Object tocTransferCaseReason) {
        setFieldValue("tocTransferCaseReason", tocTransferCaseReason);
        return this;
    }

    @JsonIgnore
    public CaseData totalClaimAmountPlusInterest(Object totalClaimAmountPlusInterest) {
        setFieldValue("totalClaimAmountPlusInterest", totalClaimAmountPlusInterest);
        return this;
    }

    @JsonIgnore
    public CaseData totalClaimAmountPlusInterestAdmitPart(Object totalClaimAmountPlusInterestAdmitPart) {
        setFieldValue("totalClaimAmountPlusInterestAdmitPart", totalClaimAmountPlusInterestAdmitPart);
        return this;
    }

    @JsonIgnore
    public CaseData totalClaimAmountPlusInterestAdmitPartString(Object totalClaimAmountPlusInterestAdmitPartString) {
        setFieldValue("totalClaimAmountPlusInterestAdmitPartString", totalClaimAmountPlusInterestAdmitPartString);
        return this;
    }

    @JsonIgnore
    public CaseData totalClaimAmountPlusInterestString(Object totalClaimAmountPlusInterestString) {
        setFieldValue("totalClaimAmountPlusInterestString", totalClaimAmountPlusInterestString);
        return this;
    }

    @JsonIgnore
    public CaseData totalInterest(Object totalInterest) {
        setFieldValue("totalInterest", totalInterest);
        return this;
    }

    @JsonIgnore
    public CaseData transferCaseDetails(Object transferCaseDetails) {
        setFieldValue("transferCaseDetails", transferCaseDetails);
        return this;
    }

    @JsonIgnore
    public CaseData transferCourtLocationList(Object transferCourtLocationList) {
        setFieldValue("transferCourtLocationList", transferCourtLocationList);
        return this;
    }

    @JsonIgnore
    public CaseData trialAdditionalDirectionsForFastTrack(Object trialAdditionalDirectionsForFastTrack) {
        setFieldValue("trialAdditionalDirectionsForFastTrack", trialAdditionalDirectionsForFastTrack);
        return this;
    }

    @JsonIgnore
    public CaseData trialAuthorityFlag(Object trialAuthorityFlag) {
        setFieldValue("trialAuthorityFlag", trialAuthorityFlag);
        return this;
    }

    @JsonIgnore
    public CaseData trialClinicalNegligence(Object trialClinicalNegligence) {
        setFieldValue("trialClinicalNegligence", trialClinicalNegligence);
        return this;
    }

    @JsonIgnore
    public CaseData trialCostsFlag(Object trialCostsFlag) {
        setFieldValue("trialCostsFlag", trialCostsFlag);
        return this;
    }

    @JsonIgnore
    public CaseData trialCreditHire(Object trialCreditHire) {
        setFieldValue("trialCreditHire", trialCreditHire);
        return this;
    }

    @JsonIgnore
    public CaseData trialDocumentaryFlag(Object trialDocumentaryFlag) {
        setFieldValue("trialDocumentaryFlag", trialDocumentaryFlag);
        return this;
    }

    @JsonIgnore
    public CaseData trialEmployersLiability(Object trialEmployersLiability) {
        setFieldValue("trialEmployersLiability", trialEmployersLiability);
        return this;
    }

    @JsonIgnore
    public CaseData trialHearingAddNewDirectionsDJ(Object trialHearingAddNewDirectionsDJ) {
        setFieldValue("trialHearingAddNewDirectionsDJ", trialHearingAddNewDirectionsDJ);
        return this;
    }

    @JsonIgnore
    public CaseData trialHearingAlternativeDisputeDJToggle(Object trialHearingAlternativeDisputeDJToggle) {
        setFieldValue("trialHearingAlternativeDisputeDJToggle", trialHearingAlternativeDisputeDJToggle);
        return this;
    }

    @JsonIgnore
    public CaseData trialHearingCostsToggle(Object trialHearingCostsToggle) {
        setFieldValue("trialHearingCostsToggle", trialHearingCostsToggle);
        return this;
    }

    @JsonIgnore
    public CaseData trialHearingDisclosureOfDocumentsDJ(Object trialHearingDisclosureOfDocumentsDJ) {
        setFieldValue("trialHearingDisclosureOfDocumentsDJ", trialHearingDisclosureOfDocumentsDJ);
        return this;
    }

    @JsonIgnore
    public CaseData trialHearingDisclosureOfDocumentsDJToggle(Object trialHearingDisclosureOfDocumentsDJToggle) {
        setFieldValue("trialHearingDisclosureOfDocumentsDJToggle", trialHearingDisclosureOfDocumentsDJToggle);
        return this;
    }

    @JsonIgnore
    public CaseData trialHearingJudgesRecitalDJ(Object trialHearingJudgesRecitalDJ) {
        setFieldValue("trialHearingJudgesRecitalDJ", trialHearingJudgesRecitalDJ);
        return this;
    }

    @JsonIgnore
    public CaseData trialHearingMethodDJ(Object trialHearingMethodDJ) {
        setFieldValue("trialHearingMethodDJ", trialHearingMethodDJ);
        return this;
    }

    @JsonIgnore
    public CaseData trialHearingNotesDJ(Object trialHearingNotesDJ) {
        setFieldValue("trialHearingNotesDJ", trialHearingNotesDJ);
        return this;
    }

    @JsonIgnore
    public CaseData trialHearingSchedulesOfLossDJ(Object trialHearingSchedulesOfLossDJ) {
        setFieldValue("trialHearingSchedulesOfLossDJ", trialHearingSchedulesOfLossDJ);
        return this;
    }

    @JsonIgnore
    public CaseData trialHearingSchedulesOfLossDJToggle(Object trialHearingSchedulesOfLossDJToggle) {
        setFieldValue("trialHearingSchedulesOfLossDJToggle", trialHearingSchedulesOfLossDJToggle);
        return this;
    }

    @JsonIgnore
    public CaseData trialHearingSettlementDJToggle(Object trialHearingSettlementDJToggle) {
        setFieldValue("trialHearingSettlementDJToggle", trialHearingSettlementDJToggle);
        return this;
    }

    @JsonIgnore
    public CaseData trialHearingTrialDJ(Object trialHearingTrialDJ) {
        setFieldValue("trialHearingTrialDJ", trialHearingTrialDJ);
        return this;
    }

    @JsonIgnore
    public CaseData trialHearingTrialDJToggle(Object trialHearingTrialDJToggle) {
        setFieldValue("trialHearingTrialDJToggle", trialHearingTrialDJToggle);
        return this;
    }

    @JsonIgnore
    public CaseData trialHearingVariationsDirectionsDJToggle(Object trialHearingVariationsDirectionsDJToggle) {
        setFieldValue("trialHearingVariationsDirectionsDJToggle", trialHearingVariationsDirectionsDJToggle);
        return this;
    }

    @JsonIgnore
    public CaseData trialHearingWitnessOfFactDJToggle(Object trialHearingWitnessOfFactDJToggle) {
        setFieldValue("trialHearingWitnessOfFactDJToggle", trialHearingWitnessOfFactDJToggle);
        return this;
    }

    @JsonIgnore
    public CaseData trialHousingDisrepair(Object trialHousingDisrepair) {
        setFieldValue("trialHousingDisrepair", trialHousingDisrepair);
        return this;
    }

    @JsonIgnore
    public CaseData trialOrderMadeWithoutHearingDJ(Object trialOrderMadeWithoutHearingDJ) {
        setFieldValue("trialOrderMadeWithoutHearingDJ", trialOrderMadeWithoutHearingDJ);
        return this;
    }

    @JsonIgnore
    public CaseData trialPersonalInjury(Object trialPersonalInjury) {
        setFieldValue("trialPersonalInjury", trialPersonalInjury);
        return this;
    }

    @JsonIgnore
    public CaseData trialReadyChecked(Object trialReadyChecked) {
        setFieldValue("trialReadyChecked", trialReadyChecked);
        return this;
    }

    @JsonIgnore
    public CaseData trialReadyRespondent2(Object trialReadyRespondent2) {
        setFieldValue("trialReadyRespondent2", trialReadyRespondent2);
        return this;
    }

    @JsonIgnore
    public CaseData trialRoadTrafficAccident(Object trialRoadTrafficAccident) {
        setFieldValue("trialRoadTrafficAccident", trialRoadTrafficAccident);
        return this;
    }

    @JsonIgnore
    public CaseData trialSelectionEvidence(Object trialSelectionEvidence) {
        setFieldValue("trialSelectionEvidence", trialSelectionEvidence);
        return this;
    }

    @JsonIgnore
    public CaseData trialSelectionEvidenceRes(Object trialSelectionEvidenceRes) {
        setFieldValue("trialSelectionEvidenceRes", trialSelectionEvidenceRes);
        return this;
    }

    @JsonIgnore
    public CaseData trialSelectionEvidenceSmallClaim(Object trialSelectionEvidenceSmallClaim) {
        setFieldValue("trialSelectionEvidenceSmallClaim", trialSelectionEvidenceSmallClaim);
        return this;
    }

    @JsonIgnore
    public CaseData trialSelectionEvidenceSmallClaimRes(Object trialSelectionEvidenceSmallClaimRes) {
        setFieldValue("trialSelectionEvidenceSmallClaimRes", trialSelectionEvidenceSmallClaimRes);
        return this;
    }

    @JsonIgnore
    public CaseData typeOfDiscontinuance(Object typeOfDiscontinuance) {
        setFieldValue("typeOfDiscontinuance", typeOfDiscontinuance);
        return this;
    }

    @JsonIgnore
    public CaseData uiStatementOfTruth(Object uiStatementOfTruth) {
        setFieldValue("uiStatementOfTruth", uiStatementOfTruth);
        return this;
    }

    @JsonIgnore
    public CaseData unassignedCaseListDisplayOrganisationReferences(Object unassignedCaseListDisplayOrganisationReferences) {
        setFieldValue("unassignedCaseListDisplayOrganisationReferences", unassignedCaseListDisplayOrganisationReferences);
        return this;
    }

    @JsonIgnore
    public CaseData unsuitableSDODate(Object unsuitableSDODate) {
        setFieldValue("unsuitableSDODate", unsuitableSDODate);
        return this;
    }

    @JsonIgnore
    public CaseData upholdingPreviousOrderReason(Object upholdingPreviousOrderReason) {
        setFieldValue("upholdingPreviousOrderReason", upholdingPreviousOrderReason);
        return this;
    }

    @JsonIgnore
    public CaseData uploadMediationDocumentsForm(Object uploadMediationDocumentsForm) {
        setFieldValue("uploadMediationDocumentsForm", uploadMediationDocumentsForm);
        return this;
    }

    @JsonIgnore
    public CaseData uploadOrderDocumentFromTemplate(Object uploadOrderDocumentFromTemplate) {
        setFieldValue("uploadOrderDocumentFromTemplate", uploadOrderDocumentFromTemplate);
        return this;
    }

    @JsonIgnore
    public CaseData uploadParticularsOfClaim(Object uploadParticularsOfClaim) {
        setFieldValue("uploadParticularsOfClaim", uploadParticularsOfClaim);
        return this;
    }

    @JsonIgnore
    public CaseData urgentFlag(Object urgentFlag) {
        setFieldValue("urgentFlag", urgentFlag);
        return this;
    }

    @JsonIgnore
    public CaseData waTaskToCompleteId(Object waTaskToCompleteId) {
        setFieldValue("waTaskToCompleteId", waTaskToCompleteId);
        return this;
    }

    @JsonIgnore
    public CaseData whenToBePaidText(Object whenToBePaidText) {
        setFieldValue("whenToBePaidText", whenToBePaidText);
        return this;
    }

    @JsonIgnore
    public CaseData withdrawClaim(Object withdrawClaim) {
        setFieldValue("withdrawClaim", withdrawClaim);
        return this;
    }

    @JsonIgnore
    public CaseData witnessReferredStatementFlag(Object witnessReferredStatementFlag) {
        setFieldValue("witnessReferredStatementFlag", witnessReferredStatementFlag);
        return this;
    }

    @JsonIgnore
    public CaseData witnessSelectionEvidence(Object witnessSelectionEvidence) {
        setFieldValue("witnessSelectionEvidence", witnessSelectionEvidence);
        return this;
    }

    @JsonIgnore
    public CaseData witnessSelectionEvidenceRes(Object witnessSelectionEvidenceRes) {
        setFieldValue("witnessSelectionEvidenceRes", witnessSelectionEvidenceRes);
        return this;
    }

    @JsonIgnore
    public CaseData witnessSelectionEvidenceSmallClaim(Object witnessSelectionEvidenceSmallClaim) {
        setFieldValue("witnessSelectionEvidenceSmallClaim", witnessSelectionEvidenceSmallClaim);
        return this;
    }

    @JsonIgnore
    public CaseData witnessSelectionEvidenceSmallClaimRes(Object witnessSelectionEvidenceSmallClaimRes) {
        setFieldValue("witnessSelectionEvidenceSmallClaimRes", witnessSelectionEvidenceSmallClaimRes);
        return this;
    }

    @JsonIgnore
    public CaseData witnessStatementFlag(Object witnessStatementFlag) {
        setFieldValue("witnessStatementFlag", witnessStatementFlag);
        return this;
    }

    @JsonIgnore
    public CaseData witnessSummaryFlag(Object witnessSummaryFlag) {
        setFieldValue("witnessSummaryFlag", witnessSummaryFlag);
        return this;
    }

    @JsonIgnore
    public CaseData writtenRepConDocClaimant(Object writtenRepConDocClaimant) {
        setFieldValue("writtenRepConDocClaimant", writtenRepConDocClaimant);
        return this;
    }

    @JsonIgnore
    public CaseData writtenRepConDocRespondentSol(Object writtenRepConDocRespondentSol) {
        setFieldValue("writtenRepConDocRespondentSol", writtenRepConDocRespondentSol);
        return this;
    }

    @JsonIgnore
    public CaseData writtenRepConDocRespondentSolTwo(Object writtenRepConDocRespondentSolTwo) {
        setFieldValue("writtenRepConDocRespondentSolTwo", writtenRepConDocRespondentSolTwo);
        return this;
    }

    @JsonIgnore
    public CaseData writtenRepConDocStaff(Object writtenRepConDocStaff) {
        setFieldValue("writtenRepConDocStaff", writtenRepConDocStaff);
        return this;
    }

    @JsonIgnore
    public CaseData writtenRepConcurrentDocument(Object writtenRepConcurrentDocument) {
        setFieldValue("writtenRepConcurrentDocument", writtenRepConcurrentDocument);
        return this;
    }

    @JsonIgnore
    public CaseData writtenRepSeqDocClaimant(Object writtenRepSeqDocClaimant) {
        setFieldValue("writtenRepSeqDocClaimant", writtenRepSeqDocClaimant);
        return this;
    }

    @JsonIgnore
    public CaseData writtenRepSeqDocRespondentSol(Object writtenRepSeqDocRespondentSol) {
        setFieldValue("writtenRepSeqDocRespondentSol", writtenRepSeqDocRespondentSol);
        return this;
    }

    @JsonIgnore
    public CaseData writtenRepSeqDocRespondentSolTwo(Object writtenRepSeqDocRespondentSolTwo) {
        setFieldValue("writtenRepSeqDocRespondentSolTwo", writtenRepSeqDocRespondentSolTwo);
        return this;
    }

    @JsonIgnore
    public CaseData writtenRepSeqDocStaff(Object writtenRepSeqDocStaff) {
        setFieldValue("writtenRepSeqDocStaff", writtenRepSeqDocStaff);
        return this;
    }

    @JsonIgnore
    public CaseData writtenRepSequentialDocument(Object writtenRepSequentialDocument) {
        setFieldValue("writtenRepSequentialDocument", writtenRepSequentialDocument);
        return this;
    }

    @JsonIgnore
    public CaseData caseBundles(Object caseBundles) {
        setFieldValue("caseBundles", caseBundles);
        return this;
    }

    @JsonIgnore
    public CaseData caseDocuments(Object caseDocuments) {
        setFieldValue("caseDocuments", caseDocuments);
        return this;
    }

    @JsonIgnore
    public CaseData claimantResponseDocuments(Object claimantResponseDocuments) {
        setFieldValue("claimantResponseDocuments", claimantResponseDocuments);
        return this;
    }

    @JsonIgnore
    public CaseData courtOfficersOrders(Object courtOfficersOrders) {
        setFieldValue("courtOfficersOrders", courtOfficersOrders);
        return this;
    }

    @JsonIgnore
    public CaseData defaultJudgmentDocuments(Object defaultJudgmentDocuments) {
        setFieldValue("defaultJudgmentDocuments", defaultJudgmentDocuments);
        return this;
    }

    @JsonIgnore
    public CaseData defendantResponseDocuments(Object defendantResponseDocuments) {
        setFieldValue("defendantResponseDocuments", defendantResponseDocuments);
        return this;
    }

    @JsonIgnore
    public CaseData duplicateClaimantDefendantResponseDocs(Object duplicateClaimantDefendantResponseDocs) {
        setFieldValue("duplicateClaimantDefendantResponseDocs", duplicateClaimantDefendantResponseDocs);
        return this;
    }

    @JsonIgnore
    public CaseData duplicateSystemGeneratedCaseDocs(Object duplicateSystemGeneratedCaseDocs) {
        setFieldValue("duplicateSystemGeneratedCaseDocs", duplicateSystemGeneratedCaseDocs);
        return this;
    }

    @JsonIgnore
    public CaseData finalOrderDocumentCollection(Object finalOrderDocumentCollection) {
        setFieldValue("finalOrderDocumentCollection", finalOrderDocumentCollection);
        return this;
    }

    @JsonIgnore
    public CaseData generalAppRespondentSolicitors(Object generalAppRespondentSolicitors) {
        setFieldValue("generalAppRespondentSolicitors", generalAppRespondentSolicitors);
        return this;
    }

    @JsonIgnore
    public CaseData generalApplications(Object generalApplications) {
        setFieldValue("generalApplications", generalApplications);
        return this;
    }

    @JsonIgnore
    public CaseData hearingDocuments(Object hearingDocuments) {
        setFieldValue("hearingDocuments", hearingDocuments);
        return this;
    }

    @JsonIgnore
    public CaseData hearingDocumentsWelsh(Object hearingDocumentsWelsh) {
        setFieldValue("hearingDocumentsWelsh", hearingDocumentsWelsh);
        return this;
    }

    @JsonIgnore
    public CaseData preTranslationDocuments(Object preTranslationDocuments) {
        setFieldValue("preTranslationDocuments", preTranslationDocuments);
        return this;
    }

    @JsonIgnore
    public CaseData queryDocuments(Object queryDocuments) {
        setFieldValue("queryDocuments", queryDocuments);
        return this;
    }

    @JsonIgnore
    public CaseData showConditionFlags(Object showConditionFlags) {
        setFieldValue("showConditionFlags", showConditionFlags);
        return this;
    }

    @JsonIgnore
    public CaseData systemGeneratedCaseDocuments(Object systemGeneratedCaseDocuments) {
        setFieldValue("systemGeneratedCaseDocuments", systemGeneratedCaseDocuments);
        return this;
    }

    @JsonIgnore
    public CaseData trialReadyDocuments(Object trialReadyDocuments) {
        setFieldValue("trialReadyDocuments", trialReadyDocuments);
        return this;
    }

    public RespondentResponseTypeSpec getRespondent1ClaimResponseTypeForSpec() {

        if (respondent1ClaimResponseTypeForSpec == null) {
            return getRespondent1ClaimResponseTestForSpec();
        } else {
            return respondent1ClaimResponseTypeForSpec;
        }
    }

    public RespondentResponseTypeSpec getRespondent2ClaimResponseTypeForSpec() {

        if (respondent2ClaimResponseTypeForSpec == null) {
            return getRespondent2ClaimResponseTestForSpec();
        } else {
            return respondent2ClaimResponseTypeForSpec;
        }
    }

    private  RespondentResponseTypeSpec respondent1ClaimResponseTypeForSpec;
    private  RespondentResponseTypeSpec respondent2ClaimResponseTypeForSpec;
    private  RespondentResponseTypeSpec claimant1ClaimResponseTypeForSpec;
    private  RespondentResponseTypeSpec claimant2ClaimResponseTypeForSpec;
    private  RespondentResponseTypeSpecPaidStatus respondent1ClaimResponsePaymentAdmissionForSpec;
    private  RespondentResponsePartAdmissionPaymentTimeLRspec defenceAdmitPartPaymentTimeRouteRequired;
    private  String defenceRouteRequired;
    private  String responseClaimTrack;
    private  RespondToClaim respondToClaim;
    private  RespondToClaim respondToAdmittedClaim;
    /**
     * money amount in pence.
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private  BigDecimal respondToAdmittedClaimOwingAmount;
    /**
     * money amount in pounds.
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private  BigDecimal respondToAdmittedClaimOwingAmountPounds;
    private  YesOrNo specDefenceFullAdmittedRequired;
    private  PaymentUponCourtOrder respondent1CourtOrderPayment;
    private  RepaymentPlanLRspec respondent1RepaymentPlan;
    private  RespondToClaimAdmitPartLRspec respondToClaimAdmitPartLRspec;
    private  UnemployedComplexTypeLRspec respondToClaimAdmitPartUnemployedLRspec;
    private  Respondent1EmployerDetailsLRspec responseClaimAdmitPartEmployer;
    private  String responseToClaimAdmitPartWhyNotPayLRspec;
    // Fields related to ROC-9453 & ROC-9455
    private  YesOrNo responseClaimMediationSpecRequired;
    private  SmallClaimMedicalLRspec applicant1ClaimMediationSpecRequired;
    private  YesOrNo defenceAdmitPartEmploymentTypeRequired;
    private  YesOrNo responseClaimExpertSpecRequired;
    private  YesOrNo applicant1ClaimExpertSpecRequired;
    private  String responseClaimWitnesses;
    private  String applicant1ClaimWitnesses;
    private  YesOrNo smallClaimHearingInterpreterRequired;
    private  String smallClaimHearingInterpreterDescription;
    private  List<EmploymentTypeCheckboxFixedListLRspec> respondToClaimAdmitPartEmploymentTypeLRspec;
    private  YesOrNo specDefenceAdmittedRequired;

    private  MediationContactInformation app1MediationContactInfo;
    private  MediationAvailability app1MediationAvailability;
    private  MediationContactInformation resp1MediationContactInfo;
    private  MediationContactInformation resp2MediationContactInfo;
    private  MediationAvailability resp1MediationAvailability;
    private  MediationAvailability resp2MediationAvailability;

    private String additionalInformationForJudge;
    private String applicantAdditionalInformationForJudge;
    @JsonUnwrapped
    private  ExpertRequirements respondToClaimExperts;

    private  String caseNote;
    private  List<Element<CaseNote>> caseNotes;

    private  String notificationSummary;

    @Valid
    private  CloseClaim withdrawClaim;

    @Valid
    private  CloseClaim discontinueClaim;

    private  BusinessProcess businessProcess;

    @JsonUnwrapped
    private  Respondent1DQ respondent1DQ;

    @JsonUnwrapped
    private  Respondent2DQ respondent2DQ;

    @JsonUnwrapped
    private  Applicant1DQ applicant1DQ;

    @JsonUnwrapped
    private  Applicant2DQ applicant2DQ;

    public boolean hasNoOngoingBusinessProcess() {
        return businessProcess == null
            || businessProcess.getStatus() == null
            || businessProcess.getStatus() == FINISHED;
    }

    private  LitigationFriend genericLitigationFriend;
    private  LitigationFriend respondent1LitigationFriend;
    private  LitigationFriend respondent2LitigationFriend;

    private  YesOrNo applicant1LitigationFriendRequired;
    private  LitigationFriend applicant1LitigationFriend;

    private  YesOrNo applicant2LitigationFriendRequired;
    private  LitigationFriend applicant2LitigationFriend;

    private  DynamicList defendantSolicitorNotifyClaimOptions;
    private  DynamicList defendantSolicitorNotifyClaimDetailsOptions;
    private  DynamicList selectLitigationFriend;
    private  String litigantFriendSelection;
    @Valid
    private  ClaimProceedsInCaseman claimProceedsInCaseman;
    @Valid
    private  ClaimProceedsInCasemanLR claimProceedsInCasemanLR;

    //CCD UI flag
    private  YesOrNo applicantSolicitor1PbaAccountsIsEmpty;
    private MultiPartyResponseTypeFlags multiPartyResponseTypeFlags;
    private YesOrNo applicantsProceedIntention;
    private  MultiPartyScenario claimantResponseScenarioFlag;
    private YesOrNo claimantResponseDocumentToDefendant2Flag;
    private YesOrNo claimant2ResponseFlag;
    private RespondentResponseTypeSpec atLeastOneClaimResponseTypeForSpecIsFullDefence;
    // used only in 2v1
    private YesOrNo specFullAdmissionOrPartAdmission;
    private YesOrNo sameSolicitorSameResponse;
    private YesOrNo specPaidLessAmountOrDisputesOrPartAdmission;
    private YesOrNo specFullDefenceOrPartAdmission1V1;
    private YesOrNo specFullDefenceOrPartAdmission;
    private YesOrNo specDisputesOrPartAdmission;
    private YesOrNo specPartAdmitPaid;
    private YesOrNo specFullAdmitPaid;

    // dates
    private  LocalDateTime submittedDate;
    private  LocalDateTime paymentSuccessfulDate;
    private  LocalDate issueDate;
    private  LocalDateTime claimNotificationDeadline;
    private  LocalDateTime claimNotificationDate;
    private  LocalDateTime claimDetailsNotificationDeadline;
    private  LocalDateTime claimDetailsNotificationDate;
    private  LocalDateTime respondent1ResponseDeadline;
    private  LocalDateTime respondent2ResponseDeadline;
    private  LocalDateTime addLegalRepDeadlineRes1;
    private  LocalDateTime addLegalRepDeadlineRes2;
    private  LocalDateTime claimDismissedDeadline;
    private  LocalDateTime respondent1TimeExtensionDate;
    private  LocalDateTime respondent2TimeExtensionDate;
    private  LocalDateTime respondent1AcknowledgeNotificationDate;
    private  LocalDateTime respondent2AcknowledgeNotificationDate;
    private  LocalDateTime respondent1ResponseDate;
    private  LocalDateTime respondent2ResponseDate;
    private  LocalDateTime applicant1ResponseDeadline;
    private  LocalDateTime applicant1ResponseDate;
    private  LocalDateTime applicant2ResponseDate;
    private  LocalDateTime takenOfflineDate;
    private  LocalDateTime takenOfflineByStaffDate;
    private  LocalDateTime unsuitableSDODate;
    private  OtherDetails otherDetails;
    private  LocalDateTime claimDismissedDate;
    private  String claimAmountBreakupSummaryObject;
    private  LocalDateTime respondent1LitigationFriendDate;
    private  LocalDateTime respondent2LitigationFriendDate;
    private  LocalDateTime respondent1RespondToSettlementAgreementDeadline;
    private  YesOrNo respondent1ResponseDeadlineChecked;
    private  String paymentTypePBA;
    private  String paymentTypePBASpec;
    private  String whenToBePaidText;

    private  LocalDateTime respondent1LitigationFriendCreatedDate;
    private  LocalDateTime respondent2LitigationFriendCreatedDate;

    private  List<IdValue<Bundle>> caseBundles = new ArrayList<>();

    private  Respondent1DebtLRspec specDefendant1Debts;
    private  Respondent1SelfEmploymentLRspec specDefendant1SelfEmploymentDetails;
    private  String detailsOfDirection;

    private  HearingSupportRequirementsDJ hearingSupportRequirementsDJ;
    private  CaseLocationCivil caseManagementLocation;
    private  CaseManagementCategory caseManagementCategory;
    private  String locationName;
    private  DynamicList defendantDetailsSpec;
    private  DynamicList defendantDetails;
    private  String bothDefendants;
    private  String bothDefendantsSpec;
    private  String partialPaymentAmount;
    private  YesOrNo partialPayment;
    private  LocalDate paymentSetDate;
    private  String repaymentSummaryObject;
    private  YesOrNo paymentConfirmationDecisionSpec;
    private  String repaymentDue;
    private  String repaymentSuggestion;
    private  String currentDatebox;
    private  LocalDate repaymentDate;
    private  String caseNameHmctsInternal;
    private  String caseNamePublic;
    private  YesOrNo ccjJudgmentAmountShowInterest;
    private  YesOrNo claimFixedCostsExist;
    private  YesOrNo partAdmit1v1Defendant;

    private  List<Element<CaseDocument>> defaultJudgmentDocuments = new ArrayList<>();

    private  String hearingSelection;

    private  YesOrNo isRespondent1;
    private  YesOrNo isRespondent2;
    private  YesOrNo isApplicant1;
    private  YesOrNo disabilityPremiumPayments;
    private  YesOrNo severeDisabilityPremiumPayments;

    private  String currentDefendant;
    private  YesOrNo claimStarted;
    private  String currentDefendantName;

    @JsonUnwrapped
    private  BreathingSpaceInfo breathing;
    private  String applicantVRespondentText;

    private YesOrNo setRequestDJDamagesFlagForWA;
    private String featureToggleWA;

    private ContactDetailsUpdatedEvent contactDetailsUpdatedEvent;

    /**
     * RTJ = Refer To Judge.
     */
    private  String eventDescriptionRTJ;
    /**
     * RTJ = Refer To Judge.
     */
    private  String additionalInformationRTJ;
    /**
     * Refer To Judge(Defence received in time).
     */
    private List<ConfirmationToggle> confirmReferToJudgeDefenceReceived;

    //general application order documents
    private  List<Element<CaseDocument>> generalOrderDocument;
    private  List<Element<CaseDocument>> generalOrderDocStaff;
    private  List<Element<CaseDocument>> generalOrderDocClaimant;
    private  List<Element<CaseDocument>> generalOrderDocRespondentSol;
    private  List<Element<CaseDocument>> generalOrderDocRespondentSolTwo;

    private  List<Element<CaseDocument>> consentOrderDocument;
    private  List<Element<CaseDocument>> consentOrderDocStaff;
    private  List<Element<CaseDocument>> consentOrderDocClaimant;
    private  List<Element<CaseDocument>> consentOrderDocRespondentSol;
    private  List<Element<CaseDocument>> consentOrderDocRespondentSolTwo;

    private  List<Element<Document>> generalAppEvidenceDocument;

    private  List<Element<Document>> gaEvidenceDocStaff;
    private  List<Element<Document>> gaEvidenceDocClaimant;
    private  List<Element<Document>> gaEvidenceDocRespondentSol;
    private  List<Element<Document>> gaEvidenceDocRespondentSolTwo;
    private  List<Element<CaseDocument>> gaAddlDoc;
    private  List<Element<CaseDocument>> gaAddlDocStaff;
    private  List<Element<CaseDocument>> gaAddlDocClaimant;
    private  List<Element<CaseDocument>> gaAddlDocRespondentSol;
    private  List<Element<CaseDocument>> gaAddlDocRespondentSolTwo;
    private  List<Element<CaseDocument>> gaAddlDocBundle;
    private  List<Element<CaseDocument>> gaDraftDocument;
    private  List<Element<CaseDocument>> gaDraftDocStaff;
    private  List<Element<CaseDocument>> gaDraftDocClaimant;
    private  List<Element<CaseDocument>> gaDraftDocRespondentSol;
    private  List<Element<CaseDocument>> gaDraftDocRespondentSolTwo;

    private  List<Element<CaseDocument>> gaRespondDoc;
    private  List<Element<CaseDocument>> preTranslationGaDocsApplicant;
    private  List<Element<CaseDocument>> preTranslationGaDocsRespondent;
    private  List<Element<CaseDocument>> hearingDocuments = new ArrayList<>();

    private  List<Element<CaseDocument>> hearingDocumentsWelsh = new ArrayList<>();

    // GA for LIP
    private  YesOrNo isGaApplicantLip;
    private  YesOrNo isGaRespondentOneLip;
    private  YesOrNo isGaRespondentTwoLip;

    private List<DocumentToKeepCollection> documentToKeepCollection;

    private RequestedCourtForTabDetails requestedCourtForTabDetailsApp;
    private RequestedCourtForTabDetails requestedCourtForTabDetailsRes1;
    private RequestedCourtForTabDetails requestedCourtForTabDetailsRes2;

    private  ChangeLanguagePreference changeLanguagePreference;
    private  PreferredLanguage claimantLanguagePreferenceDisplay;
    private  PreferredLanguage defendantLanguagePreferenceDisplay;

    private  List<Element<CaseDocument>> queryDocuments = new ArrayList<>();

    private  PreTranslationDocumentType preTranslationDocumentType;
    private  YesOrNo bilingualHint;
    private  CaseDocument respondent1OriginalDqDoc;

    private  YesOrNo isMintiLipCase;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private  String  smallClaimsPenalNotice;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private  String  fastTrackPenalNotice;

    private  List<Element<CaseDocument>> courtOfficersOrders = new ArrayList<>();
    private  YesOrNo isReferToJudgeClaim;
    private  YesOrNo enableUploadEvent;

    private  ClientContext clientContext;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private  YesOrNo isClaimDeclarationAdded;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private  String  claimDeclarationDescription;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private  YesOrNo isHumanRightsActIssues;

    /**
     * There are several fields that can hold the I2P of applicant1 depending
     * on multiparty scenario, which complicates all conditions depending on it.
     * This method tries to simplify those conditions since only one field will be
     * meaningful for that.
     *
     * @return value set among the fields that hold the I2P of applicant1
     */
    @JsonIgnore
    public YesOrNo getApplicant1ProceedsWithClaimSpec() {
        return Stream.of(
                applicant1ProceedWithClaim,
                getApplicant1ProceedWithClaimSpec2v1()
            )
            .filter(Objects::nonNull)
            .findFirst().orElse(null);
    }

    public YesOrNo getRespondent1Represented() {
        return Stream.of(
                respondent1Represented,
                specRespondent1Represented
            )
            .filter(Objects::nonNull)
            .findFirst().orElse(null);
    }

    @JsonIgnore
    public boolean isRespondent1LiP() {
        return YesOrNo.NO.equals(getRespondent1Represented());
    }

    @JsonIgnore
    public boolean isApplicantLiP() {
        return YesOrNo.NO == getApplicant1Represented();
    }

    public YesOrNo getRespondent2Represented() {
        return Stream.of(
                respondent2Represented,
                specRespondent2Represented
            )
            .filter(Objects::nonNull)
            .findFirst().orElse(null);
    }

    @JsonIgnore
    public boolean isRespondent2LiP() {
        return YesOrNo.NO == getRespondent2Represented();
    }

    @JsonIgnore
    public boolean respondent1PaidInFull() {
        return respondent1ClaimResponsePaymentAdmissionForSpec
            == RespondentResponseTypeSpecPaidStatus.PAID_FULL_OR_MORE_THAN_CLAIMED_AMOUNT;
    }

    @JsonIgnore
    public boolean isPayBySetDate() {
        return defenceAdmitPartPaymentTimeRouteRequired != null
            && defenceAdmitPartPaymentTimeRouteRequired == RespondentResponsePartAdmissionPaymentTimeLRspec.BY_SET_DATE;
    }

    @JsonIgnore
    public boolean isPayByInstallment() {
        return defenceAdmitPartPaymentTimeRouteRequired != null
            && defenceAdmitPartPaymentTimeRouteRequired
            == RespondentResponsePartAdmissionPaymentTimeLRspec.SUGGESTION_OF_REPAYMENT_PLAN;
    }

    @JsonIgnore
    public boolean isPayImmediately() {
        return RespondentResponsePartAdmissionPaymentTimeLRspec.IMMEDIATELY.equals(getDefenceAdmitPartPaymentTimeRouteRequired());
    }

    @JsonIgnore
    public boolean hasDefendantPaidTheAmountClaimed() {
        return SpecJourneyConstantLRSpec.HAS_PAID_THE_AMOUNT_CLAIMED
            .equals(getDefenceRouteRequired());
    }

    @JsonIgnore
    public boolean isPaidFullAmount() {
        RespondToClaim localRespondToClaim = null;
        if (getRespondent1ClaimResponseTypeForSpec() == FULL_DEFENCE) {
            localRespondToClaim = getRespondToClaim();
        } else if (getRespondent1ClaimResponseTypeForSpec() == PART_ADMISSION) {
            localRespondToClaim = getRespondToAdmittedClaim();
        }

        return ofNullable(localRespondToClaim)
            .map(RespondToClaim::getHowMuchWasPaid)
            .map(amount -> MonetaryConversions.penniesToPounds(amount).compareTo(totalClaimAmount) >= 0)
            .orElse(false);
    }

    @JsonIgnore
    public boolean isClaimBeingDisputed() {
        return SpecJourneyConstantLRSpec.DISPUTES_THE_CLAIM
            .equals(getDefenceRouteRequired());
    }

    @JsonIgnore
    public LocalDate getDateForRepayment() {
        return ofNullable(respondToClaimAdmitPartLRspec)
            .map(RespondToClaimAdmitPartLRspec::getWhenWillThisAmountBePaid).orElse(null);
    }

    @JsonIgnore
    public boolean hasBreathingSpace() {
        return getBreathing() != null
            && getBreathing().getEnter() != null
            && getBreathing().getLift() == null;
    }

    @JsonIgnore
    public boolean isDefendantPaymentPlanYes() {
        Set<RespondentResponsePartAdmissionPaymentTimeLRspec> paymentPlan = EnumSet.of(
            RespondentResponsePartAdmissionPaymentTimeLRspec.SUGGESTION_OF_REPAYMENT_PLAN,
            RespondentResponsePartAdmissionPaymentTimeLRspec.BY_SET_DATE
        );

        return hasApplicantAcceptedRepaymentPlan()
            || !paymentPlan.contains(getDefenceAdmitPartPaymentTimeRouteRequired())
            || isClaimantNotSettlePartAdmitClaim();
    }

    @JsonIgnore
    public boolean isDefendantPaymentPlanNo() {
        Set<RespondentResponsePartAdmissionPaymentTimeLRspec> paymentPlan = EnumSet.of(
            RespondentResponsePartAdmissionPaymentTimeLRspec.SUGGESTION_OF_REPAYMENT_PLAN,
            RespondentResponsePartAdmissionPaymentTimeLRspec.BY_SET_DATE
        );

        return hasApplicantRejectedRepaymentPlan()
            || !paymentPlan.contains(getDefenceAdmitPartPaymentTimeRouteRequired())
            || isClaimantNotSettlePartAdmitClaim();
    }

    @JsonIgnore
    public boolean isPartAdmitClaimSettled() {
        return (
            getApplicant1ProceedsWithClaimSpec() == null
                && isPartAdmitClaimSpec()
                && isClaimantIntentionSettlePartAdmit()
                && isClaimantConfirmAmountPaidPartAdmit());
    }

    @JsonIgnore
    public boolean isPartAdmitClaimNotSettled() {
        return (
            getApplicant1ProceedsWithClaimSpec() != null
                || getApplicant1AcceptAdmitAmountPaidSpec() != null
                || isClaimantIntentionNotSettlePartAdmit()
                || isClaimantConfirmAmountNotPaidPartAdmit());
    }

    @JsonIgnore
    public boolean hasDefendantNotPaid() {
        return NO.equals(getApplicant1PartAdmitConfirmAmountPaidSpec());
    }

    @JsonIgnore
    public boolean isSettlementDeclinedByClaimant() {
        return NO.equals(getApplicant1PartAdmitIntentionToSettleClaimSpec());
    }

    @JsonIgnore
    public boolean isClaimantAcceptedClaimAmount() {
        return YES.equals(getApplicant1AcceptAdmitAmountPaidSpec());
    }

    @JsonIgnore
    public boolean isClaimantRejectsClaimAmount() {
        return NO.equals(getApplicant1AcceptAdmitAmountPaidSpec());
    }

    @JsonIgnore
    public boolean isPartAdmitAlreadyPaid() {
        return YES.equals(getSpecDefenceAdmittedRequired());
    }

    @JsonIgnore
    public boolean isFullDefence() {
        return YES.equals(getApplicant1ProceedWithClaim());
    }

    @JsonIgnore
    public boolean hasDefendantAgreedToFreeMediation() {
        return YES.equals(getResponseClaimMediationSpecRequired());
    }

    @JsonIgnore
    public boolean hasDefendant2AgreedToFreeMediation() {
        return YES.equals(getResponseClaimMediationSpec2Required());
    }

    @JsonIgnore
    public boolean isMultiPartyDefendant() {
        return !YES.equals(getDefendantSingleResponseToBothClaimants())
            && YES.equals(getApplicant1ProceedWithClaim());
    }

    @JsonIgnore
    public boolean isMultiPartyClaimant(MultiPartyScenario multiPartyScenario) {
        return multiPartyScenario.equals(TWO_V_ONE)
            && YES.equals(getDefendantSingleResponseToBothClaimants())
            && YES.equals(getApplicant1ProceedWithClaimSpec2v1());
    }

    @JsonIgnore
    public boolean isPaidSomeAmountMoreThanClaimAmount() {
        return getCcjPaymentDetails().getCcjPaymentPaidSomeAmount() != null
            && getCcjPaymentDetails().getCcjPaymentPaidSomeAmount()
            .compareTo(new BigDecimal(MonetaryConversions.poundsToPennies(getTotalClaimAmount()))) > 0;
    }

    @JsonIgnore
    public boolean hasApplicantProceededWithClaim() {
        return YES == getApplicant1ProceedWithClaim()
            || YES == getApplicant1ProceedWithClaimSpec2v1()
            || NO.equals(getApplicant1AcceptAdmitAmountPaidSpec())
            || NO.equals(getApplicant1PartAdmitConfirmAmountPaidSpec())
            || NO.equals(getApplicant1PartAdmitIntentionToSettleClaimSpec());
    }

    @JsonIgnore
    public boolean hasApplicantNotProceededWithClaim() {
        return Objects.nonNull(getApplicant1ProceedWithClaim()) && NO == getApplicant1ProceedWithClaim();
    }

    @JsonIgnore
    public boolean isRespondentResponseFullDefence() {
        return (RespondentResponseTypeSpec.FULL_DEFENCE.equals(getRespondent1ClaimResponseTypeForSpec())
            && !isOneVTwoTwoLegalRep(this))
            || (RespondentResponseTypeSpec.FULL_DEFENCE.equals(getRespondent1ClaimResponseTypeForSpec())
            && RespondentResponseTypeSpec.FULL_DEFENCE.equals(getRespondent2ClaimResponseTypeForSpec()));
    }

    @JsonIgnore
    public boolean hasApplicantAcceptedRepaymentPlan() {
        return YES.equals(getApplicant1AcceptFullAdmitPaymentPlanSpec())
            || YES.equals(getApplicant1AcceptPartAdmitPaymentPlanSpec());
    }

    @JsonIgnore
    public boolean hasApplicantRejectedRepaymentPlan() {
        return NO.equals(getApplicant1AcceptFullAdmitPaymentPlanSpec())
            || NO.equals(getApplicant1AcceptPartAdmitPaymentPlanSpec());
    }

    @JsonIgnore
    public boolean isAcceptDefendantPaymentPlanForPartAdmitYes() {
        return YesOrNo.YES.equals(getApplicant1AcceptPartAdmitPaymentPlanSpec());
    }

    @JsonIgnore
    public boolean isRespondent1NotRepresented() {
        return NO.equals(getRespondent1Represented());
    }

    @JsonIgnore
    public boolean isRespondent2NotRepresented() {
        return NO.equals(Stream.of(
                respondent2Represented,
                specRespondent2Represented
            )
            .filter(Objects::nonNull)
            .findFirst().orElse(null));
    }

    @JsonIgnore
    public boolean isApplicant1NotRepresented() {
        return NO.equals(getApplicant1Represented());
    }

    @JsonIgnore
    public boolean isLRvLipOneVOne() {
        return isRespondent1LiP()
            && !isApplicant1NotRepresented()
            && isOneVOne(this);
    }

    @JsonIgnore
    public boolean isLipvLipOneVOne() {
        return isRespondent1LiP()
            && isApplicant1NotRepresented()
            && isOneVOne(this);
    }

    @JsonIgnore
    public boolean isApplicantLipOneVOne() {
        return isApplicant1NotRepresented() && isOneVOne(this);
    }

    @JsonIgnore
    public boolean isJudgementDateNotPermitted() {
        LocalDate whenWillThisAmountBePaid = null;
        LocalDate firstRepaymentDate;
        if (hasApplicant1CourtDecisionInFavourOfClaimant()) {
            if (applicant1SuggestedPayImmediately()) {
                whenWillThisAmountBePaid = getApplicant1SuggestPayImmediatelyPaymentDateForDefendantSpec();
            } else if (applicant1SuggestedPayBySetDate()) {
                whenWillThisAmountBePaid = ofNullable(getApplicant1RequestedPaymentDateForDefendantSpec()).map(
                    PaymentBySetDate::getPaymentSetDate).orElse(null);
            }
            firstRepaymentDate = getApplicant1SuggestInstalmentsFirstRepaymentDateForDefendantSpec();
        } else {
            whenWillThisAmountBePaid =
                ofNullable(getRespondToClaimAdmitPartLRspec()).map(RespondToClaimAdmitPartLRspec::getWhenWillThisAmountBePaid).orElse(
                    null);
            firstRepaymentDate = ofNullable(getRespondent1RepaymentPlan()).map(RepaymentPlanLRspec::getFirstRepaymentDate).orElse(
                null);
        }
        LocalDate respondentSettlementAgreementDeadline = ofNullable(
            getRespondent1RespondToSettlementAgreementDeadline()).map(LocalDateTime::toLocalDate).orElse(null);
        Optional<CaseDataLiP> optionalCaseDataLiP = ofNullable(getCaseDataLiP());
        YesOrNo hasDoneSettlementAgreement = optionalCaseDataLiP.map(CaseDataLiP::getRespondentSignSettlementAgreement).orElse(
            null);
        boolean hasDoneSettlementAgreementInTime = (nonNull(hasDoneSettlementAgreement) && hasDoneSettlementAgreement == YesOrNo.YES)
            || (isNull(hasDoneSettlementAgreement) && isDateAfterToday(respondentSettlementAgreementDeadline));

        return (isNull(whenWillThisAmountBePaid) && isNull(firstRepaymentDate))
            || (isDateAfterToday(whenWillThisAmountBePaid) && hasDoneSettlementAgreementInTime)
            || (isDateAfterToday(firstRepaymentDate) && hasDoneSettlementAgreementInTime)
            || (isDateAfterToday(whenWillThisAmountBePaid) && isFullAdmitPayImmediatelyClaimSpec());

    }

    @JsonIgnore
    public String getFormattedJudgementPermittedDate(LocalDate extendedRespondent1ResponseDate) {
        return formatLocalDateTime(
            extendedRespondent1ResponseDate.atTime(DeadlinesCalculator.END_OF_BUSINESS_DAY),
            DATE_TIME_AT
        );
    }

    @JsonIgnore
    public boolean isPartAdmitClaimSpec() {
        return PART_ADMISSION.equals(getRespondent1ClaimResponseTypeForSpec());
    }

    @JsonIgnore
    public boolean isPartAdmitImmediatePaymentClaimSettled() {
        return (isPartAdmitClaimSpec()
            && (Objects.nonNull(getApplicant1AcceptAdmitAmountPaidSpec())
            && YesOrNo.YES.equals(getApplicant1AcceptAdmitAmountPaidSpec()))
            && (Objects.isNull(getApplicant1AcceptPartAdmitPaymentPlanSpec())));
    }

    @JsonIgnore
    public boolean isFullAdmitClaimSpec() {
        return FULL_ADMISSION.equals(getRespondent1ClaimResponseTypeForSpec());
    }

    @JsonIgnore
    public boolean isFullAdmitPayImmediatelyClaimSpec() {
        return isFullAdmitClaimSpec() && isPayImmediately();
    }

    @JsonIgnore
    public boolean isPartAdmitPayImmediatelyClaimSpec() {
        return isPartAdmitClaimSpec() && isPayImmediately();
    }

    @JsonIgnore
    public boolean isClaimantIntentionSettlePartAdmit() {
        return YesOrNo.YES.equals(getApplicant1PartAdmitIntentionToSettleClaimSpec());
    }

    @JsonIgnore
    public boolean isClaimantIntentionNotSettlePartAdmit() {
        return YesOrNo.NO.equals(getApplicant1PartAdmitIntentionToSettleClaimSpec());
    }

    @JsonIgnore
    public boolean isClaimantConfirmAmountPaidPartAdmit() {
        return YesOrNo.YES.equals(getApplicant1PartAdmitConfirmAmountPaidSpec());
    }

    @JsonIgnore
    public boolean isClaimantConfirmAmountNotPaidPartAdmit() {
        return YesOrNo.NO.equals(getApplicant1PartAdmitConfirmAmountPaidSpec());
    }

    @JsonIgnore
    public YesOrNo doesPartPaymentRejectedOrItsFullDefenceResponse() {
        if (isClaimantNotSettlePartAdmitClaim()
            || (RespondentResponseTypeSpec.FULL_DEFENCE.equals(getRespondent1ClaimResponseTypeForSpec())
            && !(NO.equals(getApplicant1ProceedWithClaim()))
            && !(NO.equals(getApplicant1ProceedWithClaimSpec2v1())))) {
            return YES;
        }
        return NO;
    }

    @JsonIgnore
    public boolean isClaimantNotSettlePartAdmitClaim() {
        return hasDefendantNotPaid()
            || isSettlementDeclinedByClaimant()
            || isClaimantRejectsClaimAmount();
    }

    @JsonIgnore
    public boolean hasDefendantNotAgreedToFreeMediation() {
        return NO.equals(getResponseClaimMediationSpecRequired());
    }

    @JsonIgnore
    public boolean isFastTrackClaim() {
        return FAST_CLAIM.name().equals(getResponseClaimTrack());
    }

    @JsonIgnore
    public boolean isSmallClaim() {
        return SMALL_CLAIM.name().equals(getResponseClaimTrack());
    }

    @JsonIgnore
    public boolean isRejectWithNoMediation() {
        return isClaimantNotSettlePartAdmitClaim()
            && isMediationRejectedOrFastTrack();
    }

    @JsonIgnore
    public boolean isMediationRejectedOrFastTrack() {
        return ((hasClaimantNotAgreedToFreeMediation()
            || hasDefendantNotAgreedToFreeMediation())
            || isFastTrackClaim());
    }

    @JsonIgnore
    public String getApplicantOrganisationId() {
        return getOrganisationId(ofNullable(getApplicant1OrganisationPolicy()));
    }

    @JsonIgnore
    public String getRespondent1OrganisationId() {
        return getOrganisationId(ofNullable(getRespondent1OrganisationPolicy()));
    }

    @JsonIgnore
    public String getRespondent2OrganisationId() {
        return getOrganisationId(ofNullable(getRespondent2OrganisationPolicy()));
    }

    @JsonIgnore
    private String getOrganisationId(Optional<OrganisationPolicy> policy) {
        return policy
            .map(OrganisationPolicy::getOrganisation)
            .map(Organisation::getOrganisationID)
            .orElse("");
    }

    @JsonIgnore
    public boolean isPartAdmitPayImmediatelyAccepted() {
        return SPEC_CLAIM.equals(getCaseAccessCategory())
            && YES.equals(getApplicant1AcceptAdmitAmountPaidSpec())
            && (getShowResponseOneVOneFlag() != null
            && getShowResponseOneVOneFlag().equals(ResponseOneVOneShowTag.ONE_V_ONE_PART_ADMIT_PAY_IMMEDIATELY));
    }

    @JsonIgnore
    public Optional<Element<CaseDocument>> getSDODocument() {
        return getLatestDocumentOfType(DocumentType.SDO_ORDER);
    }

    @JsonIgnore
    public Optional<Element<CaseDocument>> getHiddenSDODocument() {
        return getLatestHiddenDocumentOfType(DocumentType.SDO_ORDER);
    }

    @JsonIgnore
    public Optional<Element<CaseDocument>> getTranslatedSDODocument() {
        return getLatestDocumentOfType(DocumentType.SDO_TRANSLATED_DOCUMENT);
    }

    @JsonIgnore
    public Optional<Element<CaseDocument>> getDecisionReconsiderationDocument() {
        return getLatestDocumentOfType(DocumentType.DECISION_MADE_ON_APPLICATIONS);
    }

    @JsonIgnore
    public Optional<Element<CaseDocument>> getTranslatedDecisionReconsiderationDocument() {
        return getLatestDocumentOfType(DocumentType.DECISION_MADE_ON_APPLICATIONS_TRANSLATED);
    }

    @JsonIgnore
    public Optional<Element<CaseDocument>> getLatestDocumentOfType(DocumentType documentType) {
        return ofNullable(systemGeneratedCaseDocuments)
            .flatMap(docs -> docs.stream()
                .filter(doc -> doc.getValue().getDocumentType().equals(documentType))
                .max(Comparator.comparing(doc -> doc.getValue().getCreatedDatetime())));
    }

    @JsonIgnore
    public Optional<Element<CaseDocument>> getLatestHiddenDocumentOfType(DocumentType documentType) {
        return ofNullable(preTranslationDocuments)
            .flatMap(docs -> docs.stream()
                .filter(doc -> doc.getValue().getDocumentType().equals(documentType))
                .max(Comparator.comparing(doc -> doc.getValue().getCreatedDatetime())));
    }

    @JsonIgnore
    public Optional<List<CaseDocument>> getDocumentListByType(List<Element<CaseDocument>> documentCollection, DocumentType documentType) {
        List<CaseDocument> documents = documentCollection.stream()
            .map(Element::getValue)
            .filter(doc -> doc.getDocumentType().equals(documentType))
            .toList();
        return ofNullable(documents.isEmpty() ? null : documents);
    }

    @JsonIgnore
    public Optional<Element<CaseDocument>> getDecisionOnReconsiderationDocumentFromList() {
        if (getSystemGeneratedCaseDocuments() != null) {
            return getSystemGeneratedCaseDocuments().stream()
                .filter(systemGeneratedCaseDocument -> systemGeneratedCaseDocument.getValue()
                    .getDocumentType().equals(DocumentType.DECISION_MADE_ON_APPLICATIONS)).findAny();
        }
        return empty();
    }

    @JsonIgnore
    public boolean isCcjRequestJudgmentByAdmission() {
        return getCcjPaymentDetails() != null
            && getCcjPaymentDetails().getCcjPaymentPaidSomeOption() != null;
    }

    @JsonIgnore
    public boolean hasCoscCert() {
        if (getSystemGeneratedCaseDocuments() != null) {
            return getSystemGeneratedCaseDocuments().stream()
                .filter(systemGeneratedCaseDocument -> systemGeneratedCaseDocument.getValue()
                    .getDocumentType().equals(DocumentType.CERTIFICATE_OF_DEBT_PAYMENT)).findAny().isPresent();
        }
        return false;
    }

    @JsonIgnore
    public Address getRespondent1CorrespondanceAddress() {
        return ofNullable(getCaseDataLiP())
            .map(CaseDataLiP::getRespondent1LiPResponse)
            .map(RespondentLiPResponse::getRespondent1LiPCorrespondenceAddress)
            .orElse(null);
    }

    @JsonIgnore
    public RespondToClaim getResponseToClaim() {
        return getRespondToAdmittedClaim() != null ? getRespondToAdmittedClaim() : getRespondToClaim();
    }

    @JsonIgnore
    public List<Element<RecurringIncomeLRspec>> getRecurringIncomeForRespondent1() {
        if (isFullAdmitClaimSpec()) {
            return ofNullable(getRespondent1DQ()).map(Respondent1DQ::getRespondent1DQRecurringIncomeFA).orElse(
                null);
        }
        return ofNullable(getRespondent1DQ()).map(Respondent1DQ::getRespondent1DQRecurringIncome).orElse(null);
    }

    @JsonIgnore
    public List<Element<RecurringExpenseLRspec>> getRecurringExpensesForRespondent1() {
        if (isFullAdmitClaimSpec()) {
            return ofNullable(getRespondent1DQ()).map(Respondent1DQ::getRespondent1DQRecurringExpensesFA)
                .orElse(
                    null);
        }
        return ofNullable(getRespondent1DQ()).map(Respondent1DQ::getRespondent1DQRecurringExpenses).orElse(
            null);
    }

    @JsonIgnore
    public List<Element<ManageDocument>> getManageDocumentsList() {
        return ofNullable(getManageDocuments()).orElse(new ArrayList<>());
    }

    @JsonIgnore
    public boolean getApplicant1ResponseDeadlinePassed() {
        return getApplicant1ResponseDeadline() != null
            && getApplicant1ResponseDeadline().isBefore(LocalDateTime.now())
            && getApplicant1ProceedWithClaim() == null;
    }

    @JsonIgnore
    public String getApplicant1Email() {
        return ofNullable(getApplicant1().getPartyEmail())
            .or(() -> ofNullable(getClaimantUserDetails())
                .map(IdamUserDetails::getEmail))
            .or(() -> ofNullable(getApplicantSolicitor1UserDetails())
                .map(IdamUserDetails::getEmail))
            .orElse(null);
    }

    @JsonIgnore
    public String getHelpWithFeesReferenceNumber() {
        return ofNullable(getCaseDataLiP())
            .map(CaseDataLiP::getHelpWithFees)
            .map(HelpWithFees::getHelpWithFeesReferenceNumber).orElse(null);
    }

    @JsonProperty("helpWithFees")
    public HelpWithFees getHelpWithFees() {
        return ofNullable(getCaseDataLiP())
            .map(CaseDataLiP::getHelpWithFees)
            .orElse(null);
    }

    @JsonProperty("helpWithFees")
    public void setHelpWithFees(HelpWithFees helpWithFees) {
        CaseDataLiP caseDataLiP = ofNullable(getCaseDataLiP())
            .orElseGet(CaseDataLiP::new);
        caseDataLiP.setHelpWithFees(helpWithFees);
        setCaseDataLiP(caseDataLiP);
    }

    @Override
    public CaseData setCaseDataLiP(CaseDataLiP caseDataLiP) {
        CaseDataLiP mergedCaseDataLiP = caseDataLiP;
        CaseDataLiP existingCaseDataLiP = getCaseDataLiP();
        if (mergedCaseDataLiP != null
            && mergedCaseDataLiP.getHelpWithFees() == null
            && existingCaseDataLiP != null
            && existingCaseDataLiP.getHelpWithFees() != null) {
            mergedCaseDataLiP.setHelpWithFees(existingCaseDataLiP.getHelpWithFees());
        }
        super.setCaseDataLiP(mergedCaseDataLiP);
        return this;
    }

    @JsonIgnore
    public boolean isHelpWithFees() {
        return getCaseDataLiP() != null && getCaseDataLiP().getHelpWithFees() != null
            && YES.equals(getCaseDataLiP().getHelpWithFees().getHelpWithFee());
    }

    @JsonIgnore
    public Address getRespondent1CorrespondenceAddress() {
        return ofNullable(getCaseDataLiP())
            .map(CaseDataLiP::getRespondent1LiPResponse)
            .map(RespondentLiPResponse::getRespondent1LiPCorrespondenceAddress)
            .orElse(null);
    }

    @JsonIgnore
    public String getCurrentCamundaBusinessProcessName() {
        return ofNullable(getBusinessProcess())
            .map(BusinessProcess::getCamundaEvent)
            .orElse(null);
    }

    @JsonIgnore
    public boolean isTranslatedDocumentUploaded() {
        if (getSystemGeneratedCaseDocuments() != null) {
            return getSystemGeneratedCaseDocuments().stream()
                .filter(systemGeneratedCaseDocument -> systemGeneratedCaseDocument.getValue()
                    .getDocumentType().equals(DocumentType.DEFENCE_TRANSLATED_DOCUMENT)).findAny().isPresent();
        }
        return false;
    }

    @JsonIgnore
    public boolean isRespondentSolicitorRegistered() {
        return YesOrNo.YES.equals(getRespondent1OrgRegistered());
    }

    @JsonIgnore
    public boolean isRespondentTwoSolicitorRegistered() {
        return YesOrNo.YES.equals(getRespondent2OrgRegistered());
    }

    @JsonIgnore
    public String getRespondent1Email() {
        if (isRespondent1NotRepresented()) {
            return getRespondent1().getPartyEmail();
        }
        if (isRespondentSolicitorRegistered()) {
            return getRespondentSolicitor1EmailAddress();
        }
        return null;
    }

    @JsonIgnore
    public boolean isRespondentRespondedToSettlementAgreement() {
        return getCaseDataLiP() != null && getCaseDataLiP().getRespondentSignSettlementAgreement() != null;
    }

    @JsonIgnore
    public boolean isRespondentSignedSettlementAgreement() {
        return getCaseDataLiP() != null && YesOrNo.YES.equals(getCaseDataLiP().getRespondentSignSettlementAgreement());
    }

    @JsonIgnore
    public boolean isRespondentRejectedSettlementAgreement() {
        return getCaseDataLiP() != null && YesOrNo.NO.equals(getCaseDataLiP().getRespondentSignSettlementAgreement());
    }

    @JsonIgnore
    public List<ClaimAmountBreakupDetails> getClaimAmountBreakupDetails() {
        return ofNullable(getClaimAmountBreakup())
            .map(Collection::stream)
            .map(claimAmountBreakupStream -> claimAmountBreakupStream
                .map(item -> new ClaimAmountBreakupDetails(
                    MonetaryConversions.penniesToPounds(item.getValue().getClaimAmount()),
                    item.getValue().getClaimReason()
                ))
                .toList())
            .orElse(Collections.emptyList());

    }

    @JsonIgnore
    public BigDecimal getCalculatedClaimFeeInPence() {
        return ofNullable(getClaimFee())
            .map(Fee::getCalculatedAmountInPence)
            .orElse(BigDecimal.ZERO);
    }

    @JsonIgnore
    public BigDecimal getCalculatedHearingFeeInPence() {
        return ofNullable(getHearingFee())
            .map(Fee::getCalculatedAmountInPence)
            .orElse(BigDecimal.ZERO);
    }

    @JsonIgnore
    public BigDecimal getClaimAmountInPounds() {
        if (nonNull(getClaimValue())) {
            return getClaimValue().toPounds();
        } else if (nonNull(getTotalInterest())) {
            return getTotalClaimAmount()
                .add(getTotalInterest())
                .setScale(2, RoundingMode.UNNECESSARY);
        } else if (nonNull(getTotalClaimAmount())) {
            return getTotalClaimAmount().setScale(2, RoundingMode.UNNECESSARY);
        }
        return null;
    }

    @JsonIgnore
    public BigDecimal getClaimIssueRemissionAmount() {
        return ofNullable(getClaimIssuedHwfDetails())
            .map(HelpWithFeesDetails::getRemissionAmount)
            .orElse(BigDecimal.ZERO);
    }

    @JsonIgnore
    public BigDecimal getHearingRemissionAmount() {
        return ofNullable(getHearingHwfDetails())
            .map(HelpWithFeesDetails::getRemissionAmount)
            .orElse(BigDecimal.ZERO);
    }

    public boolean hasApplicant1SignedSettlementAgreement() {
        return ofNullable(getCaseDataLiP())
            .map(CaseDataLiP::getApplicant1LiPResponse)
            .filter(ClaimantLiPResponse::hasApplicant1SignedSettlementAgreement).isPresent();

    }

    @JsonIgnore
    public boolean isHWFTypeHearing() {
        return getHwfFeeType() == FeeType.HEARING;
    }

    @JsonIgnore
    public boolean isHWFTypeClaimIssued() {
        return getHwfFeeType() == FeeType.CLAIMISSUED;
    }

    @JsonIgnore
    public CaseEvent getHwFEvent() {
        if (this.isHWFTypeHearing() && this.getHearingHwfDetails() != null) {
            return this.getHearingHwfDetails().getHwfCaseEvent();
        }
        if (this.isHWFTypeClaimIssued() && this.getClaimIssuedHwfDetails() != null) {
            return this.getClaimIssuedHwfDetails().getHwfCaseEvent();
        }
        return null;
    }

    @JsonIgnore
    public boolean isHWFOutcomeReady() {
        return (this.getCcdState() == CaseState.PENDING_CASE_ISSUED && this.isHWFTypeClaimIssued())
            || (this.getCcdState() == CaseState.HEARING_READINESS && this.isHWFTypeHearing());
    }

    @JsonIgnore
    public String getHwFReferenceNumber() {
        if (this.isHWFTypeHearing()) {
            return this.getHearingHelpFeesReferenceNumber();
        }
        if (this.isHWFTypeClaimIssued()) {
            return this.getHelpWithFeesReferenceNumber();
        }
        return null;
    }

    @JsonIgnore
    public BigDecimal getHwFFeeAmount() {
        if (this.isHWFTypeHearing()) {
            return MonetaryConversions.penniesToPounds(this.getCalculatedHearingFeeInPence());
        }
        if (this.isHWFTypeClaimIssued()) {
            return MonetaryConversions.penniesToPounds(this.getCalculatedClaimFeeInPence());
        }
        return null;
    }

    @JsonIgnore
    public BigDecimal getRemissionAmount() {
        if (this.isHWFTypeHearing()) {
            return MonetaryConversions.penniesToPounds(this.getHearingRemissionAmount());
        }
        if (this.isHWFTypeClaimIssued()) {
            return MonetaryConversions.penniesToPounds(this.getClaimIssueRemissionAmount());
        }
        return null;
    }

    @JsonIgnore
    public BigDecimal getOutstandingFeeInPounds() {
        if (this.isHWFTypeHearing() && this.getHearingHwfDetails() != null) {
            return this.getHearingHwfDetails().getOutstandingFeeInPounds();
        }
        if (this.isHWFTypeClaimIssued() && this.getClaimIssuedHwfDetails() != null) {
            return this.getClaimIssuedHwfDetails().getOutstandingFeeInPounds();
        }
        return null;
    }

    @JsonIgnore
    public boolean isSettlementAgreementDeadlineExpired() {
        return nonNull(respondent1RespondToSettlementAgreementDeadline)
            && LocalDateTime.now().isAfter(respondent1RespondToSettlementAgreementDeadline);
    }

    @JsonIgnore
    public boolean hasApplicant1AcceptedCcj() {
        return ofNullable(getCaseDataLiP())
            .map(CaseDataLiP::getApplicant1LiPResponse)
            .filter(ClaimantLiPResponse::hasApplicant1RequestedCcj).isPresent();
    }

    @JsonIgnore
    public boolean isDateAfterToday(LocalDate date) {
        return nonNull(date)
            && date.atTime(DeadlinesCalculator.END_OF_BUSINESS_DAY).isAfter(LocalDateTime.now());
    }

    @JsonIgnore
    public boolean hearingFeePaymentDoneWithHWF() {
        return isLipvLipOneVOne()
            && Objects.nonNull(getHearingHelpFeesReferenceNumber())
            && Objects.nonNull(getFeePaymentOutcomeDetails())
            && Objects.nonNull(getFeePaymentOutcomeDetails().getHwfFullRemissionGrantedForHearingFee());
    }

    @JsonIgnore
    public String getAssignedTrack() {
        return nonNull(getAllocatedTrack()) ? getAllocatedTrack().name() : getResponseClaimTrack();
    }

    @JsonIgnore
    public AllocatedTrack getAssignedTrackType() {
        return nonNull(getAssignedTrack()) ? AllocatedTrack.valueOf(getAssignedTrack()) : null;
    }

    @JsonIgnore
    public boolean hasApplicant1AcceptedCourtDecision() {
        return ofNullable(getCaseDataLiP())
            .map(CaseDataLiP::getApplicant1LiPResponse)
            .filter(ClaimantLiPResponse::hasClaimantAcceptedCourtDecision).isPresent();
    }

    @JsonIgnore
    public boolean hasApplicant1CourtDecisionInFavourOfClaimant() {
        return ofNullable(getCaseDataLiP())
            .map(CaseDataLiP::getApplicant1LiPResponse)
            .filter(ClaimantLiPResponse::hasCourtDecisionInFavourOfClaimant).isPresent();
    }

    public boolean hasApplicant1CourtDecisionInFavourOfDefendant() {
        return ofNullable(getCaseDataLiP())
            .map(CaseDataLiP::getApplicant1LiPResponse)
            .filter(ClaimantLiPResponse::hasCourtDecisionInFavourOfDefendant).isPresent();
    }

    @JsonIgnore
    public boolean claimIssueFeePaymentDoneWithHWF() {
        return Objects.nonNull(getHelpWithFeesReferenceNumber())
            && Objects.nonNull(getFeePaymentOutcomeDetails())
            && Objects.nonNull(getFeePaymentOutcomeDetails().getHwfFullRemissionGrantedForClaimIssue());
    }

    @JsonIgnore
    public boolean claimIssueFullRemissionNotGrantedHWF() {
        return Objects.nonNull(getFeePaymentOutcomeDetails())
            && Objects.nonNull(getFeePaymentOutcomeDetails().getHwfFullRemissionGrantedForClaimIssue())
            && getFeePaymentOutcomeDetails().getHwfFullRemissionGrantedForClaimIssue() == NO;
    }

    @JsonIgnore
    public boolean hearingFeeFullRemissionNotGrantedHWF() {
        return Objects.nonNull(getFeePaymentOutcomeDetails())
            && Objects.nonNull(getFeePaymentOutcomeDetails().getHwfFullRemissionGrantedForHearingFee())
            && getFeePaymentOutcomeDetails().getHwfFullRemissionGrantedForHearingFee() == NO;
    }

    @JsonIgnore
    public boolean isLipvLROneVOne() {
        return !isRespondent1LiP()
            && isApplicant1NotRepresented()
            && isOneVOne(this);
    }

    @JsonIgnore
    public boolean nocApplyForLiPClaimant() {
        return isLRvLipOneVOne()
            && (getClaimIssuedPaymentDetails() == null
            && (claimIssueFeePaymentDoneWithHWF()
            || getChangeOfRepresentation() != null));
    }

    @JsonIgnore
    public LocalDate getApplicant1ClaimSettleDate() {
        return ofNullable(getCaseDataLiP())
            .map(CaseDataLiP::getApplicant1ClaimSettledDate).orElse(null);
    }

    @JsonIgnore
    public boolean isPaidLessThanClaimAmount() {
        RespondToClaim localRespondToClaim = null;
        if (getRespondent1ClaimResponseTypeForSpec() == FULL_DEFENCE) {
            localRespondToClaim = getRespondToClaim();
        } else if (getRespondent1ClaimResponseTypeForSpec() == PART_ADMISSION) {
            localRespondToClaim = getRespondToAdmittedClaim();
        }
        return ofNullable(localRespondToClaim).map(RespondToClaim::getHowMuchWasPaid)
            .map(paid -> MonetaryConversions.penniesToPounds(paid).compareTo(totalClaimAmount) < 0).orElse(false);
    }

    @JsonIgnore
    public boolean isCourtDecisionInClaimantFavourImmediateRePayment() {
        return hasApplicant1CourtDecisionInFavourOfClaimant()
            && getApplicant1RepaymentOptionForDefendantSpec() == PaymentType.IMMEDIATELY;
    }

    @JsonIgnore
    public boolean nocApplyForLiPDefendantBeforeOffline() {
        return isLipvLROneVOne() && getChangeOfRepresentation() != null;
    }

    @JsonIgnore
    public boolean nocApplyForLiPDefendant() {
        return isLipvLROneVOne() && getChangeOfRepresentation() != null && this.getCcdState() == CaseState.PROCEEDS_IN_HERITAGE_SYSTEM;
    }

    @JsonIgnore
    public boolean isJudgeOrderVerificationRequired() {
        return (this.getCourtPermissionNeeded() != null && SettleDiscontinueYesOrNoList.YES.equals(this.getCourtPermissionNeeded()));
    }

    @JsonIgnore
    public boolean isClaimantDontWantToProceedWithFulLDefenceFD() {
        return this.isClaimBeingDisputed()
            && this.hasApplicantNotProceededWithClaim();
    }

    @JsonIgnore
    public boolean isLipCase() {
        return this.isApplicant1NotRepresented() || this.isRespondent1LiP();
    }

    @JsonIgnore
    public boolean isHearingFeePaid() {
        return nonNull(this.getHearingFeePaymentDetails())
            && SUCCESS.equals(this.getHearingFeePaymentDetails().getStatus()) || this.hearingFeePaymentDoneWithHWF();
    }

    @JsonIgnore
    public boolean isCcjRequestJudgmentByAdmissionDefendantNotPaid() {
        return getCcjPaymentDetails() != null
            && getCcjPaymentDetails().getCcjPaymentPaidSomeOption() != null
            && NO.equals(getCcjPaymentDetails().getCcjPaymentPaidSomeOption());
    }

    @JsonIgnore
    public String getApplicantSolicitor1UserDetailsEmail() {
        return applicantSolicitor1UserDetails == null ? null : applicantSolicitor1UserDetails.getEmail();
    }

    @JsonIgnore
    public String getClaimantUserDetailsEmail() {
        final IdamUserDetails claimantUserDetails = getClaimantUserDetails();
        return claimantUserDetails == null ? null : claimantUserDetails.getEmail();
    }

    @JsonIgnore
    public String getRespondent1PartyEmail() {
        final Party party = getRespondent1();
        return party == null ? null : party.getPartyEmail();
    }

    @JsonIgnore
    public String getRespondent2PartyEmail() {
        final Party party = getRespondent2();
        return party == null ? null : party.getPartyEmail();
    }

    @JsonIgnore
    public boolean isClaimUnderTranslationAfterDefResponse() {
        return this.getRespondent1ClaimResponseTypeForSpec() != null
            && this.getCcdState() == CaseState.AWAITING_RESPONDENT_ACKNOWLEDGEMENT;
    }

    @JsonIgnore
    public boolean isClaimUnderTranslationAfterClaimantResponse() {
        return this.getApplicant1ResponseDate() != null
            && this.getCcdState() == CaseState.AWAITING_APPLICANT_INTENTION;
    }

    @JsonIgnore
    public boolean isOtherRemedyClaim() {
        return this.getClaimType() != null
            && (ClaimTypeUnspec.DAMAGES_AND_OTHER_REMEDY.equals(this.getClaimTypeUnSpec())
            || ClaimTypeUnspec.HOUSING_DISREPAIR.equals(this.getClaimTypeUnSpec()));
    }
}
