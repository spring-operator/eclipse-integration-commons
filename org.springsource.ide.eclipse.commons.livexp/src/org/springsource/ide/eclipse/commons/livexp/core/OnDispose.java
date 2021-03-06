/*******************************************************************************
 * Copyright (c) 2015 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springsource.ide.eclipse.commons.livexp.core;

import org.springsource.ide.eclipse.commons.livexp.ui.Disposable;

/**
 * A Disposable may implement this interface to allow clients to attach
 * DisposeListeners.
 *
 * @author Kris De Volder
 */
public interface OnDispose extends Disposable {

	public void onDispose(DisposeListener listener);

}
