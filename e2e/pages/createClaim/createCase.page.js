const {I} = inject();

const config = require('../../config.js');

module.exports = {
  async createCase() {
    await I.waitForText('Case list');
    await I.amOnPage(config.url.manageCase + '/cases/case-create/CIVIL/'+ config.definition.caseType +'/CREATE_CLAIM/CREATE_CLAIM');
    await I.waitForText('Issue civil court proceedings', 60);
  }
};
