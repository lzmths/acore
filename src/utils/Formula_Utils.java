package utils;
import java.io.FileWriter;
import java.io.IOException;
import java.text.Normalizer;
import java.util.*;

import gov.nasa.ltl.trans.ParseErrorException;
import gov.nasa.ltl.trans.Parser;
import ltl.owl.visitors.SolverSyntaxOperatorReplacer;
import ltl.owl.visitors.SubformulaReplacer;
import main.BCLearner;
import optimization.specification.Spec;

import org.jgap.InvalidConfigurationException;

import owl.grammar.LTLParser;
import owl.ltl.*;
import owl.ltl.Formula.TemporalOperator;
import owl.ltl.parser.LtlParser;
import owl.ltl.rewriter.SyntacticSimplifier;
import solvers.LTLSolver;
import solvers.LTLSolver.SolverResult;

public class Formula_Utils {

	public static List<LabelledFormula> subformulas (LabelledFormula f) {//, List<String> variables) {
		List<LabelledFormula> s = new LinkedList<LabelledFormula>();
		
		for (Formula c : f.formula().children()) {
			LabelledFormula sf = LabelledFormula.of(c, f.variables());
			for(LabelledFormula e : subformulas(sf))
				s.add(e);		
		}
		s.add(LabelledFormula.of(f.formula(),f.variables()));
		return s;
	}

	public static Set<Formula> subformulas (Formula f) {//, List<String> variables) {
		Set<Formula> s = new HashSet<>();

		for (Formula c : f.children()) {
			for(Formula e : subformulas(c))
				s.add(e);
		}
		s.add(f);
		return s;
	}

	public static int compare(Formula f0, Formula f1) {
		return Formulas.compare(subformulas(f0), subformulas(f1));
	}
	
	public static int formulaSize (Formula f) {//, List<String> variables) {
		int size = 1;
		for (Formula c : f.children())
			size += formulaSize(c);		
		return size;
	}
	
	public static List<LabelledFormula> splitConjunction (LabelledFormula f){
		List<LabelledFormula> conjuncts = new LinkedList<>();
		if (f.formula() instanceof Conjunction) {
		      Conjunction conjunction = (Conjunction) f.formula();
		      for (Formula c : conjunction.children)
		    	  conjuncts.add(LabelledFormula.of(c, f.variables()));
		      
	    }
		else
			conjuncts.add(f);
		return conjuncts;
	}
	
	public static List<Formula> splitConjunction (Formula f){
		List<Formula> conjuncts = new LinkedList<>();
		if (f instanceof Conjunction) {
		      Conjunction conjunction = (Conjunction) f;
		      for (Formula c : conjunction.children)
		      	if (c != BooleanConstant.TRUE)
		      		conjuncts.addAll(splitConjunction(c));
	    }
		else if (f != BooleanConstant.TRUE)
			conjuncts.add(f);
		return conjuncts;
	}

	public static List<Formula> splitConjunctions(List<Formula> formulas){
		List<Formula> conjuncts = new LinkedList<>();
		for (Formula f : formulas) {
			if (f instanceof Conjunction) {
				Conjunction conjunction = (Conjunction) f;
				for (Formula c : conjunction.children)
					if (c != BooleanConstant.TRUE)
						conjuncts.addAll(splitConjunction(c));
			} else if (f != BooleanConstant.TRUE)
				conjuncts.add(f);
		}
		return conjuncts;
	}
	
	public static LabelledFormula replaceSubformula (LabelledFormula f0, LabelledFormula f1) {
		if (!f0.variables().containsAll(f1.variables()))
			throw new IllegalArgumentException("Formula_Utils.replaceSubformula: formulas should have the same set of variables.");
		
		Random rand = new Random();
		// select randomly the fub formula of f0 to be replaced
		List<LabelledFormula> subformulas_f0 = subformulas(f0);
		LabelledFormula src = subformulas_f0.get(rand.nextInt(subformulas_f0.size()));
//		System.out.println("Selected source formula "+ src);
		
		// get randomly the sub formula of f1 to be used to replace in f0.
		List<LabelledFormula> subformulas_f1 = subformulas(f1);
		LabelledFormula target = subformulas_f1.get(rand.nextInt(subformulas_f1.size()));
//		System.out.println("Selected target formula "+target);
		
		LabelledFormula f0_copy = LabelledFormula.of(f0.formula(), f0.variables());
		//replaceSubformula(f0_copy.formula(), src.formula(), target.formula());
		SubformulaReplacer visitor = new SubformulaReplacer(src.formula(),target.formula());
		Formula m = f0.formula().accept(visitor);
		f0_copy = LabelledFormula.of(m, f0.variables());
		return f0_copy;
	}

