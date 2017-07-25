package com.dw.springloadedremoteclient;

/**
 * This interface have onChange method to listen changes which given given by {@link Watcher}
 * component.
 * 
 * @author Jaydeep Kumbhani
 *
 */
public interface Listener {

  /**
   * Whenever change detect in given directory then this method invoke.
   * 
   * @param change where change found in given directory
   */
  public void onChange(Change change);

}
