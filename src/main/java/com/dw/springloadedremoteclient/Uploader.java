package com.dw.springloadedremoteclient;

import com.dw.springloadedremoteclient.Change.Type;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.HttpClientBuilder;

import java.io.File;
import java.io.IOException;

/**
 * Uploads file to given URL using HTTP PUT. See API doc for more detail.
 * 
 * @author Jaydeep Kumbhani
 *
 */
public class Uploader implements Listener {

  private String url;
  private File baseDir;
  private static final String REQUEST = "request";
  private static final String ENDPOINT = "spring-loaded";
  private static final String PATH_START_WITH = "/";

  /**
   * 
   * @param url Server end-point where file is to be uploaded using HTTP PUT.
   * @param baseDir A directory from which File is read for upload. Note:: {@link Change} event's
   *        path is starting with '/' though it's not an absolute path. It's a path relative to this
   *        directory.
   */
  public Uploader(String url, File baseDir) {
    validateConstructorParm(url, baseDir);
    this.url = url + ENDPOINT;
    this.baseDir = baseDir;
  }

  private void validateConstructorParm(String url, File baseDir) {
    if (StringUtils.isBlank(url)) {
      throw new IllegalArgumentException("url is invalid");
    }
    if (StringUtils.isBlank(baseDir.getPath())) {
      throw new IllegalArgumentException("baseDir is invalid");
    }
    if (!baseDir.isDirectory()) {
      throw new IllegalStateException("baseDir is not a Directory");
    }
    if (!baseDir.exists()) {
      throw new IllegalStateException("Base Directory is not a exists");
    }
  }

  public void onChange(Change change) {

    validatePath(change.getPath());

    String filePath = baseDir.getPath() + change.getPath();
    MultipartEntityBuilder builder = MultipartEntityBuilder.create();

    Request request = new Request();
    request.setType(change.getType());
    request.setPath(filePath);

    if (StringUtils.equals(change.getType().toString(), Type.CREATED.toString())
        || StringUtils.equals(change.getType().toString(), Type.UPDATED.toString())) {
      File updatedFile = new File(filePath);

      if (updatedFile.isDirectory()) {
        System.out.format("Changed File: %s is a Directory so nothing to do",
            updatedFile.getName());
        return;
      }
      if (!updatedFile.exists()) {
        throw new IllegalStateException("File at path: " + filePath + " is not exists");
      }
      request.setFile(updatedFile.getName());
      builder.addBinaryBody(updatedFile.getName(), updatedFile);
    }

    builder.addTextBody(REQUEST, getRequestObjectAsString(request));

    HttpPut putRequest = new HttpPut(url);
    putRequest.setEntity(builder.build());
    HttpResponse response = null;
    try {
      response = HttpClientBuilder.create().build().execute(putRequest);
    } catch (ClientProtocolException ex) {
      System.out.format("Eerror occure in HTTP protocol" + ex);
    } catch (IOException ex) {
      System.out
          .format("IOException occure due to some problem or the connection was aborted" + ex);
    }

    printResponse(change, response);
  }

  private void printResponse(Change change, HttpResponse response) {
    int statusCode = response.getStatusLine().getStatusCode();
    if (statusCode == 400 || statusCode == 404) {
      System.out.format("File %s request fail Response: %s.", change.getType().toString(),
          response.getStatusLine().getReasonPhrase());
    }

    if (statusCode == 200) {
      System.out.format("File %s request has successfully send to %s", change.getType().toString(),
          url);
    }
  }

  private String getRequestObjectAsString(Request request) {
    ObjectMapper objectMapper = new ObjectMapper();
    String requestObjectAsString = null;
    try {
      requestObjectAsString = objectMapper.writeValueAsString(request);
    } catch (JsonProcessingException ex) {
      System.out.format("Error occurred while converting Request object to JSON string" + ex);
    }
    return requestObjectAsString;
  }

  private void validatePath(String path) {
    String pathFirstChar = path.substring(0, 1);
    if (!StringUtils.equals(pathFirstChar, PATH_START_WITH)) {
      throw new IllegalArgumentException("Updated File path is invalid");
    }
  }
}
