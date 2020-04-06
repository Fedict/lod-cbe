/*
 * Copyright (c) 2020, Bart Hanssens <bart.hanssens@bosa.fgov.be>
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
package be.belgif.org;

import be.belgif.org.dao.CbeOrganization;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Map;
import java.util.Map.Entry;
import static java.util.Map.entry;
import java.util.Set;

import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;
import org.attoparser.config.ParseConfiguration;
import org.attoparser.simple.ISimpleMarkupParser;
import org.attoparser.simple.SimpleMarkupParser;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.rdf4j.model.IRI;

import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.LinkedHashModel;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.vocabulary.FOAF;
import org.eclipse.rdf4j.model.vocabulary.ORG;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.model.vocabulary.ROV;
import org.eclipse.rdf4j.model.vocabulary.SKOS;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.Rio;

/**
 * Converts HTML page from CBE into RDF
 * 
 * @author Bart Hanssens
 */
@Provider
@Produces("application/n-triples")
public class CbeRdfWriter implements MessageBodyWriter<CbeOrganization> {

	@ConfigProperty(name = "be.belgif.org.prefix.organization")
	private String orgPrefix;

	private final ValueFactory F = SimpleValueFactory.getInstance();

	@Override
	public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
		return true;
		// return RDFFormat.TURTLE.hasMIMEType(arg3.toString());
	}

	@Override
	public long getSize(CbeOrganization t, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
		return -1;
	}

	@Override
	public void writeTo(CbeOrganization t, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType, 
											MultivaluedMap<String, Object> httpHeaders, OutputStream entityStream) 
			throws IOException, WebApplicationException {
		Rio.write(mapOrgToModel(t), entityStream, RDFFormat.NTRIPLES);
	}


	private Model mapOrgToModel(CbeOrganization org) {
		Model m = new LinkedHashModel();

		IRI id = F.createIRI(orgPrefix + org.getId().replaceAll("\\.", "_"));
		m.add(id, RDF.TYPE, ORG.ORGANIZATION);
		m.add(id, RDF.TYPE, ROV.REGISTERED_ORGANIZATION);
		
		for (Entry<String, String> e: org.getNames().entrySet()) {
			m.add(id, ROV.LEGAL_NAME, F.createLiteral(e.getValue(), e.getKey()));
		}
		for (Entry<String, String> e: org.getAbbrevs().entrySet()) {
			m.add(id, SKOS.ALT_LABEL, F.createLiteral(e.getValue(), e.getKey()));
		}		

		if (org.getWebsite() != null) {
			m.add(id, FOAF.HOMEPAGE, F.createIRI(org.getWebsite()));
		}
		return m;
	}
}
