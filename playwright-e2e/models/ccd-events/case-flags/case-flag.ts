export type CaseFlagDetails = {
  caseFlagLocation: string;
  caseFlagType: string;
  caseFlagComment: string;
  creationDate: string;
  active: boolean;
};

type CaseFlags = {
  caseFlagsDetails: CaseFlagDetails[];
  activeCaseFlags: number;
};

export default CaseFlags;
