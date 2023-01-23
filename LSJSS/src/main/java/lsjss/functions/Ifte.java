package lsjss.functions;

import ec.*;
import ec.gp.*;
import lsjss.main.DoubleData;

public class Ifte extends GPNode
{
    public String toString() { return "ifte"; }

    /*
      public void checkConstraints(final EvolutionState state,
      final int tree,
      final GPIndividual typicalIndividual,
      final Parameter individualBase)
      {
      super.checkConstraints(state,tree,typicalIndividual,individualBase);
      if (children.length!=2)
      state.output.error("Incorrect number of children for node " +
      toStringForError() + " at " +
      individualBase);
      }
    */
    public int expectedChildren() { return 3; }

    public void eval(final EvolutionState state,
                     final int thread,
                     final GPData input,
                     final ADFStack stack,
                     final GPIndividual individual,
                     final Problem problem)
    {
        double condition;
        double returnIfTrue;
        double returnIfNotTrue;
        DoubleData rd = ((DoubleData)(input));

        children[0].eval(state,thread,input,stack,individual,problem);
        condition = rd.x;

        children[1].eval(state,thread,input,stack,individual,problem);
        returnIfTrue = rd.x;

        children[2].eval(state,thread,input,stack,individual,problem);
        returnIfNotTrue = rd.x;

        if (condition>0) {
            rd.x = returnIfTrue;}
        else {
            rd.x = returnIfNotTrue;
        }
    }
}