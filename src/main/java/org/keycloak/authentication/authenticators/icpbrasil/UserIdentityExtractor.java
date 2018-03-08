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

import freemarker.template.utility.NullArgumentException;
import org.bouncycastle.asn1.*;
import org.bouncycastle.asn1.x500.RDN;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x500.style.IETFUtils;
import org.keycloak.services.ServicesLogger;

import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author <a href="mailto:pnalyvayko@agi.com">Peter Nalyvayko</a>
 * @author <a href="mailto:luneo7@gmail.com">Lucas Rogerio Caetano Ferreira</a>
 * @version $Revision: 1 $
 * @date 8/9/2017
 */

public abstract class UserIdentityExtractor {

    private static final ServicesLogger logger = ServicesLogger.LOGGER;

    public abstract Object extractUserIdentity(X509Certificate[] certs);

    static class OrExtractor extends UserIdentityExtractor {

        UserIdentityExtractor extractor;
        UserIdentityExtractor other;
        OrExtractor(UserIdentityExtractor extractor, UserIdentityExtractor other) {
            this.extractor = extractor;
            this.other = other;

            if (this.extractor == null)
                throw new NullArgumentException("extractor");
            if (this.other == null)
                throw new NullArgumentException("other");
        }

        @Override
        public Object extractUserIdentity(X509Certificate[] certs) {
            Object result = this.extractor.extractUserIdentity(certs);
            if (result == null)
                result = this.other.extractUserIdentity(certs);
            return result;
        }
    }

    static class ICPBrasilExtractor extends UserIdentityExtractor {

        protected static final String PESSOA_FISICA_OBJECTID = "2.16.76.1.3.1";

        protected static final String PESSOA_JURIDICA_OBJECTID = "2.16.76.1.3.3";


        Function<X509Certificate[],Collection<?>> x509SubjectAlternativeNames;
        ICPBrasilAuthenticatorConfigModel.MappingSourceType mappingSourceType;

        ICPBrasilExtractor(Function<X509Certificate[],Collection<?>> x509SubjectAlternativeNames, ICPBrasilAuthenticatorConfigModel.MappingSourceType mappingSourceType) {
            this.x509SubjectAlternativeNames = x509SubjectAlternativeNames;
            this.mappingSourceType = mappingSourceType;
        }

        @Override
        public Object extractUserIdentity(X509Certificate[] certs) {

            if (certs == null || certs.length == 0)
                throw new IllegalArgumentException();

            Collection<?> subjectAltNames = x509SubjectAlternativeNames.apply(certs);

            if (subjectAltNames != null) {

                for (final Object obj : subjectAltNames) {
                    if (obj instanceof ArrayList) {

                        final Object value = ((ArrayList) obj).get(1);

                        if (value instanceof ASN1Sequence) {

                            final String ICPBrasilString = getICPBrasilStringFromSequence((ASN1Sequence) value);

                            if (ICPBrasilString != null) {
                                return ICPBrasilString;
                            }

                        }

                    }
                }
            }
            return null;
        }

        private String getICPBrasilStringFromSequence(final ASN1Sequence seq) {
            if (seq != null) {
                // First in sequence is the object identifier, that we must check
                final ASN1ObjectIdentifier id = ASN1ObjectIdentifier.getInstance(seq.getObjectAt(0));
                if (id != null) {
                    final boolean isPessoaFisica =  PESSOA_FISICA_OBJECTID.equals(id.getId());
                    final boolean isPessoaJuridica =  PESSOA_JURIDICA_OBJECTID.equals(id.getId());

                    logger.debug("mappingSourceType: " + mappingSourceType + " -- isPessoaFisica: " + isPessoaFisica + " -- isPessoaJuridica: " + isPessoaJuridica);
                    if ((mappingSourceType.equals(ICPBrasilAuthenticatorConfigModel.MappingSourceType.SUBJECTCPFCNPJ) && (isPessoaFisica || isPessoaJuridica))
                         || (mappingSourceType.equals(ICPBrasilAuthenticatorConfigModel.MappingSourceType.SUBJECTCPF) && isPessoaFisica)
                         || (mappingSourceType.equals(ICPBrasilAuthenticatorConfigModel.MappingSourceType.SUBJECTCNPJ) && isPessoaJuridica)  ) {
                        final ASN1TaggedObject obj = (ASN1TaggedObject) seq.getObjectAt(1);
                        ASN1Primitive prim = obj.getObject();

                        // Due to bug in java cert.getSubjectAltName, it can be tagged an extra time
                        if (prim instanceof ASN1TaggedObject) {
                            prim = ASN1TaggedObject.getInstance(((ASN1TaggedObject) prim)).getObject();
                        }
                        String content = null;
                        if (prim instanceof ASN1OctetString) {
                            content =  new String(((ASN1OctetString) prim).getOctets());
                        } else if (prim instanceof ASN1String) {
                            content = ((ASN1String) prim).getString();
                        } else{
                            return null;
                        }

                        if (isPessoaFisica && content.length() >= 20) {
                            logger.debug("Returning CPF through Pessoa Fisica ObjectID content [" + content + "]");
                            return content.substring(8, 19);
                        }
                        else if (isPessoaJuridica) {
                            logger.debug("Returning CNPJ through Pessoa Juridica ObjectID content [" + content + "]");
                            return content;
                        }
                        return null;
                    }
                }
            }
            return null;
        }
    }

