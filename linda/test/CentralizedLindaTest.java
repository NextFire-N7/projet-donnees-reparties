package linda.test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import linda.Linda;
import linda.Tuple;
import linda.shm.CentralizedLinda;

public class CentralizedLindaTest {

    Linda linda;

    @BeforeEach
    void setUp() {
        linda = new CentralizedLinda();
        linda.write(new Tuple("42", 42));
    }

    @Test
    @Timeout(1)
    void testTakeImmediate() {
        Tuple o = linda.take(new Tuple("42", Integer.class));
        assertNotNull(o);
    }

    @Test
    void testTakeBlocked() {
        Thread t = new Thread() {
            @Override
            public void run() {
                linda.take(new Tuple("42", Boolean.class));
            }
        };
        t.start();
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        assertFalse(t.getState() == Thread.State.TERMINATED);
    }

    @Test
    @Timeout(5)
    void testTakeUnblocked() {
        Thread t = new Thread() {
            @Override
            public void run() {
                linda.take(new Tuple("42", Boolean.class));
            }
        };
        t.start();
        linda.write(new Tuple("42", false));
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        assertTrue(t.getState() == Thread.State.TERMINATED);
    }

    @Test
    void testWrite() {
        this.testTakeUnblocked();
        // C'est un test mutuellement récursif.
    }

    @Test
    void testReadNone() {
        Tuple t = linda.read(new Tuple(Boolean.class, Boolean.class));
        assertNull(t);
    }

    @Test
    void testReadSomething() {
        Tuple t = linda.read(new Tuple("42", Integer.class));
        assertNull(t);
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
