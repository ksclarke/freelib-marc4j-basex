
package info.freelibrary.xquery.marc;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

import org.basex.core.Sandbox;
import org.basex.core.cmd.CreateDB;
import org.basex.core.cmd.RepoInstall;
import org.basex.io.IOFile;
import org.basex.query.QueryException;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import info.freelibrary.util.FileExtFileFilter;
import info.freelibrary.util.StringUtils;

public class BaseXModuleIT extends Sandbox {

    private static final String MODULE_NS = "http://freelibrary.info/xquery/marc";

    @BeforeClass
    public static void before() throws QueryException {
        final String fakeXARFile = System.getProperty("java.io.tmpdir") + "/" + UUID.randomUUID();
        final String xarFilePath = System.getProperty("xar.file", fakeXARFile);
        final File xarFile = BaseXModuleUtils.getFile(xarFilePath);

        if (!xarFile.exists()) {
            fail("XAR file does not exist: " + xarFilePath);
        }

        initSandbox();

        // Install our XAR file so we can test its module
        execute(new RepoInstall(xarFile.getAbsolutePath(), null));

        /*
         * Can't create a database and use it from the same BaseX query
         * http://docs.basex.org/wiki/XQuery_Update#Pending_Update_List
         */
        execute(new CreateDB("db"));
    }

    @AfterClass
    public static void after() {
        finishSandbox();
    }

    @Test
    public void testPkgInstall() throws QueryException {
        final String version = System.getProperty("xar.version");
        final String marc4jVersion = System.getProperty("marc4j.version");
        final String utilsVersion = System.getProperty("utils.jar.version");
        final String repoModuleDir = normalize(MODULE_NS + "-" + version);

        assertTrue(dirExists(repoModuleDir));
        assertTrue(fileExists(repoModuleDir + "/expath-pkg.xml"));
        assertTrue(fileExists(repoModuleDir + "/basex.xml"));
        assertTrue(fileExists(repoModuleDir + "/repo.xml"));

        assertTrue(dirExists(repoModuleDir + "/freelib-marc"));
        assertTrue(fileExists(repoModuleDir + "/freelib-marc/freelib-marc4j-basex-" + version + ".jar"));
        assertTrue(fileExists(repoModuleDir + "/freelib-marc/freelib-marc4j-" + marc4jVersion + ".jar"));
        assertTrue(fileExists(repoModuleDir + "/freelib-marc/freelib-utils-" + utilsVersion + ".jar"));
        assertTrue(fileExists(repoModuleDir + "/freelib-marc/marc-wrapper.xq"));
    }

    /**
     * Runs through the module tests in the <code>src/test/xqueries</code> directory.
     *
     * @throws QueryException If one of the tests fails to be read
     */
    @Test
    public void testModule() {
        System.out.println();

        for (final File xq : new File("src/test/xqueries").listFiles(new FileExtFileFilter("xq"))) {
            System.out.println("[INFO] Running XQuery tests from: " + xq);

            try {
                for (final String result : query(StringUtils.read(xq)).split(System.getProperty("line.separator"))) {
                    if (result.startsWith("[ERROR] ")) {
                        fail(result);
                    } else {
                        if (result.startsWith("[INFO] ") || result.startsWith("[DEBUG] ") || result.startsWith("<")) {
                            System.out.println(result);
                        }
                    }
                }
            } catch (final IOException details) {
                fail("[ERROR] Failed to read " + xq.getAbsolutePath() + ": " + details.getMessage());
            } catch (final AssertionError details) {
                System.out.println();

                if (details.getMessage().startsWith("[ERROR] ")) {
                    throw new AssertionError(details.getMessage().substring(8), details.getCause());
                } else if (details.getMessage().startsWith("Query failed")) {
                    fail("Query failed [see Sandbox exception message above]");
                } else {
                    throw details;
                }
            }
        }

        System.out.println();
    }

    /**
     * Checks if the specified path points to a file.
     *
     * @param aPath file path
     * @return True if file exists; else, false
     */
    private static boolean fileExists(final String aPath) {
        final IOFile file = new IOFile(context.repo.path().path(), aPath);
        return file.exists() && !file.isDir();
    }

    /**
     * Checks if the specified path points to a directory.
     *
     * @param aPath file path
     * @return True if dir exists; else, false
     */
    private static boolean dirExists(final String aPath) {
        return new IOFile(context.repo.path().path(), aPath).isDir();
    }

    /**
     * Normalizes the given path.
     *
     * @param path path
     * @return normalized path
     */
    private static String normalize(final String path) {
        return path.replaceAll("[^\\w.-]+", "-");
    }
}
