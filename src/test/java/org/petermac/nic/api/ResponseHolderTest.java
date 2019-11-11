package org.petermac.nic.api;

import junit.framework.TestCase;
import org.junit.Test;
import org.petermac.nic.dataminer.domain.pathos.vcf.core.MapSchema;
import org.petermac.nic.dataminer.proj.TM;

import java.util.concurrent.TimeUnit;

/**
 * Created by Nic on 12/03/2019.
 */
public class ResponseHolderTest
{
    @Test
    public void getTime() throws Exception
    {
        System.err.println(new ResponseHolder(null, MapSchema.NULL).getTime());
        TestCase.assertEquals("Not started", new ResponseHolder(null, MapSchema.NULL).getTime());
    }

    @Test
    public void getStart() throws Exception
    {
        final ResponseHolder responseHolder = new ResponseHolder(null, MapSchema.NULL);

        System.err.println(responseHolder.getStartTime());
        TestCase.assertEquals("never", responseHolder.getStartTime());

        responseHolder.start();
        System.err.println(responseHolder.getStartTime());
        TM.sleep(3, TimeUnit.SECONDS);
        System.err.println(responseHolder.getStartTime());
    }

}