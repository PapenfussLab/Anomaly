package org.petermac.nic.api;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.petermac.nic.dataminer.domain.pathos.vcf.core.GenomeAssembly;
import org.petermac.nic.dataminer.domain.pathos.vcf.core.GenomeBuild;
import org.petermac.nic.dataminer.domain.pathos.vcf.core.MapSchema;
import org.petermac.nic.dataminer.domain.pathos.vcf.core.VariantCall;
import org.petermac.nic.dataminer.domain.pathos.vcf.legacy.Variant;
import org.petermac.nic.dataminer.domain.pathos.vcf.sources.AnnotationSource;
import org.petermac.nic.dataminer.domain.pathos.vcf.sources.AnnotationURLProvider;
import org.petermac.nic.dataminer.domain.pathos.vcf.transcode.*;
import org.petermac.nic.dataminer.parse.JSON;
import org.petermac.nic.dataminer.parse.JSONObject;
import org.petermac.nic.dataminer.proj.Timeable;
import org.petermac.nic.dataminer.transcode.Strings;
import org.petermac.nic.dataminer.transcode.TransformerType;
import org.slf4j.Logger;
import org.springframework.data.annotation.Transient;
import org.springframework.hateoas.ResourceSupport;

import java.util.*;
import java.util.function.BinaryOperator;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;


/**
 * Created by   Nic         on 30/05/2018.
 * Ken Doig    on 26/10/2018   Added VICC    datasource
 * Ken Doig    on 05/11/2018   Added ClinGen Allele Registry datasource
 * Ken Doig    on 18/11/2018   Renamed ClinGen datasource ro ClinGenAR
 * Ken Doig    on 18/11/2018   Added ClinGen Evidence Registry datasource
 */
public class Response extends ResourceSupport
{
    private static final Logger log = org.slf4j.LoggerFactory.getLogger(Response.class);

    private final Collection<AnnotationURLProvider> annotationSources; //Literal Vep38 !
    private final GenomeBuild build;
    private final GenomeAssembly assembly;
    private final List<String> variants;
    private VcfFile vcfFile; //optional source.[VCF Input]//original VCF needed for optional return type of VCF(not JSON)
    private String executionException; //no valid results.
    private List<Result> results = Collections.emptyList();
    private List<String> logs = new ArrayList<>();
    private Timeable time;
    private Map<String, List<String>> orthogonalMap;


    private Response(GenomeBuild build, Collection<AnnotationURLProvider> annotationSources, Collection<String> variants)
    {
        this.build = build;
        this.assembly = build.getAssembly();
        this.annotationSources = annotationSources;
        this.vcfFile = VcfFile.InstanceOf(); //null
        this.variants = new ArrayList<>(variants);
        Collections.sort(this.variants);
        if (annotationSources.isEmpty())
            throw new IllegalArgumentException("NO Annotation Sources Provided!");
        log.info("build={}, assembly={}, sources={}", build, assembly.getCommonName(), this.annotationSources);
    }


    @JsonCreator
    public Response(@JsonProperty("variant") String variant)
    {
        this(variant, GenomeBuild.GRCh37, Collections.emptyMap());
    }


    public Response(VcfFile vcfFile, GenomeBuild build, final Map<String, AnnotationURLProvider> providerMap)
    {
        this(build, providerMap.values(), vcfFile.getVariantsList().stream().map(Variant::Create).map(VariantCall::getHgvsGenomicReference).collect(Collectors.toSet()));
        this.vcfFile = vcfFile;

        final List<VcfVar> variantsList = vcfFile.getVariantsList();
        results = new ArrayList<>(variantsList.size());//todo immutable
        variantsList.forEach(vcfVar -> results.add(new Result(build, vcfVar, vcfFile.getHeaders(), providerMap)));//from VCF
        Collections.sort(results); //align with sorted variants list.
    }

    public Response(String variant, GenomeBuild build, final Map<String, AnnotationURLProvider> providerMap)
    {
        this(build, providerMap.values(), Collections.singleton(variant));
        if (providerMap.isEmpty())
            log.warn("No sources! providerMap is empty. Possibly no support for Genome Build {} ?", getBuild());
        final Result result = new Result(build, variant, providerMap);
        results = Collections.singletonList(result); //single Variant
    }

    /**
     * Copy Ctor for invariants
     *
     * @param response
     */
    public Response(Response response)
    {
        this.build = response.build;
        this.assembly = response.assembly;
        this.annotationSources = response.annotationSources;
//        this.returnedSources = response.returnedSources;
        this.variants = response.variants;
    }

