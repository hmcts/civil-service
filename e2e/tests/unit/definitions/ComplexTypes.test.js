const { expect } = require('chai');
const { uniqWith } = require('lodash');
const {
  MEDIUM_STRING,
  isNotEmpty,
  isNotLongerThan,
  noDuplicateFoundFL
} = require('../utils/utils');
const dataProvider = require('../utils/dataProvider');

function assertFieldDefinitionIsValid(row) {
  expect(row.ID).to.be.a('string').and.satisfy(isNotLongerThan(MEDIUM_STRING));
  if (row.ListElementCode) {
    expect(row.ListElementCode).to.be.a('string').and.satisfy(isNotEmpty());
  }
  expect(row.SecurityClassification).to.eq('Public');
  expect(row.FieldType).to.be.a('string').and.satisfy(isNotLongerThan(MEDIUM_STRING));
  if (row.FieldType === 'Collection' || row.FieldType === 'FixedList' ||
   row.FieldType === 'FixedRadioList' || row.FieldType === 'MultiSelectList') {
    expect(row.FieldTypeParameter).to.be.a('string').and.satisfy(isNotEmpty());
  }
}

dataProvider.exclusions.forEach((value, key) =>  {
  describe('ComplexTypes'.concat(': ', key, ' config'), () => {
    context('should :', () => {
      let uniqResult = [];
      let complexTypesConfig = [];

      before(() => {
        complexTypesConfig = dataProvider.getConfig('../../../../ccd-definition/ComplexTypes', key);
        uniqResult = uniqWith(complexTypesConfig, noDuplicateFoundFL);
      });


      it('should have only valid definitions', () => {
        uniqResult.forEach(assertFieldDefinitionIsValid);
      });
    });
  });
});
