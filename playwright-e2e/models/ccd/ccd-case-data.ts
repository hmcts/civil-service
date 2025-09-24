export default interface CCDCaseData {
  id?: number;
  respondent1PaymentDateToStringSpec?: string;
  respondent1ClaimResponseTypeForSpec?: string;
  respondent1DQRemoteHearingLRspec?: DQRemoteHearing;
  addRespondent2?: string;
  submittedDate?: string;
  solicitorReferences?: SolicitorReferences;
  featureToggleWA?: string;
  respondent1OrgRegistered?: string;
  applicantSolicitor1UserDetails?: ApplicantSolicitor1UserDetails;
  applicantSolicitor1PbaAccounts?: ApplicantSolicitor1PbaAccounts;
  detailsOfClaim?: string;
  caseFlags?: CaseFlags;
  claimFee?: ClaimFee;
  respondent1Experts?: ExpertAndWitness[];
  respondent1Witnesses?: ExpertAndWitness[];
  respondent2Experts?: ExpertAndWitness[];
  respondent2Witnesses?: ExpertAndWitness[];
  respondent1DQVulnerabilityQuestions?: DQVulnerabilityQuestions;
  respondent1DQHearingSupport?: DQHearingSupport;
  respondent1DQHearing?: DQHearing;
  respondent1DQFixedRecoverableCosts?: DQFixedRecoverableCosts;
  respondent1DQLanguage?: DQLanguage;
  respondent1DQRemoteHearing?: DQRemoteHearing;
  applicantWitnesses?: ExpertAndWitness[];
  caseManagementLocation?: CaseManagementLocation;
  caseManagementCategory?: CaseManagementCategory;
  respondent1ResponseDate?: string;
  applicantSolicitor1PbaAccountsIsEmpty?: string;
  CaseAccessCategory?: string;
  multiPartyResponseTypeFlags?: string;
  applicant1?: ClaimantDefendant;
  applicant1LitigationFriend?: LitigationFriend;
  applicant2?: ClaimantDefendant;
  applicant2LitigationFriend?: LitigationFriend;
  issueDate?: string;
  duplicateSystemGeneratedCaseDocs?: SystemGeneratedCaseDocument[];
  respondent2OrganisationPolicy?: OrganisationPolicy;
  locationName?: string;
  claimNotificationDate?: string;
  applicant1ResponseDate?: string;
  legacyCaseReference?: string;
  respondent1Represented?: string;
  applicant1OrganisationPolicy?: OrganisationPolicy;
  respondent1?: ClaimantDefendant;
  respondent1LitigationFriend?: LitigationFriend;
  respondent2?: ClaimantDefendant;
  respondent2LitigationFriend?: LitigationFriend;
  respondent1DQExperts?: DQExperts;
  specAoSRespondentCorrespondenceAddressRequired?: string;
  applicantDefenceResponseDocumentAndDQFlag?: string;
  specRespondent1Represented?: string;
  respondent1ResponseDeadline?: string;
  respondent2ResponseDeadline?: string;
  specPaidLessAmountOrDisputesOrPartAdmission?: string;
  specFullDefenceOrPartAdmission?: string;
  respondentClaimResponseTypeForSpecGeneric?: string;
  respondent1ClaimResponsePaymentAdmissionForSpec?: string;
  showCarmFields?: string;
  specAoSApplicantCorrespondenceAddressRequired?: string;
  applicant1DQRemoteHearingLRspec?: DQRemoteHearing;
  applicantSolicitor1ClaimStatementOfTruth?: ClaimStatementOfTruth;
  responseClaimCourtLocationRequired?: string;
  paymentTypePBASpec?: string;
  applicant1DQHearingSupport?: DQHearingSupport;
  claimNotificationDeadline?: string;
  specDefenceFullAdmittedRequired?: string;
  responseClaimTrack?: string;
  caseNamePublic?: string;
  claimIssuedPaymentDetails?: ClaimIssuedPaymentDetails;
  applicant1ProceedWithClaim?: string;
  specApplicantCorrespondenceAddressRequired?: string;
  totalClaimAmount?: number;
  applicant1ClaimMediationSpecRequired?: ClaimMediationSpecRequired;
  defenceRouteRequired?: string;
  applicant1DQLanguage?: DQLanguage;
  applicant1DQVulnerabilityQuestions?: DQVulnerabilityQuestions;
  respondent1DQHearingSmallClaim?: DQHearing;
  specFullDefenceOrPartAdmission1V1?: string;
  systemGeneratedCaseDocuments?: SystemGeneratedCaseDocument[];
  showResponseOneVOneFlag?: string;
  specFullAdmissionOrPartAdmission?: string;
  applicant1DQWitnessesSmallClaim?: DQWitnesses;
  applicant1DQExperts?: DQExperts;
  applicantExperts?: ExpertAndWitness[];
  respondent1DQWitnessesSmallClaim?: DQWitness;
  urgentFlag?: string;
  responseClaimMediationSpecRequired?: string;
  respondent1DQWitnesses?: DQWitnesses;
  respondent1DQDisclosureReport?: DQDisclosureReport;
  allPartyNames?: string;
  applicant1DQDisclosureOfNonElectronicDocuments?: DQDisclosureOfNonElectronicDocuments;
  servedDocumentFiles?: ServedDocumentFiles;
  claimType?: string;
  defendantResponseDocuments?: SystemGeneratedCaseDocument[];
  duplicateClaimantDefResponseDocs?: SystemGeneratedCaseDocument[];
  unassignedCaseListDisplayOrganisationReferences?: string;
  applicant1LitigationFriendRequired?: string;
  defendant1LIPAtClaimIssued?: string;
  defendantSolicitorNotifyClaimDetailsOptions?: NotifyClaimDetailsOptions;
  claimDetailsNotificationDate?: string;
  applicant1DQFileDirectionsQuestionnaire?: DQFileDirectionsQuestionnaire;
  claimDetailsNotificationDeadline?: string;
  respondent1DQFurtherInformation?: DQFurtherInformation;
  claimantResponseDocuments?: SystemGeneratedCaseDocument[];
  paymentTypePBA?: string;
  claimTypeUnSpec?: string;
  courtLocation?: CourtLocation;
  caseListDisplayDefendantSolicitorReferences?: string;
  claimValue?: ClaimValue;
  respondent1ClaimResponseIntentionType?: string;
  respondent2ClaimResponseIntentionType?: string;
  respondent1AcknowledgeNotificationDate?: string;
  respondent2AcknowledgeNotificationDate?: string;
}

