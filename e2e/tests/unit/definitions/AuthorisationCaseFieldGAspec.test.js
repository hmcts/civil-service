const { expect, assert} = require('chai');
const { uniqWith } = require('lodash');
const { isFieldDuplicated } = require('../utils/utils');
const { createAssertExists } = require('../utils/assertBuilders');
const dataProvider = require('../utils/dataProvider');

const assertFieldExists = createAssertExists('Field');

dataProvider.exclusions.forEach((value, key) =>  {
  describe('AuthorisationCaseField'.concat(': ', key, ' config'), () => {
    context('should :', () => {
      let authorisationCaseFieldConfig = [];
      let caseFieldConfig = [];
      let errors = [];

      before(() => {
        authorisationCaseFieldConfig = dataProvider.getConfig('../../../../ga-ccd-definition/AuthorisationCaseField', key);
        caseFieldConfig = dataProvider.getConfig('../../../../ga-ccd-definition/CaseField', key);
      });

      it('contain a unique case field ID, case type ID and role (no duplicates)', () => {
        const uniqResult = uniqWith(authorisationCaseFieldConfig, isFieldDuplicated('CaseFieldID'));
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
    });
  });
});
