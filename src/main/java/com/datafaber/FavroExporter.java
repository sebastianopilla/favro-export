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
import com.google.common.base.Strings;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.mashape.unirest.request.HttpRequest;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.util.*;

/**
 * Export handler
 */
public class FavroExporter {

  // header names
  private static final String FAVRO_ORGANIZATION_ID_HEADER = "organizationId";
  private static final String FAVRO_BACKEND_ID_HEADER = "X-Favro-Backend-Identifier";
  private static final String FAVRO_RATE_LIMIT_REMAINING_HEADER = "X-RateLimit-Remaining";
  private static final String FAVRO_RATE_LIMIT_RESET = "X-RateLimit-Reset";

  // status object
  private FavroExportStatus mStatus;

  private static Logger mLogger = LogManager.getLogger("com.datafaber.FavroExporter");


  public FavroExporter (FavroExportStatus pStatus) {
    mStatus = pStatus;
  }


  /**
   * Exports the organizations of this account to a file "organizations.json" in the given directory
   * @param pDestDir directory where to write the exported data
   * @return list of organization ids found in the account
   */
  public List<String> exportOrganizations (File pDestDir) {
    String ctx = "exportOrganizations - ";
    Preconditions.checkNotNull(pDestDir);
    List<String> result = new ArrayList<>();

    JSONArray organizations = getEntities(mStatus.getFavroBaseUrl() + "/organizations");
    if (organizations != null) {
      saveJsonToFile(pDestDir, "organizations.json", organizations);
      mLogger.info(ctx + "exported " + organizations.length());
      for (int i = 0; i < organizations.length(); i++) {
        JSONObject organization = (JSONObject)organizations.get(i);
        if (organization != null) {
          result.add(organization.getString("organizationId"));
        }
      }
    }
    return result;
  }


  /**
   * Exports all the users of the given organization to a "users-organizationId.json" file in the given directory
   * @param pDestDir directory where to write the exported data
   * @param pOrganizationId id of organization under which to search for users
   */
  public void exportUsers (File pDestDir, String pOrganizationId) {
    String ctx = "exportUsers - ";
    Preconditions.checkNotNull(pDestDir);
    Preconditions.checkNotNull(pOrganizationId);

    JSONArray users = getEntities(mStatus.getFavroBaseUrl() + "/users", pOrganizationId);
    if (users != null) {
      saveJsonToFile(pDestDir, "users-" + pOrganizationId + ".json", users);
      mLogger.info(ctx + "exported " + users.length() + " users");
    }
  }


  /**
   * Exports all the collections of the given organization to a "collections-organizationId.json" file in the given directory
   * @param pDestDir directory where to write the exported data
   * @param pOrganizationId id of organization under which to search for collections
   */
  public void exportCollections (File pDestDir, String pOrganizationId) {
    String ctx = "exportCollections - ";
    Preconditions.checkNotNull(pDestDir);
    Preconditions.checkNotNull(pOrganizationId);

    JSONArray collections = getEntities(mStatus.getFavroBaseUrl() + "/collections", pOrganizationId);
    if (collections != null) {
      saveJsonToFile(pDestDir, "collections-" + pOrganizationId + ".json", collections);
      mLogger.info(ctx + "exported " + collections.length() + " collections");
    }
  }


  /**
   * Exports all the tags of the given organization to a "tags-organizationId.json" file in the given directory
   * @param pDestDir directory where to write the exported data
   * @param pOrganizationId id of organization under which to search for tags
   */
  public void exportTags (File pDestDir, String pOrganizationId) {
    String ctx = "exportTags - ";
    Preconditions.checkNotNull(pDestDir);
    Preconditions.checkNotNull(pOrganizationId);

    JSONArray tags = getEntities(mStatus.getFavroBaseUrl() + "/tags", pOrganizationId);
    if (tags != null) {
      saveJsonToFile(pDestDir, "tags-" + pOrganizationId + ".json", tags);
      mLogger.info(ctx + "exported " + tags.length() + " tags");
    }
  }


