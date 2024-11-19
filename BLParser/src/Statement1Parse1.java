import components.queue.Queue;
import components.simplereader.SimpleReader;
import components.simplereader.SimpleReader1L;
import components.simplewriter.SimpleWriter;
import components.simplewriter.SimpleWriter1L;
import components.statement.Statement;
import components.statement.Statement1;
import components.utilities.Reporter;
import components.utilities.Tokenizer;

/**
 * Layered implementation of secondary methods {@code parse} and
 * {@code parseBlock} for {@code Statement}.
 *
 * @author Shyam Sai Bethina and Yihone Chu
 *
 */
public final class Statement1Parse1 extends Statement1 {

    /*
     * Private members --------------------------------------------------------
     */

    /**
     * Converts {@code c} into the corresponding {@code Condition}.
     *
     * @param c
     *            the condition to convert
     * @return the {@code Condition} corresponding to {@code c}
     * @requires [c is a condition string]
     * @ensures parseCondition = [Condition corresponding to c]
     */
    private static Condition parseCondition(String c) {
        assert c != null : "Violation of: c is not null";
        assert Tokenizer
                .isCondition(c) : "Violation of: c is a condition string";

        /*
         * Returns the condition that it finds through valueOf and replaces all
         * instances of "-" to "_"
         */
        return Condition.valueOf(c.replace('-', '_').toUpperCase());

    }

    /**
     * Parses an IF or IF_ELSE statement from {@code tokens} into {@code s}.
     *
     * @param tokens
     *            the input tokens
     * @param s
     *            the parsed statement
     * @replaces s
     * @updates tokens
     * @requires <pre>
     * [<"IF"> is a prefix of tokens]  and
     *  [<Tokenizer.END_OF_INPUT> is a suffix of tokens]
     * </pre>
     * @ensures <pre>
     * if [an if string is a proper prefix of #tokens] then
     *  s = [IF or IF_ELSE Statement corresponding to if string at start of #tokens]  and
     *  #tokens = [if string at start of #tokens] * tokens
     * else
     *  [reports an appropriate error message to the console and terminates client]
     * </pre>
     */
    private static void parseIf(Queue<String> tokens, Statement s) {
        assert tokens != null : "Violation of: tokens is not null";
        assert s != null : "Violation of: s is not null";
        assert tokens.length() > 0 && tokens.front().equals("IF") : ""
                + "Violation of: <\"IF\"> is proper prefix of tokens";

        //Removes IF
        String ifString = tokens.dequeue();

        //Checks if the condition is valid after IF and parses it
        Reporter.assertElseFatalError(Tokenizer.isCondition(tokens.front()),
                "Error: IF condition not valid");
        Condition ifCondition = parseCondition(tokens.dequeue());

        //Check if THEN is after the condition and dequeues it
        Reporter.assertElseFatalError(tokens.front().equals("THEN"),
                "Error: Expected THEN");
        String thenString = tokens.dequeue();

        //Creates an empty statement and parses the block into itself
        Statement ifStatement = s.newInstance();
        ifStatement.parseBlock(tokens);

        //Reports if the end of the block is not ELSE or END
        Reporter.assertElseFatalError(
                tokens.front().equals("ELSE") || tokens.front().equals("END"),
                "Error: Expected ELSE or END");

        /*
         * If the end of the block is marked by ELSE, then it parses the else
         * block
         */
        if (tokens.front().equals("ELSE")) {

            String elseString = tokens.dequeue();
            //Creates an empty statement and parses the else block into itself
            Statement elseStatement = s.newInstance();
            elseStatement.parseBlock(tokens);

            /*
             * Assembles IfElse block to s using the condition and the two
             * statements.
             */
            s.assembleIfElse(ifCondition, ifStatement, elseStatement);

            /*
             * Reports if the end of the block is not marked by END, and
             * dequeues the string
             */
            Reporter.assertElseFatalError(tokens.front().equals("END"),
                    "Error: Expected END");
            String endString = tokens.dequeue();

        } else {
            /*
             * Assembles If block to s using the condition and ifStatement
             */
            s.assembleIf(ifCondition, ifStatement);

            /*
             * Reports if the end of the block is not marked by END, and
             * dequeues the string
             */
            Reporter.assertElseFatalError(tokens.front().equals("END"),
                    "Error: Expected END");
            String endString = tokens.dequeue();
        }

        /*
         * Checks if the end string of the entire block is equal to IF
         */
        String endIfString = tokens.dequeue();
        Reporter.assertElseFatalError(endIfString.equals("IF"),
                "Error: Expected IF");

    }

