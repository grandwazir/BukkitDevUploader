BukkitDevUploader: Upload artifacts to dev.bukkit.org
====================================

BukkitDevUploader is a plugin for [Maven](http://maven.org/) that allows users to upload an artifact to dev.bukkit.org as part of the build phase. The basic idea is to automate the uploading of new plugin releases to make it easier to publish plugins on dev.bukkit.org.

## Features

- Automatic version matching.
- Upload server or client mods.
- Supports setting a change log and known caveats.

Currently there is only support to upload a single file (no attached artifacts such as Javadocs or sources).

## License

BukkitDevUploader is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.

BukkitDevUploader is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.

## Documentation

Many of the features specific to BukkitDevUploader are documented [on the wiki](https://github.com/grandwazir/BukkitDevUploader/wiki). 

You may also find the [JavaDocs](http://grandwazir.github.com/BanHammer/BukkitDevUploader/index.html) and a [Maven website](http://grandwazir.github.com/BukkitDevUploader/) useful to you as well.

## Configuration

The plugin support several configuration options that can either be expressed in your project's POM file or in your settings.xml file. Where you put the plugin settings depends on whether you want a specific setting to be configured globally or on a per-project basis. 

There is also a privacy element in that if you have public repositories for your plugins it is not a good idea to share your API key by placing it in your pom.xml. In this situation you should use settings.xml.

The plugin support the following elements:

- bukkitdevuploader.key (The API key that you want to use)
- bukkitdevuploader.projectType (The type of project you are uploading. Defaults to `server-mods`)
- bukkitdevuploader.slug (The project name on the dev.bukkit site. Defaults to your project's name)
- bukkitdevuploader.changeLog (The changelog to submit. Defaults to a stub message)
- bukkitdevuploader.knownCaveats (The known caveats to submit. Defaults to "none")
- bukkitdevuploader.markupType (The type of markup you want your changelog and know caveats to be displayed with)

### Example using settings.xml

The best way to store your [API key](https://dev.bukkit.org/home/api-key/) is to use settings.xml. This can be found in your local maven repo. On linux it is usually in your home directory, for example `home/grandwazir/.m2/settings.xml`.

By doing this you keep your API key safe and out of potentially public repositories. You can override any of these values in your project poms.

    <profiles>
      <profile>
        <id>bukkitdevuploader</id>
        <properties>
          <bukkitdevuploader.apiKey>SECRET</bukkitdevuploader.apiKey>
          <bukkitdevuploader.markupType>markdown</bukkitdevuploader.markupType>
        </properties>
      </profile>
    </profiles>

    <activeProfiles>
      <activeProfile>bukkitdevuploader</activeProfile>
    </activeProfiles>

### Example using pom.xml

You can also configure values for individual plugins by adding them to your properties list in the pom. This can be useful for project specfic settings such as slugs and changelogs.

    <properties>
      <bukkitdevuploader.slug>custom-slug</bukkitdevuploader.slug>
      <bukkitdevuploader.markupType>plain</bukkitdevuploader.markupType>
    </properties>

## Installation

### Configure your pom.xml

Once you have configured your API key you need to add the plugin to the build phase of your plugin. In the example above an artifact will be uploaded every time you use the `mvn deploy` command.

    <repositories>
       <repository>
         <id>james.richardson.name</id>
         <url>http://repository.james.richardson.name</url>
       </repository>
    </repositories>

    <build>
      <plugins>
        <plugin>
          <groupId>name.richardson.james.maven</groupId>
          <artifactId>bukkit-dev-uploader</artifactId>
          <version>1.0.0</version>
          <executions>
            <execution>
              <goals>
                <goal>upload</goal>
              </goals>
              <phase>deploy</phase>
            </execution>
          </executions>
        </plugin>
      </plugins>
    </build>

You can also bind the upload goal to execute as part of a specific phase:

    <executions>
      <execution>
        <goals>
          <goal>upload</goal>
        </goals>
       <phase>install</phase>
     </execution>
    </executions>

## Reporting issues

If you want to make a bug report or feature request please do so using the [issue tracking](https://github.com/grandwazir/BukkitDevUploader/issues) on GitHub.
