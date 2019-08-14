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
package be.fedict.lodtools.cbe.common;

import com.opencsv.CSVReader;

import java.io.Reader;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Small helper class.
 *
 * @author Bart Hanssens <bart.hanssens@fedict.be>
 */
public class CsvBulkReader extends CSVReader {

	private final Iterator<String[]> iter;

	/**
	 * Check if there are more rows
	 *
	 * @return true if there are more rows
	 */
	public boolean hasNext() {
		return iter.hasNext();
	}

	/**
	 * Reads a number of lines.
	 *
	 * @param lines maximum number of lines to be read
	 * @return list of string arrays
	 */
	public List<String[]> readNext(int lines) {
		List<String[]> arr = new ArrayList<>();

		int nr = 0;
		while (iter.hasNext() && nr < lines) {
			arr.add(iter.next());
			nr++;
		}
		return arr;
	}

	/**
	 * Constructor
	 *
	 * @param reader
	 */
	public CsvBulkReader(Reader reader) {
		super(reader, ',', '\"', 1);
		iter = this.iterator();
	}
}
