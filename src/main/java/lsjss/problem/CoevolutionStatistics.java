/*
  Copyright 2006 by Sean Luke
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/


package lsjss.problem;

import ec.*;
import ec.gp.GPIndividual;
import ec.simple.SimpleProblemForm;
import ec.steadystate.*;
import ec.util.*;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

/*
 * SimpleStatistics.java
 *
 * Created: Tue Aug 10 21:10:48 1999
 * By: Sean Luke
 */

/**
 * A basic Statistics class suitable for simple problem applications.
 *
 * SimpleStatistics prints out the best individual, per subpopulation,
 * each generation.  At the end of a run, it also prints out the best
 * individual of the run.  SimpleStatistics outputs this data to a log
 * which may either be a provided file or stdout.  Compressed files will
 * be overridden on restart from checkpoint; uncompressed files will be
 * appended on restart.
 *
 * <p>SimpleStatistics implements a simple version of steady-state statistics:
 * if it quits before a generation boundary,
 * it will include the best individual discovered, even if the individual was discovered
 * after the last boundary.  This is done by using individualsEvaluatedStatistics(...)
 * to update best-individual-of-generation in addition to doing it in
 * postEvaluationStatistics(...).

 <p><b>Parameters</b><br>
 <table>
 <tr><td valign=top><i>base.</i><tt>gzip</tt><br>
 <font size=-1>boolean</font></td>
 <td valign=top>(whether or not to compress the file (.gz suffix added)</td></tr>
 <tr><td valign=top><i>base.</i><tt>file</tt><br>
 <font size=-1>String (a filename), or nonexistant (signifies stdout)</font></td>
 <td valign=top>(the log for statistics)</td></tr>
 </table>

 *
 * @author Sean Luke
 * @version 1.0
 */

public class CoevolutionStatistics extends Statistics implements SteadyStateStatisticsForm //, ec.eval.ProvidesBestSoFar
{
    public Individual[] getBestSoFar() { return best_of_run; }

    /** log file parameter */
    public static final String P_STATISTICS_FILE = "file";

    /** compress? */
    public static final String P_COMPRESS = "gzip";

    public static final String P_DO_FINAL = "do-final";
    public static final String P_DO_GENERATION = "do-generation";
    public static final String P_DO_MESSAGE = "do-message";
    public static final String P_DO_DESCRIPTION = "do-description";
    public static final String P_DO_PER_GENERATION_DESCRIPTION = "do-per-generation-description";

    /** The Statistics' log */
    public int statisticslog = 0;  // stdout

    /** The best individual we've found so far */
    public Individual[] best_of_run = null;

    /** Should we compress the file? */
    public boolean compress;
    public boolean doFinal;
    public boolean doGeneration;
    public boolean doMessage;
    public boolean doDescription;
    public boolean doPerGenerationDescription;

    /** save all individuals of each generation **/
    public String[][][] individualsPerGeneration;
    /** save all fitnesses of each generation **/
    public double[][][] fitnessesPerGeneration;
    public double[][][] fitnessesPerGenerationValidation;
    public String[][][] individualsPerGenerationValidation;
    public static final String P_PATH_RESULTS = "path-results";
    public String pathResults;

    public void setup(final EvolutionState state, final Parameter base)
    {
        super.setup(state,base);

        compress = state.parameters.getBoolean(base.push(P_COMPRESS),null,false);

        File statisticsFile = state.parameters.getFile(
                base.push(P_STATISTICS_FILE),null);

        doFinal = state.parameters.getBoolean(base.push(P_DO_FINAL),null,true);
        doGeneration = state.parameters.getBoolean(base.push(P_DO_GENERATION),null,true);
        doMessage = state.parameters.getBoolean(base.push(P_DO_MESSAGE),null,true);
        doDescription = state.parameters.getBoolean(base.push(P_DO_DESCRIPTION),null,true);
        doPerGenerationDescription = state.parameters.getBoolean(base.push(P_DO_PER_GENERATION_DESCRIPTION),null,false);

        if (silentFile)
        {
            statisticslog = Output.NO_LOGS;
        }
        else if (statisticsFile!=null)
        {
            try
            {
                statisticslog = state.output.addLog(statisticsFile, !compress, compress);
            }
            catch (IOException i)
            {
                state.output.fatal("An IOException occurred while trying to create the log " + statisticsFile + ":\n" + i);
            }
        }
        else state.output.warning("No statistics file specified, printing to stdout at end.", base.push(P_STATISTICS_FILE));
    }

