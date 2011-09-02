package com.alibaba.druid.pool.benckmark;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.NumberFormat;
import java.util.concurrent.CountDownLatch;

import javax.sql.DataSource;

import junit.framework.TestCase;

import org.apache.commons.dbcp.BasicDataSource;

import com.alibaba.druid.TestUtil;
import com.alibaba.druid.pool.DruidDataSource;

public class CaseKylin_Oracle extends TestCase {

    private String  jdbcUrl;
    private String  user;
    private String  password;
    private String  driverClass;
    private int     initialSize                   = 1;
    private int     minIdle                       = 1;
    private int     maxIdle                       = 14;
    private int     maxActive                     = 14;
    private int     maxWait                       = 60000;
    private String  validationQuery               = null;                                 // "SELECT 1";
    private int     threadCount                   = 15;
    private int     TEST_COUNT                    = 3;
    final int       LOOP_COUNT                    = 1000 * 100;
    private boolean testWhileIdle                 = true;
    private boolean testOnBorrow                  = true;
    private boolean testOnReturn                  = false;

    private boolean removeAbandoned               = true;
    private int     removeAbandonedTimeout        = 180;
    private long    timeBetweenEvictionRunsMillis = 60000;
    private long    minEvictableIdleTimeMillis    = 1800000;
    private int     numTestsPerEvictionRun        = 20;
    private String  validateQuery                 = null;

    private String  oracleDriverClass             = "com.alibaba.china.jdbc.SimpleDriver";

    protected void setUp() throws Exception {
        // jdbcUrl = "jdbc:fake:dragoon_v25masterdb";
        // user = "dragoon25";
        // password = "dragoon25";
        // driverClass = "com.alibaba.druid.mock.MockDriver";

        // jdbcUrl = "jdbc:mysql://10.20.153.104:3306/druid2";
        // user = "root";
        // password = "root";
        // driverClass = "com.mysql.jdbc.Driver";

        jdbcUrl = "jdbc:oracle:thin:@10.20.149.85:1521:ocnauto";
        user = "alibaba";
        password = "ccbuauto";
        driverClass = "oracle.jdbc.driver.OracleDriver";
    }

    public void test_perf() throws Exception {
        for (int i = 0; i < 5; ++i) {
            druid();
            dbcp();
        }
    }

    public void druid() throws Exception {
        DruidDataSource dataSource = new DruidDataSource();

        dataSource.setFilters("encoding");
        dataSource.setInitialSize(initialSize);
        dataSource.setMaxActive(maxActive);
        dataSource.setMaxIdle(maxIdle);
        dataSource.setMinIdle(minIdle);
        dataSource.setMaxWait(maxWait);
        dataSource.setPoolPreparedStatements(true);
        dataSource.setDriverClassName(driverClass);
        dataSource.setUrl(jdbcUrl);
        dataSource.setPoolPreparedStatements(true);
        dataSource.setUsername(user);
        dataSource.setPassword(password);
        dataSource.setValidationQuery(validationQuery);
        dataSource.setTestOnBorrow(testOnBorrow);
        dataSource.setTestOnBorrow(testWhileIdle);
        dataSource.setTestOnBorrow(testOnReturn);
        dataSource.setRemoveAbandoned(removeAbandoned);
        dataSource.setRemoveAbandonedTimeout(removeAbandonedTimeout);
        dataSource.setTimeBetweenEvictionRunsMillis(timeBetweenEvictionRunsMillis);
        dataSource.setMinEvictableIdleTimeMillis(minEvictableIdleTimeMillis);
        dataSource.setNumTestsPerEvictionRun(numTestsPerEvictionRun);
        dataSource.setValidationQuery(validateQuery);

        for (int i = 0; i < TEST_COUNT; ++i) {
            p0(dataSource, "druid", threadCount);
        }
        System.out.println();
    }

    public void dbcp() throws Exception {
        final BasicDataSource dataSource = new BasicDataSource();

        dataSource.setInitialSize(initialSize);
        dataSource.setMaxActive(maxActive);
        dataSource.setMaxIdle(maxIdle);
        dataSource.setMinIdle(minIdle);
        dataSource.setMaxWait(maxWait);
        dataSource.setPoolPreparedStatements(true);
        dataSource.setDriverClassName(oracleDriverClass);
        dataSource.setUrl(jdbcUrl);
        dataSource.setPoolPreparedStatements(true);
        dataSource.setUsername(user);
        dataSource.setPassword(password);
        dataSource.setValidationQuery(validationQuery);
        dataSource.setTestOnBorrow(testOnBorrow);
        dataSource.setTestOnBorrow(testWhileIdle);
        dataSource.setTestOnBorrow(testOnReturn);
        dataSource.setRemoveAbandoned(removeAbandoned);
        dataSource.setRemoveAbandonedTimeout(removeAbandonedTimeout);
        dataSource.setTimeBetweenEvictionRunsMillis(timeBetweenEvictionRunsMillis);
        dataSource.setMinEvictableIdleTimeMillis(minEvictableIdleTimeMillis);
        dataSource.setNumTestsPerEvictionRun(numTestsPerEvictionRun);
        dataSource.setValidationQuery(validateQuery);

        for (int i = 0; i < TEST_COUNT; ++i) {
            p0(dataSource, "dbcp", threadCount);
        }
        System.out.println();
    }

    private void p0(final DataSource dataSource, String name, int threadCount) throws Exception {

        final CountDownLatch startLatch = new CountDownLatch(1);
        final CountDownLatch endLatch = new CountDownLatch(threadCount);
        for (int i = 0; i < threadCount; ++i) {
            Thread thread = new Thread() {

                public void run() {
                    try {
                        startLatch.await();

                        for (int i = 0; i < LOOP_COUNT; ++i) {
                            Connection conn = dataSource.getConnection();
                            PreparedStatement stmt = conn.prepareStatement("SELECT 1 FROM DUAL");
                            ResultSet rs = stmt.executeQuery();
                            rs.next();
                            rs.getInt(1);
                            rs.close();
                            stmt.close();
                            conn.close();
                        }
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                    endLatch.countDown();
                }
            };
            thread.start();
        }
        long startMillis = System.currentTimeMillis();
        long startYGC = TestUtil.getYoungGC();
        long startFullGC = TestUtil.getFullGC();
        startLatch.countDown();
        endLatch.await();

        long millis = System.currentTimeMillis() - startMillis;
        long ygc = TestUtil.getYoungGC() - startYGC;
        long fullGC = TestUtil.getFullGC() - startFullGC;

        System.out.println("thread " + threadCount + " " + name + " millis : "
                           + NumberFormat.getInstance().format(millis) + ", YGC " + ygc + " FGC " + fullGC);
    }
}