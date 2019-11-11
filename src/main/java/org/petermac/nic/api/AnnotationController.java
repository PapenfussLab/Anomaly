package org.petermac.nic.api;

import org.petermac.nic.dataminer.domain.pathos.vcf.core.DerivedSchema;
import org.petermac.nic.dataminer.domain.pathos.vcf.core.GenomeBuild;
import org.petermac.nic.dataminer.domain.pathos.vcf.core.MapSchema;
import org.petermac.nic.dataminer.domain.pathos.vcf.services.*;
import org.petermac.nic.dataminer.domain.pathos.vcf.sources.AnnotationSource;
import org.petermac.nic.dataminer.domain.pathos.vcf.sources.AnnotationSourceFactories;
import org.petermac.nic.dataminer.domain.pathos.vcf.sources.AnnotationSourceFactory;
import org.petermac.nic.dataminer.domain.pathos.vcf.sources.AnnotationSourcesProvider;
import org.petermac.nic.dataminer.domain.pathos.vcf.transcode.VcfFile;
import org.petermac.nic.dataminer.parse.JSON;
import org.petermac.nic.dataminer.proj.BuildInfo;
import org.petermac.nic.dataminer.proj.SystemInfo;
import org.petermac.nic.dataminer.transcode.Strings;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.mvc.ControllerLinkBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.request.WebRequest;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.*;
import java.util.stream.Stream;

/**
 * Created by Nic on 25/05/2018.
 */
@RestController
@RequestMapping("/v1/annotate")// "/anomaly/v1/"
public class AnnotationController
{
    private static final Logger log = org.slf4j.LoggerFactory.getLogger(AnnotationController.class);

    private final DerivedSchemaService derivedSchemaService;
    private final PreferredTranscriptService transcriptsService;

    private final AnnotationSourceFactories sourceFactories;  //careful = component with shared info
    private final CachingService<String, Object, String, Date> cachingService;
    private final RestTemplate restTemplate;


//    private final WebApplicationContext webApplicationContext; //for getBean();

    private final ResultsTracker<ResponseHolder> RESULTS_TRACKER;

    @Autowired
    BuildInfo buildInfo;

    static final Date BOOT_TIME = new Date();

    /**
     * Ctor
     *
     * @param derivedSchemaService
     * @param annotationSourceFactories   Configure what factories we are going to support. BuiltIn - Dynamic
     * @param repoAnnotationSourceFactory Optional Dynamic Sources in Repo.
     * @param resultsTracker
     */
    public AnnotationController(@Autowired RestDerivedSchema derivedSchemaService,
                                @Autowired Transcripts transcriptsService,
                                @Autowired AnnotationSourceFactories annotationSourceFactories,
                                @Autowired RepoAnnotationSourceFactory repoAnnotationSourceFactory,
                                @Autowired ResultsTracker<ResponseHolder> resultsTracker,
                                @Autowired RepoCachingService repoCachingService,
                                @Autowired RestTemplateBuilder restTemplateBuilder)
    {
        this.sourceFactories = annotationSourceFactories;
        this.derivedSchemaService = derivedSchemaService;
        this.transcriptsService = transcriptsService;
        this.RESULTS_TRACKER = resultsTracker;
        this.cachingService = repoCachingService;
        this.restTemplate = restTemplateBuilder.build(); //so we can use actuator to monitor REST statistics !
        ;
        annotationSourceFactories.add(repoAnnotationSourceFactory); //CHOICE = todo Move into .yml
    }

    /**
     * localhost:8080/v1/annotate/error
     */
    @GetMapping("/error")
    public HttpEntity<Payload<String, AnnotationController>> error() //throws IOException//, URISyntaxException
    {
        final Payload<String, AnnotationController> error = new Payload<>("An Error occurred", AnnotationController.class);
        return error.self(error.methodOn().error()).returnResponseEntity();
    }

