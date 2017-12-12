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
package be.fedict.lodtools.cbe.updater;

import com.google.common.base.Charsets;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;

import java.text.ParseException;
import java.text.SimpleDateFormat;

import java.util.Date;
import java.util.HashMap;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.stream.Stream;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;

import org.eclipse.rdf4j.model.vocabulary.DCTERMS;
import org.eclipse.rdf4j.model.vocabulary.FOAF;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.model.vocabulary.ORG;
import org.eclipse.rdf4j.model.vocabulary.OWL;
import org.eclipse.rdf4j.model.vocabulary.RDFS;
import org.eclipse.rdf4j.model.vocabulary.ROV;
import org.eclipse.rdf4j.model.vocabulary.SKOS;

import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.RDFHandler;
import org.eclipse.rdf4j.rio.RDFWriter;
import org.eclipse.rdf4j.rio.Rio;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Convert open data CBE (Belgian company register) to RDF Triples.
 * 
 * @author Bart Hanssens <bart.hanssens@fedict.be>
 */
public class Main {
	private final static Logger LOG = LoggerFactory.getLogger(Main.class);
 
    private final static ValueFactory F = SimpleValueFactory.getInstance();

    private final static SimpleDateFormat SDF = new SimpleDateFormat("dd-MM-yyyy");
    
    private static String domain = null;
	
	private final static String DOM_BELGIF = "http://org.belgif.be";
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
	private static IRI makeID(String cbe) {
		return makeID(cbe.startsWith("0") ? PREFIX_ORG : PREFIX_SITE, cbe);
	}
	
