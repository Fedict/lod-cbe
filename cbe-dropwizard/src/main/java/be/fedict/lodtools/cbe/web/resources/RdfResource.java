/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package be.fedict.lodtools.cbe.web.resources;

import be.fedict.lodtools.cbe.web.helpers.RDFMediaType;
import com.bigdata.rdf.sail.remote.BigdataSailRemoteRepository;
import com.bigdata.rdf.sail.remote.BigdataSailRemoteRepositoryConnection;

import java.util.Map;

import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;

import org.openrdf.model.Literal;
import org.openrdf.model.Model;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.query.GraphQuery;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.QueryResults;
import org.openrdf.repository.RepositoryException;


/**
 *
 * @author Bart.Hanssens
 */

@Produces({RDFMediaType.JSONLD, RDFMediaType.NTRIPLES})
public abstract class RdfResource {
	private final BigdataSailRemoteRepository repo;
	private final ValueFactory fac;
	
	protected URI asURI(String uri) {
		return fac.createURI(uri);
	}
	
	protected Literal asLiteral(String lit) {
		return fac.createLiteral(lit);
	}
	
	protected Model prepare(String qry, Map<String,Value> bindings) {
		try {
			BigdataSailRemoteRepositoryConnection conn = this.repo.getConnection();
			GraphQuery gq = conn.prepareGraphQuery(QueryLanguage.SPARQL, qry);
			bindings.forEach((k,v) -> gq.setBinding(k, v));
			return QueryResults.asModel(gq.evaluate());
		} catch (RepositoryException|MalformedQueryException|QueryEvaluationException e) {
			throw new WebApplicationException(e);
		}
	}
				
/*
	protected Response makeResponse(GraphQueryResult res, String mime) {
		Model m = null;
		try {
			m = QueryResults.asModel(res);
		} catch (QueryEvaluationException ex) {
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
		}
		if (m.isEmpty()) {
			return Response.status(Response.Status.NOT_FOUND).build();
		}
		RDFFormat fmt = Rio.getWriterFormatForMIMEType(mime);
		if (fmt == null) {
			return Response.status(Response.Status.NOT_ACCEPTABLE).build();			
		}
		StreamingOutput stream = new StreamingOutput();
					
		try {

			Rio.write(m, writer, fmt);
		} catch (RDFHandlerException ex) {
			Logger.getLogger(RdfResource.class.getName()).log(Level.SEVERE, null, ex);
		}
	}
	*/
			
	public RdfResource(BigdataSailRemoteRepository repo) {
		this.repo = repo;
		this.fac = repo.getValueFactory();
	}
}

