import java.util.*; 

public class Context {
    
    Hashtable<String, Integer> func_numvars = new Hashtable<String, Integer>();
    Hashtable<String, Integer> id_offset = new Hashtable<String, Integer>();
    Hashtable<String, ArrayList<String>> func_parameters = 
        new Hashtable<String, ArrayList<String>>();

    public void add_parameter(String current_function, String cur_parameter) {
        func_parameters.get(current_function).add(cur_parameter);
    }

    public void add_stack_var(String current_function, String id) {

        int num_variables = func_numvars.get(current_function);
        int offset = -1* (12 + (4*num_variables));

        id_offset.put(current_function + "_" + id, offset);

        func_numvars.put(current_function, ++num_variables);
    }

    public int get_stack_offset(String current_function, String id) {
        //get the stack offset for the given variable, this includes parameters
        //first loop over the parameters
        ArrayList<String> parameters = func_parameters.get(current_function);
        for(int i = 0; i < parameters.size(); i++) {
            if(parameters.get(i).equals(id)) {
                return (i*4); //this should stay positive as we store above
            }
        }

        //now check the regular map 
        return id_offset.get(current_function + "_" + id);
    }

    public int get_stack_size(String current_function) {
        //have to forsure allocate 8 bytes 
        return 8 + (4*func_numvars.get(current_function));
    }


}