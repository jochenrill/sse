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
package sse.Backend;

import java.util.ArrayList;

import sse.IOHandler.OutputFormat.Block;

/**
 * This interface specifies how a backend should be implemented so that
 * SearchEngine and BinaryWriter can use it.
 * 
 * @author Jochen Rill
 * 
 */
public interface Backend {

	public void writeBlock(Block b, int blockId);

	public void writeBlockArray(ArrayList<Block> b, int blockId);

	public Block openBlock(int blockID, int index);

}
