/*******************************************************************************
 * Copyright (c) 2011-2013 Jochen Rill.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v2.1
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 * 
 * Contributors:
 *     Jochen Rill - initial API and implementation
 ******************************************************************************/
package sse.IOHandler;

import sse.Backend.Backend;
import sse.IOHandler.OutputFormat.Block;
import sse.IOHandler.OutputFormat.Edge;

/**
 * This class implements a search engine on the encrypted data. For correct
 * work, it needs a correctly implemented backend.
 * 
 * @author Jochen Rill
 * 
 */
public class SearchEngine {
	private int files = 0;
	private Backend backend;

	public SearchEngine(Backend backend) {

		this.backend = backend;

	}

	public int getTransferedFilesCount() {
		return files;
	}

	public long find(String word) {
		// load start block
		Block b = backend.openBlock(0, 0);
		boolean found = false;

		while (word.length() != 0) {
			found = false;
			for (Edge e : b.getEdges()) {
				if (e.getLabel() == word.charAt(0)) {

					b = backend.openBlock(e.getBlockNumber(),
							e.getIndexNumber());

					if (word.length() > 1) {
						word = word.substring(1);
					} else {
						word = "";
					}
					found = true;
					break;
				}
			}
			if (!found) {
				// obviously, there was no matching edge
				break;
			}
		}
		if (found) {
			return b.getNumOccurs();
		} else {
			return 0;
		}
	}

}
