import { APIResponse } from '@playwright/test';

export interface _ResponseOptions {
  expectedStatus?: number;
  verifyResponse?: (
    response: APIResponse | any | string,
    responseData?: {
      headers?: {
        [key: string]: string;
      };
      status?: number;
    },
  ) => Promise<void>;
}

export interface ResponseOptions extends _ResponseOptions {
  verifyResponse?: (
    response: APIResponse,
    responseData?: {
      headers?: {
        [key: string]: string;
      };
      status?: number;
    },
  ) => Promise<void>;
}

export interface ResponseJsonOptions extends _ResponseOptions {
  verifyResponse?: (
    responseJson: any,
    responseData?: {
      headers?: {
        [key: string]: string;
      };
      status?: number;
    },
  ) => Promise<void>;
}

export interface ResponseTextOptions extends _ResponseOptions {
  verifyResponse?: (
    responseText: string,
    responseData?: {
      headers?: {
        [key: string]: string;
      };
      status?: number;
    },
  ) => Promise<void>;
}

export interface _RetryResponseOptions {
  expectedStatus?: number;
  retries?: number;
  retryTimeInterval?: number;
  verifyResponse?: (
    response: APIResponse | any | string,
    responseData?: {
      headers?: {
        [key: string]: string;
      };
      status?: number;
    },
  ) => Promise<void>;
}

export interface RetryResponseOptions extends _RetryResponseOptions {
  verifyResponse?: (
    response: APIResponse,
    responseData?: {
      headers?: {
        [key: string]: string;
      };
      status?: number;
    },
  ) => Promise<void>;
}

export interface RetryResponseJsonOptions extends _RetryResponseOptions {
  verifyResponse?: (
    responseJson: any,
    responseData?: {
      headers?: {
        [key: string]: string;
      };
      status?: number;
    },
  ) => Promise<void>;
}

export interface RetryResponseTextOptions extends _RetryResponseOptions {
  verifyResponse?: (
    responseText: string,
    responseData?: {
      headers?: {
        [key: string]: string;
      };
      status?: number;
    },
  ) => Promise<void>;
}
