const {I} = inject();

module.exports = {

  fields: function (party) {
    return {
      firstName: `#${party}MediationContactInfo_firstName`,
      lastName: `#${party}MediationContactInfo_lastName`,
      emailAddress: `#${party}MediationContactInfo_emailAddress`,
      phoneNumber: `#${party}MediationContactInfo_telephoneNumber`,
    };
  },

  async enterMediationContactInformation(party) {
    await I.fillField(this.fields(party).firstName, party);
    await I.fillField(this.fields(party).lastName, 'Mediation');
    await I.fillField(this.fields(party).emailAddress, `${party}@mediation.com`);
    await I.fillField(this.fields(party).phoneNumber, '07777777777');
    await I.clickContinue();
  },
};
