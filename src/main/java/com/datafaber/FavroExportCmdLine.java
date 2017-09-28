/*
 * Copyright 2017 Sebastiano Pilla
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package com.datafaber;

import com.datafaber.model.FavroExportStatus;
import com.google.common.base.Preconditions;
import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.inf.*;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.util.*;

/**
 * Main entry point
 */
public class FavroExportCmdLine {

  // invocation example:
  //   FavroExportCmdLine -c <configuration file> -d <destination folder>
  // see https://favro.com/developer/ for the favro api

  // property names in the configuration file
  private static final String FAVRO_BASE_URL = "favro.base.url";
  private static final String FAVRO_USER = "favro.user";
  private static final String FAVRO_API_TOKEN = "favro.api.token";

  private static Logger mLogger = LogManager.getLogger("com.datafaber.FavroExportCmdLine");


  /**
   * Main entry point
   * @param pArgs command line arguments
   */
  public static void main (String[] pArgs) {
    mLogger.info("Starting export");

    // parse command line arguments and return errors if needed
    String destinationFolderPath = "", configurationFilePath = "";
    ArgumentParser parser = ArgumentParsers.newFor("FavroExport").build()
            .defaultHelp(true)
            .description("Exports your organizations' data from Favro into local files");
    parser.addArgument("-c", "--configuration")
            .type(String.class)
            .required(true);
    parser.addArgument("-d", "--destination")
            .type(String.class)
            .required(true);
    try {
      Namespace ns = parser.parseArgs(pArgs);
      destinationFolderPath = ns.getString("destination");
      configurationFilePath = ns.getString("configuration");
    } catch (ArgumentParserException ape) {
      parser.handleError(ape);
      System.exit(-1);
    }

    // empty the destination directory
    File destDir = new File(destinationFolderPath);
    if (!destDir.exists()) {
      boolean dirok = destDir.mkdirs();
      if (!dirok) {
        mLogger.error("Could not create the destination directory " + destDir.getAbsolutePath());
        return;
      }
    }
    try {
      FileUtils.cleanDirectory(destDir);
    } catch (IOException ioe) {
      mLogger.error("Could not clean the destination directory " + destDir.getAbsolutePath(), ioe);
      return;
    }

    // read the configuration
    File configFile = new File(configurationFilePath);
    Properties config = readConfiguration(configFile);
    String favroBaseUrl = config.getProperty(FAVRO_BASE_URL);
    String favroUser = config.getProperty(FAVRO_USER);
    String favroApiToken = config.getProperty(FAVRO_API_TOKEN);

    FavroExportStatus status = new FavroExportStatus(favroBaseUrl, favroUser, favroApiToken);
    FavroExporter favroExporter = new FavroExporter(status);

    // start exporting from the organizations
    List<String> organizationIds = favroExporter.exportOrganizations(destDir);
    if (organizationIds != null && organizationIds.size() > 0) {
      for (String organizationId : organizationIds) {
        favroExporter.exportUsers(destDir, organizationId);
        favroExporter.exportCollections(destDir, organizationId);
        favroExporter.exportTags(destDir, organizationId);
        List<String> widgetCommonIds = favroExporter.exportWidgets(destDir, organizationId);
        for (String widgetCommonId : widgetCommonIds) {
          favroExporter.exportColumns(destDir, organizationId, widgetCommonId);
          favroExporter.exportCards(destDir, organizationId, widgetCommonId);
        }
      }
    }

    mLogger.info("End export");
  }


  /**
   * Reads and validates the given configuration
   * @param pConfigurationFile configuration file
   * @return configuration
   */
  private static Properties readConfiguration (File pConfigurationFile) {
    Preconditions.checkNotNull(pConfigurationFile);
    Properties config = new Properties();
    try (FileInputStream fis = FileUtils.openInputStream(pConfigurationFile)) {
      config.load(fis);
    } catch (IOException ioe) {
      mLogger.error("Could not read the configuration file " + pConfigurationFile.getAbsolutePath(), ioe);
    }

    // validate the configuration file
    boolean configValid = true;
    if (null == config.getProperty(FAVRO_BASE_URL)) {
      mLogger.error("The configuration file is missing the " + FAVRO_BASE_URL + " property");
      configValid = false;
    }
    if (null == config.getProperty(FAVRO_API_TOKEN)) {
      mLogger.error("The configuration file is missing the " + FAVRO_API_TOKEN + " property");
      configValid = false;
    }

    if (!configValid) {
      throw new RuntimeException("Invalid configuration");
    }

    return config;
  }

} // end FavroExportCmdLine
