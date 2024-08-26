import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Class for parser
 */
public class Parser {
    private static class ParseError extends RuntimeException {}
    private final List<Token> tokens;
    private int current = 0;

    /**
     * Algorithm used here is Recursive Descent Parsing
     * GCC, V8 (the JavaScript VM in Chrome), Roslyn (the C# compiler written in C#)
     * and many other heavyweight production language implementations use recursive descent
     * Production rules for parser
     * expression     → assignment ;
     * assignment     → IDENTIFIER "=" assignment
     *                 | logic_or ;
     * logic_or       → logic_and ( "or" logic_and )* ;
     * logic_and      → equality ( "and" equality )* ;
     * equality       → comparison ( ( "!=" | "==" ) comparison )* ;
     * comparison     → term ( ( ">" | ">=" | "<" | "<=" ) term )* ;
     * term           → factor ( ( "-" | "+" ) factor )* ;
     * factor         → unary ( ( "/" | "*" ) unary )* ;
     * unary          → ( "!" | "-" ) unary
     *                | primary ;
     * primary        → NUMBER | STRING | "true" | "false" | "nil"
     *                | "(" expression ")" | IDENTIFIER ;
     *
     * @param tokens List<Tokens>
     */
    Parser(List<Token> tokens){
        this.tokens = tokens;
    }

    /**
     * Method to parse expression
     *
     * @return Expr
     */
    List<Stmt> parse() {
        List<Stmt> statements = new ArrayList<>();
        while(!isAtEnd()){
            statements.add(declaration());
        }

        return statements;
    }

    /**
     * Grammar for statements
     * program        → declaration* EOF ;
     * declaration    → varDecl
     *                | statement ;
     * statement      → exprStmt
     *                | ifStmt
     *                | printStmt
     *                | whileStmt
     *                | forStmt
     *                | block ;
     * ifStmt         → "if" "(" expression ")" statement
     *                ( "else" statement )? ;
     * whileStmt      → "while" "(" expression ")" statement ;
     * forStmt        → "for" "(" ( varDecl | exprStmt | ";" )
     *                  expression? ";"
     *                  expression? ")" statement ;
     * block          → "{" declaration * "}" ;
     * exprStmt       → expression ";" ;
     * printStmt      → "print" expression ";" ;
     * Method to parse statement
     *
     */
    private Stmt statement() {
        if(match(TokenType.FOR)) return forStatement();
        if(match(TokenType.IF)) return ifStatement();
        if(match(TokenType.PRINT)) return printStatement();
        if(match(TokenType.WHILE)) return whileStatement();
        if(match(TokenType.LEFT_BRACE)) return new Stmt.Block(block());

        return expressionStatement();
    }

    /**
     * Method to parse the if statement
     * ifStmt         → "if" "(" expression ")" statement
     *                ( "else" statement )? ;
     *
     * @return Stmt
     */
    private Stmt ifStatement(){
        consume(TokenType.LEFT_PAREN, "Expect '(' after 'if'.");
        Expr condition = expression();
        consume(TokenType.RIGHT_PAREN, "Expect ')' after if condition.");

        Stmt thenBranch = statement();
        Stmt elseBranch = null;
        if(match(TokenType.ELSE)){
            elseBranch = statement();
        }

        return new Stmt.If(condition, thenBranch, elseBranch);
    }

    /**
     * Method to parse block statement
     *
     * @return List<Stmt>
     */
    private List<Stmt> block(){
        List<Stmt> statements = new ArrayList<>();

        while(!check(TokenType.RIGHT_BRACE) && !isAtEnd()){
            statements.add(declaration());
        }

        consume(TokenType.RIGHT_BRACE, "Expect '}' after block.");
        return statements;
    }

    /**
     * Method to parse print statement
     * printStmt      → "print" expression ";" ;
     * @return Stmt
     */
    private Stmt printStatement() {
        Expr value = expression();
        consume(TokenType.SEMICOLON, "Expect ';' after value.");
        return new Stmt.Print(value);
    }

    /**
     * Method to parse expression statement
     * exprStmt       → expression ";" ;
     *
     * @return Stmt
     */
    private Stmt expressionStatement() {
        Expr expr = expression();
        consume(TokenType.SEMICOLON, "Expect ';' after expression.");
        return new Stmt.Expression(expr);
    }

