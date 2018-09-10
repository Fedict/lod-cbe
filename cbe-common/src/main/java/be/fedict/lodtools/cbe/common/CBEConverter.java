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
	private final static String DOM_PREF_NACE3 = "http://vocab.belgif.be/auth/nace2003/";
	private final static String DOM_PREF_TYPE = "http://vocab.belgif.be/auth/orgtype/";
	private final static String DOM_PREF_OC = "https://opencorporates.com/id/companies/be/";

	private final static String PREFIX_ORG = "/cbe/org/";
	private final static String PREFIX_REG = "/cbe/registration/";
	private final static String PREFIX_SITE = "/cbe/site/";

	private final static String SUFFIX_ID = "#id";

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
			.append(SUFFIX_ID).toString());
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
	 * Make NACEbel ID
	 *
	 * @param code NACEbel code as string
	 * @param ver NACEbel version
	 * @return IRI
	 */
	public static IRI makeNACE(String code, String ver) {
		if (code == null || code.trim().isEmpty()) {
			LOG.warn("Empty NACE code");
			return null;
		}
		String prefix = ver.startsWith("2003") ? DOM_PREF_NACE3 : DOM_PREF_NACE8;
		return F.createIRI(new StringBuilder(prefix)
			.append(code.trim()).append(SUFFIX_ID).toString());
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
	 * Generate stream of addresses
	 */
	public final static Function<String[], Stream<Statement>> Addresses = row -> {
		IRI subj = makeID(row[0]);

		Stream.Builder<Statement> s = Stream.builder();
		//    s.add(F.createStatement(subj, ORG.

		return s.build();
	};

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
		if (row[0].equals("JuridicalForm")) {
			IRI subj = makeOrgtype(row[1]);
			Literal label = F.createLiteral(row[3], row[2].toLowerCase());
			s.add(F.createStatement(subj, RDF.TYPE, SKOS.CONCEPT));
			s.add(F.createStatement(subj, SKOS.PREF_LABEL, label));
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
	 * Generate stream of activities
	 */
	public final static Function<String[], Stream<Statement>> Activities = row -> {
		IRI nace = makeNACE(row[3], row[2]);
		if (nace == null) {
			return Stream.empty();
		}
		return Stream.of(F.createStatement(makeID(row[0]), ROV.ORG_ACTIVITY, nace));
	};
}
