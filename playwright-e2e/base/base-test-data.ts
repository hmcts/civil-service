import claimantDefendantPartyTypes from '../constants/claimant-defendant-party-types';
import CaseFlagsHelper from '../helpers/case-flags-helper';
import DateHelper from '../helpers/date-helper';
import CaseFlags from '../models/case-flag';
import CCDCaseData from '../models/ccd/ccd-case-data';
import { ClaimantDefendantPartyType } from '../models/claimant-defendant-party-types';
import TestData from '../models/test-data';

export default abstract class BaseTestData {
  private _testData: TestData;

  constructor(testData: TestData) {
    this._testData = testData;
  }

  protected get workerIndex() {
    return this._testData.workerIndex;
  }

  protected get ccdCaseData() {
    return this._testData.ccdCaseData;
  }

  protected set setCCDCaseData(ccdCaseData: CCDCaseData) {
    this._testData.ccdCaseData = ccdCaseData;
  }

  protected get claimant1PartyType() {
    return this._testData.claimant1PartyType;
  }

  protected set setClaimant1PartyType(claimant1PartyType: ClaimantDefendantPartyType) {
    this._testData.claimant1PartyType = claimant1PartyType;
    if (claimant1PartyType)
      console.log('Set Claimant 1 Party Type: ' + this._testData.claimant1PartyType?.type);
  }

  protected get claimant2PartyType() {
    return this._testData.claimant2PartyType;
  }

  protected set setClaimant2PartyType(claimant2PartyType: ClaimantDefendantPartyType) {
    this._testData.claimant2PartyType = claimant2PartyType;
    if (claimant2PartyType)
      console.log('Set Claimant 2 Party Type: ' + this._testData.claimant2PartyType?.type);
  }

  protected get defendant1PartyType() {
    return this._testData.defendant1PartyType;
  }

  protected set setDefendant1PartyType(defendant1PartyType: ClaimantDefendantPartyType) {
    this._testData.defendant1PartyType = defendant1PartyType;
    if (defendant1PartyType)
      console.log('Set Defendant 1 Party Type: ' + this._testData.defendant1PartyType?.type);
  }

  protected get defendant2PartyType() {
    return this._testData.defendant2PartyType;
  }

  protected set setDefendant2PartyType(defendant2PartyType: ClaimantDefendantPartyType) {
    this._testData.defendant2PartyType = defendant2PartyType;
    if (defendant2PartyType)
      console.log('Set Defendant 2 Party Type: ' + this._testData.defendant2PartyType?.type);
  }

  protected get activeCaseFlags() {
    return this._testData.caseFlags.activeCaseFlags;
  }

  protected get caseFlagsDetails() {
    return this._testData.caseFlags.caseFlagsDetails;
  }

  protected get firstActiveCaseFlagDetails() {
    return this._testData.caseFlags.caseFlagsDetails.find(
      (caseFlagDetail) => caseFlagDetail.active,
    );
  }

  protected addCaseFlag(caseFlagDetail: {
    caseFlagLocation: string;
    caseFlagType: string;
    caseFlagComment: string;
  }) {
    console.log(
      `Adding case flag to location: ${caseFlagDetail.caseFlagLocation} with type: ${caseFlagDetail.caseFlagType}`,
    );
    this._testData.caseFlags.caseFlagsDetails.push({
      ...caseFlagDetail,
      creationDate: DateHelper.formatDateToString(DateHelper.getToday(), {
        outputFormat: 'DD Mon YYYY',
      }),
      active: true,
    });
    console.log('Incrementing Total Active Case Flags');
    this._testData.caseFlags.activeCaseFlags++;
    console.log(`Total Number of Active Case Flags: ${this._testData.caseFlags.activeCaseFlags}`);
  }

  protected deactivateCaseFlag(index: number) {
    const caseFlagDetails = this.caseFlagsDetails[index];
    console.log(
      `Deactivating case flag with location: ${caseFlagDetails.caseFlagLocation} with type: ${caseFlagDetails.caseFlagType}`,
    );
    caseFlagDetails.active = false;
    console.log('Decrementing Total Active Case Flags');
    this._testData.caseFlags.activeCaseFlags--;
    console.log(`Total Number of Active Case Flags: ${this._testData.caseFlags.activeCaseFlags}`);
  }

