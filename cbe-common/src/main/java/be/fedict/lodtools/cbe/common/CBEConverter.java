/*
 * Copyright (c) 2017, Bart Hanssens <bart.hanssens@fedict.be>
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
package be.fedict.lodtools.cbe.common;

import java.text.ParseException;
import java.text.SimpleDateFormat;

import java.util.Date;
import java.util.function.Function;
import java.util.stream.Stream;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;

import org.eclipse.rdf4j.model.vocabulary.DCTERMS;
import org.eclipse.rdf4j.model.vocabulary.FOAF;
import org.eclipse.rdf4j.model.vocabulary.LOCN;
import org.eclipse.rdf4j.model.vocabulary.ORG;
import org.eclipse.rdf4j.model.vocabulary.OWL;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.model.vocabulary.RDFS;
import org.eclipse.rdf4j.model.vocabulary.ROV;
import org.eclipse.rdf4j.model.vocabulary.SKOS;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Bart.Hanssens
 */
public class CBEConverter {

	private final static Logger LOG = LoggerFactory.getLogger(CBEConverter.class);

	private final static ValueFactory F = SimpleValueFactory.getInstance();

	private final static SimpleDateFormat SDF = new SimpleDateFormat("dd-MM-yyyy");

	public final static String ORG_BELGIF = "http://org.belgif.be";
	
	private final static String DOM_PREF_NACE8 = "http://vocab.belgif.be/auth/nace2008/";
	private final static String DOM_PREF_TYPE = "http://vocab.belgif.be/auth/orgtype/";
	private final static String DOM_PREF_OC = "https://opencorporates.com/id/companies/be/";

	private final static String PREFIX_ORG = "/id/cbe/org/";
	private final static String PREFIX_REG = "/id/cbe/registration/";
	private final static String PREFIX_SITE = "/id/cbe/site/";
	private final static String PREFIX_ADDR = "/id/cbe/addr/";

	private final static String RAMON_NACE = "http://ec.europa.eu/eurostat/ramon/ontologies/nace.rdf#";
	private final static String RAMON_DATA = "http://ec.europa.eu/eurostat/ramon/rdfdata/nace_r2/";
	private final static IRI NACE_ACT = F.createIRI(RAMON_NACE + "Activity");
	private final static IRI NACE_CODE = F.createIRI(RAMON_NACE + "code");
	
	/**
	 * Make unique ID for an organization or site
	 *
	 * @param cbe CBE number as string
	 * @return IRI
	 */
	public static IRI makeID(String cbe) {
		return makeID(cbe.startsWith("0") ? PREFIX_ORG : PREFIX_SITE, cbe);
	}

	/**
	 * Make unique ID for an organization or site
	 *
	 * @param type organization or site
	 * @param cbe CBE number as string
	 * @return IRI
	 */
	public static IRI makeID(String type, String cbe) {
		return F.createIRI(new StringBuilder(ORG_BELGIF)
			.append(type)
			.append(cbe.replaceAll("\\.", "_").replaceAll(" ", "%20"))
			.toString());
	}

	/**
	 * Make unique ID for address from address parts
	 * 
	 * @param parts zipcode, street name, number... 
	 * @return IRI
	 */
	public static IRI makeAddress(String... parts) {
		StringBuilder s = new StringBuilder(ORG_BELGIF).append(PREFIX_ADDR);
		String prefPart = "";
		for (String part: parts) {
			if (part != null && !part.isEmpty() && !part.equals(prefPart)) {
				prefPart = part;
				s.append(part.replaceAll("\\W", "_")).append("_");
			}
		}
		s.deleteCharAt(s.length()-1);
		return F.createIRI(s.toString());
	}
	
	/**
	 * Make OpenCorporates.com ID
	 *
	 * @param cbe CBE number as string
	 * @return IRI
	 */
	public static IRI makeOCID(String cbe) {
		return F.createIRI(new StringBuilder(DOM_PREF_OC)
			.append(cbe.replaceAll("\\.", "")).toString());
	}

