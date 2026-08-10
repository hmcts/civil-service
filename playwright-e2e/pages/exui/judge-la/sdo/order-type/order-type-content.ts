export const radioButtons = {
  orderType: {
    label: 'What order would you like to make?',
    disposal: {
      label: 'Disposal hearing',
      selector: '#orderType-DISPOSAL',
    },
    trail: {
      label: 'A trial to decide the amount of damages',
      selector: '#orderType-DECIDE_DAMAGES',
    },
  },
};

export const checkboxes = {
  label: 'Select additional directions for Fast Track, if any (Optional)',
  buildingDispute: {
    label: 'Building Dispute',
    selector: '#trialAdditionalDirectionsForFastTrack-fastClaimBuildingDispute',
  },
  clinicialNegligence: {
    label: 'Clinical Negligence',
    selector: '#trialAdditionalDirectionsForFastTrack-fastClaimClinicalNegligence',
  },
  creditHire: {
    label: 'Credit Hire',
    selector: '#trialAdditionalDirectionsForFastTrack-fastClaimCreditHire',
  },
  employersLiability: {
    label: 'Employers Liability',
    selector: '#trialAdditionalDirectionsForFastTrack-fastClaimEmployersLiability',
  },
  housingDisrepair: {
    label: 'Housing Disrepair',
    selector: '#trialAdditionalDirectionsForFastTrack-fastClaimHousingDisrepair',
  },
  personalInjury: {
    label: 'Personal Injury',
    selector: '#trialAdditionalDirectionsForFastTrack-fastClaimPersonalInjury',
  },
  roadTrafficAccident: {
    label: 'Road Traffic Accident',
    selector: '#trialAdditionalDirectionsForFastTrack-fastClaimRoadTrafficAccident',
  },
  noiseInducedHearingLoss: {
    label: 'Noise Induced Hearing Loss (Do not use with other options)',
    selector: '#trialAdditionalDirectionsForFastTrack-fastClaimNoiseInducedHearingLoss',
  },
};
