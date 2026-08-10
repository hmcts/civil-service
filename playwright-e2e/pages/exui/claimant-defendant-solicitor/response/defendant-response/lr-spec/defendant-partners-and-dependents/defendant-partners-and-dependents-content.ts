export const radioButtons = {
  liveWithPartner: {
    label: 'Does your client live with a partner?',
    yes: { label: 'Yes', selector: '#respondent1PartnerAndDependent_liveWithPartnerRequired_Yes' },
    no: { label: 'No', selector: '#respondent1PartnerAndDependent_liveWithPartnerRequired_No' },
  },
  // conditional: only shown when liveWithPartner = Yes
  partnerAgedOver: {
    label: 'Is the partner aged 18 or over?',
    yes: { label: 'Yes', selector: '#respondent1PartnerAndDependent_partnerAgedOver_Yes' },
    no: { label: 'No', selector: '#respondent1PartnerAndDependent_partnerAgedOver_No' },
  },
  haveAnyChildren: {
    label: 'Does your client have any children?',
    yes: { label: 'Yes', selector: '#respondent1PartnerAndDependent_haveAnyChildrenRequired_Yes' },
    no: { label: 'No', selector: '#respondent1PartnerAndDependent_haveAnyChildrenRequired_No' },
  },
  // conditional: only shown when haveAnyChildren = Yes
  receiveDisabilityPayments: {
    label:
      'Do any of the children that live your client receive severe disability premium payments',
    yes: {
      label: 'Yes',
      selector: '#respondent1PartnerAndDependent_receiveDisabilityPayments_Yes',
    },
    no: { label: 'No', selector: '#respondent1PartnerAndDependent_receiveDisabilityPayments_No' },
  },
  supportedAnyoneFinancial: {
    label: 'Does your client support anyone else financially',
    yes: {
      label: 'Yes',
      selector: '#respondent1PartnerAndDependent_supportedAnyoneFinancialRequired_Yes',
    },
    no: {
      label: 'No',
      selector: '#respondent1PartnerAndDependent_supportedAnyoneFinancialRequired_No',
    },
  },
};

export const inputs = {
  // conditional: only shown when haveAnyChildren = Yes
  numberOfUnderEleven: {
    label: 'Under 11',
    selector: '#respondent1PartnerAndDependent_howManyChildrenByAgeGroup_numberOfUnderEleven',
  },
  numberOfElevenToFifteen: {
    label: '11 to 15',
    selector: '#respondent1PartnerAndDependent_howManyChildrenByAgeGroup_numberOfElevenToFifteen',
  },
  numberOfSixteenToNineteen: {
    label: '16 to 19',
    selector: '#respondent1PartnerAndDependent_howManyChildrenByAgeGroup_numberOfSixteenToNineteen',
  },
  // conditional: only shown when supportedAnyoneFinancial = Yes
  supportPeopleNumber: {
    label: 'Number of people',
    selector: '#respondent1PartnerAndDependent_supportPeopleNumber',
  },
  supportPeopleDetails: {
    label: 'Provide details',
    selector: '#respondent1PartnerAndDependent_supportPeopleDetails',
  },
};
