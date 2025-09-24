import { Party } from '../../../../../../../models/partys';

export const subheadings = {
  availability: 'Hearing availability',
  unavailableDate: 'Unavailable date',
};

export const radioButtons = {
  unavailableDateRequired: {
    label:
      'Are there any days in the next 12 months when your client, an expert, or a witness, cannot attend a hearing?',
    yes: {
      label: 'Yes',
      selector: (claimantDefendantParty: Party) =>
        `#${claimantDefendantParty.oldKey}DQHearing_unavailableDatesRequired_Yes`,
    },
    no: {
      label: 'No',
      selector: (claimantDefendantParty: Party) =>
        `#${claimantDefendantParty.oldKey}DQHearing_unavailableDatesRequired_No`,
    },
  },
  unavailableDateType: {
    label: 'Add a single date or a date range',
    single: {
      label: 'Single Date',
      selector: (claimantDefendantParty: Party, unavailableDateNumber: number) =>
        `#${claimantDefendantParty.oldKey}DQHearing_unavailableDates_${unavailableDateNumber - 1}_unavailableDateType-SINGLE_DATE`,
      selectorFast: (claimantDefendantParty: Party, unavailableDateNumber: number) =>
        `#${claimantDefendantParty.oldKey}DQHearingFastClaim_unavailableDates_${unavailableDateNumber - 1}_unavailableDateType-SINGLE_DATE`,
    },
    range: {
      label: 'Date Range',
      selector: (claimantDefendantParty: Party, unavailableDateNumber: number) =>
        `#${claimantDefendantParty.oldKey}DQHearing_unavailableDates_${unavailableDateNumber - 1}_unavailableDateType-DATE_RANGE`,
      selectorFast: (claimantDefendantParty: Party, unavailableDateNumber: number) =>
        `#${claimantDefendantParty.oldKey}DQHearingFastClaim_unavailableDates_${unavailableDateNumber - 1}_unavailableDateType-DATE_RANGE`,
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
  addNewAvailability: {
    title: 'Add new',
    selector: (claimantDefendantParty: Party) =>
      `div[id='${claimantDefendantParty.oldKey}DQHearing_unavailableDates']  button[type='button']`,
  },
};
