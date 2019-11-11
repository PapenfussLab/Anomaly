package org.petermac.nic.api;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.petermac.nic.dao.MutalyzerResponse;
import org.petermac.nic.dataminer.domain.pathos.vcf.core.GenomeBuild;
import org.petermac.nic.dataminer.parse.JSONObject;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;

/**
 * Created by Nic on 14/03/2019.
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = {TestConfig.class, org.petermac.nic.dataminer.Application.class})
public class ClinGenARBuiltInTest
{
    private Logger log = org.slf4j.LoggerFactory.getLogger(getClass());

    @Autowired
    ClinGenARBuiltIn clinGenARBuiltIn;

    final String test_variant_chr1 = "chr1:g.43815102G>T";
    final String test_variant_chr2 = TestVariants.variant;


    @Autowired
    MutalyzerBuiltIn mutalyzerBuiltIn;

    final ArrayList<String> logged = new ArrayList<>();

    /**
     * {
     * "transcripts" : [ "LRG_510t1:c.1565+72G>T", "NM_005373.2:c.1565+72G>T", "XM_005270874.1:c.1544+72G>T" ],
     * "variant" : "chr1:g.43815102G>T",
     * "assembly" : "GRCh37",
     * "refSeq" : "NM_005373.2:c.1565+72G>T",
     * "error" : "(variantchecker): Intronic position given for a non-genomic reference sequence.",
     * "hgvsC" : "NM_005373.2:c.1565+72G>T",
     * "hgvsG" : "chr1:g.43815102G>T",
     * "hgvsP" : null,
     * "bestTranscript" : "NM_005373.2:c.1565+72G>T",
     * "filterTs" : "NM_005373.2:c.1565+72G>T",
     * "gene" : null,
     * "lrg" : "LRG_510t1:c.1565+72G>T",
     * "named" : {
     * "proteinRef" : null,
     * "restriction_sites_created" : null,
     * "restriction_sites_deleted" : null,
     * "affectedTranscripts" : null,
     * "affectedProteins" : null
     * }
     * }
     *
     * @throws Exception
     */
    @Test
    public void testGetMutalyzerChr1() throws Exception
    {
        JSONObject mutalyzerResponse = getMutalyzer(test_variant_chr1, GenomeBuild.GRCh37);
        log.info("MutalyzerResponse for  {} \n {}", test_variant_chr1, mutalyzerResponse.formatAsJson());
    }

    private MutalyzerResponse getMutalyzer(String hgvs, final GenomeBuild genomeBuild) throws Exception
    {

        log.info("Mutalyzer variant {}", hgvs);
        final MutalyzerResponse mutalyzerResponse = mutalyzerBuiltIn.call(hgvs, genomeBuild, logged);
        return mutalyzerResponse;
    }

    /**
     * {
     * "@context" : "http://reg.genome.network/schema/allele.jsonld",
     * "@id" : "http://reg.genome.network/allele/CA339989047",
     * "externalRecords" : {
     * "MyVariantInfo_hg19" : [ {
     * "@id" : "http://myvariant.info/v1/variant/chr1:g.43815102G>T?assembly=hg19",
     * "id" : "chr1:g.43815102G>T"
     * } ],
     * "MyVariantInfo_hg38" : [ {
     * "@id" : "http://myvariant.info/v1/variant/chr1:g.43349431G>T?assembly=hg38",
     * "id" : "chr1:g.43349431G>T"
     * } ]
     * },
     * "genomicAlleles" : [ {
     * "chromosome" : "1",
     * "coordinates" : [ {
     * "allele" : "T",
     * "end" : 43349431,
     * "referenceAllele" : "G",
     * "start" : 43349430
     * } ],
     * "hgvs" : [ "NC_000001.11:g.43349431G>T", "CM000663.2:g.43349431G>T" ],
     * "referenceGenome" : "GRCh38",
     * "referenceSequence" : "http://reg.genome.network/refseq/RS000049"
     * }, {
     * "chromosome" : "1",
     * "coordinates" : [ {
     * "allele" : "T",
     * "end" : 43815102,
     * "referenceAllele" : "G",
     * "start" : 43815101
     * } ],
     * "hgvs" : [ "NC_000001.10:g.43815102G>T", "CM000663.1:g.43815102G>T" ],
     * "referenceGenome" : "GRCh37",
     * "referenceSequence" : "http://reg.genome.network/refseq/RS000025"
     * }, {
     * "chromosome" : "1",
     * "coordinates" : [ {
     * "allele" : "T",
     * "end" : 43587689,
     * "referenceAllele" : "G",
     * "start" : 43587688
     * } ],
     * "hgvs" : [ "NC_000001.9:g.43587689G>T" ],
     * "referenceGenome" : "NCBI36",
     * "referenceSequence" : "http://reg.genome.network/refseq/RS000001"
     * }, {
     * "coordinates" : [ {
     * "allele" : "T",
     * "end" : 16628,
     * "referenceAllele" : "G",
     * "start" : 16627
     * } ],
     * "hgvs" : [ "NG_007525.1:g.16628G>T", "LRG_510:g.16628G>T" ],
     * "referenceSequence" : "http://reg.genome.network/refseq/RS000633"
     * } ],
     * "transcriptAlleles" : [ {
     * "coordinates" : [ {
     * "allele" : "T",
     * "end" : 1610,
     * "endIntronDirection" : "+",
     * "endIntronOffset" : 72,
     * "referenceAllele" : "G",
     * "start" : 1610,
     * "startIntronDirection" : "+",
     * "startIntronOffset" : 71
     * } ],
     * "gene" : "http://reg.genome.network/gene/GN007217",
     * "geneNCBI_id" : 4352,
     * "geneSymbol" : "MPL",
     * "hgvs" : [ "NM_005373.2:c.1565+72G>T", "LRG_510t1:c.1565+72G>T" ],
     * "proteinEffect" : {
     * "hgvs" : "NP_005364.1:p.="
     * },
     * "referenceSequence" : "http://reg.genome.network/refseq/RS030475"
     * }, {
     * "coordinates" : [ {
     * "allele" : "T",
     * "end" : 1975,
     * "endIntronDirection" : "+",
     * "endIntronOffset" : 72,
     * "referenceAllele" : "G",
     * "start" : 1975,
     * "startIntronDirection" : "+",
     * "startIntronOffset" : 71
     * } ],
     * "gene" : "http://reg.genome.network/gene/GN007217",
     * "geneNCBI_id" : 4352,
     * "geneSymbol" : "MPL",
     * "hgvs" : [ "XM_011541478.1:c.1544+72G>T" ],
     * "proteinEffect" : {
     * "hgvs" : "XP_011539780.1:p.="
     * },
     * "referenceSequence" : "http://reg.genome.network/refseq/RS108415"
     * }, {
     * "coordinates" : [ {
     * "allele" : "T",
     * "end" : 2710,
     * "endIntronDirection" : "+",
     * "endIntronOffset" : 72,
     * "referenceAllele" : "G",
     * "start" : 2710,
     * "startIntronDirection" : "+",
     * "startIntronOffset" : 71
     * } ],
     * "gene" : "http://reg.genome.network/gene/GN007217",
     * "geneNCBI_id" : 4352,
     * "geneSymbol" : "MPL",
     * "hgvs" : [ "XM_017001320.1:c.1736+72G>T" ],
     * "proteinEffect" : {
     * "hgvs" : "XP_016856809.1:p.="
     * },
     * "referenceSequence" : "http://reg.genome.network/refseq/RS557522"
     * }, {
     * "coordinates" : [ {
     * "allele" : "T",
     * "end" : 1607,
     * "endIntronDirection" : "+",
     * "endIntronOffset" : 72,
     * "referenceAllele" : "G",
     * "start" : 1607,
     * "startIntronDirection" : "+",
     * "startIntronOffset" : 71
     * } ],
     * "hgvs" : [ "ENST00000372470.7:c.1565+72G>T" ],
     * "proteinEffect" : {
     * "hgvs" : "ENSP00000361548.3:p.="
     * },
     * "referenceSequence" : "http://reg.genome.network/refseq/RS269121"
     * }, {
     * "coordinates" : [ {
     * "allele" : "T",
     * "end" : 1637,
     * "referenceAllele" : "G",
     * "start" : 1636
     * } ],
     * "hgvs" : [ "ENST00000413998.6:n.1637G>T" ],
     * "proteinEffect" : {
     * "hgvs" : "ENSP00000414004.2:p.Trp546Leu",
     * "hgvsWellDefined" : "ENSP00000414004.2:p.Trp546Leu"
     * },
     * "referenceSequence" : "http://reg.genome.network/refseq/RS283327"
     * }, {
     * "coordinates" : [ {
     * "allele" : "T",
     * "end" : 1635,
     * "referenceAllele" : "G",
     * "start" : 1634
     * } ],
     * "hgvs" : [ "ENST00000612993.1:n.1635G>T" ],
     * "proteinEffect" : {
     * "hgvs" : "ENSP00000480273.1:p.Leu545Phe",
     * "hgvsWellDefined" : "ENSP00000480273.1:p.Leu545Phe"
     * },
     * "referenceSequence" : "http://reg.genome.network/refseq/RS401553"
     * } ],
     * "type" : "nucleotide"
     * }
     *
     * @throws Exception
     */
    @Test
    public void testGetClinGenChr1() throws Exception
    {
        JSONObject clinGenResponse = clinGenARBuiltIn.getClinGenResponse(test_variant_chr1, GenomeBuild.GRCh37);
        log.info("ClinGenResponse for  {} \n {}", test_variant_chr1, clinGenResponse.formatAsJson());
    }


