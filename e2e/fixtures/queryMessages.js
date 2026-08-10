const uuid = require('uuid');
const {element} = require('../api/dataHelper');
const {uploadDocument} = require('../api/testingSupport');

const initialQueryMessage = async (userName, userId, isHearingRelated) => element({
        id: uuid.v1(),
        body: `This query was raised by ${userName}.`,
        name: userName,
        subject: `${userName} Query`,
        createdBy: userId,
        createdOn: new Date().toISOString(),
        attachments: [element({...(await uploadDocument()), filename: 'query-attachment.pdf'})],
        isHearingRelated: isHearingRelated ? 'Yes' : 'No',
        ...(isHearingRelated ? {  hearingDate: '2026-01-01' } : {})
    }
);

const queryResponseMessage = async ({id, subject, isHearingRelated, hearingDate}, userId) => element({
    id: uuid.v1(),
    body: 'Caseworker response to query.',
    name: 'Caseworker',
    subject,
    parentId: id,
    createdBy: userId,
    createdOn: new Date().toISOString(),
    attachments: [element({...(await uploadDocument()), filename: 'response-attachment.pdf'})],
    hearingDate,
    isHearingRelated,
});

const followUpQueryMessage = async ({id, subject, isHearingRelated, hearingDate, name}, userId) => element({
    name,
    subject,
    id: uuid.v1(),
    body: `${name}'s follow up to caseworker response.`,
    parentId: id,
    createdBy: userId,
    createdOn: new Date().toISOString(),
    attachments: [element({...(await uploadDocument()), filename: 'follow-up-attachment.pdf'})],
    hearingDate,
    isHearingRelated,
});

module.exports = {initialQueryMessage, queryResponseMessage, followUpQueryMessage};
