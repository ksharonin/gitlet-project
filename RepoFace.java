package gitlet;

import java.io.IOException;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.util.TreeMap;
import java.util.List;
import java.util.Collections;
import java.util.ArrayList;
import java.io.File;
import java.nio.file.Paths;

/** Class representing our Repository that faces user;
 * holds all interactions between
 * Classes of BLOB and COMMIT while also handling staging.
 * Main is intended to
 * call appropriate function given the user input
 * through ARGS.
 * @author Katrina Sharonin*/

public class RepoFace implements Serializable {

    /** Current working directory.*/
    private static final File CWD =
            new File(System.getProperty("user.dir"));

    /** Gitlet folder in CWD.*/
    static final File GITLET_FOLDER =
            Utils.join(CWD, ".gitlet");

    /** Blobs folder in gitlet folder.*/
    static final File BLOBS_FOLDER =
            Utils.join(GITLET_FOLDER, "blobs");

    /** Commits folder in gitlet folder.*/
    static final File COMMITS_FOLDER =
            Utils.join(GITLET_FOLDER, "commits");

    /** Index file in gitlet folder.*/
    static final File INDEX_FILE =
            Utils.join(GITLET_FOLDER, "index.txt");

    /** Branches folder in gitlet folder.*/
    static final File BRANCHES_FOLDER =
            Utils.join(GITLET_FOLDER, "branches");

    /** Live in general .gitlet directory. */
    private static File headd =
            Utils.join(GITLET_FOLDER, "head.txt");


    /** Current branch with a LITERAL name i.e.
     * "master" written into a txt file for ease of access.*/
    private static File currentBranchh =
            Utils.join(GITLET_FOLDER, "currentBranch.txt");

    /** The current index of this repository. */
    private static Index currIndex = existingStage();

    /** Depth mapping tree for split point.*/
    private static TreeMap<String, Integer> depthInCurrent =
            new TreeMap<>();

    /** Split point helper var.*/
    private static Integer minimizedDepth = 1000;

    /** Split point capture var.*/
    private static String minimizingHash = "";

    /** Check persistence to see if a index exists.
     * If so, load it. If not
     * Instantiate the object.
     * @return is our current stage.*/

    public static Index existingStage() {
        if (INDEX_FILE.exists()) {
            return Utils.readObject(INDEX_FILE, Index.class);
        } else {
            return new Index();
        }
    }

    /** Command which officially creates the hidden
     * directories of .gitlet and
     * all under. Also initiate tree maps
     * and other set up factors. */

    public static void initCommand() {
        if (GITLET_FOLDER.exists()) {
            System.out.println("A Gitlet version-control "
                    + "system already exists "
                    + "in the current directory.");
            System.exit(0);
        } else {

            GITLET_FOLDER.mkdir();
            BLOBS_FOLDER.mkdir();
            COMMITS_FOLDER.mkdir();
            BRANCHES_FOLDER.mkdir();

            try {
                INDEX_FILE.createNewFile();
            } catch (IOException error) {
                System.exit(0);
            }
            try {
                headd.createNewFile();
            } catch (IOException error) {
                System.exit(0);
            }
            try {
                currentBranchh.createNewFile();
            } catch (IOException error) {
                System.exit(0);
            }

            Commit firstCommit = new Commit();
            firstCommit.saveCommit();
            String commitIDFirst = firstCommit.getID();
            Utils.writeContents(headd, commitIDFirst);
            Branches initial = new Branches(firstCommit);
            initial.saveBranch();
            Utils.writeContents(currentBranchh,
                    initial.getBranchName());
            Index startingIndex = new Index();
            startingIndex.saveIndex();

        }
    }

    /** Helper responsible for making a pointer / writing in
     * This is uniquely for writing hash IDs into a file.
     * Branches will share this
     * @param writeIn is write in hash.
     * @param idVar is the given hash ID location.*/
    public static void writeInHashIntoNewFile(String idVar,
                                               File writeIn) {
        if (!writeIn.exists()) {
            try {
                writeIn.createNewFile();
            } catch (IOException error) {
                System.exit(0);
            }
        }
        Utils.writeContents(writeIn,
                idVar);
    }


    public static void addCommand(String toBeAdded) {
        File addThis;

        if (Paths.get(toBeAdded).isAbsolute()) {
            addThis = new File(toBeAdded);
        } else {
            addThis = Utils.join(RepoFace.CWD, toBeAdded);
        }

        if (!addThis.exists()) {
            System.out.println("File does not exist.");
            System.exit(0);
        }

        boolean trueChange = currIndex.
                addtoIndex(addThis);

        if (trueChange) {
            currIndex.saveIndex();
        }
    }

    /** The commit command. Does not care about
     * current working directory materials. Only
     * pays attention to added index. Compare added
     * versus previous commit. Commit object will
     * always start as a duplicate of the previous
     * commit in the branch (head)
     * @param message is given mssg by user.*/

    public static void commitCommand(String message) {
        if (currIndex.isClear()) {
            System.out.println("No changes "
                    + "added to the commit.");
            System.exit(0);
        }
        if (message.equals("")) {
            System.out.println("Please enter "
                    + "a commit message.");
            System.exit(0);
        }
        Commit prevHead = readHeadFile();
        String prevID = prevHead.getID();
        TreeMap<String, String> currAddedIndex =
                currIndex.currAddedStage();
        TreeMap<String, String> currRemovedIndex =
                currIndex.currRemovedStage();
        TreeMap<String, String> prevCommitSnapshot =
                prevHead.getSnapshot();
        for (String fileName : currAddedIndex.keySet()) {
            if (!prevCommitSnapshot.containsKey(fileName)) {
                String addedHashVer =
                        currAddedIndex.get(fileName);
                prevCommitSnapshot.
                        put(fileName, addedHashVer);
            } else {
                String addedHash =
                        currAddedIndex.get(fileName);
                String prevHash =
                        prevCommitSnapshot.get(fileName);
                if (!addedHash.equals(prevHash)) {
                    prevCommitSnapshot.remove(fileName);
                    prevCommitSnapshot.put(fileName, addedHash);
                }
            }
        }
        for (String fileName : currRemovedIndex.keySet()) {
            if (prevCommitSnapshot.
                    containsKey(fileName)) {
                prevCommitSnapshot.
                        remove(fileName);
            }
        }
        currIndex.clear();
        currIndex.saveIndex();
        Commit newCommit =
                new Commit(message,
                        prevCommitSnapshot,
                        "", prevID);
        String newCommitID = newCommit.getID();
        writeInHashIntoNewFile(newCommitID, headd);
        String currBranchyName = readFileGetBranch();
        Branches updatedCurrent =
                new Branches(newCommit, currBranchyName);
        updatedCurrent.saveBranch();
        updatedCurrent.updateCurrentBranch();
        newCommit.saveCommit();
    }

