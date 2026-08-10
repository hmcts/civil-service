export const confirmationHeading = "You have submitted the Defendant's defence";

export const paragraphs = {
  claimantsResponse: (claimantsResponseDate: string) =>
    'The Claimant legal representative will get a notification to confirm you have provided the Defendant defence.' +
    " You will be CC'ed. " +
    `The Claimant has until ${claimantsResponseDate} to discontinue or proceed with this claim`,
  firstResponse1v2DS:
    "Once the other defendant's legal representative has submitted their defence, we will send the claimant's legal representative a notification." +
    ' You will receive a copy of this notification, as it will include details of when the claimant must respond.',
};
