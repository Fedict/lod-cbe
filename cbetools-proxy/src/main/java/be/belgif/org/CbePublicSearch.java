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
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

/**
 * HTTP client that will perform the search on the CBE website
 * 
 * @see <a "https://kbopub.economie.fgov.be/kbopub/zoeknummerform.html">CBE search</a>
 * @author Bart Hanssens <bart.hanssens@bosa.fgov.be>
 */
@RegisterRestClient
public interface CbePublicSearch {
	@GET
	@Path("/toonondernemingps.html")
	@Produces(MediaType.TEXT_HTML)
	public CbeOrganization getOrgById(@QueryParam("ondernemingsnummer") String id);

	@GET
	@Path("/vestiginglijst.html")
	@Produces(MediaType.TEXT_HTML)
	public String getSiteListById(@QueryParam("ondernemingsnummer") String id);
	
	@GET
	@Path("/toonvestigingps.html")
	@Produces(MediaType.TEXT_HTML)
	public CbeOrganization getSiteById(@QueryParam("vestigingsnummer") String id);
}
