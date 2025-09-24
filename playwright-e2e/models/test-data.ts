import CaseFlags from './case-flag';
import CCDCaseData from './ccd/ccd-case-data';
import { ClaimantDefendantPartyType } from './claimant-defendant-party-types';

type TestData = {
  ccdCaseData?: CCDCaseData;
  workerIndex: number;
  claimant1PartyType?: ClaimantDefendantPartyType;
  claimant2PartyType?: ClaimantDefendantPartyType;
  defendant1PartyType?: ClaimantDefendantPartyType;
  defendant2PartyType?: ClaimantDefendantPartyType;
  caseFlags: CaseFlags;
  isDebugTestDataSetup?: boolean;
};

export default TestData;
