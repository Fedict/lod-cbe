/*
 * Copyright (c) 2020, Bart Hanssens <bart.hanssens@bosa.fgov.be>
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
package be.belgif.org.dao;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Bart.Hanssens
 */
public class CbeOrganization {
	private String id;
	private String parentId;
	private Map<String,String> names = new HashMap<>(4,1);
	private Map<String,String> abbrevs = new HashMap<>(4,1);
	private String email;
	private String website;
	private List<String> vatActivities = new ArrayList<>();
	private List<String> nssActivities = new ArrayList<>();

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getParentId() {
		return parentId;
	}

	public void setParentId(String id) {
		this.parentId = id;
	}

	public Map<String,String> getNames() {
		return names;
	}

	public void setNames(Map<String,String> names) {
		this.names = names;
	}

	public void setName(String lang, String abbrev) {
		this.names.put(lang, abbrev);
	}

	public Map<String,String> getAbbrevs() {
		return abbrevs;
	}

	public void setAbbrevs(Map<String,String> abbrevs) {
		this.abbrevs = abbrevs;
	}

	public void setAbbrev(String lang, String abbrev) {
		this.abbrevs.put(lang, abbrev);
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getWebsite() {
		return website;
	}

	public void setWebsite(String website) {
		this.website = website;
	}

	public List<String> getVatActivities() {
		return vatActivities;
	}

	public void setVatActivities(List<String> vatActivities) {
		this.vatActivities = vatActivities;
	}

	public void setVatActivity(String vatActivity) {
		this.vatActivities.add(vatActivity);
	}

	public List<String> getNssActivities() {
		return nssActivities;
	}

	public void setNssActivities(List<String> nssActivities) {
		this.nssActivities = nssActivities;
	}

	public void setNssActivity(String nssActivity) {
		this.nssActivities.add(nssActivity);
	}	
}