  /**
   * Exports all the widgets of the given organization to a "widgets-organizationId.json" file in the given directory
   * @param pDestDir directory where to write the exported data
   * @param pOrganizationId id of organization under which to search for widgets
   * @result list of widgets ids found in the organization
   */
  public List<String> exportWidgets (File pDestDir, String pOrganizationId) {
    String ctx = "exportWidgets - ";
    Preconditions.checkNotNull(pDestDir);
    Preconditions.checkNotNull(pOrganizationId);
    List<String> result = new ArrayList<>();

    JSONArray widgets = getEntities(mStatus.getFavroBaseUrl() + "/widgets", pOrganizationId);
    if (widgets != null) {
      saveJsonToFile(pDestDir, "widgets-" + pOrganizationId + ".json", widgets);
      mLogger.info(ctx + "exported " + widgets.length() + " widgets");
      for (int i = 0; i < widgets.length(); i++) {
        JSONObject widget = (JSONObject)widgets.get(i);
        if (widget != null) {
          result.add(widget.getString("widgetCommonId"));
        }
      }
    }
    return result;
  }


  /**
   * Exports all the columns of the given widget to a "columns-widgetCommonId.json" file in the given directory
   * @param pDestDir directory where to write the exported data
   * @param pOrganizationId id of organization under which to search for columns
   * @param pWidgetCommonId common id of the widget on which the columns are
   */
  public void exportColumns (File pDestDir, String pOrganizationId, String pWidgetCommonId) {
    String ctx = "exportColumns - ";
    Preconditions.checkNotNull(pDestDir);
    Preconditions.checkNotNull(pOrganizationId);
    Preconditions.checkNotNull(pWidgetCommonId);

    JSONArray columns = getEntities(mStatus.getFavroBaseUrl() + "/columns?widgetCommonId=" + pWidgetCommonId, pOrganizationId);
    if (columns != null) {
      saveJsonToFile(pDestDir, "columns-" + pWidgetCommonId + ".json", columns);
      mLogger.info(ctx + "exported " + columns.length() + " columns for widget " + pWidgetCommonId);
    }
  }


  /**
   * Exports all the cards of the given widget to a "cards-widgetCommonId.json" file in the given directory
   * @param pDestDir directory where to write the exported data
   * @param pOrganizationId id of organization under which to search for cards
   * @param pWidgetCommonId common id of the widget on which the cards are
   */
  public void exportCards (File pDestDir, String pOrganizationId, String pWidgetCommonId) {
    String ctx = "exportCards - ";
    Preconditions.checkNotNull(pDestDir);
    Preconditions.checkNotNull(pOrganizationId);
    Preconditions.checkNotNull(pWidgetCommonId);

    JSONArray cards = getEntities(mStatus.getFavroBaseUrl() + "/cards?widgetCommonId=" + pWidgetCommonId, pOrganizationId);
    if (cards != null) {
      saveJsonToFile(pDestDir, "cards-" + pWidgetCommonId + ".json", cards);
      mLogger.info(ctx + "exported " + cards.length() + " cards for widget " + pWidgetCommonId);
      exportCardData(pDestDir, pOrganizationId, cards);
    }
  }


  /**
   * Exports the task lists, tasks, comments and attachments of the given cards
   * @param pDestDir directory where to write the exported data
   * @param pOrganizationId id of organization under which to search for cards
   * @param pCards array of cards
   */
  private void exportCardData (File pDestDir, String pOrganizationId, JSONArray pCards) {
    Preconditions.checkNotNull(pDestDir);
    Preconditions.checkNotNull(pOrganizationId);
    Preconditions.checkNotNull(pCards);

    for (int i = 0; i < pCards.length(); i++) {
      JSONObject card = (JSONObject) pCards.get(i);
      if (card != null) {
        String cardCommonId = card.getString("cardCommonId");
        JSONArray attachments = card.getJSONArray("attachments");
        downloadAttachments(pDestDir, cardCommonId, attachments);
        exportTaskListsAndTasks(pDestDir, pOrganizationId, card);
        exportComments(pDestDir, pOrganizationId, card);
      }
    }
  }


