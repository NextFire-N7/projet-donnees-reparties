package linda.test;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import linda.Linda;
import linda.shm.CentralizedLinda;

public class CentralizedLindaTest {

    Linda linda;

    @BeforeEach
    void setUp() {
        linda = new CentralizedLinda();
    }

    @Test
    void testWrite() {

    }

    @Test
    void testTake() {

    }

    @Test
    void testRead() {

    }

    @Test
    void testTryTake() {

    }

    @Test
    void testTryRead() {

    }

    @Test
    void testTakeAll() {

    }

    @Test
    void testReadAll() {

    }

    @Test
    void testEventRegister() {

    }

}
