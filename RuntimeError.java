/**
 * Class for runtime errors in Fein extends runtime exception
 */
class RuntimeError extends RuntimeException {
    final Token token;
    RuntimeError(Token token, String message) {
        super(message);
        this.token = token;
    }
}
