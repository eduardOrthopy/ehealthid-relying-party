package com.oviva.ehealthid.relyingparty.util;

import edu.umd.cs.findbugs.annotations.NonNull;
import java.net.URI;
import java.util.Objects;

/**
 * Helper class for normalizing and resolving URIs with path-aware base URI support.
 *
 * <p>This helper ensures that base URIs like {@code https://host/app} properly preserve their path
 * segments when resolving relative paths, while maintaining backwards compatibility with host-only
 * URIs like {@code https://host}.
 *
 * <p>Key behaviors:
 *
 * <ul>
 *   <li>Trailing slashes are normalized (removed for consistency)
 *   <li>Path segments are preserved during resolution
 *   <li>{@link #resolve(String)} handles relative paths correctly with {@link URI#resolve}
 *   <li>{@link #path(String)} combines base path with additional segments
 * </ul>
 */
public class BaseUriHelper {

  private final URI baseUri;
  private final String basePath;

  /**
   * Creates a new BaseUriHelper with the given base URI.
   *
   * @param baseUri the base URI to normalize and use for resolution
   * @throws IllegalArgumentException if baseUri is null or has an ambiguous/invalid structure
   */
  public BaseUriHelper(@NonNull URI baseUri) {
    Objects.requireNonNull(baseUri, "baseUri must not be null");

    if (baseUri.getScheme() == null || baseUri.getHost() == null) {
      throw new IllegalArgumentException("baseUri must have a scheme and host: " + baseUri);
    }

    // Normalize: ensure the URI ends with a trailing slash for proper resolution
    // but store without it for cleaner string representation
    String uriString = baseUri.toString();
    if (uriString.endsWith("/")) {
      this.baseUri = baseUri;
      uriString = uriString.substring(0, uriString.length() - 1);
    } else {
      // Add trailing slash for resolution purposes
      this.baseUri = URI.create(uriString + "/");
    }

    // Extract and normalize the base path
    String path = baseUri.getPath();
    if (path == null || path.isEmpty() || path.equals("/")) {
      this.basePath = "";
    } else {
      // Remove trailing slash if present
      this.basePath = path.endsWith("/") ? path.substring(0, path.length() - 1) : path;
    }
  }

  /**
   * Resolves a relative path against the base URI.
   *
   * <p>This method uses {@link URI#resolve(String)} semantics, which means:
   *
   * <ul>
   *   <li>Relative paths starting with "/" are resolved from the base URI's root
   *   <li>Relative paths without "/" are resolved relative to the base path
   * </ul>
   *
   * @param relativePath the relative path to resolve (e.g., "/auth", "callback")
   * @return the fully resolved URI
   */
  @NonNull
  public URI resolve(@NonNull String relativePath) {
    Objects.requireNonNull(relativePath, "relativePath must not be null");

    // URI.resolve() expects the base to have a trailing slash to preserve the path
    // Our baseUri already has one from the constructor
    return baseUri.resolve(relativePath);
  }

  /**
   * Combines the base path with an additional path segment.
   *
   * <p>This is useful for constructing paths like cookie paths where you need the base path plus an
   * additional segment (e.g., base="/app", extra="/auth" → "/app/auth").
   *
   * @param extraPath the additional path segment to append
   * @return the combined path
   */
  @NonNull
  public String path(@NonNull String extraPath) {
    Objects.requireNonNull(extraPath, "extraPath must not be null");

    if (extraPath.isEmpty()) {
      return basePath.isEmpty() ? "/" : basePath;
    }

    // Ensure extraPath starts with /
    String normalizedExtra = extraPath.startsWith("/") ? extraPath : "/" + extraPath;

    if (basePath.isEmpty()) {
      return normalizedExtra;
    }

    return basePath + normalizedExtra;
  }

  /**
   * Returns the normalized base URI as a string (without trailing slash).
   *
   * @return the base URI string
   */
  @NonNull
  public String toString() {
    String str = baseUri.toString();
    // Remove trailing slash for string representation
    return str.endsWith("/") ? str.substring(0, str.length() - 1) : str;
  }

  /**
   * Returns the base URI object (with trailing slash for resolution).
   *
   * @return the base URI
   */
  @NonNull
  public URI getBaseUri() {
    // Return without trailing slash for external use
    return URI.create(toString());
  }

  /**
   * Returns the base path component (without trailing slash).
   *
   * @return the base path, or empty string if there is no path
   */
  @NonNull
  public String getBasePath() {
    return basePath;
  }
}
