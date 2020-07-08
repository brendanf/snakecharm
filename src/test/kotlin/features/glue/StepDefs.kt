package features.glue

import com.intellij.codeInspection.LocalInspectionEP
import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.util.Computable
import com.intellij.openapi.util.Disposer
import com.intellij.testFramework.TestApplicationManager
import com.intellij.testFramework.UsefulTestCase
import com.intellij.testFramework.fixtures.IdeaTestFixtureFactory
import com.intellij.testFramework.fixtures.InjectionTestFixture
import com.intellij.testFramework.fixtures.impl.LightTempDirTestFixtureImpl
import com.jetbrains.python.codeInsight.controlflow.ControlFlowCache
import com.jetbrains.python.fixtures.PyLightProjectDescriptor
import com.jetbrains.python.psi.PyFile
import com.jetbrains.snakecharm.SnakemakeTestCase
import com.jetbrains.snakecharm.SnakemakeTestUtil
import io.cucumber.java.en.Given
import javax.swing.SwingUtilities
import kotlin.test.fail


/**
 * @author Roman.Chernyatchik
 * @date 2019-04-28
 */
class StepDefs {
    @Given("^a (snakemake|python)? project$")
    @Throws(Exception::class)
    fun configureSnakemakeProject(projectType: String) {
        require(SnakemakeWorld.myFixture == null) {
            "fixture must be null here, looks like cleanup after prev test failed."
        }

        TestApplicationManager.getInstance()

        // From UsefulTestCase
        Disposer.setDebugMode(true)

        SnakemakeWorld.myTestRootDisposable = TestDisposable()
        SnakemakeWorld.myFoundRefs

        // XXX: Seems don't need to enable them, enabled via fixture.enableInspection()
        //if (enabledInspections != null) {
        //    InspectionProfileImpl.INIT_INSPECTIONS = true
        //}
        val additionalRoots = if (projectType == "snakemake") {
            arrayOf(SnakemakeTestUtil.getTestDataPath().resolve("MockPackages3"))
        } else {
            emptyArray()
        }

        // Write code here that turns the phrase above into concrete actions
        val projectDescriptor = PyLightProjectDescriptor(
            SnakemakeTestCase.PYTHON_3_MOCK_SDK,
            SnakemakeTestUtil.getTestDataPath().toString(),
            *additionalRoots
        )

        val factory = IdeaTestFixtureFactory.getFixtureFactory()
        val fixtureBuilder = factory.createLightFixtureBuilder(projectDescriptor)
        val tmpDirFixture = LightTempDirTestFixtureImpl(true) // "tmp://" dir by default

        SnakemakeWorld.myFixture = factory.createCodeInsightFixture(
            fixtureBuilder.fixture, tmpDirFixture
        ).apply {
            testDataPath = SnakemakeTestUtil.getTestDataPath().toString()

            if (SwingUtilities.isEventDispatchThread()) {
                setUp()
            } else {
                ApplicationManager.getApplication().invokeAndWait {
                    try {
                        setUp()
                    } catch (e: java.lang.Exception) {
                        throw RuntimeException("Error running setup", e)
                    }
                }
            }
        }
        SnakemakeWorld.myInjectionFixture = InjectionTestFixture(SnakemakeWorld.fixture())
    }

    @Given("^I expect controlflow")
    fun iexpectControlflow(expectedCFG: String) {
        val actualCFG = ApplicationManager.getApplication().runReadAction(Computable<String> {
            val flow = ControlFlowCache.getControlFlow(SnakemakeWorld.fixture().file as PyFile)
            flow.instructions.joinToString(separator = "\n")
        })
        UsefulTestCase.assertSameLines(expectedCFG.replace("\r", "").trim(), actualCFG.trim())
    }

    @Given("^([^\\]]+) inspection is enabled$")
    fun inspectionIsEnabled(inspectionName: String) {
        val fixture = SnakemakeWorld.fixture()

        for (provider in LocalInspectionEP.LOCAL_INSPECTION.extensionList) {
            val o = provider.instance
            if (o is LocalInspectionTool && inspectionName == o.shortName) {
                fixture.enableInspections(o)
                return
            }
        }
        fail("Unknown inspection:$inspectionName")
    }

    @Given("^TODO")
    fun todo() {
        TODO()
    }
}