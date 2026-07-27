type Cookie = {
  name: string;
  value: string;
  url?: string | undefined;
  domain?: string | undefined;
  path?: string | undefined;
  expires?: number | undefined;
  httpOnly?: boolean | undefined;
  secure?: boolean | undefined;
  sameSite?: 'Strict' | 'Lax' | 'None';
};

export default Cookie;
