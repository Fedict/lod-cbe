/*
 * Copyright (c) 2020, FPS BOSA
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

import jakarta.ws.rs.Produces;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.ext.MessageBodyWriter;
import jakarta.ws.rs.ext.Provider;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Map.Entry;

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
 * Converts Java object into ORG/ROV triples
 * 
 * Bart Hanssens <bart.hanssens@bosa.fgov.be>
 */
@Provider
@Produces({"application/n-triples", "application/ld+json"})
public class CbeRdfWriter implements MessageBodyWriter<CbeOrganization> {

	@ConfigProperty(name = "be.belgif.org.prefix.organization")
	protected String orgPrefix;

	@ConfigProperty(name = "be.belgif.org.prefix.site")
	protected String sitePrefix;

	@ConfigProperty(name = "be.belgif.org.prefix.nace")
	protected String nacePrefix;

	private final ValueFactory F = SimpleValueFactory.getInstance();

	@Override
	public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
		return genericType.equals(CbeOrganization.class) && 
			(	RDFFormat.NTRIPLES.hasMIMEType(mediaType.toString()) || 
				RDFFormat.JSONLD.hasMIMEType(mediaType.toString())	
			);
	}

	@Override
	public long getSize(CbeOrganization t, Class<?> type, Type genericType, Annotation[] annotations, 
						MediaType mediaType) {
		return -1;
	}

	@Override
	public void writeTo(CbeOrganization t, Class<?> type, Type genericType, Annotation[] annotations, 
			MediaType mediaType, MultivaluedMap<String, Object> httpHeaders, OutputStream entityStream) 
				throws IOException, WebApplicationException {
		RDFFormat fmt = RDFFormat.NTRIPLES.hasMIMEType(mediaType.toString()) ? RDFFormat.NTRIPLES : RDFFormat.JSONLD;
		Rio.write(mapOrgToModel(t), entityStream, fmt);
	}

	/**
	 * Turn organization object into RDF model
	 * 
	 * @param org organization object
	 * @return RDF Model 
	 */
	private Model mapOrgToModel(CbeOrganization org) {
		Model m = new LinkedHashModel();

		String orgId = org.getId().replace(".", "");
		String parentId = org.getParentId();
	
		IRI id;
		if (parentId == null) {
			id = F.createIRI(orgPrefix + orgId);
			m.add(id, RDF.TYPE, ORG.ORGANIZATION);
			m.add(id, RDF.TYPE, ROV.REGISTERED_ORGANIZATION);
		} else {
			id = F.createIRI(sitePrefix + orgId);
			m.add(id, RDF.TYPE, ORG.SITE);
			m.add(id, ORG.SITE_OF, F.createIRI(orgPrefix + parentId.replace(".", "")));
		}

		for (Entry<String, String> e: org.getNames().entrySet()) {
			m.add(id, ROV.LEGAL_NAME, F.createLiteral(e.getValue(), e.getKey()));
		}
		for (Entry<String, String> e: org.getAbbrevs().entrySet()) {
			m.add(id, SKOS.ALT_LABEL, F.createLiteral(e.getValue(), e.getKey()));
		}		
		for (String act: org.getNssActivities()) {
			m.add(id, ROV.ORG_ACTIVITY, F.createIRI(nacePrefix + act.replace(".", "")));
		}
		for (String act: org.getVatActivities()) {
			m.add(id, ROV.ORG_ACTIVITY, F.createIRI(nacePrefix + act.replace(".", "")));
		}
		if (org.getEmail() != null) {
			m.add(id, FOAF.MBOX, F.createIRI(org.getEmail()));
		}
		if (org.getWebsite() != null) {
			m.add(id, FOAF.HOMEPAGE, F.createIRI(org.getWebsite()));
		}
		return m;
	}
}
