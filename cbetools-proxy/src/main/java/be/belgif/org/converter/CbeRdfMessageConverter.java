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
package be.belgif.org.converter;

import be.belgif.org.dao.CbeOrganization;
import java.io.IOException;
import java.util.List;
import java.util.Map.Entry;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Literal;
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

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.springframework.stereotype.Component;

/**
 * Converts Java object into ORG/ROV triples
 * 
 * Bart Hanssens <bart.hanssens@bosa.fgov.be>
 */
@Component
public class CbeRdfMessageConverter implements HttpMessageConverter<CbeOrganization> {

	@Value("${be.belgif.org.prefix.organization}")
	protected String orgPrefix;

	@Value("${be.belgif.org.prefix.site}")
	protected String sitePrefix;

	@Value("${be.belgif.org.prefix.nace}")
	protected String nacePrefix;

	@Value("${be.belgif.org.prefix.nace_old}")
	protected String naceOldPrefix;

	private static final ValueFactory F = SimpleValueFactory.getInstance();

	@Override
	public boolean canRead(Class<?> clazz, MediaType mediaType) {
		return false;
	}

	@Override
	public boolean canWrite(Class<?> clazz, MediaType mediaType) {
		return clazz.equals(CbeOrganization.class) && 
			(	RDFFormat.NTRIPLES.hasMIMEType(mediaType.toString()) || 
				RDFFormat.JSONLD.hasMIMEType(mediaType.toString())	);
	}

	@Override
	public List<MediaType> getSupportedMediaTypes() {
		return List.of(MediaType.valueOf(RDFFormat.NTRIPLES.getDefaultMIMEType()),
						MediaType.valueOf(RDFFormat.JSONLD.getDefaultMIMEType()));
	}

	@Override
	public CbeOrganization read(Class<? extends CbeOrganization> clazz, HttpInputMessage inputMessage) 
			throws IOException, HttpMessageNotReadableException {
		throw new UnsupportedOperationException("Not supported");
	}

	@Override
	public void write(CbeOrganization t, MediaType contentType, HttpOutputMessage outputMessage) 
			throws IOException, HttpMessageNotWritableException {
		RDFFormat fmt = RDFFormat.NTRIPLES.hasMIMEType(contentType.toString()) ? RDFFormat.NTRIPLES : RDFFormat.JSONLD;
		Rio.write(mapOrgToModel(t), outputMessage.getBody(), fmt);
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
			Literal name = e.getKey().isEmpty() ? F.createLiteral(e.getValue()) 
												: F.createLiteral(e.getValue(), e.getKey());
			m.add(id, ROV.LEGAL_NAME, name);
		}
		for (Entry<String, String> e: org.getAbbrevs().entrySet()) {
			Literal name = e.getKey().isEmpty() ? F.createLiteral(e.getValue()) 
												: F.createLiteral(e.getValue(), e.getKey());
			m.add(id, SKOS.ALT_LABEL, name);
		}
		for (String act: org.getVatActivities()) {
			m.add(id, ROV.ORG_ACTIVITY, F.createIRI(naceOldPrefix + act.replace(".", "")));
		}
		for (String act: org.getNssOldActivities()) {
			m.add(id, ROV.ORG_ACTIVITY, F.createIRI(naceOldPrefix + act.replace(".", "")));
		}
		for (String act: org.getNssActivities()) {
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
