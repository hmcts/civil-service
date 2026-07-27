const { expect, assert} = require('chai');
const { uniqWith } = require('lodash');
const {
  MEDIUM_STRING,
  isNotEmpty,
  isNotLongerThan,
  noDuplicateFound,
  isValidCaseTypeId
} = require('../utils/utils');
const dataProvider = require('../utils/dataProvider');

function assertFieldDefinitionIsValid(row) {
  expect(row.CaseTypeID).to.be.a('string').and.satisfy(v => {
    return isValidCaseTypeId()(v);
  });
  expect(row.ID).to.be.a('string').and.satisfy(isNotLongerThan(MEDIUM_STRING));
  if (row.Name) {
    expect(row.Name).to.be.a('string').and.satisfy(isNotEmpty());
  }
  expect(row.Description).to.be.a('string').and.satisfy(isNotEmpty());
}

describe('CaseRoles', () => {
  context('should :', () => {
    let uniqResult = [];
    let caseRolesConfig = [];
    let errors = [];
    before(() => {
      caseRolesConfig = dataProvider.ccdData.CaseRoles;
      uniqResult = uniqWith(caseRolesConfig, noDuplicateFound);
    });

    it('not contain duplicated definitions of the same field', () => {
      try {
        expect(uniqResult).to.eql(caseRolesConfig);
      } catch (error) {
        caseRolesConfig.forEach(c => {
          if (!uniqResult.includes(c)) {
            errors.push(c.ID);
          }
        });
      }
      if (errors.length) {
        assert.fail(`Found duplicated CaseRoles - ${errors}`);
      }
    });

    it('should have only valid definitions', () => {
      uniqResult.forEach(assertFieldDefinitionIsValid);
    });
  });
});
