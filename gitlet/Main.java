package gitlet;

/** Driver class for Gitlet, the tiny stupid version-control system.
 *  @author Katrina Sharonin
 */
public class Main {

    /** Main usage: We will rely on three main classes:
     * REPOFACE, BLOB, and COMMIT.
     * These classes will represent interacting components. Main will
     * call proper classes through REPOFACE methods, which will
     * call on BLOB and COMMIT classes/objects.
     * @param args is input from user.*/
    public static void main(String... args) {
        if (args.length == 0) {
            noCommandsMessage();
        } else {
            switch (args[0]) {
            case "init" -> RepoFace.initCommand();
            case "add" -> addCaller(args[1], args);
            case "commit" -> commitCaller(args[1], args);
            case "rm" -> removeCaller(args[1], args);
            case "log" -> logCaller(args);
            case "global-log" -> globalLogCaller(args);
            case "find" -> findCaller(args);
            case "status" -> statusCaller(args);
            case "checkout" -> checkoutCaller(args);
            case "branch" -> {
                if (cPA(2, args) && dGFE()) {
                    RepoFace.branchCommand(args[1]);
                }
            }
            case "rm-branch" -> {
                if (cPA(2, args) && dGFE()) {
                    RepoFace.removeBranchCommand(args[1]);
                }
            }
            case "reset" -> {
                if (cPA(2, args) && dGFE()) {
                    RepoFace.resetCommand(args[1]);
                }
            }
            case "merge" -> {
                if (cPA(2, args) && dGFE()) {
                    RepoFace.mergeCommand(args[1]);
                }
            }
            default -> noCommandExists();
            }
        }
        System.exit(0);
    }

    /** Function separator & error trier.
     * @param argsInputIndex is small part.
     * @param args is whole string input.*/
    public static void addCaller(String argsInputIndex, String... args) {
        if (cPA(2, args) && dGFE()) {
            RepoFace.addCommand(argsInputIndex);
        }
    }

    /** Commit caller like above.
     * @param args is whole input.
     * @param argsInputIndex is small subpart.*/
    public static void commitCaller(String argsInputIndex, String... args) {
        if (cPA(2, args) && dGFE()) {
            RepoFace.commitCommand(argsInputIndex);
        }
    }

    /** Remove caller like above.
     * @param args is whole string.
     * @param argsInputIndex is subpart.*/
    public static void removeCaller(String argsInputIndex, String... args) {
        if (cPA(2, args) && dGFE()) {
            RepoFace.removeCommand(argsInputIndex);
        }
    }

    /** Log caller like above.
     * @param args is whole string.*/
    public static void logCaller(String... args) {
        if (cPA(1, args) && dGFE()) {
            RepoFace.logCommand();
        }
    }

    /** Global Log caller like above.
     * @param args is whole string.*/
    public static void globalLogCaller(String... args) {
        if (cPA(1, args) && dGFE()) {
            RepoFace.globalLogCommand();
        }
    }

    /** Find caller like above.
     * @param args is whole string.*/
    public static void findCaller(String... args) {
        if (dGFE()) {
            String message = args[1];
            RepoFace.findCommand(message);
        }
    }

    /** Status caller like above.
     * @param args is whole string.*/
    public static void statusCaller(String... args) {
        if (cPA(1, args) && dGFE()) {
            RepoFace.statusCommand();
        }
    }

    /** Status caller like above.
     * @param args is whole string.*/
    public static void checkoutCaller(String... args) {
        if (!RepoFace.GITLET_FOLDER.exists()) {
            System.out.println("Not in an "
                    + "initialized Gitlet directory.");
        } else if (args.length != 4 && args.length != 3
                && args.length != 2) {
            System.out.println("Incorrect operands.");
        } else {
            if (args.length == 4) {
                if (!args[2].equals("--")) {
                    System.out.println("Incorrect operands.");
                } else {
                    RepoFace.checkoutCommandCOMMITID(args[3], args[1]);
                }
            } else if (args.length == 3) {
                if (!args[1].equals("--")) {
                    System.out.println("Incorrect operands.");
                } else {
                    RepoFace.checkoutCommandFILENAME(args[2]);
                }
            } else {
                RepoFace.checkoutCommandBRANCHNAME(args[1]);
            }
        }
    }

    /** Message if no given command is entered.*/
    static void noCommandsMessage() {
        System.out.println("Please enter a command.");
    }

    /** Message if entered command is unrecognized.*/
    static void noCommandExists() {
        System.out.println("No command with that name exists.");
    }

    /** Return a boolean checking if a .gitlet has been initialized. Called
     * for every command besides init. */
    static boolean dGFE() {
        if (!RepoFace.GITLET_FOLDER.exists()) {
            System.out.println("Not in an initialized Gitlet directory.");
            return false;
        } else {
            return true;
        }
    }

    /** Evaluate if there is a proper amount of arguments
     * passed for command.
     * CHECKLEN will be compared to ARGINPUT SIZE.
     * @return is boolean if proper number present.*/

    static boolean cPA(int checkLen, String... argInput) {
        if (checkLen == argInput.length) {
            return true;
        } else {
            System.out.println("Incorrect operands.");
            return false;
        }
    }

}
