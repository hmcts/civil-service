const { expect, assert} = require('chai');
const { uniqWith } = require('lodash');
const {
  MEDIUM_STRING,
  isNotEmpty,
  isNotLongerThan,
  noDuplicateFoundCCT
} = require('../utils/utils');
const dataProvider = require('../utils/dataProvider');

function assertFieldDefinitionIsValid(row) {
  const errors = [];
  if (row.length > 0) {
        row.forEach(elem => {
          try {
            expect(elem.ID).to.be.a('string').and.satisfy(isNotLongerThan(MEDIUM_STRING));
            expect(elem.CaseEventID).to.be.a('string').and.satisfy(isNotEmpty());
            expect(elem.CaseFieldID).to.be.a('string').and.satisfy(isNotEmpty());
            expect(elem.ListElementCode).to.be.a('string').and.satisfy(isNotEmpty());
          } catch (e) {
            errors.push(`\n${elem.ID} has failed`);
          }
        });
    } else {
      try {
        expect(row.ID).to.be.a('string').and.satisfy(isNotLongerThan(MEDIUM_STRING));
        expect(row.CaseEventID).to.be.a('string').and.satisfy(isNotEmpty());
        expect(row.CaseFieldID).to.be.a('string').and.satisfy(isNotEmpty());
        expect(row.ListElementCode).to.be.a('string').and.satisfy(isNotEmpty());
      } catch (e) {
        errors.push(`\n${row.ID} has failed`);
      }
    }

  if (errors.length) {
    assert.fail(`Broken tests (${errors.length}): ${errors}`);
  }
}

dataProvider.exclusions.forEach((value, key) =>  {
  describe('CaseEventToComplexTypes'.concat(': ', key, ' config'), () => {
    context('should :', () => {
      let uniqResult = [];
      let caseEventToComplexTypes = dataProvider.getConfig('../../../../ccd-definition/CaseEventToComplexTypes', key);

      before(() => {
        uniqResult = uniqWith(caseEventToComplexTypes, noDuplicateFoundCCT);
      });


      it('should have only valid definitions', () => {
        uniqResult.forEach(assertFieldDefinitionIsValid);
      });
    });
  });
});
