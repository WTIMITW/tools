/*
 * Copyright (c) 2022-2022 Huawei Technologies Co.,Ltd.
 *
 * openGauss is licensed under Mulan PSL v2.
 * You can use this software according to the terms and conditions of the Mulan PSL v2.
 * You may obtain a copy of Mulan PSL v2 at:
 *
 *           http://license.coscl.org.cn/MulanPSL2
 *
 * THIS SOFTWARE IS PROVIDED ON AN "AS IS" BASIS, WITHOUT WARRANTIES OF ANY KIND,
 * EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO NON-INFRINGEMENT,
 * MERCHANTABILITY OR FIT FOR A PARTICULAR PURPOSE.
 * See the Mulan PSL v2 for more details.
 */

package org.opengauss.datachecker.extract.debe;

import lombok.RequiredArgsConstructor;
import org.opengauss.datachecker.common.entry.check.IncrementCheckTopic;
import org.opengauss.datachecker.common.entry.extract.SourceDataLog;
import org.opengauss.datachecker.common.util.ThreadUtil;
import org.opengauss.datachecker.extract.client.CheckingFeignClient;
import org.opengauss.datachecker.extract.config.ExtractProperties;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * IncrementDataAnalysisService
 *
 * @author ：wangchao
 * @date ：Created in 2022/7/4
 * @since ：11
 */
@RequiredArgsConstructor
@Service
public class IncrementDataAnalysisService {
    /**
     * Single thread scheduled task - execute check polling thread
     */
    private static final ScheduledExecutorService SCHEDULED_EXECUTOR = ThreadUtil.newSingleThreadScheduledExecutor();

    private final ExtractProperties extractProperties;
    private final DataConsolidationService consolidationService;
    private final CheckingFeignClient checkingFeignClient;

    /**
     * Used to record the offset of the last consumption of the incremental verification topic data,
     * which is the starting point of the next data consumption
     */
    private volatile AtomicLong lastOffSetAtomic = new AtomicLong(0L);

    /**
     * It is used to record the last execution time of the incremental verification topic data,
     * which is the starting point of the execution cycle of the next data consumption task
     */
    private volatile AtomicLong lastTimestampAtomic = new AtomicLong(0L);

    /**
     * Start the initialization load to verify the topic offset
     */
    @PostConstruct
    public void startIncrDataAnalysis() {
        if (extractProperties.getDebeziumEnable() && consolidationService.isSourceEndpoint()) {
            verificationConfiguration();
            IncrementCheckTopic topicRecordOffSet = consolidationService.getDebeziumTopicRecordOffSet();
            // Start the initialization load to verify the topic offset
            lastOffSetAtomic.set(topicRecordOffSet.getBegin());
            setLastTimestampAtomicCurrentTime();
            dataAnalysis();
        }
    }

    private void verificationConfiguration() {
        final int debeziumTimePeriod = extractProperties.getDebeziumTimePeriod();
        final int debeziumNumPeriod = extractProperties.getDebeziumNumPeriod();
        Assert.isTrue(debeziumTimePeriod > 0,
            "Debezium incremental migration verification, the time period should be greater than 0");
        Assert.isTrue(debeziumNumPeriod > 100, "Debezium incremental migration verification statistics:"
            + "the threshold value of the number of incremental change records should be greater than 100");
    }

    /**
     * Incremental log data record extraction scheduling task
     */
    public void dataAnalysis() {
        SCHEDULED_EXECUTOR
            .scheduleWithFixedDelay(peekDebeziumTopicRecordOffset(), DataNumAnalysisThreadConstant.INITIAL_DELAY,
                DataNumAnalysisThreadConstant.DELAY, TimeUnit.SECONDS);
    }

    /**
     * peekDebeziumTopicRecordOffset
     *
     * @return Incremental log data record extraction scheduling task thread
     */
    private Runnable peekDebeziumTopicRecordOffset() {
        return () -> {
            Thread.currentThread().setName(DataNumAnalysisThreadConstant.NAME);
            dataNumAnalysis();
            dataTimeAnalysis();
        };
    }

    /**
     * Incremental log data extraction and time latitude management
     */
    public void dataTimeAnalysis() {
        long time = System.currentTimeMillis();
        if ((time - lastTimestampAtomic.get()) >= extractProperties.getDebeziumTimePeriod()) {
            final List<SourceDataLog> debeziumTopicRecords =
                consolidationService.getDebeziumTopicRecords(extractProperties.getDebeziumTopic());
            if (!CollectionUtils.isEmpty(debeziumTopicRecords)) {
                checkingFeignClient.notifySourceIncrementDataLogs(debeziumTopicRecords);
                lastOffSetAtomic.addAndGet(debeziumTopicRecords.size());
            }
        }
        // Set the start calculation time point of the next time execution cycle
        lastTimestampAtomic.set(time);
    }

    /**
     * Incremental log data extraction, quantity and latitude management
     */
    public void dataNumAnalysis() {
        final long offset = consolidationService.getDebeziumTopicRecordEndOffSet();
        // Verify whether the data volume threshold dimension scenario trigger conditions are met
        if ((offset - lastOffSetAtomic.get()) >= extractProperties.getDebeziumNumPeriod()) {
            // When the data volume threshold is reached,
            // the data is extracted and pushed to the verification service.
            final List<SourceDataLog> debeziumTopicRecords =
                consolidationService.getDebeziumTopicRecords(extractProperties.getDebeziumTopic());
            checkingFeignClient.notifySourceIncrementDataLogs(debeziumTopicRecords);
            lastOffSetAtomic.addAndGet(debeziumTopicRecords.size());
            // Trigger data volume threshold dimension scenario - update time threshold
            setLastTimestampAtomicCurrentTime();
        }
    }

    private void setLastTimestampAtomicCurrentTime() {
        lastTimestampAtomic.set(System.currentTimeMillis());
    }

    interface DataNumAnalysisThreadConstant {
        /**
         * Data analysis thread pool thread name
         */
        String NAME = "DataAnalysisThread";

        /**
         * Data analysis thread pool initialization delay time
         */
        long INITIAL_DELAY = 0L;

        /**
         * Data analysis thread pool latency
         */
        long DELAY = 1L;
    }
}