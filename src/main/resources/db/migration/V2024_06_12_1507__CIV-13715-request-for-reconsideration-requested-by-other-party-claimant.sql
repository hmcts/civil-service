/**
 * Add scenario
 * Notifies the Claimant that the Defendant has requested a reconsideration of the SDO
 */
INSERT INTO dbs.scenario (name, notifications_to_delete, notifications_to_create)
VALUES ('Scenario.AAA6.CP.ReviewOrderRequestedbyOtherParty.Claimant',
        '{Notice.AAA6.CP.SDOMadebyLA.Claimant, Notice.AAA6.CP.SDOMadebyLA.Defendant}', '{"Notice.AAA6.CP.ReviewOrderRequestedbyOtherParty.Claimant" : ["requestForReconsiderationDeadlineEn", "requestForReconsiderationDeadlineCy"]}');

/**
 * Add notification template
 */
INSERT INTO dbs.dashboard_notifications_templates (template_name, title_En, title_Cy, description_En, description_Cy
                                                  ,notification_role)
VALUES ('Notice.AAA6.CP.ReviewOrderRequestedbyOtherParty.Claimant', 'Review has been requested', 'Gofynnwyd am adolygiad',
        '<p class="govuk-body">A review of an order has been requested by the other parties. You can <a href="{VIEW_REQUEST_FOR_RECONSIDERATION_DOCUMENT}" rel="noopener noreferrer" target="_blank" class="govuk-link">view their request</a> and <a href="{REQUEST_FOR_RECONSIDERATION_COMMENTS}" rel="noopener noreferrer" class="govuk-link">add comments of your own</a> by ${requestForReconsiderationDeadlineEn}. A judge will review the request and your comments and you will be contacted if the judge makes a new order. Continue doing what the current order asks of you unless you''re informed a judge has made a new order.</p>',
        '<p class="govuk-body">Mae''r partïon eraill wedi gofyn am adolygiad o orchymyn. Gallwch <a href="{VIEW_REQUEST_FOR_RECONSIDERATION_DOCUMENT}" rel="noopener noreferrer" target="_blank" class="govuk-link">weld eu cais</a> ac <a href="{REQUEST_FOR_RECONSIDERATION_COMMENTS}" rel="noopener noreferrer" class="govuk-link">ychwanegu sylwadau eich hun</a> erbyn ${requestForReconsiderationDeadlineCy}. Bydd barnwr yn adolygu''r cais a''ch sylwadau a chysylltir â chi os bydd y barnwr yn gwneud gorchymyn newydd. Parhewch i wneud yr hyn y mae''r gorchymyn presennol yn ei ofyn oni bai eich bod yn cael gwybod bod barnwr wedi gwneud gorchymyn newydd.</p>',
        'CLAIMANT');