    /** Read head file and return resulting string
     * which should be a commit ID.
     * Then find the commit object from the found ID.*/
    public static Commit readHeadFile() {
        String readID =
                Utils.readContentsAsString(headd);
        File findCommit =
                Utils.join(COMMITS_FOLDER, readID);
        Commit foundCommit =
                Utils.readObject(findCommit, Commit.class);
        return foundCommit;
    }

    /** Reads the CURRENT_BRANCH file which
     * contains a string of the current branch name.
     * This name is literal. Use this
     * name to search BRANCHES_FOLDER.
     * @return is name of branch returned.*/
    public static String readFileGetBranch() {
        return Utils.readContentsAsString(currentBranchh);
    }

    /**  Unstage the FILENAME if it is currently
     * staged for addition.
     * If the file is tracked in the current commit,
     * stage it for removal
     * and remove the file from the working directory
     * if the user has not
     * already done so (do not remove it unless it is
     * tracked in the current commit).*/

    public static void removeCommand(String fileName) {
        boolean changed = false;
        File pathToFile = Utils.join(CWD, fileName);
        try {
            if (!pathToFile.exists()) {
                pathToFile.createNewFile();
            }
        } catch (IOException error) {
            System.exit(0);
        }
        Blob fileToMakeBlob = new Blob(pathToFile);
        String newBlobID = fileToMakeBlob.blobID();
        String fileNameOrg =
                fileToMakeBlob.getSourceFileName();
        String currentBranchName =
                Utils.readContentsAsString(currentBranchh);
        File currBranchPath =
                Utils.join(BRANCHES_FOLDER, currentBranchName);
        Branches currBranchObject =
                Utils.readObject(currBranchPath, Branches.class);
        Commit currBranchLeadCommit =
                currBranchObject.getCurrCommit();
        Commit currHeadCommit = currBranchLeadCommit;
        TreeMap<String, String> mapOfHeadCommit =
                currHeadCommit.getSnapshot();
        TreeMap<String, String> currentAdded =
                currIndex.currAddedStage();
        TreeMap<String, String> currentRemoved =
                currIndex.currRemovedStage();
        boolean fileIsStaged =
                currentAdded.containsKey(fileNameOrg);
        boolean fileIsTrackedByHead =
                mapOfHeadCommit.containsKey(fileNameOrg);
        if (!fileIsStaged && !fileIsTrackedByHead) {
            System.out.println("No reason "
                    + "to remove the file.");
            System.exit(0);
        }
        if (fileIsStaged) {
            currentAdded.remove(fileNameOrg);
            changed = true;
        }
        if (fileIsTrackedByHead) {
            mapOfHeadCommit.remove(fileNameOrg);
            currentRemoved.put(fileNameOrg, newBlobID);
            Utils.restrictedDelete(fileNameOrg);
            changed = true;
        }
        if (changed) {
            currIndex.saveIndex();
        }
    }

    /**  Starting at the current head commit,
     * display information about each commit backwards
     * along the commit tree until the initial commit,
     * following the first parent commit links,
     * ignoring any second parents
     * found in merge commits.*/

    public static void logCommand() {
        Commit currHead = readHeadFile();
        Commit tempCurr = currHead;

        while (tempCurr != null) {
            Commit.printCommitObject(tempCurr);
            if (tempCurr.getParentLink() != null) {
                File findParent =
                        Utils.join(COMMITS_FOLDER,
                                tempCurr.getParentLink());
                tempCurr =
                        Utils.readObject(findParent,
                                Commit.class);
            } else {
                break;
            }

        }
    }


    /**  Like log, except displays information
     * about all commits ever made.
     * The order of the commits does not matter.
     * Hint: there is a useful method in gitlet.
     * Utils that will help you
     * iterate over files within a directory.*/
    public static void globalLogCommand() {

        List<String> eachCommitInDir =
                Utils.plainFilenamesIn(COMMITS_FOLDER);
        for (String commitUnique : eachCommitInDir) {
            Commit currCommit =
                    Commit.getCommitFromFile(commitUnique);
            Commit.printCommitObject(currCommit);
        }
    }

    /** Prints out the ids of all commits
     * that have the given commit message MSSGIN, one per line.
     * If there are multiple such commits,
     * it prints the ids out on separate lines.
     * The commit message is a single operand;
     * to indicate a multiword message,
     * put the operand in quotation marks,
     * as for the commit command above.*/

    public static void findCommand(String mssgIn) {
        boolean foundACommit = false;
        List<String> eachCommitInDirectory =
                Utils.plainFilenamesIn(COMMITS_FOLDER);

        for (String currID : eachCommitInDirectory) {
            Commit currentFromID =
                    Commit.getCommitFromFile(currID);
            String currMssg = currentFromID.getMssg();

            if (mssgIn.equals(currMssg)) {
                foundACommit = true;
                System.out.println(currentFromID.getID());
            }
        }
        if (!foundACommit) {
            System.out.println("Found no "
                    + "commit with that message.");
            System.exit(0);
        }
    }

    /** Displays what branches currently exist,
     * and marks the current branch with a *.
     * Also displays what files have been staged for
     * addition or removal. There is an empty line
     * between sections. Entries should be listed
     * in lexicographic order, using the Java
     * string-comparison order (the asterisk
     * doesn't count). */

