export const heading = 'in employment?';

export const radioButtons = {
  employmentType: {
    yes: { selector: '#defenceAdmitPartEmploymentTypeRequired_Yes' },
    no: { selector: '#defenceAdmitPartEmploymentTypeRequired_No' },
  },
  unemployedType: {
    unemployed: {
      selector: '#respondToClaimAdmitPartUnemployedLRspec_unemployedComplexTypeRequired-UNEMPLOYED',
    },
    retired: {
      selector: '#respondToClaimAdmitPartUnemployedLRspec_unemployedComplexTypeRequired-RETIRED',
    },
  },
};

export const inputs = {
  yearsUnemployed: {
    selector:
      '#respondToClaimAdmitPartUnemployedLRspec_lengthOfUnemployment_numberOfYearsInUnemployment',
  },
  monthsUnemployed: {
    selector:
      '#respondToClaimAdmitPartUnemployedLRspec_lengthOfUnemployment_numberOfMonthsInUnemployment',
  },
};
