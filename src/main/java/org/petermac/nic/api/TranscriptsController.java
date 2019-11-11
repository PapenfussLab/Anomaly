package org.petermac.nic.api;

import org.petermac.nic.dataminer.domain.pathos.vcf.dao.TranscriptDao;
import org.petermac.nic.dataminer.parse.JSON;
import org.slf4j.Logger;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

/**
 * REST Service For Anomaly. Version 1.0
 * Created by Nic on 25/05/2018.
 */
@RestController
@RequestMapping("/v1/transcripts")
public class TranscriptsController
{
    private static final Logger log = org.slf4j.LoggerFactory.getLogger(TranscriptsController.class);

    /**
     * Choose to apply security constraints here.
     *
     * @return security constraints as Any patterns
     */
    public static String SecurityIgnoringAntMatchers()
    {
        return "/v1/transcripts/**";
    }

    /**
     * Adds a Preferred Transcript
     *
     * @param transcript
     * @return
     */
    @PostMapping("")
    ResponseEntity<TranscriptDao> add(@RequestBody TranscriptDao transcript)
    {
        log.info("Adding Transcript ={}", transcript.format());
        log.info("Adding Transcript \n{}", JSON.format(transcript));
        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .buildAndExpand().toUri();

        return ResponseEntity.created(location).build();
//        return ResponseEntity.noContent().build();
    }

    @GetMapping("")
    public List<TranscriptDao> read()
    {
        List<TranscriptDao> list = new ArrayList<>();
        list.add(new TranscriptDao()
        {
            @Override
            public String getRefseq()
            {
                return "chr2:g.47702181C>T";
            }
        });
        list.add(new TranscriptDao()
        {
            @Override
            public String getRefseq()
            {
                return "chrX:g.47702181C>T";
            }
        });
        return list;
    }
}
