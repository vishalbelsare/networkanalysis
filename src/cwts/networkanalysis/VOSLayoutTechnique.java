package cwts.networkanalysis;

/**
 * VOSLayoutTechnique
 *
 * @author Ludo Waltman
 * @author Nees Jan van Eck
 */

import java.util.Arrays;
import java.util.Random;

import cwts.util.FastMath;

public class VOSLayoutTechnique
{
    protected Network network;
    protected Layout layout;
    protected int attraction;
    protected int repulsion;
    protected double edgeWeightIncrement;

    public VOSLayoutTechnique(Network network, int attraction, int repulsion, double edgeWeightIncrement)
    {
        this(network, attraction, repulsion, edgeWeightIncrement, new Random());
    }

    public VOSLayoutTechnique(Network network, int attraction, int repulsion, double edgeWeightIncrement, Random random)
    {
        this.network = network;
        layout = new Layout(network.nNodes);
        layout.initRandomCoordinates(random);
        this.attraction = attraction;
        this.repulsion = repulsion;
        this.edgeWeightIncrement = edgeWeightIncrement;
    }

    public VOSLayoutTechnique(Network network, Layout layout, int attraction, int repulsion, double edgeWeightIncrement)
    {
        this.network = network;
        this.layout = layout;
        this.attraction = attraction;
        this.repulsion = repulsion;
        this.edgeWeightIncrement = edgeWeightIncrement;
    }

    public Network getNetwork()
    {
        return network;
    }

    public Layout getLayout()
    {
        return layout;
    }

    public int getAttraction()
    {
        return attraction;
    }

    public int getRepulsion()
    {
        return repulsion;
    }

    public double getEdgeWeightIncrement()
    {
        return edgeWeightIncrement;
    }

    public void setNetwork(Network network)
    {
        this.network = network;
    }

    public void setLayout(Layout layout)
    {
        this.layout = layout;
    }

    public void setAttraction(int attraction)
    {
        this.attraction = attraction;
    }

    public void setRepulsion(int repulsion)
    {
        this.repulsion = repulsion;
    }

    public void setEdgeWeightIncrement(double edgeWeightIncrement)
    {
        this.edgeWeightIncrement = edgeWeightIncrement;
    }

    public double calcQualityFunction()
    {
        double distance, distance1, distance2, qualityFunction;
        int i, j;

        qualityFunction = 0;

        for (i = 0; i < network.nNodes; i++)
            for (j = network.firstNeighborIndices[i]; j < network.firstNeighborIndices[i + 1]; j++)
                if (network.neighbors[j] < i)
                {
                    distance1 = layout.coordinates[0][i] - layout.coordinates[0][network.neighbors[j]];
                    distance2 = layout.coordinates[1][i] - layout.coordinates[1][network.neighbors[j]];
                    distance = Math.sqrt(distance1 * distance1 + distance2 * distance2);
                    if (attraction != 0)
                        qualityFunction += network.edgeWeights[j] * FastMath.fastPow(distance, attraction) / attraction;
                    else
                        qualityFunction += network.edgeWeights[j] * Math.log(distance);
                }

        for (i = 0; i < network.nNodes; i++)
            for (j = 0; j < i; j++)
            {
                distance1 = layout.coordinates[0][i] - layout.coordinates[0][j];
                distance2 = layout.coordinates[1][i] - layout.coordinates[1][j];
                distance = Math.sqrt(distance1 * distance1 + distance2 * distance2);
                if (repulsion != 0)
                    qualityFunction -= network.nodeWeights[i] * network.nodeWeights[j] * FastMath.fastPow(distance, repulsion) / repulsion;
                else
                    qualityFunction -= network.nodeWeights[i] * network.nodeWeights[j] * Math.log(distance);
            }

        if (edgeWeightIncrement > 0)
            for (i = 0; i < network.nNodes; i++)
                for (j = 0; j < i; j++)
                {
                    distance1 = layout.coordinates[0][i] - layout.coordinates[0][j];
                    distance2 = layout.coordinates[1][i] - layout.coordinates[1][j];
                    distance = Math.sqrt(distance1 * distance1 + distance2 * distance2);
                    if (attraction != 0)
                        qualityFunction += edgeWeightIncrement * FastMath.fastPow(distance, attraction) / attraction;
                    else
                        qualityFunction += edgeWeightIncrement * Math.log(distance);
                }

        return qualityFunction;
    }

    public double runGradientDescentAlgorithm(int maxNIterations, double initialStepLength, double minStepLength, double stepLengthReduction, int requiredNQualityFunctionImprovements)
    {
        return runGradientDescentAlgorithm(maxNIterations, initialStepLength, minStepLength, stepLengthReduction, requiredNQualityFunctionImprovements, new Random());
    }

