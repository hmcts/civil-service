const { expect, assert} = require('chai');
const { uniqWith } = require('lodash');
const {
  MEDIUM_STRING,
  isNotLongerThan,
  noDuplicateFoundCT,
  isValidCaseTypeId
} = require('../utils/utils');
const dataProvider = require('../utils/dataProvider');

function assertFieldDefinitionIsValid(row) {
  expect(row.CaseTypeID).to.be.a('string').and.satisfy(v => {
    return isValidCaseTypeId()(v);
  });
  expect(row.CaseFieldID).to.be.a('string').and.satisfy(isNotLongerThan(MEDIUM_STRING));
  // todo this isn't always populated
  // expect(row.TabLabel).to.be.a('string').and.satisfy(isNotEmpty());
}

dataProvider.exclusions.forEach((value, key) =>  {
  describe('CaseTypeTab'.concat(': ', key, ' config'), () => {
    context('should :', () => {
      let uniqResult = [];
      let caseTypeTabConfig = [];
      let errors = [];

      before(() => {
        caseTypeTabConfig = dataProvider.getConfig('../../../../ccd-definition/CaseTypeTab', key);
        uniqResult = uniqWith(caseTypeTabConfig, noDuplicateFoundCT);
      });

      it('not contain duplicated definitions of the same field', () => {
        try {
          expect(uniqResult).to.eql(caseTypeTabConfig);
        } catch (error) {
          caseTypeTabConfig.forEach(c => {
            if (!uniqResult.includes(c)) {
              errors.push(c.CaseFieldID);
            }
          });
        }
        if (errors.length) {
          assert.fail(`Found duplicated CaseTypeTab - ${errors}`);
        }
      });

      it('should have only valid definitions', () => {
        uniqResult.forEach(assertFieldDefinitionIsValid);
      });
    });
  });
});