    public void writeModels(MapSchema derivedSchema)
    {
        results.forEach(result -> result.writeModel(derivedSchema));

        vcfFile.setAppendedMeta(derivedSchema.getSchema().entrySet().stream().map(entry ->
                VcfMeta.createINFO(entry.getKey(), "mapped to " + entry.getValue())
        ).collect(Collectors.toList()));

        //Add Global Headers to describe the derived schema used.
        log.info("writeModels  -> INFO vcfModelInfoMeta = {}", vcfFile.getAppendedMeta());
        clearReturnResults();
    }

    // Put results the top see we see the summaries in tail.
//    public List<Result> getResults()
//    {
//        return results;
//    }


    public Collection<String> getSources()
    {
        return annotationSources.stream().map(AnnotationURLProvider::getName).collect(Collectors.toSet());
    }

    public Collection<String> getVariants()
    {
        return variants;
    }

    public int getOk()
    {
        return getOkResults().size();
    }

    public int getFailed()
    {
        return getFailedResults().size();
    }

//    private Map<String, Result> _okMap;

    public synchronized Map<String, Result> getOkResults()
    {

//        if (_okMap == null)
        Map<String, Result> _okMap = new TreeMap<>();
        results.forEach(result -> result.addOKMap(_okMap));
        return _okMap;
    }


//    private Map<String, Result> _failMap = null;

    public synchronized Map<String, Result> getFailedResults()
    {
//        if (_failMap == null)
        Map<String, Result> _failMap = new TreeMap<>();
        results.forEach(result -> result.addFailedMap(_failMap));
        return _failMap;
    }

    public List<String> getAnnotatorLog()
    {
        return logs;
    }

    /**
     * @return actual sources, Not Common sources
     */
    Collection<AnnotationURLProvider> getLiteralAnnotationSources()
    {
        return annotationSources; //Vep38, Mutalyzer
    }


    public GenomeBuild getBuild()
    {
        return build;
    }

    public GenomeAssembly getAssembly()
    {
        return assembly;
    }

    public int getNumberOfResults()
    {
        return results.size();
    }

    /**
     * @param index
     * @return
     * @deprecated
     */
    Result resultAt(int index)
    {
        return results.get(index);
    }

    Stream<Result> resultStream()
    {
        return results.stream();
    }

    public long getNumberOfResultErrors()
    {
        return results.stream().map(result -> result.errors.values().stream().filter(summary -> !summary.exceptionTypes.isEmpty())).count();
    }

    public String getExecutionException()
    {
        return executionException;
    }

    public synchronized void setExecutionException(Exception e)
    {
        this.executionException = e.toString();
        for (Result result : results)
            result.setException(AnnotationSource.ALL.getAnnotationURLProvider(), e);
        getFailedResults();
    }

    public Map<String, Long> getTotalExceptions()
    {
        final Map<String, Long> totals = new HashMap<>();
        for (AnnotationURLProvider annotationSource : annotationSources)
            totals.put(annotationSource.getCommonSource().getName(), getTotalExceptions(annotationSource));
        return totals;
    }

    private long getTotalExceptions(AnnotationURLProvider a)
    {
        return results.stream().filter(result -> result.errors.containsKey(a.getCommonSource().getName())).count();
    }

    public Set<String> getExceptionTypes()
    {
        Set<String> set = new TreeSet<>();
        results.stream().map(result -> result.errors.values()).forEach(v -> v.stream().map(Summary::exceptions).forEach(exceptions -> exceptions.stream().map(e -> e.getClass().getSimpleName()).forEach(set::add)));
        return set;
    }

    /**
     * @return
     * @deprecated PRESERVE old functionality . use getVcfMetaNew instead - todo kdd
     */
    public List<VcfMeta> getVcfMeta()
    {
        return vcfFile.getHeaders();
    }

    public List<JSON.JSONMap> _getVcfMeta()
    {
        return vcfFile.getHeaders().stream().map(VcfMeta::getJSONMap).collect(Collectors.toList());
    }

    public Map<String, List<JSON.JSONMap>> getVcfMetaNew()
    {
//        final Map<String, List<JSON.JSONMap>> collect = vcfFile.getHeaders().stream().collect(Collectors.toMap(k -> {
        return vcfFile.getHeaders().stream().collect(Collectors.toMap(
                k -> k.getMetaType().name(),
                v -> new ArrayList<>(Collections.singletonList(v.getJSONMap())),
                (BinaryOperator<List<JSON.JSONMap>>) (o, o2) -> {
                    o.addAll(o2); //accumulator ??
                    return o;
                }, LinkedHashMap::new)); //preserve the VCF order
    }

