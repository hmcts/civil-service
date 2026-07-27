type RequestOptions = {
  readonly headers?: Record<string, string>;
  readonly body?: Record<string, any>;
  readonly method?: 'GET' | 'POST' | 'PUT' | 'DELETE';
  readonly params?: { [key: string]: string | number | boolean };
};

export default RequestOptions;
