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
package be.fedict.lodtools.cbe.web.helpers;

import com.bigdata.rdf.sail.remote.BigdataSailRemoteRepository;
import com.bigdata.rdf.sail.webapp.client.RemoteRepositoryManager;

import io.dropwizard.lifecycle.Managed;
import java.util.HashMap;
import java.util.Map;
import org.openrdf.repository.Repository;

/**
 * Turn triple store repositories into managed resource(s).
 * 
 * @author Bart.Hanssens
 */
public class RDFManagedRepositories implements Managed {
	private final RemoteRepositoryManager mgr;
	private final Map<String, Repository> map;
	
	@Override
	public void start() throws Exception {
	}

	@Override
	public void stop() throws Exception {
		for (Repository repo : map.values()) {
			repo.shutDown();
		}
		mgr.close();
	}

	/**
	 * Get a repository from the repository manager
	 * 
	 * @param repo name of the repository
	 * @return 
	 */
	public Repository getRepo(String repo) {
		BigdataSailRemoteRepository remote = (BigdataSailRemoteRepository) map.get(repo);
		if (remote == null) {
			remote = mgr.getRepositoryForNamespace(repo).getBigdataSailRemoteRepository();
			map.put(repo, remote);
		}
		return remote;
	}
	
	/**
	 * Constructor
	 * 
	 * @param mgr repo manager to manage
	 */
	public RDFManagedRepositories(RemoteRepositoryManager mgr) {
		this.map = new HashMap();
		this.mgr = mgr;
	}
}
