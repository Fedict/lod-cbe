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

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.ext.MessageBodyReader;
import jakarta.ws.rs.ext.Provider;

import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;

import org.eclipse.microprofile.config.inject.ConfigProperty;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.jsoup.select.Evaluator;


/**
 * Converts HTML result page from CBE public search into a Java object
 * 
 * @author Bart Hanssens <bart.hanssens@bosa.fgov.be>
 * @see <a href="https://kbopub.economie.fgov.be/kbopub/zoeknummerform.html">Public Search</a>
 */
@Provider
@Consumes(MediaType.TEXT_HTML)
public class CbeHtmlReader implements MessageBodyReader<CbeOrganization> {
	@ConfigProperty(name = "be.belgif.org.baseurl")
	protected String BASEURL;

	@ConfigProperty(name = "be.belgif.org.html.org.table.general")
	protected String TABLE_GENERAL;

	@ConfigProperty(name = "be.belgif.org.html.org.general.id")
	protected String GENERAL_ID_ORG;

	@ConfigProperty(name = "be.belgif.org.html.site.general.id")
	protected String GENERAL_ID_SITE;

	@ConfigProperty(name = "be.belgif.org.html.org.general.names")
	protected String GENERAL_NAMES;

	@ConfigProperty(name = "be.belgif.org.html.org.general.abbrevs")
	protected String GENERAL_ABBREVS;

	@ConfigProperty(name = "be.belgif.org.html.org.general.email")
	protected String GENERAL_EMAIL;

	@ConfigProperty(name = "be.belgif.org.html.org.general.website")
	protected String GENERAL_WEBSITE;

	@ConfigProperty(name = "be.belgif.org.html.org.lang.dutch")
	protected String LANG_NL;

	@ConfigProperty(name = "be.belgif.org.html.org.lang.french")
	protected String LANG_FR;

	@ConfigProperty(name = "be.belgif.org.html.org.lang.german")
	protected String LANG_DE;

	@ConfigProperty(name = "be.belgif.org.html.org.vat.activity")
	protected String VAT_ACTIVITY;

	@ConfigProperty(name = "be.belgif.org.html.org.nsso.activity")
	protected String NSSO_ACTIVITY;

	@ConfigProperty(name = "be.belgif.org.html.org.nsso.activity_old")
	protected String NSSO_OLD_ACTIVITY;

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
	
	/**
	 * Parse the HTML page about an organization (company, public service...)
	 * 
	 * @param in
	 * @return
	 * @throws IOException 
	 */
	private CbeOrganization parseOrganization(InputStream in) throws IOException {		
		CbeOrganization org = new CbeOrganization();

		Document doc = Jsoup.parse(in, StandardCharsets.UTF_8.toString(), BASEURL);
		Element table = doc.selectFirst(TABLE_GENERAL);
		Element orgId = table.selectFirst(GENERAL_ID_ORG);
		Element siteId = table.selectFirst(GENERAL_ID_SITE);
		
		Element names = table.selectFirst(GENERAL_NAMES);
		Element abbrevs = table.selectFirst(GENERAL_ABBREVS);
		Element email = table.selectFirst(GENERAL_EMAIL);
		Element website = table.selectFirst(GENERAL_WEBSITE);

		if (siteId == null) {
			org.setParentId(null);
			org.setId(orgId.ownText().trim());
		} else {
			org.setParentId(orgId.text().trim());
			org.setId(siteId.ownText().trim());
		}

		if (names != null) {
			Elements els = names.select(new Evaluator.MatchText());
			for(int i = 1; i < els.size(); i += 2) {
				String val = els.get(i-1).text().trim();
				if (els.get(i).selectFirst(LANG_NL) != null) org.setName("nl", val);
				if (els.get(i).selectFirst(LANG_FR) != null) org.setName("fr", val);
				if (els.get(i).selectFirst(LANG_DE) != null) org.setName("de", val);
			}
			if (org.getNames().isEmpty()) {
				org.setName("", names.text());
			}
		}

		if (abbrevs != null) {
			Elements els = abbrevs.select(new Evaluator.MatchText());
			for(int i = 1; i < els.size(); i += 2) {
				String val = els.get(i-1).text().trim();
				if (els.get(i).selectFirst(LANG_NL) != null) org.setAbbrev("nl", val);
				if (els.get(i).selectFirst(LANG_FR) != null) org.setAbbrev("fr", val);
				if (els.get(i).selectFirst(LANG_DE) != null) org.setAbbrev("de", val);
			}
			if (org.getAbbrevs().isEmpty()) {
				org.setAbbrev("", abbrevs.text());
			}
		}

		if (email != null) {
			org.setEmail(email.attr("href").trim());
		}
	
		if (website != null) {
			org.setWebsite(website.attr("href").trim());
		}
		
		Elements vatActivities = table.select(VAT_ACTIVITY);
		for (Element act: vatActivities) {
			org.setVatActivity(act.text());
		}
		Elements nssActivities = table.select(NSSO_ACTIVITY);
		for (Element act: nssActivities) {
			org.setNssActivity(act.text());
		}
		Elements nssOldActivities = table.select(NSSO_OLD_ACTIVITY);
		for (Element act: nssOldActivities) {
			org.setNssOldActivity(act.text());
		}
	
		return org;
	}
}
