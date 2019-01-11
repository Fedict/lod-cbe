/*
 * Copyright (c) 2019, FPS BOSA DG DT
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
package be.fedict.lodtools.cbeproxy;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Stream;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Statement;

import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.vocabulary.DCTERMS;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 *
 * @author Bart Hanssens
 */
public class Fetcher {
	private final static ValueFactory F = SimpleValueFactory.getInstance();
	
	private final static String URI = "http://org.belgif.be/id/cbe/";
	private final static String URI_ORG = URI + "org/";
	
	private final static String CBE = "https://kbopub.economie.fgov.be";
	private final static String LOOKUP = CBE + "/kbopub/toonondernemingps.html?ondernemingsnummer=";
	
	private final static BiFunction<IRI,Element,Stream<Statement>> Name = (subj, row) -> {
		Stream.Builder<Statement> s = Stream.builder();
		s.add(F.createStatement(subj, DCTERMS.TITLE, F.createLiteral("")));
		return s.build();
	};
	
	private final static Map<String,BiFunction<IRI,Element,Stream<Statement>>> MAP = new HashMap(){{
		put("Maatschappelijke naam:", Name);
	}};
   
		 
	public static Stream<Statement> fetchOrg(String org) throws IOException {
		String nr = org.replaceAll("_", "");
		Connection conn = Jsoup.connect(CBE + nr).userAgent("LOD-Proxy").timeout(10_000);
		
		Document doc = conn.get();
		Element elMain = doc.getElementById("main");
		
		IRI subj = F.createIRI(URI_ORG + org);
		
		return elMain.select("tr").stream().flatMap(row -> { 
			Elements el = row.getElementsByTag("td");
			String name = el.first().text();
			return MAP.get(name).apply(subj, row);
		});
	}
}
