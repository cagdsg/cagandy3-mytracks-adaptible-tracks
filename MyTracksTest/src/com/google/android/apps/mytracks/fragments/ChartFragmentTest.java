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

package com.google.android.apps.mytracks.fragments;

import com.google.android.apps.mytracks.ChartView;
import com.google.android.apps.mytracks.TrackStubUtils;
import com.google.android.apps.mytracks.content.MyTracksLocation;
import com.google.android.apps.mytracks.content.Sensor;
import com.google.android.apps.mytracks.content.Sensor.SensorDataSet;
import com.google.android.apps.mytracks.util.UnitConversions;

import android.location.Location;
import android.test.AndroidTestCase;

/**
 * Tests {@link ChartFragment}.
 *
 * @author Youtao Liu
 */
public class ChartFragmentTest extends AndroidTestCase {

  private ChartFragment chartFragment;

  private final double HOURS_PER_UNIT = 60.0;

  @Override
  protected void setUp() throws Exception {
    chartFragment = new ChartFragment();
    chartFragment.setChartView(new ChartView(getContext()));
  }

  /**
   * Tests the logic to get the incorrect values of sensor in
   * {@link ChartFragment#fillDataPoint(Location, double[])}
   */
  public void testFillDataPoint_sensorIncorrect() {
    MyTracksLocation myTracksLocation = TrackStubUtils.createMyTracksLocation();
    
    // No input.
    double[] point = fillDataPointTestHelper(myTracksLocation);
    assertEquals(Double.NaN, point[3]);
    assertEquals(Double.NaN, point[4]);
    assertEquals(Double.NaN, point[5]);

    // Input incorrect state.
    // Creates SensorData.
    Sensor.SensorData.Builder powerData = Sensor.SensorData.newBuilder()
        .setValue(20).setState(Sensor.SensorState.NONE);
    Sensor.SensorData.Builder cadenceData = Sensor.SensorData.newBuilder()
        .setValue(20).setState(Sensor.SensorState.NONE);
    Sensor.SensorData.Builder heartRateData = Sensor.SensorData.newBuilder()
        .setValue(20).setState(Sensor.SensorState.NONE);
    // Creates SensorDataSet.
    SensorDataSet sensorDataSet = myTracksLocation.getSensorDataSet();
    sensorDataSet = sensorDataSet.toBuilder()
        .setPower(powerData)
        .setCadence(cadenceData)
        .setHeartRate(heartRateData)
        .build();
    myTracksLocation.setSensorData(sensorDataSet);
    // Test.
    point = fillDataPointTestHelper(myTracksLocation);
    assertEquals(Double.NaN, point[3]);
    assertEquals(Double.NaN, point[4]);
    assertEquals(Double.NaN, point[5]);
  }

  /**
   * Tests the logic to get the correct values of sensor in
   * {@link ChartFragment#fillDataPoint(Location, double[])}.
   */
  public void testFillDataPoint_sensorCorrect() {
    MyTracksLocation myTracksLocation = TrackStubUtils.createMyTracksLocation();
    // No input.
    double[] point = fillDataPointTestHelper(myTracksLocation);
    assertEquals(Double.NaN, point[3]);
    assertEquals(Double.NaN, point[4]);
    assertEquals(Double.NaN, point[5]);

    // Creates SensorData.
    Sensor.SensorData.Builder powerData = Sensor.SensorData.newBuilder()
        .setValue(20).setState(Sensor.SensorState.SENDING);
    Sensor.SensorData.Builder cadenceData = Sensor.SensorData.newBuilder()
        .setValue(20).setState(Sensor.SensorState.SENDING);
    Sensor.SensorData.Builder heartRateData = Sensor.SensorData.newBuilder()
        .setValue(20).setState(Sensor.SensorState.SENDING);
    
    // Creates SensorDataSet.
    SensorDataSet sensorDataSet = myTracksLocation.getSensorDataSet();
    sensorDataSet = sensorDataSet.toBuilder()
        .setPower(powerData)
        .setCadence(cadenceData)
        .setHeartRate(heartRateData)
        .build();
    myTracksLocation.setSensorData(sensorDataSet);
    // Test.
    point = fillDataPointTestHelper(myTracksLocation);
    assertEquals(20.0, point[3]);
    assertEquals(20.0, point[4]);
    assertEquals(20.0, point[5]);
  }

