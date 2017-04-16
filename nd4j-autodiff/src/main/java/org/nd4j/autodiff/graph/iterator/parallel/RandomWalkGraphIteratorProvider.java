package org.nd4j.autodiff.graph.iterator.parallel;

import org.nd4j.autodiff.graph.api.IGraph;
import org.nd4j.autodiff.graph.api.NoEdgeHandling;
import org.nd4j.autodiff.graph.iterator.GraphWalkIterator;
import org.nd4j.autodiff.graph.iterator.RandomWalkIterator;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**Random walk graph iterator provider: given a graph, split up the generation of random walks
 * for parallel learning. Specifically: with N threads and V vertices:
 * - First iterator generates random walks starting at vertices 0 to V/N
 * - Second iterator generates random walks starting at vertices V/N+1 to 2*V/N
 * - and so on
 * @param <V> Vertex type
 */
public class RandomWalkGraphIteratorProvider<V> implements GraphWalkIteratorProvider<V> {

    private IGraph<V, ?> graph;
    private int walkLength;
    private Random rng;
    private NoEdgeHandling mode;

    public RandomWalkGraphIteratorProvider(IGraph<V, ?> graph, int walkLength) {
        this(graph, walkLength, System.currentTimeMillis(), NoEdgeHandling.EXCEPTION_ON_DISCONNECTED);
    }

    public RandomWalkGraphIteratorProvider(IGraph<V, ?> graph, int walkLength, long seed, NoEdgeHandling mode) {
        this.graph = graph;
        this.walkLength = walkLength;
        this.rng = new Random(seed);
        this.mode = mode;
    }


    @Override
    public List<GraphWalkIterator<V>> getGraphWalkIterators(int numIterators) {
        int nVertices = graph.numVertices();
        if (numIterators > nVertices)
            numIterators = nVertices;

        int verticesPerIter = nVertices / numIterators;

        List<GraphWalkIterator<V>> list = new ArrayList<>(numIterators);
        int last = 0;
        for (int i = 0; i < numIterators; i++) {
            int from = last;
            int to = Math.min(nVertices, from + verticesPerIter);
            if (i == numIterators - 1)
                to = nVertices;

            GraphWalkIterator<V> iter = new RandomWalkIterator<>(graph, walkLength, rng.nextLong(), mode, from, to);
            list.add(iter);
            last = to;
        }

        return list;
    }
}