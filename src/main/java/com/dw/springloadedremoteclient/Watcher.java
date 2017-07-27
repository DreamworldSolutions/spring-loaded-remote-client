package com.dw.springloadedremoteclient;

import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_DELETE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;
import static java.nio.file.StandardWatchEventKinds.OVERFLOW;

import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.WatchEvent;
import java.nio.file.WatchEvent.Kind;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashMap;
import java.util.Map;

/**
 * Continuously watches changes in given directory and notifies {@link Listener} when change is
 * detected.
 * 
 * It can handled following cases: <br>
 * - New file is added to root of the directory or sub-directory. <br>
 * - Existing File is changed at root of the dirctory of any sub-directory. <br>
 * - A directory is deleted with it's content. <br>
 * //TODO: For every file? OR only for deleted directory? <br>
 * - A new directory is added and in that a new file is added.
 * 
 * Notes::<br>
 * - When a new empty directory is added, Change listener isn't invoked.
 * 
 * @author Jaydeep Kumbhani
 *
 */
public class Watcher {

  private WatchService watcher;
  private Map<WatchKey, Path> keys;

  private String baseDir;
  private Listener listener;

  /**
   * 
   * @param baseDir Directory to be watched.
   * @param listener
   * @throws IOException
   */
  public Watcher(String baseDir, Listener listener) {
    this.baseDir = baseDir;
    this.listener = listener;
    try {
      this.watcher = FileSystems.getDefault().newWatchService();
    } catch (IOException ex) {
      ex.printStackTrace();
    }
    this.keys = new HashMap<WatchKey, Path>();

    Path path = Paths.get(baseDir);
    registerAll(path);
  }

  @SuppressWarnings("unchecked")
  static <T> WatchEvent<T> cast(WatchEvent<?> event) {
    return (WatchEvent<T>) event;
  }

  /**
   * Register the given directory, and all its sub-directories, with the WatchService.
   */
  private void registerAll(Path baseDir) {
    String firstChr = baseDir.toString().substring(0, 1);
    if (!firstChr.equals("/")) {
      System.out.format("registerAll() :: Please enter baseDir path Starting with '/'.");
      return;
    }

    try {
      Files.walkFileTree(baseDir, new SimpleFileVisitor<Path>() {
        @Override
        public FileVisitResult preVisitDirectory(Path path, BasicFileAttributes attrs)
            throws IOException {
          register(path);
          return FileVisitResult.CONTINUE;
        }
      });
    } catch (IOException ex) {
      System.out.format("registerAll() :: Error occured while getting resource on path : %s",
          baseDir);
    }
  }

  /**
   * Register the given directory with the WatchService
   */
  private void register(Path baseDir) {
    WatchKey key = null;
    try {
      key = baseDir.register(watcher, ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY);
    } catch (IOException ex) {
      System.out.format("register() :: Error occured while getting resource on path : %s", baseDir);
    }
    Path prev = keys.get(key);
    if (prev == null) {
      System.out.format("register: %s\n", baseDir);
    } else {
      if (!baseDir.equals(prev)) {
        System.out.format("update: %s -> %s\n", prev, baseDir);
      }
    }
    keys.put(key, baseDir);
  }

  public void start() {
    for (;;) {

      // wait for key to be signalled
      WatchKey key;
      try {
        key = watcher.take();
      } catch (InterruptedException ex) {
        return;
      }

      Path dir = keys.get(key);
      if (dir == null) {
        System.err.println("WatchKey not recognized!!");
        continue;
      }

      for (WatchEvent<?> event : key.pollEvents()) {
        Kind<?> kind = event.kind();

        if (kind == OVERFLOW) {
          continue;
        }

        WatchEvent<Path> ev = cast(event);
        Path name = ev.context();
        Path child = dir.resolve(name);

        Change change = new Change();
        change.setPath(child.toString());

        if (StringUtils.equals(kind.toString(), ENTRY_CREATE.toString())) {
          change.setType(Change.Type.CREATED);
        }
        if (StringUtils.equals(kind.toString(), ENTRY_MODIFY.toString())) {
          change.setType(Change.Type.UPDATED);
        }
        if (StringUtils.equals(kind.toString(), ENTRY_DELETE.toString())) {
          change.setType(Change.Type.DELETED);
        }

        System.out.format("%s: %s\n", change.getType().toString(), child);

        // if directory is created, and watching recursively, then register it and its
        // sub-directories
        if ((kind == ENTRY_CREATE)) {
          if (Files.isDirectory(child, LinkOption.NOFOLLOW_LINKS)) {
            registerAll(child);
          }
        }

        // Uploader uploder = new Uploader();
        // uploder.onChange(change);
      }

      // reset key and remove from set if directory no longer accessible
      boolean valid = key.reset();
      if (!valid) {
        keys.remove(key);

        // all directories are inaccessible
        if (keys.isEmpty()) {
          break;
        }
      }
    }
  }
}