    public static void statusCommand() {
        System.out.println("=== Branches ===");
        String currBranchName =
                Utils.readContentsAsString(currentBranchh);
        List<String> allBranchFileNames =
                Utils.plainFilenamesIn(BRANCHES_FOLDER);
        Collections.sort(allBranchFileNames);
        for (String branchName
                : allBranchFileNames) {
            File pathToBranch =
                    Utils.join(BRANCHES_FOLDER,
                            branchName);
            Branches actualBranchObject =
                    Utils.readObject(pathToBranch,
                            Branches.class);
            String actualBranchName =
                    actualBranchObject.getBranchName();
            if (actualBranchName.equals(currBranchName)) {
                System.out.println("*" + actualBranchName);
            } else {
                System.out.println(actualBranchName);
            }
        }
        System.out.println();
        System.out.println("=== "
                + "Staged Files ===");
        TreeMap<String, String> currAddedMap =
                currIndex.currAddedStage();
        List<String> willBeOrdered =
                new ArrayList<>();
        for (String fileLiteralName
                : currAddedMap.keySet()) {
            willBeOrdered.add(fileLiteralName);
        }
        willBeOrdered.sort(String::compareTo);
        for (String fileLiteralName : willBeOrdered) {
            System.out.println(fileLiteralName);
        }
        System.out.println();
        System.out.println("=== Removed Files ===");
        TreeMap<String, String> currRemovedMap =
                currIndex.currRemovedStage();
        List<String> toOrderRemoved =
                new ArrayList<>();
        for (String fileLiteralName
                : currRemovedMap.keySet()) {
            toOrderRemoved.add(fileLiteralName);
        }
        toOrderRemoved.sort(String::compareTo);
        for (String fileLiteralName
                : toOrderRemoved) {
            System.out.println(fileLiteralName);
        }
        System.out.println();
        System.out.println("=== Modifications Not Staged For Commit ===");
        System.out.println();
        System.out.println("=== Untracked Files ===");
        System.out.println();
    }

    /** Takes the version of the NAMEFILE as it exists in
     * the head commit,
     * the front of the current branch, and puts it in
     * the working directory,
     * overwriting the version of the file that's already
     * there if there is one.
     * The new version of the file is not staged.*/
    public static void checkoutCommandFILENAME(String nameFile) {
        Commit currHeadCommit = readHeadFile();
        TreeMap<String, String> currHeadSnapshot =
                currHeadCommit.getSnapshot();
        if (!currHeadSnapshot.containsKey(nameFile)) {
            System.out.println("File does not exist"
                    + " in that commit.");
            System.exit(0);
        } else {
            File checkIfExistsInCWD =
                    Utils.join(CWD.getPath(), nameFile);
            if (checkIfExistsInCWD.exists()) {
                Utils.restrictedDelete(
                        checkIfExistsInCWD);
            }
            String blobIDFromSnap =
                    currHeadSnapshot.get(nameFile);
            File blobLocation =
                    Blob.searchBlobFile(blobIDFromSnap);
            Blob readBlobFromLocation =
                    Blob.readBlobFromFile(blobLocation);
            byte[] rawContent =
                    readBlobFromLocation.getBlobContent();
            File overWrittenNew =
                    Utils.join(CWD.getPath(), nameFile);
            Utils.writeContents(overWrittenNew, rawContent);
        }
    }

    /** Takes the version of the FILENAME as it exists in the
     * commit with the given id (GIVEN ID! NOT JUST HEAD),
     * and puts it in the working directory, overwriting
     * the version of the file
     * that's already there if there is one.
     * The new version of the file is not staged.
     * @param userCommitHashID is given ID from user.
     * @param fileName is given name of file.*/
    public static void checkoutCommandCOMMITID(String fileName,
                                               String userCommitHashID) {
        List<String> commitCandidate =
                Utils.plainFilenamesIn(COMMITS_FOLDER);
        boolean matchingCommitFound = false;

        for (String candidateID : commitCandidate) {
            if (candidateID.startsWith(userCommitHashID)) {
                matchingCommitFound = true;
                Commit matchedCommit =
                        Commit.getCommitFromFile(candidateID);
                TreeMap<String, String> matchedCommitSnap =
                        matchedCommit.getSnapshot();
                if (!matchedCommitSnap.containsKey(fileName)) {
                    System.out.println("File does not "
                            + "exist in that commit.");
                    System.exit(0);
                } else {
                    File checkIfExistsInCWDagain =
                            Utils.join(CWD.getPath(), fileName);
                    if (checkIfExistsInCWDagain.exists()) {
                        Utils.restrictedDelete(checkIfExistsInCWDagain);
                    }
                    String blobIDFromSnapy =
                            matchedCommitSnap.get(fileName);
                    File blobLocationy =
                            Blob.searchBlobFile(blobIDFromSnapy);
                    Blob readBlobFromLocationy =
                            Blob.readBlobFromFile(blobLocationy);
                    byte[] rawContenty =
                            readBlobFromLocationy.getBlobContent();
                    File overWrittenNewFile =
                            Utils.join(CWD.getPath(), fileName);
                    Utils.writeContents(overWrittenNewFile, rawContenty);
                }
            }
        }
        if (!matchingCommitFound) {
            System.out.println("No commit with "
                    + "that id exists.");
            System.exit(0);
        }
    }

