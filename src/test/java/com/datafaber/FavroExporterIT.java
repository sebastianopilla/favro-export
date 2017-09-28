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
import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.apache.commons.io.FileUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * Test cases
 */
public class FavroExporterIT extends TestCase {

  /**
   * This test connect to the actual Favro API using a real account
   * The account user and token are passed as environment variables to avoid committing credentials in the Git repository
   */
  public void testExport () throws Exception {
    FavroExportStatus status = new FavroExportStatus("https://favro.com/api/v1",
            System.getenv("FAVRO_USER"),
            System.getenv("FAVRO_API_TOKEN"));
    File testExportDir = new File("testExport");
    testExportDir.mkdirs();
    FileUtils.cleanDirectory(testExportDir);
    FavroExporter exporter = new FavroExporter(status);

    // test organizations
    exporter.exportOrganizations(testExportDir);
    File organizationsFile = new File(testExportDir, "organizations.json");
    Assert.assertTrue(organizationsFile.exists());
    String content = new String(Files.readAllBytes(Paths.get(organizationsFile.toURI())));
    JSONArray organizations = new JSONArray(content);
    Assert.assertNotNull(organizations);
    Assert.assertTrue(organizations.length() >= 1);
    Assert.assertNotNull(organizations.getJSONObject(0).getString("organizationId"));
    Assert.assertNotNull(organizations.getJSONObject(0).getString("name"));

    // test users
    String organizationId = organizations.getJSONObject(0).getString("organizationId");
    exporter.exportUsers(testExportDir, organizationId);
    File usersFile = new File(testExportDir, "users-" + organizationId + ".json");
    Assert.assertTrue(usersFile.exists());
    content = new String(Files.readAllBytes(Paths.get(usersFile.toURI())));
    JSONArray users = new JSONArray(content);
    Assert.assertNotNull(users);
    Assert.assertTrue(users.length() >= 1);
    Assert.assertNotNull(users.getJSONObject(0).getString("userId"));
    Assert.assertNotNull(users.getJSONObject(0).getString("name"));
    Assert.assertNotNull(users.getJSONObject(0).getString("email"));
  }
}
