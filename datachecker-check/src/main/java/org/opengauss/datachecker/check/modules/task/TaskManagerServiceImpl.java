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

package org.opengauss.datachecker.check.modules.task;

import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.opengauss.datachecker.check.cache.TableStatusRegister;
import org.opengauss.datachecker.common.entry.enums.Endpoint;
import org.opengauss.datachecker.common.exception.CheckingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;

/**
 * TaskManagerServiceImpl
 *
 * @author ：wangchao
 * @date ：Created in 2022/5/25
 * @since ：11
 */
@Slf4j
@Service
public class TaskManagerServiceImpl implements TaskManagerService {
    @Autowired
    private TableStatusRegister tableStatusRegister;

    /**
     * Refresh the execution status of the data extraction table of the specified task
     *
     * @param tableName tableName
     * @param endpoint  endpoint {@link org.opengauss.datachecker.common.entry.enums.Endpoint}
     * @param status    status
     */
    @Override
    public void refreshTableExtractStatus(String tableName, Endpoint endpoint, int status) {
        log.info("check server refresh endpoint=[{}]  extract tableName=[{}] status=[{}]  ", endpoint.getDescription(),
            tableName, status);
        tableStatusRegister.update(tableName, status);
    }

    /**
     * Initialize task status
     *
     * @param tableNameList table name list
     */
    @Override
    public void initTableExtractStatus(List<String> tableNameList) {
        if (tableStatusRegister.isEmpty() || tableStatusRegister.isCheckCompleted()) {
            cleanTaskStatus();
            tableStatusRegister.init(new HashSet<>(tableNameList));
            log.info("check server init extract tableNameList=[{}] ", JSON.toJSONString(tableNameList));
        } else {
            // The last verification process is being executed,
            // and the table verification status data cannot be reinitialized!
            throw new CheckingException("The last verification process is being executed,"
                + " and the table verification status data cannot be reinitialized!");
        }
    }

    /**
     * Clean up task status information
     */
    @Override
    public void cleanTaskStatus() {
        tableStatusRegister.removeAll();
    }

    /**
     * query check status of current table
     *
     * @param tableName tableName
     * @return status
     */
    @Override
    public int queryTableCheckStatus(String tableName) {
        return tableStatusRegister.get(tableName);
    }
}
