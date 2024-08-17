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
     * expression     → equality ;
     * equality       → comparison ( ( "!=" | "==" ) comparison )* ;
     * comparison     → term ( ( ">" | ">=" | "<" | "<=" ) term )* ;
     * term           → factor ( ( "-" | "+" ) factor )* ;
     * factor         → unary ( ( "/" | "*" ) unary )* ;
     * unary          → ( "!" | "-" ) unary
     *                | primary ;
     * primary        → NUMBER | STRING | "true" | "false" | "nil"
     *                | "(" expression ")" ;
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
    Expr parse() {
        try{
            return expression();
        } catch (ParseError error){
            return null;
        }
    }

    /**
     * Method for expression rule
     * expression     → equality ;
     * @return Expr
     */
    private Expr expression(){
        return equality();
    }

    /**
     * Method for equality rule
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
     * Method for comparison rule
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
     * Method for term rule
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
     * Method for factor rule
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
     * Method for unary rule
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
     * Method for primary rule
     * primary → NUMBER | STRING | "true" | "false" | "nil"
     *                | "(" expression ")"
     * @return Expr
     */
    private Expr primary() {
        if (match(TokenType.FALSE)) return new Expr.Literal(false);
        if(match(TokenType.TRUE)) return new Expr.Literal(true);
        if(match(TokenType.NIL)) return new Expr.Literal(null);

        if(match(TokenType.NUMBER, TokenType.STRING)){
            return new Expr.Literal(previous().literal);
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