    /** Takes all files in the commit at the head of the branch
     * and puts them in the working directory, overwriting.
     * @param branchName is inputted branch name.*/
    public static void checkoutCommandBRANCHNAME(String branchName) {
        File wantedBranch = Utils.join(BRANCHES_FOLDER, branchName);
        if (!wantedBranch.exists()) {
            System.out.println("No such branch exists.");
            System.exit(0);
        }
        String currentBranchToCompare =
                Utils.readContentsAsString(currentBranchh);
        if (currentBranchToCompare.equals(branchName)) {
            System.out.println("No need to checkout "
                    + "the current branch.");
            System.exit(0);
        }
        Branches currentBranch = Branches.getBranchFromFile(
                                currentBranchToCompare);
        Commit currentBranchCommit = currentBranch.getCurrCommit();
        TreeMap<String, String> currentBranchSnapshot =
                currentBranchCommit.getSnapshot();
        Branches checkedOutBranch =
                Branches.getBranchFromFile(branchName);
        Commit checkOutBranchCommit =
                checkedOutBranch.getCurrCommit();
        TreeMap<String, String> checkOutCommitSnap =
                checkOutBranchCommit.getSnapshot();
        List<File> fetchedCurrCWD = existingCWDFiles();

        isolationFunction(branchName);

        for (File checkFile : fetchedCurrCWD) {
            if (currentBranchSnapshot.containsKey(checkFile.getName())
                    && !checkOutCommitSnap.containsKey(checkFile.getName())) {
                checkFile.delete();
            }
        }
        for (String fileOfCheckOutTreeMap : checkOutCommitSnap.keySet()) {
            String hashOfFile = checkOutCommitSnap.get(fileOfCheckOutTreeMap);
            File blobbyFound = Blob.searchBlobFile(hashOfFile);
            Blob foundBlob = Blob.readBlobFromFile(blobbyFound);
            Utils.writeContents(Utils.join(CWD, fileOfCheckOutTreeMap),
                    foundBlob.getBlobContent());
        }
        Utils.writeContents(headd, checkOutBranchCommit.getID());
        Utils.writeContents(currentBranchh, branchName);
        currIndex.clear();
        currIndex.saveIndex();
    }

    public static void isolationFunction(String branchName) {
        String currentBranchToCompare =
                Utils.readContentsAsString(currentBranchh);
        Branches currentBranch = Branches.getBranchFromFile(
                currentBranchToCompare);
        Commit currentBranchCommit = currentBranch.getCurrCommit();
        TreeMap<String, String> currentBranchSnapshot =
                currentBranchCommit.getSnapshot();
        Branches checkedOutBranch =
                Branches.getBranchFromFile(branchName);
        Commit checkOutBranchCommit =
                checkedOutBranch.getCurrCommit();
        TreeMap<String, String> checkOutCommitSnap =
                checkOutBranchCommit.getSnapshot();
        List<String> stringCurrCWD = Utils.plainFilenamesIn(CWD);
        for (String nameFile : stringCurrCWD) {
            File currentFile = Utils.join(CWD, nameFile);
            String currentFileName = currentFile.getName();
            Blob genBlobby = new Blob(currentFile);
            String genBlobbyID = genBlobby.blobID();
            boolean fileInCheckout = checkOutCommitSnap.
                    containsKey(currentFileName);
            boolean fileInCurrent = currentBranchSnapshot.
                    containsKey(currentFileName);
            if (fileInCurrent && fileInCheckout) {
                String checkOutHashID =
                        checkOutCommitSnap.get(currentFileName);
                String currentHashID =
                        currentBranchSnapshot.get(currentFileName);
                if (!genBlobbyID.equals(checkOutHashID)
                        && !genBlobbyID.equals(currentHashID)) {
                    untrackedError();
                }
            }
            if (fileInCheckout && !fileInCurrent && !genBlobbyID.
                    equals(checkOutCommitSnap.get(currentFileName))) {
                untrackedError();
            }
        }
    }

    /** Loops through and captures all
     * existing files in CWD.
     * @return is all CWD files in a list format.*/
    public static List<File> existingCWDFiles() {
        List<File> result = new ArrayList<>();
        List<String> filenames =
                Utils.plainFilenamesIn(CWD);

        for (String fileNamey : filenames) {
            File checkHere = Utils.join(CWD, fileNamey);
            result.add(checkHere);
        }
        return result;
    }


    /**  Creates a new branch with the given NAME,
     * and points it at the current head node. A branch is
     * nothing more than a name for a reference
     * (a SHA-1 identifier) to a commit node.
     * @param name is given branch name.*/
    public static void branchCommand(String name) {
        alternativeCheckIfExist(name);
        Commit currHead = readHeadFile();
        Branches newBranch =
                new Branches(currHead, name);
        newBranch.saveBranch();
    }

    /** ALTERNATIVE Helper function which checks if an existing
     * branch with the same name CHECKY Already exists.
     * Replace if later bugs found - seems to work as needed.
     * @param checky is given branch check.*/
    public static void alternativeCheckIfExist(String checky) {
        List<String> branchesInDirectory =
                Utils.plainFilenamesIn(BRANCHES_FOLDER);
        if (branchesInDirectory.contains(checky)) {
            System.out.println("A branch with that "
                    + "name already exists.");
            System.exit(0);
        }
    }

    /** Deletes the branch with the given REMOVENAME. This only means to delete
     * the pointer associated with the branch; it does not mean to delete
     * all commits that were created under the branch, or anything like that.
     * @param removeName is given name.*/
    public static void removeBranchCommand(String removeName) {
        checkIfBranchDoesNotExist(removeName);
        attemptingToRemoveCurrentBranch(removeName);

        File pathToBranch = Utils.join(BRANCHES_FOLDER,
                removeName);
        pathToBranch.delete();

    }

    /** Function checking if branch name even exists.
     * @param checkThis is inputted name.*/
    public static void checkIfBranchDoesNotExist(String checkThis) {
        List<String> branchesDir = Utils.plainFilenamesIn(BRANCHES_FOLDER);
        if (!branchesDir.contains(checkThis)) {
            System.out.println("A branch with that name does not exist.");
            System.exit(0);
        }
    }

    /** Check if trying to remove current branch.
     * @param checkThis is name we compare to current.*/
    public static void attemptingToRemoveCurrentBranch(String checkThis) {
        String currentBranchName =
                Utils.readContentsAsString(currentBranchh);
        if (currentBranchName.equals(checkThis)) {
            System.out.println("Cannot remove the current branch.");
            System.exit(0);
        }
    }

