module.exports = {
  discontinueClaim: (mpScenario) => {
    const data ={};
    switch (mpScenario) {
      case 'ONE_V_TWO': {
        data.userInput = {
          courtPermission: {
            courtPermissionNeeded: 'NO',
            courtPermissionNeededChecked: [
              'CourtPermissionNeededChecked'
            ]
          },
          PermissionGranted: {
            isPermissionGranted:'YES',
            permissionGrantedComplex:{
              permissionGrantedJudge:'test',
              permissionGrantedDate:'2023-02-01'
            }
          },
          DiscontinuingAgainstDefendants: {
            respondent2Represented: 'Yes',
            isDiscontinuingAgainstBothDefendants: 'YES',
          },
          DiscontinuanceType: {
            typeOfDiscontinuance:'FULL_DISCONTINUANCE'
          }
        };
      }
        break;
      case 'TWO_V_ONE': {
        data.userInput = {
          MultipleClaimant: {
            addApplicant2: 'Yes',
            claimantWhoIsDiscontinuing: {
              value: {
                code: 'bfcf6412-1b23-45c6-b451-224ab5ec1703',
                label: 'Test Inc'
              }
            }
          },
          ClaimantConsent: {
            selectedClaimantForDiscontinuance: 'Test Inc',
            claimantsConsentToDiscontinuance: 'Yes'
          },
          courtPermission: {
            courtPermissionNeeded: 'NO',
            courtPermissionNeededChecked: [
              'CourtPermissionNeededChecked'
            ]
          },
          PermissionGranted: {
            isPermissionGranted:'YES',
            permissionGrantedComplex:{
              permissionGrantedJudge:'test',
              permissionGrantedDate:'2023-02-01'
            }
          },
          DiscontinuanceType: {
            typeOfDiscontinuance:'FULL_DISCONTINUANCE',
            partDiscontinuanceDetails:'test'
          }
        };
      }
        break;
      case 'ONE_V_ONE': {
        data.userInput = {
          courtPermission: {
            courtPermissionNeeded: 'YES',
            courtPermissionNeededChecked: [
              'CourtPermissionNeededChecked'
            ]
          },
          PermissionGranted: {
            isPermissionGranted:'YES',
            permissionGrantedComplex:{
              permissionGrantedJudge:'test',
              permissionGrantedDate:'2023-02-01'
            }
          },
          DiscontinuanceType: {
            typeOfDiscontinuance:'FULL_DISCONTINUANCE'
          }
        };
      }
        break;
      case 'ONE_V_TWO_P_NEEDED': {
        data.userInput = {
          courtPermission: {
            courtPermissionNeeded: 'YES',
            courtPermissionNeededChecked: [
              'CourtPermissionNeededChecked'
            ]
          },
          PermissionGranted: {
            isPermissionGranted:'YES',
            permissionGrantedComplex:{
              permissionGrantedJudge:'test',
              permissionGrantedDate:'2023-02-01'
            }
          },
          DiscontinuingAgainstDefendants: {
            respondent2Represented: 'Yes',
            isDiscontinuingAgainstBothDefendants: 'YES',
          },
          DiscontinuanceType: {
            typeOfDiscontinuance:'FULL_DISCONTINUANCE'
          }
        };
      }
        break;
      case 'ONE_V_ONE_NO_P_NEEDED': {
        data.userInput = {
          courtPermission: {
            courtPermissionNeeded: 'NO',
            courtPermissionNeededChecked: [
              'CourtPermissionNeededChecked'
            ]
          },
          PermissionGranted: {
            isPermissionGranted:'YES',
            permissionGrantedComplex:{
              permissionGrantedJudge:'test',
              permissionGrantedDate:'2023-02-01'
            }
          },
          DiscontinuanceType: {
            typeOfDiscontinuance:'FULL_DISCONTINUANCE'
          }
        };
      }
        break;
    }
    return data;
  }
};
