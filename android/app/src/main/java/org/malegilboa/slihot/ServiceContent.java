package org.malegilboa.slihot;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.List;

/**
 * Supplies the Selichot Markdown from the device.
 *
 * Reading never requires a network connection: a previously downloaded copy is
 * used when present, otherwise the copy bundled in the APK. {@link #refresh}
 * updates the cache with a conditional request, following the same
 * stale-while-revalidate pattern as the website's service worker.
 *
 * Each cached file stores the HTTP {@code ETag} and {@code Last-Modified}
 * validators returned with it. {@link #refresh} sends them back so an unchanged
 * file answers {@code 304 Not Modified} instead of re-downloading the body.
 */
public final class ServiceContent {
  /** Reads a service file by name, without the {@code .md} extension. */
  public interface ContentSource {
    String read(String name) throws IOException;
  }

  /** Fetches a file, optionally conditioning the request on stored validators. */
  public interface RemoteSource {
    Response read(String name, String etag, String lastModified) throws IOException;
  }

  /** The outcome of a remote read: either a new body or a not-modified answer. */
  public static final class Response {
    final String body;
    final String etag;
    final String lastModified;
    final boolean notModified;

    private Response(String body, String etag, String lastModified, boolean notModified) {
      this.body = body;
      this.etag = etag;
      this.lastModified = lastModified;
      this.notModified = notModified;
    }

    static Response modified(String body, String etag, String lastModified) {
      return new Response(body, etag, lastModified, false);
    }

    static Response notModified(String etag, String lastModified) {
      return new Response(null, etag, lastModified, true);
    }
  }

  private final ContentSource bundled;
  private final RemoteSource remote;
  private final File cacheDirectory;

  public ServiceContent(ContentSource bundled, RemoteSource remote, File cacheDirectory) {
    this.bundled = bundled;
    this.remote = remote;
    this.cacheDirectory = cacheDirectory;
  }

  /** Local content: the cached download when available, otherwise the bundled copy. */
  public String read(String name) throws IOException {
    String cached = readFile(cacheFile(name));
    return cached != null ? cached : bundled.read(name);
  }

  /**
   * Downloads the latest copy with a conditional request and updates the cache.
   *
   * @return {@code true} when the downloaded copy differs from what {@link #read}
   *   would return, so the caller should render the content again.
   */
  public boolean refresh(String name) throws IOException {
    String[] stored = readValidators(name);
    String etag = stored[0];
    String lastModified = stored[1];
    Response response = remote.read(name, etag, lastModified);
    if (response.notModified) return false;

    String latest = response.body;
    String existing = readFile(cacheFile(name));
    String current = existing != null ? existing : bundled.read(name);
    store(name, latest, response.etag != null ? response.etag : etag,
        response.lastModified != null ? response.lastModified : lastModified);
    return !latest.equals(current);
  }

  private File cacheFile(String name) {
    return new File(cacheDirectory, name + ".md");
  }

  private File validatorFile(String name) {
    return new File(cacheDirectory, name + ".md.meta");
  }

  private void store(String name, String content, String etag, String lastModified) throws IOException {
    if (!cacheDirectory.isDirectory() && !cacheDirectory.mkdirs()) {
      throw new IOException("Cannot create cache directory: " + cacheDirectory);
    }
    writeAtomically(cacheFile(name), content);
    writeAtomically(validatorFile(name),
        (etag == null ? "" : etag) + "\n" + (lastModified == null ? "" : lastModified) + "\n");
  }

  private void writeAtomically(File target, String content) throws IOException {
    File temporary = new File(cacheDirectory, target.getName() + ".tmp");
    Files.write(temporary.toPath(), content.getBytes(StandardCharsets.UTF_8));
    try {
      Files.move(temporary.toPath(), target.toPath(), StandardCopyOption.REPLACE_EXISTING);
    } catch (IOException error) {
      Files.deleteIfExists(temporary.toPath());
      throw error;
    }
  }

  /** The stored {@code [ETag, Last-Modified]} pair, with {@code null} for missing entries. */
  private String[] readValidators(String name) {
    File file = validatorFile(name);
    if (!file.isFile()) return new String[] {null, null};
    try {
      List<String> lines = Files.readAllLines(file.toPath(), StandardCharsets.UTF_8);
      return new String[] {validator(lines, 0), validator(lines, 1)};
    } catch (IOException unreadable) {
      return new String[] {null, null};
    }
  }

  private static String validator(List<String> lines, int index) {
    if (index >= lines.size()) return null;
    String value = lines.get(index);
    return value.isEmpty() ? null : value;
  }

  /** The file's text, or {@code null} when it is missing or unreadable. */
  private static String readFile(File file) {
    if (!file.isFile()) return null;
    try {
      return new String(Files.readAllBytes(file.toPath()), StandardCharsets.UTF_8);
    } catch (IOException unreadable) {
      return null;
    }
  }
}
