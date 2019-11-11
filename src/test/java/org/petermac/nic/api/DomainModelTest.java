package org.petermac.nic.api;

import junit.framework.TestCase;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.petermac.nic.dataminer.domain.pathos.vcf.core.DerivedSchema;
import org.petermac.nic.dataminer.domain.pathos.vcf.services.ResourceDerivedSchema;
import org.petermac.nic.dataminer.parse.JSON;
import org.petermac.nic.dataminer.parse.JSONObject;
import org.petermac.nic.dataminer.proj.Resources;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertNotNull;

/**
 * Created by Nic on 25/07/2018.
 */
@ActiveProfiles("mongo-local")
@RunWith(SpringRunner.class)
//@SpringBootTest(classes = TestConfig.class) //ComponentScan from petermac.nic/** down.
@SpringBootTest(classes = {TestConfig.class, ResourceDerivedSchema.class, org.petermac.nic.dataminer.Application.class})
public class DomainModelTest
{


    @Autowired
    public ResourceDerivedSchema resourceDerivedSchema;

    @Test
    public void writeModelYaml() throws Exception
    {
        _writeModel(resourceDerivedSchema.readSchemaByName("derivedTestSchema.yaml"), Resources.getResourceAsString(getClass(), "/derivedTest.json"));
    }

    @Test
    public void writeModelJson() throws Exception
    {
        final String resourceAsString = Resources.getResourceAsString(getClass(), "/derivedTest.json");//note: sometimes IntelliJ does not find this under test resources.(Rebuild project to fix)
        System.err.println("resourceAsString =  " + resourceAsString);

        _writeModel(resourceDerivedSchema.readSchemaByName("derivedTestSchema.json"), resourceAsString);
    }

    @Test
    public void writePrefTxModelJson() throws Exception
    {
        final String resourceAsString = Resources.getResourceAsString(getClass(), "/derivedTest.json");//note: sometimes IntelliJ does not find this under test resources.(Rebuild project to fix)
        System.err.println("resourceAsString =  " + resourceAsString);

        _writeModel(resourceDerivedSchema.readSchemaByName("/derivedTestPrefTxSchema.json"), resourceAsString);
    }

    private static void _writeModel(final DerivedSchema schema, final String jsonObject) throws Exception
    {
        System.err.println("\n--------------------------------------");
        System.err.println("DerivedSchema = " + JSON.format(schema));
        System.err.println("--------------------------------------\n");

        TestCase.assertNotNull("Schema is NULL", schema);
        final DomainModel dm = new DomainModel(schema);

        dm.writeModel("variant", JSON.parse(jsonObject));

        System.err.println(JSON.format(dm));
        Assert.assertEquals("NC_000017.10:g.41276247A>G", dm.derived.get("myTranscript"));
        Assert.assertEquals("NC", dm.derived.get("myAccess"));
        Assert.assertEquals("chr17", dm.derived.get("myChrom"));
        Assert.assertEquals("Genomic", dm.derived.get("myType"));
        Assert.assertEquals("g.41276247A>G", dm.derived.get("myVariant"));
        Assert.assertEquals("None", dm.derived.get("moleculeType"));
        Assert.assertEquals("Molecule=None yes sir !! and CHR=chr17", dm.derived.get("moleculeType4"));
//        assertEquals("[\"CM041805\",\"rs63750200\"]", dm.derived.get("clin_sig").toString());
        Assert.assertEquals("[[\"uncertain_significance\",\"pathogenic\"]]", dm.derived.get("clin_sig").toString());
        assertNotNull(dm.derived.get("alleles"));
        System.err.println(dm.derived.get("alleles"));
        System.err.println(dm.derived.get("alleles").getClass());
        Assert.assertEquals("[\"HGMD_MUTATION\",\"C\\/T\"]", dm.derived.get("alleles").toString());
//        assertEquals("[\"HGMD_MUTATION\",\"C\\/T\"]", dm.derived.get("alleles"));
    }


    @Test
    public void testAddToOrthogonalMap() throws Exception
    {
        final DerivedSchema schema = resourceDerivedSchema.readSchemaByName("derivedTestSchema.json");
        System.out.println(JSON.format(schema));

        final DomainModel dm = new DomainModel(schema);

        dm.writeModel("variant", JSONObject.wrap(new String(Files.readAllBytes(Paths.get(getClass().getResource("/derivedTest.json").toURI())))));

        final Map<String, List<String>> orthogonalMap = dm.createOrthogonalMap();
        dm.addToOrthogonalMap(orthogonalMap);
        System.err.println("Orthogonal Map");
        System.err.println(orthogonalMap);
        System.err.println(JSON.format(orthogonalMap));
    }
}