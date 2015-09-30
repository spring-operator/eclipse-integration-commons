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

import java.util.concurrent.Executor;

/**
 * Promises similar to how we know them from JavaScript.
 * <p>
 * Note: These APIs are not currently used and have received very little testing.
 * When/if we start using them we should probably add some decent tests.
 *
 * @author Kris De Volder
 */
public class Promises {

	private Executor executor;

	public Promises(Executor executor) {
		this.executor = executor;
	}

	public Promise<Void> run(final Executable r) {
		final Deferred<Void> p = new Deferred<Void>(executor);
		executor.execute(new Runnable() {
			public void run() {
				try {
					Promise<Void> d = r.run();
					d.then(p);
				} catch (Exception e) {
					p.reject(e);
				}
			}
		});
		return p;
	}

	public static Promises getDefault() {
		return new Promises(JobBasedExecutor.getInstance());
	}

	public <T> Deferred<T> create() {
		return new Deferred<T>(executor);
	}

}
