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
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

/**
 * Proxy endpoint, either redirects to HTML page or generates RDF (based on HTTP Accept Header)
 * 
 * @author Bart Hanssens <bart.hanssens@bosa.fgov.be>
 */
@RestController
@RequestMapping("/id/")
public class CbeController {
	@Value("${be.belgif.org.redirect.org}")
	protected String REDIRECT_ORG;

	@Value("${be.belgif.org.redirect.site}")
	protected String REDIRECT_SITE;
			
	@Autowired
	CbePublicSearch pubSearch;

	// 9 to 12 numbers, starting with 0, 1 or 2
	private final static Pattern ALL_NUMBER = Pattern.compile("[0-2]\\d{8,9}");
	
	/**
	 * Check if an input parameter could be a valid CBE id
	 * 
	 * @param id 
	 */
	private void verifyId(String id) {
		if (id == null || id.isEmpty()) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "CBE ID is null or empty");
		}
		if (!ALL_NUMBER.matcher(id).matches()) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid CBE ID " + id);
		}
	}

	@GetMapping(path="/CbeRegisteredEntity/{id}", produces={"application/n-triples", "application/ld+json"})
	public CbeOrganization org(@PathVariable("id") String id) {
		verifyId(id);
		return pubSearch.getOrgById(id);
	}

	@GetMapping(path="/CbeRegisteredEntity/{id}", produces="text/html")
	public void orgRedirect(@PathVariable("id") String id, HttpServletResponse response) throws IOException {
		verifyId(id);
		response.sendRedirect(REDIRECT_ORG + id);
	}
	
	@GetMapping(path="/CbeEstablishmentUnit/{id}", produces={"application/n-triples", "application/ld+json"})
	public CbeOrganization site(@PathVariable("id") String id) {
		verifyId(id);
		return pubSearch.getSiteById(id);
	}

	@GetMapping(path="/CbeEstablishmentUnit/{id}", produces="text/html")
	public void siteRedirect(@PathVariable("id") String id, HttpServletResponse response) throws IOException {
		verifyId(id);
		response.sendRedirect(REDIRECT_SITE + id);
	}
}
