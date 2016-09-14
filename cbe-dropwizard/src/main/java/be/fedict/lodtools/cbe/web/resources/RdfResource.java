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
package be.fedict.lodtools.cbe.web.resources;

import be.fedict.lodtools.cbe.web.helpers.RDFMediaType;

import com.bigdata.rdf.sail.remote.BigdataSailRemoteRepository;
import com.bigdata.rdf.sail.remote.BigdataSailRemoteRepositoryConnection;

import java.util.Map;

import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import org.eclipse.rdf4j.model.vocabulary.OWL;
import org.eclipse.rdf4j.model.vocabulary.RDF;

import org.openrdf.model.Literal;
import org.openrdf.model.Model;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.vocabulary.DCTERMS;
import org.openrdf.model.vocabulary.SKOS;

import org.openrdf.query.GraphQuery;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.QueryResults;
import org.openrdf.repository.RepositoryException;


/**
 * Abstract resource querying the RDF triple store.
 * 
 * @author Bart.Hanssens
 */

@Produces({RDFMediaType.JSONLD, RDFMediaType.NTRIPLES})
public abstract class RdfResource {
	private final BigdataSailRemoteRepository repo;
	private final ValueFactory fac;
	
	/**
	 * Get string as URI
	 * 
	 * @param uri
	 * @return URI representation
	 */
	protected URI asURI(String uri) {
		return fac.createURI(uri);
	}
	
	/**
	 * Get string as RDF literal
	 * 
	 * @param lit
	 * @return literal 
	 */
	protected Literal asLiteral(String lit) {
		return fac.createLiteral(lit);
	}
	
	/**
	 * Prepare a SPARQL query
	 * @param qry
	 * @param bindings bindings (if any)
	 * @return 
	 */
	protected Model prepare(String qry, Map<String,Value> bindings) {
		try {
			BigdataSailRemoteRepositoryConnection conn = this.repo.getConnection();
			GraphQuery gq = conn.prepareGraphQuery(QueryLanguage.SPARQL, qry);
			bindings.forEach((k,v) -> gq.setBinding(k, v));
			
			Model m = QueryResults.asModel(gq.evaluate());
			conn.close();
			
			m.setNamespace(DCTERMS.PREFIX, DCTERMS.NAMESPACE);
			m.setNamespace(OWL.PREFIX, OWL.NAMESPACE);
			m.setNamespace(RDF.PREFIX, RDF.NAMESPACE);
			m.setNamespace(SKOS.PREFIX, SKOS.NAMESPACE);
			
			return m;
		} catch (RepositoryException|MalformedQueryException|QueryEvaluationException e) {
			throw new WebApplicationException(e);
		}
	}
	
	/**
	 * Constructor
	 * 
	 * @param repo 
	 */
	public RdfResource(BigdataSailRemoteRepository repo) {
		this.repo = repo;
		this.fac = repo.getValueFactory();
	}
}

