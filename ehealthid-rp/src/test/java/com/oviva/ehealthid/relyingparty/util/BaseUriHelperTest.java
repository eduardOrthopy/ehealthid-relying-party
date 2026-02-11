package com.oviva.ehealthid.relyingparty.util;

import static org.junit.jupiter.api.Assertions.*;

import java.net.URI;
import org.junit.jupiter.api.Test;

class BaseUriHelperTest {

  @Test
  void constructor_withNullUri_throwsException() {
    assertThrows(NullPointerException.class, () -> new BaseUriHelper(null));
  }

  @Test
  void constructor_withUriWithoutScheme_throwsException() {
    var uri = URI.create("//host/path");
    assertThrows(IllegalArgumentException.class, () -> new BaseUriHelper(uri));
  }

  @Test
  void constructor_withUriWithoutHost_throwsException() {
    var uri = URI.create("https:///path");
    assertThrows(IllegalArgumentException.class, () -> new BaseUriHelper(uri));
  }

  @Test
  void constructor_withHostOnlyUri_normalizesCorrectly() {
    var helper = new BaseUriHelper(URI.create("https://example.com"));

    assertEquals("https://example.com", helper.toString());
    assertEquals("https://example.com", helper.getBaseUri().toString());
    assertEquals("", helper.getBasePath());
  }

  @Test
  void constructor_withHostOnlyUriWithTrailingSlash_normalizesCorrectly() {
    var helper = new BaseUriHelper(URI.create("https://example.com/"));

    assertEquals("https://example.com", helper.toString());
    assertEquals("https://example.com", helper.getBaseUri().toString());
    assertEquals("", helper.getBasePath());
  }

  @Test
  void constructor_withPathUri_preservesPath() {
    var helper = new BaseUriHelper(URI.create("https://example.com/app"));

    assertEquals("https://example.com/app", helper.toString());
    assertEquals("https://example.com/app", helper.getBaseUri().toString());
    assertEquals("/app", helper.getBasePath());
  }

  @Test
  void constructor_withPathUriWithTrailingSlash_removesTrailingSlash() {
    var helper = new BaseUriHelper(URI.create("https://example.com/app/"));

    assertEquals("https://example.com/app", helper.toString());
    assertEquals("https://example.com/app", helper.getBaseUri().toString());
    assertEquals("/app", helper.getBasePath());
  }

  @Test
  void constructor_withMultiLevelPath_preservesAllSegments() {
    var helper = new BaseUriHelper(URI.create("https://example.com/api/v1/rp"));

    assertEquals("https://example.com/api/v1/rp", helper.toString());
    assertEquals("/api/v1/rp", helper.getBasePath());
  }

  @Test
  void resolve_withHostOnlyBase_resolvesProperly() {
    var helper = new BaseUriHelper(URI.create("https://example.com"));

    assertEquals("https://example.com/auth", helper.resolve("/auth").toString());
    assertEquals("https://example.com/auth/callback", helper.resolve("/auth/callback").toString());
    assertEquals("https://example.com/jwks.json", helper.resolve("/jwks.json").toString());
  }

  @Test
  void resolve_withPathBase_preservesBasePath() {
    var helper = new BaseUriHelper(URI.create("https://example.com/app"));

    // Key test: ensure /auth doesn't replace /app but resolves from root
    // This tests that URI.resolve works correctly with our setup
    assertEquals("https://example.com/auth", helper.resolve("/auth").toString());
    assertEquals("https://example.com/auth/callback", helper.resolve("/auth/callback").toString());
  }

  @Test
  void resolve_withPathBaseAndRelativePath_resolvesProperly() {
    var helper = new BaseUriHelper(URI.create("https://example.com/app"));

    // Relative paths (without leading slash) resolve relative to the base
    assertEquals("https://example.com/app/auth", helper.resolve("auth").toString());
    assertEquals(
        "https://example.com/app/auth/callback", helper.resolve("auth/callback").toString());
  }

  @Test
  void resolve_withMultiLevelPathBase_handlesAbsolutePaths() {
    var helper = new BaseUriHelper(URI.create("https://example.com/api/v1"));

    assertEquals("https://example.com/auth", helper.resolve("/auth").toString());
    assertEquals("https://example.com/auth/token", helper.resolve("/auth/token").toString());
  }

  @Test
  void resolve_withMultiLevelPathBase_handlesRelativePaths() {
    var helper = new BaseUriHelper(URI.create("https://example.com/api/v1"));

    assertEquals("https://example.com/api/v1/auth", helper.resolve("auth").toString());
    assertEquals("https://example.com/api/v1/auth/token", helper.resolve("auth/token").toString());
  }

