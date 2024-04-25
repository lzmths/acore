package user.io;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import org.jgap.InvalidConfigurationException;
import org.uma.jmetal.solution.Solution;

import dCNF.LTLSolver.SolverResult;
import geneticalgorithm.fitnessfunction.DCNFEvaluator;
import gov.nasa.ltl.trans.ParseErrorException;
import gov.nasa.ltl.trans.Parser;
import ltl.owl.visitors.SolverSyntaxOperatorReplacer;
import main.BCLearner;
import utils.Formula_Utils;
import utils.Settings;
import optimization.runners.AlgorithmRunner;
import optimization.runners.MOSARunner;
import optimization.runners.NSGAIIIRunner;
import optimization.specification.Spec;
import optimization.specification.SpecificationSolution;
import owl.ltl.Conjunction;
import owl.ltl.Formula;

public class Main {

	private static List<Formula> boundaryConditions = new LinkedList<Formula>();
	private static List<String> domain = new LinkedList<String>();
	private static List<String> goals = new LinkedList<String>();
	private static List<String> ins = new LinkedList<String>();
	private static List<String> outs = new LinkedList<String>();
	private static List<Spec> genuineSolutions = new LinkedList<Spec>();
	private static int trivialBCs = 0;
	private static boolean hasGenuine =  true;
		
