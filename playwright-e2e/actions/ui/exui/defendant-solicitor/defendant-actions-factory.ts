import BasePageActionsFactory from '../../../../base/base-page-actions-factory';
import AcknowledgeClaimPageFactory from '../../../../pages/exui/claimant-defendant-solicitor/acknowledge-claim/acknowledge-claim-page-factory';
import AddUnavailableDatesPageFactory from '../../../../pages/exui/claimant-defendant-solicitor/add-additional-dates/add-additional-dates-page-factory';
import AddDefendantLitigationFriendPageFactory from '../../../../pages/exui/claimant-defendant-solicitor/add-defendant-litigation-friend/add-defendant-litigation-friend-page-factory';
import InformAgreedExtensionDatePageFactory from '../../../../pages/exui/claimant-defendant-solicitor/inform-agreed-extension-date/inform-agreed-extension-date-page-factory';
import DefendantResponsePageFactory from '../../../../pages/exui/claimant-defendant-solicitor/response/defendant-response/defendant-response-page-factory';
import AcknowledgeClaimActions from './acknowledge-claim-actions';
import AddUnavailableDatesActions from './add-unavailable-dates-actions';
import AddDefendantLitigationFriendActions from './add-defendant-litigation-friend-actions';
import DefendantResponseActions from './defendant-response/defendant-response-actions';
import DefendantResponseSpecActions from './defendant-response/defendant-response-spec-actions';
import InformAgreedExtensionDateActions from './inform-agreed-extension-date/inform-agreed-extension-date-actions';
import InformAgreedExtensionDateSpecActions from './inform-agreed-extension-date/inform-agreed-extension-date-spec-actions';
import EvidenceUploadRespondentActions from './evidence-upload/evidence-upload-respondent-actions.ts';
import EvidenceUploadPageFactory from '../../../../pages/exui/claimant-defendant-solicitor/evidence-upload/evidence-upload-page-factory.ts';

export default class DefendantActionsFactory extends BasePageActionsFactory {
  get defendantResponseActions() {
    return new DefendantResponseActions(new DefendantResponsePageFactory(this.page), this.testData);
  }

  get defendantResponseSpecActions() {
    return new DefendantResponseSpecActions(
      new DefendantResponsePageFactory(this.page),
      this.testData,
    );
  }

  get addDefendantLitigationFriendActions() {
    return new AddDefendantLitigationFriendActions(
      new AddDefendantLitigationFriendPageFactory(this.page),
      this.testData,
    );
  }

  get addUnavailableDatesActions() {
    return new AddUnavailableDatesActions(
      new AddUnavailableDatesPageFactory(this.page),
      this.testData,
    );
  }

  get acknowlegdeClaimActions() {
    return new AcknowledgeClaimActions(new AcknowledgeClaimPageFactory(this.page), this.testData);
  }

  get informAgreedExtensionDateActions() {
    return new InformAgreedExtensionDateActions(
      new InformAgreedExtensionDatePageFactory(this.page),
      this.testData,
    );
  }

  get informAgreedExtensionDateSpecActions() {
    return new InformAgreedExtensionDateSpecActions(
      new InformAgreedExtensionDatePageFactory(this.page),
      this.testData,
    );
  }

  get evidenceUploadRespondentActions() {
    return new EvidenceUploadRespondentActions(
      new EvidenceUploadPageFactory(this.page),
      this.testData,
    );
  }
}
