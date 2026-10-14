package org.malegilboa.slihot;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public final class ServiceContentTest {
  private static final String NAME = "התחלה";

  @Rule public final TemporaryFolder cache = new TemporaryFolder();

  private final Map<String, String> bundled = new HashMap<>();
  private final Map<String, String> remote = new HashMap<>();
  private final Map<String, String> remoteEtag = new HashMap<>();
  private int downloads;
  private String lastEtagSent;
  private String lastModifiedSent;

  private ServiceContent content() {
    return new ServiceContent(this::readBundled, this::readRemote, cache.getRoot());
  }

  @Test public void readsBundledCopyBeforeAnyDownload() throws Exception {
    bundled.put(NAME, "bundled");
    assertEquals("bundled", content().read(NAME));
  }

  @Test public void refreshCachesRemoteCopyForOfflineReads() throws Exception {
    bundled.put(NAME, "bundled");
    putRemote(NAME, "updated", "\"v1\"");
    ServiceContent content = content();

    assertTrue(content.refresh(NAME));

    remote.clear();
    assertEquals("updated", content.read(NAME));
  }

  @Test public void cachedDownloadWinsOverBundledCopy() throws Exception {
    bundled.put(NAME, "bundled");
    putRemote(NAME, "downloaded", "\"v1\"");
    ServiceContent content = content();
    content.refresh(NAME);

    bundled.put(NAME, "newer-in-apk");
    assertEquals("downloaded", content.read(NAME));
  }

  @Test public void refreshReportsNoChangeWhenRemoteMatchesBundled() throws Exception {
    bundled.put(NAME, "same");
    putRemote(NAME, "same", "\"v1\"");
    assertFalse(content().refresh(NAME));
  }

  @Test public void refreshReportsChangeOnlyWhenRemoteDiffers() throws Exception {
    bundled.put(NAME, "v1");
    putRemote(NAME, "v1", "\"v1\"");
    ServiceContent content = content();
    assertFalse(content.refresh(NAME));

    putRemote(NAME, "v2", "\"v2\"");
    assertTrue(content.refresh(NAME));
    assertEquals("v2", content.read(NAME));
  }

  @Test public void refreshSendsStoredValidatorAndSkipsUnchangedBody() throws Exception {
    bundled.put(NAME, "bundled");
    putRemote(NAME, "updated", "\"v1\"");
    ServiceContent content = content();
    assertTrue(content.refresh(NAME));
    assertEquals(1, downloads);

    assertFalse(content.refresh(NAME));
    assertEquals("\"v1\"", lastEtagSent);
    assertEquals(1, downloads);
  }

  @Test public void refreshSendsStoredLastModifiedValidator() throws Exception {
    bundled.put(NAME, "bundled");
    remote.put(NAME, "updated");
    ServiceContent content = content();
    content.refresh(NAME);

    assertFalse(content.refresh(NAME));
    assertNull(lastEtagSent);
    assertEquals("LM:updated", lastModifiedSent);
  }

  @Test public void refreshFailureKeepsTheCachedCopy() throws Exception {
    bundled.put(NAME, "bundled");
    putRemote(NAME, "updated", "\"v1\"");
    ServiceContent content = content();
    content.refresh(NAME);

    remote.clear();
    try {
      content.refresh(NAME);
      fail("Expected the missing remote copy to fail");
    } catch (IOException expected) {
      // The device is offline; the cached copy must survive.
    }
    assertEquals("updated", content.read(NAME));
  }

  private void putRemote(String name, String body, String etag) {
    remote.put(name, body);
    remoteEtag.put(name, etag);
  }

  private String readBundled(String name) throws IOException {
    return read(bundled, name);
  }

  private ServiceContent.Response readRemote(String name, String etag, String lastModified) throws IOException {
    String body = read(remote, name);
    lastEtagSent = etag;
    lastModifiedSent = lastModified;
    String currentEtag = remoteEtag.get(name);
    String currentLastModified = "LM:" + body;
    if ((etag != null && etag.equals(currentEtag))
        || (lastModified != null && lastModified.equals(currentLastModified))) {
      return ServiceContent.Response.notModified(currentEtag, currentLastModified);
    }
    downloads += 1;
    return ServiceContent.Response.modified(body, currentEtag, currentLastModified);
  }

  private static String read(Map<String, String> source, String name) throws IOException {
    String value = source.get(name);
    if (value == null) throw new IOException("offline: " + name);
    return value;
  }
}