	public static void main(String[] args) throws IOException, InterruptedException, ExecutionException {
		List<String> bc = new LinkedList<String>();
		int popSize = 0;
		int maxNumOfInd = 0;
		int crossoverRate = 0;
		int mutationRate = 0;
		int gene_mutationRate = 0;
		int gene_num_of_mutations = 0;
		int guaranteePreferenceRate = -1;
		boolean random_GA_selector = false;
		int generations = 0;
		boolean randomGen = false;
		boolean allowGuaranteesRemoval = false;
		boolean allowAssumptionsAddition = false;
		boolean onlyInputsInAssumptions = false;
		int bound = 0;
		int surviving_BCs = 0;
		int ga_timeout = 0;
		int sat_timeout = 0;
		int mc_timeout = 0;
		double threshold = -1.0d;
		double severity = -1.0d;
		double likelihood = -1.0d;
		double boundary_factor = -1.0d;
		double syntactic_factor = -1.0d;
		double semantic_factor = -1.0d;
		double status_factor = -1.0d;
		double improvement_factor = -1.0d;
		boolean ga_enable = false;
		boolean nsgaiii_enable = false;
		
		boolean amosa_enable = false;
		double initialTemperature = 0;
		
		String outname = "";
		String outfitness = "";
		
		for (int i = 0; i< args.length; i++ ){
			if(args[i].startsWith("-Gen=")){
				generations = Integer.parseInt(args[i].replace("-Gen=", ""));
			}
			else if(args[i].startsWith("-Pop=")){
				popSize = Integer.parseInt(args[i].replace("-Pop=", ""));
			}
			else if(args[i].startsWith("-Max=")){
				maxNumOfInd = Integer.parseInt(args[i].replace("-Max=", ""));
			}
			else if(args[i].startsWith("-COR=")){
				crossoverRate = Integer.parseInt(args[i].replace("-COR=", ""));
			}
			else if(args[i].startsWith("-GPR=")){
				guaranteePreferenceRate = Integer.parseInt(args[i].replace("-GPR=", ""));
			}
			else if(args[i].startsWith("-MR=")){
				mutationRate = Integer.parseInt(args[i].replace("-MR=", ""));
			}
			else if(args[i].startsWith("-geneMR=")){
				gene_mutationRate = Integer.parseInt(args[i].replace("-geneMR=", ""));
			}
			else if(args[i].startsWith("-geneNUM=")){
				gene_num_of_mutations = Integer.parseInt(args[i].replace("-geneNUM=", ""));
			}
			else if(args[i].startsWith("-k=")){
				bound = Integer.parseInt(args[i].replace("-k=", ""));
			}
//			else if(args[i].startsWith("-no-docker")){
//				Settings.USE_DOCKER = false;
//			}
			else if(args[i].startsWith("-random")){
				randomGen = true;
			}
			else if(args[i].startsWith("-GA_random_selector")){
				random_GA_selector = true;
			}
			else if(args[i].startsWith("-removeG")){
				allowGuaranteesRemoval = true;
			}
			else if(args[i].startsWith("-addA")){
				allowAssumptionsAddition = true;
			}
			else if(args[i].startsWith("-onlyInputsA")){
				onlyInputsInAssumptions = true;
			}
			else if(args[i].startsWith("-GATO=")){
				ga_timeout = Integer.parseInt(args[i].replace("-GATO=", ""));
			}
			else if(args[i].startsWith("-SatTO=")){
				sat_timeout = Integer.parseInt(args[i].replace("-SatTO=", ""));
			}
			else if(args[i].startsWith("-MCTO=")){
				mc_timeout = Integer.parseInt(args[i].replace("-MCTO=", ""));
			}
			else if(args[i].startsWith("-threshold=")){
				threshold = Double.valueOf(args[i].replace("-threshold=", ""));
			}
			else if(args[i].startsWith("-sol=")){
				surviving_BCs = Integer.valueOf(args[i].replace("-sol=", ""));
			}
			else if (args[i].startsWith("-d=")) {
				domain.add(args[i].replace("-d=",""));
			}
			else if (args[i].startsWith("-g=")) {
				goals.add(args[i].replace("-g=",""));
			}
			else if(args[i].startsWith("-bc=")) {
				bc.add(args[i].replace("-bc=",""));
			}
			else if(args[i].startsWith("-sv=")) {
				severity = Double.valueOf(args[i].replace("-sv=", ""));
			}
			else if(args[i].startsWith("-lk=")) {
				likelihood = Double.valueOf(args[i].replace("-lk=", ""));
			}
			else if(args[i].startsWith("-ins=")) {
				ins = Arrays.asList(args[i].replace("-ins=","").split("\\s*,\\s*"));
			}
			else if(args[i].startsWith("-outs=")) {
				outs = Arrays.asList(args[i].replace("-outs=","").split("\\s*,\\s*"));
			}
			else if(args[i].startsWith("-out=")) {
				outname = args[i].replace("-out=","");
				solvers.LTLSolver.SATID = outname;
				solvers.LTLSolver.init();
			}
			else if(args[i].startsWith("-outfitness=")) {
				outfitness = args[i].replace("-outfitness=","");
			}
			else if(args[i].startsWith("-BCF=")) {
				boundary_factor = Double.valueOf(args[i].replace("-BCF=",""));
			}
			else if(args[i].startsWith("-SYNF=")) {
				syntactic_factor = Double.valueOf(args[i].replace("-SYNF=",""));
			}
			else if(args[i].startsWith("-SEMF=")) {
				semantic_factor = Double.valueOf(args[i].replace("-SEMF=",""));
			}
			else if(args[i].startsWith("-STF=")) {
				status_factor = Double.valueOf(args[i].replace("-STF=",""));
			}
			else if(args[i].startsWith("-IMPF=")) {
				improvement_factor = Double.valueOf(args[i].replace("-IMPF=",""));
			}
			else if(args[i].startsWith("-GA=")) {
				ga_enable = Boolean.valueOf(args[i].replace("-GA=",""));
			}
			else if(args[i].startsWith("-NSGAIII=")) {
				nsgaiii_enable = Boolean.valueOf(args[i].replace("-NSGAIII=",""));
			}
			else if(args[i].startsWith("-AMOSA=")) {
				amosa_enable = Boolean.valueOf(args[i].replace("-AMOSA=",""));
				if (amosa_enable && ((i + 1) < args.length) && args[i + 1].startsWith("-initialTemperature=")) {
					initialTemperature = Double.valueOf(args[i + 1].replace("-initialTemperature=",""));
					++i;
				}
			}
			else if(args[i].startsWith("-bcRef=")) {
				String bc_references = args[i].replace("-bcRef=","");
				Path pathToFile = Paths.get(bc_references);
				List<String> allLines = Files.readAllLines(pathToFile.toAbsolutePath());
				for (String line : allLines) {
					bc.add(line);
				}
			}
            else if (args[i].startsWith("-dirGenRef=")){
                String directoryName = args[i].replace("-dirGenRef=","");
                List<Path> specifications = Files.walk(Paths.get(directoryName)).filter(Files::isRegularFile).collect(Collectors.toList());
                
                List<String> domainn = new LinkedList<String>();
                List<String> goalss = new LinkedList<String>();
                
                for (Path pathToFile : specifications) {
                	domainn.clear();
                	goalss.clear();
    				List<String> allLines = Files.readAllLines(pathToFile.toAbsolutePath());
    				
    				for (String line : allLines) {
    					if (line.startsWith("-d=")) {
							domainn.add(line.replace("-d=",""));
    					}
    					else if (line.startsWith("-g=")) {
    						goalss.add(line.replace("-g=",""));
    					}
    				}
    				genuineSolutions.add(new Spec(
    						Formula_Utils.toFormulaList(domainn, ins, outs),
    						Formula_Utils.toFormulaList(goalss, ins, outs), ins, outs));
                }
            } else if (args[i].startsWith("-initialTemperature=")) {
            	
            }
			else {
				correctUssage();
				return;
			}
		}
		// Filter non-equivalent genuine solutions
		/*
		List<Formula> aux = new LinkedList<Formula>();
		for (Spec g : genuineSolutions)
			aux.add(g.getFormula());
		aux = Formula_Utils.getNonEquivalentSolutions(aux);
		for (Spec g : genuineSolutions) {
			if (!aux.contains(g.getFormula()))
				genuineSolutions.remove(g);
		}
		*/
		System.out.println("Domain:" + domain.size());
		System.out.println("Goals:" + goals.size());
		System.out.println("In:" + ins.size());
		System.out.println("Out:" + outs.size());
		
		genuineSolutions = Formula_Utils.getNonEquivalentSolutionsFromSpecs(genuineSolutions);
		
		System.out.println("Amount of Non-Equivalent Genuine Solutions: " +  genuineSolutions.size());
		for (Spec g : genuineSolutions)
			System.out.println(g.toFormula());
		hasGenuine = genuineSolutions.size() == 0 ? false : true;
				
		// GA configuration settings
		if (popSize > 0) Settings.GA_POPULATION_SIZE = popSize;
		if (maxNumOfInd > 0) Settings.GA_MAX_NUM_INDIVIDUALS = maxNumOfInd;
		if (crossoverRate > 0) Settings.GA_CROSSOVER_RATE = crossoverRate;
		if (mutationRate > 0) Settings.GA_MUTATION_RATE = mutationRate;
		if (gene_mutationRate > 0) Settings.GA_GENE_MUTATION_RATE = gene_mutationRate;
		if (gene_num_of_mutations >= 0) Settings.GA_GENE_NUM_OF_MUTATIONS = gene_num_of_mutations;
		if (threshold >= 0.0d) Settings.GA_THRESHOLD = threshold;
		if (guaranteePreferenceRate >= 0) Settings.GA_GUARANTEES_PREFERENCE_FACTOR = guaranteePreferenceRate;
		if (generations > 0) Settings.GA_GENERATIONS = generations;
		if (ga_timeout > 0) Settings.GA_EXECUTION_TIMEOUT = ga_timeout;

		//set solution criterion
		if (surviving_BCs > 0 && surviving_BCs < boundaryConditions.size())
			Settings.GA_SURVIVING_BCs = surviving_BCs;

		// other configurations
		if (sat_timeout > 0) Settings.SAT_TIMEOUT = sat_timeout;
		if (mc_timeout > 0) Settings.MC_TIMEOUT = mc_timeout;
		if (bound > 0) Settings.MC_BOUND = bound;
		if (allowAssumptionsAddition) Settings.allowAssumptionAddition = true;
		if (allowGuaranteesRemoval) Settings.allowGuaranteeRemoval = true;
		if (onlyInputsInAssumptions) Settings.only_inputs_in_assumptions = true;
		if (random_GA_selector) Settings.GA_RANDOM_SELECTOR = true;
		//fitness factors
		if (likelihood >= 0.0d) Settings.LIKELIHOOD_FACTOR = likelihood;
		if (severity >= 0.0d) Settings.SEVERITY_FACTOR = severity;
		if (boundary_factor >= 0.0d) Settings.BC_FACTOR = boundary_factor;
		if (semantic_factor >= 0.0d) Settings.WON_MODELS_FACTOR = semantic_factor/2.0d;
		if (semantic_factor >= 0.0d) Settings.LOST_MODELS_FACTOR = semantic_factor/2.0d;
		if (syntactic_factor >= 0.0d) Settings.SYNTACTIC_FACTOR = syntactic_factor;
		if (status_factor >= 0.0d) Settings.STATUS_FACTOR = status_factor;
		if (improvement_factor >= 0.0d) Settings.IMP_FACTOR = improvement_factor;

		if (outfitness != "") {
			if (randomGen) {
				Settings.FITNESS_RESULT_PATH = outfitness + "_random";
			} else {
				Settings.FITNESS_RESULT_PATH = outfitness;
			}
		}
		
		List<Formula> guarantees = Formula_Utils.toFormulaList(goals, ins, outs);
		List<Formula> dom_properties = Formula_Utils.toFormulaList(domain, ins, outs);
		Spec original_spec = new Spec(dom_properties, guarantees, ins, outs);

		// remove equivalent and trivial BCs...
		boundaryConditions = Formula_Utils.toFormulaList(bc, ins, outs);
		//int initialSize = boundaryConditions.size();
		System.out.println("INITIAL BCs: " + boundaryConditions.size());
		boundaryConditions = Formula_Utils.getBoundaryConditions(original_spec, boundaryConditions);
		//int sizeAfterCheck = boundaryConditions.size();
		/*
		if (initialSize > sizeAfterCheck) {
			System.err.println("WARNING: " + (initialSize - sizeAfterCheck) + " formula(s) are/is not bc(s)");
			System.exit(1);
		}
		*/
		boundaryConditions = Formula_Utils.getNonEquivalentSolutions(boundaryConditions);
		boundaryConditions.removeIf(_bc -> {
			if (Formula_Utils.areEquivalent(_bc.not(), Conjunction.of(guarantees))) {
				trivialBCs++;
				return true;
			}
			else return false;
		});
		System.out.println("Amount of non-equivalent BCs: "+ boundaryConditions.size());
		int amount = 0;
		for (Spec s : genuineSolutions) {
			amount = 0;
			for (Formula bc22 : boundaryConditions) {
				if (s.isBoundaryCondition(bc22)) {
					amount++;
					System.out.println(bc22);
				}
			}
			System.out.println(amount);
		}
		System.out.println("-");

		AlgorithmRunner ar = null;
		
		if (randomGen) {
			outname = outname + "_random";
			//ar.runRandom(original_spec, boundaryConditions);
			return;
		} else {
			int algorithmCount = 0;
			if (nsgaiii_enable) {
				++algorithmCount;
				ar = new NSGAIIIRunner(popSize, maxNumOfInd, Settings.GA_MUTATION_RATE, Settings.GA_CROSSOVER_RATE, Settings.RANDOM_GENERATOR, Settings.GA_GUARANTEES_PREFERENCE_FACTOR, 
						Settings.only_inputs_in_assumptions, Settings.GA_GENE_NUM_OF_MUTATIONS);
				System.out.println("NSGAIII: " + nsgaiii_enable);
			} else if (amosa_enable && (initialTemperature > 0)) {
				++algorithmCount;
				ar = new MOSARunner(popSize, maxNumOfInd, initialTemperature, 
						Settings.GA_MUTATION_RATE, Settings.RANDOM_GENERATOR, Settings.GA_GUARANTEES_PREFERENCE_FACTOR, 
						Settings.only_inputs_in_assumptions, Settings.GA_GENE_NUM_OF_MUTATIONS);
				System.out.println("AMOSA: " + amosa_enable);
			} else if (ga_enable) {
				//++algorithmCount;
				//System.out.println("GA: " + ga_enable);
			}
			if (algorithmCount == 1) {
				ar.run(original_spec, boundaryConditions);
			} else if (algorithmCount == 0) {
				System.out.println("Please, you must enable an algorithm and its parameters. For example, NSGAIII, GA, or AMOSA (initialTemperature > 0)");
			} else {
				System.out.println("Please, you must enable only one algorithm among NSGAIII, GA, or AMOSA");
			}
		}
		
		computeStatistics(ar, outname);

		solvers.LTLSolver.clear();
	}
	
