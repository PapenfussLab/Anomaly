package org.petermac.nic.dao;

import org.petermac.nic.dataminer.domain.pathos.vcf.core.GenomeBuild;
import org.petermac.nic.dataminer.domain.pathos.vcf.sources.AnnotationSource;
import org.slf4j.Logger;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Consumer;

import static org.petermac.nic.dataminer.proj.RestCall.Get;

/**
 * @deprecated Created by Nic on 23/08/2018.
 */
public class ApiRestCall //extends org.petermac.nic.dataminer.proj.RestCall
{
    private static Logger log = org.slf4j.LoggerFactory.getLogger(ApiRestCall.class);

    /**
     * @param hgvs
     * @param annotationSource
     * @param handleError
     * @param clazz
     * @param genomeBuild
     * @param mutResponse
     * @param <T>
     * @return
     * @throws HttpClientErrorException
     * @deprecated use Json model
     */
    static <T> T GetHgvs(String hgvs, final AnnotationSource annotationSource, Consumer<Exception> handleError, Class<T> clazz, GenomeBuild genomeBuild, MutalyzerResponse mutResponse) throws HttpClientErrorException
    {
        final String hgvsAccession = mutResponse == null ? null : mutResponse.hgvsG;

        return Get(new RestTemplate(), annotationSource.getUrl(), annotationSource.getHgvsQuery(), clazz, handleError, makeArgMap(hgvs, hgvsAccession, genomeBuild));
    }


    /**
     * @param hgvs          Cannot be null.
     * @param hgvsAccession can be null.
     * @param genomeBuild   can be NULL Object.
     * @return a map suitable for UriBuilding.
     * @deprecated Use JSON models
     */
    static Map<String, String> makeArgMap(String hgvs, String hgvsAccession, GenomeBuild genomeBuild)
    {
        final Map<String, String> argMap = new LinkedHashMap<>();
        argMap.put("hgvs", hgvs);
        argMap.put("variant", hgvs);
        if (hgvsAccession != null)
            argMap.put("hgvsAccession", hgvsAccession);
        if (!genomeBuild.isNull())
        {
            argMap.put("assembly", genomeBuild.getCommonName());
            argMap.put("assemblyGRC", genomeBuild.name());
        }
        return argMap;
    }
}