    /**
     * Method to parse while statement
     * whileStmt      → "while" "(" expression ")" statement ;
     *
     *
     * @return Stmt
     */
    private Stmt whileStatement(){
        consume(TokenType.LEFT_PAREN, "Expect '(' after 'while'.");
        Expr condition = expression();
        consume(TokenType.RIGHT_PAREN, "Expect ')' after condition.");
        Stmt body = statement();

        return new Stmt.While(condition, body);
    }

    /**
     * Method to parse for statement by DESUGARING using while
     * forStmt        → "for" "(" ( varDecl | exprStmt | ";" )
     *                  expression? ";"
     *                  expression? ")" statement ;
     *
     * @return Stmt
     */
    private Stmt forStatement(){
        consume(TokenType.LEFT_PAREN, "Expect '(' after 'for'.");

        Stmt initializer;
        if(match(TokenType.SEMICOLON)){
            initializer = null;
        } else if(match(TokenType.VAR)){
            initializer = varDeclaration();
        } else {
            initializer = expressionStatement();
        }

        Expr condition = null;
        if(!check(TokenType.SEMICOLON)) {
            condition = expression();
        }
        consume(TokenType.SEMICOLON, "Expect ';' after loop condition.");

        Expr increment = null;
        if(!check(TokenType.RIGHT_PAREN)){
            increment = expression();
        }

        consume(TokenType.RIGHT_PAREN, "Expect ')' after for clauses.");

        Stmt body = statement();

        if(increment != null){
            body = new Stmt.Block(Arrays.asList(body, new Stmt.Expression(increment)));
        }

        if(condition == null) condition = new Expr.Literal(true);
        body = new Stmt.While(condition, body);

        if(initializer != null){
            body = new Stmt.Block(Arrays.asList(initializer, body));
        }

        return body;
    }



    /**
     * Method parse expression rule
     * expression     → assignment ;
     * @return Expr
     */
    private Expr expression(){
        return assignment();
    }

    /**
     * Method to parse assignment expressions
     * assignment     → IDENTIFIER "=" assignment
     *                 | logic_or ;
     * @return Expr
     */
    private Expr assignment(){
        Expr expr = or();

        if(match(TokenType.EQUAL)) {
            Token equals = previous();
            Expr value = assignment();


            if (expr instanceof Expr.Variable) {
                Token name = ((Expr.Variable) expr).name;
                return new Expr.Assign(name, value);
            }

            error(equals, "Invalid assignment target.");
        }

        return expr;
    }

    /**
     * Method to parse 'or' expression
     * logic_or       → logic_and ( "or" logic_and )* ;
     *
     * @return Expr
     */
    private Expr or(){
        Expr expr = and();

        while(match(TokenType.OR)){
            Token operator = previous();
            Expr right = and();
            expr = new Expr.Logical(expr, operator, right);
        }

        return expr;
    }

    /**
     * Method to parse 'and' expression
     *
     * @return Expr
     */
    private Expr and(){
        Expr expr = equality();

        while(match(TokenType.AND)) {
            Token operator = previous();
            Expr right = equality();
            expr = new Expr.Logical(expr, operator, right);
        }

        return expr;
    }

    /**
     * Method to parse declaration
     * declaration    → varDecl
     *                | statement ;
     *
     * @return Stmt
     */
    private Stmt declaration()
    {
        try{
            if(match(TokenType.VAR)) return varDeclaration();

            return statement();
        }catch (ParseError error){
            synchronize();
            return null;
        }
    }

    /**
     * Method to parse var declaration
     * varDecl        → "var" IDENTIFIER ( "=" expression )? ";" ;
     * @return Stmt
     */
    private Stmt varDeclaration() {
        Token name = consume(TokenType.IDENTIFIER, "Expect variable name.");

        Expr initializer = null;
        if(match(TokenType.EQUAL)){
            initializer = expression();
        }

        consume(TokenType.SEMICOLON, "Expect ';' after variable declaration.");
        return new Stmt.Var(name, initializer);
    }


    /**
     * Method to parse equality rule
     * equality → comparison ( ( "!=" | "==" ) comparison )* ;
     *
     * @return Expr
     */
    private Expr equality(){
        Expr expr = comparison();

        while(match(TokenType.BANG_EQUAL, TokenType.EQUAL_EQUAL)){
            Token operator = previous();
            Expr right = comparison();
            expr = new Expr.Binary(expr, operator, right);
        }

        return expr;
    }

