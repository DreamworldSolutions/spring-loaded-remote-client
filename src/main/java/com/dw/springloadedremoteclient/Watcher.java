package com.dw.springloadedremoteclient;

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

  private String baseDir;
  private Listener listener;

  /**
   * 
   * @param baseDir Directory to be watched.
   * @param listener
   */
  public Watcher(String baseDir, Listener listener) {
    this.baseDir = baseDir;
    this.listener = listener;
  }

  public void start() {}

}
