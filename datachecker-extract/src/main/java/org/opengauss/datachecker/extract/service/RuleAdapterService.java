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

package org.opengauss.datachecker.extract.service;

import org.apache.commons.collections4.CollectionUtils;
import org.opengauss.datachecker.common.entry.common.Rule;
import org.opengauss.datachecker.common.entry.enums.RuleType;
import org.opengauss.datachecker.common.entry.extract.ColumnsMetaData;
import org.opengauss.datachecker.common.entry.extract.TableMetadata;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * RuleAdapterService
 *
 * @author ：wangchao
 * @date ：Created in 2022/12/1
 * @since ：11
 */
@Service
public class RuleAdapterService {
    private static final Map<RuleType, List<Rule>> RULES = new HashMap<>();

    @Resource
    private TableRuleAdapterService tableRuleAdapterService;
    @Resource
    private ColumnRuleAdapterService columnRuleAdapterService;
    @Resource
    private RowRuleAdapterService rowRuleAdapterService;

    public void init(Map<RuleType, List<Rule>> rules) {
        RULES.clear();
        RULES.putAll(rules);
    }

    public List<String> executeTableRule(List<String> tableList) {
        final List<Rule> rules = RULES.get(RuleType.TABLE);
        if (CollectionUtils.isEmpty(rules)) {
            return tableList;
        }
        return tableRuleAdapterService.executeTableRule(rules, tableList);
    }

    public List<ColumnsMetaData> executeColumnRule(List<ColumnsMetaData> columns) {
        final List<Rule> rules = RULES.get(RuleType.COLUMN);
        if (CollectionUtils.isEmpty(rules)) {
            return columns;
        }
        return columnRuleAdapterService.executeColumnRule(rules, columns);
    }

    public void executeRowRule(List<TableMetadata> tableMetadataList) {
        final List<Rule> rules = RULES.get(RuleType.ROW);
        if (CollectionUtils.isEmpty(rules)) {
            return;
        }
        rowRuleAdapterService.executeRowRule(rules,tableMetadataList);
    }
}
