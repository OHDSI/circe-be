/*
 * Copyright 2020 cknoll1.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
 /*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.ohdsi.circe;

import java.sql.SQLException;
import javax.sql.DataSource;
import org.apache.commons.lang3.StringUtils;
import org.dbunit.database.DatabaseConfig;
import org.dbunit.database.DatabaseDataSourceConnection;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.ext.postgresql.PostgresqlDataTypeFactory;
import org.junit.ClassRule;
import org.ohdsi.circe.helper.ResourceHelper;
import org.ohdsi.sql.SqlSplit;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 *
 * @author cknoll1
 */
public class AbstractDatabaseTest {

  @ClassRule
  public static PostgresSingletonRule pg = new PostgresSingletonRule(58915);

  protected static JdbcTemplate jdbcTemplate;

  protected static DataSource getDataSource() {
      return pg.getEmbeddedPostgres().getPostgresDatabase();
  }

  protected static IDatabaseConnection getConnection() throws SQLException {
    final IDatabaseConnection con = new DatabaseDataSourceConnection(getDataSource());
    con.getConfig().setProperty(DatabaseConfig.FEATURE_QUALIFIED_TABLE_NAMES, true);
    con.getConfig().setProperty(DatabaseConfig.PROPERTY_DATATYPE_FACTORY, new PostgresqlDataTypeFactory());
    return con;
  }
  
  protected static void prepareSchema(final String schemaName, final String schemaPath) {
    final String sql = StringUtils.replace(ResourceHelper.GetResourceAsString(schemaPath), "@schemaName", schemaName);
    jdbcTemplate.execute(String.format("DROP SCHEMA IF EXISTS %s CASCADE", schemaName));
    jdbcTemplate.execute(String.format("CREATE SCHEMA %s", schemaName));
    jdbcTemplate.batchUpdate(SqlSplit.splitSql(sql));
  }
  
  protected void truncateTable (String tableName) {
    jdbcTemplate.execute(String.format("TRUNCATE %s CASCADE",tableName));
  }
  protected void resetSequence(String sequenceName) {
    jdbcTemplate.execute(String.format("ALTER SEQUENCE %s RESTART WITH 1", sequenceName));
  }
}
