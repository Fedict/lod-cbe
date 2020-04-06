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
package be.belgif.org;

import be.belgif.org.dao.CbeOrganization;

import java.util.Map;

import org.attoparser.simple.AbstractSimpleMarkupHandler;


/**
 * Converts HTML page from CBE into RDF
 * 
 * @author Bart Hanssens
 */
public class CbeHtmlParser extends AbstractSimpleMarkupHandler {
	private CbeOrganization org = new CbeOrganization();
	
	private boolean inTableHeader;
	private String tableHeader;
	private String key;

	@Override
	public void handleOpenElement(String elementName, Map<String,String> attributes, int line, int col) {
		if ("td".equals(elementName) && "I".equals(attributes.get("class"))) {
			inTableHeader = true;
		}
	}

	@Override
	public void handleText(char[] buffer, int offset, int len, int line, int col) {
		if (inTableHeader) {
			tableHeader = new String(buffer);
			inTableHeader = false;
		}
	}

	@Override
	public void handleCloseElement(String elementName, int line, int col) {
		if ("tr".equals(elementName)) {
			processRow();
		}
	}
	
	private void processRow() {
		if ("Algemeen".equals(tableHeader)) {
			
		}
	}
}
