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
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.Elements;
import org.jsoup.select.Evaluator;
import org.jsoup.select.NodeFilter;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;


/**
 * Converts HTML result page from CBE public search into a Java object
 * 
 * @author Bart Hanssens <bart.hanssens@bosa.fgov.be>
 * @see <a href="https://kbopub.economie.fgov.be/kbopub/zoeknummerform.html">Public Search</a>
 */
@Component
public class CbeHtmlMessageConverter implements HttpMessageConverter<CbeOrganization>{
	@Value("${be.belgif.org.baseurl}")
	protected String BASEURL;

	@Value("${be.belgif.org.html.org.table.general}")
	protected String TABLE_GENERAL;

	@Value("${be.belgif.org.html.org.general.id}")
	protected String GENERAL_ID_ORG;

	@Value("${be.belgif.org.html.site.general.id}")
	protected String GENERAL_ID_SITE;

	@Value("${be.belgif.org.html.org.general.names}")
	protected String GENERAL_NAMES;

	@Value("${be.belgif.org.html.org.general.abbrevs}")
	protected String GENERAL_ABBREVS;

	@Value("${be.belgif.org.html.org.general.email}")
	protected String GENERAL_EMAIL;

	@Value("${be.belgif.org.html.org.general.website}")
	protected String GENERAL_WEBSITE;

	@Value("${be.belgif.org.html.org.lang.dutch}")
	protected String LANG_NL;

	@Value("${be.belgif.org.html.org.lang.french}")
	protected String LANG_FR;

	@Value("${be.belgif.org.html.org.lang.german}")
	protected String LANG_DE;

	@Value("${be.belgif.org.html.org.vat.activity}")
	protected String VAT_ACTIVITY;

	@Value("${be.belgif.org.html.org.nsso.activity}")
	protected String NSSO_ACTIVITY;

	@Value("${be.belgif.org.html.org.nsso.activity_old}")
	protected String NSSO_OLD_ACTIVITY;
	

	@Override
	public boolean canRead(Class<?> clazz, MediaType mediaType) {
		return clazz.equals(CbeOrganization.class) && MediaType.TEXT_HTML.isCompatibleWith(mediaType);
	}

	@Override
	public boolean canWrite(Class<?> clazz, MediaType mediaType) {
		return false;
	}

	@Override
	public List<MediaType> getSupportedMediaTypes() {
		return List.of(MediaType.TEXT_HTML);
	}

	@Override
	public CbeOrganization read(Class<? extends CbeOrganization> clazz, HttpInputMessage inputMessage) 
			throws IOException, HttpMessageNotReadableException {
		return parseOrganization(inputMessage.getBody());
	}

	@Override
	public void write(CbeOrganization t, MediaType contentType, HttpOutputMessage outputMessage) 
			throws IOException, HttpMessageNotWritableException {
		throw new UnsupportedOperationException("Not supported");
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
		if (table == null) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Not found");
		}
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
			List<TextNode> els = names.selectNodes("::text", TextNode.class);
			for(int i = 1; i < els.size(); i += 2) {
				String val = els.get(i-1).text().trim();
				String lang = els.get(i).text().trim();
				if (lang.startsWith(LANG_NL)) org.setName("nl", val);
				if (lang.startsWith(LANG_FR)) org.setName("fr", val);
				if (lang.startsWith(LANG_DE)) org.setName("de", val);
			}
			if (org.getNames().isEmpty()) {
				org.setName("", names.text().trim());
			}
		}

		if (abbrevs != null) {
			List<TextNode> els = abbrevs.selectNodes("::text", TextNode.class);
			for(int i = 1; i < els.size(); i += 2) {
				String val = els.get(i-1).text().trim();
				String lang = els.get(i).text().trim();
				if (lang.startsWith(LANG_NL)) org.setAbbrev("nl", val);
				if (lang.startsWith(LANG_FR)) org.setAbbrev("fr", val);
				if (lang.startsWith(LANG_DE)) org.setAbbrev("de", val);
			}
			if (org.getAbbrevs().isEmpty()) {
				org.setAbbrev("", abbrevs.text().trim());
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
