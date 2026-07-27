export const subheadings = {
  hearingRequirements: 'Hearing requirements',
};

export const radioButtons = {
  hearingType: {
    label: 'Choose your preferred type of hearing',
    hint: 'A judge will still need to approve the hearing',
    inPerson: {
      label: 'In person',
      selector: '#hearingSupportRequirementsDJ_hearingType-IN_PERSON',
    },
    video: {
      label: 'Video conference hearing',
      selector: '#hearingSupportRequirementsDJ_hearingType-VIDEO_CONF',
    },
    telephone: {
      label: 'Telephone hearing',
      selector: '#hearingSupportRequirementsDJ_hearingType-TELEPHONE_HEARING',
    },
  },

  unavailableDates: {
    label: 'Are there any dates when you cannot attend a hearing within the next 3 months?',
    yes: {
      label: 'Yes',
      selector: '#hearingSupportRequirementsDJ_hearingUnavailableDates_Yes',
    },
    no: {
      label: 'No',
      selector: '#hearingSupportRequirementsDJ_hearingUnavailableDates_No',
    },
  },

  support: {
    label: 'Does anyone require support for a court hearing?',
    yes: {
      label: 'Yes',
      selector: '#hearingSupportRequirementsDJ_hearingSupportQuestion_Yes',
    },
    no: {
      label: 'No',
      selector: '#hearingSupportRequirementsDJ_hearingSupportQuestion_No',
    },
  },
};

export const dropdowns = {
  courtLocation: {
    label: 'Preferred location',
    hint: 'This is needed if the judge decides to hold the hearing in person',
    selector: '#hearingSupportRequirementsDJ_hearingTemporaryLocation',
  },
};

export const inputs = {
  telephoneHearing: {
    label: 'Who will set up the telephone hearing ?',
    selector: '#hearingSupportRequirementsDJ_hearingTypeTelephoneHearing',
  },
  telephoneNumber: {
    label: 'Preferred telephone number',
    selector: '#hearingSupportRequirementsDJ_hearingPreferredTelephoneNumber1',
  },
  email: {
    label: 'Preferred email',
    selector: '#hearingSupportRequirementsDJ_hearingPreferredEmail',
  },
  unavailableFrom: {
    label: 'Date from',
    selectorKey: 'hearingUnavailableFrom',
  },
  unavailableTo: {
    label: 'Date to',
    selectorKey: 'hearingUnavailableUntil',
  },
  supportRequirements: {
    label:
      'Please name all people who need support and the kind of support they will need. For example, Jane Smith: requires wheelchair access',
    selector: '#hearingSupportRequirementsDJ_hearingSupportAdditional',
  },
};

export const buttons = {
  title: 'Add new',
  selector: `div[id='hearingSupportRequirementsDJ_hearingDates'] button[class='button write-collection-add-item__top']`,
};
