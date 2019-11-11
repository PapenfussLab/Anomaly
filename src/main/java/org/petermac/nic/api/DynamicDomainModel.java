package org.petermac.nic.api;

import org.petermac.nic.dataminer.domain.pathos.vcf.core.DerivedSchema;
import org.petermac.nic.dataminer.domain.pathos.vcf.core.MapSchema;
import org.petermac.nic.dataminer.domain.pathos.vcf.sources.AnnotationURLProvider;
import org.petermac.nic.dataminer.parse.JSONObject;
import org.petermac.nic.dataminer.proj.TM;
import org.slf4j.Logger;

import java.io.IOException;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiConsumer;

/**
 * ---
 * Created by Nic on 14/06/2019.
 */
public class DynamicDomainModel extends DomainModel
{
    private static Logger log = org.slf4j.LoggerFactory.getLogger(DynamicDomainModel.class);

    private final Map<String, AnnotationURLProvider> providerMap;

    /**
     * Ctor
     *
     * @param derivedSchema
     * @param providerMap   all possible provider that support the Genome Build.  cannot be null
     */
    public DynamicDomainModel(MapSchema derivedSchema, Map<String, AnnotationURLProvider> providerMap)
    {
        super(derivedSchema);
        Objects.requireNonNull(providerMap);
        this.providerMap = providerMap;
    }

    @Override
    protected void writeModelWithSchema(String variant, DerivedSchema schema, JSONObject json, BiConsumer<String, Object> f)
    {
        log.debug("variant {} schema {} providerMap={}", variant, schema.getSchemaName(), providerMap);

        if (!(json instanceof SourceInjection))
            log.warn("Expecting JSONObject to implement SourceInjection !");
        else
            setMapper((src, jsonObject) -> {
                Object sourceResult = null;// no injection

                if (jsonObject == null)
                {
                    log.debug("Cheeky: provider probe: '{}' returns {}", src, providerMap.get(src) != null);
                    return providerMap.get(src); //Cheeky !! Valid src test!
                }
                if (!((SourceInjection) json).hasSrc(src)) //not loaded.
                {
                    final AnnotationURLProvider annotationURLProvider = providerMap.get(src);
                    if (annotationURLProvider == null)
                        throw new RuntimeException("No Provider! for source " + src);
//                        sourceResult = new RuntimeException("No Provider! for source " + src);
                    else
                    {
                        try (TM tm = TM.create("mapper creating Source Result for" + annotationURLProvider.getName()).logOnClose(log))
                        {
                            sourceResult = annotationURLProvider.createSourceResult(null, jsonObject, alias -> {
                                log.debug("alias Map  = {}", getAliases());
                                final String value = getAliases().getOrDefault(alias, alias);
                                log.debug("{}  mapped to  alias value {}", alias, value);

                                return value;//ClinGenAR.transcriptAlleles[*].hgvs.set()
                            }, e -> {
                                log.warn(annotationURLProvider.getName(), e);
                                ((SourceInjection) json).inject(src, e); //<<Dynamic ERROR insertion !!!
                            });//todo need to provide the exceptions?
                        } catch (IOException e)//from AutoClose
                        {
                        }
                    }
                    ((SourceInjection) json).inject(src, sourceResult); //<<Dynamic insertion !!!
                    log.debug("Inserted Source Result {}", src);
                }
                return sourceResult;
            });

        super.writeModelWithSchema(variant, schema, json, f);
    }

    public interface SourceInjection extends JSONObject
    {
        void inject(String key, Object value);

        boolean hasSrc(String src);
    }

}