	public static Formula replaceSubformula (Formula f0, Formula f1) {
		Random rand = Settings.RANDOM_GENERATOR;
		// select randomly the fub formula of f0 to be replaced
		Set<TemporalOperator> subformulas_f0 = f0.subformulas(Formula.TemporalOperator.class);
		if (subformulas_f0.isEmpty())
			return null;
		Formula src = (Formula) subformulas_f0.toArray()[rand.nextInt(subformulas_f0.size())];
//		System.out.println("Selected source formula "+ src);

		// get randomly the sub formula of f1 to be used to replace in f0.
		Set<TemporalOperator> subformulas_f1 = f1.subformulas(Formula.TemporalOperator.class);
		if (subformulas_f1.isEmpty())
			return null;
		Formula target = (Formula)subformulas_f1.toArray()[rand.nextInt(subformulas_f1.size())];
//		System.out.println("Selected target formula "+target);

		//replaceSubformula(f0_copy.formula(), src.formula(), target.formula());
		SubformulaReplacer visitor = new SubformulaReplacer(src,target);
		Formula replaced_formula = f0.accept(visitor);
		return replaced_formula;
	}

	public static Formula combineSubformula (Formula f0, Formula f1) {
		Random rand = Settings.RANDOM_GENERATOR;
		// select randomly the fub formula of f0 to be replaced
		Set<TemporalOperator> subformulas_f0 = f0.subformulas(Formula.TemporalOperator.class);
		if (subformulas_f0.isEmpty())
			return null;
		Formula left = (Formula) subformulas_f0.toArray()[rand.nextInt(subformulas_f0.size())];
//		System.out.println("Selected source formula "+ src);

		// get randomly the sub formula of f1 to be used to replace in f0.
		Set<TemporalOperator> subformulas_f1 = f1.subformulas(Formula.TemporalOperator.class);
		if (subformulas_f1.isEmpty())
			return null;
		Formula right = (Formula)subformulas_f1.toArray()[rand.nextInt(subformulas_f1.size())];
//		System.out.println("Selected target formula "+target);

		//replaceSubformula(f0_copy.formula(), src.formula(), target.formula());
		//0:& 1:| 2:U 3:W 4:M
		int op = Settings.RANDOM_GENERATOR.nextInt(4);
		if (op == 0)
			right = Conjunction.of(left, right);
		else if (op == 1)
			right = Disjunction.of(left, right);
		else if (op == 2) {
			if (Settings.RANDOM_GENERATOR.nextBoolean())
				right = UOperator.of(left, right);
			else
				right = UOperator.of(right, left);
		}
		else if (op == 3) {
			if (Settings.RANDOM_GENERATOR.nextBoolean())
				right = WOperator.of(left, right);
			else
				right = WOperator.of(right, left);
		}

		SubformulaReplacer visitor = new SubformulaReplacer(left,right);
		Formula replaced_formula = f0.accept(visitor);
		return replaced_formula;
	}

	public static int numOfTemporalOperators(Formula formula) {
		if (formula == null || formula instanceof Literal)
			return 0;
		if (formula instanceof Formula.TemporalOperator && !(formula instanceof  XOperator)) {
			int max = 0;
			for (Formula c : formula.children()) {
				int aux = numOfTemporalOperators(c);
				if (max < aux)
					max = aux;
			}
			return max + 1;
		}
		if (formula instanceof Formula.LogicalOperator) {
			int max = 0;
			for (Formula c : formula.children()) {
				int aux = numOfTemporalOperators(c);
				if (max < aux)
					max = aux;
			}
			return max;
		}
		return 0;
	}

	public static List<Formula> toFormulaList(List<String> lst, List<String> ins, List<String> outs) {
		List<Formula> res = new LinkedList<Formula>();
		List<String> vars = new LinkedList<String>();
		vars.addAll(ins); vars.addAll(outs);
		for (String bc : lst) {
			res.add(LtlParser.parse(bc, vars).formula());
		}
		return res;
	}

	public static String toSolverSyntax(Formula f) {
		String LTLFormula = f.toString();
		LTLFormula = LTLFormula.replaceAll("\\!", "~");
		LTLFormula = LTLFormula.replaceAll("([A-Z])", " $1 ");
		LTLFormula = LTLFormula.replaceAll("_", "");
		return new String(LTLFormula); 
	}
	
	public static String toGAConflictIdentificationSyntax(Formula f) {
		String LTLFormula = f.toString();
		return toGAConflictIdentificationSyntax(LTLFormula);
	}

	public static String toGAConflictIdentificationSyntax(String LTLFormula) {
		LTLFormula = LTLFormula.replaceAll("\\!", "! ");
		LTLFormula = LTLFormula.replaceAll("([A-Z])", " $1 ");
		LTLFormula = LTLFormula.replaceAll("_", "");
		LTLFormula = LTLFormula.replaceAll("G", "[]");
		LTLFormula = LTLFormula.replaceAll("F", "<>");
		LTLFormula = LTLFormula.replaceAll("&", " && ");
		LTLFormula = LTLFormula.replaceAll("\\|", " || ");
		return new String(LTLFormula);
	}

