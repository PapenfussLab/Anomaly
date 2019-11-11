package org.petermac.nic.api;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.petermac.nic.dataminer.domain.pathos.vcf.core.DerivedSchema;
import org.petermac.nic.dataminer.domain.pathos.vcf.core.GenomeBuild;
import org.petermac.nic.dataminer.domain.pathos.vcf.dao.AnnotationSourceDao;
import org.petermac.nic.dataminer.domain.pathos.vcf.sources.AnnotationSource;
import org.petermac.nic.dataminer.domain.pathos.vcf.sources.AnnotationSourceFactories;
import org.petermac.nic.dataminer.domain.pathos.vcf.sources.AnnotationSourceFactory;
import org.petermac.nic.dataminer.domain.pathos.vcf.sources.AnnotationSourcesProvider;
import org.petermac.nic.dataminer.parse.JSON;
import org.petermac.nic.dataminer.proj.HalTraverse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * Created by Nic on 5/12/2018.
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = {TestConfig.class, org.petermac.nic.dataminer.Application.class})
@WebAppConfiguration
public class RepoAnnotationSourceFactoryTest
{
    @Autowired
    RepoAnnotationSourceFactory repoAnnotationSourceFactory;

    @Autowired
    AnnotationSourceFactories annotationSourceFactories;

    @Test
    public void getAnnotationSources() throws Exception
    {
        AnnotationSourceFactory sf = repoAnnotationSourceFactory;
        String actual = sf.getAnnotationSources(s -> s, AnnotationSource.Mutalyzer.getName()).toString();
        System.err.println(actual);
        assertEquals("[Mutalyzer]", actual);
        actual = sf.getAnnotationSources(s -> s, AnnotationSource.Mutalyzer.getName(), AnnotationSource.Vep.getName()).toString();
        System.err.println(actual);
        assertEquals("[Vep, Mutalyzer]", actual);

        actual = sf.getAnnotationSources(s -> s, AnnotationSource.Mutalyzer.getName(), AnnotationSource.Vep.getName(), "NK").toString();
        System.err.println(actual);
        assertEquals("[Vep, Mutalyzer, NK]", actual);

    }


    @Test
    public void testViaAPI() throws Exception
    {
        final Map<String, Object> arguments = new HashMap<>();
        arguments.put("name", "PrefTx");

        final HalTraverse<AnnotationSourceDao> pager = new HalTraverse<>(
                "http://localhost:8080",
                "/api/annotationSourceDaos/search/findByName?name={name}", arguments,
                AnnotationSourceDao.RESOURCE_TYPE);

        final AnnotationSourceDao dao = pager.getFirstPage().iterator().next();
        System.err.println("get ONE = " + dao.format());
    }


    @Test
    public void testBadDynamic() throws Exception
    {


        final AnnotationResponseBuilder annotationResponseBuilder = new AnnotationResponseBuilder();

        try
        {
            annotationSourceFactories.add(repoAnnotationSourceFactory); //

            final String variant = TestVariants.variant;
            final AnnotationSourcesProvider supportedSources = annotationSourceFactories.getSupportedSources(GenomeBuild.GRCh37, "Nick+");//, AnnotationSource.Mutalyzer.addPlus());
            assertTrue(supportedSources.getAnnotationSources().isEmpty());
            final DerivedSchema derivedSchema = new DerivedSchema();
            derivedSchema.getSchema().put("clinGenARId", "sourceResults.clinGenAR.@id");
            derivedSchema.getSchema().put("clinGenERContext", "sourceResults.clinGenER.@context");
            final Response response = annotationResponseBuilder.annotate(variant, derivedSchema, supportedSources);

            fail("Nto Exception ?  java.lang.IllegalArgumentException: NO Annotation Sources Provided!");
            //Alternate behaviour is Exception is not thrown = we can decide later what we want.
            assertEquals(0, response.getSources().size());
            assertEquals(1, response.getNumberOfResults());
            assertEquals(1, response.getOk());
            assertEquals(1, response.getNumberOfResultErrors());
            assertEquals(0, response.getOkResults().get(variant).sourceResults.size()); //EMPTY !!
            System.err.println(response.getVariants());
            System.err.println(JSON.format(response));
        } catch (IllegalArgumentException e)
        {
            System.err.println(e.toString());
        }

    }




}