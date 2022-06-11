# Gitlet Design Document
Author: Katrina Sharonin

## This project was written from scratch for the UC Berkeley course CS61B; not all parts complete in design doc

-------

## 1. Classes and Data Structures

**Main.java**

- The main class which handles user commands as the string of arguments `args` is passed
in. 
- Always initiates a repository by calling on the `RepoFace`. Similar to Lab 12, 
it should be able to catch and try cases for inputted commands.
- General error catching based on input -> i.e. `"Please enter a valid command"`

Static:
String `command` -> uses `args[0]`

Instance: 
`RepoFace` instance

**Blob.java**

- Class representing file contents
- Ultimately need a hashcode to distinguish between files
- SHA-1 hash will be generated based purely on contents of file

Static:

Instance: 
File `source` = source of content
String `ID` = store ID of our blob
byte[] `byteVer` = store byte version aka serialized

**RepoFace.java**

- Class representing the repository
- Key purpose is to hold interactions between blobs & commits & trees through functions
- Key functions: `add()` `commit()`, `merge()`, `log()`, etc

Static:
File `workingDirectory`
File `gitletDirectory`
Additional directory files for navigation

Instance:
Passed in `args` values called into functions


**Commit.java**

- Class representing commit node
- Must contain information of: log message, time stamp, maps of name blob references, parent reference
- Can also contain a second parent reference for merges
- Therefore: SHA-1 ID should take in: log message, time stamp, date, tree map (these are all unique components)
- All commits originate from the default commit made by init
- Link commits through parent links

Static:

Instance: 
String `ID` -> ID of the given commit 
String `parentID` -> hash of parent, essentially a "pointer"

**Index.java**

- Class hosting our staging areas, also known as states of addition and removed
- Two TreeMap<String, String> will be maintained to map name <--> contents relationships
- These need to be passable / storable in commits


## 2. Algorithms

(Broken by class)

**Main.java**

- `main(String args...)`: initialize new repository instance WITHOUT creating .gitlet directory
  * Must exit with Exit Code 0
  * After error checking, begins switch and calls accordingly 
  

- `checkLength`: check if the length of `args` matches the required length of the command 
i.e. `commit` requires a file and message -> can't accept a length of 0 -> `Incorrect Operands`


- `checkPresence`: check if args has anything -> if not, print need to enter a command

**RepoFace.java** - key handling class

- `initCommand`
  * User input: `"init"`
  * INITIATE `.gitlet` directory (children will be blob directory, commit directory, staging file, etc)
  * Create an initial commit -> call `commitCommand` with proper parameters
  * Create the first branch titled `master` and maintain `HEAD` pointer to this first commit (write contents in the head.txt file)
  * All commits in all repositories will trace back to this commit


- `addCommand`
  * Take a file -> "blobify" contents -> create a mapping between name and content ID
  * Each file will be distinguished by applying the SHA-1 hash on the content of the file
  * Relationship is added to repository index -> store in addition persistence file
  * Therefore requires serializable objects that can be saved back and forth
  * Already staged file -> must overwrite contents (search tree map for matching name ?) - clearly not the same yet must understand to replace
  * If there is an identical version (hashes the same of blob) in current commit -> do not state it to be added
  * Remove from the staging area if already present (matching hashes)
  * If in remove tree -> blob added -> remove from "remove" stage tree 


- `commitCommand`
  * Clear the addition tree -> set Tree map to a new empty tree map (disposal of old by garbage collection)
  * Moves tree from addition area  (`addition tree map`) to commit tree storage (commit will have this tree mapping references)
  * Default: the creation of a node will be copy of the previous commit 
  * Will keep versions as are and not update until pulled from added tree
  * Should not be destructive of actual directory
  * Maintain chain of nodes through hashes aka the `commitID`
  * Ignore any changes to blobs after (i.e. same hash maintained)
  * Move HEAD pointer -> update `HEAD` file 

Incomplete function decs:

- `rmCommand`
- `logCommand`
- `global-logCommand`
- `findCommand`
- `statusCommand`
- `checkoutCommand`
- `branchCommand`
- `rm-branchCommand`
- `resetCommand`
- `mergeCommand`



## 3. Persistence

Planned strategy:

* All files are broken into their contents and names
* "Blobs" holds all contents which are used for hashing
* File names to contents are mapped using tree map structure
* Must appropriately re-write contents everytime changes such as add occur
