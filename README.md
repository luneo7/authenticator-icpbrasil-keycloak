ICP-Brasil Authenticator - Keycloak v19.0.3+
===================================================

Versão com suporte ao keycloak 19.0.3 (e talvez posteriores), está sendo disponibilizado para coletar feedback, uma vez que 
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
7. Faça login na console administrativa.
8. Vá para a página "Authentication", na aba "Flows" você verá os fluxos de autenticação atuais. Não é possível alterar os padrões, então você deve criar ou copiar um. Copie o fluxo "Browser".
9. Em sua cópia, clique em "Add Execution". Selecione "ICPBrasil/Validate Username Form" e clique em "Save".
10. Mova o item "ICPBrasil/Validate Username Form" para que fique antes de "Browser Forms". Ative-o selecionando "ALTERNATIVE" na coluna "Requirement". Configure-o ao ir para a coluna "Actions" e clicar em "Config".
11. Na configuração, no item "User Identity Source", selecione uma das opções relacionadas ao ICPBrasil (Subject's CPF, Subject's CNPJ, Subject's CPF or CNPJ). Sob "User mapping method" selecione "Username or Email". Em "A name of user attribute" preencha com "uid".

## Rodando via docker

Como demonstração, existe a imagem `quay.io/weltonrodrigo/authenticator-icpbrasil-keycloak:latest`,
analize o dockerfile neste repositório para entender como ela é montada.

```shell
docker run --name keycloak -ti -p 8080:8080 \
  quay.io/weltonrodrigo/authenticator-icpbrasil-keycloak:latest
```