	/**
	private static List<SpecificationSolution> sortedSolutions(List<SpecificationSolution> solutions) {
		Comparator<SpecificationSolution> compare = new Comparator<SpecificationSolution>() {
			@Override
			public int compare(SpecificationSolution o1, SpecificationSolution o2) {
				if (o1.fitness > o2.fitness) {
					return -1;
				}
				return 1;
			}
		};
		List<SpecificationSolution> sortedSolutions = new ArrayList<>(solutions);
		Collections.sort(sortedSolutions, compare);
		return sortedSolutions;
	}
	**/
	
	private static List<main.Solution> getWeakestSolutions(List<main.Solution> eq1, List<main.Solution> eq2) {
		List<main.Solution> equivalentSolutions = new ArrayList<>();
		equivalentSolutions.addAll(eq1);
		equivalentSolutions.addAll(eq2);
		List<main.Solution> weakest = new LinkedList();

		dCNF.LTLSolver.SATID = solvers.LTLSolver.SATID + "_BCLearn_checkWeakest";
		dCNF.LTLSolver.init();
		for (int i=0;i<equivalentSolutions.size();i++) {
			main.Solution f1 = equivalentSolutions.get(i);
			boolean f1IsImplied = false;
			for (int j=0; j <equivalentSolutions.size(); j++ ) {
				if (i==j)
					continue;
				main.Solution f2 = equivalentSolutions.get(j);
				Set<gov.nasa.ltl.trans.Formula<String>> s = new HashSet<>();
				s.add(f1.BC);
				s.add(gov.nasa.ltl.trans.Formula.Not(f2.BC));
						
				SolverResult sat = SolverResult.UNSAT;
				try{ sat = DCNFEvaluator.checkSAT(s); }
				catch (Exception e) {e.printStackTrace();}
				if (sat==SolverResult.UNSAT) {
					f1IsImplied = true;
					break;
				} 
			}
			if (!f1IsImplied)
				weakest.add(f1);
		}
		dCNF.LTLSolver.clear();
		return weakest;
	}
	
