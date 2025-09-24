import { Party } from '../../../../../../models/partys';

export const subheadings = {
  mediationAvailability: 'Mediation availability',
  unavailableDates: 'Unavailable dates',
};

export const radioButtons = {
  mediationAvailability: {
    label:
      'Are there any dates in the next 3 months when you or your client cannot attend a mediation appointment?',
    hintText:
      'These should only be the dates of important events like medical appointments, other court hearings, or holidays that are already booked.' +
      ' If the mediation appointment is not attended, your client may face a penalty. ' +
      'The Small Claims Mediation Service operates Monday to Friday from 8am to 5pm, except bank holidays',
    yes: {
      label: 'Yes',
      selector: (claimantDefendantParty: Party) =>
        `#${claimantDefendantParty.shortOldKey}MediationAvailability_isMediationUnavailablityExists_Yes`,
    },
    no: {
      label: 'No',
      selector: (claimantDefendantParty: Party) =>
        `#${claimantDefendantParty.shortOldKey}MediationAvailability_isMediationUnavailablityExists_No`,
    },
  },
  unavailableDateType: {
    label: 'Add a single date or a date range',
    single: {
      selector: (claimantDefendantParty: Party, unavailableDateNumber: number) =>
        `#${claimantDefendantParty.shortOldKey}MediationAvailability_unavailableDatesForMediation_${unavailableDateNumber - 1}_unavailableDateType-SINGLE_DATE`,
    },
    range: {
      selector: (claimantDefendantParty: Party, unavailableDateNumber: number) =>
        `#${claimantDefendantParty.shortOldKey}MediationAvailability_unavailableDatesForMediation_${unavailableDateNumber - 1}_unavailableDateType-DATE_RANGE`,
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
  addNew: {
    title: 'Add new',
    selector: (claimantDefendantParty: Party) =>
      `div[id='${claimantDefendantParty.shortOldKey}MediationAvailability_unavailableDatesForMediation'] button[type='button']`,
  },
};
