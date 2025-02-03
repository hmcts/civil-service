/**
 * Add scenario
 */
INSERT INTO dbs.scenario (name, notifications_to_delete, notifications_to_create)
/**
  Claimant has commented the request for SDO reconsideration that Defendant created in the first place
  Create a notification for the Defendant to know
 */
VALUES ('Scenario.AAA6.CP.ReviewOrderRequestedbyRecipient.Defendant',
        '{Notice.AAA6.CP.ReviewOrderRequestedbyOtherParty.Claimant}', '{"Notice.AAA6.CP.ReviewOrderRequestedbyRecipient.Recipient.Defendant": []}'),
/**
  Defendant has commented the request for SDO reconsideration that Claimant created in the first place
  Create a notification for the Claimant to know
 */
 ('Scenario.AAA6.CP.ReviewOrderRequestedbyRecipient.Claimant',
        '{Notice.AAA6.CP.ReviewOrderRequestedbyOtherParty.Defendant}', '{"Notice.AAA6.CP.ReviewOrderRequestedbyRecipient.Recipient.Claimant": []}');

/**
 * Add notification template
 */
INSERT INTO dbs.dashboard_notifications_templates (template_name, title_En, title_Cy, description_En, description_Cy
                                                  ,notification_role)
VALUES ('Notice.AAA6.CP.ReviewOrderRequestedbyRecipient.Recipient.Defendant',
        'Comment made on your request',
        'Sylwadau wedi''u gwnaed ar eich cais',
        '<p class="govuk-body">The other parties have made a <u>comment on your request to review an order</u>.</p>' ||
        '<p class="govuk-body"><b>Review has been requested</b></p>' ||
        '<p class="govuk-body">A review of an order has been requested by the other parties. You can <a href="{VIEW_REQUEST_FOR_RECONSIDERATION_DOCUMENT}" rel="noopener noreferrer" target="_blank" class="govuk-link">view their request and/or comments</a>.</p>' ||
        '<p class="govuk-body">A judge will review the request and comments and you will be contacted if the judge makes a new order. Continue doing what the current order asks of you unless you''re informed a judge has made a new order.</p>',
        '<p class="govuk-body">Mae''r partïon eraill wedi gwneud <u>sylw ar eich cais i adolygu gorchymyn</u>.</p>' ||
        '<p class="govuk-body"><b>Gofynnwyd am adolygiad</b></p>' ||
        '<p class="govuk-body">Mae''r partïon eraill wedi gofyn am adolygiad o orchymyn. Gallwch <a href="{VIEW_REQUEST_FOR_RECONSIDERATION_DOCUMENT}" rel="noopener noreferrer" target="_blank" class="govuk-link">weld eu cais a/neu eu sylwadau</a>.</p>' ||
        '<p class="govuk-body">Bydd barnwr yn adolygu''r cais a''r sylwadau a chysylltir â chi os bydd y barnwr yn gwneud gorchymyn newydd. Parhewch i wneud yr hyn y mae''r gorchymyn presennol yn ei ofyn oni bai eich bod yn cael gwybod bod barnwr wedi gwneud gorchymyn newydd.</p>',
        'DEFENDANT'),
 ('Notice.AAA6.CP.ReviewOrderRequestedbyRecipient.Recipient.Claimant',
  'Comment made on your request',
  'Sylwadau wedi''u gwnaed ar eich cais',
  '<p class="govuk-body">The other parties have made a <u>comment on your request to review an order</u>.</p>' ||
  '<p class="govuk-body"><b>Review has been requested</b></p>' ||
  '<p class="govuk-body">A review of an order has been requested by the other parties. You can <a href="{VIEW_REQUEST_FOR_RECONSIDERATION_DOCUMENT}" rel="noopener noreferrer" target="_blank" class="govuk-link">view their request and/or comments</a>.</p>' ||
  '<p class="govuk-body">A judge will review the request and comments and you will be contacted if the judge makes a new order. Continue doing what the current order asks of you unless you''re informed a judge has made a new order.</p>',
  '<p class="govuk-body">Mae''r partïon eraill wedi gwneud <u>sylw ar eich cais i adolygu gorchymyn</u>.</p>' ||
  '<p class="govuk-body"><b>Gofynnwyd am adolygiad</b></p>' ||
  '<p class="govuk-body">Mae''r partïon eraill wedi gofyn am adolygiad o orchymyn. Gallwch <a href="{VIEW_REQUEST_FOR_RECONSIDERATION_DOCUMENT}" rel="noopener noreferrer" target="_blank" class="govuk-link">weld eu cais a/neu eu sylwadau</a>.</p>' ||
  '<p class="govuk-body">Bydd barnwr yn adolygu''r cais a''r sylwadau a chysylltir â chi os bydd y barnwr yn gwneud gorchymyn newydd. Parhewch i wneud yr hyn y mae''r gorchymyn presennol yn ei ofyn oni bai eich bod yn cael gwybod bod barnwr wedi gwneud gorchymyn newydd.</p>',
  'CLAIMANT');

