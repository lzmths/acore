package optimization.runners;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.uma.jmetal.algorithm.multiobjective.mosa.MOSA;
import org.uma.jmetal.algorithm.multiobjective.mosa.cooling.CoolingScheme;
import org.uma.jmetal.algorithm.multiobjective.mosa.cooling.impl.Exponential;
import org.uma.jmetal.algorithm.multiobjective.nsgaiii.NSGAIII;
import org.uma.jmetal.algorithm.multiobjective.nsgaiii.NSGAIIIBuilder;
import org.uma.jmetal.operator.crossover.CrossoverOperator;
import org.uma.jmetal.operator.mutation.MutationOperator;
import org.uma.jmetal.operator.selection.SelectionOperator;
import org.uma.jmetal.operator.selection.impl.NaryTournamentSelection;
import org.uma.jmetal.operator.selection.impl.TournamentSelection;
import org.uma.jmetal.problem.Problem;
import org.uma.jmetal.util.archive.BoundedArchive;
import org.uma.jmetal.util.archive.impl.CrowdingDistanceArchive;
import org.uma.jmetal.util.comparator.DominanceComparator;
import org.uma.jmetal.util.evaluator.impl.SequentialSolutionListEvaluator;

import optimization.runners.paretofront.HandlerParetoFront;
import optimization.runners.paretofront.MinimumBCGradeOfImprovement;
import optimization.specification.Spec;
import optimization.specification.SpecificationMutation;
import optimization.specification.SpecificationProblem;
import optimization.specification.SpecificationSolution;
import optimization.specification.SpeficationCrossover;
import owl.ltl.Formula;

public class NSGAIIIRunner implements AlgorithmRunner {

	private List<SpecificationSolution> solutions = new ArrayList<SpecificationSolution>();

	private List<Formula> boundaryConditions;
	
	private Spec spec;
	
	private int maxPopulation;
	
	private int maxEvaluation;
	
	private int numberOfParents;
	
	private int numberOfChildren;
		
	private double mutationProbability;

	private double crossoverProbability;

	private Random random;
	
	private int gaGuaranteesPreferenceFactor;
	
	private boolean onlyInputsInAssumptions;
	
	private int gaGeneNumOfMutations;
	
	private long computingTime;
	
	public NSGAIIIRunner(int maxPopulation, int maxEvaluation, double mutationProbability, double crossoverProbability, Random random, 
			int gaGuaranteesPreferenceFactor, boolean onlyInputsInAssumptions, int gaGeneNumOfMutations) {
		this.maxEvaluation = maxEvaluation;
		//this.numberOfParents = numberOfParents;
		//this.numberOfChildren = numberOfChildren;
		this.maxPopulation = maxPopulation;
		this.mutationProbability = mutationProbability;
		this.crossoverProbability = crossoverProbability;
		this.random = random;
		this.gaGuaranteesPreferenceFactor = gaGuaranteesPreferenceFactor;
		this.onlyInputsInAssumptions = onlyInputsInAssumptions;
		this.gaGeneNumOfMutations = gaGeneNumOfMutations;
	}
	
	@Override
	public List<SpecificationSolution> run(Spec originalSpec, List<Formula> boundaryConditions) {
		this.spec = originalSpec;
		this.boundaryConditions = boundaryConditions;
		try {
			Problem<SpecificationSolution> problem = new SpecificationProblem(originalSpec, boundaryConditions);
			MutationOperator<SpecificationSolution> mutationOperator = new SpecificationMutation(this.mutationProbability, 
					this.random, this.gaGuaranteesPreferenceFactor, this.onlyInputsInAssumptions, this.gaGeneNumOfMutations);
			CrossoverOperator<SpecificationSolution> crossoverOperator = new SpeficationCrossover(this.crossoverProbability, this.random, 
					this.gaGuaranteesPreferenceFactor, this.onlyInputsInAssumptions);
			SelectionOperator<List<SpecificationSolution>, SpecificationSolution> selection = new NaryTournamentSelection<SpecificationSolution>(4, 
					new DominanceComparator<SpecificationSolution>());
			
			NSGAIIIBuilder<SpecificationSolution> nsgaiiiBuilder = new NSGAIIIBuilder<SpecificationSolution>(problem);
			nsgaiiiBuilder.setCrossoverOperator(crossoverOperator);
			nsgaiiiBuilder.setMutationOperator(mutationOperator);
			nsgaiiiBuilder.setMaxIterations(this.maxEvaluation/this.maxPopulation);
			//nsgaiiiBuilder.setMaxIterations(400);
			nsgaiiiBuilder.setSelectionOperator(selection);
			//nsgaiiiBuilder.setNumberOfDivisions(6);
			
			NSGAIII<SpecificationSolution> nsgaiii = new NSGAIII<SpecificationSolution>(nsgaiiiBuilder);
			nsgaiii.setMaxPopulationSize(this.maxPopulation);
			
			org.uma.jmetal.example.AlgorithmRunner exec = new org.uma.jmetal.example.AlgorithmRunner.Executor(nsgaiii).execute();
			this.computingTime = exec.getComputingTime();
			//TODO - Check if removes all BCs. It includes initial population in the solution.
		    this.solutions = nsgaiii.getResult();
		    printSolutions(this.solutions);
		    HandlerParetoFront paretoFront = new MinimumBCGradeOfImprovement();
		    this.solutions = paretoFront.filter(this.solutions);
		    printSolutions(this.solutions);
		} catch (IOException | InterruptedException e) {
			e.printStackTrace();
		}
		return this.solutions;
	}

	private void printSolutions(List<SpecificationSolution> solutions) {
	    System.out.println("Solution SIZE: " + this.solutions.size());
	    for (SpecificationSolution sol : solutions) {
	    	System.out.println("------");
	    	System.out.println(sol.spec);
	    	System.out.println(sol.objectives()[0]);
	    	System.out.println(sol.objectives()[1]);
	    	System.out.println(sol.objectives()[2]);
	    	System.out.println(sol.objectives()[3]);
	    	System.out.println("------");
	    }
	}

	@Override
	public List<SpecificationSolution> getSolutions() {
		return this.solutions;
	}

	@Override
	public List<Formula> getBoundaryConditions() {
		return this.boundaryConditions;
	}

	@Override
	public Spec getOriginalSpecification() {
		return this.spec;
	}

	@Override
	public String printExecutionTime() {
		return String.valueOf(this.computingTime);
	}

}
