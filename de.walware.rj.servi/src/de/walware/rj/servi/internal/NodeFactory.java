/*=============================================================================#
 # Copyright (c) 2009-2016 Stephan Wahlbrink (WalWare.de) and others.
 # All rights reserved. This program and the accompanying materials
 # are made available under the terms of the Eclipse Public License v1.0
 # which accompanies this distribution, and is available at
 # http://www.eclipse.org/legal/epl-v10.html
 # 
 # Contributors:
 #     Stephan Wahlbrink - initial API and implementation
 #=============================================================================*/

package de.walware.rj.servi.internal;

import de.walware.rj.RjException;
import de.walware.rj.servi.pool.RServiNodeFactory;


public interface NodeFactory extends RServiNodeFactory {
	
	
	void createNode(NodeHandler poolObj) throws RjException;
	void stopNode(NodeHandler poolObj);
	
}
