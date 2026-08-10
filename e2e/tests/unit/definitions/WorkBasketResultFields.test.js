const { expect } = require('chai');
const { uniqWith } = require('lodash');
const {
  isNotEmpty,
  noDuplicateFoundWB,
  isValidCaseTypeId
} = require('../utils/utils');
const dataProvider = require('../utils/dataProvider');

function assertFieldDefinitionIsValid(row) {
  expect(row.CaseTypeID).to.be.a('string').and.satisfy(v => {
    return isValidCaseTypeId()(v);
  });
  expect(row.CaseFieldID).to.be.a('string').and.satisfy(isNotEmpty());
  expect(row.Label).to.be.a('string').and.satisfy(isNotEmpty());
}

describe('WorkBasketResultFields', () => {
  context('should :', () => {
    let uniqResult = [];
    let workBasketResultFields = [];

    before(() => {
      workBasketResultFields = dataProvider.ccdData.WorkBasketResultFields;
      uniqResult = uniqWith(workBasketResultFields, noDuplicateFoundWB);
    });

    it('not contain duplicated definitions of the same field', () => {
      expect(uniqResult).to.eql(workBasketResultFields);
    });

    it('should have only valid definitions', () => {
      uniqResult.forEach(assertFieldDefinitionIsValid);
    });
  });
});
