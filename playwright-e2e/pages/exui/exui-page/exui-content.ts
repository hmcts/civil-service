import CCDCaseData from '../../../models/ccd/ccd-case-data';

export const headings = { caseNumber: { selector: 'ccd-markdown >> h1' } };

export const buttons = {
  submit: {
    title: 'Submit',
    selector: 'button[type=submit]',
  },
  addNew: { title: 'Add new', selector: "button[class='button write-collection-add-item__top']" },
};

export const components = {
  loading: {
    name: 'Loading',
    selector: '.spinner-container',
  },
  error: {
    selector: "div[aria-labelledby='edit-case-event_error-summary-heading']",
  },
  fieldError: {
    selector: "div[data-module='govuk-error-summary']",
  },
  uploadDocError: {
    selector: 'span.error-message',
  },
  eventTrigger: {
    selector: 'ccd-case-event-trigger',
  },
};

export const links = {
  cancel: {
    name: 'Cancel',
    selector: "a[href='javascript:void(0)']",
  },
};

export const getDQDocName = (ccdCaseData: CCDCaseData) =>
  `defendant_directions_questionnaire_form_${ccdCaseData.legacyCaseReference}.pdf`;

export const getResponseSealedFormDocName = (ccdCaseData: CCDCaseData) =>
  `${ccdCaseData.legacyCaseReference}_response_sealed_form.pdf`;

export const getFormattedCaseId = (caseId: number) => {
  const groups = caseId.toString().match(/.{1,4}/g);
  const formattedString = '#' + groups.join('-');
  return formattedString;
};

export const getUnformattedCaseId = (caseId: string) => {
  return +caseId.split(' ')[0].split('-').join('').substring(1);
};
