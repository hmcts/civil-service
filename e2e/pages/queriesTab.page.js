const { I } = inject();

module.exports = {

  async verifyQueriesDetails(hearing = false) {
    I.waitInUrl('#Queries', 10);
    await I.waitForElement('table.query-list__table');
    I.see('Query subject');
    I.see('Last submitted by');
    I.see('Last submission date');
    I.see('Last response date');
    I.see('Response status');
    I.see('Awaiting Response');
    if (!hearing) {
      I.see('Test query subject');
      I.click('Test query subject');
      I.waitForText('Query details');
    }
    if (hearing) {
      I.see('Test Hearing query subject');
      I.click('Test Hearing query subject');
      I.waitForText('Query details');
      I.see('Is the query hearing related?');
      I.see('What is the date of the hearing?');
      I.see('Test Hearing query detail');
    }
  },

  async verifyDetailsAsCaseWorker(hearing = false) {
    I.waitInUrl('#Queries', 10);
    await I.waitForElement('table.query-list__table');
    I.see('Query subject');
    I.see('Last submitted by');
    I.see('Last submission date');
    I.see('Last response date');
    I.see('Response status');
    I.see('Awaiting Response');
    if (!hearing) {
      I.see('Test query subject');
      I.click('Test query subject');
      I.waitForText('Query details');
    }
    if (hearing) {
      I.see('Test Hearing query subject');
      I.click('Test Hearing query subject');
      I.waitForText('Query details');
      I.see('Is the query hearing related?');
      I.see('What is the date of the hearing?');
      I.see('Test Hearing query detail');
    }
    I.see('Is the query hearing related?');
  },

  async askFollowUpQuestion(party = false) {
    I.waitInUrl('#Queries', 10);
    await I.waitForElement('table.query-list__table');
    I.see('Last submitted by');
    I.see('Last submission date');
    I.see('Last response date');
    I.see('Response status');
    I.see('Responded');
    // Commented due to XUI bug
    // I.see('Caseworker');
    if (party) {
      I.see('Claimant Query');
      I.click('Claimant Query');
      I.waitForText('Query details');
      I.see('Query details');
      I.see('This query was raised by Claimant.');
      I.see('TestFile.pdf');
      I.see('Caseworker response to query.');
    }
    I.click('Ask a follow-up question');
    I.waitForText('Attach a document to this query (Optional)');
    if (party) {
      I.fillField('textarea[id="body"]', 'Claimant follow up');
    }
    I.retryUntilExists(() => I.click('Continue'), '//h1[contains(text(), \'Review query details\')]');
  },

  async verifyFollowUpQuestion(party = false) {
   await I.waitInUrl('#Queries', 10);
   await I.waitForElement('table.query-list__table');
    I.see('Last submitted by');
    I.see('Last submission date');
    I.see('Last response date');
    I.see('Response status');
    I.see('Awaiting Response');
    if (party) {
      I.see('Claimant Query');
      I.click('Claimant Query');
      I.waitForText('Query details');
      I.see('Query details');
      I.see('This query was raised by Claimant.');
      I.see('TestFile.pdf');
      I.see('Caseworker response to query.');
      I.see('Follow up query');
      I.see('Query detail');
    }
  },

  async verifyFollowUpQuestionAsCourtStaff(party = false) {
    I.waitInUrl('#Queries', 10);
    await I.waitForElement('table.query-list__table');
    I.see('Last submitted by');
    I.see('Last submission date');
    I.see('Last response date');
    I.see('Response status');
    I.see('Awaiting Response');
    if (party) {
      I.see('Claimant Query');
      I.click('Claimant Query');
      I.waitForText('Query details');
      I.see('Query details');
      I.see('This query was raised by Claimant.');
      I.see('TestFile.pdf');
      I.see('Caseworker response to query.');
      I.see('Follow up query');
      I.see('Query detail');
    }
  },
};
