const config = require('../config.js');
const {element, date} = require('../api/dataHelper');
const {checkToggleEnabled} = require('../api/testingSupport');

module.exports = {
  removeHNLFieldsFromClaimData: (data) => {
    delete data.userInput.Claimant.applicant1['partyEmail'];
    delete data.userInput.Claimant.applicant1['partyPhone'];

    if( data.userInput.SecondClaimant) {
      delete data.userInput.SecondClaimant.applicant2['partyEmail'];
      delete data.userInput.SecondClaimant.applicant2['partyPhone'];
    }

    delete data.userInput.Defendant.respondent1['partyEmail'];
    delete data.userInput.Defendant.respondent1['partyPhone'];

    if (data.userInput.SecondDefendant) {
      delete data.userInput.SecondDefendant.respondent2['partyEmail'];
      delete data.userInput.SecondDefendant.respondent2['partyPhone'];
    }
  },

  removeHNLFieldsFromUnspecClaimData: (data) => {
    delete data.valid.Defendant.respondent1['partyEmail'];
    delete data.valid.Defendant.respondent1['partyPhone'];

    if (data.valid.SecondDefendant) {
      delete data.valid.SecondDefendant.respondent2['partyEmail'];
      delete data.valid.SecondDefendant.respondent2['partyPhone'];
    }
  },

  async replaceDQFieldsIfHNLFlagIsDisabled(data, solicitor, isDefendantResponse) {
    //let isHNLEnabled = await checkToggleEnabled('hearing-and-listing-sdo');
    let isHNLEnabled = false;
    // work around for the api  tests
    console.log(`Hearing selected in Env: ${config.runningEnv}`);

    if (!isHNLEnabled) {
      const party = `${isDefendantResponse === true ?
        'respondent' : 'applicant'}${solicitor === 'solicitorTwo' ? 2 : 1}DQ`;
      data = {
        ...data,
        valid: {
          ...data.valid,
          Witnesses: {
            [`${party}Witnesses`]: {
              witnessesToAppear: 'Yes',
              details: [
                element({
                  name: 'John Doe',
                  reasonForWitness: 'None'
                })
              ]
            }
          },
          Experts: {
            [`${party}Experts`]: {
              expertRequired: 'Yes',
              details: [
                element({
                  name: 'John Doe',
                  fieldOfExpertise: 'Science',
                  whyRequired: 'Reason',
                  estimatedCost: '100',
                })
              ]
            }
          },
          Hearing: {
            [`${party}Hearing`]: {
              hearingLength: 'MORE_THAN_DAY',
              hearingLengthDays: '5',
              unavailableDatesRequired: 'Yes',
              unavailableDates: [
                element({
                  date: date(10),
                  who: 'Foo Bar'
                })
              ]
            }
          }
        },
        invalid: {
          ...data.invalid,
          Hearing: {
            past: {
              [`${party}Hearing`]: {
                hearingLength: 'MORE_THAN_DAY',
                hearingLengthDays: 5,
                unavailableDatesRequired: 'Yes',
                unavailableDates: [
                  element({
                    date: date(-1),
                    who: 'Foo Bar'
                  })
                ]
              }
            },
            moreThanYear: {
              [`${party}Hearing`]: {
                hearingLength: 'MORE_THAN_DAY',
                hearingLengthDays: 5,
                unavailableDatesRequired: 'Yes',
                unavailableDates: [
                  element({
                    date: date(367),
                    who: 'Foo Bar'
                  })
                ]
              }
            }
          }
        }
      };
    }
    return data;
  },

  async replaceExpertsIfHNLFlagIsDisabled(defendantResponseData, solicitor) {
    let isHNLEnabled = await checkToggleEnabled('hearing-and-listing-sdo');
    // work around for the api  tests
    console.log(`Experts selected in Env: ${config.runningEnv}`);
    if (!isHNLEnabled) {
      defendantResponseData = {
        ...defendantResponseData,
        valid: {
          ...defendantResponseData.valid,
          Experts: {
            [`respondent${solicitor === 'solicitorTwo' ? 2 : 1}DQExperts`]: {
              expertRequired: 'Yes',
              details: [
                element({
                  name: 'John Doe',
                  fieldOfExpertise: 'Science',
                  whyRequired: 'Reason',
                  estimatedCost: '100',
                })
              ]
            }
          }
        }
      };
    }
    return defendantResponseData;
  },
  replaceFieldsIfHNLToggleIsOffForDefendantResponse: (defendantResponseData,solicitor) => {
    if (solicitor === 'solicitorTwo') {
      defendantResponseData = {
        ...defendantResponseData,
        valid: {
          ...defendantResponseData.valid,
          HearingSupport: {},
          Language: {
            respondent2DQLanguage: {
              evidence: 'WELSH',
              court: 'WELSH',
              documents: 'WELSH'
            }
          }
        }
      };
    } else {
      defendantResponseData = {
        ...defendantResponseData,
        valid: {
          ...defendantResponseData.valid,
          HearingSupport: {},
          Language: {
            respondent1DQLanguage: {
              evidence: 'WELSH',
              court: 'WELSH',
              documents: 'WELSH'
            }
          }
        }
      };
    }
    return defendantResponseData;
  },
  replaceFieldsIfHNLToggleIsOffForClaimantResponse: (claimantResponseData) => {
    claimantResponseData = {
      ...claimantResponseData,
      valid: {
        ...claimantResponseData.valid,
        HearingSupport: {},
        Language: {
          applicant1DQLanguage: {
            evidence: 'WELSH',
            court: 'WELSH',
            documents: 'WELSH'
          }
        }
      }
    };
    return claimantResponseData;
  },
  replaceFieldsIfHNLToggleIsOffForClaimantResponseSpec: (claimantResponseData) => {
    claimantResponseData = {
      ...claimantResponseData,
      userInput: {
        ...claimantResponseData.userInput,
        HearingSupport: {},
        Language: {
          applicant1DQLanguage: {
            evidence: 'WELSH',
            court: 'WELSH',
            documents: 'WELSH'
          }
        }
      }
    };
    return claimantResponseData;
  },
  replaceFieldsIfHNLToggleIsOffForDefendantSpecResponse: (defendantResponseData,solicitor) => {
    if (solicitor === 'solicitorTwo') {
      defendantResponseData = {
        ...defendantResponseData,
        userInput: {
          ...defendantResponseData.userInput,
          HearingSupport: {},
          Language: {
            respondent2DQLanguage: {
              evidence: 'WELSH',
              court: 'WELSH',
              documents: 'WELSH'
            }
          }
        }
      };
    } else {
      defendantResponseData = {
        ...defendantResponseData,
        userInput: {
          ...defendantResponseData.userInput,
          HearingSupport: {},
          Language: {
            respondent1DQLanguage: {
              evidence: 'WELSH',
              court: 'WELSH',
              documents: 'WELSH'
            }
          }
        }
      };
    }
    return defendantResponseData;
  },
};
