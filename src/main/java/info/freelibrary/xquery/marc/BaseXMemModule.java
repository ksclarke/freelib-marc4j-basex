
package info.freelibrary.xquery.marc;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;

import javax.xml.transform.sax.SAXResult;

import org.basex.io.serial.SAXSerializer;
import org.basex.query.QueryException;
import org.basex.query.QueryModule;
import org.basex.query.QueryResource;
import org.basex.query.value.Value;
import org.basex.query.value.ValueBuilder;
import org.basex.query.value.item.Item;
import org.basex.query.value.type.Type;
import org.marc4j.MarcException;
import org.marc4j.MarcReader;
import org.marc4j.MarcStreamReader;
import org.marc4j.MarcStreamWriter;
import org.marc4j.MarcWriter;
import org.marc4j.MarcXmlHandler;
import org.marc4j.MarcXmlWriter;
import org.marc4j.RecordStack;
import org.marc4j.marc.Record;
import org.xml.sax.SAXException;

import info.freelibrary.marc4j.converter.impl.AnselToUnicode;
import info.freelibrary.marc4j.converter.impl.UnicodeToAnsel;

/**
 * An extension module for reading and writing MARC (MAchine Readable Cataloging) records from and to the file system.
 *
 * @author <a href="mailto:ksclarke@ksclarke.io">Kevin S. Clarke</a>
 */
public class BaseXMemModule extends QueryModule implements QueryResource {

    /**
     * Writes MARCXML records to a MARC file.
     *
     * @return True if the records were successfully written
     */
    public boolean write(final Value aRecSeq, final String aFilePath) throws QueryException {
        MarcWriter writer = null;

        try {
            final Iterator<Item> iterator = aRecSeq.iterator();
            final RecordStack recordStack = new RecordStack();
            final MarcXmlHandler marcHandler = new MarcXmlHandler(recordStack);

            writer = new MarcStreamWriter(new FileOutputStream(aFilePath));
            writer.setConverter(new UnicodeToAnsel());

            while (iterator.hasNext()) {
                final Item item = iterator.next();

                if (item.typeId().equals(Type.ID.ELM)) {
                    final SAXSerializer serializer = new SAXSerializer(item);

                    serializer.setContentHandler(marcHandler);
                    serializer.parse("");

                    if (recordStack.hasNext()) {
                        final Record record = recordStack.pop();

                        try {
                            writer.write(record);
                        } catch (final MarcException details) {
                            String recordId = record.getControlNumber();

                            if (recordId != null) {
                                recordId = recordId.trim();
                            } else {
                                recordId = "UNKNOWN RECORD ID";
                            }

                            throw new QueryException("Record [" + recordId + "]: " + details.getMessage());
                        } finally {
                            try {
                                serializer.close();
                            } catch (final IOException details) {
                                throw new QueryException(details.getMessage());
                            }
                        }
                    } else {
                        try {
                            serializer.close();
                        } catch (final IOException details) {
                            throw new QueryException("Didn't read MARC record and couldn't close serializer");
                        }

                        throw new QueryException("Didn't read MARC record");
                    }
                } else {
                    throw new QueryException("Write takes record element or a sequence of record elements");
                }
            }

            writer.close();
        } catch (final FileNotFoundException details) {
            throw new QueryException(details.getMessage());
        } catch (final SAXException details) {
            throw new QueryException(details.getMessage());
        } finally {
            if (writer != null) {
                writer.close();
            }
        }

        return true;
    }

    /**
     * Reads MARC records from a file into memory as MARCXML records.
     *
     * @return A sequence of MARC records
     */
    public Value read(final String aFilePath) throws QueryException {
        final BaseXContentHandler handler = new BaseXContentHandler();
        final SAXResult result = new SAXResult(handler);
        final MarcWriter writer = new MarcXmlWriter(result);
        final ValueBuilder vb = new ValueBuilder();

        InputStream inputStream = null;
        MarcReader reader;

        writer.setConverter(new AnselToUnicode());

        try {
            inputStream = new FileInputStream(BaseXModuleUtils.getFile(aFilePath));
            reader = new MarcStreamReader(inputStream);

            while (reader.hasNext()) {
                writer.write(reader.next());
                vb.add(handler.getRecord());
            }

            writer.close();

            return vb.value();
        } catch (final MarcException details) {
            throw new QueryException(details.getMessage());
        } catch (final IOException details) {
            throw new QueryException(details.getMessage());
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (final IOException details) {
                    throw new QueryException(details.getMessage());
                }
            }
        }
    }

    @Override
    public void close() {

    }

}
