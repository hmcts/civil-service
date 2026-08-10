const { I } = inject();
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
        },
      },
      individual: {
        title: `#${partyType}_individualTitle`,
        firstName: `#${partyType}_individualFirstName`,
        lastName: `#${partyType}_individualLastName`,
        day: '#individualDateOfBirth-day',
        month: '#individualDateOfBirth-month',
        year: '#individualDateOfBirth-year',
      },
      company: {
        name: `#${partyType}_companyName`,
      },
      soleTrader: {
        title: `#${partyType}_soleTraderTitle`,
        firstName: `#${partyType}_soleTraderFirstName`,
        lastName: `#${partyType}_soleTraderLastName`,
        tradingAs: `#${partyType}_soleTraderTradingAs`,
        day: '#soleTraderDateOfBirth-day',
        month: '#soleTraderDateOfBirth-month',
        year: '#soleTraderDateOfBirth-year',
      },
      organisation: {
        name: `#${partyType}_organisationName`,
      },
      address: `#${partyType}_primaryAddress_primaryAddress`,
    };
  },

  async enterDetails(partyType, address, party) {
    I.waitForElement(this.fields(partyType).type.id);
    await I.runAccessibilityTest();
    await within(this.fields(partyType).type.id, () => {
      I.click(this.fields(partyType).type.options[party]);
    });

    switch (party) {
      case 'individual':
        I.fillField(this.fields(partyType).individual.title, 'Mr');
        I.fillField(this.fields(partyType).individual.firstName, 'John');
        I.fillField(this.fields(partyType).individual.lastName, 'Jones');
        if (partyType === 'applicant1') {
          I.fillField(this.fields(partyType).individual.day, 1);
          I.fillField(this.fields(partyType).individual.month, 1);
          I.fillField(this.fields(partyType).individual.year, 1981);
        }
        break;
      case 'company':
        I.fillField(this.fields(partyType).company.name, `Example ${partyType} company`);
        break;
      case 'soleTrader':
        I.fillField(this.fields(partyType).soleTrader.title, 'Mr');
        I.fillField(this.fields(partyType).soleTrader.firstName, 'John');
        I.fillField(this.fields(partyType).soleTrader.lastName, 'Jones');
        if (partyType === 'applicant1') {
          I.fillField(this.fields(partyType).soleTrader.tradingAs, 'John Jones Windows');
          I.fillField(this.fields(partyType).soleTrader.day, 1);
          I.fillField(this.fields(partyType).soleTrader.month, 1);
          I.fillField(this.fields(partyType).soleTrader.year, 1981);
        }
        break;
      case 'organisation':
        I.fillField(this.fields(partyType).organisation.name, `Example ${partyType} organisation`);
        break;
    }

    await within(this.fields(partyType).address, () => {
      postcodeLookup.enterAddressManually(address);
    });

    await I.clickContinue();
  },
};
