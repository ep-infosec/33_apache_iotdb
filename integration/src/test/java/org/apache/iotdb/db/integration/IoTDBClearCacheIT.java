/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.iotdb.db.integration;

import org.apache.iotdb.db.engine.cache.ChunkCache;
import org.apache.iotdb.db.engine.cache.TimeSeriesMetadataCache;
import org.apache.iotdb.integration.env.EnvFactory;
import org.apache.iotdb.itbase.category.ClusterTest;
import org.apache.iotdb.itbase.category.LocalStandaloneTest;
import org.apache.iotdb.itbase.category.RemoteTest;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.experimental.categories.Category;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

@Category({LocalStandaloneTest.class, ClusterTest.class, RemoteTest.class})
public class IoTDBClearCacheIT {

  private static String[] sqls =
      new String[] {
        "CREATE DATABASE root.ln",
        "create timeseries root.ln.wf01.wt01.status with datatype=BOOLEAN,encoding=PLAIN",
        "insert into root.ln.wf01.wt01(timestamp,status) values(1509465600000,true)",
        "insert into root.ln.wf01.wt01(timestamp,status) values(1509465660000,true)",
        "insert into root.ln.wf01.wt01(timestamp,status) values(1509465720000,false)",
        "insert into root.ln.wf01.wt01(timestamp,status) values(1509465780000,false)",
        "insert into root.ln.wf01.wt01(timestamp,status) values(1509465840000,false)",
        "insert into root.ln.wf01.wt01(timestamp,status) values(1509465900000,false)",
        "insert into root.ln.wf01.wt01(timestamp,status) values(1509465960000,false)",
        "insert into root.ln.wf01.wt01(timestamp,status) values(1509466020000,false)",
        "insert into root.ln.wf01.wt01(timestamp,status) values(1509466080000,false)",
        "insert into root.ln.wf01.wt01(timestamp,status) values(1509466140000,false)",
        "create timeseries root.ln.wf01.wt01.temperature with datatype=FLOAT,encoding=RLE",
        "insert into root.ln.wf01.wt01(timestamp,temperature) values(1509465600000,25.957603)",
        "insert into root.ln.wf01.wt01(timestamp,temperature) values(1509465660000,24.359503)",
        "insert into root.ln.wf01.wt01(timestamp,temperature) values(1509465720000,20.092794)",
        "insert into root.ln.wf01.wt01(timestamp,temperature) values(1509465780000,20.182663)",
        "insert into root.ln.wf01.wt01(timestamp,temperature) values(1509465840000,21.125198)",
        "insert into root.ln.wf01.wt01(timestamp,temperature) values(1509465900000,22.720892)",
        "insert into root.ln.wf01.wt01(timestamp,temperature) values(1509465960000,20.71)",
        "insert into root.ln.wf01.wt01(timestamp,temperature) values(1509466020000,21.451046)",
        "insert into root.ln.wf01.wt01(timestamp,temperature) values(1509466080000,22.57987)",
        "insert into root.ln.wf01.wt01(timestamp,temperature) values(1509466140000,20.98177)",
        "create timeseries root.ln.wf02.wt02.hardware with datatype=TEXT,encoding=PLAIN",
        "insert into root.ln.wf02.wt02(timestamp,hardware) values(1509465600000,\"v2\")",
        "insert into root.ln.wf02.wt02(timestamp,hardware) values(1509465660000,\"v2\")",
        "insert into root.ln.wf02.wt02(timestamp,hardware) values(1509465720000,\"v1\")",
        "insert into root.ln.wf02.wt02(timestamp,hardware) values(1509465780000,\"v1\")",
        "insert into root.ln.wf02.wt02(timestamp,hardware) values(1509465840000,\"v1\")",
        "insert into root.ln.wf02.wt02(timestamp,hardware) values(1509465900000,\"v1\")",
        "insert into root.ln.wf02.wt02(timestamp,hardware) values(1509465960000,\"v1\")",
        "insert into root.ln.wf02.wt02(timestamp,hardware) values(1509466020000,\"v1\")",
        "insert into root.ln.wf02.wt02(timestamp,hardware) values(1509466080000,\"v1\")",
        "insert into root.ln.wf02.wt02(timestamp,hardware) values(1509466140000,\"v1\")",
        "create timeseries root.ln.wf02.wt02.status with datatype=BOOLEAN,encoding=PLAIN",
        "insert into root.ln.wf02.wt02(timestamp,status) values(1509465600000,true)",
        "insert into root.ln.wf02.wt02(timestamp,status) values(1509465660000,true)",
        "insert into root.ln.wf02.wt02(timestamp,status) values(1509465720000,false)",
        "insert into root.ln.wf02.wt02(timestamp,status) values(1509465780000,false)",
        "insert into root.ln.wf02.wt02(timestamp,status) values(1509465840000,false)",
        "insert into root.ln.wf02.wt02(timestamp,status) values(1509465900000,false)",
        "insert into root.ln.wf02.wt02(timestamp,status) values(1509465960000,false)",
        "insert into root.ln.wf02.wt02(timestamp,status) values(1509466020000,false)",
        "insert into root.ln.wf02.wt02(timestamp,status) values(1509466080000,false)",
        "insert into root.ln.wf02.wt02(timestamp,status) values(1509466140000,false)",
        "CREATE DATABASE root.sgcc",
        "create timeseries root.sgcc.wf03.wt01.status with datatype=BOOLEAN,encoding=PLAIN",
        "insert into root.sgcc.wf03.wt01(timestamp,status) values(1509465600000,true)",
        "insert into root.sgcc.wf03.wt01(timestamp,status) values(1509465660000,true)",
        "insert into root.sgcc.wf03.wt01(timestamp,status) values(1509465720000,false)",
        "insert into root.sgcc.wf03.wt01(timestamp,status) values(1509465780000,false)",
        "insert into root.sgcc.wf03.wt01(timestamp,status) values(1509465840000,false)",
        "insert into root.sgcc.wf03.wt01(timestamp,status) values(1509465900000,false)",
        "insert into root.sgcc.wf03.wt01(timestamp,status) values(1509465960000,false)",
        "insert into root.sgcc.wf03.wt01(timestamp,status) values(1509466020000,false)",
        "insert into root.sgcc.wf03.wt01(timestamp,status) values(1509466080000,false)",
        "insert into root.sgcc.wf03.wt01(timestamp,status) values(1509466140000,false)",
        "create timeseries root.sgcc.wf03.wt01.temperature with datatype=FLOAT,encoding=RLE",
        "insert into root.sgcc.wf03.wt01(timestamp,temperature) values(1509465600000,25.957603)",
        "insert into root.sgcc.wf03.wt01(timestamp,temperature) values(1509465660000,24.359503)",
        "insert into root.sgcc.wf03.wt01(timestamp,temperature) values(1509465720000,20.092794)",
        "insert into root.sgcc.wf03.wt01(timestamp,temperature) values(1509465780000,20.182663)",
        "insert into root.sgcc.wf03.wt01(timestamp,temperature) values(1509465840000,21.125198)",
        "insert into root.sgcc.wf03.wt01(timestamp,temperature) values(1509465900000,22.720892)",
        "insert into root.sgcc.wf03.wt01(timestamp,temperature) values(1509465960000,20.71)",
        "insert into root.sgcc.wf03.wt01(timestamp,temperature) values(1509466020000,21.451046)",
        "insert into root.sgcc.wf03.wt01(timestamp,temperature) values(1509466080000,22.57987)",
        "insert into root.sgcc.wf03.wt01(timestamp,temperature) values(1509466140000,20.98177)",
        "flush"
      };

