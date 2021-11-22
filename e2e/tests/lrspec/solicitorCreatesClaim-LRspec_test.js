const config = require('../../config.js');
const {assignCaseToDefendant} = require('../../api/testingSupport');

const caseEventMessage = eventName => `Case ${caseNumber} has been updated with event: ${eventName}`;
const caseId = () => `${caseNumber.split('-').join('').replace(/#/, '')}`;

let caseNumber;

const journeys = {
  journey1: {
    applicant: 'individual',
    defendant: 'individual',
  },
  journey2: {
    applicant: 'individual',
    defendant: 'company',
  },
  journey3: {
    applicant: 'individual',
    defendant: 'soleTrader',
  },
  journey4: {
    applicant: 'individual',
    defendant: 'organisation',
  },
  journey5: {
    applicant: 'company',
    defendant: 'company',
  },
  journey6: {
    applicant: 'company',
    defendant: 'soleTrader',
  },
  journey7: {
    applicant: 'company',
    defendant: 'organisation',
  },
  journey8: {
    applicant: 'soleTrader',
    defendant: 'soleTrader',
  },
  journey9: {
    applicant: 'soleTrader',
    defendant: 'organisation',
  },
  journey10: {
    applicant: 'organisation',
    defendant: 'organisation',
  },
};

Feature('Claim creation @e2e-tests-spec');

Scenario.skip('Applicant solicitor creates specified claim individual-to-individual @create-claim-spec', async ({I}) => {
  console.log('Applicant solicitor creates specified claim individual-to-individual @create-claim-spec');
  await I.login(config.applicantSolicitorUser);
  await I.createCaseSpec(journeys.journey1.applicant, journeys.journey1.defendant);
  caseNumber = await I.grabCaseNumber();
  await I.see(`Case ${caseNumber} has been created.`);
}).retry(3);

Scenario.skip('Applicant solicitor creates specified claim individual-to-company @create-claim-spec', async ({I}) => {
  console.log('Applicant solicitor creates specified claim individual-to-company @create-claim-spec');
  await I.login(config.applicantSolicitorUser);
  await I.createCaseSpec(journeys.journey2.applicant, journeys.journey2.defendant);
  caseNumber = await I.grabCaseNumber();
  await I.see(`Case ${caseNumber} has been created.`);
}).retry(3);

Scenario.skip('Applicant solicitor creates specified claim individual-to-soletrader @create-claim-spec', async ({I}) => {
  console.log(' Applicant solicitor creates specified claim individual-to-soletrader @create-claim-spec ');
  await I.login(config.applicantSolicitorUser);
  await I.createCaseSpec(journeys.journey3.applicant, journeys.journey3.defendant);
  caseNumber = await I.grabCaseNumber();
  await I.see(`Case ${caseNumber} has been created.`);
}).retry(3);

Scenario.skip('Applicant solicitor creates specified claim individual-to-organisation @create-claim-spec', async ({I}) => {
  console.log('Applicant solicitor creates specified claim individual-to-organisation @create-claim-spec ');
  await I.login(config.applicantSolicitorUser);
  await I.createCaseSpec(journeys.journey4.applicant, journeys.journey4.defendant);
  caseNumber = await I.grabCaseNumber();
  await I.see(`Case ${caseNumber} has been created.`);
}).retry(3);

Scenario.skip('Applicant solicitor creates specified claim company-to-company @create-claim-spec', async ({I}) => {
  console.log(' Applicant solicitor creates specified claim company-to-company @create-claim-spec');
  await I.login(config.applicantSolicitorUser);
  await I.createCaseSpec(journeys.journey5.applicant, journeys.journey5.defendant);
  caseNumber = await I.grabCaseNumber();
  await I.see(`Case ${caseNumber} has been created.`);
}).retry(3);

Scenario.skip('Applicant solicitor creates specified claim company-to-soletrader @create-claim-spec', async ({I}) => {
  console.log(' Applicant solicitor creates specified claim company-to-soletrader @create-claim-spec');
  await I.login(config.applicantSolicitorUser);
  await I.createCaseSpec(journeys.journey6.applicant, journeys.journey6.defendant);
  caseNumber = await I.grabCaseNumber();
  await I.see(`Case ${caseNumber} has been created.`);
}).retry(3);

Scenario.skip('Applicant solicitor creates specified claim company-to-organisation @create-claim-spec', async ({I}) => {
  console.log('Applicant solicitor creates specified claim company-to-organisation @create-claim-spec ');
  await I.login(config.applicantSolicitorUser);
  await I.createCaseSpec(journeys.journey7.applicant, journeys.journey7.defendant);
  caseNumber = await I.grabCaseNumber();
  await I.see(`Case ${caseNumber} has been created.`);
}).retry(3);

Scenario.skip('Applicant solicitor creates specified claim soletrader-to-soletrader @create-claim', async ({I}) => {
  console.log(' Applicant solicitor creates specified claim soletrader-to-soletrader @create-claim-spec ');
  await I.login(config.applicantSolicitorUser);
  await I.createCaseSpec(journeys.journey8.applicant, journeys.journey8.defendant);
  caseNumber = await I.grabCaseNumber();
  await I.see(`Case ${caseNumber} has been created.`);
}).retry(3);

Scenario.skip('Applicant solicitor creates specified claim soletrader-to-organisation @create-claim-spec', async ({I}) => {
  console.log('Applicant solicitor creates specified claim soletrader-to-organisation @create-claim-spec ');
  await I.login(config.applicantSolicitorUser);
  await I.createCaseSpec(journeys.journey9.applicant, journeys.journey9.defendant);
  caseNumber = await I.grabCaseNumber();
  await I.see(`Case ${caseNumber} has been created.`);
}).retry(3);

Scenario('Applicant solicitor creates specified claim organisation-to-organisation @create-claim-spec ', async ({I}) => {
  console.log(' Applicant solicitor creates specified claim organisation-to-organisation @create-claim-spec');
  await I.login(config.applicantSolicitorUser);
  await I.createCaseSpec(journeys.journey10.applicant, journeys.journey10.defendant);
  caseNumber = await I.grabCaseNumber();
  await I.see(`Case ${caseNumber} has been created.`);
  console.log('Defendant solicitor acknowledges claim-spec: ' + caseId());
  await assignCaseToDefendant(caseId());
  await I.login(config.defendantSolicitorUser);
  await I.acknowledgeClaimSpec();
  await I.see(caseEventMessage('Acknowledgement of Service'));
}).retry(3);

Scenario.skip('Defendant solicitor acknowledges claim-spec', async ({I}) => {
  console.log(' Defendant solicitor acknowledges claim-spec: ' + caseId());
  await assignCaseToDefendant(caseId());
  await I.login(config.defendantSolicitorUser);
  await I.acknowledgeClaimSpec();
  await I.see(caseEventMessage('Acknowledgement of Service'));
}).retry(3);
