package test;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;

import org.junit.jupiter.api.Test;

import com.camoga.ant.Worker;

class AntTest {

	int[][] tests = { { 2, 104, 2, 2 }, { 4, 18, 1, 1 }, { 8, 52, 2, 2 }, { 10, 104, 2, 2 }, { 11, 384, 4, 4 },
			{ 16, 68, 2, 2 }, {27, 0, 0, 0}, { 32, 84, 2, 2 }, { 35, 896, 4, 4 }, { 36, 18, 1, 1 }, { 42, 104, 2, 2 },
			{ 47, 988, 4, 4 }, { 56, 38, 1, 1 }, { 64, 100, 2, 2 }, { 71, 268, 2, 2 }, { 75, 296, 2, 2 },
			{ 76, 204, 2, 2 }, { 77, 168, 2, 2 }, { 83, 42, 1, 1 }, { 91, 18, 1, 1 }, {31819, 34911892, 4368, 4368} };

	@Test
	void testRunRule() {
		Worker w = new Worker(0, 0);
		for(int i = 0; i < tests.length; i++) {		
			long[] res = w.runRule(tests[i][0], (long) 1e9);
			System.out.println(Arrays.toString(res));
			assertEquals(tests[i][1], res[1]);
			assertEquals(tests[i][2], res[3]);
			assertEquals(tests[i][3], res[4]);
		}
	}

}
