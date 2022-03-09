// import cs132.IR.SparrowParser;
import cs132.IR.syntaxtree.*;
// import cs132.IR.sparrow.*;
// import cs132.IR.sparrow.visitor.*;
import cs132.IR.visitor.*;
//import cs132.IR.visitor.*;
import java.util.*; 

public class RegisterVisitor extends cs132.IR.visitor.GJVoidDepthFirst<Context> { 
    String current_function;

    enum State {
        OTHER, 
        LOOP
    }
    
    int loop_counter = 0;
    State current_state = State.OTHER;   

    @Override 
    public void visit(Program n, Context c) {
        //loop over each function
        for(int i = 0; i < n.f0.size(); i++) {
            n.f0.elementAt(i).accept(this, c);
        }
    }

    @Override 
    public void visit(FunctionDeclaration n, Context c) {
        String function_name = n.f1.f0.tokenImage;
        int begin_line = n.f1.f0.beginLine; //will use this for the parameters, can worry about those later
        //f3 is a list of identifiers for parameters, will worry about those later!
        //f5 -> block of code 
        current_function = function_name;

        for(int i = 0; i < n.f3.size(); i++) {
            n.f3.elementAt(i).accept(this, c);
        }

        n.f5.accept(this, c); //go into the function body 
    }

    @Override 
    public void visit(Block n, Context c) {
        //f0 -> list of instructions in the block 
        //f2 -> return call, nothing will be declared here so we worry about this when we are doing last use 

        for(int i = 0; i < n.f0.size(); i++) {
            n.f0.elementAt(i).accept(this, c); //loop over each instruction
        }
        n.f2.accept(this, c);
    }

    @Override 
    public void visit(Instruction n, Context c) {
        n.f0.accept(this, c); //go from instruction -> specific instruction type
    }

    @Override
    public void visit(LabelWithColon n, Context c) {
        //f0 -> label 
        String label_name = n.f0.f0.tokenImage;
        int label_line = n.f0.f0.beginLine;
        //current_state = State.LOOP;
        //c.set_label(label_name, label_line); //now we need to look for the backwards goto
    }

    @Override 
    public void visit(SetInteger n, Context c) {
        //f0 -> identifier, f2 = integer literal 
        //dont care about the integer literal atm I dont think
        n.f0.accept(this, c);
    }

    @Override 
    public void visit(Identifier n, Context c) {
        String identifier_name = n.f0.tokenImage;
        int identifier_begin_line = n.f0.beginLine;

        //step1, see if the id has been declared or not, if not we add it and its start line to the hashtable
        //even if a id gets reused i think its ok as we will just reserve the register for it
        c.set_begin_line(identifier_name, current_function, identifier_begin_line);
        // if(identifier_name.equals("t38")) {
        //     System.out.println(n.f0.endLine);
        // }
        //System.out.println(identifier_name);
        c.set_end_line(identifier_name, current_function, identifier_begin_line);
    }
    
    @Override 
    public void visit(SetFuncName n, Context c) {
        //f0 -> name of id 
        n.f0.accept(this, c);
        n.f3.accept(this, c);
    }

    @Override 
    public void visit(Add n, Context c) {
        n.f0.accept(this, c);
        n.f2.accept(this, c);
        n.f4.accept(this, c);
    }

    @Override 
    public void visit(Subtract n, Context c) {
        n.f0.accept(this, c);
        n.f2.accept(this, c);
        n.f4.accept(this, c);
    }

    @Override 
    public void visit(Multiply n, Context c) {
        n.f0.accept(this, c);
        n.f2.accept(this, c);
        n.f4.accept(this, c);
    }

    @Override 
    public void visit(LessThan n, Context c) {
        n.f0.accept(this, c);
        n.f2.accept(this, c);
        n.f4.accept(this, c);
    }

    @Override 
    public void visit(Load n, Context c) {
        //System.out.println("!!!");
        n.f0.accept(this, c);
        n.f3.accept(this, c);
        //n.f5.accept(this, c);
    }

    @Override 
    public void visit(Store n, Context c) {
        n.f1.accept(this, c);
        n.f3.accept(this, c);
        n.f6.accept(this, c);
    }

    @Override 
    public void visit(Move n, Context c) {
        n.f0.accept(this, c);
        n.f2.accept(this, c);
    }

    @Override 
    public void visit(Alloc n, Context c) {
        n.f0.accept(this, c);
        n.f4.accept(this, c);
    }

    @Override 
    public void visit(Call n, Context c) {
        n.f0.accept(this, c);
        n.f3.accept(this, c);

        for(int i = 0; i < n.f5.size(); i++) {
            n.f5.elementAt(i).accept(this, c);
        }
    }

    @Override 
    public void visit(IfGoto n, Context c) {
        n.f1.accept(this, c);
    }

    @Override 
    public void visit(Goto n, Context c) {
        //String label = n.f0.f0.tokenImage;
    }
}
