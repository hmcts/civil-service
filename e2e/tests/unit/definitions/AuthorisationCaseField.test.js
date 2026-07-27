const { expect, assert} = require('chai');
const { uniqWith } = require('lodash');
const { isFieldDuplicated, isValidCaseTypeId } = require('../utils/utils');
const { createAssertExists } = require('../utils/assertBuilders');
const dataProvider = require('../utils/dataProvider');

const assertFieldExists = createAssertExists('Field');

function assertFieldDefinitionIsValid(row) {
  expect(row.CaseTypeID).to.be.a('string').and.satisfy(v => {
    return isValidCaseTypeId()(v);
  });
}

dataProvider.exclusions.forEach((value, key) =>  {
  describe('AuthorisationCaseField'.concat(': ', key, ' config'), () => {
    context('should :', () => {
      let authorisationCaseFieldConfig = [];
      let caseFieldConfig = [];
      let errors = [];
      let uniqResult = [];

      before(() => {
        authorisationCaseFieldConfig = dataProvider.getConfig('../../../../ccd-definition/AuthorisationCaseField', key);
        caseFieldConfig = dataProvider.getConfig('../../../../ccd-definition/CaseField', key);
        uniqResult = uniqWith(authorisationCaseFieldConfig, isFieldDuplicated('CaseFieldID'));
      });



      it('contain a unique case field ID, case type ID and role (no duplicates)', () => {
        try {
          expect(uniqResult).to.eql(authorisationCaseFieldConfig);
        } catch (error) {
          authorisationCaseFieldConfig.forEach(c => {
            if (!uniqResult.includes(c)) {
              errors.push(c.CaseFieldID);
            }
          });
        }
        if (errors.length) {
          assert.fail(`Found duplicated AuthorisationCaseField - ${errors}`);
        }
      });

      it('use existing fields', () => {
        assertFieldExists(authorisationCaseFieldConfig, caseFieldConfig);
      });

      it('should have only valid definitions', () => {
         uniqResult.forEach(assertFieldDefinitionIsValid);
      });
    });
  });
});
