# IDAM service-token generation

Preview setup obtains an IDAM service token through `bin/shared/idam-lease-service-token.sh`. The script generates the TOTP inside the supported public `hmctspublic` registry image and then leases the token from service-auth-provider.

## Image ownership and updates

The selected image is `hmctspublic.azurecr.io/imported/toolbelt/oathtool`, pinned to manifest-list digest `sha256:ee73b804168ffaf4e00a1bf03240aa9f508ddabdd998587c0e114a336e2529ca`. The public registry does not require a Jenkins agent to authenticate to the private `hmctsprod` ACR. Platform owners maintain the imported image and must approve this source and digest before merge. An update must be reviewed, tested on the Jenkins preview agent class and made by changing the digest; callers must not use `latest` or embed registry credentials.

## Failure handling

The script stops before the lease request when the registry denies access, the digest is absent, the tool exits non-zero, or its output is empty/malformed. Messages never include the S2S secret, OTP or service token.

Run the safe negative and success checks with:

```sh
./bin/shared/test-idam-lease-service-token.sh
```

For preview troubleshooting, confirm the build runs on the standard Jenkins preview agent, Docker can pull the pinned `hmctspublic` digest, and service-auth-provider is reachable. Do not add `az acr login`, private registry credentials or a repository-specific mirror.
