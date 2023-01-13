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

package org.opengauss.datachecker.check.load;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ConfigurableApplicationContext;

import javax.annotation.Resource;

/**
 * AbstractCheckLoader
 *
 * @author ：wangchao
 * @date ：Created in 2022/11/9
 * @since ：11
 */
@Slf4j
public abstract class AbstractCheckLoader implements CheckLoader {
    protected static final int RETRY_TIMES = 3;

    private static ConfigurableApplicationContext applicationContext;
    @Resource
    private CheckEnvironment checkEnvironment;

    /**
     * Verification environment global information loader
     *
     * @param checkEnvironment checkEnvironment
     */
    @Override
    public abstract void load(CheckEnvironment checkEnvironment);

    /**
     * Handle an application event.
     *
     * @param event the event to respond to
     */
    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        applicationContext = event.getApplicationContext();
        load(checkEnvironment);
    }

    /**
     * shutdown app
     *
     * @param message shutdown message
     */
    public void shutdown(String message) {
        log.error("The check server will be shutdown , {}", message);
        log.error("check server exited .");
        System.exit(SpringApplication.exit(applicationContext));
    }
}