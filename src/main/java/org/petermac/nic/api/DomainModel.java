package org.petermac.nic.api;

import com.jayway.jsonpath.PathNotFoundException;
import org.petermac.nic.dataminer.domain.pathos.vcf.core.DerivedSchema;
import org.petermac.nic.dataminer.domain.pathos.vcf.core.MapSchema;
import org.petermac.nic.dataminer.parse.JSONObject;
import org.petermac.nic.dataminer.parse.JsonParser;
import org.petermac.nic.dataminer.transcode.Strings;
import org.slf4j.Logger;
import org.springframework.data.annotation.Transient;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.regex.Pattern;

/**
 * * Current structure loaded into PathOS SeqVariant domain model
 * </pre>
 * REST Service For Anomaly.[Annotations aggregator] Version 1.0
 * <p/>
 * Created by Nic on 25/07/2018.
 */
public class DomainModel extends DerivedSchema
{
    private static Logger log = org.slf4j.LoggerFactory.getLogger(DomainModel.class);

    public final Map<String, Object> derived;
    public Map<String, String> errors;
    private String variant;

    @Transient
    private BiFunction<String, JSONObject, Object> mapper; //for dynamic source creation


    public DomainModel()
    {
        super("", "derived model never written", "", new HashMap<>(), new HashMap<>());
        derived = new HashMap<>();
    }

    public DomainModel(MapSchema derivedSchema)
    {
        super(derivedSchema);
        derived = new HashMap<>();
    }


    protected void setMapper(BiFunction<String, JSONObject, Object> mapper)
    {
        this.mapper = mapper;
    }

    /**
     * @return
     * @see <a href=https://www.pluralsight.com/blog/tutorials/introduction-to-jsonpath>introduction-to-jsonpath</a>
     */

    public Map<String, Object> getDerived()
    {
        return derived;
    }

    public void writeModel(String variant, JSONObject json, BiConsumer<String, Object> f)
    {
        writeModelWithSchema(variant, this, json, f);
    }

    void writeModel(String variant, JSONObject json)
    {
        writeModelWithSchema(variant, this, json, (s, o) -> {
        });
    }

    /**
     * @param schema
     * @param json   the Entire Annotation data set: parsed using a JsonParser
     * @see JsonParser
     */
    protected void writeModelWithSchema(String variant, DerivedSchema schema, JSONObject json, BiConsumer<String, Object> f)
    {
        this.variant = variant;
        errors = new TreeMap<>();
        log.debug("{}", new Object()
        {
            public String toString()
            {
                return json.formatAsJson();
            }
        });

        int mapped;
        while ((mapped = mapKeys(schema, json, f)) > 0)
            log.debug("Mapped {}", mapped);
        log.debug("(EXIT) Mapped {}", mapped);
        for (final String key : schema.getSchema().keySet())
            if (!derived.containsKey(key))
                derived.put(key, schema.getDefaultValue());
    }

    class MappingValue
    {
        /**
         * "clinGen.transcriptAlleles[*].geneSymbol.set()"
         */
        private final Pattern SetFunction = Pattern.compile("\\.set\\(\\)\\s*$");
        private final BiFunction<String, JSONObject, Object> mapper;
        private final String value;
        private final Set<String> sources;
        private final boolean setFunction;

        public MappingValue(final String value, BiFunction<String, JSONObject, Object> mapper)
        {
            this.value = SetFunction.matcher(value).replaceFirst("");
            this.mapper = mapper;
            setFunction = this.value.length() != value.length();
            sources = new HashSet<>();
            for (String token : value.split("[^a-zA-Z0-9]+"))
                if (mapper.apply(token, null) != null)//Cheeky !! Valid src test!)
                    sources.add(token); //  a valid src!
        }

        /**
         * Apply some built in behaviour (like object to set)
         *
         * @param jsonParser
         * @return
         */

        public Object getFunctionValue(JsonParser jsonParser)
        {
            return setFunction ? jsonParser.getAsCsv(value) : jsonParser.get(value);
        }

        public String getValue()
        {
            return value;
        }

        @Override
        public String toString()
        {
            return Strings.cat(", ", sources, value, setFunction);
        }

        public JsonParser map(JSONObject json)
        {
            boolean injected = false;
            log.debug("root={} json={} mapper={}", sources, json, mapper);
            if (json != null && !sources.isEmpty() && mapper != null)
                for (String source : sources)
                    if (mapper.apply(source, json) != null) //multiple sources on one line ?
                        injected = true;

            return injected ? json.getJsonParser() : null; // data injected into 'json' - so need to re parse!
        }
    }

    private int mapKeys(DerivedSchema schema, JSONObject json, BiConsumer<String, Object> f)
    {
        JsonParser jsonParser = null;

        int keysMapped = 0;
        for (final String key : schema.getSchema().keySet())
        {
            if (derived.containsKey(key))
                continue;
            String currentValue = null;
            try
            {
                final MappingValue valueMapping = new MappingValue(schema.getString(key, this::logAlias), mapper);
                log.debug("valueMapping={}", valueMapping);
                final String expression = valueMapping.getValue();
                currentValue = expression;
                jsonParser = Strings.notNull(valueMapping.map(json), jsonParser); //a new object may have  been inserted into schema data.
                if (jsonParser == null)
                    jsonParser = json.getJsonParser(); //initial condition
                final String stringReplacement = jsonParser.getExpanded(expression); //looks for {...}
                log.debug("key={} expression={} valueMapping={} expanded={}", key, expression, valueMapping, stringReplacement);

                if (expression.equals(stringReplacement)) //NO change!,
                {
                    currentValue = expression;
                    derived.put(key, valueMapping.getFunctionValue(jsonParser));//.get(expression)); //Object version is the default.
                } else
                    derived.put(key, stringReplacement);//String Version: any objects are flattened to strings.
                f.accept(key, derived.get(key));
                keysMapped++;
                log.debug("mapped {} to {}. {} keys Mapped", key, derived.get(key), keysMapped);
            } catch (PathNotFoundException e)
            {
//                log.error(currentValue, e);
                log.debug("PathNotFoundException! value={} key={}", currentValue, key);
                log.debug("PathNotFoundException! for {} in JSON={}", currentValue, json.formatAsJson());
//                errors.put(key, e.toString()); No! will get these as we loop around to completion

            } catch (Exception e)
            {
                log.debug(variant, e);
//                e.printStackTrace();
                errors.put(key, e.toString());
            }
        }
        return keysMapped;
    }

    private void logAlias(String before, String after)
    {
        log.trace("{} --alias--> {}", before, after);
    }

    public void addToOrthogonalMap(final Map<String, List<String>> orthogonalMap)
    {
        DomainModel domainModel = this;
        final String preamble = "[" + variant + "]--> ";

        for (Map.Entry<String, List<String>> orthogonalEntry : orthogonalMap.entrySet())
            orthogonalEntry.getValue().add(preamble + Objects.toString(
                    domainModel.errors.get(orthogonalEntry.getKey()),        //add the error if set
                    Objects.toString(domainModel.derived.get(orthogonalEntry.getKey()))));//else the value if no error
    }
}
