package org.petermac.nic.api;

import org.petermac.nic.dao.MutalyzerResponse;
import org.petermac.nic.dataminer.domain.pathos.vcf.annotation.AnnotationPipeline;
import org.petermac.nic.dataminer.domain.pathos.vcf.core.GenomeBuild;
import org.petermac.nic.dataminer.domain.pathos.vcf.core.VariantCall;
import org.petermac.nic.dataminer.domain.pathos.vcf.legacy.Variant;
import org.petermac.nic.dataminer.domain.pathos.vcf.transcode.VcfFile;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by Nic on 7/12/2018.
 */
@Component
public class MutalyzerBuiltIn implements BuiltIn
{
    private Logger log = org.slf4j.LoggerFactory.getLogger(getClass());

    private final AnnotationPipeline mutalyzerPipeline;//    private final Pipeline<AccumulatedAnnotations> mutalyzerPipeline;

    public MutalyzerBuiltIn(@Autowired AnnotationPipeline mutalyzerPipeline)
    {
        this.mutalyzerPipeline = mutalyzerPipeline;
    }

       public Stream<MutalyzerResponse> call(VcfFile vcfFile, GenomeBuild genomeBuild, List<String> log) //throws Exception
    {
        return call(vcfFile.getVcfVars().map(Variant::Create).map(VariantCall::getHgvsGenomicReference).collect(Collectors.toSet()), genomeBuild, log);
    }

    public Stream<MutalyzerResponse> call(Collection<String> variants, GenomeBuild genomeBuild, List<String> log) //throws Exception
    {
        if (mutalyzerPipeline == null)
        {
            System.err.println("mutalyzerPipeline is NULL");
            return Stream.empty();
        }
        mutalyzerPipeline.setGenomeBuild(genomeBuild);
        mutalyzerPipeline.setListener(log::add);
        return mutalyzerPipeline.annotateHgvs(variants.stream()).map(aa ->
                {
                    final MutalyzerResponse mutResponse = new MutalyzerResponse();
                    mutResponse.refSeq = aa.getRefSeq();
                    mutResponse.assembly = aa.getGenomeBuild().toString();
                    mutResponse.variant = aa.getVariant();
                    mutResponse.error = aa.getError();
                    mutResponse.hgvsC = aa.getHgvsC();
                    mutResponse.hgvsG = aa.getHgvsG();
                    mutResponse.filterTs = aa.getFilterTs();
                    mutResponse.bestTranscript = aa.getFilterTs();
                    mutResponse.gene = aa.getGene();
                    mutResponse.transcripts = aa.getTranscripts();
                    mutResponse.hgvsP = aa.getHgvsp();
                    mutResponse.lrg = aa.getLrg();
                    if (aa.getNameChecked() != null)
                    {
                        mutResponse.named.proteinRef = aa.getNameChecked().getProtein_ref();
                        mutResponse.named.affectedProteins = aa.getNameChecked().getAffected_proteins();
                        mutResponse.named.affectedTranscripts = aa.getNameChecked().getAffected_transcripts();
                        mutResponse.named.restriction_sites_created = aa.getNameChecked().getRestriction_sites_created();
                        mutResponse.named.restriction_sites_deleted = aa.getNameChecked().getRestriction_sites_deleted();
                    }
                    return mutResponse;
                }
        );
    }

    public MutalyzerResponse call(String hgvs, GenomeBuild genomeBuild, List<String> log) throws Exception
    {
        mutalyzerPipeline.setGenomeBuild(genomeBuild);
        mutalyzerPipeline.setListener(log::add);
        return mutalyzerPipeline.annotateHgvs(Stream.of(hgvs)).map(aa ->
                {
                    final MutalyzerResponse mutResponse = new MutalyzerResponse();
                    mutResponse.refSeq = aa.getRefSeq();
                    mutResponse.assembly = aa.getGenomeBuild().toString();
                    mutResponse.variant = aa.getVariant();
                    mutResponse.error = aa.getError();
                    mutResponse.hgvsC = aa.getHgvsC();
                    mutResponse.hgvsG = aa.getHgvsG();
                    mutResponse.filterTs = aa.getFilterTs();
                    mutResponse.bestTranscript = aa.getFilterTs();
                    mutResponse.gene = aa.getGene();
                    mutResponse.transcripts = aa.getTranscripts();
                    mutResponse.hgvsP = aa.getHgvsp();
                    mutResponse.lrg = aa.getLrg();
                    if (aa.getNameChecked() != null)
                    {
                        mutResponse.named.proteinRef = aa.getNameChecked().getProtein_ref();
                        mutResponse.named.affectedProteins = aa.getNameChecked().getAffected_proteins();
                        mutResponse.named.affectedTranscripts = aa.getNameChecked().getAffected_transcripts();
                        mutResponse.named.restriction_sites_created = aa.getNameChecked().getRestriction_sites_created();
                        mutResponse.named.restriction_sites_deleted = aa.getNameChecked().getRestriction_sites_deleted();
                    }
                    return mutResponse;
                }
        ).findFirst().orElseThrow(() -> new Exception("No Mutalyzer results"));
    }
}
