import cs132.IR.registers.*;
import cs132.IR.*;
import cs132.IR.syntaxtree.*;
import cs132.IR.visitor.*;

public class SV2V {
    public static void main(String [] args) {
        try {
            Registers.SetRiscVregs();
            Node root = new SparrowParser(System.in).Program();
            SparrowVConstructor svc = new SparrowVConstructor();
            root.accept(svc);
            cs132.IR.sparrowv.Program p = svc.getProgram();
            Context c = new Context();
            ContextBuilder cb = new ContextBuilder();
            p.accept(cb, c);
            Converter cv = new Converter();
            p.accept(cv, c);
        } catch (Exception e) {
            System.out.println("Error!");
        }
    }
}