	private static void printFitnessWithoutNewBCs(List<SpecificationSolution> solutions) {
		int count = 0;
		File output = new File(Settings.FITNESS_RESULT_PATH + "_withoutNewBCs");
		printHeadWithStatisticsWithoutNewBCs(output);
		for (SpecificationSolution s : solutions) {
			printFitnessFromSolutionWithoutNewBCs(s, count++, output);
		}
	}
	
	private static void computeStatistics(AlgorithmRunner ar, String outname) throws IOException, InterruptedException {
		List<Integer> SurvivalBcsgenuineSolutions = new LinkedList<Integer>();
		Integer res;
		
		printFitnessWithoutNewBCs(ar.getSolutions());
		
		System.out.println("Fitness of the solutions in " + Settings.FITNESS_RESULT_PATH);
		printHeadWithStatistics(new File(Settings.FITNESS_RESULT_PATH));
		
		
		for (Spec s : genuineSolutions) {
			res = 0;
			for (Formula bc : boundaryConditions) {
				if (s.isBoundaryCondition(bc)) res++;
			}
			SurvivalBcsgenuineSolutions.add(res);
		}
		int[] amounts =  new int[SurvivalBcsgenuineSolutions.size()+1];
		
		//write results to file
		File file = null;
		if (outname == null)
			file = new File("ga_results.out");
		else
			file = new File(outname);
		
		file.getParentFile().mkdirs();
		file.createNewFile();
		FileWriter writer = new FileWriter(file, false);
		writer.write("");
		
		int genuineFound = 0; int zeroBcs = 0;
		List<Integer> aux = new LinkedList<Integer>(SurvivalBcsgenuineSolutions);
		Collections.sort(aux);
		List<Integer> genuines = new LinkedList<Integer>();
		List<Integer> genuinesIndex = new LinkedList<Integer>();
		for (int i = 0; i < ar.getSolutions().size(); i++) {
			SpecificationSolution s = ar.getSolutions().get(i);
			Integer index = isGenuine(s.spec);
			if (index != null) {
				genuineFound++;
				genuines.add(i);
				genuinesIndex.add(index);
			}
			if (s.survivalBC.size() == 0) zeroBcs++;
			
			for (int j = 0; j<aux.size(); j++) {
				if (s.survivalBC.size() <= aux.get(j)) {
					amounts[j] += 1;
					break;
				}
			}
			//TODO - Add HV and IGD
			//writer.write(String.format("Solution N: %s\t Fitness: %.2f \n", i, s.fitness));
			writer.write(String.format("Solution N: \n", i));
			
			writer.write("Domain: " + s.spec.getAssume() + "/n");
			writer.write("Goal: " + s.spec.getGoals() + "/n");
			
			writer.write("Specification: " + s.spec.toLabelledString() + "\n");
			writer.write("Assumptions -> Goals: " + s.spec.toFormula() + "\n");
			writer.write("CONSISTENT: " + (s.status == SpecificationSolution.SPEC_STATUS.CONSISTENT) + "\n");
			writer.write("Amount of survival BCS: " + (double)s.survivalBC.size() + "\n");
			writer.write("Percentage of survival BCS: " + (double)s.survivalBC.size()/(double)ar.getBoundaryConditions().size() + "\n");
			writer.write("\n");
			writer.flush();
		}
		writer.write("Amount of non-equivalent BCs: "+ boundaryConditions.size() + "\n");
		writer.write("Amount of false positive removed (Conjunction(Guarantees) == bc): "+ trivialBCs + "\n");
		writer.write("Amount of Genuine Solutions: "+ genuineSolutions.size() + "\n");
		if (hasGenuine) {
			for (int i = 0; i<SurvivalBcsgenuineSolutions.size(); i++)
				writer.write("Amount of survival Bcs in Genuine Solution "+ i + " (" + genuineSolutions.get(i).toLabelledString()  +") " + SurvivalBcsgenuineSolutions.get(i) + "\n");
		}
		writer.write("Amount of solutions with zero Bcs: " + zeroBcs + "\n");
		// fill the amount of specs with more bcs than all genuine solutions
		if (hasGenuine) {
			amounts[genuineSolutions.size()] = ar.getSolutions().size();
			for (int i = 0; i<genuineSolutions.size(); i++)
				amounts[genuineSolutions.size()] -= amounts[i];
			writer.write("Amount of equivalent solutions to some genuine: " + genuineFound + " " + genuines +"\n");
			writer.write("Corresponding index of each genuine solution found: " + genuinesIndex +"\n");
			writer.write("Amount of genuine solutions found: " + (new HashSet<Integer>(genuinesIndex)).size() +"\n");
			for (int i = 0; i<genuineSolutions.size(); i++)
				writer.write("Amount of solutions with less survival bcs " + "than " + (genuineSolutions.size()-i) + " genuines: "  + amounts[i] +" " + ((double)amounts[i]/(double)ar.getSolutions().size() )+"\n");
			writer.write("Amount of solutions with more survival bcs " + "than all genuines " + amounts[genuineSolutions.size()] +" " + ((double)amounts[genuineSolutions.size()]/(double)ar.getSolutions().size() ) +"\n");
		}

		writer.flush();

		writer.write(ar.printExecutionTime() + "\n");
		writer.write(Settings.print_settings() + "\n");
		writer.flush();
		
		int[] num_of_new_BCs = new int[ar.getSolutions().size()];
		List<Integer> repairsNoNewBCs = new LinkedList<>();
		
		//List<SpecificationChromosome> sortedSolutions = sortedSolutions(ga.solutions);
		List<SpecificationSolution> solutions = ar.getSolutions();
		if (Settings.GA_CHECK_NEW_BCs) {
			int pos = 0;
			int limit = 20;
			for(SpecificationSolution s : solutions) {
				int new_BCs = 100000;
				if (pos <= limit) {
					List<main.Solution> weakest;
					try {
						//weakest = computeRepairsUsefulness(s,outname);
						//new_BCs = weakest.size();
						printFitnessFromSolution(s, new_BCs, pos, new File(Settings.FITNESS_RESULT_PATH));
					}
					catch (Exception e) {
						e.printStackTrace();
					}
					//List<Integer> new_bcs_l = new ArrayList<>();
					//while ((boundaryConditions.size() - new_BCs) > 0) {
					//int threshold = 1;
					//while (weakestAccumulated.size() < boundaryConditions.size()) {
						//new_bcs_l.add(new Integer(new_BCs));
						//++threshold;
						//if (threshold == 5) {
						//	System.out.println("Resilient solution: " + s.toString());
						//	break;
						//}
					//}
					
					num_of_new_BCs[pos] = new_BCs;
					if (new_BCs <= Settings.GA_ALLOWED_NEW_BCs)
						repairsNoNewBCs.add(pos);
				} else {
					printFitnessFromSolution(s, new_BCs, pos, new File(Settings.FITNESS_RESULT_PATH));
				}
				pos++;
			}
		} else {
			int id = 0;
			for(SpecificationSolution s : solutions) {
				printFitnessFromSolution(s, Integer.MIN_VALUE, id, new File(Settings.FITNESS_RESULT_PATH));
				++id;
			}
		}
		if (Settings.GA_CHECK_NEW_BCs) {
			writer.write("Amount of repairs with no new BCs: " + repairsNoNewBCs.size() + "\n");
			writer.write("Repairs with no new BCs: " + repairsNoNewBCs + "\n");
		}
		
		writer.close();
		System.exit(0);
	}
	
