package com.dw.springloadedremoteclient;

import java.io.File;

/**
 * Initiate start to watching change in path and listener.
 *
 */
public class App {

  public static void main(String[] args) {
    Watcher w = new Watcher(new File("D:\\projects\\dw\\spring-loaded-adapter\\target\\classes"),
        new Listener() {

          public void onChange(Change change) {
            // Note:: Path must be starting with /, and baseDir shouldn't be repeated.
            System.out.format("%s: %s\n", change.getType().toString(), change.getPath());
          }
        });
    w.start();
  }
}