    /** Checks out all the files tracked by commit ID.
     * @param givenCommitID is given ID.*/
    public static void resetCommand(String givenCommitID) {
        List<String> commitCandidates =
                Utils.plainFilenamesIn(COMMITS_FOLDER);
        boolean matchingCommitFound = false;
        for (String checkCommitID : commitCandidates) {
            if (checkCommitID.startsWith(givenCommitID)) {
                matchingCommitFound = true;
                Commit getActualCommit =
                        Commit.getCommitFromFile(checkCommitID);
                TreeMap<String, String> trackedOfID =
                        getActualCommit.getSnapshot();
                TreeMap<String, String> headTracked =
                        readHeadFile().getSnapshot();
                for (String nameFile : Utils.plainFilenamesIn(CWD)) {
                    File currentFile = Utils.join(CWD, nameFile);
                    String currentFileName = currentFile.getName();
                    Blob genBlobby = new Blob(currentFile);
                    String genBlobbyID = genBlobby.blobID();
                    boolean fileInCheckout = trackedOfID.
                            containsKey(currentFileName);
                    boolean fileInCurrent = headTracked.
                            containsKey(currentFileName);
                    if (fileInCurrent && fileInCheckout) {
                        String checkOutHashID =
                                trackedOfID.get(currentFileName);
                        String currentHashID =
                                headTracked.get(currentFileName);
                        if (!genBlobbyID.equals(checkOutHashID)
                                && !genBlobbyID.equals(currentHashID)) {
                            untrackedError();
                        }
                    }
                    if (fileInCheckout && !fileInCurrent
                            && !genBlobbyID.equals(trackedOfID.
                            get(currentFileName))) {
                        untrackedError();
                    }
                }
                for (File checkFile : existingCWDFiles()) {
                    if (headTracked.containsKey(checkFile.getName())
                            && !trackedOfID.
                            containsKey(checkFile.getName())) {
                        checkFile.delete();
                    }
                }
                for (String fileOfCheckOutTreeMap : trackedOfID.keySet()) {
                    String hashOfFile = trackedOfID.
                            get(fileOfCheckOutTreeMap);
                    File blobbyFound = Blob.searchBlobFile(hashOfFile);
                    Blob foundBlob = Blob.readBlobFromFile(blobbyFound);
                    Utils.writeContents(Utils.join(CWD, fileOfCheckOutTreeMap),
                            foundBlob.getBlobContent());
                }
                resetShenanigans(checkCommitID);
            }
        }
        noCommitIDError(matchingCommitFound);
    }

    /** Helper function for reset, taking care of saving.
     * @param checkCommitID is given ID from for loop in reset.*/

    public static void resetShenanigans(String checkCommitID) {
        Utils.writeContents(headd, checkCommitID);
        String currBranchName = readFileGetBranch();
        File branchNode = Utils.join(BRANCHES_FOLDER, currBranchName);
        Commit getActualCommit =
                Commit.getCommitFromFile(checkCommitID);
        Branches newWithCommit =
                new Branches(getActualCommit, currBranchName);
        Utils.writeObject(branchNode, newWithCommit);
        currIndex.clear();
        currIndex.saveIndex();
    }

    /** Separate function for error printing.
     * @param truth will determine if error is present. */
    public static void noCommitIDError(boolean truth) {
        if (!truth) {
            System.out.println("No commit with that id exists.");
            System.exit(0);
        }
    }

    /** Untracked error function called if untrack detected.*/
    public static void untrackedError() {
        System.out.println("There is an untracked file "
                + "in the way;" + " delete it, or add and "
                + "commit it first.");
        System.exit(0);
    }

    /** Merges files from the given branch into the current branch.
     * Perform split point checks.
     * If surpassing errors and checks the function.
     * @param branchName is inputted name.*/
    public static void mergeCommand(String branchName) {
        checkIfAnyStagedAdditionsOrRemovals();
        checkIfBranchDoesNotExist(branchName);
        checkIfTryingToMergeWithSelf(branchName);
        File pathToOtherBranch = Utils.join(BRANCHES_FOLDER,
                branchName);
        Branches otherBranchObject = Utils.readObject(pathToOtherBranch,
                Branches.class);
        Commit otherBranchCommit = otherBranchObject.getCurrCommit();
        Commit currBranchCommit = readHeadFile();
        List<String> stringCurrCWD = Utils.plainFilenamesIn(CWD);
        for (String nameFile : stringCurrCWD) {
            File currentFile = Utils.join(CWD, nameFile);
            String currentFileName = currentFile.getName();
            Blob genBlobby = new Blob(currentFile);
            String genBlobbyID = genBlobby.blobID();
            boolean fileInCheckout = otherBranchCommit.
                    getSnapshot().containsKey(currentFileName);
            boolean fileInCurrent = currBranchCommit.
                    getSnapshot().containsKey(currentFileName);
            if (fileInCurrent && fileInCheckout) {
                String checkOutHashID = otherBranchCommit.
                        getSnapshot().get(currentFileName);
                String currentHashID = currBranchCommit.
                        getSnapshot().get(currentFileName);
                if (!genBlobbyID.equals(checkOutHashID)
                        && !genBlobbyID.equals(currentHashID)) {
                    System.out.println("There is an untracked file in "
                            + "the way; delete it, "
                            + "or add and commit it first.");
                    System.exit(0);
                }
            }
            if (fileInCheckout && !fileInCurrent
                    && !genBlobbyID.equals(otherBranchCommit.
                    getSnapshot().get(currentFileName))) {
                System.out.println("There is an untracked file in the way; "
                        + "delete it, or add and commit it first.");
                System.exit(0);
            }
        }
        minimizedDepth = 1000;
        minimizingHash = "";
        Commit splitPoint =
                getSplitPoint(currBranchCommit,
                otherBranchCommit);
        if (splitPoint.getID().equals(otherBranchCommit.
                getID())) {
            System.out.println("Given branch is "
                    + "an ancestor of the current branch.");
            System.exit(0);
        }
        if (splitPoint.equals(currBranchCommit)) {
            RepoFace.checkoutCommandBRANCHNAME(branchName);
            System.out.println("Current branch fast-forwarded.");
            System.exit(0);
        }
        mergeHelper1(branchName, splitPoint);
    }

