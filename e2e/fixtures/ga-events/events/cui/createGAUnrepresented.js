const TypOfApplication = {
    multiple: {
        event: 'INITIATE_GENERAL_APPLICATION',
        caseDataUpdate: {
            generalAppType: {
                types: [
                    'EXTEND_TIME',
                    'AMEND_A_STMT_OF_CASE',
                ],
            },
            generalAppRespondentAgreement: {
                hasAgreed: 'Yes',
            },
            generalAppInformOtherParty: undefined,
            generalAppAskForCosts: 'Yes',
            generalAppDetailsOfOrder: 'I am asking for permission to make a change to test document.',
            generalAppReasonsOfOrder: 'test\n\ntest',
            generalAppEvidenceDocument: undefined,
            generalAppHearingDetails: {
                HearingPreferencesPreferredType: 'IN_PERSON',
                ReasonForPreferredHearingType: 'test',
                HearingPreferredLocation: {
                    value: {
                        label: 'Central London County Court - THOMAS MORE BUILDING, ROYAL COURTS OF JUSTICE, STRAND, LONDON - WC2A 2LL',
                    },
                },
                HearingDetailsTelephoneNumber: '07000000000',
                HearingDetailsEmailID: 'civilmoneyclaimsdemo@gmail.com',
                unavailableTrialRequiredYesOrNo: 'No',
                generalAppUnavailableDates: [
                ],
                SupportRequirement: [
                    'OTHER_SUPPORT',
                ],
                SupportRequirementSignLanguage: '',
                SupportRequirementLanguageInterpreter: '',
                SupportRequirementOther: 'wheel chair',
            },
            generalAppStatementOfTruth: {
                name: 'mutiple applications',
            },
            generalAppHelpWithFees: undefined,
        },
    }
};

module.exports = {
    getPayloadForGALiP: (type = '', hwf = false) => {
        if (TypOfApplication[type]) {
            return TypOfApplication[type];
        }
        return {
            event: 'INITIATE_GENERAL_APPLICATION',
            caseDataUpdate: {
                generalAppType: {
                    types: [
                        'EXTEND_TIME',
                    ],
                },
                generalAppRespondentAgreement: {
                    hasAgreed: 'No',
                },
                generalAppInformOtherParty: {
                  isWithNotice: 'Yes',
                  reasonsForWithoutNotice: 'Reason',
                },
                generalAppAskForCosts: 'No',
                generalAppDetailsOfOrder: 'The time by which I must [specify what needs to be done] be extended to [enter the date you can do this by].',
                generalAppReasonsOfOrder: 'test',
                generalAppEvidenceDocument: undefined,
                generalAppHearingDetails: {
                    HearingPreferencesPreferredType: 'IN_PERSON',
                    ReasonForPreferredHearingType: 'test',
                    HearingPreferredLocation: {
                        value: {
                            label: 'Central London County Court - THOMAS MORE BUILDING, ROYAL COURTS OF JUSTICE, STRAND, LONDON - WC2A 2LL',
                        },
                    },
                    HearingDetailsTelephoneNumber: '07000000000',
                    HearingDetailsEmailID: 'civilmoneyclaimsdemo@gmail.com',
                    unavailableTrialRequiredYesOrNo: 'No',
                    generalAppUnavailableDates: [
                    ],
                    SupportRequirement: [
                        'OTHER_SUPPORT',
                    ],
                    SupportRequirementSignLanguage: '',
                    SupportRequirementLanguageInterpreter: '',
                    SupportRequirementOther: 'test',
                },
                generalAppStatementOfTruth: {
                    name: 'test',
                },
                generalAppHelpWithFees: hwf ? {
                    helpWithFee:'Yes',
                    helpWithFeesReferenceNumber:'HWF-A1B-23C'
                } : null,
            },
        };
    },
    getPayloadForGALiPWithout: () => {
        return {
            event: 'INITIATE_GENERAL_APPLICATION',
            caseDataUpdate: {
                generalAppType: {
                    types: [
                        'EXTEND_TIME',
                    ],
                },
                generalAppRespondentAgreement: {
                    hasAgreed: 'No',
                },
                generalAppInformOtherParty: {
                    isWithNotice: 'No',
                    reasonsForWithoutNotice: 'reason'
                },
                generalAppAskForCosts: 'No',
                generalAppDetailsOfOrder: 'The time by which I must [specify what needs to be done] be extended to [enter the date you can do this by].',
                generalAppReasonsOfOrder: 'test',
                generalAppEvidenceDocument: undefined,
                generalAppHearingDetails: {
                    HearingPreferencesPreferredType: 'IN_PERSON',
                    ReasonForPreferredHearingType: 'test',
                    HearingPreferredLocation: {
                        value: {
                            label: 'Central London County Court - THOMAS MORE BUILDING, ROYAL COURTS OF JUSTICE, STRAND, LONDON - WC2A 2LL',
                        },
                    },
                    HearingDetailsTelephoneNumber: '07000000000',
                    HearingDetailsEmailID: 'civilmoneyclaimsdemo@gmail.com',
                    unavailableTrialRequiredYesOrNo: 'No',
                    generalAppUnavailableDates: [
                    ],
                    SupportRequirement: [
                        'OTHER_SUPPORT',
                    ],
                    SupportRequirementSignLanguage: '',
                    SupportRequirementLanguageInterpreter: '',
                    SupportRequirementOther: 'test',
                },
                generalAppStatementOfTruth: {
                    name: 'test',
                },
            },
        };
    }
};
