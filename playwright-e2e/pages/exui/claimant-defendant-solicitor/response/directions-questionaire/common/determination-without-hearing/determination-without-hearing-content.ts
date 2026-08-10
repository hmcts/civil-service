import partys from '../../../../../../../constants/users/partys';
import StringHelper from '../../../../../../../helpers/string-helper';
import { Party } from '../../../../../../../models/users/partys';

export const heading = 'Determination without Hearing Questions';

export const radioButtons = {
  deterWithoutHearing: {
    label: 'Determination without hearing?',
    selectorKey: (claimantDefendantParty: Party) => {
      if (claimantDefendantParty === partys.CLAIMANT_1)
        return 'deterWithoutHearing_deterWithoutHearingYesNo';
      return `deterWithoutHearing${StringHelper.capitalise(claimantDefendantParty.oldKey)}_deterWithoutHearingYesNo`;
    },
  },
};

export const inputs = {
  deterWithoutHearing: {
    why: {
      label: 'Tell us why',
      selector: (claimantDefendantParty: Party) => {
        if (claimantDefendantParty === partys.CLAIMANT_1)
          return '#deterWithoutHearing_deterWithoutHearingWhyNot';
        return `#deterWithoutHearing${StringHelper.capitalise(claimantDefendantParty.oldKey)}_deterWithoutHearingWhyNot`;
      },
    },
  },
};
