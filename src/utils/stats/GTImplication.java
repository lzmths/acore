package utils.stats;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.lang3.tuple.Pair;

import optimization.specification.Spec;
import owl.ltl.Conjunction;
import owl.ltl.Disjunction;
import owl.ltl.Formula;
//import solvers.LTLSolver;
import utils.Formula_Utils;

public class GTImplication {

	public final static String alg = "nsgaiii";
	public final static String casesFolder = "/tmp/cases/";

	public static void main(String[] args) {
		try {
			dCNF.LTLSolver.SATID = "_999923132";
			dCNF.LTLSolver.init();
			/*
			solvers.LTLSolver.SATID = "_999923132";
			solvers.LTLSolver.init();
			*/
			//simple_arbiter_v2();
			//detector();
			//round_robin();
			//simple_arbiter_v1();
			//ltl2dba27();
			arbiter();
			//prioritized_arbiter();
			//simple_arbiter_v2();
			dCNF.LTLSolver.clear();
			//solvers.LTLSolver.clear();
		} catch (IOException | InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * Computes if repair specification is weakest then genuine specification (same domain)
	 * @param genuine
	 * @param repair
	 * @return true or false in order to answer the mentioned question
	 * @throws InterruptedException 
	 * @throws IOException 
	 */
	public static boolean weakest_comparison(Spec genuine, Spec repair) throws IOException, InterruptedException {
		List<Formula> gGoals = genuine.getGoals();
		List<Formula> rGoals = repair.getGoals();
		Formula notDisjunctionOfrGoals;
		Formula conjunctionOfgGoals;
		System.out.println(gGoals);
		System.out.println(rGoals);
		if (rGoals.size() <= 1) {
			notDisjunctionOfrGoals = rGoals.get(0).not();
		} else {
			notDisjunctionOfrGoals = Disjunction.of(rGoals.get(0).not(),rGoals.get(1).not());
			for (int i = 2; i < rGoals.size(); ++i) {
				notDisjunctionOfrGoals = Disjunction.of(notDisjunctionOfrGoals,rGoals.get(i).not());
			}	
		}
		if (gGoals.size() <= 1) {
			conjunctionOfgGoals = gGoals.get(0);
		} else {
			conjunctionOfgGoals = Conjunction.of(gGoals.get(0),gGoals.get(1));
			for (int i = 2; i < gGoals.size(); ++i) {
				conjunctionOfgGoals = Conjunction.of(conjunctionOfgGoals,gGoals.get(i));
			}	
		}
		Formula formula = Conjunction.of(notDisjunctionOfrGoals,conjunctionOfgGoals);
		System.out.println("Formula");
		System.out.println(Formula_Utils.toGAConflictIdentificationSyntax(formula));
		System.out.println(Formula_Utils.toSolverSyntax(formula));
		System.out.println("######################");
		return false;
		//return dCNF.LTLSolver.isSAT(Formula_Utils.toGAConflictIdentificationSyntax(formula)) == dCNF.LTLSolver.SolverResult.UNSAT;
		//return LTLSolver.isSAT(Formula_Utils.toSolverSyntax(formula)) == LTLSolver.SolverResult.UNSAT;
	}
	
	private static List<Spec> getGenuineSpec_arbiter(File genuineFolder, List<String> input, List<String> output) {
		String [] lGenuines = genuineFolder.list();
		List<Spec> genuineSolutions = new LinkedList<Spec>();
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
		return genuineSolutions;
	}
	
	private static void arbiter() throws IOException, InterruptedException {
		String caseName = "arbiter";
		//File caseFolder = new File(casesFolder + "/" + caseName);
		File genuineFolder = new File(casesFolder + "/arbiter/genuine");
		List<String> input = new ArrayList<String>();
		List<String> output = new ArrayList<String>();
		input.add("a");
		input.add("r1");
		input.add("r2");
		output.add("g1");
		output.add("g2");
		//List<String> domainn = new ArrayList<String>();
		//List<String> goalss = new ArrayList<String>();
		List<Spec> lGenuineSpec = getGenuineSpec_arbiter(genuineFolder, input, output);
		for (Spec genuineSpec : lGenuineSpec) {
			for (Pair<List<String>,List<String>> repair : getArbiterRepairs(input,output)) {
				Spec repairSpec = new Spec(
						Formula_Utils.toFormulaList(repair.getLeft(), input, output),
						Formula_Utils.toFormulaList(repair.getRight(), input, output), input, output);
				System.out.println(weakest_comparison(genuineSpec,repairSpec));
			}
		}
	}
	
	private static List<Pair<List<String>,List<String>>> getArbiterRepairs(List<String> input, List<String> output) {
		List<Pair<List<String>,List<String>>> repairs = new ArrayList<Pair<List<String>,List<String>>>();
		
		List<String> domainn = new ArrayList<String>();
		List<String> goalss = new ArrayList<String>();
		goalss.add("G((a | (!g1 & !g2)))");
		goalss.add("G((!a | F(g1)))");
		goalss.add("G((!r2 | F(g2)))");
		repairs.add(Pair.of(domainn,goalss));
		
		domainn = new ArrayList<String>();
		goalss = new ArrayList<String>();
		goalss.add("G((a | (!g1 & !g2)))");
		goalss.add("G((!r2 | F(g2)))");
		goalss.add("G((!r1 | ((F(G(g1))) U ((!r2 | g2)))))");
		repairs.add(Pair.of(domainn,goalss));

		return repairs;
	}
	
}