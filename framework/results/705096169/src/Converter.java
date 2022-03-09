import cs132.IR.syntaxtree.*;
import cs132.IR.visitor.*;
import java.util.*; 

public class Converter extends cs132.IR.visitor.GJDepthFirst<String, Context> {

    String current_function = "";
    
    enum State { 
        OTHER, 
        ALLOCATE,
        PARAM
    }

    State current_state = State.OTHER;

    @Override 
    public String visit(Program n, Context c) {
        //loop over each function

        String ret = "";
        for(int i = 0; i < n.f0.size(); i++) {
            ret += n.f0.elementAt(i).accept(this, c);
        }

        return ret;
    }

    @Override 
    public String visit(Identifier n, Context c) {
        //this is where most of the work is going to happen 
        String identifier_name = current_function + "_" + n.f0.tokenImage;
        String ret = "";
        if(current_state == State.ALLOCATE) {
            String allocate_register = c.getRegister(identifier_name, n.f0.beginLine );
            String stack_save = "";
            if(allocate_register.equals("t0")) { //means that we dont actually allocate to a register
                stack_save = identifier_name + " = t0\n";
            }
            String allocate_id = c.pickStack(allocate_register);
            allocate_register = c.pickRegister(allocate_register);
            String tmp = (allocate_register + " = " + n.f0.tokenImage + "\n") + stack_save;
            tmp = allocate_id + tmp;
            ret += tmp;
            // if(alloacte_register.indexOf(";") > 0) { //this means that there was a reallocation

            // }
        } else if(current_state == State.PARAM) {
            ret = n.f0.tokenImage;
        }
        return ret;
    }

    @Override 
    public String visit(FunctionDeclaration n, Context c) {
        String ret = "";
        c.dont_allocate.clear();
        c.reg_to_id.clear();
        c.load_registers();
        String function_name = n.f1.f0.tokenImage;
        current_function = function_name;
        String func_name = "func " + current_function + "(";
        current_state = State.PARAM;
        String params = "";
        for(int i = 0; i < n.f3.size(); i++) {
            params += (" " + n.f3.elementAt(i).accept(this, c));
        }
        if(params.length() > 2)
            params = params.substring(1);
        //should convert this to a list
        List<String> list = new ArrayList<String>();
        if(params.length() >= 2)
            list = new ArrayList<String>(Arrays.asList(params.split(" ")));
        
        
        //will iterate over this list assigning parameters / registers 

        //step 1 : assign all of the current registers to local stack registers 

        int regNum = 0;
        
        String assign_a_to_stack = "";
        String stack_save = "";
        for(int i = 0; i < list.size() && i < 6; i++) {
            stack_save = "";
            int regOffset = i + 2;
            String current_parameter = list.get(i);
            String current_id = current_function + "_" + current_parameter;
            String current_a = "a" + regOffset;
            String current_register = c.getRegister(current_id, n.f1.f0.beginLine);
            
            if(current_register.equals("t0")) { //means that we dont actually allocate to a register
                stack_save = current_id + " = " + current_a + "\n";
            }

            assign_a_to_stack +=  (current_register + " = " + current_a + "\n" + stack_save);
            regNum++;
        }
        //assign_a_to_stack += c.reg_to_id + "\n";

        //now we have to loop through the rest of it and assign the actual param value 
        //for now assume we wont use more than 15 parameters
        String assign_parameters = "";
        params = "";
        //System.out.println(c.reg_to_id);
        for(; regNum < list.size(); regNum++) {
            String current_id = current_function + "_" + list.get(regNum);
            String current_register = c.getRegister(current_id, n.f1.f0.beginLine); //c.isRegister(current_function, list.get(regNum));

            if(!current_register.equals("t0")) {
                assign_parameters += (current_register + " = " + current_id + "\n");
            }
            params += " " + (current_id);
        }
        
        if(params.length() > 0)
            params = params.substring(1);

        func_name += (params + ")\n");
        // current_state = State.ALLOCATE;
        // String parameter_list = "";
        // for(int i = 0; i < n.f3.size(); i++) {
        //     parameter_list += n.f3.elementAt(i).accept(this, c);
        // }
        current_state = State.OTHER;
        String body = n.f5.accept(this, c); //go into the function body 

        ret = func_name  + assign_a_to_stack + assign_parameters +  body;
        c.load_registers();
        //System.out.println(ret);
        return ret;
    }

