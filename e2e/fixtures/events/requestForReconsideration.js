module.exports = {
  createRequestForReconsideration : () => {
      return {
        valid: {
          reasonForReconsideration: {
            reasonForReconsideration: {
              reasonForReconsiderationTxt: 'The legal adviser overlooked some factors'
            }
          }
        }
      };
    },

  createRequestForReconsiderationSpec : (userType) => {
    if (userType == 'Applicant') {
      return {
        userInput: {
          reasonForReconsiderationApplicant: {
            reasonForReconsiderationApplicant: {
              reasonForReconsiderationTxt: 'The legal adviser overlooked some factors'
            }
          }
        }
      };
    }
    else if (userType == 'Respondent1') {
      return {
        userInput: {
          reasonForReconsiderationRespondent1: {
            reasonForReconsiderationRespondent1: {
              reasonForReconsiderationTxt: 'The legal adviser overlooked some factors'
            }
          }
        }
      };
    }
    else {
      return {
        userInput: {
          reasonForReconsiderationRespondent2: {
            reasonForReconsiderationRespondent2: {
              reasonForReconsiderationTxt: 'The legal adviser overlooked some factors'
            }
          }
        }
      };
    }
  },

  createRequestForReconsiderationSpecCitizen : (userType) => {
    if (userType === 'Claimant') {
      return {
        event: 'REQUEST_FOR_RECONSIDERATION',
        caseDataUpdate: {
          requestForReviewCommentsClaimant: 'The legal adviser overlooked some factors'
        }
      };
    } else {
      return {
        event: 'REQUEST_FOR_RECONSIDERATION',
        caseDataUpdate: {
          requestForReviewCommentsDefendant: 'The legal adviser overlooked some factors'
        }
      };
    }
  }
};
