package edu.illinois.nondex.it;

import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import java.util.Map;
import java.util.Set;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

public class AppTest 
{
 

    @Test
    public void testHashSet() {
        Set s = new HashSet();

        for (int i  = 0; i<1000000; i++)

            s.add(i);

        for (int i = 10; i<1000000; i++)
            s.remove(i);

        Iterator it = s.iterator();
        it.next();
        it.next();
        it.remove();
        it.next();

        // this is the natural order; 2 should be removed by the iterator remove above
        assertEquals("To Debug", "[0, 2, 3, 4, 5, 6, 7, 8, 9]", s.toString());
    }
}
