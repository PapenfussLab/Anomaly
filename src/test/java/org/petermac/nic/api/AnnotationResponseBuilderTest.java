package org.petermac.nic.api;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.petermac.nic.dataminer.domain.pathos.vcf.core.GenomeBuild;
import org.petermac.nic.dataminer.domain.pathos.vcf.core.MapSchema;
import org.petermac.nic.dataminer.domain.pathos.vcf.services.RestDerivedSchema;
import org.petermac.nic.dataminer.domain.pathos.vcf.sources.AnnotationSourceFactories;
import org.petermac.nic.dataminer.domain.pathos.vcf.sources.AnnotationSourcesProvider;
import org.petermac.nic.dataminer.parse.JSON;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * Created by Nic on 20/11/2018.
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = {TestConfig.class, org.petermac.nic.dataminer.Application.class})
public class AnnotationResponseBuilderTest
{
    private Logger log = org.slf4j.LoggerFactory.getLogger(getClass());

    @Autowired
    AnnotationResponseBuilder annotationResponseBuilder;
    @Autowired
    RestDerivedSchema derivedSchemaService;
    @Autowired
    AnnotationSourceFactories annotationSourceFactories;

    @Test
    public void testDynamicSources() throws Exception
    {
        final String schemaName = "prefTxSchema";
        final MapSchema derivedSchema = derivedSchemaService.readSchemaByName(schemaName);
        //todo should not have vcf. - because its only in the Alias.

        final AnnotationSourcesProvider supportedSources = annotationSourceFactories.getSupportedSources(GenomeBuild.GRCh37);

        log.info("AnnotationSourcesProvider = {} ", supportedSources.format());
        log.info("AnnotationSourcesProvider.getAnnotationSources() = {} ", supportedSources.getAnnotationSources());

        Response response = annotationResponseBuilder.annotate(TestVariants.variant, derivedSchema, supportedSources);

        log.info("OK response = {}", response.getOkResults());
        log.info("FAILED response = {}", response.getFailedResults());


        response.resultStream().forEach(result -> {
            System.err.println(JSON.format(result));
        });
        Assert.assertEquals(1, response.getNumberOfResults());

        response.resultStream().forEach(result -> Assert.assertEquals(TestVariants.variantPrefTx, result.domainModel.derived.get("clinGenPreferred")));
    }
}