    /**
     * localhost:8080/v1/annotate/help
     */
    @GetMapping("/help")
    public HttpEntity<Payload<String, AnnotationController>> help() throws IOException, URISyntaxException
    {
        final Payload<String, AnnotationController> help = new Payload<>("HELP: here are some simple URLS to get started with. " +
                "Build: " + buildInfo.getBuildInfo() + ". Java Memory " + SystemInfo.getJavaMemoryInfo() + ". Booted " + SystemInfo.formatDateTime(BOOT_TIME) + " current time " + SystemInfo.formatDateTime(new Date()), AnnotationController.class);
        help.self(help.methodOn().help());
        help.linkTo(help.methodOn().schema("prefTxSchema"), "Display the 'prefTxSchema' schema");
        help.linkTo(help.methodOn().annotate("chr2:g.47702181C>T", "GRCh37", "prefTxSchema"), "*****    Annotate using the supplied schema name 'prefTxSchema'. *****");
        help.linkTo(help.methodOn().helpAll(), " More detailed help");

        return help.returnResponseEntity();
    }

    /**
     * localhost:8080/v1/annotate/help/all
     */
    @GetMapping("/help/all")
    public HttpEntity<Payload<String, AnnotationController>> helpAll() throws IOException, URISyntaxException
    {
        final String key = "http://reg.genome.network/allele?hgvs=NC_000002.11%3Ag.47702181C%3ET";//TODO How to get this to work with Browser ?
        final String key2 = "http://dev-api-anomaly.ap-southeast-2.elasticbeanstalk.com/api/cache/search/findByKey?key=" + key;
        final String keylocal = "http://localhost:8080/api/cache/search/findByKey?key=" + key;

        final Payload<String, AnnotationController> help = new Payload<>("HELP: here are some simple URLS to get started with. " +
                "Build: " + buildInfo.getBuildInfo() + ". Java Memory " + SystemInfo.getJavaMemoryInfo() + ". Booted " + SystemInfo.formatDateTime(BOOT_TIME) + " current time " + SystemInfo.formatDateTime(new Date()), AnnotationController.class);
        help.self(help.methodOn().helpAll());
        help.linkTo(help.methodOn().gc(), "Collect Garbage");
        help.linkTo(help.methodOn().schemas(), "Display all available schema names");
        help.linkTo(help.methodOn().schema("prefTxSchema"), "Display the 'prefTxSchema' schema");
        help.linkTo(help.methodOn().sources(), "Display available annotation sources");
        help.linkTo(help.methodOn().builds(), "Display supported Genome Builds");
        help.linkTo(help.methodOn().rename37("chr2:g.47702181C>T"), "Rename variant to hg19 RefSeq");
        help.linkTo(help.methodOn().rename38("chr2:g.47702181C>T"), "Rename variant to hg38 RefSeq");
        help.linkTo(help.methodOn().preferredTranscript("LRG_510t1:c.1565+72G>T", "NM_005373.2:c.1565+72G>T", "XM_005270874.1:c.1544+72G>T"), "Return the preferred transcript from a list of");
        help.linkTo(help.methodOn().exceptionTest(true), "Exception TEST!");
        help.linkTo(help.methodOn().annotate("chr2:g.47702181C>T", "GRCh37", "prefTxSchema"), "*****    Annotate using the supplied schema name 'prefTxSchema'. *****");
        help.linkTo(help.methodOn().getVcfs(), "Display all available completed VCF (tags) for retrieval");
        help.linkTo(help.methodOn().getSummary(), "Display summary of all VCF annotations, completed and running.");
        help.linkTo(help.methodOn().getRunningVcf("tag"), "Get running start time");
        help.linkTo(help.methodOn().getVcfResponse(RESULTS_TRACKER.getLast(), ""), "Get last annotated VCF result [" + RESULTS_TRACKER.getLast() + "]");
        help.linkTo(help.methodOn().getVcfResponse2(RESULTS_TRACKER.getLast(), ""), "Get last annotated VCF (HATEOAS) result [" + RESULTS_TRACKER.getLast() + "]");
        help.linkTo(help.methodOn().getVcfResponseAsVcf(RESULTS_TRACKER.getLast(), ""), "Get last annotated VCF result [" + RESULTS_TRACKER.getLast() + "] in VCF Format as a TEXT stream");
        help.linkTo(help.methodOn().getVcfResponseAsVcfInJson(RESULTS_TRACKER.getLast(), ""), "Get last annotated VCF result [" + RESULTS_TRACKER.getLast() + "] in VCF Format");
//        help.linkTo(help.methodOn().derived(RESULTS_TRACKER.getLast(), "derivedTestSchema"),
//                "Get last annotated VCF result [" + RESULTS_TRACKER.getLast() + "] using a different schema to process results");
//        help.linkTo(help.methodOn().derived(RESULTS_TRACKER.getLast(), "", "VepErr:vep.error", "varG:mutalyzer.hgvsG", "consequence:vep.besttx.consequence_terms"),
//                "Get last annotated VCF result [" + RESULTS_TRACKER.getLast() + "] using a specific set of annotations");
//        help.linkTo(help.methodOn().derived(RESULTS_TRACKER.getLast(), "", "VepErr:vep.error", "varG:mutalyzer.hgvsG", "consequence:vep.besttx.consequence_terms"),
//                "Get last annotated VCF result [" + RESULTS_TRACKER.getLast() + "] using a specific set of annotations, without original source results");
//

        help.add(new Link(key, "linkToOriginal"));
        help.add(new Link(keylocal, "linkToCache"));
        return help.returnResponseEntity();
    }


