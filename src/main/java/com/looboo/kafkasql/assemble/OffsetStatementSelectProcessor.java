package com.looboo.kafkasql.assemble;

import com.looboo.kafkasql.kafka.KafkaUtil;
import com.looboo.kafkasql.parser.KafkaSqlParser;
import lombok.extern.slf4j.Slf4j;
import org.antlr.v4.runtime.tree.ParseTree;
import org.apache.kafka.common.TopicPartition;

import java.util.Map;

import static com.looboo.kafkasql.assemble.Constant.OFFSETS;

@Slf4j
public class OffsetStatementSelectProcessor implements SelectProcessor {

    private KafkaUtil kafkaUtil;

    public OffsetStatementSelectProcessor(KafkaUtil util) {
        this.kafkaUtil = util;
    }

    @Override
    public void process(ParseTree tree) {
        if (!(tree instanceof KafkaSqlParser.OffsetStatementContext)) {
            return;
        }

        if (!tree.getChild(0).getText().equalsIgnoreCase(OFFSETS)) {
            log.warn("statement does not contents offsets {}", tree.getText());
        }

        String topicName = tree.getChild(2).getText();
        if (topicName == null || topicName.isEmpty()) {
            log.warn("topic name is null or empty {}", topicName);
            return;
        }

        String topic = kafkaUtil.listTopics().stream()
                .filter(t -> t.equalsIgnoreCase(topicName))
                .findFirst()
                .orElse(null);

        Map<TopicPartition, Long> offset = kafkaUtil.getOffset(topic);

        String s = formatResult(offset);
    }

    private String formatResult(Map<TopicPartition, Long> offset) {
        StringBuilder stringBuilder = new StringBuilder();
        for (TopicPartition topicPartition : offset.keySet()) {
            stringBuilder.append("topic: " + topicPartition.topic() + "\t\tpartition: " + topicPartition.partition() + "\t\t\t\toffset: " + offset.get(topicPartition));
            stringBuilder.append("\n");
        }
        System.out.println(stringBuilder.toString());
        return stringBuilder.toString();
    }
}
