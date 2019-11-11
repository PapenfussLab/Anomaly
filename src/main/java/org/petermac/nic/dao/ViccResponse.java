package org.petermac.nic.dao;

import org.petermac.nic.api.RestResponse;

import java.util.Map;

/**
 * Created by Ken Doig on 26/10/2018.
 */
class ViccResponse implements RestResponse
{
    public ViccResponse()
    {
    }

    public String variant;
    public String error;
    public Map    hits;

    public String getVariant()
    {
        return variant;
    }
}
