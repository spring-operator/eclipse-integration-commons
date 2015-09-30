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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;

import org.eclipse.core.runtime.Assert;

/**
 * A 'Deferred' is essentialy a Promise to compute something at a later time. It implements
 * both the {@link Promise} api and the {@link Resolvable} interfaces.
 * <p>
 * The creator of a deferred will typically use the {@link Resolvable} api to
 * either resolve or reject the promise at some point in the future.
 * Consumers of this result accept a {@link Promise} and can use its API
 * to chain more computation onto it to consume the result when the promise is
 * completed (i.e. resolved or rejected).
 *
 * @author Kris De Volder
 */
public class Deferred<T> implements Resolvable<T>, Promise<T> {

	//TODO: should Promise be a separate/nested object instead of
	// being implemented by the Deferred directly?

	private final Executor executor;

	private List<Runnable> pendingResolve;
	private List<Runnable> pendingReject;

	enum State {
		PENDING,
		RESOLVED,
		REJECTED
	}

	private State state = State.PENDING;
	private Object value = null; //could be resolve value or reject exception, depending in the state.

	public Deferred(Executor executor) {
		this.executor = executor;
	}

	@Override
	public synchronized void then(final Resolvable<T> result) {
		final Deferred<T> completedPromise = this;
		on(State.RESOLVED, new Runnable() {
			public void run() {
				result.resolve(completedPromise.getValue());
			}
		});
		on(State.REJECTED, new Runnable() {
			public void run() {
				result.reject(completedPromise.getException());
			}
		});
	}

	@Override
	public synchronized <R> Promise<R> then(final Handler<T, R> handler) {
		//TODO: potential optimization... avoid creating needless promise if
		//   this promise is already completed.
		final Deferred<R> result = new Deferred<R>(executor);
		final Deferred<T> completedPromise = this;
		on(State.RESOLVED, new Runnable() {
			public void run() {
				call(handler, completedPromise.getValue(), result);
			}
		});
		on(State.REJECTED, new Runnable() {
			public void run() {
				result.reject(completedPromise.getException());
			}
		});
		return result;
	}

	@Override
	public Promise<T> otherwise(final Handler<Exception, T> errorHandler) {
		//TODO: potential optimization... avoid creating needless promise if
		//   this promise is already completed.
		final Deferred<T> result = new Deferred<T>(executor);
		final Deferred<T> completedPromise = this;
		on(State.RESOLVED, new Runnable() {
			public void run() {
				result.resolve(completedPromise.getValue());
			}
		});
		on(State.REJECTED, new Runnable() {
			public void run() {
				call(errorHandler, completedPromise.getException(), result);
			}
		});
		return result;
	}

	@Override
	public synchronized void resolve(T value) {
		resolve(State.RESOLVED, value);
	}

	@Override
	public synchronized void reject(Exception e) {
		resolve(State.REJECTED, e);
	}

	//////////////////////////////////////////////////////

	/**
	 * TRanistion to completed state, does nothing if this promise is already
	 * completed.
	 */
	private void resolve(State resolvedState, Object resolvedValue) {
		if (state==State.PENDING) {
			value = resolvedValue;
			state = resolvedState;
			execute(pending(resolvedState, false));
		}
	}

	/**
	 * Helper to get 'resolved' value of promise. Assumes the promise is (already) resolved.
	 */
	@SuppressWarnings("unchecked")
	private T getValue() {
		Assert.isLegal(this.state==State.RESOLVED);
		return (T)value;
	}

	/**
	 * Helper to get exception value of promise. Assumes the promise is (already) rejected.
	 */
	private Exception getException() {
		Assert.isLegal(this.state==State.REJECTED);
		return (Exception) value;
	}

	/**
	 * Execute a runnable at a later time, when promise enters corresponding
	 * resolvedState.
	 */
	private synchronized void on(State resolveState, Runnable runnable) {
		if (state==State.PENDING) {
			pending(resolveState).add(runnable);
		} else if (state==resolveState) {
			executor.execute(runnable);
		}
	}

	private List<Runnable> pending(State resolveState) {
		return pending(resolveState, true);
	}

	private List<Runnable> pending(State resolveState, boolean create) {
		if (resolveState == State.RESOLVED) {
			if (pendingResolve==null && create) {
				pendingResolve = new ArrayList<Runnable>(3);
			}
			return pendingResolve;
		} else if (resolveState == State.REJECTED ) {
			if (pendingReject==null && create) {
				pendingReject = new ArrayList<Runnable>(3);
			}
			return pendingReject;
		}
		// this code should be unreachable
		throw new IllegalArgumentException("Bug! Unexpected resolveState");
	}

	private void execute(List<Runnable> pending) {
		pendingReject = null;
		pendingResolve = null;
		if (pending!=null) {
			for (Runnable runnable : pending) {
				executor.execute(runnable);
			}
		}
	}

	private <A,R> void call(final Handler<A, R> handler, final A value, final Resolvable<R> result) {
		try {
			Promise<R> deferredResult = handler.call(value);
			deferredResult.then(result);
		} catch (Exception e) {
			result.reject(e);
		}
	}

}
