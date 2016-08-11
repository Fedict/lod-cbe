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
package be.fedict.lodtools.cbe;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.FileReader;
import java.io.FileWriter;

import java.text.ParseException;
import java.text.SimpleDateFormat;

import java.util.Date;
import java.util.HashMap;
import java.util.function.Function;
import java.util.stream.Stream;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.vocabulary.DCTERMS;
import org.eclipse.rdf4j.model.vocabulary.FOAF;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.model.vocabulary.ORG;
import org.eclipse.rdf4j.model.vocabulary.OWL;
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
	private final static String DOM_PREF_NACE = "http://vocab.belgif.be/nace2008/";
	private final static String DOM_PREF_OC = "https://opencorporates.com/id/companies/be/";
	
    private final static String PREFIX_ORG = "/cbe/org/";
    private final static String PREFIX_REG = "/cbe/registration/";
    private final static String PREFIX_SITE = "/cbe/site/";
    
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
                            .append(cbe.replaceAll("\\.", "_"))
                            .append("#id").toString());
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
	 * Make NACEbel ID
	 * 
	 * @param code NACEbel code as string
	 * @return IRI
	 */
	private static IRI makeNACE(String code) {
		return F.createIRI(new StringBuilder(DOM_PREF_NACE)
								.append(code).toString());
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
     * @return http: IRI
     */
    private static IRI asPage(String page) {
        String s = page.toLowerCase().trim().split(" ", 1)[0];
		s = s.replaceFirst("http//", "http://"); // correct malformed input
		s = s.replaceFirst("https//", "https://");
        return F.createIRI(s.startsWith("http") ? s : "http://" + s);
    }
    
    /**
     * Clean up and convert email address to mailto: IRI.
     * 
     * @param mail email address
     * @return mailto: IRI
     */
    private static IRI asMail(String mail) {
        String s = mail.toLowerCase().trim().split(" ", 1)[0];
        return F.createIRI("mailto:" + s);
    }
    
    /*
    private final static Function<String[],Iterable<Statement>> orgSites = row -> {
        List<Statement> arr = new ArrayList<>();
        
        IRI subj = companyID(row[0]);
        IRI reg = companyReg(row[0]);
        Date date = asDate(row[5]);
        arr.add(f.createStatement(subj, RDF.TYPE, ROV_ROG));
        arr.add(f.createStatement(reg, RDF.TYPE, ROV_REG));
        arr.add(f.createStatement(reg, DCTERMS.ISSUED, f.createLiteral(date)));
        
        return arr;
    };
    */
    
    /**
     * Generate stream of organization name triples
     */
    private final static Function<String[],Stream<Statement>> Names = row -> {
        IRI subj = makeID(row[0].startsWith("0") ? PREFIX_ORG : PREFIX_SITE, row[0]);
        String lang = "";
        switch(row[1]) {
            case "1": lang = "fr"; break;
            case "2": lang = "nl"; break;
            case "3": lang = "de"; break;
            case "4": lang = "en";
        }
        IRI pred = row[2].equals("001") ? ROV.LEGAL_NAME : SKOS.ALT_LABEL;
		
		Stream.Builder<Statement> s = Stream.builder();
        s.add(F.createStatement(subj, pred, F.createLiteral(row[3], lang)));
        return s.build();
    };
    
    /**
     * Generate stream of registration records 
     */
    private final static Function<String[],Stream<Statement>> Org = row -> {
        IRI subj = makeID(PREFIX_ORG, row[0]);
        IRI reg = makeID(PREFIX_REG, row[0]);
        Date date = asDate(row[5]);
        
        Stream.Builder<Statement> s = Stream.builder();
        s.add(F.createStatement(subj, RDF.TYPE, ROV.REGISTERED_ORGANIZATION))
            .add(F.createStatement(subj, ROV.REGISTRATION, reg))
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
     * Generate stream of contacts
     */
    private final static Function<String[],Stream<Statement>> Contacts = row -> {
        IRI subj = makeID(row[0].startsWith("0") ? PREFIX_ORG : PREFIX_SITE, row[0]);
        IRI type = null;
        IRI contact = null;
        
        switch(row[2]) {
            case "TEL": type = FOAF.PHONE; contact = asPhone(row[3]); break;
            case "WEB": type = FOAF.HOMEPAGE; contact = asPage(row[3]); break;
            case "EMAIL": type = FOAF.MBOX; contact = asMail(row[3]); break;
        }
        Statement s = F.createStatement(subj, type, contact);
        return Stream.of(s);
    };
   
    /**
     * Generate stream of activities
     */
    private final static Function<String[],Stream<Statement>> Activities = row -> {
        IRI subj = makeID(row[0].startsWith("0") ? PREFIX_ORG : PREFIX_SITE, row[0]);
        
        Statement s = F.createStatement(subj, ROV.ORG_ACTIVITY, makeNACE(row[3]));
        return Stream.of(s);
    };
   
    /**
     * Map files to the functions generating RDF triples.
     */
    private final static HashMap<String,Function> MAP = new HashMap<String,Function>(){{
        put("enterprise.csv", Org);
        put("denomination.csv", Names);
        put("establishment.csv", Sites);
        put("contact.csv", Contacts);
        put("activity.csv", Activities);
    }};
            
    /**
     * Generate RDF triples from CSV file, reading 10000 lines at once
     * 
     * @param rdf RDF writer
     * @param csv CSV containing data
     * @param fun function generating RDF triples
     * @throws IOException 
     */
    private static void add(RDFHandler rdf, FileReader csv, 
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
        
        File base = new File(args[0]);
        File outf = new File(args[1], "cbe.nt");
        
        if (args.length > 2 && args[2].startsWith("http")) {
            domain = args[2];
        } else {
            domain = DOM_BELGIF;
        }
		
        LOG.info("--- START ---");
		LOG.info("Params in = {}, out = {}, domain = {}", base, outf, domain);
		
        try (BufferedWriter w = new BufferedWriter(new FileWriter(outf))){
            RDFWriter rdf = Rio.createWriter(RDFFormat.NTRIPLES, w);
            rdf.startRDF();
            for(String file: MAP.keySet()) {
				LOG.info("Reading CSV file {}", file);
                add(rdf, new FileReader(new File(base, file)), MAP.get(file));
            }
            rdf.endRDF();
        }
		LOG.info("--- END ---");
    }
}
