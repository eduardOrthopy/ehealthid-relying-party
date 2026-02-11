package com.oviva.ehealthid.relyingparty.util;

import static org.junit.jupiter.api.Assertions.*;

import java.net.URI;
import org.junit.jupiter.api.Test;

class BaseUriHelperTest {

  @Test
  void buildUri_withSimpleBaseUri() {
    // given
    var baseUri = URI.create("https://example.com");

    // when
    var result = BaseUriHelper.buildUri(baseUri, "auth");

    // then
    assertEquals(URI.create("https://example.com/auth"), result);
  }

  @Test
  void buildUri_withBaseUriWithPath() {
    // given
    var baseUri = URI.create("https://example.com/app_one");

    // when
    var result = BaseUriHelper.buildUri(baseUri, "auth");

    // then
    assertEquals(URI.create("https://example.com/app_one/auth"), result);
  }

  @Test
  void buildUri_withMultipleSegments() {
    // given
    var baseUri = URI.create("https://example.com/app_one");

    // when
    var result = BaseUriHelper.buildUri(baseUri, "auth", "callback");

    // then
    assertEquals(URI.create("https://example.com/app_one/auth/callback"), result);
  }

  @Test
  void buildUri_withTrailingSlashInBaseUri() {
    // given
    var baseUri = URI.create("https://example.com/app_one/");

    // when
    var result = BaseUriHelper.buildUri(baseUri, "auth");

    // then
    assertEquals(URI.create("https://example.com/app_one/auth"), result);
  }

  @Test
  void buildUri_withNoPathSegments() {
    // given
    var baseUri = URI.create("https://example.com/app_one");

    // when
    var result = BaseUriHelper.buildUri(baseUri);

    // then
    assertEquals(URI.create("https://example.com/app_one"), result);
  }

  @Test
  void buildUri_withEmptyPathSegment() {
    // given
    var baseUri = URI.create("https://example.com/app_one");

    // when
    var result = BaseUriHelper.buildUri(baseUri, "auth", "", "callback");

    // then
    assertEquals(URI.create("https://example.com/app_one/auth/callback"), result);
  }

  @Test
  void buildUri_withNullPathSegment() {
    // given
    var baseUri = URI.create("https://example.com/app_one");

    // when
    var result = BaseUriHelper.buildUri(baseUri, "auth", null, "callback");

    // then
    assertEquals(URI.create("https://example.com/app_one/auth/callback"), result);
  }

  @Test
  void buildUri_withNullBaseUri_throwsException() {
    // when / then
    assertThrows(IllegalArgumentException.class, () -> BaseUriHelper.buildUri(null, "auth"));
  }

  @Test
  void buildUriString_withBaseUriWithPath() {
    // given
    var baseUri = URI.create("https://example.com/app_one");

    // when
    var result = BaseUriHelper.buildUriString(baseUri, "auth");

    // then
    assertEquals("https://example.com/app_one/auth", result);
  }

  @Test
  void buildUriString_withMultipleSegments() {
    // given
    var baseUri = URI.create("https://example.com/app_one");

    // when
    var result = BaseUriHelper.buildUriString(baseUri, "auth", "token");

    // then
    assertEquals("https://example.com/app_one/auth/token", result);
  }

  @Test
  void buildUri_preservesSchemeHostAndPort() {
    // given
    var baseUri = URI.create("https://example.com:8443/app_one");

    // when
    var result = BaseUriHelper.buildUri(baseUri, "auth", "callback");

    // then
    assertEquals(URI.create("https://example.com:8443/app_one/auth/callback"), result);
  }

  @Test
  void buildUri_withRootPath() {
    // given - base URI is just the root
    var baseUri = URI.create("https://example.com/");

    // when
    var result = BaseUriHelper.buildUri(baseUri, "auth");

    // then
    assertEquals(URI.create("https://example.com/auth"), result);
  }

  @Test
  void buildUri_withDeepPath() {
    // given
    var baseUri = URI.create("https://example.com/one/two/three");

    // when
    var result = BaseUriHelper.buildUri(baseUri, "auth", "callback");

    // then
    assertEquals(URI.create("https://example.com/one/two/three/auth/callback"), result);
  }
}
