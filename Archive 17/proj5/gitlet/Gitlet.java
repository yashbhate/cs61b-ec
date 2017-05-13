package gitlet;

import java.io.*;
import java.util.*;

/**
 * Represents a Git directory
 */
public class Gitlet {

    private static class Commit {

        public final String parentSha;
        public final Date timeStamp;
        public final String comment;
        public final List<FileVersion> files = new ArrayList<>();

        public Commit(String parentSha, Date timeStamp, String comment, Collection<FileVersion> files) {
            this.parentSha = parentSha;
            this.timeStamp = timeStamp;
            this.comment = comment;
            this.files.addAll(files);
        }

        public String sha() {
            Object[] data = new Object[3 + files.size()];
            int i = 0;
            data[i++] = parentSha;
            data[i++] = timeStamp.toString();
            data[i++] = comment;
            for (FileVersion file : files) {
                data[i++] = file.toString();
            }
            return Utils.sha1(data);
        }

        private static class FileVersion implements Serializable {
            private final String canonicalName;
            private final String contents;

            public FileVersion(String canonicalName, String contents) {
                this.canonicalName = canonicalName;
                this.contents = contents;
            }

            @Override
            public String toString() {
                return "FileVersion{" +
                        "canonicalName='" + canonicalName + '\'' +
                        ", contents='" + contents + '\'' +
                        '}';
            }
        }
    }

    public final Map<String, String> branches = new HashMap<>();
    public final Map<String, Commit> allCommits = new HashMap<>();

    public final Set<String> statusAdded = new HashSet<>();
    public final Set<String> statusRemoved = new HashSet<>();

    /** True iff this gitlet is a new instance (needs init) */
    private boolean isNew;
    private final File directory;
    /** The .gitlet directory. */
    private final File dotGitlet;
    private final File commitsFile;
    private final File statusFile;

    public Gitlet(File directory) {
        if (directory.isFile() || !directory.exists()) {
            throw new IllegalStateException("cannot init inside file or not exists");
        }
        this.directory = directory;
        dotGitlet = new File(directory, ".gitlet");
        commitsFile = new File(dotGitlet, "commits.gitlet");
        statusFile = new File(dotGitlet, "status.gitlet");
        if (dotGitlet.isFile()) {
            throw new IllegalStateException(".gitlet is file");
        }
        this.isNew = !dotGitlet.exists();
    }

    private void readCommits() {
        Map<String, String> commits;

        if (!commitsFile.isFile()) {
            return;
        }

        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(commitsFile))) {

            commits = (Map<String, String>) in.readObject();

        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        for (Map.Entry<String, String> e : commits.entrySet()) {
            if (e.getKey().equals(e.getValue())) {
                allCommits.put(e.getKey(), null);
            }
            else {
                branches.put(e.getKey(), e.getValue());
            }
        }
    }
    private void writeCommits() {

        Map<String, String> commits = new HashMap<>();
        commits.putAll(branches);
        allCommits.forEach((s, c) -> commits.put(s, s));

        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(commitsFile))) {

            out.writeObject(commits);
            out.flush();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void readStatus() {
        if (!statusFile.exists() || statusFile.isDirectory())
            return;
        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(statusFile))) {

            Set<String> added = (Set<String>) in.readObject();
            Set<String> removed = (Set<String>) in.readObject();

            statusAdded.addAll(added);
            statusRemoved.removeAll(added);
            statusRemoved.addAll(removed);
            statusAdded.removeAll(removed);

        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
    private void writeStatus() {
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(statusFile))) {

            out.writeObject(statusAdded);
            out.writeObject(statusRemoved);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void status() {
        readStatus();
        readCommits();

        System.out.println("=== Branches ===");
        branches.keySet().stream().sorted().forEach(System.out::println);
        System.out.println();

        System.out.println("=== Staged Files ===");
        statusAdded.stream().sorted().forEach(System.out::println);
        System.out.println();

        System.out.println("=== Removed Files ===");
        statusRemoved.stream().sorted().forEach(System.out::println);
        System.out.println();

        System.out.println("=== Modifications Not Staged For Commit ===");
        //
        System.out.println();

        System.out.println("=== Untracked Files ===");
        //
        System.out.println();
    }

    public void add(String filename) {
        readStatus();

        File added = new File(directory, filename);
        if (!added.isFile()) {
            System.out.print("File does not exist.");
            return;
        }

        statusAdded.add(filename);

        writeStatus();
    }

    public void rm(String filename) {
        readStatus();

        File removed = new File(directory, filename);
        if (!(statusAdded.contains(filename) || false /* head commit */)) {
            System.out.println("No reason to remove the file.");
            return;
        }

        // rough
        statusAdded.remove(filename);
        //statusRemoved.add(filename);

        writeStatus();
    }

    public void init() {
        if (!isNew) {
            System.out.println("A gitlet version-control system already exists in the current directory.");
            return;
        }
        dotGitlet.mkdir();

        Commit initial = new Commit(null, new Date(), "initial commit", Collections.emptySet());
        String sha = initial.sha();

        branches.put("master", sha);
        allCommits.put(sha, initial);

        writeCommits();
    }

    public void writeData() {

    }
}
