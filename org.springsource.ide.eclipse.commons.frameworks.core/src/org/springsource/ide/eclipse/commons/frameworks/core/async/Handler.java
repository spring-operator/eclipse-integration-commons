package org.springsource.ide.eclipse.commons.frameworks.core.async;

public interface Handler<A, R> {
	Promise<R> call(A a) throws Exception;
}
