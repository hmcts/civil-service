module.exports = {
  sendMessage: () => {
    return {
      valid: {
        sendAndReplyOption: {
          sendAndReplyOption: 'SEND'
        },
        sendMessageMetadata: {
          sendMessageMetadata: {
            recipientRoleType: 'DISTRICT_JUDGE',
            isUrgent: 'No',
            subjectType: 'APPLICATION',
            subject: 'Test'
          }
        },
        sendMessageContent: {
          sendMessageContent: 'Test Message'
        }
    }
    };
  },
  replyMessage: (messageCode, messageLabel) => {
    return {
      valid: {
        sendAndReplyOption: {
          sendAndReplyOption: 'REPLY',
          messagesToReplyTo: {
            value: {
              code: messageCode,
              label: messageLabel
            },
            list_items: [
              {
                code: messageCode,
                label: messageLabel
              }
            ]
          }
        },
        replyToMessage: {
          messageReplyMetadata: {
            recipientRoleType: 'ADMIN',
            isUrgent: 'No',
            messageContent: 'Test reply',
            senderRoleType: null,
            sentTime: null,
            senderName: null,
            subjectType: null,
            subject: null
          },
          messageHistory: 'Test reply'
        },
      }
    };
  },
  sendMessageLr: () => {
    return {
      userInput: {
        sendAndReplyOption: {
          sendAndReplyOption: 'SEND'
        },
        sendMessageMetadata: {
          sendMessageMetadata: {
            recipientRoleType: 'DISTRICT_JUDGE',
            isUrgent: 'No',
            subjectType: 'APPLICATION',
            subject: 'Test'
          }
        },
        sendMessageContent: {
          sendMessageContent: 'Test Message'
        }
      }
    };
  },
  replyMessageLr: (messageCode, messageLabel) => {
    return {
      userInput: {
        sendAndReplyOption: {
          sendAndReplyOption: 'REPLY',
          messagesToReplyTo: {
            value: {
              code: messageCode,
              label: messageLabel
            },
            list_items: [
              {
                code: messageCode,
                label: messageLabel
              }
            ]
          }
        },
        replyToMessage: {
          messageReplyMetadata: {
            recipientRoleType: 'ADMIN',
            isUrgent: 'No',
            messageContent: 'Test reply',
            senderRoleType: null,
            sentTime: null,
            senderName: null,
            subjectType: null,
            subject: null
          },
          messageHistory: 'Test reply'
        },
      }
    };
  }
};
