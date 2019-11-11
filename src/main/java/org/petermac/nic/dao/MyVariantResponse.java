package org.petermac.nic.dao;

import org.petermac.nic.api.RestResponse;

import java.util.Map;

/**
 * Created by Nick Kravchenko on 25/06/2018.
 */
class MyVariantResponse implements RestResponse
{
    public String error; //only set when error
    public boolean success = true; //only set when error
    public String _id;
    public int _version;
    public boolean observed;
    public Object chrom;
    public Pos hg19 = new Pos();
    public Pos hg38 = new Pos();//hg38
    public Map clinvar;
    public Map dbnsfp;
    public Map dbsnp;
    public Map evs;//hg38
    public Map gnomad_genome;//hg38
    public Map cadd;
    public Map snpeff;
    public Map uniprot;//hg38
    public Vcf vcf;


    public MyVariantResponse()
    {
    }

    /**
     * "hg19" : {
     * "end" : 47702181,
     * "start" : 47702181
     * }
     */
    public static class Pos
    {
        public int start, end;
    }


    public static class Dbsnp
    {
        public Dbsnp.Gene gene;
        public Pos hg19;
        public Pos hg38;

        public static class Gene
        {
            public String geneid;
            public String symbol;
        }
    }

    public static class Vcf
    {
        public String alt;
        public String position;
        public String ref;
    }
}
