package elementaryCycles.de.normalisiert.utils.graphs;

import java.util.List;

/**
 * Testfile for elementary cycle search.
 *
 * @author Frank Meyer
 *
 */
public class CycleDetector {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		String nodes[] = new String[10];
		boolean adjMatrix[][] = new boolean[10][10];

		for (int i = 0; i < 10; i++) {
			nodes[i] = "Node " + i;
		}

		adjMatrix[0][1] = true;
		adjMatrix[1][0] = true;
		adjMatrix[1][2] = true;
		adjMatrix[2][1] = true;
		adjMatrix[2][0] = true;
		adjMatrix[0][2] = true;
		adjMatrix[2][6] = true;
		adjMatrix[6][2] = true;
		adjMatrix[3][4] = true;
		adjMatrix[4][3] = true;
		adjMatrix[4][5] = true;
		adjMatrix[5][4] = true;
		adjMatrix[4][6] = true;
		adjMatrix[6][4] = true;
		adjMatrix[5][3] = true;
		adjMatrix[3][5] = true;
		adjMatrix[2][7] = true;
		adjMatrix[7][2] = true;
		adjMatrix[7][8] = true;
		adjMatrix[8][7] = true;
		adjMatrix[8][6] = true;
		adjMatrix[6][8] = true;
		adjMatrix[6][1] = true;
		adjMatrix[1][6] = true;

		ElementaryCyclesSearch ecs = new ElementaryCyclesSearch(adjMatrix, nodes);
		List cycles = ecs.getElementaryCycles();
		for (int i = 0; i < cycles.size(); i++) {
			List cycle = (List) cycles.get(i);
			for (int j = 0; j < cycle.size(); j++) {
				String node = (String) cycle.get(j);
				if (j < cycle.size() - 1) {
					System.out.print(node + " -> ");
				} else {
					System.out.print(node);
				}
			}
			System.out.print("\n");
		}
	}

}
