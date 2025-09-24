import ClaimType from '../../../../../../../enums/claim-type';
import { Party } from '../../../../../../../models/partys';

export const heading = 'Hearing availability';

export const subheadings = {
  unavailableDate: 'Unavailable date',
};

export const radioButtons = {
  unavailableDateRequired: {
    label:
      'Are there any days in the next 12 months when you, your client, an expert, or a witness, cannot attend a hearing?',
    labelFast:
      'Are there any dates when you, your client(s), experts or any witnesses are unavailable?',
    yes: {
      label: 'Yes',
      selector: (claimantParty: Party) =>
        `#${claimantParty.oldKey}DQSmallClaimHearing_unavailableDatesRequired_Yes`,
      selectorFast: (claimantParty: Party) =>
        `#${claimantParty.oldKey}DQHearingLRspec_unavailableDatesRequired_Yes`,
    },
    no: {
      label: 'No',
      selector: (claimantParty: Party) =>
        `#${claimantParty.oldKey}DQHearingSmallClaim_unavailableDatesRequired_No`,
      selectorFast: (claimantParty: Party) =>
        `#${claimantParty.oldKey}DQHearingLRspec_unavailableDatesRequired_No`,
    },
  },

  unavailableDateType: {
    label: 'Add a single date or a date range',
    single: {
      selector: (claimantParty: Party, unavailableDateNumber: number) =>
        `#${claimantParty.oldKey}DQSmallClaimHearing_smallClaimUnavailableDate_${unavailableDateNumber - 1}_unavailableDateType-SINGLE_DATE`,
      selectorFast: (claimantParty: Party, unavailableDateNumber: number) =>
        `#${claimantParty.oldKey}DQHearingLRspec_unavailableDates_${unavailableDateNumber - 1}_unavailableDateType-SINGLE_DATE`,
    },
    range: {
      selector: (claimantParty: Party, unavailableDateNumber: number) =>
        `#${claimantParty.oldKey}DQHearingSmallClaim_smallClaimUnavailableDate_${unavailableDateNumber - 1}_unavailableDateType-SINGLE_DATE`,
      selectorFast: (claimantParty: Party, unavailableDateNumber: number) =>
        `#${claimantParty.oldKey}DQHearingLRspec_unavailableDate_${unavailableDateNumber - 1}_unavailableDateType-DATE_RANGE`,
    },
  },
};

export const inputs = {
  singleDate: {
    label: 'Unavailable date',
    selectorKey: 'date',
  },
  dateFrom: {
    label: 'Date from',
    selectorKey: 'fromDate',
  },
  dateTo: {
    label: 'Date to',
    selectorKey: 'toDate',
  },
};

export const buttons = {
  addNewUnavailability: {
    title: 'Add new',
    selector: (claimantParty: Party) =>
      `div[id='${claimantParty.oldKey}DQSmallClaimHearing_smallClaimUnavailableDate'] button[type='button']`,
    selectorFast: (claimantParty: Party) =>
      `div[id='${claimantParty.oldKey}DQHearingLRspec_unavailableDates']  button[type='button']`,
  },
};
