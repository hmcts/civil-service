const { expect } = require('chai');
const { uniqWith } = require('lodash');
const {
  isNotEmpty,
  noDuplicateFoundAccessProfiles,
  isValidCaseTypeId
} = require('../utils/utils');
const dataProvider = require('../utils/dataProvider');

function assertFieldDefinitionIsValid(row) {
  expect(row.CaseTypeID).to.be.a('string').and.satisfy(v => {
    return isValidCaseTypeId()(v);
  });
  expect(row.RoleName).to.be.a('string').and.satisfy(isNotEmpty());
  expect(row.AccessProfiles).to.be.a('string').and.satisfy(isNotEmpty());
}

dataProvider.exclusions.forEach((value, key) =>  {
  describe('RoleToAccessProfiles'.concat(': ', key, ' config'), () => {
    context('should :', () => {
      let uniqResult = [];
      let roleToAccessProfiles = [];

      before(() => {
        roleToAccessProfiles = dataProvider.getConfig('../../../../ccd-definition/RoleToAccessProfiles', key);
        uniqResult = uniqWith(roleToAccessProfiles, noDuplicateFoundAccessProfiles);
      });

      it('should have only valid definitions', () => {
        uniqResult.forEach(assertFieldDefinitionIsValid);
      });
    });
  });
});
