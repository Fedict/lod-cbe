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
package be.fedict.lodtools.cbe.full;

import be.fedict.lodtools.cbe.common.CBEConverter;
import be.fedict.lodtools.cbe.common.CsvBulkReader;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;

import java.nio.charset.StandardCharsets;

import java.util.HashMap;
import java.util.function.Function;
import java.util.stream.Stream;

import org.eclipse.rdf4j.model.Statement;

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
	
    private static String domain = null;
	
    /**
     * Map files to the functions generating RDF triples.
     */
    private final static HashMap<String,Function> MAP = new HashMap<String,Function>(){{
        put("enterprise.csv", CBEConverter.Org);
        put("denomination.csv", CBEConverter.Names);
        put("establishment.csv", CBEConverter.Sites);
        put("contact.csv", CBEConverter.Contacts);
        put("activity.csv", CBEConverter.Activities);
		put("address.csv", CBEConverter.Addresses);
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
     * Main
     * 
     * @param args
     * @throws IOException 
     */
    public static void main(String[] args) throws IOException {
        if (args.length < 2) {
            System.out.println("Usage: cbe <input_dir> <output_dir>");
            System.exit(-1);
        }
        
        File base = new File(args[0]);
        File outf = new File(args[1], "cbe.nt");
		File outt = new File(args[1], "cbetypes.nt");
		
        LOG.info("--- START ---");
		LOG.info("Params in = {}, out = {}", base);
		
		// companies / organizations
        try (	FileOutputStream fout = new FileOutputStream(outf);
				BufferedWriter w = new BufferedWriter(
								new OutputStreamWriter(fout, StandardCharsets.UTF_8))){
            RDFWriter rdf = Rio.createWriter(RDFFormat.NTRIPLES, w);
            rdf.startRDF();
			
            for(String file: MAP.keySet()) {
				LOG.info("Reading CSV file {}", file);
				InputStream fin = new FileInputStream(new File(base, file));
                add(rdf, new InputStreamReader(fin, StandardCharsets.UTF_8), MAP.get(file));
            }
			
            rdf.endRDF();
        }
		
		// organization types
		try (	FileOutputStream fout = new FileOutputStream(outt);
				BufferedWriter w = new BufferedWriter(
						new OutputStreamWriter(fout, StandardCharsets.UTF_8))){
            RDFWriter rdf = Rio.createWriter(RDFFormat.NTRIPLES, w);
            rdf.startRDF();
            String file = "code.csv";
			LOG.info("Reading CSV file {}", file);
			InputStream fin = new FileInputStream(new File(base, file));
            add(rdf, new InputStreamReader(fin, StandardCharsets.UTF_8), CBEConverter.Codes);
            rdf.endRDF();
        }
		LOG.info("--- END ---");
    }
}
