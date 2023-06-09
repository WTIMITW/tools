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

package org.opengauss.datachecker.extract.kafka;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.DeleteTopicsResult;
import org.apache.kafka.clients.admin.KafkaAdminClient;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.admin.TopicListing;
import org.apache.kafka.common.KafkaFuture;
import org.opengauss.datachecker.common.exception.CreateTopicException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.KafkaException;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

/**
 * kafka Topic admin
 *
 * @author ：wangchao
 * @date ：Created in 2022/5/17
 * @since ：11
 */
@Component
@Slf4j
public class KafkaAdminService {
    @Value("${spring.kafka.bootstrap-servers}")
    private String springKafkaBootstrapServers;
    private AdminClient adminClient;

    /**
     * Initialize Admin Client
     */
    @PostConstruct
    private void initAdminClient() {
        Map<String, Object> props = new HashMap<>(1);
        props.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, springKafkaBootstrapServers);
        adminClient = KafkaAdminClient.create(props);
        try {
            adminClient.listTopics().listings().get();
        } catch (ExecutionException | InterruptedException ex) {
            log.error("kafka Client link exception: ", ex);
            throw new KafkaException("kafka Client link exception");
        }
    }

    /**
     * Create a Kafka theme. If it exists, it will not be created.
     *
     * @param topic      topic
     * @param partitions partitions
     * @return topic name
     */
    public String createTopic(String topic, int partitions) {
        try {
            KafkaFuture<Set<String>> names = adminClient.listTopics().names();
            if (names.get().contains(topic)) {
                return topic;
            } else {
                adminClient.createTopics(List.of(new NewTopic(topic, partitions, (short) 1)));
                log.info("topic={} create,numPartitions={}, short replicationFactor={}", topic, partitions, 1);
                return topic;
            }
        } catch (InterruptedException | ExecutionException e) {
            log.error("topic={} is delete error : {}", topic, e);
            throw new CreateTopicException(topic);
        }
    }

    /**
     * Delete topic and support batch
     *
     * @param topics topic
     */
    public void deleteTopic(Collection<String> topics) {
        DeleteTopicsResult deleteTopicsResult = adminClient.deleteTopics(topics);
        Map<String, KafkaFuture<Void>> kafkaFutureMap = deleteTopicsResult.topicNameValues();
        kafkaFutureMap.forEach((topic, future) -> {
            try {
                future.get();
                log.info("topic={} is delete successfull", topic);
            } catch (InterruptedException | ExecutionException e) {
                log.error("topic={} is delete error : {}", topic, e);
            }
        });
    }

    /**
     * Gets the topic with the specified prefix
     *
     * @param prefix prefix
     * @return Topic with the specified prefix
     */
    public List<String> getAllTopic(String prefix) {
        try {
            log.info("topic prefix :{}", prefix);
            return adminClient.listTopics().listings().get().stream().map(TopicListing::name)
                              .filter(name -> name.startsWith(prefix)).collect(Collectors.toList());
        } catch (InterruptedException | ExecutionException e) {
            log.error("admin client get topic error:", e);
        }
        return new ArrayList<>();
    }

    /**
     * Gets all of the topics
     *
     * @return topics
     */
    public List<String> getAllTopic() {
        try {
            return adminClient.listTopics().listings().get().stream().map(TopicListing::name)
                              .collect(Collectors.toList());
        } catch (InterruptedException | ExecutionException e) {
            log.error("admin client get topic error:", e);
        }
        return new ArrayList<>();
    }

    /**
     * Check whether the current topic exists
     *
     * @param topicName topic Name
     * @return Does it exist
     */
    public boolean isTopicExists(String topicName) {
        try {
            return adminClient.listTopics().listings().get().stream().map(TopicListing::name)
                              .anyMatch(name -> name.equalsIgnoreCase(topicName));
        } catch (InterruptedException | ExecutionException e) {
            log.error("admin client get topic error:", e);
        }
        return false;
    }
}