	/**
	 * Make organization type
	 *
	 * @param cbe CBE number as string
	 * @return IRI
	 */
	public static IRI makeOrgtype(String cbe) {
		if (cbe == null || cbe.trim().isEmpty()) {
			LOG.warn("Empty org type");
			return null;
		}
		return F.createIRI(new StringBuilder(DOM_PREF_TYPE)
			.append("CBE").append(cbe.trim()).toString());
	}

	/**
	 * Convert DD-MM-YYYY date string to date object
	 *
	 * @param date date string in DD-MM-YYYY format
	 * @return date object
	 */
	public static Date asDate(String date) {
		try {
			return SDF.parse(date);
		} catch (ParseException ex) {
			return null;
		}
	}

	/**
	 * Clean and convert phone number to tel: IRI. By default, the prefix
	 * +32 (Belgium) will be added
	 *
	 * @param phone phone number
	 * @return tel: IRI
	 */
	public static IRI asPhone(String phone) {
		if (phone == null || phone.trim().length() < 9) {
			LOG.warn("Incorrect phone {}", phone);
			return null;
		}
		String s = phone.trim().replace("(0)", "-")
			.replaceAll("[^\\d+]+", "-")
			.replaceFirst("^[-0]+", "+32-");
		return F.createIRI("tel:" + s);
	}

	/**
	 * Clean up and convert webpage to http: IRI.
	 *
	 * @param page web page
	 * @return http: IRI or null
	 */
	public static IRI asPage(String page) {
		if (page == null || page.trim().length() < 5) {
			LOG.warn("Incorrect URL {}", page);
			return null;
		}
		// multiple pages 
		String s = page.toLowerCase().trim().split(" ", 2)[0];
		if (s.length() < 5) {
			LOG.warn("Incorrect URL {}", s);
			return null;
		}

		// check for malformed input
		if (s.startsWith("http") || s.startsWith("https")) {
			if (!(s.startsWith("http://") || s.startsWith("https://"))) {
				LOG.warn("Incorrect URL {}", s);
				return null;
			}
		} else if (s.startsWith("www:")) {
			LOG.warn("Incorrect URL {}", s);
			return null;
		}
		try { 
			return F.createIRI(s.startsWith("http") ? s : "http://" + s);
		} catch (IllegalArgumentException iae) {
			LOG.warn("Incorrect URL {}", s);
			return null;
		}
	}

	/**
	 * Get broader NACEbel code or null
	 * 
	 * @param code nacebel code
	 * @return broader code as string or null
	 */
	public static String broaderNace(String code) {
		int len = code.length();
		if (len > 2 && len < 6) {
			return code.substring(0, len - 2);
		}
		if (len == 7) {
			return code.substring(0, len - 3);
		}
		return null;
	}

	/**
	 * Clean up and convert email address to mailto: IRI.
	 *
	 * @param mail email address
	 * @return mailto: IRI or null
	 */
	public static IRI asMail(String mail) {
		if (mail == null || mail.trim().length() < 5) {
			LOG.warn("Incorrect email {}", mail);
			return null;
		}
		// multiple mails
		String s = mail.toLowerCase().trim().split(" ", 2)[0];
		if (s.length() < 7 || !s.contains("@")) {
			LOG.warn("Incorrect email", mail);
			return null;
		}
		// correct malformed input
		s = s.replaceFirst("<", "").replaceFirst(">", "");
		return F.createIRI("mailto:" + s);
	}

