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

    /**
     * Method to traverse the distance and find the enclosing environment
     *
     * @param distance int
     *
     * @return Environment
     */
    Environment ancestor(int distance) {
        Environment environment = this;
        for(int i = 0; i < distance; i++) {
            environment = environment.enclosing;
        }

        return environment;
    }

    /**
     * Method to get ancestor values using distance
     *
     * @param distance int
     * @param name String
     *
     * @return Object
     */
    Object getAt(int distance, String name) {
        return ancestor(distance).values.get(name);
    }

    /**
     * Method to assign value based on distance of environment scope
     *
     * @param distance int
     * @param name Token
     * @param value Object
     */
    void assignAt(int distance, Token name, Object value) {
        ancestor(distance).values.put(name.lexeme, value);
    }

}
