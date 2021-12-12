package linda.test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Collection;

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
        // Création d'une instance linda et ajout d'un tuple.
        linda = new CentralizedLinda();
        linda.write(new Tuple("42", 42));
    }

    @Test
    @Timeout(1)
    void testTakeImmediate() {
        // On vérifie que le take passe immédiatement.
        // (grâce au timeout)
        Tuple o = linda.take(new Tuple("42", Integer.class));
        assertNotNull(o);
    }

    @Test
    void testTakeBlocked() {
        // On prend un tuple dans un thread
        Thread t = new Thread() {
            @Override
            public void run() {
                linda.take(new Tuple("42", Boolean.class));
            }
        };
        t.start();

        // On attend que le take ait pu être lancé.
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Finalement, on vérifie qu'il n'a pas eu lieu
        assertFalse(t.getState() == Thread.State.TERMINATED);
    }

    @Test
    @Timeout(5)
    void testTakeUnblocked() {

        // Similaire au test précédent, mais cette fois on vérifie que le take
        // se débloque si on écrit le tuple après le take.
        Thread t = new Thread() {
            @Override
            public void run() {
                linda.take(new Tuple("42", Boolean.class));
            }
        };
        t.start();

        // On écrit un tuple correspondant dans l'instance linda
        linda.write(new Tuple("42", false));

        // On attend que la récupération soit effectuée
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // On s'assure qu'il a été effectué.
        assertTrue(t.getState() == Thread.State.TERMINATED);
    }

    @Test
    void testTakeRemove() {
        // On vérifie qu'un take retire effectivement un objet de l'espace linda
        linda.take(new Tuple("42", Integer.class));

        // On fait la même chose que pour le cas bloquant.
        Thread t = new Thread() {
            @Override
            public void run() {
                // On retire le même tuple que précédemment.
                linda.take(new Tuple("42", Integer.class));
            }
        };
        t.start();

        // On attend que le take ait pu être lancé.
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Finalement, on vérifie qu'il n'a pas pu terminer
        assertFalse(t.getState() == Thread.State.TERMINATED);
    }

    @Test
    void testWrite() {
        this.testTakeUnblocked();
        // On était obligé d'écrire pour pouvoir lire,
        // donc si le test précédent passe, c'est qu'on a bien pu écrire.
    }

    @Test
    @Timeout(1)
    void testReadImmediate() {
        // On vérifie dans un premier temps que le read peut passer immédiatement
        Tuple t = linda.read(new Tuple("42", Integer.class));
        assertNotNull(t);

        // et qu'il laisse le tuple dans l'espace mémoire.
        linda.take(new Tuple("42", Integer.class));
    }

    @Test
    void testReadBlocked() {
        // On lit un tuple dans un thread
        Thread t = new Thread() {
            @Override
            public void run() {
                linda.take(new Tuple("42", Boolean.class));
            }
        };
        t.start();

        // On attend que la lecture ait pu être lancée.
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Finalement, on vérifie qu'elle n'a pas eu lieu
        assertFalse(t.getState() == Thread.State.TERMINATED);
    }

    @Test
    @Timeout(5)
    void testReadUnblocked() {

        // Similaire au test précédent, mais cette fois on vérifie que le take
        // se débloque si on écrit le tuple après le take.
        Thread t = new Thread() {
            @Override
            public void run() {
                linda.read(new Tuple("42", Boolean.class));
            }
        };
        t.start();

        // On écrit un couple correspondant dans l'instance linda
        linda.write(new Tuple("42", false));

        // On attend que la récupération soit effectuée
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // On s'assure que le take a été effectué
        assertTrue(t.getState() == Thread.State.TERMINATED);

        // On vérifie que l'élément n'a pas disparu.
        linda.take(new Tuple("42", Boolean.class));
    }

    @Test
    @Timeout(1)
    void testTryTakePresent() {
        // On vérifie que le tryTake passe immédiatement.
        // (grâce au timeout)
        Tuple t = linda.tryTake(new Tuple("42", Integer.class));

        // On vérifie qu'on obtient bien l'élément
        assertNotNull(t);
    }

    @Test
    @Timeout(1)
    void testTryTakeAbsent() {
        // On vérifie que le tryTake passe immédiatement.
        // (grâce au timeout)
        Tuple t = linda.tryTake(new Tuple(Boolean.class, Integer.class));
        assertNull(t);
    }

    @Test
    void testTryTakeRemove() {
        // On vérifie que tryTake retire un élément de l'espace linda.
        Tuple t = linda.tryTake(new Tuple("42", Integer.class));

        // On fait la même chose que pour le cas absent.
        t = linda.tryTake(new Tuple("42", Integer.class));

        assertNull(t);
    }

    @Test
    @Timeout(1)
    void testTryReadPresent() {
        // On vérifie que le tryRead passe immédiatement.
        // (grâce au timeout)
        Tuple t = linda.tryRead(new Tuple("42", Integer.class));

        // On vérifie qu'on obtient bien l'élément
        assertNotNull(t);
    }

    @Test
    @Timeout(1)
    void testTryReadAbsent() {
        // On vérifie que le tryRead passe immédiatement, même si l'élément est absent.
        // (grâce au timeout)
        Tuple t = linda.tryRead(new Tuple(Boolean.class, Integer.class));
        assertNull(t);
    }

    @Test
    @Timeout(1)
    void testTryReadPreserve() {
        // On lit une fois le tuple dans lespace
        Tuple t = linda.tryRead(new Tuple("42", Integer.class));

        // On vérifie que le tryRead préserve le tuple de l'espace linda.
        t = linda.tryRead(new Tuple("42", Integer.class));

        // On vérifie qu'on obtient bien l'élément
        assertNotNull(t);
    }

    @Test
    void testTakeAllPresentOne() {
        // On ajoute un tuple non compatible
        linda.write(new Tuple(true, false));

        // On récupère le tuple par défaut avec la méthode takeAll
        Collection<Tuple> t = linda.takeAll(new Tuple("42", Integer.class));

        // On vérifie qu'on a bien récupéré uniquement ce qu'on devait.
        assertTrue(t.size() == 1);
        assertTrue(t.contains(new Tuple("42", 42)));
    }

    @Test
    void testTakeAllPresentMultiple() {
        // On ajoute un tuple compatible
        linda.write(new Tuple("Departement", 31));

        // On vérifie qu'on peut récupérer les deux tuples
        Collection<Tuple> t = linda.takeAll(new Tuple(String.class, Integer.class));
        assertTrue(t.size() == 2);
        assertTrue(t.contains(new Tuple("42", 42)));
        assertTrue(t.contains(new Tuple("Departement", 31)));
    }

    @Test
    void testTakeAllAbsent() {
        // On récupère un type de tuple qui n'existe pas.
        Collection<Tuple> t = linda.takeAll(new Tuple("42", Boolean.class));

        assertTrue(t.size() == 0);
    }

    @Test
    void testTakeAllRemove() {
        // On take tous les tuples
        linda.takeAll(new Tuple("42", Integer.class));

        // On vérifie qu'ils sont effectivement retirés de l'espace
        Collection<Tuple> t = linda.takeAll(new Tuple("42", Integer.class));

        assertTrue(t.size() == 0);
    }

    @Test
    @Timeout(1)
    void testTakeAllStable() {
        // On ajoute un tuple non compatible
        linda.write(new Tuple(true, false));

        // On récupère le tuple par défaut avec la méthode takeAll
        linda.takeAll(new Tuple("42", Integer.class));

        // On vérifie que takeAll ne prend que les tuples qui lui correpondent.
        linda.take(new Tuple(Boolean.class, Boolean.class));
    }

    @Test
    void testReadAllPresentOne() {
        // On ajoute un tuple non compatible
        linda.write(new Tuple(true, false));

        // On récupère le tuple par défaut avec la méthode readAll
        Collection<Tuple> t = linda.readAll(new Tuple("42", Integer.class));

        // On vérifie qu'on a bien récupéré uniquement ce qu'on devait.
        assertTrue(t.size() == 1);
        assertTrue(t.contains(new Tuple("42", 42)));
    }

    @Test
    void testReadAllPresentMultiple() {
        // On ajoute un tuple compatible
        linda.write(new Tuple("Departement", 31));

        // On vérifie qu'on peut récupérer les deux tuples
        Collection<Tuple> t = linda.readAll(new Tuple(String.class, Integer.class));
        assertTrue(t.size() == 2);
        assertTrue(t.contains(new Tuple("42", 42)));
        assertTrue(t.contains(new Tuple("Departement", 31)));
    }

    @Test
    void testReadAllAbsent() {
        // On récupère un type de tuple qui n'existe pas.
        Collection<Tuple> t = linda.readAll(new Tuple("42", Boolean.class));

        assertTrue(t.size() == 0);
    }

    @Test
    void testReadAllPreserve() {
        // On read tous les tuples
        linda.readAll(new Tuple("42", Integer.class));

        // On vérifie qu'ils restent présents dans l'espace.
        Collection<Tuple> t = linda.takeAll(new Tuple("42", Integer.class));

        assertTrue(t.size() != 0);
    }

    @Test
    void testEventRegister() {

    }

}
