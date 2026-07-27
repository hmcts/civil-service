const {element} = require('../api/dataHelper');
module.exports = {
  witnessData: (lastName) => ({
    witnessesToAppear: 'Yes',
    details: [
      element({
        firstName: 'Witness',
        lastName,
        emailAddress: `witness-${lastName}@email.com`,
        phoneNumber: '07116778998',
        reasonForWitness: 'None'
      })
    ]
  })
};
