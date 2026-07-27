const restHelper = require('../restHelper');
const {url} = require('../../config');

const wireMockUrl = `${url.wiremockService}/__admin/mappings`;
const headers = {
  'Content-Type': 'application/json'
};

const getStubs = async () => {
  return restHelper.request(
    wireMockUrl, null, null, 'GET', 200)
    .then(async response => {
      const content = await response.json();
      return content.mappings;
    });
};

const getStubUrl = ({request}) => request.url || request.urlPath || request.urlPathPattern;

const getStub = async (stubUrl) => {
  const allStubs = await getStubs();
  return allStubs.find(stub => getStubUrl(stub) == stubUrl);
};

const sameStub = (stub1, stub2) => {
  return stub1.request.method === stub2.request.method && getStubUrl(stub1) === getStubUrl(stub2);
};

const getStubByRequest = async (request) => {
  const allStubs = await getStubs();
  return allStubs.find(stub => sameStub(stub, {request}));
};

const getStubByRequestUrl = async (stubRequestUrl) => {
  const targetStub = await getStub(stubRequestUrl);
  if (targetStub == null) {
   console.log(`Could not locate stub for: ${stubRequestUrl} request url`);
  }
  return targetStub;
};

const updateStubById = async (stubId, mappingContent) => {
  return restHelper.request(
    `${wireMockUrl}/${stubId}`, headers, mappingContent, 'PUT', 200)
    .then(response => {
      response.json();
    });
};
const createStub = async (mappingContent) => {
  return restHelper.request(
    `${wireMockUrl}`, {}, mappingContent, 'POST');
};

const createUpdateStub = async (mappingContent) => {
    const existingStub = await getStubByRequest(mappingContent.request);
    return existingStub ?
      await updateStubById(existingStub.id, mappingContent)
      : await createStub(mappingContent);
};

const updateStubResponseFileByRequestUrl = async (stubRequestUrl, bodyFileName) => {
  return getStubByRequestUrl(stubRequestUrl)
    .then(stub => updateStubById(stub.id, {
        ...stub,
        response: {
          ...stub.response,
          bodyFileName
        }
      })
    );
};

const updateStubResponseByRequestUrl = async (stubRequestUrl, responseContent) => {
  return getStubByRequestUrl(stubRequestUrl)
    .then(stub => updateStubById(stub.id, {
        ...stub,
        response: {
          ...stub.response,
          bodyFileName: null,
          body: responseContent
        }
      })
    );
};

module.exports = {
  getStubs,
  createStub,
  createUpdateStub,
  getStubByRequestUrl,
  updateStubById,
  updateStubResponseFileByRequestUrl,
  updateStubResponseByRequestUrl
};