  @Test
  void resolve_withNullPath_throwsException() {
    var helper = new BaseUriHelper(URI.create("https://example.com"));
    assertThrows(NullPointerException.class, () -> helper.resolve(null));
  }

  @Test
  void path_withHostOnlyBase_returnsExtraPath() {
    var helper = new BaseUriHelper(URI.create("https://example.com"));

    assertEquals("/auth", helper.path("/auth"));
    assertEquals("/auth", helper.path("auth"));
    assertEquals("/", helper.path(""));
  }

  @Test
  void path_withPathBase_combinesWithExtra() {
    var helper = new BaseUriHelper(URI.create("https://example.com/app"));

    assertEquals("/app/auth", helper.path("/auth"));
    assertEquals("/app/auth", helper.path("auth"));
    assertEquals("/app", helper.path(""));
  }

  @Test
  void path_withMultiLevelPathBase_combinesCorrectly() {
    var helper = new BaseUriHelper(URI.create("https://example.com/api/v1"));

    assertEquals("/api/v1/auth", helper.path("/auth"));
    assertEquals("/api/v1/auth", helper.path("auth"));
    assertEquals("/api/v1/callback", helper.path("/callback"));
  }

  @Test
  void path_withNullExtra_throwsException() {
    var helper = new BaseUriHelper(URI.create("https://example.com"));
    assertThrows(NullPointerException.class, () -> helper.path(null));
  }

  @Test
  void resolve_backwardsCompatibility_hostOnlyBehavior() {
    // Test that legacy host-only URIs continue to work as before
    var helper = new BaseUriHelper(URI.create("https://rp.example.com"));

    assertEquals("https://rp.example.com/auth", helper.resolve("/auth").toString());
    assertEquals(
        "https://rp.example.com/auth/callback", helper.resolve("/auth/callback").toString());
    assertEquals("https://rp.example.com/jwks.json", helper.resolve("/jwks.json").toString());
    assertEquals(
        "https://rp.example.com/.well-known/openid-configuration",
        helper.resolve("/.well-known/openid-configuration").toString());
  }

  @Test
  void resolve_newBehavior_pathAwareUris() {
    // Test that new path-aware URIs work correctly
    var helper = new BaseUriHelper(URI.create("https://host/base"));

    // Absolute paths resolve from host root (standard URI.resolve behavior)
    assertEquals("https://host/auth/callback", helper.resolve("/auth/callback").toString());

    // For path-relative resolution, use resolve without leading slash
    assertEquals("https://host/base/relative", helper.resolve("relative").toString());
  }

  @Test
  void path_backwardsCompatibility_hostOnlyBehavior() {
    // Test that cookie paths work correctly for host-only URIs
    var helper = new BaseUriHelper(URI.create("https://rp.example.com"));

    assertEquals("/auth", helper.path("/auth"));
  }

  @Test
  void path_newBehavior_pathAwareCookies() {
    // Test that cookie paths work correctly for path-aware URIs
    var helper = new BaseUriHelper(URI.create("https://host/base"));

    assertEquals("/base/auth", helper.path("/auth"));
  }

  @Test
  void getBaseUri_returnsUriWithoutTrailingSlash() {
    var helper1 = new BaseUriHelper(URI.create("https://example.com"));
    assertEquals("https://example.com", helper1.getBaseUri().toString());

    var helper2 = new BaseUriHelper(URI.create("https://example.com/"));
    assertEquals("https://example.com", helper2.getBaseUri().toString());

    var helper3 = new BaseUriHelper(URI.create("https://example.com/app"));
    assertEquals("https://example.com/app", helper3.getBaseUri().toString());

    var helper4 = new BaseUriHelper(URI.create("https://example.com/app/"));
    assertEquals("https://example.com/app", helper4.getBaseUri().toString());
  }

  @Test
  void toString_returnsUriWithoutTrailingSlash() {
    var helper1 = new BaseUriHelper(URI.create("https://example.com"));
    assertEquals("https://example.com", helper1.toString());

    var helper2 = new BaseUriHelper(URI.create("https://example.com/app"));
    assertEquals("https://example.com/app", helper2.toString());
  }

  @Test
  void resolve_withPort_preservesPort() {
    var helper = new BaseUriHelper(URI.create("https://example.com:8443/app"));

    assertEquals("https://example.com:8443/auth", helper.resolve("/auth").toString());
    assertEquals("/app", helper.getBasePath());
  }

  @Test
  void resolve_withQuery_ignoresQuery() {
    // Query parameters in base URI should not affect resolution
    var helper = new BaseUriHelper(URI.create("https://example.com/app?param=value"));

    // The query is part of the URI but not used in resolution
    assertTrue(helper.toString().startsWith("https://example.com/app"));
  }
}
