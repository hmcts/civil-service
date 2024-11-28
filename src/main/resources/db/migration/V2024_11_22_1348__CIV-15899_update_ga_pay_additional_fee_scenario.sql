/**
 * Removing full stop and new line from link
 */
UPDATE dbs.dashboard_notifications_templates SET description_En = replace(description_En, '<p class="govuk-body">The court requires you to pay an additional fee before your application can progress further.</p><p class="govuk-body"><a href="{GA_VIEW_APPLICATION_URL}" class="govuk-link">Pay the additional application fee.</a></p>', '<p class="govuk-body">The court requires you to pay an additional fee before your application can progress further. <a href="{GA_VIEW_APPLICATION_URL}" class="govuk-link">Pay the additional application fee</a></p>')
                                             WHERE template_name in ('Notice.AAA6.GeneralApps.AdditionalApplicationFeeRequired.Applicant');
UPDATE dbs.dashboard_notifications_templates SET description_Cy = replace(description_Cy, '<p class="govuk-body">Mae''r llys angen i chi dalu ffi ychwanegol cyn y gall eich cais gael ei brosesu ymhellach.</p><p class="govuk-body"><a href="{GA_VIEW_APPLICATION_URL}" class="govuk-link">Talu ffi’r gwneud cais ychwanegol.</a></p>', '<p class="govuk-body">Mae''r llys angen i chi dalu ffi ychwanegol cyn y gall eich cais gael ei brosesu ymhellach. <a href="{GA_VIEW_APPLICATION_URL}" class="govuk-link">Talu ffi’r gwneud cais ychwanegol</a></p>')
                                             WHERE template_name in ('Notice.AAA6.GeneralApps.AdditionalApplicationFeeRequired.Applicant');
