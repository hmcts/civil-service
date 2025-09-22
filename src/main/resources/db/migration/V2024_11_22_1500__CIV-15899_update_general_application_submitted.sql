/**
 * Removing full stop and new line from link
 */
UPDATE dbs.dashboard_notifications_templates SET description_En = replace(description_En, '<p class="govuk-body"> A judge will consider the application. </p>' ||
                                                                                          '<p class="govuk-body"> The other parties can respond within 5 working days after the application is submitted, unless you''ve chosen not to inform them. If you have a hearing in the next 10 days, your application will be treated urgently.</p>' ||
                                                                                          ' <p> <a href="{GA_VIEW_APPLICATION_URL}" class="govuk-link">View application documents</a>.</p>',
                                                                          '<p class="govuk-body"> A judge will consider the application. </p>' ||
                                                                          '<p class="govuk-body"> The other parties can respond within 5 working days after the application is submitted, unless you''ve chosen not to inform them. If you have a hearing in the next 10 days, your application will be treated urgently. ' ||
                                                                          '<a href="{GA_VIEW_APPLICATION_URL}" class="govuk-link">View application documents</a></p>')
                                             WHERE template_name in ('Notice.AAA6.GeneralApps.ApplicationSubmitted.Applicant');
UPDATE dbs.dashboard_notifications_templates SET description_Cy = replace(description_Cy, '<p class="govuk-body"> Bydd barnwr yn ystyried y cais. </p>' ||
                                                                                          '<p class="govuk-body"> Gall y partïon eraill ymateb o fewn 5 diwrnod gwaith ar ôl i’r cais gael ei gyflwyno, oni bai eich bod wedi dewis peidio â rhoi gwybod iddynt. Os oes gennych wrandawiad o fewn y 10 diwrnod nesaf, bydd eich cais yn cael ei drin ar frys. </p>' ||
                                                                                          ' <p> <a href="{GA_VIEW_APPLICATION_URL}" class="govuk-link">Gweld dogfennau’r cais</a>.</p>',
                                                                          '<p class="govuk-body"> Bydd barnwr yn ystyried y cais. </p>' ||
                                                                          '<p class="govuk-body"> Gall y partïon eraill ymateb o fewn 5 diwrnod gwaith ar ôl i’r cais gael ei gyflwyno, oni bai eich bod wedi dewis peidio â rhoi gwybod iddynt. Os oes gennych wrandawiad o fewn y 10 diwrnod nesaf, bydd eich cais yn cael ei drin ar frys. ' ||
                                                                          '<a href="{GA_VIEW_APPLICATION_URL}" class="govuk-link">Gweld dogfennau’r cais</a></p>')
                                             WHERE template_name in ('Notice.AAA6.GeneralApps.ApplicationSubmitted.Applicant');
