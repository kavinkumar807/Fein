import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;


/**
 * Main class for Fein Programming Language Interpreter
 */
public class Fein {
    private static final Interpreter interpreter = new Interpreter();

    // static variables to check whether program had any error
    static boolean hadError = false;
    static boolean hadRunTimeError = false;

    public static void main(String[] args) throws IOException {
        if(args.length > 1){
            System.out.println("Usage: jFein [script]");
            System.exit(64);
        } else if (args.length == 1) {
            runFile(args[0]);
        } else{
            runPrompt();
        }
    }

    /**
     * Method to execute Fein program when file path is given
     *
     * @param path String
     *
     * @throws IOException IOException
     */
    private static void runFile(String path) throws IOException{
        byte[] bytes = Files.readAllBytes(Paths.get(path));
        run(new String(bytes, Charset.defaultCharset()));

        // Indicate an error in the exit code
        if(hadError) System.exit(65);
        if(hadRunTimeError) System.exit(70);
    }

    /**
     * Method to line by line execution of Fein program using interactive prompt (REPL)
     *
     * @throws IOException IOException
     */
    private static void runPrompt() throws IOException{
        InputStreamReader input = new InputStreamReader(System.in);
        BufferedReader reader = new BufferedReader(input);

        for(;;){
            System.out.println("Fein <::> ");
            String line = reader.readLine();
            if (line == null) break;
            run(line);
            hadError = false;
        }
    }

    /**
     * Method to scan tokens and execute Fein program
     *
     * @param source String
     */
    private static void run(String source) {
        Scanner scanner = new Scanner(source);
        List<Token> tokens = scanner.scanTokens();
        Parser parser = new Parser(tokens);
        List<Stmt> statements = parser.parse();

        // Stop if there was a syntax error.
        if(hadError) return;

        Resolver resolver = new Resolver(interpreter);
        resolver.resolve(statements);

        // Stop if there was a resolution error.
        if(hadError) return;

        interpreter.interpret(statements);
       // System.out.println(new AstPrinter().print(expression));

//        for(Token token : tokens){
//            System.out.println(token);
//            // for line debugging
//            //System.out.println("[line " + token.line +"]" + " " + token);
//        }
    }

    /**
     * Helper method to call report with error line and message
     *
     * @param line int
     * @param message String
     */
    static void error(int line, String message){
        report(line, "", message);
    }

    /**
     * Helper method to report error
     *
     * @param line int
     * @param where String
     * @param message String
     */
    private static void report(int line, String where, String message){
        System.out.println("[line " + line +"] Error" + where + ": " + message);
        hadError = true;
    }

    /**
     * Method to handle parse errors
     *
     * @param token Token
     * @param message String
     */
    static void error(Token token, String message){
        if(token.type == TokenType.EOF){
            report(token.line, " at end", message);
        } else {
            report(token.line, " at '" + token.lexeme + "'", message);
        }
    }

    static void runtimeError(RuntimeError error){
        System.err.println(error.getMessage() + "\n[line " + error.token.line + "]");
        hadRunTimeError = true;
    }
}