	/**
	 * Guess languages based on zip code
	 * 
	 * @param code zip code
	 * @return language code or empty string
	 */
	public static String guessLang(String code) {
		int i = 0;
		
		try {
			i = Integer.valueOf(code);
		} catch (NumberFormatException ioe) {
			LOG.warn("Could not convert zip code {}", code);
		}

		if (i < 1300) {
			return "";
		}
		if ((i >= 1300 && i < 1500) || (i >= 4000 && i < 8000)) {
			return "fr";
		}
		if ((i >= 1500 && i < 4000) || (i >= 8000 && i < 10000)) {
			return "nl";
		}
		return "";
	}
	/**
	 * Generate stream of organization name triples
	 */
	public final static Function<String[], Stream<Statement>> Names = row -> {
		IRI subj = makeID(row[0]);
		String lang = "";
		switch (row[1]) {
			case "1":
				lang = "fr";
				break;
			case "2":
				lang = "nl";
				break;
			case "3":
				lang = "de";
				break;
			case "4":
				lang = "en";
				break;
		}
		IRI pred = row[2].equals("001") ? ROV.LEGAL_NAME : SKOS.ALT_LABEL;
		Literal lit = (!lang.isEmpty()) ? F.createLiteral(row[3], lang)
			: F.createLiteral(row[3]);
		Stream.Builder<Statement> s = Stream.builder();
		s.add(F.createStatement(subj, pred, lit));

		// Add label for query / display purposes
		if (pred.equals(ROV.LEGAL_NAME)
			|| (row[0].startsWith("2") && pred.equals(SKOS.ALT_LABEL))) {
			s.add(F.createStatement(subj, RDFS.LABEL, lit));
		}

		return s.build();
	};

	/**
	 * Generate stream of registration records
	 */
	public final static Function<String[], Stream<Statement>> Org = row -> {
		IRI subj = makeID(PREFIX_ORG, row[0]);
		IRI reg = makeID(PREFIX_REG, row[0]);
		IRI type = makeOrgtype(row[4]);
		Date date = asDate(row[5]);

		Stream.Builder<Statement> s = Stream.builder();
		s.add(F.createStatement(subj, RDF.TYPE, ROV.REGISTERED_ORGANIZATION))
			.add(F.createStatement(subj, ROV.REGISTRATION, reg))
			.add(F.createStatement(subj, OWL.SAMEAS, makeOCID(row[0])))
			.add(F.createStatement(reg, DCTERMS.ISSUED, F.createLiteral(date)));

		if (type != null) {
			s.add(F.createStatement(subj, ROV.ORG_TYPE, type));
		}
		return s.build();
	};

	/**
	 * Generate stream of organization name triples
	 */
	public final static Function<String[], Stream<Statement>> Sites = row -> {
		IRI site = makeID(PREFIX_SITE, row[0]);
		Date date = asDate(row[1]);
		IRI org = makeID(PREFIX_ORG, row[2]);

		Stream.Builder<Statement> s = Stream.builder();
		s.add(F.createStatement(site, RDF.TYPE, ORG.SITE))
			.add(F.createStatement(org, ORG.HAS_SITE, site))
			.add(F.createStatement(site, ORG.SITE_OF, org))
			.add(F.createStatement(site, DCTERMS.ISSUED, F.createLiteral(date)));
		return s.build();
	};

	/**
	 * Generate stream of codes
	 */
	public final static Function<String[], Stream<Statement>> Codes = row -> {
		Stream.Builder<Statement> s = Stream.builder();

		IRI subj = null;
		String lang = row[2].toLowerCase();
		Literal label = F.createLiteral(row[3], lang);
		
		switch(row[0]) {
			case "JuridicalForm":
				subj = makeOrgtype(row[1]);

				s.add(F.createStatement(subj, SKOS.PREF_LABEL, label));
				if (lang.equals("nl")) { // only once
					s.add(F.createStatement(subj, RDF.TYPE, SKOS.CONCEPT));
				}
				break;
			case "Nace2008":
				subj = F.createIRI(DOM_PREF_NACE8 + row[1]);

				s.add(F.createStatement(subj, SKOS.PREF_LABEL, label));

				if (lang.equals("nl")) { // only once
					s.add(F.createStatement(subj, RDF.TYPE, SKOS.CONCEPT));
					s.add(F.createStatement(subj, SKOS.NOTATION, F.createLiteral(row[1])));
				
					String broader = broaderNace(row[2]);
					if (broader != null) {
						s.add(F.createStatement(subj, SKOS.BROADER, F.createIRI(DOM_PREF_NACE8 + broader)));
					}
					int len = row[1].length();
					IRI pred = (len < 5) ? SKOS.EXACT_MATCH : SKOS.BROAD_MATCH;
					String ramon = row[1].substring(0, Math.min(len, 2));
					if (len > 2) {
						ramon += "." + row[1].substring(2, Math.min(len, 4));
					}
					IRI nace = F.createIRI(RAMON_DATA + ramon);
					s.add(F.createStatement(subj, pred, nace));
				}
				break;
		}
		return s.build();
	};

