export const subheadings = { bulkScanned: 'Bulk scanned or emailed documents' };

export const inputs = {
  document: {
    label: 'Bulk scanned or emailed documents',
    name: {
      label: 'Document Name',
      selector: (documentNumber: number) => `#manageDocuments_${documentNumber}_documentName`,
    },
    other: {
      label: 'What type of document is it? (Optional)',
      selector: (documentNumber: number) => `#manageDocuments_${documentNumber}_documentTypeOther`,
    },
    uploadDoc: {
      label: 'Upload essential document',
      selector: (documentNumber: number) => `#manageDocuments_${documentNumber}_documentLink`,
    },
  },
};

export const dropdowns = {
  documentType: {
    label: 'Document Type',
    selector: (documentNumber: number) => `#manageDocuments_${documentNumber}_documentType`,
    options: [
      'N9a (Paper Admission - Full or Part)',
      'N9b (Paper defence/Counterclaim)',
      'N9 (Request more time)',
      'Other',
      'Mediation Agreement',
    ],
  },
};

export const buttons = {
  addNewTop: {
    label: 'Add new',
    selector: "button[class='button write-collection-add-item__top']",
  },
};
