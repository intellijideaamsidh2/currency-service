package com.amsidh.mvc.common.filter;

import io.micrometer.tracing.TraceContext;
import io.micrometer.tracing.Tracer;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class RequestResponseLoggingFilter extends OncePerRequestFilter {

    private static final String REQUEST_TRACKING_ID_HEADER = "X-Request-Tracking-ID";
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
    private static final int MAX_PAYLOAD_LENGTH = 10000;

    private final Tracer tracer;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        long startTime = System.currentTimeMillis();
        String startTimeFormatted = DATE_FORMAT.format(new Date(startTime));

        ContentCachingRequestWrapper wrappedRequest = new ContentCachingRequestWrapper(request);
        ContentCachingResponseWrapper wrappedResponse = new ContentCachingResponseWrapper(response);

        // Get or generate tracking ID
        String trackingId = getOrGenerateTrackingId(wrappedRequest, wrappedResponse);

        try {
            // Log incoming request
            logIncomingRequest(wrappedRequest, startTimeFormatted, trackingId);

            filterChain.doFilter(wrappedRequest, wrappedResponse);

        } finally {
            long endTime = System.currentTimeMillis();
            long duration = endTime - startTime;

            // Log outgoing response
            logOutgoingResponse(wrappedRequest, wrappedResponse, startTimeFormatted,
                    DATE_FORMAT.format(new Date(endTime)), duration, trackingId);

            wrappedResponse.copyBodyToResponse();
        }
    }

    private String getOrGenerateTrackingId(HttpServletRequest request, HttpServletResponse response) {
        // First check if tracking ID exists in request header
        String trackingId = request.getHeader(REQUEST_TRACKING_ID_HEADER);

        // If not, try to get from tracing context
        if (trackingId == null && tracer != null && tracer.currentTraceContext() != null) {
            TraceContext traceContext = tracer.currentTraceContext().context();
            if (traceContext != null) {
                trackingId = traceContext.traceId();
            }
        }

        // If still null, generate UUID
        if (trackingId == null) {
            trackingId = UUID.randomUUID().toString();
        }

        // Add tracking ID to response header
        response.setHeader(REQUEST_TRACKING_ID_HEADER, trackingId);

        return trackingId;
    }

    private void logIncomingRequest(ContentCachingRequestWrapper request, String startTime, String trackingId) {
        Map<String, Object> requestLog = new LinkedHashMap<>();

        requestLog.put("type", "INCOMING_REQUEST");
        requestLog.put("trackingId", trackingId);
        requestLog.put("startTime", startTime);
        requestLog.put("method", request.getMethod());
        requestLog.put("requestUrl", getFullURL(request));
        requestLog.put("requestUri", request.getRequestURI());
        requestLog.put("queryString", request.getQueryString());

        // Client information
        requestLog.put("clientIp", getClientIpAddress(request));
        requestLog.put("userAgent", request.getHeader("User-Agent"));
        requestLog.put("referer", request.getHeader("Referer"));
        requestLog.put("locale", request.getLocale().toString());
        requestLog.put("deviceName", extractDeviceName(request.getHeader("User-Agent")));

        // Request headers
        requestLog.put("headers", getRequestHeaders(request));

        // Request body
        byte[] content = request.getContentAsByteArray();
        if (content.length > 0) {
            String body = new String(content, StandardCharsets.UTF_8);
            requestLog.put("requestBody", truncateIfNeeded(body));
            requestLog.put("contentLength", content.length);
        }

        requestLog.put("contentType", request.getContentType());
        requestLog.put("characterEncoding", request.getCharacterEncoding());
        requestLog.put("protocol", request.getProtocol());
        requestLog.put("scheme", request.getScheme());
        requestLog.put("serverName", request.getServerName());
        requestLog.put("serverPort", request.getServerPort());
        requestLog.put("contextPath", request.getContextPath());

        log.info("Request Details: {}", requestLog);
    }

    private void logOutgoingResponse(ContentCachingRequestWrapper request, ContentCachingResponseWrapper response,
            String startTime, String endTime, long duration, String trackingId) {
        Map<String, Object> responseLog = new LinkedHashMap<>();

        responseLog.put("type", "OUTGOING_RESPONSE");
        responseLog.put("trackingId", trackingId);
        responseLog.put("startTime", startTime);
        responseLog.put("endTime", endTime);
        responseLog.put("durationMs", duration);
        responseLog.put("method", request.getMethod());
        responseLog.put("requestUrl", getFullURL(request));
        responseLog.put("responseStatus", response.getStatus());
        responseLog.put("responseStatusText", getStatusText(response.getStatus()));

        // Response headers
        responseLog.put("responseHeaders", getResponseHeaders(response));

        // Response body
        byte[] content = response.getContentAsByteArray();
        if (content.length > 0) {
            String body = new String(content, StandardCharsets.UTF_8);
            responseLog.put("responseBody", truncateIfNeeded(body));
            responseLog.put("responseContentLength", content.length);
        }

        responseLog.put("responseContentType", response.getContentType());

        // Performance metrics
        if (duration > 1000) {
            responseLog.put("performanceAlert", "SLOW_REQUEST");
        }

        log.info("Response Details: {}", responseLog);
    }

    private String getClientIpAddress(HttpServletRequest request) {
        String[] headers = {
                "X-Forwarded-For", "X-Real-IP", "Proxy-Client-IP",
                "WL-Proxy-Client-IP", "HTTP_X_FORWARDED_FOR", "HTTP_X_FORWARDED",
                "HTTP_X_CLUSTER_CLIENT_IP", "HTTP_CLIENT_IP", "HTTP_FORWARDED_FOR",
                "HTTP_FORWARDED", "HTTP_VIA", "REMOTE_ADDR"
        };

        for (String header : headers) {
            String ip = request.getHeader(header);
            if (ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip)) {
                return ip.split(",")[0].trim();
            }
        }

        return request.getRemoteAddr();
    }

    private String extractDeviceName(String userAgent) {
        if (userAgent == null)
            return "Unknown";

        userAgent = userAgent.toLowerCase();

        if (userAgent.contains("mobile") || userAgent.contains("android") || userAgent.contains("iphone")) {
            if (userAgent.contains("android"))
                return "Android Device";
            if (userAgent.contains("iphone"))
                return "iPhone";
            if (userAgent.contains("ipad"))
                return "iPad";
            return "Mobile Device";
        }

        if (userAgent.contains("windows"))
            return "Windows PC";
        if (userAgent.contains("macintosh") || userAgent.contains("mac os"))
            return "Mac";
        if (userAgent.contains("linux"))
            return "Linux PC";

        if (userAgent.contains("chrome"))
            return "Chrome Browser";
        if (userAgent.contains("firefox"))
            return "Firefox Browser";
        if (userAgent.contains("safari"))
            return "Safari Browser";
        if (userAgent.contains("edge"))
            return "Edge Browser";

        return "Desktop Browser";
    }

    private Map<String, String> getRequestHeaders(HttpServletRequest request) {
        Map<String, String> headers = new LinkedHashMap<>();
        Enumeration<String> headerNames = request.getHeaderNames();

        while (headerNames.hasMoreElements()) {
            String headerName = headerNames.nextElement();
            String headerValue = request.getHeader(headerName);

            // Mask sensitive headers
            if (isSensitiveHeader(headerName)) {
                headerValue = "***MASKED***";
            }

            headers.put(headerName, headerValue);
        }

        return headers;
    }

    private Map<String, String> getResponseHeaders(HttpServletResponse response) {
        return response.getHeaderNames().stream()
                .collect(Collectors.toMap(
                        headerName -> headerName,
                        response::getHeader,
                        (existing, replacement) -> existing,
                        LinkedHashMap::new));
    }

    private boolean isSensitiveHeader(String headerName) {
        String lowerName = headerName.toLowerCase();
        return lowerName.contains("authorization") ||
                lowerName.contains("password") ||
                lowerName.contains("token") ||
                lowerName.contains("cookie") ||
                lowerName.contains("auth");
    }

    private String getFullURL(HttpServletRequest request) {
        StringBuilder requestURL = new StringBuilder(request.getRequestURL().toString());
        String queryString = request.getQueryString();

        if (queryString != null) {
            requestURL.append('?').append(queryString);
        }

        return requestURL.toString();
    }

    private String truncateIfNeeded(String content) {
        if (content == null)
            return null;
        if (content.length() <= MAX_PAYLOAD_LENGTH)
            return content;
        return content.substring(0, MAX_PAYLOAD_LENGTH) + "... [TRUNCATED]";
    }

    private String getStatusText(int status) {
        switch (status / 100) {
            case 2:
                return "SUCCESS";
            case 3:
                return "REDIRECT";
            case 4:
                return "CLIENT_ERROR";
            case 5:
                return "SERVER_ERROR";
            default:
                return "UNKNOWN";
        }
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        // Skip actuator endpoints and static resources
        return path.startsWith("/actuator") ||
                path.startsWith("/static") ||
                path.startsWith("/css") ||
                path.startsWith("/js") ||
                path.startsWith("/images") ||
                path.endsWith(".ico");
    }
}