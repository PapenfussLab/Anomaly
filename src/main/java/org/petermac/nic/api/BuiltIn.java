package org.petermac.nic.api;

import org.petermac.nic.dao.MutalyzerResponse;
import org.petermac.nic.dataminer.domain.pathos.vcf.core.GenomeBuild;

import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

/**
 * Created by Nic on 30/03/2019.
 */
public interface BuiltIn
{
    MutalyzerResponse call(String hgvs, GenomeBuild genomeBuild, List<String> log) throws Exception;

    Stream<MutalyzerResponse> call(Collection<String> variants, GenomeBuild genomeBuild, List<String> log);

}
