/**
 * Copyright 2015 DuraSpace, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.fcrepo.kernel.impl.rdf.impl;

import static com.google.common.base.Throwables.propagate;
import static com.hp.hpl.jena.graph.NodeFactory.createLiteral;
import static com.hp.hpl.jena.graph.Triple.create;
import static com.hp.hpl.jena.rdf.model.ResourceFactory.createTypedLiteral;
import static org.fcrepo.kernel.RdfLexicon.CREATED_DATE;
import static org.fcrepo.kernel.RdfLexicon.HAS_VERSION;
import static org.fcrepo.kernel.RdfLexicon.HAS_VERSION_LABEL;
import static org.fcrepo.kernel.impl.identifiers.NodeResourceConverter.nodeToResource;
import static org.slf4j.LoggerFactory.getLogger;

import java.util.Iterator;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.version.Version;
import javax.jcr.version.VersionHistory;
import com.hp.hpl.jena.rdf.model.Resource;

import org.fcrepo.kernel.models.FedoraResource;
import org.fcrepo.kernel.identifiers.IdentifierConverter;
import org.fcrepo.kernel.utils.iterators.RdfStream;

import com.google.common.base.Function;
import com.google.common.collect.Iterators;
import com.hp.hpl.jena.graph.Triple;

import org.slf4j.Logger;


/**
 * {@link RdfStream} that supplies {@link Triple}s concerning
 * the versions of a selected {@link Node}.
 *
 * @author ajs6f
 * @since Oct 15, 2013
 */
public class VersionsRdfContext extends RdfStream {

    private final VersionHistory versionHistory;

    private final IdentifierConverter<Resource, FedoraResource> idTranslator;

    private final com.hp.hpl.jena.graph.Node subject;

    private static final Logger LOGGER = getLogger(VersionsRdfContext.class);

    /**
     * Ordinary constructor.
     *
     * @param resource the resource
     * @param idTranslator the id translator
     * @throws RepositoryException if repository exception occurred
     */
    public VersionsRdfContext(final FedoraResource resource,
                              final IdentifierConverter<Resource, FedoraResource> idTranslator)
        throws RepositoryException {
        super();
        this.idTranslator = idTranslator;
        this.subject = idTranslator.reverse().convert(resource).asNode();
        versionHistory = resource.getVersionHistory();

        concat(versionTriples());
    }

    private Iterator<Triple> versionTriples() throws RepositoryException {
        final Iterator<Version> allVersions = versionHistory.getAllVersions();
        return Iterators.concat(Iterators.transform(allVersions, version2triples));
    }

    private final Function<Version, Iterator<Triple>> version2triples = new Version2Triples();

    private class Version2Triples implements Function<Version, Iterator<Triple>> {

        @Override
        public Iterator<Triple> apply(final Version version) {

            try {
                    /* Discard jcr:rootVersion */
                if (version.getName().equals(versionHistory.getRootVersion().getName())) {
                    LOGGER.trace("Skipped root version from triples");
                    return new RdfStream();
                }

                final Node frozenNode = version.getFrozenNode();
                final com.hp.hpl.jena.graph.Node versionSubject
                        = nodeToResource(idTranslator).convert(frozenNode).asNode();

                final RdfStream results = new RdfStream();

                results.concat(create(subject, HAS_VERSION.asNode(),
                        versionSubject));

                for (final String label : versionHistory
                        .getVersionLabels(version)) {
                    results.concat(create(versionSubject, HAS_VERSION_LABEL
                            .asNode(), createLiteral(label)));
                }
                results.concat(create(versionSubject, CREATED_DATE.asNode(),
                        createTypedLiteral(version.getCreated()).asNode()));

                return results;

            } catch (final RepositoryException e) {
                throw propagate(e);
            }
        }
    }


}
