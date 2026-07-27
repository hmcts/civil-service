const config = require('../../config.js');
const address = require('../address');

const Address = {
  AddressLine1: `${address.buildingAndStreet.lineOne}`, //SDT:claimant.address.line1, defendant1.address.line1, defendant2.address.line1
  AddressLine2: address.buildingAndStreet.lineTwo, //SDT:claimant.address.line2, defendant1.address.line2, defendant2.address.line2
  AddressLine3: address.buildingAndStreet.lineThree, //SDT:claimant.address.line3, defendant1.address.line3, defendant2.address.line3
  PostTown: address.town, //SDT:claimant.address.line4, defendant1.address.line4, defendant2.address.line4
  PostCode: address.postcode //SDT:claimant.address.postcode, defendant1.address.postcode, defendant2.address.postcode
};

const applicant1 = {
  type: 'ORGANISATION',
  organisationName: 'bulk claim org', //SDT: name
  primaryAddress: Address
};

const respondent1 = {
  type: 'INDIVIDUAL',
  individualFirstName: 'John', //SDT: defendant1.name
  individualLastName: 'Doe',
  individualTitle: 'Sir',
  primaryAddress: Address
};

const respondent2 = {
  type: 'INDIVIDUAL',
  individualFirstName: 'James', //SDT: defendant2.name
  individualLastName: 'Cameron',
  individualTitle: 'Dr',
  primaryAddress: Address
};

const claimAmount = '1512';
const solicitor1Email = 'hmcts.civil+organisation.1.solicitor.1@gmail.com';