    public static void isolaterOne(Commit givenBranchCommit,
                                   Commit currBranchCommit,
                                   TreeMap<String, String>
                                           splitTreeMap,
                                   TreeMap<String, String>
                                           modifyME,
                                   boolean conflictExists,
                                   String givenBranchName,
                                   Commit splitPT) {

        for (String givenFile : givenBranchCommit.
                getSnapshot().keySet()) {
            if (currBranchCommit.
                    getSnapshot().containsKey(givenFile)) {
                String givenHash = givenBranchCommit.
                                getSnapshot().get(givenFile);
                String currentHash = currBranchCommit.
                                getSnapshot().get(givenFile);
                if (!givenHash.equals(currentHash) && !currentHash.
                        equals(splitTreeMap.get(givenFile)) && !givenHash.
                        equals(splitTreeMap.get(givenFile))) {
                    modifyME = wC(givenFile, sR2(currentHash),
                            sR1(givenHash));
                    conflictExists = true;
                }
            }
            if (!currBranchCommit.getSnapshot().containsKey(givenFile)) {
                if (splitTreeMap.containsKey(givenFile)) {
                    if (!splitTreeMap.get(givenFile).equals(
                            givenBranchCommit.getSnapshot().get(givenFile))) {
                        String hashOfGiven =
                                givenBranchCommit.getSnapshot().get(givenFile);
                        Blob givenBlob =
                                takeHashAndFindBlobObject(hashOfGiven);
                        byte[] contentsOfGivenBlob = givenBlob.getBlobContent();
                        String deserializedGiven =
                                new String(contentsOfGivenBlob,
                                        StandardCharsets.UTF_8);
                        modifyME = wC(givenFile, "", deserializedGiven);
                        conflictExists = true;
                    }
                }
            }
            if (!splitTreeMap.containsKey(givenFile)) {
                if (givenBranchCommit.getSnapshot().containsKey(givenFile)
                        && currBranchCommit.
                        getSnapshot().containsKey(givenFile)) {
                    String giventHash =
                            givenBranchCommit.getSnapshot().get(givenFile);
                    String currtHash =
                            currBranchCommit.getSnapshot().get(givenFile);
                    if (!giventHash.equals(currtHash)) {
                        modifyME = wC(givenFile, sR2(currtHash),
                                sR1(giventHash));
                        conflictExists = true;
                    }
                }
            }
        }
        contOfMergeHelper1(modifyME, givenBranchName, conflictExists, splitPT);
    }

    /** Function for merge after error checking.
     * @param givenBranchName is inputted branch.
     * @param splitPT is the split point found.*/
    public static void mergeHelper1(String givenBranchName,
                                    Commit splitPT) {
        File pathToOtherBranch = Utils.join(BRANCHES_FOLDER,
                        givenBranchName);
        Branches otherBranchObject =
                Utils.readObject(pathToOtherBranch,
                        Branches.class);

        Commit givenBranchCommit =
                otherBranchObject.getCurrCommit();
        Commit currBranchCommit = readHeadFile();
        TreeMap<String, String> splitTreeMap =
                splitPT.getSnapshot();

        Commit currHead = readHeadFile();
        TreeMap<String, String> modifyME =
                currHead.getSnapshot();
        boolean conflictExists = false;

        mergeHelper2(givenBranchName, splitPT);

        isolaterOne(givenBranchCommit, currBranchCommit,
                        splitTreeMap, modifyME, conflictExists,
                givenBranchName, splitPT);
    }

    public static void contOfMergeHelper1(TreeMap<String, String> modifyME,
                                          String givenBranchName,
                                          boolean conflictExists,
                                          Commit sPT) {
        Commit currBranchCommit = readHeadFile();
        File pathToOtherBranch = Utils.join(BRANCHES_FOLDER,
                givenBranchName);
        Branches otherBranchObject =
                Utils.readObject(pathToOtherBranch, Branches.class);
        Commit givenBranchCommit =
                otherBranchObject.getCurrCommit();
        moreHelp(conflictExists, currBranchCommit,
                givenBranchCommit, sPT, modifyME, givenBranchName);
    }

    public static String sR1(String givenHash) {
        Blob givenBlob = takeHashAndFindBlobObject(givenHash);
        byte[] contentsOfGivenBlob = givenBlob.getBlobContent();
        String deserializedGiven = new String(contentsOfGivenBlob,
                StandardCharsets.UTF_8);
        return deserializedGiven;
    }

    public static String sR2(String currentHash) {
        Blob currentBlob = takeHashAndFindBlobObject(currentHash);
        byte[] contentsOfCurrentBlob = currentBlob.getBlobContent();
        String deserializedCurrent =
                new String(contentsOfCurrentBlob,
                        StandardCharsets.UTF_8);
        return deserializedCurrent;
    }