	public static Formula equivalentToSomeSolution(Formula f1, List<Formula> sol) {
		SolverSyntaxOperatorReplacer visitor  = new SolverSyntaxOperatorReplacer();
	    SyntacticSimplifier simp = new SyntacticSimplifier();
		for (Formula f2 : sol) {
			//!f1 && f2
			SolverResult sat = SolverResult.UNSAT;
			try{ sat = LTLSolver.isSAT(Formula_Utils.toSolverSyntax(Conjunction.of(f1.not(),f2).accept(visitor).accept(simp))); }
			catch (Exception e) {e.printStackTrace();}
			if (sat==SolverResult.SAT)//are not equivalent
				continue;
			else{
				// f1 && !f2
				sat = SolverResult.UNSAT;
				try{ sat = LTLSolver.isSAT(Formula_Utils.toSolverSyntax(Conjunction.of(f2.not(),f1).accept(visitor).accept(simp))); }
				catch (Exception e) {e.printStackTrace();}
				if (sat==SolverResult.SAT)//are not equivalent
					continue;
				else
					return f2;
			}
		}
		return null;
	}
	
	public static List<Formula> getBoundaryConditions(Spec spec, List<Formula> boundaryConditions) {
		List<Formula> filteredBcs = new ArrayList<Formula>();
		for (Formula bc : boundaryConditions) {
			try {
				if (spec.isBoundaryCondition(bc)) {
					filteredBcs.add(bc);
				}
			} catch (IOException | InterruptedException e) {
				e.printStackTrace();
			}
		}
		return filteredBcs;
	}
	 
	public static List<Spec> getNonEquivalentSolutionsFromSpecs(List<Spec> solutions) {
		List<Formula> aux = new LinkedList<Formula>();
		List<Spec> nonEquivalentGenuineSolutions = new ArrayList<Spec>();
		for (Spec g : solutions) {
			aux.add(g.getFormula());
		}
		aux = Formula_Utils.getNonEquivalentSolutions(aux);
		for (Spec g : solutions) {
			if (aux.contains(g.getFormula())) {
				nonEquivalentGenuineSolutions.add(g);
			}
		}
		return nonEquivalentGenuineSolutions;
	}
	
	public static List<Formula> getNonEquivalentSolutions(List<Formula> solutions) {
		List<Formula> nonEqSolutions = new LinkedList<Formula>();
		for (Formula f : solutions) {
			Formula equiv = equivalentToSomeSolution(f,nonEqSolutions);
			if (equiv==null) { 
				nonEqSolutions.add(f);
			}
			else{
				if (f.height() < equiv.height()) {
					nonEqSolutions.remove(equiv);
					nonEqSolutions.add(f);
				}
			}
		}
		return nonEqSolutions;
	}
	
	public static boolean areEquivalent(Formula f1, Formula f2) {
		return (equivalentToSomeSolution(f1,List.of(f2)) != null);
	}
	
	public static String translatorBCLearnLanguateToOWL(String str) {
		String result = "";
		char[] strArray = str.toCharArray();
		int lastPoint = 0;
		int i = 0;
		while (i < str.length()) {
			if (strArray[i] == '<' && strArray[i+1] == '>') {
				result += new String(strArray, lastPoint, i - lastPoint) + "F";
				i = i + 2;
				lastPoint = i;
			} else if (strArray[i] == '[') {
				result += new String(strArray, lastPoint, i - lastPoint) + "G";
				i = i + 2;
				lastPoint = i;
			} else if (strArray[i] == '/') {
				result += new String(strArray, lastPoint, i - lastPoint) + "&&";
				i = i + 2;
				lastPoint = i;
			} else if (strArray[i] == '\\') {
				result += new String(strArray, lastPoint, i - lastPoint) + "||";
				i = i + 2;
				lastPoint = i;
			} else if (strArray[i] == 'V') {
				result += new String(strArray, lastPoint, i - lastPoint) + "R";
				i = i + 1;
				lastPoint = i;
			} else {
				++i;
			}
		}
		if (i != lastPoint) {
			result += new String(strArray, lastPoint, i - lastPoint);
		}
		return result;
	}
	
	public static List<Formula> jfc(Spec spec, List<Formula> bcs) throws 
		ParseErrorException, InvalidConfigurationException, IOException, InterruptedException {
		return jfc(spec, bcs, false);
	}

