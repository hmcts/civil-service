import CaseFlags from '../ccd-events/case-flags/case-flag';
import CCDCaseData from '../ccd-case-data';
import { ClaimantDefendantPartyType } from '../users/claimant-defendant-party-types';

type TestData = {
  ccdCaseData: CCDCaseData;
  workerIndex: number;
  claimant1PartyType?: ClaimantDefendantPartyType;
  claimant2PartyType?: ClaimantDefendantPartyType;
  defendant1PartyType?: ClaimantDefendantPartyType;
  defendant2PartyType?: ClaimantDefendantPartyType;
  caseFlags: CaseFlags;
  isDebugTestDataSetup?: boolean;
};

export default TestData;
