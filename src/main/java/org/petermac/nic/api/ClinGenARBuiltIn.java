package org.petermac.nic.api;

import org.petermac.nic.dao.ClinGenARResponse;
import org.petermac.nic.dao.MutalyzerResponse;
import org.petermac.nic.dataminer.domain.pathos.vcf.core.GenomeBuild;
import org.petermac.nic.dataminer.domain.pathos.vcf.services.CachingService;
import org.petermac.nic.dataminer.domain.pathos.vcf.sources.AnnotationCacheWrapper;
import org.petermac.nic.dataminer.domain.pathos.vcf.sources.AnnotationSource;
import org.petermac.nic.dataminer.parse.JSONObject;
import org.petermac.nic.dataminer.parse.JsonParser;
import org.petermac.nic.dataminer.transcode.Strings;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Stream;

/**
 * Purpose: To replace Mutalyzer with ClinGenAR.
 * Created by Nic on 14/03/2019.
 * @deprecated
 */
@Component
public class ClinGenARBuiltIn implements BuiltIn
{
    private Logger log = org.slf4j.LoggerFactory.getLogger(getClass());

    @Autowired
    CachingService<String, Object, String, Date> cachingService;

    @Override
    public MutalyzerResponse call(String hgvs, GenomeBuild genomeBuild, List<String> log) //throws Exception
    {
        return toMut(hgvs, genomeBuild);
    }

    @Override
    public Stream<MutalyzerResponse> call(Collection<String> variants, GenomeBuild genomeBuild, List<String> log)
    {

        return variants.stream().map(hgvs -> call(hgvs, genomeBuild, null));
    }


    public JSONObject getClinGenResponse(String variant, final GenomeBuild genomeBuild)// throws Exception
    {
//        final String renamedHgvs = "NC_000001.10:g.43815102G>T";
        final String renamedHgvs = genomeBuild.renameToRefSeq(variant);
        log.info("{} --> {}", variant, renamedHgvs);


        log.info("ClinGen variant {}", renamedHgvs);
        return JSONObject.wrap(new AnnotationCacheWrapper(AnnotationSource.ClinGenAR.getAnnotationURLProvider(), cachingService).
                createSourceResult(null, new JSONObject.PrimitiveWrapper(Collections.singletonMap("hgvsGVariant", renamedHgvs)), s -> s, e -> {
                }));
    }

    public ClinGenARResponse writeClinGenARResponse(JSONObject clinGenResponse)
    {
        return clinGenResponse.write(ClinGenARResponse.class);
    }

    public MutalyzerResponse toMut(String variant, final GenomeBuild genomeBuild) //throws Exception
    {
        final MutalyzerResponse mutResponse = new MutalyzerResponse();

        mutResponse.refSeq = genomeBuild.renameToRefSeq(variant);
        final JSONObject clinGenResponse = getClinGenResponse(variant, genomeBuild);

        final JsonParser jsonParser = clinGenResponse.getJsonParser();


        mutResponse.variant = variant;
        mutResponse.error = Strings.collectFirst(jsonParser.getStream("error"));
        mutResponse.hgvsC = mutResponse.refSeq;
        mutResponse.hgvsG = variant;

        mutResponse.gene = Strings.getFirst(jsonParser.getList("transcriptAlleles[*].geneSymbol"));
        mutResponse.transcripts = new TreeSet<>(jsonParser.getList("transcriptAlleles[*].hgvs[*]"));

        return mutResponse;
    }
}