    public void postInitializationStatistics(final EvolutionState state)
    {
        super.postInitializationStatistics(state);
        // for random shuffle number of evaluations is equal to the number of individuals of one subpop
        // for random selection it's two times
        int numEvaluationsRandomShuffle = Math.max(state.population.subpops[0].individuals.length, state.population.subpops[1].individuals.length);
        int numEvaluationsRandomSelection = Math.max(state.population.subpops[0].individuals.length, state.population.subpops[1].individuals.length)*2;
        individualsPerGeneration = new String[state.numGenerations][state.population.subpops.length]
                [numEvaluationsRandomShuffle];
        fitnessesPerGeneration = new double[state.numGenerations][state.population.subpops.length]
                [numEvaluationsRandomShuffle];
        CoevolutionState GPstate = (CoevolutionState) state;
        fitnessesPerGenerationValidation = new double[state.numGenerations][state.population.subpops.length][GPstate.validationSet.instances.length+1];
        individualsPerGenerationValidation = new String[state.numGenerations][state.population.subpops.length][GPstate.validationSet.instances.length+1];

        // set up our best_of_run array -- can't do this in setup, because
        // we don't know if the number of subpopulations has been determined yet
        best_of_run = new Individual[state.population.subpops.length];
    }

    /** Logs the best individual of the generation. */
    public void postEvaluationStatistics(final EvolutionState state)
    {
        super.postEvaluationStatistics(state);
        //statisticslog = 0;
        // for now we just print the best fitness per subpopulation.
        Individual[] best_i = new Individual[state.population.subpops.length];  // quiets compiler complaints
        for(int x=0;x<state.population.subpops.length;x++)
        {

            best_i[x] = state.population.subpops[x].individuals[0];
            for(int y=1;y<state.population.subpops[x].individuals.length;y++) {
                if (state.population.subpops[x].individuals[y].fitness.betterThan(best_i[x].fitness))
                    best_i[x] = state.population.subpops[x].individuals[y];
                /** save all individuals of current subpop in Gen slot of the individualsPerGeneration Array **/
                GPIndividual gpInd = (GPIndividual)(state.population.subpops[x].individuals[y]);
                //individualsPerGeneration[state.generation][x][y] = gpInd.trees[0].child.makeCTree(true,
                //       gpInd.trees[0].printTerminalsAsVariablesInC, gpInd.trees[0].printTwoArgumentNonterminalsAsOperatorsInC);
                //fitnessesPerGeneration[state.generation][x][y] = -gpInd.fitness.fitness();
            }
            // now test to see if it's the new best_of_run
            if (best_of_run[x]==null || best_i[x].fitness.betterThan(best_of_run[x].fitness))
                best_of_run[x] = (Individual)(best_i[x].clone());
        }

        // print the best-of-generation individual
        if (doGeneration) state.output.println("\nGeneration: " + state.generation,statisticslog);
        if (doGeneration) state.output.println("Best Individual:",statisticslog);
        for(int x=0;x<state.population.subpops.length;x++)
        {
            if (doGeneration) state.output.println("Subpopulation " + x + ":",statisticslog);
            if (doGeneration) best_i[x].printIndividualForHumans(state,statisticslog);
            if (doMessage && !silentPrint) {
                state.output.message("Subpop " + x + " best fitness of generation" +
                        (best_i[x].evaluated ? " " : " (evaluated flag not set): ") +
                        -best_i[x].fitness.fitness());
                CoevolutionState GPstate = (CoevolutionState) state;
                CoevolutionStatistics nStateStatistics = (CoevolutionStatistics) state.statistics;
                state.output.message("Subpop " + x + " performance of best individual of generation on validation set: " +
                        nStateStatistics.fitnessesPerGenerationValidation[GPstate.generation][x][GPstate.validationSet.size]);
            }


            // describe the winner if there is a description
            if (doGeneration && doPerGenerationDescription)
            {
                if (state.evaluator.p_problem instanceof SimpleProblemForm)
                    ((SimpleProblemForm)(state.evaluator.p_problem.clone())).describe(state, best_i[x], x, 0, statisticslog);
            }
        }
    }

