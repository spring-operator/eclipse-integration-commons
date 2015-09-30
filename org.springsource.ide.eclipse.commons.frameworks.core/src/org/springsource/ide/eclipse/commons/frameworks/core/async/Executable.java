/*******************************************************************************
 * Copyright (c) 2015 Pivotal Software, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal Software, Inc. - initial API and implementation
 *******************************************************************************/
package org.springsource.ide.eclipse.commons.frameworks.core.async;

/**
 * A convenience class for creating Handler<Void, Void> by implementing
 * a more intuitive method signature.
 *
 * @author Kris De Volder
 */
public abstract class Executable implements Handler<Void, Void> {

	@Override
	public Promise<Void> call(Void ignore) throws Exception {
		return run();
	}

	protected abstract Promise<Void> run() throws Exception;
}
