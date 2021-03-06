package fr.uphf.se.kotlinresearch.core;

import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.ListBranchCommand.ListMode;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Ref;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import fr.inria.coming.changeminer.entity.FinalResult;
import fr.inria.coming.main.ComingProperties;
import fr.uphf.se.kotlinresearch.core.results.MigAIntermediateResultStore;
import fr.uphf.se.kotlinresearch.output.MigAJSONSerializer;

/**
 * 
 * @author Matias Martinez
 *
 */
public class MigaMain {

	public MigAIntermediateResultStore runAnalysis(File out, File repopath, String branch) throws Exception {
		return runAnalysis(out, repopath, branch, null);

	}

	public MigAIntermediateResultStore runAnalysis(File out, File repopath, String branch, String toIgnore)
			throws Exception {

		System.out.println("Running with branch: " + branch);
		ComingProperties.reset();
		MigaV2 core = new MigaV2();
		if (toIgnore != null && !toIgnore.isEmpty())
			ComingProperties.properties.setProperty(MigACore.COMMITS_TO_IGNORE, toIgnore);
		ComingProperties.properties.setProperty("save_result_revision_analysis", "true");
		ComingProperties.properties.setProperty("branch", branch);
		ComingProperties.properties.setProperty("projectname", repopath.getName());
		ComingProperties.properties.setProperty("location", repopath.getAbsolutePath());

		ComingProperties.properties.setProperty("out_results", out.getAbsolutePath());

		FinalResult finalResult = core.analyze();

		return core.intermediateResultStore;

	}

	public Map<String, List> runExperiment(File pathToKotlinRepo) throws IOException, GitAPIException, Exception {
		return runExperiment(new File("./coming_results/"), pathToKotlinRepo);
	}

	public Map<String, List> runExperiment(File out, File pathToKotlinRepo)
			throws IOException, GitAPIException, Exception {

		System.out.println("\n--------\nRunning on repo: " + pathToKotlinRepo.getName());
		ComingProperties.setProperty("out", out.getAbsolutePath());
		Git git = Git.open(pathToKotlinRepo);
		System.out.println("Branch: ");
		String mainBranch = git.getRepository().getFullBranch();
		System.out.println(mainBranch);
		Collection<Ref> refs = git.branchList().setListMode(ListMode.ALL).call();

		List<String> branches = new ArrayList<>();
		// the master the first one
		branches.add(mainBranch);

		for (Ref ref : refs) {
			if (!ref.getName().equals(mainBranch)) {
				branches.add(ref.getName());
			}
		}

		List<String> allCommitsAnalyzed = new ArrayList();
		Map<String, List> commitsByBranch = new HashMap<>();

		Map<String, MigAIntermediateResultStore> resultsByBranch = new HashMap<>();

		JsonObject jsonRoot = new JsonObject();

		JsonArray jsonbranches = new JsonArray();
		jsonRoot.add("data", jsonbranches);

		List<String> summaryCommitsMigrationMaster = new ArrayList<String>();
		List<String> summaryCommitsMigrationUpFileMaster = new ArrayList<String>();
		List<String> summaryCommitsMigrationADDFileMaster = new ArrayList<String>();
		List<String> summaryCommitsMigrationBrach = new ArrayList<String>();
		List<String> summaryCommitsMigrationUpFileBranch = new ArrayList<String>();
		List<String> summaryCommitsMigrationADDFileBranch = new ArrayList<String>();

		MigAJSONSerializer serializer = new MigAJSONSerializer();

		for (String iBranch : branches) {

			System.out.println("--> " + iBranch);
			String alreadyAnalyzed = allCommitsAnalyzed.stream().collect(Collectors.joining(MigaV2.CHAR_JOINT_IGNORE));

			MigAIntermediateResultStore resultsBranch = runAnalysis(out, pathToKotlinRepo, iBranch, alreadyAnalyzed);
			assertNotNull(resultsBranch);

			resultsByBranch.put(iBranch, resultsBranch);
			// assertTrue(resultsBranch.orderCommits.size() > 0);

			allCommitsAnalyzed.addAll(resultsBranch.orderCommits);
			commitsByBranch.put(iBranch, resultsBranch.orderCommits);

			JsonObject jsonBranch = serializer.extractJSon(pathToKotlinRepo.getName(), 0, resultsBranch);
			jsonBranch.addProperty("branch", iBranch);

			jsonbranches.add(jsonBranch);

			if (mainBranch.equals(iBranch)) {
				summaryCommitsMigrationMaster.addAll(resultsBranch.commitsWithMigrationsRename);
				summaryCommitsMigrationUpFileMaster.addAll(resultsBranch.commitsWithMigrationsAddMethodRemoveMethod);
				summaryCommitsMigrationADDFileMaster
						.addAll(resultsBranch.commitsWithMigrationsRemoveMethodMethodAddFile);

			} else {

				summaryCommitsMigrationBrach.addAll(resultsBranch.commitsWithMigrationsRename);
				summaryCommitsMigrationUpFileBranch.addAll(resultsBranch.commitsWithMigrationsAddMethodRemoveMethod);
				summaryCommitsMigrationADDFileBranch
						.addAll(resultsBranch.commitsWithMigrationsRemoveMethodMethodAddFile);

			}

		}

		jsonRoot.add("master_migration_rename", transformName(summaryCommitsMigrationMaster));

		jsonRoot.add("branches_migration_rename", transformName(summaryCommitsMigrationBrach));

		jsonRoot.add("master_migration_update_java_koltin", transformName(summaryCommitsMigrationUpFileMaster));

		jsonRoot.add("branches_migration_update_java_koltin", transformName(summaryCommitsMigrationUpFileBranch));

		jsonRoot.add("master_migration_add_kotlin", transformName(summaryCommitsMigrationADDFileMaster));

		jsonRoot.add("branches_migration_add_kotlin", transformName(summaryCommitsMigrationADDFileBranch));

		jsonRoot.addProperty("nr_master_migration_rename", (summaryCommitsMigrationMaster.size()));

		jsonRoot.addProperty("nr_branches_migration_rename", (summaryCommitsMigrationBrach.size()));

		jsonRoot.addProperty("nr_master_migration_update_java_koltin", (summaryCommitsMigrationUpFileMaster.size()));

		jsonRoot.addProperty("nr_branches_migration_update_java_koltin", (summaryCommitsMigrationUpFileBranch.size()));

		jsonRoot.addProperty("nr_master_migration_add_kotlin", (summaryCommitsMigrationADDFileMaster.size()));

		jsonRoot.addProperty("nr_branches_migration_add_kotlin", (summaryCommitsMigrationADDFileBranch.size()));
		jsonRoot.addProperty("project", pathToKotlinRepo.getName());

		serializer.storeJSon(out, getFileNameOfOutput(pathToKotlinRepo), jsonRoot);

		return commitsByBranch;
	}

	public String getFileNameOfOutput(File pathToKotlinRepo) {
		return "alldata_" + pathToKotlinRepo.getName();
	}

	private JsonElement transformName(List<String> summaryCommitsMigrationMaster) {
		JsonArray arr = new JsonArray();
		for (String string : summaryCommitsMigrationMaster) {
			arr.add(string);
		}

		return arr;
	}

	public static void main(String[] args) throws IOException, GitAPIException, Exception {

		if (args.length != 2) {
			System.out.println("Usage <path to results> <path to repo to analyze>");

		} else {
			File fout = new File(args[0]);
			File frepo = new File(args[1]);

			//
			MigaMain main = new MigaMain();
			main.runExperiment(fout, frepo);

		}

	}

}
