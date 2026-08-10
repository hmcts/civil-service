const config = require('../../config');
const {I} = inject();

module.exports = {

  async verifyApplicationDetails(applicationTypes, appCount) {
    if (['preview','aat'].includes(config.runningEnv)) {
      await I.wait(5);
    } else {
      await I.wait(3);
    }
    await I.waitInUrl('#Applications', 10);
    await I.waitForText('Application type', 10, locate('td[id*="GaAppDetails"] th span').first());
    await I.seeNumberOfElements('.collection-field-table .complex-panel-title', appCount);
    I.see('Application type');
    I.see('Application ID');
    I.see('Status');
    I.see('Submitted on');
    applicationTypes.forEach(type => {
      return I.see(type);
    });
  },
};
