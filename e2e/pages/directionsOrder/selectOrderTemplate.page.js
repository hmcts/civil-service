const {I} = inject();
const {expect} = require('chai');

const mtExpectedOptions = [
  'Blank template to be used after a hearing',
  'Blank template to be used before a hearing/box work',
  'Fix a date for CMC',
  'Fix a date for CCMC' // Only for Multi Track
];

const intExpectedOptions = [
  'Blank template to be used after a hearing',
  'Blank template to be used before a hearing/box work',
  'Fix a date for CMC'
];


module.exports = {
  fields: {
    label: '//h3[contains(text(), "Which template do you wish to use?")]',
    dropdown: '#finalOrderDownloadTemplateOptions',
  },

  async selectTemplateByText(trackType, optionText) {
    I.seeElement(this.fields.label);
    I.see('Which template do you wish to use?', this.fields.label);
    if (trackType === 'Intermediate Track') {
      await this.verifyDropdownOptions(intExpectedOptions);
    } else if (trackType === 'Multi Track') {
      await this.verifyDropdownOptions(mtExpectedOptions);
    }
    I.selectOption(this.fields.dropdown, optionText);
    I.seeInField(this.fields.dropdown, optionText);
    await I.clickContinue();
  },

  async verifyDropdownOptions(expectedOptions) {
    I.seeElement(this.fields.label);
    I.see('Which template do you wish to use?', this.fields.label);

    // Grab all options from the dropdown
    const actualOptions = await I.grabTextFromAll(`${this.fields.dropdown} option`);

    // Remove the placeholder "--Select a value--"
    const filteredOptions = actualOptions.filter(option => option !== '--Select a value--');

    // Assert that each expected option is in the actual options
    expectedOptions.forEach(expectedOption => {
      expect(filteredOptions).to.include(expectedOption,
        `Expected option "${expectedOption}" was not found`);
    });

    // Assert that the number of options matches
    expect(filteredOptions.length).to.equal(expectedOptions.length,
      'Number of dropdown options does not match the expected number');
  }
};