  /**
   * Tests the logic to get the value of metric Distance in
   * {@link ChartFragment#fillDataPoint(Location, double[])}.
   */
  public void testFillDataPoint_distanceMetric() {
    // By distance.
    chartFragment.setChartByDistance(true);
    // Resets last location and writes first location.
    MyTracksLocation myTracksLocation1 = TrackStubUtils.createMyTracksLocation();
    double[] point = fillDataPointTestHelper(myTracksLocation1);
    assertEquals(0.0, point[0]);

    // The second is a same location, just different time.
    MyTracksLocation myTracksLocation2 = TrackStubUtils.createMyTracksLocation();
    point = fillDataPointTestHelper(myTracksLocation2);
    assertEquals(0.0, point[0]);

    // The third location is a new location, and use metric.
    MyTracksLocation myTracksLocation3 = TrackStubUtils.createMyTracksLocation();
    myTracksLocation3.setLatitude(23);
    point = fillDataPointTestHelper(myTracksLocation3);
    
    // Computes the distance between Latitude 22 and 23.
    float[] results = new float[4];
    Location.distanceBetween(myTracksLocation2.getLatitude(), myTracksLocation2.getLongitude(),
        myTracksLocation3.getLatitude(), myTracksLocation3.getLongitude(), results);
    double distance1 = results[0] * UnitConversions.M_TO_KM;
    assertEquals(distance1, point[0]);

    // The fourth location is a new location, and use metric.
    MyTracksLocation myTracksLocation4 = TrackStubUtils.createMyTracksLocation();
    myTracksLocation4.setLatitude(24);
    point = fillDataPointTestHelper(myTracksLocation4);
    
    // Computes the distance between Latitude 23 and 24.
    Location.distanceBetween(myTracksLocation3.getLatitude(), myTracksLocation3.getLongitude(),
        myTracksLocation4.getLatitude(), myTracksLocation4.getLongitude(), results);
    double distance2 = results[0] * UnitConversions.M_TO_KM;
    assertEquals((distance1 + distance2), point[0]);
  }

  /**
   * Tests the logic to get the value of imperial Distance in
   * {@link ChartFragment#fillDataPoint(Location, double[])}.
   */
  public void testFillDataPoint_distanceImperial() {
    // By distance.
    chartFragment.setChartByDistance(true);
    // Setups to use imperial.
    chartFragment.setMetricUnits(false);

    // The first is a same location, just different time.
    MyTracksLocation myTracksLocation1 = TrackStubUtils.createMyTracksLocation();
    double[] point = fillDataPointTestHelper(myTracksLocation1);
    assertEquals(0.0, point[0]);

    // The second location is a new location, and use imperial.
    MyTracksLocation myTracksLocation2 = TrackStubUtils.createMyTracksLocation();
    myTracksLocation2.setLatitude(23);
    point = fillDataPointTestHelper(myTracksLocation2);

    /*
     * Computes the distance between Latitude 22 and 23. And for we set using
     * imperial, the distance should be multiplied by UnitConversions.KM_TO_MI.
     */
    float[] results = new float[4];
    Location.distanceBetween(myTracksLocation1.getLatitude(), myTracksLocation1.getLongitude(),
        myTracksLocation2.getLatitude(), myTracksLocation2.getLongitude(), results);
    double distance1 = results[0] * UnitConversions.M_TO_KM * UnitConversions.KM_TO_MI;
    assertEquals(distance1, point[0]);

    // The third location is a new location, and use imperial.
    MyTracksLocation myTracksLocation3 = TrackStubUtils.createMyTracksLocation();
    myTracksLocation3.setLatitude(24);
    point = fillDataPointTestHelper(myTracksLocation3);

    /*
     * Computes the distance between Latitude 23 and 24. And for we set using
     * imperial, the distance should be multiplied by UnitConversions.KM_TO_MI.
     */
    Location.distanceBetween(myTracksLocation2.getLatitude(), myTracksLocation2.getLongitude(),
        myTracksLocation3.getLatitude(), myTracksLocation3.getLongitude(), results);
    double distance2 = results[0] * UnitConversions.M_TO_KM * UnitConversions.KM_TO_MI;
    assertEquals(distance1 + distance2, point[0]);
  }

  /**
   * Tests the logic to get the values of time in
   * {@link ChartFragment#fillDataPoint(Location, double[])}.
   */
  public void testFillDataPoint_time() {
    // By time
    chartFragment.setChartByDistance(false);
    MyTracksLocation myTracksLocation1 = TrackStubUtils.createMyTracksLocation();
    double[] point = fillDataPointTestHelper(myTracksLocation1);
    assertEquals(0.0, point[0]);
    long timeSpan = 222;
    MyTracksLocation myTracksLocation2 = TrackStubUtils.createMyTracksLocation();
    myTracksLocation2.setTime(myTracksLocation1.getTime() + timeSpan);
    point = fillDataPointTestHelper(myTracksLocation2);
    assertEquals((double) timeSpan, point[0]);
  }

