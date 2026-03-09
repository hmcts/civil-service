package org.springframework.boot.autoconfigure.http;

import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class HttpMessageConverters implements Iterable<HttpMessageConverter<?>> {

    // TODO(DTSCCI-3888): Remove this class once all transitive dependencies stop importing the
    // pre-Boot-4 HttpMessageConverters type from this package.

    private final List<HttpMessageConverter<?>> converters;

    public HttpMessageConverters(HttpMessageConverter<?>... additionalConverters) {
        this(true, additionalConverters == null ? List.of() : List.of(additionalConverters));
    }

    public HttpMessageConverters(Collection<HttpMessageConverter<?>> converters) {
        this(true, converters);
    }

    public HttpMessageConverters(boolean addDefaultConverters, Collection<HttpMessageConverter<?>> converters) {
        List<HttpMessageConverter<?>> resolvedConverters = new ArrayList<>();
        if (addDefaultConverters) {
            // Mirror Boot's legacy behavior by seeding with framework defaults.
            resolvedConverters.addAll(new RestTemplate().getMessageConverters());
        }
        if (converters != null) {
            resolvedConverters.addAll(converters);
        }
        this.converters = Collections.unmodifiableList(postProcessConverters(resolvedConverters));
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
