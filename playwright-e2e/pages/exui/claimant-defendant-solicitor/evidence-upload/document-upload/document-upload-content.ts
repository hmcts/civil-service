import partys from "../../../../../constants/users/partys";
import { Party } from "../../../../../models/users/partys";

export const subheadings = {
  witnessStatement: 'Witness statement',
  expertsReport: 'Expert\'s report',
  authorities: 'Authorities',
};

export const containers = {
  witnessStatement: {
    selector: (claimantDefendantParty: Party) => `#documentWitnessStatement${claimantDefendantParty === partys.CLAIMANT_1 ? '' : 'Res'}`,
  },
  expertsReport: {
    selector: (claimantDefendantParty: Party) => `#documentExpertReport${claimantDefendantParty === partys.CLAIMANT_1 ? '' : 'Res'}`,
  },
  authorities: {
    selector: (claimantDefendantParty: Party) => `#documentAuthorities${claimantDefendantParty === partys.CLAIMANT_1 ? '' : 'Res'}`,
  }
}

export const buttons = {
  witnessStatement: {
    addNew: {
      label: 'Add new',
    },
  },
  expertsReport: {
    addNew: {
      label: 'Add new',
    },
  },
  authorities: {
    addNew: {
      label: 'Add new',
    },
  },
};

export const inputs = {
  witnessStatement:{
    witnessName: {
      label: 'Witness\'s name',
      selector: (documentNumber: number, defendantClaimant: Party) => `#documentWitnessStatement${defendantClaimant === partys.CLAIMANT_1 ? '' : 'Res'}_${documentNumber}_witnessOptionName`,
    },
    date: {
      selectorKey: 'witnessOptionUploadDate',
    },
    file: { 
      label: 'Upload a file', 
      selector: 'input[type="file"]' 
    },
  },
  expertsReport: {
    expertName: {
      label: 'Expert\'s name',
      selector: (documentNumber: number, defendantClaimant: Party) => `#documentExpertReport${defendantClaimant === partys.CLAIMANT_1 ? '' : 'Res'}_${documentNumber}_expertOptionName`,
    },
    fieldOfExpertise: {
      label: 'Field of expertise',
      selector: (documentNumber: number, defendantClaimant: Party) => `#documentExpertReport${defendantClaimant === partys.CLAIMANT_1 ? '' : 'Res'}_${documentNumber}_expertOptionExpertise`,
    },
    date: {
      selectorKey: 'expertOptionUploadDate',
    },
    file: { 
      label: 'Upload a file', 
      selector: 'input[type="file"]' 
    },
  },
  authorities: {
    file: { 
      label: 'Upload a file', 
      selector: 'input[type="file"]' 
    },
  }
};