	public static List<Formula> jfc(Spec spec, List<Formula> bcs, boolean callBCSolver) throws 
		ParseErrorException, InvalidConfigurationException, IOException, InterruptedException {
		List<Formula> domain_properties = spec.getAssume();
		List<Formula> goals = spec.getGoals();
		LinkedList<String> vars = new LinkedList<>(spec.getIns());
		vars.addAll(spec.getOuts());
		SolverSyntaxOperatorReplacer visitor  = new SolverSyntaxOperatorReplacer();
		Set<String> domain = new TreeSet<String>(); Set<String> goals_str = new TreeSet<String>();		
		LinkedList<Formula> boundary_conditions = new LinkedList<Formula>();
		Formula bc = null;
		int count = 0;

		for (Formula dom : domain_properties) {
			String domStr = LabelledFormula.of(dom.accept(visitor), vars).toString();
			domain.add(toGAConflictIdentificationSyntax(domStr));
		}

		for (Formula g : goals) {
			String goalStr = LabelledFormula.of(g.accept(visitor), vars).toString();
			goals_str.add(toGAConflictIdentificationSyntax(goalStr));
		}
		
		while(true) {
//			Domain U !Bcs
			for (Formula _bc : boundary_conditions) {
				//Formula nBC = LabelledFormula.of(_bc.not().accept(visitor),vars).formula();
				String notBc = LabelledFormula.of(_bc.not().accept(visitor),vars).toString();
				if (!domain.contains(toGAConflictIdentificationSyntax(notBc))) {
					domain.add(toGAConflictIdentificationSyntax(notBc));
				}
			}			
			
			System.out.println("===================");
			System.out.println(count);
			if (count < 10) {
				try(FileWriter fw = new FileWriter("/tmp/2_jfc_" + count, true)) {
					for (Formula _bc : boundary_conditions) {
						fw.write(LabelledFormula.of(_bc,vars) + "\n");
					}
				}
				++count;
			} else {
				return boundary_conditions;
			}
			
			bc = callBCSolverFirstBC(domain, goals_str, spec.getIns(), spec.getOuts());
				
			if (bc == null)    // There are no more  BCs.
				return boundary_conditions;	

			List<Formula> wBcs = internalContrastyFilter(bc, boundary_conditions, spec);
			boundary_conditions.add(bc);
			if (!wBcs.isEmpty())
				boundary_conditions.removeAll(wBcs);
		}
	}

	private static Formula callBCSolverFirstBC(Set<String> domain, Set<String> goals_str, List<String> ins, List<String> outs) throws InvalidConfigurationException, ParseErrorException {
		List<Formula> bc = callBCSolver(domain, goals_str, ins, outs, 1);
		if (bc == null || bc.isEmpty()) {
			return null;
		}
		return bc.get(0);
	}
	
	private static List<Formula> callBCSolver(Set<String> domain, Set<String> goals_str, List<String> ins, List<String> outs, int solutions) throws InvalidConfigurationException, ParseErrorException {
		BCLearner learner = new main.BCLearner(domain, goals_str);
		Parser.propositions.clear();
		BCLearner.GENERATIONS = 15;
		BCLearner.LIMIT_SOLUTIONS = solutions;
		BCLearner.CHROMOSOMES_SIZE = 150;
		dCNF.LTLSolver.SATID = solvers.LTLSolver.SATID + "_BCLearn";
		dCNF.LTLSolver.init();
		learner.learnBC();
		dCNF.LTLSolver.clear();
		List<main.Solution> bcs = learner.weakest;
		if (bcs == null || bcs.isEmpty()) {
			return null;
		}
		List<String> vars = new ArrayList<String>();
		vars.addAll(ins); vars.addAll(outs);
		List<String> owlStrBCs = new ArrayList<String>();
		for (main.Solution bc : bcs) {
			String oldFormateBcStr = bc.getFormulaString();
			String owlBCStr = Formula_Utils.translatorBCLearnLanguateToOWL(oldFormateBcStr);
			owlStrBCs.add(owlBCStr);
		}
		return toFormulaList(owlStrBCs, ins, outs);
	}

	private static List<Formula> internalContrastyFilter(Formula bc,
															   List<Formula> boundary_conditions,
															   Spec spec) throws IOException, InterruptedException {
		List<Formula> witnessBC = new LinkedList<Formula>();
		for (Formula _bc : boundary_conditions) {
			if (isWitness(bc,_bc, spec))
				witnessBC.add(_bc);
		}
		return witnessBC;
	}

	/**
	 * Returns if bc1 is a witness of bc2 in the spec.
	 * @param bc1
	 * @param bc2
	 * @param spec
	 * @return a boolean value representing if the first parameter is a witness formula of the second one
	 * in the spec passed as a paramenter.
	 */
	public static boolean isWitness(Formula bc1, Formula bc2, Spec spec) throws IOException, InterruptedException {
		Formula formula =  Conjunction.of(bc2,bc1.not());
		return !spec.isBoundaryCondition(formula);
	}
}