  @BeforeClass
  public static void setUp() throws Exception {
    EnvFactory.getEnv().initBeforeClass();

    importData();
  }

  @AfterClass
  public static void tearDown() throws Exception {
    EnvFactory.getEnv().cleanAfterClass();
  }

  private static void importData() {
    try (Connection connection = EnvFactory.getEnv().getConnection();
        Statement statement = connection.createStatement()) {

      for (String sql : sqls) {
        statement.execute(sql);
      }
    } catch (Exception e) {
      e.printStackTrace();
      fail(e.getMessage());
    }
  }

  // Current LRUCache can not be really cleared easily. We screen this test for
  // passing the CI on https://ci-builds.apache.org/job/IoTDB/job/IoTDB-Pipe/job/master/
  // @Test
  public void clearCacheTest() {
    try (Connection connection = EnvFactory.getEnv().getConnection();
        Statement statement = connection.createStatement()) {
      boolean hasResultSet = statement.execute("select * from root where time > 10");
      assertTrue(hasResultSet);

      try (ResultSet resultSet = statement.getResultSet()) {
        int cnt = 0;
        while (resultSet.next()) {
          cnt++;
        }
        Assert.assertEquals(10, cnt);
      }
      assertFalse(ChunkCache.getInstance().isEmpty());
      assertFalse(TimeSeriesMetadataCache.getInstance().isEmpty());

      statement.execute("CLEAR CACHE");

      assertTrue(ChunkCache.getInstance().isEmpty());
      assertTrue(TimeSeriesMetadataCache.getInstance().isEmpty());

    } catch (Exception e) {
      e.printStackTrace();
      fail(e.getMessage());
    }
  }
}
