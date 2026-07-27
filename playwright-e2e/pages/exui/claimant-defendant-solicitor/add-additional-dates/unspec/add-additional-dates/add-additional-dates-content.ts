export const subheadings = {
  unavailableDates: 'Unavailable Dates',
  unavailableDates2: 'Unavailable Dates 2',
};

export const buttons = {
  addNew: {
    label: 'Add new',
    selector:
      "div[id='additionalUnavailableDates'] button[class='button write-collection-add-item__top']",
  },
};

export const radioButtons = {
  unavailableDateType: {
    single: {
      selector: '#additionalUnavailableDates_0_unavailableDateType-SINGLE_DATE',
    },
    range: {
      selector: '#additionalUnavailableDates_1_unavailableDateType-DATE_RANGE',
    },
  },
};

export const inputs = {
  singleDate: {
    selectorKey: 'date',
    containerSelector: '#additionalUnavailableDates_0_0',
  },
  dateFrom: {
    selectorKey: 'fromDate',
    containerSelector: '#additionalUnavailableDates_1_1',
  },
  dateTo: {
    selectorKey: 'toDate',
    containerSelector: '#additionalUnavailableDates_1_1',
  },
};
