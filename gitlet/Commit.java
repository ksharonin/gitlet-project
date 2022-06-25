package gitlet;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TreeMap;
import java.util.Locale;

/** Class representing a COMMIT object. Commit contains several
 * components which must all be considered and tied.
 * @author Katrina Sharonin.*/

public class Commit implements Serializable {

    /** Passed in message of this commit instance.*/
    private final String _message;

    /** Date of the commit.*/
    private final Date _date;

    /** Unique SHA-1 ID using commit components.*/
    private final String _ID;

    /** Existing snapshot of our files tracked.*/
    private final TreeMap<String, String> _snapshot;

    /** Hash ID of direct parent.*/
    private final String _parentLink;

    /** Array list storing all prev parents. */
    private final String _secondParentLink;

    /** File location of written in commit.*/
    private final File _location;

    public Commit(String message, TreeMap<String, String> tracking,
                  String ancestors, String directParent) {
        this._message = message;
        this._date = new Date();
        this._snapshot = tracking;
        this._secondParentLink = ancestors;
        this._ID = commitHashResult();
        this._parentLink = directParent;
        this._location = Utils.join(RepoFace.COMMITS_FOLDER, this._ID);
    }

    /** Alternative constructor for the initial commit. */
    public Commit() {
        this._message = "initial commit";
        this._secondParentLink = "";
        this._snapshot = new TreeMap<>();
        this._date = new Date(0);
        this._ID = commitHashResult();
        this._parentLink = null;
        this._location = Utils.join(RepoFace.COMMITS_FOLDER, this._ID);
    }

    /** Generates a unique SHA-ID using the unique components of a commit.
     * message, date/time, tree map of relations, and parents.
     * @return resulting hash of a commit object.*/
    public String commitHashResult() {

        String ancestorsInForm = this._secondParentLink;
        String snapshotInForm = this._snapshot.toString();
        String metaDataFormed = getMetaData();
        String idFormed = Utils.sha1(ancestorsInForm,
                this._message, metaDataFormed, snapshotInForm);
        return idFormed;
    }

    /** Format and access function. Apply for given date.
     * @return meta data in string form.*/
    public String getMetaData() {
        DateFormat formmated = new SimpleDateFormat("EEE MMM d "
                + "HH:mm:ss yyyy Z", Locale.ENGLISH);
        return formmated.format(_date);
    }

    /** Access function due to privacy of variables.
     * @return is the message of the commit.*/
    public String getMssg() {
        return this._message;
    }

    /** Access function due to privacy of variables.
     * @return is the ID of the commit aka SHA1 hash.*/
    public String getID() {
        return this._ID;
    }

    /** Access function due to privacy of variables.
     * @return Accessed tree map from commit. */
    public TreeMap<String, String> getSnapshot() {
        return this._snapshot;
    }

    /** Access function due to privacy of variables.
     * @return the second parent hash.*/
    public String getSecondParent() {
        return this._secondParentLink;
    }

    /** Access function for string.
     * @return hash of main first parent.*/
    public String getParentLink() {
        return this._parentLink;
    }


    /** Write commit object into a file. Use self instance.
     * Check is location DNE -> make if not */
    public void saveCommit() {
        try {
            if (!_location.exists()) {
                _location.createNewFile();
            }
            Utils.writeObject(_location, this);

        } catch (IOException error) {
            System.exit(0);
        }
    }

    /** Function which prints object in proper order following spec.
     * @param curr is the current commit object to print.*/
    public static void printCommitObject(Commit curr) {
        System.out.println("===");
        System.out.println("commit " +  curr.getID());
        System.out.println("Date: " + curr.getMetaData());
        System.out.println(curr.getMssg());
        System.out.println();
    }

    /** Deserialize aka read commit object from file.
     * HASHID is input of commit ID.
     * @return commit from file.*/
    public static Commit getCommitFromFile(String hashID) {
        File matchingFile = Utils.join(RepoFace.COMMITS_FOLDER, hashID);

        if (!matchingFile.exists()) {
            System.out.println("No commit with that id exists.");
            System.exit(0);

        }
        return Utils.readObject(matchingFile, Commit.class);
    }
}
