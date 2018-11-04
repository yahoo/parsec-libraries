package com.yahoo.parsec.clients;

import com.google.common.base.Preconditions;
import com.ning.http.client.NameResolver;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * An implementation of {@link NameResolver} that will delegate the operations
 * to a given instance of {@link ParsecNameResolver}.
 */
class DelegateNameResolver implements NameResolver {

    private final ParsecNameResolver delegate;

    /**
     * Constructs a new resolver that will delegate the operations to the given
     * {@link ParsecNameResolver}.
     *
     * @param delegate Delegate.
     */
    public DelegateNameResolver(ParsecNameResolver delegate) {
        Preconditions.checkNotNull(delegate, "Delegate cannot be null");
        this.delegate = delegate;
    }

    @Override
    public InetAddress resolve(String name) throws UnknownHostException {
        return delegate.resolve(name);
    }

    /**
     * Gets the delegated name resolver.
     *
     * @return The delegate.
     */
    public ParsecNameResolver getDelegate() {
        return delegate;
    }
}