    public static void moreHelp(boolean conflictExists, Commit currBranchCommit,
                                Commit givenBranchCommit, Commit sPT,
                                TreeMap<String, String> modifyME,
                                String givenBranchName) {
        for (String currentFile : currBranchCommit.getSnapshot().keySet()) {
            if (givenBranchCommit.getSnapshot().containsKey(currentFile)) {
                String givenHash =
                        givenBranchCommit.getSnapshot().get(currentFile);
                String currentHash =
                        currBranchCommit.getSnapshot().get(currentFile);
                if (!givenHash.equals(currentHash) && !currentHash.equals(sPT.
                        getSnapshot().get(currentFile))
                        && !givenHash.equals(sPT.
                        getSnapshot().get(currentFile))) {
                    modifyME = wC(currentFile, sR2(currentHash),
                            sR1(givenHash));
                    conflictExists = true;
                }
            }
            if (!givenBranchCommit.getSnapshot().containsKey(currentFile)) {
                if (sPT.getSnapshot().containsKey(currentFile)) {
                    if (!sPT.getSnapshot().get(currentFile).equals(
                            currBranchCommit.getSnapshot().get(currentFile))) {
                        String hashOfCurrent =
                                currBranchCommit.getSnapshot().get(currentFile);
                        Blob givenBlob =
                                takeHashAndFindBlobObject(hashOfCurrent);
                        byte[] contentsOfGivenBlob = givenBlob.getBlobContent();
                        String deserializedGiven =
                                new String(contentsOfGivenBlob,
                                        StandardCharsets.UTF_8);
                        modifyME = wC(currentFile, deserializedGiven, "");
                        conflictExists = true;
                    }
                }
            }
            if (!sPT.getSnapshot().containsKey(currentFile)) {
                TreeMap<String, String> gTM = givenBranchCommit.getSnapshot();
                if (gTM.containsKey(currentFile)) {
                    String giventHash =
                            givenBranchCommit.getSnapshot().get(currentFile);
                    String currtHash =
                            currBranchCommit.getSnapshot().get(currentFile);
                    if (!giventHash.equals(currtHash)) {
                        String deserializedGivenn = sR1(giventHash);
                        String deserializedCurrentt = sR2(currtHash);
                        modifyME = wC(currentFile,
                                deserializedCurrentt, deserializedGivenn);
                        conflictExists = true;
                    }
                }
            }
        }
        mergeHelper3(conflictExists,
                givenBranchName, givenBranchCommit, modifyME);
    }

    /** Helper function for merge after all error checking.
     * @param conflictExists is boolean.
     * @param givenBranchCommit is commit.
     * @param givenBranchName is name string.
     * @param modifyME is treemap.*/
    public static void mergeHelper3(boolean conflictExists,
                                    String givenBranchName,
                                    Commit givenBranchCommit,
                                    TreeMap<String, String> modifyME) {
        String currentBranchName = Utils.readContentsAsString(currentBranchh);
        RepoFace.subCommitForm("Merged " + givenBranchName
                        + " into " + currentBranchName + ".",
                givenBranchCommit.getID(), modifyME);

        if (conflictExists) {
            System.out.println("Encountered a merge conflict.");
        }
    }

    /** Merge helper function. Splits tasks.
     * @param givenBranchName is given branch name from user.
     * @param splitPT is commit representing split point.*/
    public static void mergeHelper2(String givenBranchName,
                                    Commit splitPT) {
        File pathToOtherBranch = Utils.join(BRANCHES_FOLDER,
                        givenBranchName);
        Branches otherBranchObject = Utils.readObject(pathToOtherBranch,
                        Branches.class);
        Commit givenBranchCommit = otherBranchObject.
                getCurrCommit();
        Commit currBranchCommit = readHeadFile();
        TreeMap<String, String> splitTreeMap =
                splitPT.getSnapshot();
        for (String givenIterFiles : givenBranchCommit.
                getSnapshot().keySet()) {
            if (currBranchCommit.getSnapshot().
                    containsKey(givenIterFiles)
                    && givenBranchCommit.
                    getSnapshot().containsKey(givenIterFiles)
                    && splitTreeMap.
                    containsKey(givenIterFiles)) {
                String currHash =
                        currBranchCommit.
                                getSnapshot().get(givenIterFiles);
                String givenhash =
                        givenBranchCommit.
                                getSnapshot().get(givenIterFiles);
                String splitHash =
                        splitTreeMap.get(givenIterFiles);
                if (!givenhash.equals(splitHash)
                        && currHash.equals(splitHash)) {
                    RepoFace.checkoutCommandCOMMITID(givenIterFiles,
                            givenBranchCommit.getID());
                    RepoFace.addCommand(givenIterFiles);
                }
            }
            if (!splitTreeMap.containsKey(givenIterFiles)
                    && !currBranchCommit.
                    getSnapshot().containsKey(givenIterFiles)) {
                RepoFace.checkoutCommandCOMMITID(givenIterFiles,
                        givenBranchCommit.getID());
                RepoFace.addCommand(givenIterFiles);
            }
        }
        for (String splitFileIter : splitTreeMap.keySet()) {
            if (currBranchCommit.getSnapshot().
                    containsKey(splitFileIter)) {
                String currHash =
                        currBranchCommit.getSnapshot().
                                get(splitFileIter);
                String splitHash =
                        splitTreeMap.get(splitFileIter);
                if (currHash.equals(splitHash)
                        && !givenBranchCommit.
                        getSnapshot().
                        containsKey(splitFileIter)) {
                    RepoFace.removeCommand(splitFileIter);
                }
            }
        }
    }

    /** Function which writes in the conflict styles contents.
     * @param fN is file name as string.
     * @param cC is contents of curr branch.
     * @param cG is contents of inputted branch
     * @return will be modified tree map for new commit.*/
    public static TreeMap<String, String> wC(String fN, String cC, String cG) {

        String writeThisIn = "<<<<<<< HEAD\n" + cC
                + "=======\n" + cG + ">>>>>>>\n";

        File pathwayToFile = Utils.join(CWD, fN);
        Utils.writeContents(pathwayToFile, writeThisIn);
        Commit prevHead = readHeadFile();
        TreeMap<String, String> prevCommitTreeToModify = prevHead.getSnapshot();
        String shaOneUsingString = Utils.sha1(writeThisIn);
        prevCommitTreeToModify.remove(fN);
        prevCommitTreeToModify.put(fN, shaOneUsingString);

        return prevCommitTreeToModify;
    }

    /** Function which commits but has two parents.
     * @param message is message.
     * @param secondAncestor is hash of 2nd parent.
     * @param prevMap is previous commit tree map.*/
    public static void subCommitForm(String message, String secondAncestor,
                                     TreeMap<String, String> prevMap) {
        Commit prevHead =
                readHeadFile();
        String prevID =
                prevHead.getID();
        TreeMap<String, String> currAddedIndex =
                currIndex.currAddedStage();
        TreeMap<String, String> prevCommitSnapshot =
                prevMap;

        for (String fileName : currAddedIndex.keySet()) {

            if (!prevCommitSnapshot.containsKey(fileName)) {
                String addedHashVer = currAddedIndex.get(fileName);
                prevCommitSnapshot.put(fileName, addedHashVer);

            } else {
                String addedHash = currAddedIndex.get(fileName);
                String prevHash = prevCommitSnapshot.get(fileName);
                if (!addedHash.equals(prevHash)) {
                    prevCommitSnapshot.remove(fileName);
                    prevCommitSnapshot.put(fileName, addedHash);
                }
            }
        }
        currIndex.clear();
        currIndex.saveIndex();
        Commit newCommit = new Commit(message,
                prevMap, secondAncestor, prevID);
        String newCommitID = newCommit.getID();
        writeInHashIntoNewFile(newCommitID, headd);
        String currBranchyName = readFileGetBranch();
        Branches updatedCurrent = new Branches(newCommit,
                currBranchyName);
        updatedCurrent.saveBranch();
        updatedCurrent.updateCurrentBranch();
        newCommit.saveCommit();
    }


    public static Commit getSplitPoint(Commit currBranchCommit,
                                       Commit passedBranchCommit) {
        if (passedBranchCommit.getParentLink().equals(
                currBranchCommit.getID())) {
            return currBranchCommit;
        }

        String givenCommitID = passedBranchCommit.getID();

        if (currBranchCommit.getSecondParent().equals("")) {
            fromCurrentDepthMaker(1,
                    currBranchCommit.getParentLink());
        } else {
            fromCurrentDepthMaker(1,
                    currBranchCommit.getParentLink());
            fromCurrentDepthMaker(1,
                    currBranchCommit.getSecondParent());
        }
        givenAndDepthSearcher(givenCommitID);
        File findMinFromStaticHash =
                Utils.join(COMMITS_FOLDER, minimizingHash);

        return Utils.readObject(findMinFromStaticHash,
                Commit.class);
    }


    /** Function which goes down it's links and looks at the
     * depths corresponding in the depthInCurrent tree.
     * If the hash is the new minimum, update it and depth.
     * THIS IS RECURSIVE.
     * @param givenCommitID represents current Commit*/
    public static void givenAndDepthSearcher(String givenCommitID) {
        Commit actualObjectFromID = Commit.getCommitFromFile(givenCommitID);
        if (actualObjectFromID.getParentLink() == null
                && actualObjectFromID.getSecondParent().equals("")) {
            if (depthInCurrent.containsKey(givenCommitID)) {
                int checkDepth = depthInCurrent.get(givenCommitID);
                if (checkDepth < minimizedDepth) {
                    minimizedDepth = checkDepth;
                    minimizingHash = givenCommitID;
                }
            }
        } else {
            if (depthInCurrent.containsKey(givenCommitID)) {
                int checkDepthy = depthInCurrent.get(givenCommitID);
                if (checkDepthy < minimizedDepth) {
                    minimizedDepth = checkDepthy;
                    minimizingHash = givenCommitID;
                }
            }
            if (actualObjectFromID.getSecondParent().equals("")) {
                givenAndDepthSearcher(actualObjectFromID.getParentLink());
            } else {
                givenAndDepthSearcher(actualObjectFromID.getParentLink());
                givenAndDepthSearcher(actualObjectFromID.getSecondParent());
            }
        }
    }

    /** Function which recursively goes from the head commit down to
     * initial (aka no parents at all) For each commit hash,
     * it puts in the DEPTH. Called with 0 and increments with each call.
     * @param depth represents depth.
     * @param currentOnID represents the current Commit ID.*/

    public static void fromCurrentDepthMaker(int depth, String currentOnID) {
        Commit actualCommitObject = Commit.getCommitFromFile(currentOnID);
        if (actualCommitObject.getParentLink() == null
                && actualCommitObject.getSecondParent().equals("")) {
            depthInCurrent.put(currentOnID, depth);
            return;
        } else {
            depthInCurrent.put(currentOnID, depth);
            depth += 1;
            if (actualCommitObject.getSecondParent().equals("")) {
                fromCurrentDepthMaker(depth,
                        actualCommitObject.getParentLink());
            } else {
                fromCurrentDepthMaker(depth,
                        actualCommitObject.getParentLink());
                fromCurrentDepthMaker(depth,
                        actualCommitObject.getSecondParent());
            }
        }
    }


    /** For merge: If there are staged additions or removals present,
     print the error message "You have uncommitted changes." and exit.*/
    public static void checkIfAnyStagedAdditionsOrRemovals() {
        boolean checkClean = currIndex.isClear();
        if (!checkClean) {
            System.out.println("You have uncommitted changes.");
            System.exit(0);
        }
    }

    /** If attempting to merge a branch BRANCHNAMEIN with itself,
     print the error message Cannot merge a branch with itself.*/
    public static void checkIfTryingToMergeWithSelf(String branchNameIn) {
        String currentBranchName = readFileGetBranch();
        File currBranchPath = Utils.join(BRANCHES_FOLDER, currentBranchName);
        Branches actualCurrObject = Utils.readObject(currBranchPath,
                Branches.class);
        String nameOfCurrBranch = actualCurrObject.getBranchName();

        if (nameOfCurrBranch.equals(branchNameIn)) {
            System.out.println("Cannot merge a branch with itself.");
            System.exit(0);
        }
    }

    /** Helper function to find blob object.
     * @param blobHash is hash.
     * @return is blob object from hash.*/
    public static Blob takeHashAndFindBlobObject(String blobHash) {
        File formPath = Utils.join(BLOBS_FOLDER, blobHash);
        Blob foundBlob = Utils.readObject(formPath, Blob.class);
        return foundBlob;

    }

    /** Access function to return head file.
     * @return is file of head.*/
    public static File headdReturner() {
        return headd;
    }

    /** Access function for current branch file.
     * @return is file for current branch name inside.*/
    public static File cBReturner() {
        return currentBranchh;
    }
}
