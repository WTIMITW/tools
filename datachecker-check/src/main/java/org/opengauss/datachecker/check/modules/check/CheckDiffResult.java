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

package org.opengauss.datachecker.check.modules.check;

import com.alibaba.fastjson.annotation.JSONType;
import lombok.Data;
import org.springframework.util.CollectionUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

/**
 * CheckDiffResult
 *
 * @author ：wangchao
 * @date ：Created in 2022/6/18
 * @since ：11
 */
@Data
@JSONType(orders = {"schema", "table", "topic", "partitions", "result", "message", "createTime", "keyInsertSet",
    "keyUpdateSet", "keyDeleteSet", "repairInsert", "repairUpdate", "repairDelete"})
public class CheckDiffResult {
    private String schema;
    private String table;
    private String topic;
    private int partitions;
    private LocalDateTime createTime;
    private String result;
    private String message;

    private Set<String> keyInsertSet;
    private Set<String> keyUpdateSet;
    private Set<String> keyDeleteSet;

    private List<String> repairInsert;
    private List<String> repairUpdate;
    private List<String> repairDelete;

    /**
     * constructor
     */
    public CheckDiffResult() {
    }

    /**
     * constructor
     *
     * @param builder builder
     */
    public CheckDiffResult(final AbstractCheckDiffResultBuilder<?, ?> builder) {
        table = builder.getTable();
        partitions = builder.getPartitions();
        topic = builder.getTopic();
        schema = builder.getSchema();
        createTime = builder.getCreateTime();
        keyUpdateSet = builder.getKeyUpdateSet();
        keyInsertSet = builder.getKeyInsertSet();
        keyDeleteSet = builder.getKeyDeleteSet();
        repairUpdate = builder.getRepairUpdate();
        repairInsert = builder.getRepairInsert();
        repairDelete = builder.getRepairDelete();
        resultAnalysis();
    }

    private void resultAnalysis() {
        if (CollectionUtils.isEmpty(keyInsertSet) && CollectionUtils.isEmpty(keyUpdateSet) && CollectionUtils
            .isEmpty(keyDeleteSet)) {
            result = "success";
            message = schema.concat(".").concat(table).concat("_[").concat(String.valueOf(partitions))
                            .concat("] check success");
        } else {
            result = "failed";
            message =
                schema.concat(".").concat(table).concat("_[").concat(String.valueOf(partitions)).concat("] check : ")
                      .concat(" insert=" + keyInsertSet.size()).concat(" update=" + keyUpdateSet.size())
                      .concat(" delete=" + keyDeleteSet.size());
        }
    }
}