    @Override
    public String visit(Instruction n, Context c) {
        return n.f0.accept(this, c);
    }

    @Override 
    public String visit(Block n, Context c) {
        String ret = "";
        for(int i = 0; i < n.f0.size(); i++) {
            ret += n.f0.elementAt(i).accept(this, c);
        }
        //map the register to the id
        String identifier_name = current_function + "_" + n.f2.f0.tokenImage;
        String return_register = c.isRegister(current_function, n.f2.f0.tokenImage);
        if(return_register.equals(identifier_name)) {
            ret += "return " + identifier_name + "\n\n";
            return ret;
        }
        //shouldnt have to do extra work here
        String toId = n.f2.f0.tokenImage + " = " + return_register + "\n";
        ret += toId;
        ret += "return " + n.f2.f0.tokenImage + "\n\n";
        //handle return expression here, can do that later 
        return ret;
    }

    @Override 
    public String visit(SetInteger n, Context c) {
        //this is kind of a special case, as we can directly assign here!
        String ret = "";
        String identifier_name = current_function + "_" + n.f0.f0.tokenImage;
        String allocate_register = c.getRegister(identifier_name, n.f0.f0.beginLine);
        String stack_save = "";
        if(allocate_register.equals("t0")) { //means that we dont actually allocate to a register
            stack_save = identifier_name + " = t0\n";
        }
        String allocate_id = c.pickStack(allocate_register);
        allocate_register = c.pickRegister(allocate_register);
        //System.out.println(allocate_register);
        String integer = n.f2.f0.tokenImage; 

        String assign = allocate_register + " = " + integer + "\n" + stack_save;
        ret = allocate_id + assign;

        return ret;
        
    }

    @Override 
    public String visit(Label n, Context c) {
        return (n.f0.tokenImage + "\n");
    }

    @Override 
    public String visit(LabelWithColon n, Context c) {
        int sz = c.dont_allocate.size();
        String save = "";///c.determineLoopLiveness(n.f0.f0.beginLine);
        if(sz == 0 && c.dont_allocate.size() > 0)
            c.label = n.f0.f0.tokenImage;
            //save += c.dont_allocate + "\n" + c.reg_to_id + "\n";
        // if(current_function.equals("Tree_RemoveRight")) {
        //     save += c.dont_allocate;
        //     save += "!!!\n";
        // }

        return save + (n.f0.f0.tokenImage + ":\n");
    }

    @Override 
    public String visit(Add n, Context c) {
        String ret = "";
        String identifier_name = current_function + "_" + n.f0.f0.tokenImage;

        //this needs to be tested 
        int cur_line = n.f0.f0.beginLine;

        String arg1 = n.f2.f0.tokenImage;
        String arg2 = n.f4.f0.tokenImage;

        //to make things easy, we can always use temp registers
        String r1 = c.isRegister(current_function, arg1);
        String r2 = c.isRegister(current_function, arg2);

        //c.freeRegister(cur_line); //free the registers if this is their last use

        String assignR1 = "t0 = " + r1 + "\n";
        String assignR2 = "t1 = " + r2 + "\n";          

        //have to check to see if the registers should be freed first I think 
        String allocate_register = c.getRegister(identifier_name, cur_line);
        String stack_save = "";
        if(allocate_register.equals("t0")) { //means that we dont actually allocate to a register
            stack_save = identifier_name + " = t0\n";
        }
        String allocate_id = c.pickStack(allocate_register);
        allocate_register = c.pickRegister(allocate_register);

        String add_statement = allocate_register + " = t0 + t1\n" + stack_save;
        ret = assignR1 + assignR2 + allocate_id + add_statement;

        return ret; 
    }

