import partys from '../../../../../../../constants/partys';
import ClaimTrack from '../../../../../../../enums/claim-track';
import { Party } from '../../../../../../../models/partys';

export const heading = 'Respond to claim';

export const radioButtons = {
  address: {
    label: 'Is this address correct?',
    yes: {
      label: 'Yes',
      selector: (defendantParty: Party, solicitorParty: Party, claimTrack?: ClaimTrack) => {
        if (
          (defendantParty === partys.DEFENDANT_2 && claimTrack === ClaimTrack.FAST_CLAIM) ||
          solicitorParty === partys.DEFENDANT_SOLICITOR_2
        )
          return '#specAoSRespondent2HomeAddressRequired_Yes';
        else if (defendantParty === partys.DEFENDANT_2)
          return '#specAoSRespondent2CorrespondenceAddressRequired_Yes';
        else return '#specAoSApplicantCorrespondenceAddressRequired_Yes';
      },
    },
    no: {
      label: 'No',
      selector: (defendantParty: Party, solicitorParty: Party, claimTrack?: ClaimTrack) => {
        if (
          (defendantParty === partys.DEFENDANT_2 && claimTrack === ClaimTrack.FAST_CLAIM) ||
          solicitorParty === partys.DEFENDANT_SOLICITOR_2
        )
          return '#specAoSRespondent2HomeAddressRequired_No';
        else if (defendantParty.number === 2)
          return '#specAoSRespondent2CorrespondenceAddressRequired_No';
        else return '#specAoSApplicantCorrespondenceAddressRequired_No';
      },
    },
  },
};
