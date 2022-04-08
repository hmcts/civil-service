const config = require('../../../config.js');
const {assignCaseToDefendant} = require('../../../api/testingSupport');

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

Scenario('Applicant solicitor creates specified claim individual-to-individual @create-claim-spec', async ({I}) => {
  console.log('Applicant solicitor creates specified claim individual-to-individual @create-claim-spec');
  await I.login(config.applicantSolicitorUser);
  await I.createCaseSpec(journeys.journey1.applicant, journeys.journey1.defendant,false,1000);
  caseNumber = await I.grabCaseNumber();
  await I.see(`Case ${caseNumber} has been created.`);
}).retry(3);

Scenario('Applicant solicitor creates specified claim individual-to-company @create-claim-spec', async ({I}) => {
  console.log('Applicant solicitor creates specified claim individual-to-company @create-claim-spec');
  await I.login(config.applicantSolicitorUser);
  await I.createCaseSpec(journeys.journey2.applicant, journeys.journey2.defendant,false,1000);
  caseNumber = await I.grabCaseNumber();
  await I.see(`Case ${caseNumber} has been created.`);
}).retry(3);

Scenario('Applicant solicitor creates specified claim individual-to-soletrader @create-claim-spec', async ({I}) => {
  console.log(' Applicant solicitor creates specified claim individual-to-soletrader @create-claim-spec ');
  await I.login(config.applicantSolicitorUser);
  await I.createCaseSpec(journeys.journey3.applicant, journeys.journey3.defendant,false,1000);
  caseNumber = await I.grabCaseNumber();
  await I.see(`Case ${caseNumber} has been created.`);
}).retry(3);

Scenario('Applicant solicitor creates specified claim individual-to-organisation @create-claim-spec', async ({I}) => {
  console.log('Applicant solicitor creates specified claim individual-to-organisation @create-claim-spec ');
  await I.login(config.applicantSolicitorUser);
  await I.createCaseSpec(journeys.journey4.applicant, journeys.journey4.defendant,false,1000);
  caseNumber = await I.grabCaseNumber();
  await I.see(`Case ${caseNumber} has been created.`);
}).retry(3);

Scenario('Applicant solicitor creates specified claim company-to-company @create-claim-spec', async ({I}) => {
  console.log(' Applicant solicitor creates specified claim company-to-company @create-claim-spec');
  await I.login(config.applicantSolicitorUser);
  await I.createCaseSpec(journeys.journey5.applicant, journeys.journey5.defendant,false,1000);
  caseNumber = await I.grabCaseNumber();
  await I.see(`Case ${caseNumber} has been created.`);
}).retry(3);

Scenario('Applicant solicitor creates specified claim company-to-soletrader @create-claim-spec', async ({I}) => {
  console.log(' Applicant solicitor creates specified claim company-to-soletrader @create-claim-spec');
  await I.login(config.applicantSolicitorUser);
  await I.createCaseSpec(journeys.journey6.applicant, journeys.journey6.defendant,false,1000);
  caseNumber = await I.grabCaseNumber();
  await I.see(`Case ${caseNumber} has been created.`);
}).retry(3);

Scenario('Applicant solicitor creates specified claim company-to-organisation @create-claim-spec', async ({I}) => {
  console.log('Applicant solicitor creates specified claim company-to-organisation @create-claim-spec ');
  await I.login(config.applicantSolicitorUser);
  await I.createCaseSpec(journeys.journey7.applicant, journeys.journey7.defendant,false,1000);
  caseNumber = await I.grabCaseNumber();
  await I.see(`Case ${caseNumber} has been created.`);
}).retry(3);

Scenario('Applicant solicitor creates specified claim soletrader-to-soletrader @create-claim', async ({I}) => {
  console.log(' Applicant solicitor creates specified claim soletrader-to-soletrader @create-claim-spec ');
  await I.login(config.applicantSolicitorUser);
  await I.createCaseSpec(journeys.journey8.applicant, journeys.journey8.defendant,false,1000);
  caseNumber = await I.grabCaseNumber();
  await I.see(`Case ${caseNumber} has been created.`);
}).retry(3);

