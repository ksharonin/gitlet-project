package gitlet;

import java.io.IOException;
import java.io.Serializable;
import java.io.File;

/** Class representing branches as file form. Previously constructed as
 * Tree map, this will now store as a file ("master" <-> commit hash).
 * @author Katrina Sharonin.*/
public class Branches implements Serializable {

    /** Name of branch.*/
    private final String _name;
    /** Current commit of branch.*/
    private Commit _currCommittt;
    /** Commit ID of current commit.*/
    private String _commitID;

    /** Constructor for when branches name passed in.
     * @param currCommit will be used and
     * @param branchName too.*/
    public Branches(Commit currCommit, String branchName) {
        this._name = branchName;
        this._commitID = currCommit.getID();
        this._currCommittt = currCommit;
    }

    /** Constructor -> single instance per repository of new -> initial level.
     * @param starter will be first commit.*/
    public Branches(Commit starter) {
        this._name = "master";
        this._currCommittt = starter;
        this._commitID = _currCommittt.getID();
    }

    /** Function writing into BRANCHES_FOLDER. Create file that is
     * named by _name if not already existent. */
    public void saveBranch() {
        File saveHere = Utils.join(RepoFace.BRANCHES_FOLDER, this._name);
        try {
            if (!saveHere.exists()) {
                saveHere.createNewFile();
            }
            Utils.writeObject(saveHere, this);
        } catch (IOException error) {
            System.exit(0);
        }
    }

    /** Write in the new branches object into the CURRENT_BRANCH folder.
     * This will be done every commit to make sure commit IDs of branch are
     * up to date. */
    public void updateCurrentBranch() {
        Utils.writeContents(RepoFace.cBReturner(), this._name);
    }

    /** Use FINDBRANCH literal name to find a given branch in the directory.
     * Return a branches object which will have the commit within. */

    public static Branches getBranchFromFile(String findBranch) {

        File branchFileFound = Utils.join(RepoFace.BRANCHES_FOLDER, findBranch);

        if (!branchFileFound.exists()) {
            return null;
        }

        Branches found = Utils.readObject(branchFileFound, Branches.class);
        return found;
    }

    /** Function which returns the current commit of this branch.*/
    public Commit getCurrCommit() {
        return _currCommittt;
    }

    /** Function which returns the name of this branch.*/
    public String getBranchName() {
        return this._name;
    }

}
