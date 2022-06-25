package gitlet;

import java.io.IOException;
import java.io.Serializable;
import java.util.TreeMap;
import java.io.File;

/** Single class instead of two -> house both tree maps.
 * Both maps will be written into the same file and will
 * be separated after deserialization.
 * @author Katrina Sharonin.*/
public class Index implements Serializable {

    /** Added V: Treemap mapping literal name of file to
     * blobID.
     * I.e "Hello.txt" <-> "fkwjfk43" etc. */
    private TreeMap<String, String> added;

    /** Removed V: Treemap mapping literal name of file
     * to blobID.
     * I.e "Hello.txt" <-> "fkwjfk43" etc. */
    private TreeMap<String, String> removed;

    /** Construct area including added and removed. */
    public Index() {
        added = new TreeMap<>();
        removed = new TreeMap<>();
    }

    /** Save index state from correct POV. */
    public void saveIndex() {
        Utils.writeObject(RepoFace.INDEX_FILE, this);
    }

    /** Add ADDIN to existing index.
     * Modifies staging area if certain conditions are
     * fulfilled, otherwise it may be removed from the
     * "removed" staging area or
     * removed from "added". Called from RepoFace class.
     * @param addIn is the file to add in.
     * @return boolean is true if we changed anything. */

    public boolean addtoIndex(File addIn) {
        Blob blobOfAdd = new Blob(addIn);
        String blobID = blobOfAdd.blobID();
        String nameOfFile = addIn.getName();
        if (RepoFace.headdReturner().exists()) {
            String temp =
                    Utils.readContentsAsString(RepoFace.
                            headdReturner());
            boolean commitAndFile =
                    doesCAFTreeMapExist(temp,
                            blobID, nameOfFile);
            if (commitAndFile) {
                if (added.containsKey(nameOfFile)) {
                    added.remove(nameOfFile);
                    return true;
                } else if (removed.
                        containsKey(nameOfFile)) {
                    removed.remove(nameOfFile);
                    return true;
                } else {
                    return false;
                }
            }
        }
        if (added.containsKey(nameOfFile)) {
            String matchingBlobIDFoundFromKey =
                    added.get(nameOfFile);
            String previousBlob =
                    added.put(nameOfFile, blobID);
            if (previousBlob != null
                    && previousBlob.equals(blobID)) {
                return false;
            }
            if (matchingBlobIDFoundFromKey != null
                    && matchingBlobIDFoundFromKey.equals(blobID)) {
                return false;
            } else {
                added.remove(nameOfFile);
            }
        }
        if (!blobOfAdd.blobFileLocation().exists()) {
            File newLocation =
                    Utils.join(RepoFace.BLOBS_FOLDER, blobID);
            try {
                newLocation.createNewFile();
                Utils.writeObject(newLocation, blobOfAdd);
            } catch (IOException error) {
                System.exit(0);
            }
            added.put(nameOfFile, blobID);
            return true;
        }
        if (removed.containsKey(nameOfFile)) {
            removed.remove(nameOfFile);
            return true;
        }
        added.put(nameOfFile, blobID);
        return true;
    }

    /** Helper Function which takes in commit hash and (1)
     * looks for directory in COMMITS_FOLDER and
     * (2) checks tree map for given  blob hash ID + name of file.
     * Return false if either is unsatisfied.
     * MUST match EXACT file name and EXACT file hash value.
     * @param commitID is commit ID.
     * @param blobID is hash ID of blob.
     * @param fileName is literal file name.
     * @return is boolean.*/

    public boolean doesCAFTreeMapExist(String commitID,
                                                   String blobID,
                                                   String fileName) {
        File attemptToFindCommit =
                Utils.join(RepoFace.COMMITS_FOLDER, commitID);
        if (attemptToFindCommit.exists()) {
            Commit foundIt =
                    Utils.readObject(attemptToFindCommit,
                            Commit.class);
            TreeMap<String, String> getSnapshot =
                    foundIt.getSnapshot();
            return getSnapshot.containsKey(fileName)
                    && getSnapshot.get(fileName).
                    equals(blobID);
        } else {
            return false;
        }
    }

    /** Called to return existing added stage of index.
     * For external operations using class.
     * @return tree map of strage added component.*/
    public TreeMap<String, String> currAddedStage() {
        return added;
    }

    /** Called to return existing added stage of index.
     * For external operations using class.
     * @return tree map of removed index part. */
    public TreeMap<String, String> currRemovedStage() {
        return removed;
    }

    /** Clear index -> clear both tree maps.*/
    public void clear() {
        added.clear();
        removed.clear();
    }

    /** Called to see if clean; required post commit.
     * @return boolean if clear is true or false.*/
    public boolean isClear() {
        boolean addedCase = added.isEmpty();
        boolean removedCase = removed.isEmpty();
        return  addedCase && removedCase;
    }

}