export interface ServedDocumentFiles {
  particularsOfClaimDocument?: Document[];
}

export interface SolicitorReferences {
  applicantSolicitor1Reference?: string;
  respondentSolicitor1Reference?: string;
}

export interface Document {
  id?: string;
  value?: UploadDocumentValue;
}

export interface ApplicantSolicitor1PbaAccounts {
  value?: ApplicantSolicitor1PbaAccountsValue;
  list_items?: ApplicantSolicitor1PbaAccountsListItem[];
}

export interface ApplicantSolicitor1PbaAccountsValue {
  code?: string;
  label?: string;
}

export interface ApplicantSolicitor1PbaAccountsListItem {
  code?: string;
  label?: string;
}

export interface ClaimStatementOfTruth {
  name?: string;
  role?: string;
}

export interface ClaimIssuedPaymentDetails {
  status?: string;
  reference?: string;
  customerReference?: string;
}

export interface ClaimValue {
  statementOfValueInPennies?: string;
}

export interface ClaimMediationSpecRequired {
  hasAgreedFreeMediation?: string;
}

export interface NotifyClaimDetailsOptions {
  value?: NotifyClaimDetailsOptionsValue;
}

export interface NotifyClaimDetailsOptionsValue {
  code?: string;
  label?: string;
}

export interface ExpertAndWitness {
  id?: string;
  value?: ExpertAndWitnessValue;
}

export interface ExpertAndWitnessValue {
  email?: string;
  flags?: CaseFlags;
  phone?: string;
  partyID?: string;
  lastName?: string;
  firstName?: string;
}

export interface LitigationFriend {
  flags?: CaseFlags;
  partyID?: string;
  lastName?: string;
  firstName?: string;
  phoneNumber?: string;
  emailAddress?: string;
  primaryAddress?: Address;
  certificateOfSuitability?: Document[];
  hasSameAddressAsLitigant?: string;
}
export interface ClaimantDefendant {
  type?: string;
  flags?: CaseFlags;
  partyID?: string;
  partyName?: string;
  partyEmail?: string;
  companyName?: string;
  primaryAddress?: Address;
  individualTitle?: string;
  individualLastName?: string;
  individualFirstName?: string;
  partyTypeDisplayValue?: string;
}

export interface UploadDocumentValue {
  category_id?: string;
  document_url?: string;
  upload_timestamp?: string;
  document_filename?: string;
  document_binary_url?: string;
}

export interface ApplicantSolicitor1UserDetails {
  email?: string;
}

export interface SystemGeneratedCaseDocument {
  id?: string;
  value?: SystemGeneratedCaseDocumentValue;
}

export interface SystemGeneratedCaseDocumentValue {
  createdBy?: string;
  documentLink?: SystemGeneratedDocumentDocumentLink;
  documentName?: string;
  documentSize?: number;
  documentType?: string;
  createdDatetime?: string;
}

export interface SystemGeneratedDocumentDocumentLink {
  category_id?: string;
  document_url?: string;
  upload_timestamp?: string;
  document_filename?: string;
  document_binary_url?: string;
}

