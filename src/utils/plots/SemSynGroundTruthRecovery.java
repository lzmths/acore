package utils.plots;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import optimization.specification.Spec;
import optimization.specification.SpecificationProblem;
import optimization.specification.SpecificationSolution;
import owl.ltl.Formula;
import tech.tablesaw.api.DoubleColumn;
import tech.tablesaw.api.StringColumn;
import tech.tablesaw.api.Table;
import tech.tablesaw.columns.Column;
import tech.tablesaw.plotly.Plot;
import tech.tablesaw.plotly.api.ScatterPlot;
import tech.tablesaw.plotly.components.Figure;
import utils.Formula_Utils;

public class SemSynGroundTruthRecovery {

	private Table table;
	private Column syn;
	private Column sem;
	private Column specification;
	private List<SpecificationSolution> lSpecSolution;
	
	public SemSynGroundTruthRecovery() {
		this.table = Table.create();
		this.syn = DoubleColumn.create("Syntax");
		this.sem = DoubleColumn.create("Semantic");
		this.specification = StringColumn.create("Specification");
		this.lSpecSolution = new ArrayList<SpecificationSolution>();
	}
	
	public Table getSemSynTable() {
		return this.table;
	}
	
	public List<SpecificationSolution> getSpecList() {
		return this.lSpecSolution;
	}
	
	public void plot(String title) {
		this.table.addColumns(this.syn);
		this.table.addColumns(this.sem);
		this.table.addColumns(this.specification);
		Figure fig = ScatterPlot.create(title, this.table, "Syntax", "Semantic", "Specification");
		Plot.show(fig);
	}
	
	public void addSpecification(Spec originalSpec, File genuineFolder, List<String> input, List<String> output, List<Formula> bcs, 
			String specificationName) {
		List<SpecificationSolution> lSpecSol = recoveryFitness(originalSpec, genuineFolder, input, output, bcs, specificationName);
		for (SpecificationSolution specSolution : lSpecSol) {
			this.syn.append(new Double(1.0d - specSolution.syntactic_distance));
			this.sem.append(new Double(1.0d - ((specSolution.lost_models_fitness + specSolution.won_models_fitness) / 2)));
			this.specification.append(specificationName);
		}
		this.lSpecSolution.addAll(lSpecSol);
	}
	
	private List<SpecificationSolution> recoveryFitness(Spec originalSpec, File genuineFolder, List<String> input, List<String> output, List<Formula> bcs, 
			String specificationName) {
		String [] lGenuines = genuineFolder.list();
		List<Spec> genuineSolutions = new LinkedList<Spec>();
		List<String> bc = new LinkedList<String>();
		for (String genuine : lGenuines) {
			if (genuine.endsWith(".spec") || genuine.endsWith(".form")) {
				List<String> strDomain = new ArrayList<String>();
				List<String> strGoal = new ArrayList<String>();
				File specFile = new File(genuineFolder.getAbsoluteFile() + "/" + genuine);
				String line = "";
				BufferedReader br;
				try {
					br = new BufferedReader(new FileReader(specFile));
					while((line = br.readLine()) != null) {
						if (line.startsWith("-d=")) {
							line = line.replace("-d=", "");
							strDomain.add(line);
						} else if (line.startsWith("-g=")) {
							line = line.replace("-g=", "");
							strGoal.add(line);
						}
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
				genuineSolutions.add(new Spec(
						Formula_Utils.toFormulaList(strDomain, input, output),
						Formula_Utils.toFormulaList(strGoal, input, output), input, output));
			}
		}
		return calculateFitness(originalSpec, genuineSolutions, bcs);
	}
	
	private List<SpecificationSolution> calculateFitness(Spec originalSpec, List<Spec> genuineSolutions, List<Formula> bcs) {
		List<SpecificationSolution> lSpecificationSolution = new ArrayList<SpecificationSolution>();
		solvers.LTLSolver.SATID = "id3";
		solvers.LTLSolver.init();
		for (Spec genuine : genuineSolutions) {
			try {
				SpecificationProblem specProblem = new SpecificationProblem(originalSpec, bcs);
				SpecificationSolution specSolution = new SpecificationSolution(genuine);
				specProblem.calculate(specSolution);
				lSpecificationSolution.add(specSolution);
			} catch (IOException | InterruptedException e) {
				e.printStackTrace();
			}			
		}
		solvers.LTLSolver.clear();
		return lSpecificationSolution;
	}
	
}
