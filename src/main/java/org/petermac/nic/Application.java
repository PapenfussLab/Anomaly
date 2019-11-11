package org.petermac.nic;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.apache.logging.log4j.util.Strings;
import org.petermac.nic.dataminer.domain.pathos.vcf.dao.AnnotationSourceDao;
import org.petermac.nic.dataminer.domain.pathos.vcf.repo.AnnotationSourceRepository;
import org.petermac.nic.dataminer.domain.pathos.vcf.services.RestDerivedSchema;
import org.petermac.nic.dataminer.proj.*;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.info.ProjectInfoAutoConfiguration;
import org.springframework.boot.autoconfigure.web.servlet.error.ErrorMvcAutoConfiguration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.integration.support.management.IntegrationManagementConfigurer;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Created by Nick Kravchenko on 8/06/2018.
 */
@Configuration
@EnableAutoConfiguration(exclude = {ErrorMvcAutoConfiguration.class})
@ConfigurationProperties(prefix = "application")  //as in application.yml
@SpringBootApplication(exclude = {org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration.class})
public class Application
{
    private static Logger log = org.slf4j.LoggerFactory.getLogger(Application.class);
    @Autowired
    ProxySettings proxySettings;

    @Autowired
    BuildInfo buildInfo;

    @Autowired
    AnnotationSourceRepository annotationSourceRepository;

    @Bean
    @Order(1)
    ApplicationRunner getApplicationRunner()
    {
        return args ->
        {
            try
            {
                if (Strings.isNotBlank(getTimeZone()))//Users can ultimately change the default.
                    SystemInfo.SetTimeZone(getTimeZone());
                log.info("TZ={}. Date={}", SystemInfo.GetTimeZone(), new Date());
//                _timeMe();
//                _timeMe();
                SpringBoot.LogVersion();
                log.info("BUILD: {}", buildInfo);
                log.info("{} Version {}", getName(), getVersion());
                log.info("Total JVM memory {}", SystemInfo.getJavaMemoryInfo());
                log.info("ANOMALY REST API {}", proxySettings.getLocalDomain());
//                _timeTest();
                _HalTest();
            } catch (Throwable e)
            {
                log.error("", e);
            }

            log.info("Application Command line processing completed.");
        };
    }

    private void _HalTest()
    {
        final List<String> schemas = new RestDerivedSchema().getSchemas();
        log.info("{} Supported schemas: {}", schemas.size(), schemas);
        if (schemas.isEmpty())
            log.warn("No Schemas defined !!");

        annotationSourceRepository.findAll().stream().map(AnnotationSourceDao::format).forEach(s -> log.info("Annotation Source {}", s));
    }

    public static void main(String[] args)
    {
        SystemInfo.SetTimeZone("Australia/Melbourne"); //the default for Boot up
        log.info("main({}) ANOMALY REST API", Arrays.asList(args));
        SpringBoot.LogVersion();
        SpringApplication.run(Application.class, args);
    }

    @Autowired
    ProjectInfoAutoConfiguration projectInfoAutoConfiguration;

    //Configuration properties
    public String version;
    public String name;
    public String timeZone;

    public String getVersion()
    {
        return version;
    }

    public void setVersion(String version)
    {
        this.version = version;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public String getTimeZone()
    {
        return timeZone;
    }

    public void setTimeZone(String timeZone)
    {
        this.timeZone = timeZone;
    }


//---------------------------------------------------------------------------------------//
//----------------------------- Metering Experiments ------------------------------------//
//---------------------------------------------------------------------------------------//
//------------------------------ Ignore below -------------------------------------------//

    /* --Test Timing
@Configuration
@EnableIntegration
//@EnableIntegrationMBeanExport(server = "mbeanServer", managedComponents = "input")
@EnableMBeanExport
@EnableIntegrationManagement(
        defaultLoggingEnabled = "true",
        defaultCountsEnabled = "true",
        defaultStatsEnabled = "true"
//        countsEnabled = { "foo", "${count.patterns}" },
//        statsEnabled = { "qux", "!*" },
//        metricsFactory = "myMetricsFactory")
)
@EnableAspectJAutoProxy
@EnableScheduling
@Timed(value = "AnomalyTimed", extraTags = {"att", "val"}, histogram = true, description = "AnomalyTimed description")
public class Application {
**/


    @Autowired
    IntegrationManagementConfigurer integrationManagementConfigurer;

    @Autowired
    MeterRegistry meterRegistry;

//    @Bean
//    public MBeanServerFactoryBean mbeanServer()
//    {
//        return new MBeanServerFactoryBean();
//    }

    public void _timeMe()
    {
        TM.sleep(1, TimeUnit.SECONDS);
    }

    private void _timeTest()
    {
        Counter c = meterRegistry.counter("nicksCounter");
        c.increment();
        c.increment();
//                integrationManagementConfigurer.getChannelMetrics("").getSendCountLong()
        log.info("getHandlerNames {}", Arrays.asList(integrationManagementConfigurer.getHandlerNames()));
        log.info("getChannelNames {}", Arrays.asList(integrationManagementConfigurer.getChannelNames()));
        log.info("getSourceNames {}", Arrays.asList(integrationManagementConfigurer.getSourceNames()));
    }


/* // Timing
    @Service
    static class MyService
    {

        @Autowired
        private MeterRegistry registry;

        public void helloManual()
        {
            // you can keep a ref to this; ok to call multiple times, though
            Timer timer = Timer.builder("myservice").tag("method", "manual").register(registry);

            // manually do the timing calculation
            long start = System.nanoTime();
            doSomething();
            timer.record(System.nanoTime() - start, TimeUnit.NANOSECONDS);
        }

        public void helloSupplier()
        {
            Timer timer = Timer.builder("myservice").tag("method", "supplier").register(registry);

            // execution of the method is timed internally
            timer.record(() -> doSomething());
        }

        public void helloSample()
        {
            Timer timer = Timer.builder("myservice").tag("method", "sample").register(registry);

            // records time taken between Sample creation and registering the
            // stop() with the given Timer
            Timer.Sample sample = Timer.start(registry);
            doSomething();
            sample.stop(timer);
        }

        // TimedAspect adds "class" and "method" tags
        @Timed(value = "myservice.aspect", histogram = true, description = "AOP AspectJ timer")
        public void helloAspect()
        {
            doSomething();
        }

        private void doSomething()
        {
            try
            {
                Thread.sleep(50);
            } catch (InterruptedException e)
            {
                //
            }
        }
    }

    @Autowired
    private MyService myService;

    @Bean
    TimedAspect timedAspect(MeterRegistry registry)
    {
        return new TimedAspect(registry);
    }

    @Scheduled(fixedRate = 1000)
    public void postConstruct()
    {
        myService.helloManual();
        myService.helloSupplier();
        myService.helloSample();
        myService.helloAspect();
    }
    **/
}
