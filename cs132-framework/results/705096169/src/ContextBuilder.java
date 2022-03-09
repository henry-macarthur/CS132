import cs132.IR.sparrowv.visitor.*;
import cs132.IR.sparrowv.*;
import java.util.*;

public class ContextBuilder implements cs132.IR.sparrowv.visitor.ArgRetVisitor<Context,String> {
    String current_function = "";
    public String visit(cs132.IR.sparrowv.Add n, Context c) {
        String ret = "";

        return ret;
    }

    public String visit(cs132.IR.sparrowv.Alloc n, Context c) {
        String ret = "";

        return ret;
    }

    public String visit(cs132.IR.sparrowv.Block n, Context c) {
        String ret = "";
        for(int i = 0; i < n.instructions.size(); i++) {
            n.instructions.get(i).accept(this, c);
        }
        return ret;
    }
    
    public String visit(cs132.IR.sparrowv.Call n, Context c) {
        String ret = "";

        return ret;
    }

    public String visit(cs132.IR.sparrowv.ErrorMessage n, Context c) {
        String ret = "";

        return ret;
    }

    public String visit(cs132.IR.sparrowv.FunctionDecl n, Context c) {
        String ret = "";
        current_function = n.functionName.name;
        //deal with parameters later
        //for now just determine where we are going to store the function body temporaries 
        ArrayList<String> params = new ArrayList<String>();
        c.func_numvars.put(current_function, 0);
        c.func_parameters.put(current_function, params);

        //deal with parameters 
        for(int i = 0; i < n.formalParameters.size(); i++) {
            //get a parameter
            String cur_parameter = n.formalParameters.get(i).toString();
            c.add_parameter(current_function, cur_parameter);
        }

        n.block.accept(this, c);
        return ret;
    }

    public String visit(cs132.IR.sparrowv.Goto n, Context c) {
        String ret = "";

        return ret;
    }

    public String visit(cs132.IR.sparrowv.IfGoto n, Context c) {
        String ret = "";

        return ret;
    }

    public String visit(cs132.IR.sparrowv.LabelInstr n, Context c) {
        String ret = "";

        return ret;
    }

    public String visit(cs132.IR.sparrowv.LessThan n, Context c) {
        String ret = "";

        return ret;
    }

    public String visit(cs132.IR.sparrowv.Load n, Context c) {
        String ret = "";

        return ret;
    }

    public String visit(cs132.IR.sparrowv.Move_Id_Reg n, Context c) {
        String ret = "";
        String id = n.lhs.toString();
        c.add_stack_var(current_function, id);
        //dont care what the register is, just need to know where to store the current id
        return ret;
    }

    public String visit(cs132.IR.sparrowv.Move_Reg_FuncName n, Context c) {
        String ret = "";

        return ret;
    }

    public String visit(cs132.IR.sparrowv.Move_Reg_Id n, Context c) {
        String ret = "";

        return ret;
    }

    public String visit(cs132.IR.sparrowv.Move_Reg_Integer n, Context c) {
        String ret = "";

        return ret;
    }

    public String visit(cs132.IR.sparrowv.Move_Reg_Reg n, Context c) {
        String ret = "";

        return ret;
    }

    public String visit(cs132.IR.sparrowv.Multiply n, Context c) {
        String ret = "";

        return ret;
    }

    public String visit(cs132.IR.sparrowv.Print n, Context c) {
        String ret = "";

        return ret;
    }

    public String visit(cs132.IR.sparrowv.Program n, Context c) {
        String ret = "";
        for(int i = 0; i < n.funDecls.size(); i++) {
            //recurse onto each function declaration
            n.funDecls.get(i).accept(this, c);
        }
        return ret;
    }

    public String visit(cs132.IR.sparrowv.Store n, Context c) {
        String ret = "";

        return ret;
    }

    public String visit(cs132.IR.sparrowv.Subtract n, Context c) {
        String ret = "";

        return ret;
    }
}