  /**
   * Exports the task lists and tasks of the given card to files "tasklists-cardCommonId.json" and "tasks-cardCommonId.json" in the given directory
   * @param pDestDir directory where to write the exported data
   * @param pOrganizationId id of organization owning the card
   * @param pCard card containing the task lists
   */
  private void exportTaskListsAndTasks (File pDestDir, String pOrganizationId, JSONObject pCard) {
    String ctx = "exportTaskListsAndTasks - ";
    Preconditions.checkNotNull(pDestDir);
    Preconditions.checkNotNull(pOrganizationId);
    Preconditions.checkNotNull(pCard);

    if (pCard.has("tasksTotal")) {
      String cardCommonId = pCard.getString("cardCommonId");
      int tasksTotal = pCard.getInt("tasksTotal");
      if (tasksTotal > 0) {
        JSONArray tasklists = getEntities(mStatus.getFavroBaseUrl() + "/tasklists?cardCommonId=" + cardCommonId, pOrganizationId);
        JSONArray tasks = getEntities(mStatus.getFavroBaseUrl() + "/tasks?cardCommonId=" + cardCommonId, pOrganizationId);
        if (tasklists != null) {
          saveJsonToFile(pDestDir, "tasklists-" + cardCommonId + ".json", tasklists);
          mLogger.info(ctx + "exported " + tasklists.length() + " task lists for card " + cardCommonId);
        }
        if (tasks != null) {
          saveJsonToFile(pDestDir, "tasks-" + cardCommonId + ".json", tasks);
          mLogger.info(ctx + "exported " + tasks.length() + " tasks for card " + cardCommonId);
        }
      }
    }
  }


  /**
   * Exports the comments of the given card to a file "comments-cardCommonId.json" in the given directory
   * @param pDestDir directory where to write the exported data
   * @param pOrganizationId id of organization owning the card
   * @param pCard card containing the comments
   */
  private void exportComments (File pDestDir, String pOrganizationId, JSONObject pCard) {
    String ctx = "exportComments - ";
    Preconditions.checkNotNull(pDestDir);
    Preconditions.checkNotNull(pOrganizationId);
    Preconditions.checkNotNull(pCard);

    if (pCard.has("numComments")) {
      String cardCommonId = pCard.getString("cardCommonId");
      int numComments = pCard.getInt("numComments");
      if (numComments > 0) {
        JSONArray comments = getEntities(mStatus.getFavroBaseUrl() + "/comments?cardCommonId=" + cardCommonId, pOrganizationId);
        if (comments != null) {
          saveJsonToFile(pDestDir, "comments-" + cardCommonId + ".json", comments);
          mLogger.info(ctx + "exported " + comments.length() + " comments for card " + cardCommonId);
        }
      }
    }
  }


  /**
   * Exports the attachment of the given card to a subdirectory "attachments-cardCommonId" of the given directory
   * @param pDestDir destination directory
   * @param pCardCommonId id of card owning the attachments
   * @param pAttachments array of attachments
   */
  private void downloadAttachments (File pDestDir, String pCardCommonId, JSONArray pAttachments) {
    String ctx = "downloadAttachments - ";
    Preconditions.checkNotNull(pDestDir);
    Preconditions.checkNotNull(pCardCommonId);
    Preconditions.checkNotNull(pAttachments);

    if (pAttachments.length() > 0) {
      File attachmentsDir = new File(pDestDir, "attachments-" + pCardCommonId);
      if (!attachmentsDir.mkdirs()) {
        mLogger.error(ctx + "could not create directory " + attachmentsDir.getAbsolutePath());
      } else {
        for (int j = 0; j < pAttachments.length(); j++) {
          JSONObject attachment = (JSONObject)pAttachments.get(j);
          String fileName = attachment.getString("name");
          String fileUrl = attachment.getString("fileURL");
          File attachmentFile = new File(attachmentsDir, fileName);
          try {
            FileUtils.copyURLToFile(new URL(fileUrl), attachmentFile, 30000, 30000);
            mLogger.info(ctx + "exported attachment for card " + pCardCommonId + " to file " + attachmentFile.getAbsolutePath());
          } catch (IOException ioe) {
            mLogger.error(ctx + "could not download URL " + fileUrl + " to destination " + attachmentFile.getAbsolutePath());
          }
        }
      }
    }
  }


