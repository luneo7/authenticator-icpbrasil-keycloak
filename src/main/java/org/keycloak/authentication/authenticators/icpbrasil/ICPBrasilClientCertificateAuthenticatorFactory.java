/*
 * Copyright 2016 Analytical Graphics, Inc. and/or its affiliates
 * and other contributors as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.keycloak.authentication.authenticators.icpbrasil;

import org.keycloak.authentication.Authenticator;
import org.keycloak.models.AuthenticationExecutionModel;
import org.keycloak.models.KeycloakSession;

/**
 * @author <a href="mailto:pnalyvayko@agi.com">Peter Nalyvayko</a>
 * @author <a href="mailto:luneo7@gmail.com">Lucas Rogerio Caetano Ferreira</a>
 * @version $Revision: 1 $
 * @date 8/9/2017
 */
public class ICPBrasilClientCertificateAuthenticatorFactory extends AbstractICPBrasilClientCertificateAuthenticatorFactory {

    public static final String PROVIDER_ID = "auth-icpbrasil-client-username-form";
    public static final ICPBrasilClientCertificateAuthenticator SINGLETON =
            new ICPBrasilClientCertificateAuthenticator();

    public static final AuthenticationExecutionModel.Requirement[] REQUIREMENT_CHOICES = {
            AuthenticationExecutionModel.Requirement.ALTERNATIVE,
            AuthenticationExecutionModel.Requirement.DISABLED
    };


    @Override
    public String getHelpText() {
        return "Validates username and password from X509 client certificate received as a part of mutual SSL handshake.";
    }

    @Override
    public String getDisplayType() {
        return "ICPBrasil/Validate Username Form";
    }

    @Override
    public AuthenticationExecutionModel.Requirement[] getRequirementChoices() {
        return REQUIREMENT_CHOICES;
    }


    @Override
    public Authenticator create(KeycloakSession session) {
        return SINGLETON;
    }

    @Override
    public String getId() {
        return PROVIDER_ID;
    }
}
