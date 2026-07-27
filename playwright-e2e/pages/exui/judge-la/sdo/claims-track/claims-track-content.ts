export const paragraph = 'What track are you allocating the claim to?';

export const radioButtons = {
  smallTrack: {
    label: 'Do you want to allocate this claim to the small claims track?',
    yes: {
      label: 'Yes',
      selector: '#drawDirectionsOrderSmallClaims_Yes',
    },
    no: {
      label: 'No',
      selector: '#drawDirectionsOrderSmallClaims_No',
    },
  },
  claimsTrack: {
    label: 'What track are you allocating the claim to?',
    smallClaims: {
      label: 'Small Claims Track',
      selector: '#claimsTrack-smallClaimsTrack',
    },
    fast: {
      label: 'Fast Track',
      selector: '#claimsTrack-fastTrack',
    },
  },
};

export const checkboxes = {
  smallClaims: {
    label: 'Select additional directions for Small Claims Track, if any (Optional)',
    creditHire: {
      label: 'Credit Hire',
      selector: '#smallClaims-smallClaimCreditHire',
      selectorDrawDirections:
        '#drawDirectionsOrderSmallClaimsAdditionalDirections-smallClaimCreditHire',
    },
    roadTrafficAccident: {
      label: 'Road Traffic Accident',
      selector: '#smallClaims-smallClaimRoadTrafficAccident',
      selectorDrawDirections:
        '#drawDirectionsOrderSmallClaimsAdditionalDirections-smallClaimRoadTrafficAccident',
    },
    disputeResolutionHearing: {
      label: 'Dispute resolution hearing (Do not use with other options)',
      selector: '#smallClaims-smallClaimDisputeResolutionHearing',
      selectorDrawDirections:
        '#drawDirectionsOrderSmallClaimsAdditionalDirections-smallClaimDisputeResolutionHearing',
    },
    flightDelay: {
      label: 'Flight Delay',
      selector: '#smallClaims-smallClaimFlightDelay',
      selectorDrawDirections:
        '#drawDirectionsOrderSmallClaimsAdditionalDirections-smallClaimFlightDelay',
    },
  },
  fastTrack: {
    label: 'Select additional directions for Fast Track, if any (Optional)',
    buildingDispute: {
      label: 'Building Dispute',
      selector: '#fastClaims-fastClaimBuildingDispute',
    },
    clinicialNegligence: {
      label: 'Clinical Negligence',
      selector: '#fastClaims-fastClaimClinicalNegligence',
    },
    creditHire: {
      label: 'Credit Hire',
      selector: '#fastClaims-fastClaimCreditHire',
    },
    employersLiability: {
      label: 'Employers Liability',
      selector: '#fastClaims-fastClaimEmployersLiability',
    },
    housingDisrepair: {
      label: 'Housing Disrepair',
      selector: '#fastClaims-fastClaimHousingDisrepair',
    },
    personalInjury: {
      label: 'Personal Injury',
      selector: '#fastClaims-fastClaimPersonalInjury',
    },
    roadTrafficAccident: {
      label: 'Road Traffic Accident',
      selector: '#fastClaims-fastClaimRoadTrafficAccident',
    },
    noiseInducedHearingLoss: {
      label: 'Noise Induced Hearing Loss (Do not use with other options)',
      selector: '#fastClaims-fastClaimNoiseInducedHearingLoss',
    },
  },
};
