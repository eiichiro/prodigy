package io.eiichiro.prodigy;

import com.amazonaws.services.lambda.runtime.log4j2.LambdaAppender;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.AppenderRef;
import org.apache.logging.log4j.core.config.LoggerConfig;
import org.apache.logging.log4j.core.layout.PatternLayout;
import org.eiichiro.reverb.system.Environment;

public class LambdaLogFactory {

    private static final String PRODIGY_LOG_LEVEL = "PRODIGY_LOG_LEVEL";

    private static Boolean configured = false;

    public static Log getLog(Class<?> clazz) {
        if (!configured) {
            synchronized (configured) {
                if (!configured) {
                    configure();
                    configured = true;
                }
            }
        }

        return LogFactory.getLog(clazz);
    }

    private static void configure() {
        LoggerContext context = (LoggerContext) LogManager.getContext(false);
        org.apache.logging.log4j.core.config.Configuration configuration = context.getConfiguration();
        PatternLayout layout = PatternLayout.newBuilder()
                .withPattern("%d{HH:mm:ss.SSS} [%t] %-5level %logger{36} - %msg%n%throwable")
                .withConfiguration(configuration).build();
        Appender appender = LambdaAppender.newBuilder().withLayout(layout).setConfiguration(configuration)
                .withName("Lambda").build();
        appender.start();
        configuration.addAppender(appender);
        AppenderRef ref = AppenderRef.createAppenderRef("Lambda", null, null);
        AppenderRef[] refs = new AppenderRef[] {ref};
        String level = Environment.getenv(PRODIGY_LOG_LEVEL);
        Level l = (level == null) ? Level.INFO : Level.valueOf(level);
        LoggerConfig config = LoggerConfig.createLogger(false, l, "io.eiichiro.prodigy", String.valueOf(false), refs,
                null, configuration, null);
        config.addAppender(appender, null, null);
        configuration.addLogger("io.eiichiro.prodigy", config);
        context.updateLoggers();
    }

}
