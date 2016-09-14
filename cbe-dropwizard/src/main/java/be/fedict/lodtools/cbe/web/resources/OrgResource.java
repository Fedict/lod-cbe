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
import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;

import org.openrdf.model.Model;
import org.openrdf.model.Value;

/**
 *
 * @author Bart.Hanssens
 */
@Path("/cbe/{type: org|site}/{id}")
@Produces({RDFMediaType.JSONLD, RDFMediaType.NTRIPLES, RDFMediaType.TTL})
public class OrgResource extends RdfResource {
	private final static String Q_ORG_FULL = 
			"CONSTRUCT { ?org ?p ?o } "
			+ "WHERE { ?org ?p ?o }";
		
	@GET
	public Model getOrganisation(@PathParam("type") String type, @PathParam("id") String id) {
		Map<String,Value> map = new HashMap();
		map.put("org", asURI("http://org.belgif.be/cbe/" + type + "/" + id + "#id"));
		return prepare(Q_ORG_FULL, map);
	}
	
	public OrgResource(BigdataSailRemoteRepository repo) {
		super(repo);
	}
}