module.exports = {
  createClaimBulk: (mpScenario, withInterest) => {
    if (mpScenario === 'ONE_V_ONE' && withInterest === false) {
      console.log('1 v 1 claim, with no interest');
      return {
          sdtRequestIdFromSdt: 'testRequestID', // from SDT: sdtRequestIdFromSdt
          CaseAccessCategory: 'SPEC_CLAIM',
          solicitorReferences: {
            applicantSolicitor1Reference: 'SDT:claimReference', // from  SDT: claimReference
          },
          applicant1: applicant1,  // SDT: claimant details
          addApplicant2: 'No',  // from SDT: derived (SDT will not have 2 claimants)
          applicantSolicitor1CheckEmail: {
            correct: 'No',
          },
          applicantSolicitor1UserDetails: {
            email: solicitor1Email
          },
          applicant1OrganisationPolicy: {  // from SDT: derived from customerID user
            OrgPolicyCaseAssignedRole: '[APPLICANTSOLICITORONE]',
            Organisation: {
              OrganisationID: config.claimantSolicitorOrgId
            }
          },
          specApplicantCorrespondenceAddressRequired: 'No', // Does not exist in SDT, but mandatory require a value
          respondent1: respondent1,  // from SDT: defendant1 details
          specRespondent1Represented: 'No',  // from SDT: respondent will not be represented
          addRespondent2: 'No', // from SDT: derived from defendant2 details existing, no if not existing
          detailsOfClaim: 'SDT:particulars', // from SDT: defaulted to 'particulars'
          timelineOfEvents: [{
            value: {
              timelineDate: '2021-02-20', // Does not exist in SDT, but mandatory require a value
              timelineDescription: 'event 1' // Does not exist in SDT, but mandatory require a value
            }
          }],
          claimAmountBreakup: [{
            value: {
              claimReason: 'amount reason',  // Does not exist in SDT, but mandatory require a value
              claimAmount: claimAmount // Does not exist in SDT, but mandatory require a value
            }
          }],
          claimInterest: 'No', // from SDT: reserveRightToClaimInterest
          uiStatementOfTruth: {
            name: 'John Doe', // from  SDT: from sotSignature
            role: 'Bulk issuer'  // from SDT, defaulted value as mandatory field
          },
          totalClaimAmount: '1512', // from SDT: claimAmount
      };
    }

    if (mpScenario === 'ONE_V_ONE' && withInterest === true) {
      console.log('1 v 1 claim, with interest added');
      return {
        sdtRequestIdFromSdt: 'testRequestID', // from SDT: sdtRequestIdFromSdt
        CaseAccessCategory: 'SPEC_CLAIM',
        solicitorReferences: {
          applicantSolicitor1Reference: 'SDT:claimReference', // from  SDT: claimReference
        },
        applicant1: applicant1,  // SDT: claimant details
        addApplicant2: 'No',  // from SDT: derived (SDT will not have 2 claimants)
        applicantSolicitor1CheckEmail: {
          correct: 'No',
        },
        applicantSolicitor1UserDetails: {
          email: solicitor1Email
        },
        applicant1OrganisationPolicy: {  // from SDT: derived from customerID user
          OrgPolicyCaseAssignedRole: '[APPLICANTSOLICITORONE]',
          Organisation: {
            OrganisationID: config.claimantSolicitorOrgId
          }
        },
        specApplicantCorrespondenceAddressRequired: 'No', // Does not exist in SDT, but mandatory require a value
        respondent1: respondent1,  // from SDT: defendant1 details
        specRespondent1Represented: 'No',  // from SDT: respondent will not be represented
        addRespondent2: 'No', // from SDT: derived from defendant2 details existing, no if not existing
        detailsOfClaim: 'SDT:particulars', // from SDT: defaulted to 'particulars'
        timelineOfEvents: [{
          value: {
            timelineDate: '2021-02-20', // Does not exist in SDT, but mandatory require a value
            timelineDescription: 'event 1' // Does not exist in SDT, but mandatory require a value
          }
        }],
        claimAmountBreakup: [{
          value: {
            claimReason: 'amount reason',  // Does not exist in SDT, but mandatory require a value
            claimAmount: claimAmount // Does not exist in SDT, but mandatory require a value
          }
        }],
        claimInterest: 'Yes', // from SDT: reserveRightToClaimInterest
        interestFromSpecificDate: '2023-10-20',
        sameRateInterestSelection: {
          differentRate: 5
        },
        uiStatementOfTruth: {
          name: 'John Doe', // from  SDT: from sotSignature
          role: 'Bulk issuer'  // from SDT, defaulted value as mandatory field
        },
        totalClaimAmount: '1512', // from SDT: claimAmount
      };
    }

    if (mpScenario === 'ONE_V_TWO', withInterest === false) {
      console.log('1 v 2 claim, with no interest');
      return {
        sdtRequestIdFromSdt: 'testRequestID', // from SDT: sdtRequestIdFromSdt
        CaseAccessCategory: 'SPEC_CLAIM',
        solicitorReferences: {
          applicantSolicitor1Reference: 'SDT:claimReference', // from  SDT: claimReference
        },
        applicant1: applicant1,  // SDT: claimant details
        addApplicant2: 'No',  // from SDT: derived (SDT will not have 2 claimants)
        applicantSolicitor1CheckEmail: {
          correct: 'No',
        },
        applicantSolicitor1UserDetails: {
          email: solicitor1Email
        },
        applicant1OrganisationPolicy: {  // from SDT: derived from customerID user
          OrgPolicyCaseAssignedRole: '[APPLICANTSOLICITORONE]',
          Organisation: {
            OrganisationID: config.claimantSolicitorOrgId
          }
        },
        specApplicantCorrespondenceAddressRequired: 'No', // Does not exist in SDT, but mandatory require a value
        respondent1: respondent1,  // from SDT: defendant1 details
        specRespondent1Represented: 'No',  // from SDT: respondent will not be represented
        addRespondent2: 'Yes',
        respondent2: respondent2,
        specRespondent2Represented: 'No',
        detailsOfClaim: 'SDT:particulars', // from SDT: defaulted to 'particulars'
        timelineOfEvents: [{
          value: {
            timelineDate: '2021-02-20', // Does not exist in SDT, but mandatory require a value
            timelineDescription: 'event 1' // Does not exist in SDT, but mandatory require a value
          }
        }],
        claimAmountBreakup: [{
          value: {
            claimReason: 'amount reason',  // Does not exist in SDT, but mandatory require a value
            claimAmount: claimAmount // Does not exist in SDT, but mandatory require a value
          }
        }],
        claimInterest: 'No', // from SDT: reserveRightToClaimInterest
        uiStatementOfTruth: {
          name: 'John Doe', // from  SDT: from sotSignature
          role: 'Bulk issuer'  // from SDT, defaulted value as mandatory field
        },
        totalClaimAmount: '1512', // from SDT: claimAmount
      };
    }
  },
};
