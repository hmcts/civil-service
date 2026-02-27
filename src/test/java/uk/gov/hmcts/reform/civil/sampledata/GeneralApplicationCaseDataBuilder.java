package uk.gov.hmcts.reform.civil.sampledata;

import uk.gov.hmcts.reform.ccd.model.Organisation;
import uk.gov.hmcts.reform.ccd.model.OrganisationPolicy;
import uk.gov.hmcts.reform.civil.enums.BusinessProcessStatus;
import uk.gov.hmcts.reform.civil.enums.CaseState;
import uk.gov.hmcts.reform.civil.ga.enums.GAJudicialHearingType;
import uk.gov.hmcts.reform.civil.enums.PaymentStatus;
import uk.gov.hmcts.reform.civil.enums.YesOrNo;
import uk.gov.hmcts.reform.civil.ga.enums.dq.FinalOrderShowToggle;
import uk.gov.hmcts.reform.civil.ga.enums.dq.GAByCourtsInitiativeGAspec;
import uk.gov.hmcts.reform.civil.enums.dq.GAHearingDuration;
import uk.gov.hmcts.reform.civil.ga.enums.dq.GAJudgeMakeAnOrderOption;
import uk.gov.hmcts.reform.civil.ga.enums.dq.GAJudgeRequestMoreInfoOption;
import uk.gov.hmcts.reform.civil.enums.dq.GeneralApplicationTypes;
import uk.gov.hmcts.reform.civil.enums.caseprogression.OrderOnCourtsList;
import uk.gov.hmcts.reform.civil.ga.enums.dq.GaFinalOrderSelection;
import uk.gov.hmcts.reform.civil.ga.enums.hearing.HearingApplicationDetails;
import uk.gov.hmcts.reform.civil.ga.model.GeneralApplicationCaseData;
import uk.gov.hmcts.reform.civil.ga.model.GeneralApplicationParty;
import uk.gov.hmcts.reform.civil.ga.model.genapplication.GeneralApplicationPbaDetails;
import uk.gov.hmcts.reform.civil.model.Address;
import uk.gov.hmcts.reform.civil.model.BusinessProcess;
import uk.gov.hmcts.reform.civil.model.genapplication.CaseLink;
import uk.gov.hmcts.reform.civil.model.Fee;
import uk.gov.hmcts.reform.civil.model.GeneralAppParentCaseLink;
import uk.gov.hmcts.reform.civil.model.IdamUserDetails;
import uk.gov.hmcts.reform.civil.model.PaymentDetails;
import uk.gov.hmcts.reform.civil.model.common.DynamicList;
import uk.gov.hmcts.reform.civil.model.common.DynamicListElement;
import uk.gov.hmcts.reform.civil.model.common.Element;
import uk.gov.hmcts.reform.civil.documentmanagement.model.CaseDocument;
import uk.gov.hmcts.reform.civil.documentmanagement.model.Document;
import uk.gov.hmcts.reform.civil.documentmanagement.model.DocumentType;
import uk.gov.hmcts.reform.civil.model.caseprogression.FreeFormOrderValues;
import uk.gov.hmcts.reform.civil.model.genapplication.GAApplicationType;
import uk.gov.hmcts.reform.civil.ga.model.genapplication.GAApproveConsentOrder;
import uk.gov.hmcts.reform.civil.model.genapplication.CaseLocationCivil;
import uk.gov.hmcts.reform.civil.model.genapplication.GADetailsRespondentSol;
import uk.gov.hmcts.reform.civil.model.genapplication.GAHearingDateGAspec;
import uk.gov.hmcts.reform.civil.ga.model.genapplication.GAHearingNoticeApplication;
import uk.gov.hmcts.reform.civil.ga.model.genapplication.GAHearingNoticeDetail;
import uk.gov.hmcts.reform.civil.model.genapplication.GAInformOtherParty;
import uk.gov.hmcts.reform.civil.ga.model.genapplication.GAJudgesHearingListGAspec;
import uk.gov.hmcts.reform.civil.ga.model.genapplication.GAJudicialDecision;
import uk.gov.hmcts.reform.civil.ga.model.genapplication.GAJudicialMakeAnOrder;
import uk.gov.hmcts.reform.civil.ga.model.genapplication.GAJudicialRequestMoreInfo;
import uk.gov.hmcts.reform.civil.ga.model.genapplication.GAJudicialWrittenRepresentations;
import uk.gov.hmcts.reform.civil.ga.model.genapplication.GAMakeApplicationAvailableCheck;
import uk.gov.hmcts.reform.civil.ga.model.genapplication.GAOrderCourtOwnInitiativeGAspec;
import uk.gov.hmcts.reform.civil.model.genapplication.GAPbaDetails;
import uk.gov.hmcts.reform.civil.model.genapplication.GARespondentOrderAgreement;
import uk.gov.hmcts.reform.civil.model.genapplication.GASolicitorDetailsGAspec;
import uk.gov.hmcts.reform.civil.model.genapplication.GAUrgencyRequirement;
import uk.gov.hmcts.reform.civil.model.genapplication.GeneralApplication;
import uk.gov.hmcts.reform.civil.model.genapplication.GeneralApplicationsDetails;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static com.google.common.collect.Lists.newArrayList;
import static java.time.LocalDate.now;
import static java.util.Collections.singletonList;
import static uk.gov.hmcts.reform.civil.enums.CaseState.APPLICATION_ADD_PAYMENT;
import static uk.gov.hmcts.reform.civil.enums.CaseState.AWAITING_APPLICATION_PAYMENT;
import static uk.gov.hmcts.reform.civil.enums.CaseState.LISTING_FOR_A_HEARING;
import static uk.gov.hmcts.reform.civil.enums.CaseState.PENDING_APPLICATION_ISSUED;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.civil.enums.YesOrNo.YES;
import static uk.gov.hmcts.reform.civil.ga.enums.dq.GAJudgeDecisionOption.FREE_FORM_ORDER;
import static uk.gov.hmcts.reform.civil.ga.enums.dq.GAJudgeDecisionOption.LIST_FOR_A_HEARING;
import static uk.gov.hmcts.reform.civil.ga.enums.dq.GAJudgeDecisionOption.MAKE_AN_ORDER;
import static uk.gov.hmcts.reform.civil.ga.enums.dq.GAJudgeDecisionOption.MAKE_ORDER_FOR_WRITTEN_REPRESENTATIONS;
import static uk.gov.hmcts.reform.civil.ga.enums.dq.GAJudgeDecisionOption.REQUEST_MORE_INFO;
import static uk.gov.hmcts.reform.civil.ga.enums.dq.GAJudgeMakeAnOrderOption.DISMISS_THE_APPLICATION;
import static uk.gov.hmcts.reform.civil.ga.enums.dq.GAJudgeMakeAnOrderOption.GIVE_DIRECTIONS_WITHOUT_HEARING;
import static uk.gov.hmcts.reform.civil.ga.enums.dq.GAJudgeRequestMoreInfoOption.REQUEST_MORE_INFORMATION;
import static uk.gov.hmcts.reform.civil.ga.enums.dq.GAJudgeWrittenRepresentationsOptions.CONCURRENT_REPRESENTATIONS;
import static uk.gov.hmcts.reform.civil.ga.enums.dq.GAJudgeWrittenRepresentationsOptions.SEQUENTIAL_REPRESENTATIONS;
import static uk.gov.hmcts.reform.civil.enums.dq.GeneralApplicationTypes.ADJOURN_HEARING;
import static uk.gov.hmcts.reform.civil.enums.dq.GeneralApplicationTypes.EXTEND_TIME;
import static uk.gov.hmcts.reform.civil.enums.dq.GeneralApplicationTypes.VARY_PAYMENT_TERMS_OF_JUDGMENT;
import static uk.gov.hmcts.reform.civil.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.civil.utils.ElementUtils.wrapElements;

public class GeneralApplicationCaseDataBuilder {

    public static final String LEGACY_CASE_REFERENCE = "000DC001";
    public static final Long CASE_ID = 1594901956117591L;
    public static final String PARENT_CASE_ID = "1594901956117591";
    public static final LocalDateTime SUBMITTED_DATE_TIME = LocalDateTime.now();
    public static final LocalDateTime RESPONSE_DEADLINE = SUBMITTED_DATE_TIME.toLocalDate().plusDays(14)
        .atTime(23, 59, 59);
    public static final LocalDateTime APPLICANT_RESPONSE_DEADLINE = SUBMITTED_DATE_TIME.plusDays(120);
    public static final LocalDate APPLICATION_SUBMITTED_DATE = now();
    public static final LocalDateTime DEADLINE = LocalDate.now().atStartOfDay().plusDays(14);
    public static final LocalDate PAST_DATE = now().minusDays(1);
    public static final LocalDateTime NOTIFICATION_DEADLINE = LocalDate.now().atStartOfDay().plusDays(1);
    public static final BigDecimal FAST_TRACK_CLAIM_AMOUNT = BigDecimal.valueOf(10000);
    public static final String CUSTOMER_REFERENCE = "12345";

    private static final String JUDGES_DECISION = "MAKE_DECISION";
    List<DynamicListElement> listItems = singletonList(DynamicListElement.builder()
                                                           .code("code").label("label").build());

    DynamicListElement selectedLocation = DynamicListElement
        .builder().label("sitename - location name - D12 8997").build();

    private static final String HEARING_SCHEDULED = "HEARING_SCHEDULED_GA";
    private static final Fee FEE108 = new Fee().setCalculatedAmountInPence(
        BigDecimal.valueOf(10800)).setCode("FEE0443").setVersion("1");
    private static final Fee FEE14 = new Fee().setCalculatedAmountInPence(
        BigDecimal.valueOf(1400)).setCode("FEE0458").setVersion("2");
    private static final Fee FEE275 = new Fee().setCalculatedAmountInPence(
        BigDecimal.valueOf(27500)).setCode("FEE0442").setVersion("1");
    public static final String STRING_CONSTANT = "this is a string";
    private static final String DUMMY_EMAIL = "hmcts.civil@gmail.com";

