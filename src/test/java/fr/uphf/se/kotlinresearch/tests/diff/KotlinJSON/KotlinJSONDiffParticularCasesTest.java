package fr.uphf.se.kotlinresearch.tests.diff.KotlinJSON;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.Ignore;
import org.junit.Test;

import com.github.gumtreediff.actions.model.Action;
import com.github.gumtreediff.tree.ITree;
import com.github.gumtreediff.tree.TreeContext;

import fr.uphf.ast.ASTNode;
import fr.uphf.se.kotlinresearch.core.ASTConverted;
import fr.uphf.se.kotlinresearch.diff.analyzers.SingleDiff;

/**
 * 
 * @author Matias Martinez
 *
 */
public class KotlinJSONDiffParticularCasesTest {
	TreeContext context = new TreeContext();

	public SingleDiff run(String paths, String patht) throws IOException {

		// File.separator + "tmp" + File.separator + "teste.kt";

		Path p1 = Paths.get(paths);
		String fileContent1 = new String(Files.readAllBytes(p1));

		Path p2 = Paths.get(patht);
		String fileContent2 = new String(Files.readAllBytes(p2));

		ASTNode root1 = fr.uphf.analyze.Helper.getASTasJson(fileContent1, p1.getFileName().toString());

		ASTNode root2 = fr.uphf.analyze.Helper.getASTasJson(fileContent2, p2.getFileName().toString());

		// System.out.println("\n----AST S:\n " + t.getStringAST());
		// System.out.println("\n----AST t:\n " + t2.getStringAST());

		ITree trees = getTree(root1);
		ITree treet = getTree(root2);

		// System.out.println("\n----Tree S:\n " + trees.toPrettyString(context));
		// System.out.println("\n----Tree t:\n " + treet.toPrettyString(context));

		assertTrue(trees.getHeight() > 1);
		assertTrue(treet.getHeight() > 1);

		String ast1 = fr.uphf.analyze.Helper.getASTasStringJson(root1);
		String ast2 = fr.uphf.analyze.Helper.getASTasStringJson(root2);

		SingleDiff diff = new SingleDiff(this.context, trees, treet);
		assertTrue(diff.getAllOperations().size() > 0);

		assertTrue(diff.getRootOperations().size() > 0);
		int i = 0;
		for (Action ai : diff.getRootOperations()) {
			try {
				i++;
				System.out.println("--> " + i);
				//
				System.out.println("Info: " + ai.getName() + " " + ai.getNode());
				//
				System.out.println("--> " + i + "/ " + diff.getRootOperations().size() + " " + ai);

			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		// assertEquals(1, diff.getRootOperations().size());
		return diff;
	}

	public ITree getTree(ASTNode astNode) {

		ITree tree = ASTConverted.getRootTree(context, astNode);

		assertNotNull(tree);
		return tree;
	}

	@Test
	public void testImportAndParameter() throws IOException {

		/* A new import added and one argument has its value changed */

		String paths = "./src/test/resources/kotlin_real_cases/case1/MainActivity_s.kt";
		String patht = "./src/test/resources/kotlin_real_cases/case1/MainActivity_t.kt";

		SingleDiff diff = run(paths, patht);

		showChanges(diff);

		assertTrue(diff.getRootOperations().size() > 0);
		assertEquals(2, diff.getRootOperations().size());

	}

	@Test
	public void testModifiersRemoved() throws IOException {

		/* Four modifiers public removed */

		String paths = "./src/test/resources/kotlin_real_cases/case2/ApplicationComponent_s.kt";
		String patht = "./src/test/resources/kotlin_real_cases/case2/ApplicationComponent_t.kt";

		SingleDiff diff = run(paths, patht);

		showChanges(diff);

		assertTrue(diff.getRootOperations().size() > 0);
		assertEquals(4, diff.getRootOperations().size());

	}

	@Test
	public void testChangingVarToVal() throws IOException {

		/* Making a property be read-only */

		String paths = "./src/test/resources/kotlin_real_cases/case3/KotlinActivity_s.kt";
		String patht = "./src/test/resources/kotlin_real_cases/case3/KotlinActivity_t.kt";

		SingleDiff diff = run(paths, patht);

		showChanges(diff);

		assertTrue(diff.getRootOperations().size() > 0);
		assertEquals(1, diff.getRootOperations().size());

	}

	@Test
	public void testRenamingClassName() throws IOException {

		/* Class name changed */

		String paths = "./src/test/resources/kotlin_real_cases/case4/OtherActivity_s.kt";
		String patht = "./src/test/resources/kotlin_real_cases/case4/OtherActivity_t.kt";

		SingleDiff diff = run(paths, patht);

		showChanges(diff);

		assertTrue(diff.getRootOperations().size() > 0);
		assertEquals(1, diff.getRootOperations().size());

	}

	@Test
	@Ignore
	public void testChangVariableTypeAndValue() throws IOException {

		/* Class name changed */

		String paths = "./src/test/resources/kotlin_real_cases/case5/Dog_s.kt";
		String patht = "./src/test/resources/kotlin_real_cases/case5/Dog_t.kt";

		SingleDiff diff = run(paths, patht);

		showChanges(diff);

		assertTrue(diff.getRootOperations().size() > 0);
		assertEquals(2, diff.getRootOperations().size());

	}

	@Test
	public void testChangePackageName() throws IOException {

		/* Package name changed */

		String paths = "./src/test/resources/kotlin_real_cases/case6/SimpleApp_s.kt";
		String patht = "./src/test/resources/kotlin_real_cases/case6/SimpleApp_t.kt";

		SingleDiff diff = run(paths, patht);

		showChanges(diff);

		assertTrue(diff.getRootOperations().size() > 0);
		assertEquals(1, diff.getRootOperations().size());

	}

	@Test
	@Ignore
	public void testInsertPackageChangeArgument() throws IOException {

		/* Package name changed */

		String paths = "./src/test/resources/kotlin_real_cases/case7/KotlinExampleActivity_s.kt";
		String patht = "./src/test/resources/kotlin_real_cases/case7/KotlinExampleActivity_t.kt";

		SingleDiff diff = run(paths, patht);

		showChanges(diff);

		assertTrue(diff.getRootOperations().size() > 0);
		assertEquals(2, diff.getRootOperations().size());

	}

	@Test
	public void testChangeFunctionNameAndArgumentValue() throws IOException {

		/*
		 * Renaming function and change argument: Class Literal Expression, changing
		 * target class
		 */

		String paths = "./src/test/resources/kotlin_real_cases/case8/MainActivity_s.kt";
		String patht = "./src/test/resources/kotlin_real_cases/case8/MainActivity_t.kt";

		SingleDiff diff = run(paths, patht);

		showChanges(diff);

		assertTrue(diff.getRootOperations().size() > 0);
		assertEquals(2, diff.getRootOperations().size());

	}

	@Test
	public void testChangeFunctionArgumentOrder() throws IOException {

		/* Changing the order of two arguments in a function call */

		String paths = "./src/test/resources/kotlin_real_cases/case9/ItemRepository_s.kt";
		String patht = "./src/test/resources/kotlin_real_cases/case9/ItemRepository_t.kt";

		SingleDiff diff = run(paths, patht);

		assertTrue(diff.getRootOperations().size() > 0);
		assertEquals(2, diff.getRootOperations().size());

	}

	@Test
	public void testChangeAnnotationNames() throws IOException {

		/* Changing Annotation names */

		String paths = "./src/test/resources/kotlin_real_cases/case10/SimpleAdapter_s.kt";
		String patht = "./src/test/resources/kotlin_real_cases/case10/SimpleAdapter_t.kt";

		SingleDiff diff = run(paths, patht);

		showChanges(diff);

		assertTrue(diff.getRootOperations().size() > 0);
		assertEquals(4, diff.getRootOperations().size());

	}

	@Test
	public void testChangingReturnExpressionComplexStringTemplate() throws IOException {

		/* Changing return expression with complex template string */

		String paths = "./src/test/resources/kotlin_real_cases/case11/helloWorld_s.kt";
		String patht = "./src/test/resources/kotlin_real_cases/case11/helloWorld_t.kt";

		SingleDiff diff = run(paths, patht);

		showChanges(diff);

		assertTrue(diff.getRootOperations().size() > 0);
		assertEquals(9, diff.getRootOperations().size());

	}

	@Test
	public void testChangingReturnExpression() throws IOException {

		/* Changing return expression to return another dot qualified expression */

		String paths = "./src/test/resources/kotlin_real_cases/case12/helloWorld_s.kt";
		String patht = "./src/test/resources/kotlin_real_cases/case12/helloWorld_t.kt";

		SingleDiff diff = run(paths, patht);

		showChanges(diff);

		assertTrue(diff.getRootOperations().size() > 0);
		assertEquals(8, diff.getRootOperations().size());

	}

	@Test
	public void testSpecifyingPropertyType() throws IOException {

		/* Specifying Property Type */

		String paths = "./src/test/resources/kotlin_real_cases/case13/OtherActivity_s.kt";
		String patht = "./src/test/resources/kotlin_real_cases/case13/OtherActivity_t.kt";

		SingleDiff diff = run(paths, patht);

		showChanges(diff);

		assertTrue(diff.getRootOperations().size() > 0);
		assertEquals(3, diff.getRootOperations().size());

	}

	@Test
	public void testChangingBlockForOneExpressionFunc() throws IOException {

		/* Changing block expression for one expression func */

		String paths = "./src/test/resources/kotlin_real_cases/case14/GreetingController_s.kt";
		String patht = "./src/test/resources/kotlin_real_cases/case14/GreetingController_t.kt";

		SingleDiff diff = run(paths, patht);

		showChanges(diff);

		assertTrue(diff.getRootOperations().size() > 0);
		assertEquals(5, diff.getRootOperations().size());

	}

	@Test
	public void testChangingArgumentAndAddginArgument() throws IOException {

		/* Adding a empty list of arguments and changing a value of other argument */

		String paths = "./src/test/resources/kotlin_real_cases/case15/KotlinExampleActivity_s.kt";
		String patht = "./src/test/resources/kotlin_real_cases/case15/KotlinExampleActivity_t.kt";

		SingleDiff diff = run(paths, patht);

		showChanges(diff);

		assertTrue(diff.getRootOperations().size() > 0);
		assertEquals(5, diff.getRootOperations().size());

	}

	@Test
	public void testChangingPropertyAndReturnType() throws IOException {

		/* Changing property and return type */

		String paths = "./src/test/resources/kotlin_real_cases/case16/MyExample_s.kt";
		String patht = "./src/test/resources/kotlin_real_cases/case16/MyExample_t.kt";

		SingleDiff diff = run(paths, patht);

		showChanges(diff);

		assertTrue(diff.getRootOperations().size() > 0);
		assertEquals(2, diff.getRootOperations().size());

	}

	private void showChanges(SingleDiff diff) {
		TreeContext context = diff.getContext();

		for (Action action : diff.getRootOperations()) {

			ITree affectedNode = action.getNode();

			System.out.println(action.getName() + "-->" + affectedNode.toPrettyString(context));

		}
	}

}