	/**
	 * Generate stream of contacts
	 */
	public final static Function<String[], Stream<Statement>> Contacts = row -> {
		IRI subj = makeID(row[0]);
		IRI type = null;
		IRI contact = null;

		switch (row[2]) {
			case "TEL":
				type = FOAF.PHONE;
				contact = asPhone(row[3]);
				break;
			case "WEB":
				type = FOAF.HOMEPAGE;
				contact = asPage(row[3]);
				break;
			case "EMAIL":
				type = FOAF.MBOX;
				contact = asMail(row[3]);
				break;
		}
		if (contact == null) {
			return Stream.empty();
		}
		return Stream.of(F.createStatement(subj, type, contact));
	};

	/**
	 * Generate stream of addresses
	 */
	public final static Function<String[], Stream<Statement>> Addresses = row -> {
		Stream.Builder<Statement> s = Stream.builder();
		
		IRI subj = makeID(row[0]);
		IRI addr = makeAddress(row[2], row[3], row[4], row[7], row[8], row[9], row[10]);
		s.add(F.createStatement(subj, LOCN.ADDRESS_PROP, addr));
		s.add(F.createStatement(addr, RDF.TYPE, LOCN.ADDRESS));
		s.add(F.createStatement(addr, LOCN.ADMIN_UNIT_L1, 
			F.createLiteral(row[2].isEmpty() ? "België" : row[2], "nl")));
		s.add(F.createStatement(addr, LOCN.ADMIN_UNIT_L1, 
			F.createLiteral(row[3].isEmpty() ? "Belgique" : row[3], "fr")));

		if (!row[4].isEmpty()) {
			s.add(F.createStatement(addr, LOCN.POST_CODE, F.createLiteral(row[4])));
		}

		// guess language for Belgian municipalities based on zip code
		String guess = row[2].isEmpty() ? guessLang(row[4]) : "";
		// only output the municipality names and street names if the names are really different
		if (!row[5].isEmpty() && (!row[5].equals(row[6]) || !guess.equals("fr"))) {
			s.add(F.createStatement(addr, LOCN.POST_NAME, F.createLiteral(row[5], "nl")));
		}
		if (!row[6].isEmpty() && (!row[6].equals(row[5]) || !guess.equals("nl"))) {
			s.add(F.createStatement(addr, LOCN.POST_NAME, F.createLiteral(row[6], "fr")));
		}
		if (!row[7].isEmpty() && (!row[7].equals(row[8]) || !guess.equals("fr"))) {
			s.add(F.createStatement(addr, LOCN.THOROUGHFARE, F.createLiteral(row[7], "nl")));
		}
		if (!row[8].isEmpty() && (!row[8].equals(row[7]) || !guess.equals("nl"))) {
			s.add(F.createStatement(addr, LOCN.THOROUGHFARE, F.createLiteral(row[8], "fr")));
		}
		if (! row[9].isEmpty()) {
			String no = row[10].isEmpty() ? row[9] : row[9] + "/" + row[10];
			s.add(F.createStatement(addr, LOCN.LOCATOR_DESIGNATOR, F.createLiteral(no)));
		}
		return s.build();
	};
	
	/**
	 * Generate stream of activities
	 */
	public final static Function<String[], Stream<Statement>> Activities = row -> {
		if (row[3].isEmpty() || !row[2].equals("2008")) {
			// old
			return Stream.empty();
		}
		IRI subj = makeID(row[0]);
		IRI nacebel = F.createIRI(DOM_PREF_NACE8 + row[3]);
		return Stream.of(F.createStatement(subj, ROV.ORG_ACTIVITY, nacebel));
	};
}
