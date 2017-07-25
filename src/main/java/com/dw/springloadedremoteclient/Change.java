package com.dw.springloadedremoteclient;

public class Change {

  private Type type;
  private String path;

  public Type getType() {
    return type;
  }

  public void setType(Type type) {
    this.type = type;
  }

  public String getPath() {
    return path;
  }

  public void setPath(String path) {
    this.path = path;
  }

  public enum Type {
    PUT, DELETED;
  }
}
