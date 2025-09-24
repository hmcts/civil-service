import { Party } from '../../../../../../../models/partys';

export const subheadings = {
  hearingAvailability: 'Hearing availability',
  unavailableDate: 'Unavailable date',
};

export const radioButtons = {
  unavailableDateRequired: {
    label:
      'Are there any dates when you, your client(s), experts or any witnesses are unavailable?',
    yes: {
      label: 'Yes',
      selector: (claimantDefendantParty: Party) =>
        `#${claimantDefendantParty.oldKey}DQHearingFastClaim_unavailableDatesRequired_Yes`,
    },
    no: {
      no: 'No',
      selector: (claimantDefendantParty: Party) =>
        `#${claimantDefendantParty.oldKey}DQHearingFastClaim_unavailableDatesRequired_No`,
    },
  },
  unavailableDateType: {
    label: 'Add a single date or a date range',
    single: {
      selector: (claimantDefendantParty: Party, unavailableDateNumber: number) =>
        `#${claimantDefendantParty.oldKey}DQHearingFastClaim_unavailableDates_${unavailableDateNumber - 1}_unavailableDateType-SINGLE_DATE`,
    },
    range: {
      selector: (claimantDefendantParty: Party, unavailableDateNumber: number) =>
        `#${claimantDefendantParty.oldKey}DQHearingFastClaim_unavailableDate_${unavailableDateNumber - 1}_unavailableDateType-DATE_RANGE`,
    },
  },
};

export const inputs = {
  singleDate: {
    label: 'Unavailable date',
    hintText: 'This date cannot be in the past and must not be more than one year in the future',
    selectorKey: 'date',
  },
  dateFrom: {
    label: 'Date from',
    hintText: 'This date cannot be in the past and must not be more than one year in the future',
    selectorKey: 'fromDate',
  },
  dateTo: {
    label: 'Date to',
    hintText: 'This date cannot be in the past and must not be more than one year in the future',
    selectorKey: 'toDate',
  },
};

export const buttons = {
  addNewAvailability: {
    title: 'Add new',
    selector: (claimantDefendantParty: Party) =>
      `div[id='${claimantDefendantParty.oldKey}DQHearingFastClaim_unavailableDates'] button[class='button write-collection-add-item__top']`,
  },
  removeAvailability: {
    title: 'Remove',
    selector: (claimantDefendantParty: Party, unavailableDateNumber: number) =>
      `div[id='${claimantDefendantParty.oldKey}DQHearingFastClaim_unavailableDates_${unavailableDateNumber - 1}_${unavailableDateNumber - 1}'] button[class='button write-collection-remove-item__top']`,
  },
};
