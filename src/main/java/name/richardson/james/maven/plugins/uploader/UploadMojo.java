package name.richardson.james.maven.plugins.uploader;

/*
 * Copyright 2001-2005 The Apache Software Foundation.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.http.HttpResponse;
import org.apache.http.ParseException;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.FileEntity;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.model.Dependency;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Goal which uploads the file to BukkitDev.
 * 
 * @goal upload
 */
public class UploadMojo extends AbstractMojo {

  /**
   * The name of the game that we are uploading
   * 
   * @parameter expression="${curseforgeuploader.gameName}"
   *            default-value="minecraft"
   */
  private String game;

  /**
   * The API key for this user
   * 
   * @parameter expression="${curseforgeuploader.apiKey}"
   * @required
   */
  private String key;

  /**
   * Project being built
   * 
   * @parameter expression="${project}
   * @required
   */
  private MavenProject project;

  /**
   * The name of the game that we are uploading
   * 
   * @parameter expression="${curseforgeuploader.projectSlug}"
   *            default-value="${project.name}"
   */
  private String slug;

  /**
   * The name of the game that we are uploading
   * 
   * @parameter expression="${curseforgeuploader.markupType}"
   *            default-value="markdown"
   */
  private String markupType;

  /**
   * The name of the game that we are uploading
   * 
   * @parameter expression="${curseforgeuploader.changeLog}"
   *            default-value=
   *            "Uploaded using CurseForgerUploader. CHANGELOG pending."
   */
  private String changeLog;

  /**
   * The name of the game that we are uploading
   * 
   * @parameter expression="${curseforgeuploader.knownCaveats}"
   *            default-value="None"
   */
  private String knownCaveats;

  /**
   * The name of the game that we are uploading
   * 
   * @parameter expression="${curseforgeuploader.projectType}"
   *            default-value="server-mods"
   */
  private String projectType;

  private String getGameVersion() throws MojoExecutionException {
    final Map<String, String> remoteVersions = this.getRemoteBukkitVersions();
    final String localVersion = this.getLocalBukkitVersion();
    this.getLog().debug(remoteVersions.toString());
    final String gameVersion = remoteVersions.get("CB " + localVersion);
    if (gameVersion == null) throw new MojoExecutionException("Unable to determine game version!");
    this.getLog().debug("Internal game version id: " + gameVersion);
    return gameVersion;
  }

  public void execute() throws MojoExecutionException {
    this.getLog().info("Uploading project to BukkitDev");
    final String gameVersion = this.getGameVersion();
    final URIBuilder builder = new URIBuilder();
    final MultipartEntity entity = new MultipartEntity();
    HttpPost request;

    // create the request
    builder.setScheme("http");
    builder.setHost("dev.bukkit.org");
    builder.setPath("/" + this.projectType + "/" + this.slug.toLowerCase() + "/upload-file.json");
    try {
      entity.addPart("file_type", new StringBody(this.getFileType()));
      entity.addPart("name", new StringBody(this.project.getArtifact().getVersion()));
      entity.addPart("game_versions", new StringBody(gameVersion));
      entity.addPart("change_log", new StringBody(this.changeLog));
      entity.addPart("known_caveats", new StringBody(this.knownCaveats));
      entity.addPart("change_markup_type", new StringBody(this.markupType));
      entity.addPart("caveats_markup_type", new StringBody(this.markupType));
      entity.addPart("file", new FileBody(this.getArtifactFile(this.project.getArtifact())));
    } catch (UnsupportedEncodingException e) {
      throw new MojoExecutionException(e.getMessage());
    }

    // create the actual request
    try {
      request = new HttpPost(builder.build());
      request.setHeader("User-Agent", "MavenCurseForgeUploader/1.0");
      request.setHeader("X-API-Key", this.key);
      request.setEntity(entity);
    } catch (final URISyntaxException exception) {
      throw new MojoExecutionException(exception.getMessage());
    }

    this.getLog().debug(request.toString());

    // send the request and handle any replies
    try {
      HttpClient client = new DefaultHttpClient();
      HttpResponse response = client.execute(request);
      switch (response.getStatusLine().getStatusCode()) {
      case 201:
        this.getLog().info("File uploaded successfully.");
        break;
      case 403:
        this.getLog().error("You have not specifed your API key correctly or do not have permission to upload to that project.");
        break;
      case 404:
        this.getLog().error("Project was not found. Either it is specified wrong or been renamed.");
        break;
      case 422:
        this.getLog().error("There was an error in uploading the plugin");
        this.getLog().debug(request.toString());
        this.getLog().debug(EntityUtils.toString(response.getEntity()));
        break;
      default:
        this.getLog().warn("Unexpected response code: " + response.getStatusLine().getStatusCode());
        break;
      }
    } catch (ClientProtocolException exception) {
      throw new MojoExecutionException(exception.getMessage());
    } catch (IOException exception) {
      throw new MojoExecutionException(exception.getMessage());
    }

  }

  private String getFileType() {
    if (this.project.getArtifact().isRelease()) {
      return "r";
    } else {
      return "b";
    }
  }

  private String getLocalBukkitVersion() throws MojoExecutionException {
    String version = null;
    for (final Dependency project : this.project.getDependencies()) {
      if (project.getGroupId().contains("org.bukkit") && project.getArtifactId().equalsIgnoreCase("bukkit")) {
        version = project.getVersion();
      }
    }

    if (version == null) {
      throw new MojoExecutionException("Project is not built against Bukkit!");
    } else {
      this.getLog().debug("Project is built against " + version);
      return version;
    }

  }

  @SuppressWarnings("unchecked")
  private Map<String, String> getRemoteBukkitVersions() throws MojoExecutionException {
    HttpGet request = new HttpGet("http://" + this.game + ".curseforge.com/game-versions.json");
    JSONObject json;
    final Map<String, String> map = new HashMap<String, String>();

    try {
      HttpClient client = new DefaultHttpClient();
      HttpResponse response = client.execute(request);
      json = new JSONObject(EntityUtils.toString(response.getEntity()));
      final Iterator<String> itr = json.keys();
      while (itr.hasNext()) {
        final String key = itr.next();
        final String version = json.getJSONObject(key).getString("name");
        map.put(version, key);
      }
    } catch (IllegalStateException e) {
      throw new MojoExecutionException(e.getMessage());
    } catch (IOException e) {
      throw new MojoExecutionException(e.getMessage());
    } catch (ParseException e) {
      throw new MojoExecutionException(e.getMessage());
    } catch (JSONException e) {
      throw new MojoExecutionException(e.getMessage());
    }

    this.getLog().debug(map.size() + " remote versions found.");
    return map;

  }

  private File getArtifactFile(Artifact artifact) throws MojoExecutionException {
    if (artifact == null) throw new MojoExecutionException("No artifact to upload!");
    File file = artifact.getFile();
    if (file != null && file.isFile() && file.exists()) {
      return file;
    } else {
      throw new MojoExecutionException("No artifact to upload!");
    }
  }

}
