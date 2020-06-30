module.exports = {
  "plugins": [
    "codeceptjs"
  ],
  "env": {
    "browser": true,
    "commonjs": true,
    "es2020": true,
    "codeceptjs/codeceptjs": true
  },
  "extends": "eslint:recommended",
  "parserOptions": {
    "ecmaVersion": 11
  },
  "rules": {
    "quotes": [
      "error",
      "single"
    ],
    "semi": [
      "error",
      "always"
    ]
  }
};
