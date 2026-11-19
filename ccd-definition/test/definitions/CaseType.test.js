const { expect } = require('chai');
const { uniqWith } = require('lodash');
const {
  isNotEmpty,
  noDuplicateFound,
  isValidCaseTypeId
} = require('../utils/utils');
const dataProvider = require('../utils/dataProvider');

function assertFieldDefinitionIsValid(row) {
  expect(row.ID).to.be.a('string').and.satisfy(v => {
    return isValidCaseTypeId()(v);
  });
  expect(row.SecurityClassification).to.eq('Public');
  expect(row.Name).to.be.a('string').and.satisfy(isNotEmpty());
  expect(row.Description).to.be.a('string').and.satisfy(isNotEmpty());
  expect(row.JurisdictionID).to.eql('CIVIL');
}

describe('CaseType', () => {
  context('should :', () => {
    let uniqResult = [];

    before(() => {
      uniqResult = uniqWith(dataProvider.ccdData.CaseType, noDuplicateFound);
    });

    it('should have only valid definitions', () => {
      uniqResult.forEach(assertFieldDefinitionIsValid);
    });
  });
});
