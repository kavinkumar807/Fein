import java.util.HashMap;
import java.util.Map;

/**
 * Class for FeinInstance runtime representation of lox
 */
public class FeinInstance {
    private FeinClass klass;
    private final Map<String, Object> fields = new HashMap<>();

    FeinInstance(FeinClass klass){
        this.klass = klass;
    }

    /**
     * Method to get property from instance
     *
     * @param name Token
     *
     * @return Object
     */
    Object get(Token name) {
        if(fields.containsKey(name.lexeme)) {
            return fields.get(name.lexeme);
        }

        FeinFunction method = klass.findMethod(name.lexeme);
        if(method != null) return method.bind(this);

        throw new RuntimeError(name, "Undefined property '" + name.lexeme + "'.");
    }

    /**
     * Method to set the value to the field in the instance
     *
     * @param name Token
     * @param value Object
     */
    void set(Token name, Object value) {
        fields.put(name.lexeme, value);
    }

    @Override
    public String toString() {
        return klass.name + " instance";
    }
}
