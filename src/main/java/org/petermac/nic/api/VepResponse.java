package org.petermac.nic.api;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * http://grch37.rest.ensembl.org/vep/human/hgvs/chr2:g.47702181C%3ET?refseq=1;content-type=application/json
 * <p>
 * input: "chr2:g.47702181C>T",
 * + colocated_variants: [],
 * assembly_name: "GRCh37",
 * // end: 47702181,
 * seq_region_name: "2",
 * + transcript_consequences: [],
 * strand: 1,
 * id: "chr2:g.47702181C>T",
 * most_severe_consequence: "stop_gained",
 * allele_string: "C/T",
 * // start: 47702181
 * <p>
 * Created by Nick Kravchenko on 25/06/2018.
 */

public class VepResponse extends ArrayList<VepResponse.VepAnnotation>// implements RestResponse
{
//: "regulatory_feature_consequences", "start", "assembly_name", "colocated_variant_ids", "strand", "colocated_variants", "input", "allele_string", "most_severe_consequence", "seq_region_name", "transcript_consequences", "id", "transcript_consequence_ids", "transcript_consequence_Genes", "end"]
    public static class VepAnnotation
    {
        public String input;
        public String id;
        public String assembly_name;
        public String seq_region_name;
        public String strand;
        public String most_severe_consequence;
        public String allele_string;
        public int start;//Hg19
        public int end;
        public List<Map> colocated_variants; //id
        public List<Map> transcript_consequences;//transcript_id
        public Object regulatory_feature_consequences;
        public Object motif_feature_consequences;

        public VepAnnotation()
        {
        }

        public List<String> getFlatList(Collection<Map> array, String id)
        {
            final List<String> ids = new ArrayList<>();
            if (array != null)
                for (Map map : array)
                    ids.add(String.valueOf(map.get(id)));
            return ids;
        }

        public List<String> getColocated_variant_ids()
        {
            return getFlatList(colocated_variants, "id");
        }

        public List<String> getTranscript_consequence_ids()
        {
            return getFlatList(transcript_consequences, "transcript_id");
        }
        public List<String> getTranscript_consequence_Genes()
        {
            return getFlatList(transcript_consequences, "gene_symbol");
        }
    }


    public VepResponse()
    {
        super();
    }


}
