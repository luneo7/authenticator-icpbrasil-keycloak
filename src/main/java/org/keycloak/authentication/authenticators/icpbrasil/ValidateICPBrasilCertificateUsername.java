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

import java.security.cert.X509Certificate;

import javax.ws.rs.core.Response;

import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.authentication.AuthenticationFlowError;
import org.keycloak.authentication.authenticators.browser.AbstractUsernameFormAuthenticator;
import org.keycloak.events.Details;
import org.keycloak.events.Errors;
import org.keycloak.models.ModelDuplicateException;
import org.keycloak.models.UserModel;
import org.keycloak.services.ServicesLogger;

/**
 * @author <a href="mailto:pnalyvayko@agi.com">Peter Nalyvayko</a>
 * @author <a href="mailto:luneo7@gmail.com">Lucas Rogerio Caetano Ferreira</a>
 * @version $Revision: 1 $
 * @date 7/31/2016
 */

public class ValidateICPBrasilCertificateUsername extends AbstractICPBrasilClientCertificateDirectGrantAuthenticator {

    protected static ServicesLogger logger = ServicesLogger.LOGGER;

    @Override
    public void authenticate(AuthenticationFlowContext context) {

        X509Certificate[] certs = getCertificateChain(context);
        if (certs == null || certs.length == 0) {
            logger.debug("[ValidateICPBrasilCertificateUsername:authenticate] x509 client certificate is not available for mutual SSL.");
            context.getEvent().error(Errors.USER_NOT_FOUND);
            Response challengeResponse = errorResponse(Response.Status.UNAUTHORIZED.getStatusCode(), "invalid_request", "X509 client certificate is missing.");
            context.failure(AuthenticationFlowError.INVALID_USER, challengeResponse);
            return;
        }

        ICPBrasilAuthenticatorConfigModel config = null;
        if (context.getAuthenticatorConfig() != null && context.getAuthenticatorConfig().getConfig() != null) {
            config = new ICPBrasilAuthenticatorConfigModel(context.getAuthenticatorConfig());
        }
        if (config == null) {
            logger.warn("[ValidateICPBrasilCertificateUsername:authenticate] x509 Client Certificate Authentication configuration is not available.");
            context.getEvent().error(Errors.USER_NOT_FOUND);
            Response challengeResponse = errorResponse(Response.Status.UNAUTHORIZED.getStatusCode(), "invalid_request", "Configuration is missing.");
            context.failure(AuthenticationFlowError.INVALID_USER, challengeResponse);
            return;
        }
        // Validate X509 client certificate
        try {
            CertificateValidator.CertificateValidatorBuilder builder = certificateValidationParameters(config);
            CertificateValidator validator = builder.build(certs);
            validator.checkRevocationStatus()
                    .validateKeyUsage()
                    .validateExtendedKeyUsage();
        } catch(Exception e) {
            logger.error(e.getMessage(), e);
            // TODO use specific locale to load error messages
            Response challengeResponse = errorResponse(Response.Status.UNAUTHORIZED.getStatusCode(), "invalid_request", e.getMessage());
            context.failure(AuthenticationFlowError.INVALID_USER, challengeResponse);
            return;
        }

        Object userIdentity = getUserIdentityExtractor(config).extractUserIdentity(certs);
        if (userIdentity == null) {
            context.getEvent().error(Errors.INVALID_USER_CREDENTIALS);
            logger.errorf("[ValidateICPBrasilCertificateUsername:authenticate] Unable to extract user identity from certificate.");
            // TODO use specific locale to load error messages
            String errorMessage = "Unable to extract user identity from specified certificate";
            Response challengeResponse = errorResponse(Response.Status.UNAUTHORIZED.getStatusCode(), "invalid_request", errorMessage);
            context.failure(AuthenticationFlowError.INVALID_USER, challengeResponse);
            return;
        }
        UserModel user;
        try {
            context.getEvent().detail(Details.USERNAME, userIdentity.toString());
            context.getAuthenticationSession().setAuthNote(AbstractUsernameFormAuthenticator.ATTEMPTED_USERNAME, userIdentity.toString());
            user = getUserIdentityToModelMapper(config).find(context, userIdentity);
        }
        catch(ModelDuplicateException e) {
            logger.modelDuplicateException(e);
            String errorMessage = String.format("X509 certificate authentication's failed. Reason: \"%s\"", e.getMessage());
            Response challengeResponse = errorResponse(Response.Status.UNAUTHORIZED.getStatusCode(), "invalid_request", errorMessage);
            context.failure(AuthenticationFlowError.INVALID_USER, challengeResponse);
            return;
        }
        catch(Exception e) {
            logger.error(e.getMessage(), e);
            String errorMessage = String.format("X509 certificate authentication's failed. Reason: \"%s\"", e.getMessage());
            Response challengeResponse = errorResponse(Response.Status.UNAUTHORIZED.getStatusCode(), "invalid_request", errorMessage);
            context.failure(AuthenticationFlowError.INVALID_USER, challengeResponse);
            return;
        }
        if (user == null) {
            context.getEvent().error(Errors.INVALID_USER_CREDENTIALS);
            Response challengeResponse = errorResponse(Response.Status.UNAUTHORIZED.getStatusCode(), "invalid_grant", "Invalid user credentials");
            context.failure(AuthenticationFlowError.INVALID_USER, challengeResponse);
            return;
        }
        if (!user.isEnabled()) {
            context.getEvent().user(user);
            context.getEvent().error(Errors.USER_DISABLED);
            Response challengeResponse = errorResponse(Response.Status.BAD_REQUEST.getStatusCode(), "invalid_grant", "Account disabled");
            context.failure(AuthenticationFlowError.INVALID_USER, challengeResponse);
            return;
        }
        if (context.getRealm().isBruteForceProtected()) {
            if (context.getProtector().isTemporarilyDisabled(context.getSession(), context.getRealm(), user)) {
                context.getEvent().user(user);
                context.getEvent().error(Errors.USER_TEMPORARILY_DISABLED);
                Response challengeResponse = errorResponse(Response.Status.BAD_REQUEST.getStatusCode(), "invalid_grant", "Account temporarily disabled");
                context.failure(AuthenticationFlowError.INVALID_USER, challengeResponse);
                return;
            }
        }
        context.setUser(user);
        context.success();
    }

    @Override
    public void action(AuthenticationFlowContext context) {
        // Intentionally does nothing
    }
}