    /**
	 * Make unique ID for an organization or site
	 * 
	 * @param type organization or site
	 * @param cbe CBE number as string
	 * @return IRI
	 */
    private static IRI makeID(String type, String cbe) {
        return F.createIRI(new StringBuilder(domain)
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
	private static IRI makeOCID(String cbe) {
		return F.createIRI(new StringBuilder(DOM_PREF_OC)
							.append(cbe.replaceAll("\\.", "")).toString());
	}

	/**
	 * Make organization type
	 * 
	 * @param cbe CBE number as string
	 * @return IRI
	 */
	private static IRI makeOrgtype(String cbe) {
		return F.createIRI(new StringBuilder(DOM_PREF_TYPE)
									.append("CBE").append(cbe).toString());
	}
		
	/**
	 * Make NACEbel ID
	 * 
	 * @param code NACEbel code as string
	 * @param code NACEbel version
	 * @return IRI
	 */
	private static IRI makeNACE(String code, String ver) {
		String prefix = ver.startsWith("2003") ? DOM_PREF_NACE3 : DOM_PREF_NACE8;
		return F.createIRI(new StringBuilder(prefix)
				.append(code).append(SUFFIX_ID).toString());
	}
	
    /**
     * Convert DD-MM-YYYY date string to date object
     * 
     * @param date date string in DD-MM-YYYY format
     * @return date object
     */
    private static Date asDate(String date) {
        try {
            return SDF.parse(date);
        } catch (ParseException ex) {
            return null;
        }
    }
    
    /**
     * Clean and convert phone number to tel: IRI.
     * By default, the prefix +32 (Belgium) will be added
     * 
     * @param phone phone number
     * @return tel: IRI
     */
    private static IRI asPhone(String phone) {
        String s = phone.replace("(0)", "-")
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
    private static IRI asPage(String page) {
        String s = page.toLowerCase().trim().split(" ", 2)[0];
		if (s.length() < 5) {
			return null;
		}
		// correct malformed input
		s = s.replaceFirst("http:\\\\", "http://");
		s = s.replaceFirst("http//", "http://"); 
		s = s.replaceFirst("https//", "https://");
        return F.createIRI(s.startsWith("http") ? s : "http://" + s);
    }
    
    /**
     * Clean up and convert email address to mailto: IRI.
     * 
     * @param mail email address
     * @return mailto: IRI or null
     */
    private static IRI asMail(String mail) {
        String s = mail.toLowerCase().trim().split(" ", 2)[0];
		if (s.length() < 7 || !s.contains("@")) {
			return null;
		}
		// correct malformed input
		s = s.replaceFirst("<", "").replaceFirst(">", "");
        return F.createIRI("mailto:" + s);
    }

	/**
     * Generate organization names to delete
     */
    private final static Function<String[],String> Names_del = row -> {
		return makeID(row[0]).toString();
    };

	/**
     * Generate registration records to  delete
     */
    private final static Function<String[],String> Org_del = row -> {
        return makeID(PREFIX_ORG, row[0]).toString();
	};

	/**
     * Generate site ID to delete
     */	
	private final static Function<String[],String> Sites_del = row -> {
		return makeID(PREFIX_SITE, row[0]).toString();
	};

	/**
     * Generate contact ID to delete
     */
    private final static Function<String[],String> Contacts_del = row -> {
		return makeID(row[0]).toString();
	};
			
	/**
     * Generate activity ID to delet
     */
    private final static Function<String[],String> Activities_del = row -> {
        return makeID(row[0]).toString();
    };

	/**
	 * Generate addresses
	 */
	private final static Function<String[],String> Addresses_del = row -> {
		return makeID(row[0]).toString();
	};
	
	/**
     * Map files to the functions generating RDF triples.
     */
    private final static HashMap<String,Function> MAP_DEL = new HashMap<String,Function>(){{
        put("enterprise_delete.csv", Org_del);
        put("denomination_delete.csv", Names_del);
        put("establishment_delete.csv", Sites_del);
        put("contact_delete.csv", Contacts_del);
        put("activity_delete.csv", Activities_del);
		put("address_delete.csv", Addresses_del);
    }};
       

	/**
	 * Generate stream of addresses
	 */
	private final static Function<String[],Stream<Statement>> Addresses = row -> {
		IRI subj = makeID(row[0]);
		
		Stream.Builder<Statement> s = Stream.builder();
    //    s.add(F.createStatement(subj, ORG.
		
		return s.build();
	};

    /**
     * Generate stream of organization name triples
     */
    private final static Function<String[],Stream<Statement>> Names = row -> {
        IRI subj = makeID(row[0]);
        String lang = "";
        switch(row[1]) {
            case "1": lang = "fr"; break;
            case "2": lang = "nl"; break;
            case "3": lang = "de"; break;
            case "4": lang = "en"; break;
        }
        IRI pred = row[2].equals("001") ? ROV.LEGAL_NAME : SKOS.ALT_LABEL;
		Literal lit = (!lang.isEmpty()) ? F.createLiteral(row[3], lang)
										: F.createLiteral(row[3]);
		Stream.Builder<Statement> s = Stream.builder();
        s.add(F.createStatement(subj, pred, lit));
		
		// Add label for query / display purposes
		if (pred.equals(ROV.LEGAL_NAME) ||
				(row[0].startsWith("2") && pred.equals(SKOS.ALT_LABEL))) {
			s.add(F.createStatement(subj, RDFS.LABEL, lit));
		}
		
        return s.build();
    };
    
    /**
     * Generate stream of registration records 
     */
    private final static Function<String[],Stream<Statement>> Org = row -> {
        IRI subj = makeID(PREFIX_ORG, row[0]);
        IRI reg = makeID(PREFIX_REG, row[0]);
		IRI type = makeOrgtype(row[4]);
        Date date = asDate(row[5]);
        
        Stream.Builder<Statement> s = Stream.builder();
        s.add(F.createStatement(subj, RDF.TYPE, ROV.REGISTERED_ORGANIZATION))
            .add(F.createStatement(subj, ROV.REGISTRATION, reg))
			.add(F.createStatement(subj, ROV.ORG_TYPE, type))
			.add(F.createStatement(subj, OWL.SAMEAS, makeOCID(row[0])))
            .add(F.createStatement(reg, DCTERMS.ISSUED, F.createLiteral(date)));
        return s.build();
    };
    
    /**
     * Generate stream of organization name triples
     */
    private final static Function<String[],Stream<Statement>> Sites = row -> {
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
	private final static Function<String[],Stream<Statement>> Codes = row -> {
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
    private final static Function<String[],Stream<Statement>> Contacts = row -> {
        IRI subj = makeID(row[0]);
        IRI type = null;
        IRI contact = null;
        
        switch(row[2]) {
            case "TEL": type = FOAF.PHONE; contact = asPhone(row[3]); break;
            case "WEB": type = FOAF.HOMEPAGE; contact = asPage(row[3]); break;
            case "EMAIL": type = FOAF.MBOX; contact = asMail(row[3]); break;
        }
		if (contact == null) {
			return Stream.empty();
		}
		return Stream.of(F.createStatement(subj, type, contact));
	};
   
    /**
     * Generate stream of activities
     */
    private final static Function<String[],Stream<Statement>> Activities = row -> {
        return Stream.of(F.createStatement(makeID(row[0]), 
									ROV.ORG_ACTIVITY, makeNACE(row[3], row[2])));
    };
   
    /**
     * Map files to the functions generating RDF triples.
     */
    private final static HashMap<String,Function> MAP_INS = new HashMap<String,Function>(){{
        put("enterprise_insert.csv", Org);
        put("denomination_insert.csv", Names);
        put("establishment_insert.csv", Sites);
        put("contact_insert.csv", Contacts);
        put("activity_insert.csv", Activities);
		put("address_insert.csv", Addresses);
    }};
 
	
    /**
     * Generate RDF triples from CSV file, reading 10000 lines at once
     * 
     * @param rdf RDF writer
     * @param csv CSV containing data
     * @param fun function generating RDF triples
     * @throws IOException 
     */
    private static void add(RDFHandler rdf, Reader csv, 
                            Function<String[],Stream<Statement>> fun) throws IOException {
        int lines = 10000;
  
        try (CsvBulkReader r = new CsvBulkReader(csv)) {
            while(r.hasNext()) {
                r.readNext(lines).stream().flatMap(fun).forEach(rdf::handleStatement);
				LOG.debug("Reading lines");
            }
        }
    }
    
	/**
	 * Write a line + newline to a writer
	 * 
	 * @param w writer
	 * @param line line to be written
	 */
	private static void writeLine(BufferedWriter w, String line) {
		try {
			w.write(line);
			w.newLine();
		} catch (IOException ioe) {
			LOG.error("Error writing to file");
		}
	}
	
	/**
	 * Add a line (typically an ID) to a file
	 * 
	 * @param w
	 * @param csv
	 * @param fun
	 * @throws IOException 
	 */
	private static void add(BufferedWriter w, Reader csv, 
								Function<String[],String> fun) throws IOException {
		int lines = 10000;

		try (CsvBulkReader r = new CsvBulkReader(csv)) {
            while(r.hasNext()) {
                r.readNext(lines).stream().map(fun).forEach(l -> writeLine(w,l));
			}
		}
	}
	
    /**
     * Main
     * 
     * @param args
     * @throws IOException 
     */
    public static void main(String[] args) throws IOException {
        if (args.length < 2) {
            System.out.println("Usage: cbe <input_dir> <output_dir> [IRI_domain]");
            System.exit(-1);
        }
        
        File basedir = new File(args[0]);
		File outdir = new File(args[1]);

        if (args.length > 2 && args[2].startsWith("http")) {
            domain = args[2];
        } else {
            domain = DOM_BELGIF;
        }
		
        LOG.info("--- START ---");
		LOG.info("Params in = {}, out = {}, domain = {}", basedir, outdir, domain);

		for(String file: MAP_DEL.keySet()) {
			File delfile = new File(outdir, file.replaceAll("_delete", "_id"));
			try (   FileOutputStream fout = new FileOutputStream(delfile);
					BufferedWriter w = new BufferedWriter(
								new OutputStreamWriter(fout, Charsets.UTF_8))){
				LOG.info("Reading CSV file {}, writing {}", file, delfile);
				InputStream fin = new FileInputStream(new File(basedir, file));
				add(w, new InputStreamReader(fin, Charsets.UTF_8), MAP_DEL.get(file));
			} catch (IOException ex) {
			}
		}
		
        File outf = new File(args[1], "cbe-upd.nt");		
		// inserts for companies / organizations
        try (	FileOutputStream fout = new FileOutputStream(outf);
				BufferedWriter w = new BufferedWriter(
								new OutputStreamWriter(fout, Charsets.UTF_8))){
            RDFWriter rdf = Rio.createWriter(RDFFormat.NTRIPLES, w);
            rdf.startRDF();
			
            for(String file: MAP_INS.keySet()) {
				LOG.info("Reading CSV file {}", file);
				InputStream fin = new FileInputStream(new File(basedir, file));
                add(rdf, new InputStreamReader(fin, Charsets.UTF_8), MAP_INS.get(file));
            }
			
            rdf.endRDF();
        }
		
		LOG.info("--- END ---");
    }
}
