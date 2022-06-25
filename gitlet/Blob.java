package gitlet;

import java.io.File;
import java.io.Serializable;

/** Class representing a BLOB; contents of file which format
 * does not matter to system. Be able to connect BLOB to Name of file.
 * Blob contents are derived from physical file in directory.
 * @author Katrina Sharonin.*/

public class Blob implements Serializable {

    /** Literal name of source file passed in i.e. Hello.txt.*/
    private final String _sourceFileName;

    /** ID of blob using sha1 on contents. */
    private final String _blobID;

    /** File which was source of blob. Lives in CWD location.*/
    private final File _sourceOfBlob;

    /** File where blob lives, in .gitlet/BLOBS_FOLDER. Searched by
     * name of hash. */
    private final File _writtenBlob;

    /** Serialized content as a byte array.*/
    private final byte[] _blobContent;

    /** Main constructor taking in SOURCE.*/
    public Blob(File source) {
        this._blobContent = Utils.readContents(source);
        this._sourceOfBlob = source;
        this._blobID = Utils.sha1(this._blobContent);
        this._writtenBlob = searchBlobFile(this._blobID);
        this._sourceFileName = source.getName();
    }

    /** Return the Hash of the BLOB. BLOB hash is purely
     * based on the contents of the file
     * @return string is blob ID.*/
    public String blobID() {
        return this._blobID;
    }

    /** Function that returns file location of blob in folder.
     * Assumes that the file
     * exists when called. DO ERROR CATCHING BEFORE FUNCTION USED.
     * @param hashID is inputted hash ID of blob.
     * @return is file from blob ID.*/
    public static File searchBlobFile(String hashID) {
        File search = Utils.join(RepoFace.BLOBS_FOLDER, hashID);
        return search;

    }

    /** Use LOCATION to give back deserialized blob.
     * @return is actual blob object.*/
    public static Blob readBlobFromFile(File location) {
        return Utils.readObject(location, Blob.class);
    }

    /** Return saved/persisted location of the blob file.
     * @return is the written path.*/
    public File blobFileLocation() {
        return this._writtenBlob;
    }

    /** Literal name of file which will be
     * used for tree mapping.
     * @return is the string name of file.*/
    public String getSourceFileName() {
        return this._sourceFileName;
    }

    /** Return raw contents aka serialized
     * information from source file. */
    public byte[] getBlobContent() {
        return this._blobContent;
    }
}
