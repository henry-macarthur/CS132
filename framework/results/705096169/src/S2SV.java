// import cs132.IR.sparrow.visitor.*;
// import cs132.IR.syntaxtree.*;

import cs132.IR.SparrowParser;
import cs132.IR.syntaxtree.*;
import cs132.IR.sparrow.*;
import cs132.IR.sparrow.visitor.*;

public class S2SV {
    public static void main(String [] args) {
        try {
           
            cs132.IR.syntaxtree.Program program = new SparrowParser(System.in).Program();
            Context c = new Context();
            RegisterVisitor rv = new RegisterVisitor();
            LoopVisitor lv = new LoopVisitor();
            
            program.accept(lv, c);
            program.accept(rv, c);
            //System.out.println(c.id_to_end);
            c.update_if_loop();
            c.load_int();
            Converter cv = new Converter();
            
            //System.out.println("func Main()" + "\n" + "t5 = 12 \n" + "v0 = t5\n" + "print(t5) \n" + "t4 = 1 \n" + "goto main_end \n" + "main_end: \n" + "return v0");
            System.out.println(program.accept(cv, c));

        } catch (Exception e) {
            System.out.println("Error!");
        }
    }
}