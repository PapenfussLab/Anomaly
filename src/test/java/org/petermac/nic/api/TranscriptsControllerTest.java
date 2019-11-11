package org.petermac.nic.api;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.petermac.nic.dataminer.domain.pathos.vcf.dao.TranscriptDao;
import org.petermac.nic.dataminer.parse.JSON;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.context.WebApplicationContext;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

/**
 */
@RunWith(SpringRunner.class)
//@SpringBootTest(classes = TestConfig.class)
@SpringBootTest(classes = {TestConfig.class, org.petermac.nic.dataminer.Application.class})
@WebAppConfiguration
public class TranscriptsControllerTest
{
    private static final Logger log = org.slf4j.LoggerFactory.getLogger(TranscriptsControllerTest.class);

    private MockMvc mockMvc;

    private String variant = TestVariants.variant;

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Before
    public void setup() throws Exception
    {
        this.mockMvc = webAppContextSetup(webApplicationContext).build();
    }


    @Test
    public void readTranscripts() throws Exception
    {
        mockMvc.perform(get("/api/transcripts?page=0&size=2"))
                .andExpect(status().isOk())
//                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$._embedded.transcripts", hasSize(2)))
                .andExpect(jsonPath("$._embedded.transcripts[0].refseq", is("NM_015113.3")))
                .andExpect(jsonPath("$._embedded.transcripts[1].refseq", is("NM_015534.4")));
    }

    @Test
    public void findRefseq() throws Exception
    {
        mockMvc.perform(get("/api/transcripts/search/findByRefseqAndBuildAndPreferred?refseq=NM_015113.3&build=hg38&preferred=true"))
                .andExpect(status().isOk())
//                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$._embedded.transcripts", hasSize(1)))
                .andExpect(jsonPath("$._embedded.transcripts[0].refseq", is("NM_015113.3")));
    }

    @Test
    public void findAdded() throws Exception
    {
        mockMvc.perform(get("/api/transcripts/search/findByRefseqAndBuildAndPreferred?refseq=nick&build=hg38&preferred=true"))
                .andExpect(status().isOk())
//                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$._embedded.transcripts", hasSize(1)))
                .andExpect(jsonPath("$._embedded.transcripts[0].refseq", is("nick")));
    }

    @Test
    public void findNotFound() throws Exception
    {
        mockMvc.perform(get("/api/transcripts/search/findByRefseqAndBuildAndPreferred?refseq=nick&build=hg38&preferred=true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.transcripts", hasSize(0)))
        ;
    }

    final static String id = "5b0e4da852aa163160ec1015";

    @Test
    public void deleteAdded() throws Exception
    {
        mockMvc.perform(get("/api/transcripts/search/deleteByRefseq?refseq=nick"))
                .andExpect(status().isNotFound())
//                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        ;
    }

    @Test
    public void deleteNotFound() throws Exception
    {
        mockMvc.perform(get("/api/transcripts/search/deleteByRefseq?refseq=nick"))
                .andExpect(status().isNotFound())
//                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        ;
    }

    @Test
    public void createTranscript() throws Exception
    {
        final TranscriptDao transcriptDao = new TranscriptDao()
        {
            @Override
            public String getRefseq()
            {
                return "nick";
            }

            @Override
            public String getBuild()
            {
                return "hg38";
            }

            @Override
            public boolean isPreferred()
            {
                return true;
            }
        };
        String tr = JSON.format(transcriptDao);

        log.info("[POST] >> JSON Transcript = \n{}", tr);
        this.mockMvc.perform(post("/api/transcripts")
//        this.mockMvc.perform(post("/api/transcripts")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .content(tr))
                .andExpect(status().isCreated())
//                .andExpect(jsonPath("$._embedded.transcripts", hasSize(1)))
        ;
    }

    @Test
    public void CRUDTranscripts() throws Exception
    {
        findNotFound();

        createTranscript();
        findAdded();
        deleteAdded();
        findNotFound();
        deleteNotFound();


    }
}