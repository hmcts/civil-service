export class HttpError extends Error {
  constructor(method, url, status, body) {
    super(`${method} ${url} returned ${status}`);
    this.status = status;
    this.body = body;
  }
}

async function readBody(response) {
  const text = await response.text();
  if (!text) {
    return null;
  }
  try {
    return JSON.parse(text);
  } catch {
    return text;
  }
}

/**
 * Sends a JSON request and returns the parsed body. Throws HttpError for any status not in expectStatus.
 */
export async function request(method, url, { headers = {}, body, expectStatus = [200, 201] } = {}) {
  const response = await fetch(url, {
    method,
    headers: body === undefined ? headers : { 'Content-Type': 'application/json', ...headers },
    body: body === undefined ? undefined : (typeof body === 'string' ? body : JSON.stringify(body))
  });
  const parsed = await readBody(response);
  if (!expectStatus.includes(response.status)) {
    throw new HttpError(method, url, response.status, parsed);
  }
  return parsed;
}

export function sleep(ms) {
  return new Promise(resolve => setTimeout(resolve, ms));
}

/**
 * Calls fn until it returns without throwing, up to attempts times. Returns its result or rethrows the last error.
 * An error with fatal set, such as a Camunda incident, is rethrown at once.
 */
export async function retry(fn, { attempts, intervalMs }) {
  let lastError;
  for (let attempt = 1; attempt <= attempts; attempt += 1) {
    try {
      return await fn(attempt);
    } catch (error) {
      lastError = error;
      if (error.fatal) {
        throw error;
      }
      if (attempt < attempts) {
        await sleep(intervalMs);
      }
    }
  }
  throw lastError;
}

export function describeError(error) {
  const details = { message: error.message };
  if (error instanceof HttpError) {
    details.status = error.status;
    details.body = error.body;
  }
  return details;
}
