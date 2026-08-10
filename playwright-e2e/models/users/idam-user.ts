type IdamUser = {
  id: string;
  forename: string;
  surname: string;
  email: string;
  active: boolean;
  locked: boolean;
  roles: string[];
  ssoProvider: string;
  lastModified: string;
};

export default IdamUser;
