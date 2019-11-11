package org.petermac.nic.dao;

import org.petermac.nic.api.RestResponse;
import org.petermac.nic.dataminer.parse.JSONObject;

import java.util.Collection;
import java.util.Map;

/**
 * Created by Ken Doig on 05/11/2018.
 */
public class ClinGenARResponse implements RestResponse, JSONObject
{

    public ClinGenARResponse()
    {
    }

    public String renamedHgvs; // From url call.

    public String       variant;
    public String       error;
    public String       type;
    public String       id;
    public String       context;
    public Collection<Map>    genomicAlleles;
    public Collection<Map> transcriptAlleles;

    public String getVariant()
    {
        return variant;
    }
}
