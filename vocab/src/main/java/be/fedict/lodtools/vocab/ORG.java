/*
 * Copyright (c) 2016, Bart Hanssens <bart.hanssens@fedict.be>
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * * Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 * * Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
package be.fedict.lodtools.vocab;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Namespace;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleNamespace;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;

/**
 * Vocabulary constants for the W3C Organization Ontology.
 * 
 * @see <a href="https://www.w3.org/TR/vocab-org/">Organization Ontology</a>
 * @author Bart Hanssens
 */
public class ORG {
    /**
     * Organization elements namespace: http://www.w3.org/ns/org#
     */
    public static final String NAMESPACE = "http://www.w3.org/ns/org#";
    
    /**
     * Recommend prefix for the Organization namespace: "rov"
     */
    public static final String PREFIX = "org";

    /**
     * An immutable {@link Namespace} constant that represents the Organization namespace.
     */
    public static final Namespace NS = new SimpleNamespace(PREFIX, NAMESPACE);
    
    /* Class */
    public static final IRI ORGANIZATION;
    public static final IRI SITE;
    
    /* Properties */
    public static final IRI HAS_SITE;
    public static final IRI SITE_OF;
            
    static {
	ValueFactory factory = SimpleValueFactory.getInstance();
	
        ORGANIZATION = factory.createIRI(NAMESPACE, "Organization");
        SITE = factory.createIRI(NAMESPACE, "Site");
        
        HAS_SITE = factory.createIRI(NAMESPACE, "hasSite");
        SITE_OF = factory.createIRI(NAMESPACE, "siteOf");
    }
}