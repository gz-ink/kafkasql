package com.looboo.kafkasql.assemble;

import com.looboo.kafkasql.kafka.KafkaUtil;
import com.looboo.kafkasql.parser.KafkaSqlLexer;
import com.looboo.kafkasql.parser.KafkaSqlParser;
import lombok.extern.slf4j.Slf4j;
import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;

import java.io.ByteArrayInputStream;
import java.io.IOException;

@Slf4j
public class KafkaSqlDriver {
    private final String SELECT_TOKEN = KafkaSqlLexer.VOCABULARY.getSymbolicName(KafkaSqlLexer.SELECT);

    private KafkaUtil kafkaUtil;

    public KafkaSqlDriver(KafkaUtil kafkaUtil) {
        this.kafkaUtil = kafkaUtil;
    }

    public String parsing(String sql) {
        log.info("sql is {}", sql);

        KafkaSqlParser parser = buildParser(sql.toUpperCase());
        ParseTree tree = parser.selectStatement();
        if (tree.getChildCount() == 0 || !tree.getChild(0).getText().equals(SELECT_TOKEN)) {
            log.warn("[syntax error] sql must start with select");
            return "";
        }

        return processSubStatement(tree.getChild(1));
    }

    private String processSubStatement(ParseTree tree) {
        SelectProcessor selectProcessor = SelectProcessorFactory.createProcessor(tree, kafkaUtil);
        return selectProcessor.process(tree);
    }

    private KafkaSqlParser buildParser(String sql)  {
        ByteArrayInputStream inputStream = new ByteArrayInputStream(sql.getBytes());
        ANTLRInputStream input = null;
        try {
            input = new ANTLRInputStream(inputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }

        KafkaSqlLexer lexer = new KafkaSqlLexer(input);
        CommonTokenStream tokenStream = new CommonTokenStream(lexer);
        return new KafkaSqlParser(tokenStream);
    }
}
