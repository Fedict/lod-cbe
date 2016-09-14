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

import java.io.IOException;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ws.rs.Produces;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;

import org.openrdf.model.Model;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.Rio;

/**
 * RDF Writer
 * 
 * @author Bart.Hanssens
 */
@Provider
@Produces({RDFMediaType.JSONLD, RDFMediaType.NTRIPLES, RDFMediaType.TTL})
public class RDFMessageBodyWriter implements MessageBodyWriter<Model> {
	@Override
	public boolean isWriteable(Class<?> type, Type generic, Annotation[] antns, MediaType mt) {
		return generic == Model.class;
	}

	@Override
	public long getSize(Model m, Class<?> type, Type generic, Annotation[] antns, MediaType mt) {
		return 0; // ignored by Jersey 2.0 anyway
	}

	@Override
	public void writeTo(Model m, Class<?> type, Type generic, Annotation[] antns, MediaType mt, 
										MultivaluedMap<String, Object> mm, OutputStream out) 
									throws IOException, WebApplicationException {
		RDFFormat fmt;
		switch(mt.toString()) {
			case RDFMediaType.NTRIPLES: fmt = RDFFormat.NTRIPLES; break;
			case RDFMediaType.TTL: fmt = RDFFormat.TURTLE; break;
			default: fmt = RDFFormat.JSONLD; break;
		}
		try {
			Rio.write(m, out, fmt);
		} catch (RDFHandlerException ex) {
			throw new WebApplicationException(ex);
		}
	}
}
