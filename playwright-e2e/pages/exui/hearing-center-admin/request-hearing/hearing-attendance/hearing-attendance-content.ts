export const heading = 'Participant attendance';

export const radioButtons = {
  label: 'Will this be a paper hearing?',
  yes: {
    label: 'Yes',
    selector: '#paperHearingYes',
  },
  no: {
    label: 'No',
    selector: '#paperHearingNo',
  },
};

export const checkboxes = {
  label: 'What will be the methods of attendance for this hearing?',
  inPerson: {
    label: 'In Person',
    selector: '#INTER',
  },
  telephone: {
    label: 'Telephone',
    selector: '#TEL',
  },
  video: {
    label: 'Video',
    selector: '#VID',
  },
};

export const dropdowns = {
  label: 'How will each participant attend the hearing?',
  selector: 'select[id^="partyChannel"]',
  inPerson: {
    label: 'In Person',
  },
  notInAttendance: {
    label: 'Not in Attendance',
  },
  telephone: {
    label: 'Telephone',
  },
  video: {
    label: 'Video',
  },
};

export const inputs = {
  label: 'How many people will attend the hearing in person?',
  numberOfAttendees: '8',
  selector: '#attendance-number',
};
