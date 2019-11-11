package org.petermac.nic.dao;

import com.fasterxml.jackson.databind.SerializationFeature;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.petermac.nic.api.AnnotationResponseBuilder;
import org.petermac.nic.api.TestConfig;
import org.petermac.nic.api.VepFiltered;
import org.petermac.nic.api.VepResponse;
import org.petermac.nic.dataminer.domain.pathos.vcf.annotation.AccumulatedAnnotations;
import org.petermac.nic.dataminer.domain.pathos.vcf.annotation.AnnotationPipeline;
import org.petermac.nic.dataminer.domain.pathos.vcf.core.GenomeBuild;
import org.petermac.nic.dataminer.domain.pathos.vcf.services.PreferredTranscriptService;
import org.petermac.nic.dataminer.domain.pathos.vcf.sources.AnnotationSource;
import org.petermac.nic.dataminer.parse.JSON;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.DefaultUriBuilderFactory;
import org.springframework.web.util.UriBuilder;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.testng.AssertJUnit.assertEquals;

/**
 * Created by Nic on 1/08/2018.
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = {TestConfig.class, org.petermac.nic.dataminer.Application.class})
@WebAppConfiguration
public class ApiRestCallTest
{

    private static final Logger log = org.slf4j.LoggerFactory.getLogger(ApiRestCallTest.class);

    private static final MappingJackson2HttpMessageConverter MESSAGE_CONVERTER = new MappingJackson2HttpMessageConverter();

    @Autowired
    AnnotationResponseBuilder annotationResponseBuilder;

    @Autowired
    AnnotationPipeline annotationPipeline;

    @Autowired
    PreferredTranscriptService transcriptsService;


    public static void LogE(Exception e)
    {
        log.warn("Exception", e);
    }

    @Test
    public void myVariantCall() throws Exception
    {
        MESSAGE_CONVERTER.getObjectMapper().configure(SerializationFeature.INDENT_OUTPUT, false);

        MyVariantResponse ano = ApiRestCall.GetHgvs("chr2:g.47702181C>T", AnnotationSource.MyVariant, ApiRestCallTest::LogE, MyVariantResponse.class, GenomeBuild.GRCh37, null);


        System.err.println(ano);

//        Ano ano = JSON.write(Ano.class, s);

        System.err.println("Success =" + ano.success);
        System.err.println("ERROR =" + ano.error);

        System.err.println("VCF ALT =" + ano.vcf.alt);
        System.err.println("VCF REF =" + ano.vcf.ref);
        System.err.println("VCF PSO =" + ano.vcf.position);
        System.err.println("DbSNP Gene.sym =" + ((Map) ano.dbsnp.get("gene")).get("symbol"));
        System.err.println("DbSNP Gene.id =" + ((Map) ano.dbsnp.get("gene")).get("geneid"));
        System.err.println(" Hg19.start =" + ano.hg19.start);
        System.err.println(" Hg19.end =" + ano.hg19.end);
        System.err.println("ano.cadd.getClass = " + ano.cadd.getClass());

        System.err.println(JSON.format(ano));
        System.err.println("ano.cadd.getClass = " + ano.cadd.getClass());
        System.err.println("ano.cadd.get(annotype) = " + ano.cadd.get("annotype"));
    }


    @Test
    /**
     * _id: "chr2:g.47702181C>T",
     * _version: 4,
     * cadd: {},
     * chrom: "2",
     * clinvar: {},
     * dbnsfp: {},
     * dbsnp: {},
     * hg19: {},
     * observed: true,
     * snpeff: {},
     * vcf: {}
     * }
     * <p>
     * No support for GRCh38. hg19 only.
     *
     * @param hgvs
     * @return
     */
    public void myVariantCallRaw() throws Exception
    {

        MESSAGE_CONVERTER.getObjectMapper().configure(SerializationFeature.INDENT_OUTPUT, false);

        final String baseUrl = AnnotationSource.MyVariant.getUrl();
        final DefaultUriBuilderFactory factory = new DefaultUriBuilderFactory(baseUrl);
        factory.setEncodingMode(DefaultUriBuilderFactory.EncodingMode.VALUES_ONLY);
        final UriBuilder b = factory.builder();
        String hgvs = "chr2:g.47702181C>T";
        final URI uri = b.path("/v1/variant/{hgvs}").build(hgvs); // v1/variant/chr2:g.47702181C>T?fields=all
        //http://myvariant.info/v1/variant/chr2:g.47702181C>T?fields=all
        log.info("calling {}", uri);

        final RestTemplate restTemplate = new RestTemplate();
        final ResponseEntity<MyVariantResponse> response = restTemplate.getForEntity(uri, MyVariantResponse.class);
        MyVariantResponse ano = response.getBody();
        System.err.println("VCF ALT =" + ano.vcf.alt);
        System.err.println("VCF REF =" + ano.vcf.ref);
        System.err.println("VCF PSO =" + ano.vcf.position);
        System.err.println("DbSNP Gene.sym =" + ((Map) ano.dbsnp.get("gene")).get("symbol"));
        System.err.println("DbSNP Gene.id =" + ((Map) ano.dbsnp.get("gene")).get("geneid"));
        System.err.println(" Hg19.start =" + ano.hg19.start);
        System.err.println(" Hg19.end =" + ano.hg19.end);
        System.err.println("ano.cadd.getClass = " + ano.cadd.getClass());

        System.out.println(JSON.format(ano));
        System.err.println("ano.cadd.getClass = " + ano.cadd.getClass());
        System.err.println("ano.cadd.get(annotype) = " + ano.cadd.get("annotype"));
    }

    @Test
    public void myVariantCall38() throws Exception
    {

        MESSAGE_CONVERTER.getObjectMapper().configure(SerializationFeature.INDENT_OUTPUT, false);

        //chr17:g.7676154G%3EC?assembly=hg38 works
        MyVariantResponse ano = ApiRestCall.GetHgvs("chr17:g.7676154G>C", AnnotationSource.MyVariant, ApiRestCallTest::LogE, MyVariantResponse.class, GenomeBuild.GRCh38, null);

        System.err.println("Success =" + ano.success);
        System.err.println("ERROR =" + ano.error);

        System.out.println(JSON.format(ano));

        System.out.println(ano);

//        Ano ano = JSON.write(Ano.class, s);

        if (ano.vcf != null)
        {
            System.err.println("VCF ALT =" + ano.vcf.alt);
            System.err.println("VCF REF =" + ano.vcf.ref);
            System.err.println("VCF PSO =" + ano.vcf.position);
        }
        System.err.println("DbSNP Gene.sym =" + ((Map) ano.dbsnp.get("gene")).get("symbol"));
        System.err.println("DbSNP Gene.id =" + ((Map) ano.dbsnp.get("gene")).get("geneid"));
        System.err.println(" Hg19.start =" + ano.hg19.start);
        System.err.println(" Hg19.end =" + ano.hg19.end);
        if (ano.cadd != null)
        {
            System.err.println("ano.cadd.getClass = " + ano.cadd.getClass());
            System.err.println("ano.cadd.getClass = " + ano.cadd.getClass());
            System.err.println("ano.cadd.get(annotype) = " + ano.cadd.get("annotype"));
        }
        System.out.println(JSON.format(ano));

    }

    @Autowired
    VepFiltered vepFiltered;

    @Test
    public void vepCall() throws Exception
    {

        MESSAGE_CONVERTER.getObjectMapper().configure(SerializationFeature.INDENT_OUTPUT, true);

        VepResponse vepResponse = ApiRestCall.GetHgvs("chr2:g.47702181C>T", AnnotationSource.Vep, ApiRestCallTest::LogE, VepResponse.class, GenomeBuild.NULL, null);
        System.out.println("vepResponse=" + vepResponse);
        System.out.println("\nvepResponse=" + JSON.format(vepResponse));
//        VepFiltered vepFiltered = new VepFiltered(vepResponse, transcriptsService);
        vepFiltered.getFiltered(vepResponse);
        System.out.println("\nvepFiltered=" + JSON.format(vepFiltered));
        System.out.println();

        for (VepResponse.VepAnnotation ano : vepResponse)
        {

//        Ano ano = JSON.write(Ano.class, s);

            System.out.println("allele_string =" + ano.allele_string);
            System.out.println(" start =" + ano.start);
            System.out.println(" end =" + ano.end);
            System.out.println("input =" + ano.input);
            System.out.println("id =" + ano.id);
            System.out.println("assembly_name =" + ano.assembly_name);
            System.out.println("seq_region_name =" + ano.seq_region_name);
            System.out.println("strand =" + ano.strand);
            System.out.println("most_severe_consequence =" + ano.most_severe_consequence);

            System.out.println(" getColocated_variant_ids =" + ano.getColocated_variant_ids());
            final List<String> transcript_consequence_ids = ano.getTranscript_consequence_ids();
            System.out.println(" ano.getTranscript_consequence_ids =" + transcript_consequence_ids);
            System.out.println(" ano.getTranscript_consequence_Genes =" + ano.getTranscript_consequence_Genes());
//
            System.out.println("ano.transcript_consequences = " + ano.transcript_consequences.getClass());
            System.out.println("ano.transcript_consequences.iterator().next().get(variant_allele) = " + ano.transcript_consequences.iterator().next().get("variant_allele"));
            assertEquals("[NM_000251.2, NM_001258281.1, XM_005264332.1, XM_005264333.1]", transcript_consequence_ids.toString());
            assertEquals("[CM041805, rs63750200]", ano.getColocated_variant_ids().toString());
        }
    }


    @Test
    public void mutCall() throws Exception
    {

        MESSAGE_CONVERTER.getObjectMapper().configure(SerializationFeature.INDENT_OUTPUT, true);

        final Stream<AccumulatedAnnotations> stream = annotationPipeline.annotateHgvs(Stream.of("chr2:g.47702181C>T"));
        final AccumulatedAnnotations ano = stream.collect(Collectors.toList()).get(0);
        System.out.println(JSON.format(ano));


    }


    public static VepResponse vepCall(String hgvs, AnnotationSource source) //throws Exception
    {
        final String baseUrl = source.getUrl();
        log.info("baseUrl= {}", baseUrl);

        final DefaultUriBuilderFactory factory = new DefaultUriBuilderFactory(baseUrl);
        factory.setEncodingMode(DefaultUriBuilderFactory.EncodingMode.VALUES_ONLY);
        final UriBuilder b = factory.builder();
        final URI uri = b.path("/vep/human/hgvs/{hgvs}?content-type=application/json").build(hgvs);
        //http:///vep/human/hgvs/chr2:g.47702181C>T?content-type=application/json
        log.info("calling {}", uri);

        final RestTemplate restTemplate = new RestTemplate();
        final ResponseEntity<VepResponse> response = restTemplate.getForEntity(uri, VepResponse.class);
        return response.getBody();
    }


    /**
     * NON SpringBoot version fast
     *
     * @param args
     */
    public static void main(String[] args) throws Exception
    {
        mainVep();
    }


    public static void mainMut()
    {

//        MESSAGE_CONVERTER.getObjectMapper().configure(SerializationFeature.INDENT_OUTPUT, true);
//
//        final Stream<AccumulatedAnnotations> stream = annotationPipeline.annotateHgvs(Stream.of("chr2:g.47702181C>T"));
//        final AccumulatedAnnotations ano = stream.collect(Collectors.toList()).get(0);
//        System.out.println(JSON.format(ano));

    }

    public static void mainVep()
    {
        VepResponse response = new VepResponse();
//         response = AnnotationController.RestGet("chr2:g.47702181C>T", AnnotationSource.Vep, VepResponse.class);
//         response = AnnotationController.RestGet("chr2:g.47702181C>T", AnnotationSource.Vep38, VepResponse.class);
//        MyVariantResponse ano = AnnotationController.myVariantCall("chr2:g.47702181C>T");


        System.out.println(response);

//        Ano ano = JSON.write(Ano.class, s);

//        System.err.println("VCF ALT =" + response.vcf.alt);
//        System.err.println("VCF REF =" + response.vcf.ref);
//        System.err.println("VCF PSO =" + response.vcf.position);

        System.out.println(JSON.format(response));
        System.out.println("vepResponse=" + response);

        for (VepResponse.VepAnnotation ano : response)
        {

//        Ano ano = JSON.write(Ano.class, s);

            System.out.println("allele =" + ano.allele_string);
            System.out.println(" Hg19.start =" + ano.start);
            System.out.println(" Hg19.end =" + ano.end);
            System.out.println(" getColocated_variant_ids =" + ano.getColocated_variant_ids());
            System.out.println(" ano.getTranscript_consequence_ids =" + ano.getTranscript_consequence_ids());
            System.out.println("ano.transcript_consequences = " + ano.transcript_consequences.getClass());
//
//        System.out.println(JSON.format(ano));
//        System.out.println("ano.transcript_consequences = " + ano.transcript_consequences.getClass());
            System.out.println("ano.transcript_consequences.iterator().next().get(variant_allele) = " + ano.transcript_consequences.iterator().next().get("variant_allele"));
        }
      /*  System.err.println("" + JsonPath.compile("$.vcf").read(s));//{alt=T, position=47702181, ref=C}
        System.err.println("" + JsonPath.compile("$.vcf.position").read(s));//47702181
        System.err.println("" + JsonPath.compile("$.dbsnp.gene.symbol").read(s));//MSH2
        JsonPath jsonPath = JsonPath.compile(".gene");
        Object o = jsonPath.read(s);
        System.err.println("json . read = " + o);
        System.err.println("json . read class  = " + o.getClass());
        */
    }

}