    @Override 
    public String visit(Subtract n, Context c) {
        String ret = "";
        String identifier_name = current_function + "_" + n.f0.f0.tokenImage;

        //this needs to be tested 
        int cur_line = n.f0.f0.beginLine;

        String arg1 = n.f2.f0.tokenImage;
        String arg2 = n.f4.f0.tokenImage;

        //to make things easy, we can always use temp registers
        String r1 = c.isRegister(current_function, arg1);
        String r2 = c.isRegister(current_function, arg2);

        //c.freeRegister(cur_line); //free the registers if this is their last use

        String assignR1 = "t0 = " + r1 + "\n";
        String assignR2 = "t1 = " + r2 + "\n";          

        //have to check to see if the registers should be freed first I think 
        String allocate_register = c.getRegister(identifier_name, cur_line);
        String stack_save = "";
        if(allocate_register.equals("t0")) { //means that we dont actually allocate to a register
            stack_save = identifier_name + " = t0\n";
        }
        String allocate_id = c.pickStack(allocate_register);
        allocate_register = c.pickRegister(allocate_register);
        //System.out.println(allocate_register);
        String add_statement = allocate_register + " = t0 - t1\n" + stack_save;
        ret = assignR1 + assignR2 + allocate_id + add_statement;

        return ret; 
    }

    @Override 
    public String visit(Multiply n, Context c) {
        String ret = "";
        String identifier_name = current_function + "_" + n.f0.f0.tokenImage;

        //this needs to be tested 
        int cur_line = n.f0.f0.beginLine;

        String arg1 = n.f2.f0.tokenImage;
        String arg2 = n.f4.f0.tokenImage;

        //to make things easy, we can always use temp registers
        String r1 = c.isRegister(current_function, arg1);
        String r2 = c.isRegister(current_function, arg2);

        //c.freeRegister(cur_line); //free the registers if this is their last use

        String assignR1 = "t0 = " + r1 + "\n";
        String assignR2 = "t1 = " + r2 + "\n";          

        //have to check to see if the registers should be freed first I think 
        String allocate_register = c.getRegister(identifier_name, cur_line);
        String stack_save = "";
        if(allocate_register.equals("t0")) { //means that we dont actually allocate to a register
            stack_save = identifier_name + " = t0\n";
        }
        String allocate_id = c.pickStack(allocate_register);
        allocate_register = c.pickRegister(allocate_register);

        String add_statement = allocate_register + " = t0 * t1\n" + stack_save;
        ret = assignR1 + assignR2 + allocate_id + add_statement;

        return ret; 
    }

    @Override 
    public String visit(LessThan n, Context c) {
        String ret = "";
        String identifier_name = current_function + "_" + n.f0.f0.tokenImage;

        //this needs to be tested 
        int cur_line = n.f0.f0.beginLine;

        String arg1 = n.f2.f0.tokenImage;
        String arg2 = n.f4.f0.tokenImage;

        //to make things easy, we can always use temp registers
        String r1 = c.isRegister(current_function, arg1);
        String r2 = c.isRegister(current_function, arg2);
        //c.freeRegister(cur_line); //free the registers if this is their last use

        String assignR1 = "t0 = " + r1 + "\n";
        String assignR2 = "t1 = " + r2 + "\n";          

        //have to check to see if the registers should be freed first I think 
        String allocate_register = c.getRegister(identifier_name, cur_line);
        String stack_save = "";
        if(allocate_register.equals("t0")) { //means that we dont actually allocate to a register
            stack_save = identifier_name + " = t0\n";
        }
        String allocate_id = c.pickStack(allocate_register);
        allocate_register = c.pickRegister(allocate_register);

        String add_statement = allocate_register + " = t0 < t1\n" + stack_save;
        ret = assignR1 + assignR2 + allocate_id + add_statement;

        return ret; 
    }

    @Override 
    public String visit(Alloc n, Context c) {
        //f0, f4
        String ret = "";
        String identifier_name = current_function + "_" + n.f0.f0.tokenImage;
        //this needs to be tested 
        int cur_line = n.f0.f0.beginLine;

        String arg1 = n.f4.f0.tokenImage;
        String r1 = c.isRegister(current_function, arg1);
        String assignR1 = "t0 = " + r1 + "\n";
        //c.freeRegister(cur_line); //free the registers if this is their last use
        String allocate_register = c.getRegister(identifier_name, cur_line); //do the overflow check later
        String stack_save = "";
        if(allocate_register.equals("t0")) { //means that we dont actually allocate to a register
            stack_save = identifier_name + " = t0\n";
        }
        String allocate_id = c.pickStack(allocate_register);
        allocate_register = c.pickRegister(allocate_register);

        String allocate_statement = allocate_register + " = alloc(t0)\n" + stack_save;
        ret = assignR1 + allocate_id + allocate_statement;

        return ret;
    }

