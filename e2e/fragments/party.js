const {I} = inject();
const postcodeLookup = require('./addressPostcodeLookup');

module.exports = {
  fields: function (partyType) {
    return {
      type: {
        id: `#${partyType}_type`,
        options: {
          individual: 'Individual',
          company: 'Company',
          organisation: 'Organisation',
          soleTrader: 'Sole trader',
        }
      },
      company: {
        name: `#${partyType}_companyName`
      },
      individual: {
        firstName: `#${partyType}_individualFirstName`,
        lastName: `#${partyType}_individualLastName`,
        dob_day: '#individualDateOfBirth-day',
        dob_month: '#individualDateOfBirth-month',
        dob_year: '#individualDateOfBirth-year',
      },
      Org: {
        name: `#${partyType}_organisationName`,
      },
      soleTrader: {
        firstName: `#${partyType}_soleTraderFirstName`,
        lastName: `#${partyType}_soleTraderLastName`,
        tradingAs: `#${partyType}_soleTraderTradingAs`,
        dob_day: '#soleTraderDateOfBirth-day',
        dob_month: '#soleTraderDateOfBirth-month',
        dob_year: '#soleTraderDateOfBirth-year',
      },
      email:  `#${partyType}_partyEmail`,
      address: `#${partyType}_primaryAddress_primaryAddress`
    };
  },

  async enterParty(partyType, address, optionType = 'Company') {
    I.waitForElement(this.fields(partyType).type.id);
    await I.runAccessibilityTest();
    if (optionType == 'Individual') {
      I.click(this.fields(partyType).type.options.individual);
      I.fillField(this.fields(partyType).individual.firstName, 'James Dan');
      I.fillField(this.fields(partyType).individual.lastName, 'Webb');
      I.fillField(this.fields(partyType).individual.dob_day, '9');
      I.fillField(this.fields(partyType).individual.dob_month, '9');
      I.fillField(this.fields(partyType).individual.dob_year, '1990');
    } else if (optionType == 'Organisation') {
      I.click(this.fields(partyType).type.options.organisation);
      I.fillField(this.fields(partyType).Org.name, `Example ${partyType} Org`);
    } else if (optionType == 'Sole trader') {
      I.click(this.fields(partyType).type.options.soleTrader);
      I.fillField(this.fields(partyType).soleTrader.firstName, 'James Dan');
      I.fillField(this.fields(partyType).soleTrader.lastName, 'Webb');
      I.fillField(this.fields(partyType).soleTrader.tradingAs, 'Webb Trading Name');
      I.fillField(this.fields(partyType).soleTrader.dob_day, '9');
      I.fillField(this.fields(partyType).soleTrader.dob_month, '9');
      I.fillField(this.fields(partyType).soleTrader.dob_year, '1990');
    } else {
      I.click(this.fields(partyType).type.options.company);
      I.fillField(this.fields(partyType).company.name, `Example ${partyType} company`);
    } 

    //==============================================================

    I.fillField(this.fields(partyType).email, `${partyType}@example.com`);

    postcodeLookup.enterAddressManually(address);
    await I.clickContinue();
  }
}
;