    /**
     * Parses a WHILE statement from {@code tokens} into {@code s}.
     *
     * @param tokens
     *            the input tokens
     * @param s
     *            the parsed statement
     * @replaces s
     * @updates tokens
     * @requires <pre>
     * [<"WHILE"> is a prefix of tokens]  and
     *  [<Tokenizer.END_OF_INPUT> is a suffix of tokens]
     * </pre>
     * @ensures <pre>
     * if [a while string is a proper prefix of #tokens] then
     *  s = [WHILE Statement corresponding to while string at start of #tokens]  and
     *  #tokens = [while string at start of #tokens] * tokens
     * else
     *  [reports an appropriate error message to the console and terminates client]
     * </pre>
     */
    private static void parseWhile(Queue<String> tokens, Statement s) {
        assert tokens != null : "Violation of: tokens is not null";
        assert s != null : "Violation of: s is not null";
        assert tokens.length() > 0 && tokens.front().equals("WHILE") : ""
                + "Violation of: <\"WHILE\"> is proper prefix of tokens";

        tokens.dequeue(); //Removes WHILE
        Reporter.assertElseFatalError(Tokenizer.isCondition(tokens.front()),
                "Error: While condition not valid");

        //This is the condition of the statement and parses it
        String condition = tokens.dequeue();
        Condition con = parseCondition(condition);

        //Reports if DO is not present after the condition and dequeues it
        Reporter.assertElseFatalError(tokens.front().equals("DO"),
                "Error: Expected DO");

        String doString = tokens.dequeue(); //Remove DO

        Statement whileStatement = s.newInstance();
        //Creates an empty statement and parses the block into itself
        whileStatement.parseBlock(tokens);
        //Assembles the while statement to s
        s.assembleWhile(con, whileStatement);

        /*
         * Reports if the END of the while statement is not marked by END, and
         * dequeues it
         */
        Reporter.assertElseFatalError(tokens.front().equals("END"),
                "Error: Expected END, found: " + "\"" + tokens.front() + "\"");

        String endWhile = tokens.dequeue();

        /*
         * Reports if the END of the while statement is not ended by WHILE, and
         * dequeues it
         */
        Reporter.assertElseFatalError(tokens.front().equals("WHILE"),
                "Error: Does not contain While after END");

        String whileString = tokens.dequeue();

    }

    /**
     * Parses a CALL statement from {@code tokens} into {@code s}.
     *
     * @param tokens
     *            the input tokens
     * @param s
     *            the parsed statement
     * @replaces s
     * @updates tokens
     * @requires [identifier string is a proper prefix of tokens]
     * @ensures <pre>
     * s =
     *   [CALL Statement corresponding to identifier string at start of #tokens]  and
     *  #tokens = [identifier string at start of #tokens] * tokens
     * </pre>
     */
    private static void parseCall(Queue<String> tokens, Statement s) {
        assert tokens != null : "Violation of: tokens is not null";
        assert s != null : "Violation of: s is not null";
        assert tokens.length() > 0
                && Tokenizer.isIdentifier(tokens.front()) : ""
                        + "Violation of: identifier string is proper prefix of tokens";

        //Dequeues the first token and assembles the call out of that token
        String call = tokens.dequeue();
        s.assembleCall(call);
    }

    /*
     * Constructors -----------------------------------------------------------
     */

    /**
     * No-argument constructor.
     */
    public Statement1Parse1() {
        super();
    }

    /*
     * Public methods ---------------------------------------------------------
     */

    @Override
    public void parse(Queue<String> tokens) {
        assert tokens != null : "Violation of: tokens is not null";
        assert tokens.length() > 0 : ""
                + "Violation of: Tokenizer.END_OF_INPUT is a suffix of tokens";

        Reporter.assertElseFatalError(
                tokens.front().equals("IF") || tokens.front().equals("WHILE")
                        || Tokenizer.isIdentifier(tokens.front()),
                "Error: IF, WHILE, or valid Identifier not found");

        /*
         * Uses the correct parse function based on the type of token at the
         * beginning of the queue
         */
        if (tokens.front().equals("IF")) {
            parseIf(tokens, this);
        } else if (tokens.front().equals("WHILE")) {
            parseWhile(tokens, this);
        } else {
            parseCall(tokens, this);
        }

    }

    @Override
    public void parseBlock(Queue<String> tokens) {
        assert tokens != null : "Violation of: tokens is not null";
        assert tokens.length() > 0 : ""
                + "Violation of: Tokenizer.END_OF_INPUT is a suffix of tokens";

        Statement answer = this.newInstance();
        int i = 0;
        /*
         * Parses the tokens and adds them into this while the front of the
         * tokens is not END, ELSE or at the end of the input
         */
        while (!tokens.front().equals("END") && !tokens.front().equals("ELSE")
                && !tokens.front().equals(Tokenizer.END_OF_INPUT)) {
            answer.parse(tokens);
            this.addToBlock(i, answer);
            i++;
        }
    }

    /*
     * Main test method -------------------------------------------------------
     */

    /**
     * Main method.
     *
     * @param args
     *            the command line arguments
     */
    public static void main(String[] args) {
        SimpleReader in = new SimpleReader1L();
        SimpleWriter out = new SimpleWriter1L();
        /*
         * Get input file name
         */
        out.print("Enter valid BL statement(s) file name: ");
        String fileName = in.nextLine();
        /*
         * Parse input file
         */
        out.println("*** Parsing input file ***");
        Statement s = new Statement1Parse1();
        SimpleReader file = new SimpleReader1L(fileName);
        Queue<String> tokens = Tokenizer.tokens(file);
        file.close();
        s.parse(tokens); // replace with parseBlock to test other method
        /*
         * Pretty print the statement(s)
         */
        out.println("*** Pretty print of parsed statement(s) ***");
        s.prettyPrint(out, 0);

        in.close();
        out.close();
    }

}
