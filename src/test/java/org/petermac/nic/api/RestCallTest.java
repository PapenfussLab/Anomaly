package org.petermac.nic.api;

/**
 * Created by Nic on 16/11/2018.
 */
public class RestCallTest
{


}
/*
{
build: "GRCh37",
assembly: {
name: "GRCh37",
commonName: "hg19"
},
variants: [
"chr2:g.47702181C>T"
],
executionException: null,
orthogonalMap: null,
exceptionTypes: [ ],
startTime: "never",
numberOfResults: 1,
annotatorLog: [
"Starting Annotator Position Converter [Mutalyzer] ...",
"https://mutalyzer.nl/batch-job-result/batch-job-9fc25fd9-aa94-4e80-994f-42eec0a3fae3.txt",
"HgvsG: set to NC_000002.11:g.47702181C>T Transcripts: changed [] -> [LRG_218t1:c.1777C>T, NM_000251.2:c.1777C>T, NM_001258281.1:c.1579C>T, XM_005264333.1:c.1627C>T, XM_005264332.1:c.1777C>T, NM_000251.1:c.1777C>T] ",
"Completed Annotator Position Converter [Mutalyzer]",
"Starting Annotator Transcript Filter ...",
"FilterTs: set to NM_000251.2:c.1777C>T RefSeq: set to NM_000251.2:c.1777C>T ",
"Completed Annotator Transcript Filter",
"Starting Annotator Name Checker [Mutalyzer] ...",
"https://mutalyzer.nl/batch-job-result/batch-job-23811646-7cc6-4ad6-9d85-63fa4a3889f3.txt",
"Renamed: changed false -> true HgvsG: changed NC_000002.11:g.47702181C>T -> chr2:g.47702181C>T HgvsC: set to NM_000251.2:c.1777C>T Gene: set to MSH2_v001 HgvsP: set to NP_000242.1:p.(Gln593*) NameChecked: set to org.petermac.nic.dataminer.domain.pathos.vcf.annotation.NameChecked@905f860 ",
"Completed Annotator Name Checker [Mutalyzer]",
"Starting Annotator Three Shifter [Mutalyzer] ...",
"https://mutalyzer.nl/batch-job-result/batch-job-e4222ddb-af8f-4cc3-944b-4050b858bc2e.txt",
"",
"Completed Annotator Three Shifter [Mutalyzer]"
],
elapsedTime: "not started",
ok: 1,
failed: 0,
okResults: {
chr2:g.47702181C>T: {
variant: "chr2:g.47702181C>T",
hgvsGVariant: "NC_000002.11:g.47702181C>T",
build: "GRCh37",
assembly: {
name: "GRCh37",
commonName: "hg19"
},
errors: { },
vcf: null,
sourceResults: {
Mutalyzer: {
transcripts: [
"LRG_218t1:c.1777C>T",
"NM_000251.2:c.1777C>T",
"NM_001258281.1:c.1579C>T",
"XM_005264333.1:c.1627C>T",
"XM_005264332.1:c.1777C>T",
"NM_000251.1:c.1777C>T"
],
variant: "chr2:g.47702181C>T",
assembly: "GRCh37",
refSeq: "NM_000251.2:c.1777C>T",
error: "",
hgvsC: "NM_000251.2:c.1777C>T",
hgvsG: "chr2:g.47702181C>T",
hgvsP: "NP_000242.1:p.(Gln593*)",
bestTranscript: "NM_000251.2:c.1777C>T",
filterTs: "NM_000251.2:c.1777C>T",
gene: "MSH2_v001",
lrg: null,
named: {
proteinRef: "NP_000242.1",
restriction_sites_created: "AccI,Hpy166II",
restriction_sites_deleted: "HpyCH4V",
affectedTranscripts: "NM_000251.2(MSH2_v001):c.1777C>T",
affectedProteins: "NM_000251.2(MSH2_i001):p.(Gln593*)"
}
},
ClinGenAR: {},
MyVariant: {},
Vep: {
error: null,
warning: null,
vepResponse: [
{
input: "chr2:g.47702181C>T",
id: "chr2:g.47702181C>T",
assembly_name: "GRCh37",
seq_region_name: "2",
strand: "1",
most_severe_consequence: "stop_gained",
allele_string: "C/T",
start: 47702181,
end: 47702181,
colocated_variants: [
{
seq_region_name: "2",
strand: "1",
allele_string: "HGMD_MUTATION",
start: "47702181",
id: "CM041805",
end: "47702181",
phenotype_or_disease: "1"
},
{
id: "rs63750200",
clin_sig: [
"uncertain_significance",
"pathogenic"
],
end: "47702181",
phenotype_or_disease: "1",
seq_region_name: "2",
start: "47702181",
strand: "1",
allele_string: "C/T"
}
],
transcript_consequences: [
{
consequence_terms: [
"stop_gained"
],
amino_acids: "Q/*",
cdna_start: 1902,
impact: "HIGH",
bam_edit: "OK",
given_ref: "C",
protein_end: 593,
biotype: "protein_coding",
cdna_end: 1902,
cds_end: 1777,
transcript_id: "NM_000251.2",
gene_id: 4436,
strand: 1,
cds_start: 1777,
variant_allele: "T",
gene_symbol: "MSH2",
codons: "Cag/Tag",
used_ref: "C",
protein_start: 593,
gene_symbol_source: "EntrezGene"
},
{
protein_start: 527,
gene_symbol_source: "EntrezGene",
used_ref: "C",
codons: "Cag/Tag",
gene_symbol: "MSH2",
variant_allele: "T",
cds_start: 1579,
strand: 1,
gene_id: 4436,
transcript_id: "NM_001258281.1",
cds_end: 1579,
protein_end: 527,
cdna_end: 1718,
biotype: "protein_coding",
given_ref: "C",
bam_edit: "OK",
impact: "HIGH",
amino_acids: "Q/*",
cdna_start: 1718,
consequence_terms: [
"stop_gained"
]
},
{
cds_start: 1777,
variant_allele: "T",
codons: "Cag/Tag",
gene_symbol: "MSH2",
used_ref: "C",
gene_symbol_source: "EntrezGene",
protein_start: 593,
consequence_terms: [
"stop_gained"
],
impact: "HIGH",
cdna_start: 1849,
amino_acids: "Q/*",
given_ref: "C",
cds_end: 1777,
cdna_end: 1849,
protein_end: 593,
biotype: "protein_coding",
transcript_id: "XM_005264332.1",
gene_id: 4436,
strand: 1
},
{
given_ref: "C",
cdna_end: 1699,
biotype: "protein_coding",
protein_end: 543,
cds_end: 1627,
consequence_terms: [
"stop_gained"
],
amino_acids: "Q/*",
cdna_start: 1699,
impact: "HIGH",
strand: 1,
transcript_id: "XM_005264333.1",
gene_id: 4436,
variant_allele: "T",
cds_start: 1627,
used_ref: "C",
gene_symbol_source: "EntrezGene",
protein_start: 543,
codons: "Cag/Tag",
gene_symbol: "MSH2"
}
],
colocated_variant_ids: [
"CM041805",
"rs63750200"
],
transcript_consequence_ids: [
"NM_000251.2",
"NM_001258281.1",
"XM_005264332.1",
"XM_005264333.1"
],
transcript_consequence_Genes: [
"MSH2",
"MSH2",
"MSH2",
"MSH2"
]
}
],
bestTranscripts: [
"NM_000251.2:c.1777C>T"
],
besttx: {
consequence_terms: [
"stop_gained"
],
amino_acids: "Q/*",
cdna_start: 1902,
impact: "HIGH",
bam_edit: "OK",
given_ref: "C",
protein_end: 593,
biotype: "protein_coding",
cdna_end: 1902,
cds_end: 1777,
transcript_id: "NM_000251.2",
gene_id: 4436,
strand: 1,
cds_start: 1777,
variant_allele: "T",
gene_symbol: "MSH2",
codons: "Cag/Tag",
used_ref: "C",
protein_start: 593,
gene_symbol_source: "EntrezGene"
},
bestLrgs: [
null
]
},
VICC: {
hits: {
hits: [ ],
total: 0
}
},
ClinGenER: {
variantInterpretations: [ ],
@context: "https://ereg.genome.network/pcer/api/context/light"
}
},
domainModel: {
sources: "vep, mutalyzer, myvariant",
schemaName: "derived",
defaultValue: null,
schema: {
sampleName: "vcf.sample",
variant: "mutalyzer.hgvsG",
gene: "vep.besttx.gene_symbol",
consequence: "vep.besttx.consequence_terms",
hgvsc: "mutalyzer.hgvsC",
hgvsg: "mutalyzer.hgvsG",
hgvsp: "mutalyzer.hgvsP",
hgvspAa1: "mutalyzer.hgvsP",
vepHgvsc: "vep.besttx.hgvsc",
vepHgvsp: "vep.besttx.hgvsp",
readDepth: "vcf.dp",
varDepth: "vcf.ad",
varFreq: "vcf.vaf",
fwdReadDepth: "vcf.rdf",
fwdVarDepth: "vcf.adf",
revReadDepth: "vcf.rdr",
revVarDepth: "vcf.adr",
chr: "vcf.chromosome",
pos: "vcf.position",
exon: "ex{vep.besttx.exon}",
allelesWithMutTest: "MUT={vep.vepResponse.*.colocated_variants[?(@.allele_string =~ /(.*)_MUTATION/i)].allele_string}",
allelesWithMutTestOK: "vep.vepResponse.*.colocated_variants[?(@.allele_string =~ /(.*)_MUTATION/i)].allele_string",
cosmic: "vep.vepResponse.*.colocated_variants[?(@.id =~ /COSM(\d+)/i)].id",
dbsnp: "vep.vepResponse.*.colocated_variants[?(@.id =~ /rs(\d+)/i)].id",
ens_transcript: "vep.besttx.transcript_id",
ens_gene: "vep.besttx.gene_symbol",
ens_protein: "vep.besttx.hgvsp",
existing_variation: "vep.vepResponse.*.colocated_variants.*.id",
clin_sig: "vep.vepResponse.*.colocated_variants.*.clin_sig",
pubmed: "vep.vepResponse.*.colocated_variants.*.pubmed",
cadd: "vep.besttx.cadd_raw",
cadd_phred: "vep.besttx.cadd_phred",
exac: "vep.vepResponse.*.colocated_variants.*.gnomad_maf",
gmaf: "vep.vepResponse.*.colocated_variants.*.minor_allele_freq",
esp: "vep.vepResponse.*.colocated_variants.*.ea_maf",
metaLrVal: "vep.besttx.metalr_pred",
siftVal: "vep.besttx.sift_pred",
lrtVal: "vep.besttx.lrt_pred",
mutTasteVal: "vep.besttx.mutationtaster_pred",
mutAssessVal: "vep.besttx.mutationassessor_pred",
fathmmVal: "vep.besttx.fathmm_pred",
metaSvmVal: "vep.besttx.metasvm_pred",
polyphenVal: "vep.besttx.polyphen2_hvar_pred",
mutStatus: "mutalyzer.status",
mutError: "mutalyzer.error",
numamps: "vcf.numAmps",
amps: "vcf.amps",
ampbias: "vcf.ampbias",
homopolymer: "vcf.homopolymer",
varcaller: "vcf.Identified"
},
derived: {
hgvsg: null,
metaLrVal: null,
fwdReadDepth: null,
amps: null,
esp: null,
ens_protein: null,
hgvsc: null,
mutError: null,
pubmed: null,
mutTasteVal: null,
variant: null,
dbsnp: null,
hgvsp: null,
vepHgvsc: null,
varDepth: null,
cosmic: null,
allelesWithMutTest: null,
gene: null,
ens_gene: null,
revReadDepth: null,
revVarDepth: null,
chr: null,
varcaller: null,
siftVal: null,
varFreq: null,
vepHgvsp: null,
exac: null,
gmaf: null,
fathmmVal: null,
cadd_phred: null,
mutStatus: null,
sampleName: null,
homopolymer: null,
readDepth: null,
polyphenVal: null,
lrtVal: null,
pos: null,
allelesWithMutTestOK: null,
existing_variation: null,
metaSvmVal: null,
consequence: null,
ampbias: null,
cadd: null,
ens_transcript: null,
mutAssessVal: null,
numamps: null,
fwdVarDepth: null,
hgvspAa1: null,
exon: null,
clin_sig: null
},
errors: {
hgvsg: "com.jayway.jsonpath.PathNotFoundException: Missing property in path $['mutalyzer']",
metaLrVal: "com.jayway.jsonpath.PathNotFoundException: Missing property in path $['vep']",
fwdReadDepth: "com.jayway.jsonpath.PathNotFoundException: Expected to find an object with property ['rdf'] in path $['vcf'] but found 'null'. This is not a json object according to the JsonProvider: 'com.jayway.jsonpath.spi.json.JsonSmartJsonProvider'.",
amps: "com.jayway.jsonpath.PathNotFoundException: Expected to find an object with property ['amps'] in path $['vcf'] but found 'null'. This is not a json object according to the JsonProvider: 'com.jayway.jsonpath.spi.json.JsonSmartJsonProvider'.",
esp: "com.jayway.jsonpath.PathNotFoundException: Missing property in path $['vep']",
ens_protein: "com.jayway.jsonpath.PathNotFoundException: Missing property in path $['vep']",
hgvsc: "com.jayway.jsonpath.PathNotFoundException: Missing property in path $['mutalyzer']",
mutError: "com.jayway.jsonpath.PathNotFoundException: Missing property in path $['mutalyzer']",
pubmed: "com.jayway.jsonpath.PathNotFoundException: Missing property in path $['vep']",
mutTasteVal: "com.jayway.jsonpath.PathNotFoundException: Missing property in path $['vep']",
variant: "com.jayway.jsonpath.PathNotFoundException: Missing property in path $['mutalyzer']",
dbsnp: "com.jayway.jsonpath.PathNotFoundException: Missing property in path $['vep']",
hgvsp: "com.jayway.jsonpath.PathNotFoundException: Missing property in path $['mutalyzer']",
vepHgvsc: "com.jayway.jsonpath.PathNotFoundException: Missing property in path $['vep']",
varDepth: "com.jayway.jsonpath.PathNotFoundException: Expected to find an object with property ['ad'] in path $['vcf'] but found 'null'. This is not a json object according to the JsonProvider: 'com.jayway.jsonpath.spi.json.JsonSmartJsonProvider'.",
cosmic: "com.jayway.jsonpath.PathNotFoundException: Missing property in path $['vep']",
allelesWithMutTest: "com.jayway.jsonpath.PathNotFoundException: Missing property in path $['vep']",
gene: "com.jayway.jsonpath.PathNotFoundException: Missing property in path $['vep']",
ens_gene: "com.jayway.jsonpath.PathNotFoundException: Missing property in path $['vep']",
revReadDepth: "com.jayway.jsonpath.PathNotFoundException: Expected to find an object with property ['rdr'] in path $['vcf'] but found 'null'. This is not a json object according to the JsonProvider: 'com.jayway.jsonpath.spi.json.JsonSmartJsonProvider'.",
revVarDepth: "com.jayway.jsonpath.PathNotFoundException: Expected to find an object with property ['adr'] in path $['vcf'] but found 'null'. This is not a json object according to the JsonProvider: 'com.jayway.jsonpath.spi.json.JsonSmartJsonProvider'.",
chr: "com.jayway.jsonpath.PathNotFoundException: Expected to find an object with property ['chromosome'] in path $['vcf'] but found 'null'. This is not a json object according to the JsonProvider: 'com.jayway.jsonpath.spi.json.JsonSmartJsonProvider'.",
varcaller: "com.jayway.jsonpath.PathNotFoundException: Expected to find an object with property ['Identified'] in path $['vcf'] but found 'null'. This is not a json object according to the JsonProvider: 'com.jayway.jsonpath.spi.json.JsonSmartJsonProvider'.",
siftVal: "com.jayway.jsonpath.PathNotFoundException: Missing property in path $['vep']",
varFreq: "com.jayway.jsonpath.PathNotFoundException: Expected to find an object with property ['vaf'] in path $['vcf'] but found 'null'. This is not a json object according to the JsonProvider: 'com.jayway.jsonpath.spi.json.JsonSmartJsonProvider'.",
vepHgvsp: "com.jayway.jsonpath.PathNotFoundException: Missing property in path $['vep']",
exac: "com.jayway.jsonpath.PathNotFoundException: Missing property in path $['vep']",
gmaf: "com.jayway.jsonpath.PathNotFoundException: Missing property in path $['vep']",
fathmmVal: "com.jayway.jsonpath.PathNotFoundException: Missing property in path $['vep']",
cadd_phred: "com.jayway.jsonpath.PathNotFoundException: Missing property in path $['vep']",
mutStatus: "com.jayway.jsonpath.PathNotFoundException: Missing property in path $['mutalyzer']",
sampleName: "com.jayway.jsonpath.PathNotFoundException: Expected to find an object with property ['sample'] in path $['vcf'] but found 'null'. This is not a json object according to the JsonProvider: 'com.jayway.jsonpath.spi.json.JsonSmartJsonProvider'.",
homopolymer: "com.jayway.jsonpath.PathNotFoundException: Expected to find an object with property ['homopolymer'] in path $['vcf'] but found 'null'. This is not a json object according to the JsonProvider: 'com.jayway.jsonpath.spi.json.JsonSmartJsonProvider'.",
readDepth: "com.jayway.jsonpath.PathNotFoundException: Expected to find an object with property ['dp'] in path $['vcf'] but found 'null'. This is not a json object according to the JsonProvider: 'com.jayway.jsonpath.spi.json.JsonSmartJsonProvider'.",
polyphenVal: "com.jayway.jsonpath.PathNotFoundException: Missing property in path $['vep']",
lrtVal: "com.jayway.jsonpath.PathNotFoundException: Missing property in path $['vep']",
pos: "com.jayway.jsonpath.PathNotFoundException: Expected to find an object with property ['position'] in path $['vcf'] but found 'null'. This is not a json object according to the JsonProvider: 'com.jayway.jsonpath.spi.json.JsonSmartJsonProvider'.",
allelesWithMutTestOK: "com.jayway.jsonpath.PathNotFoundException: Missing property in path $['vep']",
existing_variation: "com.jayway.jsonpath.PathNotFoundException: Missing property in path $['vep']",
metaSvmVal: "com.jayway.jsonpath.PathNotFoundException: Missing property in path $['vep']",
consequence: "com.jayway.jsonpath.PathNotFoundException: Missing property in path $['vep']",
ampbias: "com.jayway.jsonpath.PathNotFoundException: Expected to find an object with property ['ampbias'] in path $['vcf'] but found 'null'. This is not a json object according to the JsonProvider: 'com.jayway.jsonpath.spi.json.JsonSmartJsonProvider'.",
cadd: "com.jayway.jsonpath.PathNotFoundException: Missing property in path $['vep']",
ens_transcript: "com.jayway.jsonpath.PathNotFoundException: Missing property in path $['vep']",
mutAssessVal: "com.jayway.jsonpath.PathNotFoundException: Missing property in path $['vep']",
numamps: "com.jayway.jsonpath.PathNotFoundException: Expected to find an object with property ['numAmps'] in path $['vcf'] but found 'null'. This is not a json object according to the JsonProvider: 'com.jayway.jsonpath.spi.json.JsonSmartJsonProvider'.",
fwdVarDepth: "com.jayway.jsonpath.PathNotFoundException: Expected to find an object with property ['adf'] in path $['vcf'] but found 'null'. This is not a json object according to the JsonProvider: 'com.jayway.jsonpath.spi.json.JsonSmartJsonProvider'.",
hgvspAa1: "com.jayway.jsonpath.PathNotFoundException: Missing property in path $['mutalyzer']",
exon: "com.jayway.jsonpath.PathNotFoundException: Missing property in path $['vep']",
clin_sig: "com.jayway.jsonpath.PathNotFoundException: Missing property in path $['vep']"
}
}
}
},
failedResults: { },
vcfMeta: [ ],
vcfMetaNew: { },
numberOfResultErrors: 1,
totalExceptions: {
ClinGenAR: 0,
MyVariant: 0,
Vep: 0,
Mutalyzer: 0,
VICC: 0,
ClinGenER: 0
},
_links: {
self: {
href: "http://localhost:8080/v1/annotate/?variant=chr2:g.47702181C%3ET&build=hg19&schema=derived&sources=ALL+"
}
}
}
 */