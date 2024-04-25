package utils.plots;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import optimization.specification.Spec;
import owl.ltl.Formula;
import utils.Formula_Utils;
import utils.Settings;

public class MainSemSynPlot {

	
	public static void main(String[] args) {
		Settings.allowGuaranteeRemoval = true;
		SemSynGroundTruthRecovery semSynPlot = new SemSynGroundTruthRecovery();
		
		/*
		minepumpGenuine(semSynPlot); //Ok
		minepumpRepairs(semSynPlot);
		*/
		
		semSynPlot.plot("Minepump");
	}

	public static void minepumpRepairs(SemSynGroundTruthRecovery semSynPlot) {
		File genuineFolder = new File(casesFolder + "/minepump-repairs/examples");
		List<String> domainn = new ArrayList<String>();
		List<String> goalss = new ArrayList<String>();
		List<String> input = new ArrayList<String>();
		List<String> output = new ArrayList<String>();
		List<String> bc = new ArrayList<String>();
		domainn.add("G((p && X(p)) -> X(X(! h)))");
		goalss.add("G(h -> X(p))");
		goalss.add("G(m -> X(! p))");
		input.add("h");
		input.add("m");
		output.add("p");
		
		/*
		domainn.add("G((p && X(p)) -> X(X(! h)))");
		goalss.add("G(m -> X(m -> !p))");
		goalss.add("G(h -> X(!m -> p))");
		input.add("h");
		input.add("m");
		output.add("p");
		*/
		
		Path pathToFile = Paths.get(casesFolder + "/minepump/bc/BCs");
		try {
			List<String> allLines = Files.readAllLines(pathToFile.toAbsolutePath());
			for (String line : allLines) {
				bc.add(line);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		List<Formula> boundaryConditions = Formula_Utils.toFormulaList(bc, input, output);
		System.out.println("INITIAL BCs: " + boundaryConditions.size());

		Spec originalSpec = new Spec(
				Formula_Utils.toFormulaList(domainn, input, output),
				Formula_Utils.toFormulaList(goalss, input, output), input, output);
		semSynPlot.addSpecification(originalSpec, genuineFolder, input, output, boundaryConditions, "Repairs");
	}
	
	public static void minepumpGenuine(SemSynGroundTruthRecovery semSynPlot) {
		File genuineFolder = new File(casesFolder + "/minepump/genuine");
		List<String> domainn = new ArrayList<String>();
		List<String> goalss = new ArrayList<String>();
		List<String> input = new ArrayList<String>();
		List<String> output = new ArrayList<String>();
		List<String> bc = new ArrayList<String>();
		domainn.add("G((p && X(p)) -> X(X(! h)))");
		goalss.add("G(h -> X(p))");
		goalss.add("G(m -> X(! p))");
		input.add("h");
		input.add("m");
		output.add("p");
		
		Path pathToFile = Paths.get(casesFolder + "/minepump/bc/BCs");
		try {
			List<String> allLines = Files.readAllLines(pathToFile.toAbsolutePath());
			for (String line : allLines) {
				bc.add(line);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		List<Formula> boundaryConditions = Formula_Utils.toFormulaList(bc, input, output);
		System.out.println("INITIAL BCs: " + boundaryConditions.size());

		Spec originalSpec = new Spec(
				Formula_Utils.toFormulaList(domainn, input, output),
				Formula_Utils.toFormulaList(goalss, input, output), input, output);
		semSynPlot.addSpecification(originalSpec, genuineFolder, input, output, boundaryConditions, "Ground-truth");
	}

	/*
	public static void main2(String[] args) {
		Settings.allowGuaranteeRemoval = true;
		SemSynPlot semSynPlot = new SemSynPlot();
		
		minepump(semSynPlot); //Ok
		detector(semSynPlot); //Ok
		simplearbiterv2(semSynPlot); //OK
		simplearbiterv1(semSynPlot); //OK
		//RG2(semSynPlot); //change
		//lily02(semSynPlot); //change domain
		prioritizedArbiter(semSynPlot);
		arbiter(semSynPlot); //OK
		ltl2dba27(semSynPlot);
		roundrobin(semSynPlot);
		//rrcs(semSynPlot);
		semSynPlot.plot("Ground-truth");
	}
	*/
	
	public final static String casesFolder = "/Users/luiz.carvalho/Downloads/SOSYM/cases";
	
	private static void rrcs(SemSynGroundTruthRecovery semSynPlot) {
		File genuineFolder = new File(casesFolder + "/rrcs/genuine");
		List<String> domainn = new ArrayList<String>();
		List<String> goalss = new ArrayList<String>();
		List<String> input = new ArrayList<String>();
		List<String> output = new ArrayList<String>();
		List<String> bc = new ArrayList<String>();
		domainn.add("G ((ta -> X(tc)) && (X(tc) -> ta))");
		domainn.add("G (X(cc) -> (ca && go))");
		goalss.add("G (tc -> !cc)");
		goalss.add("G (ta -> !go)");
		input.add("ta");
		input.add("tc");
		input.add("ca");
		input.add("cc");
		output.add("go");
		Path pathToFile = Paths.get(casesFolder + "/rrcs/bc/BCs");
		try {
			List<String> allLines = Files.readAllLines(pathToFile.toAbsolutePath());
			for (String line : allLines) {
				bc.add(line);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		List<Formula> boundaryConditions = Formula_Utils.toFormulaList(bc, input, output);
		System.out.println("INITIAL BCs: " + boundaryConditions.size());

		Spec originalSpec = new Spec(
				Formula_Utils.toFormulaList(domainn, input, output),
				Formula_Utils.toFormulaList(goalss, input, output), input, output);
		semSynPlot.addSpecification(originalSpec, genuineFolder, input, output, boundaryConditions, "rrcs");
	}
	
	public static void roundrobin(SemSynGroundTruthRecovery semSynPlot) {
		File genuineFolder = new File(casesFolder + "/round-robin/genuine");
		List<String> domainn = new ArrayList<String>();
		List<String> goalss = new ArrayList<String>();
		List<String> input = new ArrayList<String>();
		List<String> output = new ArrayList<String>();
		List<String> bc = new ArrayList<String>();
		domainn.add("G ((r_0 && ! g_0) -> X r_0)");
		domainn.add("G F (! (r_1 && g_1))");
		domainn.add("G ((! r_0 && g_0) -> X (! r_0))");
		domainn.add("G ((! r_1 && g_1) -> X (! r_1))");
		domainn.add("G F (! (r_0 && g_0))");
		domainn.add("G ((r_1 && ! g_1) -> X r_1)");
		goalss.add("G ((! g_0 && true) || (true && (! g_1)) && ((r_0 && X r_1) -> X (X (g_0 && g_1))))");
		goalss.add("G (r_0 ->  F g_0)");
		goalss.add("G (r_1 ->  F g_1)");
		input.add("r_0");
		input.add("r_1");
		input.add("r_2");
		output.add("g_0");
		output.add("g_1");
		output.add("g_2");
		Path pathToFile = Paths.get(casesFolder + "/round-robin/bc/BCs");
		try {
			List<String> allLines = Files.readAllLines(pathToFile.toAbsolutePath());
			for (String line : allLines) {
				bc.add(line);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		List<Formula> boundaryConditions = Formula_Utils.toFormulaList(bc, input, output);
		System.out.println("INITIAL BCs: " + boundaryConditions.size());

		Spec originalSpec = new Spec(
				Formula_Utils.toFormulaList(domainn, input, output),
				Formula_Utils.toFormulaList(goalss, input, output), input, output);
		semSynPlot.addSpecification(originalSpec, genuineFolder, input, output, boundaryConditions, "round-robin");
	}
	
	public static void ltl2dba27(SemSynGroundTruthRecovery semSynPlot) {
		File genuineFolder = new File(casesFolder + "/ltl2dba27/genuine");
		List<String> domainn = new ArrayList<String>();
		List<String> goalss = new ArrayList<String>();
		List<String> input = new ArrayList<String>();
		List<String> output = new ArrayList<String>();
		List<String> bc = new ArrayList<String>();
		goalss.add("(F G !(p)) <-> (G F acc)");
		input.add("p");
		output.add("acc");
		Path pathToFile = Paths.get(casesFolder + "/ltl2dba27/bc/BCs");
		try {
			List<String> allLines = Files.readAllLines(pathToFile.toAbsolutePath());
			for (String line : allLines) {
				bc.add(line);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		List<Formula> boundaryConditions = Formula_Utils.toFormulaList(bc, input, output);
		System.out.println("INITIAL BCs: " + boundaryConditions.size());

		Spec originalSpec = new Spec(
				Formula_Utils.toFormulaList(domainn, input, output),
				Formula_Utils.toFormulaList(goalss, input, output), input, output);
		semSynPlot.addSpecification(originalSpec, genuineFolder, input, output, boundaryConditions, "ltl2dba27");
	}

	
	public static void arbiter(SemSynGroundTruthRecovery semSynPlot) {
		File genuineFolder = new File(casesFolder + "/arbiter/genuine");
		List<String> domainn = new ArrayList<String>();
		List<String> goalss = new ArrayList<String>();
		List<String> input = new ArrayList<String>();
		List<String> output = new ArrayList<String>();
		List<String> bc = new ArrayList<String>();
		goalss.add("G (r1 -> F g1)");
		goalss.add("G (r2 -> F g2)");
		goalss.add("G (!a -> (!g1 && !g2))");
		input.add("a");
		input.add("r1");
		input.add("r2");
		output.add("g1");
		output.add("g2");
		Path pathToFile = Paths.get(casesFolder + "/arbiter/bc/BCs");
		try {
			List<String> allLines = Files.readAllLines(pathToFile.toAbsolutePath());
			for (String line : allLines) {
				bc.add(line);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		List<Formula> boundaryConditions = Formula_Utils.toFormulaList(bc, input, output);
		System.out.println("INITIAL BCs: " + boundaryConditions.size());

		Spec originalSpec = new Spec(
				Formula_Utils.toFormulaList(domainn, input, output),
				Formula_Utils.toFormulaList(goalss, input, output), input, output);
		semSynPlot.addSpecification(originalSpec, genuineFolder, input, output, boundaryConditions, "arbiter");
	}
	
	public static void prioritizedArbiter(SemSynGroundTruthRecovery semSynPlot) {
		File genuineFolder = new File(casesFolder + "/prioritizedArbiter/genuine");
		List<String> domainn = new ArrayList<String>();
		List<String> goalss = new ArrayList<String>();
		List<String> input = new ArrayList<String>();
		List<String> output = new ArrayList<String>();
		List<String> bc = new ArrayList<String>();
		domainn.add("G (F (! r_m))");
		goalss.add("G (r_m -> X ((! g_0 && ! g_1) U g_m))");
		goalss.add("G (! g_0 && true || (true && (! g_1)))");
		goalss.add("G (! (g_m && g_0))");
		goalss.add("G (! (g_m && g_1))");
		goalss.add("G (r_0 -> F g_0) && G (r_1 -> F g_1)");
		goalss.add("G (r_0 && X r_1 -> X (X (g_0 && g_1)))");
		input.add("r_m");
		input.add("r_1");
		input.add("r_0");
		output.add("g_m");
		output.add("g_0");
		output.add("g_1");
		Path pathToFile = Paths.get(casesFolder + "/prioritizedArbiter/bc/BCs");
		try {
			List<String> allLines = Files.readAllLines(pathToFile.toAbsolutePath());
			for (String line : allLines) {
				bc.add(line);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		List<Formula> boundaryConditions = Formula_Utils.toFormulaList(bc, input, output);
		System.out.println("INITIAL BCs: " + boundaryConditions.size());

		Spec originalSpec = new Spec(
				Formula_Utils.toFormulaList(domainn, input, output),
				Formula_Utils.toFormulaList(goalss, input, output), input, output);
		semSynPlot.addSpecification(originalSpec, genuineFolder, input, output, boundaryConditions, "prioritizedArbiter");
	}
	
	public static void lily02(SemSynGroundTruthRecovery semSynPlot) {
		File genuineFolder = new File(casesFolder + "/lily02/genuine");
		List<String> domainn = new ArrayList<String>();
		List<String> goalss = new ArrayList<String>();
		List<String> input = new ArrayList<String>();
		List<String> output = new ArrayList<String>();
		List<String> bc = new ArrayList<String>();
		goalss.add("G(req -> X (grant || X (grant || X grant)))");
		goalss.add("G(grant -> X !grant)");
		goalss.add("G(cancel -> X (!grant U go))");
		input.add("req");
		input.add("cancel");
		input.add("go");
		output.add("grant");
		Path pathToFile = Paths.get(casesFolder + "/lily02/bc/BCs");
		try {
			List<String> allLines = Files.readAllLines(pathToFile.toAbsolutePath());
			for (String line : allLines) {
				bc.add(line);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		List<Formula> boundaryConditions = Formula_Utils.toFormulaList(bc, input, output);
		System.out.println("INITIAL BCs: " + boundaryConditions.size());

		Spec originalSpec = new Spec(
				Formula_Utils.toFormulaList(domainn, input, output),
				Formula_Utils.toFormulaList(goalss, input, output), input, output);
		semSynPlot.addSpecification(originalSpec, genuineFolder, input, output, boundaryConditions, "lily02");		
	}
	
	public static void simplearbiterv2(SemSynGroundTruthRecovery semSynPlot) {
		File genuineFolder = new File(casesFolder + "/simple_arbiter_v2/genuine");
		List<String> domainn = new ArrayList<String>();
		List<String> goalss = new ArrayList<String>();
		List<String> input = new ArrayList<String>();
		List<String> output = new ArrayList<String>();
		List<String> bc = new ArrayList<String>();
		goalss.add("G((! (g_0)) || (! (g_1)))");
		goalss.add("(G ((r_0) -> (F (g_0))))");
		goalss.add("(G ((r_1) -> (F (g_1))))");
		goalss.add("(G (((r_0) && (X (r_1))) -> (F ((g_0) && (g_1)))))");
		input.add("r_0");
		input.add("r_1");
		output.add("g_0");
		output.add("g_1");		
		Path pathToFile = Paths.get(casesFolder + "/simple_arbiter_v2/bc/BCs");
		try {
			List<String> allLines = Files.readAllLines(pathToFile.toAbsolutePath());
			for (String line : allLines) {
				bc.add(line);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		List<Formula> boundaryConditions = Formula_Utils.toFormulaList(bc, input, output);
		System.out.println("INITIAL BCs: " + boundaryConditions.size());

		Spec originalSpec = new Spec(
				Formula_Utils.toFormulaList(domainn, input, output),
				Formula_Utils.toFormulaList(goalss, input, output), input, output);
		semSynPlot.addSpecification(originalSpec, genuineFolder, input, output, boundaryConditions, "simple arbiter v2");		
	}
	
	public static void simplearbiterv1(SemSynGroundTruthRecovery semSynPlot) {
		File genuineFolder = new File(casesFolder + "/simple_arbiter_v1/genuine");
		List<String> domainn = new ArrayList<String>();
		List<String> goalss = new ArrayList<String>();
		List<String> input = new ArrayList<String>();
		List<String> output = new ArrayList<String>();
		List<String> bc = new ArrayList<String>();
		goalss.add("G((! (g_0)) || (! (g_1)))");
		goalss.add("G(((r_0) && (X (r_1))) -> (X ((g_0) && (g_1))))");
		goalss.add("(G ((r_0) -> (F (g_0))))");
		goalss.add("(G ((r_1) -> (F (g_1))))");
		input.add("r_0");
		input.add("r_1");
		output.add("g_0");
		output.add("g_1");		
		Path pathToFile = Paths.get(casesFolder + "/simple_arbiter_v1/bc/BCs");
		try {
			List<String> allLines = Files.readAllLines(pathToFile.toAbsolutePath());
			for (String line : allLines) {
				bc.add(line);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		List<Formula> boundaryConditions = Formula_Utils.toFormulaList(bc, input, output);
		System.out.println("INITIAL BCs: " + boundaryConditions.size());

		Spec originalSpec = new Spec(
				Formula_Utils.toFormulaList(domainn, input, output),
				Formula_Utils.toFormulaList(goalss, input, output), input, output);
		semSynPlot.addSpecification(originalSpec, genuineFolder, input, output, boundaryConditions, "simple arbiter v1");		
	}
	
	public static void RG2(SemSynGroundTruthRecovery semSynPlot) {
		File genuineFolder = new File(casesFolder + "/RG2/genuine");
		List<String> domainn = new ArrayList<String>();
		List<String> goalss = new ArrayList<String>();
		List<String> input = new ArrayList<String>();
		List<String> output = new ArrayList<String>();
		List<String> bc = new ArrayList<String>();
		goalss.add("G(req -> F (X grant))");
		goalss.add("G( (cancel || grant) -> X !grant)");
		input.add("req");
		input.add("cancel");
		output.add("grant");
		Path pathToFile = Paths.get(casesFolder + "/RG2/bc/BCs");
		try {
			List<String> allLines = Files.readAllLines(pathToFile.toAbsolutePath());
			for (String line : allLines) {
				bc.add(line);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		List<Formula> boundaryConditions = Formula_Utils.toFormulaList(bc, input, output);
		System.out.println("INITIAL BCs: " + boundaryConditions.size());

		Spec originalSpec = new Spec(
				Formula_Utils.toFormulaList(domainn, input, output),
				Formula_Utils.toFormulaList(goalss, input, output), input, output);
		semSynPlot.addSpecification(originalSpec, genuineFolder, input, output, boundaryConditions, "RG2");		
	}

	public static void detector(SemSynGroundTruthRecovery semSynPlot) {
		File genuineFolder = new File(casesFolder + "/detector/genuine");
		List<String> domainn = new ArrayList<String>();
		List<String> goalss = new ArrayList<String>();
		List<String> input = new ArrayList<String>();
		List<String> output = new ArrayList<String>();
		List<String> bc = new ArrayList<String>();
		goalss.add("(((G (F (r_0))) && (G (F (r_1)))) <-> (G (F (g)))) && (G (((r_0) && (r_1)) -> (G (! (g)))))");
		input.add("r_0");
		input.add("r_1");
		output.add("g");
		
		Path pathToFile = Paths.get(casesFolder + "/detector/bc/BCs");
		try {
			List<String> allLines = Files.readAllLines(pathToFile.toAbsolutePath());
			for (String line : allLines) {
				bc.add(line);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		List<Formula> boundaryConditions = Formula_Utils.toFormulaList(bc, input, output);
		System.out.println("INITIAL BCs: " + boundaryConditions.size());

		Spec originalSpec = new Spec(
				Formula_Utils.toFormulaList(domainn, input, output),
				Formula_Utils.toFormulaList(goalss, input, output), input, output);
		semSynPlot.addSpecification(originalSpec, genuineFolder, input, output, boundaryConditions, "detector");
	}
	
	public static void minepump(SemSynGroundTruthRecovery semSynPlot) {
		File genuineFolder = new File(casesFolder + "/minepump/genuine");
		List<String> domainn = new ArrayList<String>();
		List<String> goalss = new ArrayList<String>();
		List<String> input = new ArrayList<String>();
		List<String> output = new ArrayList<String>();
		List<String> bc = new ArrayList<String>();
		domainn.add("G((p && X(p)) -> X(X(! h)))");
		goalss.add("G(h -> X(p))");
		goalss.add("G(m -> X(! p))");
		input.add("h");
		input.add("m");
		output.add("p");
		
		Path pathToFile = Paths.get(casesFolder + "/minepump/bc/BCs");
		try {
			List<String> allLines = Files.readAllLines(pathToFile.toAbsolutePath());
			for (String line : allLines) {
				bc.add(line);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		List<Formula> boundaryConditions = Formula_Utils.toFormulaList(bc, input, output);
		System.out.println("INITIAL BCs: " + boundaryConditions.size());

		Spec originalSpec = new Spec(
				Formula_Utils.toFormulaList(domainn, input, output),
				Formula_Utils.toFormulaList(goalss, input, output), input, output);
		semSynPlot.addSpecification(originalSpec, genuineFolder, input, output, boundaryConditions, "Minepump");
	}
		
}
