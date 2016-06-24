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
 * Vocabulary constants for the W3C Registered Organization Vocabulary.
 * 
 * @see <a href="https://www.w3.org/TR/vocab-regorg/">Registered Organization Vocabulary</a>
 * @author Bart Hanssens
 */
public class ROV {
    /**
     * Registered Organization elements namespace: http://www.w3.org/ns/regorg#
     */
    public static final String NAMESPACE = "http://www.w3.org/ns/regorg#";
    
    /**
     * Recommend prefix for the Registered Organization namespace: "rov"
     */
    public static final String PREFIX = "rov";

    /**
     * An immutable {@link Namespace} constant that represents the Registered Organization namespace.
     */
    public static final Namespace NS = new SimpleNamespace(PREFIX, NAMESPACE);
    
    /* Class */
    public static final IRI REGISTERED_ORGANIZATION;
    
    /* Properties */
    public static final IRI HAS_REGISTERED_ORGANIZATION;
    public static final IRI LEGAL_NAME;
    public static final IRI ORG_ACTIVITY;
    public static final IRI ORG_STATUS;
    public static final IRI ORG_TYPE;
    public static final IRI REGISTRATION;
            
    static {
	ValueFactory factory = SimpleValueFactory.getInstance();
	
        REGISTERED_ORGANIZATION = factory.createIRI(NAMESPACE, "RegisteredOrganization");
        
        HAS_REGISTERED_ORGANIZATION = factory.createIRI(NAMESPACE, "hasRegisteredOrganization");
        LEGAL_NAME = factory.createIRI(NAMESPACE, "legalName");
        ORG_ACTIVITY = factory.createIRI(NAMESPACE, "orgActivity");
        ORG_STATUS = factory.createIRI(NAMESPACE, "orgStatus");
        ORG_TYPE = factory.createIRI(NAMESPACE, "orgType");
        REGISTRATION = factory.createIRI(NAMESPACE, "registration");
    }
}