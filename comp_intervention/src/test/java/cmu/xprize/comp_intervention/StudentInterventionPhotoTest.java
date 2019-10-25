package cmu.xprize.comp_intervention;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

import cmu.xprize.comp_intervention.data.CInterventionStudentData;
import cmu.xprize.comp_intervention.data.Student;

import static org.junit.Assert.*;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class StudentInterventionPhotoTest {

    private static final String[] SAMPLE_CSV_LINES = {
            //id,student,group,math,story,lit,bpop,spell,picmatch,akira,write,numcompare
            "id1,aubrey,g1,12,0,12,Y,Y,N,N,N,N",
            "id2,chudi,g1,10,0,10,Y,Y,N,N,N,N",
            "id3,michah,g1,8,0,8,Y,N,N,N,N,Y",
            "id4,jacob,g2,6,0,6,N,Y,N,Y,N,N",
            "id5,joshua,g2,4,0,4,N,N,N,N,Y,N",
            "id6,john,g2,2,0,2,N,N,N,N,N,N"
    };

    @Before
    public void setUp() {
        List<Student> testStudents = new ArrayList<>();

        for (String line : SAMPLE_CSV_LINES) {
            testStudents.add(new Student(line.split(",")));
        }

        CInterventionStudentData.initialize(testStudents);
    }
    @Test
    public void addition_isCorrect() throws Exception {
        assertEquals(4, 2 + 2);
    }

    @Test
    public void testInterventionKnowledgeSupport() {

        runKnowledgeSupportTest("id2", "MATH", 10, "aubrey");
        runKnowledgeSupportTest("id1", "LIT", 12, "chudi");

        runKnowledgeSupportTest("id5", "MATH", 4, "jacob");
        runKnowledgeSupportTest("id4", "LIT", 6, "joshua");
    }

    @Test
    public void testInterventionAppSupport() {
        runApplicationSupportTest("id1", "NUMCOMPARE", "michah");
        runApplicationSupportTest("id2", "SPELL", "aubrey");

        runApplicationSupportTest("id4", "WRITE", "joshua");
        runApplicationSupportTest("id6", "BPOP", "jacob");
    }


    /**
     * Run a test for kno
     * @param currentStudentId
     * @param domain
     * @param studentLevel
     * @param expectedStudentPhoto
     */
    private void runKnowledgeSupportTest(String currentStudentId, String domain,
                                         int studentLevel, String expectedStudentPhoto) {

        CInterventionStudentData.setCurrentStudentId(currentStudentId);
        String photo = CInterventionStudentData.getPhotoForKnowledgeSupport(domain, studentLevel);
        assertEquals(photo, expectedStudentPhoto);
    }


    private void runApplicationSupportTest(String currentStudentId, String app,
                                           String expectedStudentPhoto) {
        CInterventionStudentData.setCurrentStudentId(currentStudentId);
        String photo = CInterventionStudentData.getPhotoForApplicationSupport(app);
        assertEquals(photo, expectedStudentPhoto);
    }
}