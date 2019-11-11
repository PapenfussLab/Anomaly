package org.petermac.nic.api;

import org.junit.Test;
import org.petermac.nic.dataminer.parse.JSONObject;

/**
 * Created by Nic on 1/04/2019.
 */
//@RunWith(SpringRunner.class)
//@SpringBootTest(classes = {TestConfig.class, org.petermac.nic.dataminer.Application.class})
public class VepFilteredTest
{
//    @Autowired
//    VepFiltered vepFiltered;

//    @Test
//    public void getFiltered() throws Exception
//    {
//        System.err.println(vepFiltered.getFiltered(new VepResponse()).formatAsJson());
//    }

    @Test
    public void getFiltered() throws Exception
    {
        final VepFiltered filtered = new VepFiltered().getFiltered(new VepResponse());

        System.err.println(filtered.formatAsJson());

        System.err.println(filtered.getJsonParser().get("error"));
        System.err.println(JSONObject.wrap(filtered).getJsonParser().get("error"));
    }

}