Scenario('Applicant solicitor creates specified claim soletrader-to-organisation @create-claim-spec', async ({I}) => {
  console.log('Applicant solicitor creates specified claim soletrader-to-organisation @create-claim-spec ');
  await I.login(config.applicantSolicitorUser);
  await I.createCaseSpec(journeys.journey9.applicant, journeys.journey9.defendant,false,1000);
  caseNumber = await I.grabCaseNumber();
  await I.see(`Case ${caseNumber} has been created.`);
}).retry(3);

Scenario('Applicant solicitor creates small track for claim amount £1000 organisation-to-organisation @create-claim-spec ', async ({I}) => {
  console.log(' Applicant solicitor creates specified claim organisation-to-organisation @create-claim-spec');
  await I.login(config.applicantSolicitorUser);
  await I.createCaseSpec(journeys.journey10.applicant, journeys.journey10.defendant,false,1000);
  caseNumber = await I.grabCaseNumber();
  await I.see(`Case ${caseNumber} has been created.`);
  await assignCaseToDefendant(caseId());
  await I.login(config.defendantSolicitorUser);
}).retry(3);

Scenario('Defendant solicitor acknowledges claim-spec', async ({I}) => {
  console.log(' Defendant solicitor acknowledges claim-spec: ' + caseId());
  await assignCaseToDefendant(caseId());
  await I.login(config.defendantSolicitorUser);
  await I.acknowledgeClaimSpec();
  await I.see(caseEventMessage('Acknowledgement of Service'));
}).retry(3);

Scenario('Small Track Claim -> Defendant solicitor responds to claim (£ 1000) -> Defends all of the claim -> has paid amount equal to claim amount ', async ({I}) => {
  await I.login(config.defendantSolicitorUser);
  await I.respondToClaimSpec('fullDefence','hasPaid',1000);
  await I.see(caseEventMessage('Respond to claim'));
}).retry(3);

Scenario('Small Track Claim -> Defendant solicitor responds to claim (£ 1000) -> Defends all of the claim -> has paid £ 100 less than claimed amount ', async ({I}) => {
  await I.login(config.defendantSolicitorUser);
  await I.respondToClaimSpec('fullDefence','hasPaid',100);
  await I.see(caseEventMessage('Respond to claim'));
}).retry(3);

Scenario('Small Track Claim -> Defendant solicitor responds to claim (£ 1000) -> Defends all of the claim -> dispute the claim ', async ({I}) => {
  await I.login(config.defendantSolicitorUser);
  await I.respondToClaimSpec('fullDefence','dispute',100);
  await I.see(caseEventMessage('Respond to claim'));
}).retry(3);


Scenario('Applicant solicitor creates Fast Track claim for amount £ 15000 organisation-to-organisation @create-claim-spec ', async ({I}) => {
  console.log(' Applicant solicitor creates specified claim organisation-to-organisation @create-claim-spec');
  await I.login(config.applicantSolicitorUser);
  await I.createCaseSpec(journeys.journey10.applicant, journeys.journey10.defendant,false,15000);
  caseNumber = await I.grabCaseNumber();
  await I.see(`Case ${caseNumber} has been created.`);
  await assignCaseToDefendant(caseId());
  await I.login(config.defendantSolicitorUser);
}).retry(3);

Scenario('Fast Track claim -> Defendant solicitor responds to claim amount £ 15000) -> Defends all of the claim -> dispute ', async ({I}) => {
  await I.login(config.defendantSolicitorUser);
  await I.respondToClaimSpec('fullDefence','dispute',10000);
  await I.see(caseEventMessage('Respond to claim'));
}).retry(3);

Scenario('Fast Track claim -> Defendant solicitor responds to claim amount £ 15000) -> Defends all of the claim -> hasPaid Equal to claimed amount ', async ({I}) => {
  await I.login(config.defendantSolicitorUser);
  await I.respondToClaimSpec('fullDefence','hasPaid',15000);
  await I.see(caseEventMessage('Respond to claim'));
}).retry(3);

Scenario('Fast Track claim -> Defendant solicitor responds to claim amount £ 15000) -> Defends all of the claim -> hasPaid  10000 less than claimed amount ', async ({I}) => {
  await I.login(config.defendantSolicitorUser);
  await I.respondToClaimSpec('fullDefence','hasPaid',10000);
  await I.see(caseEventMessage('Respond to claim'));
}).retry(3);
