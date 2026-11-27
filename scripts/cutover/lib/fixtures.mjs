import { randomUUID } from 'node:crypto';

/*
 * Case data for the seeded cases, taken from civil-ccd-definition's API test fixtures:
 * e2e/fixtures/events/createClaimSpecSmall.js (ONE_V_ONE) and
 * e2e/fixtures/ga-events/ga-ccd/createGeneralApplicationLR.js (createGADataWithoutNotice).
 */

function address(postFixLineOne) {
  return {
    AddressLine1: `Flat 2 - ${postFixLineOne}`,
    AddressLine2: 'Caversham House 15-17',
    AddressLine3: 'Church Road',
    PostTown: 'Reading',
    County: 'Kent',
    Country: 'United Kingdom',
    PostCode: 'RG4 7AA'
  };
}

function listElement(label) {
  return { code: randomUUID(), label };
}

/** CREATE_CLAIM_SPEC's pages, in order, with what the claimant's solicitor enters on each. */
export function createSpecClaimPages(applicantSolicitor, defendantSolicitor) {
  const validPba = listElement('PBAFUNC12345');
  return {
    References: {
      CaseAccessCategory: 'SPEC_CLAIM',
      solicitorReferences: {
        applicantSolicitor1Reference: 'Cutover applicant reference',
        respondentSolicitor1Reference: 'Cutover respondent reference'
      }
    },
    Claimant: {
      applicant1: {
        type: 'COMPANY',
        companyName: 'Test Inc',
        primaryAddress: address('applicant'),
        partyName: 'Test Inc',
        partyTypeDisplayValue: 'Company'
      }
    },
    AddAnotherClaimant: { addApplicant2: 'No' },
    Notifications: {
      applicantSolicitor1CheckEmail: { correct: 'No' },
      applicantSolicitor1UserDetails: { email: applicantSolicitor.email }
    },
    ClaimantSolicitorOrganisation: {
      applicant1OrganisationPolicy: {
        OrgPolicyReference: 'Claimant policy reference',
        OrgPolicyCaseAssignedRole: '[APPLICANTSOLICITORONE]',
        Organisation: { OrganisationID: applicantSolicitor.orgId }
      }
    },
    specCorrespondenceAddress: { specApplicantCorrespondenceAddressRequired: 'No' },
    Defendant: {
      respondent1: {
        type: 'INDIVIDUAL',
        individualFirstName: 'John',
        individualLastName: 'Doe',
        individualTitle: 'Sir',
        partyEmail: 'johndoe@example.com',
        partyPhone: '07898678902',
        primaryAddress: address('respondent'),
        partyName: 'Sir John Doe',
        partyTypeDisplayValue: 'Individual'
      }
    },
    LegalRepresentation: { specRespondent1Represented: 'Yes' },
    DefendantSolicitorOrganisation: {
      respondent1OrgRegistered: 'Yes',
      respondent1OrganisationPolicy: {
        OrgPolicyReference: 'Defendant policy reference',
        OrgPolicyCaseAssignedRole: '[RESPONDENTSOLICITORONE]',
        Organisation: { OrganisationID: defendantSolicitor.orgId }
      }
    },
    DefendantSolicitorEmail: { respondentSolicitor1EmailAddress: 'civilunspecified@gmail.com' },
    specRespondentCorrespondenceAddress: { specRespondentCorrespondenceAddressRequired: 'No' },
    AddAnotherDefendant: { addRespondent2: 'No' },
    Details: { detailsOfClaim: 'Cutover verification claim' },
    ClaimTimeline: {
      timelineOfEvents: [{ value: { timelineDate: '2021-02-01', timelineDescription: 'event 1' } }]
    },
    EvidenceList: {
      speclistYourEvidenceList: [{
        value: { evidenceType: 'CONTRACTS_AND_AGREEMENTS', contractAndAgreementsEvidence: 'evidence details' }
      }]
    },
    ClaimAmount: {
      claimAmountBreakup: [{ value: { claimReason: 'amount reason', claimAmount: '85000' } }]
    },
    ClaimInterest: { claimInterest: 'No' },
    InterestSummary: {
      claimIssuedPaymentDetails: { customerReference: 'Cutover applicant reference' },
      paymentTypePBASpec: 'PBAv3'
    },
    PbaNumber: {
      applicantSolicitor1PbaAccounts: { list_items: [validPba, listElement('PBA0078095')], value: validPba }
    },
    StatementOfTruth: {
      uiStatementOfTruth: { name: 'John Doe', role: 'Test Solicitor' }
    }
  };
}

