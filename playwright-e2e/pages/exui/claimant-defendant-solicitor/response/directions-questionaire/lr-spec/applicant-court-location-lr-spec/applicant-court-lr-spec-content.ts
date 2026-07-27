export const subheadings = {
  courtLocation: 'Court Location',
  claimant: `Claimant's requested court`,
};

export const dropdowns = {
  courtLocations: {
    label: 'Please select your preferred court hearing location',
    hintText:
      "Where the defendant is an individual or a sole trader, the case will be held at the defendant's preferred court.",
    selector: '#applicant1DQRequestedCourt_responseCourtLocations',
    options: [
      'Aberystwyth Justice Centre - Y Lanfa, Trefechan, Aberystwyth - SY23 1AS',
      'Aldershot Magistrates Court - 2 Wellington Avenue, Aldershot - GU11 1NY',
    ],
  },
};

export const inputs = {
  preferredCourtReason: {
    label: 'Briefly explain your reasons (Optional)',
    selector: '#applicant1DQRequestedCourt_reasonForHearingAtSpecificCourt',
  },
};
