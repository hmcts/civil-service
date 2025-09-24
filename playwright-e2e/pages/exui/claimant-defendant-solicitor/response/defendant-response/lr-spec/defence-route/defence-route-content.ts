import { Party } from '../../../../../../../models/partys';

export const radioButtons = {
  defenceRoute: {
    hasPaid: {
      label: 'Has paid the amount claimed',
      selector: (defendantParty: Party) =>
        `#defenceRouteRequired${defendantParty.number === 1 ? '' : defendantParty.number}-HAS_PAID_THE_AMOUNT_CLAIMED`,
    },
    disputesClaim: {
      label: 'Disputes the claim',
      selector: (defendantParty: Party) =>
        `#defenceRouteRequired${defendantParty.number === 1 ? '' : defendantParty.number}-DISPUTES_THE_CLAIM`,
    },
  },
  amountPaid: {
    label: 'How was this amount paid?',
    creditCard: {
      label: 'Credit card',
      selector: (defendantParty: Party) =>
        `#respondToClaim${defendantParty.number === 1 ? '' : defendantParty.number}_howWasThisAmountPaid-CREDIT_CARD`,
    },
    cheque: {
      label: 'Cheque',
      selector: (defendantParty: Party) =>
        `#respondToClaim${defendantParty.number === 1 ? '' : defendantParty.number}_howWasThisAmountPaid-CHEQUE`,
    },
    bacs: {
      label: 'BACS',
      selector: (defendantParty: Party) =>
        `#respondToClaim${defendantParty.number === 1 ? '' : defendantParty.number}_howWasThisAmountPaid-BACS`,
    },
    other: {
      label: 'Other',
      selector: (defendantParty: Party) =>
        `#respondToClaim${defendantParty.number === 1 ? '' : defendantParty.number}_howWasThisAmountPaid-OTHER`,
    },
  },
};

export const inputs = {
  amountPaid: {
    label: 'How much was paid?',
    selector: (defendantParty: Party) =>
      `#respondToClaim${defendantParty.number === 1 ? '' : defendantParty.number}_howMuchWasPaid`,
  },
  amountPaidDate: {
    label: 'When was this amount paid?',
    selectorKey: 'whenWasThisAmountPaid',
  },
  amountPaidOther: {
    label: 'Tell us how',
    selector: (defendantParty: Party) =>
      `#respondToClaim${defendantParty.number === 1 ? '' : defendantParty.number}_howWasThisAmountPaidOther`,
  },
};
