package com.dw.springloadedremoteclient;

import java.io.File;

/**
 * Initiate start to watching change in path and listener.
 *
 */
public class App {

  /**
   * Main method to execute start to continuously watching on given base directory and based on
   * received event notify to given listener.
   * 
   * @param args first argument is baseDirectory.
   */
  public static void main(String[] args) {
    String path = (args == null || args.length == 0) ? "target/classes" : args[0];
    System.out.println("main() :: Start to watching on baseDir : " + path);
    Watcher watcher = new Watcher(new File(path), new Listener() {

      public void onChange(Change change) {
        // Note:: Path must be starting with /, and baseDir shouldn't be repeated.
        System.out.format("%s: %s\n", change.getType().toString(), change.getPath());
      }
    });
    watcher.start();
  }
}
