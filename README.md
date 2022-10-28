ICP-Brasil Authenticator - Keycloak v19.0.3
===================================================

Versão com suporte ao keycloak 19.0.3, está sendo disponibilizado para coletar feedback, uma vez que 
eu não possuo um certificado ICP para testes.

[[ATENÇÃO]]
[[ATENÇÃO]]
[[ATENÇÃO]]

ESTE SOFTWARE VEM SEM NENHUMA GARANTIA DE FUNCIONAMENTO OU DE SUPORTE. Caso encontre algum problema com ele, eu posso 
tentar ajudar, no meu próprio tempo e no meu próprio limite, caso você abra uma issue, mas ISSO NÃO É GARANTIA 
NENHUMA DE QUE VOCÊ VAI SER RESPONDIDO.

[[ATENÇÃO]]
[[ATENÇÃO]]
[[ATENÇÃO]]

1. Necessário configurar o keycloak para autenticação mutual-tls (mTLS). Isso vai depender do proxy reverso que estiver
na frente do Keycloak ou do próprio keycloak.
2. Necessário incluir um truststore com as cadeias do ICP Brasil e configurar o keycloak para usá-las.
3. Necessário configurar o autenticador conforme item 6 deste documento.
4. Adicionar o arquivo jar na pasta /provided do Keycloak 19.0.3
5. Extrair os arquivos na pasta `lib/lib/main/org.keycloak.keycloak-themes-19.0.3.jar` dentro da pasta `/themes` do keycloak 19.0.3
A pasta deve ficar no format `<caminho_keycloak>/themes/base`
6. Copiar o arquivo `login-icpbrasil-info.ftl` deste repositório para a pasta `<caminho_keycloak>/themes/base/login`


## Como configurar o autenticador:
6. Login in the administrative console.

6. Go to the "Authentication" page, in the "Flows" tab you will see the current authentication flows. It's not possible to alter the defaults, so you have to create or to copy one. Copy the "Browser" flow.

7. In your copy, click "Add Execution".  Select "ICPBrasil/Validate Username Form" and click "Save"

8. Move the item "ICPBrasil/Validate Username Form" so that it is before "Browser Forms". Enable it by selecting "ALTERNATIVE" in the "Requirement" column. Configure it by going to the "Actions" column and clicking "Config".

9. In the configuration, in the item "User Identity Source", select one of the options related to ICPBrasil (Subject's CPF, Subject's CNPJ, Subject's CPF or CNPJ). Under "User mapping method" select "Username or Email". In the "A name of user attribute" fill in with "uid".
