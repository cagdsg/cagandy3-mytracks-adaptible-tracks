/*
 * Copyright 2012 Google Inc.
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

package com.google.android.apps.mytracks.maps;

import com.google.android.apps.mytracks.Constants;
import com.google.android.apps.mytracks.util.PreferencesUtils;
import com.google.android.maps.mytracks.R;

import android.content.Context;
import android.content.SharedPreferences;
import android.test.AndroidTestCase;

/**
 * Tests for the {@link DynamicSpeedTrackPathDescriptor}.
 * 
 * @author Youtao Liu
 */
public class DynamicSpeedTrackPathDescriptorTest extends AndroidTestCase {

  private Context context;
  private SharedPreferences sharedPreferences;

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    context = getContext();
    sharedPreferences = context.getSharedPreferences(Constants.SETTINGS_NAME, Context.MODE_PRIVATE);
  }

  /**
   * Tests the method {@link DynamicSpeedTrackPathDescriptor#getSpeedMargin()}
   * with zero, normal and illegal value.
   */
  public void testGetSpeedMargin() {
    // The default value of speedMargin is 25.
    int[] expectations = { 0, 50, 99, 25 };
    // Test
    for (int i = 0; i < expectations.length; i++) {
      PreferencesUtils.setInt(context, R.string.track_color_mode_percentage_key, expectations[i]);
      DynamicSpeedTrackPathDescriptor dynamicSpeedTrackPathDescriptor = new DynamicSpeedTrackPathDescriptor(
          context);
      assertEquals(expectations[i], dynamicSpeedTrackPathDescriptor.getSpeedMargin());
    }
  }

  /**
   * Tests {@link DynamicSpeedTrackPathDescriptor#onSharedPreferenceChanged(SharedPreferences,
   * String)} when the key is null.
   */
  public void testOnSharedPreferenceChanged_nullKey() {
    DynamicSpeedTrackPathDescriptor dynamicSpeedTrackPathDescriptor = new DynamicSpeedTrackPathDescriptor(
        context);
    int speedMargin = dynamicSpeedTrackPathDescriptor.getSpeedMargin();
    // Change value in shared preferences.
    PreferencesUtils.setInt(context, R.string.track_color_mode_percentage_key, speedMargin + 2);

    dynamicSpeedTrackPathDescriptor.onSharedPreferenceChanged(sharedPreferences, null);
    assertEquals(speedMargin, dynamicSpeedTrackPathDescriptor.getSpeedMargin());
  }

  /**
   * Tests {@link DynamicSpeedTrackPathDescriptor#onSharedPreferenceChanged(SharedPreferences,
   * String)} when the key is not null, and not trackColorModeDynamicVariation.
   */
  public void testOnSharedPreferenceChanged_otherKey() {
    DynamicSpeedTrackPathDescriptor dynamicSpeedTrackPathDescriptor = new DynamicSpeedTrackPathDescriptor(
        context);
    int speedMargin = dynamicSpeedTrackPathDescriptor.getSpeedMargin();
    // Change value in shared preferences.
    PreferencesUtils.setInt(context, R.string.track_color_mode_percentage_key, speedMargin + 2);
    dynamicSpeedTrackPathDescriptor.onSharedPreferenceChanged(sharedPreferences, "anyKey");
    assertEquals(speedMargin, dynamicSpeedTrackPathDescriptor.getSpeedMargin());
  }

  /**
   * Tests {@link DynamicSpeedTrackPathDescriptor#onSharedPreferenceChanged(SharedPreferences,
   * String)} when the key is trackColorModeDynamicVariation.
   */
  public void testOnSharedPreferenceChanged_trackColorModeDynamicVariationKey() {
    DynamicSpeedTrackPathDescriptor dynamicSpeedTrackPathDescriptor = new DynamicSpeedTrackPathDescriptor(
        context);
    int speedMargin = dynamicSpeedTrackPathDescriptor.getSpeedMargin();
    // Change value in shared preferences.
    PreferencesUtils.setInt(context, R.string.track_color_mode_percentage_key, speedMargin + 2);

    dynamicSpeedTrackPathDescriptor.onSharedPreferenceChanged(sharedPreferences,
        PreferencesUtils.getKey(context, R.string.track_color_mode_percentage_key));
    assertEquals(speedMargin + 2, dynamicSpeedTrackPathDescriptor.getSpeedMargin());
  }

  /**
   * Tests {@link DynamicSpeedTrackPathDescriptor#onSharedPreferenceChanged(SharedPreferences,
   * String)} when the values of speedMargin is the default value.
   */
  public void testOnSharedPreferenceChanged_defaultValue() {
    DynamicSpeedTrackPathDescriptor dynamicSpeedTrackPathDescriptor = new DynamicSpeedTrackPathDescriptor(
        context);
    // Change value in shared preferences
    PreferencesUtils.setInt(context, R.string.track_color_mode_percentage_key,
        PreferencesUtils.TRACK_COLOR_MODE_PERCENTAGE_DEFAULT);
    dynamicSpeedTrackPathDescriptor.onSharedPreferenceChanged(sharedPreferences,
        PreferencesUtils.getKey(context, R.string.track_color_mode_percentage_key));
    // The default value of speedMargin is 25.
    assertEquals(PreferencesUtils.TRACK_COLOR_MODE_PERCENTAGE_DEFAULT,
        dynamicSpeedTrackPathDescriptor.getSpeedMargin());
  }

  /**
   * Tests {@link DynamicSpeedTrackPathDescriptor#needsRedraw()} by wrong track
   * id.
   */
  public void testNeedsRedraw_WrongTrackId() {
    PreferencesUtils.setLong(
        context, R.string.selected_track_id_key, PreferencesUtils.SELECTED_TRACK_ID_DEFAULT);
    DynamicSpeedTrackPathDescriptor dynamicSpeedTrackPathDescriptor = new DynamicSpeedTrackPathDescriptor(
        context);
    assertEquals(false, dynamicSpeedTrackPathDescriptor.needsRedraw());
  }

  /**
   * Tests {@link DynamicSpeedTrackPathDescriptor#needsRedraw()} by different
   * averageMovingSpeed.
   */
  public void testIsDiffereceSignificant() {
    DynamicSpeedTrackPathDescriptor dynamicSpeedTrackPathDescriptor = new DynamicSpeedTrackPathDescriptor(
        context);
    double[] averageMovingSpeeds = { 0, 30, 30, 30 };
    double[] newAverageMovingSpeed = { 20, 30,
    // Difference is less than CRITICAL_DIFFERENCE_PERCENTAGE
        30 * (1 + (DynamicSpeedTrackPathDescriptor.CRITICAL_DIFFERENCE_PERCENTAGE / 100) / 2),
    // Difference is more than CRITICAL_DIFFERENCE_PERCENTAGE
        30 * (1 + (DynamicSpeedTrackPathDescriptor.CRITICAL_DIFFERENCE_PERCENTAGE / 100.00) * 2) };
    boolean[] expectedValues = { true, false, false, true };
    double[] expectedAverageMovingSpeed = { 20, 30, 30,
        30 * (1 + (DynamicSpeedTrackPathDescriptor.CRITICAL_DIFFERENCE_PERCENTAGE / 100.00) * 2) };
    // Test
    for (int i = 0; i < newAverageMovingSpeed.length; i++) {
      dynamicSpeedTrackPathDescriptor.setAverageMovingSpeed(averageMovingSpeeds[i]);
      assertEquals(expectedValues[i], dynamicSpeedTrackPathDescriptor.isDifferenceSignificant(
          averageMovingSpeeds[i], newAverageMovingSpeed[i]));
      assertEquals(
          expectedAverageMovingSpeed[i], dynamicSpeedTrackPathDescriptor.getAverageMovingSpeed());
    }
  }
}