    /**
     * localhost:8080/v1/annotate/sources
     */
    @GetMapping("/sources")
    public HttpEntity<Payload<List<AnnotationSource>, AnnotationController>> sources() throws IOException, URISyntaxException
    {
        //todo BuiltIn only! do not user AnnotationSource.values()
        final Payload<List<AnnotationSource>, AnnotationController> payload = new Payload<>(Arrays.asList(AnnotationSource.values()), AnnotationController.class);
        payload.linkTo(payload.methodOn().helpAll(), "Back to Help");

        return payload.self(payload.methodOn().sources()).returnResponseEntity();
    }

    /**
     * localhost:8080/v1/annotate/builds
     */
    @GetMapping("/builds")
    public HttpEntity<Payload<List<GenomeBuild>, AnnotationController>> builds() throws IOException, URISyntaxException
    {
        final Payload<List<GenomeBuild>, AnnotationController> payload = new Payload<>(Arrays.asList(GenomeBuild.values()), AnnotationController.class);
        payload.linkTo(payload.methodOn().helpAll(), "Back to Help");

        return payload.self(payload.methodOn().builds()).returnResponseEntity();
    }

    String npe;

    /**
     * localhost:8080/v1/annotate/builds
     */
    @GetMapping(path = "/exception/{throwIt}", produces = {MediaType.APPLICATION_JSON_UTF8_VALUE, MediaType.TEXT_PLAIN_VALUE})
    public ResponseEntity<String> exceptionTest(@PathVariable boolean throwIt) throws IOException, URISyntaxException
    {
        if (throwIt)
            npe.toLowerCase();

        return new ResponseEntity<>("Hello World!", HttpStatus.OK);
    }

    /**
     * localhost:8080/v1/gc
     */
    @GetMapping(path = "/gc", produces = {MediaType.TEXT_PLAIN_VALUE})
    public ResponseEntity<String> gc()
    {
        Runtime.getRuntime().gc();
        Runtime.getRuntime().gc();
        return new ResponseEntity<>(SystemInfo.getJavaMemoryInfo(), HttpStatus.OK);
    }

    /**
     * localhost:8080/v1/annotate/schemas
     */
    @GetMapping("/schemas")
    public HttpEntity<Payload<List<String>, AnnotationController>> schemas() throws IOException, URISyntaxException
    {
        final Payload<List<String>, AnnotationController> payload = new Payload<>(derivedSchemaService.getSchemas(), AnnotationController.class);
        payload.linkTo(payload.methodOn().helpAll(), "Back to Help");
        return payload.self(payload.methodOn().schemas()).returnResponseEntity();
    }