    /**
     * @return the derived annotated schema in the form of a VCF.
     */
    public VcfFile toVCF()
    {
//        for (VcfVar vcfVar : vcfFile.getVariantsList())
//        {
//            vcfVar.getInfo().add(new StringPairCSV("Anomaly", "v1")); //per variant info ?
//        }

        return vcfFile;
    }


    public String toEncodedVCF()
    {
        return VcfFiles.DELIMITER.join(vcfFile.encodeAppended()); //special purpose (hack) encoder that allows appended META
    }

    /**
     * @param result
     * @param annotationSource AnnotationURLProvider
     * @param error
     * @deprecated
     */
    public void setResultError(final Result result, AnnotationURLProvider annotationSource, String error)
    {
        result.getSummary(annotationSource).exceptions.add(error);
    }

    public void clearReturnResults()
    {
        for (Result result : results)
            result.sourceResults.keySet().stream().filter(Predicate.isEqual(AnnotationSource.Anomaly.getName()).negate()).forEach(key -> {
                result.sourceResults.put(key, result.sourceResults.get(key) instanceof Exception ? result.sourceResults.get(key).toString() : "removed");
            });
    }

    void setTime(Timeable time)
    {
        this.time = time;
    }

    public String getElapsedTime()
    {
        return time == null ? "not started" : time.getTime();
    }

    public String getStartTime()
    {
        return time == null || time.getStart() < 1 ? "never" : new Date(time.getStart()).toString();
    }

    public void setOrthogonalMap(Map<String, List<String>> orthogonalMap)
    {
        this.orthogonalMap = orthogonalMap;
    }

    public Map<String, List<String>> getOrthogonalMap()
    {
        return orthogonalMap;
    }

    public static class Summary
    {

        private Set<Exception> exceptionTypes = new HashSet<>();//private coz Cannot serialize some exceptions - JSON errors
        private Set<String> exceptions = new TreeSet<>();

        static Set<Exception> exceptions(Summary summary)
        {
            return summary.exceptionTypes;
        }

        public Set<String> getExceptionTypes()
        {
            return exceptionTypes.stream().map(e -> e.getClass().getSimpleName()).collect(Collectors.toSet());
        }

        public Set<String> getExceptions()
        {
            return exceptions;
        }

        public void addException(Exception e)
        {
            if (e != null)
            {
                exceptions.add(e.toString());
                exceptionTypes.add(e);
            }
        }
    }

    /**
     * Information derived specifically from a VCF "File" input.
     */
    static class VariantCallFormat
    {
        public final VariantCall variantCall;
        private final String hgvsGenomicReference;
        public final Collection<Map<String, String>> CSQInfo; //optional source.[VCF Input]
        public final Map<String, String> info;

        public VariantCallFormat(String hgvsGenomicReference)
        {
            Objects.requireNonNull(hgvsGenomicReference, "hgvsGenomicReference cannot be null");
            this.hgvsGenomicReference = hgvsGenomicReference;
            variantCall = VariantCall.NULL;
            CSQInfo = Collections.emptyList();
            info = Collections.emptyMap();

        }

