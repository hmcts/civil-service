import { Party } from '../../../../../../../models/partys';

export const subheadings = {
  courtLocation: 'Court location code',
};

export const dropdowns = {
  courtLocations: {
    label: 'Please select your preferred court hearing location',
    selector: (defendantParty: Party) =>
      `#${defendantParty.oldKey}DQRequestedCourt_responseCourtLocations`,
    options: [
      'Aberystwyth Justice Centre - Y Lanfa, Trefechan, Aberystwyth - SY23 1AS',
      'Aldershot Magistrates Court - 2 Wellington Avenue, Aldershot - GU11 1NY',
    ],
  },
};

export const inputs = {
  preferredCourtReason: {
    label: 'Briefly explain your reasons (Optional)',
    selector: (defendantParty: Party) =>
      `#${defendantParty.oldKey}DQRequestedCourt_reasonForHearingAtSpecificCourt`,
  },
};
