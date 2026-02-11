package com.oviva.ehealthid.relyingparty.util;

import jakarta.ws.rs.core.UriBuilder;
import java.net.URI;

/**
 * Utility class for constructing URIs from a base URI in a path-aware manner.
 *
 * <p>This helper ensures that any existing path segments in the base URI are preserved when
 * constructing derived URIs. For example, if the base URI is "https://example.com/app_one", then
 * constructing "/auth" will result in "https://example.com/app_one/auth" rather than
 * "https://example.com/auth".
 */
public class BaseUriHelper {

  private BaseUriHelper() {
    // Utility class, no instantiation
  }

  /**
   * Constructs a URI by appending path segments to the base URI.
   *
   * @param baseUri the base URI to build upon
   * @param pathSegments the path segments to append (without leading slashes)
   * @return the constructed URI with all path segments properly appended
   */
  public static URI buildUri(URI baseUri, String... pathSegments) {
    if (baseUri == null) {
      throw new IllegalArgumentException("baseUri must not be null");
    }

    var builder = UriBuilder.fromUri(baseUri);
    for (var segment : pathSegments) {
      if (segment != null && !segment.isEmpty()) {
        builder.path(segment);
      }
    }
    return builder.build();
  }

  /**
   * Constructs a URI string by appending path segments to the base URI.
   *
   * @param baseUri the base URI to build upon
   * @param pathSegments the path segments to append (without leading slashes)
   * @return the constructed URI as a string
   */
  public static String buildUriString(URI baseUri, String... pathSegments) {
    return buildUri(baseUri, pathSegments).toString();
  }
}