  protected setClaimantDefendantPartyTypes() {
    this.setClaimant1PartyType = claimantDefendantPartyTypes[this.ccdCaseData?.applicant1?.type];
    this.setClaimant2PartyType = claimantDefendantPartyTypes[this.ccdCaseData?.applicant2?.type];
    this.setDefendant1PartyType = claimantDefendantPartyTypes[this.ccdCaseData?.respondent1?.type];
    this.setDefendant2PartyType = claimantDefendantPartyTypes[this.ccdCaseData?.respondent2?.type];
  }

  protected setCaseFlags() {
    const caseFlags: CaseFlags = this._testData.caseFlags;
    CaseFlagsHelper.updateCaseFlagsObject(
      caseFlags,
      CaseFlagsHelper.getCaseLevelFlags(this.ccdCaseData),
    );
    CaseFlagsHelper.updateCaseFlagsObject(
      caseFlags,
      CaseFlagsHelper.getCaseFlagsForClaimantDefendant(this.ccdCaseData?.applicant1),
    );
    CaseFlagsHelper.updateCaseFlagsObject(
      caseFlags,
      CaseFlagsHelper.getCaseFlagsForLitigationFriend(this.ccdCaseData?.applicant1LitigationFriend),
    );
    CaseFlagsHelper.updateCaseFlagsObject(
      caseFlags,
      CaseFlagsHelper.getCaseFlagsForClaimantDefendant(this.ccdCaseData?.applicant2),
    );
    CaseFlagsHelper.updateCaseFlagsObject(
      caseFlags,
      CaseFlagsHelper.getCaseFlagsForLitigationFriend(this.ccdCaseData?.applicant2LitigationFriend),
    );
    CaseFlagsHelper.updateCaseFlagsObject(
      caseFlags,
      CaseFlagsHelper.getCaseFlagsForClaimantDefendant(this.ccdCaseData?.respondent1),
    );
    CaseFlagsHelper.updateCaseFlagsObject(
      caseFlags,
      CaseFlagsHelper.getCaseFlagsForLitigationFriend(
        this.ccdCaseData?.respondent1LitigationFriend,
      ),
    );
    CaseFlagsHelper.updateCaseFlagsObject(
      caseFlags,
      CaseFlagsHelper.getCaseFlagsForClaimantDefendant(this.ccdCaseData?.respondent2),
    );
    CaseFlagsHelper.updateCaseFlagsObject(
      caseFlags,
      CaseFlagsHelper.getCaseFlagsForLitigationFriend(
        this.ccdCaseData?.respondent2LitigationFriend,
      ),
    );
    CaseFlagsHelper.updateCaseFlagsObject(
      caseFlags,
      CaseFlagsHelper.getCaseFlagsForExpertAndWitness(this.ccdCaseData.applicantExperts),
    );
    CaseFlagsHelper.updateCaseFlagsObject(
      caseFlags,
      CaseFlagsHelper.getCaseFlagsForExpertAndWitness(this.ccdCaseData.applicantWitnesses),
    );
    CaseFlagsHelper.updateCaseFlagsObject(
      caseFlags,
      CaseFlagsHelper.getCaseFlagsForExpertAndWitness(this.ccdCaseData.respondent1Experts),
    );
    CaseFlagsHelper.updateCaseFlagsObject(
      caseFlags,
      CaseFlagsHelper.getCaseFlagsForExpertAndWitness(this.ccdCaseData.respondent1Witnesses),
    );
    CaseFlagsHelper.updateCaseFlagsObject(
      caseFlags,
      CaseFlagsHelper.getCaseFlagsForExpertAndWitness(this.ccdCaseData.respondent2Experts),
    );
    CaseFlagsHelper.updateCaseFlagsObject(
      caseFlags,
      CaseFlagsHelper.getCaseFlagsForExpertAndWitness(this.ccdCaseData.respondent2Witnesses),
    );
    console.log(`Total Number of Case Flags: ${caseFlags.caseFlagsDetails.length}`);
    console.log(`Total Number of Active Case Flags: ${caseFlags.activeCaseFlags}`);
  }

  protected setIsDebugTestDataSetup() {
    this._testData.isDebugTestDataSetup = true;
  }

  protected get isDebugTestDataSetup() {
    return this._testData.isDebugTestDataSetup;
  }
}
