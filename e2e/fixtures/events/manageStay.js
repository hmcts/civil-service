
module.exports = {
  manageStayRequestUpdate: () => {
    return {
      userInput: {
        manageStayOptions: {
          manageStayOption: 'REQUEST_UPDATE'
        },
        manageStayRequestUpdate: {
        }
      }
    };
  },
  manageStayLiftStay: () => {
    return {
      userInput: {
        manageStayOptions: {
          manageStayOption: 'LIFT_STAY'
        }
      }
    };
  },
  manageStayRequestUpdateDamages: () => {
    return {
      valid: {
        manageStayOptions: {
          manageStayOption: 'REQUEST_UPDATE'
        },
        manageStayRequestUpdate: {
        }
      }
    };
  },
  manageStayLiftStayDamages: () => {
    return {
      valid: {
        manageStayOptions: {
          manageStayOption: 'LIFT_STAY'
        }
      }
    };
  }
};
