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

	private final static String PREFIX_ORG = "/cbe/org/";
	private final static String PREFIX_REG = "/cbe/registration/";
	private final static String PREFIX_SITE = "/cbe/site/";

	/**
	 * Generate organization names to delete
	 */
	private final static Function<String[], String> Names_del = row -> {
		return CBEConverter.makeID(row[0]).toString();
	};

	/**
	 * Generate registration records to delete
	 */
	private final static Function<String[], String> Org_del = row -> {
		return CBEConverter.makeID(PREFIX_ORG, row[0]).toString();
	};

	/**
	 * Generate site ID to delete
	 */
	private final static Function<String[], String> Sites_del = row -> {
		return CBEConverter.makeID(PREFIX_SITE, row[0]).toString();
	};

	/**
	 * Generate contact ID to delete
	 */
	private final static Function<String[], String> Contacts_del = row -> {
		return CBEConverter.makeID(row[0]).toString();
	};

	/**
	 * Generate activity ID to delete
	 */
	private final static Function<String[], String> Activities_del = row -> {
		return CBEConverter.makeID(row[0]).toString();
	};

	/**
	 * Generate addresses
	 */
	private final static Function<String[], String> Addresses_del = row -> {
		return CBEConverter.makeID(row[0]).toString();
	};

	/**
	 * Map files to the functions generating RDF triples.
	 */
	private final static HashMap<String, Function> MAP_DEL = new HashMap<String, Function>() {
		{
			put("enterprise_delete.csv", Org_del);
			put("denomination_delete.csv", Names_del);
			put("establishment_delete.csv", Sites_del);
			put("contact_delete.csv", Contacts_del);
			put("activity_delete.csv", Activities_del);
			put("address_delete.csv", Addresses_del);
		}
	};

	/**
	 * Map files to the functions generating RDF triples.
	 */
	private final static HashMap<String, Function> MAP_INS = new HashMap<String, Function>() {
		{
			put("enterprise_insert.csv", CBEConverter.Org);
			put("denomination_insert.csv", CBEConverter.Names);
			put("establishment_insert.csv", CBEConverter.Sites);
			put("contact_insert.csv", CBEConverter.Contacts);
			put("activity_insert.csv", CBEConverter.Activities);
			put("address_insert.csv", CBEConverter.Addresses);
		}
	};

	/**
	 * Generate RDF triples from CSV file, reading 10000 lines at once
	 *
	 * @param rdf RDF writer
	 * @param csv CSV containing data
	 * @param fun function generating RDF triples
	 * @throws IOException
	 */
	private static void add(RDFHandler rdf, Reader csv,
		Function<String[], Stream<Statement>> fun) throws IOException {
		int lines = 10000;

		try (CsvBulkReader r = new CsvBulkReader(csv)) {
			while (r.hasNext()) {
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
			w.write("<" + line + ">");
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
		Function<String[], String> fun) throws IOException {
		int lines = 10000;

		try (CsvBulkReader r = new CsvBulkReader(csv)) {
			while (r.hasNext()) {
				r.readNext(lines).stream().map(fun).forEach(l -> writeLine(w, l));
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

		File basedir = new File(args[0]);
		File outdir = new File(args[1]);

		LOG.info("--- START ---");
		LOG.info("Params in = {}, out = {}", basedir, outdir);

		for (String file : MAP_DEL.keySet()) {
			File delfile = new File(outdir, file.replaceAll("_delete", "_id"));
			try (FileOutputStream fout = new FileOutputStream(delfile);
				BufferedWriter w = new BufferedWriter(
					new OutputStreamWriter(fout, StandardCharsets.UTF_8))) {
				LOG.info("Reading CSV file {}, writing {}", file, delfile);
				InputStream fin = new FileInputStream(new File(basedir, file));
				add(w, new InputStreamReader(fin, StandardCharsets.UTF_8), MAP_DEL.get(file));
			} catch (IOException ex) {
			}
		}

		File outf = new File(args[1], "cbe-upd.nt");
		// inserts for companies / organizations
		try (FileOutputStream fout = new FileOutputStream(outf);
			BufferedWriter w = new BufferedWriter(
				new OutputStreamWriter(fout, StandardCharsets.UTF_8))) {
			RDFWriter rdf = Rio.createWriter(RDFFormat.NTRIPLES, w);
			rdf.startRDF();

			for (String file : MAP_INS.keySet()) {
				LOG.info("Reading CSV file {}", file);
				InputStream fin = new FileInputStream(new File(basedir, file));
				add(rdf, new InputStreamReader(fin, StandardCharsets.UTF_8), MAP_INS.get(file));
			}

			rdf.endRDF();
		}

		LOG.info("--- END ---");
	}
}
