const {element} = require('../../api/dataHelper');
const DEFENDANT_1 = 'DEFENDANT_1';
const DEFENDANT_1_LR_INDIVIDUALS = 'DEFENDANT_1_LR_INDIVIDUALS';
const DEFENDANT_1_EXPERTS = 'DEFENDANT_1_EXPERTS';

module.exports = {
  manageDefendant1Information: (caseData) => {
    return {
      ...caseData,
      partyChosen: {
        ...caseData.partyChosen,
        value: {
          code: DEFENDANT_1
        }
      },
      partyChosenId: DEFENDANT_1,
      respondent1: {
        ...caseData.respondent1,
        partyEmail: 'testinc@mail.com'
      }
    };
  },
  manageDefendant1LROrganisationInformation: (caseData) => {
    return {
      ...caseData,
      partyChosen: {
        ...caseData.partyChosen,
        value: {
          code: DEFENDANT_1_LR_INDIVIDUALS,
          label: 'DEFENDANT 1: Individuals attending for the legal representative',
        }
      },
      partyChosenId: DEFENDANT_1_LR_INDIVIDUALS,
      updateLRIndividualsForm: [
        element({
          firstName: 'Halla',
          lastName: 'Mcintyre',
          emailAddress: 'h.mcintyre@email.com',
          phoneNumber: '07821015555',
        })
      ]
    };
  },
  manageDefendant1ExpertsInformation: (caseData) => {
    let expert = caseData.respondent1DQExperts.details[0].value;
    return {
      ...caseData,
      partyChosen: {
        ...caseData.partyChosen,
        value: {
          code: DEFENDANT_1_EXPERTS
        }
      },
      partyChosenId: DEFENDANT_1_EXPERTS,
      updateExpertsDetailsForm: [
        element({
          firstName: expert.firstName,
          lastName: expert.lastName,
          phoneNumber: expert.phoneNumber,
          emailAddress: expert.emailAddress,
          fieldOfExpertise: expert.fieldOfExpertise,
          partyId: expert.partyID
        }),
        element({
          firstName: 'Stan',
          lastName: 'Edgar',
          phoneNumber: '07811110000',
          emailAddress: 'stan.edgar@gmail.com',
          fieldOfExpertise: 'pharmaceutical'
        })
      ]
    };
  }
};