export interface OrganisationPolicy {
  Organisation?: Organisation;
  OrgPolicyReference?: string;
  OrgPolicyCaseAssignedRole?: string;
}

export interface Organisation {
  OrganisationID?: string;
}

export interface ClaimFee {
  code?: string;
  version?: number;
  calculatedAmountInPence?: number;
}

export interface CaseManagementLocation {
  region?: number;
  baseLocation?: number;
}

export interface CaseManagementCategory {
  value?: CaseManagementCategoryValue;
  list_items?: CaseManagementCategoryListItem[];
}

export interface CaseManagementCategoryValue {
  code?: string;
  label?: string;
}

export interface CaseManagementCategoryListItem {
  id?: string;
  value?: CaseManagementCategoryListItemValue;
}

export interface CaseManagementCategoryListItemValue {
  code?: string;
  label?: string;
}

export interface HearingAttendee {
  id?: string;
  value?: HearingAttendeeValue;
}

export interface HearingAttendeeValue {
  email?: string;
  flags?: CaseFlags;
  phone?: string;
  partyID?: string;
  lastName?: string;
  firstName?: string;
}

export interface CaseFlags {
  details?: CCDCaseFlagsDetails[];
  partyName?: string;
  roleOnCase?: string;
}

export interface CCDCaseFlagsDetails {
  id?: string;
  value?: CaseFlagsDetailsValue;
}

export interface CaseFlagsDetailsValue {
  name?: string;
  path?: CaseFlagsDetailsValuePath[];
  status?: string;
  flagCode?: string;
  flagComment?: string;
  dateTimeCreated?: string;
  hearingRelevant?: string;
  otherDescription?: string;
}

export interface CaseFlagsDetailsValuePath {
  id?: string;
  value?: string;
}

export interface Address {
  County?: string;
  Country?: string;
  PostCode?: string;
  PostTown?: string;
  AddressLine1?: string;
  AddressLine2?: string;
  AddressLine3?: string;
}

export interface DQExperts {
  details?: DQExpertsDetails[];
  expertRequired?: string;
  expertReportsSent?: string;
  jointExpertSuitable?: string;
}

export interface DQExpertsDetails {
  id?: string;
  value?: DQExpertsDetailsValue;
}

export interface DQExpertsDetailsValue {
  partyID?: string;
  lastName?: string;
  dateAdded?: string;
  firstName?: string;
  eventAdded?: string;
  phoneNumber?: string;
  whyRequired?: string;
  emailAddress?: string;
  estimatedCost?: string;
  fieldOfExpertise?: string;
}

export interface DQHearingSupport {
  supportRequirements?: string;
  supportRequirementsAdditional?: string;
}

export interface DQVulnerabilityQuestions {
  vulnerabilityAdjustments?: string;
  vulnerabilityAdjustmentsRequired?: string;
}

export interface DQWitnesses {
  details?: DQWitness[];
  witnessesToAppear?: string;
}

export interface DQWitness {
  id?: string;
  value?: {
    partyID?: string;
    lastName?: string;
    dateAdded?: string;
    firstName?: string;
    eventAdded?: string;
    phoneNumber?: string;
    emailAddress?: string;
    reasonForWitness?: string;
  };
}

export interface DQHearing {
  unavailableDates?: DQHearingUnavailableDate[];
  unavailableDatesRequired?: string;
}

export interface DQHearingUnavailableDate {
  id?: string;
  value?: DQHearingUnavailableDateValue;
}

export interface DQHearingUnavailableDateValue {
  date?: string;
  unavailableDateType?: string;
  toDate?: string;
  fromDate?: string;
}

export interface DQFixedRecoverableCosts {
  band?: string;
  reasons?: string;
  complexityBandingAgreed?: string;
  isSubjectToFixedRecoverableCostRegime?: string;
}

export interface DQLanguage {
  court?: string;
  documents?: string;
}

export interface DQRemoteHearing {
  reasonForRemoteHearing?: string;
  remoteHearingRequested?: string;
}

export interface DQDisclosureReport {
  disclosureProposalAgreed?: string;
  disclosureFormFiledAndServed?: string;
}

export interface DQDisclosureOfNonElectronicDocuments {
  bespokeDirections?: string;
  standardDirectionsRequired?: string;
  directionsForDisclosureProposed?: string;
}

export interface DQFileDirectionsQuestionnaire {
  explainedToClient?: string[];
  oneMonthStayRequested?: string;
  reactionProtocolCompliedWith?: string;
}

export interface DQFurtherInformation {
  futureApplications?: string;
  otherInformationForJudge?: string;
  reasonForFutureApplications?: string;
}

export interface CourtLocation {
  caseLocation?: CaseManagementLocation;
  applicantPreferredCourt?: string;
}
