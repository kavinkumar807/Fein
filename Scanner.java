import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * class for scanner
 */
public class Scanner {
    private final String source;
    private final List<Token> tokens = new ArrayList<>();
    private int start = 0;
    private int current = 0;
    private int line = 1;
    private static final Map<String, TokenType> keywords;

    static {
        keywords = new HashMap<>();
        keywords.put("and", TokenType.AND);
        keywords.put("class", TokenType.CLASS);
        keywords.put("else", TokenType.ELSE);
        keywords.put("false", TokenType.FALSE);
        keywords.put("for", TokenType.FOR);
        keywords.put("fun", TokenType.FUN);
        keywords.put("if", TokenType.IF);
        keywords.put("nil", TokenType.NIL);
        keywords.put("or", TokenType.OR);
        keywords.put("print", TokenType.PRINT);
        keywords.put("return", TokenType.RETURN);
        keywords.put("super", TokenType.SUPER);
        keywords.put("this", TokenType.THIS);
        keywords.put("true", TokenType.TRUE);
        keywords.put("var", TokenType.VAR);
        keywords.put("while", TokenType.WHILE);
    }

    public Scanner(String source) {
        this.source = source;
    }

    /**
     * Method to scan tokens
     *
     * @return List<Token>
     */
    List<Token> scanTokens(){
        while(!isAtEnd()){
            // at the beginning of the next lexeme.
            start = current;
            scanToken();
        }

        tokens.add(new Token(TokenType.EOF, "", null, line));
        return tokens;
    }

    /**
     * Method to switch lexeme character and add token
     */
    private void scanToken(){
        char c = advance();
        switch (c) {
            case '(' : addToken(TokenType.LEFT_PAREN); break;
            case ')' : addToken(TokenType.RIGHT_PAREN); break;
            case '{' : addToken(TokenType.LEFT_BRACE); break;
            case '}' : addToken(TokenType.RIGHT_BRACE); break;
            case ',' : addToken(TokenType.COMMA); break;
            case '.' : addToken(TokenType.DOT); break;
            case '-' : addToken(TokenType.MINUS); break;
            case '+' : addToken(TokenType.PLUS); break;
            case ';' : addToken(TokenType.SEMICOLON); break;
            case '*' : addToken(TokenType.STAR); break;
            case '!':
                addToken(match('=') ? TokenType.BANG_EQUAL : TokenType.BANG);
                break;
            case '=':
                addToken(match('=') ? TokenType.EQUAL_EQUAL : TokenType.EQUAL);
                break;
            case '<':
                addToken(match('=') ? TokenType.LESS_EQUAL : TokenType.LESS);
                break;
            case '>':
                addToken(match('=') ? TokenType.GREATER_EQUAL : TokenType.GREATER);
                break;
            case '/':
                if(match('/')){
                    // a comment goes until the end of the line
                    while(peek() != '\n' && !isAtEnd()) advance();
                } else if (match('*')) {
                    blockComments();
                } else {
                    addToken(TokenType.SLASH);
                }
                break;
            case ' ' :
            case '\r':
            case '\t':
                // ignore whitespaces
                break;
            case '\n':
                line++;
                break;
            case '"': string(); break;
            default:
                if(isDigit(c)){
                    number();
                } else if (isAlpha(c)) {
                    identifier();
                } else{
                    Fein.error(line, "Unexpected character.");
                }
                break;
        }
    }

    /**
     * Method to process block comments
     */
    private void blockComments() {
        while(peek() != '/' && !isAtEnd())  {
            if(peek() == '\n') line++;
            advance();
        }
        if(isAtEnd()){
            Fein.error(line, "Unterminated block comment");
            return;
        }
        if(source.charAt( current - 1) == '*'){
            advance();
        } else{
            Fein.error(line, "Unterminated block comment");
            current--;
        }
    }

    /**
     * Method to handle identifiers
     */
    private void identifier() {
        while(isAlphaNumeric(peek())) advance();

        String text = source.substring(start, current);
        TokenType type = keywords.get(text);
        if(type == null) type = TokenType.IDENTIFIER;
        addToken(type);
    }

    /**
     * Method to handle number
     */
    private void number(){
        while(isDigit(peek())) advance();

        // Look for fractional part.
        if(peek() == '.' && isDigit(peekNext())){
            // Consume the "."
            advance();

            while(isDigit(peek())) advance();
        }

        addToken(TokenType.NUMBER, Double.parseDouble(source.substring(start, current)));
    }

    /**
     * Method to handle string literal
     */
    private void string(){
        while(peek() != '"' && !isAtEnd()){
            if(peek() == '\n') line++;
            advance();
        }

        if(isAtEnd()){
            Fein.error(line, "Unterminated string");
            return;
        }
        // the closing ".
        advance();
        // Trim the surronding quotes
        String value = source.substring(start + 1, current - 1);
        addToken(TokenType.STRING, value);
    }

    /**
     * Helper method to check is alpha
     *
     * @param c char
     *
     * @return boolean
     */
    private boolean isAlpha(char c){
        return (c >= 'a' && c <= 'z') ||
                (c >= 'A' && c <= 'Z') ||
                c == '_';
    }

    /**
     * Helper method to check is alpha numeric
     *
     * @param c char
     *
     * @return boolean
     */
    private boolean isAlphaNumeric(char c){
        return isAlpha(c) || isDigit(c);
    }


    /**
     * Helper Method to check is digit
     *
     * @param c char
     *
     * @return boolean
     */
    private boolean isDigit(char c){
        return c >= '0' && c <= '9';
    }

    /**
     * Lookahead method to check subsequent characters
     *
     * @return char
     */
    private char peek(){
        if(isAtEnd()) return '\0';
        return source.charAt(current);
    }

    /**
     * Lookahead method to check two characters ahead
     *
     * @return char
     */
    private char peekNext(){
        if(current + 1 >= source.length()) return '\0';
        return source.charAt(current + 1);
    }

    /**
     * Method to match the subsequent lexeme character for one or two character tokens
     *
     * @param expected char
     *
     * @return boolean
     */
    private boolean match(char expected){
        if(isAtEnd()) return false;
        if(source.charAt(current) != expected) return false;

        current++;
        return true;
    }

    /**
     * Helper method to advance to next character
     *
     * @return source character
     */
    private char advance(){
        current++;
        return source.charAt(current - 1);
    }

    /**
     * method to add token
     *
     * @param type Token type
     */
    private void addToken(TokenType type){
        addToken(type, null);
    }

    /**
     * Overloaded method to add token
     *
     * @param type TokenType
     * @param literal Object
     */
    private void addToken(TokenType type, Object literal){
        String text = source.substring(start, current);
        tokens.add(new Token(type, text, literal, line));
    }

    /**
     * Helper Method to check is source at end
     *
     * @return boolean
     */
    private boolean isAtEnd(){
        return current >= source.length();
    }
}