    @Override 
    public String visit(Load n, Context c) {
        String ret = "";
        String identifier_name = current_function + "_" + n.f0.f0.tokenImage;
        int cur_line = n.f0.f0.beginLine;

        String arg1 = n.f3.f0.tokenImage;
        String r1 = c.isRegister(current_function, arg1);
        String assign1 = "t0 = " + r1 + "\n";
        //String assign2 = "t1 = " + n.f5.f0.tokenImage + "\n";
        //c.freeRegister(cur_line); //free the registers if this is their last use
        String allocate_register = c.getRegister(identifier_name, cur_line);
        String stack_save = "";
        if(allocate_register.equals("t0")) { //means that we dont actually allocate to a register
            stack_save = identifier_name + " = t0\n";
        }
        String allocate_id = c.pickStack(allocate_register);
        allocate_register = c.pickRegister(allocate_register);

        String load_statement = allocate_register + " = [t0 + " + n.f5.f0.tokenImage + "]\n" + stack_save;
        ret = assign1 + allocate_id + load_statement;

        return ret;
    }

    @Override
    public String visit(Store n, Context c) {
        String ret = "";
        int cur_line = n.f1.f0.beginLine;

        String arg1 = n.f1.f0.tokenImage;
        String r1 = c.isRegister(current_function, arg1);
        String assign1 = "t0 = " + r1 + "\n";

        String arg2 = n.f6.f0.tokenImage;
        String r2 = c.isRegister(current_function, arg2);
        String assign2 = "t1 = " + r2 + "\n";

        //c.freeRegister(cur_line);
        String store_statement = "[t0" + " + " + n.f3.f0.tokenImage + "] = t1\n"; // + t1;
        ret = assign1 + assign2 + store_statement;

        return ret;
    }

    @Override 
    public String visit(Print n, Context c) {
        String ret = "";
        int cur_line = n.f2.f0.beginLine;

        String arg1 = n.f2.f0.tokenImage;
        String r1 = c.isRegister(current_function, arg1);
        //c.freeRegister(cur_line); //free the registers if this is their last use
        String assign1 = "t0 = " + r1 + "\n";

        String print_statement = "print(t0)\n";
        ret = assign1 + print_statement;

        return ret;
    }

    @Override 
    public String visit(Call n, Context c) {
        
        String ret = "";
        String identifier_name = current_function + "_" + n.f0.f0.tokenImage;
        //this needs to be tested 
        int cur_line = n.f0.f0.beginLine;

        String arg1 = n.f3.f0.tokenImage;
      

        //to make things easy, we can always use temp registers
        String r1 = c.isRegister(current_function, arg1);


        String assignR1 = "t0 = " + r1 + "\n";     
        //have to check to see if the registers should be freed first I think 


        String handle_parameters = "";
        current_state = State.PARAM;
        for(int i = 0; i < n.f5.size(); i++) {
            handle_parameters += (" " + n.f5.elementAt(i).accept(this, c));
        }
        current_state = State.OTHER;
        List<String> list = new ArrayList<String>();
        if(handle_parameters.length() > 0) {
            handle_parameters = handle_parameters.substring(1);
            list = new ArrayList<String>(Arrays.asList(handle_parameters.split(" ")));
        }

        //have the name of all the parameters so we can assign them to the a registers 2-7
        int currentReg = 0;
        String assignA = "";
        for(; currentReg < list.size() && currentReg < 6; currentReg++) {
            int registerIndex = currentReg + 2;
            String currentA = "a" + registerIndex;
            assignA += (currentA + " = " + c.isRegister(current_function, list.get(currentReg)) + "\n");
        } //finish assigning the a registers, now we can pass them in the function call 

        
        handle_parameters = "";
        String reg_to_ids = "";
        
        for(; currentReg < list.size(); currentReg++) {
            String tmp = current_function + "_" + list.get(currentReg);
            if(!c.isRegister(current_function, list.get(currentReg)).equals(tmp))
                reg_to_ids += ((current_function + "_" + list.get(currentReg)) + " = " + c.isRegister(current_function, list.get(currentReg)) + "\n");
            handle_parameters += (" " + (current_function + "_" + list.get(currentReg)));
        }
        if(handle_parameters.length() > 1) 
            handle_parameters = handle_parameters.substring(1);

        //List<String> list = new ArrayList<String>(Arrays.asList(handle_parameters.split(" ")));
        
        String registers_to_id = "";
        // for(int i = 0; i < list.size(); i++) {
        //     String get_reg = c.isRegister(current_function, list.get(i));
        //     if(!get_reg.equals(list.get(i))) {
        //         String tmp = list.get(i) + " = " + get_reg + "\n";
        //         registers_to_id += tmp;
        //     }
        // }
        //c.freeRegister(cur_line); //free the registers if this is their last use

        String allocate_register = c.getRegister(identifier_name, cur_line);
        String stack_save = "";
        if(allocate_register.equals("t0")) { //means that we dont actually allocate to a register
            stack_save = identifier_name + " = t0\n";
        }
        String allocate_id = c.pickStack(allocate_register);
        allocate_register = c.pickRegister(allocate_register);
        //should loop through all of the "active registers and store them on the stack"
        //for both of these we should not save the register used for the call 
        String callerSave = c.callerSaveActiveRegisters(allocate_register);

        String callerRestore = c.callerRestoreActiveRegisters(allocate_register);
        String call_statement = allocate_register + " = call t0(" + handle_parameters + ")\n" + stack_save;
        ret = assignR1 + registers_to_id  + callerSave + assignA + reg_to_ids + allocate_id + call_statement + callerRestore;
        //need to keep track of what registers are currently active, and then save them on the stack! 
        //also need to do eventually do argument passing via registers but that should come later 
        return ret;
    }

