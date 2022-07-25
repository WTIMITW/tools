package org.opengauss.datachecker.extract.service.thread;

import lombok.extern.slf4j.Slf4j;
import org.opengauss.datachecker.common.util.IdWorker;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * @author wang chao
 * @description 数据抽取服务，基础数据打桩
 * @date 2022/5/8 19:27
 * @since 11
 **/
@Slf4j
public class ExtractMockDataThread implements Runnable {
    protected static final int MAX_INSERT_ROW_COUNT = 10000;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    protected JdbcTemplate jdbcTemplate;
    protected String tableName;
    protected long maxRowCount;
    protected int taskSn;


    /**
     * 线程构造函数
     *
     * @param dataSource  数据源注入
     * @param tableName   表名注入
     * @param maxRowCount 当前线程最大插入记录总数
     * @param taskSn      线程任务序列号
     */
    public ExtractMockDataThread(DataSource dataSource, String tableName, long maxRowCount, int taskSn) {
        jdbcTemplate = new JdbcTemplate(dataSource);
        this.tableName = tableName;
        this.maxRowCount = maxRowCount;
        this.taskSn = taskSn;
    }

    @Override
    public void run() {
        batchMockData(tableName, maxRowCount, taskSn);
    }


    public void batchMockData(String tableName, long threadMaxRowCount, int taskSn) {
        try {
            log.info("batch start insert: table:{},counut={},taskSn={}", tableName, threadMaxRowCount, taskSn);
            long batchInsertCount = threadMaxRowCount;
            long insertedCount = 0;
            // 循环分批次插入数据，限制SQL每次插入记录的最大数据量
            while (batchInsertCount >= MAX_INSERT_ROW_COUNT) {
                String batchSql = buildBatchSql(tableName, MAX_INSERT_ROW_COUNT, taskSn);
                jdbcTemplate.batchUpdate(batchSql);
                batchInsertCount = batchInsertCount - MAX_INSERT_ROW_COUNT;
                insertedCount += MAX_INSERT_ROW_COUNT;
                log.info("batch insert:threadMaxRowCount={},taskSn={},insertedCount={}", threadMaxRowCount, taskSn, insertedCount);
            }
            // 循环分批次插入数据，最后一个批次的数据插入
            if (batchInsertCount > 0 && batchInsertCount < MAX_INSERT_ROW_COUNT) {
                String batchSql = buildBatchSql(tableName, batchInsertCount, taskSn);
                jdbcTemplate.batchUpdate(batchSql);
                insertedCount += batchInsertCount;
                log.info("batch insert:totalRowCount={},taskSn={},insertedCount={}", threadMaxRowCount, taskSn, insertedCount);
            }
            log.info("batch end insert: table:{},counut={},taskSn={}", tableName, threadMaxRowCount, taskSn);
        } catch (Exception e) {
            log.error("batch insert:{},{},{}", threadMaxRowCount, taskSn, e.getMessage());
        }
    }

    private String buildBatchSql(String tableName, long rowCount, int ordler) {
        StringBuffer sb = new StringBuffer(MockMapper.INSERT.replace(":TABLENAME", tableName));
        for (int i = 0; i < rowCount; i++) {
            String id = IdWorker.nextId(String.valueOf(ordler));
            sb.append("(")
                    // b_number
                    .append("'").append(id).append("',")
                    // b_type
                    .append("'type_01',")
                    // b_user
                    .append("'user_02',")
                    //b_int
                    .append("1,")
                    //b_bigint
                    .append("32,")
                    // b_text
                    .append("'b_text_").append(id).append("',")
                    // b_longtext
                    .append("'b_longtext_").append(id).append("',")
                    // b_date
                    .append("'").append(DATE_FORMATTER.format(LocalDate.now())).append("',")
                    // b_datetime
                    .append("'").append(DATE_TIME_FORMATTER.format(LocalDateTime.now())).append("',")
                    // b_timestamp
                    .append("'").append(DATE_TIME_FORMATTER.format(LocalDateTime.now())).append("',")
                    // b_attr1
                    .append("'b_attr1_").append(id).append("',")
                    // b_attr2
                    .append("'b_attr2_").append(id).append("',")
                    // b_attr3
                    .append("'b_attr3_").append(id).append("',")
                    // b_attr4
                    .append("'b_attr4_").append(id).append("',")
                    // b_attr5
                    .append("'b_attr5_").append(id).append("',")
                    // b_attr6
                    .append("'b_attr6_").append(id).append("',")
                    // b_attr7
                    .append("'b_attr7_").append(id).append("',")
                    // b_attr8
                    .append("'b_attr8_").append(id).append("',")
                    // b_attr9
                    .append("'b_attr9_").append(id).append("',")
                    // b_attr10
                    .append("'b_attr10_").append(id).append("'")
                    .append(")")
                    .append(",");
        }
        int length = sb.length();
        sb.deleteCharAt(length - 1);
        return sb.toString();
    }


    interface MockMapper {
        String INSERT = "INSERT INTO :TABLENAME VALUES ";
    }
}