  /**
   * Saves the given JSON array (the "entities" exported by Favro) to a file
   * @param pDestDir directory where the file will be written
   * @param pFileName name of the destination file
   * @param pJson array of entities
   */
  private void saveJsonToFile (File pDestDir, String pFileName, JSONArray pJson) {
    String ctx = "saveJsonToFile - ";
    Preconditions.checkNotNull(pDestDir);
    Preconditions.checkNotNull(pFileName);
    Preconditions.checkNotNull(pJson);
    File jsonFile = new File(pDestDir, pFileName);
    try (FileWriter writer = new FileWriter(jsonFile)) {
      writer.write(pJson.toString(2));
      writer.flush();
    } catch (IOException ioe) {
      mLogger.error(ctx + "IOException saving json to file " + jsonFile.getAbsolutePath());
    }
  }


  /**
   * Retrieve an array of entities from the Favro API
   * @param pUrl url to request
   * @return array of entities
   */
  private JSONArray getEntities (String pUrl) {
    return getEntities(pUrl, null);
  }


  /**
   * Retrieve an array of entities from the Favro API
   * @param pUrl url to request
   * @param pOrganizationId id of organization owning the entities (can be null if retrieving organizations)
   * @return array of entities
   */
  private JSONArray getEntities (String pUrl, String pOrganizationId) {
    String ctx = "getEntities - ";
    Preconditions.checkNotNull(pUrl);
    JSONArray result = new JSONArray();
    int pagesRemaining = 1, currentPage = 0;
    String requestId = null;
    while (pagesRemaining > 0) {
      try {
        HttpRequest request = prepareRequest(pUrl, pOrganizationId, requestId, currentPage);
        HttpResponse<String> response = request.asString();
        mStatus.setWaitTime(checkRateLimits(response));
        mStatus.setFavroBackendId(response.getHeaders().getFirst(FAVRO_BACKEND_ID_HEADER));
        if (!checkResponse(response)) {
          mLogger.error(ctx + "Favro API returned error code " + response.getStatus() + " for request " + pUrl);
          continue;
        }
        String responseBody = response.getBody();
        JSONObject responseJson = new JSONObject(responseBody);
        requestId = responseJson.getString("requestId");
        currentPage = responseJson.getInt("page");
        int totalPages = responseJson.getInt("pages");
        if (totalPages == (currentPage - 1)) {
          // end the loop if this is the last page
          pagesRemaining = 0;
        }
        JSONArray entities = responseJson.getJSONArray("entities");
        if (entities != null && entities.length() > 0) {
          result = concatArrays(result, entities);
        }
      } catch (UnirestException ue) {
        mLogger.error(ctx + "UnirestException for request " + pUrl);
      } finally {
        pagesRemaining--;
      }
    }
    return result;
  }


  /**
   * Checks how much time we should wait before issuing the next request
   * @param pResponse response containing rate limiting headers
   * @return if negative, amount of milliseconds to wait before the next request; zero or positive if no wait time needed
   */
  private long checkRateLimits (HttpResponse<?> pResponse) {
    Preconditions.checkNotNull(pResponse);
    String reqRemainingStr = pResponse.getHeaders().getFirst(FAVRO_RATE_LIMIT_REMAINING_HEADER);
    if (!Strings.isNullOrEmpty(reqRemainingStr)) {
      Integer reqRemaining = Integer.parseInt(reqRemainingStr);
      if (reqRemaining < 1) {
        String resetTimeStr = pResponse.getHeaders().getFirst(FAVRO_RATE_LIMIT_RESET);
        if (!Strings.isNullOrEmpty(resetTimeStr)) {
          Date resetTime = parseDate(resetTimeStr);
          long resetTimeMsecs = resetTime.getTime();
          return (System.currentTimeMillis() - resetTimeMsecs);
        }
      }
    }
    return 0;
  }


