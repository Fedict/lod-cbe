/*
 * Copyright (c) 2020, FPS BOSA
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
package be.belgif.org;

import be.belgif.org.dao.CbeOrganization;

import java.net.URI;
import java.util.regex.Pattern;
import javax.inject.Inject;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;


import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.rest.client.inject.RestClient;

/**
 * Proxy endpoint, either redirects to HTML page or generates RDF (based on HTTP Accept Header)
 * 
 * @author Bart Hanssens <bart.hanssens@bosa.fgov.be>
 */
@Path("/id/")
public class CbeResource {
	@ConfigProperty(name = "be.belgif.org.redirect.org")
	protected String REDIRECT_ORG;

	@ConfigProperty(name = "be.belgif.org.redirect.site")
	protected String REDIRECT_SITE;
			
	@Inject
    @RestClient
	CbePublicSearch pubSearch;

	// 9 to 12 numbers, starting with 0, 1 or 2
	private final static Pattern ALL_NUMBER = Pattern.compile("[0-2]\\d{8,11}");
	
	/**
	 * Check if an input parameter could be a valid CBE id
	 * 
	 * @param id 
	 */
	private void verifyId(String id) {
		if (id == null || !ALL_NUMBER.matcher(id).matches()) {
			throw new WebApplicationException("Invalid CBE ID", Status.BAD_REQUEST);
		}
	}

	// Organization
	@GET
	@Path("/cbe/org/{id}")
	@Deprecated(since = "1.5")
	@Produces({"application/n-triples", "application/ld+json"})
	public Response oldOrg(@PathParam("id") String id) {
		id = id.replace("_", "");
		verifyId(id);
		return Response.seeOther(URI.create("/id/CbeRegisteredEntity/" + id)).build();
	}
	@GET
	@Path("/cbe/org/{id}")
	@Produces("text/html")
	@Deprecated(since = "1.5")
	public Response oldOrgRedirect(@PathParam("id") String id) {
		id = id.replace("_", "");
		verifyId(id);
		return Response.seeOther(URI.create(REDIRECT_ORG + id)).build();
	}
	
	@GET
	@Path("/CbeRegisteredEntity/{id}")
	@Produces({"application/n-triples", "application/ld+json"})
	public CbeOrganization org(@PathParam("id") String id) {
		verifyId(id);
		return pubSearch.getOrgById(id);
	}
	@GET
	@Path("/CbeRegisteredEntity/{id}")
	@Produces("text/html")
	public Response orgRedirect(@PathParam("id") String id) {
		verifyId(id);
		return Response.seeOther(URI.create(REDIRECT_ORG + id)).build();
	}

	// Buildings / establishments
	@GET
	@Path("/cbe/site/{id}")
	@Produces({"application/n-triples", "application/ld+json"})
	@Deprecated(since = "1.5")
	public Response oldSite(@PathParam("id") String id) {
		id = id.replace("_", "");
		verifyId(id);
		return Response.seeOther(URI.create("/id/CbeEstablishmentUnit/" + id)).build();
	}
	@GET
	@Path("/cbe/site/{id}")
	@Produces("text/html")
	@Deprecated(since = "1.5")
	public Response oldSiteRedirect(@PathParam("id") String id) {
		id = id.replace("_", "");
		verifyId(id);
		return Response.seeOther(URI.create(REDIRECT_SITE + id)).build();
	}
	
	@GET
	@Path("/CbeEstablishmentUnit/{id}")
	@Produces({"application/n-triples", "application/ld+json"})
	public CbeOrganization site(@PathParam("id") String id) {
		verifyId(id);
		return pubSearch.getSiteById(id);
	}
	@GET
	@Path("/CbeEstablishmentUnit/{id}")
	@Produces("text/html")
	public Response siteRedirect(@PathParam("id") String id) {
		verifyId(id);
		return Response.seeOther(URI.create(REDIRECT_SITE + id)).build();
	}
}