    /** Allows MultiObjectiveStatistics etc. to call super.super.finalStatistics(...) without
     calling super.finalStatistics(...) */
    protected void bypassFinalStatistics(EvolutionState state, int result)
    { super.finalStatistics(state, result);}

    /** Logs the best individual of the run. */
    public void finalStatistics(final EvolutionState state, final int result)
    {
        super.finalStatistics(state,result);
        String[] sheetNames = new String[2];
        sheetNames[0] = "JSS";
        sheetNames[1] = "LSS";
        // for now we just print the best fitness
        XSSFWorkbook workbookIndividuals = new XSSFWorkbook();
        XSSFWorkbook workbookFitnesses = new XSSFWorkbook();
        XSSFWorkbook workbookIndividualsValidation = new XSSFWorkbook();
        XSSFWorkbook workbookFitnessValidation = new XSSFWorkbook();
        if (doFinal) state.output.println("\nBest Individual of Run:",statisticslog);
        for(int x=0;x<state.population.subpops.length;x++ )
        {
            if (doFinal) state.output.println("Subpopulation " + x + ":",statisticslog);
            if (doFinal) best_of_run[x].printIndividualForHumans(state,statisticslog);
            if (doMessage && !silentPrint) state.output.message("Subpop " + x + " best fitness of run: " + best_of_run[x].fitness.fitnessToStringForHumans());

            // finally describe the winner if there is a description
            if (doFinal && doDescription)
                if (state.evaluator.p_problem instanceof SimpleProblemForm)
                    ((SimpleProblemForm)(state.evaluator.p_problem.clone())).describe(state, best_of_run[x], x, 0, statisticslog);
            /** save the individuals and fitnesses per generation in excel (one sheet for each subpop **/
            XSSFSheet sheetIndividuals = workbookIndividuals.createSheet(sheetNames[x]);
            XSSFSheet sheetFitnesses = workbookFitnesses.createSheet(sheetNames[x]);
            XSSFSheet sheetIndividualValidation = workbookIndividualsValidation.createSheet(sheetNames[x]);
            XSSFSheet sheetFitnessValidation = workbookFitnessValidation.createSheet(sheetNames[x]);

            CoevolutionState GPstate = (CoevolutionState) state;

            int rowCount = 0;
            Row titleRowIndividuals = sheetIndividuals.createRow(rowCount);
            Row titleRowFitnesses = sheetFitnesses.createRow(rowCount);
            rowCount++;
            for(int y=1;y<state.population.subpops[x].individuals.length;y++) {
                Row rowIndividuals = sheetIndividuals.createRow(rowCount);
                Row rowFitnesses = sheetFitnesses.createRow(rowCount);
                for (int z = 0; z < state.numGenerations; z++) {
                    Cell cellTitleIndividuals = titleRowIndividuals.createCell(z);
                    cellTitleIndividuals.setCellValue(z);
                    Cell cellTitleFitnesses = titleRowFitnesses.createCell(z);
                    cellTitleFitnesses.setCellValue(z);
                    Cell cellIndividual = rowIndividuals.createCell(z);
                    cellIndividual.setCellValue(individualsPerGeneration[z][x][y]);
                    Cell cellFitnesses = rowFitnesses.createCell(z);
                    cellFitnesses.setCellValue(fitnessesPerGeneration[z][x][y]);
                }
                rowCount++;
            }
            rowCount = 0;
            Row titleRowIndividualsValidation = sheetIndividualValidation.createRow(rowCount);
            Row titleRowFitnessValidation = sheetFitnessValidation.createRow(rowCount);
            rowCount++;
            for (int y=0; y<GPstate.validationSet.instances.length+1; y++){
                Row rowIndividualValidation = sheetIndividualValidation.createRow(rowCount);
                Row rowFitnessValidation = sheetFitnessValidation.createRow(rowCount);
                if (y==GPstate.validationSet.instances.length){
                    Cell cellIndividualsValidationIndex = rowIndividualValidation.createCell(0);
                    cellIndividualsValidationIndex.setCellValue("average");
                    Cell cellFitnessValidationIndex = rowFitnessValidation.createCell(0);
                    cellFitnessValidationIndex.setCellValue("average");
                } else {
                    Cell cellIndividualsValidationIndex = rowIndividualValidation.createCell(0);
                    cellIndividualsValidationIndex.setCellValue(GPstate.validationSet.instances[y].NPRODUCTS+"x"+GPstate.validationSet.instances[y].NMACHINES+"x"+GPstate.validationSet.instances[y].NPERIODS+" RS: "+GPstate.validationSet.instances[y].RANDOMSEED);
                    Cell cellFitnessValidationIndex = rowFitnessValidation.createCell(0);
                    cellFitnessValidationIndex.setCellValue(GPstate.validationSet.instances[y].NPRODUCTS+"x"+GPstate.validationSet.instances[y].NMACHINES+"x"+GPstate.validationSet.instances[y].NPERIODS+" RS: "+GPstate.validationSet.instances[y].RANDOMSEED);
                }
                for (int z = 1; z < state.numGenerations+1; z++) {
                    Cell cellTitleIndividualsValidation = titleRowIndividualsValidation.createCell(z);
                    cellTitleIndividualsValidation.setCellValue(z);
                    Cell cellTitleFitnessValidation = titleRowFitnessValidation.createCell(z);
                    cellTitleFitnessValidation.setCellValue(z);
                    Cell cellIndividualsValidation = rowIndividualValidation.createCell(z);
                    cellIndividualsValidation.setCellValue(individualsPerGenerationValidation[z-1][x][y]);
                    Cell cellFitnessValidation = rowFitnessValidation.createCell(z);
                    cellFitnessValidation.setCellValue(fitnessesPerGenerationValidation[z-1][x][y]);
                }
                rowCount++;
            }
        }
        // get current job
        int currentJob = (int) state.job[0];
        //System.out.println("job: " + currentJob);

        // get path name from parameters
        Parameter p;
        p = new Parameter(P_PATH_RESULTS);
        pathResults = state.parameters.getString(p, null);

        // create folder if not exist
        try {
            Files.createDirectories(Paths.get(pathResults+"run"+currentJob+"/"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        try {
            //Write the workbook in file system
            FileOutputStream outIndividuals = new FileOutputStream(new File(pathResults+"run"+currentJob+"/" + "individuals.xlsx"));
            workbookIndividuals.write(outIndividuals);
            outIndividuals.close();
            FileOutputStream outFitnesses = new FileOutputStream(new File(pathResults+"run"+currentJob+"/" +"fitnesses.xlsx"));
            workbookFitnesses.write(outFitnesses);
            outFitnesses.close();
            FileOutputStream outFitnessesValidation = new FileOutputStream(new File(pathResults+"run"+currentJob+"/" +"fitnesses_validation.xlsx"));
            workbookFitnessValidation.write(outFitnessesValidation);
            outFitnessesValidation.close();
            FileOutputStream outIndividualsValidation = new FileOutputStream(new File(pathResults+"run"+currentJob+"/" +"individuals_validation.xlsx"));
            workbookIndividualsValidation.write(outIndividualsValidation);
            outIndividualsValidation.close();
            //System.out.println("schedule.xlsx written successfully on disk.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
