const {buildBulkClaimAddress, date} = require('../../api/dataHelper');

const respondent1 = {
  address: buildBulkClaimAddress('respondent1')
};
const respondent2 = {
  address: buildBulkClaimAddress('respondent2')
};
const respondent1WithPartyName = {
  ...respondent1,
  name: 'Sir John Doe',
};
const respondent2WithPartyName = {
  ...respondent2,
  name: 'Dr Foo Bar',
};
const applicant1 = {
  address: buildBulkClaimAddress('applicant')
};
const applicant1WithPartyName = {
  ...applicant1,
  name: 'Dr Maddy Jane',
};

const isWithInterest = (interest) => {
  return interest;
};

module.exports = {
  bulkCreateClaimDto: (mpScenario, interest, customerId, amount, postcodeValidation) => {
    if (mpScenario === 'ONE_V_ONE') {
      return {
        bulkCustomerId: customerId,
        claimantReference: 'Claimant org',
        claimAmount: amount,
        claimant:{
          name: 'Bulk claim company',
          address:{
            addressLine1:'123 fake St',
            addressLine2:'antrim',
            postcode:'RG4 7AA'
          }
        },
        defendant1:{
          name:'Mr defendant1',
          address:{
            addressLine1:'Oak tree',
            addressLine2:'Antrim',
            addressLine3:'Antrim county',
            postcode:postcodeValidation,
          }
        },
         /* ...(isWithInterest(interest) ? {} : {
          interest: {
            dailyAmount: 100,
            claimDate: date(-1),
            owedDate: date(-1),
            claimAmountInterestBase: 5000,
          },
        }), */
        sendParticularsSeparately: false,
        reserveRightToClaimInterest: false,
        particulars: 'Particulars',
        sotSignature: 'signature',
        sotSignatureRole: 'bulkIssuerRole',
      };
    }
    if (mpScenario === 'ONE_V_TWO') {
      return {
        bulkCustomerId: customerId,
        claimantReference: 'Claimant org',
        claimAmount: amount,
        claimant: applicant1WithPartyName,
        defendant1: respondent1WithPartyName,
        defendant2: respondent2WithPartyName,
         ...(isWithInterest(interest) ? {} : {
          interest: {
            dailyAmount: 100,
            claimDate: date(-1),
            owedDate: date(-1),
            claimAmountInterestBase: 5000,
          },
        }),
        sendParticularsSeparately: false,
        reserveRightToClaimInterest: false,
        particulars: 'Particulars',
        sotSignature: 'signature',
        sotSignatureRole: 'bulkIssuerRole',
      };
    }
  },
};
