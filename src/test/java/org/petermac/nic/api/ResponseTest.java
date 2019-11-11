package org.petermac.nic.api;

import junit.framework.TestCase;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.petermac.nic.dataminer.domain.pathos.vcf.core.DerivedSchema;
import org.petermac.nic.dataminer.domain.pathos.vcf.core.GenomeBuild;
import org.petermac.nic.dataminer.domain.pathos.vcf.services.RepoCachingService;
import org.petermac.nic.dataminer.domain.pathos.vcf.services.ResourceDerivedSchema;
import org.petermac.nic.dataminer.domain.pathos.vcf.sources.AnnotationSource;
import org.petermac.nic.dataminer.domain.pathos.vcf.sources.AnnotationSourceFactories;
import org.petermac.nic.dataminer.domain.pathos.vcf.sources.AnnotationSourcesProvider;
import org.petermac.nic.dataminer.parse.JSON;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.client.RestTemplate;

import java.util.Collection;

/**
 * Created by Nic on 1/04/2019.
 */

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {TestConfig.class, org.petermac.nic.dataminer.Application.class})
public class ResponseTest
{

    @Autowired
    ResourceDerivedSchema resourceDerivedSchema;

    @Autowired
    VepFiltered vepFiltered;

    @Autowired
    RepoCachingService cachingService;

    public static final String variant = TestVariants.variant;

    @Test
    public void testResponse()
    {
        AnnotationSourceFactories annotationSourceFactories = new AnnotationSourceFactories();

        final AnnotationSourcesProvider annotationSourcesProvider = annotationSourceFactories.getSupportedSources(GenomeBuild.GRCh37, AnnotationSource.ALL.addPlus());//deals with all the argument nuances

        System.err.println(annotationSourcesProvider.format());

        final Response response = new Response(variant, annotationSourcesProvider.getGenomeBuild(), annotationSourcesProvider.getAnnotationSourcesMap(cachingService));

        System.err.println(JSON.format(response));
    }

    @Test
    public void testDynamicSources() throws Exception
    {
        final DerivedSchema schema = resourceDerivedSchema.readSchemaByName("pathos_schema.json");
        System.err.println("Aliases" + schema.getAliases());
//        System.err.println("DerivedSchema Root Elements=" + schema.getRootElements());
//        TestCase.assertEquals("[Anomaly, ClinGenAR, MyVariant, Vep]", schema.getRootElements().toString());
        System.err.println("variant=" + schema.getString("variant"));
        TestCase.assertEquals("Anomaly.vcf.variantCall.info.HGVSg", schema.getString("variant"));

        { //Equality test
            final DomainModel dm = new DomainModel(resourceDerivedSchema.readSchemaByName("pathos_schema"));
//            System.err.println("DomainModel Root Elements=" + dm.getRootElements());
//            TestCase.assertEquals(schema.getRootElements().toString(), dm.getRootElements().toString());
        }

        final AnnotationSourceFactories annotationSourceFactories = new AnnotationSourceFactories();
//        AnnotationSourcesProvider annotationSourcesProvider = annotationSourceFactories.getSupportedSources(GenomeBuild.GRCh37,
//                schema.getRootElements().toArray(new String[0]));//deals with all the argument nuances

//        System.err.println(annotationSourcesProvider.getAnnotationSources());
//        TestCase.assertEquals("[ClinGenAR, MyVariant, Vep]", annotationSourcesProvider.getAnnotationSources().toString());

//        System.err.println(annotationSourcesProvider.format());

//        final Response response = new Response(variant, annotationSourcesProvider);

        final RestTemplate restTemplate = new RestTemplate();
//        response.resultStream().forEach(result -> {
//            for (AnnotationURLProvider annotationURLProvider : annotationSourcesProvider.getAnnotationSources())
//            {
//                result.createSourceResult(restTemplate, new AnnotationCacheWrapper(annotationURLProvider, cachingService), vepFiltered);
//            }
//        });


//        System.err.println(JSON.format(response));
//        DomainModel dm = result.getModel(schema);
//        System.err.println(dm.formatAsJson());

//        response.writeModels(schema); //clears results
//        System.err.println(JSON.format(response)); //check cleared

//        Response.Result result = response.getOkResults().values().iterator().next();

//        DomainModel dm = result.domainModel;
        //Need to abolish "sourceResults" dependency
//        TestCase.assertEquals("[\"MSH2\"]", dm.getJsonParser().getAsString("derived.geneSymbol"));
//        TestCase.assertEquals(1, ((Collection) dm.getJsonParser().get("derived.geneSymbol")).size());
//        TestCase.assertNotNull("derived.geneSymbol is a List ?", dm.getJsonParser().getList("derived.geneSymbol"));
    }

    @Test
    public void testResult()
    {
        final Response.Result result = new Response.Result(GenomeBuild.GRCh37, variant, null);

//        result.createSourceResult(new RestTemplate(), AnnotationSource.ClinGenAR, null);

        System.err.println(result.formatAsJson());

    }

    @Test
    public void testWriteModel() throws Exception
    {
        JSON.logFormattingTime = true;
        final Response.Result result = new Response.Result(GenomeBuild.GRCh37, variant, null);

        final RestTemplate restTemplate = new RestTemplate();
        // Explicit sources
//        result.createSourceResult(restTemplate, AnnotationSource.ClinGenAR, null, cachingService);
//        result.createSourceResult(restTemplate, AnnotationSource.Vep.getAnnotationURLProvider(), vepFiltered, cachingService);

        result.writeModel(resourceDerivedSchema.readSchemaByName("derived.json"));
        System.err.println(result.formatAsJson());


        TestCase.assertEquals("[\"MSH2\"]", result.getJsonParser().getAsString("domainModel.derived.geneSymbol"));
        TestCase.assertEquals(1, ((Collection) result.getJsonParser().get("domainModel.derived.geneSymbol")).size());
        TestCase.assertNotNull(result.getJsonParser().getList("domainModel.derived.geneSymbol"));
    }

    @Test
    public void testWriteModelOnDemandSources() throws Exception
    {
        JSON.logFormattingTime = true;
        final DerivedSchema schema = resourceDerivedSchema.readSchemaByName("derived.json");

        final Response.Result result = new Response.Result(GenomeBuild.GRCh37, variant, null);

        result.writeModel(schema);
        System.err.println(result.formatAsJson());


        TestCase.assertEquals("[\"MSH2\"]", result.getJsonParser().getAsString("domainModel.derived.geneSymbol"));
        TestCase.assertEquals(1, ((Collection) result.getJsonParser().get("domainModel.derived.geneSymbol")).size());
        TestCase.assertNotNull(result.getJsonParser().getList("domainModel.derived.geneSymbol"));
    }
}