  /**
   * Prepares a request to the Favro API, possibly waiting a certain amount of time to satisfy the rate limits
   * @param pUrl url to request
   * @param pOrganizationId organization owning the entities
   * @param pRequestId request id from a previous response (needed for paginated requests)
   * @param pCurrentPage page to request
   * @return request with headers, query string and authentication informations
   */
  private HttpRequest prepareRequest (String pUrl, String pOrganizationId, String pRequestId, int pCurrentPage) {
    Preconditions.checkNotNull(pUrl);
    Map<String,String> headers = new HashMap<>();

    // specify the organization in the headers
    if (!Strings.isNullOrEmpty(mStatus.getFavroOrganizationId())) {
      headers.put(FAVRO_ORGANIZATION_ID_HEADER, mStatus.getFavroOrganizationId());
    }

    // route the request to a specific backend if needed
    if (!Strings.isNullOrEmpty(mStatus.getFavroBackendId())) {
      headers.put(FAVRO_BACKEND_ID_HEADER, mStatus.getFavroBackendId());
    }

    // restrict to the specified organization, if any
    if (!Strings.isNullOrEmpty(pOrganizationId)) {
      headers.put(FAVRO_ORGANIZATION_ID_HEADER, pOrganizationId);
    }

    // wait if we're over the rate limit
    if (mStatus.getWaitTime() > 0L) {
      waitForRateLimitReset(mStatus.getWaitTime());
    }

    // if this is a paged request (pCurrentPage > 0), add the page and the request id to the parameters
    // otherwise, just build the request
    HttpRequest request = Unirest.get(pUrl).
            headers(headers).
            basicAuth(mStatus.getFavroUser(), mStatus.getFavroApiToken());
    if (pRequestId != null && pCurrentPage > 0) {
      request.queryString("requestId", pRequestId);
      request.queryString("page", String.valueOf(pCurrentPage));
    }

    return request;
  }


  /**
   * Checks if the response code allows us to continue
   * @param pResponse favro response
   * @return true if the response status is 2xx
   */
  private boolean checkResponse (HttpResponse<?> pResponse) {
    return (pResponse != null)
            && (pResponse.getStatus() == 200 ||
                pResponse.getStatus() == 201 ||
                pResponse.getStatus() == 202 ||
                pResponse.getStatus() == 204);
  }


  /**
   * Waits for the specified amount of time
   * @param pMsecs number of milliseconds to wait
   */
  private void waitForRateLimitReset (long pMsecs) {
    mLogger.info("Waiting until " + new Date(System.currentTimeMillis() + pMsecs) + " before the next request");
    try {
      Thread.sleep(pMsecs);
    } catch (InterruptedException ie) {
      mLogger.error("InterruptedException waiting for rate limit reset");
    }
  }


  /**
   * Concatenate the given JSON arrays into a new array, in the order of the arguments
   * @param pArrs arrays to concatenate
   * @return concatenated array
   */
  private JSONArray concatArrays (JSONArray... pArrs) {
    JSONArray result = new JSONArray();
    for (JSONArray arr : pArrs) {
      for (int i = 0; i < arr.length(); i++) {
        result.put(arr.get(i));
      }
    }
    return result;
  }


  /**
   * Parses a string in ISO date format into a date object
   * @param pDateStr ISO-formatted date string
   * @return parsed date
   */
  private Date parseDate (String pDateStr) {
    DateTimeFormatter fmt = ISODateTimeFormat.dateTime();
    return fmt.parseDateTime(pDateStr).toDate();
  }

} // end FavroExporter
