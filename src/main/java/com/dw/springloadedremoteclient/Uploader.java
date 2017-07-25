package com.dw.springloadedremoteclient;

import java.io.File;

/**
 * Uploads file to given URL using HTTP PUT. See API doc for more detail.
 * 
 * @author Jaydeep Kumbhani
 *
 */
public class Uploader implements Listener {
  private String url;
  private File baseDir;


  /**
   * 
   * @param url Server end-point where file is to be uploaded using HTTP PUT.
   * @param baseDir A directory from which File is read for upload. Note:: {@link Change} event's
   *        path is starting with '/' though it's not an absolute path. It's a path relative to this
   *        directory.
   */
  public Uploader(String url, File baseDir) {
    this.url = url;
  }

  public void onChange(Change change) {

  }

}
