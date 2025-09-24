const apiRequest = require('./apiRequest');
const chai = require('chai');
const {expect} = chai;
const eventName = 'MANAGE_CONTACT_INFORMATION';

const updateDetails = async (caseId, updatedData) => {
  const response = await apiRequest.submitEvent(eventName, updatedData, caseId);
  expect(response.status).equal(201);

  const {case_data} = await response.json();
  return case_data;
};

const updateExpert = async (caseId, updatedData) => {
  console.log('Updating Defendant 1 expert details');
  const response = await apiRequest.submitEvent(eventName, updatedData, caseId);
  expect(response.status).equal(201);

  const {case_data} = await response.json();
  let expert = updatedData.respondent1DQExperts.details[0].value;
  expect(case_data.respondent1DQExperts.details[0].value.partyID).deep.equal(expert.partyID);
  expect(case_data.respondent1DQExperts.details[0].value.firstName).deep.equal(expert.firstName);
  expect(case_data.respondent1DQExperts.details[1].value).to.have.a.property('partyID');
  expect(case_data.respondent1DQExperts.details[1].value.firstName).deep.equal('Stan');
  console.log('Defendant 1 expert details updated');
};

const updateApplicant = async (caseId, updatedData) => {
  console.log('Updating applicant details');

  const response = await updateDetails(caseId, updatedData);

  expect(response.applicant1.partyEmail).deep.equal(updatedData.applicant1.partyEmail);
  console.log('Applicant details updated');
};

const updateLROrganisation = async (caseId, updatedData) => {
  console.log('Updating Defendant 1 LR Organisation details');

  const response = await updateDetails(caseId, updatedData);

  const expectedLRIndividual = updatedData.updateLRIndividualsForm[0].value;
  const actualLRIndividual = response.respondent1LRIndividuals[0].value;

  expect(actualLRIndividual.firstName).equal(expectedLRIndividual.firstName);
  expect(actualLRIndividual.lastName).equal(expectedLRIndividual.lastName);
  expect(actualLRIndividual.email).equal(expectedLRIndividual.emailAddress);
  expect(actualLRIndividual.phone).equal(expectedLRIndividual.phoneNumber);
  expect(actualLRIndividual).to.have.a.property('partyID');
  
  console.log('Defendant 1 LR Organisation details updated');
};

module.exports = {
  updateApplicant,
  updateExpert,
  updateLROrganisation
};
