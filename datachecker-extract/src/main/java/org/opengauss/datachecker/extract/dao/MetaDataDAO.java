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

package org.opengauss.datachecker.extract.dao;

import org.opengauss.datachecker.common.entry.extract.ColumnsMetaData;
import org.opengauss.datachecker.common.entry.extract.MetadataLoadProcess;
import org.opengauss.datachecker.common.entry.extract.TableMetadata;

import java.util.List;

/**
 * MetaDataDAO
 *
 * @author ：wangchao
 * @date ：Created in 2022/6/23
 * @since ：11
 */
public interface MetaDataDAO {
    /**
     * health
     *
     * @return health status
     */
    boolean health();

    /**
     * Return metadata loading progress
     *
     * @return metadata loading progress
     */
    MetadataLoadProcess getMetadataLoadProcess();

    /**
     * Query table metadata
     *
     * @return table metadata information
     */
    List<TableMetadata> queryTableMetadata();

    /**
     * Query the metadata information of the corresponding column of the table
     *
     * @param tableName tableName
     * @return Column metadata information
     */
    List<ColumnsMetaData> queryColumnMetadata(String tableName);

    /**
     * Query the metadata information of the corresponding column of the table
     *
     * @param tableNames tableNames
     * @return Column metadata information
     */
    List<ColumnsMetaData> queryColumnMetadata(List<String> tableNames);

}