    @ExceptionHandler(Exception.class)
    public final ResponseEntity<ErrorDetails> handleAllExceptions(Exception ex, WebRequest request)
    {
        log.warn(request.getDescription(true) + " INTERNAL_SERVER_ERROR", ex);
        return new ResponseEntity<>(new ErrorDetails(ex, request.getDescription(false)), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(FileNotFoundException.class)
    public final ResponseEntity<ErrorDetails> handleUserNotFoundException(FileNotFoundException ex, WebRequest request)
    {
        return new ResponseEntity<>(new ErrorDetails(ex, request.getDescription(true)), HttpStatus.NOT_FOUND);
    }

    /**
     * localhost:8080/v1/annotate/schema/derived
     */
    @GetMapping(path = "/schema/{name}")
    public HttpEntity<String> schema(@PathVariable String name) throws IOException, URISyntaxException
    {
        log.info("schema name={}", name);
        return new ResponseEntity<>(JSON.format(derivedSchemaService.readSchemaByName(name)), HttpStatus.OK);
    }

    //Single HGVS VERSIONS

    /**
     * Request Parameters style.
     * /v1/annotate?variant=chr2:g.47702181C>T&build=GRCh37
     *
     * @param variant HGVS variant
     * @return annotated variant response
     */
    @GetMapping("")
    public HttpEntity<Response> annotate(@RequestParam String variant,
                                         @RequestParam(required = false, defaultValue = "GRCh37") String build,
                                         @RequestParam(required = false, defaultValue = "derived") String schema
    ) throws IOException, URISyntaxException
    {
        final AnnotationSourcesProvider supportedSources = sourceFactories.getSupportedSources(GenomeBuild.safeValueOf(GenomeBuild.NormaliseAlias(build))); //deals with all the argument nuances
        final MapSchema derivedSchema = derivedSchemaService.readSchemaByName(schema);

        final Response response = annotate(variant, derivedSchema, supportedSources);
        response.add(ControllerLinkBuilder.linkTo(ControllerLinkBuilder.methodOn(AnnotationController.class).annotate(variant, build, schema)).withSelfRel());
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * Path Variables style.
     * /v1/annotate/variant/chr2:g.47702181C>T;build=GRCh37schema=pm_derived.
     *
     * @param variant HGVS variant
     * @param build
     * @return annotated variant response
     * @see AnnotationSource
     * @see GenomeBuild
     */
    @GetMapping(path = "/variant/{variant}", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public HttpEntity<Response> annotateVar(@PathVariable String variant,
                                            @MatrixVariable(required = false, defaultValue = "GRCh37") String build,
                                            @MatrixVariable(required = false, defaultValue = "derived") String schema) throws IOException, URISyntaxException
    {
        final AnnotationSourcesProvider supportedSources = sourceFactories.getSupportedSources(GenomeBuild.safeValueOf(GenomeBuild.NormaliseAlias(build))); //deals with all the argument nuances
        final MapSchema derivedSchema = derivedSchemaService.readSchemaByName(schema);

        final Response response = annotate(variant, derivedSchema, supportedSources);
        response.add(ControllerLinkBuilder.linkTo(ControllerLinkBuilder.methodOn(AnnotationController.class).annotateVar(variant, build, schema)).withSelfRel());
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    Response annotate(final String variant, final MapSchema derivedSchema, final AnnotationSourcesProvider sourcesProvider) throws IOException, URISyntaxException
    {
        log.info("variant2 schemaName={}; Arguments provider={} Aliases {}", derivedSchema.getSchemaName(), sourcesProvider.format(), derivedSchema.getAliases());
        final Response responseSingle = new Response(variant, sourcesProvider.getGenomeBuild(), sourcesProvider.getAnnotationSourcesMap(cachingService));
        responseSingle.writeModels(derivedSchema); //for single variant
        return responseSingle;
    }

    //Multi VCF VERSIONS

    /**
     * /v1/annotate/vcf?build=GRCh38&sschema=derived.yaml
     *
     * @param body    VCF Post Data - as JSON ??
     * @param build
     * @return annotated variant response
     * @see AnnotationSource
     * @see GenomeBuild
     */
    @PostMapping(path = "/vcf/test", consumes = MediaType.APPLICATION_JSON_VALUE)
    public HttpEntity<Response> annotateVcf(@RequestBody Map<String, Object> body,
                                            @RequestParam(required = false, defaultValue = "GRCh37") String build,
                                            @RequestParam(required = false, defaultValue = "derived") String schema
                                           ) //throws IOException
    {
        log.info("vcfEntry={}", body);
        log.info("build={}, schemaName={}", build, schema);
        final Response response = new Response("NOT WORKING");
        response.add(ControllerLinkBuilder.linkTo(ControllerLinkBuilder.methodOn(AnnotationController.class).annotateVcf(null, build, schema)).withSelfRel());
        return new ResponseEntity<>(response, HttpStatus.OK);
    }


    /**
     * /v1/annotate/vcf/file?build=GRCh38&schema=derived.yaml&tag=nick
     * <p>
     * Usage:
     * curl  -H "Content-Type:text/plain"
     *
     * @param body    Post Data in VCF format  Content-Type: text/plain
     * @param build   the build type passed to downstream annotating sources
     * @return annotated variant responses
     * @see AnnotationSource
     * @see GenomeBuild
     * @see #getVcfResponse(String, String)
     */
    @PostMapping(path = "/vcf/file", consumes = MediaType.TEXT_PLAIN_VALUE)
    public HttpEntity<Response> annotateVcfFile(@RequestBody String body,
                                                @RequestParam(required = false, defaultValue = "anon") String tag,
                                                @RequestParam(required = false, defaultValue = "true") boolean bg,
                                                @RequestParam(required = false, defaultValue = "GRCh37") String build,
                                                @RequestParam(required = false, defaultValue = "derived") String schema
                                               ) throws IOException, URISyntaxException
    {

        log.info("build={}, schemaName={}, tag={}", build, schema, tag);

        final VcfFile vcfFile = VcfFile.InstanceOf().decode(Stream.of(body.split("[\n\r]+")));
//        final VcfFile vcfFile = VcfFile.InstanceOf().decode(TransformerType.LINE.splitStream(body)); //Invalid ??
        log.info("vcf file = {}", vcfFile.toString());
        if (vcfFile.getVariantsList().isEmpty())
            throw new RuntimeException("Invalid VCF data: " + vcfFile);

        final AnnotationSourcesProvider supportedSources = sourceFactories.getSupportedSources(GenomeBuild.safeValueOf(GenomeBuild.NormaliseAlias(build))); //deals with all the argument nuances
        final MapSchema derivedSchema = derivedSchemaService.readSchemaByName(schema);

        final ResponseHolder responseHolder = prepareAnnotate(vcfFile, derivedSchema, supportedSources);//Split-up - create response setup body(in progress) - ==> then completion.
        responseHolder.setTracker(tag, RESULTS_TRACKER);
        responseHolder.getResponse().add(ControllerLinkBuilder.linkTo(ControllerLinkBuilder.methodOn(AnnotationController.class).annotateVcfFile(null, responseHolder.getTag(), bg, build, schema)).withSelfRel());

        if (bg)
            new Thread(responseHolder).start();
        else
            responseHolder.run(); //sync


        return new ResponseEntity<>(responseHolder.getResponse(), HttpStatus.OK);//means in-progress.
    }
    private ResponseHolder prepareAnnotate(final VcfFile vcfFile, final MapSchema derivedSchema, AnnotationSourcesProvider sourcesProvider) throws IOException, URISyntaxException
    {
        for (AnnotationSourceFactory source_factory : sourcesProvider.getSourceFactories())
            log.debug("source Factory {} supports {} ", source_factory.getClass(), source_factory.getAnnotationSources());
        log.info("VcfFile schemaName={}; Arguments provider={}", derivedSchema.getSchemaName(), sourcesProvider.format());

        return new ResponseHolder(new Response(vcfFile, sourcesProvider.getGenomeBuild(), sourcesProvider.getAnnotationSourcesMap(cachingService)), derivedSchema);
    }

    /**
     * /v1/annotate/vcf/
     *
     * @return a list of completed results.
     */

    @GetMapping("/vcf")
    public HttpEntity<Payload<Set<String>, AnnotationController>> getVcfs() throws IOException, URISyntaxException
    {
        final Payload<Set<String>, AnnotationController> payload = new Payload<>(RESULTS_TRACKER.getCompletedTags(), AnnotationController.class);
        return payload.self(payload.methodOn().getVcfs()).returnResponseEntity();
    }

    /**
     * This is the asynchronous results Polling call.
     * The caller will get a  204 - NO_CONTENT if the results are still being created.
     * <p>
     * /v1/annotate/vcf/{tag};schema=new_schem
     *
     * @param tag call will receive a NOT_FOUND 404 if the tag does note exist.
     * @return the completed annotated results
     * @see HttpStatus#NO_CONTENT
     * @see HttpStatus#NOT_FOUND
     */
    @GetMapping(path = "/vcf/{tag}")
    public ResponseEntity<Response> getVcfResponse(@PathVariable String tag,
                                                   @MatrixVariable(required = false, defaultValue = "") String schema
    ) throws IOException, URISyntaxException
    {
        final ResponseEntity<Response> responseEntity = pollResponse(tag, schema);
        if (responseEntity.getStatusCode() == HttpStatus.OK && responseEntity.getBody() != null)
        {
            responseEntity.getBody().removeLinks();
            responseEntity.getBody().add(ControllerLinkBuilder.linkTo(ControllerLinkBuilder.methodOn(AnnotationController.class).getVcfResponse(tag, schema)).withSelfRel());
        }
        return responseEntity;
    }

    @GetMapping(path = "/vcf2/{tag}")
    public ResponseEntity<Payload<Response, AnnotationController>> getVcfResponse2(@PathVariable String tag,
                                                                                   @MatrixVariable(required = false, defaultValue = "") String schema
    ) throws IOException, URISyntaxException
    {
        final ResponseEntity<Response> responseEntity = pollResponse(tag, schema);
        final Payload<Response, AnnotationController> payload = new Payload<>(responseEntity.getBody(), AnnotationController.class);
        return payload.self(payload.methodOn().getVcfResponse2(tag, schema)).returnResponseEntity(responseEntity.getStatusCode());
    }

    /**
     * Returns the annotated and encoded VCF as a text stream.
     * The caller has no visibility of exceptions - as a "Internal Server Error - 500" is returned.
     * Try the Json version to see what is going wrong and possibly a stack trace.
     *
     * @param tag call will receive a NOT_FOUND 404 if the tag does note exist.
     * @return the completed results
     * @see HttpStatus#NO_CONTENT
     * @see HttpStatus#NOT_FOUND
     * @see HttpStatus#INTERNAL_SERVER_ERROR
     * @see #getVcfResponseAsVcfInJson
     */
    @GetMapping(path = "/vcf/{tag}/vcf", produces = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity<String> getVcfResponseAsVcf(@PathVariable String tag,
                                                      @MatrixVariable(required = false, defaultValue = "") String schema
    ) throws IOException, URISyntaxException
    {
        final ResponseEntity<Response> responseEntity = pollResponse(tag, schema);
        if (responseEntity.getStatusCode() == HttpStatus.OK && responseEntity.getBody() != null)
            return new ResponseEntity<>(responseEntity.getBody().toEncodedVCF(), HttpStatus.OK);
        return new ResponseEntity<>(responseEntity.getStatusCode());
    }

    /**
     * Returns the annotated and encoded VCF inside a JSON payload.
     * This allows for visibility of exceptions.
     *
     * @param tag call will receive a NOT_FOUND 404 if the tag does note exist.
     * @return the completed results
     * @see HttpStatus#NO_CONTENT
     * @see HttpStatus#NOT_FOUND
     * @see #pollResponse(String, String)
     */
    @GetMapping(path = "/vcf/{tag}/vcf/json")
    public ResponseEntity<Payload<String, AnnotationController>> getVcfResponseAsVcfInJson(@PathVariable String tag,
                                                                                           @MatrixVariable(required = false, defaultValue = "") String schema
    ) throws IOException, URISyntaxException
    {
        final ResponseEntity<Response> responseEntity = pollResponse(tag, schema);
        final Response response = responseEntity.getBody();
        if (responseEntity.getStatusCode() == HttpStatus.OK && response != null)
        {
            final Payload<String, AnnotationController> payload = new Payload<>(response.toEncodedVCF(), AnnotationController.class);
            return payload.self(payload.methodOn().getVcfResponseAsVcfInJson(tag, schema)).returnResponseEntity();
        }
        return new ResponseEntity<>(responseEntity.getStatusCode());
    }

    /**
     * This is the common asynchronous results Polling code.
     * The caller will get a  204 - NO_CONTENT if the results are still being created.
     * <p>
     * /v1/annotate/vcf/{tag};schema=new_schem
     *
     * @param tag call will receive a NOT_FOUND 404 if the tag does note exist.
     * @return the completed results
     * @see HttpStatus#NO_CONTENT
     * @see HttpStatus#NOT_FOUND
     */
    private ResponseEntity<Response> pollResponse(String tag, String schema) throws IOException, URISyntaxException
    {
        try
        {
            final ResponseHolder holder = RESULTS_TRACKER.getCompleted(tag); //Throws FNF
            if (holder == null)
            {
                log.info("User requested tag not found {}", tag);//user input error
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
            final Response response = holder.getResponse();

            if (!schema.isEmpty())
                response.writeModels(derivedSchemaService.readSchemaByName(schema));//TODO polling call can overwrite !! FIX?
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (FileNotFoundException e)
        {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);//Busy
        }
    }

    private Response _derivedOrthogonal(Response response, String schema, String... model) throws IOException, URISyntaxException
    {
        final MapSchema schemaByName;

        if (!schema.isEmpty())
        {
            log.info("Writing new domain model by name= {}", schema);
            schemaByName = derivedSchemaService.readSchemaByName(schema);
        } else if (model != null && model.length > 0)
        {
            log.info("Writing new domain model by attributes {}", Arrays.asList(model));
            schemaByName = new DerivedSchema();
            for (String s : model)
            {
                final Strings.Tuple<String> stringTuple = Strings.StringTuple.parseStrings(s, ':');
                schemaByName.getSchema().put(stringTuple.getOne(), stringTuple.getTwo());
            }
        } else
            return null;

        final Map<String, List<String>> orthogonalMap = schemaByName.createOrthogonalMap();

        response.resultStream().forEach(result -> result.getModel(schemaByName).addToOrthogonalMap(orthogonalMap));
        final Response newResponse = new Response(response);
        newResponse.setOrthogonalMap(orthogonalMap);

        return newResponse;
    }

    /**
     * Request Parameters style.
     * /v1/annotate/derived?tag=mini_tumor.vcf&schema=mySchema&model=VepErr:vep.error,varG:mutalyzer.hgvsG
     *
     * @param tag
     * @return annotated variant response
     */
    @GetMapping("/derived")
    public HttpEntity<Response> derived(@RequestParam String tag,
                                        @RequestParam(required = false, defaultValue = "") String schema,
                                        @RequestParam(required = false, defaultValue = "") String... model
    ) throws IOException, URISyntaxException
    {
        final ResponseEntity<Response> responseEntity = getVcfResponse(tag, ""); //poll
        if (responseEntity.getStatusCode() != HttpStatus.OK)
            return responseEntity;

        final Response response = _derivedOrthogonal(responseEntity.getBody(), schema, model);
        response.removeLinks();
        response.add(ControllerLinkBuilder.linkTo(ControllerLinkBuilder.methodOn(AnnotationController.class).derived(tag, schema, model)).withSelfRel());
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * /v1/annotate/vcf/running/{tag}
     *
     * @param tag
     * @return
     */
    @GetMapping(path = "/vcf/running/{tag}")
    public HttpEntity<Payload<Response, AnnotationController>> getRunningVcf(@PathVariable String tag)
    {
        final ResponseHolder running = RESULTS_TRACKER.getRunning(tag);
        if (running == null)
        {
            log.info("User requested running tag not found {}", tag);
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        final Payload<Response, AnnotationController> payload = new Payload<>(running.getResponse(), AnnotationController.class);
        return payload.self(payload.methodOn().getRunningVcf(tag)).returnResponseEntity();
    }

    /**
     * /v1/annotate/vcf/results
     *
     * @return
     */
    @GetMapping(path = "/vcf/summary")
    public HttpEntity<Payload<ResultsTracker, AnnotationController>> getSummary() throws
            IOException, URISyntaxException
    {
        final Payload<ResultsTracker, AnnotationController> payload = new Payload<>(RESULTS_TRACKER, AnnotationController.class);

        for (String tag : RESULTS_TRACKER.getCompletedTags())
            payload.linkTo(payload.methodOn().getVcfResponse(tag, ""), "Show Details for this completed vcf tag");

        for (String tag : RESULTS_TRACKER.getRunningTags())
            payload.linkTo(payload.methodOn().getRunningVcf(tag), "Show RUNNING vcf details");


        return payload.self(payload.methodOn().getSummary()).returnResponseEntity(); //running ? or OK
    }

    //GENERAL SERVICES

    /**
     * localhost:8080/v1/annotate/refseq/hg37/chr2:g.47702181C>T
     */
    @GetMapping("/refseq/hg19/{variant}")
    public HttpEntity<Map<String, String>> rename37(@PathVariable String variant) throws IOException, URISyntaxException
    {
        return new ResponseEntity<>(Collections.singletonMap("refseq", GenomeBuild.GRCh37.renameToRefSeq(variant)), HttpStatus.OK);

//        final Payload<String, AnnotationController> payload = new Payload<>(GenomeBuild.GRCh37.renameVariant(variant), AnnotationController.class);
//        payload.linkTo(payload.methodOn().help(), "Back to Help");
//
//        return payload.self(payload.methodOn().rename37(variant)).returnResponseEntity();
    }


    /**
     * localhost:8080/v1/annotate/refseq/hg38/chr2:g.47702181C>T
     */
    @GetMapping("/refseq/hg38/{variant}")
    public HttpEntity<Map<String, String>> rename38(@PathVariable String variant) throws IOException, URISyntaxException
    {
        return new ResponseEntity<>(Collections.singletonMap("refseq", GenomeBuild.GRCh38.renameToRefSeq(variant)), HttpStatus.OK);

//        final Payload<String, AnnotationController> payload = new Payload<>(GenomeBuild.GRCh38.renameVariant(variant), AnnotationController.class);
//        payload.linkTo(payload.methodOn().help(), "Back to Help");
//
//        return payload.self(payload.methodOn().rename38(variant)).returnResponseEntity();
    }


    /**
     * Request Parameters style.
     * /v1/annotate/preferredTranscript?transcripts=?transcripts=LRG_510t1:c.1565+72G>T, NM_005373.2:c.1565+72G>T, XM_005270874.1:c.1544+72G>T
     * /v1/annotate/preferredTranscript?transcripts=?transcripts=LRG_510t1, NM_005373.2, XM_005270874.1
     *
     * @param transcripts
     * @return preferred transcript
     */
    @GetMapping(path = "/preferredTranscript", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public HttpEntity<Map<String, Object>> preferredTranscript(@RequestParam(required = false, defaultValue = "") String... transcripts) throws IOException, URISyntaxException
    {
        final Collection<String> transcriptsInput = Arrays.asList(transcripts);

        log.info("preferredTranscript({})", transcriptsInput);
        transcriptsService.loadResources();
        final Map<String, Object> preferred = new LinkedHashMap<>();
        final String prefTx = transcriptsService.selectBestTranscript(PreferredTranscriptService.asList(transcriptsInput));
        preferred.put("transcripts", transcriptsInput);
        preferred.put("preferred", prefTx);
        preferred.put("lrg",  prefTx == null ? null : transcriptsService.selectLrg(prefTx));
        return new ResponseEntity<>(preferred, HttpStatus.OK);
    }

}
