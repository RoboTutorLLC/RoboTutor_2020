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
            "id6,john,g2,2,0,2,N,N,N,N,N,N",
            "48d86234-bd53-4d8c-b946-0bb1a80929c8,P1_Halima.png,C1_T1,23,0,20,y,n,y,n,n,n",
            "e1003b4d-cba8-4c99-8baf-81dfb8a8bb9b,P1_BarakaD.png,C1_T1,27,0,15,y,y,y,y,y,y",
            "bc713259-7403-40ed-b6b9-02bd0f3ecd32,P1_John.png,C1_T1,28,0,21,y,y,y,y,n,y",
            "c35e90d8-5707-4693-a6be-3a17fbfb251a,P1_Swalehe.png,C1_T1,29,0,23,y,y,y,y,n,y",
            "92cda8ae-a713-42e1-a195-fae9cee66580,P1_Helena.png,C1_T1,31,0,24,y,y,y,y,n,y",
            "5e518c62-fd44-443c-9bbc-40c52b5c8847,P1_Nasra.png,C1_T3,28,0,19,y,n,y,n,n,n",
            "75f8577e-4fb3-4ed2-8453-95f6ff736ea1,P1_Abigail.png,C1_T3,33,0,21,y,y,y,y,y,y",
            "c7783c11-e88d-4052-be10-09560d70f5f0,P1_Mary.png,C1_T3,25,0,20,y,n,y,n,n,y",
            "ef5bdd19-0403-43a1-9b62-051cf6b31e3e,P1_Andrew.png,C1_T3,31,0,22,y,y,y,y,n,y",
            "7b13fbea-bbf2-4ebc-a168-4be24c9cef7e,P1_Makenga.png,C1_T3,32,0,23,y,y,y,y,n,y"
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
     * when we're running in Debug mode, should return a non-null photo
     */
    @Test
    public void testDebugNonNull() {

        CInterventionStudentData.setCurrentStudentId("DEBUG");
        String photoKnowledge = CInterventionStudentData.getPhotoForKnowledgeSupport("MATH", 1);
        assertNotNull(photoKnowledge);

        String photoApp = CInterventionStudentData.getPhotoForApplicationSupport("BPOP");
        assertNotNull(photoApp);

    }

    /**
     * In debug mode, should treat whole file as one group
     */
    @Test
    public void testDebugTopStudent() {
        runKnowledgeSupportTest("DEBUG", "MATH", 10, "aubrey");

    }


    @Test
    public void testJudithField() {
        String photo;
        String testId = "c35e90d8-5707-4693-a6be-3a17fbfb251a";
        CInterventionStudentData.setCurrentStudentId(testId);

        // test multiple times for randomness factor
        photo = CInterventionStudentData.getPhotoForKnowledgeSupport("MATH");
        assertEquals(photo, "P1_Helena.png");

        photo = CInterventionStudentData.getPhotoForKnowledgeSupport("MATH");
        assertEquals(photo, "P1_Helena.png");

        photo = CInterventionStudentData.getPhotoForKnowledgeSupport("MATH");
        assertEquals(photo, "P1_Helena.png");

        // test multiple times for randomness factor
        photo = CInterventionStudentData.getPhotoForKnowledgeSupport("LIT");
        assertEquals(photo, "P1_Helena.png");

        photo = CInterventionStudentData.getPhotoForKnowledgeSupport("LIT");
        assertEquals(photo, "P1_Helena.png");

        photo = CInterventionStudentData.getPhotoForKnowledgeSupport("LIT");
        assertEquals(photo, "P1_Helena.png");
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