//    ----------------------------------------------------------------------------------------------------------------


    /**
     * {
     * "transcripts" : [ "LRG_218t1:c.1777C>T", "NM_000251.2:c.1777C>T", "NM_001258281.1:c.1579C>T", "XM_005264333.1:c.1627C>T", "XM_005264332.1:c.1777C>T", "NM_000251.1:c.1777C>T" ],
     * "variant" : "chr2:g.47702181C>T",
     * "assembly" : "GRCh37",
     * "refSeq" : "NM_000251.2:c.1777C>T",
     * "error" : "",
     * "hgvsC" : "NM_000251.2:c.1777C>T",
     * "hgvsG" : "chr2:g.47702181C>T",
     * "hgvsP" : "NP_000242.1:p.(Gln593*)",
     * "bestTranscript" : "NM_000251.2:c.1777C>T",
     * "filterTs" : "NM_000251.2:c.1777C>T",
     * "gene" : "MSH2_v001",
     * "lrg" : null,
     * "named" : {
     * "proteinRef" : "NP_000242.1",
     * "restriction_sites_created" : "AccI,Hpy166II",
     * "restriction_sites_deleted" : "HpyCH4V",
     * "affectedTranscripts" : "NM_000251.2(MSH2_v001):c.1777C>T",
     * "affectedProteins" : "NM_000251.2(MSH2_i001):p.(Gln593*)"
     * }
     * }
     *
     * @throws Exception
     */
    @Test
    public void testGetMutalyzerChr2() throws Exception
    {
        JSONObject mutalyzerResponse = getMutalyzer(test_variant_chr2, GenomeBuild.GRCh37);
        log.info("MutalyzerResponse for  {} \n {}", test_variant_chr2, mutalyzerResponse.formatAsJson());
    }

    /**
     * {
     * "@context" : "http://reg.genome.network/schema/allele.jsonld",
     * "@id" : "http://reg.genome.network/allele/CA019278",
     * "externalRecords" : {
     * "ClinVarAlleles" : [ {
     * "@id" : "http://www.ncbi.nlm.nih.gov/clinvar/?term=96258[alleleid]",
     * "alleleId" : 96258,
     * "preferredName" : "NM_000251.2(MSH2):c.1777C>T (p.Gln593Ter)"
     * } ],
     * "ClinVarVariations" : [ {
     * "@id" : "http://www.ncbi.nlm.nih.gov/clinvar/variation/90783",
     * "RCV" : [ "RCV000076282", "RCV000540595" ],
     * "variationId" : 90783
     * } ],
     * "MyVariantInfo_hg19" : [ {
     * "@id" : "http://myvariant.info/v1/variant/chr2:g.47702181C>T?assembly=hg19",
     * "id" : "chr2:g.47702181C>T"
     * } ],
     * "MyVariantInfo_hg38" : [ {
     * "@id" : "http://myvariant.info/v1/variant/chr2:g.47475042C>T?assembly=hg38",
     * "id" : "chr2:g.47475042C>T"
     * } ],
     * "dbSNP" : [ {
     * "@id" : "http://www.ncbi.nlm.nih.gov/snp/63750200",
     * "rs" : 63750200
     * } ]
     * },
     * "genomicAlleles" : [ {
     * "chromosome" : "2",
     * "coordinates" : [ {
     * "allele" : "T",
     * "end" : 47475042,
     * "referenceAllele" : "C",
     * "start" : 47475041
     * } ],
     * "hgvs" : [ "NC_000002.12:g.47475042C>T", "CM000664.2:g.47475042C>T" ],
     * "referenceGenome" : "GRCh38",
     * "referenceSequence" : "http://reg.genome.network/refseq/RS000050"
     * }, {
     * "chromosome" : "2",
     * "coordinates" : [ {
     * "allele" : "T",
     * "end" : 47702181,
     * "referenceAllele" : "C",
     * "start" : 47702180
     * } ],
     * "hgvs" : [ "NC_000002.11:g.47702181C>T", "CM000664.1:g.47702181C>T" ],
     * "referenceGenome" : "GRCh37",
     * "referenceSequence" : "http://reg.genome.network/refseq/RS000026"
     * }, {
     * "chromosome" : "2",
     * "coordinates" : [ {
     * "allele" : "T",
     * "end" : 47555685,
     * "referenceAllele" : "C",
     * "start" : 47555684
     * } ],
     * "hgvs" : [ "NC_000002.10:g.47555685C>T" ],
     * "referenceGenome" : "NCBI36",
     * "referenceSequence" : "http://reg.genome.network/refseq/RS000002"
     * }, {
     * "coordinates" : [ {
     * "allele" : "T",
     * "end" : 76919,
     * "referenceAllele" : "C",
     * "start" : 76918
     * } ],
     * "hgvs" : [ "NG_007110.2:g.76919C>T", "LRG_218:g.76919C>T" ],
     * "referenceSequence" : "http://reg.genome.network/refseq/RS000486"
     * } ],
     * "transcriptAlleles" : [ {
     * "coordinates" : [ {
     * "allele" : "T",
     * "end" : 1902,
     * "referenceAllele" : "C",
     * "start" : 1901
     * } ],
     * "gene" : "http://reg.genome.network/gene/GN007325",
     * "geneNCBI_id" : 4436,
     * "geneSymbol" : "MSH2",
     * "hgvs" : [ "NM_000251.2:c.1777C>T", "LRG_218t1:c.1777C>T" ],
     * "proteinEffect" : {
     * "hgvs" : "NP_000242.1:p.Gln593Ter",
     * "hgvsWellDefined" : "NP_000242.1:p.Gln593Ter"
     * },
     * "referenceSequence" : "http://reg.genome.network/refseq/RS006313"
     * }, {
     * "coordinates" : [ {
     * "allele" : "T",
     * "end" : 1718,
     * "referenceAllele" : "C",
     * "start" : 1717
     * } ],
     * "gene" : "http://reg.genome.network/gene/GN007325",
     * "geneNCBI_id" : 4436,
     * "geneSymbol" : "MSH2",
     * "hgvs" : [ "NM_001258281.1:c.1579C>T" ],
     * "proteinEffect" : {
     * "hgvs" : "NP_001245210.1:p.Gln527Ter",
     * "hgvsWellDefined" : "NP_001245210.1:p.Gln527Ter"
     * },
     * "referenceSequence" : "http://reg.genome.network/refseq/RS019766"
     * }, {
     * "coordinates" : [ {
     * "allele" : "T",
     * "end" : 1849,
     * "referenceAllele" : "C",
     * "start" : 1848
     * } ],
     * "gene" : "http://reg.genome.network/gene/GN007325",
     * "geneNCBI_id" : 4436,
     * "geneSymbol" : "MSH2",
     * "hgvs" : [ "XM_005264332.2:c.1777C>T" ],
     * "proteinEffect" : {
     * "hgvs" : "XP_005264389.2:p.Gln593Ter",
     * "hgvsWellDefined" : "XP_005264389.2:p.Gln593Ter"
     * },
     * "referenceSequence" : "http://reg.genome.network/refseq/RS064175"
     * }, {
     * "coordinates" : [ {
     * "allele" : "T",
     * "end" : 1849,
     * "referenceAllele" : "C",
     * "start" : 1848
     * } ],
     * "gene" : "http://reg.genome.network/gene/GN007325",
     * "geneNCBI_id" : 4436,
     * "geneSymbol" : "MSH2",
     * "hgvs" : [ "XM_011532867.1:c.1777C>T" ],
     * "proteinEffect" : {
     * "hgvs" : "XP_011531169.1:p.Gln593Ter",
     * "hgvsWellDefined" : "XP_011531169.1:p.Gln593Ter"
     * },
     * "referenceSequence" : "http://reg.genome.network/refseq/RS099893"
     * }, {
     * "coordinates" : [ {
     * "allele" : "T",
     * "end" : 1849,
     * "referenceAllele" : "C",
     * "start" : 1848
     * } ],
     * "gene" : "http://reg.genome.network/gene/GN007325",
     * "geneNCBI_id" : 4436,
     * "geneSymbol" : "MSH2",
     * "hgvs" : [ "XR_939685.1:n.1849C>T" ],
     * "referenceSequence" : "http://reg.genome.network/refseq/RS134381"
     * }, {
     * "coordinates" : [ {
     * "allele" : "T",
     * "end" : 1839,
     * "referenceAllele" : "C",
     * "start" : 1838
     * } ],
     * "gene" : "http://reg.genome.network/gene/GN007325",
     * "geneNCBI_id" : 4436,
     * "geneSymbol" : "MSH2",
     * "hgvs" : [ "XM_005264332.4:c.1777C>T" ],
     * "referenceSequence" : "http://reg.genome.network/refseq/RS535844"
     * }, {
     * "coordinates" : [ {
     * "allele" : "T",
     * "end" : 1839,
     * "referenceAllele" : "C",
     * "start" : 1838
     * } ],
     * "gene" : "http://reg.genome.network/gene/GN007325",
     * "geneNCBI_id" : 4436,
     * "geneSymbol" : "MSH2",
     * "hgvs" : [ "XM_011532867.2:c.1777C>T" ],
     * "referenceSequence" : "http://reg.genome.network/refseq/RS551004"
     * }, {
     * "coordinates" : [ {
     * "allele" : "T",
     * "end" : 1839,
     * "referenceAllele" : "C",
     * "start" : 1838
     * } ],
     * "gene" : "http://reg.genome.network/gene/GN007325",
     * "geneNCBI_id" : 4436,
     * "geneSymbol" : "MSH2",
     * "hgvs" : [ "XR_001738747.2:n.1839C>T" ],
     * "referenceSequence" : "http://reg.genome.network/refseq/RS589206"
     * }, {
     * "coordinates" : [ {
     * "allele" : "T",
     * "end" : 1839,
     * "referenceAllele" : "C",
     * "start" : 1838
     * } ],
     * "gene" : "http://reg.genome.network/gene/GN007325",
     * "geneNCBI_id" : 4436,
     * "geneSymbol" : "MSH2",
     * "hgvs" : [ "XR_939685.2:n.1839C>T" ],
     * "referenceSequence" : "http://reg.genome.network/refseq/RS612357"
     * }, {
     * "coordinates" : [ {
     * "allele" : "T",
     * "end" : 2000,
     * "referenceAllele" : "C",
     * "start" : 1999
     * } ],
     * "hgvs" : [ "ENST00000233146.6:c.1777C>T" ],
     * "proteinEffect" : {
     * "hgvs" : "ENSP00000233146.2:p.Gln593Ter",
     * "hgvsWellDefined" : "ENSP00000233146.2:p.Gln593Ter"
     * },
     * "referenceSequence" : "http://reg.genome.network/refseq/RS247203"
     * }, {
     * "coordinates" : [ {
     * "allele" : "T",
     * "end" : 1839,
     * "referenceAllele" : "C",
     * "start" : 1838
     * } ],
     * "hgvs" : [ "ENST00000406134.5:c.1777C>T" ],
     * "proteinEffect" : {
     * "hgvs" : "ENSP00000384199.1:p.Gln593Ter",
     * "hgvsWellDefined" : "ENSP00000384199.1:p.Gln593Ter"
     * },
     * "referenceSequence" : "http://reg.genome.network/refseq/RS247307"
     * }, {
     * "coordinates" : [ {
     * "allele" : "T",
     * "end" : 1718,
     * "referenceAllele" : "C",
     * "start" : 1717
     * } ],
     * "hgvs" : [ "ENST00000543555.5:c.1579C>T" ],
     * "proteinEffect" : {
     * "hgvs" : "ENSP00000442697.1:p.Gln527Ter",
     * "hgvsWellDefined" : "ENSP00000442697.1:p.Gln527Ter"
     * },
     * "referenceSequence" : "http://reg.genome.network/refseq/RS247173"
     * }, {
     * "coordinates" : [ {
     * "allele" : "T",
     * "end" : 1815,
     * "referenceAllele" : "C",
     * "start" : 1814
     * } ],
     * "hgvs" : [ "ENST00000610696.4:c.*173C>T" ],
     * "proteinEffect" : {
     * "hgvs" : "ENSP00000483159.1:p.="
     * },
     * "referenceSequence" : "http://reg.genome.network/refseq/RS400534"
     * }, {
     * "coordinates" : [ {
     * "allele" : "T",
     * "end" : 1815,
     * "referenceAllele" : "C",
     * "start" : 1814
     * } ],
     * "hgvs" : [ "ENST00000613514.4:c.*317C>T" ],
     * "proteinEffect" : {
     * "hgvs" : "ENSP00000484137.1:p.="
     * },
     * "referenceSequence" : "http://reg.genome.network/refseq/RS401778"
     * }, {
     * "coordinates" : [ {
     * "allele" : "T",
     * "end" : 1816,
     * "referenceAllele" : "C",
     * "start" : 1815
     * } ],
     * "hgvs" : [ "ENST00000617333.3:c.*543C>T" ],
     * "proteinEffect" : {
     * "hgvs" : "ENSP00000482468.1:p.="
     * },
     * "referenceSequence" : "http://reg.genome.network/refseq/RS403437"
     * }, {
     * "coordinates" : [ {
     * "allele" : "T",
     * "end" : 1815,
     * "referenceAllele" : "C",
     * "start" : 1814
     * } ],
     * "hgvs" : [ "ENST00000617938.4:c.*749C>T" ],
     * "proteinEffect" : {
     * "hgvs" : "ENSP00000481158.1:p.="
     * },
     * "referenceSequence" : "http://reg.genome.network/refseq/RS403700"
     * }, {
     * "coordinates" : [ {
     * "allele" : "T",
     * "end" : 1817,
     * "referenceAllele" : "C",
     * "start" : 1816
     * } ],
     * "hgvs" : [ "ENST00000621359.2:c.1777C>T" ],
     * "proteinEffect" : {
     * "hgvs" : "ENSP00000481416.1:p.Gln593Ter",
     * "hgvsWellDefined" : "ENSP00000481416.1:p.Gln593Ter"
     * },
     * "referenceSequence" : "http://reg.genome.network/refseq/RS405232"
     * } ],
     * "type" : "nucleotide"
     * }
     *
     * @throws Exception
     */
    @Test
    public void testGetClinGenChr2() throws Exception
    {
        JSONObject clinGenResponse = clinGenARBuiltIn.getClinGenResponse(test_variant_chr2, GenomeBuild.GRCh37);
        log.info("ClinGenResponse for  {} \n {}", test_variant_chr2, clinGenResponse.formatAsJson());
    }


    @Test
    public void testToMutChr2() throws Exception
    {
        MutalyzerResponse clinGenResponse = clinGenARBuiltIn.toMut(test_variant_chr2, GenomeBuild.GRCh37);
        log.info("as Mut for  {} \n {}", test_variant_chr2, clinGenResponse.formatAsJson());
    }
}