package org.petermac.nic.api;

import junit.framework.TestCase;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.petermac.nic.dataminer.domain.pathos.vcf.core.DerivedSchema;
import org.petermac.nic.dataminer.domain.pathos.vcf.core.GenomeBuild;
import org.petermac.nic.dataminer.domain.pathos.vcf.services.RepoCachingService;
import org.petermac.nic.dataminer.domain.pathos.vcf.sources.AnnotationCacheWrapper;
import org.petermac.nic.dataminer.domain.pathos.vcf.sources.AnnotationSource;
import org.petermac.nic.dataminer.domain.pathos.vcf.sources.AnnotationSourceDelegate;
import org.petermac.nic.dataminer.domain.pathos.vcf.sources.AnnotationURLProvider;
import org.petermac.nic.dataminer.parse.JSON;
import org.petermac.nic.dataminer.parse.JSONObject;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;


/**
 * Created by Nic on 25/06/2019.
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = {TestConfig.class, RepoCachingService.class, org.petermac.nic.dataminer.Application.class})
public class DynamicDomainModelTest
{
    private static Logger log = org.slf4j.LoggerFactory.getLogger(DynamicDomainModelTest.class);
    @Autowired
    private RepoCachingService cacheService;

    // Domain Model is only exposed to this.
//    public Map<String, Object> sourceResults; //keyed by Anno source common name.

    @Test
    public void writePrefTxModelJson() throws Exception
    {
//        final String resourceAsString = Resources.getResourceAsString(getClass(), "/derivedTest.json");//note: sometimes IntelliJ does not find this under test resources.(Rebuild project to fix)
     /*   final String schemaString = "{\n" +
                "  \"schemaName\": \"derivedTestSchema\",\n" +
                "  \"defaultValue\": \"\",\n" +
                "  \"aliases\": {\n" +
                "    \"vep\": \"Vep\",\n" +
                "    \"clingen\": \"ClinGenAR\"\n" +
                "  },\n" +
                "  \"schema\": {\n" +
                "    \"clingen\" : \"clingen.*\",\n" +
                "    \"transcripts\": \"ClinGenAR.transcriptAlleles[*].set\",\n" +
                "    \"clinGenPreferred\":  \"PrefTx.preferred\",\n" +
                "    \"clinGenPreferredLRG\":  \"PrefTx.lrg\"\n" +
                "  }\n" +
                "}";
        log.debug("schemaString =  {}", schemaString);
*/
        /*
         *{
         "schemaName": "derivedTestSchema",
         "defaultValue": "",
         "aliases": {
         "vep": "Vep",
         "clingen": "ClinGenAR"
         },
         "schema": {
         "clingen" : "clingen.*",
         "transcripts": "ClinGenAR.transcriptAlleles[*].set",
         "clinGenPreferred":  "PrefTx.preferred",
         "clinGenPreferredLRG":  "PrefTx.lrg"
         }
         }
         */
        final Map<String, String> aliases = new HashMap<>();
        aliases.put("vep", "Vep");
        aliases.put("clingen", "ClinGenAR");
        aliases.put("hgvsGVariant", "Anomaly.hgvsGVariant");//<<<<<<<<<<<<< important
        aliases.put("transcripts", "ClinGenAR.transcriptAlleles[*].hgvs");//<<<<<<<<<<<<< important for PrefTx to work

        final Map<String, Object> schema = new HashMap<>();
        schema.put("clinGenPreferredTx", "PrefTx.preferred"); // needs "{transcripts}"
        schema.put("clinGenPreferredLRGTx", "PrefTx.lrg");

        schema.put("clingenTxs", "clingen.transcriptAlleles[*].hgvs");
        schema.put("clingenTxsSet", "{clingen.transcriptAlleles[*].hgvs}");
        schema.put("clingenGenes", "CSV={clingen.transcriptAlleles[*].geneSymbol}");
//        schema.put("clingen", "clingen.*");
//        schema.put("transcripts", "ClinGenAR.transcriptAlleles[*].hgvs.set()");
        schema.put("transcriptsArgumentInput", "{transcripts}");


        final DerivedSchema derivedSchema = new DerivedSchema("sources", "testPrefTx", "", aliases, schema);

        final Map<String, AnnotationURLProvider> providerMap =
//                AnnotationSource.getConcreteStream().collect(Collectors.toMap(AnnotationURLProvider::getName, Function.identity()));
                AnnotationSource.getConcreteStream().map(urlProvider -> new AnnotationCacheWrapper(urlProvider, cacheService)).collect(Collectors.toMap(AnnotationURLProvider::getName, Function.identity()));

        //Not caching !!!
        providerMap.put("PrefTx",
                new AnnotationSourceDelegate("PrefTx", "http://dev-api-anomaly.ap-southeast-2.elasticbeanstalk.com",
                        "v1/annotate/preferredTranscript?transcripts={transcripts}", false, GenomeBuild.GRCh38, GenomeBuild.GRCh37));
        log.info("providerMap = {} ", providerMap);

        final String variant = "chr9:g.97912307T>A";
//        final String variant = "chr2:g.47702181C>T";


        final JSONObject jsonObject = new DynamicJsonObject(variant);

        log.debug(jsonObject.formatAsJson());

        final DynamicDomainModel dm = new DynamicDomainModel(derivedSchema, providerMap);

        dm.writeModel(variant, jsonObject);// << write Model will inject Sources as we go

        log.debug(JSON.format(dm));

        TestCase.assertEquals("NM_000136.2:c.584A>T", dm.derived.get("clinGenPreferredTx"));
    }


    private class DynamicJsonObject implements DynamicDomainModel.SourceInjection
    {
        //Delegates to this object.
        private Map<String, Object> _sourceResults; //keyed by Anno source common name.

        public DynamicJsonObject(final String variant)
        {
            _sourceResults = new HashMap<>();
            Response.Anomaly anomaly = new Response.Anomaly(variant); //Built-In data source

            anomaly.build = GenomeBuild.GRCh37;
            anomaly.assembly = anomaly.build.getAssembly();
            anomaly.vcf = null;

            anomaly.hgvsGVariant = anomaly.build.renameToRefSeq(variant);//NC_
            anomaly.refSeq = anomaly.build.renameToRefSeq(variant); //Must be proper!
            Objects.requireNonNull(anomaly.refSeq, "hgvs reference sequence is invalid: " + variant);
            log.debug("anomaly = {}", anomaly);
            _sourceResults.put(AnnotationSource.Anomaly.getCommonSource().getName(), anomaly); //Anomaly BuiltIn
        }

        @Override
        public String formatAsJson()
        {
            return JSON.format(_sourceResults);
        }

        public void inject(String key, Object value)
        {
            _sourceResults.put(key, value);
        }

        public boolean hasSrc(String src)
        {
            return _sourceResults.containsKey(src);
        }
    }


}