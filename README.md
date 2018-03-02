ICP-Brasil Authenticator - Keycloak
===================================================

1. Keycloak must be configured to request the client certificate, to configure see the following item in Keycloak guide [Enable X.509 Cliente Certificate User Authentication](https://github.com/keycloak/keycloak-documentation/blob/master/server_admin/topics/authentication/x509.adoc#enable-x509-client-certificate-user-authentication)

2. Keycloak must be in execution

3. The project must be compiled e installed with the following deploy command

```bash
   $ ./mvnw clean install wildfly:deploy
```

4. Copy the "login-icpbrasil-info.ftl" file to the folder "themes/base/login" that's inside the Keycloak install directory

5. Login in the administrative console.

6. Go to the "Authentication" page, in the "Flows" tab you will see the current authentication flows. It's not possible to alter the defaults, so you have to create or to copy one. Copy the "Browser" flow.

7. In your copy, click "Add Execution".  Select "ICPBrasil/Validate Username Form" and click "Save"

8. Move the item "ICPBrasil/Validate Username Form" so that it is before "Browser Forms". Enable it by selecting "ALTERNATIVE" in the "Requirement" column. Configure it by going to the "Actions" column and clicking "Config".

9. In the configuration, in the item "User Identity Source", select one of the options related to ICPBrasil (Subject's CPF, Subject's CNPJ, Subject's CPF or CNPJ). Under "User mapping method" select "Username or Email". In the "A name of user attribute" fill in with "uid".