    static class X500NameRDNExtractor extends UserIdentityExtractor {

        private ASN1ObjectIdentifier x500NameStyle;
        Function<X509Certificate[],X500Name> x500Name;
        X500NameRDNExtractor(ASN1ObjectIdentifier x500NameStyle, Function<X509Certificate[],X500Name> x500Name) {
            this.x500NameStyle = x500NameStyle;
            this.x500Name = x500Name;
        }

        @Override
        public Object extractUserIdentity(X509Certificate[] certs) {

            if (certs == null || certs.length == 0)
                throw new IllegalArgumentException();

            X500Name name = x500Name.apply(certs);
            if (name != null) {
                RDN[] rnds = name.getRDNs(x500NameStyle);
                if (rnds != null && rnds.length > 0) {
                    RDN cn = rnds[0];
                    return IETFUtils.valueToString(cn.getFirst().getValue());
                }
            }
            return null;
        }
    }

    static class PatternMatcher extends UserIdentityExtractor {
        private final String _pattern;
        private final Function<X509Certificate[],String> _f;
        PatternMatcher(String pattern, Function<X509Certificate[],String> valueToMatch) {
            _pattern = pattern;
            _f = valueToMatch;
        }

        @Override
        public Object extractUserIdentity(X509Certificate[] certs) {
            String value = _f.apply(certs);

            Pattern r = Pattern.compile(_pattern, Pattern.CASE_INSENSITIVE);

            Matcher m = r.matcher(value);

            if (!m.find()) {
                logger.debugf("[PatternMatcher:extract] No matches were found for input \"%s\", pattern=\"%s\"", value, _pattern);
                return null;
            }

            if (m.groupCount() != 1) {
                logger.debugf("[PatternMatcher:extract] Match produced more than a single group for input \"%s\", pattern=\"%s\"", value, _pattern);
                return null;
            }

            return m.group(1);
        }
    }

    static class OrBuilder {
        UserIdentityExtractor extractor;
        UserIdentityExtractor other;
        OrBuilder(UserIdentityExtractor extractor) {
            this.extractor = extractor;
        }

        public UserIdentityExtractor or(UserIdentityExtractor other) {
            return new OrExtractor(extractor, other);
        }
    }

    public static UserIdentityExtractor getPatternIdentityExtractor(String pattern,
                                                                 Function<X509Certificate[],String> func) {
        return new PatternMatcher(pattern, func);
    }

    public static UserIdentityExtractor getX500NameExtractor(ASN1ObjectIdentifier identifier, Function<X509Certificate[],X500Name> x500Name) {
        return new X500NameRDNExtractor(identifier, x500Name);
    }

    public static UserIdentityExtractor getICPBrasilExtractor(Function<X509Certificate[],Collection<?>> x509SubjectAlternativeNames, ICPBrasilAuthenticatorConfigModel.MappingSourceType mappingSourceType) {
        return new ICPBrasilExtractor(x509SubjectAlternativeNames, mappingSourceType);
    }

    public static OrBuilder either(UserIdentityExtractor extractor) {
        return new OrBuilder(extractor);
    }
}
