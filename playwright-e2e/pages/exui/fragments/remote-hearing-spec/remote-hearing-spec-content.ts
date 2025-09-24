import { Party } from '../../../../models/partys';

export const subheadings = { remoteHearing: 'Remote Hearing' };

export const radioButtons = {
  remoteHearing: {
    label: 'Do you want the hearing to be held remotely?',
    hintText: 'This will be over telephone or video',
    yes: {
      label: 'Yes',
      selector: (claimantDefendantParty: Party) =>
        `#${claimantDefendantParty.oldKey}DQRemoteHearingLRspec_remoteHearingRequested_Yes`,
    },
    no: {
      label: 'No',
      selector: (claimantDefendantParty: Party) =>
        `#${claimantDefendantParty.oldKey}DQRemoteHearingLRspec_remoteHearingRequested_Yes`,
    },
  },
};

export const inputs = {
  remoteHearingReason: {
    label: 'Do you want the hearing to be held remotely?',
    selector: (claimantDefendantParty: Party) =>
      `#${claimantDefendantParty.oldKey}DQRemoteHearingLRspec_reasonForRemoteHearing`,
  },
};