    public double runGradientDescentAlgorithm(int maxNIterations, double initialStepLength, double minStepLength, double stepLengthReduction, int requiredNQualityFunctionImprovements, Random random)
    {
        boolean[] nodeVisited;
        double a, b, distance, distance1, distance2, gradient1, gradient2, gradientLength, qualityFunction, qualityFunctionOld, squaredDistance, stepLength;
        int i, j, k, l, nQualityFunctionImprovements;
        int[] nodePermutation;

        nodePermutation = cwts.util.Arrays.generateRandomPermutation(network.nNodes, random);

        stepLength = initialStepLength;
        qualityFunction = Double.POSITIVE_INFINITY;
        nQualityFunctionImprovements = 0;
        nodeVisited = new boolean[network.nNodes];
        i = 0;
        while ((i < maxNIterations) && (stepLength >= minStepLength))
        {
            qualityFunctionOld = qualityFunction;
            qualityFunction = 0;
            Arrays.fill(nodeVisited, false);
            for (j = 0; j < network.nNodes; j++)
            {
                k = nodePermutation[j];

                gradient1 = 0;
                gradient2 = 0;

                for (l = network.firstNeighborIndices[k]; l < network.firstNeighborIndices[k + 1]; l++)
                {
                    distance1 = layout.coordinates[0][k] - layout.coordinates[0][network.neighbors[l]];
                    distance2 = layout.coordinates[1][k] - layout.coordinates[1][network.neighbors[l]];
                    squaredDistance = distance1 * distance1 + distance2 * distance2;

                    distance = Math.sqrt(squaredDistance);
                    a = FastMath.fastPow(distance, attraction);

                    if (squaredDistance > 0)
                    {
                        b = network.edgeWeights[l] * a / squaredDistance;
                        gradient1 += b * distance1;
                        gradient2 += b * distance2;
                    }

                    if (!nodeVisited[network.neighbors[l]])
                        if (attraction != 0)
                            qualityFunction += network.edgeWeights[l] * a / attraction;
                        else
                            qualityFunction += network.edgeWeights[l] * Math.log(distance);
                }

                for (l = 0; l < network.nNodes; l++)
                    if (l != k)
                    {
                        distance1 = layout.coordinates[0][k] - layout.coordinates[0][l];
                        distance2 = layout.coordinates[1][k] - layout.coordinates[1][l];
                        squaredDistance = distance1 * distance1 + distance2 * distance2;
                        distance = Math.sqrt(squaredDistance);
                        a = FastMath.fastPow(distance, repulsion);

                        if (squaredDistance > 0)
                        {
                            b = network.nodeWeights[k] * network.nodeWeights[l] * a / squaredDistance;
                            gradient1 -= b * distance1;
                            gradient2 -= b * distance2;
                        }

                        if (!nodeVisited[l])
                            if (repulsion != 0)
                                qualityFunction -= network.nodeWeights[k] * network.nodeWeights[l] * a / repulsion;
                            else
                                qualityFunction -= network.nodeWeights[k] * network.nodeWeights[l] * Math.log(distance);
                    }

                if (edgeWeightIncrement > 0)
                    for (l = 0; l < network.nNodes; l++)
                        if (l != k)
                        {
                            distance1 = layout.coordinates[0][k] - layout.coordinates[0][l];
                            distance2 = layout.coordinates[1][k] - layout.coordinates[1][l];
                            squaredDistance = distance1 * distance1 + distance2 * distance2;
                            distance = Math.sqrt(squaredDistance);
                            a = FastMath.fastPow(distance, attraction);

                            if (squaredDistance > 0)
                            {
                                b = edgeWeightIncrement * a / squaredDistance;
                                gradient1 += b * distance1;
                                gradient2 += b * distance2;
                            }

                            if (!nodeVisited[l])
                                if (attraction != 0)
                                    qualityFunction += edgeWeightIncrement * a / attraction;
                                else
                                    qualityFunction += edgeWeightIncrement * Math.log(distance);
                        }

                gradientLength = Math.sqrt(gradient1 * gradient1 + gradient2 * gradient2);
                layout.coordinates[0][k] -= stepLength * gradient1 / gradientLength;
                layout.coordinates[1][k] -= stepLength * gradient2 / gradientLength;

                nodeVisited[k] = true;
            }

            if (qualityFunction < qualityFunctionOld)
            {
                nQualityFunctionImprovements++;
                if (nQualityFunctionImprovements >= requiredNQualityFunctionImprovements)
                {
                    stepLength /= stepLengthReduction;
                    nQualityFunctionImprovements = 0;
                }
            }
            else
            {
                stepLength *= stepLengthReduction;
                nQualityFunctionImprovements = 0;
            }

            i++;
        }

        return stepLength;
    }
}
