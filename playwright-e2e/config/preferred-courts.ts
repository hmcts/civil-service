import partys from '../constants/partys';

const preferredCourts = {
  [partys.CLAIMANT_1.key]: {
    default:
      'Central London County Court - Thomas More Building, Royal Courts of Justice, Strand, London - WC2A 2LL',
    dj: 'Central London County Court - Thomas More Building, Royal Courts of Justice, Strand, London - WC2A 2LL',
    hmc: 'Nottingham County Court And Family Court - Canal Street - NG1 7EJ',
  },
  [partys.DEFENDANT_1.key]: {
    default:
      'Central London County Court - Thomas More Building, Royal Courts of Justice, Strand, London - WC2A 2LL',
    hmc: 'Nottingham County Court And Family Court - Canal Street - NG1 7EJ',
  },
  [partys.DEFENDANT_2.key]: {
    default:
      'Central London County Court - Thomas More Building, Royal Courts of Justice, Strand, London - WC2A 2LL',
  },
};

export default preferredCourts;
