module.exports = {
  event: 'DEFAULT_JUDGEMENT_SPEC',
  caseDataUpdate: {
    applicant1: {
      'type': 'INDIVIDUAL',
      'flags': {
        'partyName': 'Miss Jane Doe',
        'roleOnCase': 'Claimant 1'
      },
      'partyID': '8ae936d8-7ac2-45',
      'partyName': 'Miss Jane Doe',
      'partyEmail': 'civilmoneyclaimsdemo@gmail.com',
      'partyPhone': '07446777177',
      'primaryAddress': {
        'PostCode': 'S12eu',
        'PostTown': 'sheffield',
        'AddressLine1': '123',
        'AddressLine2': 'Fake Street'
      },
      'individualTitle': 'Miss',
      'individualLastName': 'Doe',
      'individualFirstName': 'Jane',
      'individualDateOfBirth': '1995-08-28',
      'partyTypeDisplayValue': 'Individual'
    },
    respondent1: {
      'type': 'INDIVIDUAL',
      'flags': {
        'partyName': 'Sir John Doe',
        'roleOnCase': 'Defendant 1'
      },
      'partyID': '12f4ad9f-bba4-44',
      'partyName': 'Sir John Doe',
      'partyEmail': 'civilmoneyclaimsdemo@gmail.com',
      'partyPhone': '07800000000',
      'primaryAddress': {
        'PostCode': 'IG61JD',
        'PostTown': 'TestCity',
        'AddressLine1': 'TestAddressLine1',
        'AddressLine2': 'TestAddressLine2',
        'AddressLine3': 'TestAddressLine3'
      },
      'individualTitle': 'Sir',
      'individualLastName': 'Doe',
      'individualFirstName': 'John',
      'partyTypeDisplayValue': 'Individual'
    },
    totalClaimAmount: 1500,
    totalInterest: 0,
    partialPayment: 'No',
    paymentTypeSelection: 'IMMEDIATELY'
  }
};
