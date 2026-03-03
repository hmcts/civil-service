package org.springframework.boot.autoconfigure.http;

import org.springframework.http.converter.HttpMessageConverter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class HttpMessageConverters implements Iterable<HttpMessageConverter<?>> {

    private final List<HttpMessageConverter<?>> converters;

    public HttpMessageConverters(HttpMessageConverter<?>... additionalConverters) {
        this(true, additionalConverters == null ? List.of() : List.of(additionalConverters));
    }

    public HttpMessageConverters(Collection<HttpMessageConverter<?>> converters) {
        this(true, converters);
    }

    public HttpMessageConverters(boolean addDefaultConverters, Collection<HttpMessageConverter<?>> converters) {
        this.converters = Collections.unmodifiableList(postProcessConverters(
            converters == null ? List.of() : new ArrayList<>(converters)
        ));
    }

    protected List<HttpMessageConverter<?>> postProcessConverters(List<HttpMessageConverter<?>> converters) {
        return converters;
    }

    protected List<HttpMessageConverter<?>> postProcessPartConverters(List<HttpMessageConverter<?>> converters) {
        return converters;
    }

    @Override
    public Iterator<HttpMessageConverter<?>> iterator() {
        return converters.iterator();
    }

    public List<HttpMessageConverter<?>> getConverters() {
        return converters;
    }
}
