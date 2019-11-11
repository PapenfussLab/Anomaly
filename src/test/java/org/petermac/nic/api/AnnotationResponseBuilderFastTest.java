package org.petermac.nic.api;

import junit.framework.TestCase;
import org.junit.Test;
import org.petermac.nic.dataminer.domain.pathos.vcf.core.DerivedSchema;
import org.petermac.nic.dataminer.domain.pathos.vcf.core.GenomeBuild;
import org.petermac.nic.dataminer.domain.pathos.vcf.sources.*;
import org.petermac.nic.dataminer.parse.JSON;

import java.util.Collections;
import java.util.Set;
import java.util.function.Function;

/**
 * NO Spring.
 * Created by Nic on 20/11/2018.
 */
public class AnnotationResponseBuilderFastTest
{
    @Test
    public void testAnnotateCGAR_At() throws Exception
    {

        final DerivedSchema derivedSchema = new DerivedSchema();
        derivedSchema.getSchema().put("clinGenARId", "ClinGenAR.@id");
//                "transcripts": "ClinGenAR.transcriptAlleles[*].hgvs"
//                derivedSchema.getSchema().put("clinGenERContext", "ClinGenER.@context");


        final AnnotationResponseBuilder annotationResponseBuilder = new AnnotationResponseBuilder();

        final AnnotationSourcesProvider sourcesProvider = new AnnotationSourcesProvider(GenomeBuild.GRCh37, AnnotationSource.ClinGenAR.getName());
        sourcesProvider.setSourceFactories(new BuiltInAnnotationSourceFactory()); //!!needed

        System.err.println(" getAnnotationSources=" + sourcesProvider.format());

        final Response response = annotationResponseBuilder.annotate(TestVariants.variant, derivedSchema, sourcesProvider);

        System.err.println(JSON.format(response));

    }

    @Test
    public void testDynamic() throws Exception
    {

        final AnnotationSourceFactory annotationSourceFactoryNick = new AnnotationSourceFactory()
        {

            @Override
            public Set<AnnotationURLProvider> getAnnotationSources(Function<String, String> function, String... strings) throws IllegalArgumentException
            {
                return getAnnotationSources();
            }

            @Override
            public Set<AnnotationURLProvider> getAnnotationSources()
            {
                return Collections.singleton(new AnnotationSourceDelegate("NickclinGenAR", "http://reg.genome.network", "/allele?hgvs={hgvsGVariant}", true, GenomeBuild.GRCh37));
            }
        };


        final AnnotationResponseBuilder annotationResponseBuilder = new AnnotationResponseBuilder();


        final AnnotationSourcesProvider sourcesProvider = new AnnotationSourcesProvider(GenomeBuild.GRCh37);
//        final AnnotationSourcesProvider sourcesProvider = new AnnotationSourcesProvider(GenomeBuild.GRCh38); //No Support !!
        sourcesProvider.setSourceFactories(annotationSourceFactoryNick);
        final DerivedSchema derivedSchema = new DerivedSchema();
        derivedSchema.getSchema().put("clinGenARId", "NickclinGenAR.@id");
        derivedSchema.getSchema().put("clinGenERContext", "NickclinGenAR.@context");


        final Response response = annotationResponseBuilder.annotate(TestVariants.variant, derivedSchema, sourcesProvider);

        System.err.println("JSON.format(response)");
        System.err.println(JSON.format(response));

        TestCase.assertEquals("http://reg.genome.network/allele/CA019278", response.getOkResults().get(TestVariants.variant).domainModel.derived.get("clinGenARId"));

    }
}