    private static final String JUDICIAL_REQUEST_MORE_INFO_RECITAL_TEXT = "<Title> <Name> \n"
        + "Upon reviewing the application made and upon considering the information "
        + "provided by the parties, the court requests more information from the applicant.";

    // Create Claim
    protected Long ccdCaseReference;
    protected String legacyCaseReference;
    protected LocalDateTime generalAppDeadlineNotificationDate;
    protected GAInformOtherParty gaInformOtherParty;
    protected GAUrgencyRequirement gaUrgencyRequirement;
    protected GARespondentOrderAgreement gaRespondentOrderAgreement;
    protected String respondentSolicitor1EmailAddress;
    protected String respondentSolicitor2EmailAddress;
    protected GeneralApplicationPbaDetails gaPbaDetails;
    protected OrganisationPolicy applicant1OrganisationPolicy;
    protected IdamUserDetails applicantSolicitor1UserDetails;
    protected OrganisationPolicy respondent1OrganisationPolicy;
    protected YesOrNo respondent2SameLegalRepresentative;
    protected OrganisationPolicy respondent2OrganisationPolicy;
    protected GAJudicialRequestMoreInfo judicialDecisionRequestMoreInfo;
    protected CaseState ccdState;
    // Claimant Response
    protected BusinessProcess businessProcess;
    protected List<Element<GeneralApplication>> generalApplications;
    protected List<Element<GeneralApplicationsDetails>> claimantGaAppDetails;
    protected List<Element<GADetailsRespondentSol>> respondentSolGaAppDetails;
    protected List<Element<GADetailsRespondentSol>> respondentSolTwoGaAppDetails;
    protected List<Element<GeneralApplicationsDetails>> gaDetailsMasterCollection;

    protected GASolicitorDetailsGAspec generalAppApplnSolicitor;
    protected YesOrNo isGaRespondentOneLip;
    protected String applicantPartyName;
    protected String claimant1PartyName;
    protected String defendant1PartyName;
    protected YesOrNo isGaApplicantLip;
    protected YesOrNo isGaRespondentTwoLip;
    private YesOrNo isMultiParty;
    protected YesOrNo addApplicant2;
    protected List<Element<GASolicitorDetailsGAspec>> generalAppRespondentSolicitors;
    protected GAMakeApplicationAvailableCheck makeAppVisibleToRespondents;
    //General Application
    protected LocalDate submittedOn;
    private GeneralAppParentCaseLink generalAppParentCaseLink;
    private YesOrNo parentClaimantIsApplicant;
    private static final Long CASE_REFERENCE = 111111L;
    protected GAJudicialMakeAnOrder judicialMakeAnOrder;
    protected GAApplicationType generalAppType;
    protected GAApproveConsentOrder  approveConsentOrder;
    private  CaseLocationCivil caseManagementLocation;

    protected GeneralApplicationParty respondent1;
    protected GeneralApplicationParty applicant1;
    protected String emailPartyReference;

    public GeneralApplicationCaseDataBuilder emailPartyReference(String emailPartyReference) {
        this.emailPartyReference = emailPartyReference;
        return this;
    }

    public GeneralApplicationCaseDataBuilder legacyCaseReference(String legacyCaseReference) {
        this.legacyCaseReference = legacyCaseReference;
        return this;
    }

    public GeneralApplicationCaseDataBuilder generalAppDeadlineNotificationDate(LocalDateTime generalAppDeadlineNotificationDate) {
        this.generalAppDeadlineNotificationDate = generalAppDeadlineNotificationDate;
        return this;
    }

    public GeneralApplicationCaseDataBuilder generalApplications(List<Element<GeneralApplication>> generalApplications) {
        this.generalApplications = generalApplications;
        return this;
    }

    public GeneralApplicationCaseDataBuilder generalAppApplnSolicitor(GASolicitorDetailsGAspec generalAppApplnSolicitor) {
        this.generalAppApplnSolicitor = generalAppApplnSolicitor;
        return this;
    }

    public GeneralApplicationCaseDataBuilder isMultiParty(YesOrNo isMultiParty) {
        this.isMultiParty = isMultiParty;
        return this;
    }

    public GeneralApplicationCaseDataBuilder addApplicant2(YesOrNo addApplicant2) {
        this.addApplicant2 = addApplicant2;
        return this;
    }

    public GeneralApplicationCaseDataBuilder applicantSolicitor1UserDetails(IdamUserDetails applicantSolicitor1UserDetails) {
        this.applicantSolicitor1UserDetails = applicantSolicitor1UserDetails;
        return this;
    }

    public GeneralApplicationCaseDataBuilder generalAppRespondentSolicitors(List<Element<GASolicitorDetailsGAspec>>
                                                              generalAppRespondentSolicitors) {
        this.generalAppRespondentSolicitors = generalAppRespondentSolicitors;
        return this;
    }

    public GeneralApplicationCaseDataBuilder makeAppVisibleToRespondents(GAMakeApplicationAvailableCheck makeAppVisibleToRespondents) {
        this.makeAppVisibleToRespondents = makeAppVisibleToRespondents;
        return this;
    }

    public GeneralApplicationCaseDataBuilder ccdState(CaseState ccdState) {
        this.ccdState = ccdState;
        return this;
    }

    public GeneralApplicationCaseDataBuilder generalApplicationsDetails(List<Element<GeneralApplicationsDetails>>
                                                          generalApplicationsDetails) {
        this.claimantGaAppDetails = generalApplicationsDetails;
        return this;
    }

    public GeneralApplicationCaseDataBuilder gaDetailsRespondentSol(List<Element<GADetailsRespondentSol>>
                                                      gaDetailsRespondentSol) {
        this.respondentSolGaAppDetails = gaDetailsRespondentSol;
        return this;
    }

    public GeneralApplicationCaseDataBuilder gaDetailsRespondentSolTwo(List<Element<GADetailsRespondentSol>>
                                                         gaDetailsRespondentSolTwo) {
        this.respondentSolTwoGaAppDetails = gaDetailsRespondentSolTwo;
        return this;
    }

    public GeneralApplicationCaseDataBuilder gaDetailsMasterCollection(List<Element<GeneralApplicationsDetails>>
                                                         gaDetailsMasterCollection) {
        this.gaDetailsMasterCollection = gaDetailsMasterCollection;
        return this;
    }

    public GeneralApplicationCaseDataBuilder businessProcess(BusinessProcess businessProcess) {
        this.businessProcess = businessProcess;
        return this;
    }

    public GeneralApplicationCaseDataBuilder generalAppParentCaseLink(GeneralAppParentCaseLink generalAppParentCaseLink) {
        this.generalAppParentCaseLink = generalAppParentCaseLink;
        return this;
    }

    public GeneralApplicationCaseDataBuilder parentClaimantIsApplicant(YesOrNo parentClaimantIsApplicant) {
        this.parentClaimantIsApplicant = parentClaimantIsApplicant;
        return this;
    }

    public GeneralApplicationCaseDataBuilder ccdCaseReference(Long ccdCaseReference) {
        this.ccdCaseReference = ccdCaseReference;
        return this;
    }

    public GeneralApplicationCaseDataBuilder isGaRespondentOneLip(YesOrNo isGaRespondentOneLip) {
        this.isGaRespondentOneLip = isGaRespondentOneLip;
        return this;
    }

    public GeneralApplicationCaseDataBuilder applicantPartyName(String applicantPartyName) {
        this.applicantPartyName = applicantPartyName;
        return this;
    }

    public GeneralApplicationCaseDataBuilder claimant1PartyName(String claimant1PartyName) {
        this.claimant1PartyName = claimant1PartyName;
        return this;
    }

    public GeneralApplicationCaseDataBuilder defendant1PartyName(String defendant1PartyName) {
        this.defendant1PartyName = defendant1PartyName;
        return this;
    }

    public GeneralApplicationCaseDataBuilder isGaApplicantLip(YesOrNo isGaApplicantLip) {
        this.isGaApplicantLip = isGaApplicantLip;
        return this;
    }

    public GeneralApplicationCaseDataBuilder isGaRespondentTwoLip(YesOrNo isGaRespondentTwoLip) {
        this.isGaRespondentTwoLip = isGaRespondentTwoLip;
        return this;
    }

    public GeneralApplicationCaseDataBuilder gaInformOtherParty(GAInformOtherParty gaInformOtherParty) {
        this.gaInformOtherParty = gaInformOtherParty;
        return this;
    }

    public GeneralApplicationCaseDataBuilder gaUrgencyRequirement(GAUrgencyRequirement gaUrgencyRequirement) {
        this.gaUrgencyRequirement = gaUrgencyRequirement;
        return this;
    }

    public GeneralApplicationCaseDataBuilder gaRespondentOrderAgreement(GARespondentOrderAgreement gaRespondentOrderAgreement) {
        this.gaRespondentOrderAgreement = gaRespondentOrderAgreement;
        return this;
    }

    public GeneralApplicationCaseDataBuilder respondentSolicitor1EmailAddress(String email) {
        this.respondentSolicitor1EmailAddress = email;
        return this;
    }

    public GeneralApplicationCaseDataBuilder respondentSolicitor2EmailAddress(String email) {
        this.respondentSolicitor2EmailAddress = email;
        return this;
    }

    public GeneralApplicationCaseDataBuilder gaPbaDetails(GeneralApplicationPbaDetails gaPbaDetails) {
        this.gaPbaDetails = gaPbaDetails;
        return this;
    }

    public GeneralApplicationCaseDataBuilder applicant1OrganisationPolicy(OrganisationPolicy applicant1OrganisationPolicy) {
        this.applicant1OrganisationPolicy = applicant1OrganisationPolicy;
        return this;
    }

    public GeneralApplicationCaseDataBuilder respondent1OrganisationPolicy(OrganisationPolicy respondent1OrganisationPolicy) {
        this.respondent1OrganisationPolicy = respondent1OrganisationPolicy;
        return this;
    }

    public GeneralApplicationCaseDataBuilder respondent2SameLegalRepresentative(YesOrNo respondent2SameLegalRepresentative) {
        this.respondent2SameLegalRepresentative = respondent2SameLegalRepresentative;
        return this;
    }

    public GeneralApplicationCaseDataBuilder respondent2OrganisationPolicy(OrganisationPolicy respondent2OrganisationPolicy) {
        this.respondent2OrganisationPolicy = respondent2OrganisationPolicy;
        return this;
    }

    public GeneralApplicationCaseDataBuilder judicialDecisionMakeOrder(GAJudicialMakeAnOrder judicialMakeAnOrder) {
        this.judicialMakeAnOrder = judicialMakeAnOrder;
        return this;
    }

    public GeneralApplicationCaseDataBuilder generalAppType(GAApplicationType generalAppType) {
        this.generalAppType = generalAppType;
        return this;
    }

    public GeneralApplicationCaseDataBuilder approveConsentOrder(GAApproveConsentOrder approveConsentOrder) {
        this.approveConsentOrder = approveConsentOrder;
        return this;
    }

    public GeneralApplicationCaseDataBuilder caseManagementLocation(CaseLocationCivil caseManagementLocation) {
        this.caseManagementLocation = caseManagementLocation;
        return this;
    }

    public static GeneralApplicationCaseDataBuilder builder() {
        return new GeneralApplicationCaseDataBuilder();
    }

    public GeneralApplicationCaseDataBuilder judicialDecisionRequestMoreInfo(GAJudicialRequestMoreInfo judicialDecisionRequestMoreInfo) {
        this.judicialDecisionRequestMoreInfo = judicialDecisionRequestMoreInfo;
        return this;
    }

    public GeneralApplicationCaseDataBuilder atStateClaimDraft() {

        return this;
    }

    public GeneralApplicationCaseDataBuilder respondent1(GeneralApplicationParty party) {
        this.respondent1 = party;
        return this;
    }

    public GeneralApplicationCaseDataBuilder applicant1(GeneralApplicationParty party) {
        this.applicant1 = party;
        return this;
    }

    public GeneralApplicationCaseData build() {
        return new GeneralApplicationCaseData()
            .businessProcess(businessProcess)
            .ccdState(ccdState)
            .applicant1(applicant1)
            .respondent1(respondent1)
            .applicantPartyName(applicantPartyName)
            .claimant1PartyName(claimant1PartyName)
            .caseNameGaInternal("applicant v respondent")
            .defendant1PartyName(defendant1PartyName)
            .isGaRespondentOneLip(isGaRespondentOneLip)
            .isGaRespondentTwoLip(isGaRespondentTwoLip)
            .isGaApplicantLip(isGaApplicantLip)
            .isMultiParty(isMultiParty)
            .addApplicant2(addApplicant2)
            .respondentSolTwoGaAppDetails(respondentSolTwoGaAppDetails)
            .gaDetailsMasterCollection(gaDetailsMasterCollection)
            .applicantSolicitor1UserDetails(applicantSolicitor1UserDetails)
            .applicant1OrganisationPolicy(applicant1OrganisationPolicy)
            .respondentSolicitor1EmailAddress(respondentSolicitor1EmailAddress)
            .respondentSolicitor2EmailAddress(respondentSolicitor2EmailAddress)
            .respondent1OrganisationPolicy(respondent1OrganisationPolicy)
            .respondent2OrganisationPolicy(respondent2OrganisationPolicy)
            .generalAppApplnSolicitor(generalAppApplnSolicitor)
            .judicialDecisionRequestMoreInfo(judicialDecisionRequestMoreInfo)
            .generalAppRespondentSolicitors(generalAppRespondentSolicitors)
            .ccdCaseReference(ccdCaseReference)
            .respondent2SameLegalRepresentative(respondent2SameLegalRepresentative)
            .legacyCaseReference(legacyCaseReference)
            .emailPartyReference(emailPartyReference)
            .generalApplications(generalApplications)
            .generalAppInformOtherParty(gaInformOtherParty)
            .generalAppUrgencyRequirement(gaUrgencyRequirement)
            .generalAppRespondentAgreement(gaRespondentOrderAgreement)
            .generalAppParentCaseLink(generalAppParentCaseLink)
            .claimantGaAppDetails(claimantGaAppDetails)
            .respondentSolGaAppDetails(respondentSolGaAppDetails)
            .generalAppPBADetails(gaPbaDetails)
            .applicant1OrganisationPolicy(applicant1OrganisationPolicy)
            .generalAppNotificationDeadlineDate(generalAppDeadlineNotificationDate)
            .parentClaimantIsApplicant(parentClaimantIsApplicant)
            .makeAppVisibleToRespondents(makeAppVisibleToRespondents)
            .judicialDecisionMakeOrder(judicialMakeAnOrder)
            .generalAppType(generalAppType)
            .approveConsentOrder(approveConsentOrder)
            .caseManagementLocation(caseManagementLocation)
            .build();
    }

    public GeneralApplicationCaseData buildMakePaymentsCaseData() {
        uk.gov.hmcts.reform.ccd.model.Organisation orgId = new uk.gov.hmcts.reform.ccd.model.Organisation().setOrganisationID("OrgId");

        return build().copy()
            .ccdCaseReference(1644495739087775L)
            .ccdCaseReference(1644495739087775L)
            .legacyCaseReference("000DC001")
            .ccdState(CaseState.APPLICATION_SUBMITTED_AWAITING_JUDICIAL_DECISION)
            .businessProcess(new BusinessProcess().setStatus(BusinessProcessStatus.READY))
            .generalAppPBADetails(
                new GeneralApplicationPbaDetails()
                    .setFee(
                        new Fee()
                            .setCode("FE203")
                            .setCalculatedAmountInPence(BigDecimal.valueOf(27500))
                            .setVersion("1")
                            )
                    .setServiceReqReference(CUSTOMER_REFERENCE))
            .generalAppSuperClaimType("UNSPEC_CLAIM")
            .applicant1OrganisationPolicy(new OrganisationPolicy().setOrganisation(orgId))
            .generalAppApplnSolicitor(GASolicitorDetailsGAspec.builder().organisationIdentifier("OrgId").build())
            .build();
    }

