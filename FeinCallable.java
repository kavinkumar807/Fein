import java.util.List;

/**
 * Interface for Fein callable
 */
public interface FeinCallable {
    int arity();
    Object call(Interpreter interpreter, List<Object> arguments);
}
