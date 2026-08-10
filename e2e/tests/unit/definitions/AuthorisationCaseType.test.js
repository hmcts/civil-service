const { expect, assert} = require('chai');
const { uniqWith } = require('lodash');
const { noDuplicateFoundACT, isValidCaseTypeId } = require('../utils/utils');
const dataProvider = require('../utils/dataProvider');

function assertFieldDefinitionIsValid(row) {
  expect(row.CaseTypeID).to.be.a('string').and.satisfy(v => {
    return isValidCaseTypeId()(v);
  });
  expect(row.UserRoles).to.not.be.null;
  expect(row.AccessControl).to.not.be.null;
}

dataProvider.exclusions.forEach((value, key) =>  {
  describe('AuthorisationCaseType'.concat(': ', key, ' config'), () => {
    context('should :', () => {
      let uniqResult = [];
      let authorisationCaseType = [];
      let errors = [];

      before(() => {
        authorisationCaseType = dataProvider.getConfig('../../../../ccd-definition/AuthorisationCaseType', key);
        uniqResult = uniqWith(authorisationCaseType, noDuplicateFoundACT);
      });

      it('not contain duplicated definitions of the same field', () => {
        try {
          expect(uniqResult).to.eql(authorisationCaseType);
        } catch (error) {
          authorisationCaseType.forEach(c => {
            if (!uniqResult.includes(c)) {
              errors.push(c.CaseTypeID);
            }
          });
        }
        if (errors.length) {
          assert.fail(`Found duplicated AuthorisationCaseType - ${errors}`);
        }
      });

      it('should have only valid definitions', () => {
        uniqResult.forEach(assertFieldDefinitionIsValid);
      });
    });
  });
});
