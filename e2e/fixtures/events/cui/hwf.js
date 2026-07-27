module.exports = {
    full: (type = 'application') => {
        return {
            hwfFeeType: type
        };
    },
    outcome: (type = 'APPLICATION') => {
        if (type === 'APPLICATION') {
            return {
                hwfFullRemissionGrantedForGa: 'Yes'
            };
        } else {
            return {
                hwfFullRemissionGrantedForAdditionalFee: 'Yes'
            };
        }
    },
};