    /**
     * Method to parse comparison rule
     * comparison → term ( ( ">" | ">=" | "<" | "<=" ) term )* ;
     * @return Expr
     */
    private Expr comparison(){
        Expr expr = term();

        while(match(TokenType.GREATER, TokenType.GREATER_EQUAL, TokenType.LESS, TokenType.LESS_EQUAL)){
            Token operator = previous();
            Expr right = term();
            expr = new Expr.Binary(expr, operator, right);
        }

        return expr;
    }

    /**
     * Method to parse term rule
     * term → factor ( ( "-" | "+" ) factor )* ;
     * @return Expr
     */
    private Expr term(){
        Expr expr = factor();

        while(match(TokenType.MINUS, TokenType.PLUS)){
            Token operator = previous();
            Expr right = factor();
            expr = new Expr.Binary(expr, operator, right);
        }
        return expr;
    }

    /**
     * Method to parse factor rule
     * factor → unary ( ( "/" | "*" ) unary )* ;
     * @return Expr
     */
    private Expr factor(){
        Expr expr = unary();

        while(match(TokenType.SLASH, TokenType.STAR)){
            Token operator = previous();
            Expr right = unary();
            expr = new Expr.Binary(expr, operator, right);
        }
        return expr;
    }

    /**
     * Method to parse unary rule
     * unary → ( "!" | "-" ) unary | primary ;
     * @return Expr
     */
    private Expr unary(){
        if(match(TokenType.BANG, TokenType.MINUS)){
            Token operator = previous();
            Expr right = unary();
            return new Expr.Unary(operator, right);
        }
        return primary();
    }

    /**
     * Method to parse primary rule
     * primary → NUMBER | STRING | "true" | "false" | "nil"
     *                | "(" expression ")" | IDENTIFIER
     * @return Expr
     */
    private Expr primary() {
        if (match(TokenType.FALSE)) return new Expr.Literal(false);
        if(match(TokenType.TRUE)) return new Expr.Literal(true);
        if(match(TokenType.NIL)) return new Expr.Literal(null);

        if(match(TokenType.NUMBER, TokenType.STRING)){
            return new Expr.Literal(previous().literal);
        }

        if(match(TokenType.IDENTIFIER)){
            return new Expr.Variable(previous());
        }

        if(match(TokenType.LEFT_PAREN)){
            Expr expr = expression();
            consume(TokenType.RIGHT_PAREN, "Expect ')' after expression.");
            return new Expr.Grouping(expr);
        }

        throw error(peek(), "Expect expression.");
    }

    /**
     * Method to match token type
     *
     * @param types TokenType
     *
     * @return boolean
     */
    private boolean match(TokenType... types){
        for(TokenType type : types){
            if(check(type)){
                advance();
                return true;
            }
        }
        return false;
    }

    /**
     * Method to consume
     *
     * @param type TokenType
     * @param message String
     *
     * @return Token
     */
    private Token consume(TokenType type, String message){
        if(check(type)) return advance();

        throw error(peek(), message);
    }

    /**
     * Method to throw parse error
     *
     * @param token Token
     * @param message String
     * @return ParseError
     */
    private ParseError error(Token token, String message){
        Fein.error(token, message);
        return new ParseError();
    }

    /**
     * Method to synchronize statement when syntax error occurs
     */
    private void synchronize() {
        advance();

        while (!isAtEnd()) {
            if (previous().type == TokenType.SEMICOLON) return;

            switch (peek().type) {
                case CLASS:
                case FUN:
                case VAR:
                case FOR:
                case IF:
                case WHILE:
                case PRINT:
                case RETURN:
                    return;
            }

            advance();
        }
    }



    /**
     * Method to current token is of given type
     *
     * @param type TokenType
     *
     * @return boolean
     */
    private boolean check(TokenType type){
        if(isAtEnd()) return false;
        return peek().type == type;
    }

    /**
     * Method to advance to the next step
     *
     * @return previous()
     */
    private Token advance(){
        if(!isAtEnd()) current++;
        return previous();
    }

    /**
     * Method to check if token list is at End
     *
     * @return boolean
     */
    private boolean isAtEnd(){
        return peek().type == TokenType.EOF;
    }

    /**
     * Method to get current token
     *
     * @return Token
     */
    private Token peek(){
        return tokens.get(current);
    }

    /**
     * Method to get previous token
     *
     * @return Token
     */
    private Token previous(){
        return tokens.get(current - 1);
    }


}