        public VariantCallFormat(VariantCall variantCall, List<VcfMeta> vcfMetaList)
        {
            Objects.requireNonNull(variantCall, "VariantCall cannot be null");
            this.variantCall = variantCall;
            this.hgvsGenomicReference = variantCall.getHgvsGenomicReference();
            Objects.requireNonNull(hgvsGenomicReference, "hgvsGenomicReference cannot be null");

            //Interpreted VCF values
            CSQInfo = variantCall.getInfoMap(vcfMetaList, INFO.CSQ); //With META interpretation
            //List -> CSV
            info = variantCall.getInfo().entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue() == null ? "" : TransformerType.CSV.join(e.getValue().stream())));
        }

        private String getHgvsGenomicReference()
        {
            return hgvsGenomicReference;
        }
    }

    /**
     * Anomaly as a data source.
     */
    static class Anomaly implements JSONObject
    {
        //Typically next 4 are used in URL building for Anno Sources.
        public final String variant;
        /**
         * @deprecated use refSeq
         */
        public String hgvsGVariant; //same as variant, but not in Chrom format. only applicable to the Genomic NC_ accessions.
        public String refSeq; //hgvsGVariant replacement
        public GenomeBuild build;
        public GenomeAssembly assembly;
        // Optional (from VCF only)
        public VariantCallFormat vcf; //optional source.[VCF Input]
        public VcfVar vcfVar = null; //optional source.[VCF Input]

        Anomaly(String variant)
        {
            this.variant = variant;
        }

        @Override
        public String toString()
        {
            return "variant=" + variant + " build=" + build + " / " + assembly.getCommonName() + " refSeq=" + refSeq;
        }


        void addAppendedInfo(String s)
        {
            if (vcfVar != null)
            {
                if (vcfVar.getAppendedInfo().isEmpty())
                    vcfVar.setAppendedInfo(new ArrayList<>());
                vcfVar.getAppendedInfo().add(s);
            }
        }
    }

    /**
     * The public attributes here are serialised as a JSON HTTP response.
     */
    public static class Result implements Comparable<Result>, JSONObject
    {
        public final String variant;
        //        public Map<String, String> errors = new HashMap<>();
        public final Map<String, Summary> errors = new LinkedHashMap<>();
        //        public MutResponse mutalyzer; //special

        // Domain Model is only exposed to this.
        public final Map<String, Object> sourceResults; //keyed by Anno source common name.
        //        "http://dev-api-anomaly.ap-southeast-2.elasticbeanstalk.com/api/cache/5ca37b73a36cdf4547a1fb9b"
        public final Map<String, String> cachedResults; //keyed by Anno source common name.

        /**
         * The derived schema herein is the crucial piece of semantic data.
         */
        public DomainModel domainModel; //contains the derived schema data - Clients use this!

        @Transient
        private final Anomaly anomaly; //Built-In data source
        @Transient
        private DynamicDomainModel.SourceInjection dynamicSourceResults;
        @Transient
        private final Map<String, AnnotationURLProvider> providerMap;

        /**
         * Ctor
         *
         * @param build
         * @param variantCallFormat
         * @param providerMap
         */
        private Result(GenomeBuild build, VariantCallFormat variantCallFormat, Map<String, AnnotationURLProvider> providerMap)
        {
            variant = variantCallFormat.getHgvsGenomicReference();
            this.providerMap = providerMap;
            sourceResults = new LinkedHashMap<>();
            cachedResults = new LinkedHashMap<>();
            domainModel = new DomainModel();  //NO schema !!

            dynamicSourceResults = new DynamicDomainModel.SourceInjection()
            {
                @Override
                public void inject(String src, Object value)
                {
                    log.debug("adding source result for {}", src);
                    sourceResults.put(src, value);
                    final AnnotationURLProvider annotationURLProvider = providerMap.get(src);
                    if (annotationURLProvider != null && annotationURLProvider.isCaching())
                        cachedResults.put(annotationURLProvider.getCommonSource().getName(), annotationURLProvider.getCachedId());
                }

                @Override
                public boolean hasSrc(String src)
                {
                    return sourceResults.containsKey(src);
                }

                @Override
                public String formatAsJson()
                {
                    return JSON.format(sourceResults);// !! Thus schemas reference via Source.a.b (and not sourceResults.Source.a.b)
                }
            };

            //Built-In data source
            anomaly = new Anomaly(variant);

            anomaly.build = build;
            anomaly.assembly = build.getAssembly();
            anomaly.vcf = variantCallFormat;//.variantCall == null ? new VariantCallFormat() : variantCallFormat;

            anomaly.hgvsGVariant = build.renameToRefSeq(variant);//NC_
            anomaly.refSeq = build.renameToRefSeq(variant); //Must be proper!
            Objects.requireNonNull(anomaly.refSeq, "hgvs reference sequence is invalid: " + variant);
            dynamicSourceResults.inject(AnnotationSource.Anomaly.getName(), anomaly);
        }

        /**
         * Explicit variant (no VCF) Ctor
         *
         * @param build
         * @param variant
         */
        public Result(GenomeBuild build, String variant, Map<String, AnnotationURLProvider> providerMap)
        {
            this(build, new VariantCallFormat(variant), providerMap);//Explicit variant only
        }

        /**
         * variant via VCF Ctor
         *
         * @param build
         * @param vcfVar
         * @param vcfMetaList
         */
        public Result(GenomeBuild build, VcfVar vcfVar, List<VcfMeta> vcfMetaList, Map<String, AnnotationURLProvider> providerMap)
        {
            this(build, new VariantCallFormat(Variant.Create(vcfVar), vcfMetaList), providerMap);//VCF variant
            anomaly.vcfVar = vcfVar;
        }

        public void addOKMap(Map<String, Result> map)
        {
            if (errors.isEmpty())
                map.put(variant, this);
        }

        public void addFailedMap(Map<String, Result> map)
        {
            if (!errors.isEmpty())
                map.put(variant, this);
        }

        public void setResultError(AnnotationURLProvider annotationSource, String error)
        {
            getSummary(annotationSource).exceptions.add(error);
        }

        private synchronized Summary getSummary(AnnotationURLProvider annotationSource)
        {
            final String commonName = annotationSource.getCommonSource().getName();
            if (!errors.containsKey(commonName))
                errors.put(commonName, new Summary());
            return errors.get(commonName);
        }

        /**
         * NEW schema !!
         *
         * @param schema NEW schema !!
         * @return
         */
        public void writeModel(MapSchema schema)
        {
            domainModel = getModel(schema);
        }

        public DomainModel getModel(MapSchema schema)
        {
            final DynamicDomainModel domainModel = new DynamicDomainModel(schema, providerMap);
            domainModel.writeModel(variant, dynamicSourceResults, (s, o) -> anomaly.addAppendedInfo(s + "=" + o));
            return domainModel;
        }

//        /**
//         * @param urlProvider
//         * @param vepFiltered
//         * @return
//         */
//        public Result createSourceResult(RestTemplate restTemplate, AnnotationURLProvider urlProvider, VepFiltered vepFiltered)
//        {
//            return createSourceResult(restTemplate, urlProvider, vepFiltered, null);
//        }

//        /**
//         * @param urlProvider
//         * @param vepFiltered
//         * @param cachingService
//         * @return
//         */
//        public Result createSourceResult(RestTemplate restTemplate, final AnnotationURLProvider urlProvider, VepFiltered vepFiltered, CachingService<String, Object, String, Date> cachingService)
//        {
//            final AnnotationURLProvider _urlProvider = cachingService != null && !urlProvider.isCaching() ? new AnnotationCacheWrapper(urlProvider, cachingService) : urlProvider;
//            return _createSourceResult(restTemplate, _urlProvider, vepFiltered);
//        }

//        /**
//         * @param urlProvider
//         * @param vepFiltered
//         * @return
//         */
//        private Result _createSourceResult(RestTemplate restTemplate, AnnotationURLProvider urlProvider, VepFiltered vepFiltered)
//        {
//            final JSONObject jsonInputData = anomaly; //domainModel.getSourceData(); //Url extract data from this.
//            final String commonSourceName = urlProvider.getCommonSource().getName();
//            final Object sourceResult = urlProvider.createSourceResult(restTemplate, anomaly, s -> s, e -> setException(urlProvider, e));//>>> Do the WORK !!
//            if (urlProvider.isCaching())
//                cachedResults.put(commonSourceName, urlProvider.getCachedId());
//
//            if (vepFiltered != null && commonSourceName.equals(AnnotationSource.Vep.getName())) // todo make Vep provide this behaviour
//                sourceResults.put(commonSourceName, vepFiltered.getFiltered(JSONObject.wrap(sourceResult).write(VepResponse.class)));
//            else
//                sourceResults.put(commonSourceName, sourceResult);
//             Why don't we pass around the JsonParser to stop continually parsing the data ?
//            NOTE!! adding results to sourceResults effectively adds new data to the "jsonInputData" for subsequent calls - hence we have to reparse the entire "jsonInputData" each time.
//            return this;
//        }
//
//        /**
//         * @param mutalyzer
//         * @return
//         * @deprecated use createSourceResult
//         */
//        public Result setMutalyzer(MutalyzerResponse mutalyzer)
//        {
//            final AnnotationURLProvider mutalyzerProvider = AnnotationSource.Mutalyzer.getAnnotationURLProvider();
//            sourceResults.put(mutalyzerProvider.getCommonSource().getName(), mutalyzer); //Deprecated
//            if (mutalyzer == null)
//                setException(mutalyzerProvider, new Exception("No Mutalyzer results"));
//            else
//                EqualityException.assertEquals("Mutalyzer mismatch on variant", variant, mutalyzer.variant);
//            return this;
//        }

//        public void setVcf(VcfVar vcfVar, List<VcfMeta> vcfMetaList)
//        {
//            this.vcfVar = vcfVar;
//
//            final VariantCall variantCall = Variant.Create(vcfVar);
//            this.vcf = new VariantCallFormat(variantCall, vcfMetaList);
//            variant = variantCall.getHgvsGenomicReference();  //From VCF
//            refSeq = build.renameToRefSeq(variant);
//            hgvsGVariant = build.renameToRefSeq(variant);
//        }
//

        /**
         * @param source
         * @param e
         */
        public void setException(AnnotationURLProvider source, Exception e)
        {
            getSummary(source).addException(e);
        }


        @Override
        public int compareTo(Result r)
        {
            return Strings.compareTo(variant, r.variant);
        }
    }
}
