export const subheadings = {
  emailForDefendantLegalRep: "Email for second defendant's legal representative",
};

export const paragraphs = {
  emailUsage:
    "Use the email address of the legal representative's firm if you have it, otherwise use their personal email.",
  emailNote:
    'You can still issue the claim without their email address but the claim will then continue offline.',
};

export const inputs = {
  email: {
    label:
      'Enter the second defendant legal representatives email address to be used for notifications',
    hintText:
      "This should be the email address that the defendant's legal representative provided. " +
      'You can still issue the claim without their email address but the claim will then continue on paper (offline).',
    selector: '#respondentSolicitor2EmailAddress',
  },
};
