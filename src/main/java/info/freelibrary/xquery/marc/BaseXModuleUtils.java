
package info.freelibrary.xquery.marc;

import java.io.File;
import java.net.URI;

import org.basex.query.QueryException;

public class BaseXModuleUtils {

    private BaseXModuleUtils() {
        // Don't allow instantiation of utility classes
    }

    /**
     * Returns a <code>File</code> for the supplied file ID.
     *
     * @param aFileId A supplied file ID
     * @return The <code>File</code> representing the supplied file ID
     * @throws QueryException If there is trouble parsing the supplied file ID
     */
    public static final File getFile(final String aFileId) throws QueryException {
        File baseDir;

        if (aFileId.startsWith("file:")) {
            try {
                baseDir = new File(new URI(aFileId));
            } catch (final Exception details) {
                throw new QueryException(details.getMessage());
            }
        } else {
            baseDir = new File(aFileId);
        }

        return baseDir;
    }

}