    public GeneralApplicationCaseData buildPaymentFailureCaseData() {
        uk.gov.hmcts.reform.ccd.model.Organisation orgId = new uk.gov.hmcts.reform.ccd.model.Organisation().setOrganisationID("OrgId");

        return build().copy()
            .ccdCaseReference(1644495739087775L)
            .ccdCaseReference(1644495739087775L)
            .legacyCaseReference("000DC001")
            .generalAppInformOtherParty(GAInformOtherParty.builder()
                                            .isWithNotice(YES).build())
            .generalAppRespondentAgreement(GARespondentOrderAgreement.builder()
                                               .hasAgreed(YES).build())
            .businessProcess(new BusinessProcess().setStatus(BusinessProcessStatus.READY))
            .generalAppPBADetails(
                new GeneralApplicationPbaDetails()
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
            .parentClaimantIsApplicant(YES)
            .generalAppApplnSolicitor(GASolicitorDetailsGAspec.builder().organisationIdentifier("OrgId").build())
            .build();
    }

    public GeneralApplicationCaseData withNoticeCaseData() {
        uk.gov.hmcts.reform.ccd.model.Organisation orgId = new uk.gov.hmcts.reform.ccd.model.Organisation().setOrganisationID("OrgId");
        List<GeneralApplicationTypes> types = Arrays.asList(VARY_PAYMENT_TERMS_OF_JUDGMENT);
        return build().copy()
            .generalAppType(GAApplicationType.builder().types(types).build())
            .ccdCaseReference(1644495739087775L)
            .legacyCaseReference("000DC001")
            .businessProcess(new BusinessProcess().setStatus(BusinessProcessStatus.READY))
            .generalAppInformOtherParty(GAInformOtherParty.builder()
                                            .isWithNotice(YES).build())
            .generalAppRespondentAgreement(GARespondentOrderAgreement.builder()
                                               .hasAgreed(YES).build())
            .generalAppPBADetails(
                new GeneralApplicationPbaDetails()
                    .setPaymentSuccessfulDate(LocalDateTime.of(LocalDate.of(2020, 1, 1),
                                                            LocalTime.of(12, 0, 0)))
                    .setPaymentDetails(new PaymentDetails()
                                        .setStatus(PaymentStatus.SUCCESS)
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
            .generalAppApplnSolicitor(GASolicitorDetailsGAspec.builder().organisationIdentifier("OrgId").build())
            .build();
    }

    public GeneralApplicationCaseData withoutNoticeCaseData() {
        uk.gov.hmcts.reform.ccd.model.Organisation orgId = new uk.gov.hmcts.reform.ccd.model.Organisation().setOrganisationID("OrgId");

        return build().copy()
            .ccdCaseReference(1644495739087775L)
            .ccdCaseReference(1644495739087775L)
            .legacyCaseReference("000DC001")
            .businessProcess(new BusinessProcess().setStatus(BusinessProcessStatus.READY))
            .generalAppInformOtherParty(GAInformOtherParty.builder()
                                            .isWithNotice(NO).build())
            .generalAppRespondentAgreement(GARespondentOrderAgreement.builder()
                                               .hasAgreed(NO).build())
            .generalAppPBADetails(
                new GeneralApplicationPbaDetails()
                    .setPaymentSuccessfulDate(LocalDateTime.of(LocalDate.of(2020, 1, 1),
                                                            LocalTime.of(12, 0, 0)))
                    .setPaymentDetails(new PaymentDetails()
                                        .setStatus(PaymentStatus.SUCCESS)
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
            .generalAppApplnSolicitor(GASolicitorDetailsGAspec.builder().organisationIdentifier("OrgId").build())
            .build();
    }

    public final CaseDocument pdfDocument = new CaseDocument()
        .setCreatedBy("John")
        .setDocumentName("documentName")
        .setDocumentSize(0L)
        .setDocumentType(DocumentType.GENERAL_APPLICATION_DRAFT)
        .setCreatedDatetime(LocalDateTime.now())
        .setDocumentLink(new Document()
                          .setDocumentUrl("fake-url")
                          .setDocumentFileName("file-name")
                          .setDocumentBinaryUrl("binary-url"));

    public GeneralApplicationCaseData withNoticeDraftAppCaseData() {
        uk.gov.hmcts.reform.ccd.model.Organisation orgId = new uk.gov.hmcts.reform.ccd.model.Organisation().setOrganisationID("OrgId");
        String uid = "f000aa01-0451-4000-b000-000000000111";
        String uid1 = "f000aa01-0451-4000-b000-000000000000";
        List<Element<CaseDocument>> draftDocs = newArrayList();
        draftDocs.add(Element.<CaseDocument>builder().id(UUID.fromString(uid1))
                          .value(pdfDocument).build());
        draftDocs.add(Element.<CaseDocument>builder().id(UUID.fromString(uid))
                          .value(pdfDocument).build());
        return build().copy()
            .ccdCaseReference(1644495739087775L)
            .ccdCaseReference(1644495739087775L)
            .legacyCaseReference("000DC001")
            .businessProcess(new BusinessProcess().setStatus(BusinessProcessStatus.READY))
            .generalAppInformOtherParty(GAInformOtherParty.builder()
                                            .isWithNotice(YES).build())
            .generalAppRespondentAgreement(GARespondentOrderAgreement.builder()
                                               .hasAgreed(YES).build())
            .generalAppPBADetails(
                new GeneralApplicationPbaDetails()
                    .setPaymentSuccessfulDate(LocalDateTime.of(LocalDate.of(2020, 1, 1),
                                                            LocalTime.of(12, 0, 0)))
                    .setPaymentDetails(new PaymentDetails()
                                        .setStatus(PaymentStatus.SUCCESS)
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
            .generalAppApplnSolicitor(GASolicitorDetailsGAspec.builder().organisationIdentifier("OrgId").build())
            .build();
    }

    public GeneralApplicationCaseData buildPaymentSuccessfulCaseData() {
        uk.gov.hmcts.reform.ccd.model.Organisation orgId = new uk.gov.hmcts.reform.ccd.model.Organisation().setOrganisationID("OrgId");

        return build().copy()
            .ccdCaseReference(1644495739087775L)
            .ccdCaseReference(1644495739087775L)
            .legacyCaseReference("000DC001")
            .businessProcess(new BusinessProcess().setStatus(BusinessProcessStatus.READY))
            .generalAppPBADetails(
                new GeneralApplicationPbaDetails()
                    .setPaymentSuccessfulDate(LocalDateTime.of(LocalDate.of(2020, 1, 1),
                                                            LocalTime.of(12, 0, 0)))
                    .setPaymentDetails(new PaymentDetails()
                                        .setStatus(PaymentStatus.SUCCESS)
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
            .generalAppApplnSolicitor(GASolicitorDetailsGAspec.builder().organisationIdentifier("OrgId").build())
            .build();
    }

    public GeneralApplicationCaseData buildCaseDateBaseOnGeneralApplication(GeneralApplication application) {
        return new GeneralApplicationCaseData()
            .ccdState(PENDING_APPLICATION_ISSUED)
            .generalAppType(application.getGeneralAppType())
            .caseLink(application.getCaseLink())
            .generalAppRespondentAgreement(application.getGeneralAppRespondentAgreement())
            .generalAppInformOtherParty(application.getGeneralAppInformOtherParty())
            .generalAppPBADetails(convert(application.getGeneralAppPBADetails()))
            .generalAppDetailsOfOrder(application.getGeneralAppDetailsOfOrder())
            .generalAppReasonsOfOrder(application.getGeneralAppReasonsOfOrder())
            .generalAppNotificationDeadlineDate(application.getGeneralAppDateDeadline())
            .generalAppUrgencyRequirement(application.getGeneralAppUrgencyRequirement())
            .generalAppStatementOfTruth(application.getGeneralAppStatementOfTruth())
            .generalAppHearingDetails(application.getGeneralAppHearingDetails())
            .generalAppEvidenceDocument(application.getGeneralAppEvidenceDocument())
            .isMultiParty(application.getIsMultiParty())
            .parentClaimantIsApplicant(application.getParentClaimantIsApplicant())
            .generalAppParentCaseLink(application.getGeneralAppParentCaseLink())
            .generalAppRespondentSolicitors(application.getGeneralAppRespondentSolicitors())
            .isCcmccLocation(application.getIsCcmccLocation())
            .caseManagementLocation(application.getCaseManagementLocation())
            .generalAppSubmittedDateGAspec(application.getGeneralAppSubmittedDateGAspec())
            .build();
    }

    public GeneralApplicationCaseData buildCaseDateBaseOnGeneralApplicationByState(GeneralApplication application, CaseState state) {
        return new GeneralApplicationCaseData()
            .ccdState(state)
            .generalAppType(application.getGeneralAppType())
            .caseLink(application.getCaseLink())
            .generalAppRespondentAgreement(application.getGeneralAppRespondentAgreement())
            .generalAppInformOtherParty(application.getGeneralAppInformOtherParty())
            .generalAppPBADetails(convert(application.getGeneralAppPBADetails()))
            .generalAppDetailsOfOrder(application.getGeneralAppDetailsOfOrder())
            .generalAppReasonsOfOrder(application.getGeneralAppReasonsOfOrder())
            .generalAppNotificationDeadlineDate(application.getGeneralAppDateDeadline())
            .generalAppUrgencyRequirement(application.getGeneralAppUrgencyRequirement())
            .generalAppStatementOfTruth(application.getGeneralAppStatementOfTruth())
            .generalAppHearingDetails(application.getGeneralAppHearingDetails())
            .generalAppEvidenceDocument(application.getGeneralAppEvidenceDocument())
            .isMultiParty(application.getIsMultiParty())
            .parentClaimantIsApplicant(application.getParentClaimantIsApplicant())
            .generalAppParentCaseLink(application.getGeneralAppParentCaseLink())
            .generalAppRespondentSolicitors(application.getGeneralAppRespondentSolicitors())
            .isCcmccLocation(application.getIsCcmccLocation())
            .caseManagementLocation(application.getCaseManagementLocation())
            .build();
    }

    public GeneralApplicationCaseData buildCaseDateBaseOnGaForCollection(GeneralApplication application) {
        return new GeneralApplicationCaseData()
            .ccdState(AWAITING_APPLICATION_PAYMENT)
            .generalAppType(application.getGeneralAppType())
            .caseLink(application.getCaseLink())
            .generalAppRespondentAgreement(application.getGeneralAppRespondentAgreement())
            .generalAppInformOtherParty(application.getGeneralAppInformOtherParty())
            .generalAppPBADetails(convert(application.getGeneralAppPBADetails()))
            .generalAppDetailsOfOrder(application.getGeneralAppDetailsOfOrder())
            .generalAppReasonsOfOrder(application.getGeneralAppReasonsOfOrder())
            .generalAppNotificationDeadlineDate(application.getGeneralAppDateDeadline())
            .generalAppUrgencyRequirement(application.getGeneralAppUrgencyRequirement())
            .generalAppStatementOfTruth(application.getGeneralAppStatementOfTruth())
            .generalAppHearingDetails(application.getGeneralAppHearingDetails())
            .generalAppEvidenceDocument(application.getGeneralAppEvidenceDocument())
            .isMultiParty(application.getIsMultiParty())
            .parentClaimantIsApplicant(application.getParentClaimantIsApplicant())
            .generalAppParentCaseLink(application.getGeneralAppParentCaseLink())
            .generalAppRespondentSolicitors(application.getGeneralAppRespondentSolicitors())
            .isCcmccLocation(application.getIsCcmccLocation())
            .caseManagementLocation(application.getCaseManagementLocation())
            .build();
    }

    private GeneralApplicationPbaDetails convert(GAPbaDetails gaPbaDetails) {
        final GeneralApplicationPbaDetails generalApplicationPBADetails = new GeneralApplicationPbaDetails();
        generalApplicationPBADetails.setPaymentSuccessfulDate(gaPbaDetails.getPaymentSuccessfulDate());
        generalApplicationPBADetails.setPaymentDetails(gaPbaDetails.getPaymentDetails());
        generalApplicationPBADetails.setFee(gaPbaDetails.getFee());
        generalApplicationPBADetails.setServiceReqReference(gaPbaDetails.getServiceReqReference());
        generalApplicationPBADetails.setAdditionalPaymentServiceRef(gaPbaDetails.getAdditionalPaymentServiceRef());
        generalApplicationPBADetails.setAdditionalPaymentDetails(gaPbaDetails.getAdditionalPaymentDetails());
        return generalApplicationPBADetails;
    }

    public GeneralApplicationCaseData buildFeeValidationCaseData(Fee fee, boolean isConsented, boolean isWithNotice) {

        uk.gov.hmcts.reform.ccd.model.Organisation orgId = new uk.gov.hmcts.reform.ccd.model.Organisation().setOrganisationID("OrgId");

        GAInformOtherParty gaInformOtherParty = null;
        if (!isConsented) {
            gaInformOtherParty = GAInformOtherParty.builder().isWithNotice(isWithNotice ? YES : NO)
                .reasonsForWithoutNotice(isWithNotice ? null : STRING_CONSTANT)
                .build();
        }
        return new GeneralApplicationCaseData()
            .ccdCaseReference(1644495739087775L)
            .ccdCaseReference(1644495739087775L)
            .legacyCaseReference("000DC001")
            .businessProcess(new BusinessProcess().setStatus(BusinessProcessStatus.READY))
            .generalAppRespondentAgreement(GARespondentOrderAgreement.builder()
                                               .hasAgreed(isConsented ? YES : NO).build())
            .generalAppInformOtherParty(gaInformOtherParty)
            .generalAppPBADetails(
                new GeneralApplicationPbaDetails()
                    .setFee(fee)
                    .setServiceReqReference(CUSTOMER_REFERENCE))
            .applicant1OrganisationPolicy(new OrganisationPolicy().setOrganisation(orgId))
            .generalAppApplnSolicitor(GASolicitorDetailsGAspec.builder().organisationIdentifier("OrgId").build())
            .build();
    }

    public GeneralApplicationCaseData consentOrderApplication() {
        return new GeneralApplicationCaseData()
            .ccdCaseReference(CASE_ID)
            .generalAppParentCaseLink(new GeneralAppParentCaseLink().setCaseReference(PARENT_CASE_ID))
            .claimant1PartyName("Test Claimant1 Name")
            .claimant2PartyName("Test Claimant2 Name")
            .defendant1PartyName("Test Defendant1 Name")
            .defendant2PartyName("Test Defendant2 Name")
            .applicantPartyName("Test Applicant Name")
            .createdDate(SUBMITTED_DATE_TIME)
            .locationName("County Court")
            .caseManagementLocation(CaseLocationCivil.builder()
                                        .siteName("County Court")
                                        .baseLocation("2")
                                        .region("4").build())
            .generalAppType(GAApplicationType.builder()
                                .types(singletonList(EXTEND_TIME))
                                .build())
            .approveConsentOrder(new GAApproveConsentOrder().setConsentOrderDescription("testing purpose")
                                     )
            .judicialDecision(new GAJudicialDecision().setDecision(MAKE_AN_ORDER))
            .judicialDecisionMakeOrder(new GAJudicialMakeAnOrder()
                                           .setOrderText("Test Order")
                                           .setOrderCourtOwnInitiative("abcd")
                                           .setOrderCourtOwnInitiativeDate(now())
                                           .setJudicialByCourtsInitiative(GAByCourtsInitiativeGAspec.OPTION_1)
                                           .setReasonForDecisionText("Test Reason")
                                           .setMakeAnOrder(GAJudgeMakeAnOrderOption.APPROVE_OR_EDIT)
                                           .setJudgeRecitalText("Test Judge's recital"))
            .submittedOn(APPLICATION_SUBMITTED_DATE);
    }

    public GeneralApplicationCaseData finalOrderFreeForm() {
        return new GeneralApplicationCaseData()
            .ccdCaseReference(CASE_ID)
            .generalAppParentCaseLink(new GeneralAppParentCaseLink().setCaseReference(PARENT_CASE_ID))
            .claimant1PartyName("Test Claimant1 Name")
            .claimant2PartyName("Test Claimant2 Name")
            .defendant1PartyName("Test Defendant1 Name")
            .defendant2PartyName("Test Defendant2 Name")
            .applicantPartyName("Test Applicant Name")
            .judgeTitle("John Doe")
            .caseManagementLocation(CaseLocationCivil.builder().siteName("testing")
                                        .address("london court")
                                        .baseLocation("2")
                                        .postcode("BA 117").build())
            .freeFormRecitalText("abcd")
            .freeFormOrderedText("abcd")
            .orderOnCourtsList(OrderOnCourtsList.ORDER_ON_COURT_INITIATIVE)
            .orderOnCourtInitiative(new FreeFormOrderValues()
                                        .setOnInitiativeSelectionTextArea("abcd")
                                        .setOnInitiativeSelectionDate(now()))
            .createdDate(SUBMITTED_DATE_TIME)
            .submittedOn(APPLICATION_SUBMITTED_DATE);
    }

    public GeneralApplicationCaseData getCivilCaseData() {
        Address applicantAddress = new Address();
        applicantAddress.setPostCode("postcode");
        applicantAddress.setPostTown("posttown");
        applicantAddress.setAddressLine1("address1");
        applicantAddress.setAddressLine2("address2");
        applicantAddress.setAddressLine3("address3");

        Address respondentAddress = new Address();
        respondentAddress.setPostCode("respondent1postcode");
        respondentAddress.setPostTown("respondent1posttown");
        respondentAddress.setAddressLine1("respondent1address1");
        respondentAddress.setAddressLine2("respondent1address2");
        respondentAddress.setAddressLine3("respondent1address3");

        return new GeneralApplicationCaseData()
            .applicant1(new GeneralApplicationParty()
                            .setPrimaryAddress(applicantAddress)
                            .setPartyName("applicant1partyname"))
            .respondent1(new GeneralApplicationParty()
                             .setPrimaryAddress(respondentAddress)
                             .setPartyName("respondent1partyname")).build();
    }

    public GeneralApplicationCaseData generalOrderApplication() {
        return new GeneralApplicationCaseData()
            .ccdCaseReference(CASE_ID)
            .generalAppParentCaseLink(new GeneralAppParentCaseLink().setCaseReference(PARENT_CASE_ID))
            .claimant1PartyName("Test Claimant1 Name")
            .claimant2PartyName("Test Claimant2 Name")
            .defendant1PartyName("Test Defendant1 Name")
            .defendant2PartyName("Test Defendant2 Name")
            .applicantPartyName("Test Applicant Name")
            .judgeTitle("John Doe")
            .caseManagementLocation(CaseLocationCivil.builder().siteName("testing")
                                        .address("london court")
                                        .baseLocation("2")
                                        .postcode("BA 117").build())
            .isMultiParty(NO)
            .createdDate(SUBMITTED_DATE_TIME)
            .generalAppType(GAApplicationType.builder()
                                .types(singletonList(EXTEND_TIME))
                                .build())
            .judicialDecision(new GAJudicialDecision().setDecision(MAKE_AN_ORDER))
            .judicialDecisionMakeOrder(new GAJudicialMakeAnOrder()
                                           .setOrderText("Test Order")
                                           .setOrderCourtOwnInitiative("abcd")
                                           .setOrderCourtOwnInitiativeDate(now())
                                           .setJudicialByCourtsInitiative(GAByCourtsInitiativeGAspec.OPTION_1)
                                           .setReasonForDecisionText("Test Reason")
                                           .setMakeAnOrder(GAJudgeMakeAnOrderOption.APPROVE_OR_EDIT)
                                           .setShowJudgeRecitalText(List.of(FinalOrderShowToggle.SHOW))
                                           .setJudgeRecitalText("Test Judge's recital"))
            .submittedOn(APPLICATION_SUBMITTED_DATE);
    }

    public GeneralApplicationCaseData generalOrderFreeFormApplication() {
        return new GeneralApplicationCaseData()
            .ccdCaseReference(CASE_ID)
            .claimant1PartyName("Test Claimant1 Name")
            .claimant2PartyName("Test Claimant2 Name")
            .defendant1PartyName("Test Defendant1 Name")
            .defendant2PartyName("Test Defendant2 Name")
            .applicantPartyName("Test Applicant Name")
            .judgeTitle("John Doe")
            .caseManagementLocation(CaseLocationCivil.builder().siteName("testing")
                                        .address("london court")
                                        .postcode("BA 117").build())
            .isMultiParty(NO)
            .createdDate(SUBMITTED_DATE_TIME)
            .generalAppType(GAApplicationType.builder()
                                .types(singletonList(EXTEND_TIME))
                                .build())
            .judicialDecision(new GAJudicialDecision().setDecision(FREE_FORM_ORDER))
            .judicialDecisionMakeOrder(new GAJudicialMakeAnOrder()
                                           .setOrderText("Test Order")
                                           .setOrderCourtOwnInitiative("abcd")
                                           .setOrderCourtOwnInitiativeDate(now())
                                           .setJudicialByCourtsInitiative(GAByCourtsInitiativeGAspec.OPTION_1)
                                           .setReasonForDecisionText("Test Reason")
                                           .setMakeAnOrder(GAJudgeMakeAnOrderOption.APPROVE_OR_EDIT)
                                           .setShowJudgeRecitalText(List.of(FinalOrderShowToggle.SHOW))
                                           .setJudgeRecitalText("Test Judge's recital"))
            .submittedOn(APPLICATION_SUBMITTED_DATE);
    }

    public GeneralApplicationCaseData judgeFinalOrderApplication() {
        return new GeneralApplicationCaseData()
            .ccdCaseReference(CASE_ID)
            .claimant1PartyName("Test Claimant1 Name")
            .claimant2PartyName("Test Claimant2 Name")
            .defendant1PartyName("Test Defendant1 Name")
            .defendant2PartyName("Test Defendant2 Name")
            .applicantPartyName("Test Applicant Name")
            .judgeTitle("John Doe")
            .caseManagementLocation(CaseLocationCivil.builder().siteName("testing")
                                        .address("london court")
                                        .postcode("BA 117").build())
            .isMultiParty(NO)
            .createdDate(SUBMITTED_DATE_TIME)
            .generalAppType(GAApplicationType.builder()
                                .types(singletonList(EXTEND_TIME))
                                .build())
            .judicialDecision(new GAJudicialDecision().setDecision(MAKE_AN_ORDER))
            .finalOrderSelection(GaFinalOrderSelection.ASSISTED_ORDER)
            .judicialDecisionMakeOrder(new GAJudicialMakeAnOrder()
                                           .setOrderText("Test Order")
                                           .setOrderCourtOwnInitiative("abcd")
                                           .setOrderCourtOwnInitiativeDate(now())
                                           .setJudicialByCourtsInitiative(GAByCourtsInitiativeGAspec.OPTION_1)
                                           .setReasonForDecisionText("Test Reason")
                                           .setMakeAnOrder(GAJudgeMakeAnOrderOption.APPROVE_OR_EDIT)
                                           .setShowJudgeRecitalText(List.of(FinalOrderShowToggle.SHOW))
                                           .setJudgeRecitalText("Test Judge's recital"))
            .submittedOn(APPLICATION_SUBMITTED_DATE);
    }

    public GeneralApplicationCaseData directionOrderApplication() {
        return new GeneralApplicationCaseData()
            .ccdCaseReference(CASE_ID)
            .claimant1PartyName("Test Claimant1 Name")
            .claimant2PartyName("Test Claimant2 Name")
            .defendant1PartyName("Test Defendant1 Name")
            .defendant2PartyName("Test Defendant2 Name")
            .applicantPartyName("Test Applicant Name")
            .judgeTitle("John Doe")
            .generalAppParentCaseLink(new GeneralAppParentCaseLink().setCaseReference(PARENT_CASE_ID))
            .caseManagementLocation(CaseLocationCivil.builder().siteName("testing")
                                        .address("london court")
                                        .baseLocation("1")
                                        .postcode("BA 117").build())
            .createdDate(LocalDateTime.now())
            .generalAppType(GAApplicationType.builder()
                                .types(singletonList(EXTEND_TIME))
                                .build())
            .judicialDecision(new GAJudicialDecision().setDecision(MAKE_AN_ORDER))
            .judicialDecisionMakeOrder(new GAJudicialMakeAnOrder()
                                           .setDirectionsText("Test Direction")
                                           .setJudicialByCourtsInitiative(GAByCourtsInitiativeGAspec.OPTION_1)
                                           .setOrderCourtOwnInitiative("abcd")
                                           .setOrderCourtOwnInitiativeDate(now())
                                           .setReasonForDecisionText("Test Reason")
                                           .setMakeAnOrder(GIVE_DIRECTIONS_WITHOUT_HEARING)
                                           .setDirectionsResponseByDate(now())
                                           .setShowJudgeRecitalText(List.of(FinalOrderShowToggle.SHOW))
                                           .setJudgeRecitalText("Test Judge's recital"))
            .submittedOn(APPLICATION_SUBMITTED_DATE);
    }

    public GeneralApplicationCaseData dismissalOrderApplication() {
        return new GeneralApplicationCaseData()
            .ccdCaseReference(CASE_ID)
            .generalAppParentCaseLink(new GeneralAppParentCaseLink().setCaseReference(PARENT_CASE_ID))
            .claimant1PartyName("Test Claimant1 Name")
            .claimant2PartyName("Test Claimant2 Name")
            .defendant1PartyName("Test Defendant1 Name")
            .defendant2PartyName("Test Defendant2 Name")
            .applicantPartyName("Test Applicant Name")
            .judgeTitle("John Doe")
            .caseManagementLocation(CaseLocationCivil.builder().siteName("testing")
                                        .address("london court")
                                        .baseLocation("1")
                                        .postcode("BA 117").build())
            .createdDate(LocalDateTime.now())
            .generalAppType(GAApplicationType.builder()
                                .types(singletonList(EXTEND_TIME))
                                .build())
            .judicialDecision(new GAJudicialDecision().setDecision(MAKE_AN_ORDER))
            .judicialDecisionMakeOrder(new GAJudicialMakeAnOrder()
                                           .setDismissalOrderText("Test Dismissal")
                                           .setReasonForDecisionText("Test Reason")
                                           .setOrderCourtOwnInitiative("abcd")
                                           .setOrderCourtOwnInitiativeDate(now())
                                           .setJudicialByCourtsInitiative(GAByCourtsInitiativeGAspec.OPTION_1)
                                           .setMakeAnOrder(DISMISS_THE_APPLICATION))
            .submittedOn(APPLICATION_SUBMITTED_DATE);
    }

    public GeneralApplicationCaseData hearingOrderApplication(YesOrNo isAgreed, YesOrNo isWithNotice) {
        return new GeneralApplicationCaseData()
            .ccdCaseReference(CASE_ID)
            .generalAppParentCaseLink(new GeneralAppParentCaseLink().setCaseReference(PARENT_CASE_ID))
            .claimant1PartyName("Test Claimant1 Name")
            .claimant2PartyName("Test Claimant2 Name")
            .judgeTitle("John Doe")
            .generalAppParentCaseLink(new GeneralAppParentCaseLink()
                                          .setCaseReference(CASE_REFERENCE.toString()))
            .judicialListForHearing(new GAJudgesHearingListGAspec()
                                        .setHearingPreferredLocation(DynamicList.builder()
                                                                      .value(selectedLocation).listItems(listItems)
                                                                      .build())
                                        .setHearingPreferencesPreferredType(GAJudicialHearingType.IN_PERSON)
                                        .setJudicialTimeEstimate(GAHearingDuration.MINUTES_15))
            .judicialByCourtsInitiativeListForHearing(GAByCourtsInitiativeGAspec.OPTION_1)
            .orderCourtOwnInitiativeListForHearing(new GAOrderCourtOwnInitiativeGAspec()
                                                       .setOrderCourtOwnInitiative("abcd")
                                                       .setOrderCourtOwnInitiativeDate(now()))
            .defendant1PartyName("Test Defendant1 Name")
            .locationName("Nottingham County Court and Family Court (and Crown)")
            .defendant2PartyName("Test Defendant2 Name")
            .applicantPartyName("Test Applicant Name")
            .createdDate(LocalDateTime.now())
            .caseManagementLocation(CaseLocationCivil.builder().siteName("testing")
                                        .address("london court")
                                        .baseLocation("1")
                                        .postcode("BA 117").build())
            .judicialGeneralHearingOrderRecital("Test Judge's recital")
            .judicialGOHearingDirections("Test hearing direction")
            .generalAppRespondentAgreement(GARespondentOrderAgreement.builder().hasAgreed(isAgreed).build())
            .generalAppInformOtherParty(GAInformOtherParty.builder().isWithNotice(isWithNotice).build())
            .generalAppType(GAApplicationType.builder()
                                .types(singletonList(EXTEND_TIME))
                                .build())
            .judicialDecision(new GAJudicialDecision().setDecision(LIST_FOR_A_HEARING))
            .judicialHearingGOHearingReqText("test")
            .submittedOn(APPLICATION_SUBMITTED_DATE);
    }

    public GeneralApplicationCaseData hearingScheduledApplication(YesOrNo isCloak) {
        List<Element<GASolicitorDetailsGAspec>> respondentSols = new ArrayList<>();

        GASolicitorDetailsGAspec respondent1 = GASolicitorDetailsGAspec.builder().id("id")
            .email(DUMMY_EMAIL).organisationIdentifier("2").build();

        GASolicitorDetailsGAspec respondent2 = GASolicitorDetailsGAspec.builder().id("id")
            .email(DUMMY_EMAIL).organisationIdentifier("3").build();
        GeneralApplicationsDetails generalApplicationsDetails = GeneralApplicationsDetails.builder()
            .caseState(LISTING_FOR_A_HEARING.getDisplayedValue())
            .caseLink(CaseLink.builder()
                          .caseReference(String.valueOf(CASE_ID)).build())
            .build();
        GADetailsRespondentSol gaDetailsRespondentSol = GADetailsRespondentSol.builder()
            .caseState(LISTING_FOR_A_HEARING.getDisplayedValue())
            .caseLink(CaseLink.builder()
                          .caseReference(String.valueOf(CASE_ID)).build())
            .build();
        respondentSols.add(element(respondent1));
        respondentSols.add(element(respondent2));
        return new GeneralApplicationCaseData()
            .ccdCaseReference(CASE_ID)
            .generalAppParentCaseLink(new GeneralAppParentCaseLink().setCaseReference(PARENT_CASE_ID))
            .judicialListForHearing(new GAJudgesHearingListGAspec()
                                        .setHearingPreferredLocation(DynamicList.builder()
                                                                      .value(selectedLocation).listItems(listItems)
                                                                      .build())
                                        .setHearingPreferencesPreferredType(GAJudicialHearingType.IN_PERSON)
                                        .setJudicialTimeEstimate(GAHearingDuration.MINUTES_15))
            .claimant1PartyName("Test Claimant1 Name")
            .claimant2PartyName("Test Claimant2 Name")
            .defendant1PartyName("Test Defendant1 Name")
            .defendant2PartyName("Test Defendant2 Name")
            .applicantPartyName("Test Applicant Name")
            .judgeTitle("John Doe")
            .generalAppParentCaseLink(new GeneralAppParentCaseLink()
                                          .setCaseReference(CASE_REFERENCE.toString()))
            .businessProcess(new BusinessProcess().setStatus(BusinessProcessStatus.READY)
                                 .setCamundaEvent(HEARING_SCHEDULED))
            .generalAppPBADetails(
                new GeneralApplicationPbaDetails()
                    .setFee(FEE108)
                    .setServiceReqReference(CUSTOMER_REFERENCE))
            .createdDate(LocalDateTime.now())
            .generalAppType(GAApplicationType.builder()
                                .types(singletonList(EXTEND_TIME))
                                .build())
            .generalAppApplnSolicitor(
                GASolicitorDetailsGAspec.builder().email(DUMMY_EMAIL).build())
            .applicant1OrganisationPolicy(new OrganisationPolicy().setOrganisation(new Organisation().setOrganisationID("1")))
            .respondent1OrganisationPolicy(new OrganisationPolicy().setOrganisation(new Organisation().setOrganisationID("2")))
            .respondent2OrganisationPolicy(new OrganisationPolicy().setOrganisation(new Organisation().setOrganisationID("3")))
            .judicialDecision(new GAJudicialDecision().setDecision(LIST_FOR_A_HEARING))
            .caseManagementLocation(CaseLocationCivil.builder().siteName("testing")
                                        .address("london court")
                                        .baseLocation("1")
                                        .postcode("BA 117").build())
            .generalAppRespondentAgreement(GARespondentOrderAgreement.builder().hasAgreed(NO).build())
            .generalAppInformOtherParty(GAInformOtherParty.builder().isWithNotice(NO).build())
            .businessProcess(new BusinessProcess().setCamundaEvent(HEARING_SCHEDULED))
            .applicationIsCloaked(isCloak)
            .generalAppRespondentSolicitors(respondentSols)
            .gaDetailsMasterCollection(wrapElements(GeneralApplicationsDetails.builder()
                                                        .caseState(LISTING_FOR_A_HEARING.getDisplayedValue())
                                                        .caseLink(CaseLink.builder()
                                                                      .caseReference(String.valueOf(CASE_ID)).build())
                                                        .build()))
            .claimantGaAppDetails(
                wrapElements(generalApplicationsDetails
                ))
            .respondentSolGaAppDetails(wrapElements(gaDetailsRespondentSol))
            .respondentSolTwoGaAppDetails(wrapElements(gaDetailsRespondentSol))
            .gaHearingNoticeDetail(new GAHearingNoticeDetail()
                                       .setChannel(GAJudicialHearingType.IN_PERSON)
                                       .setHearingDuration(GAHearingDuration.HOUR_1)
                                       .setHearingTimeHourMinute("1530")
                                       .setHearingDate(now().plusDays(10))
                                       .setHearingLocation(getLocationDynamicList()))
            .gaHearingNoticeApplication(new GAHearingNoticeApplication()
                                            .setHearingNoticeApplicationDate(now())
                                            .setHearingNoticeApplicationDetail(HearingApplicationDetails.CLAIMANT_AND_DEFENDANT)
                                            .setHearingNoticeApplicationType("type"))
            .gaHearingNoticeInformation("testing");
    }

    public GeneralApplicationCaseData writtenRepresentationSequentialApplication() {
        return new GeneralApplicationCaseData()
            .ccdCaseReference(CASE_ID)
            .generalAppParentCaseLink(new GeneralAppParentCaseLink().setCaseReference(PARENT_CASE_ID))
            .claimant1PartyName("Test Claimant1 Name")
            .claimant2PartyName("Test Claimant2 Name")
            .defendant1PartyName("Test Defendant1 Name")
            .defendant2PartyName("Test Defendant2 Name")
            .applicantPartyName("Test Applicant Name")
            .judgeTitle("John Doe")
            .caseManagementLocation(CaseLocationCivil.builder().siteName("testing")
                                        .address("london court")
                                        .baseLocation("1")
                                        .postcode("BA 117").build())
            .judicialByCourtsInitiativeForWrittenRep(GAByCourtsInitiativeGAspec.OPTION_1)
            .orderCourtOwnInitiativeForWrittenRep(
                new GAOrderCourtOwnInitiativeGAspec()
                    .setOrderCourtOwnInitiative("abcd")
                    .setOrderCourtOwnInitiativeDate(now()))
            .judgeRecitalText("Test Judge's recital")
            .directionInRelationToHearingText("Test written order")
            .createdDate(LocalDateTime.now())
            .generalAppType(GAApplicationType.builder()
                                .types(singletonList(EXTEND_TIME))
                                .build())
            .judicialDecision(new GAJudicialDecision().setDecision(MAKE_ORDER_FOR_WRITTEN_REPRESENTATIONS))
            .judicialDecisionMakeAnOrderForWrittenRepresentations(
                new GAJudicialWrittenRepresentations()
                    .setWrittenOption(SEQUENTIAL_REPRESENTATIONS)
                    .setWrittenSequentailRepresentationsBy(now())
                    .setSequentialApplicantMustRespondWithin(now()
                                                              .plusDays(5)))
            .submittedOn(APPLICATION_SUBMITTED_DATE);
    }

    public GeneralApplicationCaseData approveApplication() {
        return new GeneralApplicationCaseData()
            .ccdCaseReference(CASE_ID)
            .claimant1PartyName("Test Claimant1 Name")
            .claimant2PartyName("Test Claimant2 Name")
            .defendant1PartyName("Test Defendant1 Name")
            .defendant2PartyName("Test Defendant2 Name")
            .applicantPartyName("Test Applicant Name")
            .caseManagementLocation(CaseLocationCivil.builder().siteName("testing")
                                        .address("london court")
                                        .postcode("BA 117").build())
            .judicialByCourtsInitiativeForWrittenRep(GAByCourtsInitiativeGAspec.OPTION_1)
            .orderCourtOwnInitiativeForWrittenRep(
                new GAOrderCourtOwnInitiativeGAspec()
                    .setOrderCourtOwnInitiative("abcd")
                    .setOrderCourtOwnInitiativeDate(LocalDate.now()))
            .judgeRecitalText("Test Judge's recital")
            .directionInRelationToHearingText("Test written order")
            .createdDate(LocalDateTime.now())
            .generalAppType(GAApplicationType.builder()
                                .types(singletonList(EXTEND_TIME))
                                .build())
            .judicialDecision(new GAJudicialDecision().setDecision(MAKE_AN_ORDER))
            .judicialDecisionMakeOrder(new GAJudicialMakeAnOrder().setMakeAnOrder(GAJudgeMakeAnOrderOption.APPROVE_OR_EDIT))
            .submittedOn(APPLICATION_SUBMITTED_DATE);
    }

    public GeneralApplicationCaseData writtenRepresentationConcurrentApplication() {
        return new GeneralApplicationCaseData()
            .ccdCaseReference(CASE_ID)
            .generalAppParentCaseLink(new GeneralAppParentCaseLink().setCaseReference(PARENT_CASE_ID))
            .claimant1PartyName("Test Claimant1 Name")
            .claimant2PartyName("Test Claimant2 Name")
            .defendant1PartyName("Test Defendant1 Name")
            .judgeTitle("John Doe")
            .caseManagementLocation(CaseLocationCivil.builder().siteName("testing")
                                        .address("london court")
                                        .baseLocation("2")
                                        .postcode("BA 117").build())
            .judicialByCourtsInitiativeForWrittenRep(GAByCourtsInitiativeGAspec.OPTION_1)
            .orderCourtOwnInitiativeForWrittenRep(
                new GAOrderCourtOwnInitiativeGAspec()
                    .setOrderCourtOwnInitiative("abcd")
                    .setOrderCourtOwnInitiativeDate(now()))
            .defendant2PartyName("Test Defendant2 Name")
            .applicantPartyName("Test Applicant Name")
            .createdDate(LocalDateTime.now())
            .judgeRecitalText("Test Judge's recital")
            .directionInRelationToHearingText("Test written order")
            .generalAppType(GAApplicationType.builder()
                                .types(singletonList(EXTEND_TIME))
                                .build())
            .judicialDecision(new GAJudicialDecision().setDecision(MAKE_ORDER_FOR_WRITTEN_REPRESENTATIONS))
            .judicialDecisionMakeAnOrderForWrittenRepresentations(
                new GAJudicialWrittenRepresentations()
                    .setWrittenOption(CONCURRENT_REPRESENTATIONS)
                    .setWrittenConcurrentRepresentationsBy(now())
                    )
            .submittedOn(APPLICATION_SUBMITTED_DATE);
    }

    public GeneralApplicationCaseData requestForInformationApplication() {
        return new GeneralApplicationCaseData()
            .ccdCaseReference(CASE_ID)
            .generalAppParentCaseLink(new GeneralAppParentCaseLink().setCaseReference(PARENT_CASE_ID))
            .claimant1PartyName("Test Claimant1 Name")
            .locationName("Nottingham County Court and Family Court (and Crown)")
            .caseManagementLocation(CaseLocationCivil.builder().siteName("testing")
                                        .address("london court")
                                        .baseLocation("2")
                                        .postcode("BA 117").build())
            .claimant2PartyName("Test Claimant2 Name")
            .defendant1PartyName("Test Defendant1 Name")
            .defendant2PartyName("Test Defendant2 Name")
            .applicantPartyName("Test Applicant Name")
            .businessProcess(new BusinessProcess().setStatus(BusinessProcessStatus.READY))
            .generalAppPBADetails(
                new GeneralApplicationPbaDetails()
                    .setFee(FEE275)
                    .setServiceReqReference(CUSTOMER_REFERENCE))
            .createdDate(LocalDateTime.now())
            .generalAppType(GAApplicationType.builder()
                                .types(singletonList(EXTEND_TIME))
                                .build())
            .judicialDecision(new GAJudicialDecision().setDecision(REQUEST_MORE_INFO))
            .generalAppRespondentAgreement(GARespondentOrderAgreement.builder().hasAgreed(NO).build())
            .generalAppInformOtherParty(GAInformOtherParty.builder().isWithNotice(YES).build())
            .judicialDecisionRequestMoreInfo(new GAJudicialRequestMoreInfo()
                                                 .setJudgeRecitalText(JUDICIAL_REQUEST_MORE_INFO_RECITAL_TEXT)
                                                 .setRequestMoreInfoOption(REQUEST_MORE_INFORMATION)
                                                 .setJudgeRequestMoreInfoByDate(now())
                                                 .setJudgeRequestMoreInfoText("test"))
            .submittedOn(APPLICATION_SUBMITTED_DATE);
    }

    public GeneralApplicationCaseData judicialDecisionWithUncloakRequestForInformationApplication(
        GAJudgeRequestMoreInfoOption requestMoreInfoOption, YesOrNo isWithNotice, YesOrNo isCloak) {

        return new GeneralApplicationCaseData()
            .ccdCaseReference(CASE_ID)
            .claimant1PartyName("Test Claimant1 Name")
            .claimant2PartyName("Test Claimant2 Name")
            .defendant1PartyName("Test Defendant1 Name")
            .defendant2PartyName("Test Defendant2 Name")
            .applicantPartyName("Test Applicant Name")
            .businessProcess(new BusinessProcess().setStatus(BusinessProcessStatus.READY)
                                 .setCamundaEvent(JUDGES_DECISION))
            .generalAppPBADetails(
                new GeneralApplicationPbaDetails()
                    .setFee(FEE108)
                    .setServiceReqReference(CUSTOMER_REFERENCE))
            .createdDate(LocalDateTime.now())
            .generalAppType(GAApplicationType.builder()
                                .types(singletonList(EXTEND_TIME))
                                .build())
            .judicialDecision(new GAJudicialDecision().setDecision(REQUEST_MORE_INFO))
            .generalAppRespondentAgreement(GARespondentOrderAgreement.builder().hasAgreed(YesOrNo.NO).build())
            .generalAppInformOtherParty(GAInformOtherParty.builder().isWithNotice(isWithNotice).build())
            .judicialDecisionRequestMoreInfo(new GAJudicialRequestMoreInfo()
                                                 .setRequestMoreInfoOption(requestMoreInfoOption)
                                                 .setJudgeRequestMoreInfoByDate(LocalDate.now())
                                                 .setJudgeRequestMoreInfoText("test"))
            .businessProcess(new BusinessProcess().setCamundaEvent(JUDGES_DECISION))
            .applicationIsCloaked(isCloak)
            .isMultiParty(NO)
            .submittedOn(APPLICATION_SUBMITTED_DATE);
    }

    public GeneralApplicationCaseData judicialOrderMadeWithUncloakApplication(YesOrNo isCloak) {
        return new GeneralApplicationCaseData()
            .ccdCaseReference(CASE_ID)
            .claimant1PartyName("Test Claimant1 Name")
            .claimant2PartyName("Test Claimant2 Name")
            .defendant1PartyName("Test Defendant1 Name")
            .defendant2PartyName("Test Defendant2 Name")
            .applicantPartyName("Test Applicant Name")
            .isGaRespondentOneLip(NO)
            .generalAppParentCaseLink(new GeneralAppParentCaseLink()
                                          .setCaseReference(CASE_REFERENCE.toString()))
            .businessProcess(new BusinessProcess().setStatus(BusinessProcessStatus.READY)
                                 .setCamundaEvent(JUDGES_DECISION))
            .generalAppPBADetails(
                new GeneralApplicationPbaDetails()
                    .setFee(FEE108)
                    .setServiceReqReference(CUSTOMER_REFERENCE))
            .createdDate(LocalDateTime.now())
            .generalAppType(GAApplicationType.builder()
                                .types(singletonList(EXTEND_TIME))
                                .build())
            .judicialDecision(new GAJudicialDecision().setDecision(MAKE_AN_ORDER))
            .generalAppRespondentAgreement(GARespondentOrderAgreement.builder().hasAgreed(NO).build())
            .generalAppInformOtherParty(GAInformOtherParty.builder().isWithNotice(NO).build())
            .businessProcess(new BusinessProcess().setCamundaEvent(JUDGES_DECISION))
            .applicationIsCloaked(isCloak)
            .submittedOn(APPLICATION_SUBMITTED_DATE)
            .gaDetailsMasterCollection(wrapElements(GeneralApplicationsDetails.builder()
                                                        .caseState(APPLICATION_ADD_PAYMENT.getDisplayedValue())
                                                        .caseLink(CaseLink.builder()
                                                                      .caseReference(String.valueOf(CASE_ID)).build())
                                                        .build()))
            .claimantGaAppDetails(wrapElements(GeneralApplicationsDetails.builder()
                                                   .caseState(APPLICATION_ADD_PAYMENT.getDisplayedValue())
                                                   .caseLink(CaseLink.builder()
                                                                 .caseReference(String.valueOf(CASE_ID)).build())
                                                   .build()));
    }

    public GeneralApplicationCaseData adjournOrVacateHearingApplication(
        YesOrNo isRespondentAgreed, LocalDate gaHearingDate) {
        GAHearingDateGAspec generalAppHearingDate = GAHearingDateGAspec.builder()
            .hearingScheduledDate(gaHearingDate)
            .build();
        return new GeneralApplicationCaseData()
            .ccdCaseReference(CASE_ID)
            .claimant1PartyName("Test Claimant1 Name")
            .defendant1PartyName("Test Defendant1 Name")
            .businessProcess(new BusinessProcess().setStatus(BusinessProcessStatus.READY))
            .generalAppPBADetails(
                new GeneralApplicationPbaDetails()
                    .setFee(FEE108)
                    .setServiceReqReference(CUSTOMER_REFERENCE))
            .createdDate(LocalDateTime.now())
            .generalAppType(GAApplicationType.builder()
                                .types(singletonList(ADJOURN_HEARING))
                                .build())
            .generalAppHearingDate(generalAppHearingDate)
            .generalAppRespondentAgreement(GARespondentOrderAgreement
                                               .builder().hasAgreed(isRespondentAgreed).build())
            .businessProcess(new BusinessProcess().setCamundaEvent(JUDGES_DECISION))
            .submittedOn(APPLICATION_SUBMITTED_DATE);
    }

    public GeneralApplicationCaseData varyApplication(List<GeneralApplicationTypes> types) {
        return new GeneralApplicationCaseData()
            .ccdCaseReference(CASE_ID)
            .claimant1PartyName("Test Claimant1 Name")
            .defendant1PartyName("Test Defendant1 Name")
            .businessProcess(new BusinessProcess().setStatus(BusinessProcessStatus.READY))
            .generalAppPBADetails(
                new GeneralApplicationPbaDetails()
                    .setFee(FEE14)
                    .setServiceReqReference(CUSTOMER_REFERENCE))
            .createdDate(LocalDateTime.now())
            .generalAppType(GAApplicationType.builder()
                                .types(types)
                                .build())
            .generalAppRespondentAgreement(GARespondentOrderAgreement
                                               .builder().hasAgreed(NO).build())
            .businessProcess(new BusinessProcess().setCamundaEvent(JUDGES_DECISION))
            .submittedOn(APPLICATION_SUBMITTED_DATE);
    }

    public GeneralApplicationCaseData getMainCaseDataWithDetails(
        boolean withGADetails,
        boolean withGADetailsResp,
        boolean withGADetailsResp2,
        boolean withGADetailsMaster) {

        GeneralApplicationCaseData caseDataBuilder = build().copy();
        caseDataBuilder.ccdCaseReference(1L);
        GeneralApplicationsDetails generalApplicationsDetails = GeneralApplicationsDetails.builder()
            .caseState(LISTING_FOR_A_HEARING.getDisplayedValue())
            .caseLink(CaseLink.builder()
                          .caseReference(String.valueOf(CASE_ID)).build())
            .build();

        if (withGADetails) {
            caseDataBuilder.claimantGaAppDetails(
                wrapElements(generalApplicationsDetails
                ));
        }

        if (withGADetailsMaster) {
            caseDataBuilder.gaDetailsMasterCollection(
                wrapElements(generalApplicationsDetails
                ));
        }

        GADetailsRespondentSol gaDetailsRespondentSol = GADetailsRespondentSol.builder()
            .caseState(LISTING_FOR_A_HEARING.getDisplayedValue())
            .caseLink(CaseLink.builder()
                          .caseReference(String.valueOf(CASE_ID)).build())
            .build();
        if (withGADetailsResp) {
            caseDataBuilder.respondentSolGaAppDetails(wrapElements(gaDetailsRespondentSol));
        }

        if (withGADetailsResp2) {
            caseDataBuilder.respondentSolTwoGaAppDetails(wrapElements(gaDetailsRespondentSol));
        }
        return caseDataBuilder;
    }

    public DynamicList getLocationDynamicList() {
        DynamicListElement location1 = DynamicListElement.builder()
            .code(String.valueOf(UUID.randomUUID())).label("ABCD - RG0 0AL").build();
        DynamicListElement location2 = DynamicListElement.builder()
            .code(String.valueOf(UUID.randomUUID())).label("PQRS - GU0 0EE").build();
        DynamicListElement location3 = DynamicListElement.builder()
            .code(String.valueOf(UUID.randomUUID())).label("WXYZ - EW0 0HE").build();
        DynamicListElement location4 = DynamicListElement.builder()
            .code(String.valueOf(UUID.randomUUID())).label("LMNO - NE0 0BH").build();

        return DynamicList.builder()
            .listItems(List.of(location1, location2, location3, location4))
            .value(location1).build();
    }

    public GeneralApplicationCaseData buildJudicialDecisionRequestMoreInfo() {
        return new GeneralApplicationCaseData()
            .ccdState(CaseState.APPLICATION_SUBMITTED_AWAITING_JUDICIAL_DECISION)
            .judicialDecisionRequestMoreInfo(new GAJudicialRequestMoreInfo().setRequestMoreInfoOption(
                REQUEST_MORE_INFORMATION).setJudgeRequestMoreInfoByDate(LocalDate.of(2024, 9, 4))).build();
    }

    public GeneralApplicationCaseData buildCaseWorkerHearingScheduledInfo() {
        return new GeneralApplicationCaseData()
            .ccdState(LISTING_FOR_A_HEARING)
            .gaHearingNoticeDetail(new GAHearingNoticeDetail().setHearingDate(
                (LocalDate.of(2024, 9, 4)))).build();
    }
}
