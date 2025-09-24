const hearingStubRequestBody = (hearing, hearingId) => ({
  request: {
    method: 'GET',
    url: `/hearing/${hearingId}`
  },
  response: {
    status: 200,
    body: `${JSON.stringify(hearing)}`,
    headers: {
      'Content-Type': 'application/json'
    }
  }
});

const unnotifiedHearingStubRequestBody = (hearingIds) => (
  {
    request: {
      method: 'GET',
      urlPathPattern: '/unNotifiedHearings/.*'
    },
    response: {
      status: 200,
      headers: {
        'Content-Type': 'application/json'
      },
      body: `${JSON.stringify(
        {
          totalFound: hearingIds.length,
          hearingIds: hearingIds
        }
      )}`,
    }
  }

);

const getpartiesNotifiedStubRequestBody = () => {
  return {
    request: {
      method: 'GET',
      urlPathPattern: '/partiesNotified/.*'
    },
    response: {
      status: 200,
      headers: {
        'Content-Type': 'application/json'
      },
      body: `${JSON.stringify({
        hearingID: '',
        responses: []
      })}`
    }
  };
};

const putPartiesNotifiedStubRequestBody = () => {
  return {
    'request': {
      'method': 'PUT',
      'urlPathPattern': '/partiesNotified/.*'
    },
    'response': {
      'status': 200,
      'headers': {
        'Content-Type': 'application/json'
      }
    }
  };
};

module.exports = {
  hearingStubRequestBody,
  unnotifiedHearingStubRequestBody,
  getpartiesNotifiedStubRequestBody,
  putPartiesNotifiedStubRequestBody
};