export function specClaimPaymentUpdate(caseId) {
  return {
    service_request_reference: '1324646546456',
    ccd_case_number: caseId,
    service_request_amount: '167.00',
    service_request_status: 'paid',
    payment: {
      payment_amount: 167.00,
      payment_reference: '13213223',
      payment_method: 'by account',
      case_reference: 'example of case ref'
    }
  };
}

/** A legal representative's application without notice, so it goes straight to a judge once paid. */
export function createGeneralApplication() {
  const types = { types: ['SUMMARY_JUDGEMENT', 'EXTEND_TIME'] };
  return {
    generalAppType: types,
    generalAppTypeLR: types,
    generalAppRespondentAgreement: { hasAgreed: 'No' },
    generalAppUrgencyRequirement: {
      generalAppUrgency: 'No',
      urgentAppConsiderationDate: null,
      reasonsForUrgency: null,
      ConsentAgreementCheckBox: []
    },
    generalAppInformOtherParty: { isWithNotice: 'No', reasonsForWithoutNotice: 'Cutover verification' },
    generalAppDetailsOfOrder: 'Test Order details',
    generalAppReasonsOfOrder: 'Test reason for order',
    generalAppEvidenceDocument: [],
    generalAppStatementOfTruthConsent: ['ConsentAgreementCheckBox'],
    generalAppStatementOfTruth: { name: 'John Doe', role: 'Test Solicitor' },
    generalAppHearingDetails: {
      hearingYesorNo: 'No',
      hearingDate: null,
      judgeName: null,
      trialRequiredYesOrNo: 'No',
      trialDateFrom: null,
      trialDateTo: null,
      HearingPreferencesPreferredType: 'IN_PERSON',
      TelephoneHearingPreferredType: null,
      ReasonForPreferredHearingType: 'sdsd',
      HearingPreferredLocation: null,
      HearingDetailsTelephoneNumber: '07446778166',
      HearingDetailsEmailID: 'update@gh.com',
      HearingDuration: 'MINUTES_15',
      generalAppHearingDays: null,
      generalAppHearingHours: null,
      generalAppHearingMinutes: null,
      unavailableTrialRequiredYesOrNo: 'No',
      vulnerabilityQuestionsYesOrNo: 'Yes',
      vulnerabilityQuestion: 'Test Test',
      SupportRequirementSignLanguage: null,
      SupportRequirementLanguageInterpreter: null,
      SupportRequirementOther: null,
      generalAppUnavailableDates: [],
      SupportRequirement: []
    },
    generalAppPBADetails: {
      paymentSuccessfulDate: null,
      fee: { calculatedAmountInPence: '11900', code: 'FEE0443', version: '2' },
      paymentDetails: {
        status: null,
        reference: null,
        errorMessage: null,
        errorCode: null,
        customerReference: null
      },
      serviceRequestReference: null
    }
  };
}

export function gaPaymentUpdate(gaCaseId) {
  return {
    service_request_reference: '1324646546456',
    ccd_case_number: gaCaseId,
    service_request_amount: '119.00',
    service_request_status: 'Paid',
    payment: {
      customer_reference: '13246546',
      date_created: '2022-07-26T19:21:50.141Z',
      date_updated: '2022-07-26T19:21:50.141Z',
      payment_reference: '13213223'
    }
  };
}
