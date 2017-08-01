package com.dw.springloadedremoteclient;

import com.dw.springloadedremoteclient.Change.Type;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.HttpClientBuilder;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Uploads file to given URL using HTTP PUT. See API doc for more detail.
 * 
 * @author Jaydeep Kumbhani
 *
 */
public class Uploader implements Listener {

  /**
   * Name of the multipart-form parameter which will contain request body.
   */
  private static final String REQ_BODY_PARAM = "request";
  private static final String ENDPOINT = "/spring-loaded";
  private static final String SYSTEM_SEPARATOR_AS_STRING = String.valueOf(File.separatorChar);
  private static final char WINDOWS_SEPARATOR = '\\';
  private static final char UNIX_SEPARATOR = '/';

  private String url;
  private File baseDir;
  private ObjectMapper objectMapper;
  private HttpClient httpClient;

  /**
   * 
   * @param url Server end-point where file is to be uploaded using HTTP PUT.
   * @param baseDir A directory from which File is read for upload. Note:: {@link Change} event's
   *        path is starting with '/' though it's not an absolute path. It's a path relative to this
   *        directory.
   */
  public Uploader(String url, File baseDir) {
    validateConstructorParam(url, baseDir);
    this.url = url + ENDPOINT;
    this.baseDir = baseDir;
    this.httpClient = HttpClientBuilder.create().build();
    this.objectMapper = new ObjectMapper();
  }

  private void validateConstructorParam(String url, File baseDir) {
    if (StringUtils.isBlank(url)) {
      throw new IllegalArgumentException("url: " + url + "  is invalid");
    }
    if (!baseDir.isDirectory()) {
      throw new IllegalStateException("baseDir: " + baseDir + " is not a Directory");
    }
    if (!baseDir.exists()) {
      throw new IllegalStateException("Base Directory: " + baseDir + " is not a exists");
    }
  }

  @Override
  public void onChange(Change change) {
    try {
      validatePath(change.getPath());

      MultipartEntityBuilder builder = MultipartEntityBuilder.create();
      if (change.getType().equals(Type.CREATED) || change.getType().equals(Type.UPDATED)) {
        if (!addFile(builder, change.getPath())) {
          return;
        }
      }

      Request request = new Request();
      request.setType(change.getType());
      request.setPath(separatorsToUnix(change.getPath()));
      request.setFile("file");

      List<Request> requests = new ArrayList<Request>();
      requests.add(request);
      builder.addTextBody(REQ_BODY_PARAM, objectMapper.writeValueAsString(requests));

      HttpPut putRequest = new HttpPut(url);
      putRequest.setEntity(builder.build());

      HttpResponse response = httpClient.execute(putRequest);

      if (response.getStatusLine().getStatusCode() == 204) {
        System.out.println("Path=" + change.getPath() + ", type=" + change.getType()
            + " has been successfully pushed to remote server");
      } else {
        StatusLine statusLine = response.getStatusLine();
        System.err.println("Path=" + change.getPath() + ", type=" + change.getType() + "Failed."
            + "\n Response: \n" + response.getEntity().toString() + "\n Response phrase: \n"
            + statusLine.getReasonPhrase() + "\n Status Code: \n" + statusLine.getStatusCode());
      }
    } catch (Exception e) {
      System.err.println("Path=" + change.getPath() + ", type=" + change.getType() + "Failed.");
      e.printStackTrace();
    }
  }
  
  private String separatorsToUnix(final String path) {
    if (path == null ) {
      return path;
    }
    return path.replace(WINDOWS_SEPARATOR, UNIX_SEPARATOR);
  }


  private boolean addFile(MultipartEntityBuilder builder, String path) {
    String filePath = baseDir.getPath() + path;
    File updatedFile = new File(filePath);

    if (updatedFile.isDirectory()) {
      System.out.format("Changed File: %s is a Directory so nothing to do", updatedFile.getName());
      return false;
    }
    if (!updatedFile.exists()) {
      System.out.println("File not found at path: " + filePath + ".");
      return false;
    }
    builder.addBinaryBody("file", updatedFile, ContentType.MULTIPART_FORM_DATA, "file");
    return true;
  }

  private void validatePath(String path) {
    if (!StringUtils.startsWith(path, SYSTEM_SEPARATOR_AS_STRING)) {
      throw new IllegalArgumentException(
          "path doesn't start with '" + SYSTEM_SEPARATOR_AS_STRING + "'");
    }
  }
}
