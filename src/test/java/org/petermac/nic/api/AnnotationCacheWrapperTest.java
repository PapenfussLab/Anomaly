package org.petermac.nic.api;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.petermac.nic.dataminer.domain.pathos.vcf.services.CachingService;
import org.petermac.nic.dataminer.domain.pathos.vcf.sources.AnnotationCacheWrapper;
import org.petermac.nic.dataminer.domain.pathos.vcf.sources.AnnotationSource;
import org.petermac.nic.dataminer.parse.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.Date;

/**
 * Created by Nic on 12/03/2019.
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = {TestConfig.class, org.petermac.nic.dataminer.Application.class})
public class AnnotationCacheWrapperTest
{

    @Autowired
    CachingService<String, Object, String, Date> cachingService;

    @Test
    public void test() throws Exception
    {
        new AnnotationCacheWrapper(AnnotationSource.Vep, cachingService)
                .createSourceResult(new RestTemplate(),
                        new JSONObject.PrimitiveWrapper(Collections.singletonMap("variant", TestVariants.variant)),
                        s-> s, e -> { });
    }

}