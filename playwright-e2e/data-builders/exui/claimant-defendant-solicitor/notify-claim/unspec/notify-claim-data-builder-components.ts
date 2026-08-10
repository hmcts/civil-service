import { claimantSolicitorUser } from '../../../../../config/users/exui-users';
import ClaimType from '../../../../../constants/cases/claim-type';
import partys from '../../../../../constants/users/partys';
import CaseDataHelper from '../../../../../helpers/case-data-helper';
import ClaimTypeHelper from '../../../../../helpers/claim-type-helper';
import DateHelper from '../../../../../helpers/date-helper';

const selectDefendantSolicitorToNotify = {
  SelectDefendantSolicitorToNotify: {
    defendantSolicitorNotifyClaimOptions: {
        list_items: [CaseDataHelper.setCodeToData('Both')],
        value: CaseDataHelper.setCodeToData('Both'),
      }
  },
};

const accessGrantedWarning = (claimType: ClaimType) => {
  if (ClaimTypeHelper.isDefendant1Represented(claimType) || ClaimTypeHelper.isDefendant2Represented(claimType)) {
    return {
      AccessGrantedWarning: {}
    };
  }
};

const certificateOfService1 = (claimType: ClaimType) => {
  if (ClaimTypeHelper.isDefendant1Unrepresented(claimType)) {
    const serviceDate = DateHelper.formatDateToString(
      DateHelper.subtractFromToday({ days: 1 }),
      { outputFormat: 'YYYY-MM-DD' },
    );

    return {
      CertificateOfService1: {
        cosNotifyClaimDefendant1: {
          cosDateOfServiceForDefendant: serviceDate,
          cosDateDeemedServedForDefendant: serviceDate,
          cosServedDocumentFiles: 'string',
          cosRecipient: 'string',
          cosRecipientServeType: 'HANDED',
          cosRecipientServeLocation: 'string',
          cosRecipientServeLocationOwnerType: 'SOLICITOR',
          cosRecipientServeLocationType: 'USUAL_RESIDENCE',
          cosSender: claimantSolicitorUser.name,
          cosSenderFirm: `Solicitor Firm - ${partys.CLAIMANT_1.key}`,
          cosUISenderStatementOfTruthLabel: [
            'CERTIFIED'
          ],
        }
      }
    }
  }

  return {};
}

const certificateOfService2 = (claimType: ClaimType) => {
  if (ClaimTypeHelper.isDefendant2Unrepresented(claimType)) {
    const serviceDate = DateHelper.formatDateToString(
      DateHelper.subtractFromToday({ days: 1 }),
      { outputFormat: 'YYYY-MM-DD' },
    );

    return {
      CertificateOfService2: {
        cosNotifyClaimDefendant2: {
          cosDateOfServiceForDefendant: serviceDate,
          cosDateDeemedServedForDefendant: serviceDate,
          cosServedDocumentFiles: 'string',
          cosRecipient: 'string',
          cosRecipientServeType: 'POSTED',
          cosRecipientServeLocation: 'string',
          cosRecipientServeLocationOwnerType: 'DEFENDANT',
          cosRecipientServeLocationType: 'LAST_KNOWN_PRINCIPAL_PLACE_BUSINESS',
          cosSender: claimantSolicitorUser.name,
          cosSenderFirm: `Solicitor Firm - ${partys.CLAIMANT_1.key}`,
          cosUISenderStatementOfTruthLabel: [
            'CERTIFIED'
          ],
        }
      }
    }
  }

  return {};
}

const notifyClaimDataBuilderComponents = {
  selectDefendantSolicitorToNotify,
  accessGrantedWarning,
  certificateOfService1,
  certificateOfService2
};

export default notifyClaimDataBuilderComponents;
