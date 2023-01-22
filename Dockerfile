FROM maven:3.8.6 as maven

# Obter os arquivos do tema do keycloak
WORKDIR /usr/src/theme_root/
RUN apt update && apt install unzip -y \
    && mvn dependency:get --no-transfer-progress -Dartifact=org.keycloak:keycloak-themes:19.0.3 -Ddest=/tmp \
    && unzip /tmp/*.jar

COPY login-icpbrasil-info.ftl theme/base/login

# build do pacote
WORKDIR /usr/src/app
COPY pom.xml .
RUN mvn -B -T1C --no-transfer-progress dependency:resolve

COPY . .
RUN mvn --batch-mode --no-transfer-progress package

FROM quay.io/keycloak/keycloak:19.0.3 AS keycloak

# Configurações do container keycloak
# ver https://www.keycloak.org/server/containers
ENV KC_HEALTH_ENABLED=true
ENV KC_METRICS_ENABLED=true
# Para usar postgres necessário ativar aqui
# ENV KC_DB=postgres
ENV KC_DB=dev-mem

WORKDIR /opt/keycloak

## inclui o arquivo de formulário no tema do keycloak
COPY --from=maven /usr/src/theme_root/theme/ ./themes/
COPY --from=maven /usr/src/app/target/authenticator-icpbrasil.jar ./providers/

RUN ./bin/kc.sh build

