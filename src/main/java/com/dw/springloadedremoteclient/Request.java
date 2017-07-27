package com.dw.springloadedremoteclient;

import com.dw.springloadedremoteclient.Change.Type;

public class Request {

  private String file;
  private String path;
  private Type type;

  public String getFile() {
    return file;
  }

  public void setFile(String file) {
    this.file = file;
  }

  public String getPath() {
    return path;
  }

  public void setPath(String path) {
    this.path = path;
  }

  public Type getType() {
    return type;
  }

  public void setType(Type type) {
    this.type = type;
  }
}
