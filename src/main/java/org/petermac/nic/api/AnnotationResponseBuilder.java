package org.petermac.nic.api;

import org.petermac.nic.dataminer.domain.pathos.vcf.core.MapSchema;
import org.petermac.nic.dataminer.domain.pathos.vcf.services.CachingService;
import org.petermac.nic.dataminer.domain.pathos.vcf.services.RepoCachingService;
import org.petermac.nic.dataminer.domain.pathos.vcf.sources.AnnotationSourcesProvider;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Date;

/**
 * Created by Nic on 31/07/2018.
 */
@Service
public class AnnotationResponseBuilder
{
    private static Logger log = org.slf4j.LoggerFactory.getLogger(AnnotationResponseBuilder.class);

    private final CachingService<String, Object, String, Date> cachingService;
    private final RestTemplate restTemplate;

    /**
     * Testing Ctor
     */
    AnnotationResponseBuilder()
    {
        this.cachingService = null;
        this.restTemplate = null;
    }


    public AnnotationResponseBuilder(@Autowired RepoCachingService repoCachingService,
                                     @Autowired RestTemplateBuilder restTemplateBuilder)
    {
        this.cachingService = repoCachingService;

        log.info("restTemplateBuilder={}", restTemplateBuilder); //? why null for tests ?>
        restTemplate = restTemplateBuilder == null ? new RestTemplate() : restTemplateBuilder.build(); //so we can use actuator to monitor REST statistics !
        log.info("restTemplateBuilder.build()={}", restTemplate);
//        restTemplate.setErrorHandler(new DefaultResponseErrorHandler());
    }



    Response annotate(final String variant, final MapSchema derivedSchema, final AnnotationSourcesProvider sourcesProvider) throws IOException, URISyntaxException
    {
        log.info("variant2 schemaName={}; Arguments provider={} Aliases {}", derivedSchema.getSchemaName(), sourcesProvider.format(), derivedSchema.getAliases());
        final Response responseSingle = new Response(variant, sourcesProvider.getGenomeBuild(), sourcesProvider.getAnnotationSourcesMap(cachingService));
        responseSingle.writeModels(derivedSchema); //for single variant
        return responseSingle;
    }
}

    /*
     * @param response
     *
    private void runAnnotationsSerial(final Response response)
    {
    final Set<AnnotationURLProvider> sources = preparePipelineByVariant(response);//pre pass

    for (int resultIndex = 0; resultIndex < response.getNumberOfResults(); resultIndex++)
    for (AnnotationURLProvider source : sources)
    annotateSource(response.resultAt(resultIndex), source); //serial
    }

    private void runAnnotationsParallel(final Response response)
    {
    final Set<AnnotationURLProvider> sources = preparePipelineByVariant(response);//pre pass
    response.resultStream().forEach(result -> annotateSources(result, sources));
    }

    /**
     * @param response
     * @return
     * @deprecated Mutalyzer is not used, so this does nothing
     *
    private Set<AnnotationURLProvider> preparePipelineByVariant(final Response response)
    {
    final Set<AnnotationURLProvider> sources = new HashSet<>(response.getLiteralAnnotationSources()); //copy
    if (sources.remove(AnnotationSource.Mutalyzer.getAnnotationURLProvider())) //Special BULK Mutalyzer run
    {
    final Stream<MutalyzerResponse> mutResponseStream = builtIn.call(response.getVariants(), response.getBuild(), response.getAnnotatorLog());
    final Map<String, MutalyzerResponse> mutMapResponses = MutalyzerResponse.mapMutByVariant(mutResponseStream);

    if (response.getNumberOfResults() != mutMapResponses.size())
    log.warn(mutMapResponses.size() + " Mutalyzer results doesn't match #variants in VCF File " + response.getVariants().size());

    response.resultStream().peek(result -> {
    if (!mutMapResponses.containsKey(result.variant))
    log.warn("Mutalyzer response not found for requested variant {}", result.variant);
    })
    .forEach(result -> result.setMutalyzer(mutMapResponses.get(result.variant)));
    }
    return sources;
    }

    /**
     * @param result
     * @param urlProvider
     * @return
     *
    private void annotateSource(final Response.Result result, AnnotationURLProvider urlProvider)
    {

        log.info("variant={} annotateSource={} {}", result.variant, urlProvider);
        try
        {
            result.createSourceResult(restTemplate, urlProvider, vepFiltered, cachingService); //support for dynamic source injection
        } catch (Exception e)
        {
            log.warn(urlProvider.getName() + " Annotate ", e);
            result.setException(urlProvider, e);
        }
    }
}
/*
    private void annotateSources(Response.Result result, Set<AnnotationURLProvider> literalAnnotationSources)
    {
//        final TM tookTotal = TM.create();
        final int length = literalAnnotationSources.size();
        if (length < 1)
            return;

        final ExecutorService executorService = Executors.newFixedThreadPool(length);
        log.info("Running {} Asynchronous Annotations ", literalAnnotationSources.size());

        try
        {
            for (final AnnotationURLProvider source : literalAnnotationSources)
                executorService.submit(() -> {
                    try
                    {
                        annotateSource(result, new AnnotationCacheWrapper(source, cachingService));//parallel
                    } catch (Exception e)
                    {
                        result.setResultError(source, e.toString());
                        log.error("annotateSource " + source, e);
                    }
                });
        } finally
        {
            executorService.shutdown();//no more work to submit, release Threads.
            log.info("Wait for asynchronous completion");
            try
            {
                executorService.awaitTermination(1, TimeUnit.HOURS);
            } catch (Exception e)
            {
                result.setResultError(AnnotationSource.ALL.getAnnotationURLProvider(), e.toString());//response.setResultError(result, AnnotationSource.ALL, e.toString());
            }
            log.info("Asynchronous Annotations completed");
        }
    }
}
*/