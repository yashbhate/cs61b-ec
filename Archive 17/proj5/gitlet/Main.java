package gitlet;

import java.io.File;

/** Driver class for Gitlet, the tiny stupid version-control system.
 *  @author You!
 */
public class Main {

    /** Usage: java gitlet.Main ARGS, where ARGS contains
     *  <COMMAND> <OPERAND> .... */
    public static void main(String... args) {
        // FILL THIS IN

        File dir = new File(System.getProperty("user.dir"));
        Gitlet gitlet = new Gitlet(dir);

        if (args.length == 0) {
            System.out.println("Please enter a command.");
            return;
        }

        switch (args[0]) {
            case "init":
                gitlet.init();
                return;
            case "add":
                if (args.length != 2) {
                    System.out.println("Incorrect operands.");
                    return;
                }
                gitlet.add(args[1]);
                return;
            case "rm":
                if (args.length != 2) {
                    System.out.println("Incorrect operands.");
                    return;
                }
                gitlet.rm(args[1]);
                return;
            case "status":
                gitlet.status();
                return;
            case "commit":
            case "find":
            case "checkout":
            case "branch":
            case "rm-branch":
            case "reset":
            case "merge":
                // todo (all commands that take args).
                System.out.println("Incorrect operands.");
                return;
            default:
                System.out.println("No command with that name exists.");
                return;
        }
    }

}
