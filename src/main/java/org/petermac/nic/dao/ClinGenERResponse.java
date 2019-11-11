package org.petermac.nic.dao;

import org.petermac.nic.api.RestResponse;

import java.util.List;
import java.util.Map;

/**
 * Created by Ken Doig on 18/11/2018.
 */
class ClinGenERResponse implements RestResponse
{
    public ClinGenERResponse()
    {
    }

    public String       error;
    public String       context;

    public List<Map>    variantInterpretations;
}
