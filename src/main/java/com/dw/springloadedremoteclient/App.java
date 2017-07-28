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
    String remoteUrl = (args == null || args.length == 0)
        ? System.getenv("springloadedremoteclient.baseUrl") : args[0];
    String baseDirPath = (args == null || args.length < 2)
        ? System.getenv("springloadedremoteclient.baseDir") : args[1];
    baseDirPath = baseDirPath == null ? "target/classes" : baseDirPath;
    System.out.println(
        "main() :: Start to watching on baseDir : " + baseDirPath + " and remoteUrl: " + remoteUrl);
    
    Watcher watcher =
        new Watcher(new File(baseDirPath), new Uploader(remoteUrl, new File(baseDirPath)));
    watcher.start();
  }
}
