const {I} = inject();

const config = require('../../config.js');

module.exports = {
   async createCaseSpecified() {
        await I.waitForText('Case list');
        await I.amOnPage(config.url.manageCase + '/cases/case-create/CIVIL/' + config.definition.caseType + '/CREATE_CLAIM_SPEC/CREATE_CLAIM_SPEC');
        await I.waitForText('Legal representatives: specified civil money claims service', 60);
    }
};

