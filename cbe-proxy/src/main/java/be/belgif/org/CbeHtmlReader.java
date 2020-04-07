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
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;

import javax.ws.rs.Consumes;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.Provider;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;


/**
 * Converts HTML page from CBE into RDF
 * 
 * @author Bart Hanssens
 */
@Provider
@Consumes(MediaType.TEXT_HTML)
public class CbeHtmlReader implements MessageBodyReader<CbeOrganization> {
	@ConfigProperty(name = "be.belgif.org.baseurl")
	protected String BASEURL;

	@ConfigProperty(name = "be.belgif.org.html.org.table.general")
	protected String TABLE_GENERAL;

	@ConfigProperty(name = "be.belgif.org.html.org.general.id")
	protected String GENERAL_ID;

	@Override
	public boolean isReadable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
		return genericType.equals(CbeOrganization.class);
	}

	@Override
	public CbeOrganization readFrom(Class<CbeOrganization> type, Type genericType, Annotation[] annotations, 
			MediaType mediaType, MultivaluedMap<String, String> httpHeaders, InputStream in) 
				throws IOException, WebApplicationException {
		return parseOrganization(in);
	}
	
	private CbeOrganization parseOrganization(InputStream in) throws IOException {
		CbeOrganization org = new CbeOrganization();
		
		Document doc = Jsoup.parse(in, StandardCharsets.UTF_8.toString(), BASEURL);
		Element table = doc.selectFirst(TABLE_GENERAL);
		Element id = table.selectFirst(GENERAL_ID);

		if (id != null) {
			org.setId(id.ownText().trim());
		}
		return org;
	}
}
