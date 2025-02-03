/**
 * Add scenario
 */
INSERT INTO dbs.scenario (name, notifications_to_delete, notifications_to_create)
VALUES ('Scenario.AAA6.ClaimIssue.Response.Await',
        '{"Notice.AAA6.ClaimIssue.ClaimFee.Required", "Notice.AAA6.ClaimIssue.HWF.Requested", "Notice.AAA6.ClaimIssue.HWF.InvalidRef", "Notice.AAA6.ClaimIssue.HWF.InfoRequired", "Notice.AAA6.ClaimIssue.HWF.Updated", "Notice.AAA6.ClaimIssue.HWF.PartRemission", "Notice.AAA6.ClaimIssue.HWF.Rejected", "Notice.AAA6.ClaimIssue.HWF.PhonePayment", "Notice.AAA6.ClaimIssue.HWF.FullRemission"}',
        '{"Notice.AAA6.ClaimIssue.Response.Await":["respondent1ResponseDeadlineEn", "respondent1ResponseDeadlineCy", "respondent1PartyName"]}');

/**
 * Add notification template
 */
INSERT INTO dbs.dashboard_notifications_templates (template_name, title_En, title_Cy, description_En, description_Cy
                                                  ,notification_role)
VALUES ('Notice.AAA6.ClaimIssue.Response.Await', 'Wait for defendant to respond', 'Aros i''r diffynnydd ymateb',
        '<p class="govuk-body">${respondent1PartyName} has until ${respondent1ResponseDeadlineEn} to respond. They can request an extra 28 days if they need it.</p>',
        '<p class="govuk-body">Mae gan ${respondent1PartyName} hyd at ${respondent1ResponseDeadlineCy} i ymateb. Gallant ofyn am 28 diwrnod ychwanegol os oes arnynt angen hynny.</p>',
        'CLAIMANT');

/**
 * Add task list items
 */
INSERT INTO dbs.task_item_template (task_name_en, category_en, task_name_cy, category_cy, template_name,
                                    scenario_name, task_status_sequence, role, task_order)
values ('<a href={VIEW_CLAIM_URL} rel="noopener noreferrer" class="govuk-link">View the claim</a>', 'The claim',
        '<a href={VIEW_CLAIM_URL} rel="noopener noreferrer" class="govuk-link">Gweld yr hawliad</a>',
        'Yr hawliad', 'Claim.View', 'Scenario.AAA6.ClaimIssue.Response.Await', '{3, 3}', 'CLAIMANT', 1),
       ('<a href={VIEW_INFO_ABOUT_CLAIMANT} rel="noopener noreferrer" class="govuk-link">View information about the claimant</a>', 'The claim',
        '<a href={VIEW_INFO_ABOUT_CLAIMANT} rel="noopener noreferrer" class="govuk-link">Gweld gwybodaeth am yr hawliad</a>',
        'Yr hawliad', 'Claim.Claimant.Info', 'Scenario.AAA6.ClaimIssue.Response.Await', '{3, 3}', 'CLAIMANT', 2),
       ('<a href={VIEW_INFO_ABOUT_DEFENDANT} rel="noopener noreferrer" class="govuk-link">View information about the defendant</a>', 'The response',
        '<a href={VIEW_INFO_ABOUT_DEFENDANT} rel="noopener noreferrer" class="govuk-link">Gweld gwybodaeth am y diffynnydd</a>',
        'Yr ymateb', 'Response.Defendant.Info', 'Scenario.AAA6.ClaimIssue.Response.Await', '{3, 3}', 'CLAIMANT', 4),
       ('<a href={VIEW_ORDERS_AND_NOTICES} rel="noopener noreferrer" class="govuk-link">View orders and notices</a>', 'Orders and notices from the court' ,
        '<a href={VIEW_ORDERS_AND_NOTICES} rel="noopener noreferrer" class="govuk-link">Gweld gorchmynion a rhybuddion</a>',
        'Gorchmynion a rhybuddion gan y llys', 'Order.View', 'Scenario.AAA6.ClaimIssue.Response.Await', '{3, 3}', 'CLAIMANT', 14),
       ('<a href={GENERAL_APPLICATIONS_INITIATION_PAGE_URL} rel="noopener noreferrer" class="govuk-link">Contact the court to request a change to my case</a>',
        'Applications', '<a href={GENERAL_APPLICATIONS_INITIATION_PAGE_URL} rel="noopener noreferrer" class="govuk-link">Contact the court to request a change to my case</a>',
         'Ceisiadau', 'Application.Create', 'Scenario.AAA6.ClaimIssue.Response.Await', '{4, 4}', 'CLAIMANT', 16);
