<p align="center">
  <img src="https://www.corda.net/wp-content/uploads/2016/11/fg005_corda_b.png" alt="Corda" width="500">
</p>

# Voting CorDapp

This project uses the bootcamp template to continue to build the BlockChain Voting System. 
Our CorDapp is intended with four modules:
* Registration module
* Issuing Ballots for the voter
* Transfer of Ballots to the candidates
* Tallying of results


Testing for the Token State, Contract and Flow are defined in `src/test/java/java_bootcamp`.
Other tests should be quite similar to these pre-defined ones.

## Set up

1. Download and install Oracle JDK 8 JVM (minimum supported version 8u131)
2. Download and install IntelliJ Community Edition (supported versions 2017.x and 2018.x)
3. Download the bootcamp-cordapp repository:

       git clone https://github.com/MubashirullahD/corda-blockchain-voting
       
4. Open IntelliJ. From the splash screen, click `Import Project`, select the `bootcamp—
cordapp` folder and click `Open`
5. Select `Import project from external model > Gradle > Next > Finish`
6. Click `File > Project Structure…` and select the Project SDK (Oracle JDK 8, 8u131+)

    i. Add a new SDK if required by clicking `New…` and selecting the JDK’s folder

7. Open the `Project` view by clicking `View > Tool Windows > Project`
8. Run the test in `src/test/java/java_bootcamp/ProjectImportedOKTest.java`. It should pass!

## Links to useful resources

This project contains example state, contract and flow implementations:

* `src/main/java/java_examples/ArtState`
* `src/main/java/java_examples/ArtContract`
* `src/main/java/java_examples/ArtTransferFlowInitiator`
* `src/main/java/java_examples/ArtTransferFlowResponder`

There are also several web resources that you will likely find useful for this
bootcamp:

* Key Concepts docs (`docs.corda.net/key-concepts.html`)
* API docs (`docs.corda.net/api-index.html`)
* Cheat sheet (`docs.corda.net/cheat-sheet.html`)
* Sample CorDapps (`www.corda.net/samples`)
* Stack Overflow (`www.stackoverflow.com/questions/tagged/corda`)

## Typical order

The following are examples that have nothing to do with the project.
The CorDapp usually has three parts for each module.
Here is a crashcourse for issung Tokens on the ledger:

### The TokenState

States define shared facts on the ledger. Our state, TokenState, will define a
token. It has the following structure:

    -------------------
    |                 |
    |   TokenState    |
    |                 |
    |   - issuer      |
    |   - owner       |
    |   - amount      |
    |                 |
    -------------------

### The TokenContract

Contracts govern how states evolve over time. Our contract, TokenContract,
will define how TokenStates evolve. It will only allow the following type of
TokenState transaction:

    -------------------------------------------------------------------------------------
    |                                                                                   |
    |    - - - - - - - - - -                                     -------------------    |
    |                                              ▲             |                 |    |
    |    |                 |                       | -►          |   TokenState    |    |
    |            NO             -------------------     -►       |                 |    |
    |    |                 |    |      Issue command       -►    |   - issuer      |    |
    |          INPUTS           |     signed by issuer     -►    |   - owner       |    |
    |    |                 |    -------------------     -►       |   - amount > 0  |    |
    |                                              | -►          |                 |    |
    |    - - - - - - - - - -                       ▼             -------------------    |
    |                                                                                   |
    -------------------------------------------------------------------------------------

              No inputs             One issue command,                One output,
                                 issuer is a required signer       amount is positive

To do so, TokenContract will impose the following constraints on transactions
involving TokenStates:

* The transaction has no input states
* The transaction has one output state
* The transaction has one command
* The output state is a TokenState
* The output state has a positive amount
* The command is an Issue command
* The command lists the TokenState's issuer as a required signer

### The TokenIssueFlow

Flows automate the process of updating the ledger. Our flow, TokenIssueFlow, will
automate the following steps:

            Issuer                  Owner                  Notary
              |                       |                       |
       Chooses a notary
              |                       |                       |
        Starts building
         a transaction                |                       |
              |
        Adds the output               |                       |
          TokenState
              |                       |                       |
           Adds the
         Issue command                |                       |
              |
         Verifies the                 |                       |
          transaction
              |                       |                       |
          Signs the
         transaction                  |                       |
              |
              |----------------------------------------------►|
              |                       |                       |
                                                         Notarises the
              |                       |                   transaction
                                                              |
              |◀----------------------------------------------|
              |                       |                       |
         Records the
         transaction                  |                       |
              |
              |----------------------►|                       |
                                      |
              |                  Records the                  |
                                 transaction
              |                       |                       |
              ▼                       ▼                       ▼

## Running our CorDapp

Normally, you'd interact with a CorDapp via a client or webserver. So we can
focus on our CorDapp, we'll be running it via the node shell instead.

Once you've finished the CorDapp's code, run it with the following steps:

* Build a test network of nodes by opening a terminal window at the root of
  your project and running the following command:

    * Windows:   `gradlew.bat deployNodesJava`
    * macOS:     `./gradlew deployNodesJava`

* Start the nodes by running the following command:

    * Windows:   `build\nodes\runnodes.bat`
    * macOS:     `build/nodes/runnodes`

* Open the nodes are started, go to the terminal of Party A (not the notary!)
  and run the following command to issue 99 tokens to Party B:

    `flow start TokenIssueFlow owner: PartyB, amount: 99`

* You can now see the tokens in the vaults of Party A and Party B (but not 
  Party C!) by running the following command in their respective terminals:

    `run vaultQuery contractStateType: java_bootcamp.TokenState`

## Updating for offline use

* Run the `gatherDependencies` Gradle task from the root of the project to 
  gather all the CorDapp's dependencies in `lib/dependencies`
* Update `gradle/wrapper/gradle-wrapper.properties` to point to a local Gradle 
  distribution (e.g. 
  `distributionUrl=gradle-4.4.1-all.zip`)
* In `build.gradle`, under both `repositories` blocks, comment out any 
  repositories other than `flatDir { ... }`
  
## Relevant to this project
* VoteState
* VoteContract
* BallotIssueFlow
* VoteIssueFlow
* BallotTransferFlow

## Explanation
The voteState has a duel purpose. It is assigned to both the candidates 
being voted for and the voter.

The voteContract defines three commandTypes, two which are very similar
except for where the voter is given a value of 1, where as the candidate
starts off with 0.

The BallotIssueFlow gives the voter their one token to cast with.
The VoteIssueFlow initializes the candidate with 0 votes.
The BallotTransferFlow increments the vote of the candidate

The Registration module is planned to create nodes that will be added to
the build.gradle file. The approach is that the election commission;
who has access to the registered voters database; will query to get the
CNIC, city and Fingerprint of each voter and create a node with these 
values.

Each polling station will be given their own build.gradle file that they
will use to launch the nodes on the network. The purpose of this approach
is to elliminate server dependency and have a more robust system.

Additional measures can be taken to make sure no one tampers with the build.gradle
file by keeping a log of generated hashes with the file.

The paper based voting system is still more secure than this. The trust of officers
need to be taken into account again for the system to function fairly. There is an 
instance in this system where if two ballots are issued to a voter, they will cast twice.
Perhaps a query can be done in the end to figure out transactions from the same nodes.
