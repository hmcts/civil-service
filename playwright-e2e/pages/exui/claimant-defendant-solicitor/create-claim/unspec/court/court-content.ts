export const subheadings = {
  courtLocationCode: 'Court location code',
};

export const inputs = {
  courtReason: {
    label: 'Briefly explain your reasons (Optional)',
    selector: '#courtLocation_reasonForHearingAtSpecificCourt',
  },
};

export const dropdowns = {
  courtLocation: {
    label: 'Please select your preferred court hearing location',
    selector: '#courtLocation_applicantPreferredCourtLocationList',
    options: {
      london:
        'Central London County Court - Thomas More Building, Royal Courts of Justice, Strand, London - WC2A 2LL',
    },
  },
};
