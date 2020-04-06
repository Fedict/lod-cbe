/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
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
	private Map<String,String> names = new HashMap<>(4,1);
	private Map<String,String> abbrevs = new HashMap<>(4,1);
	private String website;
	private List<String> vatActivities = new ArrayList<>();
	private List<String> nssActivities = new ArrayList<>();

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
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
