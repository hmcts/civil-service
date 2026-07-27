const {I} = inject();

module.exports = {

  async verifyCOSDetails(claimantName, def1Name, def2Name) {
    I.waitInUrl('#Certificate', 3);
    I.seeNumberOfElements('#cosStatementOfTruthLabel h2', 2);
    I.see('Defendant 1 details');
    I.see('Defendant 2 details');
    I.see(claimantName);
    I.see(def1Name);
    I.see(def2Name);
  },

  async verifyCOSNCDetails(claimantName, def1Name, def2Name) {
    I.waitInUrl('#Certificate', 3);
    I.seeNumberOfElements('#cosStatementOfTruthLabel h2', 4);
    I.see('Defendant 1 details');
    I.see('Defendant 2 details');
    I.see('Certificate of Service - Notify Claim Details');
    I.see(claimantName);
    I.see(def1Name);
    I.see(def2Name);
  },
};
