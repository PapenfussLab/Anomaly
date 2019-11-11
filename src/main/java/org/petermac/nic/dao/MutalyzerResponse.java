package org.petermac.nic.dao;

import org.petermac.nic.api.RestResponse;
import org.petermac.nic.dataminer.parse.JSONObject;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by Nick Kravchenko on 25/06/2018.
 *
 * @deprecated move to pure ClinGen
 */
public class MutalyzerResponse implements RestResponse, JSONObject
{
    public Collection<String> transcripts;
    public String variant;
    public String assembly;
    public String refSeq;
    public String error;


    public String hgvsC; //Result.refseq
    public String hgvsG;
    public String hgvsP; //ClinGenAR.transcriptAlleles[0].proteinEffect.hgvs
    public String bestTranscript;
    public String filterTs;
    public String gene;//ClinGenAR.transcriptAlleles[*].geneSymbol.set()
    public String lrg;////ClinGenAR.transcriptAlleles[0].hgvs[1]
    public Named named = new Named();

    public class Named
    {
        public String proteinRef;
        public String restriction_sites_created;
        public String restriction_sites_deleted;
        public String affectedTranscripts;
        public String affectedProteins;
    }

    public MutalyzerResponse()
    {
        transcripts = Collections.emptyList();
    }

    public String getVariant()
    {
        return variant;
    }

    public static Map<String, MutalyzerResponse> mapMutByVariant(Stream<MutalyzerResponse> mutResponseStream)
    {
        return mutResponseStream.collect(Collectors.toMap(MutalyzerResponse::getVariant, t -> t));
    }
}
