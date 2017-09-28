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

package com.datafaber.model;

/**
 * Used to keep status informations during an export
 */
public class FavroExportStatus {

  // Favro base url and API token used for the export
  private String mFavroBaseUrl;
  private String mFavroUser;
  private String mFavroApiToken;

  // Favro backend identifier
  private String mFavroBackendId;

  // Favro organization identifier
  private String mFavroOrganizationId;

  // time (in msecs) to wait before issuing the next request
  private long mWaitTime;

  public FavroExportStatus (String pFavroBaseUrl, String pFavroUser, String pFavroApiToken) {
    mFavroBaseUrl = pFavroBaseUrl;
    mFavroUser = pFavroUser;
    mFavroApiToken = pFavroApiToken;
  }

  public String getFavroBaseUrl () {
    return mFavroBaseUrl;
  }

  public String getFavroUser () {
    return mFavroUser;
  }

  public String getFavroApiToken () {
    return mFavroApiToken;
  }

  public String getFavroBackendId () {
    return mFavroBackendId;
  }

  public void setFavroBackendId (String pFavroBackendId) {
    mFavroBackendId = pFavroBackendId;
  }

  public String getFavroOrganizationId () {
    return mFavroOrganizationId;
  }

  public void setFavroOrganizationId (String pFavroOrganizationId) {
    mFavroOrganizationId = pFavroOrganizationId;
  }

  public long getWaitTime () {
    return mWaitTime;
  }

  public void setWaitTime (long pWaitTime) {
    mWaitTime = pWaitTime;
  }
}
