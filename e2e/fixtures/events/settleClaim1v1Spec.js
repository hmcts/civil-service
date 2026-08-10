module.exports = {
  settleClaim: (addApplicant2) => {
    const OptionsForSettlement ={};
    switch (addApplicant2){
      case 'YES': {
        OptionsForSettlement.userInput ={
          ...OptionsForSettlement.userInput,
          OptionsForSettlement:{
            addApplicant2 : 'Yes'
          }
        };
      }
      break;
      case 'NO': {
        OptionsForSettlement.userInput ={
          ...OptionsForSettlement.userInput,
          OptionsForSettlement:{
            addApplicant2 : 'No'
          }
        };
      }
        break;
    }

    return OptionsForSettlement;
  },
  claimantDetails: (addApplicant2) => {
    const ClaimantDetails ={};
    if (addApplicant2 === 'YES') {
      ClaimantDetails.userInput = {
        ...ClaimantDetails.userInput,
        ClaimantDetails: {
          markPaidForAllClaimants: 'No',
          claimantWhoIsSettling:{
            code: 'bfcf6412-1b23-45c6-b451-224ab5ec1703',
            label: 'Test Inc'
          },
        }
      };
    }
    return ClaimantDetails;
  },
};
