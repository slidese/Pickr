/*
 * Copyright (c) 2010 Google Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package se.slide.pickr.picasa.model;

import com.google.api.client.googleapis.GoogleUrl;
import com.google.api.client.util.Key;

/**
 * @author Yaniv Inbar
 */
public class PicasaUrl extends GoogleUrl {

  public static final String ROOT_URL = "http://picasaweb.google.com/data/";

  @Key
  public String kinds;

  public PicasaUrl(String encodedUrl) {
    super(encodedUrl);
  }

  /**
   * Constructs a new URL based on the given relative path.
   * 
   * @param relativePath encoded path relative to the {@link #ROOT_URL}
   * @return new URL
   */
  public static PicasaUrl relativeToRoot(String relativePath) {
    return new PicasaUrl(ROOT_URL + relativePath);
  }
}