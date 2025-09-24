module.exports = {
  getClaimFee: (amount) => {
    console.log('claim amount passed...', amount);
    switch(amount) {
      case '3000':
        return {
          calculatedAmountInPence: '11500',
          code: 'FEE0206',
          version: '6'
        };
      case '100':
        return {
          calculatedAmountInPence: '3500',
          code: 'FEE0202',
          version: '6'
        };
      case '11000':
        return {
          calculatedAmountInPence: '55000',
          code: 'FEE0209',
          version: '3'
        };
      case '30000':
          return {
            calculatedAmountInPence: '150000',
            code: 'FEE0209',
            version: '3'
          };
      case '200001':
        console.log('Use multi track claim fee');
        return {
          calculatedAmountInPence: '1000000',
          code: 'FEE0210',
          version: '4'
        };
      case '99000':
        console.log('Use intermediate track claim fee');
        return {
          calculatedAmountInPence: '495000',
          code: 'FEE0209',
          version: '3'
        };
      default:
        console.log('Please validate the claim amount passed');
    }
  }
};
