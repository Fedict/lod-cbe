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
package be.fedict.lodtools.cbe.web;

import be.fedict.lodtools.cbe.web.health.RdfStoreHealthCheck;
import be.fedict.lodtools.cbe.web.helpers.RDFManagedRepositories;
import be.fedict.lodtools.cbe.web.helpers.RDFMessageBodyWriter;
import be.fedict.lodtools.cbe.web.resources.CpsvResource;
import be.fedict.lodtools.cbe.web.resources.GeoResource;
import be.fedict.lodtools.cbe.web.resources.OrgResource;
import be.fedict.lodtools.cbe.web.resources.VocabResource;
import com.bigdata.rdf.sail.remote.BigdataSailRemoteRepository;

import com.bigdata.rdf.sail.webapp.client.RemoteRepositoryManager;

import io.dropwizard.Application;
import io.dropwizard.setup.Environment;
import java.net.URL;
import org.openrdf.repository.Repository;


/**
 * Main Dropwizard web application
 * 
 * @author Bart.Hanssens
 */
public class App extends Application<AppConfig> {
	@Override
	public String getName() {
		return "lod-cbe";
	}
	
	@Override
    public void run(AppConfig config, Environment env) {
		
		// RDF Serialization formats
		env.jersey().register(new RDFMessageBodyWriter());
		
		// Managed resource
		String endpoint = config.getSparqlPoint();
		final RemoteRepositoryManager mgr = new RemoteRepositoryManager(endpoint);
		RDFManagedRepositories blaze = new RDFManagedRepositories(mgr);
		Repository repo = blaze.getRepo("cbe");
		env.lifecycle().manage(blaze);
		
		// Monitoring
		final RdfStoreHealthCheck check = new RdfStoreHealthCheck(repo);
		env.healthChecks().register("triplesstore", check);

		
		env.jersey().register(new OrgResource(blaze.getRepo("cbe")));
		env.jersey().register(new GeoResource(blaze.getRepo("geo")));
		env.jersey().register(new VocabResource(blaze.getRepo("vocab")));
		env.jersey().register(new CpsvResource(blaze.getRepo("cpsv")));
	}
	
	/**
	 * Main 
	 * 
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {
		new App().run(args);
	}
}
