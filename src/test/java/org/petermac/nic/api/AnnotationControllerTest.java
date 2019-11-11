package org.petermac.nic.api;

import com.jayway.jsonpath.JsonPath;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.petermac.nic.dataminer.domain.pathos.vcf.core.GenomeBuild;
import org.petermac.nic.dataminer.domain.pathos.vcf.sources.AnnotationSource;
import org.petermac.nic.dataminer.proj.BuildInfo;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.testng.AssertJUnit.assertEquals;

/**
 * @author Nick Kravchenko (thanks to Josh Long)
 * @SpringBootTest(classes = {TestConfig.class, AnnotationPipeline.class, VcfFiles.class})
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = {TestConfig.class, org.petermac.nic.dataminer.Application.class})
public class AnnotationControllerTest
{

    private static final Logger log = org.slf4j.LoggerFactory.getLogger(AnnotationControllerTest.class);

    private MediaType contentType = new MediaType(MediaType.APPLICATION_JSON.getType(),
            MediaType.APPLICATION_JSON.getSubtype(),
            Charset.forName("utf8"));

    private MockMvc mockMvc;

    private final String variant = TestVariants.variantChr10;
    private final String vcfVariant1 = "13\t32911299\t.\tAACAA\tA\t.\tPASS\tADP=1504;WT=0;HET=1;HOM=0;NC=0;HGVSg=chr13:g.32911300_32911303del;HGVSc=NM_000059.3:c.2808_2811del;HGVSp=NP_000050.2:p.(Ala938Profs*21);gene=BRCA2;lrg=LRG_293t1:c.2806_2809del;muterr=(variantchecker):_Sequence_\"AAAC\"_at_position_3033_3036_was_given,_however,_the_HGVS_notation_prescribes_that_on_the_forward_strand_it_should_be_\"ACAA\"_at_position_3035_3038.;status=RENAMED_from:chr13:g.32911298_32911301del_to:chr13:g.32911300_32911303del_offset:+2bp_MERGED\tGT:GQ:SDP:DP:RD:AD:FREQ:PVAL:RBQ:ABQ:RDF:RDR:ADF:ADR\t0/1:51:1505:1504:1320:861:11.45%:1.7363E-52:39:40:1320:0:171:0";
    private final String mini_tumour = "1\t43815102\t.\tG\tT\t8427.41\t.\tAB=0.736\tGT:AD:DP:GQ:PL\t0/1:802,287:1096:99:8427,0,29208";
    private final String vcfVariant = "123";//mini_tumour;

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Before
    public void setup() throws Exception
    {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }


    @Test
    public void help() throws Exception
    {
        final String urlTemplate = "/v1/annotate/help";
        final ResultActions resultActions = mockMvc.perform(MockMvcRequestBuilders.get(urlTemplate));
        log.info("URL={} JSON={}", urlTemplate, resultActions.andReturn().getResponse().getContentAsString());

        resultActions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$..banner", not(Matchers.isEmptyOrNullString())));
    }

    @Test
    public void schemas() throws Exception
    {
        final String urlTemplate = "/v1/annotate/schemas";
        final ResultActions resultActions = mockMvc.perform(MockMvcRequestBuilders.get(urlTemplate));
        log.info("URL={} JSON={}", urlTemplate, resultActions.andReturn().getResponse().getContentAsString());

        resultActions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.schemas", Matchers.hasItems(is("derived.json"))));
    }

    @Test
    public void schema() throws Exception
    {
        final String urlTemplate = "/v1/annotate/schema/derived.json";
        final ResultActions resultActions = mockMvc.perform(MockMvcRequestBuilders.get(urlTemplate));
        log.info("URL={} JSON={}", urlTemplate, resultActions.andReturn().getResponse().getContentAsString());

        resultActions
                .andExpect(status().isOk());
//                .andExpect(content().contentType(contentType))
//                .andExpect(jsonPath("$", startsWith("{")));
    }

    @Test
    public void rename() throws Exception
    {
        final String urlTemplate = "/v1/annotate/refseq/hg19/" + variant;
        final ResultActions resultActions = mockMvc.perform(MockMvcRequestBuilders.get(urlTemplate));
        log.info("URL={} JSON={}", urlTemplate, resultActions.andReturn().getResponse().getContentAsString());

        resultActions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.refseq", is("NC_000010.10:g.89693009G>A")));
    }

    @Test
    public void testParamsGet() throws Exception
    {
///v1/annotate?variant=chr2:g.47702181C>T;build=GRCh37;sources=MyVariant,Vep,Mutalyzer
        final String urlTemplate = "/v1/annotate?variant=" + variant + "&build=GRCh37&sources=Mutalyzer+,Vep+";
        ResultActions resultActions = mockMvc.perform(MockMvcRequestBuilders.get(urlTemplate));
        log.info("URL={} JSON={}", urlTemplate, resultActions.andReturn().getResponse().getContentAsString());

        log.info("getErrorMessage={}", resultActions.andReturn().getResponse().getErrorMessage());
        resultActions
                .andExpect(status().isOk())
//                .andExpect(content().contentType(contentType))
                .andExpect(jsonPath("$.variant", is(variant)))
                .andExpect(jsonPath("$.annotationSources", Matchers.hasItems(is(AnnotationSource.Mutalyzer.name()), is(AnnotationSource.Vep38.name()))))
                .andExpect(jsonPath("$.build", is(GenomeBuild.GRCh37.name())));
    }

    @Test
    public void defaultsGet() throws Exception
    {

        final String urlTemplate = "/v1/annotate/variant/" + variant + ";build=GRCh37;schema=pathos_schema"; //Only this schema will work with 1.1.1-SNAPSHOT and above
        final ResultActions resultActions = mockMvc.perform(MockMvcRequestBuilders.get(urlTemplate));
        log.info("URL={} JSON={}", urlTemplate, resultActions.andReturn().getResponse().getContentAsString());

        resultActions //DEFAULT ARGS
                .andExpect(status().isOk())
//                .andExpect(content().contentType(contentType))
                .andExpect(jsonPath("$.variants", Matchers.hasItems(is(variant))))
                .andExpect(jsonPath("$.annotationSources",
                        Matchers.hasItems(
                                is(AnnotationSource.ClinGenAR.name()),
                                is(AnnotationSource.Vep.name()),
                                is(AnnotationSource.MyVariant.name())
                        )))
                .andExpect(jsonPath("$.build", is(GenomeBuild.GRCh37.name())));
    }

    @Test
    public void pathGet() throws Exception
    {
        final String urlTemplate = "/v1/annotate/variant/" + variant + ";build=GRCh37;schema=prefTxSchema";
        final ResultActions resultActions = mockMvc.perform(MockMvcRequestBuilders.get(urlTemplate));


        log.info("URL={} JSON={}", urlTemplate, resultActions.andReturn().getResponse().getContentAsString());
        resultActions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.variants[0]", is(variant)))
//                .andExpect(jsonPath("$.okResults.chr10:g.89693009G>A.variant", is(variant)))
                .andExpect(jsonPath("$.build", is(GenomeBuild.GRCh37.name())));
    }

    @Test
    public void myVariantGet() throws Exception
    {
        final String urlTemplate = "/v1/annotate/variant/" + variant + ";build=GRCh37;sources=MyVariant,Mutalyzer";
        final ResultActions resultActions = mockMvc.perform(MockMvcRequestBuilders.get(urlTemplate));


        final String json = resultActions.andReturn().getResponse().getContentAsString();
        log.info("URL={} JSON={}", urlTemplate, json);
        resultActions
                .andExpect(status().isOk())
//                .andExpect(content().contentType(contentType))
                .andExpect(jsonPath("$.variant", is(variant)))
                .andExpect(jsonPath("$.myVariant", not(isEmptyOrNullString())))
                .andExpect(jsonPath("$.generic", not(isEmptyOrNullString())))
                .andExpect(jsonPath("$.annotationSources", Matchers.hasItems(is(AnnotationSource.Mutalyzer.name()), is(AnnotationSource.MyVariant.name()))))
                .andExpect(jsonPath("$.build", is(GenomeBuild.GRCh37.name())));


        Object pos = JsonPath.compile("$.myVariant.vcf.position").read(json);
        System.err.println("$.myVariant.vcf.position =  " + pos);

        Object gene = JsonPath.compile("$.myVariant.dbsnp.gene.symbol").read(json);
        System.err.println("$.myVariant.dbsnp.gene.symbol =  " + gene);
        assertEquals("47702181", pos); //STRING !!
        assertEquals(47702181, (int) JsonPath.compile("$.myVariant.dbsnp.hg19.start").read(json)); //INT!!
        assertEquals("MSH2", gene);

    }

    @Test
    public void vepGet() throws Exception
    {
        final String urlTemplate = "/v1/annotate/variant/" + variant + ";build=GRCh37;sources=Vep;schema=derived";
        final ResultActions resultActions = mockMvc.perform(MockMvcRequestBuilders.get(urlTemplate));

        final String json = resultActions.andReturn().getResponse().getContentAsString();
        log.info("URL={} JSON={}", urlTemplate, json);
        resultActions
                .andExpect(status().isOk())
//                .andExpect(content().contentType(contentType))
                .andExpect(jsonPath("$.annotationSources", Matchers.hasItems(AnnotationSource.Vep.toString())))
                .andExpect(jsonPath("$.build", is(GenomeBuild.GRCh37.name())))
                .andExpect(jsonPath("$.results", Matchers.hasSize(1)))
                .andExpect(jsonPath("$.results[0].variant", Matchers.hasToString(variant)))
                .andExpect(jsonPath("$..vep", Matchers.hasItems(isEmptyOrNullString())))
                .andExpect(jsonPath("$.results[0].vep", isEmptyOrNullString()))
                .andExpect(jsonPath("$.results[0].domainModel.derived", not(isEmptyOrNullString())))
                .andExpect(jsonPath("$.results[0].domainModel.derived.existing_variation", Matchers.hasItems("CM041805", "rs63750200")))
        ;
    }

    @Test
    public void vepGetAndReturn() throws Exception
    {
        final String urlTemplate = "/v1/annotate/variant/" + variant + ";build=GRCh37;sources=Vep+;schema=derived";
        final ResultActions resultActions = mockMvc.perform(MockMvcRequestBuilders.get(urlTemplate));

        final String json = resultActions.andReturn().getResponse().getContentAsString();
        log.info("URL={} JSON={}", urlTemplate, json);
        resultActions
                .andExpect(status().isOk())
//                .andExpect(content().contentType(contentType))
                .andExpect(jsonPath("$.annotationSources", Matchers.hasItems(AnnotationSource.Vep.toString())))
                .andExpect(jsonPath("$.build", is(GenomeBuild.GRCh37.name())))
                .andExpect(jsonPath("$.okResults", Matchers.hasSize(1)))
                .andExpect(jsonPath("$.okResults[0].variant", Matchers.hasToString(variant)))
                .andExpect(jsonPath("$..vep", not(isEmptyOrNullString())))
                .andExpect(jsonPath("$.okResults[0].domainModel.derived", not(isEmptyOrNullString())))
                .andExpect(jsonPath("$.okResults[0].domainModel.derived.existing_variation", Matchers.hasItems("CM041805", "rs63750200")))
        ;
    }

    /*
        @Test
        public void vepVCFPost() throws Exception
        {
            final String urlTemplate = "/v1/annotate/vcf?build=GRCh38&sources=Vep&schema=derived";
            final MockHttpServletRequestBuilder post = MockMvcRequestBuilders.post(urlTemplate);
            final String content = JSON.format(new DomainModel());
            post.content(content);
            post.contentType(MediaType.APPLICATION_JSON_UTF8);
            log.info("{} Posting content={}", post, content);
            final ResultActions resultActions = mockMvc.perform(post);


            final String json = resultActions.andReturn().getResponse().getContentAsString();
            log.info("URL={} JSON={}", urlTemplate, json);
            resultActions
                    .andExpect(status().isOk())
    //                .andExpect(content().contentType(contentType))
                    .andExpect(jsonPath("$.variant", is(variant)))
                    .andExpect(jsonPath("$.vep", not(isEmptyOrNullString())))
                    .andExpect(jsonPath("$.annotationSources", Matchers.hasItems(is(AnnotationSource.Vep.name()))))
                    .andExpect(jsonPath("$.domainModel.derived", not(isEmptyOrNullString())))
                    .andExpect(jsonPath("$.domainModel.derived.existing_variation", Matchers.hasItems(is("CM041805"), is("rs63750200"))))
                    .andExpect(jsonPath("$.build", is(GenomeBuild.GRCh37.name())));
        }
    */
    @Test
    public void vepVCFPostFileStream() throws Exception
    {
        final String urlTemplate = "/v1/annotate/vcf/file?build=GRCh37&sources=Vep&schema=derived";
        final MockHttpServletRequestBuilder post = MockMvcRequestBuilders.post(urlTemplate);
        final String content;

        final String name = "/mini_tumour.vcf";
        final URL resource = DomainModel.class.getResource(name);
        if (resource == null)
            throw new FileNotFoundException("No such VCF file " + name);
        try
        {
            content = new String(Files.readAllBytes(Paths.get(resource.toURI())));
        } catch (URISyntaxException e)
        {
            throw new IOException(e);
        }

        post.content(content);
        post.contentType(MediaType.TEXT_PLAIN);
        log.info("{} Posting content={}", post, content);
        final ResultActions resultActions = mockMvc.perform(post);


        final String json = resultActions.andReturn().getResponse().getContentAsString();
        log.info("URL={} JSON={}", urlTemplate, json);
        resultActions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.annotationSources", Matchers.hasItems(AnnotationSource.Vep.toString())))
                .andExpect(jsonPath("$.build", is(GenomeBuild.GRCh37.name())))
                .andExpect(jsonPath("$.results", Matchers.hasSize(4)))
                .andExpect(jsonPath("$..vep", not(isEmptyOrNullString())))
                .andExpect(jsonPath("$..domainModel.derived", not(isEmptyOrNullString())))
                .andExpect(jsonPath("$..domainModel.derived", Matchers.hasSize(4)))
        ;
    }

    @Autowired
    BuildInfo buildInfo;

    @Test
    public void getBuildInfo() throws Exception
    {
        System.err.println(buildInfo.getBuildInfo());
    }

}