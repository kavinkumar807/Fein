import java.util.HashMap;
import java.util.Map;

/**
 * class for environment data structure to store variable values association
 */
public class Environment {
    final Environment enclosing;
    private final Map<String, Object> values = new HashMap<>();

    Environment(){
        enclosing = null;
    }

    Environment(Environment enclosing){
        this.enclosing = enclosing;
    }

    /**
     * Method to get the values
     *
     * @param name Token
     * @return Object
     */
    Object get(Token name){
        if(values.containsKey(name.lexeme)){
            return values.get(name.lexeme);
        }

        if(enclosing != null) return enclosing.get(name);

        throw new RuntimeError(name, "Undefined variable'"+ name.lexeme + "'.");
    }

    /**
     * Method to assign variables
     *
     * @param name Token
     * @param value Object
     */
    void assign(Token name, Object value){
        if(values.containsKey(name.lexeme)){
            values.put(name.lexeme, value);
            return;
        }

        if(enclosing != null){
            enclosing.assign(name, value);
            return;
        }

        throw new RuntimeError(name, "Undefined variable '" + name.lexeme + "'.");
    }

    /**
     * Method to process and store variable definition
     *
     * @param name String
     * @param value Object
     */
    void define(String name, Object value){
        values.put(name, value);
    }




}