  /**
   * Tests the logic to get the value of elevation in
   * {@link ChartFragment#fillDataPoint(Location, double[])} by one and two
   * points.
   */
  public void testFillDataPoint_elevation() {
    MyTracksLocation myTracksLocation1 = TrackStubUtils.createMyTracksLocation();

    /*
     * At first, clear old points of elevation, so give true to the second
     * parameter. Then only one value INITIALLONGTITUDE in buffer.
     */
    double[] point = fillDataPointTestHelper(myTracksLocation1);
    assertEquals(TrackStubUtils.INITIAL_ALTITUDE, point[1]);

    /*
     * Send another value to buffer, now there are two values, INITIALALTITUDE
     * and INITIALALTITUDE * 2.
     */
    MyTracksLocation myTracksLocation2 = TrackStubUtils.createMyTracksLocation();
    myTracksLocation2.setAltitude(TrackStubUtils.INITIAL_ALTITUDE * 2);
    point = fillDataPointTestHelper(myTracksLocation2);
    assertEquals(
        (TrackStubUtils.INITIAL_ALTITUDE + TrackStubUtils.INITIAL_ALTITUDE * 2) / 2.0, point[1]);
  }

  /**
   * Tests the logic to get the value of speed in
   * {@link ChartFragment#fillDataPoint(Location, double[])}. In this test,
   * firstly remove all points in memory, and then fill in two points one by
   * one. The speed values of these points are 129, 130.
   */
  public void testFillDataPoint_speed() {
    // Set max speed to make the speed of points are valid.
    chartFragment.setTrackMaxSpeed(200.0);

    /*
     * At first, clear old points of speed, so give true to the second
     * parameter. It will not be filled in to the speed buffer.
     */
    MyTracksLocation myTracksLocation1 = TrackStubUtils.createMyTracksLocation();
    myTracksLocation1.setSpeed(129);
    double[] point = fillDataPointTestHelper(myTracksLocation1);
    assertEquals(0.0, point[2]);

    /*
     * Tests the logic when both metricUnits and reportSpeed are true.This
     * location will be filled into speed buffer.
     */
    MyTracksLocation myTracksLocation2 = TrackStubUtils.createMyTracksLocation();

    /*
     * Add a time span here to make sure the second point is valid, the value
     * 222 here is doesn't matter.
     */
    myTracksLocation2.setTime(myTracksLocation1.getTime() + 222);
    myTracksLocation2.setSpeed(130);
    point = fillDataPointTestHelper(myTracksLocation2);
    assertEquals(130.0 * UnitConversions.MS_TO_KMH, point[2]);
  }

  /**
   * Tests the logic to compute speed when use Imperial.
   */
  public void testFillDataPoint_speedImperial() {
    // Setups to use imperial.
    chartFragment.setMetricUnits(false);
    MyTracksLocation myTracksLocation = TrackStubUtils.createMyTracksLocation();
    myTracksLocation.setSpeed(132);
    double[] point = fillDataPointTestHelper(myTracksLocation);
    assertEquals(132.0 * UnitConversions.MS_TO_KMH * UnitConversions.KM_TO_MI, point[2]);
  }

  /**
   * Tests the logic to get pace value when reportSpeed is false.
   */
  public void testFillDataPoint_pace_nonZeroSpeed() {
    // Setups reportSpeed to false.
    chartFragment.setReportSpeed(false);
    MyTracksLocation myTracksLocation = TrackStubUtils.createMyTracksLocation();
    myTracksLocation.setSpeed(134);
    double[] point = fillDataPointTestHelper(myTracksLocation);
    assertEquals(HOURS_PER_UNIT / (134.0 * UnitConversions.MS_TO_KMH), point[2]);

  }

  /**
   * Tests the logic to get pace value when reportSpeed is false and average
   * speed is zero.
   */
  public void testFillDataPoint_pace_zeroSpeed() {
    // Setups reportSpeed to false.
    chartFragment.setReportSpeed(false);
    MyTracksLocation myTracksLocation = TrackStubUtils.createMyTracksLocation();
    myTracksLocation.setSpeed(0);
    double[] point = fillDataPointTestHelper(myTracksLocation);
    assertEquals(0.0, point[2]);
  }

  /**
   * Helper method to test fillDataPoint.
   *
   * @param location location to fill
   * @return data of this location
   */
  private double[] fillDataPointTestHelper(Location location) {
    double[] point = new double[6];
    chartFragment.fillDataPoint(location, point);
    return point;
  }

}
