package com.six.indexreview.api;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;
import java.util.regex.Pattern;

/** Adds a bounded correlation identifier to every HTTP request and response. */
@Component
public class TraceIdFilter extends OncePerRequestFilter {
    /** Header used for request correlation. */
    public static final String TRACE_ID_HEADER = "X-Trace-Id";
    private static final Pattern UUID_PATTERN = Pattern.compile("[0-9a-fA-F-]{1,64}");

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String supplied = request.getHeader(TRACE_ID_HEADER);
        String traceId = supplied != null && UUID_PATTERN.matcher(supplied).matches()
                ? supplied : UUID.randomUUID().toString();
        try (MDC.MDCCloseable ignored = MDC.putCloseable("traceId", traceId)) {
            response.setHeader(TRACE_ID_HEADER, traceId);
            filterChain.doFilter(request, response);
        }
    }
}