	public static void printHeadWithStatisticsWithoutNewBCs(File outputFile) {
		try(FileWriter fw = new FileWriter(outputFile)) {
			String firstLine = "";
			firstLine += "id";
			//firstLine += "," + "fitness";
			firstLine += "," + "status_fitness";
			firstLine += "," + "bcGradeOfImprovement";
			firstLine += "," + "removedBCLikelihood";
			firstLine += "," + "survivalBCLikelihood";
			firstLine += "," + "lost_models_fitness";
			firstLine += "," + "won_models_fitness";
			firstLine += "," + "syntactic_distance";
			firstLine += "," + "initialBCs-Non-Equivalent";
			fw.write(firstLine + "\n");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void printHeadWithStatistics(File outputFile) {
		try(FileWriter fw = new FileWriter(outputFile)) {
			String firstLine = "";
			firstLine += "id";
			//firstLine += "," + "fitness";
			firstLine += "," + "status_fitness";
			firstLine += "," + "bcGradeOfImprovement";
			firstLine += "," + "removedBCLikelihood";
			firstLine += "," + "survivalBCLikelihood";
			firstLine += "," + "lost_models_fitness";
			firstLine += "," + "won_models_fitness";
			firstLine += "," + "syntactic_distance";
			firstLine += "," + "initialBCs-Non-Equivalent";
			firstLine += "," + "newBCs";
			fw.write(firstLine + "\n");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void printFitnessFromSolution(SpecificationSolution c, int newBCs, int id, File outputFile) {
		try(FileWriter fw = new FileWriter(outputFile, true)) {
			//String line = id + "," + c.fitness + "," + c.status_fitness;
			String line = id + "," + (1.0d - c.status_fitness);
			line += "," + (1.0d - c.bcGradeOfImprovement);
			line += "," + (1.0d - c.removedBCLikelihood);
			line += "," + (1.0d - c.survivalBCLikelihood);
			line += "," + (1.0d - c.lost_models_fitness);
			line += "," + (1.0d - c.won_models_fitness);
			line += "," + (1.0d - c.syntactic_distance);
			line += "," + boundaryConditions.size();
			line += "," + newBCs;
			fw.write(line + "\n");
			++id;
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void printFitnessFromSolutionWithoutNewBCs(SpecificationSolution c, int id, File outputFile) {
		try(FileWriter fw = new FileWriter(outputFile, true)) {
			//String line = id + "," + c.fitness + "," + c.status_fitness;
			String line = id + "," + (1.0d - c.status_fitness);
			line += "," + (1.0d - c.bcGradeOfImprovement);
			line += "," + (1.0d - c.removedBCLikelihood);
			line += "," + (1.0d - c.survivalBCLikelihood);
			line += "," + (1.0d - c.lost_models_fitness);
			line += "," + (1.0d - c.won_models_fitness);
			line += "," + (1.0d - c.syntactic_distance);
			line += "," + boundaryConditions.size();
			fw.write(line + "\n");
			++id;
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static List<main.Solution> computeRepairsUsefulness(SpecificationSolution solution, String outname) throws ParseErrorException, InvalidConfigurationException {
		Spec repair = solution.getSpec();
		Set<String> dom = new HashSet<>();
		SolverSyntaxOperatorReplacer visitor  = new SolverSyntaxOperatorReplacer();
		for (Formula d : repair.getAssume()) {
//			LabelledFormula dom_form = LabelledFormula.of(d, repair.getVariables());
			d = d.accept(visitor);
			System.out.println(Formula_Utils.toGAConflictIdentificationSyntax(d));
			dom.add(Formula_Utils.toGAConflictIdentificationSyntax(d));
		}

		Set<String> goals = new HashSet<>();
		for (Formula g : repair.getGoals()) {
			g = g.accept(visitor);
			goals.add(Formula_Utils.toGAConflictIdentificationSyntax(g));
		}

		System.out.println("Checking new BCs...");
		System.out.println(dom);
		System.out.println(goals);
		
		BCLearner learner = new main.BCLearner(dom, goals);
		Parser.propositions.clear();
		BCLearner.GENERATIONS = 10;
		BCLearner.CHROMOSOMES_SIZE = 200;
		
		System.out.println(solvers.LTLSolver.SATID);
		dCNF.LTLSolver.SATID = solvers.LTLSolver.SATID + "_BCLearn";
		
		System.out.println(dCNF.LTLSolver.SATID);

		dCNF.LTLSolver.init();
		learner.learnBC();
		dCNF.LTLSolver.clear();
		
		System.out.println("Finished the check");

		return learner.weakest;
		
		//return learner.solutions.size();
	}
	
	private static Integer isGenuine(Spec spec) {
		for (Spec s : genuineSolutions) {
			if (Formula_Utils.areEquivalent(s.getFormula(), spec.getFormula()))
				return genuineSolutions.indexOf(s);
		}
		return null;
	}

	private static void correctUssage(){
		System.out.println("Please, consider the README or the example.");
	}
	
}
