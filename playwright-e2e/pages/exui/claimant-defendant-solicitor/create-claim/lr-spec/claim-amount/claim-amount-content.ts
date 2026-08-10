export const subheadings = {
  claimAmount: 'Claim Amount',
};

export const paragraphs = {
  descriptionText:
    'Your claim can be for single or multiple amounts.' +
    ' Do not include interest or the claim fee, you can add these on the next page.',
};

export const buttons = {
  addNew: { title: 'Add new', selector: "button[class='button write-collection-add-item__top']" },
};

export const inputs = {
  claim: {
    reason: {
      label: 'What you are claiming for',
      hintText: 'Briefly explain each item, for example: broken tiles, roof damage.',
      selector: (claimNumber: number) => `#claimAmountBreakup_${claimNumber - 1}_claimReason`,
    },
    amount: {
      label: 'Amount',
      selector: (claimNumber: number) => `#claimAmountBreakup_${claimNumber - 1}_claimAmount`,
    },
  },
};
