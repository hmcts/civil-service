import PartyType from '../enums/party-types';

export type Party = {
  key: string;
  oldKey: string;
  number: number;
  partyType: PartyType;
  oldPartyType?: PartyType;
  shortOldKey?: string;
};

type Partys = {
  CLAIMANT_1: Party;
  CLAIMANT_2: Party;
  CLAIMANT_1_LITIGATION_FRIEND: Party;
  CLAIMANT_2_LITIGATION_FRIEND: Party;
  CLAIMANT_SOLICITOR_1: Party;
  CLAIMANT_WITNESS_1: Party;
  CLAIMANT_WITNESS_2: Party;
  CLAIMANT_EXPERT_1: Party;
  CLAIMANT_EXPERT_2: Party;
  CLAIMANT_1_MEDIATION_FRIEND: Party;
  DEFENDANT_1: Party;
  DEFENDANT_2: Party;
  DEFENDANT_1_LITIGATION_FRIEND: Party;
  DEFENDANT_2_LITIGATION_FRIEND: Party;
  DEFENDANT_SOLICITOR_1: Party;
  DEFENDANT_SOLICITOR_2: Party;
  DEFENDANT_1_WITNESS_1: Party;
  DEFENDANT_1_WITNESS_2: Party;
  DEFENDANT_2_WITNESS_1: Party;
  DEFENDANT_2_WITNESS_2: Party;
  DEFENDANT_1_EXPERT_1: Party;
  DEFENDANT_1_EXPERT_2: Party;
  DEFENDANT_2_EXPERT_1: Party;
  DEFENDANT_2_EXPERT_2: Party;
  DEFENDANT_1_MEDIATION_FRIEND: Party;
  DEFENDANT_2_MEDIATION_FRIEND: Party;
};

export default Partys;
