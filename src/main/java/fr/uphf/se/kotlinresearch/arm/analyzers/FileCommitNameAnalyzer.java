package fr.uphf.se.kotlinresearch.arm.analyzers;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import fr.inria.coming.changeminer.entity.IRevision;
import fr.inria.coming.core.engine.Analyzer;
import fr.inria.coming.core.engine.git.CommitGit;
import fr.inria.coming.core.entities.AnalysisResult;
import fr.inria.coming.core.entities.RevisionResult;
import fr.inria.coming.core.entities.interfaces.FileCommit;
import fr.inria.coming.core.entities.interfaces.IRevisionPair;
import fr.uphf.se.kotlinresearch.core.MigACore;
import fr.uphf.se.kotlinresearch.core.Outils;

/**
 * 
 * @author Matias Martinez
 *
 */
@SuppressWarnings("rawtypes")
public class FileCommitNameAnalyzer implements Analyzer<IRevision> {
	Logger log = Logger.getLogger(FileCommitNameAnalyzer.class.getName());

	@Override
	public AnalysisResult analyze(IRevision revision, RevisionResult previousResults) {
		long init = (new Date()).getTime();
		List<FileCommit> childerPairs = ((CommitGit) revision).getFileCommits();

		log.debug("\nCommit " + revision.getName());
		Map<String, IRevisionPair<String>> pre = new java.util.HashMap<String, IRevisionPair<String>>();
		Map<String, IRevisionPair<String>> post = new java.util.HashMap<String, IRevisionPair<String>>();

		List<IRevisionPair<String>> result = new ArrayList<>();
		List<IRevisionPair<String>> merged = new ArrayList<>();

		// For each file inside the revision
		for (IRevisionPair<String> iRevisionPair : childerPairs) {

			if (!fileExtension(iRevisionPair, "java") && !fileExtension(iRevisionPair, "kt"))
				continue;

			log.debug("Prev: " + iRevisionPair.getPreviousName());
			log.debug("Post: " + iRevisionPair.getName());
			log.debug("--");

			if (isNull(iRevisionPair.getPreviousName())) {

				String currentName = Outils.getFileName(iRevisionPair.getName());
				post.put(getFileName(currentName), iRevisionPair);
			} else if (isNull(iRevisionPair.getName())) {
				String previousName = Outils.getFileName(iRevisionPair.getPreviousName());
				pre.put(getFileName(previousName), iRevisionPair);
			} else
				result.add(iRevisionPair);

		}

		for (String ipre : pre.keySet()) {
			IRevisionPair<String> preFc = pre.get(ipre);
			if (post.containsKey(ipre)) {
				// merge

				IRevisionPair<String> postFc = post.get(ipre);
				preFc.setName(postFc.getName());

				preFc.setNextVersion(postFc.getNextVersion());
				log.debug("MERGING " + ipre);
				// add
				result.add(preFc);
				merged.add(preFc);
				// remove from post
				post.remove(ipre);
			} else {

				result.add(preFc);
			}
		}

		// Adding the not linked from post
		result.addAll(post.values());

		MigACore.executionsTime.add(this.getClass().getSimpleName(), new Long((new Date()).getTime() - init));

		return new RenameAnalyzerResult(revision, result, merged);
	}

	private boolean fileExtension(IRevisionPair<String> iRevisionPair, String extension) {

		return iRevisionPair.getPreviousName().endsWith(extension) || iRevisionPair.getName().endsWith(extension);
	}

	public String getFileName(String completeFileName) {

		int idx = completeFileName.lastIndexOf(File.separator);

		String s = completeFileName.substring(idx + 1);

		int idp = s.indexOf(".");
		if (idp == -1)
			return s;

		return s.substring(0, idp);
	}

	public static boolean isNull(Object previousName) {
		return previousName == null || previousName.toString().trim().isEmpty() || previousName.toString().isEmpty()
				|| "null".equals(previousName.toString())
				|| previousName.toString().toLowerCase().contains("/dev/null");
	}

	public boolean isAdded(IRevisionPair iRevisionPair) {

		return (iRevisionPair.getPreviousVersion() == null
				|| iRevisionPair.getPreviousVersion().toString().trim().isEmpty());

	}

	public boolean isDiffName(IRevisionPair iRevisionPair) {
		if (iRevisionPair instanceof FileCommit) {
			FileCommit fc = (FileCommit) iRevisionPair;
			return fc.getPreviousFileName().equals(fc.getNextFileName());
		}
		return false;

	}

	public boolean isModif(IRevisionPair iRevisionPair) {
		return (iRevisionPair.getPreviousVersion() != null
				&& !iRevisionPair.getPreviousVersion().toString().trim().isEmpty())
				&& (iRevisionPair.getNextVersion() != null
						&& !iRevisionPair.getNextVersion().toString().trim().isEmpty());

	}

	public boolean isRemove(IRevisionPair iRevisionPair) {
		return (iRevisionPair.getNextVersion() == null || iRevisionPair.getNextVersion().toString().trim().isEmpty());

	}
}
