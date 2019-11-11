package org.petermac.nic.api;

import org.petermac.nic.dataminer.domain.pathos.vcf.hgvs.ReferenceSequence;
import org.petermac.nic.dataminer.domain.pathos.vcf.hgvs.SpecialCharacters;
import org.petermac.nic.dataminer.domain.pathos.vcf.legacy.PositionAdjuster;
import org.petermac.nic.dataminer.domain.pathos.vcf.services.PreferredTranscriptService;
import org.petermac.nic.dataminer.parse.JSONObject;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * Created by Nic on 4/12/2018.
 * @deprecated
 */
@Service
public class VepFiltered implements JSONObject
{
    private static final Logger log = org.slf4j.LoggerFactory.getLogger(VepFiltered.class);

    public String error;
    public String warning;
    public VepResponse vepResponse;
    public Set<String> bestTranscripts = new HashSet<>();
    public Map besttx = new LinkedHashMap<>();
    public List<String> bestLrgs = new ArrayList<>();

    @Autowired
    PreferredTranscriptService transcriptsService;

    /**
     * Service CtoR
     */
    public VepFiltered()
    {

    }

    public VepFiltered getFiltered(VepResponse vepResponse)
    {
        return new VepFiltered(vepResponse, transcriptsService);
    }

    private VepFiltered(VepResponse vepResponse, PreferredTranscriptService transcriptsService)
    {
        log.info("getFiltered vepResponse={} ts={}", vepResponse, transcriptsService);
        bestTranscripts = new HashSet<>();
        besttx = new LinkedHashMap<>();
        bestLrgs = new ArrayList<>();
        this.vepResponse = vepResponse;
        final Map<String, Map> filterTs = new LinkedHashMap<>();//TS -> transcript_consequences
        if (vepResponse == null)
            error = "No response from VEP";
        else if (vepResponse.isEmpty())
            error = "Empty response from VEP";
        else if (transcriptsService == null)
            error = "No Transcript Service for VEP";
        else
        {
            transcriptsService.loadResources();
            for (VepResponse.VepAnnotation vepAnnotation : vepResponse)
            {
                if (vepAnnotation.transcript_consequences != null)
                    for (Map consequences : vepAnnotation.transcript_consequences)
                    {
                        String transcript_id = null;
                        try
                        {
                            final Object cds_start1 = consequences.get("cds_start");
                            if (cds_start1 != null)
                            {
                                final int cds_start = Integer.parseInt(cds_start1.toString());
                                final Object given_ref = consequences.get("given_ref");
                                final Object given_alt = consequences.get("variant_allele");
                                if (given_alt != null && given_ref != null)
                                {
                                    final PositionAdjuster positionAdjuster = new PositionAdjuster(cds_start, given_ref.toString(), given_alt.toString());
                                    transcript_id = SpecialCharacters.REFERENCE_SEPARATOR.join(consequences.get("transcript_id").toString(), ReferenceSequence.Type.CodingDNA.getPrefix() + positionAdjuster.getPosition().getPos() + positionAdjuster.getReference().format() + ">" + positionAdjuster.getAlt().format());
                                    log.info("transcript_id={}", transcript_id);

                                    filterTs.put(transcript_id, consequences);
                                }
                            }
                            if (transcript_id == null)
                                log.info("VEP: SKIPPED (null)transcript_id for map={}", consequences);
                        } catch (Exception e)
                        {
                            warning = "transcript_consequences" + e.toString();
                            log.warn("transcript_consequences", e);
                        }
                    }

                log.info("filterTs={}", filterTs.keySet());

                log.info("regulatory_feature_consequences={}", vepAnnotation.regulatory_feature_consequences); //new

                final String selectBestTranscript = transcriptsService.selectBestTranscript(PreferredTranscriptService.asList(filterTs.keySet()));
                if (selectBestTranscript == null)
                    log.info("No transcripts found for variant {}", filterTs);
                else
                {
                    bestTranscripts.add(selectBestTranscript);
                    bestLrgs.add(bestTranscripts == null ? "" : transcriptsService.selectLrg(selectBestTranscript));//, lrgMap));//>>> NK 20*
                }
                log.info("bestTranscript={}", selectBestTranscript);
            }
        }
        if (!bestTranscripts.isEmpty())
            besttx = filterTs.get(bestTranscripts.iterator().next());
    }
}
