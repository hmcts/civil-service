import { CCDEvent } from '../../../../models/ccd/ccd-events';

export const tabs = {
  summary: {
    title: 'Summary',
    selector: "div[role='tab'] >> 'Summary'",
  },
  caseFile: {
    title: 'Case File',
    selector: "div[role='tab'] >> 'Case File'",
  },
  claimDetails: {
    title: 'Claim details',
    selector: "div[role='tab'] >> 'Claim details'",
  },
  history: {
    title: 'History',
    selector: "div[role='tab'] >> 'History'",
  },
  claimDocs: {
    title: 'Claim documents',
    selector: "div[role='tab'] >> 'Claim documents'",
  },
  listingNotes: {
    title: 'List notes',
    selector: "div[role='tab'] >> 'List notes'",
  },
  paymentHistory: {
    title: 'Payment History',
    selector: "div[role='tab'] >> 'Payment History'",
  },
  serviceRequest: {
    title: 'Service Request',
    selector: "div[role='tab'] >> 'Service Request'",
  },
  bundles: {
    title: 'Bundles',
    selector: "div[role='tab'] >> 'Bundles'",
  },
  caseFlags: {
    title: 'Case Flags',
    selector: "div[role='tab'] >> 'Case Flags'",
  },
};

export const dropdowns = {
  nextStep: {
    label: 'Next step',
    selector: '#next-step',
  },
};

export const buttons = {
  go: {
    title: 'go',
    selector: "button[type='submit']",
  },
};

export const containers = {
  eventHistory: {
    selector: '.EventLogTable',
  },
  errors: {
    selector: '#errors',
  },
};

export const successBannerText = (formattedCaseId: string, ccdEvent: CCDEvent) =>
  `Case ${formattedCaseId} has been updated with event: ${ccdEvent.name}`;

export const caseFlagsNoticeText = (activeCaseFlags: number) =>
  `There ${activeCaseFlags === 1 ? 'is' : 'are'} ${activeCaseFlags} active flag${activeCaseFlags === 1 ? '' : 's'} on this case.`;