    @Override 
    public String visit(Move n, Context c) {
        String ret = "";
        String identifier_name = current_function + "_" + n.f0.f0.tokenImage;
        //this needs to be tested 
        int cur_line = n.f0.f0.beginLine;

        String arg1 = n.f2.f0.tokenImage;
        String r1 = c.isRegister(current_function, arg1);
        //c.freeRegister(cur_line);

        String assign1 = "t0 = " + r1 + "\n";
        String allocate_register = c.getRegister(identifier_name, cur_line);
        String stack_save = "";
        if(allocate_register.equals("t0")) { //means that we dont actually allocate to a register
            stack_save = identifier_name + " = t0\n";
        }
        String allocate_id = c.pickStack(allocate_register);
        allocate_register = c.pickRegister(allocate_register);

        String move_statement = allocate_register + " = t0\n" + stack_save;

        ret = assign1 + allocate_id + move_statement;

        return ret;
    }

    @Override 
    public String visit(ErrorMessage n, Context c) {
        String ret = "";

        return "error(" + n.f2.f0.tokenImage + ")\n";
    }

    @Override 
    public String visit(Goto n, Context c) {
        if(c.label.equals(n.f1.f0.tokenImage)) {
            c.label = "";
            c.dont_allocate.clear();
        }
        String ret = "goto " + n.f1.f0.tokenImage + "\n";

        return ret;
    }

    @Override 
    public String visit(IfGoto n, Context c) {
        String ret = "";
        int cur_line = n.f1.f0.beginLine;
        String arg1 = n.f1.f0.tokenImage;
        String r1 = c.isRegister(current_function, arg1);
        //c.freeRegister(cur_line);

        String assign1 = "t0 = " + r1 + "\n";

        String if_statement = "if0 t0 goto " + n.f3.f0.tokenImage  + "\n";
        ret = assign1 + if_statement;
        return ret;
    }

    @Override 
    public String visit(SetFuncName n, Context c) {
        String ret = "";
        String identifier_name = current_function + "_" + n.f0.f0.tokenImage;

        String allocate_register = c.getRegister(identifier_name, n.f0.f0.beginLine);
        String stack_save = "";
        if(allocate_register.equals("t0")) { //means that we dont actually allocate to a register
            stack_save = identifier_name + " = t0\n";
        }
        String allocate_id = c.pickStack(allocate_register);
        allocate_register = c.pickRegister(allocate_register);

        String assign = allocate_id + allocate_register + " = @" + n.f3.f0.tokenImage + "\n" + stack_save;
        return  assign;
    }

}