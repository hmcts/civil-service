export const heading = (flagType: string) => `Update flag "${flagType}"`;

export const inputs = {
  flagComment: {
    label: (flagType: string) => `Update flag "${flagType}" comments`,
    selector: '#flagComment',
  },
};

export const buttons = {
  makeInactive: {
    label: 'Make inactive',
  },
};
