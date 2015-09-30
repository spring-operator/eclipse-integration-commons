package org.springsource.ide.eclipse.commons.frameworks.core.async;

public interface Promise<T> {
	<R> Promise<R> then(Handler<T,R> handler);
	Promise<T> otherwise(Handler<Exception, T> errorHandler);
	void then(Resolvable<T> result);
}
