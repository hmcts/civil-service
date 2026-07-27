const config = require('../../config');

const trialReadyClaimant = {
    valid:{
      ConfirmReadyClaimant: {
        trialReadyApplicant:  'Yes',
        applicantRevisedHearingRequirements: {
          revisedHearingRequirements: 'Yes',
          revisedHearingComments: 'string'
        },
        applicantHearingOtherComments:{
          hearingOtherComments: 'string'
        }
      }
    }
};

const trialReadyDefendant1 = {
  valid:{
    ConfirmReadyDefendant1: {
      trialReadyRespondent1: 'Yes',
      respondent1RevisedHearingRequirements: {
        revisedHearingRequirements: 'Yes',
        revisedHearingComments: 'string'
      },
      respondent1HearingOtherComments:{
        hearingOtherComments: 'string'
      }
    }
  }
};

const trialReadyDefendant2 = {
  valid:{
    ConfirmReadyDefendant2: {
      trialReadyRespondent2: 'Yes',
      respondent2RevisedHearingRequirements: {
        revisedHearingRequirements: 'Yes',
        revisedHearingComments: 'string'
      },
      respondent2HearingOtherComments: {
        hearingOtherComments: 'string'
      }
    }
  }
};

const trialReadyDefendantLip = {
  event: 'TRIAL_READINESS',
  caseDataUpdate: {
    trialReadyRespondent1: 'Yes',
    respondent1RevisedHearingRequirements: {
      revisedHearingRequirements: 'Yes',
      revisedHearingComments: 'Nothing Special',
    },
    respondent1HearingOtherComments: {
      hearingOtherComments: 'Optional information....',
    }
  }
};

const trialReadyClaimantLip = {
  event: 'TRIAL_READINESS',
  caseDataUpdate: {
    trialReadyApplicant: 'Yes',
    applicantRevisedHearingRequirements: {
      revisedHearingRequirements: 'Yes',
      revisedHearingComments: 'Nothing Special',
    },
    applicantHearingOtherComments: {
      hearingOtherComments: 'Optional information....',
    }
  }
};

module.exports = {

  confirmTrialReady: (user) => {

    switch(user)
    {
      case config.applicantSolicitorUser:
        return trialReadyClaimant;
      case config.defendantSolicitorUser:
        return trialReadyDefendant1;
      case config.secondDefendantSolicitorUser:
        return trialReadyDefendant2;
      case config.defendantCitizenUser2:
        return trialReadyDefendantLip;
      case config.applicantCitizenUser:
        return trialReadyClaimantLip;